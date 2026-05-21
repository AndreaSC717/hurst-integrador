package com.husrt.service;

import com.husrt.model.Estudiante;
import com.husrt.repository.EstudianteRepository;
import com.husrt.repository.ProgramaRequisitoRepository;
import com.husrt.repository.RegistroAccesoRepository;
import com.husrt.repository.UniversidadRepository;
import com.husrt.util.PeriodoAcademico;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

public class EstudianteProgresoService {

    private final EstudianteRepository estudiantes = new EstudianteRepository();
    private final RegistroAccesoRepository registros = new RegistroAccesoRepository();
    private final ProgramaRequisitoRepository requisitos = new ProgramaRequisitoRepository();
    private final UniversidadRepository universidades = new UniversidadRepository();

    public record ResumenPracticas(
            Estudiante estudiante,
            String nombreUniversidad,
            int anio,
            int periodo,
            double horasCumplidas,
            int horasRequeridas,
            double horasFaltantes,
            double porcentaje,
            boolean arlVigente,
            boolean induccionOk,
            boolean inscripcionActiva,
            List<RegistroAccesoRepository.HistorialRow> historialReciente
    ) {
    }

    public ResumenPracticas resumenParaEstudiante(long idEstudiante) throws SQLException {
        Estudiante e = estudiantes.findById(idEstudiante)
                .orElseThrow(() -> new IllegalArgumentException("Estudiante no encontrado."));
        LocalDate hoy = LocalDate.now();
        int anio = PeriodoAcademico.anioDeFecha(hoy);
        int periodo = PeriodoAcademico.periodoDeFecha(hoy);

        double cumplidas = registros.sumHorasAprobadasSemestre(idEstudiante, anio, periodo);
        int requeridas = requisitos.findHorasRequeridas(e.programaAcademico()).orElse(160);
        double faltantes = Math.max(0, requeridas - cumplidas);
        double pct = requeridas > 0 ? Math.min(100, cumplidas * 100.0 / requeridas) : 0;

        boolean arlVigente = e.arlInicio() != null && e.arlFin() != null
                && !hoy.isBefore(e.arlInicio()) && !hoy.isAfter(e.arlFin());
        boolean inscripcion = estudiantes.existsInscripcionActiva(idEstudiante, anio, periodo);

        String uni = universidades.findById(e.idUniversidad())
                .map(u -> u.nombre())
                .orElse("—");

        LocalDate desde = hoy.minusDays(90);
        List<RegistroAccesoRepository.HistorialRow> historial =
                registros.historialEstudianteRango(idEstudiante, desde.atStartOfDay(), hoy.plusDays(1).atStartOfDay());

        return new ResumenPracticas(
                e, uni, anio, periodo, cumplidas, requeridas, faltantes, pct,
                arlVigente, e.induccionCompletada(), inscripcion, historial);
    }
}
