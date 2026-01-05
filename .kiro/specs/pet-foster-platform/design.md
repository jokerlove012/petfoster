# Design Document: Pet Foster Platform

## Overview

宠物寄养平台是一个基于 Vue 3 的单页应用（SPA），采用前后端分离架构。前端使用 Vue 3 + TypeScript + Vite 技术栈，通过模拟数据（Mock Data）来模拟后端 API 响应，实现完整的用户交互流程。

### 技术栈

- **框架**: Vue 3 (Composition API)
- **语言**: TypeScript
- **构建工具**: Vite
- **状态管理**: Pinia
- **路由**: Vue Router 4
- **UI 组件库**: Element Plus
- **HTTP 客户端**: Axios + Mock Service Worker (MSW)
- **地图服务**: 高德地图 API
- **样式**: SCSS + CSS Variables
- **测试**: Vitest + fast-check (Property-Based Testing)

### 核心设计原则

1. **模块化设计**: 按功能域划分模块，每个模块独立管理状态和视图
2. **类型安全**: 全面使用 TypeScript，确保编译时类型检查
3. **响应式优先**: 移动端优先的响应式设计
4. **Mock 数据驱动**: 使用 MSW 拦截 API 请求，返回模拟数据

## Architecture

```
┌─────────────────────────────────────────────────────────────────┐
│                        Vue 3 Application                         │
├─────────────────────────────────────────────────────────────────┤
│  ┌─────────────┐  ┌─────────────┐  ┌─────────────┐              │
│  │   Views     │  │  Components │  │   Layouts   │              │
│  │  (Pages)    │  │ (Reusable)  │  │  (Shells)   │              │
│  └──────┬──────┘  └──────┬──────┘  └──────┬──────┘              │
│         │                │                │                      │
│         └────────────────┼────────────────┘                      │
│                          │                                       │
│  ┌───────────────────────┴───────────────────────┐              │
│  │              Pinia Store (State)               │              │
│  │  ┌─────────┐ ┌─────────┐ ┌─────────┐         │              │
│  │  │  Auth   │ │ Booking │ │Institution│ ...    │              │
│  │  └─────────┘ └─────────┘ └─────────┘         │              │
│  └───────────────────────┬───────────────────────┘              │
│                          │                                       │
│  ┌───────────────────────┴───────────────────────┐              │
│  │              API Service Layer                 │              │
│  │         (Axios + Request Interceptors)         │              │
│  └───────────────────────┬───────────────────────┘              │
│                          │                                       │
├──────────────────────────┼──────────────────────────────────────┤
│  ┌───────────────────────┴───────────────────────┐              │
│  │         Mock Service Worker (MSW)              │              │
│  │    (Intercepts requests, returns mock data)    │              │
│  └───────────────────────────────────────────────┘              │
└─────────────────────────────────────────────────────────────────┘
```


### 目录结构

```
PetVue/
├── src/
│   ├── api/                    # API 服务层
│   │   ├── index.ts           # Axios 实例配置
│   │   ├── auth.ts            # 认证相关 API
│   │   ├── institution.ts     # 机构相关 API
│   │   ├── booking.ts         # 预约相关 API
│   │   ├── review.ts          # 评价相关 API
│   │   ├── notification.ts    # 通知相关 API
│   │   └── admin.ts           # 管理员相关 API
│   ├── components/            # 可复用组件
│   │   ├── common/            # 通用组件
│   │   ├── institution/       # 机构相关组件
│   │   ├── booking/           # 预约相关组件
│   │   └── layout/            # 布局组件
│   ├── composables/           # 组合式函数
│   ├── layouts/               # 页面布局
│   ├── mocks/                 # Mock 数据和处理器
│   │   ├── handlers/          # MSW 请求处理器
│   │   ├── data/              # 模拟数据
│   │   └── browser.ts         # MSW 浏览器配置
│   ├── router/                # 路由配置
│   ├── stores/                # Pinia 状态管理
│   ├── types/                 # TypeScript 类型定义
│   ├── utils/                 # 工具函数
│   ├── views/                 # 页面视图
│   ├── App.vue
│   └── main.ts
├── tests/                     # 测试文件
│   ├── unit/                  # 单元测试
│   └── property/              # 属性测试
├── public/
├── index.html
├── vite.config.ts
├── tsconfig.json
└── package.json
```

## Components and Interfaces

### 核心组件

#### 1. 布局组件

```typescript
// MainLayout.vue - 主布局组件
interface MainLayoutProps {
  showSidebar?: boolean;
  showHeader?: boolean;
}

// MobileLayout.vue - 移动端布局
interface MobileLayoutProps {
  showBottomNav?: boolean;
  activeTab?: string;
}

// AppHeader.vue - 顶部导航栏组件
interface AppHeaderProps {
  showSearch?: boolean;
  showNotification?: boolean;
  showAIButton?: boolean;
}

interface AppHeaderEmits {
  (e: 'search-click'): void;
  (e: 'notification-click'): void;
  (e: 'ai-click'): void;
  (e: 'logo-click'): void;
}

// UserDropdown.vue - 用户下拉菜单组件
interface UserDropdownProps {
  user: User;
}

// NotificationDropdown.vue - 通知下拉面板组件
interface NotificationDropdownProps {
  notifications: Notification[];
  unreadCount: number;
}

// SearchOverlay.vue - 搜索覆盖层组件
interface SearchOverlayProps {
  isOpen: boolean;
  placeholder?: string;
}

interface SearchOverlayEmits {
  (e: 'close'): void;
  (e: 'search', query: string): void;
}
```

#### 2. 机构搜索组件

```typescript
// InstitutionSearch.vue
interface SearchFilters {
  location?: { lat: number; lng: number };
  radius?: number;
  minRating?: number;
  priceRange?: { min: number; max: number };
  petTypes?: PetType[];
  sortBy?: 'distance' | 'rating' | 'price';
}

// InstitutionCard.vue
interface InstitutionCardProps {
  institution: FosterInstitution;
  showDistance?: boolean;
  compact?: boolean;
}
```

#### 3. 预约组件

```typescript
// BookingForm.vue
interface BookingFormProps {
  institutionId: string;
  servicePackage?: ServicePackage;
}

// BookingCalendar.vue
interface BookingCalendarProps {
  availableDates: Date[];
  selectedRange?: { start: Date; end: Date };
}
```

#### 4. 状态追踪组件

```typescript
// PetStatusTimeline.vue
interface TimelineEntry {
  id: string;
  type: 'health' | 'photo' | 'video' | 'note';
  timestamp: Date;
  content: HealthRecord | MediaContent | string;
}

// HealthRecordCard.vue
interface HealthRecordCardProps {
  record: HealthRecord;
  showAlert?: boolean;
}
```

### API 服务接口

```typescript
// api/types.ts
interface ApiResponse<T> {
  code: number;
  message: string;
  data: T;
}

interface PaginatedResponse<T> {
  items: T[];
  total: number;
  page: number;
  pageSize: number;
}

// api/auth.ts
interface LoginCredentials {
  phone: string;
  password: string;
  role: UserRole;  // 用户选择的登录角色
}

interface RegisterData {
  phone: string;
  password: string;
  name: string;
  role: UserRole;
}

interface AuthResult {
  user: User;
  token: string;
  redirectPath: string;  // 根据角色返回不同的跳转路径
}

interface AuthService {
  login(credentials: LoginCredentials): Promise<ApiResponse<AuthResult>>;
  register(data: RegisterData): Promise<ApiResponse<User>>;
  logout(): Promise<void>;
  refreshToken(): Promise<ApiResponse<AuthResult>>;
}

// api/institution.ts
interface InstitutionService {
  search(filters: SearchFilters): Promise<ApiResponse<PaginatedResponse<FosterInstitution>>>;
  getById(id: string): Promise<ApiResponse<FosterInstitution>>;
  getServicePackages(id: string): Promise<ApiResponse<ServicePackage[]>>;
  getReviews(id: string, page: number): Promise<ApiResponse<PaginatedResponse<ServiceReview>>>;
  checkAvailability(id: string, dateRange: DateRange): Promise<ApiResponse<AvailabilityResult>>;
}

// api/booking.ts
interface BookingService {
  create(data: CreateBookingData): Promise<ApiResponse<BookingOrder>>;
  getById(id: string): Promise<ApiResponse<BookingOrder>>;
  cancel(id: string, reason: string): Promise<ApiResponse<RefundResult>>;
  getHealthRecords(bookingId: string): Promise<ApiResponse<HealthRecord[]>>;
  submitReview(bookingId: string, review: ReviewData): Promise<ApiResponse<ServiceReview>>;
}
```


### Pinia Store 接口

```typescript
// stores/auth.ts
interface AuthState {
  user: User | null;
  token: string | null;
  isAuthenticated: boolean;
  permissions: Permission[];
}

// stores/institution.ts
interface InstitutionState {
  searchResults: FosterInstitution[];
  currentInstitution: FosterInstitution | null;
  filters: SearchFilters;
  favorites: string[];
  browsingHistory: BrowsingHistoryItem[];
}

// stores/booking.ts
interface BookingState {
  currentBooking: BookingOrder | null;
  bookingHistory: BookingOrder[];
  activeBookings: BookingOrder[];
}

// stores/notification.ts
interface NotificationState {
  notifications: Notification[];
  unreadCount: number;
  preferences: NotificationPreferences;
}
```

## Data Models

### 核心数据模型

```typescript
// types/user.ts
interface User {
  id: string;
  email: string;
  phone: string;
  name: string;
  avatar?: string;
  role: UserRole;
  createdAt: Date;
  updatedAt: Date;
}

type UserRole = 'pet_owner' | 'foster_institution' | 'platform_admin';

// types/institution.ts
interface FosterInstitution {
  id: string;
  name: string;
  description: string;
  address: Address;
  location: GeoLocation;
  phone: string;
  email: string;
  businessLicense: string;
  facilityPhotos: string[];
  acceptedPetTypes: PetType[];
  averageRating: number;
  reviewCount: number;
  status: InstitutionStatus;
  servicePackages: ServicePackage[];
  emergencyContact: string;
  createdAt: Date;
}

interface Address {
  province: string;
  city: string;
  district: string;
  street: string;
  detail: string;
}

interface GeoLocation {
  lat: number;
  lng: number;
}

type PetType = 'dog' | 'cat' | 'bird' | 'rabbit' | 'hamster' | 'other';
type InstitutionStatus = 'pending' | 'active' | 'suspended' | 'rejected';

// types/service.ts
interface ServicePackage {
  id: string;
  institutionId: string;
  name: string;
  description: string;
  price: number;
  priceUnit: 'day' | 'week' | 'month';
  duration: DurationOption[];
  includedServices: string[];
  petTypeRestrictions?: PetType[];
  isActive: boolean;
}

interface DurationOption {
  minDays: number;
  maxDays: number;
  discount?: number;
}

// types/booking.ts
interface BookingOrder {
  id: string;
  orderNumber: string;
  petOwnerId: string;
  institutionId: string;
  servicePackageId: string;
  petInfo: PetInfo;
  startDate: Date;
  endDate: Date;
  totalDays: number;
  totalPrice: number;
  status: BookingStatus;
  paymentStatus: PaymentStatus;
  specialNotes?: string;
  createdAt: Date;
  updatedAt: Date;
}

interface PetInfo {
  name: string;
  type: PetType;
  breed: string;
  age: number;
  weight: number;
  gender: 'male' | 'female';
  vaccinated: boolean;
  specialNeeds?: string;
  dietaryRestrictions?: string;
}

type BookingStatus = 'pending' | 'confirmed' | 'in_progress' | 'completed' | 'cancelled';
type PaymentStatus = 'pending' | 'paid' | 'refunded' | 'partial_refund';

// types/health.ts
interface HealthRecord {
  id: string;
  bookingId: string;
  date: Date;
  feedingStatus: FeedingStatus;
  activityLevel: ActivityLevel;
  healthObservations: string;
  mood: PetMood;
  weight?: number;
  temperature?: number;
  medications?: string[];
  photos?: string[];
  videos?: string[];
  isAbnormal: boolean;
  abnormalDetails?: string;
  createdBy: string;
  createdAt: Date;
}

type FeedingStatus = 'normal' | 'reduced' | 'increased' | 'refused';
type ActivityLevel = 'high' | 'normal' | 'low' | 'inactive';
type PetMood = 'happy' | 'calm' | 'anxious' | 'stressed';

// types/review.ts
interface ServiceReview {
  id: string;
  bookingId: string;
  petOwnerId: string;
  institutionId: string;
  ratings: ReviewRatings;
  overallRating: number;
  content: string;
  photos?: string[];
  createdAt: Date;
  reply?: ReviewReply;
}

interface ReviewRatings {
  serviceQuality: number;    // 1-5
  facilityCleanliness: number;
  communication: number;
  petCare: number;
}

interface ReviewReply {
  content: string;
  createdAt: Date;
}

// types/notification.ts
interface Notification {
  id: string;
  userId: string;
  type: NotificationType;
  title: string;
  content: string;
  relatedId?: string;
  isRead: boolean;
  createdAt: Date;
}

type NotificationType = 
  | 'booking_created'
  | 'booking_confirmed'
  | 'booking_cancelled'
  | 'health_update'
  | 'health_alert'
  | 'review_reminder'
  | 'message'
  | 'system';

// types/payment.ts
interface PaymentTransaction {
  id: string;
  bookingId: string;
  amount: number;
  method: PaymentMethod;
  status: TransactionStatus;
  transactionId?: string;
  createdAt: Date;
}

type PaymentMethod = 'credit_card' | 'debit_card' | 'alipay' | 'wechat_pay';
type TransactionStatus = 'pending' | 'success' | 'failed' | 'refunded';

// types/support.ts
interface SupportTicket {
  id: string;
  ticketNumber: string;
  userId: string;
  category: TicketCategory;
  subject: string;
  description: string;
  attachments?: string[];
  status: TicketStatus;
  priority: TicketPriority;
  assignedTo?: string;
  createdAt: Date;
  updatedAt: Date;
}

type TicketCategory = 'booking' | 'payment' | 'service' | 'technical' | 'other';
type TicketStatus = 'open' | 'in_progress' | 'resolved' | 'closed';
type TicketPriority = 'low' | 'medium' | 'high' | 'urgent';

interface Complaint {
  id: string;
  bookingId: string;
  petOwnerId: string;
  institutionId: string;
  category: ComplaintCategory;
  description: string;
  evidence: string[];
  status: ComplaintStatus;
  institutionResponse?: string;
  resolution?: ComplaintResolution;
  createdAt: Date;
}

type ComplaintCategory = 'service_quality' | 'pet_safety' | 'communication' | 'facility' | 'other';
type ComplaintStatus = 'pending' | 'awaiting_response' | 'under_review' | 'resolved';

interface ComplaintResolution {
  decision: string;
  remedies?: string;
  resolvedBy: string;
  resolvedAt: Date;
}
```


## Correctness Properties

*A property is a characteristic or behavior that should hold true across all valid executions of a system-essentially, a formal statement about what the system should do. Properties serve as the bridge between human-readable specifications and machine-verifiable correctness guarantees.*

Based on the prework analysis, the following correctness properties have been identified:

### Property 1: Filter results match criteria
*For any* set of search filters (rating, price range, pet type) and any list of institutions, all returned institutions should satisfy ALL applied filter criteria simultaneously.
**Validates: Requirements 2.2, 2.3, 2.4, 2.5**

### Property 2: Distance sorting correctness
*For any* user location and list of institutions, when sorted by distance, each institution in the result should have distance less than or equal to the next institution.
**Validates: Requirements 2.1**

### Property 3: Booking cost calculation
*For any* service package with price P and date range with D days, the total cost should equal P × D (minus any applicable discounts).
**Validates: Requirements 3.2**

### Property 4: Order number uniqueness
*For any* two booking orders created, their order numbers should be different.
**Validates: Requirements 3.5**

### Property 5: Health record chronological ordering
*For any* list of health records or media content, when displayed in timeline, items should be sorted by timestamp in descending order (newest first).
**Validates: Requirements 4.3**

### Property 6: Review text length validation
*For any* review text, the system should accept text with length between 10 and 500 characters, and reject text outside this range.
**Validates: Requirements 5.3**

### Property 7: Average rating calculation
*For any* institution with reviews, the average rating should equal the sum of all review ratings divided by the number of reviews.
**Validates: Requirements 5.4**

### Property 8: Price update isolation
*For any* existing booking order, when the service package price is updated, the booking order's total price should remain unchanged.
**Validates: Requirements 6.2**

### Property 9: Availability blocking
*For any* date marked as unavailable in the institution calendar, booking requests for that date should be rejected.
**Validates: Requirements 6.3**

### Property 10: Statistics calculation correctness
*For any* time period and set of orders, the calculated statistics (booking count, revenue, occupancy rate) should match the actual data.
**Validates: Requirements 6.5, 9.1, 9.2, 9.4**

### Property 11: State serialization round-trip
*For any* valid application state, serializing to JSON and then deserializing should produce an equivalent state object.
**Validates: Requirements 11.1, 11.2**

### Property 12: Corrupted data handling
*For any* malformed or corrupted JSON data, deserialization should fail gracefully without crashing and return a default state.
**Validates: Requirements 11.4**

### Property 13: Refund calculation (>48 hours)
*For any* booking cancelled more than 48 hours before service start, the refund amount should equal 100% of the total price.
**Validates: Requirements 16.3**

### Property 14: Refund calculation (≤48 hours)
*For any* booking cancelled within 48 hours of service start, the refund amount should equal 70% of the total price (30% cancellation fee).
**Validates: Requirements 16.4**

### Property 15: Notification sorting
*For any* list of notifications, when displayed in notification center, items should be sorted by creation time in descending order.
**Validates: Requirements 15.3**

### Property 16: Favorite toggle consistency
*For any* institution, adding to favorites then removing should result in the institution not being in the favorites list, and vice versa.
**Validates: Requirements 19.1, 19.3**

### Property 17: Browsing history recency
*For any* browsing history, only items from the past 30 days should be displayed, sorted by view date descending.
**Validates: Requirements 19.4**

### Property 18: Form validation error display
*For any* form with invalid inputs, each invalid field should display a specific error message.
**Validates: Requirements 13.4**

### Property 19: Table sorting correctness
*For any* data table with sortable columns, sorting by a column should order rows by that column's values.
**Validates: Requirements 14.3**

### Property 20: Search result keyword highlighting
*For any* search query and matching results, the search keywords should be highlighted in the displayed results.
**Validates: Requirements 14.5**


## Error Handling

### 错误处理策略

#### 1. API 错误处理

```typescript
// utils/errorHandler.ts
interface ApiError {
  code: string;
  message: string;
  details?: Record<string, string>;
}

const errorMessages: Record<string, string> = {
  'AUTH_INVALID_CREDENTIALS': '用户名或密码错误',
  'AUTH_TOKEN_EXPIRED': '登录已过期，请重新登录',
  'BOOKING_UNAVAILABLE': '所选时间段已被预约',
  'PAYMENT_FAILED': '支付失败，请重试',
  'NETWORK_ERROR': '网络连接失败，请检查网络',
  'VALIDATION_ERROR': '输入数据格式不正确',
};

function handleApiError(error: ApiError): void {
  const message = errorMessages[error.code] || error.message || '操作失败，请重试';
  // 显示错误提示
  ElMessage.error(message);
  
  // 特殊错误处理
  if (error.code === 'AUTH_TOKEN_EXPIRED') {
    router.push('/login');
  }
}
```

#### 2. 表单验证错误

```typescript
// 表单验证规则
const validationRules = {
  email: [
    { required: true, message: '请输入邮箱地址' },
    { type: 'email', message: '请输入有效的邮箱地址' }
  ],
  password: [
    { required: true, message: '请输入密码' },
    { min: 8, message: '密码长度不能少于8位' }
  ],
  reviewContent: [
    { required: true, message: '请输入评价内容' },
    { min: 10, max: 500, message: '评价内容需要10-500个字符' }
  ]
};
```

#### 3. 状态恢复错误

```typescript
// 状态反序列化错误处理
function deserializeState<T>(json: string, defaultState: T): T {
  try {
    const parsed = JSON.parse(json);
    // 验证数据结构
    if (!isValidState(parsed)) {
      console.warn('Invalid state structure, using default');
      return defaultState;
    }
    return parsed as T;
  } catch (error) {
    console.error('Failed to deserialize state:', error);
    return defaultState;
  }
}
```

### 用户友好的错误展示

1. **表单错误**: 在字段下方显示红色错误提示
2. **操作错误**: 使用 Toast 消息提示
3. **页面错误**: 显示友好的错误页面，提供重试或返回选项
4. **网络错误**: 显示离线提示，自动重试机制

## Testing Strategy

### 双重测试方法

本项目采用单元测试和属性测试相结合的测试策略：

#### 1. 单元测试 (Vitest)

单元测试用于验证特定示例和边界情况：

```typescript
// tests/unit/utils/priceCalculator.test.ts
import { describe, it, expect } from 'vitest';
import { calculateBookingPrice } from '@/utils/priceCalculator';

describe('calculateBookingPrice', () => {
  it('should calculate correct price for 7 days', () => {
    const result = calculateBookingPrice(100, 7);
    expect(result).toBe(700);
  });

  it('should apply discount for long stays', () => {
    const result = calculateBookingPrice(100, 30, 0.1);
    expect(result).toBe(2700); // 30 * 100 * 0.9
  });
});
```

#### 2. 属性测试 (fast-check)

属性测试用于验证在所有有效输入下都应成立的通用属性：

```typescript
// tests/property/filters.property.test.ts
import { describe, it } from 'vitest';
import * as fc from 'fast-check';
import { filterInstitutions } from '@/utils/filters';

describe('Institution Filtering Properties', () => {
  /**
   * **Feature: pet-foster-platform, Property 1: Filter results match criteria**
   * **Validates: Requirements 2.2, 2.3, 2.4, 2.5**
   */
  it('all filtered results should match all applied criteria', () => {
    fc.assert(
      fc.property(
        fc.array(institutionArbitrary),
        fc.record({
          minRating: fc.option(fc.integer({ min: 1, max: 5 })),
          priceRange: fc.option(fc.record({
            min: fc.integer({ min: 0, max: 500 }),
            max: fc.integer({ min: 500, max: 2000 })
          })),
          petTypes: fc.option(fc.array(petTypeArbitrary))
        }),
        (institutions, filters) => {
          const results = filterInstitutions(institutions, filters);
          return results.every(inst => matchesAllFilters(inst, filters));
        }
      ),
      { numRuns: 100 }
    );
  });
});
```

### 测试文件组织

```
tests/
├── unit/
│   ├── components/           # 组件单元测试
│   ├── stores/               # Store 单元测试
│   ├── utils/                # 工具函数单元测试
│   └── api/                  # API 服务单元测试
├── property/
│   ├── filters.property.test.ts      # 筛选属性测试
│   ├── pricing.property.test.ts      # 价格计算属性测试
│   ├── serialization.property.test.ts # 序列化属性测试
│   ├── sorting.property.test.ts      # 排序属性测试
│   └── validation.property.test.ts   # 验证属性测试
└── setup.ts                  # 测试配置
```

### 测试配置

```typescript
// vitest.config.ts
import { defineConfig } from 'vitest/config';
import vue from '@vitejs/plugin-vue';

export default defineConfig({
  plugins: [vue()],
  test: {
    globals: true,
    environment: 'jsdom',
    coverage: {
      provider: 'v8',
      reporter: ['text', 'json', 'html'],
    },
  },
});
```

### 属性测试要求

- 每个属性测试必须运行至少 100 次迭代
- 每个属性测试必须使用注释标注对应的正确性属性编号
- 注释格式: `**Feature: pet-foster-platform, Property {number}: {property_text}**`
- 每个正确性属性由单独的属性测试实现


## Frontend Design System

### 设计理念

**Tone**: 温暖有机 (Warm Organic) + 现代精致 (Modern Refined)

宠物寄养平台的设计采用"温暖有机"的美学方向，传达信任、关爱和专业感。设计灵感来源于自然元素和宠物的温馨陪伴感，同时保持现代化的精致感。

**核心设计原则**:
- **温暖亲切**: 使用暖色调和圆润的形状，营造安全舒适的氛围
- **有机自然**: 融入自然元素，如柔和的渐变、有机形状的装饰
- **清晰可信**: 信息层次分明，操作流程直观，建立用户信任
- **精致细腻**: 注重微交互和细节打磨，提升品质感

### 色彩系统

```scss
// 主色调 - 温暖橙色系
$primary-50: #FFF7ED;
$primary-100: #FFEDD5;
$primary-200: #FED7AA;
$primary-300: #FDBA74;
$primary-400: #FB923C;
$primary-500: #FF6B35;  // 主色
$primary-600: #EA580C;
$primary-700: #C2410C;

// 辅助色 - 柔和青绿色
$accent-50: #F0FDFA;
$accent-100: #CCFBF1;
$accent-200: #99F6E4;
$accent-300: #5EEAD4;
$accent-400: #4ECDC4;  // 辅助色
$accent-500: #14B8A6;
$accent-600: #0D9488;

// 中性色 - 温暖灰
$neutral-50: #FAFAF9;
$neutral-100: #F5F5F4;
$neutral-200: #E7E5E4;
$neutral-300: #D6D3D1;
$neutral-400: #A8A29E;
$neutral-500: #78716C;
$neutral-600: #57534E;
$neutral-700: #44403C;
$neutral-800: #292524;
$neutral-900: #1C1917;

// 语义色
$success: #22C55E;
$warning: #F59E0B;
$error: #EF4444;
$info: #3B82F6;

// CSS Variables
:root {
  --color-primary: #FF6B35;
  --color-primary-light: #FFEDD5;
  --color-primary-dark: #C2410C;
  --color-accent: #4ECDC4;
  --color-accent-light: #CCFBF1;
  --color-background: #FAFAF9;
  --color-surface: #FFFFFF;
  --color-text-primary: #1C1917;
  --color-text-secondary: #57534E;
  --color-text-muted: #A8A29E;
  --color-border: #E7E5E4;
  
  // 阴影
  --shadow-sm: 0 1px 2px 0 rgb(0 0 0 / 0.05);
  --shadow-md: 0 4px 6px -1px rgb(0 0 0 / 0.1), 0 2px 4px -2px rgb(0 0 0 / 0.1);
  --shadow-lg: 0 10px 15px -3px rgb(0 0 0 / 0.1), 0 4px 6px -4px rgb(0 0 0 / 0.1);
  --shadow-xl: 0 20px 25px -5px rgb(0 0 0 / 0.1), 0 8px 10px -6px rgb(0 0 0 / 0.1);
  
  // 圆角
  --radius-sm: 6px;
  --radius-md: 10px;
  --radius-lg: 16px;
  --radius-xl: 24px;
  --radius-full: 9999px;
}
```

### 字体系统

```scss
// 字体家族
$font-display: 'Nunito', 'PingFang SC', sans-serif;  // 标题 - 圆润友好
$font-body: 'DM Sans', 'PingFang SC', sans-serif;    // 正文 - 清晰易读
$font-mono: 'JetBrains Mono', monospace;             // 代码/数字

// 字体大小
$text-xs: 0.75rem;    // 12px
$text-sm: 0.875rem;   // 14px
$text-base: 1rem;     // 16px
$text-lg: 1.125rem;   // 18px
$text-xl: 1.25rem;    // 20px
$text-2xl: 1.5rem;    // 24px
$text-3xl: 1.875rem;  // 30px
$text-4xl: 2.25rem;   // 36px
$text-5xl: 3rem;      // 48px

// 行高
$leading-tight: 1.25;
$leading-normal: 1.5;
$leading-relaxed: 1.625;

// 字重
$font-normal: 400;
$font-medium: 500;
$font-semibold: 600;
$font-bold: 700;

// CSS Variables
:root {
  --font-display: 'Nunito', 'PingFang SC', sans-serif;
  --font-body: 'DM Sans', 'PingFang SC', sans-serif;
  --font-size-base: 16px;
  --line-height-base: 1.6;
}
```

### 组件样式规范

#### 按钮 (Buttons)

```scss
// 主要按钮
.btn-primary {
  background: linear-gradient(135deg, var(--color-primary) 0%, #FF8F5C 100%);
  color: white;
  padding: 12px 24px;
  border-radius: var(--radius-lg);
  font-weight: 600;
  font-size: 15px;
  border: none;
  cursor: pointer;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  box-shadow: 0 4px 14px 0 rgba(255, 107, 53, 0.39);
  
  &:hover {
    transform: translateY(-2px);
    box-shadow: 0 6px 20px 0 rgba(255, 107, 53, 0.5);
  }
  
  &:active {
    transform: translateY(0);
  }
}

// 次要按钮
.btn-secondary {
  background: var(--color-surface);
  color: var(--color-primary);
  padding: 12px 24px;
  border-radius: var(--radius-lg);
  font-weight: 600;
  border: 2px solid var(--color-primary);
  transition: all 0.3s ease;
  
  &:hover {
    background: var(--color-primary-light);
  }
}

// 幽灵按钮
.btn-ghost {
  background: transparent;
  color: var(--color-text-secondary);
  padding: 12px 24px;
  border-radius: var(--radius-lg);
  font-weight: 500;
  border: none;
  
  &:hover {
    background: var(--color-neutral-100);
    color: var(--color-primary);
  }
}
```

#### 卡片 (Cards)

```scss
.card {
  background: var(--color-surface);
  border-radius: var(--radius-xl);
  padding: 24px;
  box-shadow: var(--shadow-md);
  border: 1px solid var(--color-border);
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  
  &:hover {
    transform: translateY(-4px);
    box-shadow: var(--shadow-xl);
  }
}

// 机构卡片
.institution-card {
  @extend .card;
  display: grid;
  grid-template-columns: 120px 1fr;
  gap: 20px;
  
  .institution-image {
    width: 120px;
    height: 120px;
    border-radius: var(--radius-lg);
    object-fit: cover;
  }
  
  .institution-info {
    display: flex;
    flex-direction: column;
    gap: 8px;
  }
  
  .institution-name {
    font-family: var(--font-display);
    font-size: $text-xl;
    font-weight: 700;
    color: var(--color-text-primary);
  }
  
  .institution-rating {
    display: flex;
    align-items: center;
    gap: 4px;
    color: var(--color-warning);
  }
  
  .institution-tags {
    display: flex;
    gap: 8px;
    flex-wrap: wrap;
  }
}
```

#### 表单元素 (Form Elements)

```scss
.input-field {
  width: 100%;
  padding: 14px 18px;
  border: 2px solid var(--color-border);
  border-radius: var(--radius-md);
  font-size: $text-base;
  font-family: var(--font-body);
  background: var(--color-surface);
  transition: all 0.2s ease;
  
  &:focus {
    outline: none;
    border-color: var(--color-primary);
    box-shadow: 0 0 0 4px rgba(255, 107, 53, 0.1);
  }
  
  &::placeholder {
    color: var(--color-text-muted);
  }
  
  &.error {
    border-color: var(--color-error);
    
    &:focus {
      box-shadow: 0 0 0 4px rgba(239, 68, 68, 0.1);
    }
  }
}

.input-label {
  display: block;
  font-size: $text-sm;
  font-weight: 600;
  color: var(--color-text-secondary);
  margin-bottom: 8px;
}

.input-error {
  font-size: $text-sm;
  color: var(--color-error);
  margin-top: 6px;
  display: flex;
  align-items: center;
  gap: 4px;
}
```

### 动画与微交互

```scss
// 页面进入动画
@keyframes fadeInUp {
  from {
    opacity: 0;
    transform: translateY(20px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

@keyframes fadeIn {
  from { opacity: 0; }
  to { opacity: 1; }
}

@keyframes scaleIn {
  from {
    opacity: 0;
    transform: scale(0.95);
  }
  to {
    opacity: 1;
    transform: scale(1);
  }
}

// 骨架屏动画
@keyframes shimmer {
  0% {
    background-position: -200% 0;
  }
  100% {
    background-position: 200% 0;
  }
}

.skeleton {
  background: linear-gradient(
    90deg,
    var(--color-neutral-100) 25%,
    var(--color-neutral-200) 50%,
    var(--color-neutral-100) 75%
  );
  background-size: 200% 100%;
  animation: shimmer 1.5s infinite;
  border-radius: var(--radius-md);
}

// 页面过渡
.page-enter-active,
.page-leave-active {
  transition: all 0.3s ease;
}

.page-enter-from {
  opacity: 0;
  transform: translateX(20px);
}

.page-leave-to {
  opacity: 0;
  transform: translateX(-20px);
}

// 列表项交错动画
.stagger-item {
  animation: fadeInUp 0.5s ease forwards;
  opacity: 0;
  
  @for $i from 1 through 10 {
    &:nth-child(#{$i}) {
      animation-delay: #{$i * 0.05}s;
    }
  }
}

// 悬浮效果
.hover-lift {
  transition: transform 0.3s cubic-bezier(0.4, 0, 0.2, 1),
              box-shadow 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  
  &:hover {
    transform: translateY(-4px);
    box-shadow: var(--shadow-xl);
  }
}

// 按钮点击波纹
.ripple {
  position: relative;
  overflow: hidden;
  
  &::after {
    content: '';
    position: absolute;
    width: 100%;
    height: 100%;
    top: 0;
    left: 0;
    pointer-events: none;
    background-image: radial-gradient(circle, #fff 10%, transparent 10.01%);
    background-repeat: no-repeat;
    background-position: 50%;
    transform: scale(10, 10);
    opacity: 0;
    transition: transform 0.5s, opacity 1s;
  }
  
  &:active::after {
    transform: scale(0, 0);
    opacity: 0.3;
    transition: 0s;
  }
}
```

### 响应式布局

```scss
// 断点
$breakpoints: (
  'sm': 640px,
  'md': 768px,
  'lg': 1024px,
  'xl': 1280px,
  '2xl': 1536px
);

// 响应式混合宏
@mixin respond-to($breakpoint) {
  @if map-has-key($breakpoints, $breakpoint) {
    @media (min-width: map-get($breakpoints, $breakpoint)) {
      @content;
    }
  }
}

// 移动端优先的网格系统
.container {
  width: 100%;
  margin: 0 auto;
  padding: 0 16px;
  
  @include respond-to('sm') {
    max-width: 640px;
  }
  
  @include respond-to('md') {
    max-width: 768px;
    padding: 0 24px;
  }
  
  @include respond-to('lg') {
    max-width: 1024px;
  }
  
  @include respond-to('xl') {
    max-width: 1280px;
    padding: 0 32px;
  }
}

// 移动端底部导航
.mobile-nav {
  display: flex;
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  background: var(--color-surface);
  border-top: 1px solid var(--color-border);
  padding: 8px 0;
  padding-bottom: env(safe-area-inset-bottom);
  z-index: 100;
  
  @include respond-to('md') {
    display: none;
  }
  
  .nav-item {
    flex: 1;
    display: flex;
    flex-direction: column;
    align-items: center;
    gap: 4px;
    padding: 8px;
    color: var(--color-text-muted);
    font-size: $text-xs;
    
    &.active {
      color: var(--color-primary);
    }
    
    .nav-icon {
      font-size: 24px;
    }
  }
}

// 桌面端侧边栏
.sidebar {
  display: none;
  
  @include respond-to('lg') {
    display: flex;
    flex-direction: column;
    width: 260px;
    height: 100vh;
    position: fixed;
    left: 0;
    top: 0;
    background: var(--color-surface);
    border-right: 1px solid var(--color-border);
    padding: 24px 16px;
  }
}
```

### 装饰元素

```scss
// 有机形状背景装饰
.organic-blob {
  position: absolute;
  border-radius: 60% 40% 30% 70% / 60% 30% 70% 40%;
  background: linear-gradient(135deg, var(--color-primary-light) 0%, var(--color-accent-light) 100%);
  opacity: 0.5;
  filter: blur(40px);
  z-index: -1;
  animation: blob-morph 8s ease-in-out infinite;
}

@keyframes blob-morph {
  0%, 100% {
    border-radius: 60% 40% 30% 70% / 60% 30% 70% 40%;
  }
  50% {
    border-radius: 30% 60% 70% 40% / 50% 60% 30% 60%;
  }
}

// 宠物爪印装饰
.paw-pattern {
  background-image: url("data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' width='60' height='60' viewBox='0 0 60 60'%3E%3Cpath fill='%23FF6B35' fill-opacity='0.05' d='M30 20c-2.2 0-4 1.8-4 4s1.8 4 4 4 4-1.8 4-4-1.8-4-4-4zm-10 8c-1.7 0-3 1.3-3 3s1.3 3 3 3 3-1.3 3-3-1.3-3-3-3zm20 0c-1.7 0-3 1.3-3 3s1.3 3 3 3 3-1.3 3-3-1.3-3-3-3zm-15 8c-1.7 0-3 1.3-3 3s1.3 3 3 3 3-1.3 3-3-1.3-3-3-3zm10 0c-1.7 0-3 1.3-3 3s1.3 3 3 3 3-1.3 3-3-1.3-3-3-3z'/%3E%3C/svg%3E");
}

// 渐变边框
.gradient-border {
  position: relative;
  background: var(--color-surface);
  border-radius: var(--radius-xl);
  
  &::before {
    content: '';
    position: absolute;
    inset: -2px;
    border-radius: calc(var(--radius-xl) + 2px);
    background: linear-gradient(135deg, var(--color-primary), var(--color-accent));
    z-index: -1;
  }
}
```

### 图标系统

使用 Lucide Icons 作为图标库，配合自定义宠物主题图标：

```typescript
// 常用图标映射
const iconMap = {
  // 导航
  home: 'Home',
  search: 'Search',
  heart: 'Heart',
  user: 'User',
  settings: 'Settings',
  
  // 宠物相关
  dog: 'Dog',
  cat: 'Cat',
  paw: 'PawPrint',
  
  // 操作
  plus: 'Plus',
  edit: 'Pencil',
  delete: 'Trash2',
  check: 'Check',
  close: 'X',
  
  // 状态
  star: 'Star',
  starFilled: 'StarFilled',
  alert: 'AlertCircle',
  info: 'Info',
  
  // 通信
  message: 'MessageCircle',
  phone: 'Phone',
  mail: 'Mail',
  bell: 'Bell',
  
  // 位置
  mapPin: 'MapPin',
  navigation: 'Navigation',
  
  // 时间
  calendar: 'Calendar',
  clock: 'Clock',
};
```



## AI Assistant Module

### AI 助手架构

```
┌─────────────────────────────────────────────────────────────────┐
│                      AI Assistant Module                         │
├─────────────────────────────────────────────────────────────────┤
│  ┌─────────────────┐  ┌─────────────────┐  ┌─────────────────┐  │
│  │  Chat Interface │  │  Message Store  │  │  AI Service     │  │
│  │  (Vue Component)│  │  (Pinia)        │  │  (Mock API)     │  │
│  └────────┬────────┘  └────────┬────────┘  └────────┬────────┘  │
│           │                    │                    │            │
│           └────────────────────┼────────────────────┘            │
│                                │                                 │
│  ┌─────────────────────────────┴─────────────────────────────┐  │
│  │              Response Generator (Mock)                     │  │
│  │  - Institution Recommendations                             │  │
│  │  - Pet Care Tips                                          │  │
│  │  - Platform Feature Guidance                              │  │
│  └───────────────────────────────────────────────────────────┘  │
└─────────────────────────────────────────────────────────────────┘
```

### AI 助手组件接口

```typescript
// components/ai/AIChatWindow.vue
interface AIChatWindowProps {
  isOpen: boolean;
  initialMessage?: string;
}

interface AIChatWindowEmits {
  (e: 'close'): void;
  (e: 'minimize'): void;
}

// components/ai/ChatMessage.vue
interface ChatMessageProps {
  message: AIMessage;
  isTyping?: boolean;
}

// components/ai/SuggestedQuestions.vue
interface SuggestedQuestionsProps {
  questions: string[];
  onSelect: (question: string) => void;
}

// components/ai/InstitutionRecommendation.vue
interface InstitutionRecommendationProps {
  institutions: FosterInstitution[];
  reason: string;
}
```

### AI 助手数据模型

```typescript
// types/ai.ts
interface AIMessage {
  id: string;
  role: 'user' | 'assistant';
  content: string;
  timestamp: Date;
  metadata?: AIMessageMetadata;
}

interface AIMessageMetadata {
  type: AIResponseType;
  recommendations?: FosterInstitution[];
  careTips?: PetCareTip[];
  relatedLinks?: RelatedLink[];
}

type AIResponseType = 
  | 'general'
  | 'recommendation'
  | 'care_tip'
  | 'platform_guide'
  | 'booking_help';

interface PetCareTip {
  title: string;
  content: string;
  petType?: PetType;
  category: CareTipCategory;
}

type CareTipCategory = 
  | 'feeding'
  | 'health'
  | 'grooming'
  | 'exercise'
  | 'behavior'
  | 'travel';

interface RelatedLink {
  title: string;
  url: string;
  type: 'institution' | 'article' | 'faq';
}

interface ConversationSession {
  id: string;
  userId: string;
  title: string;
  messages: AIMessage[];
  createdAt: Date;
  updatedAt: Date;
}

// stores/ai.ts
interface AIState {
  currentSession: ConversationSession | null;
  sessions: ConversationSession[];
  isTyping: boolean;
  suggestedQuestions: string[];
}
```

### AI 响应模拟逻辑

```typescript
// mocks/ai/responseGenerator.ts
interface AIResponseGenerator {
  generateResponse(
    message: string,
    context: AIContext
  ): Promise<AIMessage>;
  
  getInstitutionRecommendations(
    preferences: UserPreferences
  ): FosterInstitution[];
  
  getPetCareTips(
    petType: PetType,
    concern: string
  ): PetCareTip[];
  
  getPlatformGuidance(
    topic: string
  ): string;
}

interface AIContext {
  userId: string;
  userRole: UserRole;
  currentPage?: string;
  recentSearches?: SearchFilters[];
  favorites?: string[];
}

// 预设回复模板
const responseTemplates = {
  greeting: [
    '您好！我是宠物寄养平台的 AI 助手，很高兴为您服务。',
    '有什么我可以帮助您的吗？',
  ],
  recommendation: [
    '根据您的需求，我为您推荐以下寄养机构：',
    '这些机构在{criteria}方面表现出色。',
  ],
  careTip: [
    '关于{petType}的{topic}，这里有一些建议：',
    '希望这些信息对您有帮助！',
  ],
  platformGuide: [
    '关于{feature}功能，您可以这样操作：',
    '如果还有其他问题，随时问我！',
  ],
};

// 关键词匹配规则
const intentPatterns = {
  recommendation: ['推荐', '哪家好', '选择', '建议', '附近'],
  careTip: ['怎么', '如何', '注意', '护理', '喂养', '健康'],
  booking: ['预约', '预订', '下单', '支付', '取消'],
  platform: ['怎么用', '功能', '操作', '在哪里'],
};
```

### AI 助手样式设计

```scss
// AI 聊天窗口
.ai-chat-window {
  position: fixed;
  bottom: 100px;
  right: 24px;
  width: 380px;
  height: 560px;
  background: var(--color-surface);
  border-radius: var(--radius-xl);
  box-shadow: var(--shadow-xl);
  display: flex;
  flex-direction: column;
  overflow: hidden;
  z-index: 1000;
  
  @include respond-to('sm') {
    bottom: 0;
    right: 0;
    width: 100%;
    height: 100%;
    border-radius: 0;
  }
  
  .chat-header {
    display: flex;
    align-items: center;
    justify-content: space-between;
    padding: 16px 20px;
    background: linear-gradient(135deg, var(--color-primary) 0%, #FF8F5C 100%);
    color: white;
    
    .ai-avatar {
      width: 40px;
      height: 40px;
      border-radius: 50%;
      background: rgba(255, 255, 255, 0.2);
      display: flex;
      align-items: center;
      justify-content: center;
    }
    
    .ai-name {
      font-weight: 600;
      font-size: 16px;
    }
    
    .ai-status {
      font-size: 12px;
      opacity: 0.9;
    }
  }
  
  .chat-messages {
    flex: 1;
    overflow-y: auto;
    padding: 16px;
    display: flex;
    flex-direction: column;
    gap: 12px;
  }
  
  .chat-input {
    padding: 16px;
    border-top: 1px solid var(--color-border);
    display: flex;
    gap: 12px;
    
    input {
      flex: 1;
      padding: 12px 16px;
      border: 1px solid var(--color-border);
      border-radius: var(--radius-full);
      font-size: 14px;
      
      &:focus {
        outline: none;
        border-color: var(--color-primary);
      }
    }
    
    button {
      width: 44px;
      height: 44px;
      border-radius: 50%;
      background: var(--color-primary);
      color: white;
      border: none;
      cursor: pointer;
      
      &:hover {
        background: var(--color-primary-dark);
      }
    }
  }
}

// 消息气泡
.chat-message {
  max-width: 85%;
  
  &.user {
    align-self: flex-end;
    
    .message-bubble {
      background: var(--color-primary);
      color: white;
      border-radius: 18px 18px 4px 18px;
    }
  }
  
  &.assistant {
    align-self: flex-start;
    
    .message-bubble {
      background: var(--color-neutral-100);
      color: var(--color-text-primary);
      border-radius: 18px 18px 18px 4px;
    }
  }
  
  .message-bubble {
    padding: 12px 16px;
    font-size: 14px;
    line-height: 1.5;
  }
  
  .message-time {
    font-size: 11px;
    color: var(--color-text-muted);
    margin-top: 4px;
  }
}

// 打字指示器
.typing-indicator {
  display: flex;
  gap: 4px;
  padding: 12px 16px;
  
  span {
    width: 8px;
    height: 8px;
    border-radius: 50%;
    background: var(--color-text-muted);
    animation: typing-bounce 1.4s infinite ease-in-out;
    
    &:nth-child(1) { animation-delay: 0s; }
    &:nth-child(2) { animation-delay: 0.2s; }
    &:nth-child(3) { animation-delay: 0.4s; }
  }
}

@keyframes typing-bounce {
  0%, 80%, 100% {
    transform: scale(0.6);
    opacity: 0.5;
  }
  40% {
    transform: scale(1);
    opacity: 1;
  }
}

// 推荐卡片
.ai-recommendation-card {
  background: var(--color-surface);
  border: 1px solid var(--color-border);
  border-radius: var(--radius-lg);
  padding: 12px;
  margin-top: 8px;
  
  .recommendation-header {
    font-weight: 600;
    font-size: 13px;
    color: var(--color-primary);
    margin-bottom: 8px;
  }
  
  .institution-mini-card {
    display: flex;
    gap: 10px;
    padding: 8px;
    background: var(--color-neutral-50);
    border-radius: var(--radius-md);
    cursor: pointer;
    
    &:hover {
      background: var(--color-neutral-100);
    }
    
    img {
      width: 48px;
      height: 48px;
      border-radius: var(--radius-sm);
      object-fit: cover;
    }
    
    .info {
      flex: 1;
      
      .name {
        font-weight: 500;
        font-size: 13px;
      }
      
      .rating {
        font-size: 12px;
        color: var(--color-warning);
      }
    }
  }
}

// 悬浮按钮
.ai-fab {
  position: fixed;
  bottom: 24px;
  right: 24px;
  width: 60px;
  height: 60px;
  border-radius: 50%;
  background: linear-gradient(135deg, var(--color-primary) 0%, #FF8F5C 100%);
  color: white;
  border: none;
  cursor: pointer;
  box-shadow: 0 4px 20px rgba(255, 107, 53, 0.4);
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.3s ease;
  z-index: 999;
  
  &:hover {
    transform: scale(1.1);
    box-shadow: 0 6px 24px rgba(255, 107, 53, 0.5);
  }
  
  .ai-icon {
    font-size: 28px;
  }
  
  .notification-badge {
    position: absolute;
    top: -4px;
    right: -4px;
    width: 20px;
    height: 20px;
    border-radius: 50%;
    background: var(--color-error);
    color: white;
    font-size: 11px;
    display: flex;
    align-items: center;
    justify-content: center;
  }
}
```




## Wallet System Module

### 钱包系统架构

```
┌─────────────────────────────────────────────────────────────────┐
│                      Wallet System Module                        │
├─────────────────────────────────────────────────────────────────┤
│  ┌─────────────────┐  ┌─────────────────┐  ┌─────────────────┐  │
│  │  Wallet Views   │  │  Wallet Store   │  │  Wallet API     │  │
│  │  (Vue Pages)    │  │  (Pinia)        │  │  (Service)      │  │
│  └────────┬────────┘  └────────┬────────┘  └────────┬────────┘  │
│           │                    │                    │            │
│           └────────────────────┼────────────────────┘            │
│                                │                                 │
│  ┌─────────────────────────────┴─────────────────────────────┐  │
│  │                    Core Services                           │  │
│  │  ┌─────────────┐ ┌─────────────┐ ┌─────────────┐          │  │
│  │  │  Recharge   │ │ Withdrawal  │ │ Transaction │          │  │
│  │  │  Service    │ │  Service    │ │  Service    │          │  │
│  │  └─────────────┘ └─────────────┘ └─────────────┘          │  │
│  └───────────────────────────────────────────────────────────┘  │
└─────────────────────────────────────────────────────────────────┘
```

### 钱包数据模型

```typescript
// types/wallet.ts
interface Wallet {
  id: string;
  userId: string;
  userType: 'pet_owner' | 'institution';
  balance: number;           // 可用余额（分）
  frozenBalance: number;     // 冻结余额（分）
  totalIncome: number;       // 累计收入（分）
  totalWithdraw: number;     // 累计提现（分）
  status: WalletStatus;
  createdAt: Date;
  updatedAt: Date;
}

type WalletStatus = 'active' | 'frozen' | 'disabled';

interface WalletTransaction {
  id: string;
  walletId: string;
  type: TransactionType;
  amount: number;            // 金额（分）
  fee: number;               // 手续费（分）
  balanceBefore: number;     // 交易前余额
  balanceAfter: number;      // 交易后余额
  status: TransactionStatus;
  description: string;
  relatedOrderId?: string;   // 关联订单ID
  relatedWithdrawalId?: string; // 关联提现ID
  createdAt: Date;
}

type TransactionType = 'recharge' | 'withdrawal' | 'income' | 'expense' | 'refund';
type TransactionStatus = 'pending' | 'success' | 'failed' | 'cancelled';

interface Withdrawal {
  id: string;
  walletId: string;
  amount: number;            // 提现金额（分）
  fee: number;               // 手续费（分）
  actualAmount: number;      // 实际到账金额（分）
  accountId: string;         // 提现账户ID
  status: WithdrawalStatus;
  rejectReason?: string;
  processedAt?: Date;
  createdAt: Date;
}

type WithdrawalStatus = 'pending' | 'processing' | 'success' | 'rejected' | 'cancelled';

interface WithdrawalAccount {
  id: string;
  userId: string;
  type: 'bank_card' | 'alipay';
  accountName: string;       // 账户名称
  accountNumber: string;     // 账号（脱敏显示）
  bankName?: string;         // 银行名称
  isDefault: boolean;
  createdAt: Date;
}

interface RechargeOrder {
  id: string;
  walletId: string;
  amount: number;            // 充值金额（分）
  paymentMethod: 'alipay' | 'wechat';
  status: RechargeStatus;
  paymentOrderId?: string;   // 第三方支付订单号
  paidAt?: Date;
  createdAt: Date;
  expiredAt: Date;
}

type RechargeStatus = 'pending' | 'paid' | 'expired' | 'cancelled';
```

### 钱包组件接口

```typescript
// components/wallet/WalletOverview.vue
interface WalletOverviewProps {
  wallet: Wallet;
  recentTransactions: WalletTransaction[];
}

// components/wallet/RechargeModal.vue
interface RechargeModalProps {
  isOpen: boolean;
  walletId: string;
}

interface RechargeModalEmits {
  (e: 'close'): void;
  (e: 'success', order: RechargeOrder): void;
}

// components/wallet/WithdrawModal.vue
interface WithdrawModalProps {
  isOpen: boolean;
  wallet: Wallet;
  accounts: WithdrawalAccount[];
}

interface WithdrawModalEmits {
  (e: 'close'): void;
  (e: 'success', withdrawal: Withdrawal): void;
}

// components/wallet/TransactionList.vue
interface TransactionListProps {
  transactions: WalletTransaction[];
  loading: boolean;
}

interface TransactionFilters {
  type?: TransactionType;
  startDate?: Date;
  endDate?: Date;
}

// components/wallet/WithdrawalAccountCard.vue
interface WithdrawalAccountCardProps {
  account: WithdrawalAccount;
  isSelected: boolean;
}

// components/wallet/AddAccountModal.vue
interface AddAccountModalProps {
  isOpen: boolean;
  accountType: 'bank_card' | 'alipay';
}
```

### 钱包 API 服务接口

```typescript
// api/wallet.ts
interface WalletService {
  // 钱包基础操作
  getWallet(): Promise<ApiResponse<Wallet>>;
  getBalance(): Promise<ApiResponse<{ balance: number; frozenBalance: number }>>;
  
  // 充值
  createRechargeOrder(data: CreateRechargeData): Promise<ApiResponse<RechargeOrder>>;
  getRechargeStatus(orderId: string): Promise<ApiResponse<RechargeOrder>>;
  
  // 提现
  createWithdrawal(data: CreateWithdrawalData): Promise<ApiResponse<Withdrawal>>;
  getWithdrawals(params: PaginationParams): Promise<ApiResponse<PaginatedResponse<Withdrawal>>>;
  cancelWithdrawal(id: string): Promise<ApiResponse<void>>;
  
  // 交易记录
  getTransactions(params: TransactionQueryParams): Promise<ApiResponse<PaginatedResponse<WalletTransaction>>>;
  
  // 提现账户
  getWithdrawalAccounts(): Promise<ApiResponse<WithdrawalAccount[]>>;
  addWithdrawalAccount(data: AddAccountData): Promise<ApiResponse<WithdrawalAccount>>;
  deleteWithdrawalAccount(id: string): Promise<ApiResponse<void>>;
  setDefaultAccount(id: string): Promise<ApiResponse<void>>;
  
  // 收入统计（机构）
  getIncomeStatistics(period: 'day' | 'week' | 'month'): Promise<ApiResponse<IncomeStatistics>>;
}

interface CreateRechargeData {
  amount: number;           // 金额（元）
  paymentMethod: 'alipay' | 'wechat';
}

interface CreateWithdrawalData {
  amount: number;           // 金额（元）
  accountId: string;
  password: string;         // 支付密码
}

interface TransactionQueryParams extends PaginationParams {
  type?: TransactionType;
  startDate?: string;
  endDate?: string;
}

interface AddAccountData {
  type: 'bank_card' | 'alipay';
  accountName: string;
  accountNumber: string;
  bankName?: string;
}

interface IncomeStatistics {
  totalIncome: number;
  periodIncome: number;
  orderCount: number;
  averageOrderAmount: number;
  dailyData: { date: string; amount: number }[];
}
```

### 钱包 Store 接口

```typescript
// stores/wallet.ts
interface WalletState {
  wallet: Wallet | null;
  transactions: WalletTransaction[];
  withdrawals: Withdrawal[];
  withdrawalAccounts: WithdrawalAccount[];
  incomeStatistics: IncomeStatistics | null;
  loading: boolean;
  transactionFilters: TransactionFilters;
}

interface WalletActions {
  fetchWallet(): Promise<void>;
  createRecharge(amount: number, method: 'alipay' | 'wechat'): Promise<RechargeOrder>;
  createWithdrawal(amount: number, accountId: string, password: string): Promise<Withdrawal>;
  fetchTransactions(filters?: TransactionFilters): Promise<void>;
  fetchWithdrawalAccounts(): Promise<void>;
  addWithdrawalAccount(data: AddAccountData): Promise<void>;
  deleteWithdrawalAccount(id: string): Promise<void>;
  fetchIncomeStatistics(period: 'day' | 'week' | 'month'): Promise<void>;
}
```

### 手续费计算逻辑

```typescript
// utils/feeCalculator.ts
interface FeeConfig {
  rate: number;           // 费率（如 0.01 表示 1%）
  minFee: number;         // 最低手续费（分）
  maxFee?: number;        // 最高手续费（分）
}

const DEFAULT_FEE_CONFIG: FeeConfig = {
  rate: 0.01,             // 1%
  minFee: 100,            // 最低 1 元
};

function calculateWithdrawalFee(amount: number, config: FeeConfig = DEFAULT_FEE_CONFIG): number {
  const calculatedFee = Math.floor(amount * config.rate);
  let fee = Math.max(calculatedFee, config.minFee);
  if (config.maxFee) {
    fee = Math.min(fee, config.maxFee);
  }
  return fee;
}

function calculateActualAmount(amount: number, fee: number): number {
  return amount - fee;
}
```

### 钱包相关正确性属性

基于 prework 分析，以下是钱包系统的正确性属性：

### Property 21: Wallet initialization
*For any* newly registered user or approved institution, a wallet should be automatically created with zero balance and zero frozen balance.
**Validates: Requirements 22.1, 22.2**

### Property 22: Recharge balance invariant
*For any* successful recharge of amount A, the wallet balance should increase by exactly A, and a transaction record of type "recharge" should be created.
**Validates: Requirements 23.3, 23.4**

### Property 23: Failed recharge balance unchanged
*For any* failed recharge attempt, the wallet balance should remain unchanged.
**Validates: Requirements 23.5**

### Property 24: Recharge amount validation
*For any* recharge amount, the system should accept amounts between 1 yuan (100 分) and 50000 yuan (5000000 分), and reject amounts outside this range.
**Validates: Requirements 23.6**

### Property 25: Withdrawal balance transfer
*For any* withdrawal request of amount A, the available balance should decrease by A and frozen balance should increase by A, maintaining total balance invariant.
**Validates: Requirements 24.1, 24.2**

### Property 26: Withdrawal completion
*For any* approved withdrawal of amount A with fee F, the frozen balance should decrease by A, and actual transferred amount should be A - F.
**Validates: Requirements 24.5**

### Property 27: Withdrawal rejection rollback
*For any* rejected withdrawal of amount A, the frozen balance should decrease by A and available balance should increase by A.
**Validates: Requirements 24.6**

### Property 28: Minimum withdrawal validation
*For any* withdrawal amount below 10 yuan (1000 分), the system should reject the request.
**Validates: Requirements 24.8**

### Property 29: Fee calculation correctness
*For any* withdrawal amount A, the fee should be max(A * 0.01, 100), where 100 分 is the minimum fee.
**Validates: Requirements 25.1, 25.2, 25.3**

### Property 30: Transaction filtering by type
*For any* transaction filter by type T, all returned transactions should have type equal to T.
**Validates: Requirements 26.3**

### Property 31: Transaction filtering by date range
*For any* transaction filter with date range [start, end], all returned transactions should have timestamp within the range.
**Validates: Requirements 26.4**

### Property 32: Order income transfer
*For any* completed booking order with amount A, the institution wallet balance should increase by A and an income transaction should be created.
**Validates: Requirements 27.1, 27.2**

### Property 33: Bank card number validation
*For any* bank card number, the system should validate it matches standard formats (16-19 digits) and reject invalid formats.
**Validates: Requirements 28.3**

### Property 34: Withdrawal account deletion constraint
*For any* withdrawal account with pending withdrawals, deletion should be rejected.
**Validates: Requirements 28.5**

### Property 35: Daily withdrawal limit
*For any* wallet, the total withdrawal amount in a single day should not exceed 50000 yuan (5000000 分).
**Validates: Requirements 29.2**

### Property 36: Daily withdrawal attempt limit
*For any* wallet, the number of withdrawal attempts in a single day should not exceed 5.
**Validates: Requirements 29.3**


### 钱包页面路由

```typescript
// router/index.ts 新增路由
{
  path: '/wallet',
  name: 'wallet',
  component: () => import('@/views/wallet/WalletView.vue'),
  meta: { title: '我的钱包', requiresAuth: true }
},
{
  path: '/wallet/recharge',
  name: 'walletRecharge',
  component: () => import('@/views/wallet/RechargeView.vue'),
  meta: { title: '充值', requiresAuth: true }
},
{
  path: '/wallet/withdraw',
  name: 'walletWithdraw',
  component: () => import('@/views/wallet/WithdrawView.vue'),
  meta: { title: '提现', requiresAuth: true }
},
{
  path: '/wallet/transactions',
  name: 'walletTransactions',
  component: () => import('@/views/wallet/TransactionListView.vue'),
  meta: { title: '交易记录', requiresAuth: true }
},
{
  path: '/wallet/accounts',
  name: 'walletAccounts',
  component: () => import('@/views/wallet/WithdrawalAccountsView.vue'),
  meta: { title: '提现账户', requiresAuth: true }
},
// 机构收入管理
{
  path: '/institution/income',
  name: 'institutionIncome',
  component: () => import('@/views/institution/IncomeView.vue'),
  meta: { title: '收入管理', requiresAuth: true, roles: ['institution_staff'] }
}
```

### 钱包 UI 样式

```scss
// 钱包卡片
.wallet-card {
  background: linear-gradient(135deg, var(--color-primary) 0%, #FF8F5C 100%);
  border-radius: var(--radius-xl);
  padding: 24px;
  color: white;
  
  .wallet-balance {
    .label {
      font-size: 14px;
      opacity: 0.9;
    }
    
    .amount {
      font-size: 36px;
      font-weight: 700;
      font-family: var(--font-display);
      margin: 8px 0;
      
      .currency {
        font-size: 20px;
        margin-right: 4px;
      }
    }
  }
  
  .wallet-frozen {
    font-size: 13px;
    opacity: 0.8;
    margin-top: 8px;
  }
  
  .wallet-actions {
    display: flex;
    gap: 12px;
    margin-top: 20px;
    
    .action-btn {
      flex: 1;
      padding: 12px;
      background: rgba(255, 255, 255, 0.2);
      border: none;
      border-radius: var(--radius-lg);
      color: white;
      font-weight: 600;
      cursor: pointer;
      transition: all 0.2s;
      
      &:hover {
        background: rgba(255, 255, 255, 0.3);
      }
    }
  }
}

// 交易记录项
.transaction-item {
  display: flex;
  align-items: center;
  padding: 16px;
  border-bottom: 1px solid var(--color-border);
  
  .transaction-icon {
    width: 44px;
    height: 44px;
    border-radius: 50%;
    display: flex;
    align-items: center;
    justify-content: center;
    margin-right: 12px;
    
    &.income {
      background: rgba(34, 197, 94, 0.1);
      color: var(--color-success);
    }
    
    &.expense {
      background: rgba(239, 68, 68, 0.1);
      color: var(--color-error);
    }
  }
  
  .transaction-info {
    flex: 1;
    
    .title {
      font-weight: 500;
      font-size: 15px;
    }
    
    .time {
      font-size: 13px;
      color: var(--color-text-muted);
      margin-top: 2px;
    }
  }
  
  .transaction-amount {
    font-weight: 600;
    font-size: 16px;
    
    &.positive {
      color: var(--color-success);
    }
    
    &.negative {
      color: var(--color-text-primary);
    }
  }
}

// 提现账户卡片
.account-card {
  display: flex;
  align-items: center;
  padding: 16px;
  background: var(--color-surface);
  border: 2px solid var(--color-border);
  border-radius: var(--radius-lg);
  cursor: pointer;
  transition: all 0.2s;
  
  &.selected {
    border-color: var(--color-primary);
    background: var(--color-primary-light);
  }
  
  .account-icon {
    width: 48px;
    height: 48px;
    border-radius: var(--radius-md);
    display: flex;
    align-items: center;
    justify-content: center;
    margin-right: 12px;
    
    &.bank {
      background: #E3F2FD;
      color: #1976D2;
    }
    
    &.alipay {
      background: #E8F5E9;
      color: #1976D2;
    }
  }
  
  .account-info {
    flex: 1;
    
    .name {
      font-weight: 500;
      font-size: 15px;
    }
    
    .number {
      font-size: 13px;
      color: var(--color-text-muted);
      margin-top: 2px;
    }
  }
  
  .default-badge {
    padding: 4px 8px;
    background: var(--color-primary);
    color: white;
    font-size: 11px;
    border-radius: var(--radius-sm);
  }
}

// 充值金额选择
.amount-selector {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 12px;
  
  .amount-option {
    padding: 16px;
    text-align: center;
    border: 2px solid var(--color-border);
    border-radius: var(--radius-lg);
    cursor: pointer;
    transition: all 0.2s;
    
    &.selected {
      border-color: var(--color-primary);
      background: var(--color-primary-light);
    }
    
    .value {
      font-size: 20px;
      font-weight: 700;
      color: var(--color-text-primary);
    }
    
    .unit {
      font-size: 14px;
      color: var(--color-text-muted);
    }
  }
}

// 提现信息卡片
.withdraw-info {
  background: var(--color-neutral-50);
  border-radius: var(--radius-lg);
  padding: 16px;
  
  .info-row {
    display: flex;
    justify-content: space-between;
    padding: 8px 0;
    
    &:not(:last-child) {
      border-bottom: 1px dashed var(--color-border);
    }
    
    .label {
      color: var(--color-text-secondary);
    }
    
    .value {
      font-weight: 500;
      
      &.fee {
        color: var(--color-warning);
      }
      
      &.actual {
        color: var(--color-success);
        font-size: 18px;
      }
    }
  }
}
```
