package com.crsp.mall.service;

import com.crsp.mall.entity.*;
import com.crsp.mall.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * 用户服务类 - 处理用户管理、浏览历史、收藏、购物车
 */
@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BrowsingHistoryRepository browsingHistoryRepository;

    @Autowired
    private FavoriteRepository favoriteRepository;

    @Autowired
    private CartItemRepository cartItemRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private MessageRepository messageRepository;

    // ===== 用户管理 =====

    /**
     * 根据token获取或创建游客用户
     */
    public UserEntity getOrCreateUser(String token) {
        return getOrCreateUser(token, null);
    }

    /**
     * 根据token获取或创建游客用户，记录注册IP
     */
    public UserEntity getOrCreateUser(String token, String ipAddress) {
        if (token != null && !token.isEmpty()) {
            Optional<UserEntity> existing = userRepository.findByToken(token);
            if (existing.isPresent()) {
                UserEntity user = existing.get();
                user.setLastVisit(LocalDateTime.now());
                return userRepository.save(user);
            }
        }
        // 创建新游客用户
        UserEntity user = new UserEntity();
        if (token != null && !token.isEmpty()) {
            user.setToken(token);
        }
        if (ipAddress != null && !ipAddress.isEmpty()) {
            user.setRegisterIp(ipAddress);
        }
        UserEntity savedUser = userRepository.save(user);

        // 自动发送注册欢迎系统通知
        try {
            MessageEntity welcomeMsg = new MessageEntity();
            welcomeMsg.setUserId(savedUser.getId());
            welcomeMsg.setChatType("system");
            welcomeMsg.setSenderType("admin");
            welcomeMsg.setSenderName("系统通知");
            welcomeMsg.setContent("欢迎注册成人玩具商城！您的账户已创建成功，祝您购物愉快~");
            welcomeMsg.setIsRead(false);
            messageRepository.save(welcomeMsg);
        } catch (Exception e) {
            // 发送欢迎消息失败不影响用户注册
        }

        return savedUser;
    }

    public Optional<UserEntity> getUserByToken(String token) {
        return userRepository.findByToken(token);
    }

    public Optional<UserEntity> getUserById(Long id) {
        return userRepository.findById(id);
    }

    public List<UserEntity> getAllUsers() {
        return userRepository.findAllByOrderByCreatedAtDesc();
    }

    public long getUserCount() {
        return userRepository.count();
    }

    public long getGuestCount() {
        return userRepository.countByUserType("guest");
    }

    public long getRegisteredCount() {
        return userRepository.countByUserType("user");
    }

    public long getActiveCount() {
        return userRepository.countByActive(true);
    }

    /**
     * 获取最近N天新增用户数
     */
    public long getNewUserCount(int days) {
        LocalDateTime since = LocalDateTime.now().minusDays(days);
        return userRepository.countByCreatedAtAfter(since);
    }

    /**
     * 获取用户统计信息
     */
    public java.util.Map<String, Object> getUserStatistics() {
        java.util.Map<String, Object> stats = new java.util.HashMap<>();
        stats.put("totalCount", getUserCount());
        stats.put("registeredCount", getRegisteredCount());
        stats.put("guestCount", getGuestCount());
        stats.put("activeCount", getActiveCount());
        stats.put("newToday", getNewUserCount(1));
        stats.put("newThisWeek", getNewUserCount(7));
        stats.put("newThisMonth", getNewUserCount(30));
        return stats;
    }

    /**
     * 获取用户消费总额（非取消订单）
     */
    public double getUserTotalSpending(Long userId) {
        return orderRepository.sumTotalAmountByUserIdExcludingCancelled(userId);
    }

    /**
     * 获取用户订单数量
     */
    public long getUserOrderCount(Long userId) {
        return orderRepository.countByUserId(userId);
    }

    public UserEntity saveUser(UserEntity user) {
        return userRepository.save(user);
    }

    @Transactional
    public void deleteUser(Long id) {
        // Cascade delete related data
        browsingHistoryRepository.deleteByUserId(id);
        favoriteRepository.deleteByUserId(id);
        cartItemRepository.deleteByUserId(id);
        messageRepository.deleteByUserId(id);
        userRepository.deleteById(id);
    }

    // ===== 浏览历史 =====

    /**
     * 记录浏览历史
     */
    public BrowsingHistoryEntity addBrowsingHistory(Long userId, ProductEntity product) {
        // 检查是否已有同一商品的浏览记录，如果有则更新时间
        Optional<BrowsingHistoryEntity> existing = browsingHistoryRepository
                .findTopByUserIdAndProductIdOrderByViewedAtDesc(userId, product.getId());
        
        if (existing.isPresent()) {
            BrowsingHistoryEntity history = existing.get();
            history.setViewedAt(LocalDateTime.now());
            return browsingHistoryRepository.save(history);
        }
        
        BrowsingHistoryEntity history = new BrowsingHistoryEntity();
        history.setUserId(userId);
        history.setProductId(product.getId());
        history.setProductTitle(product.getTitle());
        history.setProductPrice(product.getPrice());
        history.setProductImage(product.getFirstImageUrl());
        return browsingHistoryRepository.save(history);
    }

    public List<BrowsingHistoryEntity> getBrowsingHistory(Long userId) {
        return browsingHistoryRepository.findByUserIdOrderByViewedAtDesc(userId);
    }

    @Transactional
    public void clearBrowsingHistory(Long userId) {
        browsingHistoryRepository.deleteByUserId(userId);
    }

    // ===== 收藏 =====

    /**
     * 切换收藏状态
     */
    @Transactional
    public boolean toggleFavorite(Long userId, ProductEntity product) {
        Optional<FavoriteEntity> existing = favoriteRepository.findByUserIdAndProductId(userId, product.getId());
        if (existing.isPresent()) {
            favoriteRepository.delete(existing.get());
            return false; // 取消收藏
        } else {
            FavoriteEntity favorite = new FavoriteEntity();
            favorite.setUserId(userId);
            favorite.setProductId(product.getId());
            favorite.setProductTitle(product.getTitle());
            favorite.setProductPrice(product.getPrice());
            favorite.setProductImage(product.getFirstImageUrl());
            favoriteRepository.save(favorite);
            return true; // 添加收藏
        }
    }

    public boolean isFavorite(Long userId, Long productId) {
        return favoriteRepository.existsByUserIdAndProductId(userId, productId);
    }

    public List<FavoriteEntity> getFavorites(Long userId) {
        return favoriteRepository.findByUserIdOrderByCreatedAtDesc(userId);
    }

    public long getFavoriteCount(Long userId) {
        return favoriteRepository.countByUserId(userId);
    }

    // ===== 购物车 =====

    /**
     * 添加商品到购物车
     */
    public CartItemEntity addToCart(Long userId, ProductEntity product, String specName, Integer quantity) {
        String spec = (specName != null) ? specName : "";
        int qty = (quantity != null && quantity >= 1) ? Math.min(quantity, 999) : 1;
        Optional<CartItemEntity> existing = cartItemRepository.findByUserIdAndProductIdAndSpecName(userId, product.getId(), spec);
        
        if (existing.isPresent()) {
            CartItemEntity item = existing.get();
            int newQty = Math.min(item.getQuantity() + qty, 999);
            item.setQuantity(newQty);
            return cartItemRepository.save(item);
        }
        
        CartItemEntity item = new CartItemEntity();
        item.setUserId(userId);
        item.setProductId(product.getId());
        item.setProductTitle(product.getTitle());
        item.setProductPrice(product.getPrice());
        item.setProductImage(product.getFirstImageUrl());
        item.setSpecName(spec);
        item.setQuantity(qty);
        return cartItemRepository.save(item);
    }

    public CartItemEntity updateCartItemQuantity(Long itemId, Integer quantity, Long userId) {
        Optional<CartItemEntity> itemOpt = cartItemRepository.findById(itemId);
        if (itemOpt.isPresent()) {
            CartItemEntity item = itemOpt.get();
            if (!item.getUserId().equals(userId)) {
                return null; // 不属于该用户
            }
            item.setQuantity(quantity);
            return cartItemRepository.save(item);
        }
        return null;
    }

    public void removeCartItem(Long itemId, Long userId) {
        Optional<CartItemEntity> itemOpt = cartItemRepository.findById(itemId);
        if (itemOpt.isPresent() && itemOpt.get().getUserId().equals(userId)) {
            cartItemRepository.deleteById(itemId);
        }
    }

    public List<CartItemEntity> getCartItems(Long userId) {
        return cartItemRepository.findByUserIdOrderByCreatedAtDesc(userId);
    }

    public List<CartItemEntity> getSelectedCartItems(Long userId) {
        return cartItemRepository.findByUserIdAndSelectedTrue(userId);
    }

    public long getCartItemCount(Long userId) {
        return cartItemRepository.countByUserId(userId);
    }

    public CartItemEntity saveCartItem(CartItemEntity item) {
        return cartItemRepository.save(item);
    }

    @Transactional
    public void clearCart(Long userId) {
        cartItemRepository.deleteByUserId(userId);
    }

    public CartItemEntity updateCartItemSelected(Long itemId, Boolean selected, Long userId) {
        Optional<CartItemEntity> itemOpt = cartItemRepository.findById(itemId);
        if (itemOpt.isPresent()) {
            CartItemEntity item = itemOpt.get();
            if (!item.getUserId().equals(userId)) {
                return null; // 不属于该用户
            }
            item.setSelected(selected);
            return cartItemRepository.save(item);
        }
        return null;
    }
}
