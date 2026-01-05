package com.pet.controller;

import com.pet.common.PageResult;
import com.pet.common.Result;
import com.pet.service.AdminService;
import com.pet.service.ComplaintService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 管理员接口
 */
@RestController
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminController {
    private final AdminService adminService;
    private final ComplaintService complaintService;

    // ========== 仪表盘统计 ==========

    @GetMapping("/dashboard/stats")
    public Result<Map<String, Object>> getDashboardStats() {
        return Result.success(adminService.getDashboardStats());
    }

    // ========== 机构审核 ==========

    @GetMapping("/institutions")
    public Result<PageResult<Map<String, Object>>> getInstitutions(
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int pageSize) {
        return Result.success(adminService.getInstitutions(status, page, pageSize));
    }

    @GetMapping("/institutions/{id}")
    public Result<Map<String, Object>> getInstitutionDetail(@PathVariable String id) {
        return Result.success(adminService.getInstitutionDetail(id));
    }

    @PostMapping("/institutions/{id}/approve")
    public Result<Map<String, Object>> approveInstitution(@PathVariable String id) {
        return Result.success(adminService.approveInstitution(id));
    }

    @PostMapping("/institutions/{id}/reject")
    public Result<Map<String, Object>> rejectInstitution(
            @PathVariable String id,
            @RequestBody Map<String, String> body) {
        return Result.success(adminService.rejectInstitution(id, body.get("reason")));
    }

    // ========== 用户管理 ==========

    @GetMapping("/users")
    public Result<PageResult<Map<String, Object>>> getUsers(
            @RequestParam(required = false) String role,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int pageSize) {
        return Result.success(adminService.getUsers(role, keyword, page, pageSize));
    }

    // ========== 订单管理 ==========

    @GetMapping("/orders")
    public Result<PageResult<Map<String, Object>>> getOrders(
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int pageSize) {
        return Result.success(adminService.getOrders(status, page, pageSize));
    }

    // ========== 投诉管理 ==========

    @GetMapping("/complaints")
    public Result<PageResult<Map<String, Object>>> getComplaints(
            @RequestParam(required = false, defaultValue = "all") String status,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int pageSize) {
        return Result.success(complaintService.getComplaints(status, page, pageSize));
    }

    @GetMapping("/complaints/stats")
    public Result<Map<String, Object>> getComplaintStats() {
        return Result.success(complaintService.getComplaintStats());
    }

    @GetMapping("/complaints/{id}")
    public Result<Map<String, Object>> getComplaintDetail(@PathVariable String id) {
        return Result.success(complaintService.getComplaintDetail(id));
    }

    @PostMapping("/complaints/{id}/urge")
    public Result<Map<String, Object>> urgeResponse(@PathVariable String id) {
        return Result.success(complaintService.urgeResponse(id));
    }

    @PostMapping("/complaints/{id}/resolve")
    public Result<Map<String, Object>> resolveComplaint(
            @PathVariable String id,
            @RequestBody Map<String, String> body) {
        return Result.success(complaintService.resolveComplaint(
                id, 
                body.get("decision"), 
                body.get("remedies"),
                body.getOrDefault("resolvedBy", "管理员")
        ));
    }
}
