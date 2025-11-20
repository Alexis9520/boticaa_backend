package quantify.BoticaSaid.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import quantify.BoticaSaid.dto.producto.ProductoRequest;
import quantify.BoticaSaid.dto.producto.ProductoResponse;
import quantify.BoticaSaid.dto.proveedor.ProveedorRequest;
import quantify.BoticaSaid.model.Producto;
import quantify.BoticaSaid.model.Proveedor;
import quantify.BoticaSaid.repository.ProductoRepository;
import quantify.BoticaSaid.repository.ProveedorRepository;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class ProductoProveedorIntegrationTest {

    @Autowired
    private ProductoService productoService;

    @Autowired
    private ProveedorService proveedorService;

    @Autowired
    private ProductoRepository productoRepository;

    @Autowired
    private ProveedorRepository proveedorRepository;

    @BeforeEach
    void setUp() {
        productoRepository.deleteAll();
        proveedorRepository.deleteAll();
    }

    @Test
    void testCrearProductoConProveedor() {
        // Crear proveedor
        ProveedorRequest proveedorReq = new ProveedorRequest();
        proveedorReq.setRuc("20123456789");
        proveedorReq.setRazonComercial("Distribuidora ABC");
        Proveedor proveedor = proveedorService.crearProveedor(proveedorReq);

        // Crear producto con proveedor
        ProductoRequest productoReq = new ProductoRequest();
        productoReq.setNombre("Paracetamol");
        productoReq.setProveedorId(proveedor.getId());
        
        Object resultado = productoService.crearProductoConStock(productoReq);
        Producto producto = (Producto) resultado;

        assertNotNull(producto);
        assertFalse(producto.getProductoProveedores().isEmpty());
        assertEquals(1, producto.getProductoProveedores().size());
        assertEquals(proveedor.getId(), producto.getProductoProveedores().get(0).getProveedor().getId());
        assertEquals("Distribuidora ABC", producto.getProductoProveedores().get(0).getProveedor().getRazonComercial());
    }

    @Test
    void testCrearProductoSinProveedor() {
        // Crear producto sin proveedor
        ProductoRequest productoReq = new ProductoRequest();
        productoReq.setNombre("Aspirina");
        
        Object resultado = productoService.crearProductoConStock(productoReq);
        Producto producto = (Producto) resultado;

        assertNotNull(producto);
        assertTrue(producto.getProductoProveedores().isEmpty());
    }

    @Test
    void testActualizarProductoConProveedor() {
        // Crear proveedor
        ProveedorRequest proveedorReq = new ProveedorRequest();
        proveedorReq.setRuc("20987654321");
        proveedorReq.setRazonComercial("Distribuidora XYZ");
        Proveedor proveedor = proveedorService.crearProveedor(proveedorReq);

        // Crear producto sin proveedor
        ProductoRequest productoReq = new ProductoRequest();
        productoReq.setNombre("Ibuprofeno");
        Object resultado = productoService.crearProductoConStock(productoReq);
        Producto producto = (Producto) resultado;

        // Actualizar con proveedor
        ProductoRequest updateReq = new ProductoRequest();
        updateReq.setNombre("Ibuprofeno");
        updateReq.setProveedorId(proveedor.getId());
        
        Producto actualizado = productoService.actualizarPorID(producto.getId(), updateReq);

        assertFalse(actualizado.getProductoProveedores().isEmpty());
        assertEquals(proveedor.getId(), actualizado.getProductoProveedores().get(0).getProveedor().getId());
    }

    @Test
    void testActualizarProductoRemoverProveedor() {
        // Crear proveedor
        ProveedorRequest proveedorReq = new ProveedorRequest();
        proveedorReq.setRuc("20111222333");
        proveedorReq.setRazonComercial("Distribuidora ABC");
        Proveedor proveedor = proveedorService.crearProveedor(proveedorReq);

        // Crear producto con proveedor
        ProductoRequest productoReq = new ProductoRequest();
        productoReq.setNombre("Amoxicilina");
        productoReq.setProveedorId(proveedor.getId());
        Object resultado = productoService.crearProductoConStock(productoReq);
        Producto producto = (Producto) resultado;

        // Actualizar removiendo proveedor (null)
        ProductoRequest updateReq = new ProductoRequest();
        updateReq.setNombre("Amoxicilina");
        updateReq.setProveedorId(null);
        
        Producto actualizado = productoService.actualizarPorID(producto.getId(), updateReq);

        assertTrue(actualizado.getProductoProveedores().isEmpty());
    }

    @Test
    void testProductoResponseIncluyeProveedor() {
        // Crear proveedor
        ProveedorRequest proveedorReq = new ProveedorRequest();
        proveedorReq.setRuc("20444555666");
        proveedorReq.setRazonComercial("Farmaceutica DEF");
        Proveedor proveedor = proveedorService.crearProveedor(proveedorReq);

        // Crear producto con proveedor
        ProductoRequest productoReq = new ProductoRequest();
        productoReq.setNombre("Loratadina");
        productoReq.setProveedorId(proveedor.getId());
        Object resultado = productoService.crearProductoConStock(productoReq);
        Producto producto = (Producto) resultado;

        // Convertir a response
        ProductoResponse response = productoService.toProductoResponse(producto);

        assertNotNull(response);
        assertNotNull(response.getProveedorId());
        assertEquals(proveedor.getId(), response.getProveedorId());
        assertEquals("Farmaceutica DEF", response.getProveedorNombre());
    }

    @Test
    void testProductoResponseSinProveedor() {
        // Crear producto sin proveedor
        ProductoRequest productoReq = new ProductoRequest();
        productoReq.setNombre("Dipirona");
        Object resultado = productoService.crearProductoConStock(productoReq);
        Producto producto = (Producto) resultado;

        // Convertir a response
        ProductoResponse response = productoService.toProductoResponse(producto);

        assertNotNull(response);
        assertNull(response.getProveedorId());
        assertNull(response.getProveedorNombre());
    }

    @Test
    void testCrearProductoConProveedorInexistente() {
        // Intentar crear producto con proveedor que no existe
        ProductoRequest productoReq = new ProductoRequest();
        productoReq.setNombre("Producto Test");
        productoReq.setProveedorId(9999L);
        
        Object resultado = productoService.crearProductoConStock(productoReq);
        Producto producto = (Producto) resultado;

        assertNotNull(producto);
        assertTrue(producto.getProductoProveedores().isEmpty()); // Debe estar vacío si no existe el proveedor
    }

    @Test
    void testCrearProductoConProveedorInactivo() {
        // Crear y desactivar proveedor
        ProveedorRequest proveedorReq = new ProveedorRequest();
        proveedorReq.setRuc("20777888999");
        proveedorReq.setRazonComercial("Proveedor Inactivo");
        Proveedor proveedor = proveedorService.crearProveedor(proveedorReq);
        proveedorService.eliminarPorId(proveedor.getId());

        // Intentar crear producto con proveedor inactivo
        ProductoRequest productoReq = new ProductoRequest();
        productoReq.setNombre("Producto Test");
        productoReq.setProveedorId(proveedor.getId());
        
        Object resultado = productoService.crearProductoConStock(productoReq);
        Producto producto = (Producto) resultado;

        assertNotNull(producto);
        assertTrue(producto.getProductoProveedores().isEmpty()); // Debe estar vacío si el proveedor está inactivo
    }

    @Test
    void testCrearProductoConMultiplesProveedores() {
        // Crear dos proveedores
        ProveedorRequest proveedorReq1 = new ProveedorRequest();
        proveedorReq1.setRuc("20111111111");
        proveedorReq1.setRazonComercial("Proveedor Uno");
        Proveedor proveedor1 = proveedorService.crearProveedor(proveedorReq1);

        ProveedorRequest proveedorReq2 = new ProveedorRequest();
        proveedorReq2.setRuc("20222222222");
        proveedorReq2.setRazonComercial("Proveedor Dos");
        Proveedor proveedor2 = proveedorService.crearProveedor(proveedorReq2);

        // Crear producto con múltiples proveedores usando proveedorIds
        ProductoRequest productoReq = new ProductoRequest();
        productoReq.setNombre("Multivitamínico");
        productoReq.setProveedorIds(java.util.Arrays.asList(proveedor1.getId(), proveedor2.getId()));
        
        Object resultado = productoService.crearProductoConStock(productoReq);
        Producto producto = (Producto) resultado;

        assertNotNull(producto);
        assertEquals(2, producto.getProductoProveedores().size());
        
        // Verificar que ambos proveedores están presentes
        java.util.List<Long> proveedorIds = producto.getProductoProveedores().stream()
                .map(pp -> pp.getProveedor().getId())
                .collect(java.util.stream.Collectors.toList());
        assertTrue(proveedorIds.contains(proveedor1.getId()));
        assertTrue(proveedorIds.contains(proveedor2.getId()));
    }

    @Test
    void testCrearProductoConProveedorIdYProveedorIds() {
        // Crear tres proveedores
        ProveedorRequest proveedorReq1 = new ProveedorRequest();
        proveedorReq1.setRuc("20333333333");
        proveedorReq1.setRazonComercial("Proveedor Tres");
        Proveedor proveedor1 = proveedorService.crearProveedor(proveedorReq1);

        ProveedorRequest proveedorReq2 = new ProveedorRequest();
        proveedorReq2.setRuc("20444444444");
        proveedorReq2.setRazonComercial("Proveedor Cuatro");
        Proveedor proveedor2 = proveedorService.crearProveedor(proveedorReq2);

        ProveedorRequest proveedorReq3 = new ProveedorRequest();
        proveedorReq3.setRuc("20555555555");
        proveedorReq3.setRazonComercial("Proveedor Cinco");
        Proveedor proveedor3 = proveedorService.crearProveedor(proveedorReq3);

        // Crear producto usando tanto proveedorId como proveedorIds
        // (deben fusionarse sin duplicados)
        ProductoRequest productoReq = new ProductoRequest();
        productoReq.setNombre("Antibiótico");
        productoReq.setProveedorId(proveedor1.getId());
        productoReq.setProveedorIds(java.util.Arrays.asList(proveedor2.getId(), proveedor3.getId()));
        
        Object resultado = productoService.crearProductoConStock(productoReq);
        Producto producto = (Producto) resultado;

        assertNotNull(producto);
        assertEquals(3, producto.getProductoProveedores().size());
    }

    @Test
    void testProductoResponseConMultiplesProveedores() {
        // Crear dos proveedores
        ProveedorRequest proveedorReq1 = new ProveedorRequest();
        proveedorReq1.setRuc("20666666666");
        proveedorReq1.setRazonComercial("Proveedor Seis");
        Proveedor proveedor1 = proveedorService.crearProveedor(proveedorReq1);

        ProveedorRequest proveedorReq2 = new ProveedorRequest();
        proveedorReq2.setRuc("20777777777");
        proveedorReq2.setRazonComercial("Proveedor Siete");
        Proveedor proveedor2 = proveedorService.crearProveedor(proveedorReq2);

        // Crear producto con múltiples proveedores
        ProductoRequest productoReq = new ProductoRequest();
        productoReq.setNombre("Analgésico");
        productoReq.setProveedorIds(java.util.Arrays.asList(proveedor1.getId(), proveedor2.getId()));
        
        Object resultado = productoService.crearProductoConStock(productoReq);
        Producto producto = (Producto) resultado;

        // Convertir a response
        quantify.BoticaSaid.dto.producto.ProductoResponse response = productoService.toProductoResponse(producto);

        assertNotNull(response);
        assertNotNull(response.getProveedores());
        assertEquals(2, response.getProveedores().size());
        
        // Verificar compatibilidad: el primer proveedor debe estar en proveedorId y proveedorNombre
        assertNotNull(response.getProveedorId());
        assertNotNull(response.getProveedorNombre());
        assertEquals(proveedor1.getId(), response.getProveedorId());
        assertEquals("Proveedor Seis", response.getProveedorNombre());
    }

    @Test
    void testActualizarProductoAgregandoProveedor() {
        // Crear dos proveedores
        ProveedorRequest proveedorReq1 = new ProveedorRequest();
        proveedorReq1.setRuc("20888888888");
        proveedorReq1.setRazonComercial("Proveedor Ocho");
        Proveedor proveedor1 = proveedorService.crearProveedor(proveedorReq1);

        ProveedorRequest proveedorReq2 = new ProveedorRequest();
        proveedorReq2.setRuc("20999999999");
        proveedorReq2.setRazonComercial("Proveedor Nueve");
        Proveedor proveedor2 = proveedorService.crearProveedor(proveedorReq2);

        // Crear producto con un proveedor
        ProductoRequest productoReq = new ProductoRequest();
        productoReq.setNombre("Jarabe");
        productoReq.setProveedorId(proveedor1.getId());
        Object resultado = productoService.crearProductoConStock(productoReq);
        Producto producto = (Producto) resultado;

        // Actualizar agregando segundo proveedor
        ProductoRequest updateReq = new ProductoRequest();
        updateReq.setNombre("Jarabe");
        updateReq.setProveedorIds(java.util.Arrays.asList(proveedor1.getId(), proveedor2.getId()));
        
        Producto actualizado = productoService.actualizarPorID(producto.getId(), updateReq);

        assertNotNull(actualizado);
        assertEquals(2, actualizado.getProductoProveedores().size());
    }
}
