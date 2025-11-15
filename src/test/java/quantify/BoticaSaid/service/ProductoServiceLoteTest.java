package quantify.BoticaSaid.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import quantify.BoticaSaid.dto.producto.ProductoRequest;
import quantify.BoticaSaid.dto.stock.AgregarLoteRequest;
import quantify.BoticaSaid.model.Producto;
import quantify.BoticaSaid.repository.ProductoRepository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class ProductoServiceLoteTest {

    @Autowired
    private ProductoService productoService;

    @Autowired
    private ProductoRepository productoRepository;

    @BeforeEach
    void setUp() {
        productoRepository.deleteAll();
    }

    @Test
    void testAgregarLoteUnicoConProductoId() {
        // Crear producto
        ProductoRequest request = new ProductoRequest();
        request.setNombre("Paracetamol");
        request.setCodigoBarras("1234567890");
        Object resultado = productoService.crearProductoConStock(request);
        Producto producto = (Producto) resultado;

        // Agregar lote
        AgregarLoteRequest loteRequest = new AgregarLoteRequest();
        loteRequest.setProductoId(producto.getId());
        
        AgregarLoteRequest.LoteItem lote = new AgregarLoteRequest.LoteItem();
        lote.setCodigoStock("LOTE001");
        lote.setCantidadUnidades(100);
        lote.setFechaVencimiento(LocalDate.now().plusYears(1));
        lote.setPrecioCompra(new BigDecimal("10.50"));
        
        loteRequest.setLotes(Arrays.asList(lote));

        boolean exito = productoService.agregarLotes(loteRequest);

        assertTrue(exito);
        
        Producto actualizado = productoService.buscarPorId(producto.getId());
        assertEquals(100, actualizado.getCantidadGeneral());
        assertEquals(1, actualizado.getStocks().size());
    }

    @Test
    void testAgregarMultiplesLotesConCodigoBarras() {
        // Crear producto
        ProductoRequest request = new ProductoRequest();
        request.setNombre("Ibuprofeno");
        request.setCodigoBarras("9876543210");
        productoService.crearProductoConStock(request);

        // Agregar múltiples lotes
        AgregarLoteRequest loteRequest = new AgregarLoteRequest();
        loteRequest.setCodigoBarras("9876543210");
        
        AgregarLoteRequest.LoteItem lote1 = new AgregarLoteRequest.LoteItem();
        lote1.setCodigoStock("LOTE001");
        lote1.setCantidadUnidades(50);
        lote1.setFechaVencimiento(LocalDate.now().plusYears(1));
        lote1.setPrecioCompra(new BigDecimal("8.00"));
        
        AgregarLoteRequest.LoteItem lote2 = new AgregarLoteRequest.LoteItem();
        lote2.setCodigoStock("LOTE002");
        lote2.setCantidadUnidades(75);
        lote2.setFechaVencimiento(LocalDate.now().plusYears(2));
        lote2.setPrecioCompra(new BigDecimal("9.00"));
        
        loteRequest.setLotes(Arrays.asList(lote1, lote2));

        boolean exito = productoService.agregarLotes(loteRequest);

        assertTrue(exito);
        
        Producto actualizado = productoService.buscarPorCodigoBarras("9876543210");
        assertEquals(125, actualizado.getCantidadGeneral());
        assertEquals(2, actualizado.getStocks().size());
    }

    @Test
    void testAgregarLoteSinModificarDatosProducto() {
        // Crear producto con datos específicos
        ProductoRequest request = new ProductoRequest();
        request.setNombre("Aspirina Original");
        request.setCodigoBarras("5555555555");
        request.setLaboratorio("Lab Original");
        request.setPrecioVentaUnd(new BigDecimal("15.00"));
        Object resultado = productoService.crearProductoConStock(request);
        Producto producto = (Producto) resultado;

        // Agregar lote (NO debe cambiar nombre, laboratorio, precio)
        AgregarLoteRequest loteRequest = new AgregarLoteRequest();
        loteRequest.setProductoId(producto.getId());
        
        AgregarLoteRequest.LoteItem lote = new AgregarLoteRequest.LoteItem();
        lote.setCodigoStock("LOTE999");
        lote.setCantidadUnidades(200);
        lote.setFechaVencimiento(LocalDate.now().plusMonths(6));
        lote.setPrecioCompra(new BigDecimal("5.00"));
        
        loteRequest.setLotes(Arrays.asList(lote));

        productoService.agregarLotes(loteRequest);

        // Verificar que los datos del producto NO cambiaron
        Producto actualizado = productoService.buscarPorId(producto.getId());
        assertEquals("Aspirina Original", actualizado.getNombre());
        assertEquals("Lab Original", actualizado.getLaboratorio());
        assertEquals(new BigDecimal("15.00"), actualizado.getPrecioVentaUnd());
        assertEquals(200, actualizado.getCantidadGeneral());
    }

    @Test
    void testAgregarLoteProductoInexistente() {
        AgregarLoteRequest loteRequest = new AgregarLoteRequest();
        loteRequest.setProductoId(9999L);
        
        AgregarLoteRequest.LoteItem lote = new AgregarLoteRequest.LoteItem();
        lote.setCantidadUnidades(100);
        lote.setFechaVencimiento(LocalDate.now().plusYears(1));
        
        loteRequest.setLotes(Arrays.asList(lote));

        boolean exito = productoService.agregarLotes(loteRequest);

        assertFalse(exito);
    }

    @Test
    void testAgregarLoteSinLotes() {
        // Crear producto
        ProductoRequest request = new ProductoRequest();
        request.setNombre("Producto Test");
        Object resultado = productoService.crearProductoConStock(request);
        Producto producto = (Producto) resultado;

        // Intentar agregar sin lotes
        AgregarLoteRequest loteRequest = new AgregarLoteRequest();
        loteRequest.setProductoId(producto.getId());
        loteRequest.setLotes(Arrays.asList()); // Lista vacía

        boolean exito = productoService.agregarLotes(loteRequest);

        assertFalse(exito);
    }

    @Test
    void testAgregarLoteAcumulaCantidadGeneral() {
        // Crear producto con stock inicial
        ProductoRequest request = new ProductoRequest();
        request.setNombre("Producto Test");
        request.setCantidadGeneral(50);
        Object resultado = productoService.crearProductoConStock(request);
        Producto producto = (Producto) resultado;

        // Agregar lote adicional
        AgregarLoteRequest loteRequest = new AgregarLoteRequest();
        loteRequest.setProductoId(producto.getId());
        
        AgregarLoteRequest.LoteItem lote = new AgregarLoteRequest.LoteItem();
        lote.setCantidadUnidades(30);
        lote.setFechaVencimiento(LocalDate.now().plusYears(1));
        lote.setPrecioCompra(new BigDecimal("10.00"));
        
        loteRequest.setLotes(Arrays.asList(lote));

        productoService.agregarLotes(loteRequest);

        Producto actualizado = productoService.buscarPorId(producto.getId());
        assertEquals(80, actualizado.getCantidadGeneral()); // 50 + 30
    }
}
