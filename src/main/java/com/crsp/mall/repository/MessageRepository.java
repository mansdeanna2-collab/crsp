package com.crsp.mall.repository;

import com.crsp.mall.entity.MessageEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 消息数据访问接口
 */
@Repository
public interface MessageRepository extends JpaRepository<MessageEntity, Long> {

    /** 获取用户在某个聊天类型下的所有消息，按时间升序 */
    List<MessageEntity> findByUserIdAndChatTypeOrderByCreatedAtAsc(Long userId, String chatType);

    /** 获取用户所有消息（按时间降序，用于列表展示最近消息） */
    List<MessageEntity> findByUserIdOrderByCreatedAtDesc(Long userId);

    /** 统计用户某聊天类型下未读消息数 */
    long countByUserIdAndChatTypeAndSenderTypeAndIsRead(Long userId, String chatType, String senderType, Boolean isRead);

    /** 获取所有有消息的用户ID（去重） */
    @Query("SELECT DISTINCT m.userId FROM MessageEntity m ORDER BY m.userId")
    List<Long> findDistinctUserIds();

    /** 获取某用户最新一条消息 */
    MessageEntity findTopByUserIdOrderByCreatedAtDesc(Long userId);

    /** 获取某用户某聊天类型最新一条消息 */
    MessageEntity findTopByUserIdAndChatTypeOrderByCreatedAtDesc(Long userId, String chatType);

    /** 按用户ID删除所有消息 */
    void deleteByUserId(Long userId);

    /** 统计用户未读消息总数（来自admin的未读消息） */
    long countByUserIdAndSenderTypeAndIsRead(Long userId, String senderType, Boolean isRead);

    /** 获取所有有消息的用户ID及其最新消息时间（用于后台列表排序） */
    @Query("SELECT m.userId, MAX(m.createdAt) as latestTime FROM MessageEntity m GROUP BY m.userId ORDER BY latestTime DESC")
    List<Object[]> findUserIdsWithLatestMessageTime();

    /** 批量标记某用户某聊天类型的管理员消息为已读 */
    @Modifying
    @Query("UPDATE MessageEntity m SET m.isRead = true WHERE m.userId = :userId AND m.chatType = :chatType AND m.senderType = 'admin' AND m.isRead = false")
    int markAdminMessagesAsRead(@Param("userId") Long userId, @Param("chatType") String chatType);
}
