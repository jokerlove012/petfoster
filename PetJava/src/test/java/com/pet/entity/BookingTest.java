package com.pet.entity;

import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Booking 实体类测试
 */
public class BookingTest {

    @Test
    public void testBookingCreation() {
        Booking booking = new Booking();
        booking.setId("test-id-123");
        booking.setOrderNumber("PF20240115123456");
        booking.setUserId("user-123");
        booking.setInstitutionId("inst-123");
        booking.setServicePackageId("pkg-123");
        booking.setPetId("pet-123");
        booking.setStatus("pending");
        booking.setStartDate(LocalDate.of(2024, 1, 10));
        booking.setEndDate(LocalDate.of(2024, 1, 15));
        booking.setTotalDays(6);
        booking.setBasePrice(new BigDecimal("100.00"));
        booking.setDiscount(new BigDecimal("5.00"));
        booking.setTotalPrice(new BigDecimal("595.00"));
        booking.setPaymentStatus("pending");
        
        assertEquals("test-id-123", booking.getId());
        assertEquals("PF20240115123456", booking.getOrderNumber());
        assertEquals("pending", booking.getStatus());
        assertEquals(6, booking.getTotalDays());
    }

    @Test
    public void testBookingStatusTransitions() {
        Booking booking = new Booking();
        booking.setStatus("pending");
        
        assertEquals("pending", booking.getStatus());
        
        booking.setStatus("confirmed");
        assertEquals("confirmed", booking.getStatus());
        
        booking.setStatus("in_progress");
        assertEquals("in_progress", booking.getStatus());
        
        booking.setStatus("completed");
        assertEquals("completed", booking.getStatus());
    }

    @Test
    public void testBookingPaymentStatus() {
        Booking booking = new Booking();
        booking.setPaymentStatus("pending");
        booking.setTotalPrice(new BigDecimal("500.00"));
        
        assertEquals("pending", booking.getPaymentStatus());
        
        booking.setPaymentStatus("paid");
        booking.setPaidAt(LocalDateTime.now());
        assertEquals("paid", booking.getPaymentStatus());
        assertNotNull(booking.getPaidAt());
    }

    @Test
    public void testBookingCancellation() {
        Booking booking = new Booking();
        booking.setStatus("pending");
        booking.setCancelReason("客户改变主意");
        
        assertEquals("pending", booking.getStatus());
        assertEquals("客户改变主意", booking.getCancelReason());
        
        booking.setStatus("cancelled");
        assertEquals("cancelled", booking.getStatus());
    }

    @Test
    public void testBookingRefund() {
        Booking booking = new Booking();
        booking.setPaymentStatus("paid");
        booking.setTotalPrice(new BigDecimal("1000.00"));
        booking.setRefundAmount(new BigDecimal("700.00"));
        booking.setRefundedAt(LocalDateTime.now());
        
        assertEquals("paid", booking.getPaymentStatus());
        assertEquals(new BigDecimal("700.00"), booking.getRefundAmount());
        assertNotNull(booking.getRefundedAt());
    }

    @Test
    public void testBookingCheckIn() {
        Booking booking = new Booking();
        booking.setStatus("confirmed");
        booking.setCheckInTime(LocalDateTime.now());
        
        assertEquals("confirmed", booking.getStatus());
        assertNotNull(booking.getCheckInTime());
        
        booking.setStatus("in_progress");
        assertEquals("in_progress", booking.getStatus());
    }

    @Test
    public void testBookingCheckOut() {
        Booking booking = new Booking();
        booking.setStatus("in_progress");
        booking.setCheckOutTime(LocalDateTime.now());
        
        assertEquals("in_progress", booking.getStatus());
        assertNotNull(booking.getCheckOutTime());
        
        booking.setStatus("completed");
        assertEquals("completed", booking.getStatus());
    }

    @Test
    public void testBookingUserDeletedFlag() {
        Booking booking = new Booking();
        booking.setUserDeleted(false);
        
        assertFalse(booking.getUserDeleted());
        
        booking.setUserDeleted(true);
        assertTrue(booking.getUserDeleted());
    }

    @Test
    public void testBookingInstitutionDeletedFlag() {
        Booking booking = new Booking();
        booking.setInstitutionDeleted(false);
        
        assertFalse(booking.getInstitutionDeleted());
        
        booking.setInstitutionDeleted(true);
        assertTrue(booking.getInstitutionDeleted());
    }

    @Test
    public void testBookingEmergencyContact() {
        Booking booking = new Booking();
        String emergencyContactJson = "{\"name\":\"张三\",\"phone\":\"13800138000\"}";
        booking.setEmergencyContact(emergencyContactJson);
        
        assertEquals(emergencyContactJson, booking.getEmergencyContact());
    }

    @Test
    public void testBookingSpecialRequirements() {
        Booking booking = new Booking();
        booking.setSpecialRequirements("需要每天喂药两次");
        
        assertEquals("需要每天喂药两次", booking.getSpecialRequirements());
    }

    @Test
    public void testBookingTimestamps() {
        Booking booking = new Booking();
        LocalDateTime now = LocalDateTime.now();
        booking.setCreatedAt(now);
        booking.setUpdatedAt(now);
        
        assertEquals(now, booking.getCreatedAt());
        assertEquals(now, booking.getUpdatedAt());
    }
}
