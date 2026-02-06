package com.crsp.mall.controller;

import com.crsp.mall.entity.ProductEntity;
import com.crsp.mall.service.ProductDbService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

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
    public ResponseEntity<?> getProduct(@PathVariable Long id) {
        return productDbService.getProductById(id)
                .<ResponseEntity<?>>map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("error", "商品不存在")));
    }

    /**
     * 搜索商品
     */
    @GetMapping("/products/search")
    public List<ProductEntity> searchProducts(@RequestParam(required = false) String keyword) {
        return productDbService.searchProducts(keyword);
    }
}
