package com.epn.conexion.dronesproject.modelo;

import java.time.LocalDateTime;

/**
 * Lo que se devuelve al cliente tras crear o consultar un pedido.
 * Solo se serializa, por eso no tiene setters.
 */
public class PedidoResponse {
    private final Long id;
    private final String username;
    private final String codigoDron;
    private final String modeloDron;
    private final String tipoDron;
    private final Double distanciaSolicitada;
    private final Double pesoSolicitado;
    private final Double horasSolicitadas;
    private final Double costoTotal;
    private final LocalDateTime fechaCreacion;

    private PedidoResponse(Pedido pedido) {
        this.id = pedido.getId();
        this.username = pedido.getUsuario().getUsername();
        this.codigoDron = pedido.getDron().getCodigo();
        this.modeloDron = pedido.getDron().getModelo();
        this.tipoDron = pedido.getDron().getTipo();
        this.distanciaSolicitada = pedido.getDistanciaSolicitada();
        this.pesoSolicitado = pedido.getPesoSolicitado();
        this.horasSolicitadas = pedido.getHorasSolicitadas();
        this.costoTotal = pedido.getCostoTotal();
        this.fechaCreacion = pedido.getFechaCreacion();
    }

    public static PedidoResponse desde(Pedido pedido) {
        return new PedidoResponse(pedido);
    }

    public Long getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public String getCodigoDron() {
        return codigoDron;
    }

    public String getModeloDron() {
        return modeloDron;
    }

    public String getTipoDron() {
        return tipoDron;
    }

    public Double getDistanciaSolicitada() {
        return distanciaSolicitada;
    }

    public Double getPesoSolicitado() {
        return pesoSolicitado;
    }

    public Double getHorasSolicitadas() {
        return horasSolicitadas;
    }

    public Double getCostoTotal() {
        return costoTotal;
    }

    public LocalDateTime getFechaCreacion() {
        return fechaCreacion;
    }
}
