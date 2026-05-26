package com.husrt.ui.docente;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import com.husrt.model.Docente;
import com.husrt.model.Estudiante;
import com.husrt.model.AsignacionPractica;
import com.husrt.model.PlanPracticas;
import com.husrt.model.Rol;
import com.husrt.model.ServicioHospitalario;
import com.husrt.repository.DocenteRepository;
import com.husrt.repository.EstudianteRepository;
import com.husrt.repository.PlanPracticasRepository;
import com.husrt.repository.ServicioRepository;
import com.husrt.service.HorarioService;
import com.husrt.service.NotificacionService;
import com.husrt.session.SessionContext;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.FileChooser;

public class DocenteController {

    @FXML
    private Label lblNombreDocente;
    @FXML
    private TableView<PracticaRow> tablaPracticas;
    @FXML
    private TableColumn<PracticaRow, String> colPlan;
    @FXML
    private TableColumn<PracticaRow, String> colServicio;
    @FXML
    private TableColumn<PracticaRow, String> colPeriodo;
    @FXML
    private TableColumn<PracticaRow, String> colEstudiantes;
    @FXML
    private TableView<EstudianteRow> tablaEstudiantes;
    @FXML
    private TableColumn<EstudianteRow, String> colEstCedula;
    @FXML
    private TableColumn<EstudianteRow, String> colEstNombre;
    @FXML
    private TableColumn<EstudianteRow, String> colEstEstado;
    @FXML
    private ListView<String> listaNotificaciones;
    @FXML
    private Label msgDocente;

    private final DocenteRepository docenteRepo = new DocenteRepository();
    private final EstudianteRepository estudianteRepo = new EstudianteRepository();
    private final PlanPracticasRepository planRepo = new PlanPracticasRepository();
    private final ServicioRepository servicioRepo = new ServicioRepository();
    private final HorarioService horarioService = new HorarioService();
    private final NotificacionService notificacionService = new NotificacionService();

    private Docente docenteActual;

    @FXML
    private void initialize() {
        try {
            cargarDatosDocente();
            configurarTablaPracticas();
            configurarTablaEstudiantes();
            cargarNotificaciones();
        } catch (SQLException e) {
            msgDocente.setText("Error al cargar datos: " + e.getMessage());
        }
    }

    private void cargarDatosDocente() throws SQLException {
        var usuario = SessionContext.getCurrent();
        if (usuario == null) {
            msgDocente.setText("No hay sesión activa");
            return;
        }

        Long idDocente = usuario.idDocente();

        if (idDocente == null && SessionContext.hasRole(Rol.ADMINISTRADOR, Rol.COORDINADOR)) {
            lblNombreDocente.setText("Vista de todos los docentes");
            return;
        }

        if (idDocente == null) {
            msgDocente.setText("Su cuenta no está vinculada a un registro de docente. Contacte al administrador.");
            return;
        }

        docenteActual = docenteRepo.findById(idDocente).orElse(null);
        if (docenteActual == null) {
            msgDocente.setText("No se encontró información del docente");
            return;
        }

        lblNombreDocente.setText(docenteActual.nombre() + " " + docenteActual.apellido()
                + " — " + docenteActual.programaQueSupervisa());
    }

    private void configurarTablaPracticas() {
        colPlan.setCellValueFactory(new PropertyValueFactory<>("plan"));
        colServicio.setCellValueFactory(new PropertyValueFactory<>("servicio"));
        colPeriodo.setCellValueFactory(new PropertyValueFactory<>("periodo"));
        colEstudiantes.setCellValueFactory(new PropertyValueFactory<>("estudiantes"));

        cargarPracticas();
    }

    private void cargarPracticas() {
        try {
            List<PlanPracticas> todos = planRepo.findAll();
            List<PlanPracticas> planes = (docenteActual != null)
                    ? todos.stream().filter(p -> p.idDocente() == docenteActual.idDocente()).toList()
                    : todos;
            var serviciosPorId = servicioRepo.findAll().stream()
                    .collect(Collectors.toMap(ServicioHospitalario::idServicio, ServicioHospitalario::nombre));

            List<PracticaRow> filas = new java.util.ArrayList<>();
            for (PlanPracticas p : planes) {
                List<AsignacionPractica> asigs = planRepo.listAsignacionesByPlan(p.idPlan());
                String servicios = asigs.stream()
                        .map(a -> serviciosPorId.getOrDefault(a.idServicio(), "Servicio"))
                        .distinct()
                        .collect(Collectors.joining(", "));
                if (servicios.isEmpty()) {
                    servicios = "Sin asignaciones";
                }
                String planLabel = p.semestre() + " (año " + p.anio() + ", P" + p.periodo() + ")";
                filas.add(new PracticaRow(
                        p.idPlan(),
                        p.idDocente(),
                        planLabel,
                        servicios,
                        p.anio() + "-" + p.periodo(),
                        asigs.size() + " estudiante(s)"));
            }
            tablaPracticas.setItems(FXCollections.observableArrayList(filas));
        } catch (SQLException e) {
            msgDocente.setText("Error al cargar planes: " + e.getMessage());
        }
    }

    private void configurarTablaEstudiantes() {
        colEstCedula.setCellValueFactory(new PropertyValueFactory<>("cedula"));
        colEstNombre.setCellValueFactory(new PropertyValueFactory<>("nombre"));
        colEstEstado.setCellValueFactory(new PropertyValueFactory<>("estado"));

        cargarEstudiantes();
    }

    private void cargarEstudiantes() {
        try {
            Set<Long> idsEstudiantes = new LinkedHashSet<>();
            List<PlanPracticas> planes = planRepo.findAll();
            for (PlanPracticas p : planes) {
                if (docenteActual != null && p.idDocente() != docenteActual.idDocente()) {
                    continue;
                }
                for (AsignacionPractica a : planRepo.listAsignacionesByPlan(p.idPlan())) {
                    idsEstudiantes.add(a.idEstudiante());
                }
            }
            List<EstudianteRow> filas = new java.util.ArrayList<>();
            for (Estudiante e : estudianteRepo.findAll()) {
                if (idsEstudiantes.isEmpty() || idsEstudiantes.contains(e.idEstudiante())) {
                    filas.add(new EstudianteRow(e.cedula(), e.nombre() + " " + e.apellido(), e.estado().label()));
                }
            }
            tablaEstudiantes.setItems(FXCollections.observableArrayList(filas));
        } catch (SQLException e) {
            msgDocente.setText("Error al cargar estudiantes: " + e.getMessage());
        }
    }

    @FXML
    private void onGenerarHorarioPdf() {
        msgDocente.setText("");
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Guardar horario en PDF");
        chooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("PDF", "*.pdf"));
        File file = chooser.showSaveDialog(msgDocente.getScene().getWindow());
        if (file == null) {
            return;
        }
        try {
            horarioService.generarHorarioDocente(file.toPath());
            msgDocente.setText("PDF de horario generado correctamente: " + file.getName());
        } catch (IOException | SQLException e) {
            msgDocente.setText("Error al generar PDF: " + e.getMessage());
        }
    }

    @FXML
    private void onModificarClase() {
        msgDocente.setText("");
        PracticaRow seleccionada = tablaPracticas.getSelectionModel().getSelectedItem();
        if (seleccionada == null) {
            msgDocente.setText("Seleccione un plan de la tabla antes de modificar la clase.");
            return;
        }
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/husrt/ui/docente/modificar_clase_dialog.fxml"));
            Dialog<ButtonType> dialog = new Dialog<>();
            dialog.setDialogPane(loader.load());
            dialog.setTitle("Modificar clase");

            ModificarClaseDialogController controller = loader.getController();
            controller.cargarPlan(seleccionada.getIdPlan());

            Optional<ButtonType> result = dialog.showAndWait();
            if (result.isEmpty() || result.get().getButtonData() != ButtonBar.ButtonData.OK_DONE) {
                return;
            }

            String errorGuardar = controller.guardarCambios();
            if (errorGuardar != null) {
                msgDocente.setText(errorGuardar);
                return;
            }

            long idDocente = docenteActual != null
                    ? docenteActual.idDocente()
                    : seleccionada.getIdDocente();
            if (idDocente <= 0) {
                msgDocente.setText("No se pudo identificar el docente del plan seleccionado.");
                return;
            }

            String mensaje = controller.getMensaje();
            if (mensaje.isEmpty()) {
                var plan = controller.getPlan();
                mensaje = "El docente ha modificado el horario del plan "
                        + plan.semestre() + " (año " + plan.anio() + ", periodo " + plan.periodo()
                        + "). Revise su nueva franja horaria en el sistema.";
            }

            for (Long idEst : controller.estudiantesDelPlan()) {
                notificacionService.enviarNotificacion(idDocente, idEst, mensaje);
            }

            msgDocente.setText("Clase actualizada y notificaciones enviadas a los estudiantes del plan.");
            msgDocente.getStyleClass().removeAll("message-error", "page-subtitle");
            if (!msgDocente.getStyleClass().contains("message-ok")) {
                msgDocente.getStyleClass().add("message-ok");
            }
            cargarPracticas();
            cargarEstudiantes();
            cargarNotificaciones();
        } catch (IOException e) {
            msgDocente.setText("Error al cargar el diálogo: " + e.getMessage());
        } catch (IllegalArgumentException e) {
            msgDocente.setText(e.getMessage());
        } catch (SQLException e) {
            msgDocente.setText("Error al modificar la clase: " + e.getMessage());
        }
    }

    @FXML
    private void onVerDetallesEstudiante() {
        EstudianteRow seleccionado = tablaEstudiantes.getSelectionModel().getSelectedItem();
        if (seleccionado == null) {
            msgDocente.setText("Seleccione un estudiante para ver los detalles");
            return;
        }
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/husrt/ui/docente/detalles_estudiante_dialog.fxml"));
            Dialog<ButtonType> dialog = new Dialog<>();
            dialog.setDialogPane(loader.load());
            dialog.setTitle("Detalles del estudiante");

            DetallesEstudianteDialogController controller = loader.getController();
            controller.cargarEstudiante(seleccionado.getCedula());

            dialog.showAndWait();
        } catch (IOException e) {
            msgDocente.setText("Error al cargar el diálogo: " + e.getMessage());
        }
    }

    private void cargarNotificaciones() {
        if (docenteActual == null) {
            return;
        }
        try {
            var notificaciones = notificacionService.obtenerNotificacionesDocente(docenteActual.idDocente());
            listaNotificaciones.setItems(FXCollections.observableArrayList(
                    notificaciones.stream()
                            .map(n -> n.fechaEnvio() + " — " + n.mensaje())
                            .toList()
            ));
        } catch (SQLException e) {
            msgDocente.setText("Error al cargar notificaciones: " + e.getMessage());
        }
    }

    public static class PracticaRow {

        private final long idPlan;
        private final long idDocente;
        private final String plan;
        private final String servicio;
        private final String periodo;
        private final String estudiantes;

        public PracticaRow(long idPlan, long idDocente, String plan, String servicio, String periodo, String estudiantes) {
            this.idPlan = idPlan;
            this.idDocente = idDocente;
            this.plan = plan;
            this.servicio = servicio;
            this.periodo = periodo;
            this.estudiantes = estudiantes;
        }

        public long getIdPlan() {
            return idPlan;
        }

        public long getIdDocente() {
            return idDocente;
        }

        public String getPlan() {
            return plan;
        }

        public String getServicio() {
            return servicio;
        }

        public String getPeriodo() {
            return periodo;
        }

        public String getEstudiantes() {
            return estudiantes;
        }
    }

    public static class EstudianteRow {

        private final String cedula;
        private final String nombre;
        private final String estado;

        public EstudianteRow(String cedula, String nombre, String estado) {
            this.cedula = cedula;
            this.nombre = nombre;
            this.estado = estado;
        }

        public String getCedula() {
            return cedula;
        }

        public String getNombre() {
            return nombre;
        }

        public String getEstado() {
            return estado;
        }
    }
}
