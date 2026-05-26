package com.husrt.model;

import java.time.LocalDate;
import java.time.LocalTime;

public record AsignacionPractica(
        long idAsignacion,
        long idPlan,
        long idEstudiante,
        long idServicio,
        int diaSemana,
        LocalTime horaInicio,
        LocalTime horaFin,
        LocalDate fechaEspecifica
) {
}
