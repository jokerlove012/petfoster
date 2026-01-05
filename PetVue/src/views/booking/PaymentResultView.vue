<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { AppButton } from '@/components/common'
import { bookingApi } from '@/api/booking'

const route = useRoute()
const router = useRouter()

// 支付状态
const status = computed(() => route.query.status as 'success' | 'failed' | 'pending')
const orderId = computed(() => route.params.id as string)

// 订单信息
const orderInfo = ref({
  orderNumber: '',
  institutionName: '',
  totalPrice: 0,
  paymentMethod: '',
  paymentTime: ''
})

// 加载订单信息
const loadOrderInfo = async () => {
  try {
    const res = await bookingApi.getDetail(orderId.value)
    if (res.code === 200 && res.data) {
      const data = res.data as any
      orderInfo.value = {
        orderNumber: data.orderNumber || '',
        institutionName: data.institutionName || '',
        totalPrice: Number(data.totalPrice) || 0,
        paymentMethod: data.paymentMethod === 'wechat' ? '微信支付' : 
                       data.paymentMethod === 'alipay' ? '支付宝' : 
                       data.paymentMethod === 'card' ? '银行卡' : data.paymentMethod || '',
        paymentTime: data.paidAt ? new Date(data.paidAt).toLocaleString('zh-CN') : ''
      }
    }
  } catch (error) {
    console.error('加载订单信息失败:', error)
  }
}

onMounted(() => {
  loadOrderInfo()
})

// 状态配置
const statusConfig = computed(() => {
  switch (status.value) {
    case 'success':
      return {
        icon: '✅',
        title: '支付成功',
        subtitle: '您的预约已确认，机构将尽快与您联系',
        color: 'var(--color-success)'
      }
    case 'failed':
      return {
        icon: '❌',
        title: '支付失败',
        subtitle: '支付未完成，请重新尝试或选择其他支付方式',
        color: 'var(--color-error)'
      }
    default:
      return {
        icon: '⏳',
        title: '支付处理中',
        subtitle: '正在确认支付结果，请稍候...',
        color: 'var(--color-warning)'
      }
  }
})

// 查看订单详情
const viewOrder = () => {
  router.push({
    name: 'orderDetail',
    params: { id: orderId.value }
  })
}

// 返回首页
const goHome = () => {
  router.push('/')
}

// 重新支付
const retryPayment = () => {
  router.push({
    name: 'bookingPayment',
    params: { id: orderId.value }
  })
}

// 联系客服
const contactSupport = () => {
  router.push('/help')
}
</script>

<template>
  <div class="payment-result-view">
    <!-- 结果展示 -->
    <div class="result-section">
      <div 
        class="result-icon"
        :style="{ background: statusConfig.color }"
      >
        {{ statusConfig.icon }}
      </div>
      <h1 class="result-title">{{ statusConfig.title }}</h1>
      <p class="result-subtitle">{{ statusConfig.subtitle }}</p>
    </div>

    <!-- 成功状态 - 显示订单信息 -->
    <div v-if="status === 'success'" class="order-info-card">
      <div class="info-row">
        <span class="info-label">订单编号</span>
        <span class="info-value">{{ orderInfo.orderNumber }}</span>
      </div>
      <div class="info-row">
        <span class="info-label">寄养机构</span>
        <span class="info-value">{{ orderInfo.institutionName }}</span>
      </div>
      <div class="info-row">
        <span class="info-label">支付金额</span>
        <span class="info-value price">¥{{ orderInfo.totalPrice.toFixed(2) }}</span>
      </div>
      <div class="info-row">
        <span class="info-label">支付方式</span>
        <span class="info-value">{{ orderInfo.paymentMethod }}</span>
      </div>
      <div class="info-row">
        <span class="info-label">支付时间</span>
        <span class="info-value">{{ orderInfo.paymentTime }}</span>
      </div>
    </div>

    <!-- 成功状态 - 下一步提示 -->
    <div v-if="status === 'success'" class="next-steps">
      <h3>接下来</h3>
      <div class="step-list">
        <div class="step-item">
          <div class="step-number">1</div>
          <div class="step-content">
            <span class="step-title">等待机构确认</span>
            <span class="step-desc">机构将在24小时内确认您的预约</span>
          </div>
        </div>
        <div class="step-item">
          <div class="step-number">2</div>
          <div class="step-content">
            <span class="step-title">准备宠物物品</span>
            <span class="step-desc">准备好宠物的食物、玩具和疫苗本</span>
          </div>
        </div>
        <div class="step-item">
          <div class="step-number">3</div>
          <div class="step-content">
            <span class="step-title">按时送达</span>
            <span class="step-desc">在预约日期将宠物送至机构</span>
          </div>
        </div>
      </div>
    </div>

    <!-- 失败状态 - 失败原因 -->
    <div v-if="status === 'failed'" class="failure-info">
      <h3>可能的原因</h3>
      <ul class="reason-list">
        <li>银行卡余额不足</li>
        <li>支付密码错误</li>
        <li>网络连接不稳定</li>
        <li>支付超时</li>
      </ul>
      <p class="retry-hint">
        您的订单已保留30分钟，请尽快完成支付
      </p>
    </div>

    <!-- 操作按钮 -->
    <div class="action-buttons">
      <template v-if="status === 'success'">
        <AppButton type="primary" size="lg" @click="viewOrder">
          查看订单
        </AppButton>
        <AppButton type="outline" size="lg" @click="goHome">
          返回首页
        </AppButton>
      </template>
      
      <template v-else-if="status === 'failed'">
        <AppButton type="primary" size="lg" @click="retryPayment">
          重新支付
        </AppButton>
        <AppButton type="outline" size="lg" @click="contactSupport">
          联系客服
        </AppButton>
      </template>
      
      <template v-else>
        <AppButton type="outline" size="lg" @click="viewOrder">
          查看订单状态
        </AppButton>
      </template>
    </div>

    <!-- 客服提示 -->
    <div class="support-hint">
      <p>
        如有问题，请联系客服
        <a href="tel:400-123-4567">400-123-4567</a>
      </p>
    </div>
  </div>
</template>

<style lang="scss" scoped>
@import '@/styles/variables.scss';

.payment-result-view {
  min-height: 100vh;
  background: var(--color-surface);
  padding: 48px 24px;
  display: flex;
  flex-direction: column;
  align-items: center;
}

// 结果展示
.result-section {
  text-align: center;
  margin-bottom: 32px;
}

.result-icon {
  width: 80px;
  height: 80px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 40px;
  margin: 0 auto 24px;
  color: white;
  animation: scaleIn 0.5s ease;
}

.result-title {
  font-size: 28px;
  font-weight: 700;
  margin: 0 0 8px;
  color: var(--color-text-primary);
}

.result-subtitle {
  font-size: 15px;
  color: var(--color-text-secondary);
  margin: 0;
}

// 订单信息卡片
.order-info-card {
  width: 100%;
  max-width: 400px;
  background: white;
  border-radius: var(--radius-xl);
  padding: 24px;
  margin-bottom: 24px;
  box-shadow: var(--shadow-md);
}

.info-row {
  display: flex;
  justify-content: space-between;
  padding: 12px 0;
  border-bottom: 1px solid var(--color-border);
  
  &:last-child {
    border-bottom: none;
  }
  
  .info-label {
    font-size: 14px;
    color: var(--color-text-secondary);
  }
  
  .info-value {
    font-size: 14px;
    font-weight: 500;
    color: var(--color-text-primary);
    
    &.price {
      color: var(--color-primary);
      font-weight: 700;
    }
  }
}

// 下一步提示
.next-steps {
  width: 100%;
  max-width: 400px;
  background: white;
  border-radius: var(--radius-xl);
  padding: 24px;
  margin-bottom: 32px;
  box-shadow: var(--shadow-md);
  
  h3 {
    font-size: 16px;
    font-weight: 600;
    margin: 0 0 20px;
  }
}

.step-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.step-item {
  display: flex;
  gap: 16px;
}

.step-number {
  width: 28px;
  height: 28px;
  background: var(--color-primary-light);
  color: var(--color-primary);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 14px;
  font-weight: 600;
  flex-shrink: 0;
}

.step-content {
  display: flex;
  flex-direction: column;
  gap: 4px;
  
  .step-title {
    font-size: 15px;
    font-weight: 600;
    color: var(--color-text-primary);
  }
  
  .step-desc {
    font-size: 13px;
    color: var(--color-text-secondary);
  }
}

// 失败信息
.failure-info {
  width: 100%;
  max-width: 400px;
  background: white;
  border-radius: var(--radius-xl);
  padding: 24px;
  margin-bottom: 32px;
  box-shadow: var(--shadow-md);
  
  h3 {
    font-size: 16px;
    font-weight: 600;
    margin: 0 0 16px;
  }
}

.reason-list {
  margin: 0 0 16px;
  padding-left: 20px;
  
  li {
    font-size: 14px;
    color: var(--color-text-secondary);
    padding: 6px 0;
  }
}

.retry-hint {
  font-size: 14px;
  color: var(--color-warning);
  background: rgba(245, 158, 11, 0.1);
  padding: 12px 16px;
  border-radius: var(--radius-md);
  margin: 0;
}

// 操作按钮
.action-buttons {
  display: flex;
  flex-direction: column;
  gap: 12px;
  width: 100%;
  max-width: 300px;
  margin-bottom: 32px;
}

// 客服提示
.support-hint {
  text-align: center;
  
  p {
    font-size: 13px;
    color: var(--color-text-secondary);
    margin: 0;
    
    a {
      color: var(--color-primary);
      font-weight: 500;
      
      &:hover {
        text-decoration: underline;
      }
    }
  }
}

// 动画
@keyframes scaleIn {
  from {
    transform: scale(0);
    opacity: 0;
  }
  to {
    transform: scale(1);
    opacity: 1;
  }
}
</style>
