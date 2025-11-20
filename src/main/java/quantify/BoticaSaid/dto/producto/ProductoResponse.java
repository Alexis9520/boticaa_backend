package quantify.BoticaSaid.dto.producto;

import quantify.BoticaSaid.dto.stock.StockLoteDTO;
import java.math.BigDecimal;
import java.util.List;

public class ProductoResponse {
    private Long id;
    private String codigoBarras;
    private String nombre;
    private String concentracion;
    private int cantidadGeneral;
    private Integer cantidadMinima;
    private BigDecimal precioVentaUnd;
    private BigDecimal descuento;
    private String laboratorio;
    private String categoria;
    private Integer cantidadUnidadesBlister;
    private BigDecimal precioVentaBlister;

    // NUEVOS CAMPOS
    private String principioActivo;
    private String tipoMedicamento;
    private String presentacion;
    
    // Mantener para compatibilidad con frontend (primer proveedor de la lista)
    private Long proveedorId;
    private String proveedorNombre;
    
    // Lista de proveedores (nueva funcionalidad)
    private List<ProveedorSimpleDTO> proveedores;
    
    private List<StockLoteDTO> stocks;

    // Getters y setters

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

    public int getCantidadGeneral() {
        return cantidadGeneral;
    }

    public void setCantidadGeneral(int cantidadGeneral) {
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

    public List<StockLoteDTO> getStocks() {
        return stocks;
    }

    public void setStocks(List<StockLoteDTO> stocks) {
        this.stocks = stocks;
    }
    public String getPresentacion() { return presentacion; }
    public void setPresentacion(String presentacion) { this.presentacion = presentacion; }

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

    public List<ProveedorSimpleDTO> getProveedores() {
        return proveedores;
    }

    public void setProveedores(List<ProveedorSimpleDTO> proveedores) {
        this.proveedores = proveedores;
    }
}