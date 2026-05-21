-- ============================================================
-- HUSRT-Control — Datos demo
-- Contraseña de todos los usuarios: password
-- Hash BCrypt generado con strength=10
-- ============================================================

SET NAMES utf8mb4;
SET time_zone = 'America/Bogota';

-- ------------------------------------------------------------
-- Universidad
-- ------------------------------------------------------------
INSERT INTO universidad (id_universidad, nombre, ciudad, tipo_convenio) VALUES
(1, 'Universidad Nacional de Colombia', 'Bogotá',      'CONVENIO_DOCENCIA'),
(2, 'Universidad de Antioquia',          'Medellín',    'CONVENIO_DOCENCIA'),
(3, 'Universidad del Rosario',           'Bogotá',      'CONVENIO_INVESTIGACION');

-- ------------------------------------------------------------
-- Servicios hospitalarios
-- ------------------------------------------------------------
INSERT INTO servicio_hospitalario (id_servicio, nombre, piso, capacidad_maxima_estudiantes) VALUES
(1, 'Urgencias',          'Piso 1',  8),
(2, 'UCI Adultos',        'Piso 3',  4),
(3, 'Medicina Interna',   'Piso 2',  6),
(4, 'Cirugía General',    'Piso 4',  5),
(5, 'Pediatría',          'Piso 2',  5);

-- ------------------------------------------------------------
-- Docentes
-- ------------------------------------------------------------
INSERT INTO docente (id_docente, cedula, nombre, apellido, id_universidad, programa_que_supervisa) VALUES
(1, '80000001', 'Carlos',   'Ramírez',  1, 'Enfermería'),
(2, '80000002', 'Laura',    'Gómez',    2, 'Medicina'),
(3, '80000003', 'Andrés',   'Torres',   1, 'Fisioterapia');

-- ------------------------------------------------------------
-- Estudiantes
-- ------------------------------------------------------------
INSERT INTO estudiante (id_estudiante, cedula, nombre, apellido, foto_url, programa_academico,
                        semestre_academico, id_universidad, induccion_completada, fecha_induccion,
                        arl_vigencia_inicio, arl_vigencia_fin, estado) VALUES
(1, '1090123456', 'María',   'López',    NULL, 'Enfermería',   4, 1, TRUE, '2026-01-15', '2026-01-01', '2026-12-31', 'ACTIVO'),
(2, '1090234567', 'Juan',    'Martínez', NULL, 'Medicina',     6, 2, TRUE, '2026-01-20', '2026-01-01', '2026-12-31', 'ACTIVO'),
(3, '1090345678', 'Ana',     'Sánchez',  NULL, 'Fisioterapia', 5, 1, TRUE, '2026-02-01', '2026-01-01', '2026-12-31', 'ACTIVO'),
(4, '1090456789', 'Pedro',   'García',   NULL, 'Enfermería',   3, 1, FALSE, NULL,          NULL,          NULL,         'ACTIVO'),
(5, '1090567890', 'Sofía',   'Vargas',   NULL, 'Medicina',     7, 2, TRUE, '2025-07-10', '2025-07-01', '2025-12-31', 'ACTIVO');

-- ------------------------------------------------------------
-- Inscripciones semestrales (semestre 1/2026 = periodo 1)
-- ------------------------------------------------------------
INSERT INTO inscripcion_semestral (id_estudiante, anio, periodo, activo) VALUES
(1, 2026, 1, TRUE),
(2, 2026, 1, TRUE),
(3, 2026, 1, TRUE),
(4, 2026, 1, TRUE),
(5, 2026, 1, TRUE);

-- ------------------------------------------------------------
-- Plan de prácticas (Mayo 2026 — mes actual del demo)
-- ------------------------------------------------------------
INSERT INTO plan_practicas (id_plan, id_docente, id_universidad, semestre, mes, anio, periodo, fecha_carga) VALUES
(1, 1, 1, '2026-1', 5, 2026, 1, '2026-05-01'),
(2, 2, 2, '2026-1', 5, 2026, 1, '2026-05-01');

-- ------------------------------------------------------------
-- Asignaciones de práctica
-- dia_semana ISO: 1=Lun, 2=Mar, 3=Mié, 4=Jue, 5=Vie, 6=Sáb, 7=Dom
-- Franja amplia (06:00–22:00) para facilitar la demo
-- ------------------------------------------------------------
INSERT INTO asignacion_practica (id_plan, id_estudiante, id_servicio, dia_semana, hora_inicio, hora_fin, fecha_especifica) VALUES
-- Plan 1: Estudiante 1 (1090123456) — Lun, Mar, Mié, Jue, Vie en Urgencias
(1, 1, 1, 1, '06:00:00', '22:00:00', NULL),
(1, 1, 1, 2, '06:00:00', '22:00:00', NULL),
(1, 1, 1, 3, '06:00:00', '22:00:00', NULL),
(1, 1, 1, 4, '06:00:00', '22:00:00', NULL),
(1, 1, 1, 5, '06:00:00', '22:00:00', NULL),
-- Plan 1: Estudiante 3 — Urgencias
(1, 3, 1, 1, '06:00:00', '22:00:00', NULL),
(1, 3, 1, 4, '06:00:00', '22:00:00', NULL),
-- Plan 2: Estudiante 2 — UCI
(2, 2, 2, 2, '06:00:00', '22:00:00', NULL),
(2, 2, 2, 5, '06:00:00', '22:00:00', NULL);

-- ------------------------------------------------------------
-- Requisitos de horas por programa
-- ------------------------------------------------------------
INSERT INTO programa_requisito_horas (programa_academico, horas_requeridas_semestre) VALUES
('Enfermería',   240),
('Medicina',     320),
('Fisioterapia', 200),
('Bacteriología',180);

-- ------------------------------------------------------------
-- Usuarios del sistema
-- Contraseña: "password"  →  hash BCrypt strength-10
-- ------------------------------------------------------------
SET @hash = '$2a$10$BmYQI1MP0b7p4qAqJhmGh.RNXTGahEinn5CsSuNvHh/V7amtCbWma';

INSERT INTO usuario_sistema (nombre_usuario, contrasena_hash, rol, activo, id_estudiante) VALUES
('admin',        @hash, 'ADMINISTRADOR', TRUE,  NULL),
('coordinador',  @hash, 'COORDINADOR',   TRUE,  NULL),
('porteria',     @hash, 'PORTERIA',      TRUE,  NULL),
('consulta',     @hash, 'CONSULTA',      TRUE,  NULL),
('1090123456',   @hash, 'ESTUDIANTE',    TRUE,  1);
