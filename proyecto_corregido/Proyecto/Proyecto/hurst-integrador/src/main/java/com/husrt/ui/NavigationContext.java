package com.husrt.ui;

import java.util.function.Consumer;

public final class NavigationContext {

    public enum CoordinacionModo {
        /** All tabs: universities, services, students, instructors, plan. */
        COMPLETA,
        /** Student registration and listing only (sidebar Students menu). */
        SOLO_ESTUDIANTES
    }

    private static int coordinacionTabIndex = 0;
    private static CoordinacionModo coordinacionModo = CoordinacionModo.COMPLETA;
    private static Consumer<String> navigator;

    private NavigationContext() {
    }

    public static void setNavigator(Consumer<String> nav) {
        navigator = nav;
    }

    public static void navigate(String viewKey) {
        if (navigator != null) {
            navigator.accept(viewKey);
        }
    }

    public static void setCoordinacionTab(int index) {
        coordinacionTabIndex = Math.max(0, index);
    }

    public static void setCoordinacionModo(CoordinacionModo modo) {
        coordinacionModo = modo != null ? modo : CoordinacionModo.COMPLETA;
    }

    public static int consumeCoordinacionTab() {
        int i = coordinacionTabIndex;
        coordinacionTabIndex = 0;
        return i;
    }

    public static CoordinacionModo consumeCoordinacionModo() {
        CoordinacionModo m = coordinacionModo;
        coordinacionModo = CoordinacionModo.COMPLETA;
        return m;
    }
}
