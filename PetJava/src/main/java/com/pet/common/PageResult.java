package com.pet.common;

import lombok.Data;
import java.util.List;

/**
 * 分页结果类
 * 用于封装分页查询的结果，包含数据列表和分页信息
 * 
 * @param <T> 泛型，列表中数据的类型
 */
@Data
public class PageResult<T> {
    
    /**
     * 当前页的数据列表
     */
    private List<T> list;
    
    /**
     * 分页信息对象
     */
    private Pagination pagination;

    /**
     * 分页信息内部类
     * 封装分页所需的各项参数
     */
    @Data
    public static class Pagination {
        
        /**
         * 当前页码，从1开始
         */
        private int page;
        
        /**
         * 每页显示的记录数
         */
        private int pageSize;
        
        /**
         * 总记录数
         */
        private long total;
        
        /**
         * 总页数，自动计算
         */
        private int totalPages;
    }

    /**
     * 静态工厂方法，创建分页结果对象
     * @param list 当前页数据列表
     * @param page 当前页码
     * @param pageSize 每页大小
     * @param total 总记录数
     * @param <T> 数据类型
     * @return 封装好的分页结果对象
     */
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
