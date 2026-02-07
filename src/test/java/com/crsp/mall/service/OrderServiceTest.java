package com.crsp.mall.service;

import com.crsp.mall.entity.OrderEntity;
import com.crsp.mall.repository.OrderRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class OrderServiceTest {

    @Autowired
    private OrderService orderService;

    @Autowired
    private OrderRepository orderRepository;

    @Test
    void updateOrderStatusRejectsInvalidStatus() {
        OrderEntity order = new OrderEntity();
        order.setUserName("测试");
        order.setTotalAmount(100.0);
        order.setStatus("pending");
        order = orderRepository.save(order);

        // Invalid status should return null
        OrderEntity result = orderService.updateOrderStatus(order.getId(), "hacked_status");
        assertNull(result);

        // Null status should return null
        result = orderService.updateOrderStatus(order.getId(), null);
        assertNull(result);

        // Verify order still has original status
        OrderEntity unchanged = orderRepository.findById(order.getId()).orElse(null);
        assertNotNull(unchanged);
        assertEquals("pending", unchanged.getStatus());
    }

    @Test
    void updateOrderStatusAcceptsValidStatuses() {
        OrderEntity order = new OrderEntity();
        order.setUserName("测试");
        order.setTotalAmount(100.0);
        order.setStatus("pending");
        order = orderRepository.save(order);

        // Valid status should work
        OrderEntity result = orderService.updateOrderStatus(order.getId(), "paid");
        assertNotNull(result);
        assertEquals("paid", result.getStatus());
        assertNotNull(result.getPaidAt());

        result = orderService.updateOrderStatus(order.getId(), "shipped");
        assertNotNull(result);
        assertEquals("shipped", result.getStatus());
        assertNotNull(result.getShippedAt());

        result = orderService.updateOrderStatus(order.getId(), "completed");
        assertNotNull(result);
        assertEquals("completed", result.getStatus());
        assertNotNull(result.getCompletedAt());

        result = orderService.updateOrderStatus(order.getId(), "cancelled");
        assertNotNull(result);
        assertEquals("cancelled", result.getStatus());
    }

    @Test
    void getOrdersByUserIdReturnsOnlyUserOrders() {
        OrderEntity order1 = new OrderEntity();
        order1.setUserId(1L);
        order1.setUserName("用户1");
        order1.setTotalAmount(100.0);
        order1.setStatus("pending");
        orderRepository.save(order1);

        OrderEntity order2 = new OrderEntity();
        order2.setUserId(2L);
        order2.setUserName("用户2");
        order2.setTotalAmount(200.0);
        order2.setStatus("paid");
        orderRepository.save(order2);

        var user1Orders = orderService.getOrdersByUserId(1L);
        assertEquals(1, user1Orders.size());
        assertEquals("用户1", user1Orders.get(0).getUserName());
    }
}
