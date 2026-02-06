package com.crsp.mall.model;

/**
 * 商品模型
 */
public class Product {
    private Long id;
    private String title;
    private double price;
    private double originalPrice;
    private String sales;
    private String icon;
    private String bgColor;
    private String tag;
    private String description;
    private String spec;

    public Product() {}

    public Product(Long id, String title, double price, double originalPrice, String sales, 
                   String icon, String bgColor, String tag) {
        this.id = id;
        this.title = title;
        this.price = price;
        this.originalPrice = originalPrice;
        this.sales = sales;
        this.icon = icon;
        this.bgColor = bgColor;
        this.tag = tag;
        this.description = "这是一款优质商品，品质保证，售后无忧。隐私发货，安全放心。";
        this.spec = "默认规格";
    }

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
}
