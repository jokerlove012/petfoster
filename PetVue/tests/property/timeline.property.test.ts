/**
 * Property-Based Tests for Timeline Ordering
 * 
 * Tests the correctness properties of health record timeline ordering.
 */

import { describe, it, expect } from 'vitest'
import * as fc from 'fast-check'
import type { HealthRecord, FeedingStatus, ActivityLevel, PetMood } from '@/types/health'

// 健康记录生成器
const feedingStatusArb = fc.constantFrom<FeedingStatus>('normal', 'reduced', 'increased', 'refused')
const activityLevelArb = fc.constantFrom<ActivityLevel>('high', 'normal', 'low', 'inactive')
const petMoodArb = fc.constantFrom<PetMood>('happy', 'calm', 'anxious', 'stressed')

const healthRecordArb = fc.record({
  id: fc.uuid(),
  bookingId: fc.uuid(),
  date: fc.date({ min: new Date('2024-01-01'), max: new Date('2025-12-31') }),
  feedingStatus: feedingStatusArb,
  activityLevel: activityLevelArb,
  healthObservations: fc.string({ minLength: 10, maxLength: 200 }),
  mood: petMoodArb,
  weight: fc.option(fc.float({ min: 1, max: 50 })),
  temperature: fc.option(fc.float({ min: 35, max: 42 })),
  isAbnormal: fc.boolean(),
  createdBy: fc.uuid(),
  createdAt: fc.date({ min: new Date('2024-01-01'), max: new Date('2025-12-31') })
}) as fc.Arbitrary<HealthRecord>

/**
 * 按时间降序排序健康记录
 */
function sortRecordsByTimeDescending(records: HealthRecord[]): HealthRecord[] {
  return [...records].sort((a, b) => {
    const timeA = a.createdAt instanceof Date ? a.createdAt.getTime() : new Date(a.createdAt).getTime()
    const timeB = b.createdAt instanceof Date ? b.createdAt.getTime() : new Date(b.createdAt).getTime()
    return timeB - timeA
  })
}

/**
 * 按日期分组健康记录
 */
function groupRecordsByDate(records: HealthRecord[]): Record<string, HealthRecord[]> {
  const groups: Record<string, HealthRecord[]> = {}
  
  records.forEach(record => {
    const date = record.date instanceof Date ? record.date : new Date(record.date)
    const dateKey = date.toISOString().split('T')[0]
    if (!groups[dateKey]) {
      groups[dateKey] = []
    }
    groups[dateKey].push(record)
  })
  
  return groups
}

/**
 * 检查数组是否按降序排列
 */
function isDescendingOrder(timestamps: number[]): boolean {
  for (let i = 1; i < timestamps.length; i++) {
    if (timestamps[i] > timestamps[i - 1]) {
      return false
    }
  }
  return true
}

describe('Timeline Ordering Properties', () => {
  /**
   * **Feature: pet-foster-platform, Property 5: Health record chronological ordering**
   * **Validates: Requirements 4.3**
   * 
   * For any list of health records or media content, when displayed in timeline,
   * items should be sorted by timestamp in descending order (newest first).
   */
  it('health records are sorted by timestamp in descending order', () => {
    fc.assert(
      fc.property(
        fc.array(healthRecordArb, { minLength: 1, maxLength: 50 }),
        (records) => {
          const sorted = sortRecordsByTimeDescending(records)
          
          const timestamps = sorted.map(r => {
            const time = r.createdAt instanceof Date ? r.createdAt.getTime() : new Date(r.createdAt).getTime()
            return time
          })
          
          return isDescendingOrder(timestamps)
        }
      ),
      { numRuns: 100 }
    )
  })

  /**
   * Property: Sorting preserves all records
   * Validates that sorting doesn't lose or duplicate any records
   */
  it('sorting preserves all records', () => {
    fc.assert(
      fc.property(
        fc.array(healthRecordArb, { minLength: 0, maxLength: 50 }),
        (records) => {
          const sorted = sortRecordsByTimeDescending(records)
          
          // Same length
          if (sorted.length !== records.length) return false
          
          // Same IDs (as a set)
          const originalIds = new Set(records.map(r => r.id))
          const sortedIds = new Set(sorted.map(r => r.id))
          
          if (originalIds.size !== sortedIds.size) return false
          
          for (const id of originalIds) {
            if (!sortedIds.has(id)) return false
          }
          
          return true
        }
      ),
      { numRuns: 100 }
    )
  })

  /**
   * Property: Grouping by date preserves all records
   * Validates that grouping doesn't lose any records
   */
  it('grouping by date preserves all records', () => {
    fc.assert(
      fc.property(
        fc.array(healthRecordArb, { minLength: 0, maxLength: 50 }),
        (records) => {
          const groups = groupRecordsByDate(records)
          
          // Count total records in groups
          let totalInGroups = 0
          for (const dateKey in groups) {
            totalInGroups += groups[dateKey].length
          }
          
          return totalInGroups === records.length
        }
      ),
      { numRuns: 100 }
    )
  })

  /**
   * Property: Records in same date group have same date
   * Validates that grouping correctly groups by date
   */
  it('records in same date group have same date', () => {
    fc.assert(
      fc.property(
        fc.array(healthRecordArb, { minLength: 1, maxLength: 50 }),
        (records) => {
          const groups = groupRecordsByDate(records)
          
          for (const dateKey in groups) {
            const recordsInGroup = groups[dateKey]
            
            for (const record of recordsInGroup) {
              const recordDate = record.date instanceof Date ? record.date : new Date(record.date)
              const recordDateKey = recordDate.toISOString().split('T')[0]
              
              if (recordDateKey !== dateKey) return false
            }
          }
          
          return true
        }
      ),
      { numRuns: 100 }
    )
  })

  /**
   * Property: Sorting is idempotent
   * Validates that sorting twice gives the same result as sorting once
   */
  it('sorting is idempotent', () => {
    fc.assert(
      fc.property(
        fc.array(healthRecordArb, { minLength: 0, maxLength: 50 }),
        (records) => {
          const sortedOnce = sortRecordsByTimeDescending(records)
          const sortedTwice = sortRecordsByTimeDescending(sortedOnce)
          
          // Same order
          for (let i = 0; i < sortedOnce.length; i++) {
            if (sortedOnce[i].id !== sortedTwice[i].id) return false
          }
          
          return true
        }
      ),
      { numRuns: 100 }
    )
  })

  /**
   * Property: Empty array sorting returns empty array
   * Validates edge case handling
   */
  it('empty array sorting returns empty array', () => {
    const sorted = sortRecordsByTimeDescending([])
    expect(sorted).toEqual([])
  })

  /**
   * Property: Single record sorting returns same record
   * Validates edge case handling
   */
  it('single record sorting returns same record', () => {
    fc.assert(
      fc.property(
        healthRecordArb,
        (record) => {
          const sorted = sortRecordsByTimeDescending([record])
          return sorted.length === 1 && sorted[0].id === record.id
        }
      ),
      { numRuns: 100 }
    )
  })
})
