/**
 * 价格计算工具
 * 用于计算预约费用、折扣和退款金额
 */

export interface PriceBreakdown {
  basePrice: number
  totalDays: number
  subtotal: number
  discount: number
  discountRate: number
  totalPrice: number
}

export interface RefundResult {
  refundAmount: number
  cancellationFee: number
  refundRate: number
  reason: string
}

/**
 * 计算两个日期之间的天数（包含起止日期）
 */
export function calculateDays(startDate: Date | string, endDate: Date | string): number {
  const start = typeof startDate === 'string' ? new Date(startDate) : startDate
  const end = typeof endDate === 'string' ? new Date(endDate) : endDate
  
  // 重置时间部分，只比较日期
  start.setHours(0, 0, 0, 0)
  end.setHours(0, 0, 0, 0)
  
  const diffTime = Math.abs(end.getTime() - start.getTime())
  const diffDays = Math.ceil(diffTime / (1000 * 60 * 60 * 24))
  
  // 包含起止日期，所以加1
  return diffDays + 1
}

/**
 * 根据天数获取折扣率
 * - 7天以上: 5% 折扣
 * - 14天以上: 10% 折扣
 * - 30天以上: 15% 折扣
 */
export function getDiscountRate(days: number): number {
  if (days >= 30) return 0.15
  if (days >= 14) return 0.10
  if (days >= 7) return 0.05
  return 0
}

/**
 * 计算预约价格
 * @param pricePerDay 每日价格
 * @param days 天数
 * @param customDiscountRate 自定义折扣率（可选）
 */
export function calculateBookingPrice(
  pricePerDay: number,
  days: number,
  customDiscountRate?: number
): PriceBreakdown {
  if (pricePerDay < 0 || days < 0) {
    throw new Error('Price and days must be non-negative')
  }
  
  const subtotal = pricePerDay * days
  const discountRate = customDiscountRate ?? getDiscountRate(days)
  const discount = subtotal * discountRate
  const totalPrice = subtotal - discount
  
  return {
    basePrice: pricePerDay,
    totalDays: days,
    subtotal,
    discount: Math.round(discount * 100) / 100,
    discountRate,
    totalPrice: Math.round(totalPrice * 100) / 100
  }
}

/**
 * 计算预约价格（通过日期范围）
 */
export function calculateBookingPriceByDates(
  pricePerDay: number,
  startDate: Date | string,
  endDate: Date | string,
  customDiscountRate?: number
): PriceBreakdown {
  const days = calculateDays(startDate, endDate)
  return calculateBookingPrice(pricePerDay, days, customDiscountRate)
}

/**
 * 计算退款金额
 * - 入住前48小时以上取消: 全额退款
 * - 入住前48小时内取消: 退款70%（收取30%手续费）
 * - 入住后取消: 按剩余天数比例退款
 * 
 * @param totalPrice 订单总价
 * @param startDate 入住日期
 * @param cancelDate 取消日期（默认为当前时间）
 */
export function calculateRefund(
  totalPrice: number,
  startDate: Date | string,
  cancelDate?: Date | string
): RefundResult {
  const start = typeof startDate === 'string' ? new Date(startDate) : startDate
  const cancel = cancelDate 
    ? (typeof cancelDate === 'string' ? new Date(cancelDate) : cancelDate)
    : new Date()
  
  // 计算距离入住的小时数
  const hoursUntilStart = (start.getTime() - cancel.getTime()) / (1000 * 60 * 60)
  
  // 入住前48小时以上取消: 全额退款
  if (hoursUntilStart > 48) {
    return {
      refundAmount: totalPrice,
      cancellationFee: 0,
      refundRate: 1,
      reason: '入住前48小时以上取消，全额退款'
    }
  }
  
  // 入住前48小时内取消: 退款70%
  if (hoursUntilStart > 0) {
    const refundRate = 0.7
    const refundAmount = Math.round(totalPrice * refundRate * 100) / 100
    const cancellationFee = Math.round(totalPrice * 0.3 * 100) / 100
    
    return {
      refundAmount,
      cancellationFee,
      refundRate,
      reason: '入住前48小时内取消，收取30%手续费'
    }
  }
  
  // 入住后取消: 不退款（实际业务中可能按剩余天数计算）
  return {
    refundAmount: 0,
    cancellationFee: totalPrice,
    refundRate: 0,
    reason: '入住后取消，不予退款'
  }
}

/**
 * 格式化价格显示
 */
export function formatPrice(price: number): string {
  return `¥${price.toFixed(2)}`
}

/**
 * 格式化折扣显示
 */
export function formatDiscount(rate: number): string {
  if (rate === 0) return '无折扣'
  return `${(rate * 100).toFixed(0)}% 折扣`
}
