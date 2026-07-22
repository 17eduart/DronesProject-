package com.epn.conexion.dronesproject.controlador;

import com.epn.conexion.dronesproject.modelo.Dron;
import com.epn.conexion.dronesproject.modelo.DronRequest;
import com.epn.conexion.dronesproject.servicio.DronServicio;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/dron")

public class DroneController {
    @Autowired
    private DronServicio dronServicio;

    @GetMapping
    public List<Dron> obtenerDron(){return dronServicio.listarTodo();}

    @GetMapping("/{codigo}")
    public ResponseEntity<Dron> buscarCodigo(@PathVariable String codigo){
        return dronServicio.buscarCodigo(codigo)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/{tipo}")
    public ResponseEntity<Dron> nuevoDron(@PathVariable String tipo, @RequestBody DronRequest datos){
        Dron nuevo = dronServicio.insertar(
                tipo,
                datos.getCodigo(),
                datos.getModelo(),
                datos.getDistancia_km(),
                datos.getPeso_maximo(),
                datos.getHoras_vuelo()
        );
        return ResponseEntity.ok(nuevo);
    }

    @PutMapping("/{codigo}")
    public ResponseEntity<Dron> actualizarDron(@PathVariable String codigo, @RequestBody Dron dron){
        return dronServicio.buscarCodigo(codigo)
                .map(existente ->{
                    dron.setCodigo(codigo);
                    Dron actualizado = dronServicio.actualizar(dron);
                    return ResponseEntity.ok(actualizado);
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{codigo}")
    public ResponseEntity<Void> eliminar(@PathVariable String codigo){
        if (dronServicio.buscarCodigo(codigo).isEmpty()){
            return ResponseEntity.notFound().build();
        }
        dronServicio.eliminar(codigo);
        return ResponseEntity.noContent().build();
    }
}
