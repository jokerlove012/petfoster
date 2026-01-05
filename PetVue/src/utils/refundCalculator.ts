/**
 * 退款计算工具
 * 实现退款金额计算逻辑
 * 
 * 退款规则：
 * - 入住前48小时以上取消: 全额退款 (100%)
 * - 入住前48小时内取消: 退款70% (收取30%手续费)
 * - 入住后取消: 按剩余天数比例退款
 */

export interface RefundCalculation {
  /** 退款金额 */
  refundAmount: number
  /** 手续费 */
  cancellationFee: number
  /** 退款比例 (0-1) */
  refundRate: number
  /** 退款原因说明 */
  reason: string
  /** 退款类型 */
  type: 'full' | 'partial' | 'none'
  /** 预计到账时间（工作日） */
  estimatedDays: number
}

export interface RefundRequest {
  /** 订单总价 */
  totalPrice: number
  /** 入住日期 */
  startDate: Date | string
  /** 离店日期 */
  endDate: Date | string
  /** 取消日期（默认当前时间） */
  cancelDate?: Date | string
  /** 取消原因 */
  cancelReason?: string
}

/**
 * 计算距离入住的小时数
 */
export function getHoursUntilStart(startDate: Date | string, cancelDate?: Date | string): number {
  const start = typeof startDate === 'string' ? new Date(startDate) : startDate
  const cancel = cancelDate 
    ? (typeof cancelDate === 'string' ? new Date(cancelDate) : cancelDate)
    : new Date()
  
  return (start.getTime() - cancel.getTime()) / (1000 * 60 * 60)
}

/**
 * 计算退款金额
 * 
 * @param request 退款请求参数
 * @returns 退款计算结果
 */
export function calculateRefund(request: RefundRequest): RefundCalculation {
  const { totalPrice, startDate, endDate, cancelDate } = request
  
  const start = typeof startDate === 'string' ? new Date(startDate) : startDate
  const end = typeof endDate === 'string' ? new Date(endDate) : endDate
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
      reason: '入住前48小时以上取消，全额退款',
      type: 'full',
      estimatedDays: 5
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
      reason: '入住前48小时内取消，收取30%手续费',
      type: 'partial',
      estimatedDays: 5
    }
  }
  
  // 入住后取消: 按剩余天数比例退款
  const totalDays = Math.ceil((end.getTime() - start.getTime()) / (1000 * 60 * 60 * 24)) + 1
  const usedDays = Math.ceil((cancel.getTime() - start.getTime()) / (1000 * 60 * 60 * 24)) + 1
  const remainingDays = Math.max(0, totalDays - usedDays)
  
  if (remainingDays > 0) {
    const refundRate = remainingDays / totalDays * 0.7 // 剩余天数比例 × 70%
    const refundAmount = Math.round(totalPrice * refundRate * 100) / 100
    const cancellationFee = totalPrice - refundAmount
    
    return {
      refundAmount,
      cancellationFee: Math.round(cancellationFee * 100) / 100,
      refundRate,
      reason: `入住后取消，按剩余${remainingDays}天比例退款70%`,
      type: 'partial',
      estimatedDays: 7
    }
  }
  
  // 已完成或无剩余天数: 不退款
  return {
    refundAmount: 0,
    cancellationFee: totalPrice,
    refundRate: 0,
    reason: '服务已完成或已使用，不予退款',
    type: 'none',
    estimatedDays: 0
  }
}

/**
 * 获取退款政策说明
 */
export function getRefundPolicy(): string[] {
  return [
    '入住前48小时以上取消，可获得全额退款',
    '入住前48小时内取消，收取30%手续费，退款70%',
    '入住后取消，按剩余天数比例退款70%',
    '退款将在5-7个工作日内原路返回'
  ]
}

/**
 * 判断是否可以取消订单
 * @param startDate 入住日期
 * @param status 订单状态
 */
export function canCancelOrder(
  startDate: Date | string,
  status: string
): { canCancel: boolean; reason: string } {
  // 已完成或已取消的订单不能取消
  if (status === 'completed' || status === 'cancelled') {
    return {
      canCancel: false,
      reason: '订单已完成或已取消，无法再次取消'
    }
  }
  
  const start = typeof startDate === 'string' ? new Date(startDate) : startDate
  const now = new Date()
  
  // 已经过了入住日期
  if (now > start) {
    const daysPassed = Math.ceil((now.getTime() - start.getTime()) / (1000 * 60 * 60 * 24))
    if (daysPassed > 1) {
      return {
        canCancel: true,
        reason: '入住后取消将按剩余天数比例退款'
      }
    }
  }
  
  return {
    canCancel: true,
    reason: '可以取消订单'
  }
}

/**
 * 格式化退款金额显示
 */
export function formatRefundAmount(amount: number): string {
  return `¥${amount.toFixed(2)}`
}

/**
 * 获取退款状态文本
 */
export function getRefundStatusText(type: RefundCalculation['type']): string {
  switch (type) {
    case 'full':
      return '全额退款'
    case 'partial':
      return '部分退款'
    case 'none':
      return '不予退款'
    default:
      return '未知'
  }
}
