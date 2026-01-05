package com.pet.util;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.Map;

/**
 * 退款计算工具
 * 
 * 退款规则：
 * - 入住前48小时以上取消: 全额退款 (100%)
 * - 入住前48小时内取消: 退款70% (收取30%手续费)
 * - 入住后取消: 按剩余天数比例退款
 */
public class RefundCalculator {

    /**
     * 计算距离入住的小时数
     */
    public static long getHoursUntilStart(LocalDate startDate, LocalDateTime cancelTime) {
        LocalDateTime startDateTime = startDate.atStartOfDay();
        return ChronoUnit.HOURS.between(cancelTime, startDateTime);
    }

    /**
     * 计算退款金额
     */
    public static Map<String, Object> calculateRefund(BigDecimal totalPrice, LocalDate startDate, LocalDate endDate, LocalDateTime cancelTime) {
        if (cancelTime == null) {
            cancelTime = LocalDateTime.now();
        }

        long hoursUntilStart = getHoursUntilStart(startDate, cancelTime);
        Map<String, Object> result = new HashMap<>();

        // 入住前48小时以上取消: 全额退款
        if (hoursUntilStart > 48) {
            result.put("refundAmount", totalPrice);
            result.put("cancellationFee", BigDecimal.ZERO);
            result.put("refundRate", BigDecimal.ONE);
            result.put("reason", "入住前48小时以上取消，全额退款");
            result.put("type", "full");
            result.put("estimatedDays", 5);
            return result;
        }

        // 入住前48小时内取消: 退款70%
        if (hoursUntilStart > 0) {
            BigDecimal refundRate = new BigDecimal("0.7");
            BigDecimal refundAmount = totalPrice.multiply(refundRate).setScale(2, RoundingMode.HALF_UP);
            BigDecimal cancellationFee = totalPrice.multiply(new BigDecimal("0.3")).setScale(2, RoundingMode.HALF_UP);

            result.put("refundAmount", refundAmount);
            result.put("cancellationFee", cancellationFee);
            result.put("refundRate", refundRate);
            result.put("reason", "入住前48小时内取消，收取30%手续费");
            result.put("type", "partial");
            result.put("estimatedDays", 5);
            return result;
        }

        // 入住后取消: 按剩余天数比例退款
        int totalDays = (int) ChronoUnit.DAYS.between(startDate, endDate) + 1;
        int usedDays = (int) ChronoUnit.DAYS.between(startDate, cancelTime.toLocalDate()) + 1;
        int remainingDays = Math.max(0, totalDays - usedDays);

        if (remainingDays > 0) {
            BigDecimal refundRate = BigDecimal.valueOf(remainingDays)
                    .divide(BigDecimal.valueOf(totalDays), 4, RoundingMode.HALF_UP)
                    .multiply(new BigDecimal("0.7"));
            BigDecimal refundAmount = totalPrice.multiply(refundRate).setScale(2, RoundingMode.HALF_UP);
            BigDecimal cancellationFee = totalPrice.subtract(refundAmount).setScale(2, RoundingMode.HALF_UP);

            result.put("refundAmount", refundAmount);
            result.put("cancellationFee", cancellationFee);
            result.put("refundRate", refundRate);
            result.put("reason", "入住后取消，按剩余" + remainingDays + "天比例退款70%");
            result.put("type", "partial");
            result.put("estimatedDays", 7);
            return result;
        }

        // 已完成或无剩余天数: 不退款
        result.put("refundAmount", BigDecimal.ZERO);
        result.put("cancellationFee", totalPrice);
        result.put("refundRate", BigDecimal.ZERO);
        result.put("reason", "服务已完成或已使用，不予退款");
        result.put("type", "none");
        result.put("estimatedDays", 0);
        return result;
    }

    /**
     * 判断是否可以取消订单
     */
    public static Map<String, Object> canCancelOrder(LocalDate startDate, String status) {
        Map<String, Object> result = new HashMap<>();

        if ("completed".equals(status) || "cancelled".equals(status)) {
            result.put("canCancel", false);
            result.put("reason", "订单已完成或已取消，无法再次取消");
            return result;
        }

        LocalDate now = LocalDate.now();
        if (now.isAfter(startDate)) {
            long daysPassed = ChronoUnit.DAYS.between(startDate, now);
            if (daysPassed > 1) {
                result.put("canCancel", true);
                result.put("reason", "入住后取消将按剩余天数比例退款");
                return result;
            }
        }

        result.put("canCancel", true);
        result.put("reason", "可以取消订单");
        return result;
    }
}
