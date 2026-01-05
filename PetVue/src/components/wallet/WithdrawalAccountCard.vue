<template>
  <div :class="['account-card', { selected: isSelected }]" @click="$emit('select')">
    <div :class="['account-icon', account.type === 'bank_card' ? 'bank' : 'alipay']">
      <el-icon><CreditCard v-if="account.type === 'bank_card'" /><Wallet v-else /></el-icon>
    </div>
    <div class="account-info">
      <div class="name">
        {{ account.bankName || '支付宝' }} - {{ account.accountName }}
      </div>
      <div class="number">{{ account.accountNumber }}</div>
    </div>
    <span v-if="account.isDefault" class="default-badge">默认</span>
    <el-dropdown v-if="showActions" trigger="click" @command="handleCommand" @click.stop>
      <el-button type="text" :icon="MoreFilled" />
      <template #dropdown>
        <el-dropdown-menu>
          <el-dropdown-item v-if="!account.isDefault" command="setDefault">设为默认</el-dropdown-item>
          <el-dropdown-item command="delete" style="color: var(--el-color-danger)">删除</el-dropdown-item>
        </el-dropdown-menu>
      </template>
    </el-dropdown>
  </div>
</template>

<script setup lang="ts">
import { CreditCard, Wallet, MoreFilled } from '@element-plus/icons-vue'
import type { WithdrawalAccount } from '@/types/wallet'

defineProps<{
  account: WithdrawalAccount
  isSelected?: boolean
  showActions?: boolean
}>()

const emit = defineEmits<{
  (e: 'select'): void
  (e: 'setDefault'): void
  (e: 'delete'): void
}>()

function handleCommand(command: string) {
  if (command === 'setDefault') {
    emit('setDefault')
  } else if (command === 'delete') {
    emit('delete')
  }
}
</script>

<style scoped lang="scss">
.account-card {
  display: flex;
  align-items: center;
  padding: 16px;
  background: var(--el-bg-color);
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
}

.account-icon {
  width: 48px;
  height: 48px;
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: 12px;
  font-size: 24px;
  
  &.bank {
    background: #e3f2fd;
    color: #1976d2;
  }
  
  &.alipay {
    background: #e8f5e9;
    color: #1976d2;
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
    color: var(--el-text-color-secondary);
    margin-top: 2px;
  }
}

.default-badge {
  padding: 4px 8px;
  background: var(--el-color-primary);
  color: white;
  font-size: 11px;
  border-radius: 4px;
  margin-right: 8px;
}
</style>
