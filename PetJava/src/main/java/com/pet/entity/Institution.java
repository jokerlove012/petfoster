package com.pet.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("institution")
public class Institution {
    @TableId(type = IdType.ASSIGN_UUID)
    private String id;
    private String name;
    private String description;
    private String logo;
    private String images; // JSON array
    private String address;
    private BigDecimal latitude;
    private BigDecimal longitude;
    private String phone;
    private String email;
    private String businessHours; // JSON object
    private String petTypes; // JSON array: dog, cat, other
    private String capacity; // JSON object
    private String currentOccupancy; // JSON object
    private BigDecimal rating;
    private Integer reviewCount;
    private Boolean verified;
    private String status; // pending, active, suspended, rejected
    private String features; // JSON array
    private String licenses; // JSON array
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
    @TableLogic
    private Integer deleted;
}
