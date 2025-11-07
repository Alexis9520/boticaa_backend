package quantify.BoticaSaid.dto.stock;

import java.math.BigDecimal;

public class StockItemDTO {
    private Integer id;
    private String codigoStock;
    private String codigoBarras;
    private String nombre;
    private String concentracion;
    private Integer cantidadUnidades;
    private Integer cantidadMinima;
    private BigDecimal precioCompra;
    private BigDecimal precioVenta;
    private String fechaVencimiento;
    private String laboratorio;
    private String categoria;

    public StockItemDTO() {}

    public StockItemDTO(
            Integer id,
            String codigoStock,
            String codigoBarras,
            String nombre,
            String concentracion,
            Integer cantidadUnidades,
            Integer cantidadMinima,
            BigDecimal precioCompra,
            BigDecimal precioVenta,
            String fechaVencimiento,
            String laboratorio,
            String categoria
    ) {
        this.id = id;
        this.codigoStock = codigoStock;
        this.codigoBarras = codigoBarras;
        this.nombre = nombre;
        this.concentracion = concentracion;
        this.cantidadUnidades = cantidadUnidades;
        this.cantidadMinima = cantidadMinima;
        this.precioCompra = precioCompra;
        this.precioVenta = precioVenta;
        this.fechaVencimiento = fechaVencimiento;
        this.laboratorio = laboratorio;
        this.categoria = categoria;
    }

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public String getCodigoStock() { return codigoStock; }
    public void setCodigoStock(String codigoStock) { this.codigoStock = codigoStock; }

    public String getCodigoBarras() { return codigoBarras; }
    public void setCodigoBarras(String codigoBarras) { this.codigoBarras = codigoBarras; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getConcentracion() { return concentracion; }
    public void setConcentracion(String concentracion) { this.concentracion = concentracion; }

    public Integer getCantidadUnidades() { return cantidadUnidades; }
    public void setCantidadUnidades(Integer cantidadUnidades) { this.cantidadUnidades = cantidadUnidades; }

    public Integer getCantidadMinima() { return cantidadMinima; }
    public void setCantidadMinima(Integer cantidadMinima) { this.cantidadMinima = cantidadMinima; }

    public BigDecimal getPrecioCompra() { return precioCompra; }
    public void setPrecioCompra(BigDecimal precioCompra) { this.precioCompra = precioCompra; }

    public BigDecimal getPrecioVenta() { return precioVenta; }
    public void setPrecioVenta(BigDecimal precioVenta) { this.precioVenta = precioVenta; }

    public String getFechaVencimiento() { return fechaVencimiento; }
    public void setFechaVencimiento(String fechaVencimiento) { this.fechaVencimiento = fechaVencimiento; }

    public String getLaboratorio() { return laboratorio; }
    public void setLaboratorio(String laboratorio) { this.laboratorio = laboratorio; }

    public String getCategoria() { return categoria; }
    public void setCategoria(String categoria) { this.categoria = categoria; }
}