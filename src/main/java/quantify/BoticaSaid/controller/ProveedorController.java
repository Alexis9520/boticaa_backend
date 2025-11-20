package quantify.BoticaSaid.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import quantify.BoticaSaid.dto.proveedor.ProveedorRequest;
import quantify.BoticaSaid.dto.proveedor.ProveedorResponse;
import quantify.BoticaSaid.service.ProveedorService;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/proveedores")
public class ProveedorController {

    @Autowired
    private ProveedorService proveedorService;

    /**
     * Crear un nuevo proveedor
     * POST /proveedores
     */
    @PostMapping
    public ResponseEntity<?> crearProveedor(@RequestBody ProveedorRequest request) {
        try {
            ProveedorResponse response = proveedorService.crearProveedor(request);
            return ResponseEntity.status(201).body(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    /**
     * Obtener todos los proveedores activos
     * GET /proveedores/activos
     */
    @GetMapping("/activos")
    public ResponseEntity<List<ProveedorResponse>> obtenerProveedoresActivos() {
        List<ProveedorResponse> proveedores = proveedorService.obtenerProveedoresActivos();
        return ResponseEntity.ok(proveedores);
    }

    /**
     * Obtener proveedor por ID
     * GET /proveedores/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<ProveedorResponse> obtenerPorId(@PathVariable Long id) {
        var proveedor = proveedorService.buscarPorId(id);
        if (proveedor != null) {
            return ResponseEntity.ok(proveedorService.toProveedorResponse(proveedor));
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Listar proveedores con paginación y filtros
     * GET /proveedores?q={texto}&activo={true/false}&page={page}&size={size}
     */
    @GetMapping
    public ResponseEntity<Map<String, Object>> listarProveedores(
            @RequestParam(required = false) String q,
            @RequestParam(required = false) Boolean activo,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        Pageable pageable = PageRequest.of(page, size);
        Page<ProveedorResponse> paged = proveedorService.buscarProveedoresPaginados(q, activo, pageable);

        Map<String, Object> response = new HashMap<>();
        response.put("content", paged.getContent());
        response.put("totalElements", paged.getTotalElements());
        response.put("totalPages", paged.getTotalPages());
        response.put("page", paged.getNumber());
        response.put("size", paged.getSize());

        return ResponseEntity.ok(response);
    }

    /**
     * Actualizar proveedor
     * PUT /proveedores/{id}
     */
    @PutMapping("/{id}")
    public ResponseEntity<?> actualizarProveedor(
            @PathVariable Long id,
            @RequestBody ProveedorRequest request) {
        try {
            ProveedorResponse response = proveedorService.actualizarProveedor(id, request);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    /**
     * Eliminar proveedor (borrado lógico)
     * DELETE /proveedores/{id}
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<?> eliminarProveedor(@PathVariable Long id) {
        boolean eliminado = proveedorService.eliminarProveedor(id);
        if (eliminado) {
            return ResponseEntity.ok().build();
        } else {
            return ResponseEntity.notFound().build();
        }
    }
}
