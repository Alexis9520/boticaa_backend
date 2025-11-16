package quantify.BoticaSaid.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import quantify.BoticaSaid.dto.producto.ProductoRequest;
import quantify.BoticaSaid.dto.producto.ProductoResponse;
import quantify.BoticaSaid.dto.stock.AgregarStockRequest;
import quantify.BoticaSaid.model.Producto;
import quantify.BoticaSaid.service.ProductoService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.data.domain.Pageable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/productos")
public class ProductoController {

    @Autowired
    private ProductoService productoService;

    // ===== CREATE OPERATIONS =====

    /**
     * Crear un nuevo producto con stock inicial
     * POST /productos/nuevo
     */
    @PostMapping("/nuevo")
    public ResponseEntity<?> crearProducto(@RequestBody ProductoRequest request) {
        try {
            Object result = productoService.crearProductoConStock(request);
            if (result instanceof Map) {
                return ResponseEntity.ok(result);
            } else if (result instanceof Producto) {
                Producto producto = (Producto) result;
                ProductoResponse resp = productoService.toProductoResponse(producto);
                return ResponseEntity.status(201).body(resp);
            } else {
                return ResponseEntity.status(500).body("Error inesperado.");
            }
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(409).body(Map.of("message", e.getMessage()));
        }
    }

    // ===== READ OPERATIONS =====

    /**
     * Obtener un producto por ID
     * GET /productos/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<ProductoResponse> obtenerPorId(@PathVariable Long id) {
        Producto producto = productoService.buscarPorId(id);
        if (producto != null) {
            ProductoResponse resp = productoService.toProductoResponse(producto);
            return ResponseEntity.ok(resp);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Obtener un producto por código de barras
     * GET /productos/codigo-barras/{codigo}
     * Útil para escáneres de código de barras
     */
    @GetMapping("/codigo-barras/{codigo}")
    public ResponseEntity<ProductoResponse> obtenerPorCodigoBarras(@PathVariable String codigo) {
        Producto producto = productoService.buscarPorCodigoBarras(codigo);
        if (producto != null) {
            ProductoResponse resp = productoService.toProductoResponse(producto);
            return ResponseEntity.ok(resp);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Listar todos los productos con paginación y filtros opcionales
     * GET /productos?q={texto}&lab={laboratorio}&cat={categoria}&page={page}&size={size}
     */
    @GetMapping
    public ResponseEntity<Map<String, Object>> listarTodos(
            @RequestParam(required = false) String q,
            @RequestParam(required = false) String lab,
            @RequestParam(required = false) String cat,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Producto> paged = productoService.buscarPaginadoPorQuery(q, lab, cat, pageable);
        List<ProductoResponse> productosRes = paged.getContent().stream()
                .map(productoService::toProductoResponse)
                .toList();

        Map<String, Object> response = new HashMap<>();
        response.put("content", productosRes);
        response.put("totalElements", paged.getTotalElements());
        response.put("totalPages", paged.getTotalPages());
        response.put("page", paged.getNumber());
        response.put("size", paged.getSize());

        return ResponseEntity.ok(response);
    }

    /**
     * Buscar productos por nombre o categoría
     * GET /productos/buscar?nombre={nombre}&categoria={categoria}
     */
    @GetMapping("/buscar")
    public ResponseEntity<List<ProductoResponse>> buscarPorNombreOCategoria(
            @RequestParam(required = false) String nombre,
            @RequestParam(required = false) String categoria) {
        List<Producto> productos = productoService.buscarPorNombreOCategoria(nombre, categoria);
        List<ProductoResponse> productosRes = productos.stream()
                .map(productoService::toProductoResponse)
                .toList();
        return ResponseEntity.ok(productosRes);
    }

    /**
     * Obtener productos con stock bajo
     * GET /productos/stock-bajo?umbral={umbral}
     */
    @GetMapping("/stock-bajo")
    public ResponseEntity<List<ProductoResponse>> productosConStockBajo(
            @RequestParam(defaultValue = "10") int umbral) {
        List<Producto> productos = productoService.buscarProductosConStockMenorA(umbral);
        List<ProductoResponse> productosRes = productos.stream()
                .map(productoService::toProductoResponse)
                .toList();
        return ResponseEntity.ok(productosRes);
    }

    // ===== UPDATE OPERATIONS =====

    /**
     * Actualizar un producto por ID
     * PUT /productos/{id}
     */
    @PutMapping("/{id}")
    public ResponseEntity<ProductoResponse> actualizarPorId(
            @PathVariable Long id,
            @RequestBody ProductoRequest request) {

        Producto actualizado = productoService.actualizarPorID(id, request);

        if (actualizado != null) {
            ProductoResponse resp = productoService.toProductoResponse(actualizado);
            return ResponseEntity.ok(resp);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Agregar stock a un producto existente
     * POST /productos/agregar-stock
     */
    @PostMapping("/agregar-stock")
    public ResponseEntity<?> agregarStock(@RequestBody AgregarStockRequest request) {
        boolean exito = productoService.agregarStock(request);
        return exito
                ? ResponseEntity.ok("Stock agregado correctamente.")
                : ResponseEntity.badRequest().body("Producto no encontrado.");
    }

    /**
     * Agregar lotes de stock a un producto existente sin modificar datos del producto
     * POST /productos/agregar-lote
     */
    @PostMapping("/agregar-lote")
    public ResponseEntity<?> agregarLote(@RequestBody quantify.BoticaSaid.dto.stock.AgregarLoteRequest request) {
        boolean exito = productoService.agregarLotes(request);
        return exito
                ? ResponseEntity.ok("Lote(s) agregado(s) correctamente.")
                : ResponseEntity.badRequest().body("Producto no encontrado o lotes vacíos.");
    }

    // ===== DELETE OPERATIONS =====

    /**
     * Eliminar un producto por ID (borrado lógico)
     * DELETE /productos/{id}
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<?> eliminarPorId(@PathVariable Long id) {
        boolean eliminado = productoService.eliminarPorId(id);
        if (eliminado) {
            return ResponseEntity.ok().build();
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Listar productos por proveedor
     * GET /productos/proveedor/{proveedorId}
     */
    @GetMapping("/proveedor/{proveedorId}")
    public ResponseEntity<List<ProductoResponse>> listarProductosPorProveedor(@PathVariable Long proveedorId) {
        List<Producto> productos = productoService.buscarPorProveedorId(proveedorId);
        List<ProductoResponse> productosRes = productos.stream()
                .map(productoService::toProductoResponse)
                .toList();
        return ResponseEntity.ok(productosRes);
    }

    /**
     * Obtener productos por categoría con stocks
     * GET /productos/categoria/{categoria}
     */
    @GetMapping("/categoria/{categoria}")
    public ResponseEntity<List<ProductoResponse>> obtenerProductosPorCategoria(@PathVariable String categoria) {
        List<Producto> productos = productoService.buscarPorCategoria(categoria);
        List<ProductoResponse> productosRes = productos.stream()
                .map(productoService::toProductoResponse)
                .toList();
        return ResponseEntity.ok(productosRes);
    }
}