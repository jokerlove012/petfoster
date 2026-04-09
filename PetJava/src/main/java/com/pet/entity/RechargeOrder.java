package com.pet.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("recharge_order")
public class RechargeOrder {
    @TableId(type = IdType.ASSIGN_UUID)
    private String id;
    
    private String walletId;
    private String userId;
    private Integer amount;
    private String paymentMethod;
    private String status;
    private String paymentOrderId;
    private LocalDateTime paidAt;
    private LocalDateTime createdAt;
    private LocalDateTime expiredAt;
}
