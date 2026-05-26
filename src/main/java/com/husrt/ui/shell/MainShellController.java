package com.husrt.ui.shell;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

import com.husrt.model.Rol;
import com.husrt.model.UsuarioSistema;
import com.husrt.session.SessionContext;
import com.husrt.ui.BrandAssets;
import com.husrt.ui.NavigationContext;
import com.husrt.ui.UiAnimations;
import com.husrt.ui.UiStyles;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class MainShellController {

    private static final DateTimeFormatter FECHA = DateTimeFormatter.ofPattern("dd MMM yyyy - HH:mm");

    @FXML
    private BorderPane root;
    @FXML
    private BorderPane contentPane;
    @FXML
    private ImageView sidebarLogo;
    @FXML
    private Label lblPageTitle;
    @FXML
    private Label lblPageDate;
    @FXML
    private Label lblAvatar;
    @FXML
    private Label lblSidebarUser;
    @FXML
    private Label lblSidebarRole;
    @FXML
    private Button navDashboard;
    @FXML
    private Button navCoordinacion;
    @FXML
    private Button navEstudiantes;
    @FXML
    private Button navPorteria;
    @FXML
    private Button navDocente;
    @FXML
    private Button navReportes;
    @FXML
    private Button navAdmin;
    @FXML
    private Button navPerfil;

    private final Map<String, Button> navButtons = new HashMap<>();
    private Button activeNav;

    @FXML
    private void initialize() {
        navButtons.put("DASHBOARD", navDashboard);
        navButtons.put("COORDINACION", navCoordinacion);
        navButtons.put("ESTUDIANTES", navEstudiantes);
        navButtons.put("PORTERIA", navPorteria);
        navButtons.put("DOCENTE", navDocente);
        navButtons.put("REPORTES", navReportes);
        navButtons.put("ADMIN", navAdmin);
        navButtons.put("PERFIL", navPerfil);

        NavigationContext.setNavigator(this::navigateTo);

        sidebarLogo.setImage(BrandAssets.hospitalLogo(210));

        UsuarioSistema u = SessionContext.getCurrent();
        Rol r = u != null ? u.rol() : Rol.CONSULTA;
        lblSidebarUser.setText(u != null ? u.nombreUsuario() : "Usuario");
        lblSidebarRole.setText(etiquetaRol(r));
        lblPageDate.setText(LocalDateTime.now().format(FECHA));

        boolean esEstudiante = r == Rol.ESTUDIANTE;
        navPerfil.setVisible(esEstudiante);
        navPerfil.setManaged(esEstudiante);
        navDocente.setVisible(!esEstudiante && (r == Rol.ADMINISTRADOR || r == Rol.DOCENTE));
        navDocente.setManaged(navDocente.isVisible());
        navDashboard.setVisible(!esEstudiante);
        navDashboard.setManaged(navDashboard.isVisible());
        navCoordinacion.setVisible(!esEstudiante && (r == Rol.ADMINISTRADOR || r == Rol.COORDINADOR));
        navCoordinacion.setManaged(navCoordinacion.isVisible());
        navEstudiantes.setVisible(!esEstudiante && (r == Rol.ADMINISTRADOR || r == Rol.COORDINADOR));
        navEstudiantes.setManaged(navEstudiantes.isVisible());
        navPorteria.setVisible(!esEstudiante && (r == Rol.ADMINISTRADOR || r == Rol.COORDINADOR
                || r == Rol.CONSULTA || r == Rol.DOCENTE || r == Rol.PORTERIA));
        navPorteria.setManaged(navPorteria.isVisible());
        navReportes.setVisible(!esEstudiante && (r == Rol.ADMINISTRADOR || r == Rol.COORDINADOR || r == Rol.CONSULTA));
        navReportes.setManaged(navReportes.isVisible());
        navAdmin.setVisible(r == Rol.ADMINISTRADOR);
        navAdmin.setManaged(navAdmin.isVisible());

        if (esEstudiante) {
            openPerfil();
        } else {
            openDashboard();
        }
    }

    private static String iniciales(String nombreUsuario) {
        if (nombreUsuario == null || nombreUsuario.isBlank()) {
            return "US";
        }
        String limpio = nombreUsuario.replaceAll("[^A-Za-z0-9]", "");
        if (limpio.length() >= 2) {
            return limpio.substring(0, 2).toUpperCase();
        }
        return limpio.isEmpty() ? "US" : limpio.toUpperCase();
    }

    private static String etiquetaRol(Rol r) {
        return r.label();
    }

    private void navigateTo(String key) {
        switch (key) {
            case "DASHBOARD" -> openDashboard();
            case "COORDINACION" -> openCoordinacion();
            case "ESTUDIANTES" -> openEstudiantes();
            case "PORTERIA" -> openPorteria();
            case "DOCENTE" -> openDocente();
            case "REPORTES" -> openReportes();
            case "ADMIN" -> openAdmin();
            case "PERFIL" -> openPerfil();
            default -> {
            }
        }
    }

    private void setActiveNav(Button btn, String title) {
        if (activeNav != null) {
            activeNav.getStyleClass().remove("nav-button-active");
        }
        activeNav = btn;
        if (btn != null && !btn.getStyleClass().contains("nav-button-active")) {
            btn.getStyleClass().add("nav-button-active");
        }
        lblPageTitle.setText(title);
        lblPageDate.setText(LocalDateTime.now().format(FECHA));
    }

    private void loadCenter(String resource) throws IOException {
        try {
            Parent p = FXMLLoader.load(MainShellController.class.getResource(resource));
            contentPane.setCenter(p);
            UiStyles.apply(p);
            UiAnimations.slideFadeIn(p);
        } catch (RuntimeException e) {
            Throwable cause = e.getCause() != null ? e.getCause() : e;
            if (cause instanceof IOException io) {
                throw io;
            }
            throw new IOException(cause.getMessage(), cause);
        }
    }

    private void showLoadError(IOException e) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error al cargar pantalla");
        alert.setHeaderText(null);
        String msg = e.getMessage() != null ? e.getMessage() : e.getClass().getSimpleName();
        if (e.getCause() != null && e.getCause().getMessage() != null) {
            msg = msg + "\n" + e.getCause().getMessage();
        }
        alert.setContentText(msg);
        alert.showAndWait();
    }

    @FXML
    private void openCoordinacion() {
        try {
            NavigationContext.setCoordinacionModo(NavigationContext.CoordinacionModo.COMPLETA);
            loadCenter("/com/husrt/ui/coordinacion/coordinacion.fxml");
            setActiveNav(navCoordinacion, "Coordinación de Prácticas");
        } catch (IOException e) {
            showLoadError(e);
        }
    }

    @FXML
    private void openDocente() {
        try {
            loadCenter("/com/husrt/ui/docente/docente.fxml");
            setActiveNav(navDocente, "Panel de Docentes");
        } catch (IOException e) {
            showLoadError(e);
        }
    }

    @FXML
    private void openDashboard() {
        try {
            loadCenter("/com/husrt/ui/dashboard/dashboard.fxml");
            setActiveNav(navDashboard, "Panel Principal");
        } catch (IOException e) {
            showLoadError(e);
        }
    }

    @FXML
    private void openEstudiantes() {
        try {
            NavigationContext.setCoordinacionModo(NavigationContext.CoordinacionModo.SOLO_ESTUDIANTES);
            loadCenter("/com/husrt/ui/coordinacion/coordinacion.fxml");
            setActiveNav(navEstudiantes, "Gestión de Estudiantes");
        } catch (IOException e) {
            showLoadError(e);
        }
    }

    @FXML
    private void openPorteria() {
        try {
            loadCenter("/com/husrt/ui/porteria/porteria.fxml");
            setActiveNav(navPorteria, "Control de Acceso");
        } catch (IOException e) {
            showLoadError(e);
        }
    }

    @FXML
    private void openReportes() {
        try {
            loadCenter("/com/husrt/ui/reportes/reportes.fxml");
            setActiveNav(navReportes, "Reportes y Estadísticas");
        } catch (IOException e) {
            showLoadError(e);
        }
    }

    @FXML
    private void openAdmin() {
        try {
            loadCenter("/com/husrt/ui/admin/admin.fxml");
            setActiveNav(navAdmin, "Administración");
        } catch (IOException e) {
            showLoadError(e);
        }
    }

    @FXML
    private void openPerfil() {
        try {
            loadCenter("/com/husrt/ui/estudiante/estudiante_perfil.fxml");
            setActiveNav(navPerfil, "Mi Perfil");
        } catch (IOException e) {
            showLoadError(e);
        }
    }

    @FXML
    private void openCambioClave() {
        try {
            FXMLLoader loader = new FXMLLoader(MainShellController.class.getResource("/com/husrt/ui/cuenta/cambio_clave.fxml"));
            Parent pane = loader.load();
            Stage dialog = new Stage();
            dialog.initOwner(root.getScene().getWindow());
            dialog.initModality(Modality.APPLICATION_MODAL);
            dialog.setTitle("Cambiar contraseña");
            Scene sc = new Scene(pane, 400, 340);
            UiStyles.apply(sc);
            dialog.setScene(sc);
            dialog.showAndWait();
        } catch (IOException e) {
            showLoadError(e);
        }
    }

    @FXML
    private void onLogout() {
        try {
            SessionContext.clear();
            NavigationContext.setNavigator(null);
            Stage stage = (Stage) root.getScene().getWindow();
            Parent login = FXMLLoader.load(MainShellController.class.getResource("/com/husrt/ui/login/login.fxml"));
            Scene sc = new Scene(login, 520, 720);
            UiStyles.apply(sc);
            stage.setScene(sc);
            stage.setTitle("HUSRT-Control - Ingreso");
        } catch (IOException e) {
            showLoadError(e);
        }
    }
}
