package quantify.BoticaSaid.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import quantify.BoticaSaid.dto.ProductoRequest;
import quantify.BoticaSaid.dto.ProductoResponse;
import quantify.BoticaSaid.dto.AgregarStockRequest;
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

    // GET /productos with optional search q and optional lab/cat filters
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

    @PostMapping("/agregar-stock")
    public ResponseEntity<?> agregarStock(@RequestBody AgregarStockRequest request) {
        boolean exito = productoService.agregarStock(request);
        return exito
                ? ResponseEntity.ok("Stock agregado correctamente.")
                : ResponseEntity.badRequest().body("Producto no encontrado.");
    }

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

    @DeleteMapping("/{codigoBarras}")
    public ResponseEntity<?> eliminarPorCodigoBarras(@PathVariable String codigoBarras) {
        boolean eliminado = productoService.eliminarPorCodigoBarras(codigoBarras);
        if (eliminado) {
            return ResponseEntity.ok().build();
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<ProductoResponse> actualizarPorid(
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


    @GetMapping("/stock-bajo")
    public ResponseEntity<List<ProductoResponse>> productosConStockBajo(
            @RequestParam(defaultValue = "10") int umbral) {
        List<Producto> productos = productoService.buscarProductosConStockMenorA(umbral);
        List<ProductoResponse> productosRes = productos.stream()
                .map(productoService::toProductoResponse)
                .toList();
        return ResponseEntity.ok(productosRes);
    }
}