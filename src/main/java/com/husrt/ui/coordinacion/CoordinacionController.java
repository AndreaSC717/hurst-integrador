package com.husrt.ui.coordinacion;

import com.husrt.model.*;
import com.husrt.repository.*;
import com.husrt.service.EstudianteFotoService;
import com.husrt.ui.NavigationContext;
import com.husrt.util.PeriodoAcademico;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;

import java.io.File;
import java.nio.file.Path;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

public class CoordinacionController {

    @FXML private TabPane coordTabs;
    @FXML private TableView<Universidad> tablaUniversidades;
    @FXML private TableColumn<Universidad, Long> colUId;
    @FXML private TableColumn<Universidad, String> colUNombre;
    @FXML private TableColumn<Universidad, String> colUCiudad;
    @FXML private TableColumn<Universidad, String> colUTipo;
    @FXML private TextField uNombre;
    @FXML private TextField uCiudad;
    @FXML private TextField uTipo;
    @FXML private Label msgUniversidad;

    @FXML private TableView<ServicioHospitalario> tablaServicios;
    @FXML private TableColumn<ServicioHospitalario, Long> colSId;
    @FXML private TableColumn<ServicioHospitalario, String> colSNombre;
    @FXML private TableColumn<ServicioHospitalario, String> colSPiso;
    @FXML private TableColumn<ServicioHospitalario, Integer> colSCap;
    @FXML private TextField sNombre;
    @FXML private TextField sPiso;
    @FXML private Spinner<Integer> sCap;
    @FXML private Label msgServicio;

    @FXML private TableView<Estudiante> tablaEstudiantes;
    @FXML private TableColumn<Estudiante, Long> colEId;
    @FXML private TableColumn<Estudiante, String> colECedula;
    @FXML private TableColumn<Estudiante, String> colENombre;
    @FXML private TableColumn<Estudiante, String> colEApellido;
    @FXML private TableColumn<Estudiante, String> colEProg;
    @FXML private TextField eId;
    @FXML private TextField eCedula;
    @FXML private TextField eNombre;
    @FXML private TextField eApellido;
    @FXML private ComboBox<Universidad> eUniversidad;
    @FXML private TextField ePrograma;
    @FXML private Spinner<Integer> eSemestre;
    @FXML private CheckBox eInduccion;
    @FXML private DatePicker eFechaInd;
    @FXML private DatePicker eArlIni;
    @FXML private DatePicker eArlFin;
    @FXML private ComboBox<EstadoEstudiante> eEstado;
    @FXML private ImageView eFotoPreview;
    @FXML private Label msgEstudiante;

    @FXML private TableView<Docente> tablaDocentes;
    @FXML private TableColumn<Docente, Long> colDId;
    @FXML private TableColumn<Docente, String> colDCedula;
    @FXML private TableColumn<Docente, String> colDNombre;
    @FXML private TableColumn<Docente, String> colDApellido;
    @FXML private TableColumn<Docente, String> colDProg;
    @FXML private TextField dId;
    @FXML private TextField dCedula;
    @FXML private TextField dNombre;
    @FXML private TextField dApellido;
    @FXML private ComboBox<Universidad> dUniversidad;
    @FXML private TextField dPrograma;
    @FXML private Label msgDocente;

    @FXML private ComboBox<Docente> pDocente;
    @FXML private ComboBox<Universidad> pUniversidad;
    @FXML private TextField pSemestre;
    @FXML private Spinner<Integer> pMes;
    @FXML private Spinner<Integer> pAnio;
    @FXML private Spinner<Integer> pPeriodo;
    @FXML private Label msgPlan;

    @FXML private ComboBox<PlanPracticas> aPlan;
    @FXML private ComboBox<Estudiante> aEstudiante;
    @FXML private ComboBox<ServicioHospitalario> aServicio;
    @FXML private Spinner<Integer> aDia;
    @FXML private TextField aHoraIni;
    @FXML private TextField aHoraFin;
    @FXML private DatePicker aFechaEsp;
    @FXML private Label msgAsignacion;
    @FXML private TableView<AsignacionPractica> tablaAsignaciones;
    @FXML private TableColumn<AsignacionPractica, Long> colAId;
    @FXML private TableColumn<AsignacionPractica, Long> colAEst;
    @FXML private TableColumn<AsignacionPractica, Long> colASrv;
    @FXML private TableColumn<AsignacionPractica, Integer> colADia;
    @FXML private TableColumn<AsignacionPractica, String> colAHi;
    @FXML private TableColumn<AsignacionPractica, String> colAHf;

    private final UniversidadRepository universidades = new UniversidadRepository();
    private final ServicioRepository servicios = new ServicioRepository();
    private final EstudianteRepository estudiantes = new EstudianteRepository();
    private final DocenteRepository docentes = new DocenteRepository();
    private final PlanPracticasRepository planesRepo = new PlanPracticasRepository();
    private final EstudianteFotoService fotoService = new EstudianteFotoService();

    private Path fotoPendiente;
    private boolean quitarFotoAlGuardar;
    private String fotoUrlActual;

    private static final DateTimeFormatter HM = DateTimeFormatter.ofPattern("HH:mm");

    @FXML
    private void initialize() {
        int tab = NavigationContext.consumeCoordinacionTab();
        if (coordTabs != null && tab >= 0 && tab < coordTabs.getTabs().size()) {
            coordTabs.getSelectionModel().select(tab);
        }
        sCap.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 500, 10));
        eSemestre.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 20, 1));
        pMes.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 12, LocalDate.now().getMonthValue()));
        pAnio.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(2020, 2035, LocalDate.now().getYear()));
        pPeriodo.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 2, PeriodoAcademico.periodoDeFecha(LocalDate.now())));
        aDia.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 7, LocalDate.now().getDayOfWeek().getValue()));

        eEstado.setItems(FXCollections.observableArrayList(EstadoEstudiante.values()));

        bindUniTable();
        bindServTable();
        bindEstTable();
        bindDocTable();
        bindAsigTable();

        try {
            reloadUniversidades();
            reloadServicios();
            reloadEstudiantes();
            reloadDocentes();
            reloadPlanesCombo();
            reloadEstCombo();
            reloadServCombo();
        } catch (SQLException e) {
            msgUniversidad.setText("Error cargando datos: " + e.getMessage());
        }

        tablaUniversidades.getSelectionModel().selectedItemProperty().addListener((o, a, b) -> {
            if (b != null) {
                uNombre.setText(b.nombre());
                uCiudad.setText(b.ciudad());
                uTipo.setText(b.tipoConvenio() != null ? b.tipoConvenio() : "");
            }
        });
        tablaServicios.getSelectionModel().selectedItemProperty().addListener((o, a, b) -> {
            if (b != null) {
                sNombre.setText(b.nombre());
                sPiso.setText(b.piso() != null ? b.piso() : "");
                sCap.getValueFactory().setValue(b.capacidadMaximaEstudiantes());
            }
        });
        tablaEstudiantes.getSelectionModel().selectedItemProperty().addListener((o, a, b) -> fillEst(b));
        tablaDocentes.getSelectionModel().selectedItemProperty().addListener((o, a, b) -> fillDoc(b));

        aPlan.getSelectionModel().selectedItemProperty().addListener((o, a, b) -> {
            try {
                reloadAsignacionesTabla();
            } catch (SQLException e) {
                msgAsignacion.setText(e.getMessage());
            }
        });
    }

    private void bindUniTable() {
        colUId.setCellValueFactory(c -> new ReadOnlyObjectWrapper<>(c.getValue().idUniversidad()));
        colUNombre.setCellValueFactory(c -> new ReadOnlyObjectWrapper<>(c.getValue().nombre()));
        colUCiudad.setCellValueFactory(c -> new ReadOnlyObjectWrapper<>(c.getValue().ciudad()));
        colUTipo.setCellValueFactory(c -> new SimpleStringProperty(Optional.ofNullable(c.getValue().tipoConvenio()).orElse("")));
    }

    private void bindServTable() {
        colSId.setCellValueFactory(c -> new ReadOnlyObjectWrapper<>(c.getValue().idServicio()));
        colSNombre.setCellValueFactory(c -> new ReadOnlyObjectWrapper<>(c.getValue().nombre()));
        colSPiso.setCellValueFactory(c -> new SimpleStringProperty(Optional.ofNullable(c.getValue().piso()).orElse("")));
        colSCap.setCellValueFactory(c -> new ReadOnlyObjectWrapper<>(c.getValue().capacidadMaximaEstudiantes()));
    }

    private void bindEstTable() {
        colEId.setCellValueFactory(c -> new ReadOnlyObjectWrapper<>(c.getValue().idEstudiante()));
        colECedula.setCellValueFactory(c -> new ReadOnlyObjectWrapper<>(c.getValue().cedula()));
        colENombre.setCellValueFactory(c -> new ReadOnlyObjectWrapper<>(c.getValue().nombre()));
        colEApellido.setCellValueFactory(c -> new ReadOnlyObjectWrapper<>(c.getValue().apellido()));
        colEProg.setCellValueFactory(c -> new ReadOnlyObjectWrapper<>(c.getValue().programaAcademico()));
    }

    private void bindDocTable() {
        colDId.setCellValueFactory(c -> new ReadOnlyObjectWrapper<>(c.getValue().idDocente()));
        colDCedula.setCellValueFactory(c -> new ReadOnlyObjectWrapper<>(c.getValue().cedula()));
        colDNombre.setCellValueFactory(c -> new ReadOnlyObjectWrapper<>(c.getValue().nombre()));
        colDApellido.setCellValueFactory(c -> new ReadOnlyObjectWrapper<>(c.getValue().apellido()));
        colDProg.setCellValueFactory(c -> new ReadOnlyObjectWrapper<>(c.getValue().programaQueSupervisa()));
    }

    private void bindAsigTable() {
        colAId.setCellValueFactory(c -> new ReadOnlyObjectWrapper<>(c.getValue().idAsignacion()));
        colAEst.setCellValueFactory(c -> new ReadOnlyObjectWrapper<>(c.getValue().idEstudiante()));
        colASrv.setCellValueFactory(c -> new ReadOnlyObjectWrapper<>(c.getValue().idServicio()));
        colADia.setCellValueFactory(c -> new ReadOnlyObjectWrapper<>(c.getValue().diaSemana()));
        colAHi.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().horaInicio().toString()));
        colAHf.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().horaFin().toString()));
    }

    private void reloadUniversidades() throws SQLException {
        List<Universidad> list = universidades.findAll();
        tablaUniversidades.setItems(FXCollections.observableArrayList(list));
        eUniversidad.setItems(FXCollections.observableArrayList(list));
        dUniversidad.setItems(FXCollections.observableArrayList(list));
        pUniversidad.setItems(FXCollections.observableArrayList(list));
    }

    private void reloadServicios() throws SQLException {
        List<ServicioHospitalario> list = servicios.findAll();
        tablaServicios.setItems(FXCollections.observableArrayList(list));
        aServicio.setItems(FXCollections.observableArrayList(list));
    }

    private void reloadEstudiantes() throws SQLException {
        tablaEstudiantes.setItems(FXCollections.observableArrayList(estudiantes.findAll()));
        aEstudiante.setItems(FXCollections.observableArrayList(estudiantes.findAll()));
    }

    private void reloadDocentes() throws SQLException {
        tablaDocentes.setItems(FXCollections.observableArrayList(docentes.findAll()));
        pDocente.setItems(FXCollections.observableArrayList(docentes.findAll()));
    }

    private void reloadPlanesCombo() throws SQLException {
        List<PlanPracticas> list = planesRepo.findAll();
        aPlan.setItems(FXCollections.observableArrayList(list));
    }

    private void reloadEstCombo() throws SQLException {
        aEstudiante.setItems(FXCollections.observableArrayList(estudiantes.findAll()));
    }

    private void reloadServCombo() throws SQLException {
        aServicio.setItems(FXCollections.observableArrayList(servicios.findAll()));
    }

    private void reloadAsignacionesTabla() throws SQLException {
        PlanPracticas p = aPlan.getSelectionModel().getSelectedItem();
        if (p == null) {
            tablaAsignaciones.getItems().clear();
            return;
        }
        tablaAsignaciones.setItems(FXCollections.observableArrayList(planesRepo.listAsignacionesByPlan(p.idPlan())));
    }

    private void fillEst(Estudiante b) {
        if (b == null) {
            return;
        }
        eId.setText(String.valueOf(b.idEstudiante()));
        eCedula.setText(b.cedula());
        eNombre.setText(b.nombre());
        eApellido.setText(b.apellido());
        ePrograma.setText(b.programaAcademico());
        eSemestre.getValueFactory().setValue(b.semestreAcademico());
        eInduccion.setSelected(b.induccionCompletada());
        eFechaInd.setValue(b.fechaInduccion());
        eArlIni.setValue(b.arlInicio());
        eArlFin.setValue(b.arlFin());
        eEstado.getSelectionModel().select(b.estado());
        fotoUrlActual = b.fotoUrl();
        fotoPendiente = null;
        quitarFotoAlGuardar = false;
        mostrarFotoPreview(fotoUrlActual);
        for (Universidad u : eUniversidad.getItems()) {
            if (u.idUniversidad() == b.idUniversidad()) {
                eUniversidad.getSelectionModel().select(u);
                break;
            }
        }
    }

    private void fillDoc(Docente b) {
        if (b == null) {
            return;
        }
        dId.setText(String.valueOf(b.idDocente()));
        dCedula.setText(b.cedula());
        dNombre.setText(b.nombre());
        dApellido.setText(b.apellido());
        dPrograma.setText(b.programaQueSupervisa());
        for (Universidad u : dUniversidad.getItems()) {
            if (u.idUniversidad() == b.idUniversidad()) {
                dUniversidad.getSelectionModel().select(u);
                break;
            }
        }
    }

    @FXML
    private void onUniNuevo() {
        tablaUniversidades.getSelectionModel().clearSelection();
        uNombre.clear();
        uCiudad.clear();
        uTipo.clear();
        msgUniversidad.setText("");
    }

    @FXML
    private void onUniGuardar() {
        msgUniversidad.setText("");
        try {
            Universidad sel = tablaUniversidades.getSelectionModel().getSelectedItem();
            if (sel == null) {
                universidades.insert(new Universidad(0, uNombre.getText().trim(), uCiudad.getText().trim(), uTipo.getText().trim()));
                msgUniversidad.setText("Universidad creada.");
            } else {
                universidades.update(new Universidad(sel.idUniversidad(), uNombre.getText().trim(), uCiudad.getText().trim(), uTipo.getText().trim()));
                msgUniversidad.setText("Universidad actualizada.");
            }
            reloadUniversidades();
        } catch (SQLException e) {
            msgUniversidad.setText("Error: " + e.getMessage());
        }
    }

    @FXML
    private void onServNuevo() {
        tablaServicios.getSelectionModel().clearSelection();
        sNombre.clear();
        sPiso.clear();
        sCap.getValueFactory().setValue(10);
        msgServicio.setText("");
    }

    @FXML
    private void onServGuardar() {
        msgServicio.setText("");
        try {
            ServicioHospitalario sel = tablaServicios.getSelectionModel().getSelectedItem();
            if (sel == null) {
                servicios.insert(new ServicioHospitalario(0, sNombre.getText().trim(), sPiso.getText().trim(), sCap.getValue()));
                msgServicio.setText("Servicio creado.");
            } else {
                servicios.update(new ServicioHospitalario(sel.idServicio(), sNombre.getText().trim(), sPiso.getText().trim(), sCap.getValue()));
                msgServicio.setText("Servicio actualizado.");
            }
            reloadServicios();
        } catch (SQLException e) {
            msgServicio.setText("Error: " + e.getMessage());
        }
    }

    @FXML
    private void onEstNuevo() {
        tablaEstudiantes.getSelectionModel().clearSelection();
        eId.clear();
        eCedula.clear();
        eNombre.clear();
        eApellido.clear();
        ePrograma.clear();
        eSemestre.getValueFactory().setValue(1);
        eInduccion.setSelected(false);
        eFechaInd.setValue(null);
        eArlIni.setValue(null);
        eArlFin.setValue(null);
        eEstado.getSelectionModel().select(EstadoEstudiante.ACTIVO);
        fotoUrlActual = null;
        fotoPendiente = null;
        quitarFotoAlGuardar = false;
        mostrarFotoPreview(null);
        msgEstudiante.setText("");
    }

    @FXML
    private void onEstSubirFoto() {
        msgEstudiante.setText("");
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Seleccionar foto del estudiante");
        chooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Imágenes", EstudianteFotoService.filtroFileChooser()));
        File file = chooser.showOpenDialog(eFotoPreview.getScene().getWindow());
        if (file == null) {
            return;
        }
        Path path = file.toPath();
        if (!EstudianteFotoService.esImagen(path)) {
            msgEstudiante.setText("Seleccione un archivo JPG, PNG, GIF o WEBP.");
            return;
        }
        fotoPendiente = path;
        quitarFotoAlGuardar = false;
        eFotoPreview.setImage(new Image(path.toUri().toString(), 96, 96, true, true));
    }

    @FXML
    private void onEstQuitarFoto() {
        fotoPendiente = null;
        quitarFotoAlGuardar = true;
        fotoUrlActual = null;
        mostrarFotoPreview(null);
    }

    private void mostrarFotoPreview(String fotoUrl) {
        if (fotoPendiente != null) {
            return;
        }
        Optional<Image> img = fotoService.cargarImagen(fotoUrl);
        eFotoPreview.setImage(img.orElse(null));
    }

    private String aplicarFotoEstudiante(long idEstudiante) throws Exception {
        if (quitarFotoAlGuardar) {
            fotoService.eliminar(fotoUrlActual);
            return null;
        }
        if (fotoPendiente != null) {
            fotoService.eliminar(fotoUrlActual);
            return fotoService.guardar(idEstudiante, fotoPendiente);
        }
        return fotoUrlActual;
    }

    @FXML
    private void onEstGuardar() {
        msgEstudiante.setText("");
        try {
            if (eUniversidad.getSelectionModel().getSelectedItem() == null) {
                msgEstudiante.setText("Seleccione universidad.");
                return;
            }
            if (eEstado.getSelectionModel().getSelectedItem() == null) {
                msgEstudiante.setText("Seleccione estado.");
                return;
            }
            long idUni = eUniversidad.getSelectionModel().getSelectedItem().idUniversidad();
            Estudiante model = new Estudiante(
                    0,
                    eCedula.getText().trim(),
                    eNombre.getText().trim(),
                    eApellido.getText().trim(),
                    fotoUrlActual,
                    ePrograma.getText().trim(),
                    eSemestre.getValue(),
                    idUni,
                    eInduccion.isSelected(),
                    eFechaInd.getValue(),
                    eArlIni.getValue(),
                    eArlFin.getValue(),
                    eEstado.getSelectionModel().getSelectedItem());

            if (eId.getText() == null || eId.getText().isBlank()) {
                Optional<Estudiante> ex = estudiantes.findByCedula(model.cedula());
                if (ex.isPresent()) {
                    msgEstudiante.setText("Ya existe un estudiante con esa cédula. Use edición desde la tabla.");
                    return;
                }
                long nid = estudiantes.insert(model);
                String fotoGuardada = aplicarFotoEstudiante(nid);
                if (fotoGuardada != model.fotoUrl()) {
                    estudiantes.updateFotoUrl(nid, fotoGuardada);
                }
                int anio = PeriodoAcademico.anioDeFecha(LocalDate.now());
                int per = PeriodoAcademico.periodoDeFecha(LocalDate.now());
                if (!estudiantes.existsInscripcionSamePeriod(nid, anio, per)) {
                    estudiantes.insertInscripcion(nid, anio, per);
                }
                fotoPendiente = null;
                quitarFotoAlGuardar = false;
                fotoUrlActual = fotoGuardada;
                msgEstudiante.setText("Estudiante creado e inscrito al semestre actual.");
            } else {
                long id = Long.parseLong(eId.getText().trim());
                String fotoGuardada = aplicarFotoEstudiante(id);
                estudiantes.update(new Estudiante(
                        id,
                        model.cedula(),
                        model.nombre(),
                        model.apellido(),
                        fotoGuardada,
                        model.programaAcademico(),
                        model.semestreAcademico(),
                        model.idUniversidad(),
                        model.induccionCompletada(),
                        model.fechaInduccion(),
                        model.arlInicio(),
                        model.arlFin(),
                        model.estado()));
                fotoPendiente = null;
                quitarFotoAlGuardar = false;
                fotoUrlActual = fotoGuardada;
                mostrarFotoPreview(fotoUrlActual);
                msgEstudiante.setText("Estudiante actualizado.");
            }
            reloadEstudiantes();
            reloadEstCombo();
        } catch (Exception e) {
            msgEstudiante.setText("Error: " + e.getMessage());
        }
    }

    @FXML
    private void onDocNuevo() {
        tablaDocentes.getSelectionModel().clearSelection();
        dId.clear();
        dCedula.clear();
        dNombre.clear();
        dApellido.clear();
        dPrograma.clear();
        msgDocente.setText("");
    }

    @FXML
    private void onDocGuardar() {
        msgDocente.setText("");
        try {
            if (dUniversidad.getSelectionModel().getSelectedItem() == null) {
                msgDocente.setText("Seleccione universidad.");
                return;
            }
            long idUni = dUniversidad.getSelectionModel().getSelectedItem().idUniversidad();
            Docente model = new Docente(0, dCedula.getText().trim(), dNombre.getText().trim(), dApellido.getText().trim(), idUni, dPrograma.getText().trim());
            if (dId.getText() == null || dId.getText().isBlank()) {
                docentes.insert(model);
                msgDocente.setText("Docente creado.");
            } else {
                docentes.update(new Docente(Long.parseLong(dId.getText().trim()), model.cedula(), model.nombre(), model.apellido(), model.idUniversidad(), model.programaQueSupervisa()));
                msgDocente.setText("Docente actualizado.");
            }
            reloadDocentes();
        } catch (Exception e) {
            msgDocente.setText("Error: " + e.getMessage());
        }
    }

    @FXML
    private void onPlanCrear() {
        msgPlan.setText("");
        try {
            if (pDocente.getSelectionModel().getSelectedItem() == null || pUniversidad.getSelectionModel().getSelectedItem() == null) {
                msgPlan.setText("Seleccione docente y universidad.");
                return;
            }
            PlanPracticas p = new PlanPracticas(
                    0,
                    pDocente.getSelectionModel().getSelectedItem().idDocente(),
                    pUniversidad.getSelectionModel().getSelectedItem().idUniversidad(),
                    pSemestre.getText().trim(),
                    pMes.getValue(),
                    pAnio.getValue(),
                    pPeriodo.getValue(),
                    LocalDate.now());
            long id = planesRepo.insert(p);
            msgPlan.setText("Plan creado con ID " + id);
            reloadPlanesCombo();
        } catch (Exception e) {
            msgPlan.setText("Error: " + e.getMessage());
        }
    }

    @FXML
    private void onAsignacionGuardar() {
        msgAsignacion.setText("");
        try {
            PlanPracticas plan = aPlan.getSelectionModel().getSelectedItem();
            Estudiante est = aEstudiante.getSelectionModel().getSelectedItem();
            ServicioHospitalario srv = aServicio.getSelectionModel().getSelectedItem();
            if (plan == null || est == null || srv == null) {
                msgAsignacion.setText("Seleccione plan, estudiante y servicio.");
                return;
            }
            LocalTime hi = LocalTime.parse(aHoraIni.getText().trim(), HM);
            LocalTime hf = LocalTime.parse(aHoraFin.getText().trim(), HM);
            LocalDate fe = aFechaEsp.getValue();
            int dia = aDia.getValue();
            int cnt = planesRepo.countAsignadosMismaFranja(plan.idPlan(), srv.idServicio(), dia, fe, hi, hf) + 1;
            if (cnt > srv.capacidadMaximaEstudiantes()) {
                msgAsignacion.setText("La asignación superaría la capacidad instalada del servicio (" + srv.capacidadMaximaEstudiantes() + ").");
                return;
            }
            AsignacionPractica a = new AsignacionPractica(0, plan.idPlan(), est.idEstudiante(), srv.idServicio(), dia, hi, hf, fe);
            planesRepo.insertAsignacion(a);
            msgAsignacion.setText("Asignación creada.");
            reloadAsignacionesTabla();
        } catch (Exception e) {
            msgAsignacion.setText("Error: " + e.getMessage());
        }
    }
}
