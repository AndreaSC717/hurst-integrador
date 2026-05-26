package com.husrt.model;

public enum EstadoEstudiante {
    ACTIVO,
    INACTIVO;

    public String label() {
        return switch (this) {
            case ACTIVO -> "Activo";
            case INACTIVO -> "Inactivo";
        };
    }

    public static EstadoEstudiante fromDb(String s) {
        return s == null ? null : EstadoEstudiante.valueOf(s.trim());
    }
}
