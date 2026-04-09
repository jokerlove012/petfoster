# 🐾 宠物寄养平台 (Pet Foster Platform)

一个功能完善的宠物寄养服务平台，连接宠物主人与专业寄养机构，提供从预约、支付、寄养到健康记录的一站式服务。

## 📋 项目概览

本项目是一个全栈宠物寄养管理系统，采用前后端分离架构，包含**用户端**、**机构端**和**管理员端**三个角色视图，实现了完整的宠物寄养业务流程。

### 🎯 核心业务流程

```
用户搜索机构 → 选择服务套餐 → 创建预约订单 → 钱包支付 
    → 机构确认接单 → 宠物入住 → 每日健康记录 → 宠物离店 → 用户评价
```

## ✨ 功能模块

### 👤 用户端功能

| 模块 | 功能描述 |
|------|----------|
| **机构搜索** | 列表/地图双模式搜索，支持按距离、评分、价格筛选 |
| **机构详情** | 查看机构介绍、服务套餐、用户评价、环境照片 |
| **在线预约** | 选择宠物、服务套餐、寄养日期，自动计算费用 |
| **订单管理** | 查看订单状态、取消订单、申请退款 |
| **钱包系统** | 余额充值、支付订单、查看交易记录 |
| **健康记录** | 实时查看宠物在寄养期间的健康状态、照片、视频 |
| **评价系统** | 对已完成订单进行评分和评价 |
| **收藏功能** | 收藏喜欢的寄养机构 |
| **消息通知** | 接收订单状态变更、健康异常等通知 |
| **AI 客服** | 智能问答，解答寄养相关问题 |

### 🏢 机构端功能

| 模块 | 功能描述 |
|------|----------|
| **工作台** | 今日待办、订单统计、收入概览 |
| **订单管理** | 接单/拒单、入住/离店登记、订单详情 |
| **服务管理** | 创建/编辑服务套餐、设置价格和容量 |
| **健康记录** | 每日填写宠物健康状态、上传照片视频 |
| **客户管理** | 查看客户信息、历史订单 |
| **评价管理** | 查看和回复用户评价 |
| **投诉处理** | 处理用户投诉 |
| **数据报表** | 收入统计、订单分析、导出报表 |
| **机构设置** | 编辑机构信息、营业时间、环境照片 |

### 🔧 管理员端功能

| 模块 | 功能描述 |
|------|----------|
| **数据看板** | 平台整体数据统计、趋势图表 |
| **用户管理** | 用户列表、禁用/启用账号 |
| **机构审核** | 审核新注册机构、资质审核 |
| **订单管理** | 查看所有订单、处理异常订单 |
| **投诉管理** | 处理用户投诉工单、仲裁纠纷 |
| **退款管理** | 审核退款申请 |
| **财务管理** | 平台收入统计、提现审核 |
| **系统设置** | 平台参数配置 |

## 🛠️ 技术栈

### 前端 (PetVue)

```
Vue 3.3          - 渐进式 JavaScript 框架
TypeScript 5     - 类型安全
Vite 4           - 下一代前端构建工具
Pinia            - Vue 状态管理
Vue Router 4     - 官方路由
Element Plus     - Vue 3 UI 组件库
ECharts 5        - 数据可视化图表
SCSS             - CSS 预处理器
Axios            - HTTP 客户端
```

### 后端 (PetJava)

```
Java 8           - 编程语言
Spring Boot 2.7  - 应用框架
MyBatis-Plus 3.5 - ORM 框架
MySQL 8.0        - 关系型数据库
Maven            - 项目构建
Lombok           - 简化代码
```

### 部署环境

```
Nginx            - 反向代理、静态资源服务
```

### 第三方服务

```
DeepSeek API     - AI 智能客服
高德地图 API      - 地图搜索、地理编码
```

## 📁 项目结构

```
PetPro/
├── PetVue/                      # 前端项目
│   ├── src/
│   │   ├── api/                # API 接口封装
│   │   │   ├── auth.ts        # 认证接口
│   │   │   ├── booking.ts     # 预约接口
│   │   │   ├── institution.ts # 机构接口
│   │   │   ├── wallet.ts      # 钱包接口
│   │   │   └── ...
│   │   ├── components/         # 公共组件
│   │   │   ├── common/        # 通用组件
│   │   │   ├── booking/       # 预约相关组件
│   │   │   ├── health/        # 健康记录组件
│   │   │   ├── wallet/        # 钱包组件
│   │   │   └── ai/            # AI 客服组件
│   │   ├── views/              # 页面视图
│   │   │   ├── auth/          # 登录注册
│   │   │   ├── booking/       # 预约订单
│   │   │   ├── institution/   # 机构端页面
│   │   │   ├── admin/         # 管理员页面
│   │   │   ├── wallet/        # 钱包页面
│   │   │   └── support/       # 客服工单
│   │   ├── stores/             # Pinia 状态管理
│   │   ├── router/             # 路由配置
│   │   ├── types/              # TypeScript 类型定义
│   │   ├── utils/              # 工具函数
│   │   └── styles/             # 全局样式
│   ├── tests/                  # 测试文件
│   ├── package.json
│   └── vite.config.ts
│
├── PetJava/                     # 后端项目
│   ├── src/main/java/com/pet/
│   │   ├── controller/         # 控制器层
│   │   │   ├── AuthController.java
│   │   │   ├── BookingController.java
│   │   │   ├── InstitutionController.java
│   │   │   ├── WalletController.java
│   │   │   ├── AdminController.java
│   │   │   └── ...
│   │   ├── service/            # 业务逻辑层
│   │   ├── mapper/             # 数据访问层
│   │   ├── entity/             # 实体类
│   │   ├── dto/                # 数据传输对象
│   │   ├── config/             # 配置类
│   │   └── util/               # 工具类
│   ├── src/main/resources/
│   │   ├── application.yml     # 应用配置
│   │   └── sql/               # SQL 脚本
│   └── pom.xml
│
├── nginx.conf                   # Nginx 配置文件
├── test_data.sql               # 测试数据
└── README.md
```

## 🗄️ 数据库设计

### 核心数据表

| 表名 | 说明 |
|------|------|
| `user` | 用户表（含普通用户、机构用户、管理员） |
| `institution` | 寄养机构表 |
| `service_package` | 服务套餐表 |
| `pet` | 宠物信息表 |
| `booking` | 预约订单表 |
| `health_record` | 健康记录表 |
| `review` | 评价表 |
| `notification` | 通知消息表 |
| `complaint` | 投诉工单表 |
| `favorite` | 收藏表 |

## 🚀 快速开始

### 环境要求

- Node.js 18+
- Java 8+
- MySQL 8.0+
- Maven 3.6+
- Nginx (生产部署)

### 1. 克隆项目

```bash
git clone https://github.com/jokerlove012/petfoster.git
cd petfoster
```

### 2. 数据库配置

```sql
-- 创建数据库
CREATE DATABASE pet_foster CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- 导入表结构
mysql -u root -p pet_foster < PetJava/src/main/resources/sql/schema.sql

-- 导入测试数据（可选）
mysql -u root -p pet_foster < test_data.sql
```

### 3. 后端配置与启动

修改 `PetJava/src/main/resources/application.yml`：

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/pet_foster?useUnicode=true&characterEncoding=utf-8&serverTimezone=Asia/Shanghai
    username: root
    password: your_password

# DeepSeek AI 配置（可选）
deepseek:
  api:
    key: your_api_key
```

启动后端：

```bash
cd PetJava
mvn spring-boot:run
```

后端服务运行在 http://localhost:8080/api

### 4. 前端配置与启动

```bash
cd PetVue
npm install
npm run dev
```

前端开发服务器运行在 http://localhost:5173

### 5. 生产部署

```bash
# 构建前端
cd PetVue
npx vite build

# 部署到 Nginx
cp -r dist/* /path/to/nginx/html/pet/

# 配置 Nginx（参考 nginx.conf）
```

## 👥 用户角色与测试账号

| 角色 | 账号 | 密码 | 说明 |
|------|------|------|------|
| 普通用户 | user | 123456 | 可预约寄养服务 |
| 机构用户 | institution | 123456 | 提供寄养服务 |
| 管理员 | admin | 123456 | 管理整个平台 |

## 📸 功能截图

### 首页
- 机构推荐、搜索入口、快捷功能

### 机构搜索
- 列表模式与地图模式切换
- 多条件筛选

### 预约流程
- 选择服务 → 选择日期 → 确认支付

### 健康记录
- 每日状态更新、照片视频

### 管理后台
- 数据统计、订单管理、用户管理

## 🔧 配置说明

### Nginx 配置要点

```nginx
server {
    listen 8088;
    
    # 允许大文件上传
    client_max_body_size 20m;
    
    # 前端静态文件
    location / {
        root /path/to/pet;
        try_files $uri $uri/ /index.html;
    }
    
    # API 代理
    location /api/ {
        proxy_pass http://127.0.0.1:8080/api/;
    }
}
```

### 文件上传配置

后端 `application.yml`：
```yaml
spring:
  servlet:
    multipart:
      max-file-size: 20MB
      max-request-size: 50MB
```

## 📝 开发说明

### 代码规范

- 前端使用 ESLint + Prettier
- 后端遵循阿里巴巴 Java 开发规范
- Git 提交信息遵循 Conventional Commits

### 目录命名

- 组件：PascalCase（如 `BookingForm.vue`）
- 工具函数：camelCase（如 `priceCalculator.ts`）
- API 接口：camelCase（如 `booking.ts`）

## 🐛 常见问题

**Q: 上传文件报 413 错误？**
A: 检查 Nginx 的 `client_max_body_size` 配置

**Q: 跨域问题？**
A: 后端已配置 CORS，确保通过 Nginx 代理访问

**Q: AI 客服无响应？**
A: 检查 DeepSeek API Key 是否配置正确

## 📄 License

MIT License

## 🤝 贡献

欢迎提交 Issue 和 Pull Request！

---

**开发者**: jokerlove012  
**项目地址**: https://github.com/jokerlove012/petfoster
