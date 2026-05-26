package com.husrt.model;

public record UsuarioSistema(
        long idUsuario,
        String nombreUsuario,
        String contrasenaHash,
        Rol rol,
        boolean activo,
        Long idEstudiante
) {
}
