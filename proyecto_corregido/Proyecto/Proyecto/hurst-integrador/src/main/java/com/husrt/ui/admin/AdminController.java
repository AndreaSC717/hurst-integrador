package com.husrt.ui.admin;

import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

import com.husrt.model.AuditoriaEvento;
import com.husrt.model.Estudiante;
import com.husrt.model.Rol;
import com.husrt.model.UsuarioSistema;
import com.husrt.repository.AuditoriaRepository;
import com.husrt.repository.EstudianteRepository;
import com.husrt.repository.UsuarioRepository;
import com.husrt.service.UsuarioAdminService;

import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;

public class AdminController {

    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    @FXML
    private TableView<UsuarioSistema> tablaUsuarios;
    @FXML
    private TableColumn<UsuarioSistema, Long> colUid;
    @FXML
    private TableColumn<UsuarioSistema, String> colUname;
    @FXML
    private TableColumn<UsuarioSistema, String> colUrol;
    @FXML
    private TableColumn<UsuarioSistema, String> colUact;
    @FXML
    private TextField nuUsuario;
    @FXML
    private PasswordField nuClave;
    @FXML
    private ComboBox<Rol> nuRol;
    @FXML
    private ComboBox<Estudiante> nuEstudiante;
    @FXML
    private Label lblEstudianteVinculo;
    @FXML
    private Label msgAdmin;
    @FXML
    private ComboBox<Rol> editRol;
    @FXML
    private PasswordField resetClave;

    @FXML
    private ComboBox<String> filtroModulo;
    @FXML
    private DatePicker audDesde;
    @FXML
    private DatePicker audHasta;
    @FXML
    private TableView<AuditoriaEvento> tablaAuditoria;
    @FXML
    private TableColumn<AuditoriaEvento, String> colAudTs;
    @FXML
    private TableColumn<AuditoriaEvento, String> colAudUser;
    @FXML
    private TableColumn<AuditoriaEvento, String> colAudMod;
    @FXML
    private TableColumn<AuditoriaEvento, String> colAudAcc;
    @FXML
    private TableColumn<AuditoriaEvento, String> colAudDet;
    @FXML
    private Label msgAuditoria;

    private final UsuarioRepository usuarios = new UsuarioRepository();
    private final EstudianteRepository estudiantes = new EstudianteRepository();
    private final UsuarioAdminService adminService = new UsuarioAdminService();
    private final AuditoriaRepository auditoriaRepo = new AuditoriaRepository();

    @FXML
    private void initialize() {
        nuRol.setItems(FXCollections.observableArrayList(Rol.values()));
        nuRol.setCellFactory(lv -> new ListCell<>() {
            @Override
            protected void updateItem(Rol item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item.label());
            }
        });
        nuRol.setButtonCell(new ListCell<>() {
            @Override
            protected void updateItem(Rol item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item.label());
            }
        });
        nuRol.getSelectionModel().select(Rol.CONSULTA);
        nuRol.getSelectionModel().selectedItemProperty().addListener((o, a, b) -> actualizarVinculoEstudiante(b));

        editRol.setItems(FXCollections.observableArrayList(Rol.values()));
        editRol.setCellFactory(lv -> new ListCell<>() {
            @Override
            protected void updateItem(Rol item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item.label());
            }
        });
        editRol.setButtonCell(new ListCell<>() {
            @Override
            protected void updateItem(Rol item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item.label());
            }
        });

        colUid.setCellValueFactory(c -> new ReadOnlyObjectWrapper<>(c.getValue().idUsuario()));
        colUname.setCellValueFactory(c -> new ReadOnlyObjectWrapper<>(c.getValue().nombreUsuario()));
        colUrol.setCellValueFactory(c -> new ReadOnlyObjectWrapper<>(c.getValue().rol().label()));
        colUact.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().activo() ? "Sí" : "No"));

        colAudTs.setCellValueFactory(c -> new SimpleStringProperty(
                c.getValue().timestampEvento() != null ? c.getValue().timestampEvento().format(FMT) : ""));
        colAudUser.setCellValueFactory(c -> new SimpleStringProperty(
                c.getValue().nombreUsuario() != null ? c.getValue().nombreUsuario() : "—"));
        colAudMod.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().modulo()));
        colAudAcc.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().accion()));
        colAudDet.setCellValueFactory(c -> new SimpleStringProperty(
                c.getValue().detalle() != null ? c.getValue().detalle() : ""));

        filtroModulo.setItems(FXCollections.observableArrayList("TODOS", "ADMIN", "PORTERIA", "CUENTA"));
        filtroModulo.getSelectionModel().select("TODOS");
        LocalDate hoy = LocalDate.now();
        audDesde.setValue(hoy.minusDays(7));
        audHasta.setValue(hoy);

        try {
            List<Estudiante> list = estudiantes.findAll();
            nuEstudiante.setItems(FXCollections.observableArrayList(list));
            nuEstudiante.setCellFactory(lv -> new ListCell<>() {
                @Override
                protected void updateItem(Estudiante item, boolean empty) {
                    super.updateItem(item, empty);
                    setText(empty || item == null ? null : item.cedula() + " — " + item.nombre() + " " + item.apellido());
                }
            });
            nuEstudiante.setButtonCell(new ListCell<>() {
                @Override
                protected void updateItem(Estudiante item, boolean empty) {
                    super.updateItem(item, empty);
                    setText(empty || item == null ? null : item.cedula() + " — " + item.apellido());
                }
            });
        } catch (SQLException e) {
            msgAdmin.setText("Error al cargar estudiantes: " + e.getMessage());
        }

        actualizarVinculoEstudiante(Rol.CONSULTA);
        reload();
        onBuscarAuditoria();
    }

    private void actualizarVinculoEstudiante(Rol rol) {
        boolean estudiante = rol == Rol.ESTUDIANTE;
        nuEstudiante.setVisible(estudiante);
        nuEstudiante.setManaged(estudiante);
        lblEstudianteVinculo.setVisible(estudiante);
        lblEstudianteVinculo.setManaged(estudiante);
    }

    private UsuarioSistema seleccionado() {
        return tablaUsuarios.getSelectionModel().getSelectedItem();
    }

    private void reload() {
        try {
            tablaUsuarios.setItems(FXCollections.observableArrayList(usuarios.findAll()));
        } catch (SQLException e) {
            msgAdmin.setText("Error: " + e.getMessage());
        }
    }

    @FXML
    private void onActivar() {
        msgAdmin.setText("");
        UsuarioSistema u = seleccionado();
        if (u == null) {
            msgAdmin.setText("Seleccione un usuario de la tabla.");
            return;
        }
        try {
            adminService.setActivo(u.idUsuario(), true);
            msgAdmin.setText("Usuario activado.");
            reload();
        } catch (Exception e) {
            msgAdmin.setText(e.getMessage());
        }
    }

    @FXML
    private void onDesactivar() {
        msgAdmin.setText("");
        UsuarioSistema u = seleccionado();
        if (u == null) {
            msgAdmin.setText("Seleccione un usuario de la tabla.");
            return;
        }
        try {
            adminService.setActivo(u.idUsuario(), false);
            msgAdmin.setText("Usuario desactivado.");
            reload();
        } catch (Exception e) {
            msgAdmin.setText(e.getMessage());
        }
    }

    @FXML
    private void onEliminar() {
        msgAdmin.setText("");
        UsuarioSistema u = seleccionado();
        if (u == null) {
            msgAdmin.setText("Seleccione un usuario de la tabla.");
            return;
        }
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Eliminar usuario");
        confirm.setHeaderText("¿Desea eliminar este usuario?");
        confirm.setContentText("Se eliminará: " + u.nombreUsuario());
        Optional<ButtonType> result = confirm.showAndWait();
        if (result.isEmpty() || result.get() != ButtonType.OK) {
            return;
        }
        try {
            adminService.eliminarUsuario(u.idUsuario());
            msgAdmin.setText("Usuario eliminado.");
            reload();
        } catch (Exception e) {
            msgAdmin.setText(e.getMessage());
        }
    }

    @FXML
    private void onCambiarRol() {
        msgAdmin.setText("");
        UsuarioSistema u = seleccionado();
        Rol r = editRol.getSelectionModel().getSelectedItem();
        if (u == null) {
            msgAdmin.setText("Seleccione un usuario de la tabla.");
            return;
        }
        if (r == null) {
            msgAdmin.setText("Seleccione el nuevo rol.");
            return;
        }
        try {
            adminService.cambiarRol(u.idUsuario(), r);
            msgAdmin.setText("Rol actualizado.");
            reload();
        } catch (Exception e) {
            msgAdmin.setText(e.getMessage());
        }
    }

    @FXML
    private void onRestablecerClave() {
        msgAdmin.setText("");
        UsuarioSistema u = seleccionado();
        if (u == null) {
            msgAdmin.setText("Seleccione un usuario de la tabla.");
            return;
        }
        if (resetClave.getText().isBlank()) {
            msgAdmin.setText("Ingrese la nueva contraseña.");
            return;
        }
        try {
            adminService.restablecerPassword(u.idUsuario(), resetClave.getText());
            msgAdmin.setText("Contraseña restablecida para " + u.nombreUsuario() + ".");
            resetClave.clear();
        } catch (Exception e) {
            msgAdmin.setText(e.getMessage());
        }
    }

    @FXML
    private void onCrearUsuario() {
        msgAdmin.setText("");
        try {
            if (nuUsuario.getText().isBlank() || nuClave.getText().isBlank()) {
                msgAdmin.setText("Ingrese usuario y contraseña.");
                return;
            }
            Rol r = nuRol.getSelectionModel().getSelectedItem();
            if (r == null) {
                msgAdmin.setText("Seleccione el rol.");
                return;
            }
            Long idEst = null;
            if (r == Rol.ESTUDIANTE) {
                Estudiante sel = nuEstudiante.getSelectionModel().getSelectedItem();
                if (sel == null) {
                    msgAdmin.setText("Seleccione el estudiante a vincular.");
                    return;
                }
                if (usuarios.existsByEstudiante(sel.idEstudiante())) {
                    msgAdmin.setText("Ese estudiante ya tiene una cuenta de acceso.");
                    return;
                }
                idEst = sel.idEstudiante();
            }
            adminService.crearUsuario(nuUsuario.getText().trim(), nuClave.getText(), r, idEst);
            msgAdmin.setText("Usuario creado.");
            nuUsuario.clear();
            nuClave.clear();
            nuEstudiante.getSelectionModel().clearSelection();
            reload();
        } catch (SQLException e) {
            msgAdmin.setText("Error: " + e.getMessage());
        }
    }

    @FXML
    private void onBuscarAuditoria() {
        msgAuditoria.setText("");
        try {
            LocalDate desde = audDesde.getValue();
            LocalDate hasta = audHasta.getValue();
            if (desde == null || hasta == null) {
                msgAuditoria.setText("Seleccione el rango de fechas.");
                return;
            }
            String mod = filtroModulo.getSelectionModel().getSelectedItem();
            List<AuditoriaEvento> rows;
            if ("TODOS".equals(mod)) {
                rows = auditoriaRepo.findRecientes(500).stream()
                        .filter(a -> !a.timestampEvento().isBefore(desde.atStartOfDay())
                        && a.timestampEvento().isBefore(hasta.plusDays(1).atStartOfDay()))
                        .toList();
            } else {
                rows = auditoriaRepo.findPorModulo(mod, desde, hasta, 500);
            }
            tablaAuditoria.setItems(FXCollections.observableArrayList(rows));
            msgAuditoria.setText(rows.size() + " registro(s).");
        } catch (SQLException e) {
            msgAuditoria.setText("Error: " + e.getMessage());
        }
    }
}
