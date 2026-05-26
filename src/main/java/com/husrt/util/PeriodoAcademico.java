package com.husrt.util;

import java.time.LocalDate;

public final class PeriodoAcademico {

    private PeriodoAcademico() {
    }

    /** Enero–junio = 1, julio–diciembre = 2 (configuración simple para el prototipo). */
    public static int periodoDeFecha(LocalDate d) {
        return d.getMonthValue() <= 6 ? 1 : 2;
    }

    public static int anioDeFecha(LocalDate d) {
        return d.getYear();
    }
}
