package com.crsp.mall.repository;

import com.crsp.mall.entity.FavoriteEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 收藏数据访问接口
 */
@Repository
public interface FavoriteRepository extends JpaRepository<FavoriteEntity, Long> {
    
    List<FavoriteEntity> findByUserIdOrderByCreatedAtDesc(Long userId);
    
    Optional<FavoriteEntity> findByUserIdAndProductId(Long userId, Long productId);
    
    boolean existsByUserIdAndProductId(Long userId, Long productId);
    
    void deleteByUserIdAndProductId(Long userId, Long productId);
    
    void deleteByUserId(Long userId);
    
    long countByUserId(Long userId);
}
