package com.husrt.db;

import java.sql.SQLException;

import com.husrt.model.Rol;
import com.husrt.repository.UsuarioRepository;
import com.husrt.service.AuthService;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

/**
 * Ensures demo accounts exist (password: {@code password}).
 */
public final class DemoUsersBootstrap {

    public static final String DEMO_PASSWORD = "password";

    /** BCrypt hash of "password" used in setup_db.sql */
    private static final String DEMO_HASH_SETUP =
            "$2a$10$BmYQI1MP0b7p4qAqJhmGh.RNXTGahEinn5CsSuNvHh/V7amtCbWma";

    private DemoUsersBootstrap() {
    }

    public static void ensure() {
        SchemaMigration.ensureUsuarioSistemaColumns();
        SchemaMigration.ensureMinimalDemoData();
        String hash = hashDemo();
        UsuarioRepository repo = new UsuarioRepository();
        upsert(repo, hash, "admin", Rol.ADMINISTRADOR, null, null);
        upsert(repo, hash, "coordinador", Rol.COORDINADOR, null, null);
        upsert(repo, hash, "docente", Rol.DOCENTE, null, 1L);
        upsert(repo, hash, "porteria", Rol.PORTERIA, null, null);
        upsert(repo, hash, "consulta", Rol.CONSULTA, null, null);
        upsert(repo, hash, "1090123456", Rol.ESTUDIANTE, 1L, null);
    }

    private static void upsert(UsuarioRepository repo, String hash, String user, Rol rol,
            Long idEstudiante, Long idDocente) {
        try {
            repo.upsertDemoUser(user, hash, rol, idEstudiante, idDocente);
        } catch (SQLException e) {
            try {
                if (repo.existsUsername(user)) {
                    repo.repairDemoUser(user, hash, rol, idEstudiante, idDocente);
                    return;
                }
            } catch (SQLException ex) {
                System.err.println("[HUSRT] Repair demo user '" + user + "': " + ex.getMessage());
                return;
            }
            System.err.println("[HUSRT] Demo user '" + user + "': " + e.getMessage());
        }
    }

    private static String hashDemo() {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        if (encoder.matches(DEMO_PASSWORD, DEMO_HASH_SETUP)) {
            return DEMO_HASH_SETUP;
        }
        return new AuthService().encodePassword(DEMO_PASSWORD);
    }
}
