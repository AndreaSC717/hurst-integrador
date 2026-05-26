-- ============================================================
-- Actualizar nombres a más realistas
-- Ejecutar este script en TablePlus conectado a la base de datos husrt
-- ============================================================

-- Actualizar nombres de docentes con nombres más realistas
UPDATE docente SET nombre = 'Carlos Alberto', apellido = 'Ramírez Rodríguez' WHERE id_docente = 1;
UPDATE docente SET nombre = 'María Laura', apellido = 'Gómez Martínez' WHERE id_docente = 2;
UPDATE docente SET nombre = 'Jorge Andrés', apellido = 'Torres Sánchez' WHERE id_docente = 3;

-- Actualizar nombres de estudiantes con nombres más realistas
UPDATE estudiante SET nombre = 'María Camila', apellido = 'López González' WHERE id_estudiante = 1;
UPDATE estudiante SET nombre = 'Juan Carlos', apellido = 'Martínez Pérez' WHERE id_estudiante = 2;
UPDATE estudiante SET nombre = 'Ana María', apellido = 'Sánchez Rodríguez' WHERE id_estudiante = 3;
UPDATE estudiante SET nombre = 'Pedro José', apellido = 'García López' WHERE id_estudiante = 4;
UPDATE estudiante SET nombre = 'Sofía Andrea', apellido = 'Vargas Martínez' WHERE id_estudiante = 5;

-- Verificar los cambios
SELECT 'DOCENTES ACTUALIZADOS:' AS mensaje;
SELECT id_docente, cedula, nombre, apellido, programa_que_supervisa FROM docente;

SELECT 'ESTUDIANTES ACTUALIZADOS:' AS mensaje;
SELECT id_estudiante, cedula, nombre, apellido, programa_academico, semestre_academico FROM estudiante;
