package com.crsp.mall.service;

import com.crsp.mall.entity.ProductEntity;
import com.crsp.mall.entity.PromotionEntity;
import com.crsp.mall.repository.ProductRepository;
import com.crsp.mall.repository.PromotionRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Import({PromotionService.class, ProductDbService.class})
class PromotionServiceTest {

    @Autowired
    private PromotionService promotionService;

    @Autowired
    private PromotionRepository promotionRepository;

    @Autowired
    private ProductRepository productRepository;

    private ProductEntity createAndSaveProduct(String title) {
        ProductEntity product = new ProductEntity();
        product.setTitle(title);
        product.setPrice(100.0);
        product.setActive(true);
        return productRepository.save(product);
    }

    @Test
    void saveAndGetPromotionById() {
        ProductEntity product = createAndSaveProduct("测试商品");
        PromotionEntity promo = new PromotionEntity();
        promo.setType("flash_sale");
        promo.setProductId(product.getId());
        promo.setPromotionPrice(50.0);
        promo.setActive(true);
        PromotionEntity saved = promotionService.savePromotion(promo);

        Optional<PromotionEntity> found = promotionService.getPromotionById(saved.getId());
        assertThat(found).isPresent();
        assertThat(found.get().getType()).isEqualTo("flash_sale");
        assertThat(found.get().getPromotionPrice()).isEqualTo(50.0);
    }

    @Test
    void getPromotionsByTypeFiltersCorrectly() {
        ProductEntity product = createAndSaveProduct("测试商品");

        PromotionEntity flashSale = new PromotionEntity();
        flashSale.setType("flash_sale");
        flashSale.setProductId(product.getId());
        flashSale.setPromotionPrice(50.0);
        flashSale.setActive(true);
        promotionRepository.save(flashSale);

        PromotionEntity dailyDeal = new PromotionEntity();
        dailyDeal.setType("daily_deal");
        dailyDeal.setProductId(product.getId());
        dailyDeal.setPromotionPrice(70.0);
        dailyDeal.setActive(true);
        promotionRepository.save(dailyDeal);

        List<PromotionEntity> flashSales = promotionService.getPromotionsByType("flash_sale");
        assertThat(flashSales).hasSize(1);
        assertThat(flashSales.get(0).getType()).isEqualTo("flash_sale");

        List<PromotionEntity> dailyDeals = promotionService.getPromotionsByType("daily_deal");
        assertThat(dailyDeals).hasSize(1);
        assertThat(dailyDeals.get(0).getType()).isEqualTo("daily_deal");
    }

    @Test
    void getActivePromotionsByTypeFiltersByTimeRange() {
        ProductEntity product = createAndSaveProduct("测试商品");

        // Active promotion (within time range)
        PromotionEntity activePromo = new PromotionEntity();
        activePromo.setType("flash_sale");
        activePromo.setProductId(product.getId());
        activePromo.setPromotionPrice(50.0);
        activePromo.setActive(true);
        activePromo.setStartTime(LocalDateTime.now().minusHours(1));
        activePromo.setEndTime(LocalDateTime.now().plusHours(1));
        promotionRepository.save(activePromo);

        // Expired promotion
        PromotionEntity expiredPromo = new PromotionEntity();
        expiredPromo.setType("flash_sale");
        expiredPromo.setProductId(product.getId());
        expiredPromo.setPromotionPrice(40.0);
        expiredPromo.setActive(true);
        expiredPromo.setStartTime(LocalDateTime.now().minusHours(5));
        expiredPromo.setEndTime(LocalDateTime.now().minusHours(3));
        promotionRepository.save(expiredPromo);

        // Inactive promotion
        PromotionEntity inactivePromo = new PromotionEntity();
        inactivePromo.setType("flash_sale");
        inactivePromo.setProductId(product.getId());
        inactivePromo.setPromotionPrice(30.0);
        inactivePromo.setActive(false);
        inactivePromo.setStartTime(LocalDateTime.now().minusHours(1));
        inactivePromo.setEndTime(LocalDateTime.now().plusHours(1));
        promotionRepository.save(inactivePromo);

        List<PromotionEntity> active = promotionService.getActivePromotionsByType("flash_sale");
        assertThat(active).hasSize(1);
        assertThat(active.get(0).getPromotionPrice()).isEqualTo(50.0);
    }

    @Test
    void getPromotionsWithProductsReturnsProductData() {
        ProductEntity product = createAndSaveProduct("测试秒杀商品");

        PromotionEntity promo = new PromotionEntity();
        promo.setType("flash_sale");
        promo.setProductId(product.getId());
        promo.setPromotionPrice(50.0);
        promo.setActive(true);
        promo.setStartTime(LocalDateTime.now().minusHours(1));
        promo.setEndTime(LocalDateTime.now().plusHours(1));
        promotionRepository.save(promo);

        List<Map<String, Object>> result = promotionService.getPromotionsWithProducts("flash_sale");
        assertThat(result).hasSize(1);
        assertThat(result.get(0)).containsKey("product");
        assertThat(result.get(0)).containsKey("promotion");
        ProductEntity resultProduct = (ProductEntity) result.get(0).get("product");
        assertThat(resultProduct.getTitle()).isEqualTo("测试秒杀商品");
    }

    @Test
    void getPromotionCountsReturnsCorrectCounts() {
        ProductEntity product = createAndSaveProduct("测试商品");

        PromotionEntity p1 = new PromotionEntity();
        p1.setType("flash_sale");
        p1.setProductId(product.getId());
        p1.setPromotionPrice(50.0);
        promotionRepository.save(p1);

        PromotionEntity p2 = new PromotionEntity();
        p2.setType("flash_sale");
        p2.setProductId(product.getId());
        p2.setPromotionPrice(40.0);
        promotionRepository.save(p2);

        PromotionEntity p3 = new PromotionEntity();
        p3.setType("daily_deal");
        p3.setProductId(product.getId());
        p3.setPromotionPrice(60.0);
        promotionRepository.save(p3);

        Map<String, Long> counts = promotionService.getPromotionCounts();
        assertThat(counts.get("flash_sale")).isEqualTo(2);
        assertThat(counts.get("daily_deal")).isEqualTo(1);
        assertThat(counts.get("brand_flash")).isEqualTo(0);
        assertThat(counts.get("new_user")).isEqualTo(0);
    }

    @Test
    void deletePromotionRemovesIt() {
        ProductEntity product = createAndSaveProduct("测试商品");

        PromotionEntity promo = new PromotionEntity();
        promo.setType("flash_sale");
        promo.setProductId(product.getId());
        promo.setPromotionPrice(50.0);
        PromotionEntity saved = promotionRepository.save(promo);

        promotionService.deletePromotion(saved.getId());
        assertThat(promotionService.getPromotionById(saved.getId())).isEmpty();
    }
}
