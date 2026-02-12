package com.crsp.mall.controller;

import com.crsp.mall.entity.ProductEntity;
import com.crsp.mall.service.ProductDbService;
import com.crsp.mall.service.PromotionService;
import com.crsp.mall.service.UserService;
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

    private static final int MAX_RECOMMENDATIONS = 6;
    private static final int MAX_CART_RECOMMENDATIONS = 4;
    private static final int MAX_RELATED_PRODUCTS = 4;

    @Autowired
    private ProductDbService productDbService;

    @Autowired
    private UserService userService;

    @Autowired
    private PromotionService promotionService;
    
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
    public String search(@RequestParam(required = false) String keyword,
                         @RequestParam(required = false) String sort,
                         Model model) {
        List<ProductEntity> results = productDbService.searchProducts(keyword);
        
        // 排序处理
        if (sort != null && !"default".equals(sort)) {
            results = new ArrayList<>(results);
            if ("sales".equals(sort)) {
                results.sort((a, b) -> compareSales(b.getSales(), a.getSales()));
            } else if ("price_asc".equals(sort)) {
                results.sort((a, b) -> Double.compare(a.getPrice(), b.getPrice()));
            } else if ("price_desc".equals(sort)) {
                results.sort((a, b) -> Double.compare(b.getPrice(), a.getPrice()));
            }
        }
        
        model.addAttribute("keyword", keyword);
        model.addAttribute("sort", sort);
        model.addAttribute("products", results);
        
        // 热门搜索标签
        model.addAttribute("hotTags", List.of("按摩棒", "情趣内衣", "安全套", "延时", "飞机杯", "情趣套装"));
        
        return "search";
    }
    
    /**
     * 比较销量字符串（如"已售 2.3万件"）
     */
    private int compareSales(String a, String b) {
        return Double.compare(parseSalesNumber(a), parseSalesNumber(b));
    }
    
    private double parseSalesNumber(String sales) {
        if (sales == null) return 0;
        try {
            String num = sales.replaceAll("[^0-9.万]", "");
            if (num.contains("万")) {
                num = num.replace("万", "");
                return Double.parseDouble(num) * 10000;
            }
            return Double.parseDouble(num);
        } catch (NumberFormatException e) {
            return 0;
        }
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
                .limit(MAX_RELATED_PRODUCTS)
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
                .limit(MAX_CART_RECOMMENDATIONS)
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
                .limit(MAX_RECOMMENDATIONS)
                .toList();
        model.addAttribute("recommendProducts", recommendProducts);
        return "profile";
    }

    /**
     * 结算页面
     */
    @GetMapping("/checkout")
    public String checkout(Model model) {
        return "checkout";
    }

    /**
     * 浏览历史页面
     */
    @GetMapping("/history")
    public String history(Model model) {
        return "history";
    }

    /**
     * 我的收藏页面
     */
    @GetMapping("/favorites")
    public String favorites(Model model) {
        return "favorites";
    }

    /**
     * 我的订单页面
     */
    @GetMapping("/orders")
    public String orders(Model model) {
        return "orders";
    }

    /**
     * 限时秒杀页面
     */
    @GetMapping("/flash-sale")
    public String flashSale(Model model) {
        model.addAttribute("promotionItems", promotionService.getPromotionsWithProducts("flash_sale"));
        model.addAttribute("pageTitle", "限时秒杀");
        model.addAttribute("pageType", "flash_sale");
        java.time.LocalDateTime endTime = promotionService.getEarliestEndTime("flash_sale");
        model.addAttribute("countdownEndTime", endTime);
        return "promotion";
    }

    /**
     * 天天特价页面
     */
    @GetMapping("/daily-deal")
    public String dailyDeal(Model model) {
        model.addAttribute("promotionItems", promotionService.getPromotionsWithProducts("daily_deal"));
        model.addAttribute("pageTitle", "天天特价");
        model.addAttribute("pageType", "daily_deal");
        return "promotion";
    }

    /**
     * 品牌闪购页面
     */
    @GetMapping("/brand-flash")
    public String brandFlash(Model model) {
        model.addAttribute("promotionItems", promotionService.getPromotionsWithProducts("brand_flash"));
        model.addAttribute("pageTitle", "品牌闪购");
        model.addAttribute("pageType", "brand_flash");
        return "promotion";
    }

    /**
     * 新人专享页面
     */
    @GetMapping("/new-user")
    public String newUser(Model model) {
        model.addAttribute("promotionItems", promotionService.getPromotionsWithProducts("new_user"));
        model.addAttribute("pageTitle", "新人专享");
        model.addAttribute("pageType", "new_user");
        return "promotion";
    }
}
