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
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class ComplaintService {
    private final ComplaintMapper complaintMapper;
    private final UserMapper userMapper;
    private final InstitutionMapper institutionMapper;
    private final BookingMapper bookingMapper;
    private final ObjectMapper objectMapper;
    private final NotificationService notificationService;
    private final InstitutionService institutionService;

    /**
     * 用户提交投诉/工单
     */
    @SneakyThrows
    public Map<String, Object> submitComplaint(String userId, Map<String, Object> data) {
        log.info("提交投诉 - 用户ID: {}, 数据: {}", userId, data);
        
        Complaint complaint = new Complaint();
        
        // 生成投诉编号
        String dateStr = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String random = UUID.randomUUID().toString().substring(0, 6).toUpperCase();
        complaint.setComplaintNumber("CP" + dateStr + random);
        
        complaint.setUserId(userId);
        complaint.setCategory((String) data.get("category"));
        complaint.setDescription((String) data.get("description"));
        
        // 期望处理 - 支持 expectation 或 subject
        if (data.get("expectation") != null) {
            complaint.setExpectation((String) data.get("expectation"));
        } else if (data.get("subject") != null) {
            complaint.setExpectation((String) data.get("subject"));
        }
        
        complaint.setStatus("pending");
        
        // 如果有关联订单 - 支持 bookingOrderNumber 或 bookingId
        String bookingIdValue = null;
        if (data.get("bookingOrderNumber") != null) {
            bookingIdValue = (String) data.get("bookingOrderNumber");
        } else if (data.get("bookingId") != null) {
            bookingIdValue = (String) data.get("bookingId");
        }
        
        if (bookingIdValue != null) {
            // 尝试通过订单号找到订单
            Booking booking = bookingMapper.selectOne(
                new LambdaQueryWrapper<Booking>().eq(Booking::getOrderNumber, bookingIdValue)
            );
            if (booking == null) {
                // 如果没找到，尝试直接用ID查询
                booking = bookingMapper.selectById(bookingIdValue);
            }
            
            if (booking != null) {
                complaint.setBookingId(booking.getId());
                complaint.setInstitutionId(booking.getInstitutionId());
            } else {
                complaint.setBookingId(bookingIdValue);
            }
        }
        
        // 如果有机构ID
        if (data.get("institutionId") != null && complaint.getInstitutionId() == null) {
            complaint.setInstitutionId((String) data.get("institutionId"));
        }
        
        // 如果有附件 - 支持 evidence 或 attachments
        List<String> attachments = null;
        if (data.get("evidence") != null) {
            attachments = (List<String>) data.get("evidence");
        } else if (data.get("attachments") != null) {
            attachments = (List<String>) data.get("attachments");
        }
        
        if (attachments != null) {
            complaint.setEvidence(objectMapper.writeValueAsString(attachments));
        }
        
        complaintMapper.insert(complaint);
        
        log.info("投诉保存成功 - 投诉ID: {}, 编号: {}, 机构ID: {}, 状态: {}", 
            complaint.getId(), complaint.getComplaintNumber(), complaint.getInstitutionId(), complaint.getStatus());
        
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

    /**
     * 获取机构的投诉列表
     */
    public PageResult<Map<String, Object>> getInstitutionComplaints(String staffUserId, String status, int page, int pageSize) {
        String institutionId = getInstitutionIdByStaff(staffUserId);
        
        log.info("查询机构投诉 - 机构ID: {}, 状态: {}, 页码: {}", institutionId, status, page);
        
        // 先查询所有投诉，用于调试
        List<Complaint> allComplaints = complaintMapper.selectList(null);
        log.info("数据库中总投诉数: {}", allComplaints.size());
        for (Complaint c : allComplaints) {
            log.info("投诉 - ID: {}, 编号: {}, 机构ID: {}, 状态: {}", c.getId(), c.getComplaintNumber(), c.getInstitutionId(), c.getStatus());
        }
        
        LambdaQueryWrapper<Complaint> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Complaint::getInstitutionId, institutionId);
        if (StringUtils.hasText(status) && !"all".equals(status)) {
            wrapper.eq(Complaint::getStatus, status);
        }
        wrapper.orderByDesc(Complaint::getCreatedAt);
        
        Page<Complaint> pageResult = complaintMapper.selectPage(new Page<>(page, pageSize), wrapper);
        log.info("查询到 {} 条符合条件的投诉", pageResult.getRecords().size());
        
        List<Map<String, Object>> list = new ArrayList<>();
        for (Complaint complaint : pageResult.getRecords()) {
            list.add(toComplaintVO(complaint));
        }
        return PageResult.of(list, page, pageSize, pageResult.getTotal());
    }

    /**
     * 获取机构的投诉统计
     */
    public Map<String, Object> getInstitutionComplaintStats(String staffUserId) {
        String institutionId = getInstitutionIdByStaff(staffUserId);
        
        Map<String, Object> stats = new HashMap<>();
        stats.put("total", complaintMapper.selectCount(
            new LambdaQueryWrapper<Complaint>().eq(Complaint::getInstitutionId, institutionId)));
        stats.put("pending", complaintMapper.selectCount(
            new LambdaQueryWrapper<Complaint>().eq(Complaint::getInstitutionId, institutionId)
                .eq(Complaint::getStatus, "pending")));
        stats.put("awaiting_response", complaintMapper.selectCount(
            new LambdaQueryWrapper<Complaint>().eq(Complaint::getInstitutionId, institutionId)
                .eq(Complaint::getStatus, "awaiting_response")));
        stats.put("under_review", complaintMapper.selectCount(
            new LambdaQueryWrapper<Complaint>().eq(Complaint::getInstitutionId, institutionId)
                .eq(Complaint::getStatus, "under_review")));
        stats.put("resolved", complaintMapper.selectCount(
            new LambdaQueryWrapper<Complaint>().eq(Complaint::getInstitutionId, institutionId)
                .eq(Complaint::getStatus, "resolved")));
        return stats;
    }

    /**
     * 机构回复投诉
     */
    public Map<String, Object> institutionReply(String staffUserId, String complaintId, String response) {
        String institutionId = getInstitutionIdByStaff(staffUserId);
        Complaint complaint = complaintMapper.selectById(complaintId);
        
        if (complaint == null) {
            throw new RuntimeException("投诉不存在");
        }
        if (!institutionId.equals(complaint.getInstitutionId())) {
            throw new RuntimeException("无权操作此投诉");
        }
        
        complaint.setInstitutionResponse(response);
        complaint.setStatus("awaiting_response");
        complaintMapper.updateById(complaint);
        
        // 发送通知给管理员
        notificationService.sendToAdmins(
            "COMPLAINT_RESPONDED",
            "机构已回复投诉",
            "机构已回复投诉 " + complaint.getComplaintNumber(),
            "/admin/complaints"
        );
        
        return toComplaintVO(complaint);
    }

    /**
     * 获取机构ID（从员工ID）
     */
    private String getInstitutionIdByStaff(String staffUserId) {
        return institutionService.getInstitutionIdByStaff(staffUserId);
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
