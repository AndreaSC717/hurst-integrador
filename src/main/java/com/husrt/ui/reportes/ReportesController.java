package com.husrt.ui.reportes;

import com.husrt.model.Estudiante;
import com.husrt.repository.EstudianteRepository;
import com.husrt.service.ReporteService;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.DirectoryChooser;

import java.io.File;
import java.nio.file.Path;
import java.time.LocalDate;

public class ReportesController {

    @FXML private TextField histCedula;
    @FXML private DatePicker histDesde;
    @FXML private DatePicker histHasta;
    @FXML private DatePicker rejDesde;
    @FXML private DatePicker rejHasta;
    @FXML private DatePicker ocDesde;
    @FXML private DatePicker ocHasta;
    @FXML private Spinner<Integer> pdfAnio;
    @FXML private Spinner<Integer> pdfPeriodo;
    @FXML private Label msgReportes;

    private final ReporteService reportes = new ReporteService();
    private final EstudianteRepository estudiantes = new EstudianteRepository();

    @FXML
    private void initialize() {
        LocalDate hoy = LocalDate.now();
        histDesde.setValue(hoy.minusDays(30));
        histHasta.setValue(hoy);
        rejDesde.setValue(hoy.minusDays(30));
        rejHasta.setValue(hoy);
        ocDesde.setValue(hoy.minusDays(30));
        ocHasta.setValue(hoy);
        pdfAnio.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(2020, 2035, hoy.getYear()));
        pdfPeriodo.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 2, ReporteService.periodoActual()));
    }

    private Path pickDir(String defaultName) {
        DirectoryChooser ch = new DirectoryChooser();
        ch.setTitle("Seleccione carpeta de destino");
        File dir = ch.showDialog(msgReportes.getScene().getWindow());
        if (dir == null) {
            return null;
        }
        return dir.toPath().resolve(defaultName);
    }

    @FXML
    private void onHistorialExcel() {
        msgReportes.setText("");
        try {
            Path out = pickDir("historial_accesos.xlsx");
            if (out == null) {
                return;
            }
            var est = estudiantes.findByCedula(histCedula.getText().trim());
            if (est.isEmpty()) {
                msgReportes.setText("Estudiante no encontrado.");
                return;
            }
            Estudiante e = est.get();
            reportes.exportHistorialExcel(e.idEstudiante(), histDesde.getValue(), histHasta.getValue(), out);
            msgReportes.setText("Exportado: " + out.toAbsolutePath());
        } catch (Exception ex) {
            msgReportes.setText("Error: " + ex.getMessage());
        }
    }

    @FXML
    private void onRechazosExcel() {
        msgReportes.setText("");
        try {
            Path out = pickDir("intentos_rechazados.xlsx");
            if (out == null) {
                return;
            }
            reportes.exportRechazosExcel(rejDesde.getValue(), rejHasta.getValue(), out);
            msgReportes.setText("Exportado: " + out.toAbsolutePath());
        } catch (Exception ex) {
            msgReportes.setText("Error: " + ex.getMessage());
        }
    }

    @FXML
    private void onOcupacionExcel() {
        msgReportes.setText("");
        try {
            Path out = pickDir("ocupacion_servicios.xlsx");
            if (out == null) {
                return;
            }
            reportes.exportOcupacionExcel(ocDesde.getValue(), ocHasta.getValue(), out);
            msgReportes.setText("Exportado: " + out.toAbsolutePath());
        } catch (Exception ex) {
            msgReportes.setText("Error: " + ex.getMessage());
        }
    }

    @FXML
    private void onHorasPdf() {
        msgReportes.setText("");
        try {
            Path out = pickDir("horas_vs_requeridas.pdf");
            if (out == null) {
                return;
            }
            reportes.exportHorasPdf(pdfAnio.getValue(), pdfPeriodo.getValue(), out);
            msgReportes.setText("Exportado: " + out.toAbsolutePath());
        } catch (Exception ex) {
            msgReportes.setText("Error: " + ex.getMessage());
        }
    }

    @FXML
    private void onArlPdf() {
        msgReportes.setText("");
        try {
            Path out = pickDir("arl_proxima_vencer.pdf");
            if (out == null) {
                return;
            }
            reportes.exportArlPdf(out);
            msgReportes.setText("Exportado: " + out.toAbsolutePath());
        } catch (Exception ex) {
            msgReportes.setText("Error: " + ex.getMessage());
        }
    }
}
