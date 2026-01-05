package com.pet.controller;

import com.pet.common.Result;
import com.pet.service.HealthRecordService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/health-records")
@RequiredArgsConstructor
public class HealthRecordController {
    private final HealthRecordService healthRecordService;

    @GetMapping("/booking/{bookingId}")
    public Result<List<Map<String, Object>>> getByBookingId(@PathVariable String bookingId) {
        return Result.success(healthRecordService.getByBookingId(bookingId));
    }

    @PostMapping("/booking/{bookingId}")
    public Result<Map<String, Object>> create(
            @PathVariable String bookingId,
            @RequestHeader("X-User-Id") String userId,
            @RequestBody Map<String, Object> data) {
        return Result.success(healthRecordService.create(bookingId, userId, data));
    }
}
