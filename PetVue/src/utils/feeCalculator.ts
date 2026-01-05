import type { FeeConfig, WithdrawalLimits } from '@/types/wallet'

// 默认手续费配置
export const DEFAULT_FEE_CONFIG: FeeConfig = {
  rate: 0.01,      // 1%
  minFee: 100,     // 最低 1 元（100分）
}

// 默认提现限制
export const DEFAULT_WITHDRAWAL_LIMITS: WithdrawalLimits = {
  minAmount: 1000,           // 最低 10 元（1000分）
  maxDailyAmount: 5000000,   // 每日最高 50000 元（5000000分）
  maxDailyAttempts: 5,       // 每日最多 5 次
}

// 充值限制
export const RECHARGE_LIMITS = {
  minAmount: 100,            // 最低 1 元（100分）
  maxAmount: 5000000,        // 最高 50000 元（5000000分）
}

/**
 * 计算提现手续费
 * @param amount 提现金额（分）
 * @param config 手续费配置
 * @returns 手续费（分）
 */
export function calculateWithdrawalFee(
  amount: number,
  config: FeeConfig = DEFAULT_FEE_CONFIG
): number {
  const calculatedFee = Math.floor(amount * config.rate)
  let fee = Math.max(calculatedFee, config.minFee)
  if (config.maxFee !== undefined) {
    fee = Math.min(fee, config.maxFee)
  }
  return fee
}

/**
 * 计算实际到账金额
 * @param amount 提现金额（分）
 * @param fee 手续费（分）
 * @returns 实际到账金额（分）
 */
export function calculateActualAmount(amount: number, fee: number): number {
  return amount - fee
}

/**
 * 验证充值金额
 * @param amount 充值金额（分）
 * @returns 验证结果
 */
export function validateRechargeAmount(amount: number): { valid: boolean; message?: string } {
  if (amount < RECHARGE_LIMITS.minAmount) {
    return { valid: false, message: `充值金额不能少于 ${RECHARGE_LIMITS.minAmount / 100} 元` }
  }
  if (amount > RECHARGE_LIMITS.maxAmount) {
    return { valid: false, message: `充值金额不能超过 ${RECHARGE_LIMITS.maxAmount / 100} 元` }
  }
  return { valid: true }
}

/**
 * 验证提现金额
 * @param amount 提现金额（分）
 * @param availableBalance 可用余额（分）
 * @param limits 提现限制配置
 * @returns 验证结果
 */
export function validateWithdrawalAmount(
  amount: number,
  availableBalance: number,
  limits: WithdrawalLimits = DEFAULT_WITHDRAWAL_LIMITS
): { valid: boolean; message?: string } {
  if (amount < limits.minAmount) {
    return { valid: false, message: `提现金额不能少于 ${limits.minAmount / 100} 元` }
  }
  if (amount > availableBalance) {
    return { valid: false, message: '提现金额不能超过可用余额' }
  }
  if (amount > limits.maxDailyAmount) {
    return { valid: false, message: `单日提现金额不能超过 ${limits.maxDailyAmount / 100} 元` }
  }
  return { valid: true }
}

/**
 * 格式化金额显示（分转元）
 * @param amount 金额（分）
 * @returns 格式化后的金额字符串
 */
export function formatAmount(amount: number): string {
  return (amount / 100).toFixed(2)
}

/**
 * 元转分
 * @param yuan 金额（元）
 * @returns 金额（分）
 */
export function yuanToFen(yuan: number): number {
  return Math.round(yuan * 100)
}

/**
 * 分转元
 * @param fen 金额（分）
 * @returns 金额（元）
 */
export function fenToYuan(fen: number): number {
  return fen / 100
}
