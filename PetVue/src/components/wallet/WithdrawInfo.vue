<template>
  <div class="withdraw-info">
    <div class="info-row">
      <span class="label">提现金额</span>
      <span class="value">¥{{ formatAmount(amount) }}</span>
    </div>
    <div class="info-row">
      <span class="label">手续费 ({{ feeRate }}%)</span>
      <span class="value fee">-¥{{ formatAmount(fee) }}</span>
    </div>
    <div class="info-row">
      <span class="label">实际到账</span>
      <span class="value actual">¥{{ formatAmount(actualAmount) }}</span>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { calculateWithdrawalFee, calculateActualAmount, formatAmount, yuanToFen, DEFAULT_FEE_CONFIG } from '@/utils/feeCalculator'

const props = defineProps<{
  amount: number  // 金额（分）
}>()

const feeRate = computed(() => DEFAULT_FEE_CONFIG.rate * 100)

const fee = computed(() => {
  if (props.amount <= 0) return 0
  return calculateWithdrawalFee(props.amount)
})

const actualAmount = computed(() => {
  if (props.amount <= 0) return 0
  return calculateActualAmount(props.amount, fee.value)
})
</script>

<style scoped lang="scss">
.withdraw-info {
  background: var(--el-fill-color-light, #f5f7fa);
  border-radius: 12px;
  padding: 16px;
  
  .info-row {
    display: flex;
    justify-content: space-between;
    padding: 8px 0;
    
    &:not(:last-child) {
      border-bottom: 1px dashed var(--el-border-color-lighter, #ebeef5);
    }
    
    .label {
      color: var(--el-text-color-secondary);
    }
    
    .value {
      font-weight: 500;
      
      &.fee {
        color: var(--el-color-warning);
      }
      
      &.actual {
        color: var(--el-color-success);
        font-size: 18px;
        font-weight: 600;
      }
    }
  }
}
</style>
