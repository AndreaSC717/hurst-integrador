package com.husrt.model;

import java.time.LocalDateTime;

public record UsuarioLoginRow(
        UsuarioSistema usuario,
        int intentosFallidos,
        LocalDateTime bloqueadoHasta
) {
}
