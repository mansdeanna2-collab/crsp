package com.crsp.mall.repository;

import com.crsp.mall.entity.CartItemEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 购物车数据访问接口
 */
@Repository
public interface CartItemRepository extends JpaRepository<CartItemEntity, Long> {
    
    List<CartItemEntity> findByUserIdOrderByCreatedAtDesc(Long userId);
    
    List<CartItemEntity> findByUserIdAndSelectedTrue(Long userId);
    
    Optional<CartItemEntity> findByUserIdAndProductIdAndSpecName(Long userId, Long productId, String specName);
    
    void deleteByUserId(Long userId);
    
    long countByUserId(Long userId);
}
