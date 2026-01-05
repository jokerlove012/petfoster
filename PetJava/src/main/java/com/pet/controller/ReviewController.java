package com.pet.controller;

import com.pet.common.PageResult;
import com.pet.common.Result;
import com.pet.dto.CreateReviewRequest;
import com.pet.service.ReviewService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.Map;

@RestController
@RequestMapping("/reviews")
@RequiredArgsConstructor
public class ReviewController {
    private final ReviewService reviewService;

    @PostMapping
    public Result<Map<String, Object>> create(
            @RequestHeader("X-User-Id") String userId,
            @Valid @RequestBody CreateReviewRequest request) {
        return Result.success(reviewService.create(userId, request));
    }

    @GetMapping("/{id}")
    public Result<Map<String, Object>> getDetail(@PathVariable String id) {
        return Result.success(reviewService.getDetail(id));
    }

    @PostMapping("/{id}/reply")
    public Result<Map<String, Object>> reply(@PathVariable String id, @RequestBody Map<String, String> body) {
        return Result.success(reviewService.reply(id, body.get("content")));
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable String id) {
        reviewService.delete(id);
        return Result.success();
    }
}
