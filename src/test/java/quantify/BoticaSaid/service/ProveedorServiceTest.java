package quantify.BoticaSaid.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import quantify.BoticaSaid.dto.proveedor.ProveedorRequest;
import quantify.BoticaSaid.dto.proveedor.ProveedorResponse;
import quantify.BoticaSaid.model.Proveedor;
import quantify.BoticaSaid.repository.ProveedorRepository;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class ProveedorServiceTest {

    @Autowired
    private ProveedorService proveedorService;

    @Autowired
    private ProveedorRepository proveedorRepository;

    @BeforeEach
    void setUp() {
        proveedorRepository.deleteAll();
    }

    @Test
    void testCrearProveedor() {
        ProveedorRequest request = new ProveedorRequest();
        request.setRuc("20123456789");
        request.setRazonComercial("Proveedor Test SAC");
        request.setNumero1("123456789");
        request.setNumero2("987654321");
        request.setCorreo("test@proveedor.com");
        request.setDireccion("Calle Principal 123");

        Proveedor proveedor = proveedorService.crearProveedor(request);

        assertNotNull(proveedor);
        assertNotNull(proveedor.getId());
        assertEquals("20123456789", proveedor.getRuc());
        assertEquals("Proveedor Test SAC", proveedor.getRazonComercial());
        assertEquals("123456789", proveedor.getNumero1());
        assertEquals("987654321", proveedor.getNumero2());
        assertEquals("test@proveedor.com", proveedor.getCorreo());
        assertEquals("Calle Principal 123", proveedor.getDireccion());
        assertTrue(proveedor.isActivo());
    }

    @Test
    void testCrearProveedorSinRuc() {
        ProveedorRequest request = new ProveedorRequest();
        request.setRazonComercial("Test");

        assertThrows(IllegalArgumentException.class, () -> {
            proveedorService.crearProveedor(request);
        });
    }

    @Test
    void testBuscarPorId() {
        ProveedorRequest request = new ProveedorRequest();
        request.setRuc("20111111111");
        request.setRazonComercial("Proveedor Test");
        Proveedor creado = proveedorService.crearProveedor(request);

        Proveedor encontrado = proveedorService.buscarPorId(creado.getId());

        assertNotNull(encontrado);
        assertEquals(creado.getId(), encontrado.getId());
        assertEquals("20111111111", encontrado.getRuc());
    }

    @Test
    void testListarTodos() {
        ProveedorRequest request1 = new ProveedorRequest();
        request1.setRuc("20111111111");
        request1.setRazonComercial("Proveedor 1");
        proveedorService.crearProveedor(request1);

        ProveedorRequest request2 = new ProveedorRequest();
        request2.setRuc("20222222222");
        request2.setRazonComercial("Proveedor 2");
        proveedorService.crearProveedor(request2);

        List<Proveedor> proveedores = proveedorService.listarTodos();

        assertEquals(2, proveedores.size());
    }

    @Test
    void testBuscarPorRucORazonComercial() {
        ProveedorRequest request1 = new ProveedorRequest();
        request1.setRuc("20333333333");
        request1.setRazonComercial("Farmacia ABC SAC");
        proveedorService.crearProveedor(request1);

        ProveedorRequest request2 = new ProveedorRequest();
        request2.setRuc("20444444444");
        request2.setRazonComercial("Farmacia XYZ EIRL");
        proveedorService.crearProveedor(request2);

        List<Proveedor> resultado = proveedorService.buscarPorRucORazonComercial("ABC");

        assertEquals(1, resultado.size());
        assertEquals("Farmacia ABC SAC", resultado.get(0).getRazonComercial());
    }

    @Test
    void testActualizarProveedor() {
        ProveedorRequest request = new ProveedorRequest();
        request.setRuc("20555555555");
        request.setRazonComercial("Proveedor Original");
        Proveedor creado = proveedorService.crearProveedor(request);

        ProveedorRequest updateRequest = new ProveedorRequest();
        updateRequest.setRuc("20555555555");
        updateRequest.setRazonComercial("Proveedor Actualizado");
        updateRequest.setNumero1("999888777");

        Proveedor actualizado = proveedorService.actualizarPorId(creado.getId(), updateRequest);

        assertNotNull(actualizado);
        assertEquals("Proveedor Actualizado", actualizado.getRazonComercial());
        assertEquals("999888777", actualizado.getNumero1());
    }

    @Test
    void testEliminarProveedor() {
        ProveedorRequest request = new ProveedorRequest();
        request.setRuc("20666666666");
        request.setRazonComercial("Proveedor a Eliminar");
        Proveedor creado = proveedorService.crearProveedor(request);

        boolean resultado = proveedorService.eliminarPorId(creado.getId());

        assertTrue(resultado);
        
        Proveedor buscado = proveedorService.buscarPorId(creado.getId());
        assertNull(buscado); // No debería encontrarse porque está inactivo
    }

    @Test
    void testToProveedorResponse() {
        Proveedor proveedor = new Proveedor();
        proveedor.setId(1L);
        proveedor.setRuc("20777777777");
        proveedor.setRazonComercial("Proveedor Test SAC");
        proveedor.setNumero1("123456789");
        proveedor.setNumero2("987654321");
        proveedor.setCorreo("test@test.com");
        proveedor.setDireccion("Direccion Test");
        proveedor.setActivo(true);

        ProveedorResponse response = proveedorService.toProveedorResponse(proveedor);

        assertNotNull(response);
        assertEquals(1L, response.getId());
        assertEquals("20777777777", response.getRuc());
        assertEquals("Proveedor Test SAC", response.getRazonComercial());
        assertEquals("123456789", response.getNumero1());
        assertEquals("987654321", response.getNumero2());
        assertEquals("test@test.com", response.getCorreo());
        assertEquals("Direccion Test", response.getDireccion());
        assertTrue(response.isActivo());
    }
}
