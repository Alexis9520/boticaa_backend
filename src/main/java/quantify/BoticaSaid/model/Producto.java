package quantify.BoticaSaid.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Entity
@Table(name = "productos")
public class Producto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Puede ser null y sigue siendo único cuando tiene valor
    @Column(name = "codigo_barras", length = 255, unique = true)
    private String codigoBarras;

    @NotBlank
    @Column(nullable = false)
    private String nombre;

    private String concentracion;

    // Ahora opcional (permite null)
    @Column(name = "cantidad_general")
    private Integer cantidadGeneral;

    @Column(name = "cantidad_minima")
    private Integer cantidadMinima;

    @Column(name = "precio_venta_und")
    private BigDecimal precioVentaUnd;

    @Column(name = "descuento")
    private BigDecimal descuento;

    private String laboratorio;

    private String categoria;

    // Nuevos campos (opcionales)
    @Column(name = "principio_activo")
    private String principioActivo;

    @Column(name = "tipo_medicamento")
    private String tipoMedicamento; // "GENÉRICO" o "MARCA"

    @Column(name = "presentacion")
    private String presentacion;

    @Column(name = "fecha_creacion", insertable = false, updatable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date fechaCreacion;

    @Column(name = "fecha_actualizacion", insertable = false, updatable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date fechaActualizacion;

    @Column(nullable = false)
    private boolean activo = true;

    @OneToMany(mappedBy = "producto", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    private List<Stock> stocks = new ArrayList<>();

    @Column(name = "Cantidad_unidades_blister")
    private Integer CantidadUnidadesBlister;

    @Column(name = "precio_venta_blister")
    private BigDecimal precioVentaBlister;

    // Constructor vacío
    public Producto() {}

    // Getters y Setters
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

    public Date getFechaCreacion() {
        return fechaCreacion;
    }

    public void setFechaCreacion(Date fechaCreacion) {
        this.fechaCreacion = fechaCreacion;
    }

    public Date getFechaActualizacion() {
        return fechaActualizacion;
    }

    public void setFechaActualizacion(Date fechaActualizacion) {
        this.fechaActualizacion = fechaActualizacion;
    }

    public boolean isActivo() {
        return activo;
    }

    public void setActivo(boolean activo) {
        this.activo = activo;
    }

    public List<Stock> getStocks() {
        return stocks;
    }

    public void setStocks(List<Stock> stocks) {
        this.stocks = stocks;
    }

    public Integer getCantidadUnidadesBlister() {
        return CantidadUnidadesBlister;
    }

    public void setCantidadUnidadesBlister(Integer cantidadUnidadesBlister) {
        CantidadUnidadesBlister = cantidadUnidadesBlister;
    }

    public BigDecimal getPrecioVentaBlister() {
        return precioVentaBlister;
    }

    public void setPrecioVentaBlister(BigDecimal precioVentaBlister) {
        this.precioVentaBlister = precioVentaBlister;
    }
}