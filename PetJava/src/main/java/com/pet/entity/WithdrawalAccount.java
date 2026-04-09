package com.pet.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("withdrawal_account")
public class WithdrawalAccount {
    @TableId(type = IdType.ASSIGN_UUID)
    private String id;
    
    private String userId;
    private String type;
    private String accountName;
    private String accountNumber;
    private String bankName;
    private String bankBranch;
    private Integer isDefault;
    
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
    
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
    
    @TableLogic
    private Integer deleted;
}
