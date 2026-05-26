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
    @FXML private Spinner<Integer> cumAnio;
    @FXML private Spinner<Integer> cumPeriodo;
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
        cumAnio.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(2020, 2035, hoy.getYear()));
        cumPeriodo.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 2, ReporteService.periodoActual()));
    }

    private Path pickDir(String defaultName) {
        DirectoryChooser ch = new DirectoryChooser();
        ch.setTitle("Seleccione la carpeta para guardar el archivo");
        File dir = ch.showDialog(msgReportes.getScene().getWindow());
        if (dir == null) {
            return null;
        }
        return dir.toPath().resolve(defaultName);
    }

    private void ok(String msg) {
        msgReportes.setText("Exportado correctamente: " + msg);
        msgReportes.setStyle("-fx-text-fill: #15803d; -fx-font-weight: bold; -fx-font-size: 13px;");
    }

    private void err(String msg) {
        msgReportes.setText("Error: " + msg);
        msgReportes.setStyle("-fx-text-fill: #dc2626; -fx-font-weight: bold; -fx-font-size: 13px;");
    }

    @FXML
    private void onHistorialExcel() {
        msgReportes.setText("");
        String cedula = histCedula.getText().trim();
        if (cedula.isEmpty()) {
            err("Ingrese la cédula del estudiante.");
            return;
        }
        try {
            Path out = pickDir("historial_accesos.xlsx");
            if (out == null) {
                return;
            }
            var est = estudiantes.findByCedula(cedula);
            if (est.isEmpty()) {
                err("No se encontró estudiante con cédula '" + cedula + "'.");
                return;
            }
            reportes.exportHistorialExcel(est.get().idEstudiante(), histDesde.getValue(), histHasta.getValue(), out);
            ok(out.getFileName().toString());
        } catch (Exception ex) {
            err(ex.getMessage());
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
            ok(out.getFileName().toString());
        } catch (Exception ex) {
            err(ex.getMessage());
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
            ok(out.getFileName().toString());
        } catch (Exception ex) {
            err(ex.getMessage());
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
            ok(out.getFileName().toString());
        } catch (Exception ex) {
            err(ex.getMessage());
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
            ok(out.getFileName().toString());
        } catch (Exception ex) {
            err(ex.getMessage());
        }
    }

    @FXML
    private void onCumplimientoExcel() {
        msgReportes.setText("");
        try {
            Path out = pickDir("cumplimiento_por_grupo.xlsx");
            if (out == null) {
                return;
            }
            reportes.exportCumplimientoExcel(cumAnio.getValue(), cumPeriodo.getValue(), out);
            ok(out.getFileName().toString());
        } catch (Exception ex) {
            err(ex.getMessage());
        }
    }
}
