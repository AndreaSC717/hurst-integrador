package com.husrt.repository;

import com.husrt.db.DataSourceManager;
import com.husrt.model.AsignacionPractica;
import com.husrt.model.PlanPracticas;

import javax.sql.DataSource;
import java.sql.*;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class PlanPracticasRepository {

    private final DataSource ds = DataSourceManager.get();

    public long insert(PlanPracticas p) throws SQLException {
        String sql = """
                INSERT INTO plan_practicas (id_docente, id_universidad, semestre, mes, anio, periodo, fecha_carga)
                VALUES (?,?,?,?,?,?,?)
                """;
        try (Connection c = ds.getConnection(); PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setLong(1, p.idDocente());
            ps.setLong(2, p.idUniversidad());
            ps.setString(3, p.semestre());
            ps.setInt(4, p.mes());
            ps.setInt(5, p.anio());
            ps.setInt(6, p.periodo());
            ps.setDate(7, Date.valueOf(p.fechaCarga()));
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                keys.next();
                return keys.getLong(1);
            }
        }
    }

    public List<PlanPracticas> findAll() throws SQLException {
        String sql = "SELECT id_plan, id_docente, id_universidad, semestre, mes, anio, periodo, fecha_carga FROM plan_practicas ORDER BY anio DESC, mes DESC";
        try (Connection c = ds.getConnection();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            List<PlanPracticas> list = new ArrayList<>();
            while (rs.next()) {
                list.add(mapPlan(rs));
            }
            return list;
        }
    }

    public Optional<PlanPracticas> findById(long idPlan) throws SQLException {
        String sql = "SELECT id_plan, id_docente, id_universidad, semestre, mes, anio, periodo, fecha_carga FROM plan_practicas WHERE id_plan=?";
        try (Connection c = ds.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setLong(1, idPlan);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    return Optional.empty();
                }
                return Optional.of(mapPlan(rs));
            }
        }
    }

    private static PlanPracticas mapPlan(ResultSet rs) throws SQLException {
        return new PlanPracticas(
                rs.getLong("id_plan"),
                rs.getLong("id_docente"),
                rs.getLong("id_universidad"),
                rs.getString("semestre"),
                rs.getInt("mes"),
                rs.getInt("anio"),
                rs.getInt("periodo"),
                rs.getDate("fecha_carga").toLocalDate());
    }

    public void insertAsignacion(AsignacionPractica a) throws SQLException {
        String sql = """
                INSERT INTO asignacion_practica (id_plan, id_estudiante, id_servicio, dia_semana, hora_inicio, hora_fin, fecha_especifica)
                VALUES (?,?,?,?,?,?,?)
                """;
        try (Connection c = ds.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setLong(1, a.idPlan());
            ps.setLong(2, a.idEstudiante());
            ps.setLong(3, a.idServicio());
            ps.setInt(4, a.diaSemana());
            ps.setTime(5, Time.valueOf(a.horaInicio()));
            ps.setTime(6, Time.valueOf(a.horaFin()));
            if (a.fechaEspecifica() != null) {
                ps.setDate(7, Date.valueOf(a.fechaEspecifica()));
            } else {
                ps.setNull(7, Types.DATE);
            }
            ps.executeUpdate();
        }
    }

    public void updateAsignacion(AsignacionPractica a) throws SQLException {
        String sql = """
                UPDATE asignacion_practica
                SET id_servicio = ?, dia_semana = ?, hora_inicio = ?, hora_fin = ?, fecha_especifica = ?
                WHERE id_asignacion = ?
                """;
        try (Connection c = ds.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setLong(1, a.idServicio());
            ps.setInt(2, a.diaSemana());
            ps.setTime(3, Time.valueOf(a.horaInicio()));
            ps.setTime(4, Time.valueOf(a.horaFin()));
            if (a.fechaEspecifica() != null) {
                ps.setDate(5, Date.valueOf(a.fechaEspecifica()));
            } else {
                ps.setNull(5, Types.DATE);
            }
            ps.setLong(6, a.idAsignacion());
            ps.executeUpdate();
        }
    }

    public List<AsignacionPractica> listAsignacionesByPlan(long idPlan) throws SQLException {
        String sql = """
                SELECT id_asignacion, id_plan, id_estudiante, id_servicio, dia_semana, hora_inicio, hora_fin, fecha_especifica
                FROM asignacion_practica WHERE id_plan=? ORDER BY id_asignacion
                """;
        try (Connection c = ds.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setLong(1, idPlan);
            try (ResultSet rs = ps.executeQuery()) {
                List<AsignacionPractica> list = new ArrayList<>();
                while (rs.next()) {
                    list.add(mapAsig(rs));
                }
                return list;
            }
        }
    }

    public Optional<AsignacionPractica> findAsignacionValidaIngreso(
            long idEstudiante, int anio, int periodo, int mes, LocalDate dia, int diaSemanaIso, LocalTime hora) throws SQLException {
        String sql = """
                SELECT a.id_asignacion, a.id_plan, a.id_estudiante, a.id_servicio, a.dia_semana, a.hora_inicio, a.hora_fin, a.fecha_especifica
                FROM asignacion_practica a
                JOIN plan_practicas p ON p.id_plan = a.id_plan
                WHERE a.id_estudiante = ?
                  AND p.anio = ?
                  AND p.periodo = ?
                  AND p.mes = ?
                  AND (
                        (a.fecha_especifica IS NOT NULL AND a.fecha_especifica = ?)
                        OR (a.fecha_especifica IS NULL AND a.dia_semana = ?)
                      )
                  AND TIME(?) BETWEEN a.hora_inicio AND a.hora_fin
                ORDER BY a.id_asignacion
                LIMIT 1
                """;
        try (Connection c = ds.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setLong(1, idEstudiante);
            ps.setInt(2, anio);
            ps.setInt(3, periodo);
            ps.setInt(4, mes);
            ps.setDate(5, Date.valueOf(dia));
            ps.setInt(6, diaSemanaIso);
            ps.setTime(7, Time.valueOf(hora));
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    return Optional.empty();
                }
                return Optional.of(mapAsig(rs));
            }
        }
    }

    /** Students assigned to the service in the same plan and time slot (same weekday or specific date). */
    public int countAsignadosMismaFranja(long idPlan, long idServicio, int diaSemana, LocalDate fechaEspecifica,
                                         LocalTime hi, LocalTime hf) throws SQLException {
        String sqlFe = """
                SELECT COUNT(*) FROM asignacion_practica a
                WHERE a.id_plan = ? AND a.id_servicio = ?
                  AND a.hora_inicio = ? AND a.hora_fin = ?
                  AND a.fecha_especifica = ?
                """;
        String sqlDia = """
                SELECT COUNT(*) FROM asignacion_practica a
                WHERE a.id_plan = ? AND a.id_servicio = ?
                  AND a.hora_inicio = ? AND a.hora_fin = ?
                  AND a.fecha_especifica IS NULL AND a.dia_semana = ?
                """;
        String sql = fechaEspecifica != null ? sqlFe : sqlDia;
        try (Connection c = ds.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setLong(1, idPlan);
            ps.setLong(2, idServicio);
            ps.setTime(3, Time.valueOf(hi));
            ps.setTime(4, Time.valueOf(hf));
            if (fechaEspecifica != null) {
                ps.setDate(5, Date.valueOf(fechaEspecifica));
            } else {
                ps.setInt(5, diaSemana);
            }
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getInt(1);
            }
        }
    }

    private static AsignacionPractica mapAsig(ResultSet rs) throws SQLException {
        Date fe = rs.getDate("fecha_especifica");
        return new AsignacionPractica(
                rs.getLong("id_asignacion"),
                rs.getLong("id_plan"),
                rs.getLong("id_estudiante"),
                rs.getLong("id_servicio"),
                rs.getInt("dia_semana"),
                rs.getTime("hora_inicio").toLocalTime(),
                rs.getTime("hora_fin").toLocalTime(),
                fe != null ? fe.toLocalDate() : null);
    }
}
