package com.epn.conexion.dronesproject.modelo;

/**
 * Body de POST /pedidos.
 *
 * No tiene campo usuario ni costoTotal a proposito: el usuario sale del token
 * autenticado y el costo lo calcula el servidor. Mismo patron que
 * UsuarioRequest, para que el cliente no pueda pedir a nombre de otro ni
 * fijarse su propio precio.
 */
public class PedidoRequest {
    private String codigoDron;
    private Double distanciaSolicitada;
    private Double pesoSolicitado;
    private Double horasSolicitadas;

    public String getCodigoDron() {
        return codigoDron;
    }

    public void setCodigoDron(String codigoDron) {
        this.codigoDron = codigoDron;
    }

    public Double getDistanciaSolicitada() {
        return distanciaSolicitada;
    }

    public void setDistanciaSolicitada(Double distanciaSolicitada) {
        this.distanciaSolicitada = distanciaSolicitada;
    }

    public Double getPesoSolicitado() {
        return pesoSolicitado;
    }

    public void setPesoSolicitado(Double pesoSolicitado) {
        this.pesoSolicitado = pesoSolicitado;
    }

    public Double getHorasSolicitadas() {
        return horasSolicitadas;
    }

    public void setHorasSolicitadas(Double horasSolicitadas) {
        this.horasSolicitadas = horasSolicitadas;
    }
}
