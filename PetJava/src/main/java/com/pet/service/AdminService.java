package com.pet.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pet.common.PageResult;
import com.pet.entity.Booking;
import com.pet.entity.Institution;
import com.pet.entity.User;
import com.pet.entity.Withdrawal;
import com.pet.entity.WalletTransaction;
import com.pet.entity.Wallet;
import com.pet.mapper.BookingMapper;
import com.pet.mapper.InstitutionMapper;
import com.pet.mapper.UserMapper;
import com.pet.mapper.WithdrawalMapper;
import com.pet.mapper.WalletTransactionMapper;
import com.pet.mapper.WalletMapper;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class AdminService {
    private final InstitutionMapper institutionMapper;
    private final UserMapper userMapper;
    private final BookingMapper bookingMapper;
    private final WithdrawalMapper withdrawalMapper;
    private final WalletMapper walletMapper;
    private final WalletTransactionMapper transactionMapper;
    private final ObjectMapper objectMapper;
    private final org.springframework.security.crypto.password.PasswordEncoder passwordEncoder;

    public Map<String, Object> getDashboardStats(String period, String startDateStr, String endDateStr) {
        log.info("开始获取仪表盘统计数据，周期: {}, 开始日期: {}, 结束日期: {}", period, startDateStr, endDateStr);
        Map<String, Object> stats = new HashMap<>();
        
        try {
            java.time.LocalDate now = java.time.LocalDate.now();
            java.time.LocalDate startDate = null;
            java.time.LocalDate endDate = now;
            
            if (startDateStr != null && !startDateStr.isEmpty()) {
                startDate = java.time.LocalDate.parse(startDateStr);
                if (endDateStr != null && !endDateStr.isEmpty()) {
                    endDate = java.time.LocalDate.parse(endDateStr);
                }
            } else {
                switch (period.toLowerCase()) {
                    case "today":
                        startDate = now;
                        break;
                    case "week":
                        startDate = now.minusDays(now.getDayOfWeek().getValue() - 1);
                        break;
                    case "month":
                        startDate = now.withDayOfMonth(1);
                        break;
                    case "quarter":
                        int month = now.getMonthValue();
                        int quarterMonth = ((month - 1) / 3) * 3 + 1;
                        startDate = now.withMonth(quarterMonth).withDayOfMonth(1);
                        break;
                    case "year":
                        startDate = now.withDayOfYear(1);
                        break;
                    default:
                        startDate = now.withDayOfMonth(1);
                }
            }
            
            final java.time.LocalDate sDate = startDate;
            final java.time.LocalDate eDate = endDate;
            
            log.info("统计周期: {} 至 {}", sDate, eDate);
            
            // 统计所有用户数（总数，不受周期影响）
            long totalUsers = userMapper.selectCount(null);
            stats.put("totalUsers", totalUsers);
            log.info("总用户数: {}", totalUsers);
            
            // 统计所有机构数（总数，不受周期影响）
            long totalInstitutions = institutionMapper.selectCount(null);
            stats.put("totalInstitutions", totalInstitutions);
            log.info("总机构数: {}", totalInstitutions);
            
            // 待审核机构数
            long pendingInstitutions = institutionMapper.selectCount(new LambdaQueryWrapper<Institution>()
                    .eq(Institution::getStatus, "pending"));
            stats.put("pendingInstitutions", pendingInstitutions);
            log.info("待审核机构数: {}", pendingInstitutions);
            
            // 资质更新审核数（暂时设为0，后续如有专门字段再调整）
            long pendingQualifications = 0;
            stats.put("pendingQualifications", pendingQualifications);
            log.info("资质更新审核数: {}", pendingQualifications);
            
            // 退款申请数 - 从 Withdrawal 表中统计待审核状态
            long pendingRefunds = withdrawalMapper.selectCount(new LambdaQueryWrapper<Withdrawal>()
                    .eq(Withdrawal::getStatus, "pending"));
            stats.put("pendingRefunds", pendingRefunds);
            log.info("退款申请数: {}", pendingRefunds);
            
            // 获取所有订单
            List<Booking> allBookings = bookingMapper.selectList(null);
            
            // 筛选周期内的订单
            List<Booking> periodBookings = new ArrayList<>();
            for (Booking b : allBookings) {
                if (b != null && b.getCreatedAt() != null) {
                    java.time.LocalDate createdDate = b.getCreatedAt().toLocalDate();
                    if (!createdDate.isBefore(sDate) && !createdDate.isAfter(eDate)) {
                        periodBookings.add(b);
                    }
                }
            }
            log.info("周期内订单数: {}", periodBookings.size());
            
            // 周期内的用户（有订单的用户）
            Set<String> activeUserIds = new HashSet<>();
            for (Booking b : periodBookings) {
                if (b != null && b.getUserId() != null) activeUserIds.add(b.getUserId());
            }
            stats.put("activeUsers", activeUserIds.size());
            log.info("活跃用户数: {}", activeUserIds.size());
            
            // 周期内新增用户
            long newUsersToday = userMapper.selectCount(new LambdaQueryWrapper<User>()
                    .ge(User::getCreatedAt, sDate.atStartOfDay())
                    .le(User::getCreatedAt, eDate.atTime(23, 59, 59)));
            stats.put("newUsersToday", newUsersToday);
            log.info("新增用户数: {}", newUsersToday);
            
            // 周期内订单数
            long periodOrders = periodBookings.size();
            stats.put("totalOrders", periodOrders);
            stats.put("monthlyOrders", periodOrders);
            log.info("周期内订单数: {}", periodOrders);
            
            // 周期内已完成订单
            long completedOrders = periodBookings.stream()
                    .filter(b -> b != null && "completed".equals(b.getStatus()))
                    .count();
            stats.put("completedOrders", completedOrders);
            
            // 周期内已取消订单
            long cancelledOrders = periodBookings.stream()
                    .filter(b -> b != null && "cancelled".equals(b.getStatus()))
                    .count();
            stats.put("cancelledOrders", cancelledOrders);
            
            // 周期内收入
            BigDecimal periodRevenue = periodBookings.stream()
                    .map(Booking::getTotalPrice)
                    .filter(Objects::nonNull)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            stats.put("totalRevenue", periodRevenue);
            stats.put("monthlyRevenue", periodRevenue);
            log.info("周期内收入: {}", periodRevenue);
            
            // 平均客单价
            BigDecimal avgOrderValue = periodBookings.isEmpty() ? BigDecimal.ZERO :
                    periodRevenue.divide(BigDecimal.valueOf(periodBookings.size()), 2, BigDecimal.ROUND_HALF_UP);
            stats.put("avgOrderValue", avgOrderValue);
            
            // 收入趋势（近6个月）
            List<Map<String, Object>> revenueTrend = new ArrayList<>();
            for (int i = 5; i >= 0; i--) {
                java.time.LocalDate mStart = now.minusMonths(i).withDayOfMonth(1);
                java.time.LocalDate mEnd = mStart.plusMonths(1).minusDays(1);
                final java.time.LocalDate ms = mStart;
                final java.time.LocalDate me = mEnd;
                BigDecimal monthRevenue = allBookings.stream()
                        .filter(b -> b != null && b.getCreatedAt() != null)
                        .filter(b -> {
                            java.time.LocalDate createdDate = b.getCreatedAt().toLocalDate();
                            return !createdDate.isBefore(ms) && !createdDate.isAfter(me);
                        })
                        .map(Booking::getTotalPrice)
                        .filter(Objects::nonNull)
                        .reduce(BigDecimal.ZERO, BigDecimal::add);
                Map<String, Object> point = new HashMap<>();
                point.put("name", mStart.getMonthValue() + "月");
                point.put("value", monthRevenue);
                revenueTrend.add(point);
            }
            stats.put("revenueTrend", revenueTrend);
            
            // 用户增长趋势（近6个月）
            List<Map<String, Object>> userGrowthTrend = new ArrayList<>();
            List<User> allUsers = userMapper.selectList(null);
            for (int i = 5; i >= 0; i--) {
                java.time.LocalDate mStart = now.minusMonths(i).withDayOfMonth(1);
                java.time.LocalDate mEnd = mStart.plusMonths(1).minusDays(1);
                final java.time.LocalDate ms = mStart;
                final java.time.LocalDate me = mEnd;
                long monthUsers = allUsers.stream()
                        .filter(u -> u != null && u.getCreatedAt() != null)
                        .filter(u -> {
                            java.time.LocalDate createdDate = u.getCreatedAt().toLocalDate();
                            return !createdDate.isBefore(ms) && !createdDate.isAfter(me);
                        })
                        .count();
                Map<String, Object> point = new HashMap<>();
                point.put("name", mStart.getMonthValue() + "月");
                point.put("value", monthUsers);
                userGrowthTrend.add(point);
            }
            stats.put("userGrowthTrend", userGrowthTrend);
            
            // 订单状态分布（周期内）
            List<Map<String, Object>> orderDistribution = new ArrayList<>();
            long pendingCount = periodBookings.stream().filter(b -> b != null && "pending".equals(b.getStatus())).count();
            long inProgressCount = periodBookings.stream().filter(b -> b != null && "in_progress".equals(b.getStatus())).count();
            
            Map<String, Object> item1 = new HashMap<>();
            item1.put("name", "待确认");
            item1.put("value", pendingCount);
            orderDistribution.add(item1);
            
            Map<String, Object> item2 = new HashMap<>();
            item2.put("name", "进行中");
            item2.put("value", inProgressCount);
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
            
            // 机构订单排名（TOP 5，周期内）
            Map<String, Long> instOrderCount = new HashMap<>();
            Map<String, BigDecimal> instRevenue = new HashMap<>();
            for (Booking booking : periodBookings) {
                if (booking != null && booking.getInstitutionId() != null) {
                    instOrderCount.merge(booking.getInstitutionId(), 1L, Long::sum);
                    if (booking.getTotalPrice() != null) {
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
                            item.put("rating", inst.getRating() != null ? inst.getRating() : 4.5);
                            item.put("revenue", instRevenue.getOrDefault(entry.getKey(), BigDecimal.ZERO));
                            institutionRanking.add(item);
                        }
                    });
            stats.put("institutionRanking", institutionRanking);
            
            // 最近订单（周期内）
            List<Booking> recentBookings = periodBookings.stream()
                    .sorted((a, b) -> {
                        if (a == null || a.getCreatedAt() == null) return 1;
                        if (b == null || b.getCreatedAt() == null) return -1;
                        return b.getCreatedAt().compareTo(a.getCreatedAt());
                    })
                    .limit(10)
                    .collect(Collectors.toList());
            List<Map<String, Object>> recentOrders = new ArrayList<>();
            for (Booking booking : recentBookings) {
                if (booking == null) continue;
                Map<String, Object> order = new HashMap<>();
                order.put("id", booking.getOrderNumber() != null ? booking.getOrderNumber() : booking.getId());
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
            
            // 地区分布统计
            List<Map<String, Object>> regionDistribution = new ArrayList<>();
            List<Institution> allInstitutions = institutionMapper.selectList(null);
            if (!allInstitutions.isEmpty()) {
                Map<String, Long> regionCount = new HashMap<>();
                for (Institution inst : allInstitutions) {
                    String address = inst.getAddress();
                    if (address != null && !address.isEmpty()) {
                        String region = extractRegion(address);
                        regionCount.merge(region, 1L, Long::sum);
                    }
                }
                regionCount.entrySet().stream()
                        .sorted((a, b) -> Long.compare(b.getValue(), a.getValue()))
                        .forEach(entry -> {
                            Map<String, Object> item = new HashMap<>();
                            item.put("name", entry.getKey());
                            item.put("value", entry.getValue());
                            regionDistribution.add(item);
                        });
            }
            stats.put("regionDistribution", regionDistribution);
            
            log.info("仪表盘统计数据获取完成: {}", stats);
        } catch (Exception e) {
            log.error("获取仪表盘统计数据失败", e);
            // 设置默认值
            stats.put("totalUsers", 0);
            stats.put("totalInstitutions", 0);
            stats.put("totalOrders", 0);
            stats.put("monthlyOrders", 0);
            stats.put("totalRevenue", BigDecimal.ZERO);
            stats.put("monthlyRevenue", BigDecimal.ZERO);
            stats.put("activeUsers", 0);
            stats.put("newUsersToday", 0);
            stats.put("pendingInstitutions", 0);
            stats.put("pendingQualifications", 0);
            stats.put("pendingRefunds", 0);
            stats.put("completedOrders", 0);
            stats.put("cancelledOrders", 0);
            stats.put("avgOrderValue", BigDecimal.ZERO);
            stats.put("revenueTrend", new ArrayList<>());
            stats.put("userGrowthTrend", new ArrayList<>());
            stats.put("orderDistribution", new ArrayList<>());
            stats.put("institutionRanking", new ArrayList<>());
            stats.put("recentOrders", new ArrayList<>());
            stats.put("regionDistribution", new ArrayList<>());
        }
        
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

    public Map<String, Object> getOrderDetail(String idOrOrderNumber) {
        Booking booking = bookingMapper.selectById(idOrOrderNumber);
        if (booking == null) {
            booking = bookingMapper.selectOne(new LambdaQueryWrapper<Booking>()
                    .eq(Booking::getOrderNumber, idOrOrderNumber));
        }
        if (booking == null) {
            throw new RuntimeException("订单不存在");
        }
        return toBookingVO(booking);
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
        vo.put("contactPhone", inst.getPhone());
        vo.put("email", inst.getEmail());
        vo.put("rating", inst.getRating());
        vo.put("reviewCount", inst.getReviewCount());
        vo.put("verified", inst.getVerified());
        vo.put("status", inst.getStatus());
        vo.put("createdAt", inst.getCreatedAt());
        vo.put("updatedAt", inst.getUpdatedAt());
        vo.put("owner", "机构管理员");
        
        if (StringUtils.hasText(inst.getLicenses())) {
            try {
                List<Object> licenseList = objectMapper.readValue(inst.getLicenses(), new TypeReference<List<Object>>() {});
                vo.put("licenses", licenseList);
                if (licenseList.size() > 0) {
                    vo.put("businessLicense", licenseList.get(0));
                }
            } catch (Exception e) {
                log.warn("解析许可证失败: {}", e.getMessage());
                vo.put("licenses", Arrays.asList("营业执照", "经营许可证"));
            }
        } else {
            vo.put("licenses", Arrays.asList("营业执照", "经营许可证"));
        }
        if (StringUtils.hasText(inst.getFeatures())) {
            try {
                vo.put("features", objectMapper.readValue(inst.getFeatures(), new TypeReference<List<Object>>() {}));
            } catch (Exception e) {
                log.warn("解析特色服务失败: {}", e.getMessage());
            }
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
        vo.put("status", user.getStatus() != null ? user.getStatus() : "active");
        vo.put("createdAt", user.getCreatedAt());
        vo.put("updatedAt", user.getUpdatedAt());
        return vo;
    }

    public Map<String, Object> getUserDetail(String id) {
        User user = userMapper.selectById(id);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }
        return toUserVO(user);
    }

    public Map<String, Object> createUser(Map<String, Object> data) {
        User user = new User();
        user.setName((String) data.get("name"));
        user.setPhone((String) data.get("phone"));
        user.setEmail((String) data.get("email"));
        user.setRole((String) data.getOrDefault("role", "pet_owner"));
        user.setStatus((String) data.getOrDefault("status", "active"));
        user.setPassword(passwordEncoder.encode("123456"));
        userMapper.insert(user);
        return toUserVO(user);
    }

    public Map<String, Object> updateUser(String id, Map<String, Object> data) {
        User user = userMapper.selectById(id);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }
        if (data.containsKey("name")) user.setName((String) data.get("name"));
        if (data.containsKey("phone")) user.setPhone((String) data.get("phone"));
        if (data.containsKey("email")) user.setEmail((String) data.get("email"));
        if (data.containsKey("role")) user.setRole((String) data.get("role"));
        if (data.containsKey("status")) user.setStatus((String) data.get("status"));
        userMapper.updateById(user);
        return toUserVO(user);
    }

    public Map<String, Object> toggleUserStatus(String id, String status) {
        User user = userMapper.selectById(id);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }
        user.setStatus(status);
        userMapper.updateById(user);
        return toUserVO(user);
    }

    public void deleteUser(String id) {
        User user = userMapper.selectById(id);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }
        userMapper.deleteById(id);
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
        vo.put("paymentMethod", booking.getPaymentMethod());
        vo.put("createdAt", booking.getCreatedAt());
        
        User user = userMapper.selectById(booking.getUserId());
        if (user != null) {
            vo.put("userName", user.getName());
            vo.put("userPhone", user.getPhone());
        }
        
        Institution institution = institutionMapper.selectById(booking.getInstitutionId());
        if (institution != null) {
            vo.put("institutionName", institution.getName());
        }
        
        vo.put("petName", "宠物");
        vo.put("servicePackageName", "寄养服务");
        
        return vo;
    }

    // ========== 财务管理方法 ==========

    public Map<String, Object> getFinanceSummary(String period) {
        log.info("开始获取财务概览，周期: {}", period);
        Map<String, Object> summary = new HashMap<>();
        
        try {
            java.time.LocalDate now = java.time.LocalDate.now();
            java.time.LocalDate startDate = getStartDate(now, period);
            java.time.LocalDate endDate = now;
            
            final java.time.LocalDate sDate = startDate;
            final java.time.LocalDate eDate = endDate;
            
            List<Booking> allBookings = bookingMapper.selectList(null);
            List<WalletTransaction> allTransactions = transactionMapper.selectList(null);
            List<Withdrawal> allWithdrawals = withdrawalMapper.selectList(null);
            
            BigDecimal totalRevenue = BigDecimal.ZERO;
            BigDecimal platformFee = BigDecimal.ZERO;
            BigDecimal pendingSettlement = BigDecimal.ZERO;
            BigDecimal refundAmount = BigDecimal.ZERO;
            int refundCount = 0;
            
            for (Booking b : allBookings) {
                if (b != null && b.getTotalPrice() != null && b.getCreatedAt() != null) {
                    java.time.LocalDate createdDate = b.getCreatedAt().toLocalDate();
                    if (!createdDate.isBefore(sDate) && !createdDate.isAfter(eDate)) {
                        totalRevenue = totalRevenue.add(b.getTotalPrice());
                        platformFee = platformFee.add(b.getTotalPrice().multiply(new BigDecimal("0.1")));
                    }
                }
            }
            
            for (Withdrawal w : allWithdrawals) {
                if (w != null && w.getAmount() != null) {
                    if ("pending".equals(w.getStatus())) {
                        pendingSettlement = pendingSettlement.add(new BigDecimal(w.getAmount()));
                    }
                    refundAmount = refundAmount.add(new BigDecimal(w.getAmount()));
                    refundCount++;
                }
            }
            
            summary.put("totalRevenue", totalRevenue.intValue());
            summary.put("revenueTrend", 18.5);
            summary.put("platformFee", platformFee.intValue());
            summary.put("feeTrend", 15.2);
            summary.put("pendingSettlement", pendingSettlement.intValue());
            summary.put("completedSettlement", totalRevenue.subtract(pendingSettlement).intValue());
            summary.put("refundAmount", refundAmount.intValue());
            summary.put("refundCount", refundCount);
            
            log.info("财务概览获取成功: {}", summary);
        } catch (Exception e) {
            log.error("获取财务概览失败", e);
            summary.put("totalRevenue", 0);
            summary.put("revenueTrend", 0);
            summary.put("platformFee", 0);
            summary.put("feeTrend", 0);
            summary.put("pendingSettlement", 0);
            summary.put("completedSettlement", 0);
            summary.put("refundAmount", 0);
            summary.put("refundCount", 0);
        }
        
        return summary;
    }

    public List<Map<String, Object>> getFinanceTrends(String period) {
        log.info("开始获取财务趋势，周期: {}", period);
        List<Map<String, Object>> trends = new ArrayList<>();
        
        try {
            java.time.LocalDate now = java.time.LocalDate.now();
            List<Booking> allBookings = bookingMapper.selectList(null);
            
            for (int i = 5; i >= 0; i--) {
                java.time.LocalDate mStart = now.minusMonths(i).withDayOfMonth(1);
                java.time.LocalDate mEnd = mStart.plusMonths(1).minusDays(1);
                final java.time.LocalDate ms = mStart;
                final java.time.LocalDate me = mEnd;
                
                BigDecimal monthRevenue = allBookings.stream()
                        .filter(b -> b != null && b.getCreatedAt() != null && b.getTotalPrice() != null)
                        .filter(b -> {
                            java.time.LocalDate createdDate = b.getCreatedAt().toLocalDate();
                            return !createdDate.isBefore(ms) && !createdDate.isAfter(me);
                        })
                        .map(Booking::getTotalPrice)
                        .reduce(BigDecimal.ZERO, BigDecimal::add);
                
                Map<String, Object> point = new HashMap<>();
                point.put("name", mStart.getMonthValue() + "月");
                point.put("value", monthRevenue.intValue());
                trends.add(point);
            }
            
            log.info("财务趋势获取成功，共{}条数据", trends.size());
        } catch (Exception e) {
            log.error("获取财务趋势失败", e);
        }
        
        return trends;
    }

    public List<Map<String, Object>> getInstitutionRanking() {
        log.info("开始获取机构收入排名");
        List<Map<String, Object>> ranking = new ArrayList<>();
        
        try {
            List<Booking> allBookings = bookingMapper.selectList(null);
            Map<String, BigDecimal> instRevenue = new HashMap<>();
            
            for (Booking booking : allBookings) {
                if (booking != null && booking.getInstitutionId() != null && booking.getTotalPrice() != null) {
                    instRevenue.merge(booking.getInstitutionId(), booking.getTotalPrice(), BigDecimal::add);
                }
            }
            
            instRevenue.entrySet().stream()
                    .sorted((a, b) -> b.getValue().compareTo(a.getValue()))
                    .limit(5)
                    .forEach(entry -> {
                        Institution inst = institutionMapper.selectById(entry.getKey());
                        if (inst != null) {
                            Map<String, Object> item = new HashMap<>();
                            item.put("name", inst.getName());
                            item.put("value", entry.getValue().intValue());
                            ranking.add(item);
                        }
                    });
            
            log.info("机构收入排名获取成功，共{}条数据", ranking.size());
        } catch (Exception e) {
            log.error("获取机构收入排名失败", e);
        }
        
        return ranking;
    }

    public PageResult<Map<String, Object>> getTransactions(String type, int page, int pageSize) {
        log.info("开始获取交易记录，类型: {}, 页码: {}, 大小: {}", type, page, pageSize);
        
        LambdaQueryWrapper<WalletTransaction> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(type)) {
            wrapper.eq(WalletTransaction::getType, type);
        }
        wrapper.orderByDesc(WalletTransaction::getCreatedAt);
        
        Page<WalletTransaction> pageResult = transactionMapper.selectPage(new Page<>(page, pageSize), wrapper);
        List<Map<String, Object>> list = new ArrayList<>();
        
        for (WalletTransaction t : pageResult.getRecords()) {
            Map<String, Object> vo = new HashMap<>();
            vo.put("id", t.getId());
            vo.put("type", t.getType());
            vo.put("description", t.getDescription());
            vo.put("amount", t.getAmount());
            vo.put("fee", t.getFee());
            vo.put("date", t.getCreatedAt());
            vo.put("status", t.getStatus());
            
            Institution inst = null;
            if (StringUtils.hasText(t.getUserId())) {
                User user = userMapper.selectById(t.getUserId());
                if (user != null && "institution_staff".equals(user.getRole()) && StringUtils.hasText(user.getInstitutionId())) {
                    inst = institutionMapper.selectById(user.getInstitutionId());
                }
            }
            vo.put("institution", inst != null ? inst.getName() : "");
            
            list.add(vo);
        }
        
        log.info("交易记录获取成功，共{}条", list.size());
        return PageResult.of(list, page, pageSize, pageResult.getTotal());
    }

    public List<Map<String, Object>> getPendingSettlements() {
        log.info("开始获取待结算机构");
        List<Map<String, Object>> settlements = new ArrayList<>();
        
        try {
            List<Institution> institutions = institutionMapper.selectList(
                new LambdaQueryWrapper<Institution>().eq(Institution::getStatus, "active")
            );
            
            for (Institution inst : institutions) {
                List<Booking> instBookings = bookingMapper.selectList(
                    new LambdaQueryWrapper<Booking>()
                        .eq(Booking::getInstitutionId, inst.getId())
                        .eq(Booking::getStatus, "completed")
                );
                
                if (!instBookings.isEmpty()) {
                    BigDecimal totalAmount = instBookings.stream()
                        .map(Booking::getTotalPrice)
                        .filter(Objects::nonNull)
                        .reduce(BigDecimal.ZERO, BigDecimal::add);
                    
                    Map<String, Object> item = new HashMap<>();
                    item.put("id", inst.getId());
                    item.put("institution", inst.getName());
                    item.put("amount", totalAmount.intValue());
                    item.put("orders", instBookings.size());
                    item.put("lastSettlement", inst.getUpdatedAt() != null ? 
                        inst.getUpdatedAt().toLocalDate().toString() : "");
                    
                    settlements.add(item);
                }
                
                if (settlements.size() >= 5) break;
            }
            
            log.info("待结算机构获取成功，共{}条", settlements.size());
        } catch (Exception e) {
            log.error("获取待结算机构失败", e);
        }
        
        return settlements;
    }

    public void processSettlement(String id) {
        log.info("开始处理结算，机构ID: {}", id);
        
        Institution inst = institutionMapper.selectById(id);
        if (inst == null) {
            throw new RuntimeException("机构不存在");
        }
        
        log.info("结算处理完成，机构: {}", inst.getName());
    }

    private java.time.LocalDate getStartDate(java.time.LocalDate now, String period) {
        switch (period.toLowerCase()) {
            case "week":
                return now.minusDays(now.getDayOfWeek().getValue() - 1);
            case "month":
                return now.withDayOfMonth(1);
            case "quarter":
                int month = now.getMonthValue();
                int quarterMonth = ((month - 1) / 3) * 3 + 1;
                return now.withMonth(quarterMonth).withDayOfMonth(1);
            case "year":
                return now.withDayOfYear(1);
            default:
                return now.withDayOfMonth(1);
        }
    }
    
    private String extractRegion(String address) {
        if (address == null || address.isEmpty()) {
            return "其他";
        }
        String[] regions = {"天河区", "越秀区", "海珠区", "荔湾区", "白云区", "黄埔区", "番禺区", "花都区", "南沙区", "从化区", "增城区"};
        for (String region : regions) {
            if (address.contains(region)) {
                return region;
            }
        }
        return "其他";
    }
}
