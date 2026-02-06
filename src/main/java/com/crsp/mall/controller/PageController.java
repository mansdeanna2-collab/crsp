package com.crsp.mall.controller;

import com.crsp.mall.entity.ProductEntity;
import com.crsp.mall.service.ProductDbService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * 页面控制器
 */
@Controller
public class PageController {

    @Autowired
    private ProductDbService productDbService;
    
    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * 首页
     */
    @GetMapping("/")
    public String home(Model model) {
        List<ProductEntity> products = productDbService.getActiveProducts();
        model.addAttribute("products", products);
        return "index";
    }

    /**
     * 搜索页面（独立页面）
     */
    @GetMapping("/search")
    public String search(@RequestParam(required = false) String keyword, Model model) {
        List<ProductEntity> results = productDbService.searchProducts(keyword);
        model.addAttribute("keyword", keyword);
        model.addAttribute("products", results);
        return "search";
    }

    /**
     * 商品详情页面（独立页面，参考淘宝风格）
     */
    @GetMapping("/product/{id}")
    public String productDetail(@PathVariable Long id, Model model) {
        Optional<ProductEntity> productOpt = productDbService.getProductById(id);
        if (productOpt.isEmpty()) {
            return "redirect:/";
        }
        ProductEntity product = productOpt.get();
        model.addAttribute("product", product);
        
        // 解析展示媒体JSON
        List<Map<String, String>> displayMediaList = parseMediaJson(product.getDisplayMedia());
        model.addAttribute("displayMediaList", displayMediaList);
        
        // 解析详情媒体JSON
        List<Map<String, String>> detailMediaList = parseMediaJson(product.getDetailMedia());
        model.addAttribute("detailMediaList", detailMediaList);
        
        // 获取相关推荐商品
        List<ProductEntity> relatedProducts = productDbService.getActiveProducts().stream()
                .filter(p -> !p.getId().equals(id))
                .limit(4)
                .toList();
        model.addAttribute("relatedProducts", relatedProducts);
        return "product-detail";
    }
    
    /**
     * 解析媒体JSON字符串为List
     */
    private List<Map<String, String>> parseMediaJson(String json) {
        if (json == null || json.trim().isEmpty()) {
            return new ArrayList<>();
        }
        try {
            return objectMapper.readValue(json, new TypeReference<List<Map<String, String>>>() {});
        } catch (Exception e) {
            // JSON解析失败，返回空列表
            return new ArrayList<>();
        }
    }

    /**
     * 消息页面
     */
    @GetMapping("/message")
    public String message(Model model) {
        return "message";
    }

    /**
     * 购物车页面
     */
    @GetMapping("/cart")
    public String cart(Model model) {
        // 添加推荐商品
        List<ProductEntity> recommendProducts = productDbService.getActiveProducts().stream()
                .limit(4)
                .toList();
        model.addAttribute("recommendProducts", recommendProducts);
        return "cart";
    }

    /**
     * 个人中心页面
     */
    @GetMapping("/profile")
    public String profile(Model model) {
        // 添加推荐商品（猜你喜欢）
        List<ProductEntity> recommendProducts = productDbService.getActiveProducts().stream()
                .limit(6)
                .toList();
        model.addAttribute("recommendProducts", recommendProducts);
        return "profile";
    }
}
