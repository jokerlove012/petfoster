package com.pet.controller;

import com.pet.common.PageResult;
import com.pet.common.Result;
import com.pet.service.BookingService;
import com.pet.service.InstitutionService;
import com.pet.service.ReviewService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 机构管理端接口
 */
@RestController
@RequestMapping("/institution")
@RequiredArgsConstructor
public class InstitutionManageController {
    private final BookingService bookingService;
    private final InstitutionService institutionService;
    private final ReviewService reviewService;

    // ========== 仪表盘统计 ==========

    @GetMapping("/dashboard/stats")
    public Result<Map<String, Object>> getDashboardStats(@RequestHeader("X-User-Id") String userId) {
        return Result.success(institutionService.getDashboardStats(userId));
    }

    @GetMapping("/dashboard/recent-orders")
    public Result<List<Map<String, Object>>> getRecentOrders(
            @RequestHeader("X-User-Id") String userId,
            @RequestParam(defaultValue = "10") int limit) {
        return Result.success(institutionService.getRecentOrders(userId, limit));
    }

    @GetMapping("/dashboard/today-checkin")
    public Result<List<Map<String, Object>>> getTodayCheckIn(@RequestHeader("X-User-Id") String userId) {
        return Result.success(institutionService.getTodayCheckIn(userId));
    }

    @GetMapping("/dashboard/today-checkout")
    public Result<List<Map<String, Object>>> getTodayCheckOut(@RequestHeader("X-User-Id") String userId) {
        return Result.success(institutionService.getTodayCheckOut(userId));
    }

    // ========== 客户管理 ==========

    @GetMapping("/customers")
    public Result<PageResult<Map<String, Object>>> getCustomers(
            @RequestHeader("X-User-Id") String userId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int pageSize) {
        return Result.success(institutionService.getCustomers(userId, page, pageSize));
    }

    // ========== 订单管理 ==========

    @GetMapping("/bookings")
    public Result<PageResult<Map<String, Object>>> getBookings(
            @RequestHeader("X-User-Id") String userId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int pageSize) {
        return Result.success(bookingService.getInstitutionBookings(userId, page, pageSize));
    }

    @PostMapping("/bookings/{id}/confirm")
    public Result<Map<String, Object>> confirmBooking(
            @RequestHeader("X-User-Id") String userId,
            @PathVariable String id) {
        return Result.success(bookingService.confirmBooking(id, userId));
    }

    @PostMapping("/bookings/{id}/reject")
    public Result<Map<String, Object>> rejectBooking(
            @RequestHeader("X-User-Id") String userId,
            @PathVariable String id,
            @RequestBody Map<String, String> body) {
        return Result.success(bookingService.rejectBooking(id, userId, body.get("reason")));
    }

    @PostMapping("/bookings/{id}/check-in")
    public Result<Map<String, Object>> checkIn(
            @RequestHeader("X-User-Id") String userId,
            @PathVariable String id) {
        return Result.success(bookingService.checkIn(id, userId));
    }

    @PostMapping("/bookings/{id}/check-out")
    public Result<Map<String, Object>> checkOut(
            @RequestHeader("X-User-Id") String userId,
            @PathVariable String id) {
        return Result.success(bookingService.checkOut(id, userId));
    }

    @DeleteMapping("/bookings/{id}")
    public Result<Void> deleteBooking(
            @RequestHeader("X-User-Id") String userId,
            @PathVariable String id) {
        bookingService.deleteByInstitution(id, userId);
        return Result.success();
    }

    // ========== 服务套餐管理 ==========

    @GetMapping("/packages")
    public Result<List<Map<String, Object>>> getPackages(@RequestHeader("X-User-Id") String userId) {
        return Result.success(institutionService.getPackagesByStaff(userId));
    }

    @PostMapping("/packages")
    public Result<Map<String, Object>> createPackage(
            @RequestHeader("X-User-Id") String userId,
            @RequestBody Map<String, Object> data) {
        return Result.success(institutionService.createPackage(userId, data));
    }

    @PutMapping("/packages/{id}")
    public Result<Map<String, Object>> updatePackage(
            @RequestHeader("X-User-Id") String userId,
            @PathVariable String id,
            @RequestBody Map<String, Object> data) {
        return Result.success(institutionService.updatePackage(id, userId, data));
    }

    @PutMapping("/packages/{id}/status")
    public Result<Void> updatePackageStatus(
            @RequestHeader("X-User-Id") String userId,
            @PathVariable String id,
            @RequestBody Map<String, Boolean> body) {
        institutionService.updatePackageStatus(id, userId, body.get("isActive"));
        return Result.success();
    }

    @DeleteMapping("/packages/{id}")
    public Result<Void> deletePackage(
            @RequestHeader("X-User-Id") String userId,
            @PathVariable String id) {
        institutionService.deletePackage(id, userId);
        return Result.success();
    }

    // ========== 评价管理 ==========

    @GetMapping("/reviews")
    public Result<PageResult<Map<String, Object>>> getReviews(
            @RequestHeader("X-User-Id") String userId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int pageSize) {
        return Result.success(reviewService.getInstitutionReviews(userId, page, pageSize));
    }

    @PostMapping("/reviews/{id}/reply")
    public Result<Map<String, Object>> replyReview(
            @RequestHeader("X-User-Id") String userId,
            @PathVariable String id,
            @RequestBody Map<String, String> body) {
        return Result.success(reviewService.replyReview(id, userId, body.get("content")));
    }

    // ========== 机构设置 ==========

    @GetMapping("/settings")
    public Result<Map<String, Object>> getSettings(@RequestHeader("X-User-Id") String userId) {
        return Result.success(institutionService.getSettings(userId));
    }

    @PutMapping("/settings")
    public Result<Map<String, Object>> updateSettings(
            @RequestHeader("X-User-Id") String userId,
            @RequestBody Map<String, Object> data) {
        return Result.success(institutionService.updateSettings(userId, data));
    }

    // ========== 数据报表 ==========

    @GetMapping("/report")
    public Result<Map<String, Object>> getReportData(
            @RequestHeader("X-User-Id") String userId,
            @RequestParam(defaultValue = "month") String period) {
        return Result.success(institutionService.getReportData(userId, period));
    }

    // ========== 机构申请/创建 ==========

    @GetMapping("/profile")
    public Result<Map<String, Object>> getInstitutionProfile(@RequestHeader("X-User-Id") String userId) {
        return Result.success(institutionService.getInstitutionProfile(userId));
    }

    @PostMapping("/apply")
    public Result<Map<String, Object>> applyInstitution(
            @RequestHeader("X-User-Id") String userId,
            @RequestBody Map<String, Object> data) {
        return Result.success(institutionService.applyInstitution(userId, data));
    }
}
