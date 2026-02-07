package com.crsp.mall.repository;

import com.crsp.mall.entity.AdminEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * 管理员数据访问接口
 */
@Repository
public interface AdminRepository extends JpaRepository<AdminEntity, Long> {
    
    Optional<AdminEntity> findByUsername(String username);
    
    boolean existsByUsername(String username);
}
