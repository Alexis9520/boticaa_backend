package quantify.BoticaSaid.dto.caja;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;

public class MovimientoEfectivoDTO {

    @NotBlank(message = "dniUsuario es obligatorio")
    private String dniUsuario;

    @NotBlank(message = "tipo es obligatorio")
    @Pattern(regexp = "(?i)INGRESO|EGRESO", message = "tipo debe ser INGRESO o EGRESO")
    private String tipo; // INGRESO / EGRESO (acepta minúsculas por el regex)

    @NotNull(message = "monto es obligatorio")
    @DecimalMin(value = "0.01", message = "monto debe ser > 0")
    private BigDecimal monto;

    @NotBlank(message = "descripcion es obligatoria")
    @Size(max = 255, message = "descripcion máximo 255 caracteres")
    private String descripcion;

    public MovimientoEfectivoDTO() {}

    public MovimientoEfectivoDTO(String dniUsuario, String tipo, BigDecimal monto, String descripcion) {
        this.dniUsuario = dniUsuario;
        this.tipo = tipo;
        this.monto = monto;
        this.descripcion = descripcion;
    }

    public String getDniUsuario() { return dniUsuario; }
    public void setDniUsuario(String dniUsuario) { this.dniUsuario = dniUsuario; }

    public String getTipo() { return tipo; }
    public void setTipo(String tipo) { this.tipo = tipo; }

    public BigDecimal getMonto() { return monto; }
    public void setMonto(BigDecimal monto) { this.monto = monto; }

    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }

    // Helper opcional para estandarizar
    public String getTipoNormalizado() {
        return tipo == null ? null : tipo.toUpperCase();
    }
}