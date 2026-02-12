package com.crsp.mall.service;

import com.crsp.mall.entity.ProductEntity;
import com.crsp.mall.entity.PromotionEntity;
import com.crsp.mall.repository.PromotionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 促销活动服务类
 */
@Service
public class PromotionService {

    @Autowired
    private PromotionRepository promotionRepository;

    @Autowired
    private ProductDbService productDbService;

    /**
     * 获取所有促销活动
     */
    public List<PromotionEntity> getAllPromotions() {
        return promotionRepository.findAll();
    }

    /**
     * 根据ID获取促销活动
     */
    public Optional<PromotionEntity> getPromotionById(Long id) {
        return promotionRepository.findById(id);
    }

    /**
     * 获取指定类型的所有促销活动
     */
    public List<PromotionEntity> getPromotionsByType(String type) {
        return promotionRepository.findByTypeOrderBySortOrderAsc(type);
    }

    /**
     * 获取指定类型当前有效的促销活动
     */
    public List<PromotionEntity> getActivePromotionsByType(String type) {
        return promotionRepository.findActivePromotionsByType(type, LocalDateTime.now());
    }

    /**
     * 获取促销活动及其关联的商品信息
     */
    public List<Map<String, Object>> getPromotionsWithProducts(String type) {
        List<PromotionEntity> promotions = getActivePromotionsByType(type);
        List<Map<String, Object>> result = new ArrayList<>();

        for (PromotionEntity promotion : promotions) {
            Optional<ProductEntity> productOpt = productDbService.getProductById(promotion.getProductId());
            if (productOpt.isPresent()) {
                ProductEntity product = productOpt.get();
                if (Boolean.TRUE.equals(product.getActive())) {
                    Map<String, Object> item = new HashMap<>();
                    item.put("promotion", promotion);
                    item.put("product", product);
                    result.add(item);
                }
            }
        }
        return result;
    }

    /**
     * 保存促销活动
     */
    public PromotionEntity savePromotion(PromotionEntity promotion) {
        return promotionRepository.save(promotion);
    }

    /**
     * 删除促销活动
     */
    public void deletePromotion(Long id) {
        promotionRepository.deleteById(id);
    }

    /**
     * 获取各类型促销活动数量
     */
    public Map<String, Long> getPromotionCounts() {
        List<PromotionEntity> all = promotionRepository.findAll();
        Map<String, Long> counts = new HashMap<>();
        counts.put("flash_sale", all.stream().filter(p -> "flash_sale".equals(p.getType())).count());
        counts.put("daily_deal", all.stream().filter(p -> "daily_deal".equals(p.getType())).count());
        counts.put("brand_flash", all.stream().filter(p -> "brand_flash".equals(p.getType())).count());
        counts.put("new_user", all.stream().filter(p -> "new_user".equals(p.getType())).count());
        return counts;
    }

    /**
     * 初始化示例促销数据
     */
    public void initSamplePromotions() {
        if (promotionRepository.count() == 0) {
            List<ProductEntity> products = productDbService.getActiveProducts();
            if (products.size() >= 4) {
                LocalDateTime now = LocalDateTime.now();

                // 限时秒杀
                savePromotion(createPromotion("flash_sale", products.get(0).getId(),
                        products.get(0).getPrice() * 0.5, 50,
                        now.minusHours(1), now.plusHours(23), 1));
                savePromotion(createPromotion("flash_sale", products.get(1).getId(),
                        products.get(1).getPrice() * 0.6, 40,
                        now.minusHours(1), now.plusHours(23), 2));

                // 天天特价
                savePromotion(createPromotion("daily_deal", products.get(2).getId(),
                        products.get(2).getPrice() * 0.7, 30,
                        now.minusDays(1), now.plusDays(30), 1));
                savePromotion(createPromotion("daily_deal", products.get(3).getId(),
                        products.get(3).getPrice() * 0.8, 20,
                        now.minusDays(1), now.plusDays(30), 2));

                // 品牌闪购
                if (products.size() >= 6) {
                    savePromotion(createPromotion("brand_flash", products.get(4).getId(),
                            products.get(4).getPrice() * 0.65, 35,
                            now.minusHours(2), now.plusDays(3), 1));
                    savePromotion(createPromotion("brand_flash", products.get(5).getId(),
                            products.get(5).getPrice() * 0.7, 30,
                            now.minusHours(2), now.plusDays(3), 2));
                }

                // 新人专享
                if (products.size() >= 8) {
                    savePromotion(createPromotion("new_user", products.get(6).getId(),
                            products.get(6).getPrice() * 0.5, 50,
                            null, null, 1));
                    savePromotion(createPromotion("new_user", products.get(7).getId(),
                            products.get(7).getPrice() * 0.4, 60,
                            null, null, 2));
                }
            }
        }
    }

    private PromotionEntity createPromotion(String type, Long productId, double promotionPrice,
                                             Integer discountPercent, LocalDateTime startTime,
                                             LocalDateTime endTime, Integer sortOrder) {
        PromotionEntity promotion = new PromotionEntity();
        promotion.setType(type);
        promotion.setProductId(productId);
        promotion.setPromotionPrice(Math.round(promotionPrice * 100.0) / 100.0);
        promotion.setDiscountPercent(discountPercent);
        promotion.setStartTime(startTime);
        promotion.setEndTime(endTime);
        promotion.setSortOrder(sortOrder);
        promotion.setActive(true);
        return promotion;
    }
}
