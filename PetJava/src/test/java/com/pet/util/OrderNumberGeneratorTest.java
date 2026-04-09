package com.pet.util;

import org.junit.jupiter.api.Test;
import java.time.LocalDate;
import java.util.regex.Pattern;
import static org.junit.jupiter.api.Assertions.*;

/**
 * 订单号生成工具测试
 */
public class OrderNumberGeneratorTest {

    private static final Pattern ORDER_NUMBER_PATTERN = Pattern.compile("^[A-Z]{2,4}\\d{14,16}$");

    @Test
    public void testGenerateOrderNumber_DefaultFormat() {
        String orderNumber = OrderNumberGenerator.generateOrderNumber();
        
        assertNotNull(orderNumber, "订单号不应该为空");
        assertFalse(orderNumber.isEmpty(), "订单号不应该为空字符串");
        assertTrue(orderNumber.startsWith("PF"), "订单号应该以 PF 开头");
        assertTrue(ORDER_NUMBER_PATTERN.matcher(orderNumber).matches(), 
            "订单号应该符合格式：2-4 个字母 + 14-16 个数字");
    }

    @Test
    public void testGenerateOrderNumber_WithCustomPrefix() {
        LocalDate date = LocalDate.of(2024, 1, 15);
        String orderNumber = OrderNumberGenerator.generateOrderNumber("TEST", date);
        
        assertNotNull(orderNumber, "订单号不应该为空");
        assertTrue(orderNumber.startsWith("TEST"), "订单号应该以 TEST 开头");
        assertTrue(orderNumber.contains("20240115"), "订单号应该包含日期 20240115");
    }

    @Test
    public void testGenerateOrderNumber_DateFormat() {
        LocalDate date = LocalDate.of(2024, 12, 25);
        String orderNumber = OrderNumberGenerator.generateOrderNumber("ORD", date);
        
        assertTrue(orderNumber.contains("20241225"), "订单号应该包含格式化日期 20241225");
    }

    @Test
    public void testGenerateOrderNumber_Uniqueness() {
        String orderNumber1 = OrderNumberGenerator.generateOrderNumber();
        String orderNumber2 = OrderNumberGenerator.generateOrderNumber();
        
        assertNotEquals(orderNumber1, orderNumber2, "两次生成的订单号应该不同");
    }

    @Test
    public void testGenerateOrderNumber_MultipleUnique() {
        int count = 100;
        java.util.Set<String> orderNumbers = new java.util.HashSet<>();
        
        for (int i = 0; i < count; i++) {
            orderNumbers.add(OrderNumberGenerator.generateOrderNumber());
        }
        
        assertEquals(count, orderNumbers.size(), 
            "生成 " + count + " 个订单号应该都是唯一的");
    }

    @Test
    public void testGenerateOrderNumber_Length() {
        String orderNumber = OrderNumberGenerator.generateOrderNumber();
        
        assertTrue(orderNumber.length() >= 16, "订单号长度应该至少为 16");
        assertTrue(orderNumber.length() <= 20, "订单号长度应该最多为 20");
    }

    @Test
    public void testGenerateOrderNumber_ContainsOnlyDigitsAfterPrefix() {
        String orderNumber = OrderNumberGenerator.generateOrderNumber();
        String digitsPart = orderNumber.substring(2);
        
        assertTrue(digitsPart.matches("\\d+"), "订单号前缀后的部分应该全部是数字");
    }

    @Test
    public void testGenerateOrderNumber_DifferentDates() {
        LocalDate date1 = LocalDate.of(2024, 1, 1);
        LocalDate date2 = LocalDate.of(2024, 12, 31);
        
        String orderNumber1 = OrderNumberGenerator.generateOrderNumber("TEST", date1);
        String orderNumber2 = OrderNumberGenerator.generateOrderNumber("TEST", date2);
        
        assertTrue(orderNumber1.contains("20240101"), "应该包含日期 20240101");
        assertTrue(orderNumber2.contains("20241231"), "应该包含日期 20241231");
        assertNotEquals(orderNumber1, orderNumber2, "不同日期的订单号应该不同");
    }
}
