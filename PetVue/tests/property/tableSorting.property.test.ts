/**
 * Property Test: Table sorting correctness
 * **Feature: pet-foster-platform, Property 19: Table sorting correctness**
 * **Validates: Requirements 14.3**
 *
 * For any array of data and any sortable column, sorting should produce
 * a correctly ordered result where each element is <= (or >=) the next.
 */

import { describe, it, expect } from 'vitest'
import * as fc from 'fast-check'

// Table sorting function (extracted logic from DataTable component)
function sortData<T extends Record<string, unknown>>(
  data: T[],
  key: string,
  order: 'asc' | 'desc'
): T[] {
  return [...data].sort((a, b) => {
    const aVal = a[key]
    const bVal = b[key]
    let comparison = 0

    if (aVal === null || aVal === undefined) comparison = 1
    else if (bVal === null || bVal === undefined) comparison = -1
    else if (typeof aVal === 'number' && typeof bVal === 'number') {
      comparison = aVal - bVal
    } else {
      comparison = String(aVal).localeCompare(String(bVal))
    }

    return order === 'asc' ? comparison : -comparison
  })
}

// Check if array is sorted
function isSorted<T>(
  arr: T[],
  compareFn: (a: T, b: T) => number,
  order: 'asc' | 'desc'
): boolean {
  for (let i = 0; i < arr.length - 1; i++) {
    const comparison = compareFn(arr[i], arr[i + 1])
    if (order === 'asc' && comparison > 0) return false
    if (order === 'desc' && comparison < 0) return false
  }
  return true
}

describe('Property 19: Table sorting correctness', () => {
  // Arbitrary for table row data
  const rowArbitrary = fc.record({
    id: fc.uuid(),
    name: fc.string({ minLength: 1, maxLength: 50 }),
    age: fc.integer({ min: 0, max: 150 }),
    score: fc.float({ min: 0, max: 100, noNaN: true }),
    createdAt: fc.date().map((d) => d.toISOString())
  })

  it('should sort numeric columns correctly in ascending order', () => {
    fc.assert(
      fc.property(fc.array(rowArbitrary, { minLength: 0, maxLength: 100 }), (data) => {
        const sorted = sortData(data, 'age', 'asc')

        // Check length preserved
        expect(sorted.length).toBe(data.length)

        // Check sorted order
        const isCorrectlySorted = isSorted(
          sorted,
          (a, b) => {
            const aVal = a.age
            const bVal = b.age
            if (aVal === null || aVal === undefined) return 1
            if (bVal === null || bVal === undefined) return -1
            return aVal - bVal
          },
          'asc'
        )
        expect(isCorrectlySorted).toBe(true)
      }),
      { numRuns: 100 }
    )
  })

  it('should sort numeric columns correctly in descending order', () => {
    fc.assert(
      fc.property(fc.array(rowArbitrary, { minLength: 0, maxLength: 100 }), (data) => {
        const sorted = sortData(data, 'score', 'desc')

        // Check length preserved
        expect(sorted.length).toBe(data.length)

        // Check sorted order (descending)
        for (let i = 0; i < sorted.length - 1; i++) {
          const current = sorted[i].score
          const next = sorted[i + 1].score
          expect(current).toBeGreaterThanOrEqual(next)
        }
      }),
      { numRuns: 100 }
    )
  })

  it('should sort string columns correctly', () => {
    fc.assert(
      fc.property(fc.array(rowArbitrary, { minLength: 0, maxLength: 100 }), (data) => {
        const sorted = sortData(data, 'name', 'asc')

        // Check length preserved
        expect(sorted.length).toBe(data.length)

        // Check sorted order using localeCompare
        for (let i = 0; i < sorted.length - 1; i++) {
          const current = String(sorted[i].name)
          const next = String(sorted[i + 1].name)
          expect(current.localeCompare(next)).toBeLessThanOrEqual(0)
        }
      }),
      { numRuns: 100 }
    )
  })

  it('should preserve all original elements after sorting', () => {
    fc.assert(
      fc.property(fc.array(rowArbitrary, { minLength: 0, maxLength: 50 }), (data) => {
        const sorted = sortData(data, 'age', 'asc')

        // All original IDs should be present
        const originalIds = new Set(data.map((r) => r.id))
        const sortedIds = new Set(sorted.map((r) => r.id))

        expect(sortedIds.size).toBe(originalIds.size)
        originalIds.forEach((id) => {
          expect(sortedIds.has(id)).toBe(true)
        })
      }),
      { numRuns: 100 }
    )
  })

  it('should handle null/undefined values by placing them at the end', () => {
    fc.assert(
      fc.property(
        fc.array(
          fc.record({
            id: fc.uuid(),
            value: fc.oneof(fc.integer(), fc.constant(null), fc.constant(undefined))
          }),
          { minLength: 1, maxLength: 50 }
        ),
        (data) => {
          const sorted = sortData(data, 'value', 'asc')

          // Find first null/undefined
          const firstNullIndex = sorted.findIndex(
            (r) => r.value === null || r.value === undefined
          )

          if (firstNullIndex !== -1) {
            // All elements after should also be null/undefined
            for (let i = firstNullIndex; i < sorted.length; i++) {
              expect(
                sorted[i].value === null || sorted[i].value === undefined
              ).toBe(true)
            }

            // All elements before should be valid numbers
            for (let i = 0; i < firstNullIndex; i++) {
              expect(sorted[i].value).not.toBeNull()
              expect(sorted[i].value).not.toBeUndefined()
            }
          }
        }
      ),
      { numRuns: 100 }
    )
  })

  it('should be stable for equal elements (preserve relative order)', () => {
    fc.assert(
      fc.property(
        fc.array(
          fc.record({
            id: fc.uuid(),
            category: fc.constantFrom('A', 'B', 'C'),
            order: fc.integer({ min: 0, max: 1000 })
          }),
          { minLength: 0, maxLength: 50 }
        ),
        (data) => {
          // Add index to track original order
          const indexed = data.map((item, idx) => ({ ...item, originalIndex: idx }))
          const sorted = sortData(indexed, 'category', 'asc')

          // For items with same category, original order should be preserved
          const categories = ['A', 'B', 'C']
          categories.forEach((cat) => {
            const itemsInCategory = sorted.filter((r) => r.category === cat)
            for (let i = 0; i < itemsInCategory.length - 1; i++) {
              expect(itemsInCategory[i].originalIndex).toBeLessThan(
                itemsInCategory[i + 1].originalIndex
              )
            }
          })
        }
      ),
      { numRuns: 100 }
    )
  })

  it('should produce inverse results when toggling sort order', () => {
    fc.assert(
      fc.property(
        fc.array(rowArbitrary, { minLength: 2, maxLength: 50 }).filter(
          // Ensure unique ages for deterministic comparison
          (arr) => new Set(arr.map((r) => r.age)).size === arr.length
        ),
        (data) => {
          const ascSorted = sortData(data, 'age', 'asc')
          const descSorted = sortData(data, 'age', 'desc')

          // Reversed ascending should equal descending
          const reversedAsc = [...ascSorted].reverse()
          expect(reversedAsc.map((r) => r.id)).toEqual(descSorted.map((r) => r.id))
        }
      ),
      { numRuns: 100 }
    )
  })
})
