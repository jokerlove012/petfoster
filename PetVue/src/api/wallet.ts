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
  // 获取钱包信息
  getWallet(): Promise<ApiResponse<Wallet>> {
    return api.get('/wallet')
  },

  // 获取余额
  getBalance(): Promise<ApiResponse<{ balance: number; frozenBalance: number }>> {
    return api.get('/wallet/balance')
  },

  // 创建充值订单
  createRechargeOrder(data: CreateRechargeData): Promise<ApiResponse<RechargeOrder>> {
    return api.post('/wallet/recharge', data)
  },

  // 查询充值状态
  getRechargeStatus(orderId: string): Promise<ApiResponse<RechargeOrder>> {
    return api.get(`/wallet/recharge/${orderId}`)
  },

  // 创建提现申请
  createWithdrawal(data: CreateWithdrawalData): Promise<ApiResponse<Withdrawal>> {
    return api.post('/wallet/withdraw', data)
  },

  // 获取提现记录
  getWithdrawals(params?: { page?: number; pageSize?: number }): Promise<ApiResponse<PaginatedData<Withdrawal>>> {
    const searchParams = new URLSearchParams()
    if (params?.page) searchParams.append('page', params.page.toString())
    if (params?.pageSize) searchParams.append('pageSize', params.pageSize.toString())
    return api.get(`/wallet/withdrawals?${searchParams.toString()}`)
  },

  // 取消提现
  cancelWithdrawal(id: string): Promise<ApiResponse<void>> {
    return api.post(`/wallet/withdrawals/${id}/cancel`)
  },

  // 获取交易记录
  getTransactions(params?: TransactionQueryParams): Promise<ApiResponse<PaginatedData<WalletTransaction>>> {
    const searchParams = new URLSearchParams()
    if (params?.page) searchParams.append('page', params.page.toString())
    if (params?.pageSize) searchParams.append('pageSize', params.pageSize.toString())
    if (params?.type) searchParams.append('type', params.type)
    if (params?.startDate) searchParams.append('startDate', params.startDate)
    if (params?.endDate) searchParams.append('endDate', params.endDate)
    return api.get(`/wallet/transactions?${searchParams.toString()}`)
  },

  // 获取提现账户列表
  getWithdrawalAccounts(): Promise<ApiResponse<WithdrawalAccount[]>> {
    return api.get('/wallet/accounts')
  },

  // 添加提现账户
  addWithdrawalAccount(data: AddWithdrawalAccountData): Promise<ApiResponse<WithdrawalAccount>> {
    return api.post('/wallet/accounts', data)
  },

  // 删除提现账户
  deleteWithdrawalAccount(id: string): Promise<ApiResponse<void>> {
    return api.delete(`/wallet/accounts/${id}`)
  },

  // 设置默认账户
  setDefaultAccount(id: string): Promise<ApiResponse<void>> {
    return api.put(`/wallet/accounts/${id}/default`)
  },

  // 获取收入统计（机构）
  getIncomeStatistics(period: 'day' | 'week' | 'month'): Promise<ApiResponse<IncomeStatistics>> {
    return api.get(`/wallet/income/statistics?period=${period}`)
  }
}
