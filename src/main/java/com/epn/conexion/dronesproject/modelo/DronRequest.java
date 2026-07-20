package com.epn.conexion.dronesproject.modelo;

public class DronRequest {
    private String codigo;
    private String modelo;
    private Double distancia_km;
    private Double peso_maximo;
    private Double horas_vuelo;

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
}
