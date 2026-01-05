# PetJava - 宠物寄养平台后端

Spring Boot 后端服务，为 PetVue 前端提供 API 支持。

## 技术栈

- Java 17
- Spring Boot 3.2.0
- MyBatis Plus 3.5.5
- MySQL 8.0

## 快速开始

### 1. 创建数据库

```sql
-- 执行 SQL 文件创建数据库和表
source src/main/resources/sql/schema.sql
source src/main/resources/sql/data.sql
source src/main/resources/sql/data2.sql
```

或者在 MySQL 客户端中依次执行这三个 SQL 文件。

### 2. 配置数据库连接

修改 `src/main/resources/application.yml` 中的数据库配置：

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/pet_foster?useUnicode=true&characterEncoding=utf-8&serverTimezone=Asia/Shanghai
    username: root
    password: root  # 修改为你的密码
```

### 3. 启动服务

```bash
# 使用 Maven
mvn spring-boot:run

# 或者打包后运行
mvn package
java -jar target/pet-foster-1.0.0.jar
```

服务将在 `http://localhost:8080/api` 启动。

## 测试账号

| 角色 | 手机号 | 密码 | 说明 |
|------|--------|------|------|
| 宠物主人 | 13800138001 | 123456 | 张三 |
| 宠物主人 | 13800138002 | 123456 | 李四 |
| 机构员工 | 13900139001 | 123456 | 王五（管理员） |
| 机构员工 | 13900139002 | 123456 | 赵六（护理员） |
| 系统管理员 | 13700137001 | 123456 | 超级管理员 |

## API 接口

### 认证接口
- `POST /api/auth/login` - 登录
- `POST /api/auth/register` - 注册
- `POST /api/auth/logout` - 登出
- `GET /api/auth/me` - 获取当前用户

### 机构接口
- `GET /api/institutions` - 搜索机构列表
- `GET /api/institutions/{id}` - 获取机构详情
- `GET /api/institutions/{id}/packages` - 获取服务套餐
- `GET /api/institutions/{id}/reviews` - 获取评价列表
- `POST /api/institutions/{id}/favorite` - 收藏机构
- `DELETE /api/institutions/{id}/favorite` - 取消收藏

### 预约接口
- `POST /api/bookings` - 创建预约
- `GET /api/bookings` - 获取订单列表
- `GET /api/bookings/{id}` - 获取订单详情
- `POST /api/bookings/{id}/cancel` - 取消订单
- `POST /api/bookings/{id}/pay` - 支付订单
- `POST /api/bookings/{id}/confirm` - 确认订单
- `POST /api/bookings/{id}/check-in` - 办理入住
- `POST /api/bookings/{id}/check-out` - 办理退房

### 评价接口
- `POST /api/reviews` - 创建评价
- `GET /api/reviews/{id}` - 获取评价详情
- `POST /api/reviews/{id}/reply` - 回复评价
- `DELETE /api/reviews/{id}` - 删除评价

### 用户接口
- `GET /api/user/favorites` - 获取收藏列表
- `GET /api/user/reviews` - 获取用户评价列表

## 前端配置

前端 `PetVue/src/main.ts` 中设置 `USE_MOCK = false` 即可连接真实后端。
