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

@RestController
@RequestMapping("/institutions")
@RequiredArgsConstructor
public class InstitutionController {
    private final InstitutionService institutionService;
    private final FavoriteService favoriteService;
    private final ReviewService reviewService;

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

    @GetMapping("/{id}")
    public Result<Map<String, Object>> getDetail(@PathVariable String id) {
        return Result.success(institutionService.getDetail(id));
    }

    @GetMapping("/{id}/packages")
    public Result<List<Map<String, Object>>> getPackages(@PathVariable String id) {
        return Result.success(institutionService.getPackages(id));
    }

    @GetMapping("/{id}/reviews")
    public Result<PageResult<Map<String, Object>>> getReviews(
            @PathVariable String id,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(required = false) String sortBy) {
        return Result.success(reviewService.getByInstitution(id, sortBy, page, pageSize));
    }

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

    @PostMapping("/{id}/favorite")
    public Result<Void> addFavorite(@PathVariable String id, @RequestHeader("X-User-Id") String userId) {
        favoriteService.addFavorite(userId, id);
        return Result.success();
    }

    @DeleteMapping("/{id}/favorite")
    public Result<Void> removeFavorite(@PathVariable String id, @RequestHeader("X-User-Id") String userId) {
        favoriteService.removeFavorite(userId, id);
        return Result.success();
    }
}
