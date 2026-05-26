package com.husrt.model;

import java.time.LocalDateTime;

public record Notificacion(
        long idNotificacion,
        long idDocente,
        long idEstudiante,
        String mensaje,
        LocalDateTime fechaEnvio,
        boolean leida
) {
    public Notificacion {
        if (fechaEnvio == null) {
            fechaEnvio = LocalDateTime.now();
        }
    }
}
