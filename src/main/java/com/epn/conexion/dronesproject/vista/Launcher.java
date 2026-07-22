package com.epn.conexion.dronesproject.vista;

import javafx.application.Application;

/**
 * Punto de entrada del cliente JavaFX.
 *
 * Existe separado de DronesApp a proposito: si la clase con el main() extiende
 * Application, el lanzador de Java exige que los modulos de JavaFX esten en el
 * module-path y falla con "JavaFX runtime components are missing". Con esta
 * clase intermedia, que no extiende Application, la app arranca tambien desde
 * el classpath, que es como esta configurado este proyecto (no tiene
 * module-info.java).
 */
public class Launcher {
    public static void main(String[] args) {
        Application.launch(DronesApp.class, args);
    }
}
