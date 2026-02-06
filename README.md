# 成人玩具商城 (Adult Toy Mall)

一个基于Java Spring Boot的移动端成人玩具商城应用，采用淘宝风格设计，包含独立的搜索页面和商品详情页面。

## 📱 页面预览

### 首页 (Home)
- 顶部搜索栏（跳转到独立搜索页面）
- 轮播广告位
- 活动入口（限时秒杀、天天特价等）
- 成人玩具商品推荐列表（瀑布流布局）
- **注：已移除8个分类导航（家居、美食、美妆、母婴、运动、家电、汽车、更多）**

### 搜索页面 (Search) - 独立页面
- 独立的搜索页面
- 搜索结果网格展示
- 支持关键词搜索
- 点击商品进入详情页

### 商品详情页面 (Product Detail) - 独立页面（参考淘宝风格）
- 商品大图展示
- 价格、折扣、销量信息
- 商品标签（正品保证、隐私发货等）
- 店铺信息
- 规格选择
- 商品描述
- 相关推荐
- 底部操作栏（加入购物车、立即购买）

### 消息 (Messages)
- 消息分类Tab切换
- 消息列表展示
- 未读消息角标

### 购物车 (Shopping Cart)
- 按店铺分组的商品列表
- 商品规格展示
- 数量增减控制
- 单选/店铺全选/全选功能
- 实时价格计算
- 商品推荐
- 底部结算栏

### 我的 (Profile)
- 用户信息头部（头像、昵称、会员标识）
- 订单入口（待付款、待发货、待收货等）
- 我的资产（红包卡券、积分、余额、优惠券）
- 功能菜单列表

## 🚀 快速开始

### 环境要求
- Java 17 或更高版本
- Maven 3.6+
- Docker (可选，用于容器化部署)

### 方式一：直接启动
```bash
# 使用Maven启动
mvn spring-boot:run

# 或先打包再运行
mvn clean package
java -jar target/mall-1.0.0.jar
```

然后访问 `http://localhost:8080`

### 方式二：Docker部署 (推荐)

使用自动部署脚本，一键检测依赖并打包到Docker环境运行：

```bash
# 运行自动部署脚本 (默认端口1000)
python3 docker_deploy.py

# 指定自定义端口
python3 docker_deploy.py -p 3000

# 查看帮助
python3 docker_deploy.py --help
```

或使用 Docker Compose：

```bash
# 构建并启动
docker-compose up -d

# 查看日志
docker-compose logs -f

# 停止服务
docker-compose down
```

然后访问 `http://localhost:1000`

#### Docker管理命令
```bash
# 查看容器日志
docker logs crsp-mall-container

# 停止容器
docker stop crsp-mall-container

# 启动容器
docker start crsp-mall-container

# 删除容器
docker rm crsp-mall-container
```

### API接口
- `GET /api/products` - 获取所有商品
- `GET /api/products/{id}` - 获取单个商品详情
- `GET /api/products/search?keyword=xxx` - 搜索商品

## 📁 项目结构

```
crsp/
├── pom.xml                              # Maven配置文件
├── src/main/java/com/crsp/mall/
│   ├── MallApplication.java             # Spring Boot主入口
│   ├── controller/
│   │   ├── PageController.java          # 页面控制器
│   │   └── ApiController.java           # REST API控制器
│   ├── model/
│   │   └── Product.java                 # 商品模型
│   └── service/
│       └── ProductService.java          # 商品服务
├── src/main/resources/
│   ├── application.properties           # 应用配置
│   ├── templates/                        # Thymeleaf模板
│   │   ├── index.html                   # 首页
│   │   ├── search.html                  # 搜索页面（独立）
│   │   ├── product-detail.html          # 商品详情页（独立）
│   │   ├── message.html                 # 消息页面
│   │   ├── cart.html                    # 购物车页面
│   │   └── profile.html                 # 个人中心
│   └── static/
│       ├── css/style.css                # 样式文件
│       └── js/app.js                    # 交互逻辑
├── css/                                 # 原始CSS（保留兼容）
├── js/                                  # 原始JS（保留兼容）
└── README.md                            # 说明文档
```

## 🎨 技术特点

- **后端框架**：Java Spring Boot 3.2
- **模板引擎**：Thymeleaf
- **前端技术**：HTML5 + CSS3 + JavaScript
- **响应式设计**：适配移动端和桌面端
- **Font Awesome图标**：使用CDN加载图标库
- **渐变配色**：采用现代渐变色彩设计
- **RESTful API**：提供商品数据接口

## 📐 设计规范

- 主题色：`#ff5722` (橙红色)
- 字体：系统默认字体栈
- 最大宽度：480px（移动端优化）
- 底部导航高度：55px

## 🔧 自定义

### 修改主题色
在 `src/main/resources/static/css/style.css` 中搜索 `#ff5722` 或 `#ff6b35` 替换为你想要的颜色。

### 添加商品
在 `ProductService.java` 的 `initProducts()` 方法中添加新的商品数据。

### 修改商品标签
商品标签显示为"成人玩具"，可在 `index.html` 模板的 `.section-title` 中修改。

## 📝 License

MIT License