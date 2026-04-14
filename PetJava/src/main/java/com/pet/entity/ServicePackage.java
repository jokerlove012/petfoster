package com.pet.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 服务套餐实体类
 * 对应数据库表service_package，存储机构提供的寄养服务套餐信息
 */
@Data
@TableName("service_package")
public class ServicePackage {
    
    /**
     * 服务套餐ID，使用UUID自动生成
     */
    @TableId(type = IdType.ASSIGN_UUID)
    private String id;
    
    /**
     * 所属机构ID
     */
    private String institutionId;
    
    /**
     * 套餐名称
     */
    private String name;
    
    /**
     * 套餐描述
     */
    private String description;
    
    /**
     * 适用的宠物类型，JSON数组格式
     */
    private String petTypes;
    
    /**
     * 每日价格
     */
    private BigDecimal pricePerDay;
    
    /**
     * 套餐特色服务列表，JSON数组格式
     */
    private String features;
    
    /**
     * 最大宠物体重限制
     */
    private BigDecimal maxWeight;
    
    /**
     * 套餐是否启用
     */
    private Boolean isActive;
    
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
