package com.epn.conexion.dronesproject.modelo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PedidoRepository extends JpaRepository<Pedido, Long> {
    /** Los pedidos de un cliente, para que solo pueda ver los suyos. */
    List<Pedido> findByUsuarioUsername(String username);
}
