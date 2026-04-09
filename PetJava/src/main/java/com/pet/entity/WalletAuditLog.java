package com.pet.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("wallet_audit_log")
public class WalletAuditLog {
    @TableId(type = IdType.ASSIGN_UUID)
    private String id;
    
    private String walletId;
    private String userId;
    private String operation;
    private String details;
    private String ipAddress;
    private String userAgent;
    
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}
