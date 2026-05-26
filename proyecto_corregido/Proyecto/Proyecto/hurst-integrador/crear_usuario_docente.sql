-- Reparar usuario docente (contraseña: password)
USE husrt;

ALTER TABLE usuario_sistema MODIFY rol VARCHAR(50) NOT NULL;

INSERT INTO docente (id_docente, cedula, nombre, apellido, id_universidad, programa_que_supervisa)
VALUES (1, '80000001', 'Carlos', 'Ramírez', 1, 'Enfermería')
ON DUPLICATE KEY UPDATE nombre = VALUES(nombre);

SET @hash = '$2a$10$BmYQI1MP0b7p4qAqJhmGh.RNXTGahEinn5CsSuNvHh/V7amtCbWma';

INSERT INTO usuario_sistema (nombre_usuario, contrasena_hash, rol, activo, id_estudiante, id_docente)
VALUES ('docente', @hash, 'DOCENTE', TRUE, NULL, 1)
ON DUPLICATE KEY UPDATE
    contrasena_hash = VALUES(contrasena_hash),
    rol = 'DOCENTE',
    activo = TRUE,
    id_docente = 1,
    intentos_fallidos = 0,
    bloqueado_hasta = NULL;

SELECT nombre_usuario, rol, activo, id_docente FROM usuario_sistema WHERE nombre_usuario = 'docente';
