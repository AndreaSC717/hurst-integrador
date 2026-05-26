package com.husrt.service;

import com.husrt.model.AsignacionPractica;
import com.husrt.model.Docente;
import com.husrt.model.EstadoEstudiante;
import com.husrt.model.Estudiante;
import com.husrt.model.PlanPracticas;
import com.husrt.model.ResultadoValidacion;
import com.husrt.model.ServicioHospitalario;
import com.husrt.repository.AlertaRepository;
import com.husrt.repository.DocenteRepository;
import com.husrt.repository.EstudianteRepository;
import com.husrt.repository.PlanPracticasRepository;
import com.husrt.repository.RegistroAccesoRepository;
import com.husrt.repository.RegistroDocenteRepository;
import com.husrt.repository.ServicioRepository;
import com.husrt.util.PeriodoAcademico;

import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Optional;

public class AccesoService {

    private final EstudianteRepository estudiantes = new EstudianteRepository();
    private final PlanPracticasRepository planes = new PlanPracticasRepository();
    private final RegistroAccesoRepository registros = new RegistroAccesoRepository();
    private final RegistroDocenteRepository regDocentes = new RegistroDocenteRepository();
    private final DocenteRepository docentes = new DocenteRepository();
    private final ServicioRepository servicios = new ServicioRepository();
    private final AlertaRepository alertas = new AlertaRepository();

    public record ResultadoIngreso(boolean permitido, String mensaje) {
    }

    public ResultadoIngreso intentarIngresoEstudiante(String cedula) throws SQLException {
        LocalDate hoy = LocalDate.now();
        LocalTime ahora = LocalTime.now();
        int anio = PeriodoAcademico.anioDeFecha(hoy);
        int periodo = PeriodoAcademico.periodoDeFecha(hoy);
        int mes = hoy.getMonthValue();
        int diaSemanaIso = hoy.getDayOfWeek().getValue();

        Optional<Estudiante> op = estudiantes.findByCedula(cedula);
        if (op.isEmpty()) {
            return rechazoSinEst(null, cedula, "Estudiante no registrado.");
        }
        Estudiante e = op.get();
        long idEst = e.idEstudiante();

        if (e.estado() != EstadoEstudiante.ACTIVO) {
            return rechazo(idEst, null, "Estudiante inactivo.");
        }
        if (!estudiantes.existsInscripcionActiva(idEst, anio, periodo)) {
            return rechazo(idEst, null, "No tiene inscripción activa en el semestre actual.");
        }
        if (!e.induccionCompletada()) {
            alertas.insert("INDUCCION", idEst, null, "Check-in attempt without orientation.");
            return rechazo(idEst, null, "Inducción hospitalaria no completada.");
        }
        if (e.arlInicio() == null || e.arlFin() == null || hoy.isBefore(e.arlInicio()) || hoy.isAfter(e.arlFin())) {
            alertas.insert("ARL", idEst, null, "Check-in attempt with invalid OSH insurance.");
            return rechazo(idEst, null, "ARL no vigente en la fecha de ingreso.");
        }

        Optional<AsignacionPractica> asig = planes.findAsignacionValidaIngreso(idEst, anio, periodo, mes, hoy, diaSemanaIso, ahora);
        if (asig.isEmpty()) {
            alertas.insert("FRANJA", idEst, null, "Check-in outside time slot or no plan for current month.");
            return rechazo(idEst, null, "No hay franja habilitada en el plan de prácticas para este momento.");
        }
        AsignacionPractica a = asig.get();
        Optional<PlanPracticas> planOp = planes.findById(a.idPlan());
        if (planOp.isEmpty()) {
            return rechazo(idEst, a.idAsignacion(), "Plan de prácticas no encontrado.");
        }
        PlanPracticas plan = planOp.get();
        if (!regDocentes.tieneEntradaAbiertaParaPlan(plan.idDocente(), plan.idPlan())) {
            alertas.insert("DOCENTE", idEst, plan.idDocente(), "Check-in attempt without responsible instructor on site for the plan.");
            return rechazo(idEst, a.idAsignacion(), "El docente responsable aún no ha registrado ingreso para este plan.");
        }

        if (registros.tieneEntradaAbierta(idEst)) {
            return rechazo(idEst, a.idAsignacion(), "El estudiante ya tiene un ingreso abierto sin salida registrada.");
        }

        Optional<ServicioHospitalario> srv = servicios.findById(a.idServicio());
        if (srv.isEmpty()) {
            return rechazo(idEst, a.idAsignacion(), "Servicio hospitalario no encontrado.");
        }
        int dentro = registros.countPresentesEnServicio(a.idServicio());
        int asignados = planes.countAsignadosMismaFranja(
                a.idPlan(), a.idServicio(), a.diaSemana(), a.fechaEspecifica(), a.horaInicio(), a.horaFin());
        if (asignados >= srv.get().capacidadMaximaEstudiantes()) {
            alertas.insert("CAPACIDAD_PLAN", idEst, null,
                    "Plan assigned maximum students (" + asignados + ") for " + srv.get().nombre() + " in this slot.");
        }
        if (dentro >= srv.get().capacidadMaximaEstudiantes()) {
            alertas.insert("CAPACIDAD", idEst, null, "Service at capacity with students inside.");
            return rechazo(idEst, a.idAsignacion(), "Servicio en capacidad máxima: ya hay el máximo de estudiantes dentro.");
        }

        registros.insertAprobadoEntrada(idEst, a.idAsignacion());
        return new ResultadoIngreso(true, "Ingreso autorizado. Bienvenido(a).");
    }

    private ResultadoIngreso rechazoSinEst(Long idEst, String cedula, String msg) throws SQLException {
        if (idEst != null) {
            registros.insertIntento(idEst, null, ResultadoValidacion.RECHAZADO, msg);
        }
        return new ResultadoIngreso(false, msg);
    }

    private ResultadoIngreso rechazo(long idEst, Long idAsig, String msg) throws SQLException {
        registros.insertIntento(idEst, idAsig, ResultadoValidacion.RECHAZADO, msg);
        return new ResultadoIngreso(false, msg);
    }

    public String registrarSalidaEstudiante(String cedula) throws SQLException {
        Optional<Estudiante> op = estudiantes.findByCedula(cedula);
        if (op.isEmpty()) {
            return "Estudiante no encontrado.";
        }
        Optional<Long> idReg = registros.findRegistroAbiertoId(op.get().idEstudiante());
        if (idReg.isEmpty()) {
            return "No hay ingreso activo para registrar la salida.";
        }
        registros.cerrarSalida(idReg.get());
        return "Salida registrada correctamente.";
    }

    public String docenteEntrada(String cedulaDocente, long idPlan) throws SQLException {
        Optional<Docente> d = docentes.findByCedula(cedulaDocente);
        if (d.isEmpty()) {
            return "Docente no registrado.";
        }
        Optional<PlanPracticas> p = planes.findById(idPlan);
        if (p.isEmpty()) {
            return "Plan no encontrado.";
        }
        if (p.get().idDocente() != d.get().idDocente()) {
            alertas.insert("DOCENTE_PLAN", null, d.get().idDocente(), "Instructor attempted check-in on another plan.");
            return "Este docente no es responsable del plan seleccionado.";
        }
        if (regDocentes.tieneEntradaAbiertaParaPlan(d.get().idDocente(), idPlan)) {
            return "El docente ya tiene un ingreso activo para este plan.";
        }
        regDocentes.insertEntrada(d.get().idDocente(), idPlan);
        return "Ingreso de docente registrado.";
    }

    public String docenteSalida(String cedulaDocente, Long idPlan) throws SQLException {
        Optional<Docente> d = docentes.findByCedula(cedulaDocente);
        if (d.isEmpty()) {
            return "Docente no registrado.";
        }
        Optional<Long> idR = regDocentes.findUltimoAbiertoId(d.get().idDocente(), idPlan);
        if (idR.isEmpty()) {
            return "No hay ingreso activo de docente para cerrar.";
        }
        regDocentes.cerrarSalida(idR.get());
        return "Salida de docente registrada.";
    }
}
