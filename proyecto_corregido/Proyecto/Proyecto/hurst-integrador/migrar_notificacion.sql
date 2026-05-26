-- Migración: crear tabla notificacion si no existe
USE husrt;

CREATE TABLE IF NOT EXISTS notificacion (
    id_notificacion BIGINT       AUTO_INCREMENT PRIMARY KEY,
    id_docente      BIGINT       NOT NULL,
    id_estudiante   BIGINT       NOT NULL,
    mensaje         TEXT         NOT NULL,
    fecha_envio     DATETIME(3)  NOT NULL DEFAULT NOW(3),
    leida           BOOLEAN      NOT NULL DEFAULT FALSE,
    CONSTRAINT fk_notif_doc FOREIGN KEY (id_docente)    REFERENCES docente(id_docente),
    CONSTRAINT fk_notif_est FOREIGN KEY (id_estudiante) REFERENCES estudiante(id_estudiante)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Verificar resultado
SHOW TABLES LIKE 'notificacion';
