package quantify.BoticaSaid.dto.reports;

import java.util.List;

public class ProveedorReportDTO {
    
    private Long proveedorId;
    private String ruc;
    private String razonComercial;
    private String correo;
    private String direccion;
    private Integer totalProductos;
    private List<ProductoProveedorDTO> productos;
    
    public ProveedorReportDTO() {}
    
    public ProveedorReportDTO(Long proveedorId, String ruc, String razonComercial, 
                              String correo, String direccion, Integer totalProductos, 
                              List<ProductoProveedorDTO> productos) {
        this.proveedorId = proveedorId;
        this.ruc = ruc;
        this.razonComercial = razonComercial;
        this.correo = correo;
        this.direccion = direccion;
        this.totalProductos = totalProductos;
        this.productos = productos;
    }

    // Getters and Setters
    public Long getProveedorId() {
        return proveedorId;
    }

    public void setProveedorId(Long proveedorId) {
        this.proveedorId = proveedorId;
    }

    public String getRuc() {
        return ruc;
    }

    public void setRuc(String ruc) {
        this.ruc = ruc;
    }

    public String getRazonComercial() {
        return razonComercial;
    }

    public void setRazonComercial(String razonComercial) {
        this.razonComercial = razonComercial;
    }

    public String getCorreo() {
        return correo;
    }

    public void setCorreo(String correo) {
        this.correo = correo;
    }

    public String getDireccion() {
        return direccion;
    }

    public void setDireccion(String direccion) {
        this.direccion = direccion;
    }

    public Integer getTotalProductos() {
        return totalProductos;
    }

    public void setTotalProductos(Integer totalProductos) {
        this.totalProductos = totalProductos;
    }

    public List<ProductoProveedorDTO> getProductos() {
        return productos;
    }

    public void setProductos(List<ProductoProveedorDTO> productos) {
        this.productos = productos;
    }
    
    public static class ProductoProveedorDTO {
        private Long productoId;
        private String nombre;
        private String codigoBarras;
        private String categoria;
        private String laboratorio;
        private Integer cantidadGeneral;
        
        public ProductoProveedorDTO() {}
        
        public ProductoProveedorDTO(Long productoId, String nombre, String codigoBarras, 
                                    String categoria, String laboratorio, Integer cantidadGeneral) {
            this.productoId = productoId;
            this.nombre = nombre;
            this.codigoBarras = codigoBarras;
            this.categoria = categoria;
            this.laboratorio = laboratorio;
            this.cantidadGeneral = cantidadGeneral;
        }

        // Getters and Setters
        public Long getProductoId() {
            return productoId;
        }

        public void setProductoId(Long productoId) {
            this.productoId = productoId;
        }

        public String getNombre() {
            return nombre;
        }

        public void setNombre(String nombre) {
            this.nombre = nombre;
        }

        public String getCodigoBarras() {
            return codigoBarras;
        }

        public void setCodigoBarras(String codigoBarras) {
            this.codigoBarras = codigoBarras;
        }

        public String getCategoria() {
            return categoria;
        }

        public void setCategoria(String categoria) {
            this.categoria = categoria;
        }

        public String getLaboratorio() {
            return laboratorio;
        }

        public void setLaboratorio(String laboratorio) {
            this.laboratorio = laboratorio;
        }

        public Integer getCantidadGeneral() {
            return cantidadGeneral;
        }

        public void setCantidadGeneral(Integer cantidadGeneral) {
            this.cantidadGeneral = cantidadGeneral;
        }
    }
}
