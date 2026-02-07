package com.crsp.mall.controller;

import com.crsp.mall.entity.AdminEntity;
import com.crsp.mall.entity.OrderEntity;
import com.crsp.mall.entity.ProductEntity;
import com.crsp.mall.entity.UserEntity;
import com.crsp.mall.service.AdminService;
import com.crsp.mall.service.OrderService;
import com.crsp.mall.service.ProductDbService;
import com.crsp.mall.service.UserService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Optional;

/**
 * 后台管理控制器
 */
@Controller
@RequestMapping("/admin")
public class AdminController {

    @Autowired
    private AdminService adminService;

    @Autowired
    private ProductDbService productDbService;

    @Autowired
    private OrderService orderService;

    @Autowired
    private UserService userService;

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
        long orderCount = orderService.getAllOrders().size();
        long pendingOrders = orderService.getOrdersByStatus("pending").size();
        long userCount = userService.getUserCount();
        
        model.addAttribute("productCount", productCount);
        model.addAttribute("orderCount", orderCount);
        model.addAttribute("pendingOrders", pendingOrders);
        model.addAttribute("userCount", userCount);
        model.addAttribute("recentOrders", orderService.getAllOrders().stream().limit(5).toList());
        model.addAttribute("currentPage", "dashboard");
        
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
        if (status != null && !status.isEmpty()) {
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
        
        orderService.updateOrderStatus(id, status);
        redirectAttributes.addFlashAttribute("success", "订单状态更新成功");
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
    public String userList(HttpSession session, Model model) {
        if (session.getAttribute("admin") == null) {
            return "redirect:/admin/login";
        }
        
        List<UserEntity> users = userService.getAllUsers();
        model.addAttribute("users", users);
        model.addAttribute("currentPage", "users");
        model.addAttribute("guestCount", userService.getGuestCount());
        model.addAttribute("totalCount", userService.getUserCount());
        
        return "admin/users";
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
}
