package com.crsp.mall.service;

import com.crsp.mall.entity.ProductEntity;
import com.crsp.mall.repository.ProductRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Import(ProductDbService.class)
class ProductDbServiceTest {

    @Autowired
    private ProductDbService productDbService;

    @Autowired
    private ProductRepository productRepository;

    @Test
    void searchProductsMatchesTagDescriptionAndSpecForActiveItems() {
        ProductEntity activeProduct = new ProductEntity();
        activeProduct.setTitle("测试商品");
        activeProduct.setPrice(99.0);
        activeProduct.setTag("爆款");
        activeProduct.setDescription("超值优惠套餐");
        activeProduct.setSpec("粉色");
        activeProduct.setActive(true);
        activeProduct = productRepository.save(activeProduct);

        ProductEntity inactiveProduct = new ProductEntity();
        inactiveProduct.setTitle("测试商品2");
        inactiveProduct.setPrice(88.0);
        inactiveProduct.setTag("爆款");
        inactiveProduct.setDescription("超值优惠套餐");
        inactiveProduct.setSpec("粉色");
        inactiveProduct.setActive(false);
        inactiveProduct = productRepository.save(inactiveProduct);
        assertThat(inactiveProduct.getId()).isNotNull();

        List<ProductEntity> tagResults = productDbService.searchProducts("爆款");
        assertThat(tagResults)
                .extracting(ProductEntity::getId)
                .contains(activeProduct.getId())
                .doesNotContain(inactiveProduct.getId());

        List<ProductEntity> descriptionResults = productDbService.searchProducts("优惠");
        assertThat(descriptionResults)
                .extracting(ProductEntity::getId)
                .contains(activeProduct.getId())
                .doesNotContain(inactiveProduct.getId());

        List<ProductEntity> specResults = productDbService.searchProducts("粉色");
        assertThat(specResults)
                .extracting(ProductEntity::getId)
                .contains(activeProduct.getId())
                .doesNotContain(inactiveProduct.getId());
    }
}
