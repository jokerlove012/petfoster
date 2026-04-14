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

/**
 * 认证控制器
 * 处理用户登录、注册、登出、获取当前用户信息等认证相关请求
 */
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {
    private final UserService userService;

    /**
     * 用户登录接口
     * @param request 登录请求对象，包含手机号和密码
     * @return 登录成功后返回用户信息和token
     */
    @PostMapping("/login")
    public Result<Map<String, Object>> login(@Valid @RequestBody LoginRequest request) {
        return Result.success(userService.login(request));
    }

    /**
     * 用户注册接口
     * @param request 注册请求对象，包含手机号、密码、姓名等信息
     * @return 注册成功后返回用户信息和token
     */
    @PostMapping("/register")
    public Result<Map<String, Object>> register(@Valid @RequestBody RegisterRequest request) {
        return Result.success(userService.register(request));
    }

    /**
     * 用户登出接口
     * @return 成功响应
     */
    @PostMapping("/logout")
    public Result<Void> logout() {
        return Result.success();
    }

    /**
     * 获取当前登录用户信息
     * @param userId 从请求属性中获取的用户ID
     * @return 当前用户的详细信息
     */
    @GetMapping("/me")
    public Result<Map<String, Object>> getCurrentUser(@RequestAttribute("userId") String userId) {
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

    /**
     * 刷新token接口
     * @return 新的token
     */
    @PostMapping("/refresh")
    public Result<Map<String, Object>> refreshToken() {
        Map<String, Object> result = new HashMap<>();
        result.put("token", UUID.randomUUID().toString().replace("-", ""));
        return Result.success(result);
    }

    /**
     * 修改密码接口
     * @param userId 当前用户ID
     * @param request 包含旧密码和新密码的请求体
     * @return 成功响应
     */
    @PostMapping("/change-password")
    public Result<Void> changePassword(
            @RequestAttribute("userId") String userId,
            @RequestBody Map<String, String> request) {
        String oldPassword = request.get("oldPassword");
        String newPassword = request.get("newPassword");
        userService.changePassword(userId, oldPassword, newPassword);
        return Result.success();
    }
}
