package com.husrt.ui.login;

import com.husrt.model.Rol;
import com.husrt.model.UsuarioSistema;
import com.husrt.service.AuthService;
import com.husrt.service.LoginOutcome;
import com.husrt.session.SessionContext;
import com.husrt.ui.BrandAssets;
import com.husrt.ui.UiStyles;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.SQLException;

public class LoginController {

    @FXML private ImageView logoView;
    @FXML private TextField usuarioField;
    @FXML private PasswordField claveField;
    @FXML private Label mensajeLabel;

    private final AuthService auth = new AuthService();

    @FXML
    private void initialize() {
        logoView.setImage(BrandAssets.hospitalLogo(340));
    }

    @FXML
    private void onDemoAdmin() {
        fillDemo("admin");
    }

    @FXML
    private void onDemoCoord() {
        fillDemo("coordinador");
    }

    @FXML
    private void onDemoPorteria() {
        fillDemo("porteria");
    }

    @FXML
    private void onDemoConsulta() {
        fillDemo("consulta");
    }

    @FXML
    private void onDemoEstudiante() {
        fillDemo("1090123456");
    }

    private void fillDemo(String user) {
        usuarioField.setText(user);
        claveField.setText("password");
        onLogin();
    }

    @FXML
    private void onLogin() {
        mensajeLabel.setText("");
        mensajeLabel.getStyleClass().remove("message-ok");
        if (!mensajeLabel.getStyleClass().contains("message-error")) {
            mensajeLabel.getStyleClass().add("message-error");
        }
        try {
            LoginOutcome outcome = auth.login(usuarioField.getText().trim(), claveField.getText());
            if (outcome instanceof LoginOutcome.Failure failure) {
                mensajeLabel.setText(failure.mensaje());
                return;
            }
            UsuarioSistema cuenta = ((LoginOutcome.Success) outcome).usuario();
            if (cuenta.rol() == Rol.ESTUDIANTE && cuenta.idEstudiante() == null) {
                mensajeLabel.setText("Cuenta de estudiante sin vínculo académico. Contacte a coordinación.");
                return;
            }
            SessionContext.setCurrent(cuenta);
            Stage stage = (Stage) usuarioField.getScene().getWindow();
            Parent root = FXMLLoader.load(LoginController.class.getResource("/com/husrt/ui/shell/main_shell.fxml"));
            Scene scene = new Scene(root, 1280, 720);
            UiStyles.apply(scene);
            stage.setScene(scene);
            stage.setTitle("HUSRT-Control");
            stage.setMaximized(true);
        } catch (IOException e) {
            mensajeLabel.setText("Error al cargar la aplicación: " + e.getMessage());
        } catch (SQLException e) {
            mensajeLabel.setText(mensajeConexionDb(e));
        } catch (RuntimeException e) {
            mensajeLabel.setText(mensajeConexionDb(e));
        }
    }

    private static String mensajeConexionDb(Throwable e) {
        Throwable t = e;
        while (t != null) {
            String m = t.getMessage() != null ? t.getMessage() : "";
            if (m.contains("Unknown column 'intentos_fallidos'")
                    || m.contains("Unknown column 'bloqueado_hasta'")
                    || m.contains("auditoria_evento")) {
                return "Base de datos desactualizada. Ejecute 03_seguridad_auditoria.sql o: docker compose down -v && docker compose up -d";
            }
            if (m.contains("Communications link failure")
                    || m.contains("Connection refused")
                    || m.contains("Could not create connection to database server")) {
                return "No hay conexión con MySQL. Ejecute: docker compose up -d";
            }
            t = t.getCause();
        }
        return "Error de base de datos: " + e.getMessage();
    }
}
