package quantify.BoticaSaid.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import quantify.BoticaSaid.model.Stock;
import quantify.BoticaSaid.model.Producto;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface StockRepository extends JpaRepository<Stock, Integer>, JpaSpecificationExecutor<Stock> {

    List<Stock> findByProductoOrderByFechaVencimientoAsc(Producto producto);

    // LEGACY: trae todo (evitar en listas grandes)
    @Query("SELECT s FROM Stock s JOIN FETCH s.producto")
    List<Stock> findAllWithProducto();

    // NUEVO: paginado con filtros y producto precargado (evita N+1)
    @Override
    @EntityGraph(attributePaths = { "producto" })
    Page<Stock> findAll(org.springframework.data.jpa.domain.Specification<Stock> spec, Pageable pageable);

    // Find stocks created within a date range with product info
    @Query("SELECT s FROM Stock s JOIN FETCH s.producto p WHERE s.fechaCreacion BETWEEN :fechaInicio AND :fechaFin ORDER BY s.fechaCreacion DESC")
    List<Stock> findByFechaCreacionBetweenWithProducto(@Param("fechaInicio") LocalDateTime fechaInicio, @Param("fechaFin") LocalDateTime fechaFin);

    @Query("SELECT s FROM Stock s JOIN FETCH s.producto p WHERE s.fechaCreacion BETWEEN :fechaInicio AND :fechaFin AND p.proveedor.id = :proveedorId ORDER BY s.fechaCreacion DESC")
    List<Stock> findByFechaAndProveedor(@Param("fechaInicio") LocalDateTime start,
                                        @Param("fechaFin") LocalDateTime end,
                                        @Param("proveedorId") Long proveedorId);
}