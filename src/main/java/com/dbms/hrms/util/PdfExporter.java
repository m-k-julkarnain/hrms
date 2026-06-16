package com.dbms.hrms.util;

import com.lowagie.text.*;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.List;

public class PdfExporter {

    public static void export(String title, List<String> headers, List<List<String>> rows, HttpServletResponse response)
            throws IOException {

        Document document = new Document(PageSize.A4.rotate());
        PdfWriter.getInstance(document, response.getOutputStream());

        response.setContentType("application/pdf");
        response.setHeader("Content-Disposition", "attachment; filename=" + title + ".pdf");

        document.open();

        Paragraph p = new Paragraph(title, FontFactory.getFont(FontFactory.HELVETICA_BOLD, 16));
        p.setAlignment(Element.ALIGN_CENTER);
        p.setSpacingAfter(10);
        document.add(p);

        PdfPTable table = new PdfPTable(headers.size());
        table.setWidthPercentage(100);

        // headers
        for (String h : headers) {
            PdfPCell cell = new PdfPCell(new Phrase(h));
            table.addCell(cell);
        }

        // rows
        for (List<String> row : rows) {
            for (String value : row) {
                table.addCell(new PdfPCell(new Phrase(value)));
            }
        }

        document.add(table);
        document.close();
    }
}
