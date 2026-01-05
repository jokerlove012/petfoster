# 🐾 宠物寄养平台 (Pet Foster Platform)

一个功能完善的宠物寄养服务平台，连接宠物主人与专业寄养机构。

## 📋 项目概览

本项目是一个全栈宠物寄养管理系统，包含用户端、机构端和管理员端三个角色，提供从预约寄养到健康记录的完整服务流程。

### 🎯 核心功能

- **用户端**：机构搜索、在线预约、订单管理、钱包支付、健康记录查看、评价系统
- **机构端**：订单处理、服务管理、客户管理、健康记录填写、数据报表
- **管理员端**：用户管理、机构审核、投诉处理、财务管理、系统设置

### ✨ 特色功能

- 🤖 AI 智能客服（集成 DeepSeek）
- 🗺️ 地图搜索附近机构
- 💰 钱包充值与提现
- 📱 响应式设计，支持移动端
- 🔔 实时通知系统
- 📊 数据统计与报表

## 🛠️ 技术栈

### 前端 (PetVue)
- Vue 3 + TypeScript
- Vite 构建工具
- Pinia 状态管理
- Vue Router 路由
- Element Plus UI 组件库
- ECharts 图表
- SCSS 样式

### 后端 (PetJava)
- Java 8 + Spring Boot 2.7
- MyBatis-Plus ORM
- MySQL 8.0 数据库
- Maven 构建工具

### 部署
- Nginx 反向代理

## 📁 项目结构

```
PetPro/
├── PetVue/                 # 前端项目
│   ├── src/
│   │   ├── api/           # API 接口
│   │   ├── components/    # 公共组件
│   │   ├── views/         # 页面视图
│   │   ├── stores/        # Pinia 状态
│   │   ├── router/        # 路由配置
│   │   ├── types/         # TypeScript 类型
│   │   └── utils/         # 工具函数
│   └── package.json
├── PetJava/                # 后端项目
│   ├── src/main/java/com/pet/
│   │   ├── controller/    # 控制器
│   │   ├── service/       # 业务逻辑
│   │   ├── mapper/        # 数据访问
│   │   ├── entity/        # 实体类
│   │   └── config/        # 配置类
│   └── pom.xml
├── nginx.conf              # Nginx 配置
└── test_data.sql          # 测试数据
```

## 🚀 快速开始

### 环境要求

- Node.js 18+
- Java 8+
- MySQL 8.0
- Maven 3.6+
- Nginx

### 数据库配置

```sql
CREATE DATABASE pet_foster CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

导入数据库结构：
```bash
mysql -u root -p pet_foster < PetJava/src/main/resources/sql/schema.sql
```

### 后端启动

```bash
cd PetJava
mvn spring-boot:run
```

后端运行在 http://localhost:8080/api

### 前端启动

```bash
cd PetVue
npm install
npm run dev
```

前端开发服务器运行在 http://localhost:5173

### 生产部署

```bash
# 构建前端
cd PetVue
npx vite build

# 复制到 Nginx 目录
Copy-Item -Path "dist/*" -Destination "C:/nginx/html/pet" -Recurse -Force

# 配置 Nginx
# 将 nginx.conf 复制到 Nginx 配置目录
```

## 📝 配置说明

### 后端配置 (application.yml)

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/pet_foster
    username: root
    password: your_password

# DeepSeek AI 配置
deepseek:
  api:
    key: your_api_key
```

### 前端配置 (.env)

```
VITE_API_BASE_URL=/api
```

## 👥 用户角色

| 角色 | 说明 |
|------|------|
| user | 普通用户，可预约寄养服务 |
| institution | 机构用户，提供寄养服务 |
| admin | 管理员，管理整个平台 |

## 📄 License

MIT License

## 🤝 贡献

欢迎提交 Issue 和 Pull Request！
