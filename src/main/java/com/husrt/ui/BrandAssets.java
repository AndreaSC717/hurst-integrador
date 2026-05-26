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

    /** Carga el logo; el ancho se define en el ImageView (fitWidth). */
    public static Image hospitalLogo(double ignoredFitHint) {
        return hospitalLogo();
    }
}
