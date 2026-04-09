import api from './index'
import type { ApiResponse, PaginatedData } from '@/types/common'
import type {
  Wallet,
  WalletTransaction,
  Withdrawal,
  WithdrawalAccount,
  RechargeOrder,
  IncomeStatistics,
  CreateRechargeData,
  CreateWithdrawalData,
  AddWithdrawalAccountData,
  TransactionQueryParams
} from '@/types/wallet'

export const walletApi = {
  getWallet(): Promise<ApiResponse<Wallet>> {
    return api.get('/wallet')
  },

  createRechargeOrder(data: CreateRechargeData): Promise<ApiResponse<{ order: RechargeOrder }>> {
    return api.post('/wallet/recharge', data)
  },

  confirmRecharge(orderId: string): Promise<ApiResponse<void>> {
    return api.post(`/wallet/recharge/${orderId}/confirm`)
  },

  createWithdrawal(data: CreateWithdrawalData): Promise<ApiResponse<{ withdrawal: Withdrawal }>> {
    return api.post('/wallet/withdraw', data)
  },

  getTransactions(params?: TransactionQueryParams): Promise<ApiResponse<WalletTransaction[]>> {
    const searchParams = new URLSearchParams()
    if (params?.page) searchParams.append('page', params.page.toString())
    if (params?.pageSize) searchParams.append('pageSize', params.pageSize.toString())
    if (params?.type) searchParams.append('type', params.type)
    return api.get(`/wallet/transactions?${searchParams.toString()}`)
  },

  getWithdrawalAccounts(): Promise<ApiResponse<WithdrawalAccount[]>> {
    return api.get('/wallet/accounts')
  },

  addWithdrawalAccount(data: AddWithdrawalAccountData): Promise<ApiResponse<{ account: WithdrawalAccount }>> {
    return api.post('/wallet/accounts', data)
  },

  deleteWithdrawalAccount(id: string): Promise<ApiResponse<void>> {
    return api.delete(`/wallet/accounts/${id}`)
  },

  setDefaultAccount(id: string): Promise<ApiResponse<void>> {
    return api.put(`/wallet/accounts/${id}/default`)
  },

  setWithdrawPassword(password: string): Promise<ApiResponse<void>> {
    return api.post('/wallet/withdraw-password', { password })
  },

  verifyWithdrawPassword(password: string): Promise<ApiResponse<{ valid: boolean }>> {
    return api.post('/wallet/withdraw-password/verify', { password })
  },

  getIncomeStatistics(): Promise<ApiResponse<IncomeStatistics>> {
    return api.get('/wallet/income/statistics')
  }
}
