package com.crsp.mall.entity;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ProductEntityTest {

    @Test
    void stockStatusReflectsAvailability() {
        ProductEntity product = new ProductEntity();

        product.setStock(null);
        assertThat(product.isInStock()).isTrue();
        assertThat(product.isLowStock()).isFalse();
        assertThat(product.getStockStatus()).isEqualTo("有货");

        product.setStock(5);
        assertThat(product.isInStock()).isTrue();
        assertThat(product.isLowStock()).isTrue();
        assertThat(product.getStockStatus()).isEqualTo("库存紧张");

        product.setStock(0);
        assertThat(product.isInStock()).isFalse();
        assertThat(product.isLowStock()).isFalse();
        assertThat(product.getStockStatus()).isEqualTo("已售完");
    }

    @Test
    void getSpecPriceParsesMatchingSpec() {
        ProductEntity product = new ProductEntity();
        product.setSpecifications("[{\"name\":\"红色 M\",\"price\":\"¥199.00\"},{\"name\":\"蓝色 L\",\"price\":188}]");

        assertThat(product.getSpecPrice("红色 M")).isEqualTo(199.00);
        assertThat(product.getSpecPrice("蓝色 L")).isEqualTo(188.0);
        assertThat(product.getSpecPrice("不存在")).isNull();
    }
}
