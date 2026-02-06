package com.crsp.mall.controller;

import com.crsp.mall.entity.ProductEntity;
import com.crsp.mall.service.ProductDbService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST API控制器
 */
@RestController
@RequestMapping("/api")
public class ApiController {

    @Autowired
    private ProductDbService productDbService;

    /**
     * 获取所有商品
     */
    @GetMapping("/products")
    public List<ProductEntity> getAllProducts() {
        return productDbService.getActiveProducts();
    }

    /**
     * 获取单个商品详情
     */
    @GetMapping("/products/{id}")
    public ProductEntity getProduct(@PathVariable Long id) {
        return productDbService.getProductById(id).orElse(null);
    }

    /**
     * 搜索商品
     */
    @GetMapping("/products/search")
    public List<ProductEntity> searchProducts(@RequestParam(required = false) String keyword) {
        return productDbService.searchProducts(keyword);
    }
}
