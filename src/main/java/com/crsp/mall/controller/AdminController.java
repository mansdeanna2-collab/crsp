package com.crsp.mall.controller;

import com.crsp.mall.entity.AdminEntity;
import com.crsp.mall.entity.OrderEntity;
import com.crsp.mall.entity.ProductEntity;
import com.crsp.mall.entity.PromotionEntity;
import com.crsp.mall.entity.UserEntity;
import com.crsp.mall.service.AdminService;
import com.crsp.mall.service.OrderService;
import com.crsp.mall.service.ProductDbService;
import com.crsp.mall.service.PromotionService;
import com.crsp.mall.service.UserService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * 后台管理控制器
 */
@Controller
@RequestMapping("/admin")
public class AdminController {

    private static final Set<String> VALID_ORDER_STATUSES = Set.of("pending", "paid", "shipped", "completed", "cancelled");
    private static final Set<String> VALID_USER_TYPES = Set.of("guest", "user");

    @Autowired
    private AdminService adminService;

    @Autowired
    private ProductDbService productDbService;

    @Autowired
    private OrderService orderService;

    @Autowired
    private UserService userService;

    @Autowired
    private PromotionService promotionService;

    /**
     * 登录页面
     */
    @GetMapping("/login")
    public String loginPage(HttpSession session) {
        if (session.getAttribute("admin") != null) {
            return "redirect:/admin";
        }
        return "admin/login";
    }

    /**
     * 登录处理
     */
    @PostMapping("/login")
    public String login(@RequestParam String username, 
                       @RequestParam String password,
                       HttpSession session,
                       RedirectAttributes redirectAttributes) {
        AdminEntity admin = adminService.login(username, password);
        if (admin != null) {
            session.setAttribute("admin", admin);
            return "redirect:/admin";
        }
        redirectAttributes.addFlashAttribute("error", "用户名或密码错误");
        return "redirect:/admin/login";
    }

    /**
     * 退出登录
     */
    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.removeAttribute("admin");
        return "redirect:/admin/login";
    }

    /**
     * 后台首页 - 仪表盘
     */
    @GetMapping({"", "/"})
    public String dashboard(HttpSession session, Model model) {
        if (session.getAttribute("admin") == null) {
            return "redirect:/admin/login";
        }
        
        // 统计数据
        long productCount = productDbService.getAllProducts().size();
        List<OrderEntity> allOrders = orderService.getAllOrders();
        long orderCount = allOrders.size();
        long pendingOrders = allOrders.stream().filter(o -> "pending".equals(o.getStatus())).count();
        long userCount = userService.getUserCount();
        long registeredCount = userService.getRegisteredCount();
        long activeUserCount = userService.getActiveCount();
        double totalRevenue = allOrders.stream()
                .filter(o -> !"cancelled".equals(o.getStatus()))
                .mapToDouble(OrderEntity::getTotalAmount)
                .sum();
        
        model.addAttribute("productCount", productCount);
        model.addAttribute("orderCount", orderCount);
        model.addAttribute("pendingOrders", pendingOrders);
        model.addAttribute("userCount", userCount);
        model.addAttribute("registeredCount", registeredCount);
        model.addAttribute("activeUserCount", activeUserCount);
        model.addAttribute("totalRevenue", totalRevenue);
        model.addAttribute("recentOrders", allOrders.stream().limit(5).toList());
        model.addAttribute("currentPage", "dashboard");
        model.addAttribute("promotionCounts", promotionService.getPromotionCounts());
        
        return "admin/dashboard";
    }

    /**
     * 商品管理页面
     */
    @GetMapping("/products")
    public String productList(HttpSession session, Model model) {
        if (session.getAttribute("admin") == null) {
            return "redirect:/admin/login";
        }
        
        List<ProductEntity> products = productDbService.getAllProducts();
        model.addAttribute("products", products);
        model.addAttribute("currentPage", "products");
        
        return "admin/products";
    }

    /**
     * 添加商品页面
     */
    @GetMapping("/products/add")
    public String addProductPage(HttpSession session, Model model) {
        if (session.getAttribute("admin") == null) {
            return "redirect:/admin/login";
        }
        
        model.addAttribute("product", new ProductEntity());
        model.addAttribute("currentPage", "products");
        
        return "admin/product-form";
    }

    /**
     * 编辑商品页面
     */
    @GetMapping("/products/edit/{id}")
    public String editProductPage(@PathVariable Long id, HttpSession session, Model model) {
        if (session.getAttribute("admin") == null) {
            return "redirect:/admin/login";
        }
        
        Optional<ProductEntity> product = productDbService.getProductById(id);
        if (product.isEmpty()) {
            return "redirect:/admin/products";
        }
        
        model.addAttribute("product", product.get());
        model.addAttribute("currentPage", "products");
        
        return "admin/product-form";
    }

    /**
     * 保存商品
     */
    @PostMapping("/products/save")
    public String saveProduct(@ModelAttribute ProductEntity product, 
                             HttpSession session,
                             RedirectAttributes redirectAttributes) {
        if (session.getAttribute("admin") == null) {
            return "redirect:/admin/login";
        }
        
        productDbService.saveProduct(product);
        redirectAttributes.addFlashAttribute("success", "商品保存成功");
        return "redirect:/admin/products";
    }

    /**
     * 删除商品
     */
    @PostMapping("/products/delete/{id}")
    public String deleteProduct(@PathVariable Long id, 
                               HttpSession session,
                               RedirectAttributes redirectAttributes) {
        if (session.getAttribute("admin") == null) {
            return "redirect:/admin/login";
        }
        
        productDbService.deleteProduct(id);
        redirectAttributes.addFlashAttribute("success", "商品删除成功");
        return "redirect:/admin/products";
    }

    /**
     * 订单管理页面
     */
    @GetMapping("/orders")
    public String orderList(@RequestParam(required = false) String status,
                           HttpSession session, Model model) {
        if (session.getAttribute("admin") == null) {
            return "redirect:/admin/login";
        }
        
        List<OrderEntity> orders;
        if (status != null && !status.isEmpty() && VALID_ORDER_STATUSES.contains(status)) {
            orders = orderService.getOrdersByStatus(status);
        } else {
            orders = orderService.getAllOrders();
        }
        
        model.addAttribute("orders", orders);
        model.addAttribute("selectedStatus", status);
        model.addAttribute("currentPage", "orders");
        
        return "admin/orders";
    }

    /**
     * 更新订单状态
     */
    @PostMapping("/orders/status/{id}")
    public String updateOrderStatus(@PathVariable Long id, 
                                   @RequestParam String status,
                                   HttpSession session,
                                   RedirectAttributes redirectAttributes) {
        if (session.getAttribute("admin") == null) {
            return "redirect:/admin/login";
        }
        
        OrderEntity updated = orderService.updateOrderStatus(id, status);
        if (updated == null) {
            redirectAttributes.addFlashAttribute("error", "订单状态更新失败，请检查状态转换是否合法");
        } else {
            redirectAttributes.addFlashAttribute("success", "订单状态更新成功");
        }
        return "redirect:/admin/orders";
    }

    /**
     * 删除订单
     */
    @PostMapping("/orders/delete/{id}")
    public String deleteOrder(@PathVariable Long id,
                             HttpSession session,
                             RedirectAttributes redirectAttributes) {
        if (session.getAttribute("admin") == null) {
            return "redirect:/admin/login";
        }
        
        orderService.deleteOrder(id);
        redirectAttributes.addFlashAttribute("success", "订单删除成功");
        return "redirect:/admin/orders";
    }

    /**
     * 用户管理页面
     */
    @GetMapping("/users")
    public String userList(@RequestParam(required = false) String keyword,
                           @RequestParam(required = false) String type,
                           HttpSession session, Model model) {
        if (session.getAttribute("admin") == null) {
            return "redirect:/admin/login";
        }
        
        List<UserEntity> users = userService.getAllUsers();
        
        // 按类型过滤
        if (type != null && !type.isEmpty() && VALID_USER_TYPES.contains(type)) {
            users = users.stream().filter(u -> type.equals(u.getUserType())).toList();
        }
        // 按关键词搜索(昵称、手机或邮箱)
        if (keyword != null && !keyword.trim().isEmpty()) {
            String kw = keyword.trim().toLowerCase();
            users = users.stream().filter(u -> 
                (u.getNickname() != null && u.getNickname().toLowerCase().contains(kw)) ||
                (u.getPhone() != null && u.getPhone().contains(kw)) ||
                (u.getEmail() != null && u.getEmail().toLowerCase().contains(kw))
            ).toList();
        }
        
        model.addAttribute("users", users);
        model.addAttribute("currentPage", "users");
        model.addAttribute("guestCount", userService.getGuestCount());
        model.addAttribute("totalCount", userService.getUserCount());
        model.addAttribute("registeredCount", userService.getRegisteredCount());
        model.addAttribute("activeCount", userService.getActiveCount());
        model.addAttribute("newToday", userService.getNewUserCount(1));
        model.addAttribute("newThisWeek", userService.getNewUserCount(7));
        model.addAttribute("newThisMonth", userService.getNewUserCount(30));
        model.addAttribute("keyword", keyword);
        model.addAttribute("selectedType", type);
        
        return "admin/users";
    }

    /**
     * 用户详情页面
     */
    @GetMapping("/users/{id}")
    public String userDetail(@PathVariable Long id, HttpSession session, Model model) {
        if (session.getAttribute("admin") == null) {
            return "redirect:/admin/login";
        }
        
        Optional<UserEntity> userOpt = userService.getUserById(id);
        if (userOpt.isEmpty()) {
            return "redirect:/admin/users";
        }
        
        UserEntity user = userOpt.get();
        double totalSpending = userService.getUserTotalSpending(user.getId());
        model.addAttribute("user", user);
        model.addAttribute("currentPage", "users");
        model.addAttribute("orders", orderService.getOrdersByUserId(id));
        model.addAttribute("favorites", userService.getFavorites(id));
        model.addAttribute("cartItems", userService.getCartItems(id));
        model.addAttribute("browsingHistory", userService.getBrowsingHistory(id));
        model.addAttribute("totalSpending", totalSpending);
        model.addAttribute("userLevel", UserEntity.calculateLevel(totalSpending));
        model.addAttribute("orderCount", userService.getUserOrderCount(id));
        
        return "admin/user-detail";
    }

    /**
     * 删除用户
     */
    @PostMapping("/users/delete/{id}")
    public String deleteUser(@PathVariable Long id,
                            HttpSession session,
                            RedirectAttributes redirectAttributes) {
        if (session.getAttribute("admin") == null) {
            return "redirect:/admin/login";
        }
        
        userService.deleteUser(id);
        redirectAttributes.addFlashAttribute("success", "用户删除成功");
        return "redirect:/admin/users";
    }

    /**
     * 编辑用户信息
     */
    @PostMapping("/users/edit/{id}")
    public String editUser(@PathVariable Long id,
                          @RequestParam(required = false) String nickname,
                          @RequestParam(required = false) String phone,
                          @RequestParam(required = false) String email,
                          @RequestParam(required = false) String address,
                          @RequestParam(required = false) String gender,
                          @RequestParam(required = false) String remark,
                          @RequestParam(required = false) String userType,
                          @RequestParam(required = false) Boolean active,
                          HttpSession session,
                          RedirectAttributes redirectAttributes) {
        if (session.getAttribute("admin") == null) {
            return "redirect:/admin/login";
        }

        Optional<UserEntity> userOpt = userService.getUserById(id);
        if (userOpt.isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "用户不存在");
            return "redirect:/admin/users";
        }

        UserEntity user = userOpt.get();
        if (nickname != null) {
            String trimmedNickname = nickname.trim();
            if (!trimmedNickname.isEmpty() && trimmedNickname.length() <= 20) {
                user.setNickname(trimmedNickname);
            }
        }
        if (phone != null) {
            String trimmedPhone = phone.trim();
            if (!trimmedPhone.isEmpty() && !trimmedPhone.matches("^1[3-9]\\d{9}$")) {
                redirectAttributes.addFlashAttribute("error", "请输入正确的手机号码");
                return "redirect:/admin/users/" + id;
            }
            user.setPhone(trimmedPhone.isEmpty() ? null : trimmedPhone);
        }
        if (email != null) {
            String trimmedEmail = email.trim();
            if (trimmedEmail.isEmpty()) {
                user.setEmail(null);
            } else if (trimmedEmail.matches("^[\\w]([\\w.-]*[\\w])?@[\\w]([\\w.-]*[\\w])?\\.[a-zA-Z]{2,}$")) {
                user.setEmail(trimmedEmail);
            }
        }
        if (address != null) {
            String trimmedAddress = address.trim();
            user.setAddress(trimmedAddress.isEmpty() ? null : trimmedAddress);
        }
        if (gender != null && ("male".equals(gender) || "female".equals(gender) || "unknown".equals(gender))) {
            user.setGender(gender);
        }
        if (remark != null) {
            String trimmedRemark = remark.trim();
            user.setRemark(trimmedRemark.isEmpty() ? null : trimmedRemark);
        }
        if (userType != null && ("guest".equals(userType) || "user".equals(userType))) {
            user.setUserType(userType);
        }
        if (active != null) {
            user.setActive(active);
        }

        userService.saveUser(user);
        redirectAttributes.addFlashAttribute("success", "用户信息更新成功");
        return "redirect:/admin/users/" + id;
    }

    /**
     * 切换用户启用/禁用状态
     */
    @PostMapping("/users/toggle-status/{id}")
    public String toggleUserStatus(@PathVariable Long id,
                                   HttpSession session,
                                   RedirectAttributes redirectAttributes) {
        if (session.getAttribute("admin") == null) {
            return "redirect:/admin/login";
        }

        Optional<UserEntity> userOpt = userService.getUserById(id);
        if (userOpt.isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "用户不存在");
            return "redirect:/admin/users";
        }

        UserEntity user = userOpt.get();
        user.setActive(!Boolean.TRUE.equals(user.getActive()));
        userService.saveUser(user);
        redirectAttributes.addFlashAttribute("success",
                Boolean.TRUE.equals(user.getActive()) ? "用户已启用" : "用户已禁用");
        return "redirect:/admin/users";
    }

    /**
     * 修改密码页面
     */
    @GetMapping("/password")
    public String passwordPage(HttpSession session, Model model) {
        if (session.getAttribute("admin") == null) {
            return "redirect:/admin/login";
        }
        model.addAttribute("currentPage", "password");
        return "admin/password";
    }

    /**
     * 修改密码处理
     */
    @PostMapping("/password")
    public String changePassword(@RequestParam String oldPassword,
                                @RequestParam String newPassword,
                                @RequestParam String confirmPassword,
                                HttpSession session,
                                RedirectAttributes redirectAttributes) {
        if (session.getAttribute("admin") == null) {
            return "redirect:/admin/login";
        }

        if (newPassword == null || newPassword.length() < 6 || newPassword.length() > 32) {
            redirectAttributes.addFlashAttribute("error", "新密码长度须为6-32个字符");
            return "redirect:/admin/password";
        }
        if (!newPassword.equals(confirmPassword)) {
            redirectAttributes.addFlashAttribute("error", "两次输入的密码不一致");
            return "redirect:/admin/password";
        }

        AdminEntity admin = (AdminEntity) session.getAttribute("admin");
        boolean success = adminService.changePassword(admin.getId(), oldPassword, newPassword);
        if (success) {
            // 刷新session中的管理员信息
            adminService.getAdminById(admin.getId()).ifPresent(
                    updatedAdmin -> session.setAttribute("admin", updatedAdmin)
            );
            redirectAttributes.addFlashAttribute("success", "密码修改成功");
        } else {
            redirectAttributes.addFlashAttribute("error", "原密码错误");
        }
        return "redirect:/admin/password";
    }

    // ==================== 促销活动管理 ====================

    /**
     * 促销活动管理页面
     */
    @GetMapping("/promotions")
    public String promotionList(@RequestParam(required = false) String type,
                               HttpSession session, Model model) {
        if (session.getAttribute("admin") == null) {
            return "redirect:/admin/login";
        }

        List<PromotionEntity> promotions;
        if (type != null && !type.isEmpty()) {
            promotions = promotionService.getPromotionsByType(type);
        } else {
            promotions = promotionService.getAllPromotions();
        }

        model.addAttribute("promotions", promotions);
        model.addAttribute("selectedType", type);
        model.addAttribute("products", productDbService.getAllProducts());
        model.addAttribute("currentPage", "promotions");
        model.addAttribute("promotionCounts", promotionService.getPromotionCounts());

        return "admin/promotions";
    }

    /**
     * 添加促销活动页面
     */
    @GetMapping("/promotions/add")
    public String addPromotionPage(HttpSession session, Model model) {
        if (session.getAttribute("admin") == null) {
            return "redirect:/admin/login";
        }

        model.addAttribute("promotion", new PromotionEntity());
        model.addAttribute("products", productDbService.getActiveProducts());
        model.addAttribute("currentPage", "promotions");

        return "admin/promotion-form";
    }

    /**
     * 编辑促销活动页面
     */
    @GetMapping("/promotions/edit/{id}")
    public String editPromotionPage(@PathVariable Long id, HttpSession session, Model model) {
        if (session.getAttribute("admin") == null) {
            return "redirect:/admin/login";
        }

        Optional<PromotionEntity> promotion = promotionService.getPromotionById(id);
        if (promotion.isEmpty()) {
            return "redirect:/admin/promotions";
        }

        model.addAttribute("promotion", promotion.get());
        model.addAttribute("products", productDbService.getActiveProducts());
        model.addAttribute("currentPage", "promotions");

        return "admin/promotion-form";
    }

    /**
     * 保存促销活动
     */
    @PostMapping("/promotions/save")
    public String savePromotion(@ModelAttribute PromotionEntity promotion,
                               HttpSession session,
                               RedirectAttributes redirectAttributes) {
        if (session.getAttribute("admin") == null) {
            return "redirect:/admin/login";
        }

        promotionService.savePromotion(promotion);
        redirectAttributes.addFlashAttribute("success", "促销活动保存成功");
        return "redirect:/admin/promotions";
    }

    /**
     * 删除促销活动
     */
    @PostMapping("/promotions/delete/{id}")
    public String deletePromotion(@PathVariable Long id,
                                 HttpSession session,
                                 RedirectAttributes redirectAttributes) {
        if (session.getAttribute("admin") == null) {
            return "redirect:/admin/login";
        }

        promotionService.deletePromotion(id);
        redirectAttributes.addFlashAttribute("success", "促销活动删除成功");
        return "redirect:/admin/promotions";
    }
}
