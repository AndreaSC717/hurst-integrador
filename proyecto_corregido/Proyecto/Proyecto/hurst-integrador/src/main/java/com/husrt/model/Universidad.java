package com.husrt.model;

public record Universidad(long idUniversidad, String nombre, String ciudad, String tipoConvenio) {
    @Override
    public String toString() {
        return nombre;
    }
}
