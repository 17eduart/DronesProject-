package com.epn.conexion.dronesproject.vista;

import com.epn.conexion.dronesproject.modelo.FacturaResponse;
import com.epn.conexion.dronesproject.modelo.PedidoRequest;
import com.epn.conexion.dronesproject.modelo.PedidoResponse;
import com.epn.conexion.dronesproject.vista.cliente.ApiClient;
import com.epn.conexion.dronesproject.vista.cliente.ApiException;
import com.epn.conexion.dronesproject.vista.cliente.DronResponse;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.util.List;

/**
 * Logica compartida por las tres pantallas de pedido (liviano, pesado y
 * emergencia), que son identicas salvo por el tipo de dron que consultan.
 *
 * Las subclases solo declaran su tipo y su titulo; toda la mecanica de cargar
 * modelos, leer el formulario, llamar a la API y navegar a la factura vive
 * aqui. Es el mismo criterio que en el backend con Dron.validarCapacidad: la
 * regla que es igual para los tres se escribe una vez.
 *
 * FXMLLoader inyecta los @FXML declarados en superclases (su ControllerAccessor
 * recorre la jerarquia con getSuperclass), asi que estos campos se llenan
 * aunque el fx:id este en el FXML de la subclase.
 */
public abstract class PedidoFormControllerBase {

    @FXML
    protected ComboBox<DronResponse> box_modelo;
    @FXML
    protected TextField lbl_distancia;
    @FXML
    protected TextField lbl_peso;
    @FXML
    protected TextField lbl_tiempo_vuelo;
    @FXML
    protected Button btn_cotizar;
    @FXML
    protected Button btn_volver;
    @FXML
    protected Button btn_cerrar_sesion;
    @FXML
    protected Label lbl_error;

    /** Tipo tal como lo espera el backend: LIVIANO, CARGA o EMERGENCIA. */
    protected abstract String tipoDron();

    /** Nombre visible del tipo ("Dron Liviano"), para mensajes y titulos. */
    protected abstract String tituloPantalla();

    /**
     * Lo llama FXMLLoader al terminar de inyectar los campos. Aqui se carga el
     * ComboBox porque antes de este punto box_modelo todavia es null.
     */
    @FXML
    public void initialize() {
        cargarModelos();
    }

    private void cargarModelos() {
        try {
            // GET /dron devuelve el catalogo completo; el filtrado por tipo se
            // hace aqui porque el endpoint no acepta filtro.
            List<DronResponse> delTipo = ApiClient.getInstancia().obtenerDrones().stream()
                    .filter(dron -> tipoDron().equalsIgnoreCase(dron.getTipo()))
                    .toList();

            box_modelo.setItems(FXCollections.observableArrayList(delTipo));

            if (delTipo.isEmpty()) {
                mostrarError("No hay modelos de " + tituloPantalla() + " en el catalogo.");
            } else {
                box_modelo.getSelectionModel().selectFirst();
            }
        } catch (ApiException e) {
            mostrarError(e.getMessage());
        }
    }

    @FXML
    public void cotizar() {
        limpiarError();

        DronResponse seleccionado = box_modelo.getValue();
        if (seleccionado == null) {
            mostrarError("Seleccione un modelo de dron.");
            return;
        }

        Double distancia = leerNumero(lbl_distancia, "distancia");
        if (distancia == null) {
            return;
        }
        Double peso = leerNumero(lbl_peso, "peso");
        if (peso == null) {
            return;
        }
        Double horas = leerNumero(lbl_tiempo_vuelo, "tiempo de vuelo");
        if (horas == null) {
            return;
        }

        PedidoRequest solicitud = new PedidoRequest();
        solicitud.setCodigoDron(seleccionado.getCodigo());
        solicitud.setDistanciaSolicitada(distancia);
        solicitud.setPesoSolicitado(peso);
        solicitud.setHorasSolicitadas(horas);

        try {
            PedidoResponse pedido = ApiClient.getInstancia().crearPedido(solicitud);
            FacturaResponse factura = ApiClient.getInstancia().obtenerFactura(pedido.getId());

            SceneManager.cambiarEscena(ventana(), "factura-view.fxml", "Factura - " + tituloPantalla(),
                    FacturaController.class,
                    controlador -> controlador.mostrarFactura(factura, pedido));
        } catch (ApiException e) {
            // Si el backend rechazo por capacidad excedida (400), aqui llega su
            // mensaje real ("El peso solicitado (50.0 kg) supera..."), no uno
            // generico: es la informacion que el usuario necesita para corregir.
            mostrarError(e.getMessage());
        }
    }

    /** Vuelve a elegir tipo de dron. La sesion sigue abierta. */
    @FXML
    public void volver() {
        SceneManager.irAInicio(ventana());
    }

    @FXML
    public void cerrarSesion() {
        SceneManager.cerrarSesion(ventana());
    }

    /**
     * Lee un campo numerico. Devuelve null y deja el error puesto si el texto
     * no es un numero valido o no es positivo.
     */
    private Double leerNumero(TextField campo, String nombre) {
        String texto = campo.getText() == null ? "" : campo.getText().trim().replace(',', '.');

        if (texto.isEmpty()) {
            mostrarError("Ingrese la " + nombre + ".");
            return null;
        }
        try {
            double valor = Double.parseDouble(texto);
            if (valor <= 0) {
                mostrarError("La " + nombre + " debe ser mayor que cero.");
                return null;
            }
            return valor;
        } catch (NumberFormatException e) {
            mostrarError("La " + nombre + " debe ser un numero. Valor recibido: " + texto);
            return null;
        }
    }

    protected void mostrarError(String mensaje) {
        lbl_error.setStyle("-fx-text-fill: #c0392b;");
        lbl_error.setText(mensaje);
    }

    protected void limpiarError() {
        lbl_error.setText("");
    }

    protected Stage ventana() {
        return (Stage) box_modelo.getScene().getWindow();
    }
}
