package com.husrt.service;

import com.husrt.model.UsuarioLoginRow;
import com.husrt.model.UsuarioSistema;
import com.husrt.repository.UsuarioRepository;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

public class AuthService {

    public static final int MAX_INTENTOS_FALLIDOS = 5;
    public static final int MINUTOS_BLOQUEO = 15;

    private UsuarioRepository usuarios;
    private final BCryptPasswordEncoder bcrypt = new BCryptPasswordEncoder();

    private UsuarioRepository usuarios() {
        if (usuarios == null) {
            usuarios = new UsuarioRepository();
        }
        return usuarios;
    }

    public LoginOutcome login(String username, String passwordPlain) throws SQLException {
        String user = username != null ? username.trim() : "";
        if (user.isEmpty()) {
            return new LoginOutcome.Failure("Ingrese usuario y contraseña.");
        }
        Optional<UsuarioLoginRow> rowOp = usuarios().findLoginRow(user);
        if (rowOp.isEmpty()) {
            return new LoginOutcome.Failure("Usuario o contraseña incorrectos.");
        }
        UsuarioLoginRow row = rowOp.get();
        UsuarioSistema cuenta = row.usuario();

        LocalDateTime bloqueo = row.bloqueadoHasta();
        if (bloqueo != null && LocalDateTime.now().isBefore(bloqueo)) {
            return new LoginOutcome.Failure(mensajeBloqueo(bloqueo));
        }

        if (!cuenta.activo()) {
            return new LoginOutcome.Failure("Cuenta desactivada. Contacte al administrador para restablecer el acceso.");
        }

        if (!bcrypt.matches(passwordPlain, cuenta.contrasenaHash())) {
            int intentos = row.intentosFallidos() + 1;
            usuarios().incrementarIntentosFallidos(cuenta.idUsuario());
            if (intentos >= MAX_INTENTOS_FALLIDOS) {
                LocalDateTime hasta = LocalDateTime.now().plusMinutes(MINUTOS_BLOQUEO);
                usuarios().bloquearHasta(cuenta.idUsuario(), hasta);
                return new LoginOutcome.Failure(mensajeBloqueo(hasta)
                        + " Si olvidó su contraseña, contacte al administrador.");
            }
            int restantes = MAX_INTENTOS_FALLIDOS - intentos;
            return new LoginOutcome.Failure("Usuario o contraseña incorrectos. Intentos restantes: " + restantes + ".");
        }

        usuarios().resetIntentosFallidos(cuenta.idUsuario());
        return new LoginOutcome.Success(cuenta);
    }

    public void cambiarPassword(long idUsuario, String claveActual, String claveNueva) throws SQLException {
        if (claveNueva == null || claveNueva.length() < 6) {
            throw new IllegalArgumentException("La nueva contraseña debe tener al menos 6 caracteres.");
        }
        UsuarioSistema u = usuarios().findById(idUsuario)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado."));
        if (!bcrypt.matches(claveActual, u.contrasenaHash())) {
            throw new IllegalArgumentException("La contraseña actual es incorrecta.");
        }
        usuarios().updatePassword(idUsuario, bcrypt.encode(claveNueva));
        usuarios().resetIntentosFallidos(idUsuario);
    }

    public void restablecerPassword(long idUsuario, String claveNueva) throws SQLException {
        if (claveNueva == null || claveNueva.length() < 6) {
            throw new IllegalArgumentException("La contraseña debe tener al menos 6 caracteres.");
        }
        usuarios().updatePassword(idUsuario, bcrypt.encode(claveNueva));
        usuarios().resetIntentosFallidos(idUsuario);
    }

    public String encodePassword(String plain) {
        return bcrypt.encode(plain);
    }

    private static String mensajeBloqueo(LocalDateTime hasta) {
        return "Cuenta bloqueada temporalmente hasta "
                + hasta.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")) + ". ";
    }
}
