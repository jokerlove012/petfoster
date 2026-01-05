<template>
  <div class="wallet-card">
    <div class="wallet-balance">
      <span class="label">可用余额</span>
      <div class="amount">
        <span class="currency">¥</span>{{ formatAmount(wallet?.balance ?? 0) }}
      </div>
    </div>
    <div class="wallet-frozen" v-if="wallet?.frozenBalance">
      冻结金额: ¥{{ formatAmount(wallet.frozenBalance) }}
    </div>
    <div class="wallet-actions">
      <button class="action-btn" @click="$emit('recharge')">
        <el-icon><Plus /></el-icon>
        充值
      </button>
      <button class="action-btn" @click="$emit('withdraw')">
        <el-icon><Wallet /></el-icon>
        提现
      </button>
    </div>
  </div>
</template>

<script setup lang="ts">
import { Plus, Wallet } from '@element-plus/icons-vue'
import type { Wallet as WalletType } from '@/types/wallet'
import { formatAmount } from '@/utils/feeCalculator'

defineProps<{
  wallet: WalletType | null
}>()

defineEmits<{
  (e: 'recharge'): void
  (e: 'withdraw'): void
}>()
</script>

<style scoped lang="scss">
.wallet-card {
  background: linear-gradient(135deg, var(--color-primary, #FF6B35) 0%, #FF8F5C 100%);
  border-radius: 16px;
  padding: 24px;
  color: white;
}

.wallet-balance {
  .label {
    font-size: 14px;
    opacity: 0.9;
  }
  
  .amount {
    font-size: 36px;
    font-weight: 700;
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
    border-radius: 12px;
    color: white;
    font-weight: 600;
    cursor: pointer;
    transition: all 0.2s;
    display: flex;
    align-items: center;
    justify-content: center;
    gap: 6px;
    
    &:hover {
      background: rgba(255, 255, 255, 0.3);
    }
  }
}
</style>
