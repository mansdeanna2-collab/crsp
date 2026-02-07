package com.crsp.mall.service;

import com.crsp.mall.entity.AdminEntity;
import com.crsp.mall.repository.AdminRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * 管理员服务类
 */
@Service
public class AdminService {

    @Autowired
    private AdminRepository adminRepository;

    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

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
            if (passwordEncoder.matches(password, admin.getPassword())) {
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
     * 修改管理员密码
     */
    public boolean changePassword(Long adminId, String oldPassword, String newPassword) {
        Optional<AdminEntity> adminOpt = adminRepository.findById(adminId);
        if (adminOpt.isEmpty()) {
            return false;
        }
        AdminEntity admin = adminOpt.get();
        if (!passwordEncoder.matches(oldPassword, admin.getPassword())) {
            return false;
        }
        admin.setPassword(passwordEncoder.encode(newPassword));
        adminRepository.save(admin);
        return true;
    }

    /**
     * 初始化默认管理员
     * 如果admin用户不存在则创建，如果已存在则确保密码为默认密码admin123
     */
    public void initDefaultAdmin() {
        Optional<AdminEntity> existing = adminRepository.findByUsername("admin");
        if (existing.isPresent()) {
            // 确保默认管理员密码始终为admin123（解决持久化数据库中密码不一致问题）
            AdminEntity admin = existing.get();
            if (!passwordEncoder.matches("admin123", admin.getPassword())) {
                admin.setPassword(passwordEncoder.encode("admin123"));
                adminRepository.save(admin);
            }
        } else {
            AdminEntity admin = new AdminEntity();
            admin.setUsername("admin");
            admin.setPassword(passwordEncoder.encode("admin123"));
            admin.setName("系统管理员");
            admin.setEmail("admin@mall.com");
            admin.setRole("super_admin");
            admin.setActive(true);
            adminRepository.save(admin);
        }
    }
}
