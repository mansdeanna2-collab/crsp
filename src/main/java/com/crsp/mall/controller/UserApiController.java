package com.crsp.mall.controller;

import com.crsp.mall.entity.*;
import com.crsp.mall.service.OrderService;
import com.crsp.mall.service.ProductDbService;
import com.crsp.mall.service.UserService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
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

    @Value("${server.cookie.secure:false}")
    private boolean secureCookie;

    /**
     * 获取或创建用户（自动注册游客）
     */
    @PostMapping("/init")
    public ResponseEntity<Map<String, Object>> initUser(HttpServletRequest request, HttpServletResponse response) {
        String token = getTokenFromCookie(request);
        UserEntity user = userService.getOrCreateUser(token);
        
        // 设置cookie
        setUserTokenCookie(response, user.getToken());
        
        Map<String, Object> result = new HashMap<>();
        result.put("id", user.getId());
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

    /**
     * 更新用户个人信息
     */
    @PutMapping("/info")
    public ResponseEntity<?> updateUserInfo(@RequestBody Map<String, Object> body, HttpServletRequest request) {
        UserEntity user = getCurrentUser(request);
        if (user == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "用户未登录"));
        }

        if (body.containsKey("nickname")) {
            String nickname = body.get("nickname") != null ? body.get("nickname").toString().trim() : "";
            if (nickname.isEmpty() || nickname.length() > 20) {
                return ResponseEntity.badRequest().body(Map.of("error", "昵称长度须为1-20个字符"));
            }
            user.setNickname(nickname);
        }

        if (body.containsKey("phone")) {
            String phone = body.get("phone") != null ? body.get("phone").toString().trim() : "";
            if (!phone.isEmpty() && !phone.matches("^1[3-9]\\d{9}$")) {
                return ResponseEntity.badRequest().body(Map.of("error", "请输入正确的手机号码"));
            }
            user.setPhone(phone.isEmpty() ? null : phone);
        }

        if (body.containsKey("email")) {
            String email = body.get("email") != null ? body.get("email").toString().trim() : "";
            if (!email.isEmpty() && !email.matches("^[\\w]([\\w.-]*[\\w])?@[\\w]([\\w.-]*[\\w])?\\.[a-zA-Z]{2,}$")) {
                return ResponseEntity.badRequest().body(Map.of("error", "请输入正确的邮箱地址"));
            }
            user.setEmail(email.isEmpty() ? null : email);
        }

        userService.saveUser(user);
        return ResponseEntity.ok(Map.of("success", true));
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
    @Transactional
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

        // 验证库存并计算总价（使用当前数据库中的实际价格）
        double totalAmount = 0;
        int totalCount = 0;
        StringBuilder priceChanges = new StringBuilder();
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
            // 检测价格变化，若价格已变需要用户重新确认
            if (item.getProductPrice() != null && Math.abs(product.getPrice() - item.getProductPrice()) > 0.01) {
                priceChanges.append(String.format("「%s」 ¥%.2f → ¥%.2f；", 
                    item.getProductTitle(), item.getProductPrice(), product.getPrice()));
                // 同步更新购物车中的价格快照
                item.setProductPrice(product.getPrice());
                userService.saveCartItem(item);
            }
            totalAmount += product.getPrice() * item.getQuantity();
            totalCount += item.getQuantity();
        }

        // 若有价格变化，拒绝下单并告知用户
        if (priceChanges.length() > 0) {
            return ResponseEntity.badRequest().body(Map.of(
                "error", "以下商品价格已变动，请确认后重新提交：" + priceChanges,
                "priceChanged", true
            ));
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

        // 扣减库存（使用悲观锁防止并发超卖）
        for (CartItemEntity item : selectedItems) {
            productDbService.getProductByIdForUpdate(item.getProductId()).ifPresent(product -> {
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

    /**
     * 取消订单（仅限待付款状态）
     */
    @PostMapping("/orders/{orderId}/cancel")
    public ResponseEntity<?> cancelOrder(@PathVariable Long orderId, HttpServletRequest request) {
        ResponseEntity<?> error = validateUserOrder(request, orderId, "pending", "仅待付款订单可取消");
        if (error != null) return error;
        OrderEntity updated = orderService.updateOrderStatus(orderId, "cancelled");
        if (updated == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "取消失败"));
        }
        return ResponseEntity.ok(Map.of("success", true));
    }

    /**
     * 确认收货（仅限已发货状态）
     */
    @PostMapping("/orders/{orderId}/confirm")
    public ResponseEntity<?> confirmOrder(@PathVariable Long orderId, HttpServletRequest request) {
        ResponseEntity<?> error = validateUserOrder(request, orderId, "shipped", "仅已发货订单可确认收货");
        if (error != null) return error;
        OrderEntity updated = orderService.updateOrderStatus(orderId, "completed");
        if (updated == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "确认失败"));
        }
        return ResponseEntity.ok(Map.of("success", true));
    }

    /**
     * 验证用户订单：检查登录、订单存在、归属权、状态
     * @return null表示验证通过，否则返回错误响应
     */
    private ResponseEntity<?> validateUserOrder(HttpServletRequest request, Long orderId,
                                                 String requiredStatus, String statusError) {
        UserEntity user = getCurrentUser(request);
        if (user == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "用户未登录"));
        }
        Optional<OrderEntity> orderOpt = orderService.getOrderById(orderId);
        if (orderOpt.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "订单不存在"));
        }
        OrderEntity order = orderOpt.get();
        if (!user.getId().equals(order.getUserId())) {
            return ResponseEntity.badRequest().body(Map.of("error", "无权操作此订单"));
        }
        if (!requiredStatus.equals(order.getStatus())) {
            return ResponseEntity.badRequest().body(Map.of("error", statusError));
        }
        return null;
    }

    // ===== 辅助方法 =====

    private void setUserTokenCookie(HttpServletResponse response, String token) {
        Cookie cookie = new Cookie("user_token", token);
        cookie.setMaxAge(365 * 24 * 60 * 60); // 1年
        cookie.setPath("/");
        cookie.setHttpOnly(true);
        cookie.setSecure(secureCookie);
        cookie.setAttribute("SameSite", "Lax");
        response.addCookie(cookie);
    }

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
        setUserTokenCookie(response, user.getToken());
        
        return user;
    }
}
