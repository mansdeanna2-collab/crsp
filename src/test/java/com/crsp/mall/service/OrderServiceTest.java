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
}
