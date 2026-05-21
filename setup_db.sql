-- ============================================================
-- HUSRT-Control — Setup completo para MySQL local (sin Docker)
-- Uso: mysql -u root -p < setup_db.sql
-- ============================================================

-- Base de datos y usuario
CREATE DATABASE IF NOT EXISTS husrt
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

CREATE USER IF NOT EXISTS 'husrt'@'localhost' IDENTIFIED BY 'husrt_secret';
GRANT ALL PRIVILEGES ON husrt.* TO 'husrt'@'localhost';
FLUSH PRIVILEGES;

USE husrt;

-- ============================================================
-- ESQUEMA
-- ============================================================

CREATE TABLE IF NOT EXISTS universidad (
    id_universidad BIGINT       AUTO_INCREMENT PRIMARY KEY,
    nombre         VARCHAR(200) NOT NULL,
    ciudad         VARCHAR(100) NOT NULL,
    tipo_convenio  VARCHAR(100)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS servicio_hospitalario (
    id_servicio                   BIGINT       AUTO_INCREMENT PRIMARY KEY,
    nombre                        VARCHAR(200) NOT NULL,
    piso                          VARCHAR(50),
    capacidad_maxima_estudiantes  INT          NOT NULL DEFAULT 5
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS estudiante (
    id_estudiante        BIGINT       AUTO_INCREMENT PRIMARY KEY,
    cedula               VARCHAR(30)  NOT NULL UNIQUE,
    nombre               VARCHAR(100) NOT NULL,
    apellido             VARCHAR(100) NOT NULL,
    foto_url             VARCHAR(500),
    programa_academico   VARCHAR(200) NOT NULL,
    semestre_academico   INT          NOT NULL,
    id_universidad       BIGINT       NOT NULL,
    induccion_completada BOOLEAN      NOT NULL DEFAULT FALSE,
    fecha_induccion      DATE,
    arl_vigencia_inicio  DATE,
    arl_vigencia_fin     DATE,
    estado               ENUM('ACTIVO','INACTIVO') NOT NULL DEFAULT 'ACTIVO',
    CONSTRAINT fk_est_univ FOREIGN KEY (id_universidad)
        REFERENCES universidad(id_universidad)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS inscripcion_semestral (
    id_inscripcion BIGINT  AUTO_INCREMENT PRIMARY KEY,
    id_estudiante  BIGINT  NOT NULL,
    anio           INT     NOT NULL,
    periodo        INT     NOT NULL,
    activo         BOOLEAN NOT NULL DEFAULT TRUE,
    CONSTRAINT fk_insc_est FOREIGN KEY (id_estudiante)
        REFERENCES estudiante(id_estudiante)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS docente (
    id_docente             BIGINT       AUTO_INCREMENT PRIMARY KEY,
    cedula                 VARCHAR(30)  NOT NULL UNIQUE,
    nombre                 VARCHAR(100) NOT NULL,
    apellido               VARCHAR(100) NOT NULL,
    id_universidad         BIGINT       NOT NULL,
    programa_que_supervisa VARCHAR(200),
    CONSTRAINT fk_doc_univ FOREIGN KEY (id_universidad)
        REFERENCES universidad(id_universidad)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS plan_practicas (
    id_plan        BIGINT       AUTO_INCREMENT PRIMARY KEY,
    id_docente     BIGINT       NOT NULL,
    id_universidad BIGINT       NOT NULL,
    semestre       VARCHAR(20)  NOT NULL,
    mes            INT          NOT NULL,
    anio           INT          NOT NULL,
    periodo        INT          NOT NULL,
    fecha_carga    DATE         NOT NULL,
    CONSTRAINT fk_plan_doc  FOREIGN KEY (id_docente)     REFERENCES docente(id_docente),
    CONSTRAINT fk_plan_univ FOREIGN KEY (id_universidad) REFERENCES universidad(id_universidad)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS asignacion_practica (
    id_asignacion    BIGINT AUTO_INCREMENT PRIMARY KEY,
    id_plan          BIGINT NOT NULL,
    id_estudiante    BIGINT NOT NULL,
    id_servicio      BIGINT NOT NULL,
    dia_semana       INT    NOT NULL,
    hora_inicio      TIME   NOT NULL,
    hora_fin         TIME   NOT NULL,
    fecha_especifica DATE,
    CONSTRAINT fk_asig_plan FOREIGN KEY (id_plan)       REFERENCES plan_practicas(id_plan),
    CONSTRAINT fk_asig_est  FOREIGN KEY (id_estudiante) REFERENCES estudiante(id_estudiante),
    CONSTRAINT fk_asig_srv  FOREIGN KEY (id_servicio)   REFERENCES servicio_hospitalario(id_servicio)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS registro_acceso (
    id_registro          BIGINT      AUTO_INCREMENT PRIMARY KEY,
    id_estudiante        BIGINT      NOT NULL,
    id_asignacion        BIGINT,
    timestamp_entrada    DATETIME(3) NOT NULL,
    timestamp_salida     DATETIME(3),
    horas_cumplidas      DECIMAL(5,2),
    resultado_validacion ENUM('APROBADO','RECHAZADO') NOT NULL,
    motivo_rechazo       TEXT,
    CONSTRAINT fk_reg_est  FOREIGN KEY (id_estudiante) REFERENCES estudiante(id_estudiante),
    CONSTRAINT fk_reg_asig FOREIGN KEY (id_asignacion) REFERENCES asignacion_practica(id_asignacion)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS registro_acceso_docente (
    id_registro_docente BIGINT      AUTO_INCREMENT PRIMARY KEY,
    id_docente          BIGINT      NOT NULL,
    id_plan             BIGINT,
    timestamp_entrada   DATETIME(3) NOT NULL,
    timestamp_salida    DATETIME(3),
    CONSTRAINT fk_regd_doc  FOREIGN KEY (id_docente) REFERENCES docente(id_docente),
    CONSTRAINT fk_regd_plan FOREIGN KEY (id_plan)    REFERENCES plan_practicas(id_plan)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS alerta (
    id_alerta            BIGINT      AUTO_INCREMENT PRIMARY KEY,
    tipo_alerta          VARCHAR(50) NOT NULL,
    id_estudiante        BIGINT,
    id_docente           BIGINT,
    descripcion          TEXT,
    timestamp_generacion DATETIME(3) NOT NULL,
    resuelta             BOOLEAN     NOT NULL DEFAULT FALSE,
    CONSTRAINT fk_alt_est FOREIGN KEY (id_estudiante) REFERENCES estudiante(id_estudiante),
    CONSTRAINT fk_alt_doc FOREIGN KEY (id_docente)    REFERENCES docente(id_docente)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS auditoria_evento (
    id_evento        BIGINT       AUTO_INCREMENT PRIMARY KEY,
    timestamp_evento DATETIME(3)  NOT NULL DEFAULT NOW(3),
    id_usuario       BIGINT,
    nombre_usuario   VARCHAR(100),
    rol              VARCHAR(50),
    modulo           VARCHAR(100),
    accion           VARCHAR(200),
    detalle          TEXT,
    entidad_ref      VARCHAR(200)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS programa_requisito_horas (
    id                        BIGINT       AUTO_INCREMENT PRIMARY KEY,
    programa_academico        VARCHAR(200) NOT NULL UNIQUE,
    horas_requeridas_semestre INT          NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS usuario_sistema (
    id_usuario        BIGINT       AUTO_INCREMENT PRIMARY KEY,
    nombre_usuario    VARCHAR(100) NOT NULL UNIQUE,
    contrasena_hash   VARCHAR(255) NOT NULL,
    rol               ENUM('ADMINISTRADOR','COORDINADOR','PORTERIA','CONSULTA','ESTUDIANTE') NOT NULL,
    activo            BOOLEAN      NOT NULL DEFAULT TRUE,
    id_estudiante     BIGINT,
    intentos_fallidos INT          NOT NULL DEFAULT 0,
    bloqueado_hasta   DATETIME,
    CONSTRAINT fk_usr_est FOREIGN KEY (id_estudiante)
        REFERENCES estudiante(id_estudiante)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ============================================================
-- DATOS DEMO  (contraseña de todos los usuarios: "password")
-- ============================================================

INSERT INTO universidad (id_universidad, nombre, ciudad, tipo_convenio) VALUES
(1, 'Universidad Nacional de Colombia', 'Bogotá',   'CONVENIO_DOCENCIA'),
(2, 'Universidad de Antioquia',          'Medellín', 'CONVENIO_DOCENCIA'),
(3, 'Universidad del Rosario',           'Bogotá',   'CONVENIO_INVESTIGACION');

INSERT INTO servicio_hospitalario (id_servicio, nombre, piso, capacidad_maxima_estudiantes) VALUES
(1, 'Urgencias',        'Piso 1', 8),
(2, 'UCI Adultos',      'Piso 3', 4),
(3, 'Medicina Interna', 'Piso 2', 6),
(4, 'Cirugía General',  'Piso 4', 5),
(5, 'Pediatría',        'Piso 2', 5);

INSERT INTO docente (id_docente, cedula, nombre, apellido, id_universidad, programa_que_supervisa) VALUES
(1, '80000001', 'Carlos', 'Ramírez', 1, 'Enfermería'),
(2, '80000002', 'Laura',  'Gómez',   2, 'Medicina'),
(3, '80000003', 'Andrés', 'Torres',  1, 'Fisioterapia');

INSERT INTO estudiante (id_estudiante, cedula, nombre, apellido, foto_url, programa_academico,
                        semestre_academico, id_universidad, induccion_completada, fecha_induccion,
                        arl_vigencia_inicio, arl_vigencia_fin, estado) VALUES
(1, '1090123456', 'María', 'López',    NULL, 'Enfermería',   4, 1, TRUE,  '2026-01-15', '2026-01-01', '2026-12-31', 'ACTIVO'),
(2, '1090234567', 'Juan',  'Martínez', NULL, 'Medicina',     6, 2, TRUE,  '2026-01-20', '2026-01-01', '2026-12-31', 'ACTIVO'),
(3, '1090345678', 'Ana',   'Sánchez',  NULL, 'Fisioterapia', 5, 1, TRUE,  '2026-02-01', '2026-01-01', '2026-12-31', 'ACTIVO'),
(4, '1090456789', 'Pedro', 'García',   NULL, 'Enfermería',   3, 1, FALSE, NULL,          NULL,          NULL,         'ACTIVO'),
(5, '1090567890', 'Sofía', 'Vargas',   NULL, 'Medicina',     7, 2, TRUE,  '2025-07-10', '2025-07-01', '2025-12-31', 'ACTIVO');

INSERT INTO inscripcion_semestral (id_estudiante, anio, periodo, activo) VALUES
(1, 2026, 1, TRUE),
(2, 2026, 1, TRUE),
(3, 2026, 1, TRUE),
(4, 2026, 1, TRUE),
(5, 2026, 1, TRUE);

-- Plan para Mayo 2026
INSERT INTO plan_practicas (id_plan, id_docente, id_universidad, semestre, mes, anio, periodo, fecha_carga) VALUES
(1, 1, 1, '2026-1', 5, 2026, 1, '2026-05-01'),
(2, 2, 2, '2026-1', 5, 2026, 1, '2026-05-01');

-- Asignaciones: dia_semana ISO (1=Lun … 7=Dom), franja 06:00–22:00
INSERT INTO asignacion_practica (id_plan, id_estudiante, id_servicio, dia_semana, hora_inicio, hora_fin, fecha_especifica) VALUES
(1, 1, 1, 1, '06:00:00', '22:00:00', NULL),
(1, 1, 1, 2, '06:00:00', '22:00:00', NULL),
(1, 1, 1, 3, '06:00:00', '22:00:00', NULL),
(1, 1, 1, 4, '06:00:00', '22:00:00', NULL),
(1, 1, 1, 5, '06:00:00', '22:00:00', NULL),
(1, 3, 1, 1, '06:00:00', '22:00:00', NULL),
(1, 3, 1, 4, '06:00:00', '22:00:00', NULL),
(2, 2, 2, 2, '06:00:00', '22:00:00', NULL),
(2, 2, 2, 5, '06:00:00', '22:00:00', NULL);

INSERT INTO programa_requisito_horas (programa_academico, horas_requeridas_semestre) VALUES
('Enfermería',    240),
('Medicina',      320),
('Fisioterapia',  200),
('Bacteriología', 180);

-- Usuarios demo — contraseña: "password"
SET @hash = '$2a$10$BmYQI1MP0b7p4qAqJhmGh.RNXTGahEinn5CsSuNvHh/V7amtCbWma';

INSERT INTO usuario_sistema (nombre_usuario, contrasena_hash, rol, activo, id_estudiante) VALUES
('admin',       @hash, 'ADMINISTRADOR', TRUE, NULL),
('coordinador', @hash, 'COORDINADOR',   TRUE, NULL),
('porteria',    @hash, 'PORTERIA',      TRUE, NULL),
('consulta',    @hash, 'CONSULTA',      TRUE, NULL),
('1090123456',  @hash, 'ESTUDIANTE',    TRUE, 1);
