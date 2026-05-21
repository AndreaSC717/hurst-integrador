package com.husrt.ui.dashboard;

import com.husrt.model.PresenteEstudianteDto;
import com.husrt.repository.AlertaRepository;
import com.husrt.repository.RegistroAccesoRepository;
import com.husrt.repository.RegistroDocenteRepository;
import com.husrt.service.DashboardService;
import com.husrt.ui.NavigationContext;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

public class DashboardController {

    @FXML private Label kpiEstudiantes;
    @FXML private Label kpiProfesores;
    @FXML private Label kpiPresentes;
    @FXML private Label kpiPresentesPct;
    @FXML private Label kpiAlertas;
    @FXML private Label kpiAsistieron;
    @FXML private Label kpiRechazos;
    @FXML private VBox ocupacionBox;
    @FXML private ListView<String> listaAlertas;
    @FXML private TableView<PresenteEstudianteDto> tablaEstudiantes;
    @FXML private TableColumn<PresenteEstudianteDto, String> colCedula;
    @FXML private TableColumn<PresenteEstudianteDto, String> colNombre;
    @FXML private TableColumn<PresenteEstudianteDto, String> colApellido;
    @FXML private TableColumn<PresenteEstudianteDto, String> colServicio;
    @FXML private TableColumn<PresenteEstudianteDto, String> colEntrada;
    @FXML private TableView<RegistroDocenteRepository.DocentePresenteRow> tablaDocentes;
    @FXML private TableColumn<RegistroDocenteRepository.DocentePresenteRow, String> colDocCedula;
    @FXML private TableColumn<RegistroDocenteRepository.DocentePresenteRow, String> colDocNombre;
    @FXML private TableColumn<RegistroDocenteRepository.DocentePresenteRow, String> colDocApellido;
    @FXML private TableColumn<RegistroDocenteRepository.DocentePresenteRow, String> colDocPlan;
    @FXML private TableColumn<RegistroDocenteRepository.DocentePresenteRow, String> colDocEntrada;
    @FXML private Label ultimaActualizacion;

    private final RegistroAccesoRepository registros = new RegistroAccesoRepository();
    private final RegistroDocenteRepository docReg = new RegistroDocenteRepository();
    private final DashboardService dashboard = new DashboardService();
    private final DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM HH:mm");

    @FXML
    private void initialize() {
        colCedula.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().cedula()));
        colNombre.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().nombre()));
        colApellido.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().apellido()));
        colServicio.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().servicio()));
        colEntrada.setCellValueFactory(c -> new SimpleStringProperty(
                c.getValue().entrada() != null ? c.getValue().entrada().format(fmt) : ""));

        colDocCedula.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().cedula()));
        colDocNombre.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().nombre()));
        colDocApellido.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().apellido()));
        colDocPlan.setCellValueFactory(c -> new SimpleStringProperty(
                c.getValue().idPlan() != null ? String.valueOf(c.getValue().idPlan()) : ""));
        colDocEntrada.setCellValueFactory(c -> new SimpleStringProperty(
                c.getValue().entrada() != null ? c.getValue().entrada().format(fmt) : ""));

        listaAlertas.setCellFactory(lv -> new ListCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item);
                setWrapText(true);
            }
        });

        Timeline t = new Timeline(new KeyFrame(Duration.seconds(15), e -> refresh()));
        t.setCycleCount(Timeline.INDEFINITE);
        t.play();
        refresh();
    }

    @FXML
    private void onRefresh() {
        refresh();
    }

    @FXML
    private void onIrPorteria() {
        NavigationContext.navigate("PORTERIA");
    }

    private void refresh() {
        try {
            DashboardService.DashboardKpis k = dashboard.cargarKpis();
            kpiEstudiantes.setText(String.valueOf(k.estudiantesActivos()));
            kpiProfesores.setText(String.valueOf(k.profesores()));
            kpiPresentes.setText(String.valueOf(k.presentesAhora()));
            kpiPresentesPct.setText(String.format("%.0f%% del total", k.porcentajePresentes()));
            kpiAlertas.setText(String.valueOf(k.alertasActivas()));
            kpiAsistieron.setText(String.valueOf(k.asistieronHoy()));
            kpiRechazos.setText(String.valueOf(k.accesosRechazadosHoy()));

            listaAlertas.setItems(FXCollections.observableArrayList(
                    dashboard.alertasRecientes(8).stream()
                            .map(this::formatAlerta)
                            .toList()));
            if (listaAlertas.getItems().isEmpty()) {
                listaAlertas.setItems(FXCollections.observableArrayList(
                        registros.listCedulasFranjaVencidaSinSalida().stream()
                                .map(c -> "Franja vencida sin salida: " + c)
                                .toList()));
            }

            ocupacionBox.getChildren().clear();
            for (RegistroAccesoRepository.OcupacionServicioRow row : dashboard.ocupacionServicios()) {
                ocupacionBox.getChildren().add(buildOcupacionRow(row));
            }

            tablaEstudiantes.setItems(FXCollections.observableArrayList(registros.listEstudiantesPresentes()));
            tablaDocentes.setItems(FXCollections.observableArrayList(docReg.listDocentesPresentes()));
            ultimaActualizacion.setText("Actualizado: " + LocalDateTime.now().format(
                    DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss")));
        } catch (SQLException e) {
            ultimaActualizacion.setText("Error: " + e.getMessage());
        }
    }

    private String formatAlerta(AlertaRepository.AlertaRow a) {
        String cuando = a.ts() != null ? tiempoRelativo(a.ts()) : "";
        return cuando + " — " + (a.descripcion() != null ? a.descripcion() : a.tipo());
    }

    private static String tiempoRelativo(LocalDateTime ts) {
        long min = ChronoUnit.MINUTES.between(ts, LocalDateTime.now());
        if (min < 1) {
            return "Hace un momento";
        }
        if (min < 60) {
            return "Hace " + min + " min";
        }
        long h = ChronoUnit.HOURS.between(ts, LocalDateTime.now());
        if (h < 24) {
            return "Hace " + h + " h";
        }
        return "Hace " + ChronoUnit.DAYS.between(ts, LocalDateTime.now()) + " días";
    }

    private HBox buildOcupacionRow(RegistroAccesoRepository.OcupacionServicioRow row) {
        Label nombre = new Label(row.servicio());
        nombre.setMinWidth(140);
        ProgressBar bar = new ProgressBar(row.porcentaje() / 100.0);
        bar.getStyleClass().add("progress-occupancy");
        bar.setPrefHeight(10);
        bar.setPrefWidth(280);
        HBox.setHgrow(bar, Priority.ALWAYS);
        Label nums = new Label(row.dentro() + " / " + row.capacidad());
        nums.setStyle("-fx-text-fill: #64748b; -fx-font-size: 12px;");
        HBox box = new HBox(12, nombre, bar, nums);
        box.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
        return box;
    }
}
