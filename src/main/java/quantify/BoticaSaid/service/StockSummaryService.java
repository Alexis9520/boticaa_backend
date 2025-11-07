package quantify.BoticaSaid.service;

import quantify.BoticaSaid.dto.producto.ProductSummaryDTO;
import quantify.BoticaSaid.dto.stock.StockLoteDTO;
import quantify.BoticaSaid.model.Producto;
import quantify.BoticaSaid.model.Stock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class StockSummaryService {

    @Autowired
    private ProductoService productoService;

    /**
     * Devuelve Page<ProductSummaryDTO> usando productoService.buscarPaginadoPorQuery(q, lab, cat, pageable)
     */
    public Page<ProductSummaryDTO> getProductSummaries(String q, String lab, String cat, Pageable pageable) {
        Page<Producto> paged = productoService.buscarPaginadoPorQuery(q, lab, cat, pageable);

        Page<ProductSummaryDTO> pageDto = paged.map(prod -> {
            List<Stock> stocks = prod.getStocks() == null ? List.of() : prod.getStocks();
            List<StockLoteDTO> stockDtos = stocks.stream()
                    .map(productoService::toStockLoteDTO)
                    .collect(Collectors.toList());

            int cantidadGeneral = stocks.stream().mapToInt(s -> s.getCantidadUnidades()).sum();

            // Acumular costo total con BigDecimal (precioCompra es BigDecimal)
            BigDecimal costoTotalBd = stocks.stream()
                    .map(s -> {
                        BigDecimal precio = s.getPrecioCompra() == null ? BigDecimal.ZERO : s.getPrecioCompra();
                        BigDecimal cantidad = BigDecimal.valueOf(s.getCantidadUnidades());
                        return precio.multiply(cantidad);
                    })
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            // precio de venta: asumimos que Producto.getPrecioVentaUnd() devuelve BigDecimal
            BigDecimal precioVentaBd = prod.getPrecioVentaUnd() == null ? BigDecimal.ZERO : prod.getPrecioVentaUnd();

            double costoTotal = costoTotalBd.doubleValue();
            double precioVentaUnd = precioVentaBd.doubleValue();
            double costoPromedioUnit = cantidadGeneral > 0 ? costoTotal / cantidadGeneral : 0.0;
            double margenUnit = precioVentaUnd - costoPromedioUnit;
            double margenPct = costoPromedioUnit > 0 ? (margenUnit / costoPromedioUnit) * 100.0 : 0.0;
            double valorVentaTeorico = cantidadGeneral * precioVentaUnd;

            int unidadesVencidas = 0;
            int unidadesRiesgo30d = 0;
            Integer minDias = null;

            LocalDate hoy = LocalDate.now(java.time.ZoneId.of("America/Lima"));

            for (Stock s : stocks) {
                if (s.getFechaVencimiento() == null) continue;
                long dias = ChronoUnit.DAYS.between(hoy, s.getFechaVencimiento());
                if (minDias == null || dias < minDias) minDias = (int) dias;
                if (dias <= 0) unidadesVencidas += s.getCantidadUnidades();
                else if (dias <= 30) unidadesRiesgo30d += s.getCantidadUnidades();
            }

            int unidadesVigentes = cantidadGeneral - unidadesVencidas - unidadesRiesgo30d;
            double porcentajeEnRiesgo = cantidadGeneral > 0 ? ((unidadesVencidas + unidadesRiesgo30d) / (double) cantidadGeneral) * 100.0 : 0.0;

            ProductSummaryDTO dto = new ProductSummaryDTO();
            dto.codigoBarras = prod.getCodigoBarras();
            dto.nombre = prod.getNombre();
            dto.concentracion = prod.getConcentracion();
            dto.laboratorio = prod.getLaboratorio();
            dto.categoria = prod.getCategoria();
            dto.cantidadMinima = prod.getCantidadMinima() == null ? 0 : prod.getCantidadMinima();
            dto.cantidadGeneral = cantidadGeneral;
            dto.unidadesVencidas = unidadesVencidas;
            dto.unidadesRiesgo30d = unidadesRiesgo30d;
            dto.unidadesVigentes = unidadesVigentes;
            dto.diasHastaPrimerVencimiento = minDias;
            dto.numeroLotes = stockDtos.size();
            dto.costoTotal = costoTotal;
            dto.costoPromedioUnit = costoPromedioUnit;
            dto.precioVentaUnd = precioVentaUnd;
            dto.margenUnit = margenUnit;
            dto.margenPct = margenPct;
            dto.valorVentaTeorico = valorVentaTeorico;
            dto.porcentajeEnRiesgo = porcentajeEnRiesgo;
            dto.stocks = stockDtos;
            return dto;
        });

        return pageDto;
    }
}