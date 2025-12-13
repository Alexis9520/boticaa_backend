package quantify.BoticaSaid.dto.producto;

/**
 * DTO para métricas resumen de productos/inventario
 */
public class ProductoMetricasDTO {
    private Long totalProductosActivos;
    private Long cantidadTotalUnidades;
    private Long productosStockCritico;
    private Long lotesVencidos;

    public ProductoMetricasDTO() {
    }

    public ProductoMetricasDTO(Long totalProductosActivos, Long cantidadTotalUnidades,
            Long productosStockCritico, Long lotesVencidos) {
        this.totalProductosActivos = totalProductosActivos;
        this.cantidadTotalUnidades = cantidadTotalUnidades;
        this.productosStockCritico = productosStockCritico;
        this.lotesVencidos = lotesVencidos;
    }

    public Long getTotalProductosActivos() {
        return totalProductosActivos;
    }

    public void setTotalProductosActivos(Long totalProductosActivos) {
        this.totalProductosActivos = totalProductosActivos;
    }

    public Long getCantidadTotalUnidades() {
        return cantidadTotalUnidades;
    }

    public void setCantidadTotalUnidades(Long cantidadTotalUnidades) {
        this.cantidadTotalUnidades = cantidadTotalUnidades;
    }

    public Long getProductosStockCritico() {
        return productosStockCritico;
    }

    public void setProductosStockCritico(Long productosStockCritico) {
        this.productosStockCritico = productosStockCritico;
    }

    public Long getLotesVencidos() {
        return lotesVencidos;
    }

    public void setLotesVencidos(Long lotesVencidos) {
        this.lotesVencidos = lotesVencidos;
    }
}
