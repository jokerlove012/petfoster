package com.pet.controller;

import com.pet.common.Result;
import com.pet.dto.LoginRequest;
import com.pet.dto.RegisterRequest;
import com.pet.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {
    private final UserService userService;

    @PostMapping("/login")
    public Result<Map<String, Object>> login(@Valid @RequestBody LoginRequest request) {
        return Result.success(userService.login(request));
    }

    @PostMapping("/register")
    public Result<Map<String, Object>> register(@Valid @RequestBody RegisterRequest request) {
        return Result.success(userService.register(request));
    }

    @PostMapping("/logout")
    public Result<Void> logout() {
        return Result.success();
    }

    @GetMapping("/me")
    public Result<Map<String, Object>> getCurrentUser(@RequestHeader(value = "X-User-Id", required = false) String userId) {
        if (userId == null) {
            return Result.error(401, "未登录");
        }
        com.pet.entity.User user = userService.getById(userId);
        if (user == null) {
            return Result.error(401, "用户不存在");
        }
        Map<String, Object> vo = new HashMap<>();
        vo.put("id", user.getId());
        vo.put("email", user.getEmail());
        vo.put("phone", user.getPhone());
        vo.put("name", user.getName());
        vo.put("avatar", user.getAvatar());
        vo.put("role", user.getRole());
        vo.put("createdAt", user.getCreatedAt());
        vo.put("updatedAt", user.getUpdatedAt());
        return Result.success(vo);
    }

    @PostMapping("/refresh")
    public Result<Map<String, Object>> refreshToken() {
        Map<String, Object> result = new HashMap<>();
        result.put("token", UUID.randomUUID().toString().replace("-", ""));
        return Result.success(result);
    }

    @PostMapping("/change-password")
    public Result<Void> changePassword(
            @RequestHeader("X-User-Id") String userId,
            @RequestBody Map<String, String> request) {
        String oldPassword = request.get("oldPassword");
        String newPassword = request.get("newPassword");
        userService.changePassword(userId, oldPassword, newPassword);
        return Result.success();
    }
}
