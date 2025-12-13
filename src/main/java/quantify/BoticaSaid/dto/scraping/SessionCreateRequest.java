package quantify.BoticaSaid.dto.scraping;

public class SessionCreateRequest {
    private Integer ttl_seconds;
    private Boolean headless;
    private Boolean prefer_reuse;

    public Integer getTtl_seconds() {
        return ttl_seconds;
    }

    public void setTtl_seconds(Integer ttl_seconds) {
        this.ttl_seconds = ttl_seconds;
    }

    public Boolean getHeadless() {
        return headless;
    }

    public void setHeadless(Boolean headless) {
        this.headless = headless;
    }

    public Boolean getPrefer_reuse() {
        return prefer_reuse;
    }

    public void setPrefer_reuse(Boolean prefer_reuse) {
        this.prefer_reuse = prefer_reuse;
    }
}
