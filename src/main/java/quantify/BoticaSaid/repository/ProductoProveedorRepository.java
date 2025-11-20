package quantify.BoticaSaid.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import quantify.BoticaSaid.model.ProductoProveedor;

import java.util.List;

@Repository
public interface ProductoProveedorRepository extends JpaRepository<ProductoProveedor, Long> {
    
    List<ProductoProveedor> findByProductoId(Long productoId);
    
    List<ProductoProveedor> findByProveedorId(Long proveedorId);
    
    void deleteByProductoId(Long productoId);
    
    void deleteByProveedorId(Long proveedorId);
}
