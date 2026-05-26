package com.supertesis.asistencia.backend.service;

import org.openpdf.text.Document;
import org.openpdf.text.Element;
import org.openpdf.text.Font;
import org.openpdf.text.Image;
import org.openpdf.text.Paragraph;
import org.openpdf.text.Rectangle;
import org.openpdf.text.pdf.BaseFont;
import org.openpdf.text.pdf.PdfContentByte;
import org.openpdf.text.pdf.PdfGState;
import org.openpdf.text.pdf.PdfPCell;
import org.openpdf.text.pdf.PdfPTable;
import org.openpdf.text.pdf.PdfWriter;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.supertesis.asistencia.backend.entity.Personal;
import com.supertesis.asistencia.backend.exception.ResourceNotFoundException;
import com.supertesis.asistencia.backend.repository.PersonalRepository;
import com.supertesis.asistencia.backend.security.QrTokenService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;

@Service
@RequiredArgsConstructor
public class CarnetService {

    private final PersonalRepository personalRepository;
    private final QrTokenService qrTokenService;

    /**
     * Coordina la obtención del token de acceso y la generación del documento PDF.
     */
    public byte[] obtenerCarnetDocumento(Integer personalId) {
        // 1. Validar existencia del personal
        Personal personal = personalRepository.findById(personalId)
                .orElseThrow(() -> new ResourceNotFoundException("Personal", personalId.toString()));

        // 2. Delegar la generación del token
        String qrToken = qrTokenService.generateQrToken(personalId);

        // 3. Construcción del PDF en memoria (Dimensiones estándar de carnet)
        Rectangle tamanoCarnet = new Rectangle(241, 156);
        Document documento = new Document(tamanoCarnet, 10, 10, 8, 10);
        ByteArrayOutputStream salida = new ByteArrayOutputStream();

        try {
            PdfWriter writer = PdfWriter.getInstance(documento, salida);
            documento.open();

            // --- CAPA DE FONDO ---
            PdfContentByte fondoCanvas = writer.getDirectContentUnder();
            fondoCanvas.setColorFill(new Color(30, 58, 138)); // Azul Institucional
            fondoCanvas.rectangle(0, 130, 241, 26);
            fondoCanvas.fill();
            
            fondoCanvas.setColorStroke(Color.GRAY);
            fondoCanvas.setLineWidth(1f);
            fondoCanvas.rectangle(2, 2, 237, 152);
            fondoCanvas.stroke();

            // --- CAPA INTERMEDIA: ENCABEZADO ---
            Font fuenteTitulo = new Font(Font.HELVETICA, 10, Font.BOLD, Color.WHITE);
            Paragraph titulo = new Paragraph("SISTEMA DE ASISTENCIA", fuenteTitulo);
            titulo.setAlignment(Paragraph.ALIGN_CENTER);
            documento.add(titulo);

            // --- SOLUCIÓN PUNTO 1: TABLA ESTRUCTURADA CONTRA DESBORDAMIENTO ---
            // Tabla de 2 columnas: Columna 1 (Datos: 58%) | Columna 2 (QR y Nota: 42%)
            PdfPTable tablaEstructura = new PdfPTable(new float[]{58f, 42f});
            tablaEstructura.setTotalWidth(221f); // Ancho total utilizable dentro de los márgenes
            tablaEstructura.setLockedWidth(true);
            
            // Fuentes base utilizando BaseFont
            BaseFont helvetica = BaseFont.createFont(BaseFont.HELVETICA, BaseFont.WINANSI, BaseFont.NOT_EMBEDDED);
            Font fuenteNombre = new Font(helvetica, 10, Font.BOLD, Color.BLACK);
            Font fuenteDetalle = new Font(helvetica, 8, Font.NORMAL, Color.DARK_GRAY);
            Font fuenteNotaPie = new Font(helvetica, 5.5f, Font.ITALIC, Color.GRAY);

            // --- SUB-BLOQUE IZQUIERDO: DATOS DEL PERSONAL ---
            PdfPCell celdaDatos = new PdfPCell();
            celdaDatos.setBorder(PdfPCell.NO_BORDER);
            celdaDatos.setPaddingTop(12f);
            celdaDatos.setPaddingLeft(5f);

            // Párrafo del Nombre Completo (Soporta salto de línea automático si es muy largo)
            Paragraph pNombre = new Paragraph(personal.getNombre() + " " + personal.getApellido(), fuenteNombre);
            pNombre.setLeading(11f); // Espaciado interlineal controlado
            celdaDatos.addElement(pNombre);

            // Espacio intermedio controlado
            Paragraph pEspacio = new Paragraph(" ", new Font(helvetica, 4));
            celdaDatos.addElement(pEspacio);

            // Cédula e Identificación
            Paragraph pCedula = new Paragraph("ID / Cédula: " + personal.getCedula(), fuenteDetalle);
            celdaDatos.addElement(pCedula);

            // Departamento
            String dpto = personal.getDepartamento() != null ? personal.getDepartamento().getNombre() : "General";
            Paragraph pDpto = new Paragraph("Dpto: " + dpto, fuenteDetalle);
            celdaDatos.addElement(pDpto);

            tablaEstructura.addCell(celdaDatos);

            // --- SUB-BLOQUE DERECHO: QR E IMAGEN ---
            PdfPCell celdaQR = new PdfPCell();
            celdaQR.setBorder(PdfPCell.NO_BORDER);
            celdaQR.setHorizontalAlignment(Element.ALIGN_CENTER);
            celdaQR.setPaddingTop(2f);

            // Generación e inserción del QR dentro de la celda de la tabla
            byte[] qrBytes = this.generarQrByteArray(qrToken, 80, 80);
            Image imagenQr = Image.getInstance(qrBytes);
            imagenQr.setAlignment(Image.ALIGN_CENTER);
            celdaQR.addElement(imagenQr);

            // SOLUCIÓN PUNTO 2 (A): Mover nota de validez al lado derecho bajo el QR
            Paragraph pNota = new Paragraph("Válido para acceso institucional.", fuenteNotaPie);
            pNota.setAlignment(Paragraph.ALIGN_CENTER);
            pNota.setLeading(7f);
            celdaQR.addElement(pNota);

            tablaEstructura.addCell(celdaQR);

            // Escribir la tabla estructurada en una posición absoluta fija dentro del carnet
            tablaEstructura.writeSelectedRows(0, -1, 10, 122, writer.getDirectContent());

            // --- SOLUCIÓN PUNTO 2 (B): CAPA SUPERIOR CON SELLO UBICADO ABAJO A LA IZQUIERDA ---
            PdfContentByte capaSuperiorCanvas = writer.getDirectContent();
            capaSuperiorCanvas.saveState();

            ClassPathResource imgResource = new ClassPathResource("images/sello.png");
            if (imgResource.exists()) {
                // Opacidad sutil al 10% para que actúe como una marca de agua real no invasiva
                PdfGState estadoTransparencia = new PdfGState();
                estadoTransparencia.setFillOpacity(0.15f);
                capaSuperiorCanvas.setGState(estadoTransparencia);

                try (InputStream is = imgResource.getInputStream()) {
                    byte[] selloBytes = is.readAllBytes();
                    Image imagenSello = Image.getInstance(selloBytes);
                    imagenSello.scaleAbsolute(45, 45); // Un poco más pequeño para encajar estéticamente
                    // Posicionamiento en la esquina inferior izquierda del espacio de datos (X: 18, Y: 12)
                    imagenSello.setAbsolutePosition(18, 12);
                    capaSuperiorCanvas.addImage(imagenSello);
                }
            } else {
                System.err.println("[WARN] El archivo 'images/sello.png' no fue encontrado. Carnet generado sin marca de agua.");
            }

            capaSuperiorCanvas.restoreState();
            documento.close();
            
        } catch (Exception e) {
            throw new RuntimeException("Fallo en la infraestructura de renderizado del carnet", e);
        }

        return salida.toByteArray();
    }

    /**
     * Genera la imagen del código QR en formato PNG y en memoria RAM.
     */
    private byte[] generarQrByteArray(String texto, int ancho, int alto) throws Exception {
        QRCodeWriter qrCodeWriter = new QRCodeWriter();
        BitMatrix bitMatrix = qrCodeWriter.encode(texto, BarcodeFormat.QR_CODE, ancho, alto);
        ByteArrayOutputStream pngOutputStream = new ByteArrayOutputStream();
        MatrixToImageWriter.writeToStream(bitMatrix, "PNG", pngOutputStream);
        return pngOutputStream.toByteArray();
    }
}