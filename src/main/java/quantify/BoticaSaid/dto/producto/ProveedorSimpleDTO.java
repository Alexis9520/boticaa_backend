package quantify.BoticaSaid.dto.producto;

public class ProveedorSimpleDTO {
    private Long id;
    private String razonComercial;
    private String ruc;

    public ProveedorSimpleDTO() {}

    public ProveedorSimpleDTO(Long id, String razonComercial, String ruc) {
        this.id = id;
        this.razonComercial = razonComercial;
        this.ruc = ruc;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getRazonComercial() {
        return razonComercial;
    }

    public void setRazonComercial(String razonComercial) {
        this.razonComercial = razonComercial;
    }

    public String getRuc() {
        return ruc;
    }

    public void setRuc(String ruc) {
        this.ruc = ruc;
    }
}
