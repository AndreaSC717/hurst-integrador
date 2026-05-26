package com.husrt.model;

public record Docente(
        long idDocente,
        String cedula,
        String nombre,
        String apellido,
        long idUniversidad,
        String programaQueSupervisa
) {
    @Override
    public String toString() {
        return nombre + " " + apellido;
    }
}
