package com.husrt.repository;

import com.husrt.db.DataSourceManager;
import com.husrt.model.Universidad;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class UniversidadRepository {

    private final DataSource ds = DataSourceManager.get();

    public List<Universidad> findAll() throws SQLException {
        String sql = "SELECT id_universidad, nombre, ciudad, tipo_convenio FROM universidad ORDER BY nombre";
        try (Connection c = ds.getConnection();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            List<Universidad> list = new ArrayList<>();
            while (rs.next()) {
                list.add(new Universidad(
                        rs.getLong("id_universidad"),
                        rs.getString("nombre"),
                        rs.getString("ciudad"),
                        rs.getString("tipo_convenio")));
            }
            return list;
        }
    }

    public Optional<Universidad> findById(long id) throws SQLException {
        String sql = "SELECT id_universidad, nombre, ciudad, tipo_convenio FROM universidad WHERE id_universidad=?";
        try (Connection c = ds.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    return Optional.empty();
                }
                return Optional.of(new Universidad(
                        rs.getLong("id_universidad"),
                        rs.getString("nombre"),
                        rs.getString("ciudad"),
                        rs.getString("tipo_convenio")));
            }
        }
    }

    public void insert(Universidad u) throws SQLException {
        String sql = "INSERT INTO universidad (nombre, ciudad, tipo_convenio) VALUES (?,?,?)";
        try (Connection c = ds.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, u.nombre());
            ps.setString(2, u.ciudad());
            ps.setString(3, u.tipoConvenio());
            ps.executeUpdate();
        }
    }

    public void update(Universidad u) throws SQLException {
        String sql = "UPDATE universidad SET nombre=?, ciudad=?, tipo_convenio=? WHERE id_universidad=?";
        try (Connection c = ds.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, u.nombre());
            ps.setString(2, u.ciudad());
            ps.setString(3, u.tipoConvenio());
            ps.setLong(4, u.idUniversidad());
            ps.executeUpdate();
        }
    }
}
