<template>
  <div class="recharge-page">
    <div class="page-header">
      <el-button :icon="ArrowLeft" text @click="goBack" />
      <h1>充值</h1>
    </div>
    
    <div class="balance-info">
      当前余额: <span class="amount">¥{{ formatAmount(walletStore.balance) }}</span>
    </div>
    
    <div class="section">
      <h3>选择充值金额</h3>
      <AmountSelector v-model="rechargeAmount" />
    </div>
    
    <div class="section">
      <h3>选择支付方式</h3>
      <div class="payment-methods">
        <div
          :class="['payment-option', { selected: paymentMethod === 'alipay' }]"
          @click="paymentMethod = 'alipay'"
        >
          <div class="icon alipay">
            <el-icon><Wallet /></el-icon>
          </div>
          <span>支付宝</span>
          <el-icon v-if="paymentMethod === 'alipay'" class="check"><Check /></el-icon>
        </div>
        <div
          :class="['payment-option', { selected: paymentMethod === 'wechat' }]"
          @click="paymentMethod = 'wechat'"
        >
          <div class="icon wechat">
            <el-icon><ChatDotRound /></el-icon>
          </div>
          <span>微信支付</span>
          <el-icon v-if="paymentMethod === 'wechat'" class="check"><Check /></el-icon>
        </div>
      </div>
    </div>
    
    <div class="submit-area">
      <el-button type="primary" size="large" :loading="loading" @click="handleRecharge">
        确认充值 ¥{{ rechargeAmount }}
      </el-button>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ArrowLeft, Wallet, ChatDotRound, Check } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { useWalletStore } from '@/stores/wallet'
import { formatAmount, yuanToFen, validateRechargeAmount } from '@/utils/feeCalculator'
import AmountSelector from '@/components/wallet/AmountSelector.vue'
import type { PaymentMethod } from '@/types/wallet'

const router = useRouter()
const route = useRoute()
const walletStore = useWalletStore()

const rechargeAmount = ref(100)
const paymentMethod = ref<PaymentMethod>('alipay')
const loading = ref(false)

// 获取返回地址（从支付页面跳转过来时会带上）
const returnTo = route.query.returnTo as string | undefined

function goBack() {
  if (returnTo) {
    router.push(returnTo)
  } else {
    router.back()
  }
}

async function handleRecharge() {
  const amountInFen = yuanToFen(rechargeAmount.value)
  const validation = validateRechargeAmount(amountInFen)
  
  if (!validation.valid) {
    ElMessage.warning(validation.message)
    return
  }
  
  loading.value = true
  try {
    const order = await walletStore.createRecharge(rechargeAmount.value, paymentMethod.value)
    if (order) {
      ElMessage.success('充值订单创建成功，请完成支付')
      // 模拟支付成功
      setTimeout(() => {
        ElMessage.success('充值成功！')
        walletStore.fetchWallet()
        // 如果有返回地址，返回到支付页面继续支付
        if (returnTo) {
          router.push(returnTo)
        } else {
          router.push('/wallet')
        }
      }, 1500)
    } else {
      ElMessage.error('创建充值订单失败')
    }
  } finally {
    loading.value = false
  }
}
</script>

<style scoped lang="scss">
.recharge-page {
  padding: 20px;
  max-width: 600px;
  margin: 0 auto;
}

.page-header {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 20px;
  
  h1 {
    font-size: 20px;
    font-weight: 600;
  }
}

.balance-info {
  padding: 16px;
  background: var(--el-fill-color-light);
  border-radius: 12px;
  margin-bottom: 20px;
  
  .amount {
    font-weight: 600;
    color: var(--el-color-primary);
  }
}

.section {
  margin-bottom: 24px;
  
  h3 {
    font-size: 16px;
    font-weight: 600;
    margin-bottom: 16px;
  }
}

.payment-methods {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.payment-option {
  display: flex;
  align-items: center;
  padding: 16px;
  border: 2px solid var(--el-border-color);
  border-radius: 12px;
  cursor: pointer;
  transition: all 0.2s;
  
  &:hover {
    border-color: var(--el-color-primary-light-5);
  }
  
  &.selected {
    border-color: var(--el-color-primary);
    background: var(--el-color-primary-light-9);
  }
  
  .icon {
    width: 40px;
    height: 40px;
    border-radius: 8px;
    display: flex;
    align-items: center;
    justify-content: center;
    font-size: 20px;
    margin-right: 12px;
    
    &.alipay {
      background: #e8f5e9;
      color: #1976d2;
    }
    
    &.wechat {
      background: #e8f5e9;
      color: #07c160;
    }
  }
  
  span {
    flex: 1;
    font-weight: 500;
  }
  
  .check {
    color: var(--el-color-primary);
  }
}

.submit-area {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  padding: 16px 20px;
  background: var(--el-bg-color);
  border-top: 1px solid var(--el-border-color-lighter);
  
  .el-button {
    width: 100%;
  }
}
</style>
