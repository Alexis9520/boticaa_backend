package quantify.BoticaSaid.dto.scraping;

public class GenericErrorResponse {
    private boolean ok = false;
    private String message;

    public GenericErrorResponse() {}

    public GenericErrorResponse(String message) {
        this.message = message;
    }

    public boolean isOk() {
        return ok;
    }

    public void setOk(boolean ok) {
        this.ok = ok;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
