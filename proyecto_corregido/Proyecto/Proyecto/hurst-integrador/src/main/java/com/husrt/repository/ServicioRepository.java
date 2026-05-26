package com.husrt.repository;

import com.husrt.db.DataSourceManager;
import com.husrt.model.ServicioHospitalario;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ServicioRepository {

    private final DataSource ds = DataSourceManager.get();

    public List<ServicioHospitalario> findAll() throws SQLException {
        String sql = "SELECT id_servicio, nombre, piso, capacidad_maxima_estudiantes FROM servicio_hospitalario ORDER BY nombre";
        try (Connection c = ds.getConnection();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            List<ServicioHospitalario> list = new ArrayList<>();
            while (rs.next()) {
                list.add(map(rs));
            }
            return list;
        }
    }

    public Optional<ServicioHospitalario> findById(long id) throws SQLException {
        String sql = "SELECT id_servicio, nombre, piso, capacidad_maxima_estudiantes FROM servicio_hospitalario WHERE id_servicio=?";
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

    public void insert(ServicioHospitalario s) throws SQLException {
        String sql = "INSERT INTO servicio_hospitalario (nombre, piso, capacidad_maxima_estudiantes) VALUES (?,?,?)";
        try (Connection c = ds.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, s.nombre());
            ps.setString(2, s.piso());
            ps.setInt(3, s.capacidadMaximaEstudiantes());
            ps.executeUpdate();
        }
    }

    public void update(ServicioHospitalario s) throws SQLException {
        String sql = "UPDATE servicio_hospitalario SET nombre=?, piso=?, capacidad_maxima_estudiantes=? WHERE id_servicio=?";
        try (Connection c = ds.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, s.nombre());
            ps.setString(2, s.piso());
            ps.setInt(3, s.capacidadMaximaEstudiantes());
            ps.setLong(4, s.idServicio());
            ps.executeUpdate();
        }
    }

    private static ServicioHospitalario map(ResultSet rs) throws SQLException {
        return new ServicioHospitalario(
                rs.getLong("id_servicio"),
                rs.getString("nombre"),
                rs.getString("piso"),
                rs.getInt("capacidad_maxima_estudiantes"));
    }
}
