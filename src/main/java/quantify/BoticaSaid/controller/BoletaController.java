package quantify.BoticaSaid.controller;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import quantify.BoticaSaid.dto.boleta.BoletaResponseDTO;
import quantify.BoticaSaid.dto.boleta.DetalleBoletaResponseDTO;
import quantify.BoticaSaid.dto.common.PageResponse;
import quantify.BoticaSaid.dto.producto.DetalleProductoDTO;
import quantify.BoticaSaid.model.Boleta;
import quantify.BoticaSaid.model.Producto;
import quantify.BoticaSaid.repository.BoletaRepository;

import java.math.BigDecimal;
import java.math.RoundingMode;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@RestController
@RequestMapping("/api/boletas")
public class BoletaController {

    private final BoletaRepository boletaRepository;

    public BoletaController(BoletaRepository boletaRepository) {
        this.boletaRepository = boletaRepository;
    }

    /**
     * Listado paginado de boletas (sin detalles).
     * Contrato estándar:
     * - page: 0-based
     * - size: tamaño de página
     * - content/totalElements/page/size/totalPages
     * Filtros: search (o q), from (yyyy-MM-dd), to (yyyy-MM-dd)
     */
    @GetMapping
    public ResponseEntity<PageResponse<BoletaResponseDTO>> listarBoletas(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String q,
            @RequestParam(required = false) String from,
            @RequestParam(required = false) String to) {
        Pageable pageable = PageRequest.of(Math.max(0, page), Math.max(1, size),
                Sort.by(Sort.Direction.DESC, "fechaVenta"));

        String term = (search != null && !search.isBlank()) ? search : (q != null ? q : null);

        Specification<Boleta> spec = Specification.allOf();

        if (term != null && !term.isBlank()) {
            String like = "%" + term.trim().toLowerCase() + "%";
            spec = spec.and((root, query, cb) -> cb.or(
                    cb.like(cb.lower(root.get("nombreCliente")), like),
                    cb.like(cb.lower(root.get("numero")), like),
                    cb.like(cb.lower(root.get("id").as(String.class)), like)));
        }
        if (from != null && !from.isBlank()) {
            try {
                LocalDateTime fromDateTime = LocalDate.parse(from).atStartOfDay();
                spec = spec.and((root, query, cb) -> cb.greaterThanOrEqualTo(root.get("fechaVenta"), fromDateTime));
            } catch (Exception ignored) {
            }
        }
        if (to != null && !to.isBlank()) {
            try {
                LocalDateTime toDateTime = LocalDate.parse(to).atTime(LocalTime.MAX);
                spec = spec.and((root, query, cb) -> cb.lessThanOrEqualTo(root.get("fechaVenta"), toDateTime));
            } catch (Exception ignored) {
            }
        }

        // Precarga to-one para evitar N+1 en usuario/metodoPago. Evitamos detalles
        // (colección) en el listado.
        Page<Boleta> pageBoletas = boletaRepository.findAllWithUsuarioAndMetodo(spec, pageable);

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

        List<BoletaResponseDTO> content = pageBoletas.getContent().stream().map(b -> {
            BoletaResponseDTO dto = new BoletaResponseDTO();
            dto.setId(b.getId() != null ? b.getId().longValue() : null);
            dto.setNumero(b.getNumero());
            dto.setFecha(b.getFechaVenta() != null ? b.getFechaVenta().format(formatter) : "");
            dto.setCliente(b.getNombreCliente());
            dto.setTotalCompra(b.getTotalCompra());
            dto.setVuelto(b.getVuelto());
            dto.setMetodoPago(
                    (b.getMetodoPago() != null && b.getMetodoPago().getNombre() != null)
                            ? b.getMetodoPago().getNombre().toString()
                            : "");
            dto.setUsuario(b.getUsuario() != null ? b.getUsuario().getNombreCompleto() : "");
            // Listado sin productos
            return dto;
        }).toList();

        return ResponseEntity.ok(
                PageResponse.of(
                        content,
                        pageBoletas.getTotalElements(),
                        pageBoletas.getNumber(),
                        pageBoletas.getSize(),
                        pageBoletas.getTotalPages()));
    }

    /**
     * Obtener boleta por ID con productos enriquecidos (tipo venta, precios, etc.).
     * Soporta lógica híbrida: usa tipo_venta almacenado si existe, sino infiere por
     * precio.
     */
    @GetMapping("/{id}")
    public ResponseEntity<BoletaResponseDTO> obtenerPorId(@PathVariable Integer id) {
        var opt = boletaRepository.findByIdWithDetalles(id);
        if (opt.isEmpty())
            return ResponseEntity.notFound().build();

        var b = opt.get();
        var fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

        var dto = new BoletaResponseDTO();
        dto.setId(b.getId() != null ? b.getId().longValue() : null);
        dto.setNumero(b.getNumero());
        dto.setFecha(b.getFechaVenta() != null ? b.getFechaVenta().format(fmt) : "");
        dto.setCliente(b.getNombreCliente());
        dto.setTotalCompra(b.getTotalCompra());
        dto.setVuelto(b.getVuelto());
        dto.setMetodoPago(
                (b.getMetodoPago() != null && b.getMetodoPago().getNombre() != null)
                        ? b.getMetodoPago().getNombre().toString()
                        : "");
        dto.setUsuario(b.getUsuario() != null ? b.getUsuario().getNombreCompleto() : "");

        List<DetalleBoletaResponseDTO> detalles = b.getDetalles() == null ? List.of()
                : b.getDetalles().stream().map(d -> {
                    var det = new DetalleBoletaResponseDTO();
                    Producto producto = d.getProducto();

                    if (producto != null) {
                        det.setProductoId(producto.getId());
                        det.setCodigoBarras(producto.getCodigoBarras());
                        det.setNombre(producto.getNombre());
                        det.setUnidadesPorBlister(producto.getCantidadUnidadesBlister());
                        det.setPrecioActualUnd(producto.getPrecioVentaUnd());
                        det.setPrecioActualBlister(producto.getPrecioVentaBlister());
                    }

                    det.setCantidad(d.getCantidad());
                    det.setPrecioAplicado(d.getPrecioUnitario());

                    // Lógica híbrida para determinar tipo de venta
                    String tipoVenta = d.getTipoVenta();
                    if (tipoVenta == null && producto != null) {
                        // Inferir tipo comparando precio almacenado con precios actuales
                        BigDecimal precioAlmacenado = d.getPrecioUnitario();
                        BigDecimal precioBlister = producto.getPrecioVentaBlister();
                        BigDecimal precioUnd = producto.getPrecioVentaUnd();

                        if (precioBlister != null && precioAlmacenado != null
                                && precioAlmacenado.compareTo(precioBlister) == 0) {
                            tipoVenta = "BLISTER";
                        } else if (precioUnd != null && precioAlmacenado != null
                                && precioAlmacenado.compareTo(precioUnd) == 0) {
                            tipoVenta = "UNIDAD";
                        } else {
                            tipoVenta = "DESCONOCIDO";
                        }
                    }
                    det.setTipoVenta(tipoVenta);

                    // Calcular cantidad de blisters si aplica
                    if ("BLISTER".equals(tipoVenta) && producto != null
                            && producto.getCantidadUnidadesBlister() != null
                            && producto.getCantidadUnidadesBlister() > 0) {
                        det.setCantidadBlisters(d.getCantidad() / producto.getCantidadUnidadesBlister());
                    } else {
                        det.setCantidadBlisters(0);
                    }

                    // Detectar si el precio fue modificado
                    boolean precioModificado = false;
                    if (producto != null && d.getPrecioUnitario() != null) {
                        BigDecimal precioAlmacenado = d.getPrecioUnitario();
                        BigDecimal precioBlister = producto.getPrecioVentaBlister();
                        BigDecimal precioUnd = producto.getPrecioVentaUnd();

                        boolean coincideBlister = precioBlister != null
                                && precioAlmacenado.compareTo(precioBlister) == 0;
                        boolean coincideUnd = precioUnd != null
                                && precioAlmacenado.compareTo(precioUnd) == 0;

                        precioModificado = !coincideBlister && !coincideUnd;
                    }
                    det.setPrecioModificado(precioModificado);

                    // Calcular subtotal
                    if ("BLISTER".equals(tipoVenta) && det.getCantidadBlisters() != null
                            && det.getPrecioAplicado() != null) {
                        det.setSubtotal(det.getPrecioAplicado().multiply(BigDecimal.valueOf(det.getCantidadBlisters()))
                                .setScale(2, RoundingMode.HALF_UP));
                    } else if (det.getCantidad() != null && det.getPrecioAplicado() != null) {
                        det.setSubtotal(det.getPrecioAplicado().multiply(BigDecimal.valueOf(det.getCantidad()))
                                .setScale(2, RoundingMode.HALF_UP));
                    }

                    return det;
                }).toList();

        dto.setDetallesEnriquecidos(detalles);

        return ResponseEntity.ok(dto);
    }
}