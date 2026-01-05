package com.pet.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pet.common.PageResult;
import com.pet.dto.CreateBookingRequest;
import com.pet.entity.Booking;
import com.pet.entity.Institution;
import com.pet.entity.Pet;
import com.pet.entity.ServicePackage;
import com.pet.entity.User;
import com.pet.mapper.BookingMapper;
import com.pet.mapper.InstitutionMapper;
import com.pet.mapper.PetMapper;
import com.pet.mapper.ServicePackageMapper;
import com.pet.mapper.UserMapper;
import com.pet.util.OrderNumberGenerator;
import com.pet.util.PriceCalculator;
import com.pet.util.RefundCalculator;
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
public class BookingService {
    private final BookingMapper bookingMapper;
    private final InstitutionService institutionService;
    private final InstitutionMapper institutionMapper;
    private final ServicePackageMapper servicePackageMapper;
    private final PetMapper petMapper;
    private final UserMapper userMapper;
    private final ObjectMapper objectMapper;
    private final WalletService walletService;
    private final NotificationService notificationService;

    @SneakyThrows
    public Map<String, Object> create(String userId, CreateBookingRequest request) {
        ServicePackage pkg = institutionService.getPackageById(request.getServicePackageId());
        if (pkg == null) {
            throw new RuntimeException("套餐不存在");
        }

        LocalDate startDate = LocalDate.parse(request.getStartDate());
        LocalDate endDate = LocalDate.parse(request.getEndDate());
        
        // 使用 PriceCalculator 计算价格
        Map<String, Object> priceResult = PriceCalculator.calculateBookingPriceByDates(
                pkg.getPricePerDay(), startDate, endDate);
        
        int totalDays = (int) priceResult.get("totalDays");
        BigDecimal basePrice = (BigDecimal) priceResult.get("subtotal");
        BigDecimal discount = (BigDecimal) priceResult.get("discount");
        BigDecimal totalPrice = (BigDecimal) priceResult.get("totalPrice");

        Booking booking = new Booking();
        // 使用 OrderNumberGenerator 生成订单号
        booking.setOrderNumber(OrderNumberGenerator.generateOrderNumber());
        booking.setUserId(userId);
        booking.setInstitutionId(request.getInstitutionId());
        booking.setServicePackageId(request.getServicePackageId());
        booking.setPetId(request.getPetId());
        booking.setStatus("pending");
        booking.setStartDate(startDate);
        booking.setEndDate(endDate);
        booking.setTotalDays(totalDays);
        booking.setBasePrice(basePrice);
        booking.setDiscount(discount);
        booking.setTotalPrice(totalPrice);
        booking.setPaymentStatus("pending");
        booking.setSpecialRequirements(request.getSpecialRequirements());
        booking.setEmergencyContact(objectMapper.writeValueAsString(request.getEmergencyContact()));

        bookingMapper.insert(booking);
        
        // 发送通知给用户
        Institution inst = institutionMapper.selectById(request.getInstitutionId());
        String instName = inst != null ? inst.getName() : "机构";
        notificationService.send(userId, "booking", "订单创建成功", 
            "您的订单 " + booking.getOrderNumber() + " 已创建，预约机构：" + instName,
            "/order/" + booking.getId());
        
        // 发送通知给机构
        List<User> staffList = userMapper.selectList(
            new LambdaQueryWrapper<User>().eq(User::getInstitutionId, request.getInstitutionId()));
        for (User staff : staffList) {
            notificationService.send(staff.getId(), "booking", "新订单通知",
                "收到新订单 " + booking.getOrderNumber() + "，请及时处理",
                "/institution/orders/" + booking.getId());
        }
        
        return toBookingVO(booking);
    }

    public PageResult<Map<String, Object>> getList(String userId, String status, int page, int pageSize) {
        LambdaQueryWrapper<Booking> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Booking::getUserId, userId);
        // 过滤用户已删除的订单
        wrapper.and(w -> w.isNull(Booking::getUserDeleted).or().eq(Booking::getUserDeleted, false));
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

    public Map<String, Object> getDetail(String id) {
        Booking booking = bookingMapper.selectById(id);
        if (booking == null) {
            throw new RuntimeException("订单不存在");
        }
        return toBookingVO(booking);
    }

    public Map<String, Object> cancel(String id, String reason) {
        Booking booking = bookingMapper.selectById(id);
        if (booking == null) {
            throw new RuntimeException("订单不存在");
        }
        
        // 使用 RefundCalculator 检查是否可以取消
        Map<String, Object> canCancelResult = RefundCalculator.canCancelOrder(
                booking.getStartDate(), booking.getStatus());
        if (!(boolean) canCancelResult.get("canCancel")) {
            throw new RuntimeException((String) canCancelResult.get("reason"));
        }

        BigDecimal refundAmount = BigDecimal.ZERO;
        if ("paid".equals(booking.getPaymentStatus())) {
            // 使用 RefundCalculator 计算退款金额
            Map<String, Object> refundResult = RefundCalculator.calculateRefund(
                    booking.getTotalPrice(),
                    booking.getStartDate(),
                    booking.getEndDate(),
                    LocalDateTime.now());
            
            refundAmount = (BigDecimal) refundResult.get("refundAmount");
            booking.setRefundAmount(refundAmount);
            booking.setRefundedAt(LocalDateTime.now());
            
            String refundType = (String) refundResult.get("type");
            if ("full".equals(refundType)) {
                booking.setPaymentStatus("refunded");
            } else if ("partial".equals(refundType)) {
                booking.setPaymentStatus("partial_refund");
            }
            
            // 退款到用户钱包
            if (refundAmount.compareTo(BigDecimal.ZERO) > 0) {
                int refundAmountInFen = refundAmount.multiply(new BigDecimal(100)).intValue();
                walletService.addIncome(booking.getUserId(), refundAmountInFen, 
                    "订单退款 - " + booking.getOrderNumber(), booking.getId());
            }
        }

        booking.setStatus("cancelled");
        booking.setCancelReason(reason);
        bookingMapper.updateById(booking);
        
        // 发送通知给用户
        String notifyContent = "您的订单 " + booking.getOrderNumber() + " 已取消";
        if (refundAmount.compareTo(BigDecimal.ZERO) > 0) {
            notifyContent += "，退款 ¥" + refundAmount + " 已退回钱包";
        }
        notificationService.send(booking.getUserId(), "booking", "订单已取消", notifyContent, "/order/" + booking.getId());

        Map<String, Object> result = new HashMap<>();
        result.put("booking", toBookingVO(booking));
        result.put("refundAmount", refundAmount);
        return result;
    }

    public Map<String, Object> pay(String id, String paymentMethod) {
        Booking booking = bookingMapper.selectById(id);
        if (booking == null) {
            throw new RuntimeException("订单不存在");
        }
        if (!"pending".equals(booking.getPaymentStatus())) {
            throw new RuntimeException("订单已支付或已退款");
        }

        // 如果使用钱包支付，检查余额并扣款
        if ("wallet".equals(paymentMethod)) {
            String userId = booking.getUserId();
            int amountInFen = booking.getTotalPrice().multiply(new BigDecimal(100)).intValue();
            
            // 检查余额并扣款
            boolean success = walletService.deductBalance(userId, amountInFen, 
                "订单支付 - " + booking.getOrderNumber(), booking.getId());
            if (!success) {
                throw new RuntimeException("余额不足");
            }
        }

        booking.setPaymentStatus("paid");
        booking.setPaymentMethod(paymentMethod);
        booking.setPaidAt(LocalDateTime.now());
        bookingMapper.updateById(booking);
        return toBookingVO(booking);
    }

    public Map<String, Object> confirm(String id) {
        Booking booking = bookingMapper.selectById(id);
        if (booking == null) {
            throw new RuntimeException("订单不存在");
        }
        booking.setStatus("confirmed");
        bookingMapper.updateById(booking);
        
        // 发送通知给用户
        notificationService.send(booking.getUserId(), "booking", "订单已确认",
            "您的订单 " + booking.getOrderNumber() + " 已被机构确认",
            "/order/" + booking.getId());
        
        return toBookingVO(booking);
    }

    public Map<String, Object> reject(String id, String reason) {
        Booking booking = bookingMapper.selectById(id);
        if (booking == null) {
            throw new RuntimeException("订单不存在");
        }
        booking.setStatus("cancelled");
        booking.setCancelReason(reason);
        
        BigDecimal refundAmount = BigDecimal.ZERO;
        if ("paid".equals(booking.getPaymentStatus())) {
            refundAmount = booking.getTotalPrice();
            booking.setRefundAmount(refundAmount);
            booking.setRefundedAt(LocalDateTime.now());
            booking.setPaymentStatus("refunded");
            
            // 退款到用户钱包
            int refundAmountInFen = refundAmount.multiply(new BigDecimal(100)).intValue();
            walletService.addIncome(booking.getUserId(), refundAmountInFen, 
                "订单退款 - " + booking.getOrderNumber(), booking.getId());
        }
        bookingMapper.updateById(booking);
        
        // 发送通知给用户
        String notifyContent = "您的订单 " + booking.getOrderNumber() + " 已被取消，原因：" + reason;
        if (refundAmount.compareTo(BigDecimal.ZERO) > 0) {
            notifyContent += "，退款 ¥" + refundAmount + " 已退回钱包";
        }
        notificationService.send(booking.getUserId(), "booking", "订单已取消", notifyContent, "/order/" + booking.getId());
        
        return toBookingVO(booking);
    }

    public Map<String, Object> checkIn(String id) {
        Booking booking = bookingMapper.selectById(id);
        if (booking == null) {
            throw new RuntimeException("订单不存在");
        }
        booking.setStatus("in_progress");
        booking.setCheckInTime(LocalDateTime.now());
        bookingMapper.updateById(booking);
        
        // 发送入住通知给用户
        Pet pet = petMapper.selectById(booking.getPetId());
        String petName = pet != null ? pet.getName() : "您的宠物";
        notificationService.send(booking.getUserId(), "booking", "宠物已入住",
            petName + " 已成功入住，订单号：" + booking.getOrderNumber() + "，祝它在这里度过愉快的时光！",
            "/order/" + booking.getId());
        
        return toBookingVO(booking);
    }

    public Map<String, Object> checkOut(String id) {
        Booking booking = bookingMapper.selectById(id);
        if (booking == null) {
            throw new RuntimeException("订单不存在");
        }
        booking.setStatus("completed");
        booking.setCheckOutTime(LocalDateTime.now());
        bookingMapper.updateById(booking);
        
        // 发送离店通知给用户
        Pet pet = petMapper.selectById(booking.getPetId());
        String petName = pet != null ? pet.getName() : "您的宠物";
        notificationService.send(booking.getUserId(), "booking", "宠物已离店",
            petName + " 已完成寄养，订单号：" + booking.getOrderNumber() + "，期待下次再见！",
            "/order/" + booking.getId());
        
        // 将订单金额转入机构钱包（查找机构员工）
        if ("paid".equals(booking.getPaymentStatus()) && booking.getTotalPrice() != null) {
            LambdaQueryWrapper<User> userWrapper = new LambdaQueryWrapper<>();
            userWrapper.eq(User::getInstitutionId, booking.getInstitutionId())
                       .last("LIMIT 1");
            User staffUser = userMapper.selectOne(userWrapper);
            if (staffUser != null) {
                int amountInFen = booking.getTotalPrice().multiply(new BigDecimal(100)).intValue();
                walletService.addIncome(staffUser.getId(), 
                    amountInFen,
                    "订单收入 - " + booking.getOrderNumber(),
                    booking.getId());
            }
        }
        
        return toBookingVO(booking);
    }

    @SneakyThrows
    private Map<String, Object> toBookingVO(Booking booking) {
        Map<String, Object> vo = new HashMap<>();
        vo.put("id", booking.getId());
        vo.put("orderNumber", booking.getOrderNumber());
        vo.put("userId", booking.getUserId());
        vo.put("institutionId", booking.getInstitutionId());
        vo.put("servicePackageId", booking.getServicePackageId());
        vo.put("petId", booking.getPetId());
        vo.put("status", booking.getStatus());
        vo.put("startDate", booking.getStartDate().toString());
        vo.put("endDate", booking.getEndDate().toString());
        vo.put("checkInTime", booking.getCheckInTime());
        vo.put("checkOutTime", booking.getCheckOutTime());
        vo.put("totalDays", booking.getTotalDays());
        vo.put("basePrice", booking.getBasePrice());
        vo.put("discount", booking.getDiscount());
        vo.put("totalPrice", booking.getTotalPrice());
        vo.put("paymentStatus", booking.getPaymentStatus());
        vo.put("paymentMethod", booking.getPaymentMethod());
        vo.put("paidAt", booking.getPaidAt());
        vo.put("refundAmount", booking.getRefundAmount());
        vo.put("refundedAt", booking.getRefundedAt());
        vo.put("cancelReason", booking.getCancelReason());
        vo.put("specialRequirements", booking.getSpecialRequirements());
        if (StringUtils.hasText(booking.getEmergencyContact())) {
            vo.put("emergencyContact", objectMapper.readValue(booking.getEmergencyContact(),
                    new TypeReference<Map<String, String>>() {}));
        }
        vo.put("createdAt", booking.getCreatedAt());
        vo.put("updatedAt", booking.getUpdatedAt());
        
        // 添加关联信息
        Institution inst = institutionMapper.selectById(booking.getInstitutionId());
        if (inst != null) {
            vo.put("institutionName", inst.getName());
            vo.put("institutionAddress", inst.getAddress());
            vo.put("institutionPhone", inst.getPhone());
        }
        
        Pet pet = petMapper.selectById(booking.getPetId());
        if (pet != null) {
            vo.put("petName", pet.getName());
            vo.put("petSpecies", pet.getSpecies());
            vo.put("petBreed", pet.getBreed());
            vo.put("petAge", pet.getAge());
            vo.put("petWeight", pet.getWeight());
        }
        
        ServicePackage pkg = servicePackageMapper.selectById(booking.getServicePackageId());
        if (pkg != null) {
            vo.put("packageName", pkg.getName());
            if (StringUtils.hasText(pkg.getFeatures())) {
                vo.put("packageFeatures", objectMapper.readValue(pkg.getFeatures(), new TypeReference<List<String>>() {}));
            }
        }
        
        User user = userMapper.selectById(booking.getUserId());
        if (user != null) {
            vo.put("userName", user.getName());
            vo.put("userPhone", user.getPhone());
        }
        
        return vo;
    }

    // 机构端获取订单列表
    public PageResult<Map<String, Object>> getInstitutionBookings(String staffUserId, int page, int pageSize) {
        // 获取员工所属机构
        User staff = userMapper.selectById(staffUserId);
        if (staff == null || staff.getInstitutionId() == null) {
            return PageResult.of(new ArrayList<>(), page, pageSize, 0);
        }
        
        LambdaQueryWrapper<Booking> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Booking::getInstitutionId, staff.getInstitutionId());
        // 过滤机构已删除的订单
        wrapper.and(w -> w.isNull(Booking::getInstitutionDeleted).or().eq(Booking::getInstitutionDeleted, false));
        wrapper.orderByDesc(Booking::getCreatedAt);
        
        Page<Booking> pageResult = bookingMapper.selectPage(new Page<>(page, pageSize), wrapper);
        List<Map<String, Object>> list = new ArrayList<>();
        for (Booking booking : pageResult.getRecords()) {
            list.add(toBookingVO(booking));
        }
        return PageResult.of(list, page, pageSize, pageResult.getTotal());
    }

    public Map<String, Object> confirmBooking(String id, String staffUserId) {
        return confirm(id);
    }

    public Map<String, Object> rejectBooking(String id, String staffUserId, String reason) {
        return reject(id, reason);
    }

    public Map<String, Object> checkIn(String id, String staffUserId) {
        return checkIn(id);
    }

    public Map<String, Object> checkOut(String id, String staffUserId) {
        Booking booking = bookingMapper.selectById(id);
        if (booking == null) {
            throw new RuntimeException("订单不存在");
        }
        booking.setStatus("completed");
        booking.setCheckOutTime(LocalDateTime.now());
        bookingMapper.updateById(booking);
        
        // 发送离店通知给用户
        Pet pet = petMapper.selectById(booking.getPetId());
        String petName = pet != null ? pet.getName() : "您的宠物";
        notificationService.send(booking.getUserId(), "booking", "宠物已离店",
            petName + " 已完成寄养，订单号：" + booking.getOrderNumber() + "，期待下次再见！",
            "/order/" + booking.getId());
        
        // 将订单金额转入机构员工钱包（执行离店操作的员工）
        if ("paid".equals(booking.getPaymentStatus()) && booking.getTotalPrice() != null && staffUserId != null) {
            int amountInFen = booking.getTotalPrice().multiply(new BigDecimal(100)).intValue();
            walletService.addIncome(staffUserId, 
                amountInFen,
                "订单收入 - " + booking.getOrderNumber(),
                booking.getId());
            System.out.println("订单离店收入已转入机构钱包: userId=" + staffUserId + ", amount=" + amountInFen + "分");
        }
        
        return toBookingVO(booking);
    }

    public void delete(String id, String userId) {
        Booking booking = bookingMapper.selectById(id);
        if (booking == null) {
            throw new RuntimeException("订单不存在");
        }
        // 验证订单属于该用户
        if (!booking.getUserId().equals(userId)) {
            throw new RuntimeException("无权删除此订单");
        }
        // 只允许删除已完成或已取消的订单
        if (!"completed".equals(booking.getStatus()) && !"cancelled".equals(booking.getStatus())) {
            throw new RuntimeException("只能删除已完成或已取消的订单");
        }
        // 设置用户删除标记，而不是真正删除
        booking.setUserDeleted(true);
        bookingMapper.updateById(booking);
    }

    public void deleteByInstitution(String id, String staffUserId) {
        Booking booking = bookingMapper.selectById(id);
        if (booking == null) {
            throw new RuntimeException("订单不存在");
        }
        // 验证员工属于该机构
        User staff = userMapper.selectById(staffUserId);
        if (staff == null || staff.getInstitutionId() == null || 
            !staff.getInstitutionId().equals(booking.getInstitutionId())) {
            throw new RuntimeException("无权删除此订单");
        }
        // 只允许删除已完成或已取消的订单
        if (!"completed".equals(booking.getStatus()) && !"cancelled".equals(booking.getStatus())) {
            throw new RuntimeException("只能删除已完成或已取消的订单");
        }
        // 设置机构删除标记，而不是真正删除
        booking.setInstitutionDeleted(true);
        bookingMapper.updateById(booking);
    }
}
