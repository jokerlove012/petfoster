package com.pet.controller;

import com.pet.common.PageResult;
import com.pet.common.Result;
import com.pet.dto.CreateBookingRequest;
import com.pet.service.BookingService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.Map;

@RestController
@RequestMapping("/bookings")
@RequiredArgsConstructor
public class BookingController {
    private final BookingService bookingService;

    @PostMapping
    public Result<Map<String, Object>> create(
            @RequestHeader("X-User-Id") String userId,
            @Valid @RequestBody CreateBookingRequest request) {
        return Result.success(bookingService.create(userId, request));
    }

    @GetMapping
    public Result<PageResult<Map<String, Object>>> getList(
            @RequestHeader("X-User-Id") String userId,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int pageSize) {
        return Result.success(bookingService.getList(userId, status, page, pageSize));
    }

    @GetMapping("/{id}")
    public Result<Map<String, Object>> getDetail(@PathVariable String id) {
        return Result.success(bookingService.getDetail(id));
    }

    @PostMapping("/{id}/cancel")
    public Result<Map<String, Object>> cancel(@PathVariable String id, @RequestBody(required = false) Map<String, String> body) {
        String reason = body != null ? body.get("reason") : null;
        return Result.success(bookingService.cancel(id, reason));
    }

    @PostMapping("/{id}/pay")
    public Result<Map<String, Object>> pay(@PathVariable String id, @RequestBody Map<String, String> body) {
        return Result.success(bookingService.pay(id, body.get("paymentMethod")));
    }

    @PostMapping("/{id}/confirm")
    public Result<Map<String, Object>> confirm(@PathVariable String id) {
        return Result.success(bookingService.confirm(id));
    }

    @PostMapping("/{id}/reject")
    public Result<Map<String, Object>> reject(@PathVariable String id, @RequestBody Map<String, String> body) {
        return Result.success(bookingService.reject(id, body.get("reason")));
    }

    @PostMapping("/{id}/check-in")
    public Result<Map<String, Object>> checkIn(@PathVariable String id) {
        return Result.success(bookingService.checkIn(id));
    }

    @PostMapping("/{id}/check-out")
    public Result<Map<String, Object>> checkOut(@PathVariable String id) {
        return Result.success(bookingService.checkOut(id));
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(
            @RequestHeader("X-User-Id") String userId,
            @PathVariable String id) {
        bookingService.delete(id, userId);
        return Result.success(null);
    }
}
