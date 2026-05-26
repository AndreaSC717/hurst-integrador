package com.husrt.ui;

import javafx.scene.image.Image;

import java.util.Objects;

public final class BrandAssets {

    public static final String LOGO_PATH = "/com/husrt/ui/assets/hospital-logo.png";

    private BrandAssets() {
    }

    public static Image hospitalLogo() {
        return new Image(Objects.requireNonNull(
                BrandAssets.class.getResource(LOGO_PATH)).toExternalForm(), true);
    }

    /** Loads the logo; width is set on the ImageView (fitWidth). */
    public static Image hospitalLogo(double ignoredFitHint) {
        return hospitalLogo();
    }
}
