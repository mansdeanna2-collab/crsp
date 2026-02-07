package com.crsp.mall.entity;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class UserEntityTest {

    @Test
    void calculateLevelReturnsCorrectLevel() {
        assertThat(UserEntity.calculateLevel(0)).isEqualTo("新用户");
        assertThat(UserEntity.calculateLevel(100)).isEqualTo("普通会员");
        assertThat(UserEntity.calculateLevel(999.99)).isEqualTo("普通会员");
        assertThat(UserEntity.calculateLevel(1000)).isEqualTo("银牌会员");
        assertThat(UserEntity.calculateLevel(4999.99)).isEqualTo("银牌会员");
        assertThat(UserEntity.calculateLevel(5000)).isEqualTo("金牌会员");
        assertThat(UserEntity.calculateLevel(9999.99)).isEqualTo("金牌会员");
        assertThat(UserEntity.calculateLevel(10000)).isEqualTo("钻石会员");
        assertThat(UserEntity.calculateLevel(50000)).isEqualTo("钻石会员");
    }

    @Test
    void userTypeTextReturnsCorrectText() {
        UserEntity user = new UserEntity();
        user.setUserType("guest");
        assertThat(user.getUserTypeText()).isEqualTo("游客");

        user.setUserType("user");
        assertThat(user.getUserTypeText()).isEqualTo("注册用户");
    }

    @Test
    void addressFieldWorksCorrectly() {
        UserEntity user = new UserEntity();
        assertThat(user.getAddress()).isNull();

        user.setAddress("北京市朝阳区测试路1号");
        assertThat(user.getAddress()).isEqualTo("北京市朝阳区测试路1号");
    }
}
