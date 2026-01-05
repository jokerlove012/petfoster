package com.pet.dto;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import lombok.Data;
import java.util.Map;

@Data
public class CreateBookingRequest {
    @NotBlank(message = "机构ID不能为空")
    private String institutionId;
    @NotBlank(message = "套餐ID不能为空")
    private String servicePackageId;
    @NotBlank(message = "宠物ID不能为空")
    private String petId;
    @NotBlank(message = "开始日期不能为空")
    private String startDate;
    @NotBlank(message = "结束日期不能为空")
    private String endDate;
    private String specialRequirements;
    @NotNull(message = "紧急联系人不能为空")
    private Map<String, String> emergencyContact;
}
