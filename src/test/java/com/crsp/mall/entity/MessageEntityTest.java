package com.crsp.mall.entity;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class MessageEntityTest {

    @Test
    void chatTypeNameReturnsCorrectName() {
        MessageEntity msg = new MessageEntity();

        msg.setChatType("logistics");
        assertThat(msg.getChatTypeName()).isEqualTo("物流通知");

        msg.setChatType("store");
        assertThat(msg.getChatTypeName()).isEqualTo("成人用品旗舰店");

        msg.setChatType("promo");
        assertThat(msg.getChatTypeName()).isEqualTo("优惠活动");

        msg.setChatType("service");
        assertThat(msg.getChatTypeName()).isEqualTo("客服小蜜");

        msg.setChatType("system");
        assertThat(msg.getChatTypeName()).isEqualTo("系统通知");
    }

    @Test
    void chatTypeNameReturnsUnknownForInvalidType() {
        MessageEntity msg = new MessageEntity();
        msg.setChatType("invalid");
        assertThat(msg.getChatTypeName()).isEqualTo("未知");

        msg.setChatType(null);
        assertThat(msg.getChatTypeName()).isEqualTo("未知");
    }

    @Test
    void defaultIsReadIsFalse() {
        MessageEntity msg = new MessageEntity();
        assertThat(msg.getIsRead()).isFalse();
    }

    @Test
    void settersAndGettersWorkCorrectly() {
        MessageEntity msg = new MessageEntity();
        msg.setUserId(1L);
        msg.setChatType("service");
        msg.setSenderType("user");
        msg.setSenderName("客服小蜜");
        msg.setContent("测试消息");
        msg.setIsRead(true);

        assertThat(msg.getUserId()).isEqualTo(1L);
        assertThat(msg.getChatType()).isEqualTo("service");
        assertThat(msg.getSenderType()).isEqualTo("user");
        assertThat(msg.getSenderName()).isEqualTo("客服小蜜");
        assertThat(msg.getContent()).isEqualTo("测试消息");
        assertThat(msg.getIsRead()).isTrue();
    }
}
