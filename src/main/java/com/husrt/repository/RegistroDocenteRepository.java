package com.husrt.repository;

import com.husrt.db.DataSourceManager;

import javax.sql.DataSource;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.Optional;

public class RegistroDocenteRepository {

    private final DataSource ds = DataSourceManager.get();

    public void insertEntrada(long idDocente, Long idPlan) throws SQLException {
        String sql = "INSERT INTO registro_acceso_docente (id_docente, id_plan, timestamp_entrada, timestamp_salida) VALUES (?,?,NOW(3),NULL)";
        try (Connection c = ds.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setLong(1, idDocente);
            if (idPlan != null) {
                ps.setLong(2, idPlan);
            } else {
                ps.setNull(2, Types.BIGINT);
            }
            ps.executeUpdate();
        }
    }

    public boolean tieneEntradaAbiertaParaPlan(long idDocente, long idPlan) throws SQLException {
        String sql = """
                SELECT 1 FROM registro_acceso_docente
                WHERE id_docente=? AND id_plan=? AND timestamp_salida IS NULL
                ORDER BY timestamp_entrada DESC LIMIT 1
                """;
        try (Connection c = ds.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setLong(1, idDocente);
            ps.setLong(2, idPlan);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    public Optional<Long> findUltimoAbiertoId(long idDocente, Long idPlan) throws SQLException {
        String sql = idPlan != null
                ? """
                SELECT id_registro_docente FROM registro_acceso_docente
                WHERE id_docente=? AND id_plan=? AND timestamp_salida IS NULL
                ORDER BY timestamp_entrada DESC LIMIT 1
                """
                : """
                SELECT id_registro_docente FROM registro_acceso_docente
                WHERE id_docente=? AND timestamp_salida IS NULL
                ORDER BY timestamp_entrada DESC LIMIT 1
                """;
        try (Connection c = ds.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setLong(1, idDocente);
            if (idPlan != null) {
                ps.setLong(2, idPlan);
            }
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    return Optional.empty();
                }
                return Optional.of(rs.getLong("id_registro_docente"));
            }
        }
    }

    public void cerrarSalida(long idRegistroDocente) throws SQLException {
        String sql = "UPDATE registro_acceso_docente SET timestamp_salida = NOW(3) WHERE id_registro_docente = ?";
        try (Connection c = ds.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setLong(1, idRegistroDocente);
            ps.executeUpdate();
        }
    }

    public boolean docenteDentro(long idDocente) throws SQLException {
        String sql = "SELECT 1 FROM registro_acceso_docente WHERE id_docente=? AND timestamp_salida IS NULL LIMIT 1";
        try (Connection c = ds.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setLong(1, idDocente);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    public record DocentePresenteRow(String nombre, String apellido, String cedula, Long idPlan, LocalDateTime entrada) {
    }

    public java.util.List<DocentePresenteRow> listDocentesPresentes() throws SQLException {
        String sql = """
                SELECT d.nombre, d.apellido, d.cedula, r.id_plan, r.timestamp_entrada
                FROM registro_acceso_docente r
                JOIN docente d ON d.id_docente = r.id_docente
                WHERE r.timestamp_salida IS NULL
                ORDER BY r.timestamp_entrada DESC
                """;
        try (Connection c = ds.getConnection();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            java.util.ArrayList<DocentePresenteRow> list = new java.util.ArrayList<>();
            while (rs.next()) {
                Timestamp t = rs.getTimestamp("timestamp_entrada");
                list.add(new DocentePresenteRow(
                        rs.getString("nombre"),
                        rs.getString("apellido"),
                        rs.getString("cedula"),
                        rs.getObject("id_plan") != null ? rs.getLong("id_plan") : null,
                        t != null ? t.toLocalDateTime() : null));
            }
            return list;
        }
    }
}
