package quantify.BoticaSaid.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import quantify.BoticaSaid.dto.proveedor.ProveedorRequest;
import quantify.BoticaSaid.dto.proveedor.ProveedorResponse;
import quantify.BoticaSaid.model.Proveedor;
import quantify.BoticaSaid.service.ProveedorService;

import java.util.List;
import java.util.stream.Collectors;

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
    public ResponseEntity<ProveedorResponse> crearProveedor(@RequestBody ProveedorRequest request) {
        try {
            Proveedor proveedor = proveedorService.crearProveedor(request);
            ProveedorResponse response = proveedorService.toProveedorResponse(proveedor);
            return ResponseEntity.status(201).body(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Obtener un proveedor por ID
     * GET /proveedores/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<ProveedorResponse> obtenerPorId(@PathVariable Long id) {
        Proveedor proveedor = proveedorService.buscarPorId(id);
        if (proveedor != null) {
            ProveedorResponse response = proveedorService.toProveedorResponse(proveedor);
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Listar todos los proveedores activos con búsqueda opcional
     * GET /proveedores?q={query}
     */
    @GetMapping
    public ResponseEntity<List<ProveedorResponse>> listarProveedores(
            @RequestParam(required = false) String q) {
        List<Proveedor> proveedores;
        
        if (q != null && !q.trim().isEmpty()) {
            proveedores = proveedorService.buscarPorRucORazonComercial(q);
        } else {
            proveedores = proveedorService.listarTodos();
        }

        List<ProveedorResponse> response = proveedores.stream()
                .map(proveedorService::toProveedorResponse)
                .collect(Collectors.toList());

        return ResponseEntity.ok(response);
    }

    /**
     * Actualizar un proveedor por ID
     * PUT /proveedores/{id}
     */
    @PutMapping("/{id}")
    public ResponseEntity<ProveedorResponse> actualizarPorId(
            @PathVariable Long id,
            @RequestBody ProveedorRequest request) {

        Proveedor actualizado = proveedorService.actualizarPorId(id, request);

        if (actualizado != null) {
            ProveedorResponse response = proveedorService.toProveedorResponse(actualizado);
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Eliminar un proveedor por ID (borrado lógico)
     * DELETE /proveedores/{id}
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<?> eliminarPorId(@PathVariable Long id) {
        boolean eliminado = proveedorService.eliminarPorId(id);
        if (eliminado) {
            return ResponseEntity.ok().build();
        } else {
            return ResponseEntity.notFound().build();
        }
    }
}
