import { describe, it, expect } from 'vitest'
import * as fc from 'fast-check'

/**
 * 浏览历史项
 */
interface BrowsingHistoryItem {
  id: string
  visitedAt: string // ISO date string
}

/**
 * 过滤30天内的浏览历史并按访问时间降序排列
 */
function filterRecentHistory(
  history: BrowsingHistoryItem[],
  referenceDate: Date = new Date()
): BrowsingHistoryItem[] {
  const thirtyDaysAgo = new Date(referenceDate)
  thirtyDaysAgo.setDate(thirtyDaysAgo.getDate() - 30)

  return history
    .filter((item) => new Date(item.visitedAt) >= thirtyDaysAgo)
    .sort(
      (a, b) =>
        new Date(b.visitedAt).getTime() - new Date(a.visitedAt).getTime()
    )
}

/**
 * 生成浏览历史项的 Arbitrary
 */
const browsingHistoryItemArbitrary = (referenceDate: Date) =>
  fc.record({
    id: fc.uuid(),
    // 生成过去60天内的日期（包含30天内和30天外的）
    visitedAt: fc
      .integer({ min: 0, max: 60 })
      .map((daysAgo) => {
        const date = new Date(referenceDate)
        date.setDate(date.getDate() - daysAgo)
        date.setHours(
          Math.floor(Math.random() * 24),
          Math.floor(Math.random() * 60),
          Math.floor(Math.random() * 60)
        )
        return date.toISOString()
      })
  })

describe('Browsing History Properties', () => {
  /**
   * **Feature: pet-foster-platform, Property 17: Browsing history recency**
   * **Validates: Requirements 19.4**
   *
   * For any browsing history, only items from the past 30 days should be displayed,
   * sorted by view date descending.
   */
  it('should only include items from the past 30 days', () => {
    const referenceDate = new Date('2025-06-15T12:00:00Z')

    fc.assert(
      fc.property(
        fc.array(browsingHistoryItemArbitrary(referenceDate), {
          minLength: 0,
          maxLength: 50
        }),
        (history) => {
          const filtered = filterRecentHistory(history, referenceDate)
          const thirtyDaysAgo = new Date(referenceDate)
          thirtyDaysAgo.setDate(thirtyDaysAgo.getDate() - 30)

          // 所有过滤后的项都应该在30天内
          return filtered.every(
            (item) => new Date(item.visitedAt) >= thirtyDaysAgo
          )
        }
      ),
      { numRuns: 100 }
    )
  })

  it('should exclude items older than 30 days', () => {
    const referenceDate = new Date('2025-06-15T12:00:00Z')

    fc.assert(
      fc.property(
        fc.array(browsingHistoryItemArbitrary(referenceDate), {
          minLength: 0,
          maxLength: 50
        }),
        (history) => {
          const filtered = filterRecentHistory(history, referenceDate)
          const thirtyDaysAgo = new Date(referenceDate)
          thirtyDaysAgo.setDate(thirtyDaysAgo.getDate() - 30)

          // 原始历史中超过30天的项不应该出现在过滤结果中
          const oldItems = history.filter(
            (item) => new Date(item.visitedAt) < thirtyDaysAgo
          )

          return oldItems.every(
            (oldItem) => !filtered.some((f) => f.id === oldItem.id)
          )
        }
      ),
      { numRuns: 100 }
    )
  })

  it('should sort by visit date in descending order (newest first)', () => {
    const referenceDate = new Date('2025-06-15T12:00:00Z')

    fc.assert(
      fc.property(
        fc.array(browsingHistoryItemArbitrary(referenceDate), {
          minLength: 0,
          maxLength: 50
        }),
        (history) => {
          const filtered = filterRecentHistory(history, referenceDate)

          // 检查排序：每个项的时间应该 >= 下一个项的时间
          for (let i = 0; i < filtered.length - 1; i++) {
            const currentTime = new Date(filtered[i].visitedAt).getTime()
            const nextTime = new Date(filtered[i + 1].visitedAt).getTime()
            if (currentTime < nextTime) {
              return false
            }
          }
          return true
        }
      ),
      { numRuns: 100 }
    )
  })

  it('should preserve all items within 30 days', () => {
    const referenceDate = new Date('2025-06-15T12:00:00Z')

    fc.assert(
      fc.property(
        fc.array(browsingHistoryItemArbitrary(referenceDate), {
          minLength: 0,
          maxLength: 50
        }),
        (history) => {
          const filtered = filterRecentHistory(history, referenceDate)
          const thirtyDaysAgo = new Date(referenceDate)
          thirtyDaysAgo.setDate(thirtyDaysAgo.getDate() - 30)

          // 原始历史中30天内的项都应该出现在过滤结果中
          const recentItems = history.filter(
            (item) => new Date(item.visitedAt) >= thirtyDaysAgo
          )

          return recentItems.every((recentItem) =>
            filtered.some((f) => f.id === recentItem.id)
          )
        }
      ),
      { numRuns: 100 }
    )
  })

  it('should return empty array for empty history', () => {
    const referenceDate = new Date('2025-06-15T12:00:00Z')
    const result = filterRecentHistory([], referenceDate)
    expect(result).toEqual([])
  })

  it('should handle history with all items older than 30 days', () => {
    const referenceDate = new Date('2025-06-15T12:00:00Z')

    fc.assert(
      fc.property(
        fc.array(
          fc.record({
            id: fc.uuid(),
            // 生成31-60天前的日期
            visitedAt: fc.integer({ min: 31, max: 60 }).map((daysAgo) => {
              const date = new Date(referenceDate)
              date.setDate(date.getDate() - daysAgo)
              return date.toISOString()
            })
          }),
          { minLength: 1, maxLength: 20 }
        ),
        (history) => {
          const filtered = filterRecentHistory(history, referenceDate)
          return filtered.length === 0
        }
      ),
      { numRuns: 100 }
    )
  })

  it('should handle history with all items within 30 days', () => {
    const referenceDate = new Date('2025-06-15T12:00:00Z')

    fc.assert(
      fc.property(
        fc.array(
          fc.record({
            id: fc.uuid(),
            // 生成0-29天前的日期
            visitedAt: fc.integer({ min: 0, max: 29 }).map((daysAgo) => {
              const date = new Date(referenceDate)
              date.setDate(date.getDate() - daysAgo)
              return date.toISOString()
            })
          }),
          { minLength: 1, maxLength: 20 }
        ),
        (history) => {
          const filtered = filterRecentHistory(history, referenceDate)
          return filtered.length === history.length
        }
      ),
      { numRuns: 100 }
    )
  })
})
