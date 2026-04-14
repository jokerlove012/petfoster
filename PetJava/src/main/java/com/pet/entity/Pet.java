package com.pet.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 宠物实体类
 * 对应数据库表pet，存储宠物的基本信息
 */
@Data
@TableName("pet")
public class Pet {
    
    /**
     * 宠物ID，使用UUID自动生成
     */
    @TableId(type = IdType.ASSIGN_UUID)
    private String id;
    
    /**
     * 所属用户ID（宠物主人）
     */
    private String userId;
    
    /**
     * 宠物名字
     */
    private String name;
    
    /**
     * 宠物种类：dog(狗)、cat(猫)、other(其他)
     */
    private String species;
    
    /**
     * 宠物品种
     */
    private String breed;
    
    /**
     * 宠物年龄
     */
    private Integer age;
    
    /**
     * 宠物体重
     */
    private BigDecimal weight;
    
    /**
     * 宠物性别：male(公)、female(母)
     */
    private String gender;
    
    /**
     * 宠物头像URL
     */
    private String avatar;
    
    /**
     * 健康注意事项
     */
    private String healthNotes;
    
    /**
     * 是否已接种疫苗
     */
    private Boolean vaccinated;
    
    /**
     * 是否已绝育
     */
    private Boolean neutered;
    
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
