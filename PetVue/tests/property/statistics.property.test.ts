/**
 * Property-Based Tests for Statistics Calculation
 * 
 * Tests the correctness properties of statistics calculation for institution dashboard.
 */

import { describe, it, expect } from 'vitest'
import * as fc from 'fast-check'

interface Order {
  id: string
  amount: number
  status: 'pending' | 'confirmed' | 'ongoing' | 'completed' | 'cancelled'
  createdAt: Date
}

interface DailyStats {
  date: string
  revenue: number
  orderCount: number
  occupancy: number
}

/**
 * 计算总收入（只计算已完成订单）
 */
function calculateTotalRevenue(orders: Order[]): number {
  return orders
    .filter(o => o.status === 'completed')
    .reduce((sum, o) => sum + o.amount, 0)
}

/**
 * 计算订单数量（按状态）
 */
function countOrdersByStatus(orders: Order[], status: Order['status']): number {
  return orders.filter(o => o.status === status).length
}

/**
 * 计算入住率
 */
function calculateOccupancyRate(occupiedSlots: number, totalSlots: number): number {
  if (totalSlots === 0) return 0
  return Math.round((occupiedSlots / totalSlots) * 100)
}

/**
 * 计算平均每日收入
 */
function calculateAverageDailyRevenue(dailyStats: DailyStats[]): number {
  if (dailyStats.length === 0) return 0
  const total = dailyStats.reduce((sum, d) => sum + d.revenue, 0)
  return Math.round(total / dailyStats.length)
}

/**
 * 计算环比增长率
 */
function calculateGrowthRate(current: number, previous: number): number {
  if (previous === 0) return current > 0 ? 100 : 0
  return Math.round(((current - previous) / previous) * 100 * 10) / 10
}

// 订单生成器
const orderStatusArb = fc.constantFrom<Order['status']>('pending', 'confirmed', 'ongoing', 'completed', 'cancelled')

const orderArb = fc.record({
  id: fc.uuid(),
  amount: fc.integer({ min: 100, max: 10000 }),
  status: orderStatusArb,
  createdAt: fc.date({ min: new Date('2024-01-01'), max: new Date('2025-12-31') })
}) as fc.Arbitrary<Order>

// 每日统计生成器
const dailyStatsArb = fc.record({
  date: fc.date().map(d => d.toISOString().split('T')[0]),
  revenue: fc.integer({ min: 0, max: 50000 }),
  orderCount: fc.integer({ min: 0, max: 50 }),
  occupancy: fc.integer({ min: 0, max: 100 })
}) as fc.Arbitrary<DailyStats>

describe('Statistics Calculation Properties', () => {
  /**
   * **Feature: pet-foster-platform, Property 10: Statistics calculation correctness**
   * **Validates: Requirements 6.5, 9.1, 9.2, 9.4**
   * 
   * Statistics should be calculated correctly from order data.
   */
  it('total revenue only includes completed orders', () => {
    fc.assert(
      fc.property(
        fc.array(orderArb, { minLength: 0, maxLength: 100 }),
        (orders) => {
          const revenue = calculateTotalRevenue(orders)
          const completedOrders = orders.filter(o => o.status === 'completed')
          const expectedRevenue = completedOrders.reduce((sum, o) => sum + o.amount, 0)
          
          return revenue === expectedRevenue
        }
      ),
      { numRuns: 100 }
    )
  })

  it('revenue is non-negative', () => {
    fc.assert(
      fc.property(
        fc.array(orderArb, { minLength: 0, maxLength: 100 }),
        (orders) => {
          const revenue = calculateTotalRevenue(orders)
          return revenue >= 0
        }
      ),
      { numRuns: 100 }
    )
  })

  it('order count by status is accurate', () => {
    fc.assert(
      fc.property(
        fc.array(orderArb, { minLength: 0, maxLength: 100 }),
        orderStatusArb,
        (orders, status) => {
          const count = countOrdersByStatus(orders, status)
          const expected = orders.filter(o => o.status === status).length
          
          return count === expected
        }
      ),
      { numRuns: 100 }
    )
  })

  it('sum of all status counts equals total orders', () => {
    fc.assert(
      fc.property(
        fc.array(orderArb, { minLength: 0, maxLength: 100 }),
        (orders) => {
          const statuses: Order['status'][] = ['pending', 'confirmed', 'ongoing', 'completed', 'cancelled']
          const totalCount = statuses.reduce((sum, status) => sum + countOrdersByStatus(orders, status), 0)
          
          return totalCount === orders.length
        }
      ),
      { numRuns: 100 }
    )
  })

  it('occupancy rate is between 0 and 100', () => {
    fc.assert(
      fc.property(
        fc.integer({ min: 0, max: 100 }),
        fc.integer({ min: 1, max: 100 }),
        (occupied, total) => {
          // 确保 occupied <= total
          const actualOccupied = Math.min(occupied, total)
          const rate = calculateOccupancyRate(actualOccupied, total)
          return rate >= 0 && rate <= 100
        }
      ),
      { numRuns: 100 }
    )
  })

  it('occupancy rate is 0 when no slots', () => {
    const rate = calculateOccupancyRate(0, 0)
    expect(rate).toBe(0)
  })

  it('occupancy rate is 100 when fully occupied', () => {
    fc.assert(
      fc.property(
        fc.integer({ min: 1, max: 100 }),
        (slots) => {
          const rate = calculateOccupancyRate(slots, slots)
          return rate === 100
        }
      ),
      { numRuns: 100 }
    )
  })

  it('average daily revenue is non-negative', () => {
    fc.assert(
      fc.property(
        fc.array(dailyStatsArb, { minLength: 0, maxLength: 365 }),
        (stats) => {
          const avg = calculateAverageDailyRevenue(stats)
          return avg >= 0
        }
      ),
      { numRuns: 100 }
    )
  })

  it('average daily revenue is 0 for empty stats', () => {
    const avg = calculateAverageDailyRevenue([])
    expect(avg).toBe(0)
  })

  it('growth rate is positive when current > previous', () => {
    fc.assert(
      fc.property(
        fc.integer({ min: 1, max: 100000 }),
        fc.integer({ min: 1, max: 100000 }),
        (current, previous) => {
          if (current <= previous) return true // 跳过
          const rate = calculateGrowthRate(current, previous)
          // 由于四舍五入，当差异很小时可能为0
          return rate >= 0
        }
      ),
      { numRuns: 100 }
    )
  })

  it('growth rate is negative when current < previous', () => {
    fc.assert(
      fc.property(
        fc.integer({ min: 1, max: 100000 }),
        fc.integer({ min: 1, max: 100000 }),
        (current, previous) => {
          if (current >= previous) return true // 跳过
          const rate = calculateGrowthRate(current, previous)
          // 由于四舍五入，当差异很小时可能为0
          return rate <= 0
        }
      ),
      { numRuns: 100 }
    )
  })

  it('growth rate is 0 when current equals previous', () => {
    fc.assert(
      fc.property(
        fc.integer({ min: 1, max: 100000 }),
        (value) => {
          const rate = calculateGrowthRate(value, value)
          return rate === 0
        }
      ),
      { numRuns: 100 }
    )
  })

  it('growth rate handles zero previous value', () => {
    fc.assert(
      fc.property(
        fc.integer({ min: 0, max: 100000 }),
        (current) => {
          const rate = calculateGrowthRate(current, 0)
          if (current > 0) return rate === 100
          return rate === 0
        }
      ),
      { numRuns: 100 }
    )
  })
})
