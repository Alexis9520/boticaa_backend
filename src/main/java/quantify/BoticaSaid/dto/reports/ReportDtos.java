package quantify.BoticaSaid.dto.reports;

import java.math.BigDecimal;

public class ReportDtos {

    public static class SalesSummaryDto {
        public BigDecimal ventas;
        public Long tickets;
        public Long unidades;
        public BigDecimal ticket_promedio;
        public BigDecimal upt;
    }

    public static class SalesByDayDto {
        public String fecha; // YYYY-MM-DD
        public Long tickets;
        public Long unidades;
        public BigDecimal ventas;
        public BigDecimal ticket_promedio;
        public BigDecimal upt;
    }

    public static class TopProductDto {
        public String codigo_barras;
        public String nombre;
        public String categoria;
        public Long unidades;
        public BigDecimal ventas;
    }

    public static class PaymentMixDto {
        public String metodo_pago;
        public Long tickets;
        public BigDecimal total;
    }

    public static class InventoryItemDto {
        public String codigo_barras;
        public String nombre;
        public String categoria;
        public String presentacion;
        public Integer cantidad_minima;
        public Long stock_actual;
        public String prox_vencimiento; // YYYY-MM-DD
        public BigDecimal costo_promedio;
        public BigDecimal precio_venta_und;
        public BigDecimal precio_venta_blister;

        // Para “near-expiry” (por lote)
        public Integer lote_id;
        public Long cantidad_unidades;
        public String fecha_vencimiento; // YYYY-MM-DD
        public BigDecimal precio_compra;
    }

    public static class TopCustomerDto {
        public String dni;
        public String nombre;
        public Long tickets;
        public BigDecimal ventas;
    }
}