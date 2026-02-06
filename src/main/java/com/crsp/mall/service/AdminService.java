package com.crsp.mall.service;

import com.crsp.mall.entity.AdminEntity;
import com.crsp.mall.repository.AdminRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.List;
import java.util.Optional;

/**
 * 管理员服务类
 */
@Service
public class AdminService {

    @Autowired
    private AdminRepository adminRepository;

    /**
     * 获取所有管理员
     */
    public List<AdminEntity> getAllAdmins() {
        return adminRepository.findAll();
    }

    /**
     * 根据ID获取管理员
     */
    public Optional<AdminEntity> getAdminById(Long id) {
        return adminRepository.findById(id);
    }

    /**
     * 管理员登录
     */
    public AdminEntity login(String username, String password) {
        Optional<AdminEntity> adminOpt = adminRepository.findByUsername(username);
        if (adminOpt.isPresent()) {
            AdminEntity admin = adminOpt.get();
            // Compare hashed passwords
            if (admin.getPassword().equals(hashPassword(password))) {
                admin.setLastLogin(LocalDateTime.now());
                adminRepository.save(admin);
                return admin;
            }
        }
        return null;
    }

    /**
     * 保存管理员
     */
    public AdminEntity saveAdmin(AdminEntity admin) {
        return adminRepository.save(admin);
    }

    /**
     * 删除管理员
     */
    public void deleteAdmin(Long id) {
        adminRepository.deleteById(id);
    }

    /**
     * 检查用户名是否存在
     */
    public boolean existsByUsername(String username) {
        return adminRepository.existsByUsername(username);
    }

    /**
     * 初始化默认管理员
     */
    public void initDefaultAdmin() {
        if (!adminRepository.existsByUsername("admin")) {
            AdminEntity admin = new AdminEntity();
            admin.setUsername("admin");
            admin.setPassword(hashPassword("admin123")); // Store hashed password
            admin.setName("系统管理员");
            admin.setEmail("admin@mall.com");
            admin.setRole("super_admin");
            admin.setActive(true);
            adminRepository.save(admin);
        }
    }

    /**
     * Hash password using SHA-256
     * Note: For production, consider using BCrypt with Spring Security
     */
    private String hashPassword(String password) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(password.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Failed to hash password", e);
        }
    }
}
