package quantify.BoticaSaid.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import quantify.BoticaSaid.model.ProductoProveedor;

import java.util.List;
import java.util.Optional;

public interface ProductoProveedorRepository extends JpaRepository<ProductoProveedor, Long> {

    // Buscar todas las relaciones de un producto
    List<ProductoProveedor> findByProductoId(Long productoId);

    // Buscar todas las relaciones de un proveedor
    List<ProductoProveedor> findByProveedorId(Long proveedorId);

    // Buscar la relación específica entre producto y proveedor
    @Query("SELECT pp FROM ProductoProveedor pp WHERE pp.producto.id = :productoId AND pp.proveedor.id = :proveedorId")
    Optional<ProductoProveedor> findByProductoIdAndProveedorId(@Param("productoId") Long productoId, @Param("proveedorId") Long proveedorId);

    // Eliminar relaciones de un producto
    void deleteByProductoId(Long productoId);

    // Eliminar relaciones de un proveedor
    void deleteByProveedorId(Long proveedorId);
}
