package com.crsp.mall.controller;

import com.crsp.mall.entity.*;
import com.crsp.mall.service.OrderService;
import com.crsp.mall.service.ProductDbService;
import com.crsp.mall.service.UserService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * 用户API控制器 - 处理用户相关的REST接口
 */
@RestController
@RequestMapping("/api/user")
public class UserApiController {

    @Autowired
    private UserService userService;

    @Autowired
    private ProductDbService productDbService;

    @Autowired
    private OrderService orderService;

    /**
     * 获取或创建用户（自动注册游客）
     */
    @PostMapping("/init")
    public ResponseEntity<Map<String, Object>> initUser(HttpServletRequest request, HttpServletResponse response) {
        String token = getTokenFromCookie(request);
        UserEntity user = userService.getOrCreateUser(token);
        
        // 设置cookie
        Cookie cookie = new Cookie("user_token", user.getToken());
        cookie.setMaxAge(365 * 24 * 60 * 60); // 1年
        cookie.setPath("/");
        cookie.setHttpOnly(true);
        response.addCookie(cookie);
        
        Map<String, Object> result = new HashMap<>();
        result.put("id", user.getId());
        result.put("token", user.getToken());
        result.put("nickname", user.getNickname());
        result.put("userType", user.getUserType());
        return ResponseEntity.ok(result);
    }

    /**
     * 获取用户信息
     */
    @GetMapping("/info")
    public ResponseEntity<?> getUserInfo(HttpServletRequest request) {
        UserEntity user = getCurrentUser(request);
        if (user == null) {
            return ResponseEntity.ok(Map.of("error", "用户未初始化"));
        }
        Map<String, Object> result = new HashMap<>();
        result.put("id", user.getId());
        result.put("nickname", user.getNickname());
        result.put("userType", user.getUserType());
        result.put("phone", user.getPhone());
        result.put("email", user.getEmail());
        result.put("favoriteCount", userService.getFavoriteCount(user.getId()));
        result.put("cartCount", userService.getCartItemCount(user.getId()));
        return ResponseEntity.ok(result);
    }

    // ===== 浏览历史 =====

    /**
     * 记录浏览历史
     */
    @PostMapping("/history/{productId}")
    public ResponseEntity<?> addHistory(@PathVariable Long productId, HttpServletRequest request, HttpServletResponse response) {
        UserEntity user = getOrInitUser(request, response);
        Optional<ProductEntity> productOpt = productDbService.getProductById(productId);
        if (productOpt.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "商品不存在"));
        }
        BrowsingHistoryEntity history = userService.addBrowsingHistory(user.getId(), productOpt.get());
        return ResponseEntity.ok(Map.of("success", true, "id", history.getId()));
    }

    /**
     * 获取浏览历史列表
     */
    @GetMapping("/history")
    public ResponseEntity<?> getHistory(HttpServletRequest request) {
        UserEntity user = getCurrentUser(request);
        if (user == null) {
            return ResponseEntity.ok(List.of());
        }
        return ResponseEntity.ok(userService.getBrowsingHistory(user.getId()));
    }

    /**
     * 清空浏览历史
     */
    @DeleteMapping("/history")
    public ResponseEntity<?> clearHistory(HttpServletRequest request) {
        UserEntity user = getCurrentUser(request);
        if (user == null) {
            return ResponseEntity.ok(Map.of("success", true));
        }
        userService.clearBrowsingHistory(user.getId());
        return ResponseEntity.ok(Map.of("success", true));
    }

    // ===== 收藏 =====

    /**
     * 切换收藏状态
     */
    @PostMapping("/favorite/{productId}")
    public ResponseEntity<?> toggleFavorite(@PathVariable Long productId, HttpServletRequest request, HttpServletResponse response) {
        UserEntity user = getOrInitUser(request, response);
        Optional<ProductEntity> productOpt = productDbService.getProductById(productId);
        if (productOpt.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "商品不存在"));
        }
        boolean isFavorite = userService.toggleFavorite(user.getId(), productOpt.get());
        return ResponseEntity.ok(Map.of("success", true, "isFavorite", isFavorite));
    }

    /**
     * 检查是否已收藏
     */
    @GetMapping("/favorite/{productId}")
    public ResponseEntity<?> checkFavorite(@PathVariable Long productId, HttpServletRequest request) {
        UserEntity user = getCurrentUser(request);
        if (user == null) {
            return ResponseEntity.ok(Map.of("isFavorite", false));
        }
        boolean isFavorite = userService.isFavorite(user.getId(), productId);
        return ResponseEntity.ok(Map.of("isFavorite", isFavorite));
    }

    /**
     * 获取收藏列表
     */
    @GetMapping("/favorites")
    public ResponseEntity<?> getFavorites(HttpServletRequest request) {
        UserEntity user = getCurrentUser(request);
        if (user == null) {
            return ResponseEntity.ok(List.of());
        }
        return ResponseEntity.ok(userService.getFavorites(user.getId()));
    }

    // ===== 购物车 =====

    /**
     * 添加商品到购物车
     */
    @PostMapping("/cart")
    public ResponseEntity<?> addToCart(@RequestBody Map<String, Object> body, HttpServletRequest request, HttpServletResponse response) {
        UserEntity user = getOrInitUser(request, response);
        
        if (body.get("productId") == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "商品ID不能为空"));
        }
        
        Long productId;
        Integer quantity;
        try {
            productId = Long.valueOf(body.get("productId").toString());
            quantity = body.get("quantity") != null ? Integer.valueOf(body.get("quantity").toString()) : 1;
        } catch (NumberFormatException e) {
            return ResponseEntity.badRequest().body(Map.of("error", "参数格式无效"));
        }
        
        String specName = body.get("specName") != null ? body.get("specName").toString() : "";
        
        Optional<ProductEntity> productOpt = productDbService.getProductById(productId);
        if (productOpt.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "商品不存在"));
        }
        
        CartItemEntity item = userService.addToCart(user.getId(), productOpt.get(), specName, quantity);
        long cartCount = userService.getCartItemCount(user.getId());
        return ResponseEntity.ok(Map.of("success", true, "id", item.getId(), "cartCount", cartCount));
    }

    /**
     * 获取购物车列表
     */
    @GetMapping("/cart")
    public ResponseEntity<?> getCart(HttpServletRequest request) {
        UserEntity user = getCurrentUser(request);
        if (user == null) {
            return ResponseEntity.ok(List.of());
        }
        return ResponseEntity.ok(userService.getCartItems(user.getId()));
    }

    /**
     * 更新购物车商品数量
     */
    @PutMapping("/cart/{itemId}")
    public ResponseEntity<?> updateCartItem(@PathVariable Long itemId, @RequestBody Map<String, Object> body, HttpServletRequest request) {
        UserEntity user = getCurrentUser(request);
        if (user == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "用户未登录"));
        }
        if (body.get("quantity") == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "数量不能为空"));
        }
        Integer quantity;
        try {
            quantity = Integer.valueOf(body.get("quantity").toString());
        } catch (NumberFormatException e) {
            return ResponseEntity.badRequest().body(Map.of("error", "数量格式无效"));
        }
        if (quantity < 1 || quantity > 999) {
            return ResponseEntity.badRequest().body(Map.of("error", "数量必须在1-999之间"));
        }
        CartItemEntity item = userService.updateCartItemQuantity(itemId, quantity, user.getId());
        if (item == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "购物车项不存在"));
        }
        return ResponseEntity.ok(Map.of("success", true));
    }

    /**
     * 更新购物车商品选中状态
     */
    @PutMapping("/cart/{itemId}/select")
    public ResponseEntity<?> updateCartItemSelected(@PathVariable Long itemId, @RequestBody Map<String, Object> body, HttpServletRequest request) {
        UserEntity user = getCurrentUser(request);
        if (user == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "用户未登录"));
        }
        if (body.get("selected") == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "选中状态不能为空"));
        }
        Boolean selected = Boolean.valueOf(body.get("selected").toString());
        CartItemEntity item = userService.updateCartItemSelected(itemId, selected, user.getId());
        if (item == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "购物车项不存在"));
        }
        return ResponseEntity.ok(Map.of("success", true));
    }

    /**
     * 删除购物车商品
     */
    @DeleteMapping("/cart/{itemId}")
    public ResponseEntity<?> removeCartItem(@PathVariable Long itemId, HttpServletRequest request) {
        UserEntity user = getCurrentUser(request);
        if (user == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "用户未登录"));
        }
        userService.removeCartItem(itemId, user.getId());
        return ResponseEntity.ok(Map.of("success", true));
    }

    /**
     * 清空购物车
     */
    @DeleteMapping("/cart")
    public ResponseEntity<?> clearCart(HttpServletRequest request) {
        UserEntity user = getCurrentUser(request);
        if (user == null) {
            return ResponseEntity.ok(Map.of("success", true));
        }
        userService.clearCart(user.getId());
        return ResponseEntity.ok(Map.of("success", true));
    }

    // ===== 结算/下单 =====

    /**
     * 提交订单
     */
    @PostMapping("/checkout")
    public ResponseEntity<?> checkout(@RequestBody Map<String, Object> body, HttpServletRequest request) {
        UserEntity user = getCurrentUser(request);
        if (user == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "请先初始化用户"));
        }

        String userName = body.get("userName") != null ? body.get("userName").toString().trim() : "";
        String userPhone = body.get("userPhone") != null ? body.get("userPhone").toString().trim() : "";
        String shippingAddress = body.get("shippingAddress") != null ? body.get("shippingAddress").toString().trim() : "";
        String remark = body.get("remark") != null ? body.get("remark").toString().trim() : "";

        // 输入验证
        if (userName.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "请输入收货人姓名"));
        }
        // 中国大陆手机号格式: 1[3-9]开头，11位数字
        if (userPhone.isEmpty() || !userPhone.matches("^1[3-9]\\d{9}$")) {
            return ResponseEntity.badRequest().body(Map.of("error", "请输入正确的手机号码"));
        }
        if (shippingAddress.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "请输入收货地址"));
        }

        // 获取选中的购物车商品
        List<CartItemEntity> selectedItems = userService.getSelectedCartItems(user.getId());
        if (selectedItems.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "请选择要结算的商品"));
        }

        // 验证库存并计算总价
        double totalAmount = 0;
        int totalCount = 0;
        for (CartItemEntity item : selectedItems) {
            Optional<ProductEntity> productOpt = productDbService.getProductById(item.getProductId());
            if (productOpt.isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("error", "商品 \"" + item.getProductTitle() + "\" 已下架"));
            }
            ProductEntity product = productOpt.get();
            if (!Boolean.TRUE.equals(product.getActive())) {
                return ResponseEntity.badRequest().body(Map.of("error", "商品 \"" + item.getProductTitle() + "\" 已下架"));
            }
            if (!product.isInStock()) {
                return ResponseEntity.badRequest().body(Map.of("error", "商品 \"" + item.getProductTitle() + "\" 已售罄"));
            }
            if (product.getStock() != null && product.getStock() < item.getQuantity()) {
                return ResponseEntity.badRequest().body(Map.of("error", "商品 \"" + item.getProductTitle() + "\" 库存不足，当前库存: " + product.getStock()));
            }
            totalAmount += item.getProductPrice() * item.getQuantity();
            totalCount += item.getQuantity();
        }

        // 创建订单
        OrderEntity order = new OrderEntity();
        order.setUserId(user.getId());
        order.setUserName(userName);
        order.setUserPhone(userPhone);
        order.setShippingAddress(shippingAddress);
        order.setTotalAmount(totalAmount);
        order.setProductCount(totalCount);
        order.setRemark(remark);
        order.setStatus("pending");
        
        OrderEntity savedOrder = orderService.saveOrder(order);

        // 扣减库存
        for (CartItemEntity item : selectedItems) {
            productDbService.getProductById(item.getProductId()).ifPresent(product -> {
                if (product.getStock() != null) {
                    product.setStock(product.getStock() - item.getQuantity());
                    productDbService.saveProduct(product);
                }
            });
        }

        // 清除已下单的购物车商品
        for (CartItemEntity item : selectedItems) {
            userService.removeCartItem(item.getId(), user.getId());
        }

        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("orderNo", savedOrder.getOrderNo());
        result.put("totalAmount", totalAmount);
        result.put("productCount", totalCount);
        return ResponseEntity.ok(result);
    }

    /**
     * 获取用户订单列表
     */
    @GetMapping("/orders")
    public ResponseEntity<?> getUserOrders(HttpServletRequest request) {
        UserEntity user = getCurrentUser(request);
        if (user == null) {
            return ResponseEntity.ok(List.of());
        }
        return ResponseEntity.ok(orderService.getOrdersByUserId(user.getId()));
    }

    // ===== 辅助方法 =====

    private String getTokenFromCookie(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if ("user_token".equals(cookie.getName())) {
                    return cookie.getValue();
                }
            }
        }
        return null;
    }

    private UserEntity getCurrentUser(HttpServletRequest request) {
        String token = getTokenFromCookie(request);
        if (token == null || token.isEmpty()) {
            return null;
        }
        return userService.getUserByToken(token).orElse(null);
    }

    private UserEntity getOrInitUser(HttpServletRequest request, HttpServletResponse response) {
        String token = getTokenFromCookie(request);
        UserEntity user = userService.getOrCreateUser(token);
        
        // 设置cookie
        Cookie cookie = new Cookie("user_token", user.getToken());
        cookie.setMaxAge(365 * 24 * 60 * 60);
        cookie.setPath("/");
        cookie.setHttpOnly(true);
        response.addCookie(cookie);
        
        return user;
    }
}
