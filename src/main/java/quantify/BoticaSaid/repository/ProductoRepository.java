package quantify.BoticaSaid.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import quantify.BoticaSaid.model.Producto;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductoRepository extends JpaRepository<Producto, Long> {

    // Consulta que incluye stocks (NO USAR PARA PAGINACIÓN)
    @Query("SELECT DISTINCT p FROM Producto p LEFT JOIN FETCH p.stocks WHERE p.activo = true")
    List<Producto> findByActivoTrueWithStocks();

    // Buscar por código con stocks
    @Query("SELECT p FROM Producto p LEFT JOIN FETCH p.stocks WHERE p.codigoBarras = :codigoBarras")
    Optional<Producto> findByCodigoBarrasWithStocks(@Param("codigoBarras") String codigoBarras);

    // PAGINACIÓN simple: Solo productos activos
    @Query("SELECT p FROM Producto p WHERE p.activo = true")
    Page<Producto> findByActivoTrue(Pageable pageable);

    // BÚSQUEDA paginada con filtros opcionales q (texto), lab (laboratorio) y cat (categoría).
    // No usamos JOIN FETCH aquí para mantener la semántica correcta de Page.
    @Query("""
        SELECT p FROM Producto p
        WHERE p.activo = true
          AND (
              :q IS NULL
              OR :q = ''
              OR LOWER(p.nombre) LIKE LOWER(CONCAT('%', :q, '%'))
              OR LOWER(p.codigoBarras) LIKE LOWER(CONCAT('%', :q, '%'))
              OR LOWER(p.categoria) LIKE LOWER(CONCAT('%', :q, '%'))
              OR LOWER(p.laboratorio) LIKE LOWER(CONCAT('%', :q, '%'))
          )
          AND ( :lab IS NULL OR :lab = '' OR LOWER(p.laboratorio) = LOWER(:lab) )
          AND ( :cat IS NULL OR :cat = '' OR LOWER(p.categoria) = LOWER(:cat) )
    """)
    Page<Producto> search(@Param("q") String q, @Param("lab") String lab, @Param("cat") String cat, Pageable pageable);

    // Fallbacks / helpers
    Producto findByCodigoBarras(String codigoBarras);
    List<Producto> findByActivoTrue();
    List<Producto> findByNombreContainingIgnoreCaseAndActivoTrue(String nombre);
    List<Producto> findByCategoriaContainingIgnoreCaseAndActivoTrue(String categoria);
    List<Producto> findByNombreContainingIgnoreCaseAndCategoriaContainingIgnoreCaseAndActivoTrue(String nombre, String categoria);

    @Query(
            value = """
            SELECT p.nombre, SUM(d.cantidad) AS totalVendidas,
              (SUM(d.cantidad) * 100.0 / (SELECT SUM(d2.cantidad) FROM detalles_boleta d2)) AS porcentaje
            FROM detalles_boleta d
            JOIN productos p ON d.producto_id = p.id
            GROUP BY p.id, p.nombre
            ORDER BY totalVendidas DESC
            """,
            nativeQuery = true
    )
    List<Object[]> findProductosMasVendidos(Pageable pageable);

    @Query("SELECT p FROM Producto p LEFT JOIN FETCH p.stocks WHERE p.id = :id")
    Optional<Producto> findByIdWithStocks(@Param("id") Long id);

    // Find products by supplier
    @Query("SELECT p FROM Producto p WHERE p.proveedor.id = :proveedorId AND p.activo = true")
    List<Producto> findByProveedorIdAndActivoTrue(@Param("proveedorId") Long proveedorId);

    // Find products created within a date range
    @Query("SELECT p FROM Producto p WHERE p.fechaCreacion BETWEEN :fechaInicio AND :fechaFin AND p.activo = true")
    List<Producto> findByFechaCreacionBetween(@Param("fechaInicio") java.util.Date fechaInicio, @Param("fechaFin") java.util.Date fechaFin);

    // Find products by category with stocks
    @Query("SELECT DISTINCT p FROM Producto p LEFT JOIN FETCH p.stocks WHERE LOWER(p.categoria) = LOWER(:categoria) AND p.activo = true")
    List<Producto> findByCategoriaWithStocks(@Param("categoria") String categoria);

}