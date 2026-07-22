package com.epn.conexion.dronesproject.servicio;

import com.epn.conexion.dronesproject.exception.RecursoNoEncontradoException;
import com.epn.conexion.dronesproject.modelo.Dron;
import com.epn.conexion.dronesproject.modelo.DronRepository;
import com.epn.conexion.dronesproject.modelo.Pedido;
import com.epn.conexion.dronesproject.modelo.PedidoRepository;
import com.epn.conexion.dronesproject.modelo.Usuario;
import com.epn.conexion.dronesproject.modelo.UsuarioRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Logica de negocio de los pedidos: calcula el costo con el dron elegido y
 * persiste el resultado. El costo y la fecha los fija el servidor, nunca el
 * cliente.
 */
@Service
public class PedidoServicio {

    private final PedidoRepository pedidoRepository;
    private final UsuarioRepository usuarioRepository;
    private final DronRepository dronRepository;

    public PedidoServicio(PedidoRepository pedidoRepository,
                          UsuarioRepository usuarioRepository,
                          DronRepository dronRepository) {
        this.pedidoRepository = pedidoRepository;
        this.usuarioRepository = usuarioRepository;
        this.dronRepository = dronRepository;
    }

    /**
     * Crea un pedido para el usuario indicado.
     *
     * @param username el del token autenticado, nunca uno que venga en el body
     * @throws IllegalArgumentException si lo solicitado excede la capacidad del dron
     * @throws RecursoNoEncontradoException si el dron o el usuario no existen
     */
    public Pedido crear(String username, String codigoDron, Double distanciaSolicitada,
                        Double pesoSolicitado, Double horasSolicitadas) {

        Usuario usuario = usuarioRepository.findByUsername(username)
                .orElseThrow(() -> new RecursoNoEncontradoException("No existe el usuario " + username));

        Dron dron = dronRepository.findById(codigoDron)
                .orElseThrow(() -> new RecursoNoEncontradoException("No existe el dron " + codigoDron));

        // Punto donde actua el polimorfismo: sobre una referencia Dron, cada
        // subclase valida su capacidad y aplica su tarifa. Si algo excede el
        // maximo la excepcion sale de aqui y no se guarda ningun pedido.
        double costo = dron.calcularCosto(distanciaSolicitada, pesoSolicitado, horasSolicitadas);

        Pedido pedido = new Pedido(usuario, dron, distanciaSolicitada, pesoSolicitado,
                horasSolicitadas, costo, LocalDateTime.now());

        return pedidoRepository.save(pedido);
    }

    public List<Pedido> listarPorUsuario(String username) {
        return pedidoRepository.findByUsuarioUsername(username);
    }

    /** Catalogo completo de pedidos: solo tiene sentido para un ADMINISTRADOR. */
    public List<Pedido> listarTodo() {
        return pedidoRepository.findAll();
    }

    public Optional<Pedido> buscarPorId(Long id) {
        return pedidoRepository.findById(id);
    }
}
