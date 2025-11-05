package quantify.BoticaSaid.dto.reports;

import java.math.BigDecimal;

public class CajaSummaryDto {
    public BigDecimal ingresos; // total ventas en periodo
    public BigDecimal egresos;  // total egresos de caja en periodo
    public BigDecimal neto;     // ingresos - egresos
}