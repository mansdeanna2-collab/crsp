package com.crsp.mall.repository;

import com.crsp.mall.entity.OrderEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 订单数据访问接口
 */
@Repository
public interface OrderRepository extends JpaRepository<OrderEntity, Long> {
    
    Optional<OrderEntity> findByOrderNo(String orderNo);
    
    List<OrderEntity> findByStatus(String status);
    
    List<OrderEntity> findByUserIdOrderByCreatedAtDesc(Long userId);
    
    List<OrderEntity> findAllByOrderByCreatedAtDesc();

    long countByUserId(Long userId);

    @Query("SELECT COALESCE(SUM(o.totalAmount), 0) FROM OrderEntity o WHERE o.userId = :userId AND o.status <> 'cancelled'")
    double sumTotalAmountByUserIdExcludingCancelled(@Param("userId") Long userId);
}
