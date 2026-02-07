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

    @Test
    void initDefaultAdminResetsPasswordIfChanged() {
        // First init creates admin with "admin123"
        adminService.initDefaultAdmin();
        AdminEntity admin = adminService.login("admin", "admin123");
        assertNotNull(admin);

        // Simulate password being changed externally
        adminService.changePassword(admin.getId(), "admin123", "different_password");
        // Verify the changed password works
        assertNotNull(adminService.login("admin", "different_password"));
        // admin123 no longer works
        assertNull(adminService.login("admin", "admin123"));

        // Re-init should reset password back to "admin123"
        adminService.initDefaultAdmin();
        assertNotNull(adminService.login("admin", "admin123"));
        assertNull(adminService.login("admin", "different_password"));
    }

    @Test
    void changePasswordSucceedsWithCorrectOldPassword() {
        adminService.initDefaultAdmin();
        AdminEntity admin = adminService.login("admin", "admin123");
        assertNotNull(admin);

        boolean result = adminService.changePassword(admin.getId(), "admin123", "newpass456");
        assertTrue(result);

        // Old password should fail
        assertNull(adminService.login("admin", "admin123"));
        // New password should work
        assertNotNull(adminService.login("admin", "newpass456"));
    }

    @Test
    void changePasswordFailsWithWrongOldPassword() {
        adminService.initDefaultAdmin();
        AdminEntity admin = adminService.login("admin", "admin123");
        assertNotNull(admin);

        boolean result = adminService.changePassword(admin.getId(), "wrongold", "newpass456");
        assertFalse(result);

        // Original password should still work
        assertNotNull(adminService.login("admin", "admin123"));
    }

    @Test
    void changePasswordFailsWithNonexistentAdmin() {
        boolean result = adminService.changePassword(99999L, "anything", "newpass456");
        assertFalse(result);
    }
}
