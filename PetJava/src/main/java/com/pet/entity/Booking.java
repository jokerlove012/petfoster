package com.pet.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 预约/订单实体类
 * 对应数据库表booking，存储宠物寄养预约订单的信息
 */
@Data
@TableName("booking")
public class Booking {
    
    /**
     * 订单ID，使用UUID自动生成
     */
    @TableId(type = IdType.ASSIGN_UUID)
    private String id;
    
    /**
     * 订单编号，用于展示
     */
    private String orderNumber;
    
    /**
     * 用户ID（宠物主人）
     */
    private String userId;
    
    /**
     * 机构ID
     */
    private String institutionId;
    
    /**
     * 服务套餐ID
     */
    private String servicePackageId;
    
    /**
     * 宠物ID
     */
    private String petId;
    
    /**
     * 订单状态：pending(待确认)、confirmed(已确认)、in_progress(进行中)、completed(已完成)、cancelled(已取消)
     */
    private String status;
    
    /**
     * 寄养开始日期
     */
    private LocalDate startDate;
    
    /**
     * 寄养结束日期
     */
    private LocalDate endDate;
    
    /**
     * 实际入住时间
     */
    private LocalDateTime checkInTime;
    
    /**
     * 实际退房时间
     */
    private LocalDateTime checkOutTime;
    
    /**
     * 寄养总天数
     */
    private Integer totalDays;
    
    /**
     * 基础价格
     */
    private BigDecimal basePrice;
    
    /**
     * 优惠金额
     */
    private BigDecimal discount;
    
    /**
     * 订单总价
     */
    private BigDecimal totalPrice;
    
    /**
     * 支付状态：pending(待支付)、paid(已支付)、refunded(已退款)、partial_refund(部分退款)
     */
    private String paymentStatus;
    
    /**
     * 支付方式：wechat(微信)、alipay(支付宝)、card(银行卡)
     */
    private String paymentMethod;
    
    /**
     * 支付时间
     */
    private LocalDateTime paidAt;
    
    /**
     * 退款金额
     */
    private BigDecimal refundAmount;
    
    /**
     * 退款时间
     */
    private LocalDateTime refundedAt;
    
    /**
     * 取消原因
     */
    private String cancelReason;
    
    /**
     * 特殊要求
     */
    private String specialRequirements;
    
    /**
     * 紧急联系人信息，JSON对象格式
     */
    private String emergencyContact;
    
    /**
     * 用户删除标记
     */
    @TableField("user_deleted")
    private Boolean userDeleted;
    
    /**
     * 机构删除标记
     */
    @TableField("institution_deleted")
    private Boolean institutionDeleted;
    
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
