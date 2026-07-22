package com.epn.conexion.dronesproject.vista;

import com.epn.conexion.dronesproject.modelo.FacturaResponse;
import com.epn.conexion.dronesproject.modelo.PedidoResponse;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.util.Locale;

/**
 * Controller de factura-view.fxml
 *
 * Recibe dos objetos, no uno: FacturaResponse trae el numero, el cliente, el
 * detalle del dron y el desglose de COSTOS, pero los campos
 * "Distancia Recorrida" y "Peso del paquete" del formulario piden las
 * magnitudes del servicio (km y kg), que viven en PedidoResponse. Poner ahi
 * componenteDistancia/componentePeso habria mostrado dolares bajo una etiqueta
 * que dice kilometros.
 *
 * Ambos quedan guardados como campos porque el PDF se arma con ellos: no se
 * vuelve a consultar la API al imprimir.
 */
public class FacturaController {

    @FXML
    private TextField lbl_Nfactura;
    @FXML
    private TextField lbl_cliente;
    @FXML
    private TextField lbl_tipoDron;
    @FXML
    private TextField lbl_costoBase;
    @FXML
    private TextField lbl_distanciaRecorrida;
    @FXML
    private TextField lbl_pesoPaquete;
    @FXML
    private TextField lbl_totalFactura;
    @FXML
    private Button btn_imprimirFactura;
    @FXML
    private Button btn_volver;
    @FXML
    private Button btn_cerrar_sesion;

    private FacturaResponse factura;
    private PedidoResponse pedido;

    /** La llama SceneManager antes de mostrar la pantalla. */
    public void mostrarFactura(FacturaResponse factura, PedidoResponse pedido) {
        this.factura = factura;
        this.pedido = pedido;

        lbl_Nfactura.setText(factura.getNumeroFactura());
        lbl_cliente.setText(factura.getNombreCliente());
        lbl_tipoDron.setText(factura.getDetalleDron());
        lbl_costoBase.setText(dinero(factura.getCostoBase()));
        lbl_totalFactura.setText(dinero(factura.getTotal()));

        lbl_distanciaRecorrida.setText(magnitud(pedido.getDistanciaSolicitada(), "km"));
        lbl_pesoPaquete.setText(magnitud(pedido.getPesoSolicitado(), "kg"));
    }

    @FXML
    public void imprimirFactura() {
        if (factura == null || pedido == null) {
            alerta(Alert.AlertType.ERROR, "No hay una factura cargada para imprimir.");
            return;
        }

        FileChooser selector = new FileChooser();
        selector.setTitle("Guardar factura");
        selector.setInitialFileName(GeneradorFacturaPdf.nombreSugerido(factura));
        selector.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Documento PDF", "*.pdf"));

        File destino = selector.showSaveDialog(ventana());
        if (destino == null) {
            return; // el usuario cancelo: no hay nada que avisar
        }

        try {
            GeneradorFacturaPdf.generar(factura, pedido, destino);
            alerta(Alert.AlertType.INFORMATION,
                    "Factura guardada en:\n" + destino.getAbsolutePath());
        } catch (IOException e) {
            // Carpeta protegida, disco lleno, archivo abierto en otro
            // programa... se informa y la aplicacion sigue funcionando.
            alerta(Alert.AlertType.ERROR,
                    "No se pudo guardar el PDF.\n" + e.getMessage());
        }
    }

    /** Vuelve a cotizar sin cerrar la sesion. */
    @FXML
    public void volverAlInicio() {
        SceneManager.irAInicio(ventana());
    }

    @FXML
    public void cerrarSesion() {
        SceneManager.cerrarSesion(ventana());
    }

    private void alerta(Alert.AlertType tipo, String mensaje) {
        Alert dialogo = new Alert(tipo);
        dialogo.setTitle(tipo == Alert.AlertType.ERROR ? "Error" : "Listo");
        dialogo.setHeaderText(null);
        dialogo.setContentText(mensaje);
        dialogo.showAndWait();
    }

    private Stage ventana() {
        return (Stage) lbl_Nfactura.getScene().getWindow();
    }

    private String dinero(Double valor) {
        return valor == null ? "" : String.format(Locale.US, "$ %.2f", valor);
    }

    private String magnitud(Double valor, String unidad) {
        return valor == null ? "" : String.format(Locale.US, "%.2f %s", valor, unidad);
    }
}
