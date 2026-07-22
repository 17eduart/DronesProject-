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
    public double calcularCosto(double distanciaSolicitada, double pesoSolicitado, double horasSolicitadas){
        validarCapacidad(distanciaSolicitada, pesoSolicitado, horasSolicitadas);
        return costo_base+componenteDistancia(distanciaSolicitada)+componentePeso(pesoSolicitado);
    }

    @Override
    public String getTipo(){
        return "LIVIANO";
    }

    @Override
    protected double tarifaPorKm(){
        return 0.50;
    }

    /** El dron liviano no cobra por peso, solo por distancia. */
    @Override
    protected double tarifaPorKg(){
        return 0.0;
    }

}
