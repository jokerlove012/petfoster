package com.pet.dto;

import javax.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 登录请求DTO
 * 用于接收用户登录时提交的手机号、密码和角色信息
 */
@Data
public class LoginRequest {
    
    /**
     * 手机号，不能为空
     */
    @NotBlank(message = "手机号不能为空")
    private String phone;
    
    /**
     * 密码，不能为空
     */
    @NotBlank(message = "密码不能为空")
    private String password;
    
    /**
     * 角色，不能为空（pet_owner、institution_staff、admin）
     */
    @NotBlank(message = "角色不能为空")
    private String role;
}
