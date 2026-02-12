package com.crsp.mall.entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;

/**
 * 促销活动实体类
 */
@Entity
@Table(name = "promotions")
public class PromotionEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String type;

    @Column(name = "product_id", nullable = false)
    private Long productId;

    private double promotionPrice;

    private Integer discountPercent;

    @Column(name = "start_time")
    private LocalDateTime startTime;

    @Column(name = "end_time")
    private LocalDateTime endTime;

    private Boolean active = true;

    private Integer sortOrder = 0;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    /**
     * 判断促销是否当前有效（active且在时间范围内）
     */
    public boolean isCurrentlyActive() {
        if (active == null || !active) {
            return false;
        }
        LocalDateTime now = LocalDateTime.now();
        if (startTime != null && now.isBefore(startTime)) {
            return false;
        }
        if (endTime != null && now.isAfter(endTime)) {
            return false;
        }
        return true;
    }

    /**
     * 获取促销类型的中文显示名称
     */
    public String getTypeDisplayName() {
        if (type == null) {
            return "";
        }
        switch (type) {
            case "flash_sale":
                return "限时秒杀";
            case "daily_deal":
                return "天天特价";
            case "brand_flash":
                return "品牌闪购";
            case "new_user":
                return "新人专享";
            default:
                return type;
        }
    }

    /**
     * 获取促销时间状态：未开始/进行中/已结束
     */
    public String getTimeStatus() {
        LocalDateTime now = LocalDateTime.now();
        if (startTime != null && now.isBefore(startTime)) {
            return "未开始";
        }
        if (endTime != null && now.isAfter(endTime)) {
            return "已结束";
        }
        return "进行中";
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public Long getProductId() { return productId; }
    public void setProductId(Long productId) { this.productId = productId; }

    public double getPromotionPrice() { return promotionPrice; }
    public void setPromotionPrice(double promotionPrice) { this.promotionPrice = promotionPrice; }

    public Integer getDiscountPercent() { return discountPercent; }
    public void setDiscountPercent(Integer discountPercent) { this.discountPercent = discountPercent; }

    public LocalDateTime getStartTime() { return startTime; }
    public void setStartTime(LocalDateTime startTime) { this.startTime = startTime; }

    public LocalDateTime getEndTime() { return endTime; }
    public void setEndTime(LocalDateTime endTime) { this.endTime = endTime; }

    public Boolean getActive() { return active; }
    public void setActive(Boolean active) { this.active = active; }

    public Integer getSortOrder() { return sortOrder; }
    public void setSortOrder(Integer sortOrder) { this.sortOrder = sortOrder; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
