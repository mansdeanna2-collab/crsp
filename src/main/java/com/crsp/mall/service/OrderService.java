package com.crsp.mall.service;

import com.crsp.mall.entity.OrderEntity;
import com.crsp.mall.repository.OrderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * 订单服务类
 */
@Service
public class OrderService {

    @Autowired
    private OrderRepository orderRepository;

    /**
     * 获取所有订单
     */
    public List<OrderEntity> getAllOrders() {
        return orderRepository.findAllByOrderByCreatedAtDesc();
    }

    /**
     * 根据ID获取订单
     */
    public Optional<OrderEntity> getOrderById(Long id) {
        return orderRepository.findById(id);
    }

    /**
     * 根据订单号获取订单
     */
    public Optional<OrderEntity> getOrderByNo(String orderNo) {
        return orderRepository.findByOrderNo(orderNo);
    }

    /**
     * 根据状态获取订单
     */
    public List<OrderEntity> getOrdersByStatus(String status) {
        return orderRepository.findByStatus(status);
    }

    /**
     * 根据用户ID获取订单
     */
    public List<OrderEntity> getOrdersByUserId(Long userId) {
        return orderRepository.findByUserIdOrderByCreatedAtDesc(userId);
    }

    /**
     * 保存订单
     */
    public OrderEntity saveOrder(OrderEntity order) {
        return orderRepository.save(order);
    }

    private static final Set<String> VALID_STATUSES = Set.of("pending", "paid", "shipped", "completed", "cancelled");

    // Valid state transitions: current status -> allowed next statuses
    private static final Map<String, Set<String>> VALID_TRANSITIONS = Map.of(
        "pending", Set.of("paid", "cancelled"),
        "paid", Set.of("shipped", "cancelled"),
        "shipped", Set.of("completed"),
        "completed", Set.of(),
        "cancelled", Set.of()
    );

    /**
     * 更新订单状态（验证状态转换合法性）
     */
    @Transactional
    public OrderEntity updateOrderStatus(Long id, String status) {
        if (status == null || !VALID_STATUSES.contains(status)) {
            return null;
        }
        Optional<OrderEntity> optionalOrder = orderRepository.findById(id);
        if (optionalOrder.isPresent()) {
            OrderEntity order = optionalOrder.get();
            // Validate state transition
            Set<String> allowedNextStatuses = VALID_TRANSITIONS.getOrDefault(order.getStatus(), Set.of());
            if (!allowedNextStatuses.contains(status)) {
                return null;
            }
            order.setStatus(status);
            
            // 更新相应的时间戳
            switch (status) {
                case "paid" -> order.setPaidAt(LocalDateTime.now());
                case "shipped" -> order.setShippedAt(LocalDateTime.now());
                case "completed" -> order.setCompletedAt(LocalDateTime.now());
                case "cancelled" -> order.setCancelledAt(LocalDateTime.now());
            }
            
            return orderRepository.save(order);
        }
        return null;
    }

    /**
     * 删除订单
     */
    public void deleteOrder(Long id) {
        orderRepository.deleteById(id);
    }

    /**
     * 初始化示例订单数据（已移除默认示例订单）
     */
    public void initSampleOrders() {
        // no-op
    }

    private void createSampleOrder(String userName, String phone, String address, 
                                    double amount, int count, String status) {
        OrderEntity order = new OrderEntity();
        order.setUserName(userName);
        order.setUserPhone(phone);
        order.setShippingAddress(address);
        order.setTotalAmount(amount);
        order.setProductCount(count);
        order.setStatus(status);
        orderRepository.save(order);
    }
}
