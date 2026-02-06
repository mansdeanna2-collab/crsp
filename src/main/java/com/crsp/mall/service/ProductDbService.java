package com.crsp.mall.service;

import com.crsp.mall.entity.ProductEntity;
import com.crsp.mall.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

/**
 * 商品数据库服务类
 */
@Service
public class ProductDbService {

    @Autowired
    private ProductRepository productRepository;

    /**
     * 获取所有商品
     */
    public List<ProductEntity> getAllProducts() {
        return productRepository.findAll();
    }

    /**
     * 获取所有上架商品
     */
    public List<ProductEntity> getActiveProducts() {
        return productRepository.findByActiveTrue();
    }

    /**
     * 根据ID获取商品
     */
    public Optional<ProductEntity> getProductById(Long id) {
        return productRepository.findById(id);
    }

    /**
     * 搜索商品
     */
    public List<ProductEntity> searchProducts(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return productRepository.findByActiveTrue();
        }
        return productRepository.findByTitleContainingIgnoreCaseAndActiveTrue(keyword.trim());
    }

    /**
     * 保存商品
     */
    public ProductEntity saveProduct(ProductEntity product) {
        return productRepository.save(product);
    }

    /**
     * 删除商品
     */
    public void deleteProduct(Long id) {
        productRepository.deleteById(id);
    }

    /**
     * 初始化默认商品数据
     */
    public void initDefaultProducts() {
        if (productRepository.count() == 0) {
            saveProduct(createProduct("高级震动按摩棒 多频调节", 128, 256, "已售 2.3万件",
                    "fa-wand-magic-sparkles", "#ffecd2, #fcb69f", "新品", 100));
            saveProduct(createProduct("智能遥控跳蛋 静音设计", 89, 178, "已售 1.5万件",
                    "fa-circle", "#a1c4fd, #c2e9fb", "热卖", 150));
            saveProduct(createProduct("情趣内衣套装 蕾丝款", 68, 136, "已售 8500件",
                    "fa-shirt", "#d299c2, #fef9d7", "特价", 200));
            saveProduct(createProduct("延时喷剂 植物配方", 158, 316, "已售 3.2万件",
                    "fa-spray-can", "#f5f7fa, #c3cfe2", "爆款", 80));
            saveProduct(createProduct("安全套超薄装 12只", 39, 78, "已售 1.8万件",
                    "fa-box", "#ffecd2, #fcb69f", "新品", 500));
            saveProduct(createProduct("情趣套装 夫妻调情", 299, 598, "已售 9800件",
                    "fa-gift", "#667eea, #764ba2", "热卖", 60));
            saveProduct(createProduct("飞机杯 自动加热款", 259, 518, "已售 5600件",
                    "fa-mug-hot", "#43e97b, #38f9d7", "推荐", 75));
            saveProduct(createProduct("仿真倒模 名器", 399, 798, "已售 4200件",
                    "fa-gem", "#fa709a, #fee140", "精品", 45));
        }
    }

    private ProductEntity createProduct(String title, double price, double originalPrice, String sales,
                                         String icon, String bgColor, String tag, Integer stock) {
        ProductEntity product = new ProductEntity();
        product.setTitle(title);
        product.setPrice(price);
        product.setOriginalPrice(originalPrice);
        product.setSales(sales);
        product.setIcon(icon);
        product.setBgColor(bgColor);
        product.setTag(tag);
        product.setStock(stock);
        product.setDescription("这是一款优质商品，品质保证，售后无忧。隐私发货，安全放心。");
        product.setSpec("默认规格");
        product.setActive(true);
        return product;
    }
}
