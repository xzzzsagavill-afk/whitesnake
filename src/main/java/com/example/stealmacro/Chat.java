package com.example.stealmacro;

import java.util.ArrayDeque;
import java.util.Deque;

/** Лог последних сообщений чата и экшн-бара (для условия "текст в чате"). */
public final class Chat {
    private Chat() {}
    private record Msg(long t, String s) {}
    private static final Deque<Msg> log = new ArrayDeque<>();

    public static void add(String s) {
        log.add(new Msg(System.currentTimeMillis(), s.toLowerCase()));
        while (log.size() > 100) log.poll();
    }
    public static boolean seen(String text, long since) {
        if (text == null || text.isEmpty()) return false;
        String q = text.toLowerCase();
        for (Msg m : log) if (m.t >= since && m.s.contains(q)) return true;
        return false;
    }
}
