package com.husrt.ui.estudiante;

import com.husrt.model.Estudiante;
import com.husrt.model.Rol;
import com.husrt.repository.EstudianteRepository;
import com.husrt.repository.RegistroAccesoRepository;
import com.husrt.service.EstudianteFotoService;
import com.husrt.service.EstudianteProgresoService;
import com.husrt.session.SessionContext;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;

import java.io.File;
import java.nio.file.Path;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

public class EstudiantePerfilController {

    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    @FXML private Label lblNombre;
    @FXML private Label lblCedula;
    @FXML private Label lblPrograma;
    @FXML private Label lblUniversidad;
    @FXML private Label lblEstado;
    @FXML private Label lblArl;
    @FXML private Label lblInduccion;
    @FXML private Label lblInscripcion;
    @FXML private Label lblPeriodo;
    @FXML private ProgressBar progressHoras;
    @FXML private Label lblHorasResumen;
    @FXML private Label lblHorasFaltantes;
    @FXML private TableView<RegistroAccesoRepository.HistorialRow> tablaHistorial;
    @FXML private TableColumn<RegistroAccesoRepository.HistorialRow, String> colEntrada;
    @FXML private TableColumn<RegistroAccesoRepository.HistorialRow, String> colSalida;
    @FXML private TableColumn<RegistroAccesoRepository.HistorialRow, String> colHoras;
    @FXML private TableColumn<RegistroAccesoRepository.HistorialRow, String> colResultado;
    @FXML private ImageView fotoPreview;
    @FXML private Label msgPerfil;

    private final EstudianteRepository estudiantes = new EstudianteRepository();
    private final EstudianteFotoService fotoService = new EstudianteFotoService();
    private final EstudianteProgresoService progresoService = new EstudianteProgresoService();

    private Estudiante estudianteActual;
    private String fotoUrlActual;
    private Path fotoPendiente;
    private boolean quitarFotoAlGuardar;

    @FXML
    private void initialize() {
        colEntrada.setCellValueFactory(c -> new SimpleStringProperty(
                c.getValue().entrada() != null ? c.getValue().entrada().format(FMT) : ""));
        colSalida.setCellValueFactory(c -> new SimpleStringProperty(
                c.getValue().salida() != null ? c.getValue().salida().format(FMT) : "—"));
        colHoras.setCellValueFactory(c -> new SimpleStringProperty(
                c.getValue().horas() != null ? String.format("%.1f", c.getValue().horas()) : "—"));
        colResultado.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().resultado()));

        if (!SessionContext.hasRole(Rol.ESTUDIANTE)) {
            msgPerfil.setText("Acceso solo para estudiantes.");
            return;
        }
        Long idEst = SessionContext.getCurrent().idEstudiante();
        if (idEst == null) {
            msgPerfil.setText("Su cuenta no está vinculada a un registro académico. Contacte a coordinación.");
            return;
        }
        try {
            EstudianteProgresoService.ResumenPracticas resumen = progresoService.resumenParaEstudiante(idEst);
            cargarResumen(resumen);
        } catch (Exception e) {
            msgPerfil.setText("Error: " + e.getMessage());
        }
    }

    private void cargarResumen(EstudianteProgresoService.ResumenPracticas r) {
        Estudiante e = r.estudiante();
        estudianteActual = e;
        fotoUrlActual = e.fotoUrl();
        fotoPendiente = null;
        quitarFotoAlGuardar = false;

        lblNombre.setText(e.nombre() + " " + e.apellido());
        lblCedula.setText("Cédula: " + e.cedula());
        lblPrograma.setText("Programa: " + e.programaAcademico() + " — Semestre académico " + e.semestreAcademico());
        lblUniversidad.setText("Universidad: " + r.nombreUniversidad());
        lblEstado.setText("Estado: " + e.estado().name());
        lblArl.setText("ARL vigente: " + (r.arlVigente() ? "Sí" : "No")
                + (e.arlFin() != null ? " (hasta " + e.arlFin() + ")" : ""));
        lblInduccion.setText("Inducción hospitalaria: " + (r.induccionOk() ? "Completada" : "Pendiente"));
        lblInscripcion.setText("Inscripción semestre actual: " + (r.inscripcionActiva() ? "Activa" : "No activa"));

        lblPeriodo.setText("Año " + r.anio() + " — Periodo " + r.periodo());
        progressHoras.setProgress(r.porcentaje() / 100.0);
        lblHorasResumen.setText(String.format(
                "%.1f h cumplidas de %d h requeridas (%.1f%%)",
                r.horasCumplidas(), r.horasRequeridas(), r.porcentaje()));
        if (r.horasFaltantes() > 0) {
            lblHorasFaltantes.setText(String.format("Le faltan %.1f horas para completar el semestre.", r.horasFaltantes()));
            lblHorasFaltantes.setStyle("-fx-text-fill: #a65c00;");
        } else {
            lblHorasFaltantes.setText("¡Ha cumplido las horas requeridas para este semestre!");
            lblHorasFaltantes.setStyle("-fx-text-fill: #2d6a2d;");
        }

        tablaHistorial.setItems(FXCollections.observableArrayList(r.historialReciente()));
        mostrarPreview(fotoUrlActual);
    }

    @FXML
    private void onSubirFoto() {
        msgPerfil.setText("");
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Seleccionar su foto");
        chooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Imágenes", EstudianteFotoService.filtroFileChooser()));
        File file = chooser.showOpenDialog(fotoPreview.getScene().getWindow());
        if (file == null) {
            return;
        }
        Path path = file.toPath();
        if (!EstudianteFotoService.esImagen(path)) {
            msgPerfil.setText("Use JPG, PNG, GIF o WEBP (máx. 5 MB).");
            return;
        }
        fotoPendiente = path;
        quitarFotoAlGuardar = false;
        fotoPreview.setImage(new Image(path.toUri().toString(), 140, 140, true, true));
    }

    @FXML
    private void onQuitarFoto() {
        fotoPendiente = null;
        quitarFotoAlGuardar = true;
        fotoUrlActual = null;
        fotoPreview.setImage(null);
        msgPerfil.setText("La foto se eliminará al guardar.");
    }

    @FXML
    private void onGuardar() {
        msgPerfil.setText("");
        if (estudianteActual == null) {
            return;
        }
        try {
            String nuevaFoto = aplicarFoto(estudianteActual.idEstudiante());
            Estudiante actualizado = new Estudiante(
                    estudianteActual.idEstudiante(),
                    estudianteActual.cedula(),
                    estudianteActual.nombre(),
                    estudianteActual.apellido(),
                    nuevaFoto,
                    estudianteActual.programaAcademico(),
                    estudianteActual.semestreAcademico(),
                    estudianteActual.idUniversidad(),
                    estudianteActual.induccionCompletada(),
                    estudianteActual.fechaInduccion(),
                    estudianteActual.arlInicio(),
                    estudianteActual.arlFin(),
                    estudianteActual.estado());
            estudiantes.update(actualizado);
            fotoPendiente = null;
            quitarFotoAlGuardar = false;
            fotoUrlActual = nuevaFoto;
            estudianteActual = actualizado;
            EstudianteProgresoService.ResumenPracticas resumen =
                    progresoService.resumenParaEstudiante(actualizado.idEstudiante());
            cargarResumen(resumen);
            msgPerfil.setText("Foto guardada correctamente.");
        } catch (Exception e) {
            msgPerfil.setText("Error: " + e.getMessage());
        }
    }

    private String aplicarFoto(long idEstudiante) throws Exception {
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

    private void mostrarPreview(String fotoUrl) {
        if (fotoPendiente != null) {
            return;
        }
        fotoPreview.setImage(fotoService.cargarImagen(fotoUrl).orElse(null));
    }
}
