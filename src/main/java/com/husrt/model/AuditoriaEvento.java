package com.husrt.model;

import java.time.LocalDateTime;

public record AuditoriaEvento(
        long idEvento,
        LocalDateTime timestampEvento,
        Long idUsuario,
        String nombreUsuario,
        String rol,
        String modulo,
        String accion,
        String detalle,
        String entidadRef
) {
}
