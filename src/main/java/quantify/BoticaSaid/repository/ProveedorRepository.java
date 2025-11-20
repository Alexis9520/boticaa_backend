package quantify.BoticaSaid.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import quantify.BoticaSaid.model.Proveedor;

import java.util.List;
import java.util.Optional;

public interface ProveedorRepository extends JpaRepository<Proveedor, Long> {

    // Buscar proveedor por RUC
    Optional<Proveedor> findByRuc(String ruc);

    // Buscar proveedores activos
    List<Proveedor> findByActivoTrue();

    // Buscar proveedores por nombre (búsqueda parcial)
    @Query("SELECT p FROM Proveedor p WHERE p.activo = true AND LOWER(p.nombre) LIKE LOWER(CONCAT('%', :nombre, '%'))")
    List<Proveedor> findByNombreContaining(@Param("nombre") String nombre);

    // Buscar proveedores con paginación y filtros
    @Query("SELECT p FROM Proveedor p WHERE " +
           "(:q IS NULL OR LOWER(p.nombre) LIKE LOWER(CONCAT('%', :q, '%')) OR LOWER(p.ruc) LIKE LOWER(CONCAT('%', :q, '%'))) " +
           "AND (:activo IS NULL OR p.activo = :activo)")
    Page<Proveedor> findByFiltros(@Param("q") String q, @Param("activo") Boolean activo, Pageable pageable);
}
