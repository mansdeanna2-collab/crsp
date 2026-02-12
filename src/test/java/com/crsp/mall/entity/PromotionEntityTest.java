package com.crsp.mall.entity;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class PromotionEntityTest {

    @Test
    void isCurrentlyActiveReturnsTrueWhenActiveAndWithinTimeRange() {
        PromotionEntity promotion = new PromotionEntity();
        promotion.setActive(true);
        promotion.setStartTime(LocalDateTime.now().minusHours(1));
        promotion.setEndTime(LocalDateTime.now().plusHours(1));

        assertThat(promotion.isCurrentlyActive()).isTrue();
    }

    @Test
    void isCurrentlyActiveReturnsFalseWhenInactive() {
        PromotionEntity promotion = new PromotionEntity();
        promotion.setActive(false);
        promotion.setStartTime(LocalDateTime.now().minusHours(1));
        promotion.setEndTime(LocalDateTime.now().plusHours(1));

        assertThat(promotion.isCurrentlyActive()).isFalse();
    }

    @Test
    void isCurrentlyActiveReturnsFalseWhenBeforeStartTime() {
        PromotionEntity promotion = new PromotionEntity();
        promotion.setActive(true);
        promotion.setStartTime(LocalDateTime.now().plusHours(1));
        promotion.setEndTime(LocalDateTime.now().plusHours(2));

        assertThat(promotion.isCurrentlyActive()).isFalse();
    }

    @Test
    void isCurrentlyActiveReturnsFalseWhenAfterEndTime() {
        PromotionEntity promotion = new PromotionEntity();
        promotion.setActive(true);
        promotion.setStartTime(LocalDateTime.now().minusHours(2));
        promotion.setEndTime(LocalDateTime.now().minusHours(1));

        assertThat(promotion.isCurrentlyActive()).isFalse();
    }

    @Test
    void isCurrentlyActiveReturnsTrueWhenTimesAreNull() {
        PromotionEntity promotion = new PromotionEntity();
        promotion.setActive(true);
        promotion.setStartTime(null);
        promotion.setEndTime(null);

        assertThat(promotion.isCurrentlyActive()).isTrue();
    }

    @Test
    void getTypeDisplayNameReturnsCorrectChineseNames() {
        PromotionEntity promotion = new PromotionEntity();

        promotion.setType("flash_sale");
        assertThat(promotion.getTypeDisplayName()).isEqualTo("限时秒杀");

        promotion.setType("daily_deal");
        assertThat(promotion.getTypeDisplayName()).isEqualTo("天天特价");

        promotion.setType("brand_flash");
        assertThat(promotion.getTypeDisplayName()).isEqualTo("品牌闪购");

        promotion.setType("new_user");
        assertThat(promotion.getTypeDisplayName()).isEqualTo("新人专享");
    }

    @Test
    void getTypeDisplayNameReturnsEmptyStringForNull() {
        PromotionEntity promotion = new PromotionEntity();
        promotion.setType(null);
        assertThat(promotion.getTypeDisplayName()).isEqualTo("");
    }

    @Test
    void getTimeStatusReturnsCorrectStatus() {
        PromotionEntity promotion = new PromotionEntity();

        // Not started
        promotion.setStartTime(LocalDateTime.now().plusHours(1));
        promotion.setEndTime(LocalDateTime.now().plusHours(2));
        assertThat(promotion.getTimeStatus()).isEqualTo("未开始");

        // In progress
        promotion.setStartTime(LocalDateTime.now().minusHours(1));
        promotion.setEndTime(LocalDateTime.now().plusHours(1));
        assertThat(promotion.getTimeStatus()).isEqualTo("进行中");

        // Ended
        promotion.setStartTime(LocalDateTime.now().minusHours(2));
        promotion.setEndTime(LocalDateTime.now().minusHours(1));
        assertThat(promotion.getTimeStatus()).isEqualTo("已结束");
    }

    @Test
    void getTimeStatusReturnsInProgressWhenTimesAreNull() {
        PromotionEntity promotion = new PromotionEntity();
        promotion.setStartTime(null);
        promotion.setEndTime(null);
        assertThat(promotion.getTimeStatus()).isEqualTo("进行中");
    }
}
