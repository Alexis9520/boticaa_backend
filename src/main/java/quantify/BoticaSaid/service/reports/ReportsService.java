package quantify.BoticaSaid.service.reports;

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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.*;
import java.time.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class ReportsService {

    private static final Logger log = LoggerFactory.getLogger(ReportsService.class);

    private final JdbcTemplate jdbc;

    public ReportsService(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    // ========= Utilidades de fechas =========
    private static LocalDateTime parseIso(String s, boolean endOfDay) {
        if (s == null || s.isBlank()) return null;
        try { return LocalDateTime.ofInstant(Instant.parse(s), ZoneId.systemDefault()); } catch (Exception ignored) {}
        try { return OffsetDateTime.parse(s).toLocalDateTime(); } catch (Exception ignored) {}
        try { return LocalDateTime.parse(s); } catch (Exception ignored) {}
        LocalDate d = LocalDate.parse(s);
        return endOfDay ? d.atTime(23, 59, 59, 999_000_000) : d.atStartOfDay();
    }
    private static Timestamp tsStart(String from) {
        LocalDateTime ldt = parseIso(from, false);
        return ldt == null ? null : Timestamp.valueOf(ldt.withHour(0).withMinute(0).withSecond(0).withNano(0));
    }
    private static Timestamp tsEnd(String to) {
        LocalDateTime ldt = parseIso(to, true);
        return ldt == null ? null : Timestamp.valueOf(ldt.withHour(23).withMinute(59).withSecond(59).withNano(999_000_000));
    }

    // ========= Ventas =========
    public SalesSummaryDto getSalesSummary(String from, String to) {
        Timestamp f = tsStart(from);
        Timestamp t = tsEnd(to);
        String sql = """
      SELECT COUNT(DISTINCT b.id) AS tickets,
             COALESCE(SUM(b.total_compra),0) AS ventas,
             COALESCE(SUM(d.cantidad),0) AS unidades
      FROM boletas b
      JOIN detalles_boleta d ON d.boleta_id = b.id
      WHERE (? IS NULL OR b.fecha_venta >= ?)
        AND (? IS NULL OR b.fecha_venta <= ?)
      """;
        Map<String,Object> r = jdbc.queryForMap(sql, f,f,t,t);
        SalesSummaryDto dto = new SalesSummaryDto();
        dto.tickets = ((Number) r.getOrDefault("tickets", 0)).longValue();
        dto.ventas = (BigDecimal) r.getOrDefault("ventas", BigDecimal.ZERO);
        dto.unidades = ((Number) r.getOrDefault("unidades", 0)).longValue();
        dto.ticket_promedio = dto.tickets == 0 ? BigDecimal.ZERO :
                dto.ventas.divide(BigDecimal.valueOf(dto.tickets), 2, RoundingMode.HALF_UP);
        dto.upt = dto.tickets == 0 ? BigDecimal.ZERO :
                BigDecimal.valueOf(dto.unidades).divide(BigDecimal.valueOf(dto.tickets), 2, RoundingMode.HALF_UP);
        return dto;
    }

    public List<SalesByDayDto> getSalesByDay(String from, String to) {
        Timestamp f = tsStart(from);
        Timestamp t = tsEnd(to);
        String sql = """
      SELECT DATE(b.fecha_venta) AS fecha,
             COUNT(DISTINCT b.id) AS tickets,
             COALESCE(SUM(d.cantidad),0) AS unidades,
             COALESCE(SUM(b.total_compra),0) AS ventas
      FROM boletas b
      JOIN detalles_boleta d ON d.boleta_id = b.id
      WHERE (? IS NULL OR b.fecha_venta >= ?)
        AND (? IS NULL OR b.fecha_venta <= ?)
      GROUP BY DATE(b.fecha_venta)
      ORDER BY fecha
      """;
        return jdbc.query(sql, ps -> { ps.setTimestamp(1,f); ps.setTimestamp(2,f); ps.setTimestamp(3,t); ps.setTimestamp(4,t); },
                (rs,i) -> {
                    SalesByDayDto d = new SalesByDayDto();
                    Date date = rs.getDate("fecha");
                    d.fecha = date.toLocalDate().toString();
                    d.tickets = rs.getLong("tickets");
                    d.unidades = rs.getLong("unidades");
                    d.ventas = rs.getBigDecimal("ventas");
                    d.ticket_promedio = d.tickets == 0 ? BigDecimal.ZERO :
                            d.ventas.divide(BigDecimal.valueOf(d.tickets), 2, RoundingMode.HALF_UP);
                    d.upt = d.tickets == 0 ? BigDecimal.ZERO :
                            BigDecimal.valueOf(d.unidades).divide(BigDecimal.valueOf(d.tickets), 2, RoundingMode.HALF_UP);
                    return d;
                });
    }

    public List<SalesByHourDto> getSalesByHour(String from, String to) {
        Timestamp f = tsStart(from);
        Timestamp t = tsEnd(to);
        String sql = """
      SELECT HOUR(b.fecha_venta) AS hora,
             COUNT(DISTINCT b.id) AS tickets,
             COALESCE(SUM(b.total_compra),0) AS ventas
      FROM boletas b
      WHERE (? IS NULL OR b.fecha_venta >= ?)
        AND (? IS NULL OR b.fecha_venta <= ?)
      GROUP BY HOUR(b.fecha_venta)
      ORDER BY hora
      """;
        return jdbc.query(sql, ps -> { ps.setTimestamp(1,f); ps.setTimestamp(2,f); ps.setTimestamp(3,t); ps.setTimestamp(4,t); },
                (rs,i)->{
                    SalesByHourDto d = new SalesByHourDto();
                    d.hora = rs.getInt("hora");
                    d.tickets = rs.getLong("tickets");
                    d.ventas = rs.getBigDecimal("ventas");
                    return d;
                });
    }

    public List<TopProductDto> getTopProducts(String from, String to, int limit) {
        Timestamp f = tsStart(from);
        Timestamp t = tsEnd(to);
        String sql = """
      SELECT p.codigo_barras, p.nombre, p.categoria,
             COALESCE(SUM(d.cantidad),0) AS unidades,
             COALESCE(SUM(d.cantidad * d.precio_unitario),0) AS ventas
      FROM detalles_boleta d
      JOIN productos p ON p.id = d.producto_id
      JOIN boletas b ON b.id = d.boleta_id
      WHERE (? IS NULL OR b.fecha_venta >= ?)
        AND (? IS NULL OR b.fecha_venta <= ?)
      GROUP BY p.codigo_barras, p.nombre, p.categoria
      ORDER BY ventas DESC
      LIMIT ?
      """;
        return jdbc.query(sql, ps -> { ps.setTimestamp(1,f); ps.setTimestamp(2,f); ps.setTimestamp(3,t); ps.setTimestamp(4,t); ps.setInt(5, limit); },
                (rs,i)->{
                    TopProductDto d = new TopProductDto();
                    d.codigo_barras = rs.getString("codigo_barras");
                    d.nombre = rs.getString("nombre");
                    d.categoria = rs.getString("categoria");
                    d.unidades = rs.getLong("unidades");
                    d.ventas = rs.getBigDecimal("ventas");
                    return d;
                });
    }

    public List<PaymentMixDto> getPaymentMix(String from, String to) {
        Timestamp f = tsStart(from);
        Timestamp t = tsEnd(to);
        String sql = """
      SELECT m.nombre AS metodo_pago,
             COUNT(*) AS tickets,
             COALESCE(SUM(b.total_compra),0) AS total
      FROM metodo_pago m
      JOIN boletas b ON b.id = m.boleta_id
      WHERE (? IS NULL OR b.fecha_venta >= ?)
        AND (? IS NULL OR b.fecha_venta <= ?)
      GROUP BY m.nombre
      ORDER BY total DESC
      """;
        return jdbc.query(sql, ps -> { ps.setTimestamp(1,f); ps.setTimestamp(2,f); ps.setTimestamp(3,t); ps.setTimestamp(4,t); },
                (rs,i)->{
                    PaymentMixDto d = new PaymentMixDto();
                    d.metodo_pago = rs.getString("metodo_pago");
                    d.tickets = rs.getLong("tickets");
                    d.total = rs.getBigDecimal("total");
                    return d;
                });
    }

    // ========= Caja =========
    // Reemplazar el método getCajaSummary existente por este
    public CajaSummaryDto getCajaSummary(String from, String to) {
        Timestamp f = tsStart(from);
        Timestamp t = tsEnd(to);

        // 1) Ventas totales (boletas)
        String ventasSql = """
      SELECT COALESCE(SUM(b.total_compra),0) AS ventas
      FROM boletas b
      WHERE (? IS NULL OR b.fecha_venta >= ?)
        AND (? IS NULL OR b.fecha_venta <= ?)
      """;
        BigDecimal ingresosVentas = jdbc.queryForObject(ventasSql, new Object[]{f,f,t,t}, BigDecimal.class);
        if (ingresosVentas == null) ingresosVentas = BigDecimal.ZERO;

        // 2) Ventas en efectivo (por método de pago)
        String ventasEfectivoSql = """
      SELECT COALESCE(SUM(b.total_compra),0) AS ventas_efectivo
      FROM boletas b
      JOIN metodo_pago m ON m.boleta_id = b.id
      WHERE m.nombre = 'Efectivo'
        AND (? IS NULL OR b.fecha_venta >= ?)
        AND (? IS NULL OR b.fecha_venta <= ?)
      """;
        BigDecimal ventasEfectivo;
        try {
            ventasEfectivo = jdbc.queryForObject(ventasEfectivoSql, new Object[]{f,f,t,t}, BigDecimal.class);
            if (ventasEfectivo == null) ventasEfectivo = BigDecimal.ZERO;
        } catch (Exception e) {
            log.warn("getCajaSummary - error querying ventasEfectivo: {}", e.toString());
            ventasEfectivo = BigDecimal.ZERO;
        }

        // 3) Ingresos manuales (movimientos_efectivo tipo = 'INGRESO')
        String ingresosManualesSql = """
      SELECT COALESCE(SUM(m.monto),0) AS ingresos_manuales
      FROM movimientos_efectivo m
      WHERE m.tipo = 'INGRESO'
        AND (? IS NULL OR m.fecha >= ?)
        AND (? IS NULL OR m.fecha <= ?)
      """;
        BigDecimal ingresosManuales;
        try {
            ingresosManuales = jdbc.queryForObject(ingresosManualesSql, new Object[]{f,f,t,t}, BigDecimal.class);
            if (ingresosManuales == null) ingresosManuales = BigDecimal.ZERO;
        } catch (Exception e) {
            log.warn("getCajaSummary - error querying ingresosManuales: {}", e.toString());
            ingresosManuales = BigDecimal.ZERO;
        }

        // 4) Egresos (movimientos_efectivo tipo = 'EGRESO')
        String egresosSql = """
      SELECT COALESCE(SUM(m.monto),0) AS egresos
      FROM movimientos_efectivo m
      WHERE m.tipo = 'EGRESO'
        AND (? IS NULL OR m.fecha >= ?)
        AND (? IS NULL OR m.fecha <= ?)
      """;
        BigDecimal egresos;
        try {
            egresos = jdbc.queryForObject(egresosSql, new Object[]{f,f,t,t}, BigDecimal.class);
            if (egresos == null) egresos = BigDecimal.ZERO;
        } catch (Exception e) {
            log.warn("getCajaSummary - error querying egresos: {}", e.toString());
            egresos = BigDecimal.ZERO;
        }

        // 5) Efectivo inicial (opcional) - depende de si tienes tabla 'caja' con campo saldo_inicial
        BigDecimal efectivoInicial = BigDecimal.ZERO;
        try {
            // Ajusta la query si tu esquema es distinto; ejemplo ilustrativo:
            // efectivoInicial = jdbc.queryForObject("SELECT COALESCE(saldo_inicial,0) FROM caja WHERE id = ? LIMIT 1", BigDecimal.class, cajaId);
        } catch (Exception ignored) {}

        BigDecimal ingresosTotales = ingresosVentas.add(ingresosManuales);
        BigDecimal neto = ingresosTotales.subtract(egresos);

        log.debug("getCajaSummary -> ventas={}, ventasEfectivo={}, ingresosManuales={}, egresos={}, efectivoInicial={}, neto={}",
                ingresosVentas, ventasEfectivo, ingresosManuales, egresos, efectivoInicial, neto);

        CajaSummaryDto dto = new CajaSummaryDto();
        dto.ingresosVentas = ingresosVentas;
        dto.ingresosManuales = ingresosManuales;
        dto.ventasEfectivo = ventasEfectivo;
        dto.egresos = egresos;
        dto.neto = neto;
        dto.efectivoInicial = efectivoInicial;
        return dto;
    }

    // ========= Inventario FULL =========

    private static final List<String> SORT_WHITELIST = List.of(
            "codigo_barras","nombre","categoria","laboratorio","presentacion",
            "precio_venta_und","precio_venta_blister","cantidad_minima",
            "stock_total","lotes","lotes_vencidos","proximo_vencimiento","fecha_creacion","fecha_actualizacion"
    );

    private static String mapSortColumn(String sort) {
        if (sort == null) return "p.nombre";
        switch (sort) {
            case "codigo_barras":
            case "nombre":
            case "categoria":
            case "laboratorio":
            case "presentacion":
            case "precio_venta_und":
            case "precio_venta_blister":
            case "cantidad_minima":
            case "fecha_creacion":
            case "fecha_actualizacion":
                return "p." + sort;
            case "stock_total":
            case "lotes":
            case "lotes_vencidos":
            case "proximo_vencimiento":
                return sort;
            default:
                return "p.nombre";
        }
    }

    public PageResponse<InventoryProductFullDto> getInventoryFull(String search, String categoria, Boolean activo,
                                                                  int page, int size, String sort, String dir) {
        int offset = Math.max(0, page) * Math.max(1, size);
        int limit = Math.max(1, size);

        String sortColRaw = (sort != null && SORT_WHITELIST.contains(sort)) ? sort : "nombre";
        String sortCol = mapSortColumn(sortColRaw);
        String sortDir = ("desc".equalsIgnoreCase(dir)) ? "DESC" : "ASC";

        StringBuilder where = new StringBuilder(" WHERE 1=1 ");
        List<Object> args = new ArrayList<>();

        if (search != null && !search.isBlank()) {
            where.append(" AND (p.codigo_barras LIKE ? OR p.nombre LIKE ? OR p.laboratorio LIKE ? OR p.categoria LIKE ? OR p.principio_activo LIKE ?) ");
            String like = "%" + search.trim() + "%";
            args.add(like); args.add(like); args.add(like); args.add(like); args.add(like);
        }
        if (categoria != null && !categoria.isBlank()) {
            where.append(" AND p.categoria = ? ");
            args.add(categoria.trim());
        }
        if (activo != null) {
            where.append(" AND p.activo = ? ");
            args.add(activo ? 1 : 0); // BIT/TINYINT(1)
        }

        String countSql = "SELECT COUNT(*) FROM productos p " + where;
        long total = jdbc.queryForObject(countSql, args.toArray(), Long.class);

        String dataSql =
                """
                SELECT
                  p.codigo_barras,
                  p.cantidad_unidades_blister,
                  p.activo,
                  p.cantidad_general,
                  p.categoria,
                  p.concentracion,
                  p.descuento,
                  p.fecha_actualizacion,
                  p.fecha_creacion,
                  p.laboratorio,
                  p.nombre,
                  p.precio_venta_blister,
                  p.precio_venta_und,
                  p.cantidad_minima,
                  p.principio_activo,
                  p.tipo_medicamento,
                  p.presentacion,
                  COALESCE(SUM(s.cantidad_unidades),0) AS stock_total,
                  COUNT(s.id)                          AS lotes,
                  SUM(CASE WHEN s.fecha_vencimiento IS NOT NULL AND s.fecha_vencimiento < CURDATE() THEN 1 ELSE 0 END) AS lotes_vencidos,
                  MIN(s.fecha_vencimiento)             AS proximo_vencimiento,
                  SUM(COALESCE(s.cantidad_unidades,0) * COALESCE(s.precio_compra,0)) AS valor_compra_total,
                  CASE WHEN COALESCE(SUM(s.cantidad_unidades),0) > 0
                       THEN SUM(COALESCE(s.cantidad_unidades,0) * COALESCE(s.precio_compra,0)) / SUM(COALESCE(s.cantidad_unidades,0))
                       ELSE NULL END AS costo_promedio
                FROM productos p
                LEFT JOIN stock s ON s.producto_id = p.id
                """ +
                        where +
                        """
                        GROUP BY
                          p.codigo_barras, p.cantidad_unidades_blister, p.activo, p.cantidad_general, p.categoria,
                          p.concentracion, p.descuento, p.fecha_actualizacion, p.fecha_creacion, p.laboratorio,
                          p.nombre, p.precio_venta_blister, p.precio_venta_und, p.cantidad_minima, p.principio_activo,
                          p.tipo_medicamento, p.presentacion
                        """ +
                        " ORDER BY " + sortCol + " " + sortDir + " " +
                        " LIMIT ? OFFSET ? ";

        List<Object> dataArgs = new ArrayList<>(args);
        dataArgs.add(limit);
        dataArgs.add(offset);

        List<InventoryProductFullDto> content = jdbc.query(
                dataSql,
                dataArgs.toArray(),
                (rs, i) -> mapInventoryProductFull(rs)
        );

        return new PageResponse<>(content, total, page, size);
    }

    public List<InventoryProductFullDto> getInventoryForExport(String scope, int days) {
        StringBuilder where = new StringBuilder(" WHERE 1=1 ");
        List<Object> args = new ArrayList<>();

        String base =
                """
                SELECT
                  p.codigo_barras,
                  p.cantidad_unidades_blister,
                  p.activo,
                  p.cantidad_general,
                  p.categoria,
                  p.concentracion,
                  p.descuento,
                  p.fecha_actualizacion,
                  p.fecha_creacion,
                  p.laboratorio,
                  p.nombre,
                  p.precio_venta_blister,
                  p.precio_venta_und,
                  p.cantidad_minima,
                  p.principio_activo,
                  p.tipo_medicamento,
                  p.presentacion,
                  COALESCE(SUM(s.cantidad_unidades),0) AS stock_total,
                  COUNT(s.id)                          AS lotes,
                  SUM(CASE WHEN s.fecha_vencimiento IS NOT NULL AND s.fecha_vencimiento < CURDATE() THEN 1 ELSE 0 END) AS lotes_vencidos,
                  MIN(s.fecha_vencimiento)             AS proximo_vencimiento,
                  SUM(COALESCE(s.cantidad_unidades,0) * COALESCE(s.precio_compra,0)) AS valor_compra_total,
                  CASE WHEN COALESCE(SUM(s.cantidad_unidades),0) > 0
                       THEN SUM(COALESCE(s.cantidad_unidades,0) * COALESCE(s.precio_compra,0)) / SUM(COALESCE(s.cantidad_unidades,0))
                       ELSE NULL END AS costo_promedio
                FROM productos p
                LEFT JOIN stock s ON s.producto_id = p.id
                """;

        String groupBy =
                """
                GROUP BY
                  p.codigo_barras, p.cantidad_unidades_blister, p.activo, p.cantidad_general, p.categoria,
                  p.concentracion, p.descuento, p.fecha_actualizacion, p.fecha_creacion, p.laboratorio,
                  p.nombre, p.precio_venta_blister, p.precio_venta_und, p.cantidad_minima, p.principio_activo,
                  p.tipo_medicamento, p.presentacion
                """;

        String having = "";
        if ("low".equalsIgnoreCase(scope)) {
            having = " HAVING COALESCE(SUM(s.cantidad_unidades),0) <= COALESCE(p.cantidad_minima,0) ";
        } else if ("out-of-stock".equalsIgnoreCase(scope)) {
            having = " HAVING COALESCE(SUM(s.cantidad_unidades),0) = 0 ";
        } else if ("near-expiry".equalsIgnoreCase(scope)) {
            having =
                    " HAVING MIN(CASE WHEN s.fecha_vencimiento IS NULL THEN DATE('9999-12-31') ELSE s.fecha_vencimiento END) <= DATE_ADD(CURDATE(), INTERVAL ? DAY) ";
            args.add(days);
        }

        String sql = base + where + groupBy + having + " ORDER BY p.nombre ASC ";
        return jdbc.query(sql, args.toArray(), (rs,i) -> mapInventoryProductFull(rs));
    }

    private static InventoryProductFullDto mapInventoryProductFull(ResultSet rs) throws SQLException {
        InventoryProductFullDto d = new InventoryProductFullDto();
        d.codigo_barras = rs.getString("codigo_barras");
        d.cantidad_unidades_blister = getInteger(rs, "cantidad_unidades_blister");
        d.activo = getBooleanNullable(rs, "activo");
        d.cantidad_general = getInteger(rs, "cantidad_general");
        d.categoria = rs.getString("categoria");
        d.concentracion = rs.getString("concentracion");
        d.descuento = rs.getBigDecimal("descuento");
        Timestamp fa = rs.getTimestamp("fecha_actualizacion"); d.fecha_actualizacion = fa == null ? null : fa.toInstant().toString();
        Timestamp fc = rs.getTimestamp("fecha_creacion");      d.fecha_creacion = fc == null ? null : fc.toInstant().toString();
        d.laboratorio = rs.getString("laboratorio");
        d.nombre = rs.getString("nombre");
        d.precio_venta_blister = rs.getBigDecimal("precio_venta_blister");
        d.precio_venta_und = rs.getBigDecimal("precio_venta_und");
        d.cantidad_minima = getInteger(rs, "cantidad_minima");
        d.principio_activo = rs.getString("principio_activo");
        d.tipo_medicamento = rs.getString("tipo_medicamento");
        d.presentacion = rs.getString("presentacion");

        d.stock_total = rs.getLong("stock_total");
        d.lotes = rs.getInt("lotes");
        d.lotes_vencidos = rs.getInt("lotes_vencidos");
        Date pv = rs.getDate("proximo_vencimiento");
        d.proximo_vencimiento = pv == null ? null : pv.toLocalDate().toString();
        d.valor_compra_total = rs.getBigDecimal("valor_compra_total");
        d.costo_promedio = rs.getBigDecimal("costo_promedio");
        return d;
    }

    public List<InventoryLotDto> getInventoryLotsByProduct(String codigoBarras) {
        String sql = """
      SELECT s.id AS lote_id, s.cantidad_unidades, s.fecha_vencimiento, s.precio_compra,
             CASE WHEN s.fecha_vencimiento IS NOT NULL AND s.fecha_vencimiento < CURDATE() THEN 'VENCIDO' ELSE 'OK' END AS estado
      FROM stock s
      JOIN productos p ON s.producto_id = p.id
      WHERE p.codigo_barras = ?
      ORDER BY s.fecha_vencimiento IS NULL, s.fecha_vencimiento
      """;
        return jdbc.query(sql, ps -> ps.setString(1, codigoBarras), (rs,i)->{
            InventoryLotDto d = new InventoryLotDto();
            d.lote_id = rs.getInt("lote_id");
            d.codigo_barras = codigoBarras;
            d.cantidad_unidades = rs.getLong("cantidad_unidades");
            Date fv = rs.getDate("fecha_vencimiento");
            d.fecha_vencimiento = fv == null ? null : fv.toLocalDate().toString();
            d.precio_compra = rs.getBigDecimal("precio_compra");
            d.estado = rs.getString("estado");
            return d;
        });
    }

    // ========= Clientes (auto-detección de esquema) =========
    public List<TopCustomerDto> getTopCustomers(String from, String to, int limit, String sortBy) {
        Timestamp f = tsStart(from);
        Timestamp t = tsEnd(to);
        String orderCol = "tickets".equalsIgnoreCase(sortBy) ? "tickets" : "ventas";

        boolean hasClienteId = hasColumn("boletas", "cliente_id");
        String cliDni = pickColumn("clientes", "dni", "documento", "numero_documento", "ruc");
        String cliNombre = pickColumn("clientes", "nombre", "nombres", "razon_social", "nombre_completo");
        String bolNombre = pickColumn("boletas", "cliente_nombre", "nombre_cliente", "cliente");

        if (hasClienteId && cliNombre != null) {
            String dniExpr = (cliDni != null) ? ("c.`" + cliDni + "`") : "NULL";
            String nombreExpr = "c.`" + cliNombre + "`";
            String groupBy = (cliDni != null) ? ("c.`" + cliDni + "`, c.`" + cliNombre + "`")
                    : ("c.`" + cliNombre + "`");

            String sql = """
        SELECT
          %s AS dni,
          %s AS nombre,
          COUNT(DISTINCT b.id)            AS tickets,
          COALESCE(SUM(d.cantidad),0)     AS unidades,
          COALESCE(SUM(b.total_compra),0) AS ventas,
          MAX(b.fecha_venta)              AS ultima_compra
        FROM boletas b
        JOIN clientes c ON c.id = b.cliente_id
        LEFT JOIN detalles_boleta d ON d.boleta_id = b.id
        WHERE (? IS NULL OR b.fecha_venta >= ?)
          AND (? IS NULL OR b.fecha_venta <= ?)
        GROUP BY %s
        ORDER BY %s DESC
        LIMIT ?
      """.formatted(dniExpr, nombreExpr, groupBy, orderCol);

            log.info("[TopCustomers] Usando variante JOIN clientes.id (dniCol={}, nombreCol={}) orderBy={}",
                    cliDni, cliNombre, orderCol);

            return jdbc.query(sql, ps -> {
                ps.setTimestamp(1, f); ps.setTimestamp(2, f);
                ps.setTimestamp(3, t); ps.setTimestamp(4, t);
                ps.setInt(5, limit);
            }, this::mapTopCustomer);
        }

        if (bolNombre != null) {
            String nombreExpr = "b.`" + bolNombre + "`";
            String sql = """
        SELECT
          NULL                             AS dni,
          %s                               AS nombre,
          COUNT(DISTINCT b.id)             AS tickets,
          COALESCE(SUM(d.cantidad),0)      AS unidades,
          COALESCE(SUM(b.total_compra),0)  AS ventas,
          MAX(b.fecha_venta)               AS ultima_compra
        FROM boletas b
        LEFT JOIN detalles_boleta d ON d.boleta_id = b.id
        WHERE %s IS NOT NULL AND %s <> ''
          AND (? IS NULL OR b.fecha_venta >= ?)
          AND (? IS NULL OR b.fecha_venta <= ?)
        GROUP BY %s
        ORDER BY %s DESC
        LIMIT ?
      """.formatted(nombreExpr, nombreExpr, nombreExpr, nombreExpr, orderCol);

            log.info("[TopCustomers] Usando variante nombre en boletas (col={}) orderBy={}", bolNombre, orderCol);

            return jdbc.query(sql, ps -> {
                ps.setTimestamp(1, f); ps.setTimestamp(2, f);
                ps.setTimestamp(3, t); ps.setTimestamp(4, t);
                ps.setInt(5, limit);
            }, this::mapTopCustomer);
        }

        log.warn("[TopCustomers] No se encontraron columnas compatibles. Revisa esquema: boletas(cliente_id / cliente_nombre|nombre_cliente|cliente), clientes(dni|documento|numero_documento|ruc, nombre|nombres|razon_social|nombre_completo)");
        return List.of();
    }

    private TopCustomerDto mapTopCustomer(ResultSet rs, int i) throws SQLException {
        TopCustomerDto d = new TopCustomerDto();
        d.dni = rs.getString("dni");
        d.nombre = rs.getString("nombre");
        d.tickets = rs.getLong("tickets");
        d.unidades = rs.getLong("unidades");
        d.ventas = rs.getBigDecimal("ventas");
        Timestamp ult = rs.getTimestamp("ultima_compra");
        d.ultima_compra = ult == null ? null : ult.toInstant().toString();
        return d;
    }

    // ========= Helpers de introspección y mapeo =========
    private boolean hasColumn(String table, String column) {
        // Usa EXISTS explícito para evitar ambigüedad de sobrecarga en JdbcTemplate.query
        String sql = """
      SELECT EXISTS(
        SELECT 1
        FROM INFORMATION_SCHEMA.COLUMNS
        WHERE TABLE_SCHEMA = DATABASE()
          AND TABLE_NAME = ?
          AND COLUMN_NAME = ?
      ) AS present
    """;
        Integer present = jdbc.queryForObject(sql, new Object[]{table, column}, Integer.class);
        return present != null && present != 0;
    }

    private String pickColumn(String table, String... candidates) {
        for (String c : candidates) {
            if (hasColumn(table, c)) return c;
        }
        return null;
    }

    private static Integer getInteger(ResultSet rs, String col) throws SQLException {
        int v = rs.getInt(col);
        return rs.wasNull() ? null : v;
    }
    private static Boolean getBooleanNullable(ResultSet rs, String col) throws SQLException {
        Object o = rs.getObject(col);
        if (o == null) return null;
        if (o instanceof Boolean b) return b;
        if (o instanceof Number n) return n.intValue() != 0;
        if (o instanceof byte[] arr) return arr.length > 0 && arr[0] != 0;
        return Boolean.valueOf(o.toString());
    }
}