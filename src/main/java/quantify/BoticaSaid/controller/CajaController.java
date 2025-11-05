package quantify.BoticaSaid.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import quantify.BoticaSaid.dto.*;
import quantify.BoticaSaid.model.Caja;
import quantify.BoticaSaid.service.CajaService;

import java.util.List;

@RestController
@RequestMapping("/api/cajas")
public class CajaController {

    private final CajaService cajaService;

    public CajaController(CajaService cajaService) {
        this.cajaService = cajaService;
    }

    /* ======================= Apertura / Cierre ======================= */

    @PostMapping("/abrir")
    public ResponseEntity<CajaResumenDTO> abrirCaja(@RequestBody CajaAperturaDTO cajaAperturaDTO) {
        Caja nuevaCaja = cajaService.abrirCaja(cajaAperturaDTO);
        CajaResumenDTO dto = cajaService.obtenerResumenCajaActual(nuevaCaja.getUsuario().getDni());
        return ResponseEntity.ok(dto);
    }

    @PostMapping("/cerrar")
    public ResponseEntity<CajaResumenDTO> cerrarCaja(@RequestBody CierreCajaDTO cierreCajaDTO) {
        Caja cajaCerrada = cajaService.cerrarCaja(cierreCajaDTO);
        CajaResumenDTO dto = cajaService.convertirCajaAResumen(cajaCerrada);
        return ResponseEntity.ok(dto);
    }

    /* ======================= Consultas ======================= */

    @GetMapping("/actual")
    public ResponseEntity<CajaResumenDTO> obtenerCajaActual(@RequestParam String dniUsuario) {
        CajaResumenDTO cajaResumen = cajaService.obtenerResumenCajaActual(dniUsuario);
        if (cajaResumen == null) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(cajaResumen);
    }

    /**
     * Historial de cajas PAGINADO.
     * - conMovimientos=false: no incluye movimientos (rápido).
     * - conMovimientos=true: incluye movimientos; si soloManuales=false también incluiría no manuales.
     */
    @GetMapping("/historial")
    public ResponseEntity<PageResponse<CajaResumenDTO>> obtenerHistorialCajas(
            @RequestParam(defaultValue = "false") boolean conMovimientos,
            @RequestParam(defaultValue = "true") boolean soloManuales,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        PageResponse<CajaResumenDTO> historial = conMovimientos
                ? cajaService.obtenerHistorialCajasConMovimientosPaginado(soloManuales, page, size)
                : cajaService.obtenerHistorialCajasPaginado(page, size);

        if (historial.content == null || historial.content.isEmpty()) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(historial);
    }

    @GetMapping("/abiertas")
    public ResponseEntity<List<CajaResumenDTO>> obtenerCajasAbiertas() {
        List<CajaResumenDTO> abiertas = cajaService.obtenerCajasAbiertas();
        if (abiertas.isEmpty()) return ResponseEntity.noContent().build();
        return ResponseEntity.ok(abiertas);
    }

    /* ======================= Movimientos manuales ======================= */

    @PostMapping("/movimiento")
    public ResponseEntity<String> registrarMovimientoManual(@RequestBody MovimientoEfectivoDTO dto) {
        try {
            cajaService.registrarMovimientoManual(dto);
            return ResponseEntity.status(HttpStatus.CREATED).body("Movimiento manual registrado exitosamente");
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Error: " + e.getMessage());
        }
    }

    @GetMapping("/{cajaId}/movimientos")
    public ResponseEntity<List<MovimientoDTO>> obtenerMovimientosCaja(
            @PathVariable Integer cajaId,
            @RequestParam(defaultValue = "true") boolean soloManuales,
            @RequestParam(defaultValue = "ASC") String direction
    ) {
        List<MovimientoDTO> lista = cajaService.obtenerMovimientosCaja(cajaId, soloManuales, direction);
        if (lista.isEmpty()) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(lista);
    }
}