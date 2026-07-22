package com.epn.conexion.dronesproject.vista;

/**
 * Controller de escenaPesado.fxml
 *
 * OJO con el nombre: la interfaz habla de dron "Pesado", pero el backend solo
 * conoce los tipos LIVIANO, CARGA y EMERGENCIA. Mandar "PESADO" hace que
 * DronServicio lance IllegalArgumentException y la API responda 400. La
 * traduccion Pesado -> CARGA se hace aqui y en ningun otro lado.
 */
public class PedidoPesadoController extends PedidoFormControllerBase {

    @Override
    protected String tipoDron() {
        return "CARGA";
    }

    @Override
    protected String tituloPantalla() {
        return "Dron Pesado";
    }
}
