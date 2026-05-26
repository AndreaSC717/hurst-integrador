package com.husrt.repository;

import com.husrt.db.DataSourceManager;
import com.husrt.model.PresenteEstudianteDto;
import com.husrt.model.ResultadoValidacion;

import javax.sql.DataSource;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class RegistroAccesoRepository {

    private final DataSource ds = DataSourceManager.get();

    public void insertIntento(long idEstudiante, Long idAsignacion, ResultadoValidacion resultado, String motivo) throws SQLException {
        String sql = """
                INSERT INTO registro_acceso (id_estudiante, id_asignacion, timestamp_entrada, timestamp_salida, horas_cumplidas, resultado_validacion, motivo_rechazo)
                VALUES (?,?,NOW(3),NULL,NULL,?,?)
                """;
        try (Connection c = ds.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setLong(1, idEstudiante);
            if (idAsignacion != null) {
                ps.setLong(2, idAsignacion);
            } else {
                ps.setNull(2, Types.BIGINT);
            }
            ps.setString(3, resultado.name());
            if (motivo != null) {
                ps.setString(4, motivo);
            } else {
                ps.setNull(4, Types.VARCHAR);
            }
            ps.executeUpdate();
        }
    }

    public void insertAprobadoEntrada(long idEstudiante, long idAsignacion) throws SQLException {
        insertIntento(idEstudiante, idAsignacion, ResultadoValidacion.APROBADO, null);
    }

    public Optional<Long> findRegistroAbiertoId(long idEstudiante) throws SQLException {
        String sql = """
                SELECT id_registro FROM registro_acceso
                WHERE id_estudiante=? AND resultado_validacion='APROBADO' AND timestamp_salida IS NULL
                ORDER BY timestamp_entrada DESC LIMIT 1
                """;
        try (Connection c = ds.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setLong(1, idEstudiante);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    return Optional.empty();
                }
                return Optional.of(rs.getLong("id_registro"));
            }
        }
    }

    public boolean tieneEntradaAbierta(long idEstudiante) throws SQLException {
        return findRegistroAbiertoId(idEstudiante).isPresent();
    }

    public void cerrarSalida(long idRegistro) throws SQLException {
        String sql = """
                UPDATE registro_acceso
                SET timestamp_salida = NOW(3),
                    horas_cumplidas = ROUND(TIMESTAMPDIFF(MINUTE, timestamp_entrada, NOW(3)) / 60.0, 2)
                WHERE id_registro = ?
                """;
        try (Connection c = ds.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setLong(1, idRegistro);
            ps.executeUpdate();
        }
    }

    public int countPresentesEnServicio(long idServicio) throws SQLException {
        String sql = """
                SELECT COUNT(*) FROM registro_acceso r
                JOIN asignacion_practica a ON a.id_asignacion = r.id_asignacion
                WHERE r.resultado_validacion = 'APROBADO'
                  AND r.timestamp_salida IS NULL
                  AND a.id_servicio = ?
                """;
        try (Connection c = ds.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setLong(1, idServicio);
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getInt(1);
            }
        }
    }

    public record HistorialRow(
            long idRegistro,
            String cedula,
            String nombreCompleto,
            LocalDateTime entrada,
            LocalDateTime salida,
            Double horas,
            String resultado,
            String motivo
    ) {
    }

    public List<HistorialRow> historialEstudianteRango(long idEstudiante, LocalDateTime desde, LocalDateTime hasta) throws SQLException {
        String sql = """
                SELECT r.id_registro, e.cedula, CONCAT(e.nombre,' ',e.apellido) AS nombre_completo,
                       r.timestamp_entrada, r.timestamp_salida, r.horas_cumplidas, r.resultado_validacion, r.motivo_rechazo
                FROM registro_acceso r
                JOIN estudiante e ON e.id_estudiante = r.id_estudiante
                WHERE r.id_estudiante = ?
                  AND r.timestamp_entrada >= ? AND r.timestamp_entrada < ?
                ORDER BY r.timestamp_entrada DESC
                """;
        try (Connection c = ds.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setLong(1, idEstudiante);
            ps.setTimestamp(2, Timestamp.valueOf(desde));
            ps.setTimestamp(3, Timestamp.valueOf(hasta));
            try (ResultSet rs = ps.executeQuery()) {
                List<HistorialRow> list = new ArrayList<>();
                while (rs.next()) {
                    Timestamp te = rs.getTimestamp("timestamp_entrada");
                    Timestamp ts = rs.getTimestamp("timestamp_salida");
                    list.add(new HistorialRow(
                            rs.getLong("id_registro"),
                            rs.getString("cedula"),
                            rs.getString("nombre_completo"),
                            te != null ? te.toLocalDateTime() : null,
                            ts != null ? ts.toLocalDateTime() : null,
                            rs.getObject("horas_cumplidas") != null ? rs.getDouble("horas_cumplidas") : null,
                            rs.getString("resultado_validacion"),
                            rs.getString("motivo_rechazo")));
                }
                return list;
            }
        }
    }

    public List<PresenteEstudianteDto> listEstudiantesPresentes() throws SQLException {
        String sql = """
                SELECT e.nombre, e.apellido, e.cedula, s.nombre AS servicio, r.timestamp_entrada
                FROM registro_acceso r
                JOIN estudiante e ON e.id_estudiante = r.id_estudiante
                JOIN asignacion_practica a ON a.id_asignacion = r.id_asignacion
                JOIN servicio_hospitalario s ON s.id_servicio = a.id_servicio
                WHERE r.resultado_validacion = 'APROBADO' AND r.timestamp_salida IS NULL
                ORDER BY r.timestamp_entrada DESC
                """;
        try (Connection c = ds.getConnection();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            List<PresenteEstudianteDto> list = new ArrayList<>();
            while (rs.next()) {
                Timestamp t = rs.getTimestamp("timestamp_entrada");
                list.add(new PresenteEstudianteDto(
                        rs.getString("nombre"),
                        rs.getString("apellido"),
                        rs.getString("cedula"),
                        rs.getString("servicio"),
                        t != null ? t.toLocalDateTime() : null));
            }
            return list;
        }
    }

    public double sumHorasAprobadasSemestre(long idEstudiante, int anio, int periodo) throws SQLException {
        String sql = """
                SELECT COALESCE(SUM(r.horas_cumplidas), 0) AS total
                FROM registro_acceso r
                WHERE r.id_estudiante = ?
                  AND r.resultado_validacion = 'APROBADO'
                  AND YEAR(r.timestamp_entrada) = ?
                  AND (
                    (? = 1 AND MONTH(r.timestamp_entrada) BETWEEN 1 AND 6)
                    OR (? = 2 AND MONTH(r.timestamp_entrada) BETWEEN 7 AND 12)
                  )
                """;
        try (Connection c = ds.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setLong(1, idEstudiante);
            ps.setInt(2, anio);
            ps.setInt(3, periodo);
            ps.setInt(4, periodo);
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getDouble("total");
            }
        }
    }

    public record RechazoRow(LocalDateTime ts, String cedula, String nombre, String motivo) {
    }

    public List<RechazoRow> listRechazados(LocalDateTime desde, LocalDateTime hasta) throws SQLException {
        String sql = """
                SELECT r.timestamp_entrada, e.cedula, CONCAT(e.nombre,' ',e.apellido) AS nombre, r.motivo_rechazo
                FROM registro_acceso r
                JOIN estudiante e ON e.id_estudiante = r.id_estudiante
                WHERE r.resultado_validacion = 'RECHAZADO'
                  AND r.timestamp_entrada >= ? AND r.timestamp_entrada < ?
                ORDER BY r.timestamp_entrada DESC
                """;
        try (Connection c = ds.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setTimestamp(1, Timestamp.valueOf(desde));
            ps.setTimestamp(2, Timestamp.valueOf(hasta));
            try (ResultSet rs = ps.executeQuery()) {
                List<RechazoRow> list = new ArrayList<>();
                while (rs.next()) {
                    Timestamp t = rs.getTimestamp("timestamp_entrada");
                    list.add(new RechazoRow(
                            t != null ? t.toLocalDateTime() : null,
                            rs.getString("cedula"),
                            rs.getString("nombre"),
                            rs.getString("motivo_rechazo")));
                }
                return list;
            }
        }
    }

    public record OcupacionRow(String servicio, LocalDateTime dia, long ingresos) {
    }

    public List<OcupacionRow> ocupacionHistorica(LocalDateTime desde, LocalDateTime hasta) throws SQLException {
        String sql = """
                SELECT s.nombre AS servicio, DATE(r.timestamp_entrada) AS dia, COUNT(*) AS ingresos
                FROM registro_acceso r
                JOIN asignacion_practica a ON a.id_asignacion = r.id_asignacion
                JOIN servicio_hospitalario s ON s.id_servicio = a.id_servicio
                WHERE r.resultado_validacion = 'APROBADO'
                  AND r.timestamp_entrada >= ? AND r.timestamp_entrada < ?
                GROUP BY s.nombre, DATE(r.timestamp_entrada)
                ORDER BY dia DESC, servicio
                """;
        try (Connection c = ds.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setTimestamp(1, Timestamp.valueOf(desde));
            ps.setTimestamp(2, Timestamp.valueOf(hasta));
            try (ResultSet rs = ps.executeQuery()) {
                List<OcupacionRow> list = new ArrayList<>();
                while (rs.next()) {
                    Date d = rs.getDate("dia");
                    list.add(new OcupacionRow(
                            rs.getString("servicio"),
                            d != null ? d.toLocalDate().atStartOfDay() : null,
                            rs.getLong("ingresos")));
                }
                return list;
            }
        }
    }

    public int countIngresosAprobadosHoy() throws SQLException {
        String sql = """
                SELECT COUNT(DISTINCT id_estudiante) FROM registro_acceso
                WHERE resultado_validacion = 'APROBADO' AND DATE(timestamp_entrada) = CURDATE()
                """;
        try (Connection c = ds.getConnection();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            rs.next();
            return rs.getInt(1);
        }
    }

    public int countRechazosHoy() throws SQLException {
        String sql = """
                SELECT COUNT(*) FROM registro_acceso
                WHERE resultado_validacion = 'RECHAZADO' AND DATE(timestamp_entrada) = CURDATE()
                """;
        try (Connection c = ds.getConnection();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            rs.next();
            return rs.getInt(1);
        }
    }

    public record OcupacionServicioRow(String servicio, int dentro, int capacidad) {
        public double porcentaje() {
            return capacidad > 0 ? Math.min(100, dentro * 100.0 / capacidad) : 0;
        }
    }

    public List<OcupacionServicioRow> ocupacionActualPorServicio() throws SQLException {
        String sql = """
                SELECT s.nombre, s.capacidad_maxima_estudiantes,
                       (SELECT COUNT(DISTINCT r.id_estudiante)
                        FROM registro_acceso r
                        JOIN asignacion_practica a ON a.id_asignacion = r.id_asignacion
                        WHERE a.id_servicio = s.id_servicio
                          AND r.resultado_validacion = 'APROBADO'
                          AND r.timestamp_salida IS NULL) AS dentro
                FROM servicio_hospitalario s
                ORDER BY s.nombre
                """;
        try (Connection c = ds.getConnection();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            List<OcupacionServicioRow> list = new ArrayList<>();
            while (rs.next()) {
                list.add(new OcupacionServicioRow(
                        rs.getString("nombre"),
                        rs.getInt("dentro"),
                        rs.getInt("capacidad_maxima_estudiantes")));
            }
            return list;
        }
    }

    public record ValidacionRow(LocalDateTime ts, String cedula, String nombre, String resultado) {
    }

    public List<ValidacionRow> ultimasValidaciones(int limit) throws SQLException {
        String sql = """
                SELECT r.timestamp_entrada, e.cedula, CONCAT(e.nombre,' ',e.apellido) AS nombre,
                       r.resultado_validacion
                FROM registro_acceso r
                JOIN estudiante e ON e.id_estudiante = r.id_estudiante
                ORDER BY r.timestamp_entrada DESC
                LIMIT ?
                """;
        try (Connection c = ds.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, limit);
            try (ResultSet rs = ps.executeQuery()) {
                List<ValidacionRow> list = new ArrayList<>();
                while (rs.next()) {
                    Timestamp t = rs.getTimestamp("timestamp_entrada");
                    list.add(new ValidacionRow(
                            t != null ? t.toLocalDateTime() : null,
                            rs.getString("cedula"),
                            rs.getString("nombre"),
                            rs.getString("resultado_validacion")));
                }
                return list;
            }
        }
    }

    public List<String> listCedulasFranjaVencidaSinSalida() throws SQLException {
        String sql = """
                SELECT e.cedula
                FROM registro_acceso r
                JOIN asignacion_practica a ON a.id_asignacion = r.id_asignacion
                JOIN estudiante e ON e.id_estudiante = r.id_estudiante
                WHERE r.resultado_validacion = 'APROBADO' AND r.timestamp_salida IS NULL
                  AND DATE(r.timestamp_entrada) = CURDATE()
                  AND TIME(NOW()) > a.hora_fin
                """;
        try (Connection c = ds.getConnection();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            List<String> list = new ArrayList<>();
            while (rs.next()) {
                list.add(rs.getString("cedula"));
            }
            return list;
        }
    }
}
