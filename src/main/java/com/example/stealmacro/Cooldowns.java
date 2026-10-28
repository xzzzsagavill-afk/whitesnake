package com.example.stealmacro;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

public final class Cooldowns {
    private Cooldowns() {}
    private static final Map<String, Long> end = new HashMap<>();

    public static Model.Ability find(String name) {
        for (Model.Ability a : Store.data.abilities) if (a.name != null && a.name.equals(name)) return a;
        return null;
    }
    public static long remaining(Model.Ability a) { return Math.max(0, end.getOrDefault(a.groupKey(), 0L) - System.currentTimeMillis()); }
    public static boolean ready(Model.Ability a) { return remaining(a) <= 0; }
    public static void start(Model.Ability a) {
        if (a.cooldown > 0) end.put(a.groupKey(), System.currentTimeMillis() + (long) (a.cooldown * 1000));
    }
    public static Map<String, Long> active() {
        Map<String, Long> m = new LinkedHashMap<>();
        long now = System.currentTimeMillis();
        for (Map.Entry<String, Long> e : end.entrySet()) if (e.getValue() > now) m.put(e.getKey(), e.getValue() - now);
        return m;
    }
    public static void clear() { end.clear(); }
}
