package com.epn.conexion.dronesproject.modelo;

import jakarta.persistence.DiscriminatorColumn;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;

@Entity
@DiscriminatorValue("CARGA")

public class DronCarga extends Dron{
    public DronCarga(){}

    public DronCarga(String codigo,String modelo, Double distancia_km, Double peso_maximo, Double horas_vuelo){
        super(codigo, modelo, distancia_km, peso_maximo, horas_vuelo);
        this.costo_base=6.0;
        if (peso_maximo>=30){
            throw new IllegalArgumentException("El peso del dron no debe pasar los 30 kg");
        }
        if (horas_vuelo>300){
            throw new IllegalArgumentException("Los minutos de vuelo del dron no deben pasar los 300 minutos");
        }
    }

    @Override
    public double calcularCosto(double distanciaSolicitada, double pesoSolicitado, double horasSolicitadas){
        validarCapacidad(distanciaSolicitada, pesoSolicitado, horasSolicitadas);
        return costo_base+componenteDistancia(distanciaSolicitada)+componentePeso(pesoSolicitado);
    }

    @Override
    public String getTipo(){
        return "CARGA";
    }

    @Override
    protected double tarifaPorKm(){
        return 0.70;
    }

    @Override
    protected double tarifaPorKg(){
        return 1.20;
    }

}
