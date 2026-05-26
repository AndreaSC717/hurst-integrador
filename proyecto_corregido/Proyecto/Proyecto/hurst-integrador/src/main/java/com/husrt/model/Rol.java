package com.husrt.model;

public enum Rol {
    ADMINISTRADOR,
    COORDINADOR,
    DOCENTE,
    PORTERIA,
    CONSULTA,
    ESTUDIANTE;

    public String label() {
        return switch (this) {
            case ADMINISTRADOR -> "Administrador";
            case COORDINADOR -> "Coordinador";
            case DOCENTE -> "Docente";
            case PORTERIA -> "Portería";
            case CONSULTA -> "Consulta";
            case ESTUDIANTE -> "Estudiante";
        };
    }

    public static Rol fromDb(String s) {
        if (s == null || s.isBlank()) {
            return null;
        }
        try {
            return Rol.valueOf(s.trim());
        } catch (IllegalArgumentException ex) {
            return CONSULTA;
        }
    }
}
