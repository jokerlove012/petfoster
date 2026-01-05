package com.pet.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("favorite")
public class Favorite {
    @TableId(type = IdType.ASSIGN_UUID)
    private String id;
    private String userId;
    private String institutionId;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}
