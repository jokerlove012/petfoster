package com.pet.controller;

import com.pet.common.PageResult;
import com.pet.common.Result;
import com.pet.service.FavoriteService;
import com.pet.service.ReviewService;
import com.pet.service.UserService;
import com.pet.service.ComplaintService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
public class UserController {
    private final FavoriteService favoriteService;
    private final ReviewService reviewService;
    private final UserService userService;
    private final ComplaintService complaintService;

    @GetMapping("/profile")
    public Result<Map<String, Object>> getProfile(@RequestHeader("X-User-Id") String userId) {
        return Result.success(userService.getProfile(userId));
    }

    @PutMapping("/profile")
    public Result<Map<String, Object>> updateProfile(
            @RequestHeader("X-User-Id") String userId,
            @RequestBody Map<String, Object> data) {
        return Result.success(userService.updateProfile(userId, data));
    }

    @GetMapping("/favorites")
    public Result<PageResult<Map<String, Object>>> getFavorites(
            @RequestHeader("X-User-Id") String userId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int pageSize) {
        return Result.success(favoriteService.getFavorites(userId, page, pageSize));
    }

    @GetMapping("/reviews")
    public Result<PageResult<Map<String, Object>>> getUserReviews(
            @RequestHeader("X-User-Id") String userId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int pageSize) {
        return Result.success(reviewService.getUserReviews(userId, page, pageSize));
    }

    // ========== 投诉/工单 ==========
    
    @PostMapping("/complaints")
    public Result<Map<String, Object>> submitComplaint(
            @RequestHeader("X-User-Id") String userId,
            @RequestBody Map<String, Object> data) {
        return Result.success(complaintService.submitComplaint(userId, data));
    }

    @GetMapping("/complaints")
    public Result<PageResult<Map<String, Object>>> getUserComplaints(
            @RequestHeader("X-User-Id") String userId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int pageSize) {
        return Result.success(complaintService.getUserComplaints(userId, page, pageSize));
    }
}
