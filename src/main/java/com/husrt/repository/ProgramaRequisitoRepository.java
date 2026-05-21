package com.husrt.repository;

import com.husrt.db.DataSourceManager;
import com.husrt.model.ProgramaRequisitoHoras;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;

public class ProgramaRequisitoRepository {

    private final DataSource ds = DataSourceManager.get();

    public Optional<Integer> findHorasRequeridas(String programaAcademico) throws SQLException {
        String sql = "SELECT horas_requeridas_semestre FROM programa_requisito_horas WHERE programa_academico = ? LIMIT 1";
        try (Connection c = ds.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, programaAcademico);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    return Optional.empty();
                }
                return Optional.of(rs.getInt("horas_requeridas_semestre"));
            }
        }
    }

    public java.util.List<ProgramaRequisitoHoras> findAll() throws SQLException {
        String sql = "SELECT id, programa_academico, horas_requeridas_semestre FROM programa_requisito_horas ORDER BY programa_academico";
        try (Connection c = ds.getConnection();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            java.util.ArrayList<ProgramaRequisitoHoras> list = new java.util.ArrayList<>();
            while (rs.next()) {
                list.add(new ProgramaRequisitoHoras(
                        rs.getLong("id"),
                        rs.getString("programa_academico"),
                        rs.getInt("horas_requeridas_semestre")));
            }
            return list;
        }
    }
}
