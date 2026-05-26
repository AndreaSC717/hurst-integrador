package com.husrt.ui.porteria;

import com.husrt.model.Estudiante;
import com.husrt.model.PlanPracticas;
import com.husrt.repository.EstudianteRepository;
import com.husrt.repository.PlanPracticasRepository;
import com.husrt.service.AccesoService;
import com.husrt.service.AuditoriaService;
import com.husrt.service.EstudianteFotoService;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

import java.sql.SQLException;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

public class PorteriaController {

    @FXML
    private TextField cedulaEstudiante;
    @FXML
    private Label resultadoEstudiante;
    @FXML
    private ImageView fotoEstudiante;
    @FXML
    private Label nombreEstudiante;
    @FXML
    private TextField cedulaDocente;
    @FXML
    private ComboBox<PlanPracticas> planCombo;
    @FXML
    private Label resultadoDocente;
    @FXML
    private ListView<String> listaValidaciones;

    private final AccesoService acceso = new AccesoService();
    private final PlanPracticasRepository planesRepo = new PlanPracticasRepository();
    private final EstudianteRepository estudiantes = new EstudianteRepository();
    private final EstudianteFotoService fotoService = new EstudianteFotoService();
    private final AuditoriaService auditoria = new AuditoriaService();
    private final com.husrt.repository.RegistroAccesoRepository registroRepo =
            new com.husrt.repository.RegistroAccesoRepository();
    private final DateTimeFormatter fmtVal = DateTimeFormatter.ofPattern("HH:mm");

    @FXML
    private void initialize() {
        listaValidaciones.setCellFactory(lv -> new ListCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item);
                setWrapText(true);
            }
        });
        planCombo.setCellFactory(lv -> new ListCell<>() {
            @Override
            protected void updateItem(PlanPracticas item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText("ID " + item.idPlan() + " — " + item.semestre() + " (" + item.anio() + "-" + item.periodo() + " mes " + item.mes() + ")");
                }
            }
        });
        planCombo.setButtonCell(new ListCell<>() {
            @Override
            protected void updateItem(PlanPracticas item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText("ID " + item.idPlan() + " — " + item.semestre());
                }
            }
        });
        reloadPlanes();
        cargarValidaciones();
    }

    private void cargarValidaciones() {
        try {
            List<String> items = registroRepo.ultimasValidaciones(12).stream()
                    .map(v -> {
                        String hora = v.ts() != null ? v.ts().format(fmtVal) : "";
                        String estado = "APROBADO".equals(v.resultado()) ? "Aprobado" : "Rechazado";
                        return hora + " — " + estado + " — " + v.nombre();
                    })
                    .toList();
            listaValidaciones.setItems(FXCollections.observableArrayList(items));
        } catch (SQLException ignored) {
        }
    }

    private void reloadPlanes() {
        try {
            List<PlanPracticas> list = planesRepo.findAll();
            planCombo.setItems(FXCollections.observableArrayList(list));
            if (!list.isEmpty()) {
                planCombo.getSelectionModel().selectFirst();
            }
        } catch (SQLException e) {
            resultadoDocente.setText("Error cargando planes: " + e.getMessage());
        }
    }

    @FXML
    private void onIngresoEstudiante() {
        resultadoEstudiante.setText("");
        try {
            String cedula = cedulaEstudiante.getText().trim();
            AccesoService.ResultadoIngreso r = acceso.intentarIngresoEstudiante(cedula);
            resultadoEstudiante.setText(r.mensaje());
            auditoria.registrar("PORTERIA", "INGRESO_ESTUDIANTE",
                    "cedula=" + cedula + ", permitido=" + r.permitido() + ", msg=" + r.mensaje(), cedula);
            mostrarDatosEstudiante(cedula);
            cargarValidaciones();
        } catch (SQLException e) {
            resultadoEstudiante.setText("Error: " + e.getMessage());
            limpiarVistaEstudiante();
        }
    }

    @FXML
    private void onSalidaEstudiante() {
        resultadoEstudiante.setText("");
        try {
            String cedula = cedulaEstudiante.getText().trim();
            String msg = acceso.registrarSalidaEstudiante(cedula);
            resultadoEstudiante.setText(msg);
            auditoria.registrar("PORTERIA", "SALIDA_ESTUDIANTE", "cedula=" + cedula + ", msg=" + msg, cedula);
            mostrarDatosEstudiante(cedula);
            cargarValidaciones();
        } catch (SQLException e) {
            resultadoEstudiante.setText("Error: " + e.getMessage());
            limpiarVistaEstudiante();
        }
    }

    private void mostrarDatosEstudiante(String cedula) throws SQLException {
        Optional<Estudiante> op = estudiantes.findByCedula(cedula);
        if (op.isEmpty()) {
            limpiarVistaEstudiante();
            return;
        }
        Estudiante e = op.get();
        nombreEstudiante.setText(e.nombre() + " " + e.apellido() + " — " + e.programaAcademico());
        Optional<Image> img = fotoService.cargarImagen(e.fotoUrl());
        if (img.isPresent()) {
            fotoEstudiante.setImage(img.get());
            fotoEstudiante.setVisible(true);
            fotoEstudiante.setManaged(true);
        } else {
            fotoEstudiante.setImage(null);
            fotoEstudiante.setVisible(false);
            fotoEstudiante.setManaged(false);
        }
    }

    private void limpiarVistaEstudiante() {
        nombreEstudiante.setText("");
        fotoEstudiante.setImage(null);
        fotoEstudiante.setVisible(false);
        fotoEstudiante.setManaged(false);
    }

    @FXML
    private void onDocenteIngreso() {
        resultadoDocente.setText("");
        PlanPracticas p = planCombo.getSelectionModel().getSelectedItem();
        if (p == null) {
            resultadoDocente.setText("Seleccione un plan.");
            return;
        }
        try {
            String cedula = cedulaDocente.getText().trim();
            String msg = acceso.docenteEntrada(cedula, p.idPlan());
            resultadoDocente.setText(msg);
            auditoria.registrar("PORTERIA", "INGRESO_DOCENTE",
                    "cedula=" + cedula + ", plan=" + p.idPlan() + ", msg=" + msg, cedula);
        } catch (SQLException e) {
            resultadoDocente.setText("Error: " + e.getMessage());
        }
    }

    @FXML
    private void onDocenteSalida() {
        resultadoDocente.setText("");
        PlanPracticas p = planCombo.getSelectionModel().getSelectedItem();
        if (p == null) {
            resultadoDocente.setText("Seleccione un plan.");
            return;
        }
        try {
            String cedula = cedulaDocente.getText().trim();
            String msg = acceso.docenteSalida(cedula, p.idPlan());
            resultadoDocente.setText(msg);
            auditoria.registrar("PORTERIA", "SALIDA_DOCENTE",
                    "cedula=" + cedula + ", plan=" + p.idPlan() + ", msg=" + msg, cedula);
        } catch (SQLException e) {
            resultadoDocente.setText("Error: " + e.getMessage());
        }
    }
}
