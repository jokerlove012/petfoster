package com.pet.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("user")
public class User {
    @TableId(type = IdType.ASSIGN_UUID)
    private String id;
    private String email;
    private String phone;
    private String password;
    private String name;
    private String avatar;
    private String gender; // male, female
    private LocalDate birthday;
    private String address;
    private String role; // pet_owner, institution_staff, admin
    private String adminLevel; // super, normal (for admin)
    private String institutionId; // for institution_staff
    private String position; // manager, caretaker, receptionist
    private String permissions; // JSON array
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
    @TableLogic
    private Integer deleted;
}
