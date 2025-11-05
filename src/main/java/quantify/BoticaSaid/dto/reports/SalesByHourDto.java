package quantify.BoticaSaid.dto.reports;

import java.math.BigDecimal;

public class SalesByHourDto {
    public int hora;            // 0..23
    public long tickets;        // cantidad de tickets
    public BigDecimal ventas;   // total vendido en esa hora
}