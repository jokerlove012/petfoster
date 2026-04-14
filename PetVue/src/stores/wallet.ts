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
      if (res && res.code === 200 && res.data) {
        wallet.value = res.data
      } else {
        console.warn('获取钱包信息失败:', res)
      }
    } catch (error) {
      console.error('获取钱包信息错误:', error)
    } finally {
      loading.value = false
    }
  }

  async function createRecharge(amount: number, paymentMethod: 'alipay' | 'wechat'): Promise<RechargeOrder | null> {
    loading.value = true
    try {
      const res = await walletApi.createRechargeOrder({ amount, paymentMethod })
      if (res.code === 200) {
        const order = res.data.order
        return order
      }
      return null
    } finally {
      loading.value = false
    }
  }

  async function confirmRecharge(orderId: string): Promise<boolean> {
    loading.value = true
    try {
      const res = await walletApi.confirmRecharge(orderId)
      if (res.code === 200) {
        await fetchWallet()
        await fetchTransactions({ pageSize: 5 })
        return true
      }
      return false
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
        const withdrawal = res.data.withdrawal
        withdrawals.value.unshift(withdrawal)
        // 更新钱包余额
        if (wallet.value) {
          wallet.value.balance -= withdrawal.amount
          wallet.value.frozenBalance += withdrawal.amount
        }
        return withdrawal
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
      if (res && res.code === 200 && res.data) {
        if (Array.isArray(res.data)) {
          transactions.value = res.data
          transactionsTotal.value = res.data.length
        } else if ((res.data as any).list) {
          const data = res.data as any
          transactions.value = data.list || []
          transactionsTotal.value = data.pagination?.total ?? data.total ?? data.list?.length ?? 0
        } else {
          transactions.value = []
          transactionsTotal.value = 0
        }
      } else {
        console.warn('获取交易记录失败:', res)
        transactions.value = []
        transactionsTotal.value = 0
      }
    } catch (error) {
      console.error('获取交易记录错误:', error)
      transactions.value = []
      transactionsTotal.value = 0
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
        const account = res.data.account
        withdrawalAccounts.value.push(account)
        return account
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

  async function fetchIncomeStatistics() {
    loading.value = true
    try {
      const res = await walletApi.getIncomeStatistics()
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
    confirmRecharge,
    createWithdrawal,
    fetchTransactions,
    fetchWithdrawalAccounts,
    addWithdrawalAccount,
    deleteWithdrawalAccount,
    setDefaultAccount,
    fetchIncomeStatistics
  }
})
