package quantify.BoticaSaid.dto.reports;

import java.math.BigDecimal;

public class CustomerDtos {
    public static class TopCustomerDto {
        public String dni;
        public String nombre;
        public long tickets;      // frecuencia (visitas)
        public long unidades;     // unidades compradas
        public BigDecimal ventas; // total comprado
        public String ultima_compra; // ISO date-time
    }
}