package quantify.BoticaSaid.dto.pedido;

import com.fasterxml.jackson.annotation.JsonFormat;
import quantify.BoticaSaid.dto.stock.AgregarLoteRequest;

import java.time.LocalDate;

/**
 * DTO para agregar stock y crear un pedido simultáneamente
 */
public class AgregarStockConPedidoRequest {
    
    private AgregarLoteRequest stockData;
    
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate fechaDePedido;

    // Constructor vacío
    public AgregarStockConPedidoRequest() {}

    // Getters y Setters
    public AgregarLoteRequest getStockData() {
        return stockData;
    }

    public void setStockData(AgregarLoteRequest stockData) {
        this.stockData = stockData;
    }

    public LocalDate getFechaDePedido() {
        return fechaDePedido;
    }

    public void setFechaDePedido(LocalDate fechaDePedido) {
        this.fechaDePedido = fechaDePedido;
    }
}
