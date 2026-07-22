package com.epn.conexion.dronesproject.modelo;

import jakarta.persistence.*;

/**
 * Modelo de dron del catalogo.
 *
 * IMPORTANTE - semantica de los campos: distancia_km, peso_maximo y
 * horas_vuelo NO describen un viaje concreto, describen la CAPACIDAD MAXIMA
 * del modelo. Es decir:
 *
 *   distancia_km  = hasta cuantos km puede volar este modelo
 *   peso_maximo   = cuantos kg como maximo puede cargar
 *   horas_vuelo   = cuantos minutos de autonomia tiene
 *
 * Lo que un cliente pide en un Pedido son valores SOLICITADOS, que se
 * comparan contra estas capacidades y se cobran segun la tarifa del tipo.
 * Por eso calcularCosto recibe los valores solicitados como argumentos en
 * lugar de leer los campos de la entidad.
 */
@Entity
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name="tipo", discriminatorType = DiscriminatorType.STRING)
@Table(name = "dron")

public abstract class Dron {
    @Id
    private String codigo;
    private String modelo;
    protected Double distancia_km;
    protected Double peso_maximo;
    protected Double horas_vuelo;
    protected Double costo_base;


    public Dron() {}

    public Dron(String modelo, Double distancia_km, Double peso_maximo, Double horas_vuelo, Double costo_base) {
        this.modelo = modelo;
        this.distancia_km = distancia_km;
        this.peso_maximo = peso_maximo;
        this.horas_vuelo = horas_vuelo;
        this.costo_base = costo_base;
    }

    public Dron(String codigo, String modelo, Double distancia_km, Double peso_maximo, Double horas_vuelo) {
        this.codigo = codigo;
        this.modelo = modelo;
        this.distancia_km = distancia_km;
        this.peso_maximo = peso_maximo;
        this.horas_vuelo = horas_vuelo;
    }

    /**
     * Costo de un servicio concreto sobre este modelo de dron.
     *
     * @param distanciaSolicitada km que se quieren recorrer
     * @param pesoSolicitado      kg que se quieren transportar
     * @param horasSolicitadas    minutos de vuelo que se necesitan
     * @throws IllegalArgumentException si algun valor supera la capacidad del modelo
     */
    public abstract double calcularCosto(double distanciaSolicitada,
                                         double pesoSolicitado,
                                         double horasSolicitadas);

    /** Identificador del tipo, igual al @DiscriminatorValue de la subclase. */
    public abstract String getTipo();

    /**
     * Cuanto cobra este tipo por km. Se expone aparte de calcularCosto para
     * poder desglosar la factura sin duplicar las tarifas.
     */
    protected abstract double tarifaPorKm();

    /** Cuanto cobra este tipo por kg. Cero en los tipos que no cobran por peso. */
    protected abstract double tarifaPorKg();

    public double componenteDistancia(double distanciaSolicitada) {
        return distanciaSolicitada * tarifaPorKm();
    }

    public double componentePeso(double pesoSolicitado) {
        return pesoSolicitado * tarifaPorKg();
    }

    /**
     * Comprueba que lo solicitado cabe dentro de la capacidad del modelo.
     *
     * Vive aqui y no repetida en cada subclase porque la regla es la misma
     * para las tres; lo que cambia entre tipos son los limites, que ya son
     * datos de la entidad.
     */
    protected void validarCapacidad(double distanciaSolicitada,
                                    double pesoSolicitado,
                                    double horasSolicitadas) {
        if (distanciaSolicitada > distancia_km) {
            throw new IllegalArgumentException(
                    "La distancia solicitada (" + distanciaSolicitada + " km) supera la capacidad del modelo "
                            + modelo + ", cuyo maximo es " + distancia_km + " km");
        }
        if (pesoSolicitado > peso_maximo) {
            throw new IllegalArgumentException(
                    "El peso solicitado (" + pesoSolicitado + " kg) supera la capacidad del modelo "
                            + modelo + ", cuyo maximo es " + peso_maximo + " kg");
        }
        if (horasSolicitadas > horas_vuelo) {
            throw new IllegalArgumentException(
                    "El tiempo de vuelo solicitado (" + horasSolicitadas + " minutos) supera la autonomia del modelo "
                            + modelo + ", cuyo maximo es " + horas_vuelo + " minutos");
        }
    }

    public String getCodigo() {
        return codigo;
    }

    public void setCodigo(String codigo) {
        this.codigo = codigo;
    }

    public String getModelo() {
        return modelo;
    }

    public void setModelo(String modelo) {
        this.modelo = modelo;
    }

    public Double getDistancia_km() {
        return distancia_km;
    }

    public void setDistancia_km(Double distancia_km) {
        this.distancia_km = distancia_km;
    }

    public Double getPeso_maximo() {
        return peso_maximo;
    }

    public void setPeso_maximo(Double peso_maximo) {
        this.peso_maximo = peso_maximo;
    }

    public Double getHoras_vuelo() {
        return horas_vuelo;
    }

    public void setHoras_vuelo(Double horas_vuelo) {
        this.horas_vuelo = horas_vuelo;
    }

    public Double getCosto_base() {
        return costo_base;
    }

    public void setCosto_base(Double costo_base) {
        this.costo_base = costo_base;
    }

    @Override
    public String toString() {
        return "Dron{" +
                "codigo='" + codigo + '\'' +
                ", modelo='" + modelo + '\'' +
                ", distancia_km=" + distancia_km +
                ", peso_maximo=" + peso_maximo +
                ", horas_vuelo=" + horas_vuelo +
                ", costo_base=" + costo_base +
                '}';
    }
}

