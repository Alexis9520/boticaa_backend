package quantify.BoticaSaid.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import quantify.BoticaSaid.dto.venta.VentaRequestDTO;
import quantify.BoticaSaid.dto.venta.VentaResponseDTO;
import quantify.BoticaSaid.service.VentaService;

import java.util.List;

@RestController
@RequestMapping("/api/ventas")
public class VentaController {

    private final VentaService ventaService;

    public VentaController(VentaService ventaService) {
        this.ventaService = ventaService;
    }

    @PostMapping
    public ResponseEntity<VentaResponseDTO> registrarVenta(@RequestBody VentaRequestDTO ventaDTO) {
        try {
            VentaResponseDTO respuesta = ventaService.registrarVenta(ventaDTO);
            System.out.println("Venta registrada exitosamente. Número de boleta: " + (respuesta != null ? respuesta.getNumero() : "null"));
            return ResponseEntity.status(HttpStatus.CREATED).body(respuesta);
        } catch (RuntimeException e) {
            System.err.println("Error al registrar venta: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        }
    }

    @GetMapping
    public ResponseEntity<List<VentaResponseDTO>> listarVentas() {
        List<VentaResponseDTO> ventas = ventaService.listarVentas();
        return ResponseEntity.ok(ventas);
    }
}