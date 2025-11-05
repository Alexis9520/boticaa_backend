package quantify.BoticaSaid.dto.reports;

import java.math.BigDecimal;

public class InventoryDtos {

    // Producto con TODOS los campos de la BD + agregados de stock
    public static class InventoryProductFullDto {
        public String codigo_barras;
        public Integer cantidad_unidades_blister;
        public Boolean activo;
        public Integer cantidad_general;
        public String categoria;
        public String concentracion;
        public BigDecimal descuento;
        public String fecha_actualizacion; // ISO date-time
        public String fecha_creacion;      // ISO date-time
        public String laboratorio;
        public String nombre;
        public BigDecimal precio_venta_blister;
        public BigDecimal precio_venta_und;
        public Integer cantidad_minima;
        public String principio_activo;
        public String tipo_medicamento;
        public String presentacion;

        // Agregados de stock
        public Long stock_total;
        public Integer lotes;
        public Integer lotes_vencidos;
        public String proximo_vencimiento; // ISO date
        public BigDecimal valor_compra_total;
        public BigDecimal costo_promedio;
    }

    // Lote por producto
    public static class InventoryLotDto {
        public Integer lote_id;
        public String codigo_barras;
        public Long cantidad_unidades;
        public String fecha_vencimiento; // ISO date
        public BigDecimal precio_compra;
        public String estado; // "VENCIDO" | "OK"
    }
}