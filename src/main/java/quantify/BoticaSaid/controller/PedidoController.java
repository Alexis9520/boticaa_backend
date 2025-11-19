package quantify.BoticaSaid.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import quantify.BoticaSaid.dto.pedido.AgregarStockConPedidoRequest;
import quantify.BoticaSaid.dto.pedido.PedidoReporteDTO;
import quantify.BoticaSaid.service.PedidoService;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/pedidos")
public class PedidoController {

    @Autowired
    private PedidoService pedidoService;

    /**
     * Endpoint para agregar stock y crear pedido(s) simultáneamente
     * 
     * POST /api/pedidos/agregar-stock
     * Body: {
     *   "stockData": {
     *     "productoId": 1,  // O usar "codigoBarras"
     *     "lotes": [
     *       {
     *         "codigoStock": "LOTE001",
     *         "cantidadUnidades": 100,
     *         "fechaVencimiento": "2025-12-31",
     *         "precioCompra": 10.50
     *       }
     *     ]
     *   },
     *   "fechaDePedido": "2024-11-19"
     * }
     * 
     * @param request contiene los datos del stock y la fecha de pedido
     * @return ResponseEntity con mensaje de éxito o error
     */
    @PostMapping("/agregar-stock")
    public ResponseEntity<Map<String, Object>> agregarStockConPedido(@RequestBody AgregarStockConPedidoRequest request) {
        Map<String, Object> response = new HashMap<>();
        
        boolean exito = pedidoService.agregarStockConPedido(request);
        
        if (exito) {
            response.put("success", true);
            response.put("message", "Stock agregado y pedido(s) creado(s) exitosamente");
            return ResponseEntity.ok(response);
        } else {
            response.put("success", false);
            response.put("message", "Error al agregar stock o crear pedido. Verifique que el producto exista y esté activo.");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }

    /**
     * Endpoint para obtener reporte de pedidos filtrado por proveedor y/o fecha
     * 
     * GET /api/pedidos/reporte?ID_proveedor=1&fecha_de_pedido=2024-11-19
     * 
     * @param proveedorId ID del proveedor (opcional)
     * @param fechaPedido Fecha de pedido en formato yyyy-MM-dd (opcional)
     * @return ResponseEntity con lista de pedidos y su información detallada
     */
    @GetMapping("/reporte")
    public ResponseEntity<List<PedidoReporteDTO>> obtenerReporte(
            @RequestParam(value = "ID_proveedor", required = false) Long proveedorId,
            @RequestParam(value = "fecha_de_pedido", required = false) 
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaPedido) {
        
        List<PedidoReporteDTO> reporte = pedidoService.obtenerReporte(proveedorId, fechaPedido);
        return ResponseEntity.ok(reporte);
    }
}
