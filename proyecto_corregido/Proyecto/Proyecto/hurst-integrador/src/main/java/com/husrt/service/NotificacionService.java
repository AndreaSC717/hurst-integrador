package com.husrt.service;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import javax.sql.DataSource;

import com.husrt.db.DataSourceManager;
import com.husrt.model.Notificacion;

public class NotificacionService {

    private final DataSource ds = DataSourceManager.get();

    public void enviarNotificacion(long idDocente, long idEstudiante, String mensaje) throws SQLException {
        String sql = "INSERT INTO notificacion (id_docente, id_estudiante, mensaje, fecha_envio, leida) VALUES (?, ?, ?, ?, FALSE)";
        try (Connection c = ds.getConnection(); PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setLong(1, idDocente);
            ps.setLong(2, idEstudiante);
            ps.setString(3, mensaje);
            ps.setObject(4, LocalDateTime.now());
            ps.executeUpdate();
        }
    }

    public List<Notificacion> obtenerNotificacionesEstudiante(long idEstudiante) throws SQLException {
        String sql = "SELECT id_notificacion, id_docente, id_estudiante, mensaje, fecha_envio, leida FROM notificacion WHERE id_estudiante = ? ORDER BY fecha_envio DESC";
        try (Connection c = ds.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setLong(1, idEstudiante);
            try (ResultSet rs = ps.executeQuery()) {
                List<Notificacion> list = new ArrayList<>();
                while (rs.next()) {
                    list.add(map(rs));
                }
                return list;
            }
        }
    }

    public List<Notificacion> obtenerNotificacionesDocente(long idDocente) throws SQLException {
        String sql = "SELECT id_notificacion, id_docente, id_estudiante, mensaje, fecha_envio, leida FROM notificacion WHERE id_docente = ? ORDER BY fecha_envio DESC";
        try (Connection c = ds.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setLong(1, idDocente);
            try (ResultSet rs = ps.executeQuery()) {
                List<Notificacion> list = new ArrayList<>();
                while (rs.next()) {
                    list.add(map(rs));
                }
                return list;
            }
        }
    }

    public void marcarComoLeida(long idNotificacion) throws SQLException {
        String sql = "UPDATE notificacion SET leida = TRUE WHERE id_notificacion = ?";
        try (Connection c = ds.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setLong(1, idNotificacion);
            ps.executeUpdate();
        }
    }

    private static Notificacion map(ResultSet rs) throws SQLException {
        return new Notificacion(
                rs.getLong("id_notificacion"),
                rs.getLong("id_docente"),
                rs.getLong("id_estudiante"),
                rs.getString("mensaje"),
                rs.getObject("fecha_envio", LocalDateTime.class),
                rs.getBoolean("leida"));
    }
}
