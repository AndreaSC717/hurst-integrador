package com.husrt.repository;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import javax.sql.DataSource;

import com.husrt.db.DataSourceManager;
import com.husrt.model.EstadoEstudiante;
import com.husrt.model.Estudiante;

public class EstudianteRepository {

    private final DataSource ds = DataSourceManager.get();

    public Optional<Estudiante> findByCedula(String cedula) throws SQLException {
        String sql = """
                SELECT id_estudiante, cedula, nombre, apellido, foto_url, programa_academico, semestre_academico,
                       id_universidad, induccion_completada, fecha_induccion, arl_vigencia_inicio, arl_vigencia_fin, estado, vacunas_completas
                FROM estudiante WHERE cedula = ?
                """;
        try (Connection c = ds.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, cedula.trim());
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    return Optional.empty();
                }
                return Optional.of(map(rs));
            }
        }
    }

    public Optional<Estudiante> findById(long id) throws SQLException {
        String sql = """
                SELECT id_estudiante, cedula, nombre, apellido, foto_url, programa_academico, semestre_academico,
                       id_universidad, induccion_completada, fecha_induccion, arl_vigencia_inicio, arl_vigencia_fin, estado, vacunas_completas
                FROM estudiante WHERE id_estudiante = ?
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

    public List<Estudiante> findAll() throws SQLException {
        String sql = """
                SELECT id_estudiante, cedula, nombre, apellido, foto_url, programa_academico, semestre_academico,
                       id_universidad, induccion_completada, fecha_induccion, arl_vigencia_inicio, arl_vigencia_fin, estado, vacunas_completas
                FROM estudiante ORDER BY apellido, nombre
                """;
        try (Connection c = ds.getConnection(); PreparedStatement ps = c.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
            List<Estudiante> list = new ArrayList<>();
            while (rs.next()) {
                list.add(map(rs));
            }
            return list;
        }
    }

    public long insert(Estudiante e) throws SQLException {
        String sql = """
                INSERT INTO estudiante (cedula, nombre, apellido, foto_url, programa_academico, semestre_academico,
                  id_universidad, induccion_completada, fecha_induccion, arl_vigencia_inicio, arl_vigencia_fin, estado, vacunas_completas)
                VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?)
                """;
        try (Connection c = ds.getConnection(); PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            bindWrite(ps, e, false);
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                keys.next();
                return keys.getLong(1);
            }
        }
    }

    public void updateFotoUrl(long idEstudiante, String fotoUrl) throws SQLException {
        String sql = "UPDATE estudiante SET foto_url=? WHERE id_estudiante=?";
        try (Connection c = ds.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            if (fotoUrl != null) {
                ps.setString(1, fotoUrl);
            } else {
                ps.setNull(1, Types.VARCHAR);
            }
            ps.setLong(2, idEstudiante);
            ps.executeUpdate();
        }
    }

    public void update(Estudiante e) throws SQLException {
        String sql = """
                UPDATE estudiante SET cedula=?, nombre=?, apellido=?, foto_url=?, programa_academico=?, semestre_academico=?,
                  id_universidad=?, induccion_completada=?, fecha_induccion=?, arl_vigencia_inicio=?, arl_vigencia_fin=?, estado=?, vacunas_completas=?
                WHERE id_estudiante=?
                """;
        try (Connection c = ds.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            bindWrite(ps, e, true);
            ps.executeUpdate();
        }
    }

    private void bindWrite(PreparedStatement ps, Estudiante e, boolean forUpdate) throws SQLException {
        int i = 1;
        ps.setString(i++, e.cedula());
        ps.setString(i++, e.nombre());
        ps.setString(i++, e.apellido());
        if (e.fotoUrl() != null) {
            ps.setString(i++, e.fotoUrl());
        } else {
            ps.setNull(i++, Types.VARCHAR);
        }
        ps.setString(i++, e.programaAcademico());
        ps.setInt(i++, e.semestreAcademico());
        ps.setLong(i++, e.idUniversidad());
        ps.setBoolean(i++, e.induccionCompletada());
        ps.setObject(i++, e.fechaInduccion());
        ps.setObject(i++, e.arlInicio());
        ps.setObject(i++, e.arlFin());
        ps.setString(i++, e.estado().name());
        ps.setBoolean(i++, e.vacunasCompletas());
        if (forUpdate) {
            ps.setLong(i, e.idEstudiante());
        }
    }

    private static Estudiante map(ResultSet rs) throws SQLException {
        Date fi = rs.getDate("fecha_induccion");
        Date ai = rs.getDate("arl_vigencia_inicio");
        Date af = rs.getDate("arl_vigencia_fin");
        return new Estudiante(
                rs.getLong("id_estudiante"),
                rs.getString("cedula"),
                rs.getString("nombre"),
                rs.getString("apellido"),
                rs.getString("foto_url"),
                rs.getString("programa_academico"),
                rs.getInt("semestre_academico"),
                rs.getLong("id_universidad"),
                rs.getBoolean("induccion_completada"),
                fi != null ? fi.toLocalDate() : null,
                ai != null ? ai.toLocalDate() : null,
                af != null ? af.toLocalDate() : null,
                EstadoEstudiante.fromDb(rs.getString("estado")),
                rs.getBoolean("vacunas_completas"));
    }

    public int countActivos() throws SQLException {
        String sql = "SELECT COUNT(*) FROM estudiante WHERE estado = 'ACTIVO'";
        try (Connection c = ds.getConnection(); PreparedStatement ps = c.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
            rs.next();
            return rs.getInt(1);
        }
    }

    public boolean existsInscripcionActiva(long idEstudiante, int anio, int periodo) throws SQLException {
        String sql = "SELECT 1 FROM inscripcion_semestral WHERE id_estudiante=? AND anio=? AND periodo=? AND activo=TRUE LIMIT 1";
        try (Connection c = ds.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setLong(1, idEstudiante);
            ps.setInt(2, anio);
            ps.setInt(3, periodo);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    public void insertInscripcion(long idEstudiante, int anio, int periodo) throws SQLException {
        String sql = "INSERT INTO inscripcion_semestral (id_estudiante, anio, periodo, activo) VALUES (?,?,?,TRUE)";
        try (Connection c = ds.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setLong(1, idEstudiante);
            ps.setInt(2, anio);
            ps.setInt(3, periodo);
            ps.executeUpdate();
        }
    }

    public boolean existsInscripcionSamePeriod(long idEstudiante, int anio, int periodo) throws SQLException {
        String sql = "SELECT 1 FROM inscripcion_semestral WHERE id_estudiante=? AND anio=? AND periodo=? LIMIT 1";
        try (Connection c = ds.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setLong(1, idEstudiante);
            ps.setInt(2, anio);
            ps.setInt(3, periodo);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    public record ArlAlertaRow(String cedula, String nombre, String apellido, java.time.LocalDate arlFin) {

    }

    public List<ArlAlertaRow> listArlProximaVencer(int dias) throws SQLException {
        String sql = """
                SELECT cedula, nombre, apellido, arl_vigencia_fin
                FROM estudiante
                WHERE estado = 'ACTIVO'
                  AND arl_vigencia_fin IS NOT NULL
                  AND arl_vigencia_fin BETWEEN CURDATE() AND DATE_ADD(CURDATE(), INTERVAL ? DAY)
                ORDER BY arl_vigencia_fin
                """;
        try (Connection c = ds.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, dias);
            try (ResultSet rs = ps.executeQuery()) {
                List<ArlAlertaRow> list = new ArrayList<>();
                while (rs.next()) {
                    Date af = rs.getDate("arl_vigencia_fin");
                    list.add(new ArlAlertaRow(
                            rs.getString("cedula"),
                            rs.getString("nombre"),
                            rs.getString("apellido"),
                            af != null ? af.toLocalDate() : null));
                }
                return list;
            }
        }
    }
}
