package quantify.BoticaSaid.dto.producto;

import jakarta.validation.constraints.NotBlank;
import quantify.BoticaSaid.dto.stock.StockRequest;

import java.math.BigDecimal;
import java.util.List;

public class ProductoRequest {
    private Long id;
    private String codigoBarras;

    @NotBlank(message = "El nombre del producto es obligatorio")
    private String nombre;

    private String concentracion;

    // Ahora opcional (permite null)
    private Integer cantidadGeneral;

    private Integer cantidadMinima;
    private BigDecimal precioVentaUnd;
    private BigDecimal descuento;
    private String laboratorio;
    private String categoria;

    // Ahora opcional (permite null)
    private Integer cantidadUnidadesBlister;

    private BigDecimal precioVentaBlister;

    // Nuevos campos (también opcionales)
    private String principioActivo;
    private String tipoMedicamento;
    private String presentacion;

    private Long proveedorId;

    private List<StockRequest> stocks;

    // Getters y setters

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public String getConcentracion() {
        return concentracion;
    }

    public void setConcentracion(String concentracion) {
        this.concentracion = concentracion;
    }

    public Integer getCantidadGeneral() {
        return cantidadGeneral;
    }

    public void setCantidadGeneral(Integer cantidadGeneral) {
        this.cantidadGeneral = cantidadGeneral;
    }

    public Integer getCantidadMinima() {
        return cantidadMinima;
    }

    public void setCantidadMinima(Integer cantidadMinima) {
        this.cantidadMinima = cantidadMinima;
    }

    public BigDecimal getPrecioVentaUnd() {
        return precioVentaUnd;
    }

    public void setPrecioVentaUnd(BigDecimal precioVentaUnd) {
        this.precioVentaUnd = precioVentaUnd;
    }

    public BigDecimal getDescuento() {
        return descuento;
    }

    public void setDescuento(BigDecimal descuento) {
        this.descuento = descuento;
    }

    public String getLaboratorio() {
        return laboratorio;
    }

    public void setLaboratorio(String laboratorio) {
        this.laboratorio = laboratorio;
    }

    public String getCategoria() {
        return categoria;
    }

    public void setCategoria(String categoria) {
        this.categoria = categoria;
    }

    public Integer getCantidadUnidadesBlister() {
        return cantidadUnidadesBlister;
    }

    public void setCantidadUnidadesBlister(Integer cantidadUnidadesBlister) {
        this.cantidadUnidadesBlister = cantidadUnidadesBlister;
    }

    public BigDecimal getPrecioVentaBlister() {
        return precioVentaBlister;
    }

    public void setPrecioVentaBlister(BigDecimal precioVentaBlister) {
        this.precioVentaBlister = precioVentaBlister;
    }

    public String getPrincipioActivo() {
        return principioActivo;
    }

    public void setPrincipioActivo(String principioActivo) {
        this.principioActivo = principioActivo;
    }

    public String getTipoMedicamento() {
        return tipoMedicamento;
    }

    public void setTipoMedicamento(String tipoMedicamento) {
        this.tipoMedicamento = tipoMedicamento;
    }

    public String getPresentacion() {
        return presentacion;
    }

    public void setPresentacion(String presentacion) {
        this.presentacion = presentacion;
    }

    public List<StockRequest> getStocks() {
        return stocks;
    }

    public void setStocks(List<StockRequest> stocks) {
        this.stocks = stocks;
    }

    public Long getProveedorId() {
        return proveedorId;
    }

    public void setProveedorId(Long proveedorId) {
        this.proveedorId = proveedorId;
    }
}