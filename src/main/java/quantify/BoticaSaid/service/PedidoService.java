package quantify.BoticaSaid.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import quantify.BoticaSaid.dto.pedido.AgregarStockConPedidoRequest;
import quantify.BoticaSaid.dto.pedido.PedidoReporteDTO;
import quantify.BoticaSaid.dto.stock.AgregarLoteRequest;
import quantify.BoticaSaid.model.Pedido;
import quantify.BoticaSaid.model.Producto;
import quantify.BoticaSaid.model.Proveedor;
import quantify.BoticaSaid.model.Stock;
import quantify.BoticaSaid.repository.PedidoRepository;
import quantify.BoticaSaid.repository.ProductoRepository;
import quantify.BoticaSaid.repository.StockRepository;

import java.time.LocalDate;
import java.time.LocalDateTime;
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

        // Obtener el proveedor del producto (puede ser null)
        Proveedor proveedor = producto.getProveedor();

        // Guardar los códigos de stock para identificar los stocks creados después
        List<String> codigosStock = new ArrayList<>();
        LocalDateTime timestampInicio = LocalDateTime.now();

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

        // Buscar los stocks recién creados basándonos en el timestamp
        LocalDateTime timestampFin = LocalDateTime.now().plusSeconds(1);
        List<Stock> stocksCreados = stockRepository.findByFechaCreacionBetweenWithProducto(timestampInicio, timestampFin);

        // Filtrar solo los stocks del producto actual
        stocksCreados = stocksCreados.stream()
            .filter(s -> s.getProducto().getId().equals(productoGuardado.getId()))
            .toList();

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

    /**
     * Obtener reporte de pedidos filtrado por proveedor y/o fecha de pedido
     * 
     * @param proveedorId ID del proveedor (opcional)
     * @param fechaPedido Fecha de pedido (opcional)
     * @return Lista de DTOs con información del reporte
     */
    public List<PedidoReporteDTO> obtenerReporte(Long proveedorId, LocalDate fechaPedido) {
        List<Pedido> pedidos = pedidoRepository.findByFilters(proveedorId, fechaPedido);
        
        return pedidos.stream()
            .map(this::convertirAPedidoReporteDTO)
            .collect(Collectors.toList());
    }

    /**
     * Convierte un Pedido a PedidoReporteDTO
     * 
     * @param pedido El pedido a convertir
     * @return DTO con la información del reporte
     */
    private PedidoReporteDTO convertirAPedidoReporteDTO(Pedido pedido) {
        PedidoReporteDTO dto = new PedidoReporteDTO();
        
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
        
        return dto;
    }
}
