package com.crsp.mall.service;

import com.crsp.mall.entity.AdminEntity;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class AdminServiceTest {

    @Autowired
    private AdminService adminService;

    @Test
    void initDefaultAdminCreatesAdminWithBCryptPassword() {
        adminService.initDefaultAdmin();
        assertTrue(adminService.existsByUsername("admin"));
    }

    @Test
    void loginSucceedsWithCorrectCredentials() {
        adminService.initDefaultAdmin();
        AdminEntity admin = adminService.login("admin", "admin123");
        assertNotNull(admin);
        assertEquals("admin", admin.getUsername());
        assertNotNull(admin.getLastLogin());
    }

    @Test
    void loginFailsWithWrongPassword() {
        adminService.initDefaultAdmin();
        AdminEntity admin = adminService.login("admin", "wrongpassword");
        assertNull(admin);
    }

    @Test
    void loginFailsWithNonexistentUser() {
        AdminEntity admin = adminService.login("nonexistent", "admin123");
        assertNull(admin);
    }
}
