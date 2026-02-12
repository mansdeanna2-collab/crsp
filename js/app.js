/**
 * 成人玩具商城 - JavaScript交互
 */

let bottomNavEl = null;

document.addEventListener('DOMContentLoaded', function() {
    // 初始化导航
    initNavigation();
    // 初始化购物车功能
    initCart();
    // 初始化消息Tab切换
    initMessageTabs();
    // 初始化定位功能
    initLocation();
    // 初始化拍照搜索功能
    initCameraSearch();
    // 初始化商品详情功能
    initProductDetail();
    // 初始化搜索功能
    initSearch();
    // 初始化弹窗关闭功能
    initModals();
    // 初始化轮播图
    initBannerSlider();
    // 初始化聊天详情返回
    initChatDetail();
    // 初始化聊天输入
    initChatInput();
    // 初始化聊天页面滑动防护
    initChatSwipePrevention();
    // 初始化订单页面
    initOrderPage();
    // 初始化角标数据
    bottomNavEl = document.querySelector('.bottom-nav');
    primeBadgeCounts();
    // 初始化底部角标状态
    refreshBottomNavBadges();
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

            // 确保底部导航可见（从聊天详情页返回时）
            const bottomNav = document.querySelector('.bottom-nav');
            if (bottomNav) {
                bottomNav.style.display = '';
            }
        });
    });
}

/**
 * 初始化角标的数值来源
 */
function primeBadgeCounts() {
    document.querySelectorAll('.message-item .message-badge').forEach(badge => {
        const raw = (badge.textContent || '').replace(/[^\d]/g, '');
        const val = parseInt(raw, 10);
        badge.dataset.count = isNaN(val) ? '0' : String(val);
    });
    document.querySelectorAll('.nav-item .nav-badge').forEach(badge => {
        const raw = (badge.textContent || '').replace(/[^\d]/g, '');
        const val = parseInt(raw, 10);
        badge.dataset.count = isNaN(val) ? '0' : String(val);
    });
}

/**
 * 统一的角标展示
 * @param {HTMLElement} badgeEl 需要更新的角标元素
 * @param {number} count 展示的数量，0则隐藏
 */
function setBadgeValue(badgeEl, count) {
    if (!badgeEl) return;
    if (count > 0) {
        badgeEl.dataset.count = String(count);
        badgeEl.style.display = 'inline-flex';
        badgeEl.textContent = count > 99 ? '99+' : String(count);
    } else {
        badgeEl.style.display = 'none';
        badgeEl.dataset.count = '0';
    }
}

/**
 * 未读消息总数 -> 底部导航
 */
function updateMessageNavBadge() {
    const messageBadges = document.querySelectorAll('.message-item .message-badge');
    let total = 0;
    messageBadges.forEach(badge => {
        const val = parseInt(badge.dataset.count || '0', 10);
        if (!isNaN(val)) {
            total += val;
        }
    });
    const navBadge = document.querySelector('.nav-item[data-page=\"message\"] .nav-badge');
    setBadgeValue(navBadge, total);
}

/**
 * 购物车选中数量 -> 底部导航
 * @returns {number} 选中商品的数量总和
 */
function getSelectedCartQuantity() {
    const cartItems = document.querySelectorAll('.cart-item');
    let count = 0;
    cartItems.forEach(item => {
        const checkbox = item.querySelector('.item-checkbox');
        if (checkbox && checkbox.checked) {
            const qtyEl = item.querySelector('.qty-num');
            const qty = parseInt(qtyEl ? qtyEl.textContent : '0', 10);
            count += isNaN(qty) ? 0 : qty;
        }
    });
    return count;
}

/**
 * 更新购物车角标
 * @param {number} [precomputedCount] 预先计算的数量，未提供则自动计算
 */
function updateCartNavBadge(precomputedCount) {
    const navBadge = document.querySelector('.nav-item[data-page=\"cart\"] .nav-badge');
    const count = typeof precomputedCount === 'number' ? precomputedCount : getSelectedCartQuantity();
    setBadgeValue(navBadge, count);
}

/**
 * 刷新底部导航角标（消息、购物车）
 */
function refreshBottomNavBadges() {
    if (!bottomNavEl) return;
    updateMessageNavBadge();
    updateCartNavBadge();
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

    updateCartNavBadge(count);
}

/**
 * 消息Tab切换（带过滤功能）
 */
function initMessageTabs() {
    const tabs = document.querySelectorAll('.message-tab');
    const messageItems = document.querySelectorAll('.message-item');
    const messageList = document.querySelector('.message-list');

    tabs.forEach(tab => {
        tab.addEventListener('click', function() {
            tabs.forEach(t => t.classList.remove('active'));
            this.classList.add('active');

            const category = this.getAttribute('data-category');
            let visibleMessageCount = 0;
            messageItems.forEach(function(item) {
                if (category === 'all' || item.getAttribute('data-category') === category) {
                    item.classList.remove('hidden');
                    visibleMessageCount++;
                } else {
                    item.classList.add('hidden');
                }
            });

            // 显示/隐藏空状态提示
            let emptyTip = messageList.querySelector('.message-empty');
            if (visibleMessageCount === 0) {
                if (!emptyTip) {
                    emptyTip = document.createElement('div');
                    emptyTip.className = 'message-empty';
                    const icon = document.createElement('i');
                    icon.className = 'fas fa-inbox';
                    const text = document.createElement('p');
                    text.textContent = '暂无消息';
                    emptyTip.appendChild(icon);
                    emptyTip.appendChild(text);
                    messageList.appendChild(emptyTip);
                }
                emptyTip.style.display = '';
            } else {
                if (emptyTip) {
                    emptyTip.style.display = 'none';
                }
            }
        });
    });
}


/**
 * 消息项点击 - 打开聊天详情页
 */
document.querySelectorAll('.message-item').forEach(item => {
    item.addEventListener('click', function() {
        const title = this.querySelector('.message-title').textContent;
        const msgType = this.getAttribute('data-msg-type');
        const avatarStyle = this.querySelector('.message-avatar').getAttribute('style');
        const avatarIcon = this.querySelector('.message-avatar i').className;
        // 清除该消息的未读角标
        const badge = this.querySelector('.message-badge');
        if (badge) {
            badge.remove();
        }
        updateMessageNavBadge();
        showChatDetail(title, msgType, avatarStyle, avatarIcon);
    });
});

/**
 * 显示聊天详情页
 */
function showChatDetail(title, msgType, avatarStyle, avatarIcon) {
    const chatPage = document.getElementById('page-chat-detail');
    const chatTitle = document.getElementById('chatDetailTitle');
    const chatBody = document.getElementById('chatDetailBody');
    const messagePage = document.getElementById('page-message');
    const bottomNav = document.querySelector('.bottom-nav');

    if (!chatPage || !chatBody) return;

    // 简单清理输入以防注入
    const safeStyle = sanitizeStyleAttr(avatarStyle);
    const safeIcon = sanitizeClassName(avatarIcon);

    chatTitle.textContent = title;
    chatBody.innerHTML = '';

    if (msgType === 'logistics') {
        chatBody.innerHTML = renderLogisticsDetail();
    } else if (msgType === 'trade') {
        chatBody.innerHTML = renderTradeDetail();
    } else if (msgType === 'promo') {
        chatBody.innerHTML = renderPromoDetail();
    } else if (msgType === 'store') {
        chatBody.innerHTML = renderStoreChatDetail(safeStyle, safeIcon);
    } else if (msgType === 'service') {
        chatBody.innerHTML = renderServiceChatDetail(safeStyle, safeIcon);
    } else {
        chatBody.innerHTML = renderSystemDetail();
    }

    // 切换页面
    messagePage.classList.remove('active');
    chatPage.classList.add('active');
    // 隐藏底部导航
    if (bottomNav) {
        bottomNav.style.display = 'none';
    }
    window.scrollTo(0, 0);
}

/**
 * 清理样式属性值，仅允许安全的CSS渐变背景
 */
function sanitizeStyleAttr(style) {
    if (!style) return '';
    // 仅允许 background 属性中使用 linear-gradient 和安全的颜色值
    const match = style.match(/background:\s*(linear-gradient\(\s*\d+deg,\s*#[0-9a-fA-F]{3,8}(?:,\s*#[0-9a-fA-F]{3,8})*\s*\))/);
    if (match) {
        return 'background: ' + match[1];
    }
    return '';
}

/**
 * 清理CSS类名，仅允许合法的类名字符
 */
function sanitizeClassName(className) {
    if (!className) return '';
    return className.replace(/[^a-zA-Z0-9\s\-_]/g, '');
}

/**
 * 物流详情渲染
 */
function renderLogisticsDetail() {
    return '<div class="logistics-timeline">' +
        '<div class="logistics-timeline-header">' +
            '<i class="fas fa-truck"></i>' +
            '<div>' +
                '<div class="logistics-title">包裹运输中</div>' +
                '<div class="logistics-order">订单号: 2024021200001</div>' +
            '</div>' +
        '</div>' +
        '<div class="timeline-list">' +
            '<div class="timeline-item active">' +
                '<span class="timeline-dot"></span>' +
                '<div class="timeline-info">您的包裹已到达北京转运中心，预计明天送达<div class="timeline-date">2024-02-12 10:30</div></div>' +
            '</div>' +
            '<div class="timeline-item active">' +
                '<span class="timeline-dot"></span>' +
                '<div class="timeline-info">包裹已从上海发出<div class="timeline-date">2024-02-11 15:20</div></div>' +
            '</div>' +
            '<div class="timeline-item">' +
                '<span class="timeline-dot"></span>' +
                '<div class="timeline-info">卖家已发货，等待揽收<div class="timeline-date">2024-02-11 09:00</div></div>' +
            '</div>' +
            '<div class="timeline-item">' +
                '<span class="timeline-dot"></span>' +
                '<div class="timeline-info">订单已支付<div class="timeline-date">2024-02-10 22:15</div></div>' +
            '</div>' +
        '</div>' +
    '</div>' +
    '<div class="notification-card">' +
        '<div class="notification-card-header">' +
            '<div class="notification-card-icon" style="background: linear-gradient(135deg, #ff6b6b, #ff8e53);"><i class="fas fa-box"></i></div>' +
            '<div class="notification-card-title"><h4>商品信息</h4><span class="notification-time">时尚女装夏季新款连衣裙</span></div>' +
        '</div>' +
        '<div class="notification-card-body"><p>颜色: 粉色 | 尺码: M</p><p>数量: 1件</p></div>' +
    '</div>';
}

/**
 * 交易详情渲染
 */
function renderTradeDetail() {
    return '<div class="notification-card">' +
        '<div class="notification-card-header">' +
            '<div class="notification-card-icon" style="background: linear-gradient(135deg, #667eea, #764ba2);"><i class="fas fa-check-circle"></i></div>' +
            '<div class="notification-card-title"><h4>交易完成</h4><span class="notification-time">昨天 14:30</span></div>' +
        '</div>' +
        '<div class="notification-card-body"><p>您的订单已确认收货。</p><p>订单号: 2024020800002</p><p>商品: 休闲运动鞋 透气舒适</p><p>金额: ¥168.00</p></div>' +
    '</div>' +
    '<div class="notification-card">' +
        '<div class="notification-card-header">' +
            '<div class="notification-card-icon" style="background: linear-gradient(135deg, #43e97b, #38f9d7);"><i class="fas fa-star"></i></div>' +
            '<div class="notification-card-title"><h4>评价提醒</h4><span class="notification-time">待评价</span></div>' +
        '</div>' +
        '<div class="notification-card-body"><p>快来分享您的使用体验吧，还可获得积分奖励！</p></div>' +
    '</div>';
}

/**
 * 优惠活动详情渲染
 */
function renderPromoDetail() {
    return '<div class="notification-card">' +
        '<div class="notification-card-header">' +
            '<div class="notification-card-icon" style="background: linear-gradient(135deg, #43e97b, #38f9d7);"><i class="fas fa-percent"></i></div>' +
            '<div class="notification-card-title"><h4>618大促即将开始</h4><span class="notification-time">昨天 09:00</span></div>' +
        '</div>' +
        '<div class="notification-card-body"><p>618年中大促即将开始，以下优惠券已为您准备好：</p><p>🎫 满200减30 通用券</p><p>🎫 满500减80 品类券</p><p>🎫 满1000减150 大额券</p><p>活动时间: 6月1日-6月18日</p></div>' +
    '</div>';
}

/**
 * 店铺聊天详情渲染
 */
function renderStoreChatDetail(avatarStyle, avatarIcon) {
    return '<div class="chat-message">' +
        '<div class="chat-message-time">昨天 14:20</div>' +
        '<div class="chat-bubble-row from-other">' +
            '<div class="chat-bubble-avatar" style="' + avatarStyle + '"><i class="' + avatarIcon + '"></i></div>' +
            '<div class="chat-bubble">亲，您关注的商品正在限时特惠中~ 现在下单还能享受满减优惠哦！</div>' +
        '</div>' +
    '</div>' +
    '<div class="chat-message">' +
        '<div class="chat-bubble-row from-other">' +
            '<div class="chat-bubble-avatar" style="' + avatarStyle + '"><i class="' + avatarIcon + '"></i></div>' +
            '<div class="chat-bubble">限时特价 ¥128，原价 ¥256，仅剩最后50件！</div>' +
        '</div>' +
    '</div>';
}

/**
 * 客服聊天详情渲染
 */
function renderServiceChatDetail(avatarStyle, avatarIcon) {
    return '<div class="chat-message">' +
        '<div class="chat-message-time">周一 10:00</div>' +
        '<div class="chat-bubble-row from-other">' +
            '<div class="chat-bubble-avatar" style="' + avatarStyle + '"><i class="' + avatarIcon + '"></i></div>' +
            '<div class="chat-bubble">您好，有什么可以帮助您的吗？</div>' +
        '</div>' +
    '</div>' +
    '<div class="chat-message">' +
        '<div class="chat-bubble-row from-self">' +
            '<div class="chat-bubble-avatar" style="background: linear-gradient(135deg, #ff6b35, #ff5722);"><i class="fas fa-user"></i></div>' +
            '<div class="chat-bubble">我想问一下退货流程</div>' +
        '</div>' +
    '</div>' +
    '<div class="chat-message">' +
        '<div class="chat-bubble-row from-other">' +
            '<div class="chat-bubble-avatar" style="' + avatarStyle + '"><i class="' + avatarIcon + '"></i></div>' +
            '<div class="chat-bubble">好的，您可以在"我的订单"中找到需要退货的订单，点击"退换/售后"即可发起退货申请。如有疑问可随时联系我~</div>' +
        '</div>' +
    '</div>';
}

/**
 * 系统通知详情渲染
 */
function renderSystemDetail() {
    return '<div class="notification-card">' +
        '<div class="notification-card-header">' +
            '<div class="notification-card-icon" style="background: linear-gradient(135deg, #a18cd1, #fbc2eb);"><i class="fas fa-bell"></i></div>' +
            '<div class="notification-card-title"><h4>系统通知</h4><span class="notification-time">上周</span></div>' +
        '</div>' +
        '<div class="notification-card-body"><p>您的账户安全等级已更新为"高"。</p><p>建议定期修改密码以确保账户安全。</p></div>' +
    '</div>';
}

/**
 * 初始化聊天详情返回按钮
 */
function initChatDetail() {
    const backBtn = document.getElementById('chatBackBtn');
    if (backBtn) {
        backBtn.addEventListener('click', function() {
            const chatPage = document.getElementById('page-chat-detail');
            const messagePage = document.getElementById('page-message');
            const bottomNav = document.querySelector('.bottom-nav');
            chatPage.classList.remove('active');
            messagePage.classList.add('active');
            // 恢复底部导航
            if (bottomNav) {
                bottomNav.style.display = '';
            }
        });
    }
}

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

/**
 * 初始化弹窗关闭功能
 */
function initModals() {
    // 关闭按钮点击
    document.querySelectorAll('.close-modal').forEach(btn => {
        btn.addEventListener('click', function() {
            const modal = this.closest('.modal');
            closeModal(modal);
        });
    });

    // 点击弹窗外部关闭
    document.querySelectorAll('.modal').forEach(modal => {
        modal.addEventListener('click', function(e) {
            if (e.target === this) {
                closeModal(this);
            }
        });
    });
}

/**
 * 打开弹窗
 */
function openModal(modalId) {
    const modal = document.getElementById(modalId);
    if (modal) {
        modal.classList.add('active');
        document.body.style.overflow = 'hidden';
    }
}

/**
 * 关闭弹窗
 */
function closeModal(modal) {
    if (modal) {
        modal.classList.remove('active');
        document.body.style.overflow = '';
        // 如果是相机弹窗，停止相机
        if (modal.id === 'camera-modal') {
            stopCamera();
        }
    }
}

/**
 * 初始化定位功能
 */
function initLocation() {
    const locationBtn = document.querySelector('.location');
    const locationModal = document.getElementById('location-modal');
    const locationCurrent = document.querySelector('.location-current span');
    const locationItems = document.querySelectorAll('.location-item');
    const refreshLocationBtn = document.querySelector('.refresh-location-btn');

    if (locationBtn) {
        locationBtn.addEventListener('click', function() {
            openModal('location-modal');
            // 尝试获取当前位置
            getCurrentLocation();
        });
    }

    // 刷新定位按钮
    if (refreshLocationBtn) {
        refreshLocationBtn.addEventListener('click', function() {
            getCurrentLocation();
        });
    }

    // 城市选择点击
    locationItems.forEach(item => {
        item.addEventListener('click', function() {
            const city = this.getAttribute('data-city');
            updateLocation(city);
            // 标记选中状态
            locationItems.forEach(i => i.classList.remove('active'));
            this.classList.add('active');
            closeModal(locationModal);
        });
    });

    // 从本地存储恢复位置
    const savedLocation = localStorage.getItem('userLocation');
    if (savedLocation) {
        updateLocation(savedLocation);
    }
}

/**
 * 获取当前位置
 */
function getCurrentLocation() {
    const locationCurrent = document.querySelector('.location-current span');
    const refreshBtn = document.querySelector('.refresh-location-btn');
    
    if (!navigator.geolocation) {
        locationCurrent.textContent = '浏览器不支持定位';
        return;
    }

    locationCurrent.textContent = '正在定位...';
    if (refreshBtn) {
        refreshBtn.disabled = true;
    }
    
    navigator.geolocation.getCurrentPosition(
        function(position) {
            // 成功获取位置
            const lat = position.coords.latitude;
            const lng = position.coords.longitude;
            const accuracy = position.coords.accuracy;
            
            // 显示坐标信息
            locationCurrent.textContent = `定位成功 (精度: ${Math.round(accuracy)}m)`;
            
            // 调用逆地理编码获取城市名
            reverseGeocode(lat, lng);
            
            if (refreshBtn) {
                refreshBtn.disabled = false;
            }
        },
        function(error) {
            // 获取位置失败
            let errorMsg = '定位失败';
            switch(error.code) {
                case error.PERMISSION_DENIED:
                    errorMsg = '用户拒绝定位，请手动选择城市';
                    break;
                case error.POSITION_UNAVAILABLE:
                    errorMsg = '位置信息不可用，请手动选择城市';
                    break;
                case error.TIMEOUT:
                    errorMsg = '定位超时，请手动选择城市';
                    break;
            }
            locationCurrent.textContent = errorMsg;
            if (refreshBtn) {
                refreshBtn.disabled = false;
            }
        },
        {
            enableHighAccuracy: true,
            timeout: 15000,
            maximumAge: 300000 // 5分钟内的位置信息可复用
        }
    );
}

/**
 * 逆地理编码 - 使用OpenStreetMap Nominatim API
 */
function reverseGeocode(lat, lng) {
    const locationCurrent = document.querySelector('.location-current span');
    
    // 使用OpenStreetMap Nominatim API（免费，无需API密钥）
    const url = `https://nominatim.openstreetmap.org/reverse?format=json&lat=${lat}&lon=${lng}&zoom=10&accept-language=zh-CN`;
    
    fetch(url, {
        headers: {
            'User-Agent': 'MallApp/1.0'
        }
    })
    .then(response => response.json())
    .then(data => {
        let city = '未知位置';
        
        if (data && data.address) {
            // 优先获取城市名，其次是区县名
            city = data.address.city || 
                   data.address.county || 
                   data.address.state || 
                   data.address.town ||
                   data.address.village ||
                   '未知位置';
        }
        
        locationCurrent.textContent = `当前位置: ${city}`;
        updateLocation(city);
        
        // 保存到本地存储
        localStorage.setItem('userLocation', city);
        localStorage.setItem('userCoords', JSON.stringify({lat, lng}));
    })
    .catch(error => {
        console.error('逆地理编码失败:', error);
        // 使用备用方案：根据坐标估算城市
        const estimatedCity = estimateCityFromCoords(lat, lng);
        locationCurrent.textContent = `当前位置: ${estimatedCity} (坐标: ${lat.toFixed(4)}, ${lng.toFixed(4)})`;
        updateLocation(estimatedCity);
    });
}

/**
 * 根据坐标估算城市（备用方案）
 */
function estimateCityFromCoords(lat, lng) {
    // 主要省市坐标范围
    const cities = [
        { name: '北京', lat: 39.9042, lng: 116.4074, range: 1 },
        { name: '天津', lat: 39.3434, lng: 117.3616, range: 1 },
        { name: '上海', lat: 31.2304, lng: 121.4737, range: 1 },
        { name: '重庆', lat: 29.4316, lng: 106.9123, range: 1 },
        { name: '河北', lat: 38.0428, lng: 114.5149, range: 0.8 },
        { name: '山西', lat: 37.8706, lng: 112.5489, range: 0.8 },
        { name: '辽宁', lat: 41.8057, lng: 123.4315, range: 0.8 },
        { name: '吉林', lat: 43.8171, lng: 125.3235, range: 0.8 },
        { name: '黑龙江', lat: 45.8038, lng: 126.5349, range: 0.8 },
        { name: '江苏', lat: 32.0603, lng: 118.7969, range: 0.8 },
        { name: '浙江', lat: 30.2741, lng: 120.1551, range: 0.8 },
        { name: '安徽', lat: 31.8206, lng: 117.2272, range: 0.8 },
        { name: '福建', lat: 26.0745, lng: 119.2965, range: 0.8 },
        { name: '江西', lat: 28.6820, lng: 115.8579, range: 0.8 },
        { name: '山东', lat: 36.6512, lng: 117.1201, range: 0.8 },
        { name: '河南', lat: 34.7466, lng: 113.6254, range: 0.8 },
        { name: '湖北', lat: 30.5928, lng: 114.3055, range: 0.8 },
        { name: '湖南', lat: 28.2278, lng: 112.9389, range: 0.8 },
        { name: '广东', lat: 23.1291, lng: 113.2644, range: 0.8 },
        { name: '海南', lat: 20.0442, lng: 110.1983, range: 0.8 },
        { name: '四川', lat: 30.5728, lng: 104.0668, range: 0.8 },
        { name: '贵州', lat: 26.6470, lng: 106.6302, range: 0.8 },
        { name: '云南', lat: 25.0389, lng: 102.7183, range: 0.8 },
        { name: '陕西', lat: 34.3416, lng: 108.9398, range: 0.8 },
        { name: '甘肃', lat: 36.0611, lng: 103.8343, range: 0.8 },
        { name: '青海', lat: 36.6171, lng: 101.7782, range: 0.8 },
        { name: '台湾', lat: 25.0330, lng: 121.5654, range: 0.8 },
        { name: '内蒙古', lat: 40.8426, lng: 111.7492, range: 0.8 },
        { name: '广西', lat: 22.8170, lng: 108.3669, range: 0.8 },
        { name: '西藏', lat: 29.6525, lng: 91.1721, range: 0.8 },
        { name: '宁夏', lat: 38.4872, lng: 106.2309, range: 0.8 },
        { name: '新疆', lat: 43.8256, lng: 87.6168, range: 0.8 },
        { name: '香港', lat: 22.3193, lng: 114.1694, range: 0.8 },
        { name: '澳门', lat: 22.1987, lng: 113.5439, range: 0.8 }
    ];
    
    let nearestCity = '当前位置';
    let minDistance = Infinity;
    
    cities.forEach(city => {
        const distance = Math.sqrt(
            Math.pow(lat - city.lat, 2) + 
            Math.pow(lng - city.lng, 2)
        );
        if (distance < city.range && distance < minDistance) {
            minDistance = distance;
            nearestCity = city.name;
        }
    });
    
    return nearestCity;
}

/**
 * 更新定位显示
 */
function updateLocation(city) {
    const locationText = document.querySelector('.location span');
    if (locationText) {
        locationText.textContent = city;
    }
    // 保存到本地存储
    localStorage.setItem('userLocation', city);
}

/**
 * 初始化拍照搜索功能
 */
let cameraStream = null;

function initCameraSearch() {
    const cameraIcon = document.querySelector('.header-icons i.fa-camera');
    const captureBtn = document.getElementById('capture-btn');
    const retakeBtn = document.getElementById('retake-btn');
    const searchImageBtn = document.getElementById('search-image-btn');
    const uploadBtn = document.getElementById('upload-btn');
    const fileInput = document.getElementById('file-input');

    if (cameraIcon) {
        cameraIcon.addEventListener('click', function() {
            openModal('camera-modal');
            startCamera();
        });
    }

    // 拍照按钮
    if (captureBtn) {
        captureBtn.addEventListener('click', captureImage);
    }

    // 重拍按钮
    if (retakeBtn) {
        retakeBtn.addEventListener('click', function() {
            showCameraView();
            startCamera();
        });
    }

    // 搜索按钮
    if (searchImageBtn) {
        searchImageBtn.addEventListener('click', function() {
            searchByImage();
        });
    }

    // 从相册选择
    if (uploadBtn) {
        uploadBtn.addEventListener('click', function() {
            fileInput.click();
        });
    }

    // 文件选择
    if (fileInput) {
        fileInput.addEventListener('change', function(e) {
            const file = e.target.files[0];
            if (file) {
                const reader = new FileReader();
                reader.onload = function(event) {
                    showCapturedImage(event.target.result);
                };
                reader.readAsDataURL(file);
            }
        });
    }
}

/**
 * 启动相机
 */
function startCamera() {
    const video = document.getElementById('camera-preview');
    
    if (navigator.mediaDevices && navigator.mediaDevices.getUserMedia) {
        navigator.mediaDevices.getUserMedia({ 
            video: { 
                facingMode: 'environment',
                width: { ideal: 640 },
                height: { ideal: 480 }
            } 
        })
        .then(function(stream) {
            cameraStream = stream;
            video.srcObject = stream;
            video.style.display = 'block';
        })
        .catch(function(err) {
            console.error('相机访问失败:', err);
            alert('无法访问相机，请确保已授权相机权限');
        });
    } else {
        alert('您的浏览器不支持相机功能');
    }
}

/**
 * 停止相机
 */
function stopCamera() {
    if (cameraStream) {
        cameraStream.getTracks().forEach(track => track.stop());
        cameraStream = null;
    }
    const video = document.getElementById('camera-preview');
    if (video) {
        video.srcObject = null;
    }
}

/**
 * 拍照
 */
function captureImage() {
    const video = document.getElementById('camera-preview');
    const canvas = document.getElementById('camera-canvas');
    const ctx = canvas.getContext('2d');
    
    canvas.width = video.videoWidth;
    canvas.height = video.videoHeight;
    ctx.drawImage(video, 0, 0);
    
    const imageData = canvas.toDataURL('image/jpeg');
    showCapturedImage(imageData);
    stopCamera();
}

/**
 * 显示拍摄的图片
 */
function showCapturedImage(imageData) {
    const video = document.getElementById('camera-preview');
    const imageContainer = document.getElementById('captured-image-container');
    const capturedImage = document.getElementById('captured-image');
    const captureBtn = document.getElementById('capture-btn');
    const retakeBtn = document.getElementById('retake-btn');
    const searchImageBtn = document.getElementById('search-image-btn');

    video.style.display = 'none';
    imageContainer.style.display = 'block';
    capturedImage.src = imageData;
    
    captureBtn.style.display = 'none';
    retakeBtn.style.display = 'flex';
    searchImageBtn.style.display = 'flex';
}

/**
 * 显示相机视图
 */
function showCameraView() {
    const video = document.getElementById('camera-preview');
    const imageContainer = document.getElementById('captured-image-container');
    const captureBtn = document.getElementById('capture-btn');
    const retakeBtn = document.getElementById('retake-btn');
    const searchImageBtn = document.getElementById('search-image-btn');

    video.style.display = 'block';
    imageContainer.style.display = 'none';
    
    captureBtn.style.display = 'flex';
    retakeBtn.style.display = 'none';
    searchImageBtn.style.display = 'none';
}

/**
 * 图片搜索
 */
function searchByImage() {
    // 关闭相机弹窗
    closeModal(document.getElementById('camera-modal'));
    
    // 显示搜索结果（模拟）
    showSearchResults('图片搜索');
}

/**
 * 初始化搜索功能
 */
function initSearch() {
    const searchBtn = document.querySelector('.search-btn');
    const searchInput = document.querySelector('.search-bar input');

    if (searchBtn) {
        searchBtn.addEventListener('click', function() {
            const keyword = searchInput.value.trim();
            if (keyword) {
                showSearchResults(keyword);
            } else {
                alert('请输入搜索关键词');
            }
        });
    }

    if (searchInput) {
        searchInput.addEventListener('keypress', function(e) {
            if (e.key === 'Enter') {
                searchBtn.click();
            }
        });
    }
}

/**
 * 显示搜索结果
 */
function showSearchResults(keyword) {
    const searchResults = document.getElementById('search-results');
    
    // 模拟搜索结果
    const products = [
        { title: '时尚女装夏季新款', price: '¥128', icon: 'fa-tshirt', bg: '#ffecd2, #fcb69f' },
        { title: '新款智能手机', price: '¥2999', icon: 'fa-mobile-alt', bg: '#a1c4fd, #c2e9fb' },
        { title: '护肤套装补水保湿', price: '¥89', icon: 'fa-pump-soap', bg: '#d299c2, #fef9d7' },
        { title: '无线蓝牙耳机', price: '¥199', icon: 'fa-headphones', bg: '#f5f7fa, #c3cfe2' }
    ];

    searchResults.innerHTML = products.map((product, index) => `
        <div class="search-result-item" data-index="${index}">
            <div class="search-result-image" style="background: linear-gradient(135deg, ${product.bg});">
                <i class="fas ${product.icon}"></i>
            </div>
            <div class="search-result-info">
                <h4>${product.title}</h4>
                <span class="result-price">${product.price}</span>
            </div>
        </div>
    `).join('');

    // 为搜索结果添加点击事件
    searchResults.querySelectorAll('.search-result-item').forEach(item => {
        item.addEventListener('click', function() {
            const index = this.getAttribute('data-index');
            const product = products[index];
            closeModal(document.getElementById('search-modal'));
            const priceValue = parseFloat(product.price.replace(/[^\d.]/g, '')) || 0;
            showProductDetail({
                title: product.title,
                price: product.price,
                originalPrice: '¥' + (priceValue * 2),
                sales: '已售 1000+件',
                icon: product.icon,
                bg: product.bg
            });
        });
    });

    openModal('search-modal');
}

/**
 * 初始化商品详情功能
 */
function initProductDetail() {
    // 定义每个商品的规格数据（含价格）
    var productSpecsData = [
        [ {name: '粉色 S', price: '¥118'}, {name: '粉色 M', price: '¥128'}, {name: '黑色 M', price: '¥138'} ],
        [ {name: '64GB', price: '¥2699'}, {name: '128GB', price: '¥2999'}, {name: '256GB', price: '¥3299'} ],
        [ {name: '基础套装', price: '¥89'}, {name: '豪华套装', price: '¥139'} ],
        [ {name: '白色', price: '¥199'}, {name: '黑色', price: '¥199'}, {name: '蓝色', price: '¥219'} ],
        [ {name: '白色 40', price: '¥158'}, {name: '白色 42', price: '¥168'}, {name: '黑色 42', price: '¥178'} ],
        [ {name: '标准版', price: '¥299'}, {name: '高配版', price: '¥399'} ]
    ];

    document.querySelectorAll('.product-card').forEach((card, index) => {
        card.addEventListener('click', function() {
            const titleEl = this.querySelector('.product-title');
            const priceEl = this.querySelector('.price');
            const originalPriceEl = this.querySelector('.original-price');
            const salesEl = this.querySelector('.product-sales');
            const imageEl = this.querySelector('.product-image');
            const iconEl = this.querySelector('.product-image i');

            const productData = {
                title: titleEl ? titleEl.textContent : '商品名称',
                price: priceEl ? priceEl.textContent : '¥0',
                originalPrice: originalPriceEl ? originalPriceEl.textContent : '¥0',
                sales: salesEl ? salesEl.textContent : '已售 0件',
                icon: iconEl ? iconEl.className.replace('fas ', '') : 'fa-shopping-bag',
                bg: imageEl ? imageEl.style.background : 'linear-gradient(135deg, #ffecd2, #fcb69f)',
                specs: productSpecsData[index] || [{name: '默认规格'}]
            };

            showProductDetail(productData);
        });
    });

    // 加入购物车按钮
    const addCartBtn = document.querySelector('.add-cart-btn');
    if (addCartBtn) {
        addCartBtn.addEventListener('click', function() {
            var priceEl = document.getElementById('detail-price');
            var currentPrice = priceEl ? priceEl.textContent : '¥0';
            var specInfo = '';
            if (currentSelectedSpec >= 0 && currentProductSpecs[currentSelectedSpec]) {
                specInfo = ' 规格：' + currentProductSpecs[currentSelectedSpec].name;
            }
            alert('已加入购物车 价格：' + currentPrice + specInfo);
            closeModal(document.getElementById('product-detail-modal'));
        });
    }

    // 立即购买按钮
    const buyNowBtn = document.querySelector('.buy-now-btn');
    if (buyNowBtn) {
        buyNowBtn.addEventListener('click', function() {
            var priceEl = document.getElementById('detail-price');
            var currentPrice = priceEl ? priceEl.textContent : '¥0';
            var specInfo = '';
            if (currentSelectedSpec >= 0 && currentProductSpecs[currentSelectedSpec]) {
                specInfo = ' 规格：' + currentProductSpecs[currentSelectedSpec].name;
            }
            alert('前往结算页面 价格：' + currentPrice + specInfo);
            closeModal(document.getElementById('product-detail-modal'));
        });
    }
}

/**
 * 当前选中的规格索引
 */
var currentSelectedSpec = -1;
var currentProductSpecs = [];

/**
 * 显示商品详情
 */
function showProductDetail(productData) {
    const detailImage = document.getElementById('detail-image');
    const detailImage2 = document.getElementById('detail-image-2');
    const detailImage3 = document.getElementById('detail-image-3');
    const detailTitle = document.getElementById('detail-title');
    const detailPrice = document.getElementById('detail-price');
    const detailOriginalPrice = document.getElementById('detail-original-price');
    const detailSales = document.getElementById('detail-sales');
    const detailTrack = document.getElementById('detail-image-track');

    if (detailImage) {
        // 解析背景样式
        let bgStyle = productData.bg;
        if (bgStyle && !bgStyle.includes('linear-gradient')) {
            bgStyle = `linear-gradient(135deg, ${bgStyle})`;
        }
        detailImage.style.background = bgStyle;
        detailImage.innerHTML = `<i class="fas ${productData.icon}"></i>`;
        // 为其他图片设置不同的展示样式
        if (detailImage2) {
            detailImage2.style.background = bgStyle;
            detailImage2.innerHTML = `<i class="fas ${productData.icon}"></i>`;
        }
        if (detailImage3) {
            detailImage3.style.background = bgStyle;
            detailImage3.innerHTML = `<i class="fas ${productData.icon}"></i>`;
        }
    }

    if (detailTitle) detailTitle.textContent = productData.title;
    if (detailPrice) detailPrice.textContent = productData.price;
    if (detailOriginalPrice) detailOriginalPrice.textContent = productData.originalPrice;
    if (detailSales) detailSales.textContent = productData.sales;

    // 渲染规格选项
    currentSelectedSpec = -1;
    currentProductSpecs = productData.specs || [{name: '默认规格'}];
    var specContainer = document.getElementById('detail-spec-options');
    if (specContainer) {
        specContainer.innerHTML = '';
        currentProductSpecs.forEach(function(spec, idx) {
            var tag = document.createElement('span');
            tag.className = 'spec-tag';
            tag.textContent = spec.name + (spec.price ? ' ' + spec.price : '');
            tag.style.cursor = 'pointer';
            tag.addEventListener('click', function() {
                currentSelectedSpec = idx;
                // 更新选中样式
                specContainer.querySelectorAll('.spec-tag').forEach(function(t) {
                    t.style.background = '#f5f5f5';
                    t.style.color = '#666';
                    t.style.border = 'none';
                });
                tag.style.background = '#fff5f2';
                tag.style.color = '#ff5722';
                tag.style.border = '1px solid #ff5722';
                // 更新价格显示
                if (spec.price && detailPrice) {
                    detailPrice.textContent = spec.price;
                }
            });
            specContainer.appendChild(tag);
        });
    }

    // 重置图片滑块位置
    if (detailTrack) {
        detailTrack.style.transition = 'none';
        detailTrack.style.transform = 'translateX(0)';
    }
    var detailDots = document.querySelectorAll('#detail-image-indicators .detail-dot');
    detailDots.forEach(function(d, i) { d.classList.toggle('active', i === 0); });

    openModal('product-detail-modal');
    initDetailImageSlider();
}

/**
 * 首页轮播图自动播放与滑动
 */
function initBannerSlider() {
    var track = document.getElementById('bannerTrack');
    var dots = document.querySelectorAll('#bannerIndicators .banner-dot');
    if (!track || dots.length === 0) return;
    var items = track.querySelectorAll('.banner-item');
    var total = items.length;
    if (total <= 0) return;
    var current = 0;
    var timer = null;

    function goTo(index) {
        current = ((index % total) + total) % total;
        track.style.transform = 'translateX(-' + (current * 100) + '%)';
        dots.forEach(function(d, i) { d.classList.toggle('active', i === current); });
    }

    function next() { goTo(current + 1); }

    function startAutoPlay() { timer = setInterval(next, 3000); }
    function stopAutoPlay() { clearInterval(timer); }

    dots.forEach(function(dot, i) {
        dot.addEventListener('click', function() { goTo(i); stopAutoPlay(); startAutoPlay(); });
    });

    // 触摸滑动
    let startX = 0;
    let startY = 0;
    let isDragging = false;
    let directionLocked = false;
    let isHorizontal = false;

    track.addEventListener('touchstart', function(e) {
        startX = e.touches[0].clientX;
        startY = e.touches[0].clientY;
        isDragging = true;
        directionLocked = false;
        isHorizontal = false;
        stopAutoPlay();
        track.style.transition = 'none';
    }, { passive: true });

    track.addEventListener('touchmove', function(e) {
        if (!isDragging) return;
        var diffX = e.touches[0].clientX - startX;
        var diffY = e.touches[0].clientY - startY;

        // Lock direction on first significant move
        if (!directionLocked && (Math.abs(diffX) > 5 || Math.abs(diffY) > 5)) {
            directionLocked = true;
            isHorizontal = Math.abs(diffX) > Math.abs(diffY);
        }

        if (!directionLocked || !isHorizontal) return;

        e.preventDefault();

        // Apply boundary resistance at edges
        var atStart = current === 0 && diffX > 0;
        var atEnd = current === total - 1 && diffX < 0;
        if (atStart || atEnd) {
            diffX = diffX * 0.3; // dampen drag at boundaries
        }

        var baseTranslate = -current * 100;
        var dragPercent = (diffX / track.offsetWidth) * 100;
        track.style.transform = 'translateX(' + (baseTranslate + dragPercent) + '%)';
    }, { passive: false });

    track.addEventListener('touchend', function(e) {
        if (!isDragging) return;
        isDragging = false;
        directionLocked = false;
        track.style.transition = 'transform 0.4s ease';
        var diff = startX - e.changedTouches[0].clientX;
        if (Math.abs(diff) > 40) {
            diff > 0 ? goTo(current + 1) : goTo(current - 1);
        } else {
            goTo(current);
        }
        startAutoPlay();
    }, { passive: true });

    track.addEventListener('touchcancel', function() {
        isDragging = false;
        directionLocked = false;
        track.style.transition = 'transform 0.4s ease';
        goTo(current);
        startAutoPlay();
    }, { passive: true });

    // 鼠标拖拽支持（桌面端）
    let mouseStartX = 0;
    let mouseIsDragging = false;

    track.addEventListener('mousedown', function(e) {
        mouseStartX = e.clientX;
        mouseIsDragging = true;
        stopAutoPlay();
        track.style.transition = 'none';
        e.preventDefault();
    });

    document.addEventListener('mousemove', function(e) {
        if (!mouseIsDragging) return;
        var diffX = e.clientX - mouseStartX;

        var atStart = current === 0 && diffX > 0;
        var atEnd = current === total - 1 && diffX < 0;
        if (atStart || atEnd) {
            diffX = diffX * 0.3;
        }

        var baseTranslate = -current * 100;
        var dragPercent = (diffX / track.offsetWidth) * 100;
        track.style.transform = 'translateX(' + (baseTranslate + dragPercent) + '%)';
    });

    document.addEventListener('mouseup', function(e) {
        if (!mouseIsDragging) return;
        mouseIsDragging = false;
        track.style.transition = 'transform 0.4s ease';
        var diff = mouseStartX - e.clientX;
        if (Math.abs(diff) > 40) {
            diff > 0 ? goTo(current + 1) : goTo(current - 1);
        } else {
            goTo(current);
        }
        startAutoPlay();
    });

    startAutoPlay();
}

/**
 * 商品详情图片滑动器
 */
function initDetailImageSlider() {
    var track = document.getElementById('detail-image-track');
    var dots = document.querySelectorAll('#detail-image-indicators .detail-dot');
    if (!track || dots.length === 0) return;
    var items = track.querySelectorAll('.detail-image');
    var total = items.length;
    if (total <= 0) return;
    var current = 0;

    function goTo(index) {
        current = ((index % total) + total) % total;
        track.style.transition = 'transform 0.4s ease';
        track.style.transform = 'translateX(-' + (current * 100) + '%)';
        dots.forEach(function(d, i) { d.classList.toggle('active', i === current); });
    }

    dots.forEach(function(dot, i) {
        dot.addEventListener('click', function() { goTo(i); });
    });

    // 触摸滑动
    var startX = 0;
    var startY = 0;
    var isDragging = false;
    var directionLocked = false;
    var isHorizontal = false;

    track.addEventListener('touchstart', function(e) {
        startX = e.touches[0].clientX;
        startY = e.touches[0].clientY;
        isDragging = true;
        directionLocked = false;
        isHorizontal = false;
        track.style.transition = 'none';
    }, { passive: true });

    track.addEventListener('touchmove', function(e) {
        if (!isDragging) return;
        var diffX = e.touches[0].clientX - startX;
        var diffY = e.touches[0].clientY - startY;

        if (!directionLocked && (Math.abs(diffX) > 5 || Math.abs(diffY) > 5)) {
            directionLocked = true;
            isHorizontal = Math.abs(diffX) > Math.abs(diffY);
        }

        if (!directionLocked || !isHorizontal) return;
        e.preventDefault();

        var atStart = current === 0 && diffX > 0;
        var atEnd = current === total - 1 && diffX < 0;
        if (atStart || atEnd) {
            diffX = diffX * 0.3;
        }

        var baseTranslate = -current * 100;
        var dragPercent = (diffX / track.offsetWidth) * 100;
        track.style.transform = 'translateX(' + (baseTranslate + dragPercent) + '%)';
    }, { passive: false });

    track.addEventListener('touchend', function(e) {
        if (!isDragging) return;
        isDragging = false;
        directionLocked = false;
        track.style.transition = 'transform 0.4s ease';
        var diff = startX - e.changedTouches[0].clientX;
        if (Math.abs(diff) > 40) {
            diff > 0 ? goTo(current + 1) : goTo(current - 1);
        } else {
            goTo(current);
        }
    }, { passive: true });
}

/**
 * 初始化聊天输入功能
 */
function initChatInput() {
    var chatInput = document.getElementById('chatInput');
    var chatSendBtn = document.getElementById('chatSendBtn');

    if (!chatInput || !chatSendBtn) return;

    // 阻止输入框的点击和焦点事件冒泡
    chatInput.addEventListener('click', function(e) {
        e.stopPropagation();
    });

    chatInput.addEventListener('focus', function(e) {
        e.stopPropagation();
        // 等待移动端键盘弹出后滚动到底部（键盘动画约需300ms）
        setTimeout(function() {
            window.scrollTo(0, document.body.scrollHeight);
        }, 300);
    });

    chatInput.addEventListener('touchstart', function(e) {
        e.stopPropagation();
    });

    // 发送消息
    chatSendBtn.addEventListener('click', function() {
        sendChatMessage();
    });

    chatInput.addEventListener('keypress', function(e) {
        if (e.key === 'Enter') {
            e.preventDefault();
            sendChatMessage();
        }
    });
}

/**
 * 发送聊天消息
 */
function sendChatMessage() {
    var chatInput = document.getElementById('chatInput');
    var chatBody = document.getElementById('chatDetailBody');
    var msg = chatInput.value.trim();
    if (!msg || !chatBody) return;

    var msgDiv = document.createElement('div');
    msgDiv.className = 'chat-message';
    msgDiv.innerHTML = '<div class="chat-bubble-row from-self">' +
        '<div class="chat-bubble-avatar" style="background: linear-gradient(135deg, #ff6b35, #ff5722);"><i class="fas fa-user"></i></div>' +
        '<div class="chat-bubble">' + escapeHtml(msg) + '</div>' +
        '</div>';
    chatBody.appendChild(msgDiv);
    chatInput.value = '';
    chatBody.scrollTop = chatBody.scrollHeight;
}

/**
 * HTML转义防止XSS
 */
function escapeHtml(text) {
    var div = document.createElement('div');
    div.appendChild(document.createTextNode(text));
    return div.innerHTML;
}

/**
 * 初始化聊天页面滑动防护
 */
function initChatSwipePrevention() {
    var chatPage = document.getElementById('page-chat-detail');
    if (!chatPage) return;

    var touchStartX = 0;
    var touchStartY = 0;

    chatPage.addEventListener('touchstart', function(e) {
        touchStartX = e.touches[0].clientX;
        touchStartY = e.touches[0].clientY;
    }, { passive: true });

    chatPage.addEventListener('touchmove', function(e) {
        var diffX = e.touches[0].clientX - touchStartX;
        var diffY = e.touches[0].clientY - touchStartY;
        // 如果是水平滑动，阻止默认行为以防止页面切换
        if (Math.abs(diffX) > Math.abs(diffY) && Math.abs(diffX) > 10) {
            e.preventDefault();
        }
    }, { passive: false });
}

/**
 * 初始化订单页面
 */
function initOrderPage() {
    // 订单Tab点击 - 覆盖原有alert行为
    document.querySelectorAll('.order-tab').forEach(function(tab) {
        // 移除原有的click事件（通过克隆节点）
        var newTab = tab.cloneNode(true);
        tab.parentNode.replaceChild(newTab, tab);
        newTab.addEventListener('click', function() {
            var title = this.querySelector('span:not(.tab-badge)').textContent;
            openOrderPage(title);
        });
    });

    // 查看全部订单
    var viewAllBtn = document.querySelector('.view-all');
    if (viewAllBtn) {
        var newBtn = viewAllBtn.cloneNode(true);
        viewAllBtn.parentNode.replaceChild(newBtn, viewAllBtn);
        newBtn.addEventListener('click', function() {
            openOrderPage('全部');
        });
    }

    // 订单页面返回按钮
    var backBtn = document.getElementById('ordersBackBtn');
    if (backBtn) {
        backBtn.addEventListener('click', function() {
            var orderPage = document.getElementById('page-orders');
            var profilePage = document.getElementById('page-profile');
            var bottomNav = document.querySelector('.bottom-nav');
            orderPage.classList.remove('active');
            profilePage.classList.add('active');
            if (bottomNav) {
                bottomNav.style.display = '';
            }
        });
    }

    // 订单筛选Tab切换
    var filterTabs = document.querySelectorAll('.order-filter-tab');
    filterTabs.forEach(function(tab) {
        tab.addEventListener('click', function() {
            filterTabs.forEach(function(t) { t.classList.remove('active'); });
            this.classList.add('active');
            var status = this.getAttribute('data-status');
            renderOrderList(status);
        });
    });
}

/**
 * 打开订单页面
 */
function openOrderPage(tabName) {
    var profilePage = document.getElementById('page-profile');
    var orderPage = document.getElementById('page-orders');
    var bottomNav = document.querySelector('.bottom-nav');

    if (!orderPage) return;

    profilePage.classList.remove('active');
    orderPage.classList.add('active');
    if (bottomNav) {
        bottomNav.style.display = 'none';
    }

    // 设置对应的筛选tab
    var statusMap = {
        '全部': 'all',
        '待付款': 'pending-payment',
        '待发货': 'pending-ship',
        '待收货': 'pending-receive',
        '待评价': 'pending-review',
        '退换/售后': 'all'
    };
    var status = statusMap[tabName] || 'all';
    var filterTabs = document.querySelectorAll('.order-filter-tab');
    filterTabs.forEach(function(t) {
        t.classList.toggle('active', t.getAttribute('data-status') === status);
    });

    renderOrderList(status);
    window.scrollTo(0, 0);
}

/**
 * 模拟订单数据
 */
var mockOrders = [
    {
        id: '2024021200001',
        shop: '品牌官方旗舰店',
        status: 'pending-receive',
        statusText: '待收货',
        product: '时尚女装夏季新款连衣裙 修身显瘦',
        spec: '颜色：粉色 尺码：M',
        price: '¥128.00',
        qty: 1,
        total: '¥128.00',
        icon: 'fa-tshirt',
        bg: 'linear-gradient(135deg, #ffecd2, #fcb69f)',
        actions: ['查看物流', '确认收货']
    },
    {
        id: '2024021100002',
        shop: '数码旗舰店',
        status: 'pending-payment',
        statusText: '待付款',
        product: '新款智能手机 5G全网通',
        spec: '颜色：黑色 存储：128GB',
        price: '¥2,999.00',
        qty: 1,
        total: '¥2,999.00',
        icon: 'fa-mobile-alt',
        bg: 'linear-gradient(135deg, #a1c4fd, #c2e9fb)',
        actions: ['取消订单', '立即付款']
    },
    {
        id: '2024020800003',
        shop: '运动专营店',
        status: 'pending-ship',
        statusText: '待发货',
        product: '休闲运动鞋 透气舒适 跑步鞋',
        spec: '颜色：白色 尺码：42',
        price: '¥168.00',
        qty: 1,
        total: '¥168.00',
        icon: 'fa-shoe-prints',
        bg: 'linear-gradient(135deg, #667eea, #764ba2)',
        actions: ['催发货', '退款']
    },
    {
        id: '2024020500004',
        shop: '美妆旗舰店',
        status: 'pending-review',
        statusText: '待评价',
        product: '护肤套装补水保湿 面霜精华套装',
        spec: '规格：标准套装',
        price: '¥89.00',
        qty: 2,
        total: '¥178.00',
        icon: 'fa-pump-soap',
        bg: 'linear-gradient(135deg, #d299c2, #fef9d7)',
        actions: ['评价', '再次购买']
    },
    {
        id: '2024020100005',
        shop: '品牌官方旗舰店',
        status: 'pending-receive',
        statusText: '待收货',
        product: '无线蓝牙耳机 降噪入耳式',
        spec: '颜色：白色',
        price: '¥199.00',
        qty: 1,
        total: '¥199.00',
        icon: 'fa-headphones',
        bg: 'linear-gradient(135deg, #f5f7fa, #c3cfe2)',
        actions: ['查看物流', '确认收货']
    }
];

/**
 * 渲染订单列表
 */
function renderOrderList(status) {
    var orderListEl = document.getElementById('orderList');
    if (!orderListEl) return;

    var filtered = status === 'all' ? mockOrders : mockOrders.filter(function(o) { return o.status === status; });

    if (filtered.length === 0) {
        orderListEl.innerHTML = '<div class="order-empty"><i class="fas fa-inbox"></i><p>暂无相关订单</p></div>';
        return;
    }

    orderListEl.innerHTML = filtered.map(function(order) {
        var actionBtns = order.actions.map(function(action, i) {
            var cls = i === order.actions.length - 1 ? 'order-action-btn primary' : 'order-action-btn';
            return '<button class="' + cls + '">' + action + '</button>';
        }).join('');

        return '<div class="order-card">' +
            '<div class="order-card-header">' +
                '<div class="order-shop-name"><i class="fas fa-store"></i>' + order.shop + '</div>' +
                '<div class="order-status">' + order.statusText + '</div>' +
            '</div>' +
            '<div class="order-product">' +
                '<div class="order-product-image" style="background: ' + order.bg + ';">' +
                    '<i class="fas ' + order.icon + '"></i>' +
                '</div>' +
                '<div class="order-product-info">' +
                    '<div class="order-product-title">' + order.product + '</div>' +
                    '<div class="order-product-spec">' + order.spec + '</div>' +
                    '<div class="order-product-price">' +
                        '<span class="price">' + order.price + '</span>' +
                        '<span class="qty">x' + order.qty + '</span>' +
                    '</div>' +
                '</div>' +
            '</div>' +
            '<div class="order-card-footer">' +
                '<div class="order-total">合计: <span class="total-price">' + order.total + '</span></div>' +
                '<div class="order-actions">' + actionBtns + '</div>' +
            '</div>' +
        '</div>';
    }).join('');
}
