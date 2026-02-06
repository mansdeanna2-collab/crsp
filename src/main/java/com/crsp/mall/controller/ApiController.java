package com.crsp.mall.controller;

import com.crsp.mall.model.Product;
import com.crsp.mall.service.ProductService;
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
    private ProductService productService;

    /**
     * 获取所有商品
     */
    @GetMapping("/products")
    public List<Product> getAllProducts() {
        return productService.getAllProducts();
    }

    /**
     * 获取单个商品详情
     */
    @GetMapping("/products/{id}")
    public Product getProduct(@PathVariable Long id) {
        return productService.getProductById(id);
    }

    /**
     * 搜索商品
     */
    @GetMapping("/products/search")
    public List<Product> searchProducts(@RequestParam(required = false) String keyword) {
        return productService.searchProducts(keyword);
    }
}
