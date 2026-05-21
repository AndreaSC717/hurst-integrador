package com.husrt.model;

import java.time.LocalDateTime;

public record PresenteEstudianteDto(
        String nombre,
        String apellido,
        String cedula,
        String servicio,
        LocalDateTime entrada
) {
}
