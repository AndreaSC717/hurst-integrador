package com.husrt.ui;

import javafx.animation.FadeTransition;
import javafx.animation.TranslateTransition;
import javafx.scene.Node;
import javafx.util.Duration;

public final class UiAnimations {

    private UiAnimations() {
    }

    public static void fadeIn(Node node, Duration duration) {
        if (node == null) {
            return;
        }
        node.setOpacity(0);
        FadeTransition fade = new FadeTransition(duration, node);
        fade.setFromValue(0);
        fade.setToValue(1);
        fade.play();
    }

    public static void slideFadeIn(Node node) {
        if (node == null) {
            return;
        }
        node.setOpacity(0);
        node.setTranslateY(12);
        FadeTransition fade = new FadeTransition(Duration.millis(320), node);
        fade.setFromValue(0);
        fade.setToValue(1);
        TranslateTransition slide = new TranslateTransition(Duration.millis(320), node);
        slide.setFromY(12);
        slide.setToY(0);
        fade.play();
        slide.play();
    }
}
