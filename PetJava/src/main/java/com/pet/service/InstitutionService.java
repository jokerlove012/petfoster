package com.pet.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pet.common.PageResult;
import com.pet.entity.Booking;
import com.pet.entity.Institution;
import com.pet.entity.Pet;
import com.pet.entity.Review;
import com.pet.entity.ServicePackage;
import com.pet.entity.User;
import com.pet.mapper.BookingMapper;
import com.pet.mapper.InstitutionMapper;
import com.pet.mapper.PetMapper;
import com.pet.mapper.ReviewMapper;
import com.pet.mapper.ServicePackageMapper;
import com.pet.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
public class InstitutionService {
    private final InstitutionMapper institutionMapper;
    private final ServicePackageMapper servicePackageMapper;
    private final ReviewMapper reviewMapper;
    private final UserMapper userMapper;
    private final BookingMapper bookingMapper;
    private final PetMapper petMapper;
    private final ObjectMapper objectMapper;
    private final GeocodingService geocodingService;

    @SneakyThrows
    public PageResult<Map<String, Object>> search(String keyword, String petType, BigDecimal minRating,
                                                   BigDecimal maxPrice, String sortBy, BigDecimal lat, BigDecimal lng,
                                                   int page, int pageSize) {
        LambdaQueryWrapper<Institution> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Institution::getStatus, "active");
        
        if (StringUtils.hasText(keyword)) {
            wrapper.and(w -> w.like(Institution::getName, keyword)
                    .or().like(Institution::getDescription, keyword)
                    .or().like(Institution::getAddress, keyword));
        }
        
        if (StringUtils.hasText(petType)) {
            wrapper.like(Institution::getPetTypes, petType);
        }
        
        if (minRating != null) {
            wrapper.ge(Institution::getRating, minRating);
        }
        
        if ("rating".equals(sortBy)) {
            wrapper.orderByDesc(Institution::getRating);
        } else if ("reviewCount".equals(sortBy)) {
            wrapper.orderByDesc(Institution::getReviewCount);
        } else {
            wrapper.orderByDesc(Institution::getRating);
        }
        
        Page<Institution> pageResult = institutionMapper.selectPage(new Page<>(page, pageSize), wrapper);
        
        List<Map<String, Object>> list = new ArrayList<>();
        for (Institution inst : pageResult.getRecords()) {
            Map<String, Object> vo = toInstitutionVO(inst);
            
            // 计算距离
            if (lat != null && lng != null) {
                double distance = calculateDistance(lat.doubleValue(), lng.doubleValue(),
                        inst.getLatitude().doubleValue(), inst.getLongitude().doubleValue());
                vo.put("distance", Math.round(distance * 10) / 10.0);
            }
            
            // 获取最低价格
            List<ServicePackage> packages = servicePackageMapper.selectList(
                    new LambdaQueryWrapper<ServicePackage>()
                            .eq(ServicePackage::getInstitutionId, inst.getId())
                            .eq(ServicePackage::getIsActive, true));
            if (!packages.isEmpty()) {
                BigDecimal minPrice = packages.stream()
                        .map(ServicePackage::getPricePerDay)
                        .min(BigDecimal::compareTo)
                        .orElse(BigDecimal.ZERO);
                vo.put("minPrice", minPrice);
            }
            
            list.add(vo);
        }
        
        // 按价格筛选
        if (maxPrice != null) {
            list.removeIf(vo -> {
                Object minPriceObj = vo.get("minPrice");
                if (minPriceObj == null) return true;
                BigDecimal minPrice = minPriceObj instanceof BigDecimal ? 
                        (BigDecimal) minPriceObj : new BigDecimal(minPriceObj.toString());
                return minPrice.compareTo(maxPrice) > 0;
            });
        }
        
        // 按距离排序
        if ("distance".equals(sortBy) && lat != null && lng != null) {
            list.sort((a, b) -> {
                Double distA = (Double) a.get("distance");
                Double distB = (Double) b.get("distance");
                if (distA == null) return 1;
                if (distB == null) return -1;
                return distA.compareTo(distB);
            });
        }
        
        // 按价格排序
        if ("price".equals(sortBy)) {
            list.sort((a, b) -> {
                Object priceA = a.get("minPrice");
                Object priceB = b.get("minPrice");
                if (priceA == null) return 1;
                if (priceB == null) return -1;
                BigDecimal pa = priceA instanceof BigDecimal ? (BigDecimal) priceA : new BigDecimal(priceA.toString());
                BigDecimal pb = priceB instanceof BigDecimal ? (BigDecimal) priceB : new BigDecimal(priceB.toString());
                return pa.compareTo(pb);
            });
        }
        
        return PageResult.of(list, page, pageSize, pageResult.getTotal());
    }

    @SneakyThrows
    public Map<String, Object> getDetail(String id) {
        Institution inst = institutionMapper.selectById(id);
        if (inst == null) {
            throw new RuntimeException("机构不存在");
        }
        
        Map<String, Object> vo = toInstitutionVO(inst);
        
        // 获取服务套餐
        List<ServicePackage> packages = servicePackageMapper.selectList(
                new LambdaQueryWrapper<ServicePackage>()
                        .eq(ServicePackage::getInstitutionId, id)
                        .eq(ServicePackage::getIsActive, true));
        List<Map<String, Object>> packageVOs = new ArrayList<>();
        for (ServicePackage pkg : packages) {
            packageVOs.add(toPackageVO(pkg));
        }
        vo.put("servicePackages", packageVOs);
        
        // 获取最近评价
        List<Review> reviews = reviewMapper.selectList(
                new LambdaQueryWrapper<Review>()
                        .eq(Review::getInstitutionId, id)
                        .orderByDesc(Review::getCreatedAt)
                        .last("LIMIT 5"));
        List<Map<String, Object>> reviewVOs = new ArrayList<>();
        for (Review review : reviews) {
            reviewVOs.add(toReviewVO(review));
        }
        vo.put("recentReviews", reviewVOs);
        
        return vo;
    }

    public List<Map<String, Object>> getPackages(String institutionId) {
        List<ServicePackage> packages = servicePackageMapper.selectList(
                new LambdaQueryWrapper<ServicePackage>()
                        .eq(ServicePackage::getInstitutionId, institutionId)
                        .eq(ServicePackage::getIsActive, true));
        List<Map<String, Object>> result = new ArrayList<>();
        for (ServicePackage pkg : packages) {
            result.add(toPackageVO(pkg));
        }
        return result;
    }

    public ServicePackage getPackageById(String id) {
        return servicePackageMapper.selectById(id);
    }

    public Institution getById(String id) {
        return institutionMapper.selectById(id);
    }

    @SneakyThrows
    private Map<String, Object> toInstitutionVO(Institution inst) {
        Map<String, Object> vo = new HashMap<>();
        vo.put("id", inst.getId());
        vo.put("name", inst.getName());
        vo.put("description", inst.getDescription());
        vo.put("logo", inst.getLogo());
        vo.put("images", parseJsonArray(inst.getImages()));
        vo.put("address", inst.getAddress());
        vo.put("latitude", inst.getLatitude());
        vo.put("longitude", inst.getLongitude());
        vo.put("phone", inst.getPhone());
        vo.put("email", inst.getEmail());
        vo.put("businessHours", parseJsonObject(inst.getBusinessHours()));
        vo.put("petTypes", parseJsonArray(inst.getPetTypes()));
        vo.put("capacity", parseJsonObject(inst.getCapacity()));
        vo.put("currentOccupancy", parseJsonObject(inst.getCurrentOccupancy()));
        vo.put("rating", inst.getRating());
        vo.put("reviewCount", inst.getReviewCount());
        vo.put("verified", inst.getVerified());
        vo.put("status", inst.getStatus());
        vo.put("features", parseJsonArray(inst.getFeatures()));
        vo.put("licenses", parseJsonArray(inst.getLicenses()));
        vo.put("createdAt", inst.getCreatedAt());
        vo.put("updatedAt", inst.getUpdatedAt());
        return vo;
    }

    @SneakyThrows
    private Map<String, Object> toPackageVO(ServicePackage pkg) {
        Map<String, Object> vo = new HashMap<>();
        vo.put("id", pkg.getId());
        vo.put("institutionId", pkg.getInstitutionId());
        vo.put("name", pkg.getName());
        vo.put("description", pkg.getDescription());
        vo.put("petTypes", parseJsonArray(pkg.getPetTypes()));
        vo.put("pricePerDay", pkg.getPricePerDay());
        vo.put("features", parseJsonArray(pkg.getFeatures()));
        vo.put("maxWeight", pkg.getMaxWeight());
        vo.put("isActive", pkg.getIsActive());
        return vo;
    }

    @SneakyThrows
    private Map<String, Object> toReviewVO(Review review) {
        Map<String, Object> vo = new HashMap<>();
        vo.put("id", review.getId());
        vo.put("bookingId", review.getBookingId());
        vo.put("userId", review.getUserId());
        vo.put("institutionId", review.getInstitutionId());
        vo.put("rating", parseJsonObject(review.getRating()));
        vo.put("content", review.getContent());
        vo.put("images", parseJsonArray(review.getImages()));
        vo.put("reply", parseJsonObject(review.getReply()));
        vo.put("isAnonymous", review.getIsAnonymous());
        vo.put("createdAt", review.getCreatedAt());
        vo.put("updatedAt", review.getUpdatedAt());
        return vo;
    }

    @SneakyThrows
    private List<Object> parseJsonArray(String json) {
        if (!StringUtils.hasText(json)) return new ArrayList<>();
        return objectMapper.readValue(json, new TypeReference<List<Object>>() {});
    }

    @SneakyThrows
    private Map<String, Object> parseJsonObject(String json) {
        if (!StringUtils.hasText(json)) return new HashMap<>();
        return objectMapper.readValue(json, new TypeReference<Map<String, Object>>() {});
    }

    private double calculateDistance(double lat1, double lng1, double lat2, double lng2) {
        double R = 6371; // 地球半径（公里）
        double dLat = Math.toRadians(lat2 - lat1);
        double dLng = Math.toRadians(lng2 - lng1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                        Math.sin(dLng / 2) * Math.sin(dLng / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return R * c;
    }

    // ========== 机构管理端方法 ==========

    private String getInstitutionIdByStaff(String staffUserId) {
        User staff = userMapper.selectById(staffUserId);
        if (staff == null || staff.getInstitutionId() == null) {
            throw new RuntimeException("用户不是机构员工");
        }
        return staff.getInstitutionId();
    }

    public List<Map<String, Object>> getPackagesByStaff(String staffUserId) {
        String institutionId = getInstitutionIdByStaff(staffUserId);
        List<ServicePackage> packages = servicePackageMapper.selectList(
                new LambdaQueryWrapper<ServicePackage>()
                        .eq(ServicePackage::getInstitutionId, institutionId));
        List<Map<String, Object>> result = new ArrayList<>();
        for (ServicePackage pkg : packages) {
            result.add(toPackageVO(pkg));
        }
        return result;
    }

    @SneakyThrows
    public Map<String, Object> createPackage(String staffUserId, Map<String, Object> data) {
        String institutionId = getInstitutionIdByStaff(staffUserId);
        
        ServicePackage pkg = new ServicePackage();
        pkg.setInstitutionId(institutionId);
        pkg.setName((String) data.get("name"));
        pkg.setDescription((String) data.get("description"));
        pkg.setPricePerDay(new BigDecimal(data.get("pricePerDay").toString()));
        pkg.setFeatures(objectMapper.writeValueAsString(data.get("features")));
        pkg.setPetTypes(objectMapper.writeValueAsString(data.get("petTypes")));
        if (data.get("maxWeight") != null) {
            pkg.setMaxWeight(new BigDecimal(data.get("maxWeight").toString()));
        }
        pkg.setIsActive(true);
        
        servicePackageMapper.insert(pkg);
        return toPackageVO(pkg);
    }

    @SneakyThrows
    public Map<String, Object> updatePackage(String id, String staffUserId, Map<String, Object> data) {
        ServicePackage pkg = servicePackageMapper.selectById(id);
        if (pkg == null) {
            throw new RuntimeException("套餐不存在");
        }
        
        if (data.containsKey("name")) pkg.setName((String) data.get("name"));
        if (data.containsKey("description")) pkg.setDescription((String) data.get("description"));
        if (data.containsKey("pricePerDay")) pkg.setPricePerDay(new BigDecimal(data.get("pricePerDay").toString()));
        if (data.containsKey("features")) pkg.setFeatures(objectMapper.writeValueAsString(data.get("features")));
        if (data.containsKey("petTypes")) pkg.setPetTypes(objectMapper.writeValueAsString(data.get("petTypes")));
        if (data.containsKey("maxWeight")) pkg.setMaxWeight(new BigDecimal(data.get("maxWeight").toString()));
        
        servicePackageMapper.updateById(pkg);
        return toPackageVO(pkg);
    }

    public void updatePackageStatus(String id, String staffUserId, Boolean isActive) {
        ServicePackage pkg = servicePackageMapper.selectById(id);
        if (pkg == null) {
            throw new RuntimeException("套餐不存在");
        }
        pkg.setIsActive(isActive);
        servicePackageMapper.updateById(pkg);
    }

    public void deletePackage(String id, String staffUserId) {
        servicePackageMapper.deleteById(id);
    }

    @SneakyThrows
    public Map<String, Object> getSettings(String staffUserId) {
        String institutionId = getInstitutionIdByStaff(staffUserId);
        Institution inst = institutionMapper.selectById(institutionId);
        if (inst == null) {
            return new HashMap<>();
        }
        
        Map<String, Object> result = new HashMap<>();
        
        // 基本信息
        Map<String, Object> basicInfo = new HashMap<>();
        basicInfo.put("name", inst.getName());
        basicInfo.put("logo", inst.getLogo());
        basicInfo.put("description", inst.getDescription());
        basicInfo.put("address", inst.getAddress());
        basicInfo.put("phone", inst.getPhone());
        basicInfo.put("email", inst.getEmail());
        basicInfo.put("businessHours", parseJsonObject(inst.getBusinessHours()));
        result.put("basicInfo", basicInfo);
        
        // 服务设置
        Map<String, Object> serviceSettings = new HashMap<>();
        serviceSettings.put("acceptedPets", parseJsonArray(inst.getPetTypes()));
        serviceSettings.put("capacity", parseJsonObject(inst.getCapacity()));
        result.put("serviceSettings", serviceSettings);
        
        return result;
    }

    @SneakyThrows
    public Map<String, Object> updateSettings(String staffUserId, Map<String, Object> data) {
        String institutionId = getInstitutionIdByStaff(staffUserId);
        Institution inst = institutionMapper.selectById(institutionId);
        if (inst == null) {
            throw new RuntimeException("机构不存在");
        }
        
        Map<String, Object> basicInfo = (Map<String, Object>) data.get("basicInfo");
        if (basicInfo != null) {
            if (basicInfo.containsKey("name")) inst.setName((String) basicInfo.get("name"));
            if (basicInfo.containsKey("description")) inst.setDescription((String) basicInfo.get("description"));
            if (basicInfo.containsKey("address")) inst.setAddress((String) basicInfo.get("address"));
            if (basicInfo.containsKey("phone")) inst.setPhone((String) basicInfo.get("phone"));
            if (basicInfo.containsKey("email")) inst.setEmail((String) basicInfo.get("email"));
            if (basicInfo.get("latitude") != null) {
                inst.setLatitude(new BigDecimal(basicInfo.get("latitude").toString()));
            }
            if (basicInfo.get("longitude") != null) {
                inst.setLongitude(new BigDecimal(basicInfo.get("longitude").toString()));
            }
            if (basicInfo.containsKey("businessHours")) {
                inst.setBusinessHours(objectMapper.writeValueAsString(basicInfo.get("businessHours")));
            }
            
            // 如果地址有值，自动进行地理编码更新经纬度
            String newAddress = (String) basicInfo.get("address");
            System.out.println("=== updateSettings: newAddress = " + newAddress);
            if (newAddress != null && !newAddress.isEmpty()) {
                System.out.println("=== Calling geocode for: " + newAddress);
                BigDecimal[] coords = geocodingService.geocode(newAddress);
                System.out.println("=== Geocode result: " + (coords != null ? coords[0] + "," + coords[1] : "null"));
                if (coords != null) {
                    inst.setLongitude(coords[0]);
                    inst.setLatitude(coords[1]);
                }
            }
        }
        
        Map<String, Object> serviceSettings = (Map<String, Object>) data.get("serviceSettings");
        if (serviceSettings != null) {
            if (serviceSettings.containsKey("acceptedPets")) {
                inst.setPetTypes(objectMapper.writeValueAsString(serviceSettings.get("acceptedPets")));
            }
            if (serviceSettings.containsKey("capacity")) {
                inst.setCapacity(objectMapper.writeValueAsString(serviceSettings.get("capacity")));
            }
        }
        
        institutionMapper.updateById(inst);
        return getSettings(staffUserId);
    }

    // ========== 仪表盘统计方法 ==========

    public Map<String, Object> getDashboardStats(String staffUserId) {
        String institutionId = getInstitutionIdByStaff(staffUserId);
        Institution inst = institutionMapper.selectById(institutionId);
        
        Map<String, Object> stats = new HashMap<>();
        
        // 查询订单统计
        List<Booking> allBookings = bookingMapper.selectList(
                new LambdaQueryWrapper<Booking>().eq(Booking::getInstitutionId, institutionId));
        
        int totalOrders = allBookings.size();
        int completedOrders = (int) allBookings.stream().filter(b -> "completed".equals(b.getStatus())).count();
        int cancelledOrders = (int) allBookings.stream().filter(b -> "cancelled".equals(b.getStatus())).count();
        int inProgressOrders = (int) allBookings.stream().filter(b -> "in_progress".equals(b.getStatus())).count();
        int pendingOrders = (int) allBookings.stream().filter(b -> "pending".equals(b.getStatus())).count();
        
        // 本月收入
        LocalDate firstDayOfMonth = LocalDate.now().withDayOfMonth(1);
        BigDecimal monthlyRevenue = allBookings.stream()
                .filter(b -> "paid".equals(b.getPaymentStatus()) && b.getPaidAt() != null 
                        && b.getPaidAt().toLocalDate().isAfter(firstDayOfMonth.minusDays(1)))
                .map(Booking::getTotalPrice)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        // 今日入住/离店
        LocalDate today = LocalDate.now();
        int todayCheckIn = (int) allBookings.stream()
                .filter(b -> b.getStartDate() != null && b.getStartDate().equals(today) 
                        && ("confirmed".equals(b.getStatus()) || "in_progress".equals(b.getStatus())))
                .count();
        int todayCheckOut = (int) allBookings.stream()
                .filter(b -> b.getEndDate() != null && b.getEndDate().equals(today) 
                        && "in_progress".equals(b.getStatus()))
                .count();
        
        stats.put("totalOrders", totalOrders);
        stats.put("monthlyRevenue", monthlyRevenue);
        stats.put("averageRating", inst != null ? inst.getRating() : BigDecimal.ZERO);
        stats.put("completedOrders", completedOrders);
        stats.put("cancelledOrders", cancelledOrders);
        stats.put("pendingOrders", pendingOrders);
        stats.put("inProgressOrders", inProgressOrders);
        stats.put("todayCheckIn", todayCheckIn);
        stats.put("todayCheckOut", todayCheckOut);
        stats.put("ordersTrend", 5);
        stats.put("revenueTrend", 8);
        stats.put("occupancyTrend", 3);
        stats.put("ratingTrend", 0);
        
        // 入住率
        int occupancyRate = totalOrders > 0 ? (inProgressOrders * 100 / Math.max(1, totalOrders)) : 0;
        stats.put("occupancyRate", Math.min(occupancyRate, 100));
        
        // 房间状态
        Map<String, Object> roomStatus = new HashMap<>();
        int totalRooms = 30;
        if (inst != null) {
            Map<String, Object> capacity = parseJsonObject(inst.getCapacity());
            int total = capacity.values().stream()
                    .mapToInt(v -> v instanceof Number ? ((Number) v).intValue() : 0)
                    .sum();
            if (total > 0) totalRooms = total;
        }
        roomStatus.put("total", totalRooms);
        roomStatus.put("occupied", inProgressOrders);
        roomStatus.put("available", Math.max(0, totalRooms - inProgressOrders));
        roomStatus.put("maintenance", 0);
        stats.put("roomStatus", roomStatus);
        
        // ========== 图表数据 ==========
        
        // 收入趋势（近6个月）
        List<Map<String, Object>> revenueTrendData = new ArrayList<>();
        for (int i = 5; i >= 0; i--) {
            LocalDate monthStart = LocalDate.now().minusMonths(i).withDayOfMonth(1);
            LocalDate monthEnd = monthStart.plusMonths(1).minusDays(1);
            final LocalDate ms = monthStart;
            final LocalDate me = monthEnd;
            BigDecimal monthRevenue = allBookings.stream()
                    .filter(b -> "paid".equals(b.getPaymentStatus()) && b.getPaidAt() != null)
                    .filter(b -> {
                        LocalDate paidDate = b.getPaidAt().toLocalDate();
                        return !paidDate.isBefore(ms) && !paidDate.isAfter(me);
                    })
                    .map(Booking::getTotalPrice)
                    .filter(Objects::nonNull)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            Map<String, Object> point = new HashMap<>();
            point.put("name", monthStart.getMonthValue() + "月");
            point.put("value", monthRevenue);
            revenueTrendData.add(point);
        }
        stats.put("revenueTrendData", revenueTrendData);
        
        // 服务分布（按套餐统计）
        Map<String, Long> packageCount = new HashMap<>();
        for (Booking booking : allBookings) {
            if (booking.getServicePackageId() != null) {
                ServicePackage pkg = servicePackageMapper.selectById(booking.getServicePackageId());
                if (pkg != null) {
                    packageCount.merge(pkg.getName(), 1L, Long::sum);
                }
            }
        }
        List<Map<String, Object>> serviceDistributionData = new ArrayList<>();
        packageCount.forEach((name, count) -> {
            Map<String, Object> item = new HashMap<>();
            item.put("name", name);
            item.put("value", count);
            serviceDistributionData.add(item);
        });
        if (serviceDistributionData.isEmpty()) {
            Map<String, Object> emptyItem = new HashMap<>();
            emptyItem.put("name", "暂无数据");
            emptyItem.put("value", 0);
            serviceDistributionData.add(emptyItem);
        }
        stats.put("serviceDistributionData", serviceDistributionData);
        
        // 每周订单（按星期统计）
        String[] weekDays = {"周一", "周二", "周三", "周四", "周五", "周六", "周日"};
        Map<Integer, Long> weeklyCount = new HashMap<>();
        for (int i = 1; i <= 7; i++) weeklyCount.put(i, 0L);
        for (Booking booking : allBookings) {
            if (booking.getCreatedAt() != null) {
                int dayOfWeek = booking.getCreatedAt().getDayOfWeek().getValue();
                weeklyCount.merge(dayOfWeek, 1L, Long::sum);
            }
        }
        List<Map<String, Object>> weeklyOrdersData = new ArrayList<>();
        for (int i = 1; i <= 7; i++) {
            Map<String, Object> item = new HashMap<>();
            item.put("name", weekDays[i - 1]);
            item.put("value", weeklyCount.get(i));
            weeklyOrdersData.add(item);
        }
        stats.put("weeklyOrdersData", weeklyOrdersData);
        
        // 宠物类型分布
        Map<String, Long> petTypeCount = new HashMap<>();
        for (Booking booking : allBookings) {
            if (booking.getPetId() != null) {
                Pet pet = petMapper.selectById(booking.getPetId());
                if (pet != null) {
                    String species = pet.getSpecies() != null ? pet.getSpecies() : "other";
                    petTypeCount.merge(species, 1L, Long::sum);
                }
            }
        }
        List<Map<String, Object>> petTypeDistributionData = new ArrayList<>();
        long totalPetCount = petTypeCount.values().stream().mapToLong(Long::longValue).sum();
        petTypeCount.forEach((type, count) -> {
            Map<String, Object> item = new HashMap<>();
            item.put("name", "dog".equals(type) ? "狗狗" : "cat".equals(type) ? "猫咪" : "其他");
            item.put("value", totalPetCount > 0 ? Math.round(count * 100.0 / totalPetCount) : 0);
            petTypeDistributionData.add(item);
        });
        if (petTypeDistributionData.isEmpty()) {
            Map<String, Object> emptyItem = new HashMap<>();
            emptyItem.put("name", "暂无数据");
            emptyItem.put("value", 0);
            petTypeDistributionData.add(emptyItem);
        }
        stats.put("petTypeDistributionData", petTypeDistributionData);
        
        return stats;
    }

    public List<Map<String, Object>> getRecentOrders(String staffUserId, int limit) {
        String institutionId = getInstitutionIdByStaff(staffUserId);
        List<Booking> bookings = bookingMapper.selectList(
                new LambdaQueryWrapper<Booking>()
                        .eq(Booking::getInstitutionId, institutionId)
                        .orderByDesc(Booking::getCreatedAt)
                        .last("LIMIT " + limit));
        
        List<Map<String, Object>> result = new ArrayList<>();
        for (Booking booking : bookings) {
            Map<String, Object> vo = new HashMap<>();
            vo.put("id", booking.getId());
            vo.put("orderNumber", booking.getOrderNumber());
            vo.put("status", booking.getStatus());
            vo.put("totalPrice", booking.getTotalPrice());
            vo.put("startDate", booking.getStartDate());
            vo.put("endDate", booking.getEndDate());
            vo.put("createdAt", booking.getCreatedAt());
            
            // 添加宠物信息
            if (booking.getPetId() != null) {
                Pet pet = petMapper.selectById(booking.getPetId());
                if (pet != null) {
                    vo.put("petName", pet.getName());
                    vo.put("petSpecies", pet.getSpecies());
                }
            }
            
            // 添加用户信息
            if (booking.getUserId() != null) {
                User user = userMapper.selectById(booking.getUserId());
                if (user != null) {
                    vo.put("userName", user.getName());
                    vo.put("userPhone", user.getPhone());
                }
            }
            
            // 添加套餐信息
            if (booking.getServicePackageId() != null) {
                ServicePackage pkg = servicePackageMapper.selectById(booking.getServicePackageId());
                if (pkg != null) {
                    vo.put("packageName", pkg.getName());
                }
            }
            
            result.add(vo);
        }
        return result;
    }

    // 获取报表数据
    public Map<String, Object> getReportData(String staffUserId, String period) {
        String institutionId = getInstitutionIdByStaff(staffUserId);
        
        // 确定时间范围
        LocalDate endDate = LocalDate.now();
        LocalDate startDate;
        switch (period) {
            case "week": startDate = endDate.minusWeeks(1); break;
            case "quarter": startDate = endDate.minusMonths(3); break;
            case "year": startDate = endDate.minusYears(1); break;
            default: startDate = endDate.minusMonths(1); break;
        }
        
        // 查询时间范围内的订单
        List<Booking> bookings = bookingMapper.selectList(
                new LambdaQueryWrapper<Booking>()
                        .eq(Booking::getInstitutionId, institutionId)
                        .ge(Booking::getCreatedAt, startDate.atStartOfDay())
                        .le(Booking::getCreatedAt, endDate.plusDays(1).atStartOfDay()));
        
        Map<String, Object> result = new HashMap<>();
        
        // 核心指标
        int totalOrders = bookings.size();
        BigDecimal totalRevenue = bookings.stream()
                .filter(b -> "paid".equals(b.getPaymentStatus()))
                .map(Booking::getTotalPrice)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal avgOrderValue = totalOrders > 0 ? 
                totalRevenue.divide(BigDecimal.valueOf(totalOrders), 2, BigDecimal.ROUND_HALF_UP) : BigDecimal.ZERO;
        int inProgressCount = (int) bookings.stream().filter(b -> "in_progress".equals(b.getStatus())).count();
        double occupancyRate = totalOrders > 0 ? (inProgressCount * 100.0 / totalOrders) : 0;
        
        Map<String, Object> coreMetrics = new HashMap<>();
        coreMetrics.put("totalOrders", totalOrders);
        coreMetrics.put("ordersTrend", 5.0);
        coreMetrics.put("totalRevenue", totalRevenue);
        coreMetrics.put("revenueTrend", 8.0);
        coreMetrics.put("avgOrderValue", avgOrderValue);
        coreMetrics.put("occupancyRate", Math.round(occupancyRate * 10) / 10.0);
        result.put("coreMetrics", coreMetrics);
        
        // 订单趋势（按日期分组）
        Map<LocalDate, Long> ordersByDate = bookings.stream()
                .collect(java.util.stream.Collectors.groupingBy(
                        b -> b.getCreatedAt().toLocalDate(),
                        java.util.stream.Collectors.counting()));
        List<Map<String, Object>> orderTrend = new ArrayList<>();
        for (LocalDate date = startDate; !date.isAfter(endDate); date = date.plusDays(1)) {
            Map<String, Object> point = new HashMap<>();
            point.put("name", date.getMonthValue() + "月" + date.getDayOfMonth() + "日");
            point.put("value", ordersByDate.getOrDefault(date, 0L));
            orderTrend.add(point);
        }
        result.put("orderTrend", orderTrend);
        
        // 收入趋势
        Map<LocalDate, BigDecimal> revenueByDate = bookings.stream()
                .filter(b -> "paid".equals(b.getPaymentStatus()) && b.getPaidAt() != null)
                .collect(java.util.stream.Collectors.groupingBy(
                        b -> b.getPaidAt().toLocalDate(),
                        java.util.stream.Collectors.reducing(BigDecimal.ZERO, Booking::getTotalPrice, BigDecimal::add)));
        List<Map<String, Object>> revenueTrend = new ArrayList<>();
        for (LocalDate date = startDate; !date.isAfter(endDate); date = date.plusDays(1)) {
            Map<String, Object> point = new HashMap<>();
            point.put("name", date.getMonthValue() + "月" + date.getDayOfMonth() + "日");
            point.put("value", revenueByDate.getOrDefault(date, BigDecimal.ZERO));
            revenueTrend.add(point);
        }
        result.put("revenueTrend", revenueTrend);
        
        // 宠物类型分布
        Map<String, Long> petTypeCount = new HashMap<>();
        for (Booking booking : bookings) {
            if (booking.getPetId() != null) {
                com.pet.entity.Pet pet = petMapper.selectById(booking.getPetId());
                if (pet != null) {
                    String species = pet.getSpecies() != null ? pet.getSpecies() : "other";
                    petTypeCount.merge(species, 1L, Long::sum);
                }
            }
        }
        List<Map<String, Object>> petTypeDistribution = new ArrayList<>();
        petTypeCount.forEach((type, count) -> {
            Map<String, Object> item = new HashMap<>();
            item.put("name", "dog".equals(type) ? "狗狗" : "cat".equals(type) ? "猫咪" : "其他");
            item.put("value", count);
            petTypeDistribution.add(item);
        });
        if (petTypeDistribution.isEmpty()) {
            Map<String, Object> emptyItem = new HashMap<>();
            emptyItem.put("name", "暂无数据");
            emptyItem.put("value", 0);
            petTypeDistribution.add(emptyItem);
        }
        result.put("petTypeDistribution", petTypeDistribution);
        
        // 套餐销售排名
        Map<String, Long> packageCount = new HashMap<>();
        for (Booking booking : bookings) {
            if (booking.getServicePackageId() != null) {
                ServicePackage pkg = servicePackageMapper.selectById(booking.getServicePackageId());
                if (pkg != null) {
                    packageCount.merge(pkg.getName(), 1L, Long::sum);
                }
            }
        }
        List<Map<String, Object>> packageRanking = packageCount.entrySet().stream()
                .sorted((a, b) -> Long.compare(b.getValue(), a.getValue()))
                .limit(5)
                .map(e -> {
                    Map<String, Object> item = new HashMap<>();
                    item.put("name", e.getKey());
                    item.put("value", e.getValue());
                    return item;
                })
                .collect(java.util.stream.Collectors.toList());
        if (packageRanking.isEmpty()) {
            Map<String, Object> emptyItem = new HashMap<>();
            emptyItem.put("name", "暂无数据");
            emptyItem.put("value", 0);
            packageRanking.add(emptyItem);
        }
        result.put("packageRanking", packageRanking);
        
        return result;
    }

    public List<Map<String, Object>> getTodayCheckIn(String staffUserId) {
        String institutionId = getInstitutionIdByStaff(staffUserId);
        LocalDate today = LocalDate.now();
        
        // 查询今日入住的订单（开始日期是今天，状态是confirmed或in_progress）
        List<Booking> bookings = bookingMapper.selectList(
                new LambdaQueryWrapper<Booking>()
                        .eq(Booking::getInstitutionId, institutionId)
                        .eq(Booking::getStartDate, today)
                        .in(Booking::getStatus, "confirmed", "in_progress"));
        
        List<Map<String, Object>> result = new ArrayList<>();
        for (Booking booking : bookings) {
            Map<String, Object> item = new HashMap<>();
            item.put("id", booking.getId());
            item.put("checkIn", "14:00"); // 默认入住时间
            item.put("status", "in_progress".equals(booking.getStatus()) ? "checked_in" : "waiting");
            
            // 宠物信息
            if (booking.getPetId() != null) {
                Pet pet = petMapper.selectById(booking.getPetId());
                if (pet != null) {
                    item.put("name", pet.getName());
                    item.put("type", pet.getBreed() != null ? pet.getBreed() : (pet.getSpecies() != null ? pet.getSpecies() : ""));
                    item.put("avatar", "cat".equals(pet.getSpecies()) ? "🐱" : "🐕");
                }
            }
            
            // 用户信息
            if (booking.getUserId() != null) {
                User user = userMapper.selectById(booking.getUserId());
                if (user != null) {
                    item.put("owner", user.getName());
                    item.put("phone", user.getPhone());
                }
            }
            
            item.put("room", "-");
            result.add(item);
        }
        return result;
    }

    public List<Map<String, Object>> getTodayCheckOut(String staffUserId) {
        String institutionId = getInstitutionIdByStaff(staffUserId);
        LocalDate today = LocalDate.now();
        
        // 查询今日离店的订单（结束日期是今天，状态是in_progress）
        List<Booking> bookings = bookingMapper.selectList(
                new LambdaQueryWrapper<Booking>()
                        .eq(Booking::getInstitutionId, institutionId)
                        .eq(Booking::getEndDate, today)
                        .eq(Booking::getStatus, "in_progress"));
        
        List<Map<String, Object>> result = new ArrayList<>();
        for (Booking booking : bookings) {
            Map<String, Object> item = new HashMap<>();
            item.put("id", booking.getId());
            item.put("checkOut", "12:00"); // 默认离店时间
            item.put("status", "pending"); // 待离店
            
            // 宠物信息
            if (booking.getPetId() != null) {
                Pet pet = petMapper.selectById(booking.getPetId());
                if (pet != null) {
                    item.put("name", pet.getName());
                    item.put("type", pet.getBreed() != null ? pet.getBreed() : (pet.getSpecies() != null ? pet.getSpecies() : ""));
                    item.put("avatar", "cat".equals(pet.getSpecies()) ? "🐱" : "🐕");
                }
            }
            
            // 用户信息
            if (booking.getUserId() != null) {
                User user = userMapper.selectById(booking.getUserId());
                if (user != null) {
                    item.put("owner", user.getName());
                    item.put("phone", user.getPhone());
                }
            }
            
            item.put("room", "-");
            result.add(item);
        }
        return result;
    }

    public PageResult<Map<String, Object>> getCustomers(String staffUserId, int page, int pageSize) {
        String institutionId = getInstitutionIdByStaff(staffUserId);
        
        // 从订单中获取所有客户（去重）
        List<Booking> allBookings = bookingMapper.selectList(
                new LambdaQueryWrapper<Booking>()
                        .eq(Booking::getInstitutionId, institutionId)
                        .orderByDesc(Booking::getCreatedAt));
        
        // 按用户ID分组统计
        Map<String, List<Booking>> bookingsByUser = new HashMap<>();
        for (Booking booking : allBookings) {
            if (booking.getUserId() != null) {
                bookingsByUser.computeIfAbsent(booking.getUserId(), k -> new ArrayList<>()).add(booking);
            }
        }
        
        List<Map<String, Object>> customers = new ArrayList<>();
        for (Map.Entry<String, List<Booking>> entry : bookingsByUser.entrySet()) {
            String userId = entry.getKey();
            List<Booking> userBookings = entry.getValue();
            
            User user = userMapper.selectById(userId);
            if (user == null) continue;
            
            Map<String, Object> customer = new HashMap<>();
            customer.put("id", user.getId());
            customer.put("name", user.getName());
            customer.put("phone", user.getPhone());
            customer.put("email", user.getEmail());
            customer.put("avatar", user.getAvatar());
            
            // 统计订单数和消费金额
            int orderCount = userBookings.size();
            BigDecimal totalSpent = userBookings.stream()
                    .filter(b -> "paid".equals(b.getPaymentStatus()))
                    .map(Booking::getTotalPrice)
                    .filter(Objects::nonNull)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            
            customer.put("orderCount", orderCount);
            customer.put("totalSpent", totalSpent);
            
            // 最近一次订单时间
            Booking lastBooking = userBookings.get(0);
            customer.put("lastOrderDate", lastBooking.getCreatedAt());
            customer.put("lastOrderStatus", lastBooking.getStatus());
            
            // 获取用户的宠物信息
            List<Pet> pets = petMapper.selectList(
                    new LambdaQueryWrapper<Pet>().eq(Pet::getUserId, userId));
            List<Map<String, Object>> petList = new ArrayList<>();
            for (Pet pet : pets) {
                Map<String, Object> petInfo = new HashMap<>();
                petInfo.put("id", pet.getId());
                petInfo.put("name", pet.getName());
                petInfo.put("species", pet.getSpecies());
                petInfo.put("breed", pet.getBreed());
                petList.add(petInfo);
            }
            customer.put("pets", petList);
            
            customers.add(customer);
        }
        
        // 分页
        int total = customers.size();
        int start = (page - 1) * pageSize;
        int end = Math.min(start + pageSize, total);
        List<Map<String, Object>> pagedList = start < total ? customers.subList(start, end) : new ArrayList<>();
        
        return PageResult.of(pagedList, page, pageSize, total);
    }

    // ========== 机构申请/资料 ==========

    public Map<String, Object> getInstitutionProfile(String staffUserId) {
        User staff = userMapper.selectById(staffUserId);
        if (staff == null) {
            throw new RuntimeException("用户不存在");
        }
        
        Map<String, Object> result = new HashMap<>();
        result.put("hasInstitution", staff.getInstitutionId() != null);
        
        if (staff.getInstitutionId() != null) {
            Institution inst = institutionMapper.selectById(staff.getInstitutionId());
            if (inst != null) {
                result.put("institution", toInstitutionVO(inst));
                result.put("status", inst.getStatus());
            }
        }
        
        return result;
    }

    @SneakyThrows
    public Map<String, Object> applyInstitution(String staffUserId, Map<String, Object> data) {
        User staff = userMapper.selectById(staffUserId);
        if (staff == null) {
            throw new RuntimeException("用户不存在");
        }
        
        // 检查是否已有机构
        if (staff.getInstitutionId() != null) {
            Institution existing = institutionMapper.selectById(staff.getInstitutionId());
            if (existing != null && !"rejected".equals(existing.getStatus())) {
                throw new RuntimeException("您已有关联的机构");
            }
        }
        
        // 创建新机构
        Institution inst = new Institution();
        inst.setName((String) data.get("name"));
        inst.setDescription((String) data.get("description"));
        inst.setLogo((String) data.get("logo"));
        inst.setAddress((String) data.get("address"));
        inst.setPhone((String) data.get("phone"));
        inst.setEmail((String) data.get("email"));
        
        if (data.get("latitude") != null) {
            inst.setLatitude(new BigDecimal(data.get("latitude").toString()));
        }
        if (data.get("longitude") != null) {
            inst.setLongitude(new BigDecimal(data.get("longitude").toString()));
        }
        
        // 如果没有提供经纬度，自动进行地理编码
        String address = (String) data.get("address");
        if (address != null && !address.isEmpty() && 
            (data.get("latitude") == null || data.get("longitude") == null)) {
            BigDecimal[] coords = geocodingService.geocode(address);
            if (coords != null) {
                inst.setLongitude(coords[0]);
                inst.setLatitude(coords[1]);
            }
        }
        
        if (data.get("businessHours") != null) {
            inst.setBusinessHours(objectMapper.writeValueAsString(data.get("businessHours")));
        }
        if (data.get("petTypes") != null) {
            inst.setPetTypes(objectMapper.writeValueAsString(data.get("petTypes")));
        }
        if (data.get("licenses") != null) {
            inst.setLicenses(objectMapper.writeValueAsString(data.get("licenses")));
        }
        if (data.get("features") != null) {
            inst.setFeatures(objectMapper.writeValueAsString(data.get("features")));
        }
        
        inst.setStatus("pending"); // 待审核
        inst.setVerified(false);
        inst.setRating(BigDecimal.ZERO);
        inst.setReviewCount(0);
        
        institutionMapper.insert(inst);
        
        // 关联用户到机构
        staff.setInstitutionId(inst.getId());
        userMapper.updateById(staff);
        
        return toInstitutionVO(inst);
    }
}
