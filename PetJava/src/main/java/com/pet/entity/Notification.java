package com.pet.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("notification")
public class Notification {
    @TableId(type = IdType.ASSIGN_UUID)
    private String id;
    
    private String userId;
    private String type;  // booking/payment/health/review/system/message
    private String title;
    private String content;
    private Boolean isRead;
    private String link;
    private String data;  // JSON
    
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    @TableLogic
    private Integer deleted;
}
