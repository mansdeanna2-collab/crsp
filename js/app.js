/**
 * 成人玩具商城 - JavaScript交互
 */

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
 * 消息Tab切换（带过滤功能）
 */
function initMessageTabs() {
    const tabs = document.querySelectorAll('.message-tab');
    const messageItems = document.querySelectorAll('.message-item');

    tabs.forEach(tab => {
        tab.addEventListener('click', function() {
            tabs.forEach(t => t.classList.remove('active'));
            this.classList.add('active');

            const category = this.getAttribute('data-category');
            messageItems.forEach(function(item) {
                if (category === 'all' || item.getAttribute('data-category') === category) {
                    item.classList.remove('hidden');
                } else {
                    item.classList.add('hidden');
                }
            });
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

    if (!chatPage || !chatBody) return;

    chatTitle.textContent = title;
    chatBody.innerHTML = '';

    if (msgType === 'logistics') {
        chatBody.innerHTML = renderLogisticsDetail();
    } else if (msgType === 'trade') {
        chatBody.innerHTML = renderTradeDetail();
    } else if (msgType === 'promo') {
        chatBody.innerHTML = renderPromoDetail();
    } else if (msgType === 'store') {
        chatBody.innerHTML = renderStoreChatDetail(avatarStyle, avatarIcon);
    } else if (msgType === 'service') {
        chatBody.innerHTML = renderServiceChatDetail(avatarStyle, avatarIcon);
    } else {
        chatBody.innerHTML = renderSystemDetail();
    }

    // 切换页面
    messagePage.classList.remove('active');
    chatPage.classList.add('active');
    window.scrollTo(0, 0);
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
            chatPage.classList.remove('active');
            messagePage.classList.add('active');
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
    // 主要城市坐标范围
    const cities = [
        { name: '北京', lat: 39.9042, lng: 116.4074, range: 1 },
        { name: '上海', lat: 31.2304, lng: 121.4737, range: 1 },
        { name: '广州', lat: 23.1291, lng: 113.2644, range: 0.8 },
        { name: '深圳', lat: 22.5431, lng: 114.0579, range: 0.5 },
        { name: '杭州', lat: 30.2741, lng: 120.1551, range: 0.8 },
        { name: '成都', lat: 30.5728, lng: 104.0668, range: 0.8 },
        { name: '南京', lat: 32.0603, lng: 118.7969, range: 0.8 },
        { name: '武汉', lat: 30.5928, lng: 114.3055, range: 0.8 },
        { name: '西安', lat: 34.3416, lng: 108.9398, range: 0.8 },
        { name: '重庆', lat: 29.4316, lng: 106.9123, range: 1 }
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
                bg: imageEl ? imageEl.style.background : 'linear-gradient(135deg, #ffecd2, #fcb69f)'
            };

            showProductDetail(productData);
        });
    });

    // 加入购物车按钮
    const addCartBtn = document.querySelector('.add-cart-btn');
    if (addCartBtn) {
        addCartBtn.addEventListener('click', function() {
            alert('已加入购物车');
            closeModal(document.getElementById('product-detail-modal'));
        });
    }

    // 立即购买按钮
    const buyNowBtn = document.querySelector('.buy-now-btn');
    if (buyNowBtn) {
        buyNowBtn.addEventListener('click', function() {
            alert('前往结算页面');
            closeModal(document.getElementById('product-detail-modal'));
        });
    }
}

/**
 * 显示商品详情
 */
function showProductDetail(productData) {
    const detailImage = document.getElementById('detail-image');
    const detailTitle = document.getElementById('detail-title');
    const detailPrice = document.getElementById('detail-price');
    const detailOriginalPrice = document.getElementById('detail-original-price');
    const detailSales = document.getElementById('detail-sales');

    if (detailImage) {
        // 解析背景样式
        let bgStyle = productData.bg;
        if (bgStyle && !bgStyle.includes('linear-gradient')) {
            bgStyle = `linear-gradient(135deg, ${bgStyle})`;
        }
        detailImage.style.background = bgStyle;
        detailImage.innerHTML = `<i class="fas ${productData.icon}"></i>`;
    }

    if (detailTitle) detailTitle.textContent = productData.title;
    if (detailPrice) detailPrice.textContent = productData.price;
    if (detailOriginalPrice) detailOriginalPrice.textContent = productData.originalPrice;
    if (detailSales) detailSales.textContent = productData.sales;

    openModal('product-detail-modal');
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
