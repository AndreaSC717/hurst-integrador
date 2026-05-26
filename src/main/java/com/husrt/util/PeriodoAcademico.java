package com.husrt.util;

import java.time.LocalDate;

public final class PeriodoAcademico {

    private PeriodoAcademico() {
    }

    /** January–June = 1, July–December = 2 (simple prototype configuration). */
    public static int periodoDeFecha(LocalDate d) {
        return d.getMonthValue() <= 6 ? 1 : 2;
    }

    public static int anioDeFecha(LocalDate d) {
        return d.getYear();
    }
}
