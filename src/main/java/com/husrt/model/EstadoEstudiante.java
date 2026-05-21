package com.husrt.model;

public enum EstadoEstudiante {
    ACTIVO,
    INACTIVO;

    public static EstadoEstudiante fromDb(String s) {
        return s == null ? null : EstadoEstudiante.valueOf(s.trim());
    }
}
