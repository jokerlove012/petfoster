package com.pet.dto;

import javax.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 注册请求DTO
 * 用于接收用户注册时提交的信息
 */
@Data
public class RegisterRequest {
    
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
     * 角色，不能为空（pet_owner、institution_staff）
     */
    @NotBlank(message = "角色不能为空")
    private String role;
    
    /**
     * 用户姓名（可选）
     */
    private String name;
}
