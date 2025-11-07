package quantify.BoticaSaid.dto.producto;

import quantify.BoticaSaid.dto.stock.StockLoteDTO;
import java.util.List;

public class ProductSummaryDTO {
    public String codigoBarras;
    public String nombre;
    public String concentracion;
    public String laboratorio;
    public String categoria;
    public int cantidadMinima;
    public int cantidadGeneral;
    public int unidadesVencidas;
    public int unidadesRiesgo30d;
    public int unidadesVigentes;
    public Integer diasHastaPrimerVencimiento; // nullable
    public int numeroLotes;
    public double costoTotal;
    public double costoPromedioUnit;
    public double precioVentaUnd;
    public double margenUnit;
    public double margenPct;
    public double valorVentaTeorico;
    public double porcentajeEnRiesgo;
    public List<StockLoteDTO> stocks;
}