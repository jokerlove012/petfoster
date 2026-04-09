# Spring Security + Redis 配置说明

## ✅ 已完成的功能

### 1. Spring Security 安全增强

#### 新增的安全特性：
- **密码加密**: 使用 BCryptPasswordEncoder 对用户密码进行加密存储
- **JWT 认证**: 基于 JWT Token 的无状态认证机制
- **角色权限**: 支持 ROLE_PET_OWNER、ROLE_INSTITUTION_STAFF、ROLE_ADMIN 三种角色
- **接口保护**: 除公开接口外，所有接口需要认证
- **统一异常处理**: 401 未授权响应格式统一

#### 公开接口（无需认证）：
- `/auth/**` - 认证相关接口
- `/institutions/**` - 机构查询接口
- `/uploads/**` - 文件上传访问
- `/ai/**` - AI 客服接口

### 2. Redis 缓存集成

#### 新增的缓存功能：
- **Redis 连接配置**: Lettuce 连接池配置
- **RedisTemplate**: 自定义 Redis 序列化配置（JSON）
- **CacheManager**: Spring Cache 集成
- **CacheService**: 封装的缓存服务，提供便捷的缓存操作

#### 缓存服务方法：
```java
// 基础操作
cacheService.set(key, value);
cacheService.set(key, value, timeout, TimeUnit);
cacheService.get(key);
cacheService.delete(key);

// Hash 操作
cacheService.setHash(key, hashKey, value);
cacheService.getHash(key, hashKey);

// 其他
cacheService.hasKey(key);
cacheService.expire(key, timeout, TimeUnit);
```

## 📋 文件变更清单

### 新增文件：

1. **`com.pet.config.SecurityConfig.java`** - Spring Security 配置
2. **`com.pet.config.RedisConfig.java`** - Redis 配置
3. **`com.pet.security.JwtAuthenticationEntryPoint.java`** - JWT 认证入口点
4. **`com.pet.security.JwtAuthenticationFilter.java`** - JWT 认证过滤器
5. **`com.pet.security.UserDetailsServiceImpl.java`** - 用户详情服务
6. **`com.pet.security.SecurityUser.java`** - 安全用户实体
7. **`com.pet.service.CacheService.java`** - 缓存服务
8. **`SECURITY_REDIS_SETUP.md`** - 本文档

### 修改文件：

1. **`pom.xml`** - 添加 Spring Security、Redis、Cache 依赖
2. **`application.yml`** - 添加 Redis 配置
3. **`com.pet.service.UserService.java`** - 集成密码加密
4. **`com.pet.config.WebConfig.java`** - 移除旧的 JwtInterceptor

## 🚀 使用前准备

### 1. 安装并启动 Redis

#### Windows:
```bash
# 下载 Redis for Windows
# 或使用 Docker
docker run -d -p 6379:6379 --name redis redis:latest
```

#### Linux/Mac:
```bash
# 使用包管理器安装
# Ubuntu/Debian
sudo apt-get install redis-server

# Mac (Homebrew)
brew install redis

# 启动 Redis
redis-server

# 或使用 Docker
docker run -d -p 6379:6379 --name redis redis:latest
```

### 2. 更新数据库密码（重要！）

由于现在使用了 BCrypt 加密，现有用户的明文密码将无法登录。你需要：

#### 方案一：更新现有用户密码
```sql
-- 先删除现有测试用户，重新注册（会自动加密）
DELETE FROM user WHERE phone IN ('user', 'institution', 'admin');
```

#### 方案二：使用代码生成加密密码
创建一个临时接口或使用测试类生成加密密码，然后手动更新数据库。

BCrypt 加密后的密码示例（密码为 123456）：
```
$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5EH
```

### 3. 配置 application.yml

当前配置（无需修改，如需要可调整）：
```yaml
spring:
  redis:
    host: localhost
    port: 6379
    password:
    database: 0
    timeout: 3000ms

jwt:
  secret: pet-foster-platform-secret-key-2024-jwt-token-security
  expiration: 1800000  # 30分钟（毫秒）
```

## 🔑 API 使用说明

### 认证流程

1. **登录获取 Token**
```http
POST /api/auth/login
Content-Type: application/json

{
  "phone": "user",
  "password": "123456",
  "role": "pet_owner"
}

// 响应
{
  "code": 200,
  "message": "success",
  "data": {
    "user": {...},
    "token": "eyJhbGciOiJIUzUxMiJ9..."
  }
}
```

2. **使用 Token 访问需要认证的接口**
```http
GET /api/wallet
Authorization: Bearer eyJhbGciOiJIUzUxMiJ9...
```

### 前端配置（前端已配置好，无需修改）

前端的 `PetVue/src/api/index.ts` 已配置好：
- 自动从 `sessionStorage` 读取 token
- 自动添加 `Authorization: Bearer {token}` 请求头
- 401 错误时自动跳转到登录页

## 💡 在 Service 中使用缓存

### 示例：在 InstitutionService 中使用缓存

```java
@Service
@RequiredArgsConstructor
public class InstitutionService {
    
    private final CacheService cacheService;
    private final InstitutionMapper institutionMapper;
    
    public Map<String, Object> getDetail(String id) {
        // 尝试从缓存获取
        String cacheKey = "institution:detail:" + id;
        Object cached = cacheService.get(cacheKey);
        if (cached != null) {
            return (Map<String, Object>) cached;
        }
        
        // 缓存未命中，查询数据库
        Map<String, Object> detail = queryFromDB(id);
        
        // 写入缓存，有效期 1 小时
        cacheService.set(cacheKey, detail, 1, TimeUnit.HOURS);
        
        return detail;
    }
    
    public void updateInstitution(String id, Map<String, Object> data) {
        // 更新数据库
        updateDB(id, data);
        
        // 清除相关缓存
        cacheService.delete("institution:detail:" + id);
        cacheService.delete("institution:list");
    }
}
```

### 使用 Spring Cache 注解（更简洁）

```java
@Service
@RequiredArgsConstructor
public class InstitutionService {
    
    @Cacheable(value = "institution", key = "#id")
    public Map<String, Object> getDetail(String id) {
        return queryFromDB(id);
    }
    
    @CachePut(value = "institution", key = "#id")
    public Map<String, Object> updateInstitution(String id, Map<String, Object> data) {
        updateDB(id, data);
        return queryFromDB(id);
    }
    
    @CacheEvict(value = "institution", key = "#id")
    public void deleteInstitution(String id) {
        deleteFromDB(id);
    }
}
```

## 🔒 安全最佳实践

1. **生产环境修改 JWT Secret**
```yaml
jwt:
  secret: your-production-secret-key-at-least-256-bits-long
```

2. **配置 HTTPS**
   - 生产环境务必使用 HTTPS
   - 防止 Token 被窃取

3. **Redis 密码保护**
```yaml
spring:
  redis:
    password: your-redis-password
```

4. **Token 过期时间**
   - 根据安全需求调整过期时间
   - 敏感操作可缩短过期时间

## 📝 测试建议

1. 先启动 Redis
2. 更新测试用户密码（重新注册）
3. 测试登录功能，验证 Token 获取
4. 测试需要认证的接口
5. 验证缓存功能是否生效

## ⚠️ 注意事项

1. **密码加密**: 现有明文密码无法登录，需要重新注册或更新
2. **Redis 依赖**: 确保 Redis 服务正在运行
3. **旧拦截器**: 已移除旧的 JwtInterceptor，使用 Spring Security 过滤器
4. **向后兼容**: 前端无需修改，已配置好 Token 处理

## 🎯 下一步建议

1. 添加 @PreAuthorize 注解进行方法级权限控制
2. 实现 Token 刷新机制
3. 添加 Redis 限流功能
4. 集成 Spring Session（可选）
5. 添加审计日志功能

---

**配置完成日期**: 2026-04-06  
**版本**: 1.0.0
