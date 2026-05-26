-- Repara cuentas demo (contraseña: password). Ejecutar en la base husrt si el login falla.
USE husrt;

ALTER TABLE usuario_sistema
    MODIFY rol VARCHAR(50) NOT NULL;

SET @hash = '$2a$10$BmYQI1MP0b7p4qAqJhmGh.RNXTGahEinn5CsSuNvHh/V7amtCbWma';

INSERT INTO usuario_sistema (nombre_usuario, contrasena_hash, rol, activo, id_estudiante, id_docente, intentos_fallidos, bloqueado_hasta)
VALUES
('admin',       @hash, 'ADMINISTRADOR', TRUE, NULL, NULL, 0, NULL),
('coordinador', @hash, 'COORDINADOR',   TRUE, NULL, NULL, 0, NULL),
('docente',     @hash, 'DOCENTE',       TRUE, NULL, 1,    0, NULL),
('porteria',    @hash, 'PORTERIA',      TRUE, NULL, NULL, 0, NULL),
('consulta',    @hash, 'CONSULTA',      TRUE, NULL, NULL, 0, NULL),
('1090123456',  @hash, 'ESTUDIANTE',    TRUE, 1,    NULL, 0, NULL)
ON DUPLICATE KEY UPDATE
    contrasena_hash = VALUES(contrasena_hash),
    rol = VALUES(rol),
    activo = TRUE,
    id_estudiante = VALUES(id_estudiante),
    id_docente = VALUES(id_docente),
    intentos_fallidos = 0,
    bloqueado_hasta = NULL;
