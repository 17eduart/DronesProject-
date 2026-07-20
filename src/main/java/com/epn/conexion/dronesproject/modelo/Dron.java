package com.epn.conexion.dronesproject.modelo;

import jakarta.persistence.*;

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

    public abstract double calcular_costo();

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

