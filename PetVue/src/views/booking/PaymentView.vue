<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useBookingStore, useAuthStore, useWalletStore } from '@/stores'
import type { PaymentMethod } from '@/types/booking'
import { AppButton } from '@/components/common'
import { formatPrice } from '@/utils/priceCalculator'
import { bookingApi } from '@/api/booking'
import { ElMessageBox, ElMessage } from 'element-plus'

const route = useRoute()
const router = useRouter()
const bookingStore = useBookingStore()
const authStore = useAuthStore()
const walletStore = useWalletStore()

// 订单信息
const orderId = computed(() => route.params.id as string)
const isLoading = ref(true)
const orderInfo = ref<{
  orderNumber: string
  institutionName: string
  serviceName: string
  petName: string
  startDate: string
  endDate: string
  totalDays: number
  totalPrice: number
} | null>(null)

// 钱包余额（分转元）
const walletBalance = computed(() => walletStore.balance / 100)

// 支付方式
const paymentMethods = computed(() => [
  { id: 'wechat', name: '微信支付', icon: '💚', desc: '推荐使用' },
  { id: 'alipay', name: '支付宝', icon: '💙', desc: '快捷支付' },
  { id: 'wallet', name: '钱包余额', icon: '👛', desc: `余额 ¥${walletBalance.value.toFixed(2)}` }
])

const selectedMethod = ref<PaymentMethod>('wechat')
const isProcessing = ref(false)
const countdown = ref(30 * 60) // 30分钟倒计时
let countdownTimer: number | null = null

// 格式化倒计时
const formattedCountdown = computed(() => {
  const minutes = Math.floor(countdown.value / 60)
  const seconds = countdown.value % 60
  return `${minutes.toString().padStart(2, '0')}:${seconds.toString().padStart(2, '0')}`
})

// 加载订单信息
const loadOrderInfo = async () => {
  isLoading.value = true
  try {
    console.log('Loading order info for:', orderId.value)
    const res = await bookingApi.getDetail(orderId.value)
    console.log('API response:', res)
    if (res.code === 200 && res.data) {
      const data = res.data as any
      console.log('Order data:', data)
      console.log('totalPrice:', data.totalPrice, typeof data.totalPrice)
      orderInfo.value = {
        orderNumber: data.orderNumber || '',
        institutionName: data.institutionName || '未知机构',
        serviceName: data.packageName || '标准套餐',
        petName: data.petName || '未知宠物',
        startDate: data.startDate || '',
        endDate: data.endDate || '',
        totalDays: data.totalDays || 0,
        totalPrice: Number(data.totalPrice) || 0
      }
      console.log('orderInfo set:', orderInfo.value)
    } else {
      console.error('API returned error:', res)
      ElMessage.error('加载订单信息失败')
    }
  } catch (error) {
    console.error('加载订单信息失败:', error)
    ElMessage.error('加载订单信息失败，请刷新重试')
  } finally {
    isLoading.value = false
  }
}

// 开始倒计时
const startCountdown = () => {
  countdownTimer = window.setInterval(() => {
    if (countdown.value > 0) {
      countdown.value--
    } else {
      // 超时，返回订单页
      if (countdownTimer) clearInterval(countdownTimer)
      router.push('/user/orders')
    }
  }, 1000)
}

// 选择支付方式
const selectMethod = (method: PaymentMethod) => {
  selectedMethod.value = method
}

// 提交支付
const handlePayment = async () => {
  if (isProcessing.value) return
  
  if (!orderInfo.value) {
    ElMessage.error('订单信息加载中，请稍候')
    return
  }
  
  // 如果选择钱包支付，先刷新余额再检查
  if (selectedMethod.value === 'wallet') {
    await walletStore.fetchWallet()
    const currentBalance = walletStore.balance / 100  // 分转元
    const requiredAmount = orderInfo.value.totalPrice  // 元
    if (currentBalance < requiredAmount) {
      ElMessageBox.alert(
        `<div style="text-align: center;">
          <p style="font-size: 16px; margin-bottom: 16px;">余额不足，请充值！</p>
          <p style="color: #666;">当前余额：<span style="color: #f56c6c; font-weight: bold;">¥${currentBalance.toFixed(2)}</span></p>
          <p style="color: #666;">应付金额：<span style="color: #e6a23c; font-weight: bold;">¥${requiredAmount.toFixed(2)}</span></p>
        </div>`,
        '支付失败',
        {
          dangerouslyUseHTMLString: true,
          confirmButtonText: '去充值',
          callback: () => {
            // 带上订单ID，充值完成后返回支付页面
            router.push(`/wallet/recharge?returnTo=/booking/payment/${orderId.value}`)
          }
        }
      )
      return
    }
  }
  
  isProcessing.value = true
  
  try {
    // 调用后端支付API
    const res = await bookingApi.pay(orderId.value, selectedMethod.value)
    if (res.code === 200) {
      // 支付成功，跳转到结果页
      router.push({
        name: 'paymentResult',
        params: { id: orderId.value },
        query: { status: 'success' }
      })
    } else {
      // 后端返回错误
      ElMessage.error(res.message || '支付失败')
      isProcessing.value = false
    }
  } catch (error: any) {
    // 支付失败
    const errorMsg = error.response?.data?.message || error.message || '支付失败，请重试'
    if (errorMsg.includes('余额不足')) {
      ElMessage.error('余额不足，请充值！')
    } else {
      ElMessage.error(errorMsg)
    }
    isProcessing.value = false
  }
}

// 取消支付
const handleCancel = () => {
  router.back()
}

onMounted(async () => {
  await loadOrderInfo()
  startCountdown()
  await walletStore.fetchWallet()
})

// 清理定时器
import { onUnmounted } from 'vue'
onUnmounted(() => {
  if (countdownTimer) clearInterval(countdownTimer)
})
</script>

<template>
  <div class="payment-view">
    <!-- 页面头部 -->
    <div class="payment-header">
      <h1>确认支付</h1>
      <div class="countdown">
        <span class="countdown-label">请在</span>
        <span class="countdown-time">{{ formattedCountdown }}</span>
        <span class="countdown-label">内完成支付</span>
      </div>
    </div>

    <div class="payment-content">
      <!-- 加载中 -->
      <div v-if="isLoading" class="loading-card">
        <div class="loading-spinner"></div>
        <p>正在加载订单信息...</p>
      </div>
      
      <!-- 订单信息 -->
      <div class="order-card" v-else-if="orderInfo">
        <div class="order-header">
          <span class="order-label">订单信息</span>
          <span class="order-number">{{ orderInfo.orderNumber }}</span>
        </div>
        
        <div class="order-details">
          <div class="detail-row">
            <span class="detail-label">寄养机构</span>
            <span class="detail-value">{{ orderInfo.institutionName }}</span>
          </div>
          <div class="detail-row">
            <span class="detail-label">服务套餐</span>
            <span class="detail-value">{{ orderInfo.serviceName }}</span>
          </div>
          <div class="detail-row">
            <span class="detail-label">寄养宠物</span>
            <span class="detail-value">{{ orderInfo.petName }}</span>
          </div>
          <div class="detail-row">
            <span class="detail-label">寄养日期</span>
            <span class="detail-value">{{ orderInfo.startDate }} 至 {{ orderInfo.endDate }}</span>
          </div>
          <div class="detail-row">
            <span class="detail-label">寄养天数</span>
            <span class="detail-value">{{ orderInfo.totalDays }} 天</span>
          </div>
        </div>
        
        <div class="order-total">
          <span class="total-label">应付金额</span>
          <span class="total-price">¥{{ orderInfo.totalPrice.toFixed(2) }}</span>
        </div>
      </div>

      <!-- 支付方式选择 -->
      <div class="payment-methods">
        <h3>选择支付方式</h3>
        
        <div class="method-list">
          <div 
            v-for="method in paymentMethods"
            :key="method.id"
            class="method-item"
            :class="{ selected: selectedMethod === method.id }"
            @click="selectMethod(method.id as PaymentMethod)"
          >
            <div class="method-icon">{{ method.icon }}</div>
            <div class="method-info">
              <span class="method-name">{{ method.name }}</span>
              <span class="method-desc">{{ method.desc }}</span>
            </div>
            <div class="method-check">
              <span v-if="selectedMethod === method.id">✓</span>
            </div>
          </div>
        </div>
      </div>

      <!-- 支付协议 -->
      <div class="payment-agreement">
        <p>
          点击"立即支付"即表示您已阅读并同意
          <a href="#">《服务协议》</a>和<a href="#">《隐私政策》</a>
        </p>
      </div>
    </div>

    <!-- 底部操作栏 -->
    <div class="payment-footer">
      <div class="footer-price">
        <span class="price-label">应付：</span>
        <span class="price-value" v-if="orderInfo">¥{{ orderInfo.totalPrice.toFixed(2) }}</span>
      </div>
      <div class="footer-actions">
        <AppButton type="outline" @click="handleCancel">取消</AppButton>
        <AppButton 
          type="primary" 
          :loading="isProcessing"
          @click="handlePayment"
        >
          {{ isProcessing ? '支付中...' : '立即支付' }}
        </AppButton>
      </div>
    </div>
  </div>
</template>

<style lang="scss" scoped>
@import '@/styles/variables.scss';

.payment-view {
  min-height: 100vh;
  background: var(--color-surface);
  padding-bottom: 100px;
}

// 加载状态
.loading-card {
  background: white;
  border-radius: var(--radius-xl);
  padding: 48px 24px;
  margin-bottom: 24px;
  box-shadow: var(--shadow-md);
  text-align: center;
  
  p {
    margin: 16px 0 0;
    color: var(--color-text-secondary);
  }
}

.loading-spinner {
  width: 40px;
  height: 40px;
  border: 3px solid var(--color-border);
  border-top-color: var(--color-primary);
  border-radius: 50%;
  margin: 0 auto;
  animation: spin 1s linear infinite;
}

@keyframes spin {
  to { transform: rotate(360deg); }
}

// 页面头部
.payment-header {
  background: linear-gradient(135deg, var(--color-primary) 0%, #FF8F5C 100%);
  color: white;
  padding: 32px 24px;
  text-align: center;
  
  h1 {
    font-size: 24px;
    font-weight: 600;
    margin: 0 0 16px;
  }
}

.countdown {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  
  .countdown-label {
    font-size: 14px;
    opacity: 0.9;
  }
  
  .countdown-time {
    font-size: 24px;
    font-weight: 700;
    font-family: var(--font-mono, monospace);
    background: rgba(255, 255, 255, 0.2);
    padding: 4px 12px;
    border-radius: 8px;
  }
}

// 内容区域
.payment-content {
  padding: 24px;
  max-width: 600px;
  margin: 0 auto;
}

// 订单卡片
.order-card {
  background: white;
  border-radius: var(--radius-xl);
  padding: 24px;
  margin-bottom: 24px;
  box-shadow: var(--shadow-md);
}

.order-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding-bottom: 16px;
  border-bottom: 1px solid var(--color-border);
  margin-bottom: 16px;
  
  .order-label {
    font-size: 16px;
    font-weight: 600;
  }
  
  .order-number {
    font-size: 13px;
    color: var(--color-text-secondary);
    font-family: var(--font-mono, monospace);
  }
}

.order-details {
  margin-bottom: 16px;
}

.detail-row {
  display: flex;
  justify-content: space-between;
  padding: 10px 0;
  
  .detail-label {
    font-size: 14px;
    color: var(--color-text-secondary);
  }
  
  .detail-value {
    font-size: 14px;
    font-weight: 500;
    color: var(--color-text-primary);
  }
}

.order-total {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding-top: 16px;
  border-top: 1px dashed var(--color-border);
  
  .total-label {
    font-size: 16px;
    font-weight: 600;
  }
  
  .total-price {
    font-size: 28px;
    font-weight: 700;
    color: var(--color-primary);
  }
}

// 支付方式
.payment-methods {
  background: white;
  border-radius: var(--radius-xl);
  padding: 24px;
  margin-bottom: 24px;
  box-shadow: var(--shadow-md);
  
  h3 {
    font-size: 16px;
    font-weight: 600;
    margin: 0 0 16px;
  }
}

.method-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.method-item {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 16px;
  border: 2px solid var(--color-border);
  border-radius: var(--radius-lg);
  cursor: pointer;
  transition: all 0.2s ease;
  
  &:hover {
    border-color: var(--color-primary-light);
  }
  
  &.selected {
    border-color: var(--color-primary);
    background: var(--color-primary-light);
  }
}

.method-icon {
  width: 48px;
  height: 48px;
  background: var(--color-surface);
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 24px;
}

.method-info {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 2px;
  
  .method-name {
    font-size: 16px;
    font-weight: 600;
  }
  
  .method-desc {
    font-size: 13px;
    color: var(--color-text-secondary);
  }
}

.method-check {
  width: 24px;
  height: 24px;
  border-radius: 50%;
  background: var(--color-primary);
  color: white;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 14px;
  opacity: 0;
  
  .method-item.selected & {
    opacity: 1;
  }
}

// 支付协议
.payment-agreement {
  text-align: center;
  
  p {
    font-size: 12px;
    color: var(--color-text-secondary);
    margin: 0;
    
    a {
      color: var(--color-primary);
      
      &:hover {
        text-decoration: underline;
      }
    }
  }
}

// 底部操作栏
.payment-footer {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px 24px;
  background: white;
  border-top: 1px solid var(--color-border);
  box-shadow: 0 -4px 12px rgba(0, 0, 0, 0.05);
}

.footer-price {
  display: flex;
  align-items: baseline;
  gap: 4px;
  
  .price-label {
    font-size: 14px;
    color: var(--color-text-secondary);
  }
  
  .price-value {
    font-size: 24px;
    font-weight: 700;
    color: var(--color-primary);
  }
}

.footer-actions {
  display: flex;
  gap: 12px;
}
</style>
