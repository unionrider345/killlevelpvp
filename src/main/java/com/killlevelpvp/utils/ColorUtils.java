package com.killlevelpvp.utils;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Utility class for handling color codes and text formatting
 */
public class ColorUtils {

    private static final Pattern HEX_PATTERN = Pattern.compile("&#([A-Fa-f0-9]{6})");

    /**
     * Colorize a string with legacy color codes (&a, &b, etc.) and hex colors (&#RRGGBB)
     */
    public static String colorize(String message) {
        if (message == null) return "";
        
        // Handle hex colors first
        Matcher matcher = HEX_PATTERN.matcher(message);
        StringBuilder buffer = new StringBuilder();
        
        while (matcher.find()) {
            String hex = matcher.group(1);
            // Convert to Bukkit's hex format: §x§R§R§G§G§B§B
            StringBuilder hexBuilder = new StringBuilder("§x");
            for (char c : hex.toCharArray()) {
                hexBuilder.append("§").append(c);
            }
            matcher.appendReplacement(buffer, hexBuilder.toString());
        }
        matcher.appendTail(buffer);
        
        // Replace & with § for legacy colors
        return buffer.toString().replace("&", "§");
    }

    /**
     * Convert a colorized string to an Adventure Component
     */
    public static Component toComponent(String message) {
        if (message == null) return Component.empty();
        return LegacyComponentSerializer.legacySection().deserialize(colorize(message));
    }

    /**
     * Strip all color codes from a string
     */
    public static String stripColor(String message) {
        if (message == null) return "";
        return message.replaceAll("§[0-9a-fk-or]", "").replaceAll("&[0-9a-fk-or]", "");
    }
}
