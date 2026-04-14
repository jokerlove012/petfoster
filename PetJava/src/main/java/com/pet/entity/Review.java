package com.pet.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 评价实体类
 * 对应数据库表review，存储用户对寄养机构的评价信息
 */
@Data
@TableName("review")
public class Review {
    
    /**
     * 评价ID，使用UUID自动生成
     */
    @TableId(type = IdType.ASSIGN_UUID)
    private String id;
    
    /**
     * 关联的订单ID
     */
    private String bookingId;
    
    /**
     * 评价用户ID
     */
    private String userId;
    
    /**
     * 被评价的机构ID
     */
    private String institutionId;
    
    /**
     * 评分信息，JSON对象格式：包含overall(总体)、environment(环境)、service(服务)、hygiene(卫生)、communication(沟通)
     */
    private String rating;
    
    /**
     * 评价内容
     */
    private String content;
    
    /**
     * 评价图片列表，JSON数组格式
     */
    private String images;
    
    /**
     * 机构回复，JSON对象格式：包含content(内容)、repliedAt(回复时间)
     */
    private String reply;
    
    /**
     * 是否匿名评价
     */
    private Boolean isAnonymous;
    
    /**
     * 创建时间，插入时自动填充
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
    
    /**
     * 更新时间，插入和更新时自动填充
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
    
    /**
     * 逻辑删除标记：0-未删除，1-已删除
     */
    @TableLogic
    private Integer deleted;
}
