package com.epn.conexion.dronesproject.vista;

/** Controller de escenaLiviano.fxml */
public class PedidoLivianoController extends PedidoFormControllerBase {

    @Override
    protected String tipoDron() {
        return "LIVIANO";
    }

    @Override
    protected String tituloPantalla() {
        return "Dron Liviano";
    }
}
