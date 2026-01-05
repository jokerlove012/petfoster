/**
 * Property-Based Tests for Pricing Calculator
 * 
 * Tests the correctness properties of booking price and refund calculations.
 */

import { describe, it, expect } from 'vitest'
import * as fc from 'fast-check'
import {
  calculateDays,
  calculateBookingPrice,
  calculateBookingPriceByDates,
  calculateRefund,
  getDiscountRate
} from '@/utils/priceCalculator'

describe('Pricing Calculator Properties', () => {
  /**
   * **Feature: pet-foster-platform, Property 3: Booking cost calculation**
   * **Validates: Requirements 3.2**
   * 
   * For any service package with price P and date range with D days,
   * the total cost should equal P × D (minus any applicable discounts).
   */
  it('booking cost equals price × days minus discount', () => {
    fc.assert(
      fc.property(
        fc.integer({ min: 1, max: 1000 }), // pricePerDay
        fc.integer({ min: 1, max: 365 }),  // days
        (pricePerDay, days) => {
          const result = calculateBookingPrice(pricePerDay, days)
          
          // Subtotal should be price × days
          expect(result.subtotal).toBe(pricePerDay * days)
          
          // Total should be subtotal minus discount
          const expectedTotal = result.subtotal - result.discount
          expect(result.totalPrice).toBeCloseTo(expectedTotal, 2)
          
          // Discount should be subtotal × discountRate
          const expectedDiscount = result.subtotal * result.discountRate
          expect(result.discount).toBeCloseTo(expectedDiscount, 2)
          
          return true
        }
      ),
      { numRuns: 100 }
    )
  })

  /**
   * Property: Discount rate increases with longer stays
   * Validates that longer booking periods receive higher discounts
   */
  it('discount rate increases with longer stays', () => {
    fc.assert(
      fc.property(
        fc.integer({ min: 1, max: 6 }),   // shortDays (no discount)
        fc.integer({ min: 7, max: 13 }),  // mediumDays (5% discount)
        fc.integer({ min: 14, max: 29 }), // longDays (10% discount)
        fc.integer({ min: 30, max: 365 }), // veryLongDays (15% discount)
        (shortDays, mediumDays, longDays, veryLongDays) => {
          const shortRate = getDiscountRate(shortDays)
          const mediumRate = getDiscountRate(mediumDays)
          const longRate = getDiscountRate(longDays)
          const veryLongRate = getDiscountRate(veryLongDays)
          
          // Discount rates should be non-decreasing
          expect(shortRate).toBeLessThanOrEqual(mediumRate)
          expect(mediumRate).toBeLessThanOrEqual(longRate)
          expect(longRate).toBeLessThanOrEqual(veryLongRate)
          
          // Specific discount tiers
          expect(shortRate).toBe(0)
          expect(mediumRate).toBe(0.05)
          expect(longRate).toBe(0.10)
          expect(veryLongRate).toBe(0.15)
          
          return true
        }
      ),
      { numRuns: 100 }
    )
  })

  /**
   * Property: Total price is always less than or equal to subtotal
   * Validates that discounts never increase the price
   */
  it('total price is always <= subtotal', () => {
    fc.assert(
      fc.property(
        fc.integer({ min: 1, max: 1000 }),
        fc.integer({ min: 1, max: 365 }),
        (pricePerDay, days) => {
          const result = calculateBookingPrice(pricePerDay, days)
          return result.totalPrice <= result.subtotal
        }
      ),
      { numRuns: 100 }
    )
  })

  /**
   * Property: Days calculation is symmetric
   * Validates that swapping start and end dates gives the same result
   */
  it('days calculation is symmetric', () => {
    fc.assert(
      fc.property(
        fc.date({ min: new Date('2024-01-01'), max: new Date('2025-12-31') }),
        fc.date({ min: new Date('2024-01-01'), max: new Date('2025-12-31') }),
        (date1, date2) => {
          const days1 = calculateDays(date1, date2)
          const days2 = calculateDays(date2, date1)
          return days1 === days2
        }
      ),
      { numRuns: 100 }
    )
  })

  /**
   * Property: Days calculation is always positive
   * Validates that the number of days is always at least 1
   */
  it('days calculation is always positive', () => {
    fc.assert(
      fc.property(
        fc.date({ min: new Date('2024-01-01'), max: new Date('2025-12-31') }),
        fc.date({ min: new Date('2024-01-01'), max: new Date('2025-12-31') }),
        (date1, date2) => {
          const days = calculateDays(date1, date2)
          return days >= 1
        }
      ),
      { numRuns: 100 }
    )
  })

  /**
   * **Feature: pet-foster-platform, Property 13: Refund calculation (>48 hours)**
   * **Validates: Requirements 16.3**
   * 
   * For any booking cancelled more than 48 hours before service start,
   * the refund amount should equal 100% of the total price.
   */
  it('full refund when cancelled >48 hours before start', () => {
    fc.assert(
      fc.property(
        fc.integer({ min: 100, max: 10000 }), // totalPrice
        fc.integer({ min: 49, max: 720 }),    // hoursBeforeStart (>48 hours)
        (totalPrice, hoursBeforeStart) => {
          const startDate = new Date()
          startDate.setHours(startDate.getHours() + hoursBeforeStart)
          
          const result = calculateRefund(totalPrice, startDate)
          
          // Should get full refund
          expect(result.refundAmount).toBe(totalPrice)
          expect(result.cancellationFee).toBe(0)
          expect(result.refundRate).toBe(1)
          
          return true
        }
      ),
      { numRuns: 100 }
    )
  })

  /**
   * **Feature: pet-foster-platform, Property 14: Refund calculation (≤48 hours)**
   * **Validates: Requirements 16.4**
   * 
   * For any booking cancelled within 48 hours of service start,
   * the refund amount should equal 70% of the total price (30% cancellation fee).
   */
  it('70% refund when cancelled ≤48 hours before start', () => {
    fc.assert(
      fc.property(
        fc.integer({ min: 100, max: 10000 }), // totalPrice
        fc.integer({ min: 1, max: 48 }),      // hoursBeforeStart (≤48 hours)
        (totalPrice, hoursBeforeStart) => {
          const startDate = new Date()
          startDate.setHours(startDate.getHours() + hoursBeforeStart)
          
          const result = calculateRefund(totalPrice, startDate)
          
          // Should get 70% refund
          const expectedRefund = Math.round(totalPrice * 0.7 * 100) / 100
          const expectedFee = Math.round(totalPrice * 0.3 * 100) / 100
          
          expect(result.refundAmount).toBeCloseTo(expectedRefund, 2)
          expect(result.cancellationFee).toBeCloseTo(expectedFee, 2)
          expect(result.refundRate).toBe(0.7)
          
          return true
        }
      ),
      { numRuns: 100 }
    )
  })

  /**
   * Property: Refund + cancellation fee equals total price
   * Validates that no money is lost or created in refund calculation
   */
  it('refund + cancellation fee equals total price', () => {
    fc.assert(
      fc.property(
        fc.integer({ min: 100, max: 10000 }),
        fc.integer({ min: 1, max: 720 }),
        (totalPrice, hoursBeforeStart) => {
          const startDate = new Date()
          startDate.setHours(startDate.getHours() + hoursBeforeStart)
          
          const result = calculateRefund(totalPrice, startDate)
          
          // Refund + fee should equal total (within rounding tolerance)
          const sum = result.refundAmount + result.cancellationFee
          return Math.abs(sum - totalPrice) < 0.01
        }
      ),
      { numRuns: 100 }
    )
  })

  /**
   * Property: Custom discount rate is applied correctly
   * Validates that custom discount rates override automatic discounts
   */
  it('custom discount rate is applied correctly', () => {
    fc.assert(
      fc.property(
        fc.integer({ min: 1, max: 1000 }),
        fc.integer({ min: 1, max: 365 }),
        fc.integer({ min: 0, max: 50 }).map(n => n / 100), // 0 to 0.5 as discount rate
        (pricePerDay, days, customRate) => {
          const result = calculateBookingPrice(pricePerDay, days, customRate)
          
          // Custom rate should be used
          expect(result.discountRate).toBe(customRate)
          
          // Discount should be calculated with custom rate
          const expectedDiscount = result.subtotal * customRate
          expect(result.discount).toBeCloseTo(expectedDiscount, 2)
          
          return true
        }
      ),
      { numRuns: 100 }
    )
  })
})
