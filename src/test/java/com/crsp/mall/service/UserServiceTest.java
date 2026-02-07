package com.crsp.mall.service;

import com.crsp.mall.entity.CartItemEntity;
import com.crsp.mall.entity.ProductEntity;
import com.crsp.mall.entity.UserEntity;
import com.crsp.mall.repository.CartItemRepository;
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

    private ProductEntity createProduct() {
        ProductEntity product = new ProductEntity();
        product.setId(999L);
        product.setTitle("测试商品");
        product.setPrice(100.0);
        return product;
    }
}
