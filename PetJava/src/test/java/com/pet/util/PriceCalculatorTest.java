package com.pet.util;

import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;
import static org.junit.jupiter.api.Assertions.*;

/**
 * 价格计算工具测试
 */
public class PriceCalculatorTest {

    @Test
    public void testCalculateDays() {
        LocalDate startDate = LocalDate.of(2024, 1, 1);
        LocalDate endDate = LocalDate.of(2024, 1, 5);
        
        int days = PriceCalculator.calculateDays(startDate, endDate);
        
        assertEquals(5, days, "应该包含首尾两天");
    }

    @Test
    public void testCalculateDays_SingleDay() {
        LocalDate startDate = LocalDate.of(2024, 1, 1);
        LocalDate endDate = LocalDate.of(2024, 1, 1);
        
        int days = PriceCalculator.calculateDays(startDate, endDate);
        
        assertEquals(1, days, "单日应该是 1 天");
    }

    @Test
    public void testGetDiscountRate_NoDiscount() {
        BigDecimal discountRate = PriceCalculator.getDiscountRate(3);
        
        assertEquals(new BigDecimal("0"), discountRate, "3 天应该没有折扣");
    }

    @Test
    public void testGetDiscountRate_5Percent() {
        BigDecimal discountRate = PriceCalculator.getDiscountRate(7);
        
        assertEquals(new BigDecimal("0.05"), discountRate, "7 天应该是 5% 折扣");
    }

    @Test
    public void testGetDiscountRate_10Percent() {
        BigDecimal discountRate = PriceCalculator.getDiscountRate(14);
        
        assertEquals(new BigDecimal("0.10"), discountRate, "14 天应该是 10% 折扣");
    }

    @Test
    public void testGetDiscountRate_15Percent() {
        BigDecimal discountRate = PriceCalculator.getDiscountRate(30);
        
        assertEquals(new BigDecimal("0.15"), discountRate, "30 天应该是 15% 折扣");
    }

    @Test
    public void testCalculateBookingPrice_NoDiscount() {
        BigDecimal pricePerDay = new BigDecimal("100");
        int days = 3;
        
        Map<String, Object> result = PriceCalculator.calculateBookingPrice(pricePerDay, days);
        
        assertEquals(0, ((BigDecimal) result.get("subtotal")).compareTo(new BigDecimal("300")), "小计应该是 300");
        assertEquals(0, ((BigDecimal) result.get("discount")).compareTo(new BigDecimal("0")), "没有折扣");
        assertEquals(0, ((BigDecimal) result.get("totalPrice")).compareTo(new BigDecimal("300")), "总价应该是 300");
        assertEquals(3, result.get("totalDays"), "天数应该是 3");
    }

    @Test
    public void testCalculateBookingPrice_With5PercentDiscount() {
        BigDecimal pricePerDay = new BigDecimal("100");
        int days = 7;
        
        Map<String, Object> result = PriceCalculator.calculateBookingPrice(pricePerDay, days);
        
        assertEquals(new BigDecimal("700"), result.get("subtotal"), "小计应该是 700");
        assertEquals(new BigDecimal("35.00"), result.get("discount"), "折扣应该是 35");
        assertEquals(new BigDecimal("665.00"), result.get("totalPrice"), "总价应该是 665");
    }

    @Test
    public void testCalculateBookingPrice_WithCustomDiscount() {
        BigDecimal pricePerDay = new BigDecimal("100");
        int days = 5;
        BigDecimal customDiscountRate = new BigDecimal("0.20");
        
        Map<String, Object> result = PriceCalculator.calculateBookingPrice(pricePerDay, days, customDiscountRate);
        
        assertEquals(new BigDecimal("500"), result.get("subtotal"), "小计应该是 500");
        assertEquals(new BigDecimal("100.00"), result.get("discount"), "自定义折扣应该是 100");
        assertEquals(new BigDecimal("400.00"), result.get("totalPrice"), "总价应该是 400");
    }

    @Test
    public void testCalculateBookingPriceByDates() {
        BigDecimal pricePerDay = new BigDecimal("100");
        LocalDate startDate = LocalDate.of(2024, 1, 1);
        LocalDate endDate = LocalDate.of(2024, 1, 10);
        
        Map<String, Object> result = PriceCalculator.calculateBookingPriceByDates(pricePerDay, startDate, endDate);
        
        assertEquals(10, result.get("totalDays"), "应该是 10 天");
        assertEquals(new BigDecimal("1000"), result.get("subtotal"), "小计应该是 1000");
        assertEquals(new BigDecimal("50.00"), result.get("discount"), "10 天应该有 5% 折扣");
        assertEquals(new BigDecimal("950.00"), result.get("totalPrice"), "总价应该是 950");
    }

    @Test
    public void testCalculateBookingPrice_Rounding() {
        BigDecimal pricePerDay = new BigDecimal("99.99");
        int days = 7;
        
        Map<String, Object> result = PriceCalculator.calculateBookingPrice(pricePerDay, days);
        
        assertNotNull(result.get("totalPrice"), "总价不应该为空");
        assertTrue(result.get("totalPrice") instanceof BigDecimal, "总价应该是 BigDecimal 类型");
    }
}
