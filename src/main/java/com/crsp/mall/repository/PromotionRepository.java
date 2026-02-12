package com.crsp.mall.repository;

import com.crsp.mall.entity.PromotionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 促销活动数据访问接口
 */
@Repository
public interface PromotionRepository extends JpaRepository<PromotionEntity, Long> {

    List<PromotionEntity> findByTypeOrderBySortOrderAsc(String type);

    List<PromotionEntity> findByTypeAndActiveTrueOrderBySortOrderAsc(String type);

    @Query("""
        SELECT p FROM PromotionEntity p
        WHERE p.type = :type AND p.active = true
        AND (p.startTime IS NULL OR p.startTime <= :now)
        AND (p.endTime IS NULL OR p.endTime >= :now)
        ORDER BY p.sortOrder ASC
        """)
    List<PromotionEntity> findActivePromotionsByType(@Param("type") String type, @Param("now") LocalDateTime now);

    List<PromotionEntity> findByProductId(Long productId);
}
