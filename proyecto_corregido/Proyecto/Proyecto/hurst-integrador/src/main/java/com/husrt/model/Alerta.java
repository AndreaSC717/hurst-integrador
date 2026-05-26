package com.husrt.model;

import java.time.LocalDateTime;

public record Alerta(
        long idAlerta,
        String tipoAlerta,
        Long idEstudiante,
        Long idDocente,
        String descripcion,
        LocalDateTime timestampGeneracion,
        boolean resuelta
) {
}
