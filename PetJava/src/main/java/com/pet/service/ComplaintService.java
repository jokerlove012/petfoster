package com.pet.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pet.common.PageResult;
import com.pet.entity.Booking;
import com.pet.entity.Complaint;
import com.pet.entity.Institution;
import com.pet.entity.User;
import com.pet.mapper.BookingMapper;
import com.pet.mapper.ComplaintMapper;
import com.pet.mapper.InstitutionMapper;
import com.pet.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ComplaintService {
    private final ComplaintMapper complaintMapper;
    private final UserMapper userMapper;
    private final InstitutionMapper institutionMapper;
    private final BookingMapper bookingMapper;
    private final ObjectMapper objectMapper;
    private final NotificationService notificationService;

    /**
     * 用户提交投诉/工单
     */
    @SneakyThrows
    public Map<String, Object> submitComplaint(String userId, Map<String, Object> data) {
        Complaint complaint = new Complaint();
        
        // 生成投诉编号
        String dateStr = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String random = UUID.randomUUID().toString().substring(0, 6).toUpperCase();
        complaint.setComplaintNumber("TK" + dateStr + random);
        
        complaint.setUserId(userId);
        complaint.setCategory((String) data.get("category"));
        complaint.setDescription((String) data.get("description"));
        complaint.setExpectation((String) data.get("subject")); // 用subject作为期望/标题
        complaint.setStatus("pending");
        
        // 如果有关联订单
        if (data.get("bookingId") != null) {
            complaint.setBookingId((String) data.get("bookingId"));
            // 获取订单关联的机构
            Booking booking = bookingMapper.selectById((String) data.get("bookingId"));
            if (booking != null) {
                complaint.setInstitutionId(booking.getInstitutionId());
            }
        }
        
        // 如果有附件
        if (data.get("attachments") != null) {
            complaint.setEvidence(objectMapper.writeValueAsString(data.get("attachments")));
        }
        
        complaintMapper.insert(complaint);
        
        // 发送通知给管理员 - 链接到投诉管理页面
        notificationService.sendToAdmins(
            "NEW_COMPLAINT",
            "新投诉工单",
            "收到新的投诉工单 " + complaint.getComplaintNumber() + "，请及时处理",
            "/admin/complaints"
        );
        
        // 发送确认通知给用户 - 链接到我的工单页面
        notificationService.send(
            userId,
            "COMPLAINT_SUBMITTED",
            "工单提交成功",
            "您的工单 " + complaint.getComplaintNumber() + " 已提交，我们将尽快处理",
            "/support/my-tickets"
        );
        
        Map<String, Object> result = new HashMap<>();
        result.put("id", complaint.getId());
        result.put("complaintNumber", complaint.getComplaintNumber());
        result.put("status", complaint.getStatus());
        return result;
    }

    /**
     * 获取用户的投诉列表
     */
    public PageResult<Map<String, Object>> getUserComplaints(String userId, int page, int pageSize) {
        LambdaQueryWrapper<Complaint> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Complaint::getUserId, userId);
        wrapper.orderByDesc(Complaint::getCreatedAt);
        
        Page<Complaint> pageResult = complaintMapper.selectPage(new Page<>(page, pageSize), wrapper);
        List<Map<String, Object>> list = new ArrayList<>();
        for (Complaint complaint : pageResult.getRecords()) {
            list.add(toComplaintVO(complaint));
        }
        return PageResult.of(list, page, pageSize, pageResult.getTotal());
    }

    public PageResult<Map<String, Object>> getComplaints(String status, int page, int pageSize) {
        LambdaQueryWrapper<Complaint> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(status) && !"all".equals(status)) {
            wrapper.eq(Complaint::getStatus, status);
        }
        wrapper.orderByDesc(Complaint::getCreatedAt);
        
        Page<Complaint> pageResult = complaintMapper.selectPage(new Page<>(page, pageSize), wrapper);
        List<Map<String, Object>> list = new ArrayList<>();
        for (Complaint complaint : pageResult.getRecords()) {
            list.add(toComplaintVO(complaint));
        }
        return PageResult.of(list, page, pageSize, pageResult.getTotal());
    }

    public Map<String, Object> getComplaintStats() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("all", complaintMapper.selectCount(null));
        stats.put("pending", complaintMapper.selectCount(new LambdaQueryWrapper<Complaint>().eq(Complaint::getStatus, "pending")));
        stats.put("awaiting_response", complaintMapper.selectCount(new LambdaQueryWrapper<Complaint>().eq(Complaint::getStatus, "awaiting_response")));
        stats.put("under_review", complaintMapper.selectCount(new LambdaQueryWrapper<Complaint>().eq(Complaint::getStatus, "under_review")));
        stats.put("resolved", complaintMapper.selectCount(new LambdaQueryWrapper<Complaint>().eq(Complaint::getStatus, "resolved")));
        return stats;
    }

    public Map<String, Object> getComplaintDetail(String id) {
        Complaint complaint = complaintMapper.selectById(id);
        if (complaint == null) {
            throw new RuntimeException("投诉不存在");
        }
        return toComplaintVO(complaint);
    }

    public Map<String, Object> urgeResponse(String id) {
        Complaint complaint = complaintMapper.selectById(id);
        if (complaint == null) {
            throw new RuntimeException("投诉不存在");
        }
        // 这里可以发送通知给机构
        return toComplaintVO(complaint);
    }

    @SneakyThrows
    public Map<String, Object> resolveComplaint(String id, String decision, String remedies, String resolvedBy) {
        Complaint complaint = complaintMapper.selectById(id);
        if (complaint == null) {
            throw new RuntimeException("投诉不存在");
        }
        
        complaint.setStatus("resolved");
        Map<String, Object> resolution = new HashMap<>();
        resolution.put("decision", decision);
        resolution.put("remedies", remedies);
        resolution.put("resolvedBy", resolvedBy);
        resolution.put("resolvedAt", LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        complaint.setResolution(objectMapper.writeValueAsString(resolution));
        
        complaintMapper.updateById(complaint);
        
        // 发送通知给用户
        notificationService.send(
            complaint.getUserId(),
            "COMPLAINT_RESOLVED",
            "工单已处理",
            "您的工单 " + complaint.getComplaintNumber() + " 已处理完成，处理结果：" + decision,
            "/support/my-tickets"
        );
        
        return toComplaintVO(complaint);
    }

    @SneakyThrows
    private Map<String, Object> toComplaintVO(Complaint complaint) {
        Map<String, Object> vo = new HashMap<>();
        vo.put("id", complaint.getId());
        vo.put("complaintNumber", complaint.getComplaintNumber());
        vo.put("category", complaint.getCategory());
        vo.put("description", complaint.getDescription());
        vo.put("expectation", complaint.getExpectation());
        vo.put("status", complaint.getStatus());
        vo.put("institutionResponse", complaint.getInstitutionResponse());
        vo.put("createdAt", complaint.getCreatedAt() != null ? 
                complaint.getCreatedAt().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")) : null);
        vo.put("updatedAt", complaint.getUpdatedAt() != null ? 
                complaint.getUpdatedAt().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")) : null);
        
        // 解析证据
        if (StringUtils.hasText(complaint.getEvidence())) {
            vo.put("evidence", objectMapper.readValue(complaint.getEvidence(), new TypeReference<List<String>>() {}));
        } else {
            vo.put("evidence", new ArrayList<>());
        }
        
        // 解析处理结果
        if (StringUtils.hasText(complaint.getResolution())) {
            vo.put("resolution", objectMapper.readValue(complaint.getResolution(), new TypeReference<Map<String, Object>>() {}));
        }
        
        // 获取投诉人信息
        User user = userMapper.selectById(complaint.getUserId());
        if (user != null) {
            vo.put("petOwnerName", user.getName());
            String phone = user.getPhone();
            if (phone != null && phone.length() > 7) {
                vo.put("petOwnerPhone", phone.substring(0, 3) + "****" + phone.substring(7));
            } else {
                vo.put("petOwnerPhone", phone);
            }
        }
        
        // 获取机构信息
        Institution inst = institutionMapper.selectById(complaint.getInstitutionId());
        if (inst != null) {
            vo.put("institutionName", inst.getName());
            vo.put("institutionId", inst.getId());
        }
        
        // 获取订单信息
        Booking booking = bookingMapper.selectById(complaint.getBookingId());
        if (booking != null) {
            vo.put("bookingOrderNumber", booking.getOrderNumber());
        }
        
        return vo;
    }
}
