package quantify.BoticaSaid.service.reports;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;

import java.io.OutputStream;
import java.util.List;
import java.util.function.BiConsumer;

public class ExcelExportUtil {

    public static <T> void export(
            String sheetName,
            String[] headers,
            List<T> rows,
            BiConsumer<Row, T> rowWriter,
            OutputStream out
    ) throws Exception {
        try (SXSSFWorkbook wb = new SXSSFWorkbook(200)) {
            Sheet sheet = wb.createSheet(sheetName);

            if (sheet instanceof org.apache.poi.xssf.streaming.SXSSFSheet sx) {
                sx.trackAllColumnsForAutoSizing();
            }

            Row h = sheet.createRow(0);
            CellStyle headerStyle = wb.createCellStyle();
            Font bold = wb.createFont();
            bold.setBold(true);
            headerStyle.setFont(bold);
            for (int i = 0; i < headers.length; i++) {
                Cell c = h.createCell(i);
                c.setCellValue(headers[i]);
                c.setCellStyle(headerStyle);
            }

            int r = 1;
            for (T row : rows) {
                Row rr = sheet.createRow(r++);
                rowWriter.accept(rr, row);
            }

            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }

            wb.write(out);
            wb.dispose();
        }
    }
}