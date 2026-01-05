<template>
  <div class="withdraw-page">
    <div class="page-header">
      <el-button :icon="ArrowLeft" text @click="goBack" />
      <h1>提现</h1>
    </div>
    
    <div class="balance-info">
      可提现余额: <span class="amount">¥{{ formatAmount(walletStore.balance) }}</span>
    </div>
    
    <div class="section">
      <h3>提现金额</h3>
      <el-input
        v-model="withdrawAmount"
        type="number"
        placeholder="请输入提现金额"
        size="large"
        :min="10"
      >
        <template #prepend>¥</template>
        <template #append>
          <el-button text @click="withdrawAll">全部提现</el-button>
        </template>
      </el-input>
      <div class="amount-tips">最低提现金额 10 元</div>
    </div>
    
    <WithdrawInfo v-if="withdrawAmountInFen > 0" :amount="withdrawAmountInFen" />
    
    <div class="section">
      <div class="section-header">
        <h3>提现到</h3>
        <el-button type="text" @click="goToAccounts">管理账户</el-button>
      </div>
      <div class="accounts-list" v-if="walletStore.withdrawalAccounts.length">
        <WithdrawalAccountCard
          v-for="account in walletStore.withdrawalAccounts"
          :key="account.id"
          :account="account"
          :is-selected="selectedAccountId === account.id"
          @select="selectedAccountId = account.id"
        />
      </div>
      <el-empty v-else description="请先添加提现账户">
        <el-button type="primary" @click="goToAccounts">添加账户</el-button>
      </el-empty>
    </div>
    
    <div class="submit-area">
      <el-button
        type="primary"
        size="large"
        :loading="loading"
        :disabled="!canSubmit"
        @click="showPasswordDialog = true"
      >
        确认提现
      </el-button>
    </div>
    
    <!-- 密码验证弹窗 -->
    <el-dialog v-model="showPasswordDialog" title="输入支付密码" width="90%" :max-width="400">
      <el-input
        v-model="password"
        type="password"
        placeholder="请输入支付密码"
        show-password
      />
      <template #footer>
        <el-button @click="showPasswordDialog = false">取消</el-button>
        <el-button type="primary" :loading="loading" @click="handleWithdraw">确认</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ArrowLeft } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { useWalletStore } from '@/stores/wallet'
import { formatAmount, yuanToFen, fenToYuan, validateWithdrawalAmount } from '@/utils/feeCalculator'
import WithdrawInfo from '@/components/wallet/WithdrawInfo.vue'
import WithdrawalAccountCard from '@/components/wallet/WithdrawalAccountCard.vue'

const router = useRouter()
const walletStore = useWalletStore()

const withdrawAmount = ref('')
const selectedAccountId = ref('')
const password = ref('')
const showPasswordDialog = ref(false)
const loading = ref(false)

const withdrawAmountInFen = computed(() => {
  const val = parseFloat(withdrawAmount.value)
  return isNaN(val) ? 0 : yuanToFen(val)
})

const canSubmit = computed(() => {
  return withdrawAmountInFen.value >= 1000 && selectedAccountId.value
})

onMounted(async () => {
  await walletStore.fetchWallet()
  await walletStore.fetchWithdrawalAccounts()
  
  // 默认选中默认账户
  const defaultAccount = walletStore.defaultAccount
  if (defaultAccount) {
    selectedAccountId.value = defaultAccount.id
  }
})

function goBack() {
  router.back()
}

function goToAccounts() {
  router.push('/wallet/accounts')
}

function withdrawAll() {
  withdrawAmount.value = fenToYuan(walletStore.balance).toString()
}

async function handleWithdraw() {
  if (!password.value) {
    ElMessage.warning('请输入支付密码')
    return
  }
  
  const validation = validateWithdrawalAmount(withdrawAmountInFen.value, walletStore.balance)
  if (!validation.valid) {
    ElMessage.warning(validation.message)
    return
  }
  
  loading.value = true
  try {
    const withdrawal = await walletStore.createWithdrawal(
      parseFloat(withdrawAmount.value),
      selectedAccountId.value,
      password.value
    )
    
    if (withdrawal) {
      ElMessage.success('提现申请已提交')
      showPasswordDialog.value = false
      router.push('/wallet')
    } else {
      ElMessage.error('提现申请失败')
    }
  } finally {
    loading.value = false
  }
}
</script>

<style scoped lang="scss">
.withdraw-page {
  padding: 20px;
  padding-bottom: 100px;
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
  
  .section-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 12px;
  }
  
  h3 {
    font-size: 16px;
    font-weight: 600;
    margin-bottom: 12px;
  }
  
  .amount-tips {
    font-size: 13px;
    color: var(--el-text-color-secondary);
    margin-top: 8px;
  }
}

.accounts-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
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
