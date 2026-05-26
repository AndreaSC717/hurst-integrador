package com.husrt.model;

public record ServicioHospitalario(long idServicio, String nombre, String piso, int capacidadMaximaEstudiantes) {
    @Override
    public String toString() {
        return nombre;
    }
}
