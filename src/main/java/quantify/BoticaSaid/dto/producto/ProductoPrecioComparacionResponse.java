package quantify.BoticaSaid.dto.producto;

import java.math.BigDecimal;
import java.util.List;

public class ProductoPrecioComparacionResponse {
    private Long productoId;
    private String nombreProducto;
    private String codigoBarras;
    private List<ProveedorPrecioDetalleDTO> proveedores;

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

    public List<ProveedorPrecioDetalleDTO> getProveedores() {
        return proveedores;
    }

    public void setProveedores(List<ProveedorPrecioDetalleDTO> proveedores) {
        this.proveedores = proveedores;
    }

    public static class ProveedorPrecioDetalleDTO {
        private Long proveedorId;
        private String proveedorNombre;
        private String proveedorRuc;
        private BigDecimal ultimoPrecio;
        private String fechaUltimoPedido;
        private BigDecimal precioPromedio;
        private Integer totalPedidos;

        public Long getProveedorId() {
            return proveedorId;
        }

        public void setProveedorId(Long proveedorId) {
            this.proveedorId = proveedorId;
        }

        public String getProveedorNombre() {
            return proveedorNombre;
        }

        public void setProveedorNombre(String proveedorNombre) {
            this.proveedorNombre = proveedorNombre;
        }

        public String getProveedorRuc() {
            return proveedorRuc;
        }

        public void setProveedorRuc(String proveedorRuc) {
            this.proveedorRuc = proveedorRuc;
        }

        public BigDecimal getUltimoPrecio() {
            return ultimoPrecio;
        }

        public void setUltimoPrecio(BigDecimal ultimoPrecio) {
            this.ultimoPrecio = ultimoPrecio;
        }

        public String getFechaUltimoPedido() {
            return fechaUltimoPedido;
        }

        public void setFechaUltimoPedido(String fechaUltimoPedido) {
            this.fechaUltimoPedido = fechaUltimoPedido;
        }

        public BigDecimal getPrecioPromedio() {
            return precioPromedio;
        }

        public void setPrecioPromedio(BigDecimal precioPromedio) {
            this.precioPromedio = precioPromedio;
        }

        public Integer getTotalPedidos() {
            return totalPedidos;
        }

        public void setTotalPedidos(Integer totalPedidos) {
            this.totalPedidos = totalPedidos;
        }
    }
}
