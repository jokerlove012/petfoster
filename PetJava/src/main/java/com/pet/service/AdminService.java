package com.pet.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pet.common.PageResult;
import com.pet.entity.Booking;
import com.pet.entity.Institution;
import com.pet.entity.User;
import com.pet.mapper.BookingMapper;
import com.pet.mapper.InstitutionMapper;
import com.pet.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.util.*;

@Service
@RequiredArgsConstructor
public class AdminService {
    private final InstitutionMapper institutionMapper;
    private final UserMapper userMapper;
    private final BookingMapper bookingMapper;
    private final ObjectMapper objectMapper;

    public Map<String, Object> getDashboardStats() {
        Map<String, Object> stats = new HashMap<>();
        
        // 统计用户数
        long totalUsers = userMapper.selectCount(new LambdaQueryWrapper<User>()
                .eq(User::getRole, "pet_owner"));
        stats.put("totalUsers", totalUsers);
        
        // 活跃用户（有订单的用户）
        List<Booking> allBookings = bookingMapper.selectList(null);
        Set<String> activeUserIds = new HashSet<>();
        for (Booking b : allBookings) {
            if (b.getUserId() != null) activeUserIds.add(b.getUserId());
        }
        stats.put("activeUsers", activeUserIds.size());
        
        // 今日新增用户
        java.time.LocalDate today = java.time.LocalDate.now();
        long newUsersToday = userMapper.selectCount(new LambdaQueryWrapper<User>()
                .ge(User::getCreatedAt, today.atStartOfDay()));
        stats.put("newUsersToday", newUsersToday);
        
        // 统计机构数
        long totalInstitutions = institutionMapper.selectCount(new LambdaQueryWrapper<Institution>()
                .eq(Institution::getStatus, "active"));
        stats.put("totalInstitutions", totalInstitutions);
        
        // 待审核机构数
        long pendingInstitutions = institutionMapper.selectCount(new LambdaQueryWrapper<Institution>()
                .eq(Institution::getStatus, "pending"));
        stats.put("pendingInstitutions", pendingInstitutions);
        
        // 统计订单数
        long totalOrders = bookingMapper.selectCount(null);
        stats.put("totalOrders", totalOrders);
        
        // 已完成订单
        long completedOrders = bookingMapper.selectCount(new LambdaQueryWrapper<Booking>()
                .eq(Booking::getStatus, "completed"));
        stats.put("completedOrders", completedOrders);
        
        // 已取消订单
        long cancelledOrders = bookingMapper.selectCount(new LambdaQueryWrapper<Booking>()
                .eq(Booking::getStatus, "cancelled"));
        stats.put("cancelledOrders", cancelledOrders);
        
        // 统计收入
        List<Booking> paidBookings = bookingMapper.selectList(new LambdaQueryWrapper<Booking>()
                .eq(Booking::getPaymentStatus, "paid"));
        BigDecimal totalRevenue = paidBookings.stream()
                .map(Booking::getTotalPrice)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        stats.put("totalRevenue", totalRevenue);
        
        // 平均客单价
        BigDecimal avgOrderValue = paidBookings.isEmpty() ? BigDecimal.ZERO :
                totalRevenue.divide(BigDecimal.valueOf(paidBookings.size()), 2, BigDecimal.ROUND_HALF_UP);
        stats.put("avgOrderValue", avgOrderValue);
        
        // 收入趋势（近6个月）
        List<Map<String, Object>> revenueTrend = new ArrayList<>();
        for (int i = 5; i >= 0; i--) {
            java.time.LocalDate monthStart = java.time.LocalDate.now().minusMonths(i).withDayOfMonth(1);
            java.time.LocalDate monthEnd = monthStart.plusMonths(1).minusDays(1);
            final java.time.LocalDate ms = monthStart;
            final java.time.LocalDate me = monthEnd;
            BigDecimal monthRevenue = paidBookings.stream()
                    .filter(b -> b.getPaidAt() != null)
                    .filter(b -> {
                        java.time.LocalDate paidDate = b.getPaidAt().toLocalDate();
                        return !paidDate.isBefore(ms) && !paidDate.isAfter(me);
                    })
                    .map(Booking::getTotalPrice)
                    .filter(Objects::nonNull)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            Map<String, Object> point = new HashMap<>();
            point.put("name", monthStart.getMonthValue() + "月");
            point.put("value", monthRevenue);
            revenueTrend.add(point);
        }
        stats.put("revenueTrend", revenueTrend);
        
        // 用户增长趋势（近6个月）
        List<Map<String, Object>> userGrowthTrend = new ArrayList<>();
        List<User> allUsers = userMapper.selectList(null);
        for (int i = 5; i >= 0; i--) {
            java.time.LocalDate monthStart = java.time.LocalDate.now().minusMonths(i).withDayOfMonth(1);
            java.time.LocalDate monthEnd = monthStart.plusMonths(1).minusDays(1);
            final java.time.LocalDate ms = monthStart;
            final java.time.LocalDate me = monthEnd;
            long monthUsers = allUsers.stream()
                    .filter(u -> u.getCreatedAt() != null)
                    .filter(u -> {
                        java.time.LocalDate createdDate = u.getCreatedAt().toLocalDate();
                        return !createdDate.isBefore(ms) && !createdDate.isAfter(me);
                    })
                    .count();
            Map<String, Object> point = new HashMap<>();
            point.put("name", monthStart.getMonthValue() + "月");
            point.put("value", monthUsers);
            userGrowthTrend.add(point);
        }
        stats.put("userGrowthTrend", userGrowthTrend);
        
        // 订单状态分布
        List<Map<String, Object>> orderDistribution = new ArrayList<>();
        Long pendingCount = bookingMapper.selectCount(new LambdaQueryWrapper<Booking>().eq(Booking::getStatus, "pending"));
        Long inProgressCount = bookingMapper.selectCount(new LambdaQueryWrapper<Booking>().eq(Booking::getStatus, "in_progress"));
        
        Map<String, Object> item1 = new HashMap<>();
        item1.put("name", "待确认");
        item1.put("value", pendingCount != null ? pendingCount : 0L);
        orderDistribution.add(item1);
        
        Map<String, Object> item2 = new HashMap<>();
        item2.put("name", "进行中");
        item2.put("value", inProgressCount != null ? inProgressCount : 0L);
        orderDistribution.add(item2);
        
        Map<String, Object> item3 = new HashMap<>();
        item3.put("name", "已完成");
        item3.put("value", completedOrders);
        orderDistribution.add(item3);
        
        Map<String, Object> item4 = new HashMap<>();
        item4.put("name", "已取消");
        item4.put("value", cancelledOrders);
        orderDistribution.add(item4);
        
        stats.put("orderDistribution", orderDistribution);
        
        // 机构订单排名（TOP 5）
        Map<String, Long> instOrderCount = new HashMap<>();
        Map<String, BigDecimal> instRevenue = new HashMap<>();
        for (Booking booking : allBookings) {
            if (booking.getInstitutionId() != null) {
                instOrderCount.merge(booking.getInstitutionId(), 1L, Long::sum);
                if ("paid".equals(booking.getPaymentStatus()) && booking.getTotalPrice() != null) {
                    instRevenue.merge(booking.getInstitutionId(), booking.getTotalPrice(), BigDecimal::add);
                }
            }
        }
        List<Map<String, Object>> institutionRanking = new ArrayList<>();
        instOrderCount.entrySet().stream()
                .sorted((a, b) -> Long.compare(b.getValue(), a.getValue()))
                .limit(5)
                .forEach(entry -> {
                    Institution inst = institutionMapper.selectById(entry.getKey());
                    if (inst != null) {
                        Map<String, Object> item = new HashMap<>();
                        item.put("name", inst.getName());
                        item.put("value", entry.getValue());
                        item.put("rating", inst.getRating());
                        item.put("revenue", instRevenue.getOrDefault(entry.getKey(), BigDecimal.ZERO));
                        institutionRanking.add(item);
                    }
                });
        stats.put("institutionRanking", institutionRanking);
        
        // 最近订单
        List<Booking> recentBookings = bookingMapper.selectList(new LambdaQueryWrapper<Booking>()
                .orderByDesc(Booking::getCreatedAt)
                .last("LIMIT 10"));
        List<Map<String, Object>> recentOrders = new ArrayList<>();
        for (Booking booking : recentBookings) {
            Map<String, Object> order = new HashMap<>();
            order.put("id", booking.getOrderNumber());
            order.put("status", booking.getStatus());
            order.put("amount", booking.getTotalPrice());
            order.put("paymentMethod", booking.getPaymentMethod() != null ? booking.getPaymentMethod() : "微信支付");
            
            User user = userMapper.selectById(booking.getUserId());
            order.put("user", user != null ? user.getName() : "未知");
            
            Institution inst = institutionMapper.selectById(booking.getInstitutionId());
            order.put("institution", inst != null ? inst.getName() : "未知");
            
            order.put("pet", "宠物");
            order.put("petType", "dog");
            
            recentOrders.add(order);
        }
        stats.put("recentOrders", recentOrders);
        
        return stats;
    }

    public PageResult<Map<String, Object>> getInstitutions(String status, int page, int pageSize) {
        LambdaQueryWrapper<Institution> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(status)) {
            wrapper.eq(Institution::getStatus, status);
        }
        wrapper.orderByDesc(Institution::getCreatedAt);
        
        Page<Institution> pageResult = institutionMapper.selectPage(new Page<>(page, pageSize), wrapper);
        List<Map<String, Object>> list = new ArrayList<>();
        for (Institution inst : pageResult.getRecords()) {
            list.add(toInstitutionVO(inst));
        }
        return PageResult.of(list, page, pageSize, pageResult.getTotal());
    }

    public Map<String, Object> getInstitutionDetail(String id) {
        Institution inst = institutionMapper.selectById(id);
        if (inst == null) {
            throw new RuntimeException("机构不存在");
        }
        return toInstitutionVO(inst);
    }

    public Map<String, Object> approveInstitution(String id) {
        Institution inst = institutionMapper.selectById(id);
        if (inst == null) {
            throw new RuntimeException("机构不存在");
        }
        inst.setStatus("active");
        inst.setVerified(true);
        institutionMapper.updateById(inst);
        return toInstitutionVO(inst);
    }

    public Map<String, Object> rejectInstitution(String id, String reason) {
        Institution inst = institutionMapper.selectById(id);
        if (inst == null) {
            throw new RuntimeException("机构不存在");
        }
        inst.setStatus("rejected");
        institutionMapper.updateById(inst);
        
        Map<String, Object> result = toInstitutionVO(inst);
        result.put("rejectReason", reason);
        return result;
    }

    public PageResult<Map<String, Object>> getUsers(String role, String keyword, int page, int pageSize) {
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(role)) {
            wrapper.eq(User::getRole, role);
        }
        if (StringUtils.hasText(keyword)) {
            wrapper.and(w -> w.like(User::getName, keyword)
                    .or().like(User::getPhone, keyword));
        }
        wrapper.orderByDesc(User::getCreatedAt);
        
        Page<User> pageResult = userMapper.selectPage(new Page<>(page, pageSize), wrapper);
        List<Map<String, Object>> list = new ArrayList<>();
        for (User user : pageResult.getRecords()) {
            list.add(toUserVO(user));
        }
        return PageResult.of(list, page, pageSize, pageResult.getTotal());
    }

    public PageResult<Map<String, Object>> getOrders(String status, int page, int pageSize) {
        LambdaQueryWrapper<Booking> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(status)) {
            wrapper.eq(Booking::getStatus, status);
        }
        wrapper.orderByDesc(Booking::getCreatedAt);
        
        Page<Booking> pageResult = bookingMapper.selectPage(new Page<>(page, pageSize), wrapper);
        List<Map<String, Object>> list = new ArrayList<>();
        for (Booking booking : pageResult.getRecords()) {
            list.add(toBookingVO(booking));
        }
        return PageResult.of(list, page, pageSize, pageResult.getTotal());
    }

    @SneakyThrows
    private Map<String, Object> toInstitutionVO(Institution inst) {
        Map<String, Object> vo = new HashMap<>();
        vo.put("id", inst.getId());
        vo.put("name", inst.getName());
        vo.put("description", inst.getDescription());
        vo.put("logo", inst.getLogo());
        vo.put("address", inst.getAddress());
        vo.put("phone", inst.getPhone());
        vo.put("email", inst.getEmail());
        vo.put("rating", inst.getRating());
        vo.put("reviewCount", inst.getReviewCount());
        vo.put("verified", inst.getVerified());
        vo.put("status", inst.getStatus());
        vo.put("createdAt", inst.getCreatedAt());
        vo.put("updatedAt", inst.getUpdatedAt());
        
        if (StringUtils.hasText(inst.getLicenses())) {
            vo.put("licenses", objectMapper.readValue(inst.getLicenses(), new TypeReference<List<Object>>() {}));
        }
        if (StringUtils.hasText(inst.getFeatures())) {
            vo.put("features", objectMapper.readValue(inst.getFeatures(), new TypeReference<List<Object>>() {}));
        }
        
        return vo;
    }

    private Map<String, Object> toUserVO(User user) {
        Map<String, Object> vo = new HashMap<>();
        vo.put("id", user.getId());
        vo.put("name", user.getName());
        vo.put("phone", user.getPhone());
        vo.put("email", user.getEmail());
        vo.put("avatar", user.getAvatar());
        vo.put("role", user.getRole());
        vo.put("createdAt", user.getCreatedAt());
        return vo;
    }

    private Map<String, Object> toBookingVO(Booking booking) {
        Map<String, Object> vo = new HashMap<>();
        vo.put("id", booking.getId());
        vo.put("orderNumber", booking.getOrderNumber());
        vo.put("userId", booking.getUserId());
        vo.put("institutionId", booking.getInstitutionId());
        vo.put("status", booking.getStatus());
        vo.put("startDate", booking.getStartDate());
        vo.put("endDate", booking.getEndDate());
        vo.put("totalPrice", booking.getTotalPrice());
        vo.put("paymentStatus", booking.getPaymentStatus());
        vo.put("createdAt", booking.getCreatedAt());
        return vo;
    }
}
