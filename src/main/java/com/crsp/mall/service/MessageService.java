package com.crsp.mall.service;

import com.crsp.mall.entity.MessageEntity;
import com.crsp.mall.repository.MessageRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;

/**
 * 消息服务类 - 处理消息的发送、接收和管理
 */
@Service
public class MessageService {

    private static final Set<String> VALID_CHAT_TYPES = Set.of("logistics", "store", "promo", "service", "system");
    private static final int MAX_CONTENT_LENGTH = 1000;

    @Autowired
    private MessageRepository messageRepository;

    /**
     * 用户发送消息
     */
    public MessageEntity sendUserMessage(Long userId, String chatType, String content) {
        if (userId == null || chatType == null || content == null || content.trim().isEmpty()) {
            return null;
        }
        if (!VALID_CHAT_TYPES.contains(chatType)) {
            return null;
        }
        String trimmed = content.trim();
        if (trimmed.length() > MAX_CONTENT_LENGTH) {
            trimmed = trimmed.substring(0, MAX_CONTENT_LENGTH);
        }

        MessageEntity message = new MessageEntity();
        message.setUserId(userId);
        message.setChatType(chatType);
        message.setSenderType("user");
        message.setContent(trimmed);
        message.setIsRead(true); // 用户自己发的消息默认已读
        return messageRepository.save(message);
    }

    /**
     * 后台发送消息给用户
     */
    public MessageEntity sendAdminMessage(Long userId, String chatType, String senderName, String content) {
        if (userId == null || chatType == null || content == null || content.trim().isEmpty()) {
            return null;
        }
        if (!VALID_CHAT_TYPES.contains(chatType)) {
            return null;
        }
        String trimmed = content.trim();
        if (trimmed.length() > MAX_CONTENT_LENGTH) {
            trimmed = trimmed.substring(0, MAX_CONTENT_LENGTH);
        }

        MessageEntity message = new MessageEntity();
        message.setUserId(userId);
        message.setChatType(chatType);
        message.setSenderType("admin");
        message.setSenderName(senderName);
        message.setContent(trimmed);
        message.setIsRead(false);
        return messageRepository.save(message);
    }

    /**
     * 获取用户某个聊天类型的消息历史
     */
    public List<MessageEntity> getChatHistory(Long userId, String chatType) {
        return messageRepository.findByUserIdAndChatTypeOrderByCreatedAtAsc(userId, chatType);
    }

    /**
     * 标记某聊天类型的消息为已读（批量更新）
     */
    @Transactional
    public void markAsRead(Long userId, String chatType) {
        messageRepository.markAdminMessagesAsRead(userId, chatType);
    }

    /**
     * 获取用户未读消息数（某聊天类型）
     */
    public long getUnreadCount(Long userId, String chatType) {
        return messageRepository.countByUserIdAndChatTypeAndSenderTypeAndIsRead(userId, chatType, "admin", false);
    }

    /**
     * 获取用户总未读消息数
     */
    public long getTotalUnreadCount(Long userId) {
        return messageRepository.countByUserIdAndSenderTypeAndIsRead(userId, "admin", false);
    }

    /**
     * 获取所有有消息的用户ID列表（按最新消息时间排序）
     */
    public List<Object[]> getUserIdsWithLatestMessageTime() {
        return messageRepository.findUserIdsWithLatestMessageTime();
    }

    /**
     * 获取用户某聊天类型的最新消息
     */
    public MessageEntity getLatestMessage(Long userId, String chatType) {
        return messageRepository.findTopByUserIdAndChatTypeOrderByCreatedAtDesc(userId, chatType);
    }

    /**
     * 获取用户最新消息
     */
    public MessageEntity getLatestMessage(Long userId) {
        return messageRepository.findTopByUserIdOrderByCreatedAtDesc(userId);
    }

    /**
     * 删除用户所有消息
     */
    @Transactional
    public void deleteUserMessages(Long userId) {
        messageRepository.deleteByUserId(userId);
    }

    /**
     * 验证聊天类型是否有效
     */
    public boolean isValidChatType(String chatType) {
        return chatType != null && VALID_CHAT_TYPES.contains(chatType);
    }

    /**
     * 获取有效的聊天类型集合
     */
    public Set<String> getValidChatTypes() {
        return VALID_CHAT_TYPES;
    }
}
