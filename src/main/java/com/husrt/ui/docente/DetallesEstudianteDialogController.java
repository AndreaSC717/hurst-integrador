package com.husrt.ui.docente;

import java.sql.SQLException;

import com.husrt.model.Estudiante;
import com.husrt.repository.EstudianteRepository;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;

public class DetallesEstudianteDialogController {

    @FXML
    private Label lblCedula;
    @FXML
    private Label lblNombre;
    @FXML
    private Label lblPrograma;
    @FXML
    private Label lblSemestre;
    @FXML
    private Label lblEstado;
    @FXML
    private Label lblArl;
    @FXML
    private Label lblInduccion;
    @FXML
    private Label lblInscripcion;
    @FXML
    private Label lblHoras;
    @FXML
    private ProgressBar progressHoras;

    private final EstudianteRepository estudianteRepo = new EstudianteRepository();

    public void cargarEstudiante(String cedula) {
        try {
            Estudiante estudiante = estudianteRepo.findByCedula(cedula).orElse(null);
            if (estudiante == null) {
                return;
            }

            lblCedula.setText(estudiante.cedula());
            lblNombre.setText(estudiante.nombre() + " " + estudiante.apellido());
            lblPrograma.setText(estudiante.programaAcademico());
            lblSemestre.setText(String.valueOf(estudiante.semestreAcademico()));
            lblEstado.setText(estudiante.estado().label());

            // Load basic requirements
            lblArl.setText(estudiante.arlFin() != null ? "✅ Vigente hasta " + estudiante.arlFin() : "❌ Pendiente");
            lblInduccion.setText("✅ Completada");
            lblInscripcion.setText("✅ Activa");

            // Practice progress (simulated)
            lblHoras.setText("Horas cumplidas: 80 / 160 (50,0%)");
            progressHoras.setProgress(0.5);

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
