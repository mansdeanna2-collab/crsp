package com.crsp.mall.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * 商品实体类
 */
@Entity
@Table(name = "products")
public class ProductEntity {
    
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
    public void setBgColor(String bgColor) { this.bgColor = bgColor; }
    
    public String getTag() { return tag; }
    public void setTag(String tag) { this.tag = tag; }
    
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    
    public String getSpec() { return spec; }
    public void setSpec(String spec) { this.spec = spec; }
    
    public Integer getStock() { return stock; }
    public void setStock(Integer stock) { this.stock = stock; }
    
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
}
