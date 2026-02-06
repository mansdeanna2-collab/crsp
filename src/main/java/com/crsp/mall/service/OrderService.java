package com.crsp.mall.service;

import com.crsp.mall.entity.OrderEntity;
import com.crsp.mall.repository.OrderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

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
     * 保存订单
     */
    public OrderEntity saveOrder(OrderEntity order) {
        return orderRepository.save(order);
    }

    /**
     * 更新订单状态
     */
    public OrderEntity updateOrderStatus(Long id, String status) {
        Optional<OrderEntity> optionalOrder = orderRepository.findById(id);
        if (optionalOrder.isPresent()) {
            OrderEntity order = optionalOrder.get();
            order.setStatus(status);
            
            // 更新相应的时间戳
            switch (status) {
                case "paid" -> order.setPaidAt(LocalDateTime.now());
                case "shipped" -> order.setShippedAt(LocalDateTime.now());
                case "completed" -> order.setCompletedAt(LocalDateTime.now());
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
     * 初始化示例订单数据
     */
    public void initSampleOrders() {
        if (orderRepository.count() == 0) {
            createSampleOrder("张三", "13800138001", "北京市朝阳区xxx街道", 128.00, 1, "pending");
            createSampleOrder("李四", "13800138002", "上海市浦东新区xxx路", 256.00, 2, "paid");
            createSampleOrder("王五", "13800138003", "广州市天河区xxx大道", 89.00, 1, "shipped");
            createSampleOrder("赵六", "13800138004", "深圳市南山区xxx中心", 458.00, 3, "completed");
        }
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
