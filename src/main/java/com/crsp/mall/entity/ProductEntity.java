package com.crsp.mall.entity;

import jakarta.persistence.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;

/**
 * 商品实体类
 */
@Entity
@Table(name = "products")
public class ProductEntity {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(ProductEntity.class);

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private String title;
    
    @Column(nullable = false)
    private double price;
    
    private double originalPrice;
    
    private String sales;
    
    private String icon;
    
    private String bgColor;
    
    private String tag;
    
    @Column(length = 1000)
    private String description;
    
    private String spec;
    
    private Integer stock;
    
    private Boolean active = true;
    
    // 商品展示图片/视频 (JSON格式存储多个媒体项，每项包含type: image/video, url: 链接)
    @Column(name = "display_media", length = 5000)
    private String displayMedia;
    
    // 商品详情图片/视频 (JSON格式存储多个媒体项)
    @Column(name = "detail_media", length = 10000)
    private String detailMedia;
    
    // 商品规格列表 (JSON格式存储，包含name, image, price字段)
    @Column(name = "specifications", length = 10000)
    private String specifications;
    
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
    
    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    
    public double getPrice() { return price; }
    public void setPrice(double price) { this.price = price; }
    
    public double getOriginalPrice() { return originalPrice; }
    public void setOriginalPrice(double originalPrice) { this.originalPrice = originalPrice; }
    
    public String getSales() { return sales; }
    public void setSales(String sales) { this.sales = sales; }
    
    public String getIcon() { return icon; }
    public void setIcon(String icon) { this.icon = icon; }
    
    public String getBgColor() { return bgColor; }
    public void setBgColor(String bgColor) {
        // Sanitize bgColor to only allow hex colors and commas (prevent CSS injection)
        if (bgColor != null && !bgColor.matches("^[#a-fA-F0-9, ]+$")) {
            this.bgColor = "#ffecd2, #fcb69f";
        } else {
            this.bgColor = bgColor;
        }
    }
    
    public String getTag() { return tag; }
    public void setTag(String tag) { this.tag = tag; }
    
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    
    public String getSpec() { return spec; }
    public void setSpec(String spec) { this.spec = spec; }
    
    public Integer getStock() { return stock; }
    public void setStock(Integer stock) { this.stock = stock; }

    /**
     * Stock is treated as available when value is null (unlimited/unspecified).
     */
    public boolean isInStock() {
        return stock == null || stock > 0;
    }

    public boolean isLowStock() {
        return stock != null && stock > 0 && stock <= 10;
    }

    public String getStockStatus() {
        if (!isInStock()) {
            return "已售完";
        }
        if (isLowStock()) {
            return "库存紧张";
        }
        return "有货";
    }
    
    public Boolean getActive() { return active; }
    public void setActive(Boolean active) { this.active = active; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
    
    public String getDisplayMedia() { return displayMedia; }
    public void setDisplayMedia(String displayMedia) { this.displayMedia = displayMedia; }
    
    public String getDetailMedia() { return detailMedia; }
    public void setDetailMedia(String detailMedia) { this.detailMedia = detailMedia; }
    
    public String getSpecifications() { return specifications; }
    public void setSpecifications(String specifications) { this.specifications = specifications; }
    
    /**
     * 获取第一个展示图片的URL（用于商品列表显示）
     * @return 图片URL，如果没有则返回null
     */
    public String getFirstImageUrl() {
        if (displayMedia == null || displayMedia.trim().isEmpty()) {
            return null;
        }
        try {
            // 简单解析JSON数组获取第一个图片
            // 使用静态ObjectMapper以提高性能
            java.util.List<java.util.Map<String, String>> mediaList = OBJECT_MAPPER.readValue(
                displayMedia, 
                new com.fasterxml.jackson.core.type.TypeReference<java.util.List<java.util.Map<String, String>>>() {}
            );
            for (java.util.Map<String, String> media : mediaList) {
                if ("image".equals(media.get("type")) && media.get("url") != null) {
                    return media.get("url");
                }
            }
        } catch (Exception e) {
            LOGGER.warn("商品ID={}的displayMedia JSON解析失败: {}", id, e.getMessage());
        }
        return null;
    }
    
    /**
     * 获取第一个展示视频的URL
     * @return 视频URL，如果没有则返回null
     */
    public String getFirstVideoUrl() {
        if (displayMedia == null || displayMedia.trim().isEmpty()) {
            return null;
        }
        try {
            java.util.List<java.util.Map<String, String>> mediaList = OBJECT_MAPPER.readValue(
                displayMedia, 
                new com.fasterxml.jackson.core.type.TypeReference<java.util.List<java.util.Map<String, String>>>() {}
            );
            for (java.util.Map<String, String> media : mediaList) {
                if ("video".equals(media.get("type")) && media.get("url") != null) {
                    return media.get("url");
                }
            }
        } catch (Exception e) {
            LOGGER.warn("商品ID={}的displayMedia视频JSON解析失败: {}", id, e.getMessage());
        }
        return null;
    }
    
    // 静态ObjectMapper实例，线程安全可复用
    private static final com.fasterxml.jackson.databind.ObjectMapper OBJECT_MAPPER = new com.fasterxml.jackson.databind.ObjectMapper();
    
    /**
     * 检查是否有展示媒体（图片或视频）
     * @return true如果有展示媒体
     */
    public boolean hasDisplayMedia() {
        return getFirstImageUrl() != null || getFirstVideoUrl() != null;
    }
}
