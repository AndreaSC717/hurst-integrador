package com.husrt.model;

import java.time.LocalDate;

public record Estudiante(
        long idEstudiante,
        String cedula,
        String nombre,
        String apellido,
        String fotoUrl,
        String programaAcademico,
        int semestreAcademico,
        long idUniversidad,
        boolean induccionCompletada,
        LocalDate fechaInduccion,
        LocalDate arlInicio,
        LocalDate arlFin,
        EstadoEstudiante estado,
        boolean vacunasCompletas
        ) {

    @Override
    public String toString() {
        return nombre + " " + apellido;
    }
}
