package com.crsp.mall.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * 消息实体类 - 支持用户与各类服务账号之间的消息通信
 */
@Entity
@Table(name = "messages")
public class MessageEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 所属用户ID */
    @Column(name = "user_id", nullable = false)
    private Long userId;

    /** 聊天类型: logistics=物流通知, store=成人用品旗舰店, promo=优惠活动, service=客服小蜜, system=系统通知 */
    @Column(name = "chat_type", nullable = false, length = 20)
    private String chatType;

    /** 发送者类型: user=用户发送, admin=后台发送 */
    @Column(name = "sender_type", nullable = false, length = 10)
    private String senderType;

    /** 发送者名称（后台发送时记录选择的账号名称） */
    @Column(name = "sender_name", length = 50)
    private String senderName;

    /** 消息内容 */
    @Column(name = "content", nullable = false, length = 1000)
    private String content;

    /** 是否已读 */
    @Column(name = "is_read")
    private Boolean isRead = false;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        if (isRead == null) {
            isRead = false;
        }
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public String getChatType() { return chatType; }
    public void setChatType(String chatType) { this.chatType = chatType; }

    public String getSenderType() { return senderType; }
    public void setSenderType(String senderType) { this.senderType = senderType; }

    public String getSenderName() { return senderName; }
    public void setSenderName(String senderName) { this.senderName = senderName; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public Boolean getIsRead() { return isRead; }
    public void setIsRead(Boolean isRead) { this.isRead = isRead; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    /** 获取聊天类型的中文名称 */
    public String getChatTypeName() {
        if (chatType == null) return "未知";
        return switch (chatType) {
            case "logistics" -> "物流通知";
            case "store" -> "成人用品旗舰店";
            case "promo" -> "优惠活动";
            case "service" -> "客服小蜜";
            case "system" -> "系统通知";
            default -> "未知";
        };
    }
}
