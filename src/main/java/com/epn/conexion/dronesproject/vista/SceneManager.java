package com.epn.conexion.dronesproject.vista;

import com.epn.conexion.dronesproject.vista.cliente.SessionManager;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.URL;
import java.util.function.Consumer;

/**
 * Cambia la escena de la ventana principal.
 *
 * Los FXML NO estan en el paquete de estas clases (vista), sino directamente
 * en /com/epn/conexion/dronesproject/, junto a application.properties. Por eso
 * las rutas se resuelven de forma absoluta desde la raiz del classpath en vez
 * de relativa a la clase.
 */
public final class SceneManager {

    private static final String RUTA_BASE = "/com/epn/conexion/dronesproject/";

    private SceneManager() {}

    /** Cambio de pantalla simple, sin datos que pasar. */
    public static void cambiarEscena(Stage stage, String nombreFxml, String titulo) {
        cambiarEscena(stage, nombreFxml, titulo, Object.class, controlador -> {});
    }

    /** Vuelve a la pantalla de seleccion de tipo, manteniendo la sesion abierta. */
    public static void irAInicio(Stage stage) {
        cambiarEscena(stage, "tipoDron-view.fxml", "Seleccion de dron");
    }

    /**
     * Cierra la sesion y vuelve al login.
     *
     * Esta aqui, y no repetido en cada controller, porque "cerrar sesion" son
     * dos cosas que tienen que pasar juntas: limpiar el token Y salir de la
     * zona autenticada. Si mañana hay que limpiar algo mas al salir, se agrega
     * en un solo sitio y no en los tres botones.
     */
    public static void cerrarSesion(Stage stage) {
        SessionManager.getInstancia().cerrarSesion();
        cambiarEscena(stage, "login-view.fxml", "Home-Drones - Inicio de sesion");
    }

    /**
     * Cambio de pantalla configurando el controller ANTES de mostrarla.
     *
     * El configurador se ejecuta entre el load() y el show(), que es el punto
     * clave: si en su lugar este metodo devolviera el controller para que el
     * llamador lo configurara despues, la ventana ya estaria visible y el
     * usuario veria un parpadeo con los campos vacios antes de que se llenen.
     * Con un Consumer eso no puede pasar, porque no hay forma de llamarlo
     * "tarde".
     *
     * El parametro Class<C> evita el cast sin comprobar: si el fx:controller
     * del FXML no coincide con el tipo esperado, falla aqui con un mensaje
     * claro en vez de con un ClassCastException a destiempo.
     *
     * @param tipoControlador clase del controller declarado en el fx:controller
     * @param configurador    recibe el controller ya construido e inyectado
     */
    public static <C> void cambiarEscena(Stage stage, String nombreFxml, String titulo,
                                         Class<C> tipoControlador, Consumer<C> configurador) {
        URL recurso = SceneManager.class.getResource(RUTA_BASE + nombreFxml);
        if (recurso == null) {
            throw new IllegalStateException("No se encontro el FXML en el classpath: "
                    + RUTA_BASE + nombreFxml);
        }

        FXMLLoader cargador = new FXMLLoader(recurso);
        Parent raiz;
        try {
            raiz = cargador.load();
        } catch (IOException e) {
            // Un FXML roto es un error de programacion, no algo que el usuario
            // pueda corregir; se propaga sin envolver en un dialogo.
            throw new UncheckedIOException("No se pudo cargar " + nombreFxml, e);
        }

        Object controlador = cargador.getController();
        if (controlador != null && tipoControlador.isInstance(controlador)) {
            configurador.accept(tipoControlador.cast(controlador));
        } else if (controlador != null && tipoControlador != Object.class) {
            throw new IllegalStateException("El fx:controller de " + nombreFxml + " es "
                    + controlador.getClass().getName() + ", no " + tipoControlador.getName());
        }

        stage.setScene(new Scene(raiz));
        stage.setTitle(titulo);
        stage.show();
    }
}
