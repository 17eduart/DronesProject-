package com.epn.conexion.dronesproject.vista;

import com.epn.conexion.dronesproject.modelo.LoginResponse;
import com.epn.conexion.dronesproject.vista.cliente.ApiClient;
import com.epn.conexion.dronesproject.vista.cliente.ApiException;
import com.epn.conexion.dronesproject.vista.cliente.SessionManager;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

/** Controller de login-view.fxml */
public class LoginController {

    @FXML
    private TextField lbl_username;
    @FXML
    private PasswordField lbl_password;
    @FXML
    private Button btn_inicio_sesion;
    @FXML
    private Button btn_registro;
    @FXML
    private Label lbl_error;

    /** Lo llama RegistroController tras un alta correcta. */
    public void mostrarMensaje(String mensaje) {
        lbl_error.setStyle("-fx-text-fill: #1e8449;");
        lbl_error.setText(mensaje);
    }

    @FXML
    public void iniciarSesion() {
        limpiarError();

        String usuario = lbl_username.getText() == null ? "" : lbl_username.getText().trim();
        String clave = lbl_password.getText() == null ? "" : lbl_password.getText();

        if (usuario.isEmpty() || clave.isEmpty()) {
            mostrarError("Ingrese usuario y contraseña.");
            return;
        }

        try {
            LoginResponse respuesta = ApiClient.getInstancia().login(usuario, clave);
            SessionManager.getInstancia().guardarSesion(
                    respuesta.getToken(), respuesta.getUsername(), respuesta.getRol());

            SceneManager.cambiarEscena(ventana(), "tipoDron-view.fxml", "Seleccion de dron");
        } catch (ApiException e) {
            // Credenciales malas o servidor caido: se avisa y la app sigue viva.
            mostrarError(e.getMessage());
        }
    }

    @FXML
    public void irARegistro() {
        SceneManager.cambiarEscena(ventana(), "register-view.fxml", "Registro");
    }

    private void mostrarError(String mensaje) {
        lbl_error.setStyle("-fx-text-fill: #c0392b;");
        lbl_error.setText(mensaje);
    }

    private void limpiarError() {
        lbl_error.setText("");
    }

    private Stage ventana() {
        return (Stage) lbl_username.getScene().getWindow();
    }
}
