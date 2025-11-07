package quantify.BoticaSaid.controller;

import quantify.BoticaSaid.dto.common.PageResponse;
import quantify.BoticaSaid.dto.producto.ProductSummaryDTO;
import quantify.BoticaSaid.dto.stock.StockItemDTO;
import quantify.BoticaSaid.service.StockService;
import quantify.BoticaSaid.service.StockSummaryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/stock")
public class StockController {

    @Autowired
    private StockService stockService;

    @Autowired
    private StockSummaryService stockSummaryService;

    // NUEVO: /api/stock -> paginado (con filtros)
    @GetMapping
    public ResponseEntity<PageResponse<StockItemDTO>> listarStock(
            @RequestParam(required = false) String q,         // busca por nombre o código
            @RequestParam(required = false) String lab,       // laboratorio
            @RequestParam(required = false) String cat,       // categoría
            @RequestParam(required = false) String codigo,    // código de barras exacto
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        var pageable = PageRequest.of(page, size);
        var paged = stockService.listarStockPaginado(q, lab, cat, codigo, pageable);
        return ResponseEntity.ok(paged);
    }

    // Ya estaba paginado (resumen por producto)
    @GetMapping("/products")
    public ResponseEntity<Map<String, Object>> listarResumenProductos(
            @RequestParam(required = false) String q,
            @RequestParam(required = false) String lab,
            @RequestParam(required = false) String cat,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        var paged = stockSummaryService.getProductSummaries(q, lab, cat, PageRequest.of(page, size));
        Map<String, Object> response = new HashMap<>();
        response.put("content", paged.getContent());
        response.put("totalElements", paged.getTotalElements());
        response.put("totalPages", paged.getTotalPages());
        response.put("page", paged.getNumber());
        response.put("size", paged.getSize());
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Void> actualizarStock(@PathVariable int id, @RequestBody StockItemDTO dto) {
        stockService.actualizarStock(id, dto);
        return ResponseEntity.ok().build();
    }
}