package com.husrt.repository;

import com.husrt.db.DataSourceManager;
import com.husrt.model.AuditoriaEvento;

import javax.sql.DataSource;
import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class AuditoriaRepository {

    private final DataSource ds = DataSourceManager.get();

    public void insert(Long idUsuario, String nombreUsuario, String rol,
                       String modulo, String accion, String detalle, String entidadRef) throws SQLException {
        String sql = """
                INSERT INTO auditoria_evento (id_usuario, nombre_usuario, rol, modulo, accion, detalle, entidad_ref)
                VALUES (?,?,?,?,?,?,?)
                """;
        try (Connection c = ds.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            if (idUsuario != null) {
                ps.setLong(1, idUsuario);
            } else {
                ps.setNull(1, Types.BIGINT);
            }
            ps.setString(2, nombreUsuario);
            ps.setString(3, rol);
            ps.setString(4, modulo);
            ps.setString(5, accion);
            ps.setString(6, detalle);
            ps.setString(7, entidadRef);
            ps.executeUpdate();
        }
    }

    public List<AuditoriaEvento> findPorModulo(String modulo, LocalDate desde, LocalDate hasta, int limit) throws SQLException {
        LocalDateTime d0 = desde.atStartOfDay();
        LocalDateTime d1 = hasta.plusDays(1).atStartOfDay();
        String sql = """
                SELECT id_evento, timestamp_evento, id_usuario, nombre_usuario, rol, modulo, accion, detalle, entidad_ref
                FROM auditoria_evento
                WHERE modulo = ? AND timestamp_evento >= ? AND timestamp_evento < ?
                ORDER BY timestamp_evento DESC
                LIMIT ?
                """;
        try (Connection c = ds.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, modulo);
            ps.setTimestamp(2, Timestamp.valueOf(d0));
            ps.setTimestamp(3, Timestamp.valueOf(d1));
            ps.setInt(4, limit);
            try (ResultSet rs = ps.executeQuery()) {
                return mapList(rs);
            }
        }
    }

    public List<AuditoriaEvento> findRecientes(int limit) throws SQLException {
        String sql = """
                SELECT id_evento, timestamp_evento, id_usuario, nombre_usuario, rol, modulo, accion, detalle, entidad_ref
                FROM auditoria_evento
                ORDER BY timestamp_evento DESC
                LIMIT ?
                """;
        try (Connection c = ds.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, limit);
            try (ResultSet rs = ps.executeQuery()) {
                return mapList(rs);
            }
        }
    }

    private static List<AuditoriaEvento> mapList(ResultSet rs) throws SQLException {
        List<AuditoriaEvento> list = new ArrayList<>();
        while (rs.next()) {
            Timestamp ts = rs.getTimestamp("timestamp_evento");
            list.add(new AuditoriaEvento(
                    rs.getLong("id_evento"),
                    ts != null ? ts.toLocalDateTime() : null,
                    rs.getObject("id_usuario", Long.class),
                    rs.getString("nombre_usuario"),
                    rs.getString("rol"),
                    rs.getString("modulo"),
                    rs.getString("accion"),
                    rs.getString("detalle"),
                    rs.getString("entidad_ref")));
        }
        return list;
    }
}
