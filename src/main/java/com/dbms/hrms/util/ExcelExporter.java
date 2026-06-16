package com.dbms.hrms.util;

import jakarta.servlet.http.HttpServletResponse;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.IOException;
import java.util.List;

public class ExcelExporter {

    public static void export(String filename,
                              List<String> headers,
                              List<List<String>> rows,
                              HttpServletResponse response) throws IOException {

        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Sheet1");

        // header
        Row headerRow = sheet.createRow(0);
        for (int i = 0; i < headers.size(); i++) {
            headerRow.createCell(i).setCellValue(headers.get(i));
        }

        // rows
        int rowIndex = 1;
        for (List<String> row : rows) {
            Row excelRow = sheet.createRow(rowIndex++);
            for (int col = 0; col < headers.size(); col++) {
                excelRow.createCell(col).setCellValue(col < row.size() ? row.get(col) : "");
            }
        }

        for (int i = 0; i < headers.size(); i++) sheet.autoSizeColumn(i);

        response.setContentType("application/vnd.ms-excel");
        response.setHeader("Content-Disposition", "attachment; filename=" + filename + ".xlsx");
        workbook.write(response.getOutputStream());
        workbook.close();
    }
}

