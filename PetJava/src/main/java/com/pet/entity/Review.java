package com.pet.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("review")
public class Review {
    @TableId(type = IdType.ASSIGN_UUID)
    private String id;
    private String bookingId;
    private String userId;
    private String institutionId;
    private String rating; // JSON object: overall, environment, service, hygiene, communication
    private String content;
    private String images; // JSON array
    private String reply; // JSON object: content, repliedAt
    private Boolean isAnonymous;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
    @TableLogic
    private Integer deleted;
}
