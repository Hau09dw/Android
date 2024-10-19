package com.example.project_management_g1.MODEL;

public class ClickDebouncer {
    private static final long DEBOUNCE_TIME_MS = 350;
    private static long lastClickTime = 0;
    // khoan cach click cho phep la phai lon hon 300 mili giay
    public static boolean isClickAllowed() {
        long currentTime = System.currentTimeMillis();
        if (currentTime - lastClickTime > DEBOUNCE_TIME_MS) {
            lastClickTime = currentTime;
            return true;
        }
        return false;
    }
}
