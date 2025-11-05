package quantify.BoticaSaid.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.jpa.repository.EntityGraph.EntityGraphType;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import quantify.BoticaSaid.model.Boleta;
import quantify.BoticaSaid.model.Usuario;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface BoletaRepository extends JpaRepository<Boleta, Integer>, JpaSpecificationExecutor<Boleta> {

    List<Boleta> findByUsuarioAndFechaVentaBetween(Usuario usuario, LocalDateTime desde, LocalDateTime hasta);

    @Query("SELECT SUM(b.totalCompra) FROM Boleta b WHERE b.fechaVenta >= :fecha")
    Optional<Double> sumTotalCompraByFechaVentaAfter(@Param("fecha") LocalDateTime fecha);

    @Query("SELECT SUM(b.totalCompra) FROM Boleta b WHERE b.fechaVenta BETWEEN :desde AND :hasta")
    Optional<Double> sumTotalCompraByFechaVentaBetween(@Param("desde") LocalDateTime desde, @Param("hasta") LocalDateTime hasta);

    @Query("SELECT COUNT(DISTINCT b.dniCliente) FROM Boleta b WHERE b.fechaVenta >= :fecha")
    Optional<Integer> countDistinctDniClienteByFechaVentaAfter(@Param("fecha") LocalDateTime fecha);

    @Query("SELECT COUNT(DISTINCT b.dniCliente) FROM Boleta b WHERE b.fechaVenta BETWEEN :desde AND :hasta")
    Optional<Integer> countDistinctDniClienteByFechaVentaBetween(@Param("desde") LocalDateTime desde, @Param("hasta") LocalDateTime hasta);

    @Query("SELECT COUNT(DISTINCT b.nombreCliente) FROM Boleta b WHERE b.fechaVenta >= :inicioDia AND b.nombreCliente IS NOT NULL AND b.nombreCliente <> ''")
    Optional<Integer> countDistinctNombreClienteByFechaVentaAfter(@Param("inicioDia") LocalDateTime inicioDia);

    @Query("SELECT b FROM Boleta b ORDER BY b.fechaVenta DESC")
    List<Boleta> findTopNByOrderByFechaVentaDesc(Pageable pageable);

    @Query(
            value = """
        SELECT DATE_FORMAT(fecha_venta, '%H:00') as hora, SUM(total_compra)
        FROM boletas
        WHERE fecha_venta >= :hace24Horas
        GROUP BY hora
        ORDER BY hora
        """, nativeQuery = true
    )
    List<Object[]> obtenerVentasPorHora(@Param("hace24Horas") LocalDateTime hace24Horas);

    // Listado paginado: precarga to-one (usuario, metodoPago) y evita N+1
    @EntityGraph(type = EntityGraphType.FETCH, attributePaths = { "usuario", "metodoPago" })
    Page<Boleta> findAll(Specification<Boleta> spec, Pageable pageable);

    default Page<Boleta> findAllWithUsuarioAndMetodo(Specification<Boleta> spec, Pageable pageable) {
        return findAll(spec, pageable);
    }

    // NUEVO: traer una boleta con sus detalles y producto (para el expand del front)
    @Query("""
        SELECT b
        FROM Boleta b
        LEFT JOIN FETCH b.usuario
        LEFT JOIN FETCH b.metodoPago
        LEFT JOIN FETCH b.detalles d
        LEFT JOIN FETCH d.producto
        WHERE b.id = :id
        """)
    Optional<Boleta> findByIdWithDetalles(@Param("id") Integer id);
}