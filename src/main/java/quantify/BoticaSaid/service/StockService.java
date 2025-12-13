package quantify.BoticaSaid.service;

import jakarta.transaction.Transactional;
import quantify.BoticaSaid.dto.common.PageResponse;
import quantify.BoticaSaid.dto.stock.StockItemDTO;
import quantify.BoticaSaid.model.Stock;
import quantify.BoticaSaid.model.Producto;
import quantify.BoticaSaid.repository.PedidoRepository;
import quantify.BoticaSaid.repository.ProductoRepository;
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
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class StockService {

    @Autowired
    private StockRepository stockRepository;

    @Autowired
    private PedidoRepository pedidoRepository;

    @Autowired
    private ProductoRepository productoRepository;

    private final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    private StockItemDTO mapToDto(Stock stock) {
        Producto producto = stock.getProducto();
        String fechaIso = stock.getFechaVencimiento() != null
                ? stock.getFechaVencimiento().format(DATE_FORMATTER)
                : null;
        return new StockItemDTO(
                stock.getId(),
                stock.getCodigoStock(),
                producto.getId(),
                producto.getNombre(),
                producto.getConcentracion(),
                stock.getCantidadUnidades(),
                producto.getCantidadMinima() != null ? producto.getCantidadMinima() : 0,
                stock.getPrecioCompra(),
                producto.getPrecioVentaUnd(),
                fechaIso,
                producto.getLaboratorio(),
                producto.getCategoria(),
                stock.getActivo());
    }

    public List<StockItemDTO> listarStockPorVencer(int dias) {
        LocalDate hoy = LocalDate.now();
        LocalDate limite = hoy.plusDays(dias);
        return stockRepository.findExpiringBetween(hoy, limite)
                .stream()
                .map(this::mapToDto)
                .toList();
    }

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
                    producto.getId(),
                    producto.getNombre(),
                    producto.getConcentracion(),
                    stock.getCantidadUnidades(),
                    producto.getCantidadMinima() != null ? producto.getCantidadMinima() : 0,
                    stock.getPrecioCompra(),
                    producto.getPrecioVentaUnd(),
                    fechaIso,
                    producto.getLaboratorio(),
                    producto.getCategoria(),
                    stock.getActivo());
        }).collect(Collectors.toList());
    }

    // NUEVO: paginado + filtros (q, lab, cat, codigo)
    public PageResponse<StockItemDTO> listarStockPaginado(String q, String lab, String cat, String codigo,
            Pageable pageable) {
        Specification<Stock> spec = (root, query, cb) -> {
            // join con producto (ajusta el nombre "producto" si tu entidad difiere)
            var producto = root.join("producto");

            List<Predicate> preds = new ArrayList<>();

            if (q != null && !q.isBlank()) {
                String like = "%" + q.trim().toLowerCase() + "%";
                preds.add(cb.or(
                        cb.like(cb.lower(producto.get("nombre")), like),
                        cb.like(cb.lower(producto.get("codigoBarras")), like)));
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
                    producto.getId(),
                    producto.getNombre(),
                    producto.getConcentracion(),
                    stock.getCantidadUnidades(),
                    producto.getCantidadMinima() != null ? producto.getCantidadMinima() : 0,
                    stock.getPrecioCompra(),
                    producto.getPrecioVentaUnd(),
                    fechaIso,
                    producto.getLaboratorio(),
                    producto.getCategoria(),
                    stock.getActivo());
        }).toList();

        return PageResponse.of(content, page.getTotalElements(), page.getNumber(), page.getSize(),
                page.getTotalPages());
    }

    @Transactional
    public int desactivarStocksVacios() {
        List<Stock> stocksVacios = stockRepository.findByCantidadUnidades(0);
        int count = 0;
        for (Stock stock : stocksVacios) {
            if (stock.getActivo() == null || stock.getActivo()) {
                stock.setActivo(false);
                stockRepository.save(stock);
                count++;
            }
        }
        return count;
    }

    @Transactional
    public void crearStock(StockItemDTO dto) {
        Producto producto = null;

        // Buscar por ID (ajusta el getter si tu DTO usa otro nombre)
        if (dto.getIdProducto() != null) {
            Optional<Producto> prodOpt = productoRepository.findByIdWithStocks(dto.getIdProducto());
            producto = prodOpt.orElseGet(() -> productoRepository.findById(dto.getIdProducto()).orElse(null));
        }

        if (producto == null || !Boolean.TRUE.equals(producto.isActivo())) {
            throw new RuntimeException("Producto no encontrado o inactivo");
        }

        Stock nuevoStock = new Stock();
        nuevoStock.setCodigoStock(dto.getCodigoStock());
        int cantidad = dto.getCantidadUnidades() != null ? dto.getCantidadUnidades() : 0;
        nuevoStock.setCantidadUnidades(cantidad);
        nuevoStock.setCantidadInicial(cantidad);

        // Parsear fecha (String -> LocalDate) de forma segura
        LocalDate fechaVenc = null;
        try {
            if (dto.getFechaVencimiento() != null && !dto.getFechaVencimiento().isBlank()) {
                fechaVenc = LocalDate.parse(dto.getFechaVencimiento(), DateTimeFormatter.ofPattern("yyyy-MM-dd"));
            }
        } catch (Exception ignored) {
            fechaVenc = null;
        }
        nuevoStock.setFechaVencimiento(fechaVenc);

        nuevoStock.setPrecioCompra(dto.getPrecioCompra());
        nuevoStock.setProducto(producto);

        // Asegurar lista de stocks inicializada
        if (producto.getStocks() == null) {
            producto.setStocks(new ArrayList<>());
        }
        producto.getStocks().add(nuevoStock);

        int actual = producto.getCantidadGeneral() != null ? producto.getCantidadGeneral() : 0;
        producto.setCantidadGeneral(actual + cantidad);

        // Guardar producto (su relación debe persistir el nuevo Stock según el mapping)
        productoRepository.save(producto);
    }

    // language: java
    @Transactional
    public void actualizarStock(int id, StockItemDTO dto) {
        // Validación: No se puede editar si pertenece a un pedido
        if (pedidoRepository.existsByStockId(id)) {
            throw new RuntimeException("No se puede editar este stock porque pertenece a un pedido existente.");
        }

        Stock stock = stockRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Stock no encontrado"));

        // guardar cantidad antigua (stock.getCantidadUnidades() es int primitivo)
        int cantidadAntigua = stock.getCantidadUnidades();
        int cantidadNueva = dto.getCantidadUnidades() != null ? dto.getCantidadUnidades() : 0;

        stock.setCodigoStock(dto.getCodigoStock());
        stock.setCantidadUnidades(cantidadNueva);
        stock.setPrecioCompra(dto.getPrecioCompra());

        try {
            if (dto.getFechaVencimiento() != null && !dto.getFechaVencimiento().isBlank()) {
                LocalDate fecha = LocalDate.parse(dto.getFechaVencimiento(), DateTimeFormatter.ofPattern("yyyy-MM-dd"));
                stock.setFechaVencimiento(fecha);
            } else {
                stock.setFechaVencimiento(null);
            }
        } catch (Exception e) {
            stock.setFechaVencimiento(null);
        }

        stockRepository.save(stock);

        // Actualizar cantidadGeneral del producto relacionado
        Producto producto = stock.getProducto();
        if (producto != null) {
            int actual = producto.getCantidadGeneral() != null ? producto.getCantidadGeneral() : 0;
            int delta = cantidadNueva - cantidadAntigua;
            producto.setCantidadGeneral(Math.max(0, actual + delta));
            productoRepository.save(producto);
        }
    }

    @Transactional
    public void eliminarStock(int id) {
        // Validación: No se puede eliminar si pertenece a un pedido
        if (pedidoRepository.existsByStockId(id)) {
            throw new RuntimeException("No se puede eliminar este stock porque pertenece a un pedido existente.");
        }

        Stock stock = stockRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Stock no encontrado"));

        Producto producto = stock.getProducto();
        // stock.getCantidadUnidades() es int primitivo -> usar directamente
        int cantidad = stock.getCantidadUnidades();

        // Restar cantidad del producto antes de eliminar el stock
        if (producto != null) {
            int actual = producto.getCantidadGeneral() != null ? producto.getCantidadGeneral() : 0;
            producto.setCantidadGeneral(Math.max(0, actual - cantidad));
            productoRepository.save(producto);
        }

        // Eliminar el stock
        stockRepository.deleteById(id);
    }
}