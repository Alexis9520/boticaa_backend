package quantify.BoticaSaid.dto.common;

import java.util.List;

public class PageResponse<T> {
    public List<T> content;
    public long total;           // compatibilidad
    public long totalElements;   // compat front
    public int page;
    public int size;
    public int totalPages;

    public PageResponse() {}

    public PageResponse(List<T> content, long total, int page, int size, int totalPages) {
        this.content = content;
        this.total = total;
        this.totalElements = total;
        this.page = page;
        this.size = size;
        this.totalPages = totalPages;
    }

    public static <T> PageResponse<T> of(List<T> content, long total, int page, int size, int totalPages) {
        return new PageResponse<>(content, total, page, size, totalPages);
    }
}