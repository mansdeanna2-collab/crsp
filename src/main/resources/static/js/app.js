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

    if (locationBtn) {
        locationBtn.addEventListener('click', function() {
            openModal('location-modal');
            // 尝试获取当前位置
            getCurrentLocation();
        });
    }

    // 城市选择点击
    locationItems.forEach(item => {
        item.addEventListener('click', function() {
            const city = this.getAttribute('data-city');
            updateLocation(city);
            closeModal(locationModal);
        });
    });
}

/**
 * 获取当前位置
 */
function getCurrentLocation() {
    const locationCurrent = document.querySelector('.location-current span');
    
    if (navigator.geolocation) {
        locationCurrent.textContent = '正在定位...';
        
        navigator.geolocation.getCurrentPosition(
            function(position) {
                // 成功获取位置
                const lat = position.coords.latitude;
                const lng = position.coords.longitude;
                // 这里可以调用逆地理编码API获取城市名
                // 演示目的，使用模拟城市
                reverseGeocode(lat, lng);
            },
            function(error) {
                // 获取位置失败
                let errorMsg = '定位失败';
                switch(error.code) {
                    case error.PERMISSION_DENIED:
                        errorMsg = '用户拒绝定位请求';
                        break;
                    case error.POSITION_UNAVAILABLE:
                        errorMsg = '位置信息不可用';
                        break;
                    case error.TIMEOUT:
                        errorMsg = '定位请求超时';
                        break;
                }
                locationCurrent.textContent = errorMsg;
            },
            {
                enableHighAccuracy: true,
                timeout: 10000,
                maximumAge: 0
            }
        );
    } else {
        locationCurrent.textContent = '浏览器不支持定位';
    }
}

/**
 * 逆地理编码（模拟）
 */
function reverseGeocode(lat, lng) {
    const locationCurrent = document.querySelector('.location-current span');
    // 实际项目中应该调用地图API进行逆地理编码
    // 这里模拟返回一个城市
    setTimeout(function() {
        const cities = ['北京', '上海', '广州', '深圳', '杭州', '成都'];
        const randomCity = cities[Math.floor(Math.random() * cities.length)];
        locationCurrent.textContent = `当前位置: ${randomCity} (${lat.toFixed(4)}, ${lng.toFixed(4)})`;
        updateLocation(randomCity);
    }, 500);
}

/**
 * 更新定位显示
 */
function updateLocation(city) {
    const locationText = document.querySelector('.location span');
    if (locationText) {
        locationText.textContent = city;
    }
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
