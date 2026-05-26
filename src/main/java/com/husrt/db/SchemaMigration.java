package com.husrt.db;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Minimal schema adjustments for databases created with earlier versions of the SQL script.
 */
public final class SchemaMigration {

    private SchemaMigration() {
    }

    public static void ensureMinimalDemoData() {
        try (Connection c = DataSourceManager.get().getConnection(); Statement st = c.createStatement()) {
            st.executeUpdate("""
                    INSERT IGNORE INTO universidad (id_universidad, nombre, ciudad, tipo_convenio)
                    VALUES (1, 'Universidad Nacional de Colombia', 'Bogotá', 'CONVENIO_DOCENCIA')
                    """);
            st.executeUpdate("""
                    INSERT IGNORE INTO docente (id_docente, cedula, nombre, apellido, id_universidad, programa_que_supervisa)
                    VALUES (1, '80000001', 'Carlos', 'Ramírez', 1, 'Enfermería')
                    """);
            st.executeUpdate("""
                    INSERT IGNORE INTO estudiante (id_estudiante, cedula, nombre, apellido, foto_url, programa_academico,
                        semestre_academico, id_universidad, induccion_completada, estado, vacunas_completas)
                    VALUES (1, '1090123456', 'María', 'López', NULL, 'Enfermería', 4, 1, TRUE, 'ACTIVO', TRUE)
                    """);
        } catch (SQLException e) {
            System.err.println("[HUSRT] Minimal demo data: " + e.getMessage());
        }
    }

    public static void ensureUsuarioSistemaColumns() {
        try (Connection c = DataSourceManager.get().getConnection(); Statement st = c.createStatement()) {
            try {
                st.executeUpdate("""
                        ALTER TABLE usuario_sistema
                        MODIFY rol ENUM(
                            'ADMINISTRADOR','COORDINADOR','DOCENTE','PORTERIA','CONSULTA','ESTUDIANTE'
                        ) NOT NULL
                        """);
            } catch (SQLException ignored) {
                try {
                    st.executeUpdate("""
                            ALTER TABLE usuario_sistema
                            MODIFY rol VARCHAR(50) NOT NULL
                            """);
                } catch (SQLException e2) {
                    System.err.println("[HUSRT] Role column: " + e2.getMessage());
                }
            }
            try {
                st.executeUpdate("""
                        ALTER TABLE usuario_sistema
                        ADD COLUMN intentos_fallidos INT NOT NULL DEFAULT 0
                        """);
            } catch (SQLException ignored) {
            }
            try {
                st.executeUpdate("""
                        ALTER TABLE usuario_sistema
                        ADD COLUMN bloqueado_hasta DATETIME NULL
                        """);
            } catch (SQLException ignored) {
            }
            try {
                st.executeUpdate("""
                        ALTER TABLE usuario_sistema
                        ADD COLUMN id_docente BIGINT NULL
                        """);
            } catch (SQLException ignored) {
            }
        } catch (SQLException e) {
            System.err.println("[HUSRT] Schema migration: " + e.getMessage());
        }
    }
}
