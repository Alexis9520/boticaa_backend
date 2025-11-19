package quantify.BoticaSaid.controller.reports;

import quantify.BoticaSaid.dto.reports.InventoryDtos.InventoryLotDto;
import quantify.BoticaSaid.dto.reports.InventoryDtos.InventoryProductFullDto;
import quantify.BoticaSaid.dto.reports.PageResponse;
import quantify.BoticaSaid.dto.reports.CustomerDtos.TopCustomerDto;
import quantify.BoticaSaid.dto.reports.SalesByHourDto;
import quantify.BoticaSaid.dto.reports.CajaSummaryDto;
import quantify.BoticaSaid.dto.reports.ReportDtos.PaymentMixDto;
import quantify.BoticaSaid.dto.reports.ReportDtos.SalesByDayDto;
import quantify.BoticaSaid.dto.reports.ReportDtos.SalesSummaryDto;
import quantify.BoticaSaid.dto.reports.ReportDtos.TopProductDto;
import quantify.BoticaSaid.service.reports.ReportsService;
import quantify.BoticaSaid.service.reports.ExcelStyleUtil;
import quantify.BoticaSaid.service.reports.InventoryProfessionalExporter;

import jakarta.servlet.http.HttpServletResponse;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.streaming.SXSSFSheet;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/reports")
public class ReportsController {

    private final ReportsService svc;

    public ReportsController(ReportsService svc) {
        this.svc = svc;
    }

    // ===== Ventas (JSON) =====
    @GetMapping("/sales/summary")
    public SalesSummaryDto salesSummary(@RequestParam(required = false) String from,
                                        @RequestParam(required = false) String to) {
        return svc.getSalesSummary(from, to);
    }

    @GetMapping("/sales/by-day")
    public List<SalesByDayDto> salesByDay(@RequestParam(required = false) String from,
                                          @RequestParam(required = false) String to) {
        return svc.getSalesByDay(from, to);
    }

    @GetMapping("/sales/by-hour")
    public List<SalesByHourDto> salesByHour(@RequestParam(required = false) String from,
                                            @RequestParam(required = false) String to) {
        return svc.getSalesByHour(from, to);
    }

    @GetMapping("/sales/top-products")
    public List<TopProductDto> topProducts(@RequestParam(required = false) String from,
                                           @RequestParam(required = false) String to,
                                           @RequestParam(defaultValue = "10") int limit) {
        return svc.getTopProducts(from, to, limit);
    }

    @GetMapping("/sales/payment-mix")
    public List<PaymentMixDto> paymentMix(@RequestParam(required = false) String from,
                                          @RequestParam(required = false) String to) {
        return svc.getPaymentMix(from, to);
    }

    // ===== Ventas export (XLSX, con estilos) =====
    @GetMapping(value = "/sales/export", produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    public void exportSales(@RequestParam(required = false) String from,
                            @RequestParam(required = false) String to,
                            @RequestParam(defaultValue = "day") String group_by,
                            HttpServletResponse res) throws Exception {

        setDownloadHeaders(res, "ventas_" + group_by + ".xlsx");

        try (SXSSFWorkbook wb = new SXSSFWorkbook(200)) {
            var styles = ExcelStyleUtil.createStyles(wb);
            var sheet = wb.createSheet("Ventas");
            ExcelStyleUtil.trackAutosizeIfSXSSF(sheet);

            String[] headers;
            if ("product".equalsIgnoreCase(group_by)) {
                headers = new String[]{"Código", "Producto", "Categoría", "Unidades", "Ventas"};
            } else {
                headers = new String[]{"Fecha", "Tickets", "Unidades", "Ventas", "Ticket Prom.", "UPT"};
            }
            int lastCol = headers.length - 1;

            ExcelStyleUtil.mergeTitle(sheet, 0, lastCol, styles.title, "Reporte de Ventas");
            Row h = sheet.createRow(1);
            for (int i = 0; i < headers.length; i++) {
                Cell c = h.createCell(i);
                c.setCellValue(headers[i]);
                c.setCellStyle(styles.header);
            }
            ExcelStyleUtil.setRowHeights(sheet.getRow(0), h);

            int r = 2;
            if ("product".equalsIgnoreCase(group_by)) {
                var rows = svc.getTopProducts(from, to, 10_000);
                for (var it : rows) {
                    Row rr = sheet.createRow(r++);
                    int c = 0;
                    Cell cc;
                    cc = rr.createCell(c++); cc.setCellValue(nvl(it.codigo_barras)); cc.setCellStyle(styles.text);
                    cc = rr.createCell(c++); cc.setCellValue(nvl(it.nombre));        cc.setCellStyle(styles.text);
                    cc = rr.createCell(c++); cc.setCellValue(nvl(it.categoria));     cc.setCellStyle(styles.text);
                    cc = rr.createCell(c++); cc.setCellValue(it.unidades);           cc.setCellStyle(styles.integer);
                    cc = rr.createCell(c++); cc.setCellValue(nbd(it.ventas));         cc.setCellStyle(styles.currency);
                    ExcelStyleUtil.fillRowStyles(rr, (r % 2 == 0) ? styles.zebra1 : styles.zebra2, headers.length);
                }
            } else {
                var rows = svc.getSalesByDay(from, to);
                for (var it : rows) {
                    Row rr = sheet.createRow(r++);
                    int c = 0;
                    Cell cc;
                    cc = rr.createCell(c++); cc.setCellValue(nvl(it.fecha));            cc.setCellStyle(styles.text);
                    cc = rr.createCell(c++); cc.setCellValue(it.tickets);               cc.setCellStyle(styles.integer);
                    cc = rr.createCell(c++); cc.setCellValue(it.unidades);              cc.setCellStyle(styles.integer);
                    cc = rr.createCell(c++); cc.setCellValue(nbd(it.ventas));           cc.setCellStyle(styles.currency);
                    cc = rr.createCell(c++); cc.setCellValue(nbd(it.ticket_promedio));  cc.setCellStyle(styles.currency);
                    cc = rr.createCell(c++); cc.setCellValue(nbd(it.upt));              cc.setCellStyle(styles.number);
                    ExcelStyleUtil.fillRowStyles(rr, (r % 2 == 0) ? styles.zebra1 : styles.zebra2, headers.length);
                }
            }

            ExcelStyleUtil.setupSheetCommon(sheet, 1, lastCol);
            ExcelStyleUtil.autosizeColumns(sheet, headers.length, new int[]{12, 26, 18, 12, 14, 12});

            wb.write(res.getOutputStream());
            wb.dispose();
        }
    }

    // ===== Caja (JSON) =====
    @GetMapping("/caja/summary")
    public CajaSummaryDto cajaSummary(@RequestParam(required = false) String from,
                                      @RequestParam(required = false) String to) {
        return svc.getCajaSummary(from, to);
    }

    // ===== Inventario FULL (JSON) =====
    @GetMapping("/inventory/full")
    public PageResponse<InventoryProductFullDto> inventoryFull(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String categoria,
            @RequestParam(required = false) Boolean activo,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size,
            @RequestParam(defaultValue = "nombre") String sort,
            @RequestParam(defaultValue = "asc") String dir
    ) {
        return svc.getInventoryFull(search, categoria, activo, page, size, sort, dir);
    }

    @GetMapping("/inventory/lots")
    public List<InventoryLotDto> inventoryLots(@RequestParam String codigo_barras) {
        return svc.getInventoryLotsByProduct(codigo_barras);
    }

    // ===== Inventario export (resumen por scope) con estilos =====
    @GetMapping(value = "/inventory/export", produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    public void exportInventory(@RequestParam(defaultValue = "all") String scope,
                                @RequestParam(defaultValue = "30") int days,
                                HttpServletResponse res) throws Exception {
        var rows = svc.getInventoryForExport(scope, days);
        setDownloadHeaders(res, "inventario_" + scope + ".xlsx");

        try (SXSSFWorkbook wb = new SXSSFWorkbook(200)) {
            var styles = ExcelStyleUtil.createStyles(wb);
            var sheet = wb.createSheet("Inventario");
            ExcelStyleUtil.trackAutosizeIfSXSSF(sheet);

            String[] headers = {
                    "Código","Nombre","Categoría","Presentación","Laboratorio",
                    "Stock total","Stock mínimo","Lotes","Lotes vencidos","Próx. venc.",
                    "Precio UND","Precio BL","Costo prom.","Valor compra total","Activo"
            };
            int lastCol = headers.length - 1;

            ExcelStyleUtil.mergeTitle(sheet, 0, lastCol, styles.title, "Inventario (resumen: " + scope + ")");
            Row h = sheet.createRow(1);
            for (int i = 0; i < headers.length; i++) {
                Cell c = h.createCell(i);
                c.setCellValue(headers[i]);
                c.setCellStyle(styles.header);
            }
            ExcelStyleUtil.setRowHeights(sheet.getRow(0), h);

            int r = 2;
            for (var p : rows) {
                Row rr = sheet.createRow(r++);
                int c = 0;
                Cell cc;
                cc = rr.createCell(c++); cc.setCellValue(nvl(p.codigo_barras));          cc.setCellStyle(styles.textCenter);
                cc = rr.createCell(c++); cc.setCellValue(nvl(p.nombre));                  cc.setCellStyle(styles.text);
                cc = rr.createCell(c++); cc.setCellValue(nvl(p.categoria));               cc.setCellStyle(styles.text);
                cc = rr.createCell(c++); cc.setCellValue(nvl(p.presentacion));            cc.setCellStyle(styles.text);
                cc = rr.createCell(c++); cc.setCellValue(nvl(p.laboratorio));             cc.setCellStyle(styles.text);
                cc = rr.createCell(c++); cc.setCellValue(p.stock_total);                  cc.setCellStyle(styles.integer);
                cc = rr.createCell(c++); cc.setCellValue(n(p.cantidad_minima));           cc.setCellStyle(styles.integer);
                cc = rr.createCell(c++); cc.setCellValue(n(p.lotes));                     cc.setCellStyle(styles.integer);
                cc = rr.createCell(c++); cc.setCellValue(n(p.lotes_vencidos));            cc.setCellStyle(styles.integer);
                cc = rr.createCell(c++); cc.setCellValue(nvl(p.proximo_vencimiento));     cc.setCellStyle(styles.textCenter);
                cc = rr.createCell(c++); cc.setCellValue(nbd(p.precio_venta_und));        cc.setCellStyle(styles.currency);
                cc = rr.createCell(c++); cc.setCellValue(nbd(p.precio_venta_blister));    cc.setCellStyle(styles.currency);
                cc = rr.createCell(c++); cc.setCellValue(nbd(p.costo_promedio));          cc.setCellStyle(styles.currency);
                cc = rr.createCell(c++); cc.setCellValue(nbd(p.valor_compra_total));      cc.setCellStyle(styles.currency);
                cc = rr.createCell(c++); cc.setCellValue(p.activo == null ? "" : (p.activo ? "Sí" : "No")); cc.setCellStyle(styles.textCenter);

                ExcelStyleUtil.fillRowStyles(rr, (r % 2 == 0) ? styles.zebra1 : styles.zebra2, headers.length);
            }

            ExcelStyleUtil.setupSheetCommon(sheet, 1, lastCol);
            ExcelStyleUtil.autosizeColumns(sheet, headers.length, new int[]{12, 28, 18, 18, 18, 12, 12, 10, 12, 14, 12, 12, 12, 16, 10});

            wb.write(res.getOutputStream());
            wb.dispose();
        }
    }

    // ===== Inventario export FULL (dos hojas) con estilos =====
    @GetMapping(value = "/inventory/export-full", produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    public void exportInventoryFull(@RequestParam(required = false) String search,
                                    @RequestParam(required = false) String categoria,
                                    @RequestParam(required = false) Boolean activo,
                                    HttpServletResponse res) throws Exception {

        var page = svc.getInventoryFull(search, categoria, activo, 0, Integer.MAX_VALUE, "nombre", "asc");
        var products = page.content;

        var allLots = new java.util.ArrayList<InventoryLotDto>();
        for (var p : products) {
            allLots.addAll(svc.getInventoryLotsByProduct(p.codigo_barras));
        }

        setDownloadHeaders(res, "inventario_full.xlsx");

        try (SXSSFWorkbook wb = new SXSSFWorkbook(200)) {
            var styles = ExcelStyleUtil.createStyles(wb);

            // Hoja Productos
            var sheet1 = wb.createSheet("Productos");
            ExcelStyleUtil.trackAutosizeIfSXSSF(sheet1);

            String[] headers1 = {
                    "Código","Nombre","Activo","Categoría","Concentración","Presentación","Laboratorio",
                    "Principio activo","Tipo medicamento",
                    "Cant. unidades blister","Cantidad general","Stock mínimo",
                    "Precio UND","Precio BL","Descuento",
                    "Stock total","Lotes","Lotes vencidos","Próx. venc.","Valor compra total","Costo promedio"
            };
            int lastCol1 = headers1.length - 1;

            ExcelStyleUtil.mergeTitle(sheet1, 0, lastCol1, styles.title, "Inventario - Productos");
            Row h1 = sheet1.createRow(1);
            for (int i = 0; i < headers1.length; i++) {
                Cell c = h1.createCell(i);
                c.setCellValue(headers1[i]);
                c.setCellStyle(styles.header);
            }
            ExcelStyleUtil.setRowHeights(sheet1.getRow(0), h1);

            int r1 = 2;
            for (var p : products) {
                Row rr = sheet1.createRow(r1++);
                int c = 0;
                Cell cc;
                cc = rr.createCell(c++); cc.setCellValue(nvl(p.codigo_barras));        cc.setCellStyle(styles.textCenter);
                cc = rr.createCell(c++); cc.setCellValue(nvl(p.nombre));                cc.setCellStyle(styles.text);
                cc = rr.createCell(c++); cc.setCellValue(p.activo == null ? "" : (p.activo ? "Sí" : "No")); cc.setCellStyle(styles.textCenter);
                cc = rr.createCell(c++); cc.setCellValue(nvl(p.categoria));             cc.setCellStyle(styles.text);
                cc = rr.createCell(c++); cc.setCellValue(nvl(p.concentracion));         cc.setCellStyle(styles.text);
                cc = rr.createCell(c++); cc.setCellValue(nvl(p.presentacion));          cc.setCellStyle(styles.text);
                cc = rr.createCell(c++); cc.setCellValue(nvl(p.laboratorio));           cc.setCellStyle(styles.text);
                cc = rr.createCell(c++); cc.setCellValue(nvl(p.principio_activo));      cc.setCellStyle(styles.text);
                cc = rr.createCell(c++); cc.setCellValue(nvl(p.tipo_medicamento));      cc.setCellStyle(styles.text);
                cc = rr.createCell(c++); cc.setCellValue(n(p.cantidad_unidades_blister)); cc.setCellStyle(styles.integer);
                cc = rr.createCell(c++); cc.setCellValue(n(p.cantidad_general));        cc.setCellStyle(styles.integer);
                cc = rr.createCell(c++); cc.setCellValue(n(p.cantidad_minima));         cc.setCellStyle(styles.integer);
                cc = rr.createCell(c++); cc.setCellValue(nbd(p.precio_venta_und));      cc.setCellStyle(styles.currency);
                cc = rr.createCell(c++); cc.setCellValue(nbd(p.precio_venta_blister));  cc.setCellStyle(styles.currency);
                cc = rr.createCell(c++); cc.setCellValue(nbd(p.descuento));             cc.setCellStyle(styles.currency);
                cc = rr.createCell(c++); cc.setCellValue(n(p.stock_total));             cc.setCellStyle(styles.integer);
                cc = rr.createCell(c++); cc.setCellValue(n(p.lotes));                   cc.setCellStyle(styles.integer);
                cc = rr.createCell(c++); cc.setCellValue(n(p.lotes_vencidos));          cc.setCellStyle(styles.integer);
                cc = rr.createCell(c++); cc.setCellValue(nvl(p.proximo_vencimiento));   cc.setCellStyle(styles.text);
                cc = rr.createCell(c++); cc.setCellValue(nbd(p.valor_compra_total));    cc.setCellStyle(styles.currency);
                cc = rr.createCell(c++); cc.setCellValue(nbd(p.costo_promedio));        cc.setCellStyle(styles.currency);

                ExcelStyleUtil.fillRowStyles(rr, (r1 % 2 == 0) ? styles.zebra1 : styles.zebra2, headers1.length);
            }

            ExcelStyleUtil.setupSheetCommon(sheet1, 1, lastCol1);
            ExcelStyleUtil.autosizeColumns(sheet1, headers1.length,
                    new int[]{12, 28, 8, 16, 16, 16, 18, 20, 14, 14, 14, 12, 12, 12, 12, 12, 10, 12, 14, 16, 14});

            // Hoja Lotes
            var sheet2 = wb.createSheet("Lotes");
            ExcelStyleUtil.trackAutosizeIfSXSSF(sheet2);

            String[] headers2 = {"Código","Lote","Unidades","Vencimiento","Compra","Estado"};
            int lastCol2 = headers2.length - 1;
            ExcelStyleUtil.mergeTitle(sheet2, 0, lastCol2, styles.title, "Inventario - Lotes");
            Row h2 = sheet2.createRow(1);
            for (int i = 0; i < headers2.length; i++) {
                Cell c = h2.createCell(i);
                c.setCellValue(headers2[i]);
                c.setCellStyle(styles.header);
            }
            ExcelStyleUtil.setRowHeights(sheet2.getRow(0), h2);

            int r2 = 2;
            for (var lt : allLots) {
                Row rr = sheet2.createRow(r2++);
                int c = 0;
                Cell cc;
                cc = rr.createCell(c++); cc.setCellValue(nvl(lt.codigo_barras));      cc.setCellStyle(styles.textCenter);
                cc = rr.createCell(c++); cc.setCellValue(n(lt.lote_id));               cc.setCellStyle(styles.integer);
                cc = rr.createCell(c++); cc.setCellValue(n(lt.cantidad_unidades));     cc.setCellStyle(styles.integer);
                cc = rr.createCell(c++); cc.setCellValue(nvl(lt.fecha_vencimiento));   cc.setCellStyle(styles.text);
                cc = rr.createCell(c++); cc.setCellValue(nbd(lt.precio_compra));       cc.setCellStyle(styles.currency);
                cc = rr.createCell(c++); cc.setCellValue(nvl(lt.estado));              cc.setCellStyle(styles.textCenter);

                ExcelStyleUtil.fillRowStyles(rr, (r2 % 2 == 0) ? styles.zebra1 : styles.zebra2, headers2.length);
            }

            ExcelStyleUtil.setupSheetCommon(sheet2, 1, lastCol2);
            ExcelStyleUtil.autosizeColumns(sheet2, headers2.length, new int[]{12, 10, 12, 14, 12, 12});

            wb.write(res.getOutputStream());
            wb.dispose();
        }
    }

    // ===== Inventario export PROFESSIONAL (Inventario_Botica.xlsx) =====
    @GetMapping(value = "/inventory/export-professional", produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    public void exportInventoryProfessional(@RequestParam(required = false) String search,
                                            @RequestParam(required = false) String categoria,
                                            @RequestParam(required = false) Boolean activo,
                                            HttpServletResponse res) throws Exception {
        var page = svc.getInventoryFull(search, categoria, activo, 0, Integer.MAX_VALUE, "nombre", "asc");
        var products = page.content;

        var allLots = new java.util.ArrayList<InventoryLotDto>();
        for (var p : products) {
            allLots.addAll(svc.getInventoryLotsByProduct(p.codigo_barras));
        }

        res.setHeader("Content-Type", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        res.setHeader("Content-Disposition", "attachment; filename=\"Inventario_Botica.xlsx\"");

        InventoryProfessionalExporter.exportInventarioProfesional(products, allLots, res.getOutputStream());
    }

    // ===== Clientes (JSON + export) =====
    @GetMapping("/customers/top")
    public List<TopCustomerDto> customersTop(@RequestParam(required = false) String from,
                                             @RequestParam(required = false) String to,
                                             @RequestParam(defaultValue = "20") int limit,
                                             @RequestParam(defaultValue = "ventas") String sortBy) {
        return svc.getTopCustomers(from, to, limit, sortBy);
    }

    @GetMapping(value = "/customers/export", produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    public void exportCustomers(@RequestParam(required = false) String from,
                                @RequestParam(required = false) String to,
                                HttpServletResponse res) throws Exception {
        var rows = svc.getTopCustomers(from, to, 10_000, "ventas");
        setDownloadHeaders(res, "clientes_top.xlsx");

        try (SXSSFWorkbook wb = new SXSSFWorkbook(200)) {
            var styles = ExcelStyleUtil.createStyles(wb);
            var sheet = wb.createSheet("Top clientes");
            ExcelStyleUtil.trackAutosizeIfSXSSF(sheet);

            String[] headers = {"DNI","Nombre","Tickets","Unidades","Ventas","Última compra"};
            int lastCol = headers.length - 1;

            ExcelStyleUtil.mergeTitle(sheet, 0, lastCol, styles.title, "Top Clientes");
            Row h = sheet.createRow(1);
            for (int i=0;i<headers.length;i++){
                Cell c = h.createCell(i);
                c.setCellValue(headers[i]);
                c.setCellStyle(styles.header);
            }
            ExcelStyleUtil.setRowHeights(sheet.getRow(0), h);

            int r=2;
            for (var cst : rows) {
                Row rr = sheet.createRow(r++);
                int c=0;
                Cell cc;
                cc = rr.createCell(c++); cc.setCellValue(nvl(cst.dni));               cc.setCellStyle(styles.textCenter);
                cc = rr.createCell(c++); cc.setCellValue(nvl(cst.nombre));            cc.setCellStyle(styles.text);
                cc = rr.createCell(c++); cc.setCellValue(cst.tickets);                cc.setCellStyle(styles.integer);
                cc = rr.createCell(c++); cc.setCellValue(cst.unidades);               cc.setCellStyle(styles.integer);
                cc = rr.createCell(c++); cc.setCellValue(nbd(cst.ventas));            cc.setCellStyle(styles.currency);
                cc = rr.createCell(c++); cc.setCellValue(nvl(cst.ultima_compra));     cc.setCellStyle(styles.textCenter);

                ExcelStyleUtil.fillRowStyles(rr, (r % 2 == 0) ? styles.zebra1 : styles.zebra2, headers.length);
            }

            ExcelStyleUtil.setupSheetCommon(sheet, 1, lastCol);
            ExcelStyleUtil.autosizeColumns(sheet, headers.length, new int[]{12, 26, 12, 12, 14, 18});

            wb.write(res.getOutputStream());
            wb.dispose();
        }
    }

    // ===== Endpoint que recibe JSON con id =====
    @PostMapping("/receive-id")
    public org.springframework.http.ResponseEntity<?> receiveId(@RequestBody Map<String, Object> payload) {
        try {
            if (payload == null || !payload.containsKey("id")) {
                return org.springframework.http.ResponseEntity.badRequest()
                        .body(java.util.Map.of("error", "El campo 'id' es requerido"));
            }
            
            Object idValue = payload.get("id");
            return org.springframework.http.ResponseEntity.ok(java.util.Map.of(
                    "message", "ID recibido correctamente",
                    "id", idValue,
                    "received_at", java.time.LocalDateTime.now().toString()
            ));
        } catch (Exception e) {
            return org.springframework.http.ResponseEntity.badRequest()
                    .body(java.util.Map.of("error", e.getMessage()));
        }
    }

    // ===== Reportes de Lotes en formato JSON =====
    @GetMapping("/lotes-json")
    public org.springframework.http.ResponseEntity<?> reporteLotesJson(
            @RequestParam String fechaInicio,
            @RequestParam String fechaFin,
            @RequestParam(required = false) String proveedorID) {
        try {
            // Asegúrate de pasar los 3 argumentos aquí:
            var lotes = svc.getLotesReportByDateRange(fechaInicio, fechaFin, proveedorID);

            return org.springframework.http.ResponseEntity.ok(lotes);
        } catch (IllegalArgumentException e) {
            return org.springframework.http.ResponseEntity.badRequest()
                    .body(java.util.Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return org.springframework.http.ResponseEntity.internalServerError()
                    .body(java.util.Map.of("error", "Error interno: " + e.getMessage()));
        }
    }

/**
    // ===== Reportes de Lotes agregados por rango de fechas (Excel) =====
    @GetMapping(value = "/lotes", produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    public void reporteLotesExcel(
            @RequestParam String fechaInicio,
            @RequestParam String fechaFin,
            HttpServletResponse res) throws Exception {

        try {
            var lotes = svc.getLotesReportByDateRange(fechaInicio, fechaFin);
            setDownloadHeaders(res, "lotes_" + fechaInicio + "_" + fechaFin + ".xlsx");

            try (SXSSFWorkbook wb = new SXSSFWorkbook(200)) {
            var styles = ExcelStyleUtil.createStyles(wb);
            var sheet = wb.createSheet("Lotes");
            ExcelStyleUtil.trackAutosizeIfSXSSF(sheet);

            String[] headers = {
                    "Nombre del Producto", "Concentración", "Presentación", "Lote",
                    "Fecha Vencimiento", "Cantidad Inicial", "REG/SAN",
                    "CANTIDAD RECIBIDA", "CONDICIONES ALMACENAMIENTO",
                    "EMPAQUE MEDIATO", "EMPAQUE INMEDIATO", "TIPO ENVASE",
                    "ESTADO DEL ENVASE"
            };
            int lastCol = headers.length - 1;

            ExcelStyleUtil.mergeTitle(sheet, 0, lastCol, styles.title,
                    "Lotes del " + fechaInicio + " al " + fechaFin);
            Row h = sheet.createRow(1);
            for (int i = 0; i < headers.length; i++) {
                Cell c = h.createCell(i);
                c.setCellValue(headers[i]);
                c.setCellStyle(styles.header);
            }
            ExcelStyleUtil.setRowHeights(sheet.getRow(0), h);

            int r = 2;
            for (var lote : lotes) {
                Row rr = sheet.createRow(r++);
                int c = 0;
                Cell cc;
                cc = rr.createCell(c++); cc.setCellValue(nvl(lote.getNombreProducto()));        cc.setCellStyle(styles.text);
                cc = rr.createCell(c++); cc.setCellValue(nvl(lote.getConcentracion()));         cc.setCellStyle(styles.text);
                cc = rr.createCell(c++); cc.setCellValue(nvl(lote.getPresentacion()));          cc.setCellStyle(styles.text);
                cc = rr.createCell(c++); cc.setCellValue(n(lote.getStockId()));                 cc.setCellStyle(styles.integer);
                cc = rr.createCell(c++); cc.setCellValue(lote.getFechaVencimiento() == null ? "" : lote.getFechaVencimiento().toString()); cc.setCellStyle(styles.textCenter);
                cc = rr.createCell(c++); cc.setCellValue(n(lote.getCantidadInicial()));         cc.setCellStyle(styles.integer);
                // Empty columns
                cc = rr.createCell(c++); cc.setCellValue("");                                   cc.setCellStyle(styles.text);
                cc = rr.createCell(c++); cc.setCellValue("");                                   cc.setCellStyle(styles.text);
                cc = rr.createCell(c++); cc.setCellValue("");                                   cc.setCellStyle(styles.text);
                cc = rr.createCell(c++); cc.setCellValue("");                                   cc.setCellStyle(styles.text);
                cc = rr.createCell(c++); cc.setCellValue("");                                   cc.setCellStyle(styles.text);
                cc = rr.createCell(c++); cc.setCellValue("");                                   cc.setCellStyle(styles.text);
                cc = rr.createCell(c++); cc.setCellValue("");                                   cc.setCellStyle(styles.text);

                ExcelStyleUtil.fillRowStyles(rr, (r % 2 == 0) ? styles.zebra1 : styles.zebra2, headers.length);
            }

            ExcelStyleUtil.setupSheetCommon(sheet, 1, lastCol);
            ExcelStyleUtil.autosizeColumns(sheet, headers.length,
                    new int[]{28, 16, 16, 10, 16, 14, 12, 16, 22, 16, 16, 14, 18});

                wb.write(res.getOutputStream());
                wb.dispose();
            }
        } catch (IllegalArgumentException e) {
            res.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            res.setContentType("application/json");
            res.getWriter().write("{\"error\": \"" + e.getMessage() + "\"}");
        }
    }
*/
    // ===== Reporte de Proveedores con su lista de productos =====
    @GetMapping("/proveedores")
    public org.springframework.http.ResponseEntity<?> reporteProveedores() {
        try {
            var proveedores = svc.getProveedoresReport();
            return org.springframework.http.ResponseEntity.ok(proveedores);
        } catch (Exception e) {
            return org.springframework.http.ResponseEntity.badRequest()
                    .body(java.util.Map.of("error", e.getMessage()));
        }
    }

    // ===== Exportar productos por rango de fechas =====
    @GetMapping(value = "/productos/export", produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    public void exportProductos(
            @RequestParam String fechaInicio,
            @RequestParam String fechaFin,
            HttpServletResponse res) throws Exception {
        
        var productos = svc.getProductosByDateRange(fechaInicio, fechaFin);
        setDownloadHeaders(res, "productos_" + fechaInicio + "_" + fechaFin + ".xlsx");

        try (SXSSFWorkbook wb = new SXSSFWorkbook(200)) {
            var styles = ExcelStyleUtil.createStyles(wb);
            var sheet = wb.createSheet("Productos");
            ExcelStyleUtil.trackAutosizeIfSXSSF(sheet);

            String[] headers = {
                    "ID", "Código", "Nombre", "Categoría", "Laboratorio", 
                    "Concentración", "Presentación", "Stock", "Precio UND", 
                    "Fecha Creación"
            };
            int lastCol = headers.length - 1;

            ExcelStyleUtil.mergeTitle(sheet, 0, lastCol, styles.title, 
                    "Productos creados del " + fechaInicio + " al " + fechaFin);
            Row h = sheet.createRow(1);
            for (int i = 0; i < headers.length; i++) {
                Cell c = h.createCell(i);
                c.setCellValue(headers[i]);
                c.setCellStyle(styles.header);
            }
            ExcelStyleUtil.setRowHeights(sheet.getRow(0), h);

            int r = 2;
            for (var p : productos) {
                Row rr = sheet.createRow(r++);
                int c = 0;
                Cell cc;
                Object idObj = p.get("id");
                cc = rr.createCell(c++); cc.setCellValue(idObj instanceof Number ? ((Number)idObj).doubleValue() : 0); cc.setCellStyle(styles.integer);
                cc = rr.createCell(c++); cc.setCellValue(nvl((String)p.get("codigo_barras")));   cc.setCellStyle(styles.textCenter);
                cc = rr.createCell(c++); cc.setCellValue(nvl((String)p.get("nombre")));          cc.setCellStyle(styles.text);
                cc = rr.createCell(c++); cc.setCellValue(nvl((String)p.get("categoria")));       cc.setCellStyle(styles.text);
                cc = rr.createCell(c++); cc.setCellValue(nvl((String)p.get("laboratorio")));     cc.setCellStyle(styles.text);
                cc = rr.createCell(c++); cc.setCellValue(nvl((String)p.get("concentracion")));   cc.setCellStyle(styles.text);
                cc = rr.createCell(c++); cc.setCellValue(nvl((String)p.get("presentacion")));    cc.setCellStyle(styles.text);
                Object cantObj = p.get("cantidad_general");
                cc = rr.createCell(c++); cc.setCellValue(cantObj instanceof Number ? ((Number)cantObj).doubleValue() : 0); cc.setCellStyle(styles.integer);
                cc = rr.createCell(c++); cc.setCellValue(nbd((BigDecimal)p.get("precio_venta_und"))); cc.setCellStyle(styles.currency);
                cc = rr.createCell(c++); cc.setCellValue(nvl((String)p.get("fecha_creacion")));  cc.setCellStyle(styles.textCenter);

                ExcelStyleUtil.fillRowStyles(rr, (r % 2 == 0) ? styles.zebra1 : styles.zebra2, headers.length);
            }

            ExcelStyleUtil.setupSheetCommon(sheet, 1, lastCol);
            ExcelStyleUtil.autosizeColumns(sheet, headers.length, 
                    new int[]{8, 12, 28, 16, 18, 16, 16, 12, 12, 18});

            wb.write(res.getOutputStream());
            wb.dispose();
        }
    }

    // ===== Util =====
    private static void setDownloadHeaders(HttpServletResponse res, String filename) throws Exception {
        res.setHeader("Content-Type", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        String enc = URLEncoder.encode(filename, StandardCharsets.UTF_8).replace("+", "%20");
        res.setHeader("Content-Disposition", "attachment; filename*=UTF-8''" + enc);
    }
    private static String nvl(String s) { return s == null ? "" : s; }
    private static double nbd(java.math.BigDecimal b) { return b == null ? 0d : b.doubleValue(); }
    private static double n(Number n) { return n == null ? 0d : n.doubleValue(); }
}