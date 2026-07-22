package com.epn.conexion.dronesproject.vista;

import com.epn.conexion.dronesproject.vista.cliente.ApiClient;
import com.epn.conexion.dronesproject.vista.cliente.ApiException;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

/** Controller de register-view.fxml */
public class RegistroController {

    @FXML
    private TextField lbl_username;
    @FXML
    private PasswordField lbl_password;
    @FXML
    private PasswordField lbl_password_repetir;
    @FXML
    private Button btn_registrarse;
    @FXML
    private Button btn_volver_login;
    @FXML
    private Label lbl_error;

    @FXML
    public void registrarUsuario() {
        lbl_error.setText("");

        String usuario = lbl_username.getText() == null ? "" : lbl_username.getText().trim();
        String clave = lbl_password.getText() == null ? "" : lbl_password.getText();
        String claveRepetida = lbl_password_repetir.getText() == null ? "" : lbl_password_repetir.getText();

        if (usuario.isEmpty() || clave.isEmpty() || claveRepetida.isEmpty()) {
            mostrarError("Complete todos los campos.");
            return;
        }

        // Validacion local: no tiene sentido gastar una peticion HTTP para
        // descubrir algo que se puede comprobar aqui mismo.
        if (!clave.equals(claveRepetida)) {
            mostrarError("Las contraseñas no coinciden.");
            return;
        }

        try {
            ApiClient.getInstancia().registrar(usuario, clave);

            // El registro publico siempre crea un CLIENTE; el rol no se manda
            // desde aqui y el backend lo ignoraria de todos modos.
            SceneManager.cambiarEscena(ventana(), "login-view.fxml", "Inicio de sesion",
                    LoginController.class,
                    login -> login.mostrarMensaje("Usuario " + usuario + " registrado. Ya puede iniciar sesion."));
        } catch (ApiException e) {
            mostrarError(e.getMessage());
        }
    }

    /** Vuelve al login sin registrar nada; no toca la API. */
    @FXML
    public void volverALogin() {
        SceneManager.cambiarEscena(ventana(), "login-view.fxml", "Home-Drones - Inicio de sesion");
    }

    private void mostrarError(String mensaje) {
        lbl_error.setStyle("-fx-text-fill: #c0392b;");
        lbl_error.setText(mensaje);
    }

    private Stage ventana() {
        return (Stage) lbl_username.getScene().getWindow();
    }
}
