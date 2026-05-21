package com.husrt.model;

import java.time.LocalDateTime;

public record RegistroAccesoDocente(
        long idRegistroDocente,
        long idDocente,
        Long idPlan,
        LocalDateTime timestampEntrada,
        LocalDateTime timestampSalida
) {
}
