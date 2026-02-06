package com.crsp.mall.service;

import com.crsp.mall.model.Product;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 商品服务类
 */
@Service
public class ProductService {

    private final List<Product> products = new ArrayList<>();

    public ProductService() {
        initProducts();
    }

    /**
     * 初始化成人玩具商品数据
     */
    private void initProducts() {
        products.add(new Product(1L, "高级震动按摩棒 多频调节", 128, 256, "已售 2.3万件",
                "fa-wand-magic-sparkles", "#ffecd2, #fcb69f", "新品"));
        products.add(new Product(2L, "智能遥控跳蛋 静音设计", 89, 178, "已售 1.5万件",
                "fa-circle", "#a1c4fd, #c2e9fb", "热卖"));
        products.add(new Product(3L, "情趣内衣套装 蕾丝款", 68, 136, "已售 8500件",
                "fa-shirt", "#d299c2, #fef9d7", "特价"));
        products.add(new Product(4L, "延时喷剂 植物配方", 158, 316, "已售 3.2万件",
                "fa-spray-can", "#f5f7fa, #c3cfe2", "爆款"));
        products.add(new Product(5L, "安全套超薄装 12只", 39, 78, "已售 1.8万件",
                "fa-box", "#ffecd2, #fcb69f", "新品"));
        products.add(new Product(6L, "情趣套装 夫妻调情", 299, 598, "已售 9800件",
                "fa-gift", "#667eea, #764ba2", "热卖"));
        products.add(new Product(7L, "飞机杯 自动加热款", 259, 518, "已售 5600件",
                "fa-mug-hot", "#43e97b, #38f9d7", "推荐"));
        products.add(new Product(8L, "仿真倒模 名器", 399, 798, "已售 4200件",
                "fa-gem", "#fa709a, #fee140", "精品"));
    }

    /**
     * 获取所有商品
     */
    public List<Product> getAllProducts() {
        return new ArrayList<>(products);
    }

    /**
     * 根据ID获取商品
     */
    public Product getProductById(Long id) {
        return products.stream()
                .filter(p -> p.getId().equals(id))
                .findFirst()
                .orElse(null);
    }

    /**
     * 搜索商品
     */
    public List<Product> searchProducts(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return new ArrayList<>(products);
        }
        String lowerKeyword = keyword.toLowerCase();
        return products.stream()
                .filter(p -> p.getTitle().toLowerCase().contains(lowerKeyword) ||
                           p.getTag().toLowerCase().contains(lowerKeyword))
                .collect(Collectors.toList());
    }
}
