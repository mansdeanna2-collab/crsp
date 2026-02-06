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
        assertThat(product.getStockStatus()).isEqualTo("已售罄");
    }
}
