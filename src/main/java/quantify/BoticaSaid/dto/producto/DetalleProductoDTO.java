package quantify.BoticaSaid.dto.producto;

import java.math.BigDecimal;

/**
 * DTO para detalles de producto en operaciones de venta
 * Soporta búsqueda por ID o código de barras
 */
public class DetalleProductoDTO {
    private Long id;  // ID del producto (alternativa a codBarras)
    private String codBarras;  // Código de barras (para escáneres)
    private String nombre;
    private int cantidad;
    private BigDecimal precio;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCodBarras() {
        return codBarras;
    }

    public void setCodBarras(String codBarras) {
        this.codBarras = codBarras;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public int getCantidad() {
        return cantidad;
    }

    public void setCantidad(int cantidad) {
        this.cantidad = cantidad;
    }

    public BigDecimal getPrecio() {
        return precio;
    }

    public void setPrecio(BigDecimal precio) {
        this.precio = precio;
    }

}