package com.epn.conexion.dronesproject.vista;

import javafx.application.Application;
import javafx.stage.Stage;

/**
 * Aplicacion JavaFX. Arranca en la pantalla de login.
 *
 * Es solo el cliente: da por hecho que el backend Spring Boot ya esta corriendo
 * en http://localhost:8080 (se puede apuntar a otro con -Dapi.base.url=...).
 * Si no lo esta, el login mostrara el error de conexion en pantalla en vez de
 * caerse.
 */
public class DronesApp extends Application {

    @Override
    public void start(Stage stage) {
        stage.setResizable(false);
        SceneManager.cambiarEscena(stage, "login-view.fxml", "Home-Drones - Inicio de sesion");
    }
}
