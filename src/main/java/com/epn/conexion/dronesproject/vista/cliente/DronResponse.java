package com.epn.conexion.dronesproject.vista.cliente;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Vista de cliente de un dron del catalogo (GET /dron).
 *
 * Existe porque la entidad Dron NO se puede deserializar aqui: es abstracta y
 * no declara @JsonTypeInfo, asi que Jackson no tiene forma de elegir entre
 * DronLiviano, DronCarga y DronEmergencia.
 *
 * Los nombres con guion bajo son los que emite el servidor (Jackson los deriva
 * de getDistancia_km(), getPeso_maximo(), etc.), por eso van explicitos en
 * @JsonProperty en lugar de renombrar los campos de la entidad.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class DronResponse {

    private final String codigo;
    private final String modelo;
    private final String tipo;
    private final Double distanciaKm;
    private final Double pesoMaximo;
    private final Double horasVuelo;
    private final Double costoBase;

    public DronResponse(@JsonProperty("codigo") String codigo,
                        @JsonProperty("modelo") String modelo,
                        @JsonProperty("tipo") String tipo,
                        @JsonProperty("distancia_km") Double distanciaKm,
                        @JsonProperty("peso_maximo") Double pesoMaximo,
                        @JsonProperty("horas_vuelo") Double horasVuelo,
                        @JsonProperty("costo_base") Double costoBase) {
        this.codigo = codigo;
        this.modelo = modelo;
        this.tipo = tipo;
        this.distanciaKm = distanciaKm;
        this.pesoMaximo = pesoMaximo;
        this.horasVuelo = horasVuelo;
        this.costoBase = costoBase;
    }

    public String getCodigo() {
        return codigo;
    }

    public String getModelo() {
        return modelo;
    }

    public String getTipo() {
        return tipo;
    }

    public Double getDistanciaKm() {
        return distanciaKm;
    }

    public Double getPesoMaximo() {
        return pesoMaximo;
    }

    public Double getHorasVuelo() {
        return horasVuelo;
    }

    public Double getCostoBase() {
        return costoBase;
    }

    /**
     * Lo que se ve en el ComboBox de modelos. Incluye las capacidades para que
     * el usuario sepa cuanto puede pedir antes de que el backend lo rechace.
     */
    @Override
    public String toString() {
        return modelo + " (" + codigo + ") - hasta " + distanciaKm + " km, "
                + pesoMaximo + " kg, " + horasVuelo + " min";
    }
}
