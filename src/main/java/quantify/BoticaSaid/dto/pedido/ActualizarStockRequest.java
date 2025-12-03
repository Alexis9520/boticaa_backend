// language: java
package quantify.BoticaSaid.dto.pedido;

import java.time.LocalDate;
import java.math.BigDecimal;

public class ActualizarStockRequest {
    private String codigoStock;
    private Integer cantidadInicial;
    private LocalDate fechaVencimiento;
    private BigDecimal precioCompra;

    public String getCodigoStock() { return codigoStock; }
    public void setCodigoStock(String codigoStock) { this.codigoStock = codigoStock; }

    public Integer getCantidadInicial() { return cantidadInicial; }
    public void setCantidadInicial(Integer cantidadInicial) { this.cantidadInicial = cantidadInicial; }

    public LocalDate getFechaVencimiento() { return fechaVencimiento; }
    public void setFechaVencimiento(LocalDate fechaVencimiento) { this.fechaVencimiento = fechaVencimiento; }

    public BigDecimal getPrecioCompra() { return precioCompra; }
    public void setPrecioCompra(BigDecimal precioCompra) { this.precioCompra = precioCompra; }
}