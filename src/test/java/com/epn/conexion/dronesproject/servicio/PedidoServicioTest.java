package com.epn.conexion.dronesproject.servicio;

import com.epn.conexion.dronesproject.exception.RecursoNoEncontradoException;
import com.epn.conexion.dronesproject.modelo.Dron;
import com.epn.conexion.dronesproject.modelo.DronCarga;
import com.epn.conexion.dronesproject.modelo.DronEmergencia;
import com.epn.conexion.dronesproject.modelo.DronLiviano;
import com.epn.conexion.dronesproject.modelo.DronRepository;
import com.epn.conexion.dronesproject.modelo.Pedido;
import com.epn.conexion.dronesproject.modelo.PedidoRepository;
import com.epn.conexion.dronesproject.modelo.Usuario;
import com.epn.conexion.dronesproject.modelo.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Pruebas de PedidoServicio con los repositorios simulados: sin Spring y sin
 * MySQL, pero con drones reales, de modo que el costo lo calcula de verdad
 * cada subclase.
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class PedidoServicioTest {

    @Mock
    private PedidoRepository pedidoRepository;
    @Mock
    private UsuarioRepository usuarioRepository;
    @Mock
    private DronRepository dronRepository;

    @InjectMocks
    private PedidoServicio pedidoServicio;

    private Usuario cliente;

    @BeforeEach
    void prepararUsuario() {
        cliente = new Usuario("ana", "hash-simulado", Usuario.ROL_CLIENTE);
        when(usuarioRepository.findByUsername("ana")).thenReturn(Optional.of(cliente));
        when(pedidoRepository.save(any(Pedido.class)))
                .thenAnswer(invocacion -> invocacion.getArgument(0));
    }

    private void catalogoCon(Dron dron) {
        when(dronRepository.findById(dron.getCodigo())).thenReturn(Optional.of(dron));
    }

    @Test
    @DisplayName("El costo guardado coincide con calcularCosto del DronLiviano")
    void costo_de_dron_liviano() {
        Dron dron = new DronLiviano("LV01", "Maverick", 100.0, 5.0, 120.0);
        catalogoCon(dron);

        Pedido pedido = pedidoServicio.crear("ana", "LV01", 20.0, 4.0, 100.0);

        assertEquals(dron.calcularCosto(20.0, 4.0, 100.0), pedido.getCostoTotal(), 0.0001);
        assertEquals(10.50, pedido.getCostoTotal(), 0.0001);
    }

    @Test
    @DisplayName("El costo guardado coincide con calcularCosto del DronCarga")
    void costo_de_dron_carga() {
        Dron dron = new DronCarga("CG01", "Titan", 100.0, 29.0, 300.0);
        catalogoCon(dron);

        Pedido pedido = pedidoServicio.crear("ana", "CG01", 20.0, 8.0, 100.0);

        assertEquals(dron.calcularCosto(20.0, 8.0, 100.0), pedido.getCostoTotal(), 0.0001);
        assertEquals(29.60, pedido.getCostoTotal(), 0.0001);
    }

    @Test
    @DisplayName("El costo guardado coincide con calcularCosto del DronEmergencia")
    void costo_de_dron_emergencia() {
        Dron dron = new DronEmergencia("EM01", "Rescue", 100.0, 15.0, 180.0);
        catalogoCon(dron);

        Pedido pedido = pedidoServicio.crear("ana", "EM01", 20.0, 8.0, 100.0);

        assertEquals(dron.calcularCosto(20.0, 8.0, 100.0), pedido.getCostoTotal(), 0.0001);
        assertEquals(47.00, pedido.getCostoTotal(), 0.0001);
    }

    @Test
    @DisplayName("El servidor asigna usuario y fecha; no vienen del cliente")
    void el_servidor_asigna_usuario_y_fecha() {
        catalogoCon(new DronLiviano("LV01", "Maverick", 100.0, 5.0, 120.0));
        LocalDateTime antes = LocalDateTime.now();

        pedidoServicio.crear("ana", "LV01", 10.0, 1.0, 50.0);

        ArgumentCaptor<Pedido> guardado = ArgumentCaptor.forClass(Pedido.class);
        verify(pedidoRepository).save(guardado.capture());

        assertEquals(cliente, guardado.getValue().getUsuario());
        assertNotNull(guardado.getValue().getFechaCreacion());
        assertTrue(!guardado.getValue().getFechaCreacion().isBefore(antes));
    }

    @Test
    @DisplayName("Exceder la capacidad lanza IllegalArgumentException y NO persiste nada")
    void capacidad_excedida_no_persiste() {
        catalogoCon(new DronLiviano("LV01", "Maverick", 30.0, 5.0, 120.0));

        IllegalArgumentException e = assertThrows(IllegalArgumentException.class,
                () -> pedidoServicio.crear("ana", "LV01", 500.0, 1.0, 10.0));

        assertTrue(e.getMessage().contains("distancia"), e.getMessage());

        // Lo importante: no queda un pedido a medias en la base.
        verify(pedidoRepository, never()).save(any());
    }

    @Test
    @DisplayName("Un dron inexistente lanza RecursoNoEncontradoException y NO persiste nada")
    void dron_inexistente_no_persiste() {
        when(dronRepository.findById("NO-EXISTE")).thenReturn(Optional.empty());

        assertThrows(RecursoNoEncontradoException.class,
                () -> pedidoServicio.crear("ana", "NO-EXISTE", 10.0, 1.0, 10.0));

        verify(pedidoRepository, never()).save(any());
    }
}
