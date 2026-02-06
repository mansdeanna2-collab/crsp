/**
 * 淘宝商城模板 - JavaScript交互
 */

document.addEventListener('DOMContentLoaded', function() {
    // 初始化导航
    initNavigation();
    // 初始化购物车功能
    initCart();
    // 初始化消息Tab切换
    initMessageTabs();
});

/**
 * 底部导航切换
 */
function initNavigation() {
    const navItems = document.querySelectorAll('.nav-item');
    const pages = document.querySelectorAll('.page');
    
    navItems.forEach(item => {
        item.addEventListener('click', function() {
            const pageName = this.getAttribute('data-page');
            
            // 更新导航激活状态
            navItems.forEach(nav => nav.classList.remove('active'));
            this.classList.add('active');
            
            // 切换页面
            pages.forEach(page => {
                page.classList.remove('active');
                if (page.id === `page-${pageName}`) {
                    page.classList.add('active');
                    // 滚动到顶部
                    window.scrollTo(0, 0);
                }
            });
        });
    });
}

/**
 * 购物车功能
 */
function initCart() {
    // 数量加减按钮
    const minusBtns = document.querySelectorAll('.qty-btn.minus');
    const plusBtns = document.querySelectorAll('.qty-btn.plus');
    
    minusBtns.forEach(btn => {
        btn.addEventListener('click', function() {
            const qtySpan = this.nextElementSibling;
            let qty = parseInt(qtySpan.textContent);
            if (qty > 1) {
                qtySpan.textContent = qty - 1;
                updateCartTotal();
            }
        });
    });
    
    plusBtns.forEach(btn => {
        btn.addEventListener('click', function() {
            const qtySpan = this.previousElementSibling;
            let qty = parseInt(qtySpan.textContent);
            if (qty < 99) {
                qtySpan.textContent = qty + 1;
                updateCartTotal();
            }
        });
    });
    
    // 单选框功能
    const itemCheckboxes = document.querySelectorAll('.item-checkbox');
    const shopCheckboxes = document.querySelectorAll('.shop-checkbox');
    const selectAllCheckbox = document.querySelector('.select-all');
    
    // 商品单选
    itemCheckboxes.forEach(checkbox => {
        checkbox.addEventListener('change', function() {
            const cartShop = this.closest('.cart-shop');
            const shopCheckbox = cartShop.querySelector('.shop-checkbox');
            const allItemsInShop = cartShop.querySelectorAll('.item-checkbox');
            const checkedItemsInShop = cartShop.querySelectorAll('.item-checkbox:checked');
            
            // 更新店铺全选状态
            shopCheckbox.checked = allItemsInShop.length === checkedItemsInShop.length;
            
            // 更新总全选状态
            updateSelectAllStatus();
            updateCartTotal();
        });
    });
    
    // 店铺全选
    shopCheckboxes.forEach(checkbox => {
        checkbox.addEventListener('change', function() {
            const cartShop = this.closest('.cart-shop');
            const itemsInShop = cartShop.querySelectorAll('.item-checkbox');
            
            itemsInShop.forEach(item => {
                item.checked = this.checked;
            });
            
            updateSelectAllStatus();
            updateCartTotal();
        });
    });
    
    // 全选
    if (selectAllCheckbox) {
        selectAllCheckbox.addEventListener('change', function() {
            const allCheckboxes = document.querySelectorAll('.item-checkbox, .shop-checkbox');
            allCheckboxes.forEach(cb => {
                cb.checked = this.checked;
            });
            updateCartTotal();
        });
    }
}

/**
 * 更新全选状态
 */
function updateSelectAllStatus() {
    const selectAllCheckbox = document.querySelector('.select-all');
    const allItemCheckboxes = document.querySelectorAll('.item-checkbox');
    const checkedItemCheckboxes = document.querySelectorAll('.item-checkbox:checked');
    
    if (selectAllCheckbox) {
        selectAllCheckbox.checked = allItemCheckboxes.length === checkedItemCheckboxes.length;
    }
}

/**
 * 更新购物车总价
 */
function updateCartTotal() {
    const cartItems = document.querySelectorAll('.cart-item');
    let total = 0;
    let count = 0;
    
    cartItems.forEach(item => {
        const checkbox = item.querySelector('.item-checkbox');
        if (checkbox && checkbox.checked) {
            const priceText = item.querySelector('.item-price').textContent;
            const price = parseFloat(priceText.replace('¥', ''));
            const qty = parseInt(item.querySelector('.qty-num').textContent);
            total += price * qty;
            count += qty;
        }
    });
    
    const totalPriceEl = document.querySelector('.total-price');
    const checkoutBtn = document.querySelector('.checkout-btn');
    
    if (totalPriceEl) {
        totalPriceEl.textContent = '¥' + total.toFixed(2);
    }
    
    if (checkoutBtn) {
        checkoutBtn.textContent = `结算(${count})`;
    }
}

/**
 * 消息Tab切换
 */
function initMessageTabs() {
    const tabs = document.querySelectorAll('.message-tab');
    
    tabs.forEach(tab => {
        tab.addEventListener('click', function() {
            tabs.forEach(t => t.classList.remove('active'));
            this.classList.add('active');
        });
    });
}

/**
 * 搜索功能
 */
document.querySelector('.search-btn')?.addEventListener('click', function() {
    const searchInput = document.querySelector('.search-bar input');
    const keyword = searchInput.value.trim();
    
    if (keyword) {
        alert(`搜索: ${keyword}`);
        // 实际项目中这里会跳转到搜索结果页面
    }
});

/**
 * 搜索框回车事件
 */
document.querySelector('.search-bar input')?.addEventListener('keypress', function(e) {
    if (e.key === 'Enter') {
        document.querySelector('.search-btn')?.click();
    }
});

/**
 * 商品卡片点击
 */
document.querySelectorAll('.product-card').forEach(card => {
    card.addEventListener('click', function() {
        // 实际项目中这里会跳转到商品详情页
        alert('查看商品详情');
    });
});

/**
 * 消息项点击
 */
document.querySelectorAll('.message-item').forEach(item => {
    item.addEventListener('click', function() {
        const title = this.querySelector('.message-title').textContent;
        // 实际项目中这里会跳转到消息详情页
        alert(`打开消息: ${title}`);
    });
});

/**
 * 菜单项点击
 */
document.querySelectorAll('.menu-item').forEach(item => {
    item.addEventListener('click', function() {
        const title = this.querySelector('span').textContent;
        // 实际项目中这里会跳转到对应功能页面
        alert(`打开: ${title}`);
    });
});

/**
 * 订单Tab点击
 */
document.querySelectorAll('.order-tab').forEach(tab => {
    tab.addEventListener('click', function() {
        const title = this.querySelector('span').textContent;
        // 实际项目中这里会跳转到对应订单列表
        alert(`查看: ${title}`);
    });
});

/**
 * 结算按钮点击
 */
document.querySelector('.checkout-btn')?.addEventListener('click', function() {
    const checkedCount = document.querySelectorAll('.item-checkbox:checked').length;
    
    if (checkedCount === 0) {
        alert('请选择要结算的商品');
        return;
    }
    
    // 实际项目中这里会跳转到结算页面
    alert('前往结算页面');
});

/**
 * 分类导航点击
 */
document.querySelectorAll('.category-item').forEach(item => {
    item.addEventListener('click', function() {
        const category = this.querySelector('span').textContent;
        // 实际项目中这里会跳转到对应分类页面
        alert(`查看分类: ${category}`);
    });
});

/**
 * 活动入口点击
 */
document.querySelectorAll('.activity-item').forEach(item => {
    item.addEventListener('click', function() {
        const activity = this.querySelector('span').textContent;
        // 实际项目中这里会跳转到对应活动页面
        alert(`进入活动: ${activity}`);
    });
});
