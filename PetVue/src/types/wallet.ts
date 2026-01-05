// 钱包状态
export type WalletStatus = 'active' | 'frozen' | 'disabled'

// 交易类型
export type TransactionType = 'recharge' | 'withdrawal' | 'income' | 'expense' | 'refund'

// 交易状态
export type TransactionStatus = 'pending' | 'success' | 'failed' | 'cancelled'

// 提现状态
export type WithdrawalStatus = 'pending' | 'processing' | 'success' | 'rejected' | 'cancelled'

// 充值状态
export type RechargeStatus = 'pending' | 'paid' | 'expired' | 'cancelled'

// 支付方式
export type PaymentMethod = 'alipay' | 'wechat'

// 提现账户类型
export type WithdrawalAccountType = 'bank_card' | 'alipay'

// 钱包
export interface Wallet {
  id: string
  userId: string
  userType: 'pet_owner' | 'institution'
  balance: number           // 可用余额（分）
  frozenBalance: number     // 冻结余额（分）
  totalIncome: number       // 累计收入（分）
  totalWithdraw: number     // 累计提现（分）
  status: WalletStatus
  createdAt: string
  updatedAt: string
}

// 钱包交易记录
export interface WalletTransaction {
  id: string
  walletId: string
  type: TransactionType
  amount: number            // 金额（分）
  fee: number               // 手续费（分）
  balanceBefore: number     // 交易前余额
  balanceAfter: number      // 交易后余额
  status: TransactionStatus
  description: string
  relatedOrderId?: string   // 关联订单ID
  relatedWithdrawalId?: string // 关联提现ID
  createdAt: string
}

// 提现记录
export interface Withdrawal {
  id: string
  walletId: string
  amount: number            // 提现金额（分）
  fee: number               // 手续费（分）
  actualAmount: number      // 实际到账金额（分）
  accountId: string         // 提现账户ID
  accountInfo?: WithdrawalAccount // 提现账户信息
  status: WithdrawalStatus
  rejectReason?: string
  processedAt?: string
  createdAt: string
}


// 提现账户
export interface WithdrawalAccount {
  id: string
  userId: string
  type: WithdrawalAccountType
  accountName: string       // 账户名称（持卡人姓名）
  accountNumber: string     // 账号（脱敏显示）
  bankName?: string         // 银行名称
  isDefault: boolean
  createdAt: string
}

// 充值订单
export interface RechargeOrder {
  id: string
  walletId: string
  amount: number            // 充值金额（分）
  paymentMethod: PaymentMethod
  status: RechargeStatus
  paymentOrderId?: string   // 第三方支付订单号
  paidAt?: string
  createdAt: string
  expiredAt: string
}

// 收入统计
export interface IncomeStatistics {
  totalIncome: number       // 总收入（分）
  periodIncome: number      // 周期收入（分）
  orderCount: number        // 订单数量
  averageOrderAmount: number // 平均订单金额（分）
  dailyData: DailyIncomeData[]
}

export interface DailyIncomeData {
  date: string
  amount: number
  orderCount: number
}

// API 请求参数
export interface CreateRechargeData {
  amount: number           // 金额（元）
  paymentMethod: PaymentMethod
}

export interface CreateWithdrawalData {
  amount: number           // 金额（元）
  accountId: string
  password: string         // 支付密码
}

export interface AddWithdrawalAccountData {
  type: WithdrawalAccountType
  accountName: string
  accountNumber: string
  bankName?: string
}

export interface TransactionQueryParams {
  page?: number
  pageSize?: number
  type?: TransactionType
  startDate?: string
  endDate?: string
}

// 手续费配置
export interface FeeConfig {
  rate: number             // 费率（如 0.01 表示 1%）
  minFee: number           // 最低手续费（分）
  maxFee?: number          // 最高手续费（分）
}

// 提现限制配置
export interface WithdrawalLimits {
  minAmount: number        // 最低提现金额（分）
  maxDailyAmount: number   // 每日最高提现金额（分）
  maxDailyAttempts: number // 每日最多提现次数
}
