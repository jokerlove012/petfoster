package com.pet.common;

import org.junit.jupiter.api.Test;
import java.util.ArrayList;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

/**
 * PageResult 分页结果测试
 */
public class PageResultTest {

    @Test
    public void testPageResultCreation() {
        List<String> list = new ArrayList<>();
        list.add("item1");
        list.add("item2");

        PageResult<String> pageResult = PageResult.of(list, 1, 10, 25);

        assertEquals(2, pageResult.getList().size());
        assertEquals(1, pageResult.getPagination().getPage());
        assertEquals(10, pageResult.getPagination().getPageSize());
        assertEquals(25, pageResult.getPagination().getTotal());
        assertEquals(3, pageResult.getPagination().getTotalPages());
    }

    @Test
    public void testPageResultEmptyList() {
        List<String> list = new ArrayList<>();

        PageResult<String> pageResult = PageResult.of(list, 1, 10, 0);

        assertEquals(0, pageResult.getList().size());
        assertEquals(1, pageResult.getPagination().getPage());
        assertEquals(10, pageResult.getPagination().getPageSize());
        assertEquals(0, pageResult.getPagination().getTotal());
        assertEquals(0, pageResult.getPagination().getTotalPages());
    }

    @Test
    public void testPageResultCalculation() {
        List<String> list = new ArrayList<>();
        for (int i = 0; i < 50; i++) {
            list.add("item" + i);
        }

        PageResult<String> pageResult = PageResult.of(list, 1, 20, 100);

        assertEquals(50, pageResult.getList().size());
        assertEquals(1, pageResult.getPagination().getPage());
        assertEquals(20, pageResult.getPagination().getPageSize());
        assertEquals(100, pageResult.getPagination().getTotal());
        assertEquals(5, pageResult.getPagination().getTotalPages());
    }

    @Test
    public void testPageResultLastPage() {
        List<String> list = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            list.add("item" + i);
        }

        PageResult<String> pageResult = PageResult.of(list, 5, 10, 50);

        assertEquals(5, pageResult.getPagination().getPage());
        assertEquals(10, pageResult.getPagination().getPageSize());
        assertEquals(50, pageResult.getPagination().getTotal());
        assertEquals(5, pageResult.getPagination().getTotalPages());
    }

    @Test
    public void testPageResultWithZeroPageSize() {
        List<String> list = new ArrayList<>();

        PageResult<String> pageResult = PageResult.of(list, 1, 0, 10);

        assertEquals(1, pageResult.getPagination().getPage());
        assertEquals(0, pageResult.getPagination().getPageSize());
        assertEquals(10, pageResult.getPagination().getTotal());
    }
}
