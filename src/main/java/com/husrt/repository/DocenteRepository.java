package com.husrt.repository;

import com.husrt.db.DataSourceManager;
import com.husrt.model.Docente;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class DocenteRepository {

    private final DataSource ds = DataSourceManager.get();

    public Optional<Docente> findByCedula(String cedula) throws SQLException {
        String sql = "SELECT id_docente, cedula, nombre, apellido, id_universidad, programa_que_supervisa FROM docente WHERE cedula=?";
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

    public Optional<Docente> findById(long id) throws SQLException {
        String sql = "SELECT id_docente, cedula, nombre, apellido, id_universidad, programa_que_supervisa FROM docente WHERE id_docente=?";
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

    public int countAll() throws SQLException {
        String sql = "SELECT COUNT(*) FROM docente";
        try (Connection c = ds.getConnection();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            rs.next();
            return rs.getInt(1);
        }
    }

    public List<Docente> findAll() throws SQLException {
        String sql = "SELECT id_docente, cedula, nombre, apellido, id_universidad, programa_que_supervisa FROM docente ORDER BY apellido, nombre";
        try (Connection c = ds.getConnection();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            List<Docente> list = new ArrayList<>();
            while (rs.next()) {
                list.add(map(rs));
            }
            return list;
        }
    }

    public void insert(Docente d) throws SQLException {
        String sql = "INSERT INTO docente (cedula, nombre, apellido, id_universidad, programa_que_supervisa) VALUES (?,?,?,?,?)";
        try (Connection c = ds.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, d.cedula());
            ps.setString(2, d.nombre());
            ps.setString(3, d.apellido());
            ps.setLong(4, d.idUniversidad());
            ps.setString(5, d.programaQueSupervisa());
            ps.executeUpdate();
        }
    }

    public void update(Docente d) throws SQLException {
        String sql = "UPDATE docente SET cedula=?, nombre=?, apellido=?, id_universidad=?, programa_que_supervisa=? WHERE id_docente=?";
        try (Connection c = ds.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, d.cedula());
            ps.setString(2, d.nombre());
            ps.setString(3, d.apellido());
            ps.setLong(4, d.idUniversidad());
            ps.setString(5, d.programaQueSupervisa());
            ps.setLong(6, d.idDocente());
            ps.executeUpdate();
        }
    }

    private static Docente map(ResultSet rs) throws SQLException {
        return new Docente(
                rs.getLong("id_docente"),
                rs.getString("cedula"),
                rs.getString("nombre"),
                rs.getString("apellido"),
                rs.getLong("id_universidad"),
                rs.getString("programa_que_supervisa"));
    }
}
