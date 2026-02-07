package com.crsp.mall.controller;

import com.crsp.mall.entity.OrderEntity;
import com.crsp.mall.entity.UserEntity;
import com.crsp.mall.repository.OrderRepository;
import com.crsp.mall.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import jakarta.servlet.http.Cookie;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class UserApiControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserService userService;

    @Autowired
    private OrderRepository orderRepository;

    @Test
    void cancelOrderRejectNonPendingOrder() throws Exception {
        UserEntity user = userService.getOrCreateUser(null);
        Cookie cookie = new Cookie("user_token", user.getToken());

        OrderEntity order = new OrderEntity();
        order.setUserId(user.getId());
        order.setUserName("测试");
        order.setTotalAmount(100.0);
        order.setStatus("paid");
        order = orderRepository.save(order);

        mockMvc.perform(post("/api/user/orders/" + order.getId() + "/cancel").cookie(cookie))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("仅待付款订单可取消"));
    }

    @Test
    void cancelOrderSucceedsForPendingOrder() throws Exception {
        UserEntity user = userService.getOrCreateUser(null);
        Cookie cookie = new Cookie("user_token", user.getToken());

        OrderEntity order = new OrderEntity();
        order.setUserId(user.getId());
        order.setUserName("测试");
        order.setTotalAmount(100.0);
        order.setStatus("pending");
        order = orderRepository.save(order);

        mockMvc.perform(post("/api/user/orders/" + order.getId() + "/cancel").cookie(cookie))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    void cancelOrderRejectsOtherUsersOrder() throws Exception {
        UserEntity user1 = userService.getOrCreateUser(null);
        UserEntity user2 = userService.getOrCreateUser(null);
        Cookie cookie = new Cookie("user_token", user2.getToken());

        OrderEntity order = new OrderEntity();
        order.setUserId(user1.getId());
        order.setUserName("测试");
        order.setTotalAmount(100.0);
        order.setStatus("pending");
        order = orderRepository.save(order);

        mockMvc.perform(post("/api/user/orders/" + order.getId() + "/cancel").cookie(cookie))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("无权操作此订单"));
    }

    @Test
    void confirmOrderSucceedsForShippedOrder() throws Exception {
        UserEntity user = userService.getOrCreateUser(null);
        Cookie cookie = new Cookie("user_token", user.getToken());

        OrderEntity order = new OrderEntity();
        order.setUserId(user.getId());
        order.setUserName("测试");
        order.setTotalAmount(100.0);
        order.setStatus("shipped");
        order = orderRepository.save(order);

        mockMvc.perform(post("/api/user/orders/" + order.getId() + "/confirm").cookie(cookie))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    void confirmOrderRejectsNonShippedOrder() throws Exception {
        UserEntity user = userService.getOrCreateUser(null);
        Cookie cookie = new Cookie("user_token", user.getToken());

        OrderEntity order = new OrderEntity();
        order.setUserId(user.getId());
        order.setUserName("测试");
        order.setTotalAmount(100.0);
        order.setStatus("pending");
        order = orderRepository.save(order);

        mockMvc.perform(post("/api/user/orders/" + order.getId() + "/confirm").cookie(cookie))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("仅已发货订单可确认收货"));
    }

    @Test
    void updateProfileSucceedsWithValidData() throws Exception {
        UserEntity user = userService.getOrCreateUser(null);
        Cookie cookie = new Cookie("user_token", user.getToken());

        mockMvc.perform(put("/api/user/info").cookie(cookie)
                .contentType("application/json")
                .content("{\"nickname\":\"新昵称\",\"phone\":\"13900139000\",\"email\":\"test@example.com\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    void updateProfileRejectsInvalidPhone() throws Exception {
        UserEntity user = userService.getOrCreateUser(null);
        Cookie cookie = new Cookie("user_token", user.getToken());

        mockMvc.perform(put("/api/user/info").cookie(cookie)
                .contentType("application/json")
                .content("{\"phone\":\"12345\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("请输入正确的手机号码"));
    }

    @Test
    void updateProfileRejectsInvalidEmail() throws Exception {
        UserEntity user = userService.getOrCreateUser(null);
        Cookie cookie = new Cookie("user_token", user.getToken());

        mockMvc.perform(put("/api/user/info").cookie(cookie)
                .contentType("application/json")
                .content("{\"email\":\"not-an-email\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("请输入正确的邮箱地址"));
    }

    @Test
    void updateProfileRejectsEmptyNickname() throws Exception {
        UserEntity user = userService.getOrCreateUser(null);
        Cookie cookie = new Cookie("user_token", user.getToken());

        mockMvc.perform(put("/api/user/info").cookie(cookie)
                .contentType("application/json")
                .content("{\"nickname\":\"\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("昵称长度须为1-20个字符"));
    }

    @Test
    void updateProfileRejectsWithoutLogin() throws Exception {
        mockMvc.perform(put("/api/user/info")
                .contentType("application/json")
                .content("{\"nickname\":\"test\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("用户未登录"));
    }
}
