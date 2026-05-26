package com.husrt.ui.shell;

import com.husrt.model.Rol;
import com.husrt.model.UsuarioSistema;
import com.husrt.session.SessionContext;
import com.husrt.ui.BrandAssets;
import com.husrt.ui.NavigationContext;
import com.husrt.ui.UiStyles;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

public class MainShellController {

    private static final DateTimeFormatter FECHA = DateTimeFormatter.ofPattern("dd MMM yyyy — HH:mm");

    @FXML private BorderPane root;
    @FXML private BorderPane contentPane;
    @FXML private ImageView sidebarLogo;
    @FXML private Label lblPageTitle;
    @FXML private Label lblPageDate;
    @FXML private Label lblSidebarUser;
    @FXML private Label lblSidebarRole;
    @FXML private Button navDashboard;
    @FXML private Button navEstudiantes;
    @FXML private Button navProfesores;
    @FXML private Button navRotaciones;
    @FXML private Button navPorteria;
    @FXML private Button navReportes;
    @FXML private Button navCoord;
    @FXML private Button navAdmin;
    @FXML private Button navPerfil;

    private final Map<String, Button> navButtons = new HashMap<>();
    private Button activeNav;

    @FXML
    private void initialize() {
        navButtons.put("DASHBOARD", navDashboard);
        navButtons.put("ESTUDIANTES", navEstudiantes);
        navButtons.put("PROFESORES", navProfesores);
        navButtons.put("ROTACIONES", navRotaciones);
        navButtons.put("PORTERIA", navPorteria);
        navButtons.put("REPORTES", navReportes);
        navButtons.put("COORD", navCoord);
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
        navPorteria.setVisible(!esEstudiante && (r == Rol.ADMINISTRADOR || r == Rol.PORTERIA));
        navPorteria.setManaged(navPorteria.isVisible());
        navDashboard.setVisible(!esEstudiante);
        navDashboard.setManaged(navDashboard.isVisible());
        navEstudiantes.setVisible(!esEstudiante && (r == Rol.ADMINISTRADOR || r == Rol.COORDINADOR));
        navEstudiantes.setManaged(navEstudiantes.isVisible());
        navProfesores.setVisible(navEstudiantes.isVisible());
        navProfesores.setManaged(navProfesores.isVisible());
        navRotaciones.setVisible(navEstudiantes.isVisible());
        navRotaciones.setManaged(navRotaciones.isVisible());
        navCoord.setVisible(navEstudiantes.isVisible());
        navCoord.setManaged(navCoord.isVisible());
        navReportes.setVisible(!esEstudiante && (r == Rol.ADMINISTRADOR || r == Rol.COORDINADOR || r == Rol.CONSULTA));
        navReportes.setManaged(navReportes.isVisible());
        navAdmin.setVisible(r == Rol.ADMINISTRADOR);
        navAdmin.setManaged(navAdmin.isVisible());

        try {
            if (esEstudiante) {
                openPerfil();
            } else {
                openDashboard();
            }
        } catch (IOException ignored) {
        }
    }

    private static String etiquetaRol(Rol r) {
        return switch (r) {
            case ADMINISTRADOR -> "Administrador";
            case COORDINADOR -> "Coordinadora";
            case PORTERIA -> "Portería";
            case CONSULTA -> "Consulta";
            case ESTUDIANTE -> "Estudiante";
        };
    }

    private void navigateTo(String key) {
        try {
            switch (key) {
                case "DASHBOARD" -> openDashboard();
                case "ESTUDIANTES" -> openEstudiantes();
                case "PROFESORES" -> openProfesores();
                case "ROTACIONES" -> openRotaciones();
                case "PORTERIA" -> openPorteria();
                case "REPORTES" -> openReportes();
                case "COORD" -> openCoordinacion();
                case "ADMIN" -> openAdmin();
                case "PERFIL" -> openPerfil();
                default -> { }
            }
        } catch (IOException ignored) {
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
        Parent p = FXMLLoader.load(MainShellController.class.getResource(resource));
        contentPane.setCenter(p);
        UiStyles.apply(p);
    }

    @FXML
    private void openPorteria() throws IOException {
        loadCenter("/com/husrt/ui/porteria/porteria.fxml");
        setActiveNav(navPorteria, "Control de Acceso");
    }

    @FXML
    private void openDashboard() throws IOException {
        loadCenter("/com/husrt/ui/dashboard/dashboard.fxml");
        setActiveNav(navDashboard, "Dashboard Principal");
    }

    @FXML
    private void openEstudiantes() throws IOException {
        NavigationContext.setCoordinacionTab(2);
        loadCenter("/com/husrt/ui/coordinacion/coordinacion.fxml");
        setActiveNav(navEstudiantes, "Gestión de Estudiantes");
    }

    @FXML
    private void openProfesores() throws IOException {
        NavigationContext.setCoordinacionTab(3);
        loadCenter("/com/husrt/ui/coordinacion/coordinacion.fxml");
        setActiveNav(navProfesores, "Gestión de Profesores");
    }

    @FXML
    private void openRotaciones() throws IOException {
        NavigationContext.setCoordinacionTab(4);
        loadCenter("/com/husrt/ui/coordinacion/coordinacion.fxml");
        setActiveNav(navRotaciones, "Rotaciones y Planes");
    }

    @FXML
    private void openCoordinacion() throws IOException {
        NavigationContext.setCoordinacionTab(0);
        loadCenter("/com/husrt/ui/coordinacion/coordinacion.fxml");
        setActiveNav(navCoord, "Coordinación");
    }

    @FXML
    private void openReportes() throws IOException {
        loadCenter("/com/husrt/ui/reportes/reportes.fxml");
        setActiveNav(navReportes, "Reportes y Estadísticas");
    }

    @FXML
    private void openAdmin() throws IOException {
        loadCenter("/com/husrt/ui/admin/admin.fxml");
        setActiveNav(navAdmin, "Administración");
    }

    @FXML
    private void openPerfil() throws IOException {
        loadCenter("/com/husrt/ui/estudiante/estudiante_perfil.fxml");
        setActiveNav(navPerfil, "Mi perfil");
    }

    @FXML
    private void openCambioClave() throws IOException {
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
    }

    @FXML
    private void onLogout() throws IOException {
        SessionContext.clear();
        NavigationContext.setNavigator(null);
        Stage stage = (Stage) root.getScene().getWindow();
        Parent login = FXMLLoader.load(MainShellController.class.getResource("/com/husrt/ui/login/login.fxml"));
        Scene sc = new Scene(login, 520, 720);
        UiStyles.apply(sc);
        stage.setScene(sc);
        stage.setTitle("HUSRT-Control — Ingreso");
    }
}
