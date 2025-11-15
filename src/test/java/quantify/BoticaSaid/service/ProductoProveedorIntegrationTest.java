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
        assertNotNull(producto.getProveedor());
        assertEquals(proveedor.getId(), producto.getProveedor().getId());
        assertEquals("Distribuidora ABC", producto.getProveedor().getRazonComercial());
    }

    @Test
    void testCrearProductoSinProveedor() {
        // Crear producto sin proveedor
        ProductoRequest productoReq = new ProductoRequest();
        productoReq.setNombre("Aspirina");
        
        Object resultado = productoService.crearProductoConStock(productoReq);
        Producto producto = (Producto) resultado;

        assertNotNull(producto);
        assertNull(producto.getProveedor());
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

        assertNotNull(actualizado.getProveedor());
        assertEquals(proveedor.getId(), actualizado.getProveedor().getId());
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

        assertNull(actualizado.getProveedor());
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
        assertNull(producto.getProveedor()); // Debe ser null si no existe el proveedor
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
        assertNull(producto.getProveedor()); // Debe ser null si el proveedor está inactivo
    }
}
