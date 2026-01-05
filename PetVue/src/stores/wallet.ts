import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import type {
  Wallet,
  WalletTransaction,
  Withdrawal,
  WithdrawalAccount,
  RechargeOrder,
  IncomeStatistics,
  TransactionType,
  TransactionQueryParams
} from '@/types/wallet'
import { walletApi } from '@/api/wallet'

export const useWalletStore = defineStore('wallet', () => {
  // State
  const wallet = ref<Wallet | null>(null)
  const transactions = ref<WalletTransaction[]>([])
  const withdrawals = ref<Withdrawal[]>([])
  const withdrawalAccounts = ref<WithdrawalAccount[]>([])
  const incomeStatistics = ref<IncomeStatistics | null>(null)
  const loading = ref(false)
  const transactionsTotal = ref(0)
  const withdrawalsTotal = ref(0)

  // Getters
  const balance = computed(() => wallet.value?.balance ?? 0)
  const frozenBalance = computed(() => wallet.value?.frozenBalance ?? 0)
  const availableBalance = computed(() => balance.value)
  const totalBalance = computed(() => balance.value + frozenBalance.value)

  const defaultAccount = computed(() => 
    withdrawalAccounts.value.find(a => a.isDefault)
  )

  const bankAccounts = computed(() => 
    withdrawalAccounts.value.filter(a => a.type === 'bank_card')
  )

  const alipayAccounts = computed(() => 
    withdrawalAccounts.value.filter(a => a.type === 'alipay')
  )

  const pendingWithdrawals = computed(() => 
    withdrawals.value.filter(w => w.status === 'pending' || w.status === 'processing')
  )

  const transactionsByType = computed(() => (type: TransactionType) => 
    transactions.value.filter(t => t.type === type)
  )

  // Actions
  async function fetchWallet() {
    loading.value = true
    try {
      const res = await walletApi.getWallet()
      if (res.code === 200) {
        wallet.value = res.data
      }
    } finally {
      loading.value = false
    }
  }

  async function createRecharge(amount: number, paymentMethod: 'alipay' | 'wechat'): Promise<RechargeOrder | null> {
    loading.value = true
    try {
      const res = await walletApi.createRechargeOrder({ amount, paymentMethod })
      if (res.code === 200) {
        return res.data
      }
      return null
    } finally {
      loading.value = false
    }
  }


  async function createWithdrawal(
    amount: number,
    accountId: string,
    password: string
  ): Promise<Withdrawal | null> {
    loading.value = true
    try {
      const res = await walletApi.createWithdrawal({ amount, accountId, password })
      if (res.code === 200) {
        withdrawals.value.unshift(res.data)
        // 更新钱包余额
        if (wallet.value) {
          wallet.value.balance -= res.data.amount
          wallet.value.frozenBalance += res.data.amount
        }
        return res.data
      }
      return null
    } finally {
      loading.value = false
    }
  }

  async function fetchTransactions(params?: TransactionQueryParams) {
    loading.value = true
    try {
      const res = await walletApi.getTransactions(params)
      if (res.code === 200) {
        transactions.value = res.data.list
        transactionsTotal.value = res.data.pagination.total
      }
    } finally {
      loading.value = false
    }
  }

  async function fetchWithdrawals(params?: { page?: number; pageSize?: number }) {
    loading.value = true
    try {
      const res = await walletApi.getWithdrawals(params)
      if (res.code === 200) {
        withdrawals.value = res.data.list
        withdrawalsTotal.value = res.data.pagination.total
      }
    } finally {
      loading.value = false
    }
  }

  async function fetchWithdrawalAccounts() {
    loading.value = true
    try {
      const res = await walletApi.getWithdrawalAccounts()
      if (res.code === 200) {
        withdrawalAccounts.value = res.data
      }
    } finally {
      loading.value = false
    }
  }

  async function addWithdrawalAccount(data: {
    type: 'bank_card' | 'alipay'
    accountName: string
    accountNumber: string
    bankName?: string
  }): Promise<WithdrawalAccount | null> {
    loading.value = true
    try {
      const res = await walletApi.addWithdrawalAccount(data)
      if (res.code === 200) {
        withdrawalAccounts.value.push(res.data)
        return res.data
      }
      return null
    } finally {
      loading.value = false
    }
  }

  async function deleteWithdrawalAccount(id: string): Promise<boolean> {
    loading.value = true
    try {
      const res = await walletApi.deleteWithdrawalAccount(id)
      if (res.code === 200) {
        const index = withdrawalAccounts.value.findIndex(a => a.id === id)
        if (index > -1) {
          withdrawalAccounts.value.splice(index, 1)
        }
        return true
      }
      return false
    } finally {
      loading.value = false
    }
  }

  async function setDefaultAccount(id: string): Promise<boolean> {
    loading.value = true
    try {
      const res = await walletApi.setDefaultAccount(id)
      if (res.code === 200) {
        withdrawalAccounts.value.forEach(a => {
          a.isDefault = a.id === id
        })
        return true
      }
      return false
    } finally {
      loading.value = false
    }
  }

  async function fetchIncomeStatistics(period: 'day' | 'week' | 'month') {
    loading.value = true
    try {
      const res = await walletApi.getIncomeStatistics(period)
      if (res.code === 200) {
        incomeStatistics.value = res.data
      }
    } finally {
      loading.value = false
    }
  }

  function updateWalletBalance(newBalance: number, newFrozenBalance?: number) {
    if (wallet.value) {
      wallet.value.balance = newBalance
      if (newFrozenBalance !== undefined) {
        wallet.value.frozenBalance = newFrozenBalance
      }
    }
  }

  function addTransaction(transaction: WalletTransaction) {
    transactions.value.unshift(transaction)
    transactionsTotal.value++
  }

  function clearWallet() {
    wallet.value = null
    transactions.value = []
    withdrawals.value = []
    withdrawalAccounts.value = []
    incomeStatistics.value = null
  }

  return {
    // State
    wallet,
    transactions,
    withdrawals,
    withdrawalAccounts,
    incomeStatistics,
    loading,
    transactionsTotal,
    withdrawalsTotal,
    // Getters
    balance,
    frozenBalance,
    availableBalance,
    totalBalance,
    defaultAccount,
    bankAccounts,
    alipayAccounts,
    pendingWithdrawals,
    transactionsByType,
    // Actions
    fetchWallet,
    createRecharge,
    createWithdrawal,
    fetchTransactions,
    fetchWithdrawals,
    fetchWithdrawalAccounts,
    addWithdrawalAccount,
    deleteWithdrawalAccount,
    setDefaultAccount,
    fetchIncomeStatistics,
    updateWalletBalance,
    addTransaction,
    clearWallet
  }
})
