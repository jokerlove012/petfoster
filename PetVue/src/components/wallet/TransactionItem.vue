<template>
  <div class="transaction-item">
    <div :class="['transaction-icon', iconClass]">
      <el-icon><component :is="iconComponent" /></el-icon>
    </div>
    <div class="transaction-info">
      <div class="title">{{ transaction.description }}</div>
      <div class="time">{{ formatTime(transaction.createdAt) }}</div>
    </div>
    <div :class="['transaction-amount', amountClass]">
      {{ amountPrefix }}¥{{ formatAmount(transaction.amount) }}
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { Plus, Minus, Wallet, ShoppingCart, RefreshRight } from '@element-plus/icons-vue'
import type { WalletTransaction } from '@/types/wallet'
import { formatAmount } from '@/utils/feeCalculator'

const props = defineProps<{
  transaction: WalletTransaction
}>()

const iconClass = computed(() => {
  const incomeTypes = ['recharge', 'income', 'refund']
  return incomeTypes.includes(props.transaction.type) ? 'income' : 'expense'
})

const iconComponent = computed(() => {
  const icons: Record<string, any> = {
    recharge: Plus,
    withdrawal: Wallet,
    income: ShoppingCart,
    expense: Minus,
    refund: RefreshRight
  }
  return icons[props.transaction.type] || Minus
})

const amountClass = computed(() => {
  const incomeTypes = ['recharge', 'income', 'refund']
  return incomeTypes.includes(props.transaction.type) ? 'positive' : 'negative'
})

const amountPrefix = computed(() => {
  const incomeTypes = ['recharge', 'income', 'refund']
  return incomeTypes.includes(props.transaction.type) ? '+' : '-'
})

function formatTime(dateStr: string): string {
  const date = new Date(dateStr)
  return date.toLocaleString('zh-CN', {
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit'
  })
}
</script>

<style scoped lang="scss">
.transaction-item {
  display: flex;
  align-items: center;
  padding: 16px;
  border-bottom: 1px solid var(--el-border-color-lighter, #ebeef5);
}

.transaction-icon {
  width: 44px;
  height: 44px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: 12px;
  font-size: 20px;
  
  &.income {
    background: rgba(34, 197, 94, 0.1);
    color: #22c55e;
  }
  
  &.expense {
    background: rgba(239, 68, 68, 0.1);
    color: #ef4444;
  }
}

.transaction-info {
  flex: 1;
  
  .title {
    font-weight: 500;
    font-size: 15px;
    color: var(--el-text-color-primary);
  }
  
  .time {
    font-size: 13px;
    color: var(--el-text-color-secondary);
    margin-top: 2px;
  }
}

.transaction-amount {
  font-weight: 600;
  font-size: 16px;
  
  &.positive {
    color: #22c55e;
  }
  
  &.negative {
    color: var(--el-text-color-primary);
  }
}
</style>
