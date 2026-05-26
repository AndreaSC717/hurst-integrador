package com.husrt.ui;

import javafx.scene.Parent;
import javafx.scene.Scene;

public final class UiStyles {

    private static final String THEME = "/com/husrt/ui/husrt-theme.css";

    private UiStyles() {
    }

    public static void apply(Scene scene) {
        if (scene != null && !scene.getStylesheets().contains(themeUrl())) {
            scene.getStylesheets().add(themeUrl());
        }
    }

    public static void apply(Parent root) {
        if (root != null && root.getScene() != null) {
            apply(root.getScene());
        }
    }

    public static String themeUrl() {
        return UiStyles.class.getResource(THEME).toExternalForm();
    }
}
