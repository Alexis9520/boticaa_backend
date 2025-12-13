package quantify.BoticaSaid.dto.boleta;

import java.math.BigDecimal;

/**
 * DTO enriquecido para detalles de boleta con información de tipo de venta
 * Soporta datos legacy (tipo_venta null) mediante inferencia por precio
 */
public class DetalleBoletaResponseDTO {
    private Long productoId;
    private String codigoBarras;
    private String nombre;
    private String tipoVenta; // "BLISTER", "UNIDAD", o "DESCONOCIDO"
    private Integer cantidad; // cantidad total en unidades almacenadas
    private Integer cantidadBlisters; // nro de blisters (si tipoVenta=BLISTER)
    private Integer unidadesPorBlister; // config del producto
    private BigDecimal precioAplicado; // precio usado en la venta (almacenado)
    private BigDecimal precioActualUnd; // precio actual del producto
    private BigDecimal precioActualBlister; // precio actual blister del producto
    private Boolean precioModificado; // true si el precio del producto cambió desde la venta
    private BigDecimal subtotal; // precioAplicado * cantidad (o blisters)

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

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getTipoVenta() {
        return tipoVenta;
    }

    public void setTipoVenta(String tipoVenta) {
        this.tipoVenta = tipoVenta;
    }

    public Integer getCantidad() {
        return cantidad;
    }

    public void setCantidad(Integer cantidad) {
        this.cantidad = cantidad;
    }

    public Integer getCantidadBlisters() {
        return cantidadBlisters;
    }

    public void setCantidadBlisters(Integer cantidadBlisters) {
        this.cantidadBlisters = cantidadBlisters;
    }

    public Integer getUnidadesPorBlister() {
        return unidadesPorBlister;
    }

    public void setUnidadesPorBlister(Integer unidadesPorBlister) {
        this.unidadesPorBlister = unidadesPorBlister;
    }

    public BigDecimal getPrecioAplicado() {
        return precioAplicado;
    }

    public void setPrecioAplicado(BigDecimal precioAplicado) {
        this.precioAplicado = precioAplicado;
    }

    public BigDecimal getPrecioActualUnd() {
        return precioActualUnd;
    }

    public void setPrecioActualUnd(BigDecimal precioActualUnd) {
        this.precioActualUnd = precioActualUnd;
    }

    public BigDecimal getPrecioActualBlister() {
        return precioActualBlister;
    }

    public void setPrecioActualBlister(BigDecimal precioActualBlister) {
        this.precioActualBlister = precioActualBlister;
    }

    public Boolean getPrecioModificado() {
        return precioModificado;
    }

    public void setPrecioModificado(Boolean precioModificado) {
        this.precioModificado = precioModificado;
    }

    public BigDecimal getSubtotal() {
        return subtotal;
    }

    public void setSubtotal(BigDecimal subtotal) {
        this.subtotal = subtotal;
    }
}
