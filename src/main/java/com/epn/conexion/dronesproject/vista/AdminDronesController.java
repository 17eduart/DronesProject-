package com.epn.conexion.dronesproject.vista;

import com.epn.conexion.dronesproject.modelo.DronRequest;
import com.epn.conexion.dronesproject.vista.cliente.ApiClient;
import com.epn.conexion.dronesproject.vista.cliente.ApiException;
import com.epn.conexion.dronesproject.vista.cliente.DronResponse;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.util.List;
import java.util.Optional;

/**
 * Controller de admin-drones-view.fxml: CRUD del catalogo de drones.
 *
 * Solo tiene sentido para un ADMINISTRADOR. La pantalla no se protege sola:
 * quien decide es el backend, que responde 403 a POST/PUT/DELETE de /dron si
 * el token no es de administrador. Aqui esos 403 se muestran en lbl_error con
 * el mensaje real, igual que cualquier otro error de la API.
 */
public class AdminDronesController {

    /** Los tres tipos que reconoce DronServicio. Cualquier otro da 400. */
    private static final List<String> TIPOS = List.of("LIVIANO", "CARGA", "EMERGENCIA");

    @FXML
    private TableView<DronResponse> tabla_drones;
    @FXML
    private TableColumn<DronResponse, String> col_codigo;
    @FXML
    private TableColumn<DronResponse, String> col_tipo;
    @FXML
    private TableColumn<DronResponse, String> col_modelo;
    @FXML
    private TableColumn<DronResponse, Double> col_distancia;
    @FXML
    private TableColumn<DronResponse, Double> col_peso;
    @FXML
    private TableColumn<DronResponse, Double> col_horas;

    @FXML
    private ComboBox<String> box_tipo;
    @FXML
    private TextField lbl_codigo;
    @FXML
    private TextField lbl_modelo;
    @FXML
    private TextField lbl_distancia;
    @FXML
    private TextField lbl_peso;
    @FXML
    private TextField lbl_horas;

    @FXML
    private Button btn_agregar;
    @FXML
    private Button btn_actualizar;
    @FXML
    private Button btn_eliminar;
    @FXML
    private Button btn_limpiar;
    @FXML
    private Button btn_volver;
    @FXML
    private Button btn_cerrar_sesion;

    @FXML
    private Label lbl_error;

    @FXML
    public void initialize() {
        configurarColumnas();

        box_tipo.setItems(FXCollections.observableArrayList(TIPOS));

        tabla_drones.getSelectionModel().selectedItemProperty()
                .addListener((observable, anterior, seleccionado) -> {
                    if (seleccionado != null) {
                        cargarEnFormulario(seleccionado);
                    }
                });

        recargarTabla();
    }

    /**
     * Las columnas no traen cellValueFactory desde el FXML. Se usan lambdas en
     * vez de PropertyValueFactory porque estas se comprueban al compilar: si
     * alguien renombra getDistanciaKm(), esto deja de compilar, mientras que
     * PropertyValueFactory("distanciaKm") fallaria en silencio dejando la
     * columna vacia al ejecutar.
     */
    private void configurarColumnas() {
        col_codigo.setCellValueFactory(fila -> new SimpleStringProperty(fila.getValue().getCodigo()));
        col_tipo.setCellValueFactory(fila -> new SimpleStringProperty(fila.getValue().getTipo()));
        col_modelo.setCellValueFactory(fila -> new SimpleStringProperty(fila.getValue().getModelo()));
        col_distancia.setCellValueFactory(fila -> new SimpleObjectProperty<>(fila.getValue().getDistanciaKm()));
        col_peso.setCellValueFactory(fila -> new SimpleObjectProperty<>(fila.getValue().getPesoMaximo()));
        col_horas.setCellValueFactory(fila -> new SimpleObjectProperty<>(fila.getValue().getHorasVuelo()));
    }

    private void recargarTabla() {
        try {
            tabla_drones.setItems(FXCollections.observableArrayList(
                    ApiClient.getInstancia().obtenerDrones()));
        } catch (ApiException e) {
            tabla_drones.setItems(FXCollections.observableArrayList());
            mostrarError(e.getMessage());
        }
    }

    /**
     * Vuelca la fila seleccionada en el formulario para editarla.
     *
     * Codigo y tipo quedan bloqueados: los dos forman la identidad de la fila.
     * El codigo es la clave primaria, y el tipo es el discriminador de la
     * herencia SINGLE_TABLE, asi que cambiarlo en un PUT hace que Hibernate
     * falle con DuplicateKeyException ("a different object with the same
     * identifier...") y la API devuelva 500. Para cambiar el tipo de un dron
     * hay que eliminarlo y crearlo de nuevo.
     */
    private void cargarEnFormulario(DronResponse dron) {
        limpiarError();

        lbl_codigo.setText(dron.getCodigo());
        lbl_modelo.setText(dron.getModelo());
        lbl_distancia.setText(textoDe(dron.getDistanciaKm()));
        lbl_peso.setText(textoDe(dron.getPesoMaximo()));
        lbl_horas.setText(textoDe(dron.getHorasVuelo()));
        box_tipo.setValue(dron.getTipo());

        lbl_codigo.setDisable(true);
        box_tipo.setDisable(true);
    }

    // ------------------------------------------------------------------
    // Acciones
    // ------------------------------------------------------------------

    @FXML
    public void agregarDron() {
        limpiarError();

        String tipo = box_tipo.getValue();
        if (tipo == null) {
            mostrarError("Seleccione el tipo de dron.");
            return;
        }
        String codigo = texto(lbl_codigo);
        if (codigo.isEmpty()) {
            mostrarError("Ingrese el codigo del dron.");
            return;
        }

        DronRequest solicitud = leerFormulario(codigo);
        if (solicitud == null) {
            return; // leerFormulario ya dejo el error puesto
        }

        try {
            ApiClient.getInstancia().crearDron(tipo, solicitud);
            recargarTabla();
            limpiarFormulario();
            mostrarExito("Dron " + codigo + " agregado.");
        } catch (ApiException e) {
            // Mensaje real del backend: limites de peso/minutos de la subclase,
            // tipo inexistente, o 403 si el token no es de administrador.
            mostrarError(e.getMessage());
        }
    }

    @FXML
    public void actualizarDron() {
        limpiarError();

        DronResponse seleccionado = tabla_drones.getSelectionModel().getSelectedItem();
        if (seleccionado == null) {
            mostrarError("Seleccione en la tabla el dron que quiere actualizar.");
            return;
        }

        // Codigo y tipo se toman de la fila, no del formulario: alli estan
        // deshabilitados justamente para que no se puedan cambiar.
        DronRequest solicitud = leerFormulario(seleccionado.getCodigo());
        if (solicitud == null) {
            return;
        }

        try {
            ApiClient.getInstancia().actualizarDron(
                    seleccionado.getTipo(), seleccionado.getCodigo(), solicitud);
            recargarTabla();
            limpiarFormulario();
            mostrarExito("Dron " + seleccionado.getCodigo() + " actualizado.");
        } catch (ApiException e) {
            mostrarError(e.getMessage());
        }
    }

    @FXML
    public void eliminarDron() {
        limpiarError();

        DronResponse seleccionado = tabla_drones.getSelectionModel().getSelectedItem();
        if (seleccionado == null) {
            mostrarError("Seleccione en la tabla el dron que quiere eliminar.");
            return;
        }

        if (!confirmarEliminacion(seleccionado)) {
            return;
        }

        try {
            ApiClient.getInstancia().eliminarDron(seleccionado.getCodigo());
            recargarTabla();
            limpiarFormulario();
            mostrarExito("Dron " + seleccionado.getCodigo() + " eliminado.");
        } catch (ApiException e) {
            mostrarError(e.getMessage());
        }
    }

    @FXML
    public void limpiarFormulario() {
        limpiarError();

        lbl_codigo.clear();
        lbl_modelo.clear();
        lbl_distancia.clear();
        lbl_peso.clear();
        lbl_horas.clear();
        box_tipo.setValue(null);

        lbl_codigo.setDisable(false);
        box_tipo.setDisable(false);

        // Deselecciona; el listener ignora el null, asi que no repuebla nada.
        tabla_drones.getSelectionModel().clearSelection();
    }

    @FXML
    public void volver() {
        SceneManager.irAInicio(ventana());
    }

    @FXML
    public void cerrarSesion() {
        SceneManager.cerrarSesion(ventana());
    }

    // ------------------------------------------------------------------
    // Apoyo
    // ------------------------------------------------------------------

    /**
     * Arma el DronRequest validando los tres numericos. Devuelve null y deja
     * el mensaje en lbl_error si algo no es un numero valido, para que el
     * error se vea sin gastar una peticion ni tumbar la aplicacion.
     */
    private DronRequest leerFormulario(String codigo) {
        String modelo = texto(lbl_modelo);
        if (modelo.isEmpty()) {
            mostrarError("Ingrese el modelo del dron.");
            return null;
        }

        Double distancia = leerNumero(lbl_distancia, "distancia");
        if (distancia == null) {
            return null;
        }
        Double peso = leerNumero(lbl_peso, "peso maximo");
        if (peso == null) {
            return null;
        }
        Double horas = leerNumero(lbl_horas, "horas de vuelo");
        if (horas == null) {
            return null;
        }

        DronRequest solicitud = new DronRequest();
        solicitud.setCodigo(codigo);
        solicitud.setModelo(modelo);
        solicitud.setDistancia_km(distancia);
        solicitud.setPeso_maximo(peso);
        solicitud.setHoras_vuelo(horas);
        return solicitud;
    }

    private Double leerNumero(TextField campo, String nombre) {
        String valor = texto(campo).replace(',', '.');

        if (valor.isEmpty()) {
            mostrarError("Ingrese " + nombre + ".");
            return null;
        }
        try {
            double numero = Double.parseDouble(valor);
            if (numero <= 0) {
                mostrarError("El valor de " + nombre + " debe ser mayor que cero.");
                return null;
            }
            return numero;
        } catch (NumberFormatException e) {
            mostrarError("El valor de " + nombre + " debe ser un numero. Se recibio: " + valor);
            return null;
        }
    }

    private boolean confirmarEliminacion(DronResponse dron) {
        Alert confirmacion = new Alert(Alert.AlertType.CONFIRMATION);
        confirmacion.setTitle("Confirmar eliminacion");
        confirmacion.setHeaderText(null);
        confirmacion.setContentText("¿Eliminar el dron " + dron.getCodigo()
                + " (" + dron.getModelo() + ")? Esta accion no se puede deshacer.");

        Optional<ButtonType> respuesta = confirmacion.showAndWait();
        return respuesta.isPresent() && respuesta.get() == ButtonType.OK;
    }

    private String texto(TextField campo) {
        return campo.getText() == null ? "" : campo.getText().trim();
    }

    private String textoDe(Double valor) {
        return valor == null ? "" : String.valueOf(valor);
    }

    private void mostrarError(String mensaje) {
        lbl_error.setStyle("-fx-text-fill: #c0392b;");
        lbl_error.setText(mensaje);
    }

    private void mostrarExito(String mensaje) {
        lbl_error.setStyle("-fx-text-fill: #1e8449;");
        lbl_error.setText(mensaje);
    }

    private void limpiarError() {
        lbl_error.setText("");
    }

    private Stage ventana() {
        return (Stage) tabla_drones.getScene().getWindow();
    }
}
