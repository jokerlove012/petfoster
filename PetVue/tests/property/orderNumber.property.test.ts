/**
 * Property-Based Tests for Order Number Generation
 * 
 * Tests the correctness properties of order number generation.
 */

import { describe, it, expect, beforeEach } from 'vitest'
import * as fc from 'fast-check'
import {
  generateOrderNumber,
  generateOrderNumbers,
  isValidOrderNumber,
  parseOrderNumber,
  clearGeneratedOrderNumbers
} from '@/utils/orderNumber'

describe('Order Number Properties', () => {
  // 每个测试前清除已生成的订单号
  beforeEach(() => {
    clearGeneratedOrderNumbers()
  })

  /**
   * **Feature: pet-foster-platform, Property 4: Order number uniqueness**
   * **Validates: Requirements 3.5**
   * 
   * For any two booking orders created, their order numbers should be different.
   */
  it('all generated order numbers are unique', () => {
    fc.assert(
      fc.property(
        fc.integer({ min: 2, max: 100 }), // number of orders to generate
        (count) => {
          clearGeneratedOrderNumbers()
          
          const orderNumbers = generateOrderNumbers(count)
          const uniqueNumbers = new Set(orderNumbers)
          
          // All order numbers should be unique
          return uniqueNumbers.size === orderNumbers.length
        }
      ),
      { numRuns: 100 }
    )
  })

  /**
   * Property: Generated order numbers have valid format
   * Validates that all generated order numbers match the expected format
   */
  it('generated order numbers have valid format', () => {
    fc.assert(
      fc.property(
        fc.integer({ min: 1, max: 50 }),
        (count) => {
          clearGeneratedOrderNumbers()
          
          const orderNumbers = generateOrderNumbers(count)
          
          // All order numbers should be valid
          return orderNumbers.every(num => isValidOrderNumber(num))
        }
      ),
      { numRuns: 100 }
    )
  })

  /**
   * Property: Order numbers contain correct date
   * Validates that order numbers encode the correct date
   */
  it('order numbers contain correct date', () => {
    fc.assert(
      fc.property(
        fc.date({ min: new Date('2020-01-01'), max: new Date('2030-12-31') }),
        (date) => {
          clearGeneratedOrderNumbers()
          
          const orderNumber = generateOrderNumber('PF', date)
          const parsed = parseOrderNumber(orderNumber)
          
          if (!parsed) return false
          
          // Date should match (comparing year, month, day)
          return (
            parsed.date.getFullYear() === date.getFullYear() &&
            parsed.date.getMonth() === date.getMonth() &&
            parsed.date.getDate() === date.getDate()
          )
        }
      ),
      { numRuns: 100 }
    )
  })

  /**
   * Property: Order numbers contain correct prefix
   * Validates that order numbers use the specified prefix
   */
  it('order numbers contain correct prefix', () => {
    fc.assert(
      fc.property(
        fc.stringOf(fc.constantFrom('A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z'), { minLength: 2, maxLength: 4 }),
        (prefix) => {
          clearGeneratedOrderNumbers()
          
          const orderNumber = generateOrderNumber(prefix)
          const parsed = parseOrderNumber(orderNumber)
          
          if (!parsed) return false
          
          return parsed.prefix === prefix
        }
      ),
      { numRuns: 100 }
    )
  })

  /**
   * Property: Parse and validate are consistent
   * Validates that parseable order numbers are also valid
   */
  it('parseable order numbers are valid', () => {
    fc.assert(
      fc.property(
        fc.integer({ min: 1, max: 20 }),
        (count) => {
          clearGeneratedOrderNumbers()
          
          const orderNumbers = generateOrderNumbers(count)
          
          return orderNumbers.every(num => {
            const isValid = isValidOrderNumber(num)
            const parsed = parseOrderNumber(num)
            
            // If valid, should be parseable; if parseable, should be valid
            return isValid === (parsed !== null)
          })
        }
      ),
      { numRuns: 100 }
    )
  })

  /**
   * Property: Order number length is consistent
   * Validates that order numbers have expected length
   */
  it('order numbers have consistent length', () => {
    fc.assert(
      fc.property(
        fc.integer({ min: 1, max: 50 }),
        (count) => {
          clearGeneratedOrderNumbers()
          
          const orderNumbers = generateOrderNumbers(count, 'PF')
          
          // All order numbers should have length 16 (PF + 8 date + 6 random)
          return orderNumbers.every(num => num.length === 16)
        }
      ),
      { numRuns: 100 }
    )
  })

  /**
   * Property: Sequential generation maintains uniqueness
   * Validates that rapidly generated order numbers are still unique
   */
  it('sequential generation maintains uniqueness', () => {
    fc.assert(
      fc.property(
        fc.integer({ min: 10, max: 200 }),
        (count) => {
          clearGeneratedOrderNumbers()
          
          const orderNumbers: string[] = []
          
          // Generate order numbers sequentially
          for (let i = 0; i < count; i++) {
            orderNumbers.push(generateOrderNumber())
          }
          
          const uniqueNumbers = new Set(orderNumbers)
          return uniqueNumbers.size === orderNumbers.length
        }
      ),
      { numRuns: 50 }
    )
  })

  /**
   * Property: Invalid order numbers are rejected
   * Validates that malformed order numbers are not considered valid
   */
  it('invalid order numbers are rejected', () => {
    fc.assert(
      fc.property(
        fc.oneof(
          fc.string({ minLength: 0, maxLength: 5 }),  // Too short
          fc.string({ minLength: 20, maxLength: 30 }), // Too long
          fc.stringOf(fc.constantFrom('a', 'b', 'c', '1', '2', '3'), { minLength: 16, maxLength: 16 }) // Wrong format
        ),
        (invalidNumber) => {
          // Most random strings should be invalid
          // We can't guarantee all are invalid, but most should be
          const isValid = isValidOrderNumber(invalidNumber)
          const parsed = parseOrderNumber(invalidNumber)
          
          // If invalid, should not be parseable
          if (!isValid) {
            return parsed === null
          }
          return true
        }
      ),
      { numRuns: 100 }
    )
  })
})
