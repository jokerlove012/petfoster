package com.pet.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("booking")
public class Booking {
    @TableId(type = IdType.ASSIGN_UUID)
    private String id;
    private String orderNumber;
    private String userId;
    private String institutionId;
    private String servicePackageId;
    private String petId;
    private String status; // pending, confirmed, in_progress, completed, cancelled
    private LocalDate startDate;
    private LocalDate endDate;
    private LocalDateTime checkInTime;
    private LocalDateTime checkOutTime;
    private Integer totalDays;
    private BigDecimal basePrice;
    private BigDecimal discount;
    private BigDecimal totalPrice;
    private String paymentStatus; // pending, paid, refunded, partial_refund
    private String paymentMethod; // wechat, alipay, card
    private LocalDateTime paidAt;
    private BigDecimal refundAmount;
    private LocalDateTime refundedAt;
    private String cancelReason;
    private String specialRequirements;
    private String emergencyContact; // JSON object
    private Boolean userDeleted; // 用户删除标记
    private Boolean institutionDeleted; // 机构删除标记
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
    @TableLogic
    private Integer deleted;
}
