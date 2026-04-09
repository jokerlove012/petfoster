package com.pet.entity;

import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Institution 实体类测试
 */
public class InstitutionTest {

    @Test
    public void testInstitutionCreation() {
        Institution institution = new Institution();
        institution.setId("inst-123");
        institution.setName("阳光宠物寄养中心");
        institution.setDescription("专业的宠物寄养服务");
        institution.setPhone("010-12345678");
        institution.setEmail("contact@sunshinepet.com");
        institution.setStatus("active");
        
        assertEquals("inst-123", institution.getId());
        assertEquals("阳光宠物寄养中心", institution.getName());
        assertEquals("active", institution.getStatus());
    }

    @Test
    public void testInstitutionStatus() {
        Institution institution = new Institution();
        
        institution.setStatus("pending");
        assertEquals("pending", institution.getStatus());
        
        institution.setStatus("active");
        assertEquals("active", institution.getStatus());
        
        institution.setStatus("suspended");
        assertEquals("suspended", institution.getStatus());
        
        institution.setStatus("rejected");
        assertEquals("rejected", institution.getStatus());
    }

    @Test
    public void testInstitutionVerified() {
        Institution unverified = new Institution();
        unverified.setVerified(false);
        assertFalse(unverified.getVerified());
        
        Institution verified = new Institution();
        verified.setVerified(true);
        assertTrue(verified.getVerified());
    }

    @Test
    public void testInstitutionLocation() {
        Institution institution = new Institution();
        institution.setLatitude(new BigDecimal("39.9042"));
        institution.setLongitude(new BigDecimal("116.4074"));
        institution.setAddress("北京市朝阳区某某街道 1 号");
        
        assertEquals(new BigDecimal("39.9042"), institution.getLatitude());
        assertEquals(new BigDecimal("116.4074"), institution.getLongitude());
        assertEquals("北京市朝阳区某某街道 1 号", institution.getAddress());
    }

    @Test
    public void testInstitutionRating() {
        Institution institution = new Institution();
        institution.setRating(new BigDecimal("4.5"));
        institution.setReviewCount(100);
        
        assertEquals(new BigDecimal("4.5"), institution.getRating());
        assertEquals(100, institution.getReviewCount());
    }

    @Test
    public void testInstitutionCapacity() {
        Institution institution = new Institution();
        institution.setCapacity("{\"dog\": 50, \"cat\": 30, \"other\": 10}");
        institution.setCurrentOccupancy("{\"dog\": 30, \"cat\": 20, \"other\": 5}");
        
        assertNotNull(institution.getCapacity());
        assertNotNull(institution.getCurrentOccupancy());
    }

    @Test
    public void testInstitutionPetTypes() {
        Institution institution = new Institution();
        institution.setPetTypes("[\"dog\", \"cat\", \"other\"]");
        
        assertNotNull(institution.getPetTypes());
    }

    @Test
    public void testInstitutionBusinessHours() {
        Institution institution = new Institution();
        institution.setBusinessHours("{\"open\": \"09:00\", \"close\": \"18:00\"}");
        
        assertNotNull(institution.getBusinessHours());
    }

    @Test
    public void testInstitutionFeatures() {
        Institution institution = new Institution();
        institution.setFeatures("[\"24 小时监控\", \"独立活动区域\", \"专业护理\"]");
        
        assertNotNull(institution.getFeatures());
    }

    @Test
    public void testInstitutionLicenses() {
        Institution institution = new Institution();
        institution.setLicenses("[\"营业执照\", \"动物防疫合格证\"]");
        
        assertNotNull(institution.getLicenses());
    }

    @Test
    public void testInstitutionImages() {
        Institution institution = new Institution();
        institution.setImages("[\"url1.jpg\", \"url2.jpg\", \"url3.jpg\"]");
        institution.setLogo("logo.jpg");
        
        assertNotNull(institution.getImages());
        assertNotNull(institution.getLogo());
    }

    @Test
    public void testInstitutionTimestamps() {
        Institution institution = new Institution();
        LocalDateTime now = LocalDateTime.now();
        institution.setCreatedAt(now);
        institution.setUpdatedAt(now);
        
        assertEquals(now, institution.getCreatedAt());
        assertEquals(now, institution.getUpdatedAt());
    }

    @Test
    public void testInstitutionDeletedFlag() {
        Institution institution = new Institution();
        institution.setDeleted(0);
        
        assertEquals(0, institution.getDeleted());
        
        institution.setDeleted(1);
        assertEquals(1, institution.getDeleted());
    }
}
