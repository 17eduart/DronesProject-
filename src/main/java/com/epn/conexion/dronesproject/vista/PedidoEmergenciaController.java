package com.epn.conexion.dronesproject.vista;

/** Controller de escenaEmergencia.fxml */
public class PedidoEmergenciaController extends PedidoFormControllerBase {

    @Override
    protected String tipoDron() {
        return "EMERGENCIA";
    }

    @Override
    protected String tituloPantalla() {
        return "Dron Emergencia";
    }
}
