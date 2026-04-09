package com.pet.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("withdrawal")
public class Withdrawal {
    @TableId(type = IdType.ASSIGN_UUID)
    private String id;
    
    private String walletId;
    private String userId;
    private Integer amount;
    private Integer fee;
    private Integer actualAmount;
    private String accountId;
    private String status;
    private String auditRemark;
    private LocalDateTime auditedAt;
    private String auditedBy;
    
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
    
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}
