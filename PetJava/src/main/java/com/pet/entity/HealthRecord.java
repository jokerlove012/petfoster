package com.pet.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 健康记录实体类
 * 对应数据库表health_record，存储宠物寄养期间的每日健康记录
 */
@Data
@TableName("health_record")
public class HealthRecord {
    
    /**
     * 健康记录ID，使用UUID自动生成
     */
    @TableId(type = IdType.ASSIGN_UUID)
    private String id;
    
    /**
     * 关联的订单ID
     */
    private String bookingId;
    
    /**
     * 记录日期
     */
    private LocalDate date;
    
    /**
     * 进食状态：normal(正常)、reduced(减少)、increased(增加)、refused(拒绝进食)
     */
    private String feedingStatus;
    
    /**
     * 活动水平：high(高)、normal(正常)、low(低)、inactive(不活跃)
     */
    private String activityLevel;
    
    /**
     * 健康观察备注
     */
    private String healthObservations;
    
    /**
     * 情绪状态：happy(开心)、calm(平静)、anxious(焦虑)、stressed(紧张)
     */
    private String mood;
    
    /**
     * 体重记录
     */
    private BigDecimal weight;
    
    /**
     * 体温记录
     */
    private BigDecimal temperature;
    
    /**
     * 用药记录，JSON数组格式
     */
    private String medications;
    
    /**
     * 照片列表，JSON数组格式
     */
    private String photos;
    
    /**
     * 视频列表，JSON数组格式
     */
    private String videos;
    
    /**
     * 是否有异常情况
     */
    private Boolean isAbnormal;
    
    /**
     * 异常情况详细描述
     */
    private String abnormalDetails;
    
    /**
     * 记录创建人ID
     */
    private String createdBy;
    
    /**
     * 创建时间，插入时自动填充
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}
