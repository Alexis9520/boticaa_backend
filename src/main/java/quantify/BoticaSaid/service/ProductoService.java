package quantify.BoticaSaid.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import quantify.BoticaSaid.dto.dashboard.DashboardResumenDTO;
import quantify.BoticaSaid.dto.producto.ProductoRequest;
import quantify.BoticaSaid.dto.producto.ProductoResponse;
import quantify.BoticaSaid.dto.stock.AgregarStockRequest;
import quantify.BoticaSaid.dto.stock.StockLoteDTO;
import quantify.BoticaSaid.model.Producto;
import quantify.BoticaSaid.model.Stock;
import quantify.BoticaSaid.repository.ProductoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

import quantify.BoticaSaid.repository.StockRepository;
import quantify.BoticaSaid.repository.ProveedorRepository;
import quantify.BoticaSaid.model.Proveedor;

@Service
public class ProductoService {

    @Autowired
    private ProductoRepository productoRepository;

    @Autowired
    private StockRepository stockRepository;

    @Autowired
    private ProveedorRepository proveedorRepository;

    /**
     * Crear producto con stock.
     * Sólo el nombre es obligatorio.
     * Se mantiene la lógica de reactivación si existe el código de barras y está inactivo.
     */
    @Transactional
    public Object crearProductoConStock(ProductoRequest request) {

        // Validar único campo obligatorio
        if (request.getNombre() == null || request.getNombre().trim().isEmpty()) {
            throw new IllegalArgumentException("El nombre del producto es obligatorio.");
        }

        System.out.println("=== CREANDO PRODUCTO ===");
        System.out.println("Código de barras: " + request.getCodigoBarras());
        System.out.println("Stocks recibidos: " + (request.getStocks() != null ? request.getStocks().size() : 0));

        // Buscar por código de barras sólo si vino un valor (puede ser null)
        Producto existente = (request.getCodigoBarras() != null && !request.getCodigoBarras().isBlank())
                ? productoRepository.findByCodigoBarras(request.getCodigoBarras())
                : null;

        if (existente != null) {
            if (!existente.isActivo()) {
                // Reactivar y actualizar datos (campos opcionales pueden ser null)
                existente.setActivo(true);
                existente.setNombre(request.getNombre());
                existente.setCodigoBarras(request.getCodigoBarras());
                existente.setConcentracion(request.getConcentracion());
                existente.setCantidadGeneral(request.getCantidadGeneral()); // Se recalculará si hay stocks
                existente.setPrecioVentaUnd(request.getPrecioVentaUnd());
                existente.setDescuento(request.getDescuento());
                existente.setLaboratorio(request.getLaboratorio());
                existente.setCategoria(request.getCategoria());
                existente.setCantidadUnidadesBlister(request.getCantidadUnidadesBlister());
                existente.setPrecioVentaBlister(request.getPrecioVentaBlister());
                existente.setCantidadMinima(request.getCantidadMinima());
                existente.setPrincipioActivo(request.getPrincipioActivo());
                existente.setTipoMedicamento(request.getTipoMedicamento());
                existente.setPresentacion(request.getPresentacion());

                // Manejar proveedor
                if (request.getProveedorId() != null) {
                    Optional<Proveedor> proveedorOpt = proveedorRepository.findById(request.getProveedorId());
                    if (proveedorOpt.isPresent() && proveedorOpt.get().isActivo()) {
                        existente.setProveedor(proveedorOpt.get());
                    } else {
                        existente.setProveedor(null);
                    }
                } else {
                    existente.setProveedor(null);
                }

                // Limpiar stocks anteriores (orphanRemoval activo)
                existente.getStocks().clear();

                int acumulador = 0;
                if (request.getStocks() != null && !request.getStocks().isEmpty()) {
                    for (var stockReq : request.getStocks()) {
                        Stock stock = new Stock();
                        stock.setCodigoStock(stockReq.getCodigoStock());
                        int cant = stockReq.getCantidadUnidades(); // int
                        acumulador += cant;
                        stock.setCantidadUnidades(cant);
                        stock.setCantidadInicial(cant); // Set initial quantity
                        stock.setFechaVencimiento(stockReq.getFechaVencimiento());
                        stock.setPrecioCompra(stockReq.getPrecioCompra());
                        stock.setProducto(existente);
                        existente.getStocks().add(stock);
                    }
                    existente.setCantidadGeneral(acumulador);
                } else {
                    // Si no hay stocks y no se mandó cantidadGeneral -> poner 0
                    if (existente.getCantidadGeneral() == null) {
                        existente.setCantidadGeneral(0);
                    }
                }

                Producto guardado = productoRepository.save(existente);
                Map<String, Object> response = new HashMap<>();
                response.put("reactivado", true);
                response.put("producto", guardado);
                return response;
            } else {
                throw new IllegalArgumentException("Ya existe un producto activo con ese código de barras.");
            }
        }

        // Crear nuevo producto
        Producto producto = new Producto();
        producto.setCodigoBarras(request.getCodigoBarras());
        producto.setNombre(request.getNombre());
        producto.setConcentracion(request.getConcentracion());
        producto.setPrecioVentaUnd(request.getPrecioVentaUnd());
        producto.setDescuento(request.getDescuento());
        producto.setLaboratorio(request.getLaboratorio());
        producto.setCategoria(request.getCategoria());
        producto.setCantidadUnidadesBlister(request.getCantidadUnidadesBlister());
        producto.setPrecioVentaBlister(request.getPrecioVentaBlister());
        producto.setActivo(true);
        producto.setCantidadMinima(request.getCantidadMinima());
        producto.setPrincipioActivo(request.getPrincipioActivo());
        producto.setTipoMedicamento(request.getTipoMedicamento());
        producto.setPresentacion(request.getPresentacion());

        // Manejar proveedor
        if (request.getProveedorId() != null) {
            Optional<Proveedor> proveedorOpt = proveedorRepository.findById(request.getProveedorId());
            if (proveedorOpt.isPresent() && proveedorOpt.get().isActivo()) {
                producto.setProveedor(proveedorOpt.get());
            }
        }

        int acumuladorPadre = 0;
        if (request.getStocks() != null && !request.getStocks().isEmpty()) {
            for (var stockReq : request.getStocks()) {
                Stock stock = new Stock();
                stock.setCodigoStock(stockReq.getCodigoStock());
                int cant = stockReq.getCantidadUnidades(); // int
                acumuladorPadre += cant;
                stock.setCantidadUnidades(cant);
                stock.setCantidadInicial(cant); // Set initial quantity
                stock.setFechaVencimiento(stockReq.getFechaVencimiento());
                stock.setPrecioCompra(stockReq.getPrecioCompra());
                stock.setProducto(producto);
                producto.getStocks().add(stock);
            }
            producto.setCantidadGeneral(acumuladorPadre);
        } else {
            // Si no hay stocks: usar lo que vino en el request o 0
            producto.setCantidadGeneral(request.getCantidadGeneral() != null ? request.getCantidadGeneral() : 0);
        }

        Producto guardado = productoRepository.save(producto);
        return guardado;
    }

    // Buscar producto por ID con stocks
    public Producto buscarPorId(Long id) {
        Optional<Producto> prodOpt = productoRepository.findByIdWithStocks(id);
        if (prodOpt.isPresent() && prodOpt.get().isActivo()) {
            return prodOpt.get();
        }
        Optional<Producto> prod = productoRepository.findById(id);
        return (prod.isPresent() && prod.get().isActivo()) ? prod.get() : null;
    }

    // Buscar producto por código de barras con stocks
    public Producto buscarPorCodigoBarras(String codigoBarras) {
        if (codigoBarras == null || codigoBarras.isBlank()) return null;
        Optional<Producto> prodOpt = productoRepository.findByCodigoBarrasWithStocks(codigoBarras);
        if (prodOpt.isPresent() && prodOpt.get().isActivo()) {
            return prodOpt.get();
        }
        Producto prod = productoRepository.findByCodigoBarras(codigoBarras);
        return (prod != null && prod.isActivo()) ? prod : null;
    }

    // Listar todos los productos activos con stocks
    public List<Producto> listarTodos() {
        try {
            return productoRepository.findByActivoTrueWithStocks();
        } catch (Exception e) {
            return productoRepository.findByActivoTrue();
        }
    }

    // Agregar stock adicional
    @Transactional
    public boolean agregarStock(AgregarStockRequest request) {
        Producto producto = null;

        // Buscar por ID
        if (request.getProductoId() != null) {
            Optional<Producto> prodOpt = productoRepository.findByIdWithStocks(request.getProductoId());
            producto = prodOpt.orElseGet(() -> productoRepository.findById(request.getProductoId()).orElse(null));
        }

        // Buscar por código de barras si no se encontró por ID
        if (producto == null && request.getCodigoBarras() != null && !request.getCodigoBarras().isBlank()) {
            Optional<Producto> prodOpt = productoRepository.findByCodigoBarrasWithStocks(request.getCodigoBarras());
            producto = prodOpt.orElseGet(() -> productoRepository.findByCodigoBarras(request.getCodigoBarras()));
        }

        if (producto == null || !producto.isActivo()) {
            return false;
        }

        Stock nuevoStock = new Stock();
        nuevoStock.setCodigoStock(request.getCodigoStock());
        int cant = request.getCantidadUnidades(); // int esperado
        nuevoStock.setCantidadUnidades(cant);
        nuevoStock.setCantidadInicial(cant); // Set initial quantity
        nuevoStock.setFechaVencimiento(request.getFechaVencimiento());
        nuevoStock.setPrecioCompra(request.getPrecioCompra());
        nuevoStock.setProducto(producto);

        producto.getStocks().add(nuevoStock);

        int actual = producto.getCantidadGeneral() != null ? producto.getCantidadGeneral() : 0;
        int agregar = request.getCantidadUnidades(); // int
        producto.setCantidadGeneral(actual + agregar);

        productoRepository.save(producto);
        return true;
    }

    /**
     * Agregar uno o múltiples lotes de stock a un producto existente
     * sin modificar los datos del producto
     */
    @Transactional
    public boolean agregarLotes(quantify.BoticaSaid.dto.stock.AgregarLoteRequest request) {
        Producto producto = null;

        // Buscar por ID
        if (request.getProductoId() != null) {
            Optional<Producto> prodOpt = productoRepository.findByIdWithStocks(request.getProductoId());
            producto = prodOpt.orElseGet(() -> productoRepository.findById(request.getProductoId()).orElse(null));
        }

        // Buscar por código de barras si no se encontró por ID
        if (producto == null && request.getCodigoBarras() != null && !request.getCodigoBarras().isBlank()) {
            Optional<Producto> prodOpt = productoRepository.findByCodigoBarrasWithStocks(request.getCodigoBarras());
            producto = prodOpt.orElseGet(() -> productoRepository.findByCodigoBarras(request.getCodigoBarras()));
        }

        if (producto == null || !producto.isActivo()) {
            return false;
        }

        if (request.getLotes() == null || request.getLotes().isEmpty()) {
            return false;
        }

        int totalUnidades = 0;
        for (var loteItem : request.getLotes()) {
            Stock nuevoStock = new Stock();
            nuevoStock.setCodigoStock(loteItem.getCodigoStock());
            int cant = loteItem.getCantidadUnidades();
            nuevoStock.setCantidadUnidades(cant);
            nuevoStock.setCantidadInicial(cant); // Set initial quantity
            nuevoStock.setFechaVencimiento(loteItem.getFechaVencimiento());
            nuevoStock.setPrecioCompra(loteItem.getPrecioCompra());
            nuevoStock.setProducto(producto);

            producto.getStocks().add(nuevoStock);
            totalUnidades += cant;
        }

        int actual = producto.getCantidadGeneral() != null ? producto.getCantidadGeneral() : 0;
        producto.setCantidadGeneral(actual + totalUnidades);

        productoRepository.save(producto);
        return true;
    }

    // Buscar por nombre o categoría (legacy)
    public List<Producto> buscarPorNombreOCategoria(String nombre, String categoria) {
        try {
            List<Producto> todosConStocks = productoRepository.findByActivoTrueWithStocks();
            return filtrarPorNombreCategoria(todosConStocks, nombre, categoria);
        } catch (Exception e) {
            if (nombre != null && categoria != null) {
                return productoRepository.findByNombreContainingIgnoreCaseAndCategoriaContainingIgnoreCaseAndActivoTrue(nombre, categoria);
            } else if (nombre != null) {
                return productoRepository.findByNombreContainingIgnoreCaseAndActivoTrue(nombre);
            } else if (categoria != null) {
                return productoRepository.findByCategoriaContainingIgnoreCaseAndActivoTrue(categoria);
            } else {
                return productoRepository.findByActivoTrue();
            }
        }
    }

    private List<Producto> filtrarPorNombreCategoria(List<Producto> base, String nombre, String categoria) {
        return base.stream()
                .filter(p -> {
                    boolean ok = true;
                    if (nombre != null) {
                        ok = ok && p.getNombre() != null &&
                                p.getNombre().toLowerCase().contains(nombre.toLowerCase());
                    }
                    if (categoria != null) {
                        ok = ok && p.getCategoria() != null &&
                                p.getCategoria().toLowerCase().contains(categoria.toLowerCase());
                    }
                    return ok;
                })
                .toList();
    }

    // Borrado lógico por ID
    @Transactional
    public boolean eliminarPorId(Long id) {
        Optional<Producto> productoOpt = productoRepository.findById(id);
        if (productoOpt.isPresent() && productoOpt.get().isActivo()) {
            Producto producto = productoOpt.get();
            producto.setActivo(false);
            productoRepository.save(producto);
            return true;
        }
        return false;
    }

    // Borrado lógico por código de barras (Deprecated)
    @Transactional
    @Deprecated
    public boolean eliminarPorCodigoBarras(String codigoBarras) {
        if (codigoBarras == null || codigoBarras.isBlank()) return false;
        Producto producto = productoRepository.findByCodigoBarras(codigoBarras);
        if (producto != null && producto.isActivo()) {
            producto.setActivo(false);
            productoRepository.save(producto);
            return true;
        }
        return false;
    }

    // Actualizar por código de barras (Deprecated)
    @Transactional
    @Deprecated
    public Producto actualizarPorCodigoBarras(String codigoBarras, ProductoRequest request) {
        if (codigoBarras == null || codigoBarras.isBlank()) {
            throw new IllegalArgumentException("Código de barras inválido.");
        }
        Optional<Producto> prodOpt = productoRepository.findByCodigoBarrasWithStocks(codigoBarras);
        Producto producto = prodOpt.orElseGet(() -> productoRepository.findByCodigoBarras(codigoBarras));

        if (producto != null && producto.isActivo()) {
            aplicarDatosProductoDesdeRequest(producto, request, true);
            Producto guardado = productoRepository.save(producto);
            return productoRepository.findByCodigoBarrasWithStocks(codigoBarras).orElse(guardado);
        }
        return null;
    }

    // Actualizar por ID
    @Transactional
    public Producto actualizarPorID(Long id, ProductoRequest request) {
        Producto producto = productoRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Producto no encontrado con id: " + id));

        if (!producto.isActivo()) {
            throw new IllegalArgumentException("No se puede actualizar un producto inactivo");
        }

        aplicarDatosProductoDesdeRequest(producto, request, true);

        Producto guardado = productoRepository.save(producto);
        return productoRepository.findByIdWithStocks(guardado.getId()).orElse(guardado);
    }

    /**
     * Aplica los datos del request al producto.
     * Si replaceStocks = true, limpia y recrea stocks.
     * Recalcula cantidadGeneral si hay stocks nuevos; de lo contrario toma valor del request (o conserva si ambos null).
     */
    private void aplicarDatosProductoDesdeRequest(Producto producto, ProductoRequest request, boolean replaceStocks) {
        // Sólo el nombre es obligatorio; validamos si se pretende cambiarlo.
        if (request.getNombre() == null || request.getNombre().trim().isEmpty()) {
            throw new IllegalArgumentException("El nombre del producto es obligatorio.");
        }

        producto.setCodigoBarras(request.getCodigoBarras());
        producto.setNombre(request.getNombre());
        producto.setConcentracion(request.getConcentracion());
        producto.setPrecioVentaUnd(request.getPrecioVentaUnd());
        producto.setDescuento(request.getDescuento());
        producto.setLaboratorio(request.getLaboratorio());
        producto.setCategoria(request.getCategoria());
        producto.setCantidadUnidadesBlister(request.getCantidadUnidadesBlister());
        producto.setPrecioVentaBlister(request.getPrecioVentaBlister());
        producto.setCantidadMinima(request.getCantidadMinima());
        producto.setPrincipioActivo(request.getPrincipioActivo());
        producto.setTipoMedicamento(request.getTipoMedicamento());
        producto.setPresentacion(request.getPresentacion());

        // Manejar proveedor
        if (request.getProveedorId() != null) {
            Optional<Proveedor> proveedorOpt = proveedorRepository.findById(request.getProveedorId());
            if (proveedorOpt.isPresent() && proveedorOpt.get().isActivo()) {
                producto.setProveedor(proveedorOpt.get());
            } else {
                producto.setProveedor(null);
            }
        } else {
            producto.setProveedor(null);
        }

        if (replaceStocks) {
            producto.getStocks().clear();
            int acumulador = 0;
            if (request.getStocks() != null && !request.getStocks().isEmpty()) {
                for (var stockReq : request.getStocks()) {
                    Stock stock = new Stock();
                    stock.setCodigoStock(stockReq.getCodigoStock());
                    int cant = stockReq.getCantidadUnidades(); // int
                    acumulador += cant;
                    stock.setCantidadUnidades(cant);
                    stock.setCantidadInicial(cant); // Set initial quantity
                    stock.setFechaVencimiento(stockReq.getFechaVencimiento());
                    stock.setPrecioCompra(stockReq.getPrecioCompra());
                    stock.setProducto(producto);
                    producto.getStocks().add(stock);
                }
                producto.setCantidadGeneral(acumulador);
            } else {
                // Si no hay stocks nuevos: usar valor del request o conservar el existente
                if (request.getCantidadGeneral() != null) {
                    producto.setCantidadGeneral(request.getCantidadGeneral());
                } else if (producto.getCantidadGeneral() == null) {
                    producto.setCantidadGeneral(0);
                }
            }
        } else {
            // No se reemplazan stocks: sólo ajustar cantidadGeneral si vino explícita
            if (request.getCantidadGeneral() != null) {
                producto.setCantidadGeneral(request.getCantidadGeneral());
            } else if (producto.getCantidadGeneral() == null) {
                producto.setCantidadGeneral(0);
            }
        }
    }

    // Productos con stock menor a umbral
    public List<Producto> buscarProductosConStockMenorA(int umbral) {
        try {
            List<Producto> productos = productoRepository.findByActivoTrueWithStocks();
            return productos.stream()
                    .filter(p -> (p.getCantidadGeneral() != null ? p.getCantidadGeneral() : 0) < umbral)
                    .toList();
        } catch (Exception e) {
            List<Producto> productos = productoRepository.findByActivoTrue();
            return productos.stream()
                    .filter(p -> (p.getCantidadGeneral() != null ? p.getCantidadGeneral() : 0) < umbral)
                    .toList();
        }
    }

    public StockLoteDTO toStockLoteDTO(Stock stock) {
        StockLoteDTO dto = new StockLoteDTO();
        dto.setCodigoStock(stock.getCodigoStock());
        dto.setCantidadUnidades(stock.getCantidadUnidades());
        dto.setFechaVencimiento(stock.getFechaVencimiento());
        dto.setPrecioCompra(stock.getPrecioCompra());
        return dto;
    }

    public ProductoResponse toProductoResponse(Producto producto) {
        ProductoResponse resp = new ProductoResponse();
        resp.setId(producto.getId());
        resp.setCodigoBarras(producto.getCodigoBarras());
        resp.setNombre(producto.getNombre());
        resp.setConcentracion(producto.getConcentracion());
        resp.setCantidadGeneral(producto.getCantidadGeneral());
        resp.setCantidadMinima(producto.getCantidadMinima());
        resp.setPrecioVentaUnd(producto.getPrecioVentaUnd());
        resp.setDescuento(producto.getDescuento());
        resp.setLaboratorio(producto.getLaboratorio());
        resp.setCategoria(producto.getCategoria());
        resp.setCantidadUnidadesBlister(producto.getCantidadUnidadesBlister());
        resp.setPrecioVentaBlister(producto.getPrecioVentaBlister());
        resp.setPrincipioActivo(producto.getPrincipioActivo());
        resp.setTipoMedicamento(producto.getTipoMedicamento());
        resp.setPresentacion(producto.getPresentacion());

        // Agregar información del proveedor
        if (producto.getProveedor() != null) {
            resp.setProveedorId(producto.getProveedor().getId());
            resp.setProveedorNombre(producto.getProveedor().getRazonComercial() != null 
                ? producto.getProveedor().getRazonComercial() 
                : producto.getProveedor().getRuc());
        }

        if (producto.getStocks() != null && !producto.getStocks().isEmpty()) {
            resp.setStocks(
                    producto.getStocks().stream()
                            .map(this::toStockLoteDTO)
                            .collect(Collectors.toList())
            );
        } else {
            resp.setStocks(new ArrayList<>());
        }

        return resp;
    }

    public List<DashboardResumenDTO.ProductoMasVendidoDTO> getProductosMasVendidosDTO(int top) {
        List<Object[]> resultados = productoRepository.findProductosMasVendidos(PageRequest.of(0, top));
        return resultados.stream().map(r -> {
            DashboardResumenDTO.ProductoMasVendidoDTO dto = new DashboardResumenDTO.ProductoMasVendidoDTO();
            dto.nombre = (String) r[0];
            dto.unidades = ((Number) r[1]).intValue();
            dto.porcentaje = r.length > 2 ? ((Number) r[2]).doubleValue() : 0.0;
            return dto;
        }).collect(Collectors.toList());
    }

    public List<DashboardResumenDTO.ProductoCriticoDTO> getProductosCriticosDTO() {
        int umbralCritico = 10;
        return listarTodos().stream()
                .filter(p -> (p.getCantidadGeneral() != null ? p.getCantidadGeneral() : 0) < umbralCritico)
                .map(p -> {
                    DashboardResumenDTO.ProductoCriticoDTO dto = new DashboardResumenDTO.ProductoCriticoDTO();
                    dto.nombre = p.getNombre();
                    dto.stock = p.getCantidadGeneral() != null ? p.getCantidadGeneral() : 0;
                    return dto;
                }).collect(Collectors.toList());
    }

    public List<DashboardResumenDTO.ProductoVencimientoDTO> getProductosPorVencerDTO() {
        int diasAviso = 30;
        LocalDate hoy = LocalDate.now(java.time.ZoneId.of("America/Lima"));

        return listarTodos().stream()
                .flatMap(p -> p.getStocks().stream()
                        .filter(stock -> stock.getFechaVencimiento() != null)
                        .filter(stock -> {
                            long dias = java.time.temporal.ChronoUnit.DAYS.between(hoy, stock.getFechaVencimiento());
                            return dias >= 0 && dias <= diasAviso;
                        })
                        .map(stock -> {
                            DashboardResumenDTO.ProductoVencimientoDTO dto = new DashboardResumenDTO.ProductoVencimientoDTO();
                            dto.nombre = p.getNombre();
                            long dias = java.time.temporal.ChronoUnit.DAYS.between(hoy, stock.getFechaVencimiento());
                            dto.dias = (int) dias;
                            return dto;
                        })
                )
                .sorted(Comparator.comparingInt(dto -> dto.dias))
                .collect(Collectors.toList());
    }

    // Paginación legacy
    public List<Producto> listarTodosPaginado(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Producto> paged = productoRepository.findByActivoTrue(pageable);
        List<Producto> productos = paged.getContent();
        productos.forEach(p -> {
            if (p.getStocks() != null) p.getStocks().size();
        });
        return productos;
    }

    public Page<Producto> listarTodosPaginadoPage(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return productoRepository.findByActivoTrue(pageable);
    }

    // Búsqueda paginada con filtros
    public Page<Producto> buscarPaginadoPorQuery(String q, String lab, String cat, Pageable pageable) {
        Page<Producto> paged;
        boolean anyFilter = (q != null && !q.trim().isEmpty()) || (lab != null && !lab.isBlank()) || (cat != null && !cat.isBlank());
        if (!anyFilter) {
            paged = productoRepository.findByActivoTrue(pageable);
        } else {
            paged = productoRepository.search(
                    q == null ? null : q.trim(),
                    lab == null ? null : lab.trim(),
                    cat == null ? null : cat.trim(),
                    pageable
            );
        }

        // Forzar carga de stocks
        paged.getContent().forEach(p -> {
            if (p.getStocks() != null) p.getStocks().size();
        });

        return paged;
    }

    // Buscar productos por proveedor
    public List<Producto> buscarPorProveedorId(Long proveedorId) {
        return productoRepository.findByProveedorIdAndActivoTrue(proveedorId);
    }
}