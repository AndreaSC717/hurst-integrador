package com.husrt.ui.docente;

import java.sql.SQLException;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.husrt.model.AsignacionPractica;
import com.husrt.model.Estudiante;
import com.husrt.model.PlanPracticas;
import com.husrt.model.ServicioHospitalario;
import com.husrt.repository.EstudianteRepository;
import com.husrt.repository.PlanPracticasRepository;
import com.husrt.repository.ServicioRepository;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.control.TextArea;

public class ModificarClaseDialogController {

    private static final DateTimeFormatter HM = DateTimeFormatter.ofPattern("HH:mm");

    @FXML
    private Label lblPlanInfo;
    @FXML
    private ComboBox<AsignacionItem> cmbAsignacion;
    @FXML
    private ComboBox<ServicioHospitalario> cmbServicio;
    @FXML
    private Spinner<Integer> spDia;
    @FXML
    private ComboBox<String> cmbHoraIni;
    @FXML
    private ComboBox<String> cmbHoraFin;
    @FXML
    private DatePicker dpFechaEsp;
    @FXML
    private TextArea txtMensaje;
    @FXML
    private Label lblError;

    private final PlanPracticasRepository planes = new PlanPracticasRepository();
    private final ServicioRepository servicios = new ServicioRepository();
    private final EstudianteRepository estudiantes = new EstudianteRepository();

    private PlanPracticas planActual;
    private List<AsignacionPractica> asignaciones = List.of();

    @FXML
    private void initialize() {
        spDia.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 7, 1));
        List<String> horas = buildTimeOptions();
        cmbHoraIni.setItems(FXCollections.observableArrayList(horas));
        cmbHoraFin.setItems(FXCollections.observableArrayList(horas));

        cmbAsignacion.getSelectionModel().selectedItemProperty().addListener((o, a, item) -> {
            if (item != null) {
                cargarCamposDesde(item.asignacion());
            }
        });
    }

    public void cargarPlan(long idPlan) throws SQLException {
        lblError.setText("");
        planActual = planes.findById(idPlan).orElseThrow(
                () -> new IllegalArgumentException("Plan no encontrado (ID " + idPlan + ")."));
        lblPlanInfo.setText("Plan: " + planActual.semestre()
                + " — Año " + planActual.anio() + ", periodo " + planActual.periodo()
                + ", mes " + planActual.mes());

        asignaciones = planes.listAsignacionesByPlan(idPlan);
        List<AsignacionItem> items = new ArrayList<>();
        for (AsignacionPractica a : asignaciones) {
            items.add(new AsignacionItem(a, etiquetaAsignacion(a)));
        }
        if (items.isEmpty()) {
            throw new IllegalArgumentException(
                    "Este plan no tiene asignaciones. Créelas en Coordinación → Plan y asignaciones.");
        }
        cmbAsignacion.setItems(FXCollections.observableArrayList(items));
        cmbAsignacion.getSelectionModel().selectFirst();

        cmbServicio.setItems(FXCollections.observableArrayList(servicios.findAll()));
    }

    private String etiquetaAsignacion(AsignacionPractica a) throws SQLException {
        String est = estudiantes.findById(a.idEstudiante())
                .map(e -> e.nombre() + " " + e.apellido())
                .orElse("Estudiante #" + a.idEstudiante());
        String srv = servicios.findById(a.idServicio())
                .map(ServicioHospitalario::nombre)
                .orElse("Servicio #" + a.idServicio());
        return est + " — " + srv + " — día " + a.diaSemana()
                + " " + a.horaInicio().format(HM) + "-" + a.horaFin().format(HM);
    }

    private void cargarCamposDesde(AsignacionPractica a) {
        for (ServicioHospitalario s : cmbServicio.getItems()) {
            if (s.idServicio() == a.idServicio()) {
                cmbServicio.getSelectionModel().select(s);
                break;
            }
        }
        spDia.getValueFactory().setValue(a.diaSemana());
        cmbHoraIni.getSelectionModel().select(a.horaInicio().format(HM));
        cmbHoraFin.getSelectionModel().select(a.horaFin().format(HM));
        dpFechaEsp.setValue(a.fechaEspecifica());
    }

    /** Saves changes to the database. Returns null if OK, or an error message. */
    public String guardarCambios() {
        lblError.setText("");
        AsignacionItem item = cmbAsignacion.getSelectionModel().getSelectedItem();
        ServicioHospitalario srv = cmbServicio.getSelectionModel().getSelectedItem();
        if (item == null) {
            return "Seleccione una asignación.";
        }
        if (srv == null) {
            return "Seleccione un servicio hospitalario.";
        }
        String hiStr = cmbHoraIni.getSelectionModel().getSelectedItem();
        String hfStr = cmbHoraFin.getSelectionModel().getSelectedItem();
        if (hiStr == null || hfStr == null) {
            return "Seleccione hora de inicio y de fin.";
        }
        try {
            LocalTime hi = LocalTime.parse(hiStr, HM);
            LocalTime hf = LocalTime.parse(hfStr, HM);
            if (!hf.isAfter(hi)) {
                return "La hora de fin debe ser posterior a la de inicio.";
            }
            AsignacionPractica original = item.asignacion();
            AsignacionPractica actualizada = new AsignacionPractica(
                    original.idAsignacion(),
                    original.idPlan(),
                    original.idEstudiante(),
                    srv.idServicio(),
                    spDia.getValue(),
                    hi,
                    hf,
                    dpFechaEsp.getValue());
            planes.updateAsignacion(actualizada);
            return null;
        } catch (Exception ex) {
            return "Error al guardar: " + ex.getMessage();
        }
    }

    public String getMensaje() {
        return txtMensaje.getText() != null ? txtMensaje.getText().trim() : "";
    }

    public PlanPracticas getPlan() {
        return planActual;
    }

    public List<Long> estudiantesDelPlan() {
        return asignaciones.stream().map(AsignacionPractica::idEstudiante).distinct().toList();
    }

    public Optional<AsignacionItem> asignacionSeleccionada() {
        return Optional.ofNullable(cmbAsignacion.getSelectionModel().getSelectedItem());
    }

    private static List<String> buildTimeOptions() {
        List<String> horas = new ArrayList<>();
        for (int hora = 0; hora < 24; hora++) {
            for (int minuto : new int[]{0, 30}) {
                horas.add(String.format("%02d:%02d", hora, minuto));
            }
        }
        return horas;
    }

    public record AsignacionItem(AsignacionPractica asignacion, String etiqueta) {
        @Override
        public String toString() {
            return etiqueta;
        }
    }
}
