package com.crsp.mall.service;

import com.crsp.mall.entity.CartItemEntity;
import com.crsp.mall.entity.FavoriteEntity;
import com.crsp.mall.entity.OrderEntity;
import com.crsp.mall.entity.ProductEntity;
import com.crsp.mall.entity.UserEntity;
import com.crsp.mall.repository.CartItemRepository;
import com.crsp.mall.repository.FavoriteRepository;
import com.crsp.mall.repository.OrderRepository;
import com.crsp.mall.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class UserServiceTest {

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CartItemRepository cartItemRepository;

    @Autowired
    private FavoriteRepository favoriteRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Test
    void getOrCreateUserCreatesGuestWithToken() {
        UserEntity user = userService.getOrCreateUser(null);
        assertNotNull(user.getId());
        assertNotNull(user.getToken());
        assertEquals("guest", user.getUserType());
        assertTrue(user.getNickname().startsWith("游客"));
    }

    @Test
    void getOrCreateUserReturnsExistingUser() {
        UserEntity first = userService.getOrCreateUser(null);
        UserEntity second = userService.getOrCreateUser(first.getToken());
        assertEquals(first.getId(), second.getId());
    }

    @Test
    void addToCartCapsQuantityAt999() {
        UserEntity user = userService.getOrCreateUser(null);
        ProductEntity product = createProduct();

        CartItemEntity item = userService.addToCart(user.getId(), product, "", 1000);
        assertEquals(999, item.getQuantity());
    }

    @Test
    void addToCartMergesExistingItem() {
        UserEntity user = userService.getOrCreateUser(null);
        ProductEntity product = createProduct();

        userService.addToCart(user.getId(), product, "默认", 2);
        CartItemEntity merged = userService.addToCart(user.getId(), product, "默认", 3);
        assertEquals(5, merged.getQuantity());
    }

    @Test
    void updateCartItemQuantityRejectsWrongUser() {
        UserEntity owner = userService.getOrCreateUser(null);
        UserEntity other = userService.getOrCreateUser(null);
        ProductEntity product = createProduct();

        CartItemEntity item = userService.addToCart(owner.getId(), product, "", 1);

        // Other user should not be able to update
        CartItemEntity result = userService.updateCartItemQuantity(item.getId(), 5, other.getId());
        assertNull(result);

        // Owner should be able to update
        CartItemEntity ownerResult = userService.updateCartItemQuantity(item.getId(), 5, owner.getId());
        assertNotNull(ownerResult);
        assertEquals(5, ownerResult.getQuantity());
    }

    @Test
    void updateCartItemSelectedRejectsWrongUser() {
        UserEntity owner = userService.getOrCreateUser(null);
        UserEntity other = userService.getOrCreateUser(null);
        ProductEntity product = createProduct();

        CartItemEntity item = userService.addToCart(owner.getId(), product, "", 1);

        CartItemEntity result = userService.updateCartItemSelected(item.getId(), false, other.getId());
        assertNull(result);

        CartItemEntity ownerResult = userService.updateCartItemSelected(item.getId(), false, owner.getId());
        assertNotNull(ownerResult);
        assertFalse(ownerResult.getSelected());
    }

    @Test
    void removeCartItemRejectsWrongUser() {
        UserEntity owner = userService.getOrCreateUser(null);
        UserEntity other = userService.getOrCreateUser(null);
        ProductEntity product = createProduct();

        CartItemEntity item = userService.addToCart(owner.getId(), product, "", 1);
        Long itemId = item.getId();

        // Other user cannot remove
        userService.removeCartItem(itemId, other.getId());
        assertTrue(cartItemRepository.findById(itemId).isPresent());

        // Owner can remove
        userService.removeCartItem(itemId, owner.getId());
        assertFalse(cartItemRepository.findById(itemId).isPresent());
    }

    @Test
    void deleteUserCascadesRelatedData() {
        UserEntity user = userService.getOrCreateUser(null);
        Long userId = user.getId();
        ProductEntity product = createProduct();

        // Add related data
        userService.addToCart(userId, product, "", 1);
        userService.toggleFavorite(userId, product);
        userService.addBrowsingHistory(userId, product);

        // Verify data exists
        assertTrue(cartItemRepository.countByUserId(userId) > 0);
        assertTrue(favoriteRepository.countByUserId(userId) > 0);

        // Delete user
        userService.deleteUser(userId);

        // All related data should be gone
        assertEquals(0, cartItemRepository.countByUserId(userId));
        assertEquals(0, favoriteRepository.countByUserId(userId));
        assertFalse(userRepository.findById(userId).isPresent());
    }

    private ProductEntity createProduct() {
        ProductEntity product = new ProductEntity();
        product.setId(999L);
        product.setTitle("测试商品");
        product.setPrice(100.0);
        return product;
    }

    @Test
    void getRegisteredCountReturnsCorrectCount() {
        // Initially no users
        long initial = userService.getRegisteredCount();

        UserEntity user = userService.getOrCreateUser(null);
        user.setUserType("user");
        userService.saveUser(user);

        assertEquals(initial + 1, userService.getRegisteredCount());
    }

    @Test
    void getActiveCountReturnsCorrectCount() {
        long initialActive = userService.getActiveCount();

        UserEntity user1 = userService.getOrCreateUser(null);
        UserEntity user2 = userService.getOrCreateUser(null);
        user2.setActive(false);
        userService.saveUser(user2);

        // user1 is active by default, user2 is disabled
        assertEquals(initialActive + 1, userService.getActiveCount());
    }

    @Test
    void getUserTotalSpendingCalculatesCorrectly() {
        UserEntity user = userService.getOrCreateUser(null);

        // Create orders
        OrderEntity order1 = new OrderEntity();
        order1.setUserId(user.getId());
        order1.setUserName("测试");
        order1.setTotalAmount(100.0);
        order1.setStatus("completed");
        orderRepository.save(order1);

        OrderEntity order2 = new OrderEntity();
        order2.setUserId(user.getId());
        order2.setUserName("测试");
        order2.setTotalAmount(200.0);
        order2.setStatus("pending");
        orderRepository.save(order2);

        // Cancelled order should not count
        OrderEntity cancelledOrder = new OrderEntity();
        cancelledOrder.setUserId(user.getId());
        cancelledOrder.setUserName("测试");
        cancelledOrder.setTotalAmount(500.0);
        cancelledOrder.setStatus("cancelled");
        orderRepository.save(cancelledOrder);

        assertEquals(300.0, userService.getUserTotalSpending(user.getId()), 0.01);
    }

    @Test
    void getUserOrderCountReturnsCorrectCount() {
        UserEntity user = userService.getOrCreateUser(null);

        assertEquals(0, userService.getUserOrderCount(user.getId()));

        OrderEntity order = new OrderEntity();
        order.setUserId(user.getId());
        order.setUserName("测试");
        order.setTotalAmount(100.0);
        order.setStatus("completed");
        orderRepository.save(order);

        assertEquals(1, userService.getUserOrderCount(user.getId()));
    }
}
