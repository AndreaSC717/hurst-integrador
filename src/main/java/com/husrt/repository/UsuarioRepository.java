package com.husrt.repository;

import com.husrt.db.DataSourceManager;
import com.husrt.model.Rol;
import com.husrt.model.UsuarioLoginRow;
import com.husrt.model.UsuarioSistema;

import javax.sql.DataSource;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class UsuarioRepository {

    private final DataSource ds = DataSourceManager.get();

    public Optional<UsuarioSistema> findByUsername(String username) throws SQLException {
        String sql = """
                SELECT id_usuario, nombre_usuario, contrasena_hash, rol, activo, id_estudiante, id_docente
                FROM usuario_sistema WHERE nombre_usuario = ? AND activo = TRUE
                """;
        try (Connection c = ds.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    return Optional.empty();
                }
                return Optional.of(map(rs));
            }
        }
    }

    public Optional<UsuarioLoginRow> findLoginRow(String username) throws SQLException {
        String sql = """
                SELECT id_usuario, nombre_usuario, contrasena_hash, rol, activo, id_estudiante, id_docente,
                       intentos_fallidos, bloqueado_hasta
                FROM usuario_sistema WHERE nombre_usuario = ?
                """;
        try (Connection c = ds.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    return Optional.empty();
                }
                Timestamp bloqueo = rs.getTimestamp("bloqueado_hasta");
                return Optional.of(new UsuarioLoginRow(
                        map(rs),
                        rs.getInt("intentos_fallidos"),
                        bloqueo != null ? bloqueo.toLocalDateTime() : null));
            }
        }
    }

    public Optional<UsuarioSistema> findById(long id) throws SQLException {
        String sql = """
                SELECT id_usuario, nombre_usuario, contrasena_hash, rol, activo, id_estudiante, id_docente
                FROM usuario_sistema WHERE id_usuario = ?
                """;
        try (Connection c = ds.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    return Optional.empty();
                }
                return Optional.of(map(rs));
            }
        }
    }

    public void resetDemoCredentials(String nombreUsuario, String hash) throws SQLException {
        String sql = """
                UPDATE usuario_sistema
                SET contrasena_hash = ?, activo = TRUE, intentos_fallidos = 0, bloqueado_hasta = NULL
                WHERE nombre_usuario = ?
                """;
        try (Connection c = ds.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, hash);
            ps.setString(2, nombreUsuario);
            ps.executeUpdate();
        }
    }

    public void repairDemoUser(String nombreUsuario, String hash, Rol rol, Long idEstudiante, Long idDocente)
            throws SQLException {
        String sql = """
                UPDATE usuario_sistema
                SET contrasena_hash = ?, rol = ?, activo = TRUE,
                    id_estudiante = ?, id_docente = ?,
                    intentos_fallidos = 0, bloqueado_hasta = NULL
                WHERE nombre_usuario = ?
                """;
        try (Connection c = ds.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            int i = 1;
            ps.setString(i++, hash);
            ps.setString(i++, rol.name());
            if (idEstudiante != null) {
                ps.setLong(i++, idEstudiante);
            } else {
                ps.setNull(i++, Types.BIGINT);
            }
            if (idDocente != null) {
                ps.setLong(i++, idDocente);
            } else {
                ps.setNull(i++, Types.BIGINT);
            }
            ps.setString(i, nombreUsuario);
            ps.executeUpdate();
        }
    }

    public boolean existsUsername(String nombreUsuario) throws SQLException {
        String sql = "SELECT 1 FROM usuario_sistema WHERE nombre_usuario = ? LIMIT 1";
        try (Connection c = ds.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, nombreUsuario);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    public void upsertDemoUser(String nombreUsuario, String hash, Rol rol, Long idEstudiante, Long idDocente)
            throws SQLException {
        String sql = """
                INSERT INTO usuario_sistema (nombre_usuario, contrasena_hash, rol, activo, id_estudiante, id_docente,
                    intentos_fallidos, bloqueado_hasta)
                VALUES (?, ?, ?, TRUE, ?, ?, 0, NULL)
                ON DUPLICATE KEY UPDATE
                    contrasena_hash = VALUES(contrasena_hash),
                    rol = VALUES(rol),
                    activo = TRUE,
                    id_estudiante = VALUES(id_estudiante),
                    id_docente = VALUES(id_docente),
                    intentos_fallidos = 0,
                    bloqueado_hasta = NULL
                """;
        try (Connection c = ds.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            int i = 1;
            ps.setString(i++, nombreUsuario);
            ps.setString(i++, hash);
            ps.setString(i++, rol.name());
            if (idEstudiante != null) {
                ps.setLong(i++, idEstudiante);
            } else {
                ps.setNull(i++, Types.BIGINT);
            }
            if (idDocente != null) {
                ps.setLong(i++, idDocente);
            } else {
                ps.setNull(i++, Types.BIGINT);
            }
            ps.executeUpdate();
        }
    }

    public void insert(String nombreUsuario, String hash, Rol rol, Long idEstudiante) throws SQLException {
        String sql = """
                INSERT INTO usuario_sistema (nombre_usuario, contrasena_hash, rol, activo, id_estudiante)
                VALUES (?,?,?,TRUE,?)
                """;
        try (Connection c = ds.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, nombreUsuario);
            ps.setString(2, hash);
            ps.setString(3, rol.name());
            if (idEstudiante != null) {
                ps.setLong(4, idEstudiante);
            } else {
                ps.setNull(4, Types.BIGINT);
            }
            ps.executeUpdate();
        }
    }

    public boolean existsByEstudiante(long idEstudiante) throws SQLException {
        String sql = "SELECT 1 FROM usuario_sistema WHERE id_estudiante = ? LIMIT 1";
        try (Connection c = ds.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setLong(1, idEstudiante);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    public void updateActivo(long id, boolean activo) throws SQLException {
        String sql = "UPDATE usuario_sistema SET activo = ? WHERE id_usuario = ?";
        try (Connection c = ds.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setBoolean(1, activo);
            ps.setLong(2, id);
            ps.executeUpdate();
        }
    }

    public void updatePassword(long id, String hash) throws SQLException {
        String sql = "UPDATE usuario_sistema SET contrasena_hash = ? WHERE id_usuario = ?";
        try (Connection c = ds.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, hash);
            ps.setLong(2, id);
            ps.executeUpdate();
        }
    }

    public void updateRol(long id, Rol rol) throws SQLException {
        String sql = "UPDATE usuario_sistema SET rol = ? WHERE id_usuario = ?";
        try (Connection c = ds.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, rol.name());
            ps.setLong(2, id);
            ps.executeUpdate();
        }
    }

    public void deleteById(long id) throws SQLException {
        String sql = "DELETE FROM usuario_sistema WHERE id_usuario = ?";
        try (Connection c = ds.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setLong(1, id);
            ps.executeUpdate();
        }
    }

    public void incrementarIntentosFallidos(long id) throws SQLException {
        String sql = "UPDATE usuario_sistema SET intentos_fallidos = intentos_fallidos + 1 WHERE id_usuario = ?";
        try (Connection c = ds.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setLong(1, id);
            ps.executeUpdate();
        }
    }

    public void resetIntentosFallidos(long id) throws SQLException {
        String sql = """
                UPDATE usuario_sistema
                SET intentos_fallidos = 0, bloqueado_hasta = NULL
                WHERE id_usuario = ?
                """;
        try (Connection c = ds.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setLong(1, id);
            ps.executeUpdate();
        }
    }

    public void bloquearHasta(long id, LocalDateTime hasta) throws SQLException {
        String sql = "UPDATE usuario_sistema SET bloqueado_hasta = ? WHERE id_usuario = ?";
        try (Connection c = ds.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setTimestamp(1, Timestamp.valueOf(hasta));
            ps.setLong(2, id);
            ps.executeUpdate();
        }
    }

    public List<UsuarioSistema> findAll() throws SQLException {
        String sql = """
                SELECT id_usuario, nombre_usuario, contrasena_hash, rol, activo, id_estudiante, id_docente
                FROM usuario_sistema ORDER BY nombre_usuario
                """;
        try (Connection c = ds.getConnection();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            List<UsuarioSistema> list = new ArrayList<>();
            while (rs.next()) {
                list.add(map(rs));
            }
            return list;
        }
    }

    private static UsuarioSistema map(ResultSet rs) throws SQLException {
        return new UsuarioSistema(
                rs.getLong("id_usuario"),
                rs.getString("nombre_usuario"),
                rs.getString("contrasena_hash"),
                Rol.fromDb(rs.getString("rol")),
                rs.getBoolean("activo"),
                rs.getObject("id_estudiante", Long.class),
                rs.getObject("id_docente", Long.class));
    }
}
