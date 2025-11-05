package quantify.BoticaSaid.dto.reports;

import java.util.List;

public class PageResponse<T> {
    public List<T> content;
    public long totalElements;
    public int page;
    public int size;
    public int totalPages;

    public PageResponse(List<T> content, long totalElements, int page, int size) {
        this.content = content;
        this.totalElements = totalElements;
        this.page = page;
        this.size = size;
        this.totalPages = (int) Math.ceil((double) totalElements / (double) size);
    }
}