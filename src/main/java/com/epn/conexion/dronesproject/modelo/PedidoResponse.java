package com.epn.conexion.dronesproject.modelo;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDateTime;

/**
 * Lo que se devuelve al cliente tras crear o consultar un pedido.
 *
 * Dos constructores: el privado la arma desde la entidad (servidor) y el
 * @JsonCreator la reconstruye desde JSON (clientes). Sin setters: se construye
 * entera o no se construye.
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

    /** Constructor para deserializar en el cliente. */
    @JsonCreator
    public PedidoResponse(@JsonProperty("id") Long id,
                          @JsonProperty("username") String username,
                          @JsonProperty("codigoDron") String codigoDron,
                          @JsonProperty("modeloDron") String modeloDron,
                          @JsonProperty("tipoDron") String tipoDron,
                          @JsonProperty("distanciaSolicitada") Double distanciaSolicitada,
                          @JsonProperty("pesoSolicitado") Double pesoSolicitado,
                          @JsonProperty("horasSolicitadas") Double horasSolicitadas,
                          @JsonProperty("costoTotal") Double costoTotal,
                          @JsonProperty("fechaCreacion") LocalDateTime fechaCreacion) {
        this.id = id;
        this.username = username;
        this.codigoDron = codigoDron;
        this.modeloDron = modeloDron;
        this.tipoDron = tipoDron;
        this.distanciaSolicitada = distanciaSolicitada;
        this.pesoSolicitado = pesoSolicitado;
        this.horasSolicitadas = horasSolicitadas;
        this.costoTotal = costoTotal;
        this.fechaCreacion = fechaCreacion;
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
