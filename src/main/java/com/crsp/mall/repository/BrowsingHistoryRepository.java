package com.crsp.mall.repository;

import com.crsp.mall.entity.BrowsingHistoryEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 浏览历史数据访问接口
 */
@Repository
public interface BrowsingHistoryRepository extends JpaRepository<BrowsingHistoryEntity, Long> {
    
    List<BrowsingHistoryEntity> findByUserIdOrderByViewedAtDesc(Long userId);
    
    Optional<BrowsingHistoryEntity> findTopByUserIdAndProductIdOrderByViewedAtDesc(Long userId, Long productId);
    
    long countByUserId(Long userId);
    
    void deleteByUserId(Long userId);
}
