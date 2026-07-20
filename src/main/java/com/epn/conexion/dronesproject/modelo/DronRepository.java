package com.epn.conexion.dronesproject.modelo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DronRepository extends JpaRepository<Dron, String> {
}
