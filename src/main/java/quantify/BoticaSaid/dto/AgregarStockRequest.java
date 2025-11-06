package quantify.BoticaSaid.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * DTO para agregar stock a un producto
 * Soporta búsqueda de producto por ID o código de barras
 */
public class AgregarStockRequest {
    private String codigoStock;
    private Long productoId;  // ID del producto (alternativa a codigoBarras)
    private String codigoBarras;  // Código de barras del producto (para escáneres)
    private int cantidadUnidades;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate fechaVencimiento;

    private BigDecimal precioCompra;

    // Constructor vacío
    public AgregarStockRequest() {}

    // Getters y Setters

    public String getCodigoStock() {
        return codigoStock;
    }

    public void setCodigoStock(String codigoStock) {
        this.codigoStock = codigoStock;
    }

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