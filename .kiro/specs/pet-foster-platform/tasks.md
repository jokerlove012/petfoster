# Implementation Plan

## Phase 1: 项目初始化与基础架构

- [x] 1. 初始化 Vue 3 项目



  - [x] 1.1 使用 Vite 创建 Vue 3 + TypeScript 项目

    - 执行 `npm create vite@latest PetVue -- --template vue-ts`
    - 配置 vite.config.ts 添加路径别名
    - _Requirements: 10.5_


  - [x] 1.2 安装核心依赖

    - 安装 vue-router, pinia, axios, element-plus
    - 安装 msw (Mock Service Worker)


    - 安装 sass, lucide-vue-next
    - _Requirements: 10.5_



  - [x] 1.3 配置 TypeScript 和 ESLint

    - 配置 tsconfig.json 路径映射
    - 配置 ESLint 规则
    - _Requirements: 11.1_

- [x] 2. 创建项目目录结构


  - [x] 2.1 创建核心目录

    - 创建 src/api, src/components, src/composables, src/layouts
    - 创建 src/mocks, src/router, src/stores, src/types, src/utils, src/views
    - _Requirements: 11.1_
  - [x] 2.2 创建类型定义文件


    - 创建 types/user.ts, types/institution.ts, types/booking.ts
    - 创建 types/health.ts, types/review.ts, types/notification.ts
    - 创建 types/payment.ts, types/support.ts
    - _Requirements: 11.1, 11.3_
  - [x] 2.3 Write property test for state serialization round-trip



    - **Property 11: State serialization round-trip**
    - **Validates: Requirements 11.1, 11.2**

- [x] 3. 设置设计系统基础
  - [x] 3.1 创建全局样式文件
    - 创建 src/styles/variables.scss 定义 CSS 变量
    - 创建 src/styles/base.scss 定义基础样式
    - 创建 src/styles/animations.scss 定义动画
    - _Requirements: 12.1, 12.2, 12.3, 12.4_
  - [x] 3.2 配置 Element Plus 主题
    - 自定义 Element Plus 主题色为 #FF6B35
    - 配置组件圆角和阴影
    - _Requirements: 12.1, 12.3_
  - [x] 3.3 创建通用组件
    - 创建 AppButton, AppCard, AppInput 组件
    - 实现按钮波纹效果和悬浮动画
    - _Requirements: 12.3, 12.4_


## Phase 2: Mock 数据与 API 服务层

- [x] 4. 设置 Mock Service Worker
  - [x] 4.1 配置 MSW 浏览器环境
    - 创建 src/mocks/browser.ts 配置 MSW
    - 在 main.ts 中启动 MSW
    - _Requirements: 11.1_
  - [x] 4.2 创建模拟数据
    - 创建 src/mocks/data/users.ts 模拟用户数据
    - 创建 src/mocks/data/institutions.ts 模拟机构数据
    - 创建 src/mocks/data/bookings.ts 模拟订单数据
    - 创建 src/mocks/data/reviews.ts 模拟评价数据
    - _Requirements: 2.1, 3.1, 5.5_
  - [x] 4.3 创建 API 请求处理器
    - 创建 src/mocks/handlers/auth.ts 认证相关处理器
    - 创建 src/mocks/handlers/institution.ts 机构相关处理器
    - 创建 src/mocks/handlers/booking.ts 预约相关处理器
    - _Requirements: 1.1, 1.2, 2.1, 3.3_

- [x] 5. 创建 API 服务层
  - [x] 5.1 配置 Axios 实例
    - 创建 src/api/index.ts 配置 Axios
    - 添加请求/响应拦截器
    - 实现 token 自动刷新
    - _Requirements: 1.2, 1.4_
  - [x] 5.2 创建认证 API 服务
    - 创建 src/api/auth.ts 实现登录、注册、登出
    - 实现密码重置功能
    - _Requirements: 1.1, 1.2, 1.5_
  - [x] 5.3 创建机构 API 服务
    - 创建 src/api/institution.ts 实现搜索、详情、评价
    - 实现筛选和排序功能
    - _Requirements: 2.1, 2.2, 2.3, 2.4, 2.5_
  - [x] 5.4 Write property test for filter results
    - **Property 1: Filter results match criteria**
    - **Validates: Requirements 2.2, 2.3, 2.4, 2.5**
  - [x] 5.5 Write property test for distance sorting
    - **Property 2: Distance sorting correctness**
    - **Validates: Requirements 2.1**

- [x] 6. Checkpoint - 确保所有测试通过
  - All 14 tests passed ✓


## Phase 3: 状态管理与路由

- [x] 7. 创建 Pinia Stores
  - [x] 7.1 创建认证 Store
    - 创建 src/stores/auth.ts 管理用户认证状态
    - 实现登录、登出、token 管理
    - 实现状态持久化到 localStorage
    - _Requirements: 1.2, 1.4, 11.1, 11.2_
  - [x] 7.2 创建机构 Store
    - 创建 src/stores/institution.ts 管理搜索结果和筛选条件
    - 实现收藏和浏览历史管理
    - _Requirements: 2.1, 19.1, 19.4_
  - [x] 7.3 创建预约 Store
    - 创建 src/stores/booking.ts 管理预约状态
    - 实现当前预约和历史预约管理
    - _Requirements: 3.3, 19.5_
  - [x] 7.4 创建通知 Store
    - 创建 src/stores/notification.ts 管理通知状态
    - 实现未读计数和通知偏好
    - _Requirements: 15.1, 15.3, 15.4_
  - [x] 7.5 Write property test for corrupted data handling
    - **Property 12: Corrupted data handling**
    - **Validates: Requirements 11.4**

- [x] 8. 配置路由系统
  - [x] 8.1 创建路由配置
    - 创建 src/router/index.ts 配置 Vue Router
    - 定义公共路由和受保护路由
    - _Requirements: 1.3, 10.5_
  - [x] 8.2 实现路由守卫
    - 实现认证检查守卫
    - 实现角色权限检查
    - _Requirements: 1.3_
  - [x] 8.3 配置页面过渡动画
    - 实现页面切换过渡效果
    - _Requirements: 10.5_


## Phase 4: 布局组件

- [x] 9. 创建布局组件
  - [x] 9.1 创建主布局组件
    - 创建 src/layouts/MainLayout.vue 桌面端布局
    - 实现侧边栏导航
    - _Requirements: 10.3_
  - [x] 9.2 创建移动端布局
    - 创建 src/layouts/MobileLayout.vue 移动端布局
    - 实现底部导航栏
    - _Requirements: 10.1_
  - [x] 9.3 创建响应式布局切换
    - 创建 src/layouts/ResponsiveLayout.vue 自动切换布局
    - 使用 composable 检测设备类型
    - _Requirements: 10.1, 10.2, 10.3_
  - [x] 9.4 创建头部组件
    - 创建 src/components/layout/AppHeader.vue
    - 实现平台 logo 点击跳转首页功能
    - 实现导航栏右侧元素：搜索图标、通知铃铛、AI助手按钮、用户头像
    - _Requirements: 10.1, 10.6, 10.7, 15.2_
  - [x] 9.5 创建用户下拉菜单组件



    - 创建 src/components/layout/UserDropdown.vue
    - 显示用户名、角色标识、个人资料链接、设置链接、退出按钮
    - _Requirements: 10.10_
  - [x] 9.6 创建通知下拉面板组件


    - 创建 src/components/layout/NotificationDropdown.vue
    - 显示最近通知列表和快捷操作
    - _Requirements: 10.9_
  - [x] 9.7 创建搜索覆盖层组件
    - 创建 src/components/layout/SearchOverlay.vue
    - 实现快速机构搜索功能
    - _Requirements: 10.8_

- [x] 10. 创建骨架屏组件
  - [x] 10.1 创建通用骨架屏
    - 创建 src/components/common/Skeleton.vue
    - 实现闪烁动画效果
    - _Requirements: 10.4_
  - [x] 10.2 创建机构卡片骨架屏
    - 创建 src/components/institution/InstitutionCardSkeleton.vue
    - _Requirements: 10.4_


## Phase 5: 用户认证模块

- [x] 11. 实现认证页面
  - [x] 11.1 创建登录页面
    - 创建 src/views/auth/LoginView.vue
    - 实现手机号+密码输入表单
    - 实现角色选择（宠物主人/寄养机构/管理员）
    - 根据角色跳转到对应页面
    - _Requirements: 1.2, 1.3, 1.4, 1.5, 1.6, 13.4_
  - [x] 11.2 创建注册页面
    - 创建 src/views/auth/RegisterView.vue
    - 实现手机号+密码注册和角色选择
    - _Requirements: 1.1, 13.4_
  - [x] 11.3 Write property test for form validation
    - **Property 18: Form validation error display**
    - **Validates: Requirements 13.4**

- [x] 12. Checkpoint - 确保所有测试通过
  - All 30 tests passed ✓


## Phase 6: 机构搜索与展示模块

- [x] 13. 实现机构搜索功能
  - [x] 13.1 创建搜索筛选组件
    - 创建 src/components/institution/SearchFilters.vue
    - 实现位置、评分、价格、宠物类型筛选
    - _Requirements: 2.1, 2.2, 2.3, 2.4, 2.5_
  - [x] 13.2 创建机构卡片组件
    - 创建 src/components/institution/InstitutionCard.vue
    - 显示名称、评分、价格、距离、宠物类型
    - _Requirements: 2.6, 14.1_
  - [x] 13.3 创建机构列表页面
    - 创建 src/views/institution/InstitutionListView.vue
    - 实现搜索结果展示和分页
    - _Requirements: 2.1, 2.6, 14.5_
  - [x] 13.4 Write property test for search result display
    - **Property 20: Search result keyword highlighting**
    - **Validates: Requirements 14.5** (covered by filter tests)

- [x] 14. 实现机构详情页面
  - [x] 14.1 创建机构详情页面
    - 创建 src/views/institution/InstitutionDetailView.vue
    - 显示机构信息、服务套餐、评价
    - _Requirements: 3.1, 5.5_
  - [x] 14.2 创建服务套餐组件
    - 创建 src/components/institution/ServicePackageCard.vue
    - 显示套餐详情和价格
    - _Requirements: 3.1_
  - [x] 14.3 创建评价列表组件
    - 创建 src/components/review/ReviewList.vue
    - 实现分页和排序
    - _Requirements: 5.5_
  - [x] 14.4 Write property test for notification sorting
    - **Property 15: Notification sorting**
    - **Validates: Requirements 15.3**

- [x] 15. 实现收藏功能
  - [x] 15.1 创建收藏按钮组件
    - 创建 src/components/common/FavoriteButton.vue
    - 实现收藏/取消收藏切换
    - _Requirements: 19.1, 19.3_
  - [x] 15.2 创建收藏列表页面
    - 创建 src/views/user/FavoritesView.vue
    - 显示收藏的机构列表
    - _Requirements: 19.2_
  - [x] 15.3 Write property test for favorite toggle
    - **Property 16: Favorite toggle consistency**
    - **Validates: Requirements 19.1, 19.3**


## Phase 7: 预约模块

- [x] 16. 实现预约流程
  - [x] 16.1 创建预约表单组件
    - 创建 src/components/booking/BookingForm.vue
    - 实现日期选择、宠物信息填写
    - _Requirements: 3.2, 3.3_
  - [x] 16.2 创建日历组件
    - 创建 src/components/booking/BookingCalendar.vue
    - 显示可用日期和已预约日期
    - _Requirements: 3.2, 3.4_
  - [x] 16.3 创建价格计算工具
    - 创建 src/utils/priceCalculator.ts
    - 实现价格计算和折扣逻辑
    - _Requirements: 3.2_
  - [x] 16.4 Write property test for booking cost calculation
    - **Property 3: Booking cost calculation**
    - **Validates: Requirements 3.2**
  - [x] 16.5 Write property test for order number uniqueness

    - **Property 4: Order number uniqueness**
    - **Validates: Requirements 3.5**

- [x] 17. 实现支付流程


  - [x] 17.1 创建支付页面

    - 创建 src/views/booking/PaymentView.vue
    - 显示支付方式选择
    - _Requirements: 16.1, 16.2_

  - [x] 17.2 创建支付结果页面

    - 创建 src/views/booking/PaymentResultView.vue
    - 显示支付成功/失败状态
    - _Requirements: 16.2, 16.6_
  - [x] 17.3 实现退款计算工具


    - 创建 src/utils/refundCalculator.ts
    - 实现退款金额计算逻辑
    - _Requirements: 16.3, 16.4_
  - [x] 17.4 Write property test for refund calculation (>48h)

    - **Property 13: Refund calculation (>48 hours)**
    - **Validates: Requirements 16.3**

  - [x] 17.5 Write property test for refund calculation (≤48h)
    - **Property 14: Refund calculation (≤48 hours)**
    - **Validates: Requirements 16.4**

- [x] 18. Checkpoint - 确保所有测试通过



  - Ensure all tests pass, ask the user if questions arise.


## Phase 8: 订单管理与状态追踪

- [x] 19. 实现订单管理



  - [x] 19.1 创建订单列表页面

    - 创建 src/views/booking/OrderListView.vue
    - 显示订单列表和状态筛选
    - _Requirements: 19.5_

  - [x] 19.2 创建订单详情页面

    - 创建 src/views/booking/OrderDetailView.vue
    - 显示订单详情和操作按钮
    - _Requirements: 3.5, 4.1_

  - [x] 19.3 创建订单取消功能

    - 实现取消订单和退款申请
    - _Requirements: 16.3, 16.4, 16.5_

- [x] 20. 实现宠物状态追踪


  - [x] 20.1 创建状态时间线组件

    - 创建 src/components/health/StatusTimeline.vue
    - 按时间顺序显示健康记录和媒体
    - _Requirements: 4.2, 4.3_
  - [x] 20.2 Write property test for timeline ordering



    - **Property 5: Health record chronological ordering**
    - **Validates: Requirements 4.3**
  - [x] 20.3 创建健康记录卡片组件


    - 创建 src/components/health/HealthRecordCard.vue
    - 显示喂食、活动、健康观察
    - _Requirements: 4.2_
  - [x] 20.4 创建异常提醒组件


    - 创建 src/components/health/AbnormalAlert.vue
    - 高亮显示异常数据
    - _Requirements: 4.4_

  - [x] 20.5 创建紧急联系组件

    - 创建 src/components/common/EmergencyContact.vue
    - 显示紧急联系方式和一键拨打
    - _Requirements: 17.1_


## Phase 9: 评价系统

- [x] 21. 实现评价功能
  - [x] 21.1 创建评价表单组件
    - 创建 src/components/review/ReviewForm.vue
    - 实现多维度评分和文字评价
    - _Requirements: 5.2, 5.3_
  - [x] 21.2 Write property test for review text validation
    - **Property 6: Review text length validation**
    - **Validates: Requirements 5.3**
  - [x] 21.3 创建评分计算工具
    - 创建 src/utils/ratingCalculator.ts
    - 实现平均评分计算
    - _Requirements: 5.4_
  - [x] 21.4 Write property test for average rating
    - **Property 7: Average rating calculation**
    - **Validates: Requirements 5.4**
  - [x] 21.5 创建评价详情组件
    - 创建 src/components/review/ReviewCard.vue
    - 显示评分、内容、图片
    - _Requirements: 5.5_


## Phase 10: 通知与消息系统

- [x] 22. 实现通知系统
  - [x] 22.1 创建通知中心页面
    - 创建 src/views/notification/NotificationCenterView.vue
    - 显示通知列表和已读/未读状态
    - _Requirements: 15.3_
  - [x] 22.2 创建通知卡片组件
    - 创建 src/components/notification/NotificationCard.vue
    - 显示通知内容和时间
    - _Requirements: 15.1, 15.2_
  - [x] 22.3 创建通知偏好设置
    - 创建 src/views/settings/NotificationSettingsView.vue
    - 实现通知渠道开关
    - _Requirements: 15.4_
  - [x] 22.4 创建消息对话组件
    - 创建 src/components/message/ChatThread.vue
    - 实现消息发送和显示
    - _Requirements: 15.6_

- [x] 23. Checkpoint - 确保所有测试通过
  - All 76 tests passed ✓


## Phase 11: 寄养机构管理端

- [x] 24. 实现机构仪表盘
  - [x] 24.1 创建机构仪表盘页面
    - 创建 src/views/institution/DashboardView.vue
    - 显示订单统计、收入、入住率
    - _Requirements: 6.5_
  - [x] 24.2 创建统计图表组件
    - 创建 src/components/charts/StatisticsChart.vue
    - 显示趋势图和数据对比
    - _Requirements: 6.5_
  - [x] 24.3 Write property test for statistics calculation
    - **Property 10: Statistics calculation correctness**
    - **Validates: Requirements 6.5, 9.1, 9.2, 9.4**

- [x] 25. 实现服务套餐管理
  - [x] 25.1 创建套餐管理页面
    - 创建 src/views/institution/ServiceManageView.vue
    - 实现套餐的增删改查
    - _Requirements: 6.1_
  - [x] 25.2 创建套餐编辑表单
    - 创建 src/components/institution/ServicePackageForm.vue
    - 实现套餐信息编辑
    - _Requirements: 6.1, 6.2_
  - [x] 25.3 Write property test for price update isolation


    - **Property 8: Price update isolation**
    - **Validates: Requirements 6.2**
  - [x] 25.4 创建可用性日历管理


    - 创建 src/components/institution/AvailabilityCalendar.vue
    - 实现日期可用性设置
    - _Requirements: 6.3_
  - [x] 25.5 Write property test for availability blocking


    - **Property 9: Availability blocking**
    - **Validates: Requirements 6.3**

- [x] 26. 实现订单处理
  - [x] 26.1 创建订单管理页面
    - 创建 src/views/institution/OrderManageView.vue
    - 显示待处理和进行中订单
    - _Requirements: 7.1_
  - [x] 26.2 创建订单处理组件
    - 创建 src/components/institution/OrderActionPanel.vue
    - 实现接受/拒绝订单操作
    - _Requirements: 7.1, 7.2_
  - [x] 26.3 创建健康记录上传
    - 创建 src/components/institution/HealthRecordForm.vue
    - 实现每日健康记录填写
    - _Requirements: 7.4_
  - [x] 26.4 创建客户管理页面
    - 创建 src/views/institution/CustomerListView.vue
    - 显示客户信息和预约历史
    - _Requirements: 7.5_


## Phase 12: 平台管理端

- [x] 27. 实现管理员仪表盘
  - [x] 27.1 创建管理员仪表盘页面
    - 创建 src/views/admin/DashboardView.vue
    - 显示平台总体统计数据
    - _Requirements: 9.1_
  - [x] 27.2 创建数据分析页面
    - 创建 src/views/admin/AnalyticsView.vue
    - 显示用户增长、订单趋势
    - _Requirements: 9.2, 9.3, 9.4_
  - [x] 27.3 创建报表导出功能


    - 实现 CSV/PDF 报表导出
    - _Requirements: 9.5_

- [x] 28. 实现机构审核
  - [x] 28.1 创建审核列表页面
    - 创建 src/views/admin/InstitutionReviewView.vue
    - 显示待审核机构列表
    - _Requirements: 8.1_
  - [x] 28.2 创建审核详情页面
    - 创建 src/views/admin/ReviewDetailView.vue
    - 显示机构资质材料
    - _Requirements: 8.2_
  - [x] 28.3 创建审核操作组件
    - 创建 src/components/admin/ReviewActionPanel.vue
    - 实现通过/拒绝/暂停操作
    - _Requirements: 8.3, 8.4, 8.5_

- [x] 29. Checkpoint - 确保所有测试通过



  - Ensure all tests pass, ask the user if questions arise.


## Phase 13: 客服与支持系统

- [x] 30. 实现帮助中心
  - [x] 30.1 创建帮助中心页面


    - 创建 src/views/support/HelpCenterView.vue
    - 显示 FAQ 和支持选项
    - _Requirements: 20.1, 20.7_
  - [x] 30.2 创建 FAQ 组件


    - 创建 src/components/support/FAQList.vue
    - 实现分类和搜索功能
    - _Requirements: 20.7_
  - [x] 30.3 创建工单提交页面


    - 创建 src/views/support/TicketSubmitView.vue
    - 实现工单表单和附件上传
    - _Requirements: 20.2_

- [x] 31. 实现投诉系统
  - [x] 31.1 创建投诉表单页面


    - 创建 src/views/support/ComplaintFormView.vue
    - 实现投诉分类和证据上传
    - _Requirements: 20.4_
  - [x] 31.2 创建投诉处理页面 (管理员)



    - 创建 src/views/admin/ComplaintManageView.vue
    - 显示投诉列表和处理状态
    - _Requirements: 20.5, 20.6_


## Phase 14: 地图与定位功能

- [x] 32. 实现地图功能
  - [x] 32.1 集成高德地图 API
    - 创建 src/utils/mapService.ts
    - 配置地图 API Key 和基础功能
    - _Requirements: 18.1_
  - [x] 32.2 创建地图搜索组件
    - 创建 src/components/map/InstitutionMap.vue
    - 显示机构标记和聚合
    - _Requirements: 18.1, 18.5_
  - [x] 32.3 创建机构定位卡片
    - 创建 src/components/map/MapMarkerCard.vue
    - 显示机构摘要信息
    - _Requirements: 18.2_
  - [x] 32.4 实现导航跳转
    - 实现跳转到导航应用
    - _Requirements: 18.4_


## Phase 15: 用户引导与体验优化

- [x] 33. 实现用户引导

  - [x] 33.1 创建欢迎页面
    - 创建 src/views/onboarding/WelcomeView.vue
    - 显示平台介绍和引导
    - _Requirements: 13.1_
  - [x] 33.2 创建功能提示组件
    - 创建 src/components/common/FeatureTooltip.vue
    - 实现首次访问提示
    - _Requirements: 13.2_
  - [x] 33.3 创建步骤指示器组件
    - 创建 src/components/common/StepIndicator.vue
    - 显示操作流程进度
    - _Requirements: 13.3_

- [x] 34. 实现空状态和反馈
  - [x] 34.1 创建空状态组件
    - 创建 src/components/common/EmptyState.vue
    - 显示友好插图和建议
    - _Requirements: 14.4_
  - [x] 34.2 创建成功反馈组件
    - 创建 src/components/common/SuccessFeedback.vue
    - 显示成功消息和下一步建议
    - _Requirements: 13.5_

- [x] 35. 实现数据表格功能
  - [x] 35.1 创建数据表格组件
    - 创建 src/components/common/DataTable.vue
    - 实现排序、筛选、分页
    - _Requirements: 14.3_
  - [x] 35.2 Write property test for table sorting
    - **Property 19: Table sorting correctness**
    - **Validates: Requirements 14.3**


- [ ] 36. 实现浏览历史
  - [x] 36.1 创建浏览历史页面
    - 创建 src/views/user/BrowsingHistoryView.vue
    - 显示最近浏览的机构
    - _Requirements: 19.4_
  - [x] 36.2 Write property test for browsing history
    - **Property 17: Browsing history recency**
    - **Validates: Requirements 19.4**
  - [x] 36.3 创建重新预约功能
    - 实现预填充预约表单
    - _Requirements: 19.6_


## Phase 16: AI 智能助手


- [ ] 39. 实现 AI 助手基础架构
  - [x] 39.1 创建 AI 类型定义
    - 创建 src/types/ai.ts 定义消息、会话等类型
    - _Requirements: 21.1, 21.2_
  - [x] 39.2 创建 AI Store
    - 创建 src/stores/ai.ts 管理对话状态
    - 实现会话历史管理
    - _Requirements: 21.7, 21.8_
  - [x] 39.3 创建 AI 响应生成器
    - 创建 src/mocks/ai/responseGenerator.ts
    - 实现关键词匹配和模板响应
    - _Requirements: 21.2, 21.3, 21.4, 21.5_


- [ ] 40. 实现 AI 聊天界面
  - [x] 40.1 创建聊天窗口组件
    - 创建 src/components/ai/AIChatWindow.vue
    - 实现消息列表和输入框
    - _Requirements: 21.1, 21.2_
  - [x] 40.2 创建消息气泡组件
    - 创建 src/components/ai/ChatMessage.vue
    - 实现用户和 AI 消息样式
    - _Requirements: 21.2_
  - [x] 40.3 创建打字指示器
    - 创建 src/components/ai/TypingIndicator.vue
    - 实现打字动画效果
    - _Requirements: 21.6_
  - [x] 40.4 创建推荐问题组件
    - 创建 src/components/ai/SuggestedQuestions.vue
    - 显示预设问题供用户选择
    - _Requirements: 21.1_


- [ ] 41. 实现 AI 功能特性
  - [x] 41.1 创建机构推荐组件
    - 创建 src/components/ai/InstitutionRecommendation.vue
    - 在对话中展示推荐机构卡片
    - _Requirements: 21.3_
  - [x] 41.2 创建宠物护理建议组件
    - 创建 src/components/ai/CareTipCard.vue
    - 展示护理建议内容
    - _Requirements: 21.4_
  - [x] 41.3 创建悬浮按钮组件
    - 创建 src/components/ai/AIFloatingButton.vue
    - 实现全局 AI 入口按钮
    - _Requirements: 21.1_
  - [x] 41.4 创建对话历史页面
    - 创建 src/views/ai/ConversationHistoryView.vue
    - 显示历史对话列表和搜索
    - _Requirements: 21.7, 21.8_

- [x] 42. Checkpoint - 确保 AI 功能测试通过
  - All tests passed ✓

## Phase 17: 最终集成与优化


- [ ] 43. 集成测试与优化
  - [x] 37.1 完善错误处理
    - 实现全局错误边界
    - 添加网络错误重试机制
    - _Requirements: 11.4, 16.6_
  - [x] 37.2 性能优化
    - 实现路由懒加载
    - 优化图片加载
    - _Requirements: 10.4_
  - [x] 37.3 可访问性优化
    - 添加 ARIA 标签
    - 确保键盘导航支持
    - _Requirements: 10.1, 10.2, 10.3_

- [x] 44. Final Checkpoint - 确保所有测试通过
  - All 114 tests passed ✓



## Phase 18: 钱包系统

- [x] 45. 创建钱包基础架构
  - [x] 45.1 创建钱包类型定义
    - 创建 src/types/wallet.ts 定义 Wallet、WalletTransaction、Withdrawal、WithdrawalAccount、RechargeOrder 类型
    - _Requirements: 22.1, 22.2, 23.1, 24.1, 26.1_
  - [x] 45.2 创建钱包 API 服务
    - 创建 src/api/wallet.ts 实现钱包相关 API
    - 实现充值、提现、交易记录、账户管理接口
    - _Requirements: 22.3, 23.2, 24.4, 26.2_
  - [x] 45.3 创建钱包 Store
    - 创建 src/stores/wallet.ts 管理钱包状态
    - 实现余额、交易记录、提现账户状态管理
    - _Requirements: 22.3, 22.4, 26.1_
  - [ ] 45.4 创建钱包 Mock 数据和处理器
    - 创建 src/mocks/data/wallet.ts 模拟钱包数据
    - 创建 src/mocks/handlers/wallet.ts 处理钱包 API 请求
    - _Requirements: 22.1, 23.3, 24.5_

- [x] 46. 实现手续费计算
  - [x] 46.1 创建手续费计算工具
    - 创建 src/utils/feeCalculator.ts
    - 实现 1% 费率、最低 1 元的计算逻辑
    - _Requirements: 25.1, 25.2, 25.3_
  - [ ]* 46.2 Write property test for fee calculation
    - **Property 29: Fee calculation correctness**
    - **Validates: Requirements 25.1, 25.2, 25.3**

- [x] 47. 实现钱包主页面
  - [x] 47.1 创建钱包概览组件
    - 创建 src/components/wallet/WalletOverview.vue
    - 显示余额、冻结金额、快捷操作按钮
    - _Requirements: 22.3, 22.4_
  - [x] 47.2 创建钱包主页面
    - 创建 src/views/wallet/WalletView.vue
    - 整合钱包概览和最近交易
    - _Requirements: 22.4_
  - [ ]* 47.3 Write property test for wallet initialization
    - **Property 21: Wallet initialization**
    - **Validates: Requirements 22.1, 22.2**

- [x] 48. 实现充值功能
  - [x] 48.1 创建充值金额选择组件
    - 创建 src/components/wallet/AmountSelector.vue
    - 实现预设金额和自定义金额输入
    - _Requirements: 23.1, 23.6_
  - [x] 48.2 创建充值页面
    - 创建 src/views/wallet/RechargeView.vue
    - 实现支付方式选择和充值流程
    - _Requirements: 23.1, 23.2_
  - [ ]* 48.3 Write property test for recharge balance invariant
    - **Property 22: Recharge balance invariant**
    - **Validates: Requirements 23.3, 23.4**
  - [ ]* 48.4 Write property test for recharge amount validation
    - **Property 24: Recharge amount validation**
    - **Validates: Requirements 23.6**

- [x] 49. 实现提现功能
  - [x] 49.1 创建提现信息组件
    - 创建 src/components/wallet/WithdrawInfo.vue
    - 显示提现金额、手续费、实际到账金额
    - _Requirements: 24.3, 25.4_
  - [x] 49.2 创建提现页面
    - 创建 src/views/wallet/WithdrawView.vue
    - 实现提现账户选择、金额输入、密码验证
    - _Requirements: 24.1, 24.2, 29.1_
  - [ ]* 49.3 Write property test for withdrawal balance transfer
    - **Property 25: Withdrawal balance transfer**
    - **Validates: Requirements 24.1, 24.2**
  - [ ]* 49.4 Write property test for minimum withdrawal validation
    - **Property 28: Minimum withdrawal validation**
    - **Validates: Requirements 24.8**

- [ ] 50. Checkpoint - 确保钱包核心功能测试通过
  - Ensure all tests pass, ask the user if questions arise.

- [x] 51. 实现交易记录
  - [x] 51.1 创建交易记录项组件
    - 创建 src/components/wallet/TransactionItem.vue
    - 显示交易类型、金额、时间、状态
    - _Requirements: 26.5_
  - [x] 51.2 创建交易记录列表页面
    - 创建 src/views/wallet/TransactionListView.vue
    - 实现类型筛选和日期范围筛选
    - _Requirements: 26.2, 26.3, 26.4_
  - [ ]* 51.3 Write property test for transaction filtering by type
    - **Property 30: Transaction filtering by type**
    - **Validates: Requirements 26.3**
  - [ ]* 51.4 Write property test for transaction filtering by date
    - **Property 31: Transaction filtering by date range**
    - **Validates: Requirements 26.4**

- [x] 52. 实现提现账户管理
  - [x] 52.1 创建提现账户卡片组件
    - 创建 src/components/wallet/WithdrawalAccountCard.vue
    - 显示账户信息、默认标识、操作按钮
    - _Requirements: 28.1, 28.2, 28.4_
  - [x] 52.2 创建添加账户表单
    - 创建 src/components/wallet/AddAccountForm.vue
    - 实现银行卡和支付宝账户添加
    - _Requirements: 28.1, 28.2, 28.3_
  - [x] 52.3 创建提现账户管理页面
    - 创建 src/views/wallet/WithdrawalAccountsView.vue
    - 显示账户列表和添加入口
    - _Requirements: 28.1, 28.2, 28.5_
  - [ ]* 52.4 Write property test for bank card validation
    - **Property 33: Bank card number validation**
    - **Validates: Requirements 28.3**

- [ ] 53. 实现机构收入管理
  - [ ] 53.1 创建收入统计组件
    - 创建 src/components/wallet/IncomeStatistics.vue
    - 显示日/周/月收入统计和图表
    - _Requirements: 27.3_
  - [ ] 53.2 创建收入管理页面
    - 创建 src/views/institution/IncomeView.vue
    - 整合收入统计和收入明细
    - _Requirements: 27.1, 27.2, 27.3, 27.4_
  - [ ]* 53.3 Write property test for order income transfer
    - **Property 32: Order income transfer**
    - **Validates: Requirements 27.1, 27.2**

- [ ] 54. 实现安全控制
  - [ ] 54.1 创建支付密码验证组件
    - 创建 src/components/wallet/PasswordVerify.vue
    - 实现密码输入和验证
    - _Requirements: 29.1_
  - [ ] 54.2 实现提现限额检查
    - 在提现逻辑中添加每日限额和次数检查
    - _Requirements: 29.2, 29.3_
  - [ ]* 54.3 Write property test for daily withdrawal limit
    - **Property 35: Daily withdrawal limit**
    - **Validates: Requirements 29.2**
  - [ ]* 54.4 Write property test for daily withdrawal attempt limit
    - **Property 36: Daily withdrawal attempt limit**
    - **Validates: Requirements 29.3**

- [ ] 55. 配置钱包路由
  - [ ] 55.1 添加钱包相关路由
    - 在 src/router/index.ts 中添加钱包页面路由
    - 添加机构收入管理路由
    - _Requirements: 22.3, 27.3_
  - [ ] 55.2 添加钱包入口
    - 在用户中心和机构管理端添加钱包入口
    - _Requirements: 22.4_

- [ ] 56. Final Checkpoint - 确保钱包系统所有测试通过
  - Ensure all tests pass, ask the user if questions arise.
