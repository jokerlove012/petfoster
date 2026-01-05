package com.pet.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("service_package")
public class ServicePackage {
    @TableId(type = IdType.ASSIGN_UUID)
    private String id;
    private String institutionId;
    private String name;
    private String description;
    private String petTypes; // JSON array
    private BigDecimal pricePerDay;
    private String features; // JSON array
    private BigDecimal maxWeight;
    private Boolean isActive;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
    @TableLogic
    private Integer deleted;
}
