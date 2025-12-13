package quantify.BoticaSaid.dto.scraping;

public class ScrapeRequest {
    private String nro;
    private String nroDeRegistroSanitario;
    private String session_id;

    public String getNro() {
        return nro;
    }

    public void setNro(String nro) {
        this.nro = nro;
    }

    public String getNroDeRegistroSanitario() {
        return nroDeRegistroSanitario;
    }

    public void setNroDeRegistroSanitario(String nroDeRegistroSanitario) {
        this.nroDeRegistroSanitario = nroDeRegistroSanitario;
    }

    public String getSession_id() {
        return session_id;
    }

    public void setSession_id(String session_id) {
        this.session_id = session_id;
    }
}
