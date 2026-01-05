package com.pet.common;

import lombok.Data;
import java.util.List;

@Data
public class PageResult<T> {
    private List<T> list;
    private Pagination pagination;

    @Data
    public static class Pagination {
        private int page;
        private int pageSize;
        private long total;
        private int totalPages;
    }

    public static <T> PageResult<T> of(List<T> list, int page, int pageSize, long total) {
        PageResult<T> result = new PageResult<>();
        result.setList(list);
        Pagination pagination = new Pagination();
        pagination.setPage(page);
        pagination.setPageSize(pageSize);
        pagination.setTotal(total);
        pagination.setTotalPages((int) Math.ceil((double) total / pageSize));
        result.setPagination(pagination);
        return result;
    }
}
