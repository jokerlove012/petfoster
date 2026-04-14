package com.pet.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 寄养机构实体类
 * 对应数据库表institution，存储寄养机构的详细信息
 */
@Data
@TableName("institution")
public class Institution {
    
    /**
     * 机构ID，使用UUID自动生成
     */
    @TableId(type = IdType.ASSIGN_UUID)
    private String id;
    
    /**
     * 机构名称
     */
    private String name;
    
    /**
     * 机构描述
     */
    private String description;
    
    /**
     * 机构Logo URL
     */
    private String logo;
    
    /**
     * 机构图片列表，JSON数组格式
     */
    private String images;
    
    /**
     * 机构地址
     */
    private String address;
    
    /**
     * 纬度，用于地图定位
     */
    private BigDecimal latitude;
    
    /**
     * 经度，用于地图定位
     */
    private BigDecimal longitude;
    
    /**
     * 联系电话
     */
    private String phone;
    
    /**
     * 联系邮箱
     */
    private String email;
    
    /**
     * 营业时间，JSON对象格式
     */
    private String businessHours;
    
    /**
     * 可接收的宠物类型，JSON数组格式：dog、cat、other
     */
    private String petTypes;
    
    /**
     * 容量信息，JSON对象格式
     */
    private String capacity;
    
    /**
     * 当前入住情况，JSON对象格式
     */
    private String currentOccupancy;
    
    /**
     * 综合评分
     */
    private BigDecimal rating;
    
    /**
     * 评价数量
     */
    private Integer reviewCount;
    
    /**
     * 是否已认证
     */
    private Boolean verified;
    
    /**
     * 机构状态：pending(待审核)、active(正常营业)、suspended(暂停营业)、rejected(审核拒绝)
     */
    private String status;
    
    /**
     * 特色服务列表，JSON数组格式
     */
    private String features;
    
    /**
     * 资质证书列表，JSON数组格式
     */
    private String licenses;
    
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
