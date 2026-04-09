package com.pet.entity;

import org.junit.jupiter.api.Test;
import java.time.LocalDate;
import java.time.LocalDateTime;
import static org.junit.jupiter.api.Assertions.*;

/**
 * User 实体类测试
 */
public class UserTest {

    @Test
    public void testUserCreation() {
        User user = new User();
        user.setId("user-123");
        user.setEmail("test@example.com");
        user.setPhone("13800138000");
        user.setName("张三");
        user.setGender("male");
        user.setBirthday(LocalDate.of(1990, 1, 1));
        user.setRole("pet_owner");
        
        assertEquals("user-123", user.getId());
        assertEquals("test@example.com", user.getEmail());
        assertEquals("张三", user.getName());
        assertEquals("pet_owner", user.getRole());
    }

    @Test
    public void testUserRoles() {
        User petOwner = new User();
        petOwner.setRole("pet_owner");
        assertEquals("pet_owner", petOwner.getRole());
        
        User staff = new User();
        staff.setRole("institution_staff");
        staff.setInstitutionId("inst-123");
        staff.setPosition("caretaker");
        assertEquals("institution_staff", staff.getRole());
        assertEquals("inst-123", staff.getInstitutionId());
        
        User admin = new User();
        admin.setRole("admin");
        admin.setAdminLevel("super");
        assertEquals("admin", admin.getRole());
        assertEquals("super", admin.getAdminLevel());
    }

    @Test
    public void testUserInstitutionStaff() {
        User staff = new User();
        staff.setRole("institution_staff");
        staff.setInstitutionId("inst-123");
        staff.setPosition("manager");
        staff.setPermissions("[\"manage_bookings\", \"view_reports\"]");
        
        assertEquals("inst-123", staff.getInstitutionId());
        assertEquals("manager", staff.getPosition());
        assertNotNull(staff.getPermissions());
    }

    @Test
    public void testUserAdminLevels() {
        User superAdmin = new User();
        superAdmin.setRole("admin");
        superAdmin.setAdminLevel("super");
        
        User normalAdmin = new User();
        normalAdmin.setRole("admin");
        normalAdmin.setAdminLevel("normal");
        
        assertEquals("super", superAdmin.getAdminLevel());
        assertEquals("normal", normalAdmin.getAdminLevel());
        assertNotEquals(superAdmin.getAdminLevel(), normalAdmin.getAdminLevel());
    }

    @Test
    public void testUserGender() {
        User maleUser = new User();
        maleUser.setGender("male");
        assertEquals("male", maleUser.getGender());
        
        User femaleUser = new User();
        femaleUser.setGender("female");
        assertEquals("female", femaleUser.getGender());
    }

    @Test
    public void testUserBirthday() {
        User user = new User();
        LocalDate birthday = LocalDate.of(1995, 6, 15);
        user.setBirthday(birthday);
        
        assertEquals(birthday, user.getBirthday());
    }

    @Test
    public void testUserContactInfo() {
        User user = new User();
        user.setEmail("john.doe@example.com");
        user.setPhone("13900139000");
        user.setAddress("北京市朝阳区某某街道");
        
        assertEquals("john.doe@example.com", user.getEmail());
        assertEquals("13900139000", user.getPhone());
        assertEquals("北京市朝阳区某某街道", user.getAddress());
    }

    @Test
    public void testUserAvatar() {
        User user = new User();
        user.setAvatar("https://example.com/avatar/user-123.jpg");
        
        assertEquals("https://example.com/avatar/user-123.jpg", user.getAvatar());
    }

    @Test
    public void testUserTimestamps() {
        User user = new User();
        LocalDateTime now = LocalDateTime.now();
        user.setCreatedAt(now);
        user.setUpdatedAt(now);
        
        assertEquals(now, user.getCreatedAt());
        assertEquals(now, user.getUpdatedAt());
    }

    @Test
    public void testUserDeletedFlag() {
        User user = new User();
        user.setDeleted(0);
        
        assertEquals(0, user.getDeleted());
        
        user.setDeleted(1);
        assertEquals(1, user.getDeleted());
    }
}
