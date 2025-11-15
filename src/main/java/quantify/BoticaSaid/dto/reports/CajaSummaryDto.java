package quantify.BoticaSaid.dto.reports;

import java.math.BigDecimal;

public class CajaSummaryDto {
    public BigDecimal ingresosVentas;    // sum de boletas.total_compra
    public BigDecimal ingresosManuales;  // sum de movimientos_efectivo.tipo = 'INGRESO'
    public BigDecimal ventasEfectivo;    // sum de boletas.total_compra con metodo pago = 'Efectivo'
    public BigDecimal egresos;           // sum de movimientos_efectivo.tipo = 'EGRESO'
    public BigDecimal neto;              // ingresosVentas + ingresosManuales - egresos
    public BigDecimal efectivoInicial;   // opcional, si aplicable
}