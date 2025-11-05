package quantify.BoticaSaid.service.reports;

import quantify.BoticaSaid.dto.reports.InventoryDtos.InventoryLotDto;
import quantify.BoticaSaid.dto.reports.InventoryDtos.InventoryProductFullDto;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.ss.util.CellRangeAddressList;
import org.apache.poi.ss.util.CellReference;
import org.apache.poi.ss.util.RegionUtil;
import org.apache.poi.util.IOUtils;
import org.apache.poi.xddf.usermodel.chart.*;
import org.apache.poi.xssf.usermodel.*;

import java.awt.*;
import java.awt.Color;
import java.awt.Font;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.time.LocalDate;
import java.util.List;
import javax.imageio.ImageIO;

import static quantify.BoticaSaid.service.reports.ExcelStyleUtil.*;

/**
 * Genera el archivo Inventario_Botica.xlsx con:
 * - Hoja Productos (formato profesional, validaciones, fórmulas, condicionales)
 * - Hoja Lotes (estado auto, validación de códigos)
 * - Hoja Dashboard (KPIs, gráficos)
 * - Hoja oculta Listas (fuentes de validación)
 */
public class InventoryProfessionalExporter {

    // Columnas Productos (A..U)
    private static final int COL_CODIGO = 0;              // A
    private static final int COL_NOMBRE = 1;              // B
    private static final int COL_ACTIVO = 2;              // C
    private static final int COL_CATEGORIA = 3;           // D
    private static final int COL_CONC = 4;                // E
    private static final int COL_PRESENT = 5;             // F
    private static final int COL_LAB = 6;                 // G
    private static final int COL_PRINCIPIO = 7;           // H
    private static final int COL_TIPO = 8;                // I
    private static final int COL_CANT_UNID = 9;           // J
    private static final int COL_STOCK = 10;              // K
    private static final int COL_MIN = 11;                // L
    private static final int COL_PRECIO = 12;             // M
    private static final int COL_VALOR_TOTAL = 13;        // N
    private static final int COL_DTO = 14;                // O
    private static final int COL_LOTES = 15;              // P
    private static final int COL_LOTES_VENC = 16;         // Q
    private static final int COL_PROX_VENC = 17;          // R
    private static final int COL_VALOR_COMPRA = 18;       // S
    private static final int COL_COSTO_PROM = 19;         // T
    private static final int COL_ULT_ACT = 20;            // U

    public static void exportInventarioProfesional(
            List<InventoryProductFullDto> products,
            List<InventoryLotDto> lots,
            OutputStream out
    ) throws Exception {

        try (XSSFWorkbook wb = new XSSFWorkbook()) {
            Styles st = ExcelStyleUtil.createStyles(wb);

            // 1) Hoja Listas (oculta)
            XSSFSheet listas = wb.createSheet("Listas");
            buildListasSheet(wb, listas);

            // 2) Hoja Productos
            XSSFSheet productos = wb.createSheet("Productos");
            buildProductosSheet(wb, productos, st, products);

            // 3) Hoja Lotes
            XSSFSheet lotesSheet = wb.createSheet("Lotes");
            buildLotesSheet(wb, lotesSheet, st, lots, products.size());

            // 4) Dashboard
            XSSFSheet dashboard = wb.createSheet("Dashboard");
            buildDashboardSheet(wb, dashboard, st, products.size());

            // Pie de página en todas las hojas
            for (Sheet s : wb) {
                s.getFooter().setLeft("Inventario generado automáticamente – © 2025 QuantifyTech");
            }

            // Oculta “Listas”
            int idxListas = wb.getSheetIndex("Listas");
            wb.setSheetHidden(idxListas, true);

            // Escribir
            wb.write(out);
        }
    }

    private static void buildListasSheet(XSSFWorkbook wb, XSSFSheet sheet) {
        Row h = sheet.createRow(0);
        h.createCell(0).setCellValue("Categorias");
        h.createCell(1).setCellValue("Laboratorios");
        h.createCell(2).setCellValue("Tipos");

        String[] categorias = new String[]{
                "Antibióticos","Analgésicos","Vitaminas","Vasodilatadores","Laxantes","Antiinflamatorios"
        };
        String[] laboratorios = new String[]{
                "Quantify","Bayer","Teva","Pfizer","Genfar"
        };
        String[] tipos = new String[]{ "Marca","Genérico" };

        int r = 1;
        int max = Math.max(categorias.length, Math.max(laboratorios.length, tipos.length));
        for (int i = 0; i < max; i++) {
            Row row = sheet.getRow(r) != null ? sheet.getRow(r) : sheet.createRow(r);
            if (i < categorias.length) row.createCell(0).setCellValue(categorias[i]);
            if (i < laboratorios.length) row.createCell(1).setCellValue(laboratorios[i]);
            if (i < tipos.length) row.createCell(2).setCellValue(tipos[i]);
            r++;
        }

        createNamedRange(wb, "CategoriasList", sheet.getSheetName(), 1, 0, categorias.length);
        createNamedRange(wb, "LaboratoriosList", sheet.getSheetName(), 1, 1, laboratorios.length);
        createNamedRange(wb, "TiposList", sheet.getSheetName(), 1, 2, tipos.length);

        sheet.autoSizeColumn(0); sheet.autoSizeColumn(1); sheet.autoSizeColumn(2);
    }

    private static void createNamedRange(XSSFWorkbook wb, String name, String sheetName, int startRow, int col, int count) {
        Name nm = wb.createName();
        nm.setNameName(name);
        String colLetter = CellReference.convertNumToColString(col);
        int start = startRow + 1; // 1-based
        int end = startRow + count;
        nm.setRefersToFormula("'" + sheetName + "'!$" + colLetter + "$" + start + ":$" + colLetter + "$" + end);
    }

    private static void buildProductosSheet(XSSFWorkbook wb, XSSFSheet sheet, Styles st, List<InventoryProductFullDto> products) throws Exception {
        trackAutosizeIfSXSSF(sheet);

        String[] headers = {
                "Código","Nombre del Producto","Activo","Categoría","Concentración","Presentación","Laboratorio",
                "Principio Activo","Tipo Medicamento","Cant. Unidades","Stock Total","Stock Mínimo",
                "Precio UND (S/.)","Valor Total (S/.)","Descuento (%)","Lotes","Lotes Vencidos","Próx. Vencimiento",
                "Valor Compra Total","Costo Promedio","Última Actualización"
        };
        int lastCol = headers.length - 1;

        mergeTitle(sheet, 0, lastCol, st.title, "Inventario - Productos");


        Row hh = sheet.createRow(1);
        for (int i = 0; i < headers.length; i++) {
            Cell c = hh.createCell(i);
            c.setCellValue(headers[i]);
            c.setCellStyle(st.header);
        }
        setRowHeights(sheet.getRow(0), hh);

        int rowStart = 2;

        for (int i = 0; i < products.size(); i++) {
            InventoryProductFullDto p = products.get(i);
            Row r = sheet.createRow(rowStart + i);

            setText(r, COL_CODIGO, st.textCenter, p.codigo_barras);
            setText(r, COL_NOMBRE, st.text, p.nombre);
            setText(r, COL_ACTIVO, st.textCenter, p.activo == null ? "" : (p.activo ? "Sí" : "No"));
            setText(r, COL_CATEGORIA, st.text, nvl(p.categoria));
            setText(r, COL_CONC, st.text, nvl(p.concentracion));
            setText(r, COL_PRESENT, st.text, nvl(p.presentacion));
            setText(r, COL_LAB, st.text, nvl(p.laboratorio));
            setText(r, COL_PRINCIPIO, st.text, nvl(p.principio_activo));
            setText(r, COL_TIPO, st.text, nvl(p.tipo_medicamento));

            setNumber(r, COL_CANT_UNID, st.integer, p.cantidad_unidades_blister != null ? p.cantidad_unidades_blister : p.cantidad_general);
            setNumber(r, COL_STOCK, st.integer, p.stock_total);
            setNumber(r, COL_MIN, st.integer, p.cantidad_minima != null ? p.cantidad_minima : 0);

            setDouble(r, COL_PRECIO, st.currency, p.precio_venta_und != null ? p.precio_venta_und.doubleValue() : 0d);

            String rowExcel = String.valueOf(rowStart + i + 1);
            formula(r, COL_VALOR_TOTAL, st.currency, col(COL_STOCK) + rowExcel + "*" + col(COL_PRECIO) + rowExcel);

            double dto = 0d;
            if (p.descuento != null) {
                dto = p.descuento.doubleValue();
                if (dto > 1) dto = dto / 100d; // normaliza 10 => 0.10
            }
            setDouble(r, COL_DTO, st.percent, dto);

            setNumber(r, COL_LOTES, st.integer, p.lotes);
            setNumber(r, COL_LOTES_VENC, st.integer, p.lotes_vencidos);

            if (p.proximo_vencimiento != null && !p.proximo_vencimiento.isBlank()) {
                try {
                    LocalDate ld = LocalDate.parse(p.proximo_vencimiento);
                    Cell c = r.createCell(COL_PROX_VENC);
                    c.setCellValue(java.sql.Date.valueOf(ld));
                    c.setCellStyle(st.date);
                } catch (Exception ignored) {
                    setText(r, COL_PROX_VENC, st.textCenter, p.proximo_vencimiento);
                }
            } else {
                setText(r, COL_PROX_VENC, st.textCenter, "");
            }

            formula(r, COL_VALOR_COMPRA, st.currency, col(COL_VALOR_TOTAL) + rowExcel + "*(1-" + col(COL_DTO) + rowExcel + ")");
            formula(r, COL_COSTO_PROM, st.currency, "IF(" + col(COL_STOCK) + rowExcel + ">0," + col(COL_VALOR_COMPRA) + rowExcel + "/" + col(COL_STOCK) + rowExcel + ",0)");
            formula(r, COL_ULT_ACT, st.date, "TODAY()");

            fillRowStyles(r, ((rowStart + i) % 2 == 0) ? st.zebra1 : st.zebra2, COL_ULT_ACT + 1);
        }

        // Validaciones desplegables
        addListValidation(sheet, COL_CATEGORIA, rowStart, rowStart + Math.max(1, products.size()) - 1, "CategoriasList");
        addListValidation(sheet, COL_LAB, rowStart, rowStart + Math.max(1, products.size()) - 1, "LaboratoriosList");
        addListValidation(sheet, COL_TIPO, rowStart, rowStart + Math.max(1, products.size()) - 1, "TiposList");

        // Validaciones numéricas
        addDecimalValidation(sheet, COL_PRECIO, rowStart, rowStart + Math.max(1, products.size()) - 1, 0, 1e12);
        addIntegerValidation(sheet, COL_MIN, rowStart, rowStart + Math.max(1, products.size()) - 1, 0, Integer.MAX_VALUE);

        // Condicionales
        addStockConditionalFormatting(sheet, rowStart, rowStart + products.size(), COL_STOCK, COL_MIN);
        addVencimientoConditionalFormatting(sheet, rowStart, rowStart + products.size(), COL_PROX_VENC);

        // Fijar encabezado, filtros, tamaños
        setupSheetCommon(sheet, 1, lastCol);
        autosizeColumns(sheet, lastCol + 1, new int[]{12, 30, 8, 16, 16, 16, 16, 16, 14, 12, 12, 12, 14, 16, 12, 10, 12, 14, 18, 16, 16});

        // Proteger fórmulas
        protectFormulas(sheet, rowStart, rowStart + products.size() - 1, new int[]{COL_VALOR_TOTAL, COL_VALOR_COMPRA, COL_COSTO_PROM, COL_ULT_ACT});
        sheet.protectSheet("quantify");
    }

    private static void buildLotesSheet(XSSFWorkbook wb, XSSFSheet sheet, Styles st, List<InventoryLotDto> lots, int productsCount) {
        trackAutosizeIfSXSSF(sheet);

        String[] headers = {"Código Producto","Lote","Unidades","Fecha de Vencimiento","Precio Compra","Estado"};
        int lastCol = headers.length - 1;

        mergeTitle(sheet, 0, lastCol, st.title, "Inventario - Lotes");

        Row h = sheet.createRow(1);
        for (int i = 0; i < headers.length; i++) {
            Cell c = h.createCell(i);
            c.setCellValue(headers[i]);
            c.setCellStyle(st.header);
        }
        setRowHeights(sheet.getRow(0), h);

        int rowStart = 2;
        for (int i = 0; i < lots.size(); i++) {
            InventoryLotDto l = lots.get(i);
            Row r = sheet.createRow(rowStart + i);

            setText(r, 0, st.textCenter, l.codigo_barras);
            setNumber(r, 1, st.integer, l.lote_id);
            setNumber(r, 2, st.integer, l.cantidad_unidades);

            if (l.fecha_vencimiento != null && !l.fecha_vencimiento.isBlank()) {
                try {
                    LocalDate ld = LocalDate.parse(l.fecha_vencimiento);
                    Cell c = r.createCell(3);
                    c.setCellValue(java.sql.Date.valueOf(ld));
                    c.setCellStyle(st.date);
                } catch (Exception ignored) {
                    setText(r, 3, st.textCenter, l.fecha_vencimiento);
                }
            } else {
                setText(r, 3, st.textCenter, "");
            }

            setDouble(r, 4, st.currency, l.precio_compra != null ? l.precio_compra.doubleValue() : 0d);

            String rowExcel = String.valueOf(rowStart + i + 1);
            formula(r, 5, st.textCenter, "IF(" + col(3) + rowExcel + "<TODAY(),\"VENCIDO\",\"VIGENTE\")");

            fillRowStyles(r, ((rowStart + i) % 2 == 0) ? st.zebra1 : st.zebra2, headers.length);
        }

        // Validación: Código Producto contra rango de códigos en Productos
        int productosRowStart = 2;
        int productosRowEnd = productosRowStart + Math.max(1, productsCount) - 1;
        String codigosRange = "'Productos'!$A$" + (productosRowStart + 1) + ":$A$" + (productosRowEnd + 1);
        addListValidationFormula(sheet, 0, rowStart, rowStart + Math.max(1, lots.size()) - 1, codigosRange);

        setupSheetCommon(sheet, 1, lastCol);
        autosizeColumns(sheet, headers.length, new int[]{16, 10, 12, 16, 14, 12});

        protectFormulas(sheet, rowStart, rowStart + Math.max(1, lots.size()) - 1, new int[]{5});
        sheet.protectSheet("quantify");
    }

    private static void buildDashboardSheet(XSSFWorkbook wb, XSSFSheet sheet, Styles st, int productsCount) {
        Row title = sheet.createRow(0);
        Cell t = title.createCell(0);
        t.setCellValue("Dashboard de Inventario");
        t.setCellStyle(st.title);
        sheet.addMergedRegion(new CellRangeAddress(0,0,0,8));
        setRegionBorder(sheet, 0,0,0,8);

        Row dateRow = sheet.createRow(1);
        Cell d1 = dateRow.createCell(0); d1.setCellValue("Última actualización:"); d1.setCellStyle(st.text);
        Cell d2 = dateRow.createCell(1); d2.setCellFormula("TODAY()"); d2.setCellStyle(st.date);

        int baseRow = 3;

        Row k1 = sheet.createRow(baseRow);
        k1.createCell(0).setCellValue("Productos activos"); k1.getCell(0).setCellStyle(st.text);
        Cell k1v = k1.createCell(1); k1v.setCellFormula("COUNTIF(Productos!C:C,\"Sí\")"); k1v.setCellStyle(st.integer);

        Row k2 = sheet.createRow(baseRow+1);
        k2.createCell(0).setCellValue("Valor total inventario"); k2.getCell(0).setCellStyle(st.text);
        Cell k2v = k2.createCell(1); k2v.setCellFormula("SUM(Productos!N:N)"); k2v.setCellStyle(st.currency);

        int prodStart = 3; // fila de datos en “Productos”
        int prodEnd = prodStart + Math.max(1, productsCount) - 1;
        Row k3 = sheet.createRow(baseRow+2);
        k3.createCell(0).setCellValue("Por debajo de stock mínimo"); k3.getCell(0).setCellStyle(st.text);
        Cell k3v = k3.createCell(1);
        k3v.setCellFormula("SUMPRODUCT(--(Productos!K" + prodStart + ":K" + prodEnd + "<Productos!L" + prodStart + ":L" + prodEnd + "))");
        k3v.setCellStyle(st.integer);

        Row k4 = sheet.createRow(baseRow+3);
        k4.createCell(0).setCellValue("Vencidos o próximos a vencer (<=30 días)"); k4.getCell(0).setCellStyle(st.text);
        Cell k4v = k4.createCell(1);
        k4v.setCellFormula("COUNTIF(Productos!R" + prodStart + ":R" + prodEnd + ",\"<=\"&TODAY()+30)");
        k4v.setCellStyle(st.integer);

        int tableCatRow = baseRow + 6;
        Row thCat = sheet.createRow(tableCatRow);
        thCat.createCell(0).setCellValue("Categoría"); thCat.getCell(0).setCellStyle(st.header);
        thCat.createCell(1).setCellValue("Valor inventario"); thCat.getCell(1).setCellStyle(st.header);

        XSSFSheet listas = wb.getSheet("Listas");
        int catCount = countListLength(listas, 0);
        for (int i = 0; i < catCount; i++) {
            Row r = sheet.createRow(tableCatRow + 1 + i);
            Cell cCat = r.createCell(0);
            cCat.setCellFormula("Listas!A" + (2 + i));
            cCat.setCellStyle(st.text);

            Cell cVal = r.createCell(1);
            String cCatRef = ref(cCat);
            cVal.setCellFormula("SUMIF(Productos!D:D," + cCatRef + ",Productos!N:N)");
            cVal.setCellStyle(st.currency);
        }

        XSSFDrawing drawing = sheet.createDrawingPatriarch();
        XSSFClientAnchor barAnchor = drawing.createAnchor(0, 0, 0, 0, 3, tableCatRow, 9, tableCatRow + 14);
        XSSFChart barChart = drawing.createChart(barAnchor);
        barChart.setTitleText("Valor de inventario por categoría");
        barChart.setTitleOverlay(false);
        XDDFChartLegend legend = barChart.getOrAddLegend();
        legend.setPosition(LegendPosition.TOP_RIGHT);

        XDDFCategoryAxis catAxis = barChart.createCategoryAxis(AxisPosition.BOTTOM);
        XDDFValueAxis valAxis = barChart.createValueAxis(AxisPosition.LEFT);
        valAxis.setCrosses(AxisCrosses.AUTO_ZERO);

        XDDFDataSource<String> xs = XDDFDataSourcesFactory.fromStringCellRange(sheet,
                new CellRangeAddress(tableCatRow + 1, tableCatRow + catCount, 0, 0));
        XDDFNumericalDataSource<Double> ys = XDDFDataSourcesFactory.fromNumericCellRange(sheet,
                new CellRangeAddress(tableCatRow + 1, tableCatRow + catCount, 1, 1));

        XDDFBarChartData data = (XDDFBarChartData) barChart.createData(ChartTypes.BAR, catAxis, valAxis);
        data.setBarDirection(BarDirection.COL);
        XDDFBarChartData.Series series = (XDDFBarChartData.Series) data.addSeries(xs, ys);
        series.setTitle("Valor por categoría", null);
        barChart.plot(data);

        int tableLabCol = 11;
        int tableLabRow = tableCatRow;
        Row thLab = sheet.getRow(tableLabRow);
        if (thLab == null) thLab = sheet.createRow(tableLabRow);
        Cell hl0 = thLab.createCell(tableLabCol);
        hl0.setCellValue("Laboratorio"); hl0.setCellStyle(st.header);
        Cell hl1 = thLab.createCell(tableLabCol+1);
        hl1.setCellValue("Valor inventario"); hl1.setCellStyle(st.header);

        int labCount = countListLength(listas, 1);
        for (int i = 0; i < labCount; i++) {
            Row r = sheet.getRow(tableLabRow + 1 + i);
            if (r == null) r = sheet.createRow(tableLabRow + 1 + i);
            Cell cLab = r.createCell(tableLabCol);
            cLab.setCellFormula("Listas!B" + (2 + i));
            cLab.setCellStyle(st.text);

            Cell cVal = r.createCell(tableLabCol + 1);
            String cLabRef = ref(cLab);
            cVal.setCellFormula("SUMIF(Productos!G:G," + cLabRef + ",Productos!N:N)");
            cVal.setCellStyle(st.currency);
        }

        XSSFClientAnchor pieAnchor = drawing.createAnchor(0, 0, 0, 0, tableLabCol, tableLabRow + 1, tableLabCol + 6, tableLabRow + 15);
        XSSFChart pieChart = drawing.createChart(pieAnchor);
        pieChart.setTitleText("Distribución por laboratorio");
        pieChart.setTitleOverlay(false);

        XDDFDataSource<String> pCats = XDDFDataSourcesFactory.fromStringCellRange(sheet,
                new CellRangeAddress(tableLabRow + 1, tableLabRow + labCount, tableLabCol, tableLabCol));
        XDDFNumericalDataSource<Double> pVals = XDDFDataSourcesFactory.fromNumericCellRange(sheet,
                new CellRangeAddress(tableLabRow + 1, tableLabRow + labCount, tableLabCol + 1, tableLabCol + 1));

        XDDFPieChartData pieData = (XDDFPieChartData) pieChart.createData(ChartTypes.PIE, null, null);
        XDDFPieChartData.Series pSeries = (XDDFPieChartData.Series) pieData.addSeries(pCats, pVals);
        pSeries.setTitle("Laboratorios", null);
        pieChart.plot(pieData);

        autosizeColumns(sheet, 20, new int[]{18, 14});
        sheet.setDisplayGridlines(false);
    }

    private static int countListLength(XSSFSheet listas, int colIndex) {
        int r = 1; // desde fila 2
        while (true) {
            Row row = listas.getRow(r);
            if (row == null) break;
            Cell c = row.getCell(colIndex);
            if (c == null || c.getCellType() == CellType.BLANK) break;
            r++;
        }
        return r - 1;
    }

    private static void addListValidation(Sheet sheet, int col, int rowStart, int rowEnd, String namedRange) {
        DataValidationHelper dvh = sheet.getDataValidationHelper();
        DataValidationConstraint dvc = dvh.createFormulaListConstraint(namedRange);
        CellRangeAddressList regions = new CellRangeAddressList(rowStart, rowEnd, col, col);
        DataValidation dv = dvh.createValidation(dvc, regions);
        dv.setShowErrorBox(true);
        sheet.addValidationData(dv);
    }

    private static void addListValidationFormula(Sheet sheet, int col, int rowStart, int rowEnd, String formulaRange) {
        DataValidationHelper dvh = sheet.getDataValidationHelper();
        DataValidationConstraint dvc = dvh.createFormulaListConstraint(formulaRange);
        CellRangeAddressList regions = new CellRangeAddressList(rowStart, rowEnd, col, col);
        DataValidation dv = dvh.createValidation(dvc, regions);
        dv.setShowErrorBox(true);
        sheet.addValidationData(dv);
    }

    private static void addDecimalValidation(Sheet sheet, int col, int rowStart, int rowEnd, double min, double max) {
        DataValidationHelper dvh = sheet.getDataValidationHelper();
        DataValidationConstraint dvc = dvh.createDecimalConstraint(DataValidationConstraint.OperatorType.BETWEEN, String.valueOf(min), String.valueOf(max));
        CellRangeAddressList regions = new CellRangeAddressList(rowStart, rowEnd, col, col);
        DataValidation dv = dvh.createValidation(dvc, regions);
        dv.setShowErrorBox(true);
        sheet.addValidationData(dv);
    }

    private static void addIntegerValidation(Sheet sheet, int col, int rowStart, int rowEnd, int min, int max) {
        DataValidationHelper dvh = sheet.getDataValidationHelper();
        DataValidationConstraint dvc = dvh.createIntegerConstraint(DataValidationConstraint.OperatorType.BETWEEN, String.valueOf(min), String.valueOf(max));
        CellRangeAddressList regions = new CellRangeAddressList(rowStart, rowEnd, col, col);
        DataValidation dv = dvh.createValidation(dvc, regions);
        dv.setShowErrorBox(true);
        sheet.addValidationData(dv);
    }

    private static void addStockConditionalFormatting(Sheet sheet, int rowStart, int rowEnd, int colStock, int colMin) {
        SheetConditionalFormatting scf = sheet.getSheetConditionalFormatting();
        ConditionalFormattingRule r1 = scf.createConditionalFormattingRule("$" + col(colStock) + (rowStart+1) + "<$" + col(colMin) + (rowStart+1));
        PatternFormatting f1 = r1.createPatternFormatting();
        f1.setFillBackgroundColor(IndexedColors.ROSE.getIndex());
        f1.setFillPattern(PatternFormatting.SOLID_FOREGROUND);

        ConditionalFormattingRule r2 = scf.createConditionalFormattingRule("$" + col(colStock) + (rowStart+1) + "=$" + col(colMin) + (rowStart+1));
        PatternFormatting f2 = r2.createPatternFormatting();
        f2.setFillBackgroundColor(IndexedColors.LIGHT_YELLOW.getIndex());
        f2.setFillPattern(PatternFormatting.SOLID_FOREGROUND);

        ConditionalFormattingRule r3 = scf.createConditionalFormattingRule("$" + col(colStock) + (rowStart+1) + ">$" + col(colMin) + (rowStart+1));
        PatternFormatting f3 = r3.createPatternFormatting();
        f3.setFillBackgroundColor(IndexedColors.LIGHT_GREEN.getIndex());
        f3.setFillPattern(PatternFormatting.SOLID_FOREGROUND);

        CellRangeAddress[] regions = { new CellRangeAddress(rowStart, rowEnd, 0, COL_ULT_ACT) };
        scf.addConditionalFormatting(regions, r1);
    }

    private static void addVencimientoConditionalFormatting(Sheet sheet, int rowStart, int rowEnd, int colFecha) {
        SheetConditionalFormatting scf = sheet.getSheetConditionalFormatting();
        ConditionalFormattingRule r = scf.createConditionalFormattingRule("$" + col(colFecha) + (rowStart+1) + "<=TODAY()+30");
        PatternFormatting pf = r.createPatternFormatting();
        pf.setFillBackgroundColor(IndexedColors.RED.getIndex());
        pf.setFillPattern(PatternFormatting.SOLID_FOREGROUND);
        CellRangeAddress[] regions = { new CellRangeAddress(rowStart, rowEnd, colFecha, colFecha) };
        scf.addConditionalFormatting(regions, r);
    }

    private static void protectFormulas(Sheet sheet, int rowStart, int rowEnd, int[] formulaCols) {
        // Desbloquear todo y bloquear solo columnas de fórmulas
        for (int r = rowStart; r <= rowEnd; r++) {
            Row row = sheet.getRow(r);
            if (row == null) continue;

            // Desbloquear todas las celdas de la fila
            for (int c = 0; c <= COL_ULT_ACT; c++) {
                Cell cell = row.getCell(c);
                if (cell == null) continue;
                CellStyle st = cell.getCellStyle();
                if (st == null) st = sheet.getWorkbook().createCellStyle();
                st.setLocked(false);
                cell.setCellStyle(st);
            }

            // Bloquear solo fórmulas
            for (int fc : formulaCols) {
                Cell cell = row.getCell(fc);
                if (cell == null) cell = row.createCell(fc);
                CellStyle st = cell.getCellStyle();
                if (st == null) st = sheet.getWorkbook().createCellStyle();
                st.setLocked(true);
                cell.setCellStyle(st);
            }
        }
    }

    private static void setRegionBorder(Sheet sheet, int firstRow, int lastRow, int firstCol, int lastCol) {
        CellRangeAddress region = new CellRangeAddress(firstRow, lastRow, firstCol, lastCol);
        RegionUtil.setBorderTop(BorderStyle.THIN, region, sheet);
        RegionUtil.setBorderBottom(BorderStyle.THIN, region, sheet);
        RegionUtil.setBorderLeft(BorderStyle.THIN, region, sheet);
        RegionUtil.setBorderRight(BorderStyle.THIN, region, sheet);
    }


    private static void setText(Row r, int col, CellStyle st, String v) {
        Cell c = r.createCell(col);
        c.setCellValue(v == null ? "" : v);
        c.setCellStyle(st);
    }

    private static void setNumber(Row r, int col, CellStyle st, Number v) {
        Cell c = r.createCell(col);
        if (v == null) c.setBlank();
        else c.setCellValue(v.doubleValue());
        c.setCellStyle(st);
    }

    private static void setDouble(Row r, int col, CellStyle st, double v) {
        Cell c = r.createCell(col);
        c.setCellValue(v);
        c.setCellStyle(st);
    }

    private static void formula(Row r, int col, CellStyle st, String f) {
        Cell c = r.createCell(col);
        c.setCellFormula(f);
        c.setCellStyle(st);
    }

    private static String nvl(String s) { return s == null ? "" : s; }

    private static String col(int idx) {
        return CellReference.convertNumToColString(idx);
    }

    private static String ref(Cell cell) {
        return new CellReference(cell.getRowIndex(), cell.getColumnIndex()).formatAsString();
    }
}