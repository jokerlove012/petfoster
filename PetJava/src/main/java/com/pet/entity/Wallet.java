package com.pet.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("wallet")
public class Wallet {
    @TableId(type = IdType.ASSIGN_UUID)
    private String id;
    
    private String userId;
    private String userType;
    private Integer balance;
    private Integer frozenBalance;
    private Integer totalIncome;
    private Integer totalWithdraw;
    private String withdrawPassword;
    private String status;
    private Integer dailyWithdrawAmount;
    private Integer dailyWithdrawCount;
    private LocalDate lastWithdrawDate;
    
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
    
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
    
    @TableLogic
    private Integer deleted;
}
