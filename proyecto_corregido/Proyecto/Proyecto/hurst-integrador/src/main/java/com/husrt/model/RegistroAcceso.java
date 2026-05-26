package com.husrt.model;

import java.time.LocalDateTime;

public record RegistroAcceso(
        long idRegistro,
        long idEstudiante,
        Long idAsignacion,
        LocalDateTime timestampEntrada,
        LocalDateTime timestampSalida,
        Double horasCumplidas,
        ResultadoValidacion resultado,
        String motivoRechazo
) {
}
