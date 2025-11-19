package quantify.BoticaSaid.dto.pedido;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class PedidoReporteDTO {
    
    private String codigoBarras;
    private String producto;
    private String concentracion;
    private String presentacion;
    private String codigoStock;
    private Integer cantUnidades;
    private Integer cantInicial;
    private LocalDate fVencimiento;
    private BigDecimal precioCompra;
    private LocalDateTime fCreacion;

    // Constructor vacío
    public PedidoReporteDTO() {}

    // Constructor con todos los parámetros
    public PedidoReporteDTO(String codigoBarras, String producto, String concentracion, 
                           String presentacion, String codigoStock, Integer cantUnidades,
                           Integer cantInicial, LocalDate fVencimiento, 
                           BigDecimal precioCompra, LocalDateTime fCreacion) {
        this.codigoBarras = codigoBarras;
        this.producto = producto;
        this.concentracion = concentracion;
        this.presentacion = presentacion;
        this.codigoStock = codigoStock;
        this.cantUnidades = cantUnidades;
        this.cantInicial = cantInicial;
        this.fVencimiento = fVencimiento;
        this.precioCompra = precioCompra;
        this.fCreacion = fCreacion;
    }

    // Getters y Setters
    public String getCodigoBarras() {
        return codigoBarras;
    }

    public void setCodigoBarras(String codigoBarras) {
        this.codigoBarras = codigoBarras;
    }

    public String getProducto() {
        return producto;
    }

    public void setProducto(String producto) {
        this.producto = producto;
    }

    public String getConcentracion() {
        return concentracion;
    }

    public void setConcentracion(String concentracion) {
        this.concentracion = concentracion;
    }

    public String getPresentacion() {
        return presentacion;
    }

    public void setPresentacion(String presentacion) {
        this.presentacion = presentacion;
    }

    public String getCodigoStock() {
        return codigoStock;
    }

    public void setCodigoStock(String codigoStock) {
        this.codigoStock = codigoStock;
    }

    public Integer getCantUnidades() {
        return cantUnidades;
    }

    public void setCantUnidades(Integer cantUnidades) {
        this.cantUnidades = cantUnidades;
    }

    public Integer getCantInicial() {
        return cantInicial;
    }

    public void setCantInicial(Integer cantInicial) {
        this.cantInicial = cantInicial;
    }

    public LocalDate getFVencimiento() {
        return fVencimiento;
    }

    public void setFVencimiento(LocalDate fVencimiento) {
        this.fVencimiento = fVencimiento;
    }

    public BigDecimal getPrecioCompra() {
        return precioCompra;
    }

    public void setPrecioCompra(BigDecimal precioCompra) {
        this.precioCompra = precioCompra;
    }

    public LocalDateTime getFCreacion() {
        return fCreacion;
    }

    public void setFCreacion(LocalDateTime fCreacion) {
        this.fCreacion = fCreacion;
    }
}
