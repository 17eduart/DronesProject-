package com.epn.conexion.dronesproject.controlador;

import com.epn.conexion.dronesproject.exception.RecursoNoEncontradoException;
import com.epn.conexion.dronesproject.modelo.FacturaResponse;
import com.epn.conexion.dronesproject.modelo.Pedido;
import com.epn.conexion.dronesproject.modelo.PedidoRequest;
import com.epn.conexion.dronesproject.modelo.PedidoResponse;
import com.epn.conexion.dronesproject.modelo.Usuario;
import com.epn.conexion.dronesproject.seguridad.UsuarioDetailsService;
import com.epn.conexion.dronesproject.servicio.PedidoServicio;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/pedidos")
public class PedidoController {

    private static final String AUTHORITY_ADMIN =
            UsuarioDetailsService.PREFIJO_ROL + Usuario.ROL_ADMINISTRADOR;

    private final PedidoServicio pedidoServicio;

    public PedidoController(PedidoServicio pedidoServicio) {
        this.pedidoServicio = pedidoServicio;
    }

    @PostMapping
    public ResponseEntity<PedidoResponse> crear(Authentication autenticacion,
                                                @RequestBody PedidoRequest datos) {

        Pedido pedido = pedidoServicio.crear(
                autenticacion.getName(),
                datos.getCodigoDron(),
                datos.getDistanciaSolicitada(),
                datos.getPesoSolicitado(),
                datos.getHorasSolicitadas());

        return ResponseEntity.status(HttpStatus.CREATED).body(PedidoResponse.desde(pedido));
    }

    @GetMapping
    public List<PedidoResponse> listar(Authentication autenticacion) {
        List<Pedido> pedidos = esAdministrador(autenticacion)
                ? pedidoServicio.listarTodo()
                : pedidoServicio.listarPorUsuario(autenticacion.getName());

        return pedidos.stream().map(PedidoResponse::desde).toList();
    }

    @GetMapping("/{id}")
    public PedidoResponse buscar(Authentication autenticacion, @PathVariable Long id) {
        return PedidoResponse.desde(obtenerSiTienePermiso(autenticacion, id));
    }

    @GetMapping("/{id}/factura")
    public FacturaResponse factura(Authentication autenticacion, @PathVariable Long id) {
        return FacturaResponse.desde(obtenerSiTienePermiso(autenticacion, id));
    }

    private Pedido obtenerSiTienePermiso(Authentication autenticacion, Long id) {
        Pedido pedido = pedidoServicio.buscarPorId(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("No existe el pedido " + id));

        if (!esAdministrador(autenticacion)
                && !pedido.getUsuario().getUsername().equals(autenticacion.getName())) {
            // AccessDeniedException la recoge ExceptionTranslationFilter y la
            // convierte en 403 con el mismo JSON que el resto de errores.
            throw new AccessDeniedException("El pedido " + id + " pertenece a otro usuario");
        }

        return pedido;
    }

    private boolean esAdministrador(Authentication autenticacion) {
        return autenticacion.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch(AUTHORITY_ADMIN::equals);
    }
}
