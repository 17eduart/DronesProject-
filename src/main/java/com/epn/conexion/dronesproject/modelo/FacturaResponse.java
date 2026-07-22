package com.epn.conexion.dronesproject.modelo;

import java.time.LocalDateTime;

/**
 * Factura de un pedido, con el costo desglosado en sus tres componentes.
 *
 * El total sale del costoTotal guardado en el pedido, no de recalcularlo: es
 * lo que efectivamente se cobro. Los componentes si se recalculan a partir de
 * las tarifas del dron, asi que si algun dia cambian las tarifas de un modelo,
 * las facturas viejas mantendrian el total correcto pero el desglose dejaria
 * de sumar. Cuando eso importe, habra que persistir tambien los componentes.
 */
public class FacturaResponse {
    private final String numeroFactura;
    private final LocalDateTime fechaEmision;
    private final String nombreCliente;
    private final String detalleDron;
    private final Double costoBase;
    private final Double componenteDistancia;
    private final Double componentePeso;
    private final Double total;

    private FacturaResponse(Pedido pedido) {
        Dron dron = pedido.getDron();

        this.numeroFactura = String.format("FAC-%06d", pedido.getId());
        this.fechaEmision = pedido.getFechaCreacion();
        this.nombreCliente = pedido.getUsuario().getUsername();
        this.detalleDron = dron.getModelo() + " (" + dron.getTipo() + ")";
        this.costoBase = dron.getCosto_base();
        this.componenteDistancia = dron.componenteDistancia(pedido.getDistanciaSolicitada());
        this.componentePeso = dron.componentePeso(pedido.getPesoSolicitado());
        this.total = pedido.getCostoTotal();
    }

    public static FacturaResponse desde(Pedido pedido) {
        return new FacturaResponse(pedido);
    }

    public String getNumeroFactura() {
        return numeroFactura;
    }

    public LocalDateTime getFechaEmision() {
        return fechaEmision;
    }

    public String getNombreCliente() {
        return nombreCliente;
    }

    public String getDetalleDron() {
        return detalleDron;
    }

    public Double getCostoBase() {
        return costoBase;
    }

    public Double getComponenteDistancia() {
        return componenteDistancia;
    }

    public Double getComponentePeso() {
        return componentePeso;
    }

    public Double getTotal() {
        return total;
    }
}
