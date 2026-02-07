package com.crsp.mall.repository;

import com.crsp.mall.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 用户数据访问接口
 */
@Repository
public interface UserRepository extends JpaRepository<UserEntity, Long> {
    
    Optional<UserEntity> findByToken(String token);
    
    List<UserEntity> findByUserType(String userType);
    
    List<UserEntity> findAllByOrderByCreatedAtDesc();
    
    long countByUserType(String userType);
    
    long countByActive(Boolean active);
    
    boolean existsByToken(String token);
}
