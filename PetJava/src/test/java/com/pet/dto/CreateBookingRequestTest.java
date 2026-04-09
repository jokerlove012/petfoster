package com.pet.dto;

import org.junit.jupiter.api.Test;
import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import static org.junit.jupiter.api.Assertions.*;

/**
 * CreateBookingRequest DTO 测试
 */
public class CreateBookingRequestTest {

    private final Validator validator;

    public CreateBookingRequestTest() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        this.validator = factory.getValidator();
    }

    @Test
    public void testValidCreateBookingRequest() {
        CreateBookingRequest request = new CreateBookingRequest();
        request.setInstitutionId("inst-123");
        request.setServicePackageId("pkg-123");
        request.setPetId("pet-123");
        request.setStartDate("2024-01-10");
        request.setEndDate("2024-01-15");
        
        Map<String, String> emergencyContact = new HashMap<>();
        emergencyContact.put("name", "张三");
        emergencyContact.put("phone", "13800138000");
        request.setEmergencyContact(emergencyContact);
        
        Set<ConstraintViolation<CreateBookingRequest>> violations = validator.validate(request);
        
        assertTrue(violations.isEmpty(), "有效的请求不应该有验证错误");
    }

    @Test
    public void testMissingInstitutionId() {
        CreateBookingRequest request = new CreateBookingRequest();
        request.setServicePackageId("pkg-123");
        request.setPetId("pet-123");
        request.setStartDate("2024-01-10");
        request.setEndDate("2024-01-15");
        request.setEmergencyContact(new HashMap<>());
        
        Set<ConstraintViolation<CreateBookingRequest>> violations = validator.validate(request);
        
        assertFalse(violations.isEmpty(), "缺少机构 ID 应该有验证错误");
        assertTrue(violations.stream()
            .anyMatch(v -> v.getPropertyPath().toString().equals("institutionId")));
    }

    @Test
    public void testMissingServicePackageId() {
        CreateBookingRequest request = new CreateBookingRequest();
        request.setInstitutionId("inst-123");
        request.setPetId("pet-123");
        request.setStartDate("2024-01-10");
        request.setEndDate("2024-01-15");
        request.setEmergencyContact(new HashMap<>());
        
        Set<ConstraintViolation<CreateBookingRequest>> violations = validator.validate(request);
        
        assertFalse(violations.isEmpty(), "缺少套餐 ID 应该有验证错误");
        assertTrue(violations.stream()
            .anyMatch(v -> v.getPropertyPath().toString().equals("servicePackageId")));
    }

    @Test
    public void testMissingPetId() {
        CreateBookingRequest request = new CreateBookingRequest();
        request.setInstitutionId("inst-123");
        request.setServicePackageId("pkg-123");
        request.setStartDate("2024-01-10");
        request.setEndDate("2024-01-15");
        request.setEmergencyContact(new HashMap<>());
        
        Set<ConstraintViolation<CreateBookingRequest>> violations = validator.validate(request);
        
        assertFalse(violations.isEmpty(), "缺少宠物 ID 应该有验证错误");
        assertTrue(violations.stream()
            .anyMatch(v -> v.getPropertyPath().toString().equals("petId")));
    }

    @Test
    public void testMissingStartDate() {
        CreateBookingRequest request = new CreateBookingRequest();
        request.setInstitutionId("inst-123");
        request.setServicePackageId("pkg-123");
        request.setPetId("pet-123");
        request.setEndDate("2024-01-15");
        request.setEmergencyContact(new HashMap<>());
        
        Set<ConstraintViolation<CreateBookingRequest>> violations = validator.validate(request);
        
        assertFalse(violations.isEmpty(), "缺少开始日期应该有验证错误");
        assertTrue(violations.stream()
            .anyMatch(v -> v.getPropertyPath().toString().equals("startDate")));
    }

    @Test
    public void testMissingEndDate() {
        CreateBookingRequest request = new CreateBookingRequest();
        request.setInstitutionId("inst-123");
        request.setServicePackageId("pkg-123");
        request.setPetId("pet-123");
        request.setStartDate("2024-01-10");
        request.setEmergencyContact(new HashMap<>());
        
        Set<ConstraintViolation<CreateBookingRequest>> violations = validator.validate(request);
        
        assertFalse(violations.isEmpty(), "缺少结束日期应该有验证错误");
        assertTrue(violations.stream()
            .anyMatch(v -> v.getPropertyPath().toString().equals("endDate")));
    }

    @Test
    public void testMissingEmergencyContact() {
        CreateBookingRequest request = new CreateBookingRequest();
        request.setInstitutionId("inst-123");
        request.setServicePackageId("pkg-123");
        request.setPetId("pet-123");
        request.setStartDate("2024-01-10");
        request.setEndDate("2024-01-15");
        
        Set<ConstraintViolation<CreateBookingRequest>> violations = validator.validate(request);
        
        assertFalse(violations.isEmpty(), "缺少紧急联系人应该有验证错误");
        assertTrue(violations.stream()
            .anyMatch(v -> v.getPropertyPath().toString().equals("emergencyContact")));
    }

    @Test
    public void testSpecialRequirements() {
        CreateBookingRequest request = new CreateBookingRequest();
        request.setInstitutionId("inst-123");
        request.setServicePackageId("pkg-123");
        request.setPetId("pet-123");
        request.setStartDate("2024-01-10");
        request.setEndDate("2024-01-15");
        request.setSpecialRequirements("需要每天喂药两次");
        request.setEmergencyContact(new HashMap<>());
        
        Set<ConstraintViolation<CreateBookingRequest>> violations = validator.validate(request);
        
        assertTrue(violations.isEmpty(), "特殊要求是可选字段，不应该有验证错误");
        assertEquals("需要每天喂药两次", request.getSpecialRequirements());
    }
}
