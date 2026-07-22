package com.epn.conexion.dronesproject.vista;

import com.epn.conexion.dronesproject.modelo.FacturaResponse;
import com.epn.conexion.dronesproject.modelo.PedidoResponse;
import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

/**
 * Escribe la factura de un pedido en un PDF.
 *
 * Esta clase no sabe nada de JavaFX a proposito: no abre dialogos ni muestra
 * alertas, solo recibe datos y escribe bytes. Eso permite probarla en el build
 * sin abrir una ventana, que es la unica forma de verificar que el PDF sale
 * bien. La parte de interfaz (elegir carpeta, avisar al usuario) vive en
 * FacturaController.
 */
public final class GeneradorFacturaPdf {

    private static final DateTimeFormatter FORMATO_FECHA =
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private static final Font FUENTE_TITULO = FontFactory.getFont(FontFactory.HELVETICA, 20f, Font.BOLD);
    private static final Font FUENTE_SUBTITULO = FontFactory.getFont(FontFactory.HELVETICA, 12f, Font.BOLD);
    private static final Font FUENTE_NORMAL = FontFactory.getFont(FontFactory.HELVETICA, 11f, Font.NORMAL);
    private static final Font FUENTE_TOTAL = FontFactory.getFont(FontFactory.HELVETICA, 13f, Font.BOLD);

    private GeneradorFacturaPdf() {}

    /** Nombre sugerido del archivo para el dialogo de guardado. */
    public static String nombreSugerido(FacturaResponse factura) {
        return "Factura-" + factura.getNumeroFactura() + ".pdf";
    }

    /**
     * Genera el PDF en el archivo indicado.
     *
     * @throws IOException si no se puede escribir (carpeta protegida, disco
     *                     lleno, archivo abierto en otro programa...)
     */
    public static void generar(FacturaResponse factura, PedidoResponse pedido, File destino)
            throws IOException {
        try (OutputStream salida = new FileOutputStream(destino)) {
            generar(factura, pedido, salida);
        }
    }

    /** Variante sobre un stream; es la que usan los tests. */
    public static void generar(FacturaResponse factura, PedidoResponse pedido, OutputStream salida)
            throws IOException {
        Document documento = new Document(PageSize.A4);
        try {
            PdfWriter.getInstance(documento, salida);
            documento.open();

            documento.add(titulo());
            documento.add(cabecera(factura));
            documento.add(detalle(factura, pedido));
            documento.add(pie());
        } catch (DocumentException e) {
            // DocumentException no es de E/S, pero para quien llama el efecto
            // es el mismo: no se pudo producir el archivo.
            throw new IOException("No se pudo generar el contenido del PDF: " + e.getMessage(), e);
        } finally {
            if (documento.isOpen()) {
                documento.close();
            }
        }
    }

    private static Element titulo() {
        Paragraph titulo = new Paragraph("FACTURA", FUENTE_TITULO);
        titulo.setAlignment(Element.ALIGN_CENTER);
        titulo.setSpacingAfter(4f);
        return titulo;
    }

    private static Element cabecera(FacturaResponse factura) {
        Paragraph cabecera = new Paragraph();
        cabecera.setAlignment(Element.ALIGN_CENTER);
        cabecera.add(new Phrase("Home-Drones - Escuela Politecnica Nacional\n", FUENTE_SUBTITULO));
        cabecera.add(new Phrase("N.o " + factura.getNumeroFactura() + "\n", FUENTE_NORMAL));
        if (factura.getFechaEmision() != null) {
            cabecera.add(new Phrase("Emitida: " + factura.getFechaEmision().format(FORMATO_FECHA) + "\n",
                    FUENTE_NORMAL));
        }
        cabecera.setSpacingAfter(18f);
        return cabecera;
    }

    private static Element detalle(FacturaResponse factura, PedidoResponse pedido) {
        PdfPTable tabla = new PdfPTable(2);
        tabla.setWidthPercentage(85f);

        fila(tabla, "Cliente", factura.getNombreCliente(), false);
        fila(tabla, "Dron", factura.getDetalleDron(), false);
        fila(tabla, "Distancia recorrida", magnitud(pedido.getDistanciaSolicitada(), "km"), false);
        fila(tabla, "Peso del paquete", magnitud(pedido.getPesoSolicitado(), "kg"), false);
        fila(tabla, "Tiempo de vuelo", magnitud(pedido.getHorasSolicitadas(), "min"), false);

        fila(tabla, "Costo base", dinero(factura.getCostoBase()), false);
        fila(tabla, "Costo por distancia", dinero(factura.getComponenteDistancia()), false);
        fila(tabla, "Costo por peso", dinero(factura.getComponentePeso()), false);

        fila(tabla, "TOTAL", dinero(factura.getTotal()), true);
        return tabla;
    }

    private static void fila(PdfPTable tabla, String concepto, String valor, boolean destacada) {
        Font fuente = destacada ? FUENTE_TOTAL : FUENTE_NORMAL;

        PdfPCell celdaConcepto = new PdfPCell(new Phrase(concepto, fuente));
        celdaConcepto.setPadding(6f);

        PdfPCell celdaValor = new PdfPCell(new Phrase(valor == null ? "" : valor, fuente));
        celdaValor.setPadding(6f);
        celdaValor.setHorizontalAlignment(Element.ALIGN_RIGHT);

        tabla.addCell(celdaConcepto);
        tabla.addCell(celdaValor);
    }

    private static Element pie() {
        Paragraph pie = new Paragraph(
                "\nDocumento generado automaticamente. Gracias por su compra.", FUENTE_NORMAL);
        pie.setAlignment(Element.ALIGN_CENTER);
        pie.setSpacingBefore(20f);
        return pie;
    }

    private static String dinero(Double valor) {
        return valor == null ? "" : String.format(Locale.US, "$ %.2f", valor);
    }

    private static String magnitud(Double valor, String unidad) {
        return valor == null ? "" : String.format(Locale.US, "%.2f %s", valor, unidad);
    }
}
