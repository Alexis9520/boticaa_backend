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
        request.setNombre("Proveedor Test");
        request.setContacto("Juan Perez");
        request.setTelefono("123456789");
        request.setEmail("test@proveedor.com");
        request.setDireccion("Calle Principal 123");

        Proveedor proveedor = proveedorService.crearProveedor(request);

        assertNotNull(proveedor);
        assertNotNull(proveedor.getId());
        assertEquals("Proveedor Test", proveedor.getNombre());
        assertEquals("Juan Perez", proveedor.getContacto());
        assertEquals("123456789", proveedor.getTelefono());
        assertEquals("test@proveedor.com", proveedor.getEmail());
        assertEquals("Calle Principal 123", proveedor.getDireccion());
        assertTrue(proveedor.isActivo());
    }

    @Test
    void testCrearProveedorSinNombre() {
        ProveedorRequest request = new ProveedorRequest();
        request.setContacto("Juan Perez");

        assertThrows(IllegalArgumentException.class, () -> {
            proveedorService.crearProveedor(request);
        });
    }

    @Test
    void testBuscarPorId() {
        ProveedorRequest request = new ProveedorRequest();
        request.setNombre("Proveedor Test");
        Proveedor creado = proveedorService.crearProveedor(request);

        Proveedor encontrado = proveedorService.buscarPorId(creado.getId());

        assertNotNull(encontrado);
        assertEquals(creado.getId(), encontrado.getId());
        assertEquals("Proveedor Test", encontrado.getNombre());
    }

    @Test
    void testListarTodos() {
        ProveedorRequest request1 = new ProveedorRequest();
        request1.setNombre("Proveedor 1");
        proveedorService.crearProveedor(request1);

        ProveedorRequest request2 = new ProveedorRequest();
        request2.setNombre("Proveedor 2");
        proveedorService.crearProveedor(request2);

        List<Proveedor> proveedores = proveedorService.listarTodos();

        assertEquals(2, proveedores.size());
    }

    @Test
    void testBuscarPorNombre() {
        ProveedorRequest request1 = new ProveedorRequest();
        request1.setNombre("Farmacia ABC");
        proveedorService.crearProveedor(request1);

        ProveedorRequest request2 = new ProveedorRequest();
        request2.setNombre("Farmacia XYZ");
        proveedorService.crearProveedor(request2);

        List<Proveedor> resultado = proveedorService.buscarPorNombre("ABC");

        assertEquals(1, resultado.size());
        assertEquals("Farmacia ABC", resultado.get(0).getNombre());
    }

    @Test
    void testActualizarProveedor() {
        ProveedorRequest request = new ProveedorRequest();
        request.setNombre("Proveedor Original");
        Proveedor creado = proveedorService.crearProveedor(request);

        ProveedorRequest updateRequest = new ProveedorRequest();
        updateRequest.setNombre("Proveedor Actualizado");
        updateRequest.setContacto("Nuevo Contacto");

        Proveedor actualizado = proveedorService.actualizarPorId(creado.getId(), updateRequest);

        assertNotNull(actualizado);
        assertEquals("Proveedor Actualizado", actualizado.getNombre());
        assertEquals("Nuevo Contacto", actualizado.getContacto());
    }

    @Test
    void testEliminarProveedor() {
        ProveedorRequest request = new ProveedorRequest();
        request.setNombre("Proveedor a Eliminar");
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
        proveedor.setNombre("Proveedor Test");
        proveedor.setContacto("Contacto Test");
        proveedor.setTelefono("123456789");
        proveedor.setEmail("test@test.com");
        proveedor.setDireccion("Direccion Test");
        proveedor.setActivo(true);

        ProveedorResponse response = proveedorService.toProveedorResponse(proveedor);

        assertNotNull(response);
        assertEquals(1L, response.getId());
        assertEquals("Proveedor Test", response.getNombre());
        assertEquals("Contacto Test", response.getContacto());
        assertEquals("123456789", response.getTelefono());
        assertEquals("test@test.com", response.getEmail());
        assertEquals("Direccion Test", response.getDireccion());
        assertTrue(response.isActivo());
    }
}
