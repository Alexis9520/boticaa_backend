package quantify.BoticaSaid.dto.common;

public class MetodoPagoDTO {

    private String nombre;
    private Double efectivo;
    private Double digital;
    private Double efectivoFix;

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public Double getEfectivo() {
        return efectivo;
    }

    public Double getEfectivoFix() {
        return efectivoFix;
    }

    public void setEfectivoFix(Double efectivoFix) {
        this.efectivoFix = efectivoFix;
    }

    public void setEfectivo(Double efectivo) {
        this.efectivo = efectivo;
    }

    public Double getDigital() {
        return digital;
    }

    public void setDigital(Double digital) {
        this.digital = digital;
    }
}
