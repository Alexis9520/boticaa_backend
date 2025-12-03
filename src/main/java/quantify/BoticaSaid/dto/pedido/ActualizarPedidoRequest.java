// language: java
package quantify.BoticaSaid.dto.pedido;

import java.time.LocalDate;

public class ActualizarPedidoRequest {
    private Long proveedorId;
    private LocalDate fechaDePedido;
    private ActualizarStockRequest stock;

    public Long getProveedorId() { return proveedorId; }
    public void setProveedorId(Long proveedorId) { this.proveedorId = proveedorId; }

    public LocalDate getFechaDePedido() { return fechaDePedido; }
    public void setFechaDePedido(LocalDate fechaDePedido) { this.fechaDePedido = fechaDePedido; }

    public ActualizarStockRequest getStock() { return stock; }
    public void setStock(ActualizarStockRequest stock) { this.stock = stock; }
}