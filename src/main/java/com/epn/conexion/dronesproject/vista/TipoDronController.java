package com.epn.conexion.dronesproject.vista;

import com.epn.conexion.dronesproject.vista.cliente.SessionManager;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.stage.Stage;

/** Controller de tipoDron-view.fxml */
public class TipoDronController {

    @FXML
    private Button btn_liviano;
    @FXML
    private Button btn_pesado;
    @FXML
    private Button btn_emergencia;
    @FXML
    private Button btn_cerrar_sesion;
    @FXML
    private Button btn_admin_catalogo;

    /**
     * Oculta la entrada al catalogo para quien no sea ADMINISTRADOR.
     *
     * setManaged(false) ademas de setVisible(false): un nodo invisible pero
     * "managed" sigue ocupando su hueco en el layout, y quedaria un espacio
     * vacio raro en la pantalla del CLIENTE.
     *
     * Esto es comodidad de interfaz, no seguridad: aunque alguien lograra
     * llegar a la pantalla, el backend responde 403 a las rutas de catalogo
     * porque quien manda es el rol del token, no lo que muestre la ventana.
     */
    @FXML
    public void initialize() {
        boolean esAdmin = SessionManager.getInstancia().esAdministrador();
        btn_admin_catalogo.setVisible(esAdmin);
        btn_admin_catalogo.setManaged(esAdmin);
    }

    @FXML
    public void irAAdminCatalogo() {
        SceneManager.cambiarEscena(ventana(), "admin-drones-view.fxml", "Administrar catalogo de drones");
    }

    @FXML
    public void seleccionarLiviano() {
        SceneManager.cambiarEscena(ventana(), "escenaLiviano.fxml", "Pedido - Dron Liviano");
    }

    @FXML
    public void seleccionarPesado() {
        // "Pesado" es solo el nombre de la pantalla; el tipo que entiende el
        // backend es CARGA. La traduccion la hace PedidoPesadoController.
        SceneManager.cambiarEscena(ventana(), "escenaPesado.fxml", "Pedido - Dron Pesado");
    }

    @FXML
    public void seleccionarEmergencia() {
        SceneManager.cambiarEscena(ventana(), "escenaEmergencia.fxml", "Pedido - Dron Emergencia");
    }

    @FXML
    public void cerrarSesion() {
        SceneManager.cerrarSesion(ventana());
    }

    private Stage ventana() {
        return (Stage) btn_liviano.getScene().getWindow();
    }
}
