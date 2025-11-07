package quantify.BoticaSaid.service;

import quantify.BoticaSaid.dto.common.PageResponse;
import quantify.BoticaSaid.dto.stock.StockItemDTO;
import quantify.BoticaSaid.model.Stock;
import quantify.BoticaSaid.model.Producto;
import quantify.BoticaSaid.repository.StockRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import jakarta.persistence.criteria.Predicate;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class StockService {

    @Autowired
    private StockRepository stockRepository;

    // LEGACY (devuelve todo). Evita usarlo en listas grandes.
    public List<StockItemDTO> listarStock() {
        List<Stock> stocks = stockRepository.findAllWithProducto();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

        return stocks.stream().map(stock -> {
            Producto producto = stock.getProducto();
            String fechaIso = stock.getFechaVencimiento() != null
                    ? stock.getFechaVencimiento().format(formatter)
                    : null;
            return new StockItemDTO(
                    stock.getId(),
                    stock.getCodigoStock(),
                    producto.getCodigoBarras(),
                    producto.getNombre(),
                    producto.getConcentracion(),
                    stock.getCantidadUnidades(),
                    producto.getCantidadMinima() != null ? producto.getCantidadMinima() : 0,
                    stock.getPrecioCompra(),
                    producto.getPrecioVentaUnd(),
                    fechaIso,
                    producto.getLaboratorio(),
                    producto.getCategoria()
            );
        }).collect(Collectors.toList());
    }

    // NUEVO: paginado + filtros (q, lab, cat, codigo)
    public PageResponse<StockItemDTO> listarStockPaginado(String q, String lab, String cat, String codigo, Pageable pageable) {
        Specification<Stock> spec = (root, query, cb) -> {
            // join con producto (ajusta el nombre "producto" si tu entidad difiere)
            var producto = root.join("producto");

            List<Predicate> preds = new ArrayList<>();

            if (q != null && !q.isBlank()) {
                String like = "%" + q.trim().toLowerCase() + "%";
                preds.add(cb.or(
                        cb.like(cb.lower(producto.get("nombre")), like),
                        cb.like(cb.lower(producto.get("codigoBarras")), like)
                ));
            }
            if (codigo != null && !codigo.isBlank()) {
                preds.add(cb.equal(producto.get("codigoBarras"), codigo.trim()));
            }
            if (lab != null && !lab.isBlank()) {
                preds.add(cb.equal(producto.get("laboratorio"), lab.trim()));
            }
            if (cat != null && !cat.isBlank()) {
                preds.add(cb.equal(producto.get("categoria"), cat.trim()));
            }
            return preds.isEmpty() ? cb.conjunction() : cb.and(preds.toArray(new Predicate[0]));
        };

        var page = stockRepository.findAll(spec, pageable);

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        var content = page.getContent().stream().map(stock -> {
            Producto producto = stock.getProducto();
            String fechaIso = stock.getFechaVencimiento() != null
                    ? stock.getFechaVencimiento().format(formatter)
                    : null;
            return new StockItemDTO(
                    stock.getId(),
                    stock.getCodigoStock(),
                    producto.getCodigoBarras(),
                    producto.getNombre(),
                    producto.getConcentracion(),
                    stock.getCantidadUnidades(),
                    producto.getCantidadMinima() != null ? producto.getCantidadMinima() : 0,
                    stock.getPrecioCompra(),
                    producto.getPrecioVentaUnd(),
                    fechaIso,
                    producto.getLaboratorio(),
                    producto.getCategoria()
            );
        }).toList();

        return PageResponse.of(content, page.getTotalElements(), page.getNumber(), page.getSize(), page.getTotalPages());
    }

    public void actualizarStock(int id, StockItemDTO dto) {
        Stock stock = stockRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Stock no encontrado"));
        stock.setCodigoStock(dto.getCodigoStock());
        stock.setCantidadUnidades(dto.getCantidadUnidades());
        stock.setPrecioCompra(dto.getPrecioCompra());

        try {
            if (dto.getFechaVencimiento() != null) {
                LocalDate fecha = LocalDate.parse(dto.getFechaVencimiento(), DateTimeFormatter.ofPattern("yyyy-MM-dd"));
                stock.setFechaVencimiento(fecha);
            }
        } catch (Exception e) {
            stock.setFechaVencimiento(null);
        }
        stockRepository.save(stock);
    }
}