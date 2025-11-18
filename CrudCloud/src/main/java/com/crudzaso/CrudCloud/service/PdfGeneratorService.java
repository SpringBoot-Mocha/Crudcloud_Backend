package com.crudzaso.CrudCloud.service;

import com.itextpdf.text.*;
import com.itextpdf.text.pdf.PdfWriter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * PDF Generator Service - Genera PDFs con credenciales de BD
 */
@Service
@Slf4j
public class PdfGeneratorService {

    /**
     * Genera PDF con credenciales completas de la instancia
     */
    public byte[] generateCredentialsPdf(
            String instanceName,
            String databaseEngine,
            String host,
            Integer port,
            String databaseName,
            String username,
            String password) throws IOException {

        Document document = new Document(PageSize.A4, 40, 40, 40, 40);
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        try {
            PdfWriter.getInstance(document, outputStream);
            document.open();

            // Título
            Paragraph title = new Paragraph();
            title.setAlignment(Element.ALIGN_CENTER);
            title.add(new Chunk("CREDENCIALES DE BASE DE DATOS", 
                FontFactory.getFont(FontFactory.HELVETICA_BOLD, 20, BaseColor.BLUE)));
            document.add(title);
            
            document.add(new Paragraph(" "));

            // Fecha
            Paragraph date = new Paragraph();
            date.setAlignment(Element.ALIGN_RIGHT);
            date.add(new Chunk(LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")),
                FontFactory.getFont(FontFactory.HELVETICA, 10, BaseColor.GRAY)));
            document.add(date);
            
            document.add(new Paragraph(" "));

            // Tabla de credenciales
            com.itextpdf.text.pdf.PdfPTable table = new com.itextpdf.text.pdf.PdfPTable(2);
            table.setWidthPercentage(100);
            table.setSpacingBefore(10f);
            table.setSpacingAfter(10f);

            // Encabezados
            addTableCell(table, "PROPIEDAD", true);
            addTableCell(table, "VALOR", true);

            // Datos
            addTableCell(table, "Nombre de instancia", false);
            addTableCell(table, instanceName, false);

            addTableCell(table, "Motor de BD", false);
            addTableCell(table, databaseEngine, false);

            addTableCell(table, "Host", false);
            addTableCell(table, host, false);

            addTableCell(table, "Puerto", false);
            addTableCell(table, port.toString(), false);

            addTableCell(table, "Base de datos", false);
            addTableCell(table, databaseName, false);

            addTableCell(table, "Usuario", false);
            addTableCell(table, username, false);

            addTableCell(table, "Contraseña", false);
            addTableCell(table, password, false);

            document.add(table);

            document.add(new Paragraph(" "));

            // Advertencia
            Paragraph warning = new Paragraph();
            warning.add(new Chunk("⚠️ ADVERTENCIA DE SEGURIDAD",
                FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12, BaseColor.RED)));
            document.add(warning);

            Paragraph warningText = new Paragraph();
            warningText.setAlignment(Element.ALIGN_JUSTIFIED);
            warningText.add(new Chunk(
                "Este documento contiene información sensible. Guárdalo en un lugar seguro. " +
                "La contraseña aparece aquí por única vez. Si la pierdes, deberás generar una nueva " +
                "desde el panel de administración. Nunca compartas este documento con terceros.",
                FontFactory.getFont(FontFactory.HELVETICA, 10)));
            document.add(warningText);

            document.add(new Paragraph(" "));

            // Footer
            Paragraph footer = new Paragraph();
            footer.setAlignment(Element.ALIGN_CENTER);
            footer.add(new Chunk("CrudCloud © 2025 - Gestión de Bases de Datos en la Nube",
                FontFactory.getFont(FontFactory.HELVETICA, 9, BaseColor.GRAY)));
            document.add(footer);

            document.close();
            log.info("✅ PDF de credenciales generado exitosamente");

            return outputStream.toByteArray();

        } catch (DocumentException e) {
            log.error("❌ Error generando PDF: {}", e.getMessage(), e);
            throw new IOException("Error generando PDF de credenciales", e);
        }
    }

    private void addTableCell(com.itextpdf.text.pdf.PdfPTable table, String content, boolean isHeader) {
        com.itextpdf.text.pdf.PdfPCell cell = new com.itextpdf.text.pdf.PdfPCell(new Phrase(content));
        
        if (isHeader) {
            cell.setBackgroundColor(BaseColor.BLUE);
            cell.setVerticalAlignment(Element.ALIGN_CENTER);
            Phrase phrase = new Phrase(content, FontFactory.getFont(FontFactory.HELVETICA_BOLD, 11, BaseColor.WHITE));
            cell.setPhrase(phrase);
        } else {
            cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
            cell.setPadding(8f);
        }
        
        table.addCell(cell);
    }
}
