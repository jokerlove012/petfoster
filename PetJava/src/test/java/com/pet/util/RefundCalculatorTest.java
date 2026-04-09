package com.pet.util;

import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Map;
import static org.junit.jupiter.api.Assertions.*;

/**
 * 退款计算工具测试
 */
public class RefundCalculatorTest {

    @Test
    public void testGetHoursUntilStart_FutureDate() {
        LocalDate startDate = LocalDate.of(2024, 1, 10);
        LocalDateTime cancelTime = LocalDateTime.of(2024, 1, 5, 10, 0);
        
        long hours = RefundCalculator.getHoursUntilStart(startDate, cancelTime);
        
        assertTrue(hours > 48, "应该大于 48 小时");
    }

    @Test
    public void testGetHoursUntilStart_PastDate() {
        LocalDate startDate = LocalDate.of(2024, 1, 5);
        LocalDateTime cancelTime = LocalDateTime.of(2024, 1, 10, 10, 0);
        
        long hours = RefundCalculator.getHoursUntilStart(startDate, cancelTime);
        
        assertTrue(hours < 0, "应该是负数，表示已经过了入住时间");
    }

    @Test
    public void testCalculateRefund_FullRefund_MoreThan48Hours() {
        BigDecimal totalPrice = new BigDecimal("1000");
        LocalDate startDate = LocalDate.of(2024, 1, 10);
        LocalDate endDate = LocalDate.of(2024, 1, 15);
        LocalDateTime cancelTime = LocalDateTime.of(2024, 1, 5, 10, 0);
        
        Map<String, Object> result = RefundCalculator.calculateRefund(totalPrice, startDate, endDate, cancelTime);
        
        assertEquals(new BigDecimal("1000"), result.get("refundAmount"), "应该全额退款");
        assertEquals(new BigDecimal("0"), result.get("cancellationFee"), "不应该有取消费");
        assertEquals(BigDecimal.ONE, result.get("refundRate"), "退款率应该是 100%");
        assertEquals("full", result.get("type"), "退款类型应该是全额退款");
    }

    @Test
    public void testCalculateRefund_PartialRefund_Within48Hours() {
        BigDecimal totalPrice = new BigDecimal("1000");
        LocalDate startDate = LocalDate.of(2024, 1, 10);
        LocalDate endDate = LocalDate.of(2024, 1, 15);
        LocalDateTime cancelTime = LocalDateTime.of(2024, 1, 9, 10, 0);
        
        Map<String, Object> result = RefundCalculator.calculateRefund(totalPrice, startDate, endDate, cancelTime);
        
        assertEquals(new BigDecimal("700.00"), result.get("refundAmount"), "应该退款 70%");
        assertEquals(new BigDecimal("300.00"), result.get("cancellationFee"), "取消费应该是 30%");
        assertEquals(new BigDecimal("0.7"), result.get("refundRate"), "退款率应该是 70%");
        assertEquals("partial", result.get("type"), "退款类型应该是部分退款");
    }

    @Test
    public void testCalculateRefund_AfterCheckIn() {
        BigDecimal totalPrice = new BigDecimal("1000");
        LocalDate startDate = LocalDate.of(2024, 1, 1);
        LocalDate endDate = LocalDate.of(2024, 1, 10);
        LocalDateTime cancelTime = LocalDateTime.of(2024, 1, 5, 10, 0);
        
        Map<String, Object> result = RefundCalculator.calculateRefund(totalPrice, startDate, endDate, cancelTime);
        
        assertNotNull(result.get("refundAmount"), "退款金额不应该为空");
        assertTrue(((BigDecimal) result.get("refundAmount")).compareTo(totalPrice) < 0, "退款金额应该小于总金额");
        assertEquals("partial", result.get("type"), "退款类型应该是部分退款");
    }

    @Test
    public void testCalculateRefund_NoRefund_Completed() {
        BigDecimal totalPrice = new BigDecimal("1000");
        LocalDate startDate = LocalDate.of(2024, 1, 1);
        LocalDate endDate = LocalDate.of(2024, 1, 5);
        LocalDateTime cancelTime = LocalDateTime.of(2024, 1, 10, 10, 0);
        
        Map<String, Object> result = RefundCalculator.calculateRefund(totalPrice, startDate, endDate, cancelTime);
        
        assertEquals(new BigDecimal("0"), result.get("refundAmount"), "已完成服务不应该退款");
        assertEquals("none", result.get("type"), "退款类型应该是无退款");
    }

    @Test
    public void testCalculateRefund_NullCancelTime() {
        BigDecimal totalPrice = new BigDecimal("1000");
        LocalDate startDate = LocalDate.of(2024, 1, 10);
        LocalDate endDate = LocalDate.of(2024, 1, 15);
        
        Map<String, Object> result = RefundCalculator.calculateRefund(totalPrice, startDate, endDate, null);
        
        assertNotNull(result, "结果不应该为空");
        assertNotNull(result.get("refundAmount"), "退款金额不应该为空");
    }

    @Test
    public void testCanCancelOrder_PendingStatus() {
        LocalDate startDate = LocalDate.of(2024, 1, 10);
        String status = "pending";
        
        Map<String, Object> result = RefundCalculator.canCancelOrder(startDate, status);
        
        assertTrue((boolean) result.get("canCancel"), "pending 状态的订单应该可以取消");
    }

    @Test
    public void testCanCancelOrder_CompletedStatus() {
        LocalDate startDate = LocalDate.of(2024, 1, 1);
        String status = "completed";
        
        Map<String, Object> result = RefundCalculator.canCancelOrder(startDate, status);
        
        assertFalse((boolean) result.get("canCancel"), "completed 状态的订单不能取消");
    }

    @Test
    public void testCanCancelOrder_CancelledStatus() {
        LocalDate startDate = LocalDate.of(2024, 1, 1);
        String status = "cancelled";
        
        Map<String, Object> result = RefundCalculator.canCancelOrder(startDate, status);
        
        assertFalse((boolean) result.get("canCancel"), "已取消的订单不能再次取消");
    }
}
