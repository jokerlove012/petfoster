package com.pet.util;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.Map;

/**
 * 价格计算工具
 */
public class PriceCalculator {

    /**
     * 计算两个日期之间的天数
     */
    public static int calculateDays(LocalDate startDate, LocalDate endDate) {
        return (int) ChronoUnit.DAYS.between(startDate, endDate) + 1;
    }

    /**
     * 根据天数获取折扣率
     * - 7天以上: 5% 折扣
     * - 14天以上: 10% 折扣
     * - 30天以上: 15% 折扣
     */
    public static BigDecimal getDiscountRate(int days) {
        if (days >= 30) return new BigDecimal("0.15");
        if (days >= 14) return new BigDecimal("0.10");
        if (days >= 7) return new BigDecimal("0.05");
        return BigDecimal.ZERO;
    }

    /**
     * 计算预约价格
     */
    public static Map<String, Object> calculateBookingPrice(BigDecimal pricePerDay, int days) {
        return calculateBookingPrice(pricePerDay, days, null);
    }

    /**
     * 计算预约价格（带自定义折扣率）
     */
    public static Map<String, Object> calculateBookingPrice(BigDecimal pricePerDay, int days, BigDecimal customDiscountRate) {
        BigDecimal subtotal = pricePerDay.multiply(BigDecimal.valueOf(days));
        BigDecimal discountRate = customDiscountRate != null ? customDiscountRate : getDiscountRate(days);
        BigDecimal discount = subtotal.multiply(discountRate).setScale(2, RoundingMode.HALF_UP);
        BigDecimal totalPrice = subtotal.subtract(discount).setScale(2, RoundingMode.HALF_UP);

        Map<String, Object> result = new HashMap<>();
        result.put("basePrice", pricePerDay);
        result.put("totalDays", days);
        result.put("subtotal", subtotal);
        result.put("discount", discount);
        result.put("discountRate", discountRate);
        result.put("totalPrice", totalPrice);
        return result;
    }

    /**
     * 计算预约价格（通过日期范围）
     */
    public static Map<String, Object> calculateBookingPriceByDates(BigDecimal pricePerDay, LocalDate startDate, LocalDate endDate) {
        int days = calculateDays(startDate, endDate);
        return calculateBookingPrice(pricePerDay, days);
    }
}
