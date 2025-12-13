package quantify.BoticaSaid.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import quantify.BoticaSaid.dto.dashboard.DashboardResumenDTO;
import quantify.BoticaSaid.dto.pedido.ActualizarPedidoRequest;
import quantify.BoticaSaid.dto.pedido.ActualizarStockRequest;
import quantify.BoticaSaid.dto.pedido.AgregarStockConPedidoRequest;
import quantify.BoticaSaid.dto.pedido.PedidoReporteDTO;
import quantify.BoticaSaid.dto.stock.AgregarLoteRequest;
import quantify.BoticaSaid.model.Pedido;
import quantify.BoticaSaid.model.Producto;
import quantify.BoticaSaid.model.Proveedor;
import quantify.BoticaSaid.model.ProductoProveedor;
import quantify.BoticaSaid.model.Stock;
import quantify.BoticaSaid.repository.PedidoRepository;
import quantify.BoticaSaid.repository.ProductoRepository;
import quantify.BoticaSaid.repository.StockRepository;
import quantify.BoticaSaid.repository.ProveedorRepository;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class PedidoService {

    @Autowired
    private PedidoRepository pedidoRepository;

    @Autowired
    private ProductoRepository productoRepository;

    @Autowired
    private StockRepository stockRepository;

    @Autowired
    private ProductoService productoService;

    @Autowired
    private ProveedorRepository proveedorRepository;

    /**
     * Agregar stock a un producto y crear registros de pedido para cada lote
     *
     * @param request contiene los datos del stock y la fecha de pedido
     * @return true si se agregó el stock y se crearon los pedidos exitosamente
     */
    @Transactional
    public boolean agregarStockConPedido(AgregarStockConPedidoRequest request) {
        if (request.getStockData() == null || request.getFechaDePedido() == null) {
            return false;
        }

        AgregarLoteRequest stockData = request.getStockData();

        // Buscar el producto
        Producto producto = null;
        if (stockData.getProductoId() != null) {
            Optional<Producto> prodOpt = productoRepository.findByIdWithStocks(stockData.getProductoId());
            producto = prodOpt.orElseGet(() -> productoRepository.findById(stockData.getProductoId()).orElse(null));
        }

        if (producto == null && stockData.getCodigoBarras() != null && !stockData.getCodigoBarras().isBlank()) {
            Optional<Producto> prodOpt = productoRepository.findByCodigoBarrasWithStocks(stockData.getCodigoBarras());
            producto = prodOpt.orElseGet(() -> productoRepository.findByCodigoBarras(stockData.getCodigoBarras()));
        }

        if (producto == null || !producto.isActivo()) {
            return false;
        }

        if (stockData.getLotes() == null || stockData.getLotes().isEmpty()) {
            return false;
        }

        // Obtener el proveedor: usar el proveedorId del request o el primer proveedor
        // del producto
        Proveedor proveedor = null;
        if (request.getProveedorId() != null) {
            // Buscar proveedor por ID del request
            proveedor = producto.getProductoProveedores().stream()
                    .map(pp -> pp.getProveedor())
                    .filter(p -> p.getId().equals(request.getProveedorId()))
                    .findFirst()
                    .orElse(null);
        } else if (!producto.getProductoProveedores().isEmpty()) {
            // Tomar el primer proveedor si no se especificó uno
            proveedor = producto.getProductoProveedores().get(0).getProveedor();
        }

        // Guardar los códigos de stock para identificar los stocks creados después
        List<String> codigosStock = new ArrayList<>();

        int totalUnidades = 0;
        for (var loteItem : stockData.getLotes()) {
            Stock nuevoStock = new Stock();
            nuevoStock.setCodigoStock(loteItem.getCodigoStock());
            int cant = loteItem.getCantidadUnidades();
            nuevoStock.setCantidadUnidades(cant);
            nuevoStock.setCantidadInicial(cant);
            nuevoStock.setFechaVencimiento(loteItem.getFechaVencimiento());
            nuevoStock.setPrecioCompra(loteItem.getPrecioCompra());
            nuevoStock.setProducto(producto);

            producto.getStocks().add(nuevoStock);
            codigosStock.add(loteItem.getCodigoStock());
            totalUnidades += cant;
        }

        // Actualizar cantidad general del producto
        int actual = producto.getCantidadGeneral() != null ? producto.getCantidadGeneral() : 0;
        producto.setCantidadGeneral(actual + totalUnidades);

        // Guardar el producto (esto guardará en cascada los stocks)
        Producto productoGuardado = productoRepository.save(producto);

        // Buscar los stocks por los códigos que acabamos de crear
        List<Stock> stocksCreados = stockRepository.findByCodigoStockInAndProductoId(
                codigosStock, productoGuardado.getId());

        // Crear un pedido por cada stock creado
        for (Stock stock : stocksCreados) {
            Pedido pedido = new Pedido();
            pedido.setStock(stock);
            pedido.setProducto(productoGuardado);
            pedido.setProveedor(proveedor);
            pedido.setFechaDePedido(request.getFechaDePedido());

            pedidoRepository.save(pedido);
        }

        return true;
    }

    public List<PedidoReporteDTO> obtenerReporte(Long proveedorId, LocalDate fechaPedido) {
        List<Pedido> pedidos = pedidoRepository.findByFilters(proveedorId, fechaPedido);

        return pedidos.stream()
                .map(this::convertirAPedidoReporteDTO)
                .collect(Collectors.toList());
    }

    private PedidoReporteDTO convertirAPedidoReporteDTO(Pedido pedido) {
        PedidoReporteDTO dto = new PedidoReporteDTO();

        // Asignar id del pedido (en lugar del id del producto)
        dto.setPedidoId(pedido != null ? pedido.getId() : null);

        // Datos del producto
        Producto producto = pedido.getProducto();
        if (producto != null) {
            dto.setCodigoBarras(producto.getCodigoBarras());
            dto.setProducto(producto.getNombre());
            dto.setConcentracion(producto.getConcentracion());
            dto.setPresentacion(producto.getPresentacion());
        }

        // Datos del stock
        Stock stock = pedido.getStock();
        if (stock != null) {
            dto.setCodigoStock(stock.getCodigoStock());
            dto.setCantUnidades(stock.getCantidadUnidades());
            dto.setCantInicial(stock.getCantidadInicial());
            dto.setFVencimiento(stock.getFechaVencimiento());
            dto.setPrecioCompra(stock.getPrecioCompra());
            dto.setFCreacion(stock.getFechaCreacion());
        }

        // Fecha del pedido
        dto.setFechaDePedido(pedido.getFechaDePedido());

        return dto;
    }

    @Transactional
    public boolean actualizarPedido(Long pedidoId, ActualizarPedidoRequest request) {
        Optional<Pedido> optPedido = pedidoRepository.findById(pedidoId);
        if (optPedido.isEmpty()) {
            return false;
        }
        Pedido pedido = optPedido.get();
        Stock stock = pedido.getStock();
        Producto producto = pedido.getProducto();

        if (stock == null || producto == null) {
            return false;
        }

        ActualizarStockRequest s = request.getStock();
        boolean cualquierCambio = false;

        if (s != null) {
            // cantidadInicial
            if (s.getCantidadInicial() != null) {
                int newInicial = s.getCantidadInicial();
                Integer oldInicialObj = stock.getCantidadInicial();
                int oldInicial = oldInicialObj != null ? oldInicialObj : 0;

                Integer currentUnidadesObj = stock.getCantidadUnidades();
                int currentUnidades = currentUnidadesObj != null ? currentUnidadesObj : 0;

                int deltaInicial = newInicial - oldInicial;
                int recalculatedUnidades = currentUnidades + deltaInicial;
                if (recalculatedUnidades < 0)
                    recalculatedUnidades = 0;

                int currentGeneral = producto.getCantidadGeneral() != null ? producto.getCantidadGeneral() : 0;
                int diffUnidades = recalculatedUnidades - currentUnidades;
                int nuevoGeneral = currentGeneral + diffUnidades;
                if (nuevoGeneral < 0)
                    nuevoGeneral = 0;

                producto.setCantidadGeneral(nuevoGeneral);
                stock.setCantidadInicial(newInicial);
                stock.setCantidadUnidades(recalculatedUnidades);

                cualquierCambio = true;
            }

            // fecha de vencimiento
            if (s.getFechaVencimiento() != null) {
                stock.setFechaVencimiento(s.getFechaVencimiento());
                cualquierCambio = true;
            }

            // precio de compra (ya es BigDecimal en el DTO)
            if (s.getPrecioCompra() != null) {
                stock.setPrecioCompra(s.getPrecioCompra());
                cualquierCambio = true;
            }

            // opcional: código de lote
            if (s.getCodigoStock() != null) {
                stock.setCodigoStock(s.getCodigoStock());
                cualquierCambio = true;
            }
        }

        // fecha de pedido en el pedido
        if (request.getFechaDePedido() != null) {
            pedido.setFechaDePedido(request.getFechaDePedido());
            cualquierCambio = true;
        }

        if (cualquierCambio) {
            productoRepository.save(producto);
            stockRepository.save(stock);
            pedidoRepository.save(pedido);
            return true;
        }

        return false;
    }

    @Transactional
    public boolean eliminarPedido(Long pedidoId) {
        Optional<Pedido> optPedido = pedidoRepository.findById(pedidoId);
        if (optPedido.isEmpty()) {
            return false;
        }
        Pedido pedido = optPedido.get();
        Stock stock = pedido.getStock();
        Producto producto = pedido.getProducto();

        if (producto != null && stock != null) {
            int qty = stock.getCantidadUnidades(); // primitivo
            Integer currentGeneral = producto.getCantidadGeneral() != null ? producto.getCantidadGeneral() : 0;
            producto.setCantidadGeneral(Math.max(0, currentGeneral - qty));

            // Remover stock de la lista del producto si existe (usar Objects.equals para
            // seguridad con primitivos/objetos)
            producto.getStocks().removeIf(pStock -> java.util.Objects.equals(pStock.getId(), stock.getId()));
            productoRepository.save(producto);
        }

        // Eliminar pedido primero (para no romper FK) y luego el stock
        pedidoRepository.delete(pedido);
        if (stock != null) {
            stockRepository.delete(stock);
        }

        return true;
    }

    public DashboardResumenDTO.PedidosMetricasDTO buildPedidosMetricas() {
        LocalDate hoy = LocalDate.now();
        LocalDate inicioMes = hoy.withDayOfMonth(1);
        LocalDate hace30Dias = hoy.minusDays(29);

        DashboardResumenDTO.PedidosMetricasDTO dto = new DashboardResumenDTO.PedidosMetricasDTO();
        dto.totalHoy = pedidoRepository.countByFechaDePedido(hoy);
        dto.totalMes = pedidoRepository.countByFechaDePedidoBetween(inicioMes, hoy);

        LocalDate inicioMesAnterior = inicioMes.minusMonths(1);
        LocalDate finMesAnterior = inicioMes.minusDays(1);
        long totalMesAnterior = pedidoRepository.countByFechaDePedidoBetween(inicioMesAnterior, finMesAnterior);
        dto.variacionMes = totalMesAnterior == 0 ? 100.0
                : ((double) (dto.totalMes - totalMesAnterior) / totalMesAnterior) * 100;

        List<Pedido> pedidosConStock = pedidoRepository.findWithStockBetween(hace30Dias, hoy);
        dto.leadTimePromedioDias = calcularLeadTimePromedio(pedidosConStock);

        dto.serieUltimosDias = pedidoRepository.countByFechaGrouped(hace30Dias, hoy).stream()
                .map(row -> {
                    DashboardResumenDTO.SerieTemporalDTO punto = new DashboardResumenDTO.SerieTemporalDTO();
                    punto.etiqueta = row[0].toString();
                    punto.total = ((Number) row[1]).longValue();
                    return punto;
                }).toList();

        dto.pedidosRecientes = pedidoRepository.findLatestPedidos(PageRequest.of(0, 5)).stream()
                .map(this::toPedidoDetalleDTO)
                .toList();
        return dto;
    }

    private double calcularLeadTimePromedio(List<Pedido> pedidos) {
        if (pedidos.isEmpty()) {
            return 0.0;
        }
        double totalDias = pedidos.stream()
                .filter(p -> p.getStock() != null && p.getStock().getFechaCreacion() != null)
                .mapToDouble(p -> java.time.temporal.ChronoUnit.DAYS
                        .between(p.getFechaDePedido(), p.getStock().getFechaCreacion().toLocalDate()))
                .filter(d -> d >= 0)
                .average()
                .orElse(0.0);
        return Math.round(totalDias * 10d) / 10d;
    }

    private DashboardResumenDTO.PedidoDetalleDTO toPedidoDetalleDTO(Pedido pedido) {
        DashboardResumenDTO.PedidoDetalleDTO dto = new DashboardResumenDTO.PedidoDetalleDTO();
        dto.pedidoId = pedido.getId();
        dto.proveedor = pedido.getProveedor() != null ? pedido.getProveedor().getRazonComercial() : "-";
        dto.producto = pedido.getProducto() != null ? pedido.getProducto().getNombre() : "-";
        dto.fechaPedido = pedido.getFechaDePedido() != null ? pedido.getFechaDePedido().toString() : null;
        Stock stock = pedido.getStock();
        dto.unidades = stock != null ? stock.getCantidadUnidades() : null;
        if (stock != null && stock.getFechaCreacion() != null && pedido.getFechaDePedido() != null) {
            long dias = java.time.temporal.ChronoUnit.DAYS
                    .between(pedido.getFechaDePedido(), stock.getFechaCreacion().toLocalDate());
            dto.leadTimeDias = (int) Math.max(dias, 0);
        }
        return dto;
    }

    public DashboardResumenDTO.ProveedoresMetricasDTO buildProveedoresMetricas() {
        LocalDate hoy = LocalDate.now();
        LocalDate hace30Dias = hoy.minusDays(30);
        LocalDate hace90Dias = hoy.minusDays(90);

        DashboardResumenDTO.ProveedoresMetricasDTO dto = new DashboardResumenDTO.ProveedoresMetricasDTO();
        dto.activos = proveedorRepository.countByActivoTrue();
        dto.conPedidos30Dias = pedidoRepository.findProveedorIdsBetween(hace30Dias, hoy).size();
        dto.sinPedidos90Dias = Math.max(0,
                dto.activos - pedidoRepository.findProveedorIdsBetween(hace90Dias, hoy).size());

        List<Pedido> pedidosVentana = pedidoRepository.findWithStockBetween(hace90Dias, hoy);
        dto.leadTimePromedioDias = calcularLeadTimePromedio(pedidosVentana);

        dto.topProveedores = pedidoRepository.topProveedoresPorPedidos(hace90Dias, hoy, PageRequest.of(0, 5)).stream()
                .map(row -> {
                    DashboardResumenDTO.ProveedorRankingDTO ranking = new DashboardResumenDTO.ProveedorRankingDTO();
                    ranking.proveedorId = ((Number) row[0]).longValue();
                    ranking.nombre = (String) row[1];
                    ranking.pedidos = ((Number) row[2]).longValue();
                    return ranking;
                }).toList();

        return dto;
    }
}
