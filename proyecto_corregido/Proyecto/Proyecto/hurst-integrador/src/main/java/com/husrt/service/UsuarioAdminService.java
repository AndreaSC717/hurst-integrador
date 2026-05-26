package com.husrt.service;

import com.husrt.model.Rol;
import com.husrt.model.UsuarioSistema;
import com.husrt.repository.UsuarioRepository;
import com.husrt.session.SessionContext;

import java.sql.SQLException;

public class UsuarioAdminService {

    private final UsuarioRepository usuarios = new UsuarioRepository();
    private final AuthService auth = new AuthService();
    private final AuditoriaService auditoria = new AuditoriaService();

    public void crearUsuario(String nombreUsuario, String clave, Rol rol, Long idEstudiante) throws SQLException {
        String hash = auth.encodePassword(clave);
        usuarios.insert(nombreUsuario, hash, rol, idEstudiante);
        auditoria.registrar("ADMIN", "CREAR_USUARIO",
                "usuario=" + nombreUsuario + ", rol=" + rol.name(), nombreUsuario);
    }

    public void setActivo(long idUsuario, boolean activo) throws SQLException {
        UsuarioSistema actual = SessionContext.getCurrent();
        if (actual != null && actual.idUsuario() == idUsuario && !activo) {
            throw new IllegalArgumentException("No puede desactivar su propia cuenta.");
        }
        UsuarioSistema target = usuarios.findById(idUsuario)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado."));
        usuarios.updateActivo(idUsuario, activo);
        auditoria.registrar("ADMIN", activo ? "ACTIVAR_USUARIO" : "DESACTIVAR_USUARIO",
                "usuario=" + target.nombreUsuario(), String.valueOf(idUsuario));
    }

    public void restablecerPassword(long idUsuario, String nuevaClave) throws SQLException {
        UsuarioSistema target = usuarios.findById(idUsuario)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado."));
        auth.restablecerPassword(idUsuario, nuevaClave);
        auditoria.registrar("ADMIN", "RESET_CLAVE",
                "usuario=" + target.nombreUsuario(), String.valueOf(idUsuario));
    }

    public void cambiarRol(long idUsuario, Rol nuevoRol) throws SQLException {
        UsuarioSistema actual = SessionContext.getCurrent();
        if (actual != null && actual.idUsuario() == idUsuario) {
            throw new IllegalArgumentException("No puede cambiar su propio rol.");
        }
        UsuarioSistema target = usuarios.findById(idUsuario)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado."));
        usuarios.updateRol(idUsuario, nuevoRol);
        auditoria.registrar("ADMIN", "CAMBIO_ROL",
                "usuario=" + target.nombreUsuario() + ", rol=" + nuevoRol.name(), String.valueOf(idUsuario));
    }

    public void eliminarUsuario(long idUsuario) throws SQLException {
        UsuarioSistema actual = SessionContext.getCurrent();
        if (actual != null && actual.idUsuario() == idUsuario) {
            throw new IllegalArgumentException("No puede eliminar su propia cuenta.");
        }
        UsuarioSistema target = usuarios.findById(idUsuario)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado."));
        usuarios.deleteById(idUsuario);
        auditoria.registrar("ADMIN", "ELIMINAR_USUARIO",
                "usuario=" + target.nombreUsuario(), String.valueOf(idUsuario));
    }
}
