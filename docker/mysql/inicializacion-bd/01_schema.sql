-- ============================================================
-- HUSRT-Control — Esquema de base de datos
-- Motor: MySQL 8.4  |  Charset: utf8mb4
-- ============================================================

SET NAMES utf8mb4;
SET time_zone = 'America/Bogota';

-- ------------------------------------------------------------
-- Universidad convenio
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS universidad (
    id_universidad BIGINT       AUTO_INCREMENT PRIMARY KEY,
    nombre         VARCHAR(200) NOT NULL,
    ciudad         VARCHAR(100) NOT NULL,
    tipo_convenio  VARCHAR(100)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ------------------------------------------------------------
-- Servicios hospitalarios
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS servicio_hospitalario (
    id_servicio                   BIGINT       AUTO_INCREMENT PRIMARY KEY,
    nombre                        VARCHAR(200) NOT NULL,
    piso                          VARCHAR(50),
    capacidad_maxima_estudiantes  INT          NOT NULL DEFAULT 5
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ------------------------------------------------------------
-- Estudiantes
-- ------------------------------------------------------------
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

-- ------------------------------------------------------------
-- Inscripción semestral
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS inscripcion_semestral (
    id_inscripcion BIGINT  AUTO_INCREMENT PRIMARY KEY,
    id_estudiante  BIGINT  NOT NULL,
    anio           INT     NOT NULL,
    periodo        INT     NOT NULL,
    activo         BOOLEAN NOT NULL DEFAULT TRUE,
    CONSTRAINT fk_insc_est FOREIGN KEY (id_estudiante)
        REFERENCES estudiante(id_estudiante)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ------------------------------------------------------------
-- Docentes
-- ------------------------------------------------------------
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

-- ------------------------------------------------------------
-- Planes de prácticas mensuales
-- ------------------------------------------------------------
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

-- ------------------------------------------------------------
-- Asignaciones de práctica (franjas horarias por estudiante)
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS asignacion_practica (
    id_asignacion   BIGINT AUTO_INCREMENT PRIMARY KEY,
    id_plan         BIGINT NOT NULL,
    id_estudiante   BIGINT NOT NULL,
    id_servicio     BIGINT NOT NULL,
    dia_semana      INT    NOT NULL,   -- ISO: 1=Lun … 7=Dom
    hora_inicio     TIME   NOT NULL,
    hora_fin        TIME   NOT NULL,
    fecha_especifica DATE,              -- NULL → aplica al dia_semana de cada semana
    CONSTRAINT fk_asig_plan FOREIGN KEY (id_plan)       REFERENCES plan_practicas(id_plan),
    CONSTRAINT fk_asig_est  FOREIGN KEY (id_estudiante) REFERENCES estudiante(id_estudiante),
    CONSTRAINT fk_asig_srv  FOREIGN KEY (id_servicio)   REFERENCES servicio_hospitalario(id_servicio)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ------------------------------------------------------------
-- Registro de acceso de estudiantes
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS registro_acceso (
    id_registro             BIGINT       AUTO_INCREMENT PRIMARY KEY,
    id_estudiante           BIGINT       NOT NULL,
    id_asignacion           BIGINT,
    timestamp_entrada       DATETIME(3)  NOT NULL,
    timestamp_salida        DATETIME(3),
    horas_cumplidas         DECIMAL(5,2),
    resultado_validacion    ENUM('APROBADO','RECHAZADO') NOT NULL,
    motivo_rechazo          TEXT,
    CONSTRAINT fk_reg_est  FOREIGN KEY (id_estudiante) REFERENCES estudiante(id_estudiante),
    CONSTRAINT fk_reg_asig FOREIGN KEY (id_asignacion) REFERENCES asignacion_practica(id_asignacion)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ------------------------------------------------------------
-- Registro de acceso de docentes
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS registro_acceso_docente (
    id_registro_docente BIGINT      AUTO_INCREMENT PRIMARY KEY,
    id_docente          BIGINT      NOT NULL,
    id_plan             BIGINT,
    timestamp_entrada   DATETIME(3) NOT NULL,
    timestamp_salida    DATETIME(3),
    CONSTRAINT fk_regd_doc  FOREIGN KEY (id_docente) REFERENCES docente(id_docente),
    CONSTRAINT fk_regd_plan FOREIGN KEY (id_plan)    REFERENCES plan_practicas(id_plan)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ------------------------------------------------------------
-- Alertas del sistema
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS alerta (
    id_alerta             BIGINT       AUTO_INCREMENT PRIMARY KEY,
    tipo_alerta           VARCHAR(50)  NOT NULL,
    id_estudiante         BIGINT,
    id_docente            BIGINT,
    descripcion           TEXT,
    timestamp_generacion  DATETIME(3)  NOT NULL,
    resuelta              BOOLEAN      NOT NULL DEFAULT FALSE,
    CONSTRAINT fk_alt_est FOREIGN KEY (id_estudiante) REFERENCES estudiante(id_estudiante),
    CONSTRAINT fk_alt_doc FOREIGN KEY (id_docente)    REFERENCES docente(id_docente)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ------------------------------------------------------------
-- Auditoría de eventos
-- ------------------------------------------------------------
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

-- ------------------------------------------------------------
-- Requisitos de horas por programa académico
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS programa_requisito_horas (
    id                        BIGINT       AUTO_INCREMENT PRIMARY KEY,
    programa_academico        VARCHAR(200) NOT NULL UNIQUE,
    horas_requeridas_semestre INT          NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ------------------------------------------------------------
-- Usuarios del sistema
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS usuario_sistema (
    id_usuario       BIGINT       AUTO_INCREMENT PRIMARY KEY,
    nombre_usuario   VARCHAR(100) NOT NULL UNIQUE,
    contrasena_hash  VARCHAR(255) NOT NULL,
    rol              ENUM('ADMINISTRADOR','COORDINADOR','PORTERIA','CONSULTA','ESTUDIANTE') NOT NULL,
    activo           BOOLEAN      NOT NULL DEFAULT TRUE,
    id_estudiante    BIGINT,
    intentos_fallidos INT         NOT NULL DEFAULT 0,
    bloqueado_hasta  DATETIME,
    CONSTRAINT fk_usr_est FOREIGN KEY (id_estudiante)
        REFERENCES estudiante(id_estudiante)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
