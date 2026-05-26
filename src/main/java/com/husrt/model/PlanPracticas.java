package com.husrt.model;

import java.time.LocalDate;

public record PlanPracticas(
        long idPlan,
        long idDocente,
        long idUniversidad,
        String semestre,
        int mes,
        int anio,
        int periodo,
        LocalDate fechaCarga
) {
}
