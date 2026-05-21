package com.husrt.ui.cuenta;

import com.husrt.service.AuditoriaService;
import com.husrt.service.AuthService;
import com.husrt.session.SessionContext;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.stage.Stage;

public class CambioClaveController {

    @FXML private PasswordField claveActual;
    @FXML private PasswordField claveNueva;
    @FXML private PasswordField claveConfirmar;
    @FXML private Label msgCambio;

    private final AuthService auth = new AuthService();
    private final AuditoriaService auditoria = new AuditoriaService();

    @FXML
    private void onGuardar() {
        msgCambio.setText("");
        if (SessionContext.getCurrent() == null) {
            msgCambio.setText("Sesión no válida.");
            return;
        }
        if (claveNueva.getText().length() < 6) {
            msgCambio.setText("La nueva contraseña debe tener al menos 6 caracteres.");
            return;
        }
        if (!claveNueva.getText().equals(claveConfirmar.getText())) {
            msgCambio.setText("La confirmación no coincide.");
            return;
        }
        try {
            long id = SessionContext.getCurrent().idUsuario();
            auth.cambiarPassword(id, claveActual.getText(), claveNueva.getText());
            auditoria.registrar("CUENTA", "CAMBIO_CLAVE", "Contraseña actualizada por el usuario");
            msgCambio.setStyle("-fx-text-fill: green;");
            msgCambio.setText("Contraseña actualizada correctamente.");
            claveActual.clear();
            claveNueva.clear();
            claveConfirmar.clear();
        } catch (Exception e) {
            msgCambio.setStyle("-fx-text-fill: red;");
            msgCambio.setText(e.getMessage());
        }
    }

    @FXML
    private void onCancelar() {
        Stage stage = (Stage) claveActual.getScene().getWindow();
        stage.close();
    }
}
