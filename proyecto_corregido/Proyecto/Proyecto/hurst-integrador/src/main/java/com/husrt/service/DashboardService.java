package com.husrt.service;

import com.husrt.repository.AlertaRepository;
import com.husrt.repository.DocenteRepository;
import com.husrt.repository.EstudianteRepository;
import com.husrt.repository.RegistroAccesoRepository;

import java.sql.SQLException;
import java.util.List;

public class DashboardService {

    private final EstudianteRepository estudiantes = new EstudianteRepository();
    private final DocenteRepository docentes = new DocenteRepository();
    private final RegistroAccesoRepository registros = new RegistroAccesoRepository();
    private final AlertaRepository alertas = new AlertaRepository();

    public record DashboardKpis(
            int estudiantesActivos,
            int profesores,
            int presentesAhora,
            int alertasActivas,
            int asistieronHoy,
            int accesosRechazadosHoy,
            double porcentajePresentes
    ) {
    }

    public DashboardKpis cargarKpis() throws SQLException {
        int activos = estudiantes.countActivos();
        int prof = docentes.countAll();
        int presentes = registros.listEstudiantesPresentes().size();
        int alertasN = alertas.countNoResueltas();
        int asistieron = registros.countIngresosAprobadosHoy();
        int rechazos = registros.countRechazosHoy();
        double pct = activos > 0 ? Math.min(100, presentes * 100.0 / activos) : 0;
        return new DashboardKpis(activos, prof, presentes, alertasN, asistieron, rechazos, pct);
    }

    public List<AlertaRepository.AlertaRow> alertasRecientes(int limit) throws SQLException {
        return alertas.listRecientes(limit);
    }

    public List<RegistroAccesoRepository.OcupacionServicioRow> ocupacionServicios() throws SQLException {
        return registros.ocupacionActualPorServicio();
    }
}
