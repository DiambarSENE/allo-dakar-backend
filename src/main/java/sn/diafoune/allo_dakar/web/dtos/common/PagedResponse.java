package sn.diafoune.allo_dakar.web.dtos.common;

import org.springframework.data.domain.Page;

import java.util.List;

public record PagedResponse<T>(boolean success, String message, List<T> data, PaginationMeta pagination) {

    public static <T> PagedResponse<T> of(String message, Page<T> page) {
        return new PagedResponse<>(true, message, page.getContent(), PaginationMeta.from(page));
    }
}
