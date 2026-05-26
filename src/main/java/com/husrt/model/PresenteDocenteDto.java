package com.husrt.model;

import java.time.LocalDateTime;

public record PresenteDocenteDto(
        String nombre,
        String apellido,
        String cedula,
        Long idPlan,
        LocalDateTime entrada
) {
}
