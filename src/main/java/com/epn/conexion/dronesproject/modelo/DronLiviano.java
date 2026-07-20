package com.epn.conexion.dronesproject.modelo;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;

@Entity
@DiscriminatorValue("LIVIANO")

public class DronLiviano extends Dron{
    public DronLiviano(){}

    public DronLiviano(String codigo, String modelo, Double distancia_km, Double peso_maximo, Double horas_vuelo){
        super(codigo, modelo, distancia_km, peso_maximo, horas_vuelo);
        this.costo_base=0.50;
        if (peso_maximo>5){
            throw new IllegalArgumentException("El peso maximo del paquete es de 5Kg");
        }
        if (horas_vuelo>120){
            throw new IllegalArgumentException("El tiempo de vuelo es de maximo 120 minutos");
        }
    }

    @Override
    public double calcular_costo(){
        return costo_base+(distancia_km*0.50);
    }

}
