package com.nerithmc.system;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class Util {

    private Util() {}

    /** Parses 90, 30s, 10m, 1h, 1h30m, 1d. Returns -1 if invalid. */
    public static long parseSeconds(String input) {
        if (input == null) return -1;
        String s = input.toLowerCase().trim();
        if (s.matches("\\d+")) return Long.parseLong(s);
        if (!s.matches("(\\d+[dhms])+")) return -1;
        Matcher m = Pattern.compile("(\\d+)([dhms])").matcher(s);
        long total = 0;
        while (m.find()) {
            long v = Long.parseLong(m.group(1));
            switch (m.group(2).charAt(0)) {
                case 'd' -> total += v * 86400;
                case 'h' -> total += v * 3600;
                case 'm' -> total += v * 60;
                default -> total += v;
            }
        }
        return total;
    }

    public static String formatTime(long seconds) {
        long h = seconds / 3600;
        long m = (seconds % 3600) / 60;
        long s = seconds % 60;
        return h > 0 ? String.format("%d:%02d:%02d", h, m, s) : String.format("%02d:%02d", m, s);
    }

    public static Component comp(String text) {
        return LegacyComponentSerializer.legacyAmpersand().deserialize(text);
    }

    public static String color(String text) {
        return text.replace('&', '§');
    }
}
