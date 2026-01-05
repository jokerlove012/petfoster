<template>
  <div class="wallet-page">
    <div class="page-header">
      <h1>我的钱包</h1>
    </div>
    
    <WalletOverview
      :wallet="walletStore.wallet"
      @recharge="goToRecharge"
      @withdraw="goToWithdraw"
    />
    
    <div class="section">
      <div class="section-header">
        <h3>最近交易</h3>
        <el-button type="text" @click="goToTransactions">查看全部</el-button>
      </div>
      <div class="transaction-list" v-if="walletStore.transactions.length">
        <TransactionItem
          v-for="tx in recentTransactions"
          :key="tx.id"
          :transaction="tx"
        />
      </div>
      <el-empty v-else description="暂无交易记录" />
    </div>
    
    <div class="quick-links">
      <div class="link-item" @click="goToAccounts">
        <el-icon><CreditCard /></el-icon>
        <span>提现账户</span>
        <el-icon><ArrowRight /></el-icon>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { CreditCard, ArrowRight } from '@element-plus/icons-vue'
import { useWalletStore } from '@/stores/wallet'
import WalletOverview from '@/components/wallet/WalletOverview.vue'
import TransactionItem from '@/components/wallet/TransactionItem.vue'

const router = useRouter()
const walletStore = useWalletStore()

const recentTransactions = computed(() => walletStore.transactions.slice(0, 5))

onMounted(async () => {
  await walletStore.fetchWallet()
  await walletStore.fetchTransactions({ pageSize: 5 })
})

function goToRecharge() {
  router.push('/wallet/recharge')
}

function goToWithdraw() {
  router.push('/wallet/withdraw')
}

function goToTransactions() {
  router.push('/wallet/transactions')
}

function goToAccounts() {
  router.push('/wallet/accounts')
}
</script>

<style scoped lang="scss">
.wallet-page {
  padding: 20px;
  max-width: 600px;
  margin: 0 auto;
}

.page-header {
  margin-bottom: 20px;
  
  h1 {
    font-size: 24px;
    font-weight: 600;
  }
}

.section {
  margin-top: 24px;
  background: var(--el-bg-color);
  border-radius: 12px;
  padding: 16px;
  
  .section-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 12px;
    
    h3 {
      font-size: 16px;
      font-weight: 600;
    }
  }
}

.quick-links {
  margin-top: 24px;
  
  .link-item {
    display: flex;
    align-items: center;
    padding: 16px;
    background: var(--el-bg-color);
    border-radius: 12px;
    cursor: pointer;
    transition: background 0.2s;
    
    &:hover {
      background: var(--el-fill-color-light);
    }
    
    span {
      flex: 1;
      margin-left: 12px;
      font-size: 15px;
    }
  }
}
</style>
