package quantify.BoticaSaid.dto.reports;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class LoteReportDTO {
    
    private Long productoId;
    private String nombreProducto;
    private String codigoBarras;
    private String concentracion;
    private String presentacion;
    private Integer stockId;
    private String codigoStock;
    private Integer cantidadUnidades;
    private Integer cantidadInicial;
    private LocalDate fechaVencimiento;
    private BigDecimal precioCompra;
    private LocalDateTime fechaCreacion;
    
    public LoteReportDTO() {}
    
    public LoteReportDTO(Long productoId, String nombreProducto, String codigoBarras, 
                         String concentracion, String presentacion,
                         Integer stockId, String codigoStock, Integer cantidadUnidades, 
                         Integer cantidadInicial,
                         LocalDate fechaVencimiento, BigDecimal precioCompra, LocalDateTime fechaCreacion) {
        this.productoId = productoId;
        this.nombreProducto = nombreProducto;
        this.codigoBarras = codigoBarras;
        this.concentracion = concentracion;
        this.presentacion = presentacion;
        this.stockId = stockId;
        this.codigoStock = codigoStock;
        this.cantidadUnidades = cantidadUnidades;
        this.cantidadInicial = cantidadInicial;
        this.fechaVencimiento = fechaVencimiento;
        this.precioCompra = precioCompra;
        this.fechaCreacion = fechaCreacion;
    }

    // Getters and Setters
    public Long getProductoId() {
        return productoId;
    }

    public void setProductoId(Long productoId) {
        this.productoId = productoId;
    }

    public String getNombreProducto() {
        return nombreProducto;
    }

    public void setNombreProducto(String nombreProducto) {
        this.nombreProducto = nombreProducto;
    }

    public String getCodigoBarras() {
        return codigoBarras;
    }

    public void setCodigoBarras(String codigoBarras) {
        this.codigoBarras = codigoBarras;
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

    public Integer getStockId() {
        return stockId;
    }

    public void setStockId(Integer stockId) {
        this.stockId = stockId;
    }

    public String getCodigoStock() {
        return codigoStock;
    }

    public void setCodigoStock(String codigoStock) {
        this.codigoStock = codigoStock;
    }

    public Integer getCantidadUnidades() {
        return cantidadUnidades;
    }

    public void setCantidadUnidades(Integer cantidadUnidades) {
        this.cantidadUnidades = cantidadUnidades;
    }

    public Integer getCantidadInicial() {
        return cantidadInicial;
    }

    public void setCantidadInicial(Integer cantidadInicial) {
        this.cantidadInicial = cantidadInicial;
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

    public LocalDateTime getFechaCreacion() {
        return fechaCreacion;
    }

    public void setFechaCreacion(LocalDateTime fechaCreacion) {
        this.fechaCreacion = fechaCreacion;
    }
}
