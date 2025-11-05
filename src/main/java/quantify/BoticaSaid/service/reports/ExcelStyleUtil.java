package quantify.BoticaSaid.service.reports;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.streaming.SXSSFSheet;

public final class ExcelStyleUtil {

    private ExcelStyleUtil() {}

    public static class Styles {
        public final CellStyle title;
        public final CellStyle header;
        public final CellStyle text;
        public final CellStyle textCenter;
        public final CellStyle integer;
        public final CellStyle number;
        public final CellStyle currency;
        public final CellStyle percent;
        public final CellStyle date;
        public final CellStyle zebra1;
        public final CellStyle zebra2;
        public final CellStyle unlocked; // para permitir edición si se protege la hoja

        private Styles(CellStyle title, CellStyle header, CellStyle text, CellStyle textCenter,
                       CellStyle integer, CellStyle number, CellStyle currency, CellStyle percent, CellStyle date,
                       CellStyle zebra1, CellStyle zebra2, CellStyle unlocked) {
            this.title = title;
            this.header = header;
            this.text = text;
            this.textCenter = textCenter;
            this.integer = integer;
            this.number = number;
            this.currency = currency;
            this.percent = percent;
            this.date = date;
            this.zebra1 = zebra1;
            this.zebra2 = zebra2;
            this.unlocked = unlocked;
        }
    }

    public static Styles createStyles(Workbook wb) {
        DataFormat df = wb.createDataFormat();

        // Fuentes
        Font base = wb.createFont();
        base.setFontHeightInPoints((short)10);
        base.setColor(IndexedColors.BLACK.getIndex());

        Font boldWhite = wb.createFont();
        boldWhite.setBold(true);
        boldWhite.setFontHeightInPoints((short)10);
        boldWhite.setColor(IndexedColors.WHITE.getIndex());

        Font titleFont = wb.createFont();
        titleFont.setBold(true);
        titleFont.setFontHeightInPoints((short)14);
        titleFont.setColor(IndexedColors.WHITE.getIndex());

        // Borde fino
        BorderStyle b = BorderStyle.THIN;
        short borderColor = IndexedColors.GREY_50_PERCENT.getIndex();

        // Base
        CellStyle baseStyle = wb.createCellStyle();
        baseStyle.setFont(base);
        baseStyle.setBorderTop(b); baseStyle.setTopBorderColor(borderColor);
        baseStyle.setBorderRight(b); baseStyle.setRightBorderColor(borderColor);
        baseStyle.setBorderBottom(b); baseStyle.setBottomBorderColor(borderColor);
        baseStyle.setBorderLeft(b); baseStyle.setLeftBorderColor(borderColor);
        baseStyle.setVerticalAlignment(VerticalAlignment.CENTER);
        baseStyle.setWrapText(false);

        CellStyle text = wb.createCellStyle();
        text.cloneStyleFrom(baseStyle);
        text.setAlignment(HorizontalAlignment.LEFT);

        CellStyle textCenter = wb.createCellStyle();
        textCenter.cloneStyleFrom(baseStyle);
        textCenter.setAlignment(HorizontalAlignment.CENTER);

        CellStyle integer = wb.createCellStyle();
        integer.cloneStyleFrom(baseStyle);
        integer.setAlignment(HorizontalAlignment.RIGHT);
        integer.setDataFormat(df.getFormat("#,##0"));

        CellStyle number = wb.createCellStyle();
        number.cloneStyleFrom(baseStyle);
        number.setAlignment(HorizontalAlignment.RIGHT);
        number.setDataFormat(df.getFormat("#,##0.00"));

        CellStyle currency = wb.createCellStyle();
        currency.cloneStyleFrom(baseStyle);
        currency.setAlignment(HorizontalAlignment.RIGHT);
        currency.setDataFormat(df.getFormat("\"S/\"#,##0.00"));

        CellStyle percent = wb.createCellStyle();
        percent.cloneStyleFrom(baseStyle);
        percent.setAlignment(HorizontalAlignment.RIGHT);
        percent.setDataFormat(df.getFormat("0.00%"));

        CellStyle date = wb.createCellStyle();
        date.cloneStyleFrom(baseStyle);
        date.setAlignment(HorizontalAlignment.CENTER);
        date.setDataFormat(df.getFormat("dd/MM/yyyy"));

        CellStyle header = wb.createCellStyle();
        header.cloneStyleFrom(baseStyle);
        header.setAlignment(HorizontalAlignment.CENTER);
        header.setFillForegroundColor(IndexedColors.DARK_BLUE.getIndex());
        header.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        header.setFont(boldWhite);

        CellStyle title = wb.createCellStyle();
        title.cloneStyleFrom(baseStyle);
        title.setAlignment(HorizontalAlignment.CENTER);
        title.setFillForegroundColor(IndexedColors.BLUE_GREY.getIndex());
        title.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        title.setFont(titleFont);

        CellStyle zebra1 = wb.createCellStyle();
        zebra1.cloneStyleFrom(baseStyle);
        zebra1.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
        zebra1.setFillPattern(FillPatternType.SOLID_FOREGROUND);

        CellStyle zebra2 = wb.createCellStyle();
        zebra2.cloneStyleFrom(baseStyle);
        zebra2.setFillForegroundColor(IndexedColors.WHITE.getIndex());
        zebra2.setFillPattern(FillPatternType.SOLID_FOREGROUND);

        CellStyle unlocked = wb.createCellStyle();
        unlocked.cloneStyleFrom(baseStyle);
        unlocked.setLocked(false);

        return new Styles(title, header, text, textCenter, integer, number, currency, percent, date, zebra1, zebra2, unlocked);
    }

    public static void setupSheetCommon(Sheet sheet, int headerRowIndex, int lastCol) {
        sheet.createFreezePane(0, headerRowIndex + 1); // congela encabezado
        sheet.setAutoFilter(new CellRangeAddress(headerRowIndex, headerRowIndex, 0, lastCol));
        sheet.setDisplayGridlines(false);

        PrintSetup ps = sheet.getPrintSetup();
        ps.setLandscape(true);
        sheet.setFitToPage(true);
        sheet.setAutobreaks(true);

        sheet.setMargin(Sheet.LeftMargin, 0.3);
        sheet.setMargin(Sheet.RightMargin, 0.3);
        sheet.setMargin(Sheet.TopMargin, 0.5);
        sheet.setMargin(Sheet.BottomMargin, 0.5);

        sheet.setZoom(110);
    }

    public static void trackAutosizeIfSXSSF(Sheet sheet) {
        if (sheet instanceof SXSSFSheet sx) {
            sx.trackAllColumnsForAutoSizing();
        }
    }

    public static void autosizeColumns(Sheet sheet, int colCount, int[] minWidthsChars) {
        for (int c = 0; c < colCount; c++) {
            sheet.autoSizeColumn(c);
            if (minWidthsChars != null && c < minWidthsChars.length && minWidthsChars[c] > 0) {
                int wanted = minWidthsChars[c] * 256;
                if (sheet.getColumnWidth(c) < wanted) {
                    sheet.setColumnWidth(c, wanted);
                }
            }
        }
    }

    public static void setRowHeights(Row titleRow, Row headerRow) {
        if (titleRow != null) titleRow.setHeightInPoints(22f);
        if (headerRow != null) headerRow.setHeightInPoints(18f);
    }

    public static void mergeTitle(Sheet sheet, int titleRowIndex, int lastCol, CellStyle titleStyle, String titleText) {
        Row r = sheet.createRow(titleRowIndex);
        Cell c = r.createCell(0);
        c.setCellValue(titleText);
        c.setCellStyle(titleStyle);
        if (lastCol > 0) {
            CellRangeAddress region = new CellRangeAddress(titleRowIndex, titleRowIndex, 0, lastCol);
            sheet.addMergedRegion(region);
            for (int i = 1; i <= lastCol; i++) {
                Cell cc = r.createCell(i);
                cc.setCellStyle(titleStyle);
            }
        }
    }

    public static void fillRowStyles(Row row, CellStyle baseStyle, int colCount) {
        for (int c = 0; c < colCount; c++) {
            Cell cell = row.getCell(c);
            if (cell == null) cell = row.createCell(c);
            if (cell.getCellStyle() == null || cell.getCellStyle().getIndex() == 0) {
                cell.setCellStyle(baseStyle);
            }
        }
    }
}