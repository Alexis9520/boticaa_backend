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

@Service
public class ProductoService {

    @Autowired
    private ProductoRepository productoRepository;

    @Autowired
    private StockRepository stockRepository;

    // 1. Crear producto con stock (valida duplicidad por código de barras, permite reactivar)
    @Transactional
    public Object crearProductoConStock(ProductoRequest request) {

        int acumuladorPadre = 0;

        System.out.println("=== CREANDO PRODUCTO ===");
        System.out.println("Código de barras: " + request.getCodigoBarras());
        System.out.println("Stocks recibidos: " + (request.getStocks() != null ? request.getStocks().size() : 0));

        Producto existente = productoRepository.findByCodigoBarras(request.getCodigoBarras());
        if (existente != null) {
            if (!existente.isActivo()) {
                // Reactivar y actualizar datos
                existente.setActivo(true);
                existente.setNombre(request.getNombre());
                existente.setConcentracion(request.getConcentracion());
                existente.setCantidadGeneral(request.getCantidadGeneral());
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

                existente.getStocks().clear(); // orphanRemoval

                if (request.getStocks() != null && !request.getStocks().isEmpty()) {
                    for (var stockReq : request.getStocks()) {
                        Stock stock = new Stock();
                        stock.setCodigoStock(stockReq.getCodigoStock());
                        stock.setCantidadUnidades(stockReq.getCantidadUnidades());
                        stock.setFechaVencimiento(stockReq.getFechaVencimiento());
                        stock.setPrecioCompra(stockReq.getPrecioCompra());
                        stock.setProducto(existente);
                        existente.getStocks().add(stock);
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

        Producto producto = new Producto();
        producto.setCodigoBarras(request.getCodigoBarras());
        producto.setNombre(request.getNombre());
        producto.setConcentracion(request.getConcentracion());
        producto.setCantidadGeneral(request.getCantidadGeneral());
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

        if (request.getStocks() != null && !request.getStocks().isEmpty()) {
            for (var stockReq : request.getStocks()) {
                Stock stock = new Stock();
                stock.setCodigoStock(stockReq.getCodigoStock());
                stock.setCantidadUnidades(stockReq.getCantidadUnidades());
                acumuladorPadre += stockReq.getCantidadUnidades();
                stock.setFechaVencimiento(stockReq.getFechaVencimiento());
                stock.setPrecioCompra(stockReq.getPrecioCompra());
                stock.setProducto(producto);
                producto.getStocks().add(stock);
            }
        }

        producto.setCantidadGeneral(acumuladorPadre);
        Producto guardado = productoRepository.save(producto);
        return guardado;
    }

    // 2. Buscar producto por ID con stocks
    public Producto buscarPorId(Long id) {
        Optional<Producto> prodOpt = productoRepository.findByIdWithStocks(id);
        if (prodOpt.isPresent() && prodOpt.get().isActivo()) {
            return prodOpt.get();
        }
        Optional<Producto> prod = productoRepository.findById(id);
        return (prod.isPresent() && prod.get().isActivo()) ? prod.get() : null;
    }

    // 3. Buscar producto por código de barras con stocks
    public Producto buscarPorCodigoBarras(String codigoBarras) {
        Optional<Producto> prodOpt = productoRepository.findByCodigoBarrasWithStocks(codigoBarras);
        if (prodOpt.isPresent() && prodOpt.get().isActivo()) {
            return prodOpt.get();
        }
        Producto prod = productoRepository.findByCodigoBarras(codigoBarras);
        return (prod != null && prod.isActivo()) ? prod : null;
    }

    // 4. Listar todos los productos activos con stocks
    public List<Producto> listarTodos() {
        try {
            List<Producto> productos = productoRepository.findByActivoTrueWithStocks();
            return productos;
        } catch (Exception e) {
            return productoRepository.findByActivoTrue();
        }
    }

    // 5. Agregar stock adicional
    @Transactional
    public boolean agregarStock(AgregarStockRequest request) {
        Producto producto = null;
        
        // Intentar buscar por ID primero
        if (request.getProductoId() != null) {
            Optional<Producto> prodOpt = productoRepository.findByIdWithStocks(request.getProductoId());
            producto = prodOpt.orElseGet(() -> productoRepository.findById(request.getProductoId()).orElse(null));
        }
        
        // Si no se encontró por ID, buscar por código de barras
        if (producto == null && request.getCodigoBarras() != null) {
            Optional<Producto> prodOpt = productoRepository.findByCodigoBarrasWithStocks(request.getCodigoBarras());
            producto = prodOpt.orElseGet(() -> productoRepository.findByCodigoBarras(request.getCodigoBarras()));
        }

        if (producto == null || !producto.isActivo()) {
            return false;
        }

        Stock nuevoStock = new Stock();
        nuevoStock.setCodigoStock(request.getCodigoStock());
        nuevoStock.setCantidadUnidades(request.getCantidadUnidades());
        nuevoStock.setFechaVencimiento(request.getFechaVencimiento());
        nuevoStock.setPrecioCompra(request.getPrecioCompra());
        nuevoStock.setProducto(producto);

        producto.getStocks().add(nuevoStock);
        producto.setCantidadGeneral(producto.getCantidadGeneral() + request.getCantidadUnidades());
        productoRepository.save(producto);

        return true;
    }

    // 6. Buscar por nombre o categoría con stocks (legacy endpoint)
    public List<Producto> buscarPorNombreOCategoria(String nombre, String categoria) {
        try {
            List<Producto> todosConStocks = productoRepository.findByActivoTrueWithStocks();
            if (nombre != null && categoria != null) {
                return todosConStocks.stream()
                        .filter(p -> p.getNombre().toLowerCase().contains(nombre.toLowerCase())
                                && p.getCategoria().toLowerCase().contains(categoria.toLowerCase()))
                        .toList();
            } else if (nombre != null) {
                return todosConStocks.stream()
                        .filter(p -> p.getNombre().toLowerCase().contains(nombre.toLowerCase()))
                        .toList();
            } else if (categoria != null) {
                return todosConStocks.stream()
                        .filter(p -> p.getCategoria().toLowerCase().contains(categoria.toLowerCase()))
                        .toList();
            } else {
                return todosConStocks;
            }
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

    // 7. Borrado lógico por ID (set activo=false)
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

    // 8. Borrado lógico por código de barras (set activo=false) - Deprecated, usar eliminarPorId
    @Transactional
    @Deprecated
    public boolean eliminarPorCodigoBarras(String codigoBarras) {
        Producto producto = productoRepository.findByCodigoBarras(codigoBarras);
        if (producto != null && producto.isActivo()) {
            producto.setActivo(false);
            productoRepository.save(producto);
            return true;
        }
        return false;
    }

    // 9. Actualizar datos de un producto con stocks por código de barras - Deprecated, usar actualizarPorID
    @Transactional
    @Deprecated
    public Producto actualizarPorCodigoBarras(String codigoBarras, ProductoRequest request) {
        Optional<Producto> prodOpt = productoRepository.findByCodigoBarrasWithStocks(codigoBarras);
        Producto producto = prodOpt.orElseGet(() -> productoRepository.findByCodigoBarras(codigoBarras));

        if (producto != null && producto.isActivo()) {
            producto.setNombre(request.getNombre());
            producto.setConcentracion(request.getConcentracion());
            producto.setCantidadGeneral(request.getCantidadGeneral());
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

            producto.getStocks().clear();

            if (request.getStocks() != null && !request.getStocks().isEmpty()) {
                for (var stockReq : request.getStocks()) {
                    Stock stock = new Stock();
                    stock.setCodigoStock(stockReq.getCodigoStock());
                    stock.setCantidadUnidades(stockReq.getCantidadUnidades());
                    stock.setFechaVencimiento(stockReq.getFechaVencimiento());
                    stock.setPrecioCompra(stockReq.getPrecioCompra());
                    stock.setProducto(producto);
                    producto.getStocks().add(stock);
                }
            }

            Producto guardado = productoRepository.save(producto);

            Optional<Producto> verificacion = productoRepository.findByCodigoBarrasWithStocks(codigoBarras);
            return verificacion.orElse(guardado);
        }
        return null;
    }

    // 10. Actualizar datos de un producto con stocks por ID
    @Transactional
    public Producto actualizarPorID(Long id, ProductoRequest request) {
        // Buscar producto o lanzar excepción si no existe
        Producto producto = productoRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Producto no encontrado con id: " + id));

        if (!producto.isActivo()) {
            throw new IllegalArgumentException("No se puede actualizar un producto inactivo");
        }

        // Actualizar campos básicos
        producto.setCodigoBarras(request.getCodigoBarras());
        producto.setNombre(request.getNombre());
        producto.setConcentracion(request.getConcentracion());
        producto.setCantidadGeneral(request.getCantidadGeneral());
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

        // Reemplazar stocks: requiere cascade = ALL y orphanRemoval = true en Producto.stocks
        producto.getStocks().clear();

        if (request.getStocks() != null && !request.getStocks().isEmpty()) {
            for (var stockReq : request.getStocks()) {
                Stock stock = new Stock();
                // no seteamos id para crear nuevos registros; si quieres actualizar por id
                // tendrías que buscar cada stock por id y actualizarlo en vez de crear nuevos
                stock.setCodigoStock(stockReq.getCodigoStock());
                stock.setCantidadUnidades(stockReq.getCantidadUnidades());
                stock.setFechaVencimiento(stockReq.getFechaVencimiento());
                stock.setPrecioCompra(stockReq.getPrecioCompra());
                stock.setProducto(producto);
                producto.getStocks().add(stock);
            }
        }

        // Guardar cambios
        Producto guardado = productoRepository.save(producto);

        // Devolver el producto con stocks precargados (añadimos método en repo)
        return productoRepository.findByIdWithStocks(guardado.getId()).orElse(guardado);
    }



    // 11. Buscar productos con stock menor a cierto umbral con stocks
    public List<Producto> buscarProductosConStockMenorA(int umbral) {
        try {
            List<Producto> productos = productoRepository.findByActivoTrueWithStocks();
            List<Producto> resultado = new ArrayList<>();
            for (Producto p : productos) {
                if (p.getCantidadGeneral() < umbral) {
                    resultado.add(p);
                }
            }
            return resultado;
        } catch (Exception e) {
            List<Producto> productos = productoRepository.findByActivoTrue();
            List<Producto> resultado = new ArrayList<>();
            for (Producto p : productos) {
                if (p.getCantidadGeneral() < umbral) {
                    resultado.add(p);
                }
            }
            return resultado;
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
        List<Object[]> resultados = productoRepository.findProductosMasVendidos((Pageable) PageRequest.of(0, top));
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
                .filter(p -> p.getCantidadGeneral() < umbralCritico)
                .map(p -> {
                    DashboardResumenDTO.ProductoCriticoDTO dto = new DashboardResumenDTO.ProductoCriticoDTO();
                    dto.nombre = p.getNombre();
                    dto.stock = p.getCantidadGeneral();
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

    // Métodos de paginación compatibles (legacy)
    public List<Producto> listarTodosPaginado(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Producto> paged = productoRepository.findByActivoTrue(pageable);
        List<Producto> productos = paged.getContent();
        productos.forEach(p -> p.getStocks().size());
        return productos;
    }

    public Page<Producto> listarTodosPaginadoPage(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return productoRepository.findByActivoTrue(pageable);
    }

    // Nuevo: búsqueda paginada con filtros (q, lab, cat) y carga de stocks
    public Page<Producto> buscarPaginadoPorQuery(String q, String lab, String cat, Pageable pageable) {
        Page<Producto> paged;
        boolean anyFilter = (q != null && !q.trim().isEmpty()) || (lab != null && !lab.isBlank()) || (cat != null && !cat.isBlank());
        if (!anyFilter) {
            paged = productoRepository.findByActivoTrue(pageable);
        } else {
            paged = productoRepository.search(q == null ? null : q.trim(), lab == null ? null : lab.trim(), cat == null ? null : cat.trim(), pageable);
        }

        // Forzar carga de stocks para los productos de la página
        paged.getContent().forEach(p -> {
            if (p.getStocks() != null) p.getStocks().size();
        });

        return paged;
    }
}