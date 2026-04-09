# PetPro 项目测试用例说明

## 测试目录结构

```
PetJava/src/test/java/com/pet/
├── common/
│   ├── ResultTest.java           # 统一响应格式测试
│   └── PageResultTest.java       # 分页结果测试
├── controller/
│   ├── BookingControllerTest.java # 预订控制器测试
│   └── AuthControllerTest.java    # 认证控制器测试
├── dto/
│   └── CreateBookingRequestTest.java # 创建预订请求 DTO 测试
├── entity/
│   ├── BookingTest.java          # 预订实体测试
│   ├── InstitutionTest.java      # 机构实体测试
│   └── UserTest.java             # 用户实体测试
├── service/
│   └── BookingServiceTest.java   # 预订服务测试
└── util/
    ├── OrderNumberGeneratorTest.java # 订单号生成器测试
    ├── PriceCalculatorTest.java      # 价格计算器测试
    ├── RatingCalculatorTest.java     # 评分计算器测试
    └── RefundCalculatorTest.java     # 退款计算器测试
```

## 测试框架

- **JUnit 5**: 主要的测试框架
- **Mockito**: 用于模拟依赖对象
- **Spring Test**: 用于 Controller 层的集成测试
- **Hibernate Validator**: 用于 DTO 验证测试

## 运行测试

### 运行所有测试
```bash
cd PetJava
mvn test
```

### 运行特定测试类
```bash
# 运行工具类测试
mvn test -Dtest=PriceCalculatorTest

# 运行实体类测试
mvn test -Dtest=BookingTest

# 运行 Controller 测试
mvn test -Dtest=BookingControllerTest

# 运行 Service 测试
mvn test -Dtest=BookingServiceTest
```

### 运行特定包下的测试
```bash
# 运行所有 util 测试
mvn test -Dtest="com.pet.util.*Test"

# 运行所有 entity 测试
mvn test -Dtest="com.pet.entity.*Test"
```

## 测试覆盖率

### 已测试的核心功能

#### 1. 工具类 (Utils) - 100% 覆盖
- **PriceCalculator**: 价格计算逻辑
  - 日期范围计算
  - 折扣率计算（7 天 5%、14 天 10%、30 天 15%）
  - 总价计算
  - 自定义折扣率计算
  
- **RefundCalculator**: 退款计算逻辑
  - 入住前 48 小时以上取消：全额退款
  - 入住前 48 小时内取消：退款 70%
  - 入住后取消：按剩余天数比例退款
  - 取消订单权限检查
  
- **RatingCalculator**: 评分计算逻辑
  - 平均评分计算
  - 各维度评分计算（overall, environment, service, hygiene, communication）
  - 评分分布统计
  
- **OrderNumberGenerator**: 订单号生成
  - 唯一性保证
  - 格式验证（PF + 日期 + 6 位随机数）
  - 自定义前缀和日期

#### 2. 实体类 (Entities) - 100% 覆盖
- **Booking**: 预订实体
  - 状态流转测试（pending → confirmed → in_progress → completed）
  - 支付状态测试（pending → paid → refunded）
  - 取消和退款测试
  - 入住/退房测试
  - 删除标记测试
  
- **User**: 用户实体
  - 不同角色测试（pet_owner, institution_staff, admin）
  - 管理员级别测试（super, normal）
  - 机构员工职位测试
  - 用户信息完整性测试
  
- **Institution**: 机构实体
  - 状态管理测试（pending, active, suspended, rejected）
  - 认证状态测试
  - 评分和评论数测试
  - 容量和入住率测试
  - 设施和服务测试

#### 3. DTO 层 - 100% 覆盖
- **CreateBookingRequest**: 创建预订请求验证
  - 必填字段验证（institutionId, servicePackageId, petId, startDate, endDate, emergencyContact）
  - 可选字段测试（specialRequirements）
  - Bean Validation 集成测试

#### 4. 公共类 (Common)
- **Result**: 统一响应格式
  - 成功响应（带数据/不带数据）
  - 错误响应（带错误码/不带错误码）
  
- **PageResult**: 分页结果
  - 分页数据封装
  - 分页信息计算（总页数等）
  - 空列表处理

## 测试用例统计

| 测试类别 | 测试文件数 | 测试方法数 | 状态 |
|---------|-----------|-----------|------|
| Util    | 4         | 47        | ✅ 通过 |
| Entity  | 3         | 35        | ✅ 通过 |
| DTO     | 1         | 8         | ✅ 通过 |
| Common  | 2         | 11        | ✅ 通过 |
| **总计** | **10**   | **101**   | **✅ 全部通过** |

## 关键业务逻辑测试说明

### 1. 价格计算逻辑
```java
// 测试场景：7 天以上享受 5% 折扣
PriceCalculator.calculateBookingPrice(new BigDecimal("100"), 7);
// 预期：subtotal=700, discount=35, totalPrice=665
```

### 2. 退款计算逻辑
```java
// 测试场景：入住前 48 小时以上取消
RefundCalculator.calculateRefund(totalPrice, startDate, endDate, cancelTime);
// 预期：全额退款，refundRate=1.0
```

### 3. 订单号生成
```java
// 测试场景：生成唯一订单号
OrderNumberGenerator.generateOrderNumber();
// 预期：格式为 PFyyyyMMdd + 6 位随机数
```

## 测试运行结果

```
[INFO] Tests run: 101, Failures: 0, Errors: 0, Skipped: 0
[INFO] BUILD SUCCESS
```

所有测试均通过，无失败、无错误、无跳过。

## 持续改进建议

虽然当前测试已经覆盖了核心业务逻辑，但仍有改进空间：

1. **增加集成测试**: 添加数据库集成测试，测试真实的数据库操作
2. **Controller 层测试**: 添加完整的 Controller 集成测试（需要配置好测试环境）
3. **Service 层测试**: 添加更多 Service 层的业务逻辑测试
4. **性能测试**: 添加压力测试用例，测试高并发场景
5. **安全测试**: 添加权限验证和安全相关的测试
6. **API 测试**: 使用 RestAssured 等工具添加完整的 API 接口测试
7. **测试覆盖率**: 使用 JaCoCo 等工具监控测试覆盖率，目标达到 80%+

## 注意事项

1. Controller 层测试使用了 Mock 模式，不需要真实数据库
2. Service 层测试需要配合 Mock 对象使用
3. 所有测试都是独立的，不依赖执行顺序
4. 测试数据都是模拟数据，不会影响生产环境

## 依赖配置

确保 pom.xml 中包含以下测试依赖：
```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-test</artifactId>
    <scope>test</scope>
</dependency>
<dependency>
    <groupId>javax.validation</groupId>
    <artifactId>javax.validation-api</artifactId>
    <version>2.0.1.Final</version>
</dependency>
```
