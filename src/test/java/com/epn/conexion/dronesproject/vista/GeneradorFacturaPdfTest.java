package com.epn.conexion.dronesproject.vista;

import com.epn.conexion.dronesproject.modelo.Dron;
import com.epn.conexion.dronesproject.modelo.DronCarga;
import com.epn.conexion.dronesproject.modelo.FacturaResponse;
import com.epn.conexion.dronesproject.modelo.Pedido;
import com.epn.conexion.dronesproject.modelo.PedidoResponse;
import com.epn.conexion.dronesproject.modelo.Usuario;
import com.lowagie.text.pdf.PdfReader;
import com.lowagie.text.pdf.parser.PdfTextExtractor;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Verifica que el PDF de la factura se genera de verdad.
 *
 * Es la unica forma de comprobarlo sin abrir una ventana: GeneradorFacturaPdf
 * no depende de JavaFX, asi que se puede invocar directamente desde el build.
 * Lo que no se puede probar aqui es el FileChooser ni el Alert, que si
 * necesitan interfaz grafica.
 */
class GeneradorFacturaPdfTest {

    /** Los mismos datos que produciria un pedido real de 20 km / 8 kg. */
    private Pedido pedidoDePrueba() {
        Usuario ana = new Usuario("ana", "hash-simulado", Usuario.ROL_CLIENTE);
        Dron dron = new DronCarga("CG01", "Titan", 100.0, 29.0, 300.0);

        Pedido pedido = new Pedido(ana, dron, 20.0, 8.0, 100.0,
                dron.calcularCosto(20.0, 8.0, 100.0),
                LocalDateTime.of(2026, 7, 22, 10, 15, 30));
        pedido.setId(7L);
        return pedido;
    }

    @Test
    @DisplayName("Genera un archivo PDF que existe y no esta vacio")
    void genera_un_archivo_no_vacio(@TempDir Path carpeta) throws IOException {
        Pedido pedido = pedidoDePrueba();
        File destino = carpeta.resolve("Factura-FAC-000007.pdf").toFile();

        GeneradorFacturaPdf.generar(FacturaResponse.desde(pedido), PedidoResponse.desde(pedido), destino);

        assertTrue(destino.exists(), "el archivo no se creo");
        assertTrue(destino.length() > 0, "el archivo quedo vacio");
    }

    @Test
    @DisplayName("El archivo generado es realmente un PDF (cabecera %PDF-)")
    void el_archivo_es_un_pdf_valido(@TempDir Path carpeta) throws IOException {
        Pedido pedido = pedidoDePrueba();
        File destino = carpeta.resolve("factura.pdf").toFile();

        GeneradorFacturaPdf.generar(FacturaResponse.desde(pedido), PedidoResponse.desde(pedido), destino);

        byte[] contenido = Files.readAllBytes(destino.toPath());

        // Un PDF valido empieza por "%PDF-" y termina por "%%EOF"; comprobar
        // solo el tamaño dejaria pasar un archivo truncado o a medio escribir.
        String cabecera = new String(contenido, 0, 5, StandardCharsets.ISO_8859_1);
        assertEquals("%PDF-", cabecera, "no tiene cabecera de PDF");

        String cola = new String(contenido, Math.max(0, contenido.length - 10),
                Math.min(10, contenido.length), StandardCharsets.ISO_8859_1);
        assertTrue(cola.contains("%%EOF"), "el PDF quedo sin cerrar: " + cola);
    }

    @Test
    @DisplayName("El PDF lleva los datos de la factura, no una plantilla vacia")
    void el_pdf_contiene_los_datos() throws IOException {
        Pedido pedido = pedidoDePrueba();
        ByteArrayOutputStream salida = new ByteArrayOutputStream();

        GeneradorFacturaPdf.generar(FacturaResponse.desde(pedido), PedidoResponse.desde(pedido), salida);

        // OpenPDF comprime el flujo de contenido, asi que buscar los literales
        // en los bytes crudos no sirve: hay que leer el PDF como PDF.
        PdfReader lector = new PdfReader(salida.toByteArray());
        String texto;
        try {
            assertEquals(1, lector.getNumberOfPages());
            texto = new PdfTextExtractor(lector).getTextFromPage(1);
        } finally {
            lector.close();
        }

        assertTrue(texto.contains("FAC-000007"), "falta el numero de factura: " + texto);
        assertTrue(texto.contains("ana"), "falta el cliente: " + texto);
        assertTrue(texto.contains("Titan"), "falta el modelo del dron: " + texto);
        assertTrue(texto.contains("20.00 km"), "falta la distancia: " + texto);
        assertTrue(texto.contains("8.00 kg"), "falta el peso: " + texto);
        assertTrue(texto.contains("29.60"), "falta el total: " + texto);
    }

    @Test
    @DisplayName("El desglose impreso suma el total")
    void el_desglose_impreso_cuadra() throws IOException {
        Pedido pedido = pedidoDePrueba();
        FacturaResponse factura = FacturaResponse.desde(pedido);
        ByteArrayOutputStream salida = new ByteArrayOutputStream();

        GeneradorFacturaPdf.generar(factura, PedidoResponse.desde(pedido), salida);

        PdfReader lector = new PdfReader(salida.toByteArray());
        String texto;
        try {
            texto = new PdfTextExtractor(lector).getTextFromPage(1);
        } finally {
            lector.close();
        }

        // 6.00 + 14.00 + 9.60 = 29.60: los tres componentes y el total estan
        // impresos, de modo que la factura se puede verificar a mano.
        assertTrue(texto.contains("6.00"), "falta el costo base: " + texto);
        assertTrue(texto.contains("14.00"), "falta el costo por distancia: " + texto);
        assertTrue(texto.contains("9.60"), "falta el costo por peso: " + texto);
        assertEquals(factura.getTotal(),
                factura.getCostoBase() + factura.getComponenteDistancia() + factura.getComponentePeso(),
                0.0001);
    }

    @Test
    @DisplayName("El nombre sugerido usa el numero de factura")
    void nombre_sugerido() {
        FacturaResponse factura = FacturaResponse.desde(pedidoDePrueba());

        assertEquals("Factura-FAC-000007.pdf", GeneradorFacturaPdf.nombreSugerido(factura));
    }

    @Test
    @DisplayName("Si no se puede escribir, lanza IOException en vez de dejar el archivo a medias")
    void ruta_invalida_lanza_io_exception(@TempDir Path carpeta) {
        Pedido pedido = pedidoDePrueba();
        // Una carpeta no se puede abrir como archivo de salida.
        File destinoInvalido = carpeta.toFile();

        assertThrows(IOException.class, () -> GeneradorFacturaPdf.generar(
                FacturaResponse.desde(pedido), PedidoResponse.desde(pedido), destinoInvalido));
    }
}
