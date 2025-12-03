package quantify.BoticaSaid.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import quantify.BoticaSaid.model.Pedido;

import java.math.BigDecimal;
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

       boolean existsByStockId(int stockId);

       long countByFechaDePedido(LocalDate fecha);

       long countByFechaDePedidoBetween(LocalDate inicio, LocalDate fin);

       @Query("SELECT p FROM Pedido p JOIN FETCH p.stock s WHERE p.fechaDePedido BETWEEN :inicio AND :fin")
       List<Pedido> findWithStockBetween(@Param("inicio") LocalDate inicio, @Param("fin") LocalDate fin);

       @Query("SELECT p FROM Pedido p JOIN FETCH p.proveedor pr JOIN FETCH p.producto prod ORDER BY p.fechaDePedido DESC, p.id DESC")
       List<Pedido> findLatestPedidos(org.springframework.data.domain.Pageable pageable);

       @Query("SELECT p.fechaDePedido, COUNT(p) FROM Pedido p WHERE p.fechaDePedido BETWEEN :inicio AND :fin GROUP BY p.fechaDePedido ORDER BY p.fechaDePedido")
       List<Object[]> countByFechaGrouped(@Param("inicio") LocalDate inicio, @Param("fin") LocalDate fin);

       @Query("SELECT DISTINCT p.proveedor.id FROM Pedido p WHERE p.fechaDePedido BETWEEN :inicio AND :fin")
       List<Long> findProveedorIdsBetween(@Param("inicio") LocalDate inicio, @Param("fin") LocalDate fin);

       @Query("SELECT pr.id, COALESCE(pr.razonComercial, pr.ruc), COUNT(p) FROM Pedido p JOIN p.proveedor pr WHERE p.fechaDePedido BETWEEN :inicio AND :fin GROUP BY pr.id, pr.razonComercial, pr.ruc ORDER BY COUNT(p) DESC")
       List<Object[]> topProveedoresPorPedidos(@Param("inicio") LocalDate inicio, @Param("fin") LocalDate fin, org.springframework.data.domain.Pageable pageable);

       @Query("""
              SELECT prov.id AS proveedorId,
                     COALESCE(prov.razonComercial, prov.ruc) AS nombre,
                     prov.ruc AS ruc,
                     COUNT(p) AS totalPedidos,
                     MAX(p.fechaDePedido) AS fechaUltimoPedido,
                     AVG(s.precioCompra) AS precioPromedio
              FROM Pedido p
              JOIN p.stock s
              JOIN p.proveedor prov
              WHERE p.producto.id = :productoId
              GROUP BY prov.id, prov.razonComercial, prov.ruc
              ORDER BY fechaUltimoPedido DESC
              """)
       List<Object[]> resumenPreciosPorProducto(@Param("productoId") Long productoId);

       @Query(value = """
              SELECT s.precio_compra
              FROM pedidos p
              JOIN stock s ON p.id_stock = s.id
              WHERE p.id_producto = :productoId AND p.id_proveedor = :proveedorId
              ORDER BY p.fecha_de_pedido DESC
              LIMIT 1
              """, nativeQuery = true)
       BigDecimal findUltimoPrecio(@Param("productoId") Long productoId,
                                   @Param("proveedorId") Long proveedorId);
}
