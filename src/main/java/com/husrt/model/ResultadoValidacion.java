package com.husrt.model;

public enum ResultadoValidacion {
    APROBADO,
    RECHAZADO;

    public String label() {
        return switch (this) {
            case APROBADO -> "Aprobado";
            case RECHAZADO -> "Rechazado";
        };
    }

    public static String labelFromDb(String s) {
        if (s == null || s.isBlank()) {
            return "";
        }
        try {
            return valueOf(s.trim()).label();
        } catch (IllegalArgumentException ex) {
            return s;
        }
    }

    public static ResultadoValidacion fromDb(String s) {
        return s == null ? null : ResultadoValidacion.valueOf(s.trim());
    }
}
