package com.pet.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 用户实体类
 * 对应数据库表user，存储系统中所有用户的信息，包括宠物主人、机构员工和系统管理员
 */
@Data
@TableName("user")
public class User {
    
    /**
     * 用户ID，使用UUID自动生成
     */
    @TableId(type = IdType.ASSIGN_UUID)
    private String id;
    
    /**
     * 用户邮箱
     */
    private String email;
    
    /**
     * 用户手机号，用于登录
     */
    private String phone;
    
    /**
     * 用户密码，加密存储
     */
    private String password;
    
    /**
     * 用户真实姓名
     */
    private String name;
    
    /**
     * 用户头像URL
     */
    private String avatar;
    
    /**
     * 用户性别：male(男)、female(女)
     */
    private String gender;
    
    /**
     * 用户生日
     */
    private LocalDate birthday;
    
    /**
     * 用户地址
     */
    private String address;
    
    /**
     * 用户角色：pet_owner(宠物主人)、institution_staff(机构员工)、admin(系统管理员)
     */
    private String role;
    
    /**
     * 用户状态：active(正常)、banned(封禁)
     */
    private String status;
    
    /**
     * 管理员级别：super(超级管理员)、normal(普通管理员)
     */
    private String adminLevel;
    
    /**
     * 所属机构ID，仅机构员工使用
     */
    private String institutionId;
    
    /**
     * 职位：manager(经理)、caretaker(护理员)、receptionist(前台)
     */
    private String position;
    
    /**
     * 权限列表，JSON数组格式
     */
    private String permissions;
    
    /**
     * 创建时间，插入时自动填充
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
    
    /**
     * 更新时间，插入和更新时自动填充
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
    
    /**
     * 逻辑删除标记：0-未删除，1-已删除
     */
    @TableLogic
    private Integer deleted;
}
