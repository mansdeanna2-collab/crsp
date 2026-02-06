package com.crsp.mall.repository;

import com.crsp.mall.entity.ProductEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 商品数据访问接口
 */
@Repository
public interface ProductRepository extends JpaRepository<ProductEntity, Long> {
    
    List<ProductEntity> findByActiveTrue();
    
    List<ProductEntity> findByTitleContainingIgnoreCaseOrTagContainingIgnoreCase(String title, String tag);
    
    List<ProductEntity> findByTitleContainingIgnoreCaseAndActiveTrue(String title);
}
