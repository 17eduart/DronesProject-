package com.epn.conexion.dronesproject.servicio;

import com.epn.conexion.dronesproject.modelo.Dron;
import com.epn.conexion.dronesproject.modelo.DronCarga;
import com.epn.conexion.dronesproject.modelo.DronEmergencia;
import com.epn.conexion.dronesproject.modelo.DronLiviano;
import com.epn.conexion.dronesproject.modelo.DronRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class DronServicio {
    @Autowired
    private DronRepository dronRepository;

    public List<Dron> listarTodo(){
        return dronRepository.findAll();
    }

    public Optional<Dron> buscarCodigo(String codigo){
        return dronRepository.findById(codigo);
    }

    public Dron insertar(String tipo, String codigo, String modelo, Double distancia_km, Double peso_maximo, Double horas_vuelo){
        Dron dron = switch (tipo.toUpperCase()){
            case "LIVIANO" -> new DronLiviano(codigo, modelo, distancia_km, peso_maximo, horas_vuelo);
            case "CARGA" -> new DronCarga(codigo, modelo, distancia_km, peso_maximo, horas_vuelo);
            case "EMERGENCIA" -> new DronEmergencia(codigo, modelo, distancia_km, peso_maximo, horas_vuelo);
            default -> throw new IllegalArgumentException("El Tipo de Dron no existe");
        };
        return dronRepository.save(dron);
    }

    public Dron insertar(Dron dron){
        return dronRepository.save(dron);
    }

    public Dron actualizar(String tipo, String codigo, String modelo, Double distancia_km, Double peso_maximo, Double horas_vuelo){
        Dron dron = switch (tipo.toUpperCase()){
            case "LIVIANO" -> new DronLiviano(codigo, modelo, distancia_km, peso_maximo, horas_vuelo);
            case "CARGA" -> new DronCarga(codigo, modelo, distancia_km, peso_maximo, horas_vuelo);
            case "EMERGENCIA" -> new DronEmergencia(codigo, modelo, distancia_km, peso_maximo, horas_vuelo);
            default -> throw new IllegalArgumentException("El Tipo de Dron no existe");
        };
        return dronRepository.save(dron);
    }

    public Dron actualizar(Dron dron){
        return dronRepository.save(dron);
    }

    public void eliminar(String codigo){
        dronRepository.deleteById(codigo);
    }


}
