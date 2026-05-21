package com.husrt.model;

public enum Rol {
    ADMINISTRADOR,
    COORDINADOR,
    PORTERIA,
    CONSULTA,
    ESTUDIANTE;

    public static Rol fromDb(String s) {
        return s == null ? null : Rol.valueOf(s.trim());
    }
}
