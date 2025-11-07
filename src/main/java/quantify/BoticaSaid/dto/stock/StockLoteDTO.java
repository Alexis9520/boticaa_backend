package quantify.BoticaSaid.dto.stock;

import java.math.BigDecimal;
import java.time.LocalDate;

public class StockLoteDTO {
    private String codigoStock;      // <-- AGREGA ESTA LÍNEA
    private int cantidadUnidades;
    private LocalDate fechaVencimiento;
    private BigDecimal precioCompra;

    // Getters y setters

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