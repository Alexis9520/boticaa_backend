package quantify.BoticaSaid.dto.proveedor;

import jakarta.validation.constraints.NotBlank;

public class ProveedorRequest {

    @NotBlank(message = "El RUC del proveedor es obligatorio")
    private String ruc;

    private String razonComercial;

    private String numero1;

    private String numero2;

    private String correo;

    private String direccion;

    // Constructor vacío
    public ProveedorRequest() {}

    // Getters y Setters

    public String getRuc() {
        return ruc;
    }

    public void setRuc(String ruc) {
        this.ruc = ruc;
    }

    public String getRazonComercial() {
        return razonComercial;
    }

    public void setRazonComercial(String razonComercial) {
        this.razonComercial = razonComercial;
    }

    public String getNumero1() {
        return numero1;
    }

    public void setNumero1(String numero1) {
        this.numero1 = numero1;
    }

    public String getNumero2() {
        return numero2;
    }

    public void setNumero2(String numero2) {
        this.numero2 = numero2;
    }

    public String getCorreo() {
        return correo;
    }

    public void setCorreo(String correo) {
        this.correo = correo;
    }

    public String getDireccion() {
        return direccion;
    }

    public void setDireccion(String direccion) {
        this.direccion = direccion;
    }
}
