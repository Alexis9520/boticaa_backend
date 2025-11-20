package quantify.BoticaSaid.dto.proveedor;

import java.math.BigDecimal;

/**
 * DTO simplificado para mostrar información de proveedor en ProductoResponse
 */
public class ProveedorSimpleDTO {
    
    private Long id;
    private String nombre;
    private String ruc;
    private BigDecimal precioCompra;
    private boolean esPrincipal;

    // Constructor vacío
    public ProveedorSimpleDTO() {}

    // Constructor con parámetros
    public ProveedorSimpleDTO(Long id, String nombre, String ruc, BigDecimal precioCompra, boolean esPrincipal) {
        this.id = id;
        this.nombre = nombre;
        this.ruc = ruc;
        this.precioCompra = precioCompra;
        this.esPrincipal = esPrincipal;
    }

    // Getters y Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getRuc() {
        return ruc;
    }

    public void setRuc(String ruc) {
        this.ruc = ruc;
    }

    public BigDecimal getPrecioCompra() {
        return precioCompra;
    }

    public void setPrecioCompra(BigDecimal precioCompra) {
        this.precioCompra = precioCompra;
    }

    public boolean isEsPrincipal() {
        return esPrincipal;
    }

    public void setEsPrincipal(boolean esPrincipal) {
        this.esPrincipal = esPrincipal;
    }
}
