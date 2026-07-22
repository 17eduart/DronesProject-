package com.epn.conexion.dronesproject.modelo;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.time.LocalDateTime;

/**
 * Servicio contratado por un usuario sobre un dron del catalogo.
 *
 * costoTotal y fechaCreacion los asigna el servidor, nunca el cliente. Se
 * persisten para que la factura refleje lo que se cobro en su momento aunque
 * despues cambien las tarifas del modelo.
 */
@Entity
@Table(name = "pedido")
public class Pedido {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    @ManyToOne(optional = false)
    @JoinColumn(name = "dron_codigo", nullable = false)
    private Dron dron;

    @Column(nullable = false)
    private Double distanciaSolicitada;

    @Column(nullable = false)
    private Double pesoSolicitado;

    @Column(nullable = false)
    private Double horasSolicitadas;

    @Column(nullable = false)
    private Double costoTotal;

    @Column(nullable = false)
    private LocalDateTime fechaCreacion;

    public Pedido() {}

    public Pedido(Usuario usuario, Dron dron, Double distanciaSolicitada, Double pesoSolicitado,
                  Double horasSolicitadas, Double costoTotal, LocalDateTime fechaCreacion) {
        this.usuario = usuario;
        this.dron = dron;
        this.distanciaSolicitada = distanciaSolicitada;
        this.pesoSolicitado = pesoSolicitado;
        this.horasSolicitadas = horasSolicitadas;
        this.costoTotal = costoTotal;
        this.fechaCreacion = fechaCreacion;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Usuario getUsuario() {
        return usuario;
    }

    public void setUsuario(Usuario usuario) {
        this.usuario = usuario;
    }

    public Dron getDron() {
        return dron;
    }

    public void setDron(Dron dron) {
        this.dron = dron;
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

    public Double getCostoTotal() {
        return costoTotal;
    }

    public void setCostoTotal(Double costoTotal) {
        this.costoTotal = costoTotal;
    }

    public LocalDateTime getFechaCreacion() {
        return fechaCreacion;
    }

    public void setFechaCreacion(LocalDateTime fechaCreacion) {
        this.fechaCreacion = fechaCreacion;
    }
}
