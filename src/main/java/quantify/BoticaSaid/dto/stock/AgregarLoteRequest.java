package quantify.BoticaSaid.dto.stock;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * DTO para agregar uno o varios lotes de stock a un producto
 * sin modificar los datos del producto
 */
public class AgregarLoteRequest {
    private Long productoId;  // ID del producto (alternativa a codigoBarras)
    private String codigoBarras;  // Código de barras del producto (para escáneres)
    private List<LoteItem> lotes;

    public static class LoteItem {
        private String codigoStock;
        private int cantidadUnidades;
        
        @JsonFormat(pattern = "yyyy-MM-dd")
        private LocalDate fechaVencimiento;
        
        private BigDecimal precioCompra;

        // Constructor vacío
        public LoteItem() {}

        // Getters y Setters
        public String getCodigoStock() {
            return codigoStock;
        }

        public void setCodigoStock(String codigoStock) {
            this.codigoStock = codigoStock;
        }

        public int getCantidadUnidades() {
            return cantidadUnidades;
        }

        public void setCantidadUnidades(int cantidadUnidades) {
            this.cantidadUnidades = cantidadUnidades;
        }

        public LocalDate getFechaVencimiento() {
            return fechaVencimiento;
        }

        public void setFechaVencimiento(LocalDate fechaVencimiento) {
            this.fechaVencimiento = fechaVencimiento;
        }

        public BigDecimal getPrecioCompra() {
            return precioCompra;
        }

        public void setPrecioCompra(BigDecimal precioCompra) {
            this.precioCompra = precioCompra;
        }
    }

    // Constructor vacío
    public AgregarLoteRequest() {}

    // Getters y Setters
    public Long getProductoId() {
        return productoId;
    }

    public void setProductoId(Long productoId) {
        this.productoId = productoId;
    }

    public String getCodigoBarras() {
        return codigoBarras;
    }

    public void setCodigoBarras(String codigoBarras) {
        this.codigoBarras = codigoBarras;
    }

    public List<LoteItem> getLotes() {
        return lotes;
    }

    public void setLotes(List<LoteItem> lotes) {
        this.lotes = lotes;
    }
}
