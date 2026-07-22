package com.epn.conexion.dronesproject.modelo;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;

@Entity
@DiscriminatorValue("EMERGENCIA")

public class DronEmergencia extends Dron{
    public DronEmergencia(){}

    public DronEmergencia(String codigo, String modelo, Double distancia_km, Double peso_maximo, Double horas_vuelo){
        super(codigo, modelo, distancia_km, peso_maximo, horas_vuelo);
        this.costo_base=15.0;
        if (peso_maximo>15){
            throw new IllegalArgumentException("El peso maximo del paquete es de 15Kg");
        }
        if (horas_vuelo>180){
            throw new IllegalArgumentException("El tiempo de vuelo es de maximo 180 minutos");
        }
    }

    @Override
    public double calcular_costo(){
        return costo_base+(distancia_km*1.00)+(peso_maximo*1.50);
    }

}
