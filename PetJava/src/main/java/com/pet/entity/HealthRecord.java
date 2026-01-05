package com.pet.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("health_record")
public class HealthRecord {
    @TableId(type = IdType.ASSIGN_UUID)
    private String id;
    private String bookingId;
    private LocalDate date;
    private String feedingStatus; // normal, reduced, increased, refused
    private String activityLevel; // high, normal, low, inactive
    private String healthObservations;
    private String mood; // happy, calm, anxious, stressed
    private BigDecimal weight;
    private BigDecimal temperature;
    private String medications; // JSON array
    private String photos; // JSON array
    private String videos; // JSON array
    private Boolean isAbnormal;
    private String abnormalDetails;
    private String createdBy;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}
