package quantify.BoticaSaid.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import quantify.BoticaSaid.dto.producto.ProductoRequest;
import quantify.BoticaSaid.dto.stock.StockRequest;
import quantify.BoticaSaid.model.Producto;
import quantify.BoticaSaid.repository.ProductoRepository;
import quantify.BoticaSaid.service.ProductoService;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class ProductoControllerCategoriaTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ProductoService productoService;

    @Autowired
    private ProductoRepository productoRepository;

    @BeforeEach
    void setUp() {
        productoRepository.deleteAll();

        // Crear productos de prueba con diferentes categorías
        ProductoRequest producto1 = new ProductoRequest();
        producto1.setNombre("Paracetamol 500mg");
        producto1.setCategoria("Analgésicos");
        producto1.setLaboratorio("Lab A");
        producto1.setPrecioVentaUnd(new BigDecimal("10.50"));
        
        StockRequest stock1 = new StockRequest();
        stock1.setCodigoStock("LOTE001");
        stock1.setCantidadUnidades(100);
        stock1.setFechaVencimiento(LocalDate.now().plusYears(1));
        stock1.setPrecioCompra(new BigDecimal("5.00"));
        producto1.setStocks(Arrays.asList(stock1));
        
        productoService.crearProductoConStock(producto1);

        ProductoRequest producto2 = new ProductoRequest();
        producto2.setNombre("Ibuprofeno 400mg");
        producto2.setCategoria("Analgésicos");
        producto2.setLaboratorio("Lab B");
        producto2.setPrecioVentaUnd(new BigDecimal("12.00"));
        
        StockRequest stock2 = new StockRequest();
        stock2.setCodigoStock("LOTE002");
        stock2.setCantidadUnidades(50);
        stock2.setFechaVencimiento(LocalDate.now().plusYears(2));
        stock2.setPrecioCompra(new BigDecimal("6.00"));
        producto2.setStocks(Arrays.asList(stock2));
        
        productoService.crearProductoConStock(producto2);

        ProductoRequest producto3 = new ProductoRequest();
        producto3.setNombre("Amoxicilina 500mg");
        producto3.setCategoria("Antibióticos");
        producto3.setLaboratorio("Lab C");
        producto3.setPrecioVentaUnd(new BigDecimal("15.00"));
        
        StockRequest stock3 = new StockRequest();
        stock3.setCodigoStock("LOTE003");
        stock3.setCantidadUnidades(75);
        stock3.setFechaVencimiento(LocalDate.now().plusMonths(6));
        stock3.setPrecioCompra(new BigDecimal("8.00"));
        producto3.setStocks(Arrays.asList(stock3));
        
        productoService.crearProductoConStock(producto3);
    }

    @Test
    @WithMockUser(username = "test", roles = {"USER"})
    void testObtenerProductosPorCategoria_Analgésicos() throws Exception {
        mockMvc.perform(get("/productos/categoria/Analgésicos")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].categoria").value("Analgésicos"))
                .andExpect(jsonPath("$[1].categoria").value("Analgésicos"))
                .andExpect(jsonPath("$[0].stocks").isArray())
                .andExpect(jsonPath("$[0].stocks.length()").value(1))
                .andExpect(jsonPath("$[0].cantidadGeneral").value(100));
    }

    @Test
    @WithMockUser(username = "test", roles = {"USER"})
    void testObtenerProductosPorCategoria_Antibióticos() throws Exception {
        mockMvc.perform(get("/productos/categoria/Antibióticos")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].categoria").value("Antibióticos"))
                .andExpect(jsonPath("$[0].nombre").value("Amoxicilina 500mg"))
                .andExpect(jsonPath("$[0].stocks").isArray())
                .andExpect(jsonPath("$[0].stocks.length()").value(1))
                .andExpect(jsonPath("$[0].cantidadGeneral").value(75));
    }

    @Test
    @WithMockUser(username = "test", roles = {"USER"})
    void testObtenerProductosPorCategoria_CategoriaNoExistente() throws Exception {
        mockMvc.perform(get("/productos/categoria/NoExiste")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    @WithMockUser(username = "test", roles = {"USER"})
    void testObtenerProductosPorCategoria_CaseInsensitive() throws Exception {
        mockMvc.perform(get("/productos/categoria/analgésicos")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.length()").value(2));
    }
}
