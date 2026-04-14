package com.pet.controller;

import com.pet.common.PageResult;
import com.pet.common.Result;
import com.pet.service.FavoriteService;
import com.pet.service.InstitutionService;
import com.pet.service.ReviewService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 寄养机构控制器
 * 处理机构搜索、详情查询、服务套餐、评价、收藏等功能
 */
@RestController
@RequestMapping("/institutions")
@RequiredArgsConstructor
public class InstitutionController {
    private final InstitutionService institutionService;
    private final FavoriteService favoriteService;
    private final ReviewService reviewService;

    /**
     * 搜索机构列表
     * 支持多条件筛选和排序
     * @param keyword 关键词搜索（可选）
     * @param petType 宠物类型筛选（可选）
     * @param minRating 最低评分筛选（可选）
     * @param maxPrice 最高价格筛选（可选）
     * @param sortBy 排序方式（可选）
     * @param lat 用户纬度，用于距离排序（可选）
     * @param lng 用户经度，用于距离排序（可选）
     * @param page 页码，默认1
     * @param pageSize 每页数量，默认10
     * @return 分页的机构列表
     */
    @GetMapping
    public Result<PageResult<Map<String, Object>>> search(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String petType,
            @RequestParam(required = false) BigDecimal minRating,
            @RequestParam(required = false) BigDecimal maxPrice,
            @RequestParam(required = false) String sortBy,
            @RequestParam(required = false) BigDecimal lat,
            @RequestParam(required = false) BigDecimal lng,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int pageSize) {
        return Result.success(institutionService.search(keyword, petType, minRating, maxPrice, sortBy, lat, lng, page, pageSize));
    }

    /**
     * 获取机构详情
     * @param id 机构ID
     * @return 机构详细信息
     */
    @GetMapping("/{id}")
    public Result<Map<String, Object>> getDetail(@PathVariable String id) {
        return Result.success(institutionService.getDetail(id));
    }

    /**
     * 获取机构的服务套餐列表
     * @param id 机构ID
     * @return 服务套餐列表
     */
    @GetMapping("/{id}/packages")
    public Result<List<Map<String, Object>>> getPackages(@PathVariable String id) {
        return Result.success(institutionService.getPackages(id));
    }

    /**
     * 获取机构的评价列表
     * @param id 机构ID
     * @param page 页码，默认1
     * @param pageSize 每页数量，默认10
     * @param sortBy 排序方式（可选）
     * @return 分页的评价列表
     */
    @GetMapping("/{id}/reviews")
    public Result<PageResult<Map<String, Object>>> getReviews(
            @PathVariable String id,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(required = false) String sortBy) {
        return Result.success(reviewService.getByInstitution(id, sortBy, page, pageSize));
    }

    /**
     * 检查机构在指定日期是否有空位
     * @param id 机构ID
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @param petType 宠物类型
     * @return 可用性信息
     */
    @GetMapping("/{id}/availability")
    public Result<Map<String, Object>> checkAvailability(
            @PathVariable String id,
            @RequestParam String startDate,
            @RequestParam String endDate,
            @RequestParam String petType) {
        // 简化实现
        Map<String, Object> result = new HashMap<>();
        result.put("available", true);
        result.put("remainingSlots", 5);
        result.put("startDate", startDate);
        result.put("endDate", endDate);
        return Result.success(result);
    }

    /**
     * 收藏机构
     * @param id 机构ID
     * @param userId 当前用户ID
     * @return 成功响应
     */
    @PostMapping("/{id}/favorite")
    public Result<Void> addFavorite(@PathVariable String id, @RequestHeader("X-User-Id") String userId) {
        favoriteService.addFavorite(userId, id);
        return Result.success();
    }

    /**
     * 取消收藏机构
     * @param id 机构ID
     * @param userId 当前用户ID
     * @return 成功响应
     */
    @DeleteMapping("/{id}/favorite")
    public Result<Void> removeFavorite(@PathVariable String id, @RequestHeader("X-User-Id") String userId) {
        favoriteService.removeFavorite(userId, id);
        return Result.success();
    }
}
