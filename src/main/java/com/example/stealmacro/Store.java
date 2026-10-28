package com.example.stealmacro;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.neoforged.fml.loading.FMLPaths;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;

public final class Store {
    private Store() {}
    public static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    public static Model.Root data = new Model.Root();

    private static Path file() { return FMLPaths.CONFIGDIR.get().resolve("stealmacro.json"); }

    public static void load() {
        Path f = file();
        try {
            if (Files.exists(f)) {
                Model.Root r = GSON.fromJson(Files.readString(f), Model.Root.class);
                if (r != null) data = r;
            }
        } catch (Exception e) {
            System.err.println("[stealmacro] не смог прочитать конфиг, делаю бэкап: " + e);
            try { Files.copy(f, f.resolveSibling("stealmacro.json.bak"), StandardCopyOption.REPLACE_EXISTING); } catch (Exception ignored) {}
            data = new Model.Root();
        }
        if (data.settings == null) data.settings = new Model.Settings();
        if (data.abilities == null) data.abilities = new ArrayList<>();
        if (data.macros == null) data.macros = new ArrayList<>();
        if (!data.seeded) {
            data.seeded = true;
            data.abilities.addAll(Defaults.whiteSnake());
            data.macros.addAll(Defaults.macros());
        }
        save();
    }

    public static void save() {
        try { Files.writeString(file(), GSON.toJson(data)); }
        catch (Exception e) { System.err.println("[stealmacro] не смог сохранить: " + e); }
    }

    public static <T> T copy(T o, Class<T> c) { return GSON.fromJson(GSON.toJson(o), c); }

    /** при переименовании способности обновляем ссылки в макросах */
    public static void renameAbility(String old, String neu) {
        for (Model.Macro m : data.macros)
            for (Model.Step s : m.steps)
                if (old.equals(s.ability)) s.ability = neu;
    }
}
