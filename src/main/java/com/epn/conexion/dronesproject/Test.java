package com.epn.conexion.dronesproject;

import com.epn.conexion.dronesproject.servicio.UsuarioServicio;
import com.epn.conexion.dronesproject.modelo.Dron;
import com.epn.conexion.dronesproject.servicio.DronServicio;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication

public class Test implements CommandLineRunner {

    @Autowired
    private DronServicio dronServicio;

    @Autowired
    private UsuarioServicio usuarioServicio;

    public static void main(String[] args) {
        SpringApplication.run(Test.class, args);
    }


    @Override
    public void run(String... args) throws Exception {

        System.out.println("\n========== PRUEBA CRUD DRONES ==========");

        dronServicio.insertar("LIVIANO", "LV01", "Maverick", 10.0, 3.0, 110.0);
        dronServicio.insertar("CARGA",   "CG01", "Titan",    20.0, 8.0, 200.0);

        System.out.println("\n--- Listado completo ---");
        dronServicio.listarTodo().forEach(System.out::println);

        // 3) BUSCAR POR CÓDIGO (Optional)
        System.out.println("\n--- Buscar LV01 ---");
        dronServicio.buscarCodigo("LV01")
                 .ifPresentOrElse(System.out::println,
                        () -> System.out.println("No encontrado"));

        // 4) ACTUALIZAR
        System.out.println("\n--- Actualizar LV01 ---");
        dronServicio.actualizar("LIVIANO", "LV01", "Maverick-Editado", 12.0, 3.5, 100.0);
        dronServicio.buscarCodigo("LV01").ifPresent(System.out::println);

        // 5) POLIMORFISMO: calcular_costo() sobre referencia Dron
        System.out.println("\n--- Costos (polimorfismo) ---");
        for (Dron d : dronServicio.listarTodo()) {
            System.out.println(d.getModelo()
                    + " (" + d.getClass().getSimpleName() + ")"
                    + " -> costo: " + d.calcular_costo());
        }

        // 6) ELIMINAR
        System.out.println("\n--- Eliminar CG01 ---");
        dronServicio.eliminar("CG01");
        System.out.println("¿Existe CG01? " + dronServicio.buscarCodigo("CG01").isPresent());

        // 7) CONTROL DE ERRORES (try/catch, ver nota abajo)
        System.out.println("\n--- Casos de error esperados ---");
        try {
            dronServicio.insertar("VOLADOR", "X1", "x", 1.0, 1.0, 1.0);   // tipo inválido
        } catch (IllegalArgumentException e) {
            System.out.println("OK, error capturado: " + e.getMessage());
        }
        try {
            dronServicio.insertar("LIVIANO", "X2", "x", 1.0, 10.0, 1.0);  // peso > 5
        } catch (IllegalArgumentException e) {
            System.out.println("OK, error capturado: " + e.getMessage());
        }

        System.out.println("\n========== PRUEBA USUARIOS ==========");
        // (requiere @Autowired UsuarioServicio en esta clase)

        // 8) REGISTRO
        try {
            usuarioServicio.registrar("eduart", "prueba1", "ADMIN");
            System.out.println("Usuario registrado");
        } catch (IllegalArgumentException e) {
            System.out.println("Ya existía: " + e.getMessage());
        }

        // 9) LOGIN correcto e incorrecto
        System.out.println("Login correcto:   " + usuarioServicio.login("eduart", "prueba1")); // true
        System.out.println("Login incorrecto: " + usuarioServicio.login("eduart", "malo"));    // false
    }}
