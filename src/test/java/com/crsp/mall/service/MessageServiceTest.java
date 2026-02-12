package com.crsp.mall.service;

import com.crsp.mall.entity.MessageEntity;
import com.crsp.mall.entity.UserEntity;
import com.crsp.mall.repository.MessageRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class MessageServiceTest {

    @Autowired
    private MessageService messageService;

    @Autowired
    private UserService userService;

    @Autowired
    private MessageRepository messageRepository;

    @Test
    void sendUserMessageCreatesMessage() {
        UserEntity user = userService.getOrCreateUser(null);
        MessageEntity msg = messageService.sendUserMessage(user.getId(), "service", "你好");

        assertNotNull(msg);
        assertNotNull(msg.getId());
        assertEquals(user.getId(), msg.getUserId());
        assertEquals("service", msg.getChatType());
        assertEquals("user", msg.getSenderType());
        assertEquals("你好", msg.getContent());
        assertTrue(msg.getIsRead()); // User's own message is auto-read
    }

    @Test
    void sendUserMessageRejectsInvalidChatType() {
        UserEntity user = userService.getOrCreateUser(null);
        MessageEntity msg = messageService.sendUserMessage(user.getId(), "invalid", "你好");
        assertNull(msg);
    }

    @Test
    void sendUserMessageRejectsEmptyContent() {
        UserEntity user = userService.getOrCreateUser(null);
        assertNull(messageService.sendUserMessage(user.getId(), "service", ""));
        assertNull(messageService.sendUserMessage(user.getId(), "service", "  "));
        assertNull(messageService.sendUserMessage(user.getId(), "service", null));
    }

    @Test
    void sendAdminMessageCreatesMessage() {
        UserEntity user = userService.getOrCreateUser(null);
        MessageEntity msg = messageService.sendAdminMessage(user.getId(), "service", "客服小蜜", "您好，有什么需要帮助？");

        assertNotNull(msg);
        assertEquals("admin", msg.getSenderType());
        assertEquals("客服小蜜", msg.getSenderName());
        assertFalse(msg.getIsRead()); // Admin message starts unread
    }

    @Test
    void getChatHistoryReturnsMessages() {
        UserEntity user = userService.getOrCreateUser(null);
        messageService.sendUserMessage(user.getId(), "service", "第一条");
        messageService.sendAdminMessage(user.getId(), "service", "客服小蜜", "第二条");
        messageService.sendUserMessage(user.getId(), "store", "其他聊天");

        List<MessageEntity> history = messageService.getChatHistory(user.getId(), "service");
        assertEquals(2, history.size());
        assertEquals("第一条", history.get(0).getContent());
        assertEquals("第二条", history.get(1).getContent());
    }

    @Test
    void markAsReadMarksAdminMessages() {
        UserEntity user = userService.getOrCreateUser(null);
        messageService.sendAdminMessage(user.getId(), "service", "客服小蜜", "未读消息1");
        messageService.sendAdminMessage(user.getId(), "service", "客服小蜜", "未读消息2");

        assertEquals(2, messageService.getUnreadCount(user.getId(), "service"));

        messageService.markAsRead(user.getId(), "service");
        assertEquals(0, messageService.getUnreadCount(user.getId(), "service"));
    }

    @Test
    void getUnreadCountReturnsCorrectCount() {
        UserEntity user = userService.getOrCreateUser(null);
        messageService.sendAdminMessage(user.getId(), "service", "客服小蜜", "消息1");
        messageService.sendAdminMessage(user.getId(), "store", "旗舰店", "消息2");
        messageService.sendUserMessage(user.getId(), "service", "用户消息"); // user msg不计入unread

        assertEquals(1, messageService.getUnreadCount(user.getId(), "service"));
        assertEquals(1, messageService.getUnreadCount(user.getId(), "store"));
        // 总未读数 = service(1) + store(1) + system(1 注册欢迎消息)
        assertEquals(3, messageService.getTotalUnreadCount(user.getId()));
    }

    @Test
    void getLatestMessageReturnsNewestMessage() {
        UserEntity user = userService.getOrCreateUser(null);
        messageService.sendUserMessage(user.getId(), "service", "第一条");
        messageService.sendUserMessage(user.getId(), "service", "第二条");

        MessageEntity latest = messageService.getLatestMessage(user.getId(), "service");
        assertNotNull(latest);
        assertEquals("第二条", latest.getContent());
    }

    @Test
    void deleteUserMessagesRemovesAll() {
        UserEntity user = userService.getOrCreateUser(null);
        messageService.sendUserMessage(user.getId(), "service", "消息1");
        messageService.sendUserMessage(user.getId(), "store", "消息2");

        messageService.deleteUserMessages(user.getId());
        assertEquals(0, messageService.getChatHistory(user.getId(), "service").size());
        assertEquals(0, messageService.getChatHistory(user.getId(), "store").size());
    }

    @Test
    void isValidChatTypeReturnsTrueForValidTypes() {
        assertTrue(messageService.isValidChatType("logistics"));
        assertTrue(messageService.isValidChatType("store"));
        assertTrue(messageService.isValidChatType("promo"));
        assertTrue(messageService.isValidChatType("service"));
        assertTrue(messageService.isValidChatType("system"));
    }

    @Test
    void isValidChatTypeReturnsFalseForInvalidTypes() {
        assertFalse(messageService.isValidChatType("invalid"));
        assertFalse(messageService.isValidChatType(""));
        assertFalse(messageService.isValidChatType(null));
    }

    @Test
    void sendUserMessageTruncatesLongContent() {
        UserEntity user = userService.getOrCreateUser(null);
        String longContent = "a".repeat(1500);
        MessageEntity msg = messageService.sendUserMessage(user.getId(), "service", longContent);

        assertNotNull(msg);
        assertEquals(1000, msg.getContent().length());
    }

    @Test
    void deleteUserCascadesMessages() {
        UserEntity user = userService.getOrCreateUser(null);
        Long userId = user.getId();
        messageService.sendUserMessage(userId, "service", "消息");

        // Verify message exists (1 user message + 1 welcome message)
        assertEquals(1, messageService.getChatHistory(userId, "service").size());

        // Delete user - messages should be cascade deleted
        userService.deleteUser(userId);
        assertEquals(0, messageService.getChatHistory(userId, "service").size());
    }

    @Test
    void newUserReceivesWelcomeSystemMessage() {
        UserEntity user = userService.getOrCreateUser(null);
        List<MessageEntity> systemMessages = messageService.getChatHistory(user.getId(), "system");

        assertEquals(1, systemMessages.size());
        MessageEntity welcome = systemMessages.get(0);
        assertEquals("system", welcome.getChatType());
        assertEquals("admin", welcome.getSenderType());
        assertEquals("系统通知", welcome.getSenderName());
        assertTrue(welcome.getContent().contains("欢迎"));
        assertFalse(welcome.getIsRead());
    }
}
