package quantify.BoticaSaid.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import quantify.BoticaSaid.model.Pedido;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface PedidoRepository extends JpaRepository<Pedido, Long> {
    
    @Query("SELECT p FROM Pedido p " +
           "LEFT JOIN FETCH p.producto " +
           "LEFT JOIN FETCH p.stock " +
           "LEFT JOIN FETCH p.proveedor " +
           "WHERE (:proveedorId IS NULL OR p.proveedor.id = :proveedorId) " +
           "AND (:fechaPedido IS NULL OR p.fechaDePedido = :fechaPedido)")
    List<Pedido> findByFilters(@Param("proveedorId") Long proveedorId, 
                                @Param("fechaPedido") LocalDate fechaPedido);
}
