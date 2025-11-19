package quantify.BoticaSaid.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import quantify.BoticaSaid.dto.pedido.AgregarStockConPedidoRequest;
import quantify.BoticaSaid.dto.producto.ProductoRequest;
import quantify.BoticaSaid.dto.proveedor.ProveedorRequest;
import quantify.BoticaSaid.dto.stock.AgregarLoteRequest;
import quantify.BoticaSaid.model.Pedido;
import quantify.BoticaSaid.model.Producto;
import quantify.BoticaSaid.model.Proveedor;
import quantify.BoticaSaid.repository.PedidoRepository;
import quantify.BoticaSaid.repository.ProductoRepository;
import quantify.BoticaSaid.repository.ProveedorRepository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class PedidoServiceTest {

    @Autowired
    private PedidoService pedidoService;

    @Autowired
    private ProductoService productoService;

    @Autowired
    private ProveedorService proveedorService;

    @Autowired
    private PedidoRepository pedidoRepository;

    @Autowired
    private ProductoRepository productoRepository;

    @Autowired
    private ProveedorRepository proveedorRepository;

    @BeforeEach
    void setUp() {
        pedidoRepository.deleteAll();
        productoRepository.deleteAll();
        proveedorRepository.deleteAll();
    }

    @Test
    void testAgregarStockConPedidoExitoso() {
        // Crear proveedor
        ProveedorRequest proveedorRequest = new ProveedorRequest();
        proveedorRequest.setRuc("20123456789");
        proveedorRequest.setRazonComercial("Distribuidora ABC");
        Proveedor proveedor = proveedorService.crearProveedor(proveedorRequest);

        // Crear producto con proveedor
        ProductoRequest productoRequest = new ProductoRequest();
        productoRequest.setNombre("Paracetamol 500mg");
        productoRequest.setCodigoBarras("1234567890");
        productoRequest.setProveedorId(proveedor.getId());
        Object resultado = productoService.crearProductoConStock(productoRequest);
        Producto producto = (Producto) resultado;

        // Crear request para agregar stock con pedido
        AgregarStockConPedidoRequest request = new AgregarStockConPedidoRequest();
        request.setFechaDePedido(LocalDate.now());

        AgregarLoteRequest stockData = new AgregarLoteRequest();
        stockData.setProductoId(producto.getId());

        AgregarLoteRequest.LoteItem lote1 = new AgregarLoteRequest.LoteItem();
        lote1.setCodigoStock("LOTE001");
        lote1.setCantidadUnidades(100);
        lote1.setFechaVencimiento(LocalDate.now().plusYears(1));
        lote1.setPrecioCompra(new BigDecimal("10.50"));

        AgregarLoteRequest.LoteItem lote2 = new AgregarLoteRequest.LoteItem();
        lote2.setCodigoStock("LOTE002");
        lote2.setCantidadUnidades(150);
        lote2.setFechaVencimiento(LocalDate.now().plusYears(2));
        lote2.setPrecioCompra(new BigDecimal("11.00"));

        stockData.setLotes(Arrays.asList(lote1, lote2));
        request.setStockData(stockData);

        // Ejecutar el servicio
        boolean exito = pedidoService.agregarStockConPedido(request);

        // Verificaciones
        assertTrue(exito, "El servicio debería retornar true");

        // Verificar que se crearon los pedidos
        List<Pedido> pedidos = pedidoRepository.findAll();
        assertEquals(2, pedidos.size(), "Deberían haberse creado 2 pedidos (uno por lote)");

        // Verificar los datos de los pedidos
        for (Pedido pedido : pedidos) {
            assertNotNull(pedido.getStock(), "El pedido debe tener un stock asociado");
            assertNotNull(pedido.getProducto(), "El pedido debe tener un producto asociado");
            assertNotNull(pedido.getProveedor(), "El pedido debe tener un proveedor asociado");
            assertEquals(LocalDate.now(), pedido.getFechaDePedido(), "La fecha de pedido debe coincidir");
            assertEquals(producto.getId(), pedido.getProducto().getId(), "El ID del producto debe coincidir");
            assertEquals(proveedor.getId(), pedido.getProveedor().getId(), "El ID del proveedor debe coincidir");
        }

        // Verificar que se agregó el stock al producto
        Producto productoActualizado = productoService.buscarPorId(producto.getId());
        assertEquals(250, productoActualizado.getCantidadGeneral(), "La cantidad general debe ser 250 (100 + 150)");
        assertEquals(2, productoActualizado.getStocks().size(), "Deberían existir 2 lotes de stock");
    }

    @Test
    void testAgregarStockConPedidoSinProveedor() {
        // Crear producto sin proveedor
        ProductoRequest productoRequest = new ProductoRequest();
        productoRequest.setNombre("Ibuprofeno 400mg");
        productoRequest.setCodigoBarras("9876543210");
        Object resultado = productoService.crearProductoConStock(productoRequest);
        Producto producto = (Producto) resultado;

        // Crear request para agregar stock con pedido
        AgregarStockConPedidoRequest request = new AgregarStockConPedidoRequest();
        request.setFechaDePedido(LocalDate.now().minusDays(1));

        AgregarLoteRequest stockData = new AgregarLoteRequest();
        stockData.setProductoId(producto.getId());

        AgregarLoteRequest.LoteItem lote = new AgregarLoteRequest.LoteItem();
        lote.setCodigoStock("LOTE003");
        lote.setCantidadUnidades(75);
        lote.setFechaVencimiento(LocalDate.now().plusMonths(6));
        lote.setPrecioCompra(new BigDecimal("8.75"));

        stockData.setLotes(Arrays.asList(lote));
        request.setStockData(stockData);

        // Ejecutar el servicio
        boolean exito = pedidoService.agregarStockConPedido(request);

        // Verificaciones
        assertTrue(exito, "El servicio debería retornar true aunque no haya proveedor");

        // Verificar que se creó el pedido
        List<Pedido> pedidos = pedidoRepository.findAll();
        assertEquals(1, pedidos.size(), "Debería haberse creado 1 pedido");

        Pedido pedido = pedidos.get(0);
        assertNotNull(pedido.getStock(), "El pedido debe tener un stock asociado");
        assertNotNull(pedido.getProducto(), "El pedido debe tener un producto asociado");
        assertNull(pedido.getProveedor(), "El pedido NO debe tener un proveedor (producto sin proveedor)");
        assertEquals(producto.getId(), pedido.getProducto().getId(), "El ID del producto debe coincidir");
    }

    @Test
    void testAgregarStockConPedidoConCodigoBarras() {
        // Crear producto
        ProductoRequest productoRequest = new ProductoRequest();
        productoRequest.setNombre("Aspirina 100mg");
        productoRequest.setCodigoBarras("5555555555");
        productoService.crearProductoConStock(productoRequest);

        // Crear request usando código de barras
        AgregarStockConPedidoRequest request = new AgregarStockConPedidoRequest();
        request.setFechaDePedido(LocalDate.now());

        AgregarLoteRequest stockData = new AgregarLoteRequest();
        stockData.setCodigoBarras("5555555555"); // Usar código de barras en lugar de ID

        AgregarLoteRequest.LoteItem lote = new AgregarLoteRequest.LoteItem();
        lote.setCodigoStock("LOTE004");
        lote.setCantidadUnidades(50);
        lote.setFechaVencimiento(LocalDate.now().plusYears(1));
        lote.setPrecioCompra(new BigDecimal("5.25"));

        stockData.setLotes(Arrays.asList(lote));
        request.setStockData(stockData);

        // Ejecutar el servicio
        boolean exito = pedidoService.agregarStockConPedido(request);

        // Verificaciones
        assertTrue(exito, "El servicio debería funcionar con código de barras");

        List<Pedido> pedidos = pedidoRepository.findAll();
        assertEquals(1, pedidos.size(), "Debería haberse creado 1 pedido");
    }

    @Test
    void testAgregarStockConPedidoProductoInexistente() {
        // Crear request con producto inexistente
        AgregarStockConPedidoRequest request = new AgregarStockConPedidoRequest();
        request.setFechaDePedido(LocalDate.now());

        AgregarLoteRequest stockData = new AgregarLoteRequest();
        stockData.setProductoId(99999L); // ID inexistente

        AgregarLoteRequest.LoteItem lote = new AgregarLoteRequest.LoteItem();
        lote.setCodigoStock("LOTE005");
        lote.setCantidadUnidades(10);
        lote.setFechaVencimiento(LocalDate.now().plusDays(30));
        lote.setPrecioCompra(new BigDecimal("1.00"));

        stockData.setLotes(Arrays.asList(lote));
        request.setStockData(stockData);

        // Ejecutar el servicio
        boolean exito = pedidoService.agregarStockConPedido(request);

        // Verificaciones
        assertFalse(exito, "El servicio debería retornar false con producto inexistente");

        List<Pedido> pedidos = pedidoRepository.findAll();
        assertEquals(0, pedidos.size(), "No debería haberse creado ningún pedido");
    }

    @Test
    void testAgregarStockConPedidoSinDatos() {
        // Request vacío
        AgregarStockConPedidoRequest request = new AgregarStockConPedidoRequest();

        boolean exito = pedidoService.agregarStockConPedido(request);

        assertFalse(exito, "El servicio debería retornar false sin datos");

        List<Pedido> pedidos = pedidoRepository.findAll();
        assertEquals(0, pedidos.size(), "No debería haberse creado ningún pedido");
    }
}
