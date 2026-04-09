package com.pet.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("wallet_transaction")
public class WalletTransaction {
    @TableId(type = IdType.ASSIGN_UUID)
    private String id;
    
    private String walletId;
    private String userId;
    private String type;
    private Integer amount;
    private Integer fee;
    private Integer balanceBefore;
    private Integer balanceAfter;
    private String status;
    private String description;
    private String relatedOrderId;
    private String relatedWithdrawalId;
    
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}
