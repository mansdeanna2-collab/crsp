package com.crsp.mall.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * 用户实体类 - 支持游客自动注册和普通用户
 */
@Entity
@Table(name = "users")
public class UserEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "token", unique = true, nullable = false)
    private String token;
    
    @Column(name = "nickname")
    private String nickname;
    
    @Column(name = "phone")
    private String phone;
    
    @Column(name = "email")
    private String email;
    
    @Column(name = "avatar")
    private String avatar;
    
    @Column(name = "address", length = 500)
    private String address;
    
    // 用户类型: guest=游客, user=注册用户
    @Column(name = "user_type", nullable = false)
    private String userType = "guest";
    
    @Column(name = "active")
    private Boolean active = true;
    
    @Column(name = "last_visit")
    private LocalDateTime lastVisit;
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        lastVisit = LocalDateTime.now();
        if (token == null) {
            token = UUID.randomUUID().toString();
        }
        if (nickname == null) {
            nickname = "游客" + token.substring(0, 8);
        }
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
    
    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }
    
    public String getNickname() { return nickname; }
    public void setNickname(String nickname) { this.nickname = nickname; }
    
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    
    public String getAvatar() { return avatar; }
    public void setAvatar(String avatar) { this.avatar = avatar; }
    
    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }
    
    public String getUserType() { return userType; }
    public void setUserType(String userType) { this.userType = userType; }
    
    public Boolean getActive() { return active; }
    public void setActive(Boolean active) { this.active = active; }
    
    public LocalDateTime getLastVisit() { return lastVisit; }
    public void setLastVisit(LocalDateTime lastVisit) { this.lastVisit = lastVisit; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
    
    public String getUserTypeText() {
        return switch (userType) {
            case "guest" -> "游客";
            case "user" -> "注册用户";
            default -> "未知";
        };
    }

    /**
     * 根据消费金额计算用户等级
     */
    public static String calculateLevel(double totalSpending) {
        if (totalSpending >= 10000) return "钻石会员";
        if (totalSpending >= 5000) return "金牌会员";
        if (totalSpending >= 1000) return "银牌会员";
        if (totalSpending > 0) return "普通会员";
        return "新用户";
    }
}
