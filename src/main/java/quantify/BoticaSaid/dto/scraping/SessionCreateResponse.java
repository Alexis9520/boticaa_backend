package quantify.BoticaSaid.dto.scraping;

import java.util.Map;

public class SessionCreateResponse {
    private boolean ok;
    private int status;
    private Map<String, Object> json;

    public boolean isOk() {
        return ok;
    }

    public void setOk(boolean ok) {
        this.ok = ok;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public Map<String, Object> getJson() {
        return json;
    }

    public void setJson(Map<String, Object> json) {
        this.json = json;
    }
}
