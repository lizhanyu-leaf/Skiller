package com.leaf.skiller.util;

public class KeyCooldown {
    private static long lastToggleTime = 0;
    private static final long COOLDOWN_MS = 500;

    public static boolean canToggle() {
        long now = System.currentTimeMillis();
        if (now - lastToggleTime < COOLDOWN_MS) {
            return false;
        }
        lastToggleTime = now;
        return true;
    }
}