package com.crsp.mall.controller;

import com.crsp.mall.entity.AdminEntity;
import com.crsp.mall.entity.OrderEntity;
import com.crsp.mall.entity.UserEntity;
import com.crsp.mall.repository.OrderRepository;
import com.crsp.mall.service.AdminService;
import com.crsp.mall.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class AdminControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private AdminService adminService;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private UserService userService;

    private MockHttpSession adminSession;

    @BeforeEach
    void setUp() {
        adminService.initDefaultAdmin();
        AdminEntity admin = adminService.login("admin", "admin123");
        adminSession = new MockHttpSession();
        adminSession.setAttribute("admin", admin);
    }

    @Test
    void updateOrderStatusShowsErrorForInvalidTransition() throws Exception {
        OrderEntity order = new OrderEntity();
        order.setUserName("测试");
        order.setTotalAmount(100.0);
        order.setStatus("completed");
        order = orderRepository.save(order);

        // completed -> pending is not a valid transition
        mockMvc.perform(post("/admin/orders/status/" + order.getId())
                .session(adminSession)
                .param("status", "pending"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/orders"))
                .andExpect(flash().attribute("error", "订单状态更新失败，请检查状态转换是否合法"));
    }

    @Test
    void updateOrderStatusShowsSuccessForValidTransition() throws Exception {
        OrderEntity order = new OrderEntity();
        order.setUserName("测试");
        order.setTotalAmount(100.0);
        order.setStatus("pending");
        order = orderRepository.save(order);

        // pending -> paid is a valid transition
        mockMvc.perform(post("/admin/orders/status/" + order.getId())
                .session(adminSession)
                .param("status", "paid"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/orders"))
                .andExpect(flash().attribute("success", "订单状态更新成功"));
    }

    @Test
    void editUserRejectsInvalidPhone() throws Exception {
        UserEntity user = userService.getOrCreateUser(null);

        mockMvc.perform(post("/admin/users/edit/" + user.getId())
                .session(adminSession)
                .param("phone", "12345"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/users/" + user.getId()))
                .andExpect(flash().attribute("error", "请输入正确的手机号码"));
    }

    @Test
    void editUserAcceptsValidPhone() throws Exception {
        UserEntity user = userService.getOrCreateUser(null);

        mockMvc.perform(post("/admin/users/edit/" + user.getId())
                .session(adminSession)
                .param("phone", "13900139000"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/users/" + user.getId()))
                .andExpect(flash().attribute("success", "用户信息更新成功"));
    }

    @Test
    void editUserAcceptsEmptyPhone() throws Exception {
        UserEntity user = userService.getOrCreateUser(null);

        mockMvc.perform(post("/admin/users/edit/" + user.getId())
                .session(adminSession)
                .param("phone", ""))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/users/" + user.getId()))
                .andExpect(flash().attribute("success", "用户信息更新成功"));
    }
}
