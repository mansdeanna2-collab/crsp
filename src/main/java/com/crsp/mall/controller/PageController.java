package com.crsp.mall.controller;

import com.crsp.mall.model.Product;
import com.crsp.mall.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

/**
 * 页面控制器
 */
@Controller
public class PageController {

    @Autowired
    private ProductService productService;

    /**
     * 首页
     */
    @GetMapping("/")
    public String home(Model model) {
        List<Product> products = productService.getAllProducts();
        model.addAttribute("products", products);
        return "index";
    }

    /**
     * 搜索页面（独立页面）
     */
    @GetMapping("/search")
    public String search(@RequestParam(required = false) String keyword, Model model) {
        List<Product> results;
        if (keyword != null && !keyword.trim().isEmpty()) {
            results = productService.searchProducts(keyword);
        } else {
            results = productService.getAllProducts();
        }
        model.addAttribute("keyword", keyword);
        model.addAttribute("products", results);
        return "search";
    }

    /**
     * 商品详情页面（独立页面，参考淘宝风格）
     */
    @GetMapping("/product/{id}")
    public String productDetail(@PathVariable Long id, Model model) {
        Product product = productService.getProductById(id);
        if (product == null) {
            return "redirect:/";
        }
        model.addAttribute("product", product);
        // 获取相关推荐商品
        List<Product> relatedProducts = productService.getAllProducts().stream()
                .filter(p -> !p.getId().equals(id))
                .limit(4)
                .toList();
        model.addAttribute("relatedProducts", relatedProducts);
        return "product-detail";
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
        return "cart";
    }

    /**
     * 个人中心页面
     */
    @GetMapping("/profile")
    public String profile(Model model) {
        return "profile";
    }
}
