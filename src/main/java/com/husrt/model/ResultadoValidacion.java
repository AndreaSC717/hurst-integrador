package com.husrt.model;

public enum ResultadoValidacion {
    APROBADO,
    RECHAZADO;

    public static ResultadoValidacion fromDb(String s) {
        return s == null ? null : ResultadoValidacion.valueOf(s.trim());
    }
}
