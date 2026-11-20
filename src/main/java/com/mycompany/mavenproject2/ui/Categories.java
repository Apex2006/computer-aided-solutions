package com.mycompany.mavenproject2.ui;

import java.util.Map;

/**
 * How each of the 8 categories looks: its icon, cover color and tagline,
 * as in the Figma design.
 *
 * @author Luis Gabriel Santiago
 */
public final class Categories {

    /** Icon name, cover background color and icon color. */
    public record Style(String icon, String cover, String iconColor, String tagline) {
    }

    private static final Map<String, Style> STYLES = Map.of(
            "FOOD_COOKING", new Style("flame", "#FDEBDD", "#C2410C",
                    "Smarter cooking with timers, thermometers and reminders"),
            "GARDENING", new Style("sprout", "#E3F1E4", "#2E7D32",
                    "Keep plants alive with sensors, pumps and lights"),
            "STUDIES", new Style("book-open", "#E8E6F8", "#4C3FA8",
                    "Study better with timers, trackers and desk gadgets"),
            "HOUSEKEEPING", new Style("house", "#E1F0EC", "#1F6F63",
                    "Chores made easier with simple automation"),
            "ENERGY_SAVING", new Style("zap", "#FCF0C3", "#A15C00",
                    "Save electricity at home with timers, sensors and smart plugs"),
            "HOME_SECURITY", new Style("lock", "#DCE8FB", "#1D4FA8",
                    "Alarms, sensors and cameras for a safer home"),
            "HEALTH_WELLNESS", new Style("heart", "#FBE3EA", "#B4235A",
                    "Monitors and reminders for a healthier routine"),
            "PETS", new Style("paw-print", "#EFEBE3", "#6B5B3E",
                    "Feeders, water alerts and trackers for pets"));

    private Categories() {
    }

    public static Style style(String category) {
        return STYLES.getOrDefault(category, new Style("file-text", "#EEEFEA", "#5E6A5F", ""));
    }
}
