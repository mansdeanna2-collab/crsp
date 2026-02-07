package com.crsp.mall.repository;

import com.crsp.mall.entity.ProductEntity;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 商品数据访问接口
 */
@Repository
public interface ProductRepository extends JpaRepository<ProductEntity, Long> {
    
    List<ProductEntity> findByActiveTrue();

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT p FROM ProductEntity p WHERE p.id = :id")
    Optional<ProductEntity> findByIdForUpdate(@Param("id") Long id);

    @Query("""
        SELECT p FROM ProductEntity p
        WHERE p.active = true AND (
            LOWER(p.title) LIKE LOWER(CONCAT('%', :keyword, '%'))
            OR LOWER(COALESCE(p.tag, '')) LIKE LOWER(CONCAT('%', :keyword, '%'))
            OR LOWER(COALESCE(p.description, '')) LIKE LOWER(CONCAT('%', :keyword, '%'))
            OR LOWER(COALESCE(p.spec, '')) LIKE LOWER(CONCAT('%', :keyword, '%'))
        )
        """)
    List<ProductEntity> searchActiveProducts(@Param("keyword") String keyword);
}
