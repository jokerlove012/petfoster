package com.pet.controller;

import com.pet.common.PageResult;
import com.pet.common.Result;
import com.pet.dto.CreateBookingRequest;
import com.pet.service.BookingService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.Map;

/**
 * 预约/订单控制器
 * 处理宠物寄养预约的创建、查询、取消、支付、入住、退房等操作
 */
@RestController
@RequestMapping("/bookings")
@RequiredArgsConstructor
public class BookingController {
    private final BookingService bookingService;

    /**
     * 创建预约订单
     * @param userId 当前用户ID
     * @param request 创建预约的请求参数
     * @return 创建成功的订单信息
     */
    @PostMapping
    public Result<Map<String, Object>> create(
            @RequestHeader("X-User-Id") String userId,
            @Valid @RequestBody CreateBookingRequest request) {
        return Result.success(bookingService.create(userId, request));
    }

    /**
     * 获取订单列表
     * @param userId 当前用户ID
     * @param status 订单状态筛选（可选）
     * @param page 页码，默认1
     * @param pageSize 每页数量，默认10
     * @return 分页的订单列表
     */
    @GetMapping
    public Result<PageResult<Map<String, Object>>> getList(
            @RequestHeader("X-User-Id") String userId,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int pageSize) {
        return Result.success(bookingService.getList(userId, status, page, pageSize));
    }

    /**
     * 获取订单详情
     * @param id 订单ID
     * @return 订单详细信息
     */
    @GetMapping("/{id}")
    public Result<Map<String, Object>> getDetail(@PathVariable String id) {
        return Result.success(bookingService.getDetail(id));
    }

    /**
     * 取消订单
     * @param id 订单ID
     * @param body 包含取消原因的请求体（可选）
     * @return 取消后的订单信息
     */
    @PostMapping("/{id}/cancel")
    public Result<Map<String, Object>> cancel(@PathVariable String id, @RequestBody(required = false) Map<String, String> body) {
        String reason = body != null ? body.get("reason") : null;
        return Result.success(bookingService.cancel(id, reason));
    }

    /**
     * 支付订单
     * @param id 订单ID
     * @param body 包含支付方式的请求体
     * @return 支付后的订单信息
     */
    @PostMapping("/{id}/pay")
    public Result<Map<String, Object>> pay(@PathVariable String id, @RequestBody Map<String, String> body) {
        return Result.success(bookingService.pay(id, body.get("paymentMethod")));
    }

    /**
     * 机构确认订单
     * @param id 订单ID
     * @return 确认后的订单信息
     */
    @PostMapping("/{id}/confirm")
    public Result<Map<String, Object>> confirm(@PathVariable String id) {
        return Result.success(bookingService.confirm(id));
    }

    /**
     * 机构拒绝订单
     * @param id 订单ID
     * @param body 包含拒绝原因的请求体
     * @return 拒绝后的订单信息
     */
    @PostMapping("/{id}/reject")
    public Result<Map<String, Object>> reject(@PathVariable String id, @RequestBody Map<String, String> body) {
        return Result.success(bookingService.reject(id, body.get("reason")));
    }

    /**
     * 办理入住
     * @param id 订单ID
     * @return 入住后的订单信息
     */
    @PostMapping("/{id}/check-in")
    public Result<Map<String, Object>> checkIn(@PathVariable String id) {
        return Result.success(bookingService.checkIn(id));
    }

    /**
     * 办理退房
     * @param id 订单ID
     * @return 退房后的订单信息
     */
    @PostMapping("/{id}/check-out")
    public Result<Map<String, Object>> checkOut(@PathVariable String id) {
        return Result.success(bookingService.checkOut(id));
    }

    /**
     * 删除订单（逻辑删除）
     * @param userId 当前用户ID
     * @param id 订单ID
     * @return 成功响应
     */
    @DeleteMapping("/{id}")
    public Result<Void> delete(
            @RequestHeader("X-User-Id") String userId,
            @PathVariable String id) {
        bookingService.delete(id, userId);
        return Result.success(null);
    }
}
