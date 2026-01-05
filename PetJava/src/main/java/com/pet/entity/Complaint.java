package com.pet.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("complaint")
public class Complaint {
    @TableId(type = IdType.ASSIGN_UUID)
    private String id;
    private String complaintNumber;  // 投诉编号
    private String category;         // 投诉类型: service_quality, pet_safety, communication, facility, price, other
    private String userId;           // 投诉人ID
    private String institutionId;    // 被投诉机构ID
    private String bookingId;        // 相关订单ID
    private String description;      // 投诉内容
    private String evidence;         // 证据材料 JSON array
    private String expectation;      // 期望处理
    private String status;           // 状态: pending, awaiting_response, under_review, resolved
    private String institutionResponse; // 机构回复
    private String resolution;       // 处理结果 JSON: decision, remedies, resolvedBy, resolvedAt
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
    @TableLogic
    private Integer deleted;
}
