package com.husrt.repository;

import com.husrt.db.DataSourceManager;

import javax.sql.DataSource;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class AlertaRepository {

    private final DataSource ds = DataSourceManager.get();

    public void insert(String tipo, Long idEstudiante, Long idDocente, String descripcion) throws SQLException {
        String sql = """
                INSERT INTO alerta (tipo_alerta, id_estudiante, id_docente, descripcion, timestamp_generacion, resuelta)
                VALUES (?,?,?,?,NOW(3),FALSE)
                """;
        try (Connection c = ds.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, tipo);
            if (idEstudiante != null) {
                ps.setLong(2, idEstudiante);
            } else {
                ps.setNull(2, java.sql.Types.BIGINT);
            }
            if (idDocente != null) {
                ps.setLong(3, idDocente);
            } else {
                ps.setNull(3, java.sql.Types.BIGINT);
            }
            ps.setString(4, descripcion);
            ps.executeUpdate();
        }
    }

    public record AlertaRow(LocalDateTime ts, String tipo, String descripcion) {
    }

    public int countNoResueltas() throws SQLException {
        String sql = "SELECT COUNT(*) FROM alerta WHERE resuelta = FALSE";
        try (Connection c = ds.getConnection();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            rs.next();
            return rs.getInt(1);
        }
    }

    public List<AlertaRow> listRecientes(int limit) throws SQLException {
        String sql = """
                SELECT timestamp_generacion, tipo_alerta, descripcion
                FROM alerta
                ORDER BY timestamp_generacion DESC
                LIMIT ?
                """;
        try (Connection c = ds.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, limit);
            try (ResultSet rs = ps.executeQuery()) {
                List<AlertaRow> list = new ArrayList<>();
                while (rs.next()) {
                    Timestamp t = rs.getTimestamp("timestamp_generacion");
                    list.add(new AlertaRow(
                            t != null ? t.toLocalDateTime() : null,
                            rs.getString("tipo_alerta"),
                            rs.getString("descripcion")));
                }
                return list;
            }
        }
    }
}
