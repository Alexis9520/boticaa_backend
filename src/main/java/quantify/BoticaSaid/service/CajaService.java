package quantify.BoticaSaid.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import quantify.BoticaSaid.dto.caja.CajaAperturaDTO;
import quantify.BoticaSaid.dto.caja.CajaResumenDTO;
import quantify.BoticaSaid.dto.caja.CierreCajaDTO;
import quantify.BoticaSaid.dto.caja.MovimientoDTO;
import quantify.BoticaSaid.dto.caja.MovimientoEfectivoDTO;
import quantify.BoticaSaid.dto.common.PageResponse;
import quantify.BoticaSaid.dto.dashboard.DashboardResumenDTO;
import quantify.BoticaSaid.model.*;
import quantify.BoticaSaid.repository.BoletaRepository;
import quantify.BoticaSaid.repository.CajaRepository;
import quantify.BoticaSaid.repository.MovimientoEfectivoRepository;
import quantify.BoticaSaid.repository.UsuarioRepository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Comparator;
import java.util.List;

@Service
public class CajaService {

    private final CajaRepository cajaRepository;
    private final UsuarioRepository usuarioRepository;
    private final MovimientoEfectivoRepository movimientoEfectivoRepository;
    private final BoletaRepository boletaRepository;

    private static final ZoneId ZONE = ZoneId.of("America/Lima");

    public CajaService(
            CajaRepository cajaRepository,
            UsuarioRepository usuarioRepository,
            MovimientoEfectivoRepository movimientoEfectivoRepository,
            BoletaRepository boletaRepository
    ) {
        this.cajaRepository = cajaRepository;
        this.usuarioRepository = usuarioRepository;
        this.movimientoEfectivoRepository = movimientoEfectivoRepository;
        this.boletaRepository = boletaRepository;
    }

    /* ===================== Apertura / Cierre ===================== */

    @Transactional
    public Caja abrirCaja(CajaAperturaDTO dto) {
        Usuario usuario = usuarioRepository.findByDni(dto.getDniUsuario())
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado con DNI: " + dto.getDniUsuario()));

        if (cajaRepository.existsByUsuarioAndFechaCierreIsNull(usuario)) {
            throw new RuntimeException("Ya existe una caja abierta para este usuario.");
        }

        Caja caja = new Caja();
        caja.setUsuario(usuario);
        caja.setFechaApertura(LocalDateTime.now(ZONE));
        caja.setEfectivoInicial(dto.getEfectivoInicial());
        caja.setEfectivoFinal(null);
        caja.setTotalYape(null);
        caja.setDiferencia(null);

        cajaRepository.save(caja);
        return caja;
    }

    @Transactional
    public Caja cerrarCaja(CierreCajaDTO cierreCajaDTO) {
        Usuario usuario = usuarioRepository.findByDni(cierreCajaDTO.getDniUsuario())
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado con DNI: " + cierreCajaDTO.getDniUsuario()));

        Caja caja = cajaRepository.findByUsuarioAndFechaCierreIsNull(usuario)
                .orElseThrow(() -> new RuntimeException("No hay caja abierta para el usuario."));

        LocalDateTime desde = caja.getFechaApertura();
        LocalDateTime hasta = LocalDateTime.now(ZONE);

        // --- CÁLCULO DE DIFERENCIA CORREGIDO ---

        // 1. El total esperado es el que ya calculaste durante el día.
        BigDecimal totalEfectivoEsperado = safe(caja.getEfectivoFinal());

        // 2. El total declarado es el que envía el usuario (contado a mano).
        //    Usamos la variable 'safe' en ambos lados para evitar NullPointerException.
        BigDecimal efectivoFinalDeclarado = safe(cierreCajaDTO.getEfectivoFinalDeclarado());

        // 3. La diferencia es Declarado - Esperado.
        BigDecimal diferencia = efectivoFinalDeclarado.subtract(totalEfectivoEsperado);

        // --- FIN DE LA CORRECCIÓN ---


        // --- Aún necesitas calcular el total digital (Yape) para el reporte ---
        List<Boleta> boletas = boletaRepository.findByUsuarioAndFechaVentaBetween(usuario, desde, hasta);

        BigDecimal ventasMixtoDigital = boletas.stream()
                .filter(b -> b.getMetodoPago() != null && b.getMetodoPago().getNombre() == MetodoPago.NombreMetodo.MIXTO)
                .map(b -> BigDecimal.valueOf(b.getMetodoPago().getDigital()))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal ventasYape = boletas.stream()
                .filter(b -> b.getMetodoPago() != null && b.getMetodoPago().getNombre() == MetodoPago.NombreMetodo.YAPE)
                .map(b -> BigDecimal.valueOf(b.getMetodoPago().getDigital())) // Asumiendo que Yape puro también usa 'digital'
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalVentasDigital = ventasYape.add(ventasMixtoDigital);
        // --- Fin del cálculo de Yape ---


        // Guardar los datos finales del cierre
        caja.setFechaCierre(hasta);
        caja.setEfectivoFinalDeclarado(efectivoFinalDeclarado);
        caja.setTotalYape(totalVentasDigital); // yape + parte digital mixto
        caja.setDiferencia(diferencia);
        // Nota: 'caja.setEfectivoFinal()' no se toca, se queda como el total esperado por el sistema.

        return cajaRepository.save(caja);
    }

    /* ===================== Consultas básicas ===================== */

    public Caja obtenerCajaAbiertaPorUsuario(String dniUsuario) {
        Usuario usuario = usuarioRepository.findByDni(dniUsuario)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado con DNI: " + dniUsuario));
        return cajaRepository.findByUsuarioAndFechaCierreIsNull(usuario)
                .orElse(null);
    }

    @Transactional(readOnly = true)
    public List<CajaResumenDTO> obtenerHistorialCajas() {
        List<Caja> cajas = cajaRepository.findAllByOrderByFechaAperturaDesc();
        return cajas.stream()
                .map(c -> convertirCajaAResumen(c, true)) // legacy: incluye movimientos manuales
                .toList();
    }

    @Transactional(readOnly = true)
    public List<CajaResumenDTO> obtenerHistorialCajasConMovimientos(boolean soloManuales) {
        List<Caja> cajas = cajaRepository.findAllByOrderByFechaAperturaDesc();
        return cajas.stream()
                .map(caja -> {
                    // base sin movimientos (para no duplicar trabajo)
                    CajaResumenDTO dto = convertirCajaAResumen(caja, false);
                    if (!soloManuales) {
                        var todos = movimientoEfectivoRepository.findByCaja(caja);
                        var movDtos = todos.stream()
                                .sorted(Comparator.comparing(MovimientoEfectivo::getFecha))
                                .map(this::mapMovimientoBasico)
                                .toList();
                        dto.setMovimientos(movDtos);
                    } else {
                        var manuales = movimientoEfectivoRepository.findByCajaAndEsManual(caja, true);
                        var movDtos = manuales.stream()
                                .sorted(Comparator.comparing(MovimientoEfectivo::getFecha))
                                .map(this::mapMovimientoBasico)
                                .toList();
                        dto.setMovimientos(movDtos);
                    }
                    if (dto.getId() == null && caja.getId() != null) dto.setId(caja.getId().intValue());
                    return dto;
                })
                .toList();
    }

    /* ===================== Historial PAGINADO ===================== */

    @Transactional(readOnly = true)
    public PageResponse<CajaResumenDTO> obtenerHistorialCajasPaginado(int page, int size) {
        Page<Caja> cajas = cajaRepository.findAllByOrderByFechaAperturaDesc(PageRequest.of(page, size));
        List<CajaResumenDTO> content = cajas.getContent().stream()
                .map(c -> convertirCajaAResumen(c, false)) // listado: sin movimientos => mucho más rápido
                .toList();
        long total = cajas.getTotalElements();
        int totalPages = cajas.getTotalPages();
        return PageResponse.of(content, total, page, size, totalPages);
    }

    @Transactional(readOnly = true)
    public PageResponse<CajaResumenDTO> obtenerHistorialCajasConMovimientosPaginado(boolean soloManuales, int page, int size) {
        Page<Caja> cajas = cajaRepository.findAllByOrderByFechaAperturaDesc(PageRequest.of(page, size));
        List<CajaResumenDTO> content = cajas.getContent().stream()
                .map(caja -> {
                    CajaResumenDTO dto = convertirCajaAResumen(caja, false);
                    if (!soloManuales) {
                        var todos = movimientoEfectivoRepository.findByCaja(caja);
                        var movDtos = todos.stream()
                                .sorted(Comparator.comparing(MovimientoEfectivo::getFecha))
                                .map(this::mapMovimientoBasico)
                                .toList();
                        dto.setMovimientos(movDtos);
                    } else {
                        var manuales = movimientoEfectivoRepository.findByCajaAndEsManual(caja, true);
                        var movDtos = manuales.stream()
                                .sorted(Comparator.comparing(MovimientoEfectivo::getFecha))
                                .map(this::mapMovimientoBasico)
                                .toList();
                        dto.setMovimientos(movDtos);
                    }
                    if (dto.getId() == null && caja.getId() != null) dto.setId(caja.getId().intValue());
                    return dto;
                })
                .toList();
        long total = cajas.getTotalElements();
        int totalPages = cajas.getTotalPages();
        return PageResponse.of(content, total, page, size, totalPages);
    }

    @Transactional(readOnly = true)
    public List<CajaResumenDTO> obtenerCajasAbiertas() {
        List<Caja> abiertas = cajaRepository.findByFechaCierreIsNull();
        return abiertas.stream()
                .map(this::convertirCajaAResumen)
                .toList();
    }

    @Transactional(readOnly = true)
    public CajaResumenDTO obtenerResumenCajaActual(String dniUsuario) {
        Usuario usuario = usuarioRepository.findByDni(dniUsuario)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado con DNI: " + dniUsuario));

        Caja caja = cajaRepository.findByUsuarioAndFechaCierreIsNull(usuario).orElse(null);
        if (caja == null) return null;
        return convertirCajaAResumen(caja);
    }

    /* ===================== Movimientos ===================== */

    @Transactional
    public void registrarMovimientoManual(MovimientoEfectivoDTO dto) {
        // --- VALIDACIONES AÑADIDAS ---
        if (dto.getMonto() == null) {
            throw new RuntimeException("El 'monto' no puede ser nulo.");
        }
        if (dto.getTipo() == null || dto.getTipo().isEmpty()) {
            throw new RuntimeException("El 'tipo' de movimiento no puede ser nulo o vacío.");
        }

        // --- DEBUG 1: DATOS DE ENTRADA ---
        System.out.println("\n--- [DEBUG] INICIO registrarMovimientoManual ---");
        System.out.println("[DEBUG] DNI Usuario: " + dto.getDniUsuario());
        System.out.println("[DEBUG] Monto DTO: " + dto.getMonto());
        System.out.println("[DEBUG] Tipo DTO: " + dto.getTipo());
        System.out.println("[DEBUG] Descripcion DTO: " + dto.getDescripcion());

        Usuario usuario = usuarioRepository.findByDni(dto.getDniUsuario())
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado con DNI: " + dto.getDniUsuario()));

        Caja caja = cajaRepository.findByUsuarioAndFechaCierreIsNull(usuario)
                .orElseThrow(() -> new RuntimeException("No hay caja abierta para el usuario."));

        // --- DEBUG 2: ESTADO INICIAL DE LA CAJA ---
        System.out.println("[DEBUG] Caja encontrada: ID " + caja.getId());
        BigDecimal efectivoActual = caja.getEfectivoFinal();
        if (efectivoActual == null) {
            efectivoActual = BigDecimal.ZERO;
            System.out.println("[DEBUG] Efectivo Final (Actual) era NULO. Se establece en 0.0");
        }
        System.out.println("[DEBUG] Efectivo Final (Actual): " + efectivoActual);

        BigDecimal montoMovimiento = dto.getMonto();
        MovimientoEfectivo.TipoMovimiento tipo = MovimientoEfectivo.TipoMovimiento.valueOf(dto.getTipo().toUpperCase());

        // 2. Sumar o restar según el tipo de movimiento
        BigDecimal nuevoEfectivoFinal; // Declaramos fuera para poder loguearlo
        if (tipo == MovimientoEfectivo.TipoMovimiento.INGRESO) {
            nuevoEfectivoFinal = efectivoActual.add(montoMovimiento);
            System.out.println("[DEBUG] Calculando (INGRESO): " + efectivoActual + " + " + montoMovimiento + " = " + nuevoEfectivoFinal);
        } else if (tipo == MovimientoEfectivo.TipoMovimiento.EGRESO) {
            nuevoEfectivoFinal = efectivoActual.subtract(montoMovimiento);
            System.out.println("[DEBUG] Calculando (EGRESO): " + efectivoActual + " - " + montoMovimiento + " = " + nuevoEfectivoFinal);
            if (nuevoEfectivoFinal.compareTo(BigDecimal.ZERO) < 0) {
                throw new RuntimeException("El monto de egreso supera el efectivo en caja.");
            }
        } else {
            // Por si acaso tienes más tipos
            nuevoEfectivoFinal = efectivoActual;
            System.out.println("[DEBUG] Tipo de movimiento no reconocido para cálculo. Sin cambios.");
        }

        caja.setEfectivoFinal(nuevoEfectivoFinal);
        System.out.println("[DEBUG] Nuevo Efectivo Final asignado a la entidad Caja.");

        try {
            // 3. Guardar la entidad Caja actualizada
            System.out.println("[DEBUG] Intentando guardar Caja (cajaRepository.save)...");
            cajaRepository.save(caja);
            System.out.println("[DEBUG] -> Caja guardada (en transacción).");
        } catch (Exception e) {
            System.out.println("[DEBUG] !!! ERROR AL GUARDAR LA CAJA: " + e.getMessage());
            throw e; // Re-lanzar para que la transacción haga rollback
        }

        // --- FIN DE LAS LÍNEAS AÑADIDAS ---

        // El resto de tu código para guardar el movimiento (está perfecto)
        MovimientoEfectivo mov = new MovimientoEfectivo();
        mov.setCaja(caja);
        mov.setTipo(tipo); // Reutilizamos la variable 'tipo'
        mov.setMonto(dto.getMonto());

        // Sospechoso: ¿Es 'descripcion' nula y la columna 'NOT NULL'?
        String descripcion = (dto.getDescripcion() == null || dto.getDescripcion().isEmpty())
                ? "Movimiento manual"
                : dto.getDescripcion();
        mov.setDescripcion(descripcion);
        mov.setFecha(LocalDateTime.now(ZoneId.of("America/Lima")));
        mov.setUsuario(usuario);
        mov.setEsManual(true);

        try {
            System.out.println("[DEBUG] Intentando guardar Movimiento (movimientoEfectivoRepository.save)...");
            movimientoEfectivoRepository.save(mov);
            System.out.println("[DEBUG] -> Movimiento guardado (en transacción).");
        } catch (Exception e) {
            // --- ESTE ES EL ERROR MÁS PROBABLE ---
            System.out.println("\n[DEBUG] !!! ERROR AL GUARDAR EL MOVIMIENTO: " + e.getMessage());
            System.out.println("[DEBUG] !!! ESTE ERROR PROVOCARÁ EL ROLLBACK DEL GUARDADO DE LA CAJA.");
            throw e; // Re-lanzar para que la transacción haga rollback
        }

        System.out.println("[DEBUG] --- FIN registrarMovimientoManual (Commit en progreso) ---\n");
    }
    @Transactional
    public MovimientoDTO registrarMovimientoManualYDevolver(MovimientoEfectivoDTO dto) {
        registrarMovimientoManual(dto);
        Usuario usuario = usuarioRepository.findByDni(dto.getDniUsuario()).orElseThrow();
        Caja caja = cajaRepository.findByUsuarioAndFechaCierreIsNull(usuario).orElse(null);
        if (caja == null) return null;
        return movimientoEfectivoRepository.findByCajaAndEsManual(caja, true).stream()
                .max(Comparator.comparing(MovimientoEfectivo::getFecha))
                .map(this::mapMovimientoBasico)
                .orElse(null);
    }

    /* ===================== Conversión / Cálculo ===================== */

    // Versión completa (por compatibilidad)
    public CajaResumenDTO convertirCajaAResumen(Caja caja) {
        return convertirCajaAResumen(caja, true);
    }

    // Nueva: permite evitar cargar movimientos en listados
    public CajaResumenDTO convertirCajaAResumen(Caja caja, boolean incluirMovsManuales) {
        List<MovimientoEfectivo> movimientosManual = incluirMovsManuales
                ? movimientoEfectivoRepository.findByCajaAndEsManual(caja, true)
                : List.of();

        BigDecimal ingresos = movimientosManual.stream()
                .filter(m -> m.getTipo() == MovimientoEfectivo.TipoMovimiento.INGRESO)
                .map(MovimientoEfectivo::getMonto)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal egresos = movimientosManual.stream()
                .filter(m -> m.getTipo() == MovimientoEfectivo.TipoMovimiento.EGRESO)
                .map(MovimientoEfectivo::getMonto)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        LocalDateTime desde = caja.getFechaApertura();
        LocalDateTime hasta = caja.getFechaCierre() != null ? caja.getFechaCierre() : LocalDateTime.now(ZONE);

        List<Boleta> boletas = boletaRepository.findByUsuarioAndFechaVentaBetween(
                caja.getUsuario(), desde, hasta);

        BigDecimal ventasEfectivo = boletas.stream()
                .filter(b -> b.getMetodoPago() != null && b.getMetodoPago().getNombre() == MetodoPago.NombreMetodo.EFECTIVO)
                .map(Boleta::getTotalCompra)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal ventasMixtoEfectivo = boletas.stream()
                .filter(b -> b.getMetodoPago() != null && b.getMetodoPago().getNombre() == MetodoPago.NombreMetodo.MIXTO)
                .map(b -> BigDecimal.valueOf(b.getMetodoPago().getEfectivo()))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal ventasMixtoDigital = boletas.stream()
                .filter(b -> b.getMetodoPago() != null && b.getMetodoPago().getNombre() == MetodoPago.NombreMetodo.MIXTO)
                .map(b -> BigDecimal.valueOf(b.getMetodoPago().getDigital()))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal ventasYape = boletas.stream()
                .filter(b -> b.getMetodoPago() != null && b.getMetodoPago().getNombre() == MetodoPago.NombreMetodo.YAPE)
                .map(b -> BigDecimal.valueOf(b.getMetodoPago().getDigital()))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalVentasEfectivo = ventasEfectivo.add(ventasMixtoEfectivo);
        BigDecimal totalVentasDigital = ventasYape.add(ventasMixtoDigital);
        BigDecimal totalVentas = totalVentasEfectivo.add(totalVentasDigital);

        BigDecimal efectivo = safe(caja.getEfectivoInicial())
                .add(ingresos)
                .add(totalVentasEfectivo)
                .subtract(egresos);

        BigDecimal totalYape = ventasYape.add(ventasMixtoDigital);

        List<MovimientoDTO> movimientosDTO = incluirMovsManuales
                ? movimientosManual.stream().map(this::mapMovimientoBasico).toList()
                : List.of();

        CajaResumenDTO dto = new CajaResumenDTO();
        dto.setId(caja.getId() == null ? null : caja.getId().intValue());
        dto.setEfectivoInicial(caja.getEfectivoInicial());
        dto.setEfectivoFinal(caja.getEfectivoFinal());
        dto.setEfectivoFinalDeclarado(caja.getEfectivoFinalDeclarado());
        dto.setIngresos(ingresos);
        dto.setEgresos(egresos);
        dto.setVentasEfectivo(totalVentasEfectivo);
        dto.setVentasYape(ventasYape);

        dto.setVentasPlin(BigDecimal.ZERO);
        dto.setVentasMixto(ventasMixtoDigital);
        dto.setTotalVentas(totalVentas);
        dto.setEfectivo(efectivo);
        dto.setTotalYape(totalYape != null ? totalYape : BigDecimal.ZERO);
        dto.setMovimientos(movimientosDTO);
        dto.setDiferencia(caja.getDiferencia());
        dto.setCajaAbierta(caja.getFechaCierre() == null);
        dto.setFechaApertura(caja.getFechaApertura() != null ? caja.getFechaApertura().toString() : null);
        dto.setFechaCierre(caja.getFechaCierre() != null ? caja.getFechaCierre().toString() : null);
        dto.setUsuarioResponsable(
                caja.getUsuario() != null ? caja.getUsuario().getNombreCompleto() : null
        );
        return dto;
    }

    private MovimientoDTO mapMovimientoBasico(MovimientoEfectivo m) {
        MovimientoDTO dto = new MovimientoDTO();
        dto.setId(m.getId().longValue());
        dto.setFecha(m.getFecha().toString());
        dto.setTipo(m.getTipo().toString().toLowerCase());
        dto.setDescripcion(m.getDescripcion());
        dto.setMonto(m.getMonto());
        dto.setUsuario(m.getUsuario().getNombreCompleto());
        return dto;
    }

    /* ===================== Dashboard ===================== */

    @Transactional(readOnly = true)
    public DashboardResumenDTO.SaldoCajaDTO getSaldoActual() {
        Caja caja = cajaRepository.findFirstByFechaCierreIsNullOrderByFechaAperturaDesc()
                .orElse(null);

        DashboardResumenDTO.SaldoCajaDTO dto = new DashboardResumenDTO.SaldoCajaDTO();
        if (caja == null) {
            dto.total = 0.0;
            dto.efectivo = 0.0;
            dto.yape = 0.0;
            return dto;
        }
        BigDecimal efectivo = caja.getEfectivoFinal() != null ? caja.getEfectivoFinal() : BigDecimal.ZERO;
        BigDecimal yape = caja.getTotalYape() != null ? caja.getTotalYape() : BigDecimal.ZERO;
        dto.efectivo = efectivo.doubleValue();
        dto.yape = yape.doubleValue();
        dto.total = efectivo.add(yape).doubleValue();
        return dto;
    }

    /* ===================== Util ===================== */
    private static BigDecimal safe(BigDecimal val) {
        return val == null ? BigDecimal.ZERO : val;
    }

    @Transactional(readOnly = true)
    public List<MovimientoDTO> obtenerMovimientosCaja(Integer cajaId, boolean soloManuales, String direction) {
        Caja caja = cajaRepository.findById(Long.valueOf(cajaId))
                .orElseThrow(() -> new RuntimeException("Caja no encontrada con id: " + cajaId));

        List<MovimientoEfectivo> movimientos;

        if (soloManuales) {
            movimientos = movimientoEfectivoRepository.findByCajaAndEsManualOrderByFechaAsc(caja, true);
        } else {
            movimientos = movimientoEfectivoRepository.findByCajaOrderByFechaAsc(caja);
        }

        if ("DESC".equalsIgnoreCase(direction)) {
            movimientos = movimientos.stream()
                    .sorted((a,b) -> b.getFecha().compareTo(a.getFecha()))
                    .toList();
        }

        return movimientos.stream()
                .map(this::mapMovimientoBasico)
                .toList();
    }
}