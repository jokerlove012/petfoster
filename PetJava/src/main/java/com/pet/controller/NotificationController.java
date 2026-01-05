package com.pet.controller;

import com.pet.common.Result;
import com.pet.entity.Notification;
import com.pet.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/notifications")
@RequiredArgsConstructor
public class NotificationController {
    private final NotificationService notificationService;

    /**
     * 获取通知列表
     */
    @GetMapping
    public Result<List<Map<String, Object>>> list(
            @RequestHeader("X-User-Id") String userId,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) Boolean isRead) {
        List<Notification> list = notificationService.getByUserId(userId, type, isRead);
        List<Map<String, Object>> result = list.stream()
                .map(notificationService::toVO)
                .collect(Collectors.toList());
        return Result.success(result);
    }

    /**
     * 获取未读数量
     */
    @GetMapping("/unread-count")
    public Result<Map<String, Object>> unreadCount(@RequestHeader("X-User-Id") String userId) {
        int count = notificationService.getUnreadCount(userId);
        Map<String, Object> result = new HashMap<>();
        result.put("count", count);
        return Result.success(result);
    }

    /**
     * 标记为已读
     */
    @PutMapping("/{id}/read")
    public Result<Void> markAsRead(
            @PathVariable String id,
            @RequestHeader("X-User-Id") String userId) {
        notificationService.markAsRead(id, userId);
        return Result.success(null);
    }

    /**
     * 标记全部已读
     */
    @PutMapping("/read-all")
    public Result<Void> markAllAsRead(@RequestHeader("X-User-Id") String userId) {
        notificationService.markAllAsRead(userId);
        return Result.success(null);
    }

    /**
     * 删除通知
     */
    @DeleteMapping("/{id}")
    public Result<Void> delete(
            @PathVariable String id,
            @RequestHeader("X-User-Id") String userId) {
        notificationService.delete(id, userId);
        return Result.success(null);
    }

    /**
     * 清空已读通知
     */
    @DeleteMapping("/clear-read")
    public Result<Void> clearRead(@RequestHeader("X-User-Id") String userId) {
        notificationService.clearRead(userId);
        return Result.success(null);
    }
}
