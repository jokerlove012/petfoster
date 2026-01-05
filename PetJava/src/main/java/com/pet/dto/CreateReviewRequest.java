package com.pet.dto;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import lombok.Data;
import java.util.List;
import java.util.Map;

@Data
public class CreateReviewRequest {
    @NotBlank(message = "订单ID不能为空")
    private String bookingId;
    @NotNull(message = "评分不能为空")
    private Map<String, Integer> rating;
    @NotBlank(message = "评价内容不能为空")
    private String content;
    private List<String> images;
    private Boolean isAnonymous;
}
