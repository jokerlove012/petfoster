<template>
  <div class="transactions-page">
    <div class="page-header">
      <el-button :icon="ArrowLeft" text @click="goBack" />
      <h1>交易记录</h1>
    </div>
    
    <div class="filters">
      <el-select v-model="filterType" placeholder="交易类型" clearable @change="loadTransactions">
        <el-option label="全部" value="" />
        <el-option label="充值" value="recharge" />
        <el-option label="提现" value="withdrawal" />
        <el-option label="收入" value="income" />
        <el-option label="支出" value="expense" />
        <el-option label="退款" value="refund" />
      </el-select>
      <el-date-picker
        v-model="dateRange"
        type="daterange"
        range-separator="至"
        start-placeholder="开始日期"
        end-placeholder="结束日期"
        value-format="YYYY-MM-DD"
        @change="loadTransactions"
      />
    </div>
    
    <div class="transaction-list" v-loading="walletStore.loading">
      <template v-if="walletStore.transactions.length">
        <TransactionItem
          v-for="tx in walletStore.transactions"
          :key="tx.id"
          :transaction="tx"
        />
      </template>
      <el-empty v-else description="暂无交易记录" />
    </div>
    
    <div class="pagination" v-if="walletStore.transactionsTotal > pageSize">
      <el-pagination
        v-model:current-page="currentPage"
        :page-size="pageSize"
        :total="walletStore.transactionsTotal"
        layout="prev, pager, next"
        @current-change="loadTransactions"
      />
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ArrowLeft } from '@element-plus/icons-vue'
import { useWalletStore } from '@/stores/wallet'
import TransactionItem from '@/components/wallet/TransactionItem.vue'
import type { TransactionType } from '@/types/wallet'

const router = useRouter()
const walletStore = useWalletStore()

const filterType = ref<TransactionType | ''>('')
const dateRange = ref<[string, string] | null>(null)
const currentPage = ref(1)
const pageSize = 20

onMounted(() => {
  loadTransactions()
})

function goBack() {
  router.back()
}

async function loadTransactions() {
  await walletStore.fetchTransactions({
    page: currentPage.value,
    pageSize,
    type: filterType.value || undefined,
    startDate: dateRange.value?.[0],
    endDate: dateRange.value?.[1]
  })
}
</script>

<style scoped lang="scss">
.transactions-page {
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

.filters {
  display: flex;
  gap: 12px;
  margin-bottom: 20px;
  flex-wrap: wrap;
  
  .el-select {
    width: 120px;
  }
}

.transaction-list {
  background: var(--el-bg-color);
  border-radius: 12px;
  min-height: 200px;
}

.pagination {
  display: flex;
  justify-content: center;
  margin-top: 20px;
}
</style>
