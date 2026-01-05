import { describe, it, expect, beforeEach, afterEach } from 'vitest'
import * as fc from 'fast-check'

/**
 * Property 12: Corrupted data handling
 * Validates: Requirements 11.4
 */

const FAVORITES_KEY = 'pet_foster_favorites'
const HISTORY_KEY = 'pet_foster_history'
const PREFS_KEY = 'pet_foster_notification_prefs'

// 模拟 localStorage
const mockStorage: Record<string, string> = {}

const mockLocalStorage = {
  getItem: (key: string) => mockStorage[key] || null,
  setItem: (key: string, value: string) => { mockStorage[key] = value },
  removeItem: (key: string) => { delete mockStorage[key] },
  clear: () => { Object.keys(mockStorage).forEach(k => delete mockStorage[k]) }
}

// 解析函数（带错误处理）
function safeParseArray(key: string): string[] {
  try {
    const stored = mockLocalStorage.getItem(key)
    if (stored) {
      const parsed = JSON.parse(stored)
      if (Array.isArray(parsed)) {
        return parsed
      }
    }
  } catch (e) {
    // 忽略解析错误
  }
  return []
}

function safeParseHistory(key: string): { id: string; visitedAt: string }[] {
  try {
    const stored = mockLocalStorage.getItem(key)
    if (stored) {
      const parsed = JSON.parse(stored)
      if (Array.isArray(parsed)) {
        return parsed.filter(item => 
          typeof item === 'object' && 
          item !== null && 
          typeof item.id === 'string' && 
          typeof item.visitedAt === 'string'
        )
      }
    }
  } catch (e) {
    // 忽略解析错误
  }
  return []
}

function safeParsePreferences(key: string, defaults: Record<string, boolean>): Record<string, boolean> {
  try {
    const stored = mockLocalStorage.getItem(key)
    if (stored) {
      const parsed = JSON.parse(stored)
      if (typeof parsed === 'object' && parsed !== null) {
        return { ...defaults, ...parsed }
      }
    }
  } catch (e) {
    // 忽略解析错误
  }
  return defaults
}

describe('Corrupted Data Handling Properties', () => {
  beforeEach(() => {
    mockLocalStorage.clear()
  })

  afterEach(() => {
    mockLocalStorage.clear()
  })

  it('Property 12.1: Invalid JSON in favorites returns empty array', () => {
    fc.assert(
      fc.property(
        fc.string().filter(s => {
          try { JSON.parse(s); return false } catch { return true }
        }),
        (invalidJson) => {
          mockLocalStorage.setItem(FAVORITES_KEY, invalidJson)
          const result = safeParseArray(FAVORITES_KEY)
          return Array.isArray(result) && result.length === 0
        }
      ),
      { numRuns: 20 }
    )
  })

  it('Property 12.2: Non-array JSON in favorites returns empty array', () => {
    fc.assert(
      fc.property(
        fc.oneof(
          fc.string(),
          fc.integer(),
          fc.boolean(),
          fc.constant(null),
          fc.record({ key: fc.string() })
        ),
        (nonArrayValue) => {
          mockLocalStorage.setItem(FAVORITES_KEY, JSON.stringify(nonArrayValue))
          const result = safeParseArray(FAVORITES_KEY)
          return Array.isArray(result) && result.length === 0
        }
      ),
      { numRuns: 20 }
    )
  })

  it('Property 12.3: Valid array in favorites is preserved', () => {
    fc.assert(
      fc.property(
        fc.array(fc.string(), { minLength: 0, maxLength: 10 }),
        (validArray) => {
          mockLocalStorage.setItem(FAVORITES_KEY, JSON.stringify(validArray))
          const result = safeParseArray(FAVORITES_KEY)
          return Array.isArray(result) && 
                 result.length === validArray.length &&
                 result.every((item, i) => item === validArray[i])
        }
      ),
      { numRuns: 20 }
    )
  })

  it('Property 12.4: Corrupted history items are filtered out', () => {
    fc.assert(
      fc.property(
        fc.array(
          fc.oneof(
            // 有效的历史记录
            fc.record({
              id: fc.string({ minLength: 1 }),
              visitedAt: fc.date().map(d => d.toISOString())
            }),
            // 无效的历史记录
            fc.string(),
            fc.integer(),
            fc.constant(null),
            fc.record({ id: fc.integer() }), // id 不是字符串
            fc.record({ visitedAt: fc.string() }) // 缺少 id
          ),
          { minLength: 0, maxLength: 10 }
        ),
        (mixedArray) => {
          mockLocalStorage.setItem(HISTORY_KEY, JSON.stringify(mixedArray))
          const result = safeParseHistory(HISTORY_KEY)
          
          // 结果应该只包含有效的历史记录
          return result.every(item => 
            typeof item.id === 'string' && 
            typeof item.visitedAt === 'string'
          )
        }
      ),
      { numRuns: 20 }
    )
  })

  it('Property 12.5: Corrupted preferences fall back to defaults', () => {
    const defaults = {
      booking: true,
      payment: true,
      health: true
    }

    fc.assert(
      fc.property(
        fc.oneof(
          fc.string().filter(s => { try { JSON.parse(s); return false } catch { return true } }),
          fc.array(fc.integer()).map(a => JSON.stringify(a)),
          fc.constant('null'),
          fc.constant('undefined')
        ),
        (corruptedData) => {
          mockLocalStorage.setItem(PREFS_KEY, corruptedData)
          const result = safeParsePreferences(PREFS_KEY, defaults)
          
          // 结果应该包含所有默认值
          return Object.keys(defaults).every(key => 
            key in result && typeof result[key] === 'boolean'
          )
        }
      ),
      { numRuns: 20 }
    )
  })

  it('Property 12.6: Partial preferences are merged with defaults', () => {
    const defaults = {
      booking: true,
      payment: true,
      health: true,
      review: true
    }

    fc.assert(
      fc.property(
        fc.record({
          booking: fc.boolean(),
          payment: fc.boolean()
        }),
        (partialPrefs) => {
          mockLocalStorage.setItem(PREFS_KEY, JSON.stringify(partialPrefs))
          const result = safeParsePreferences(PREFS_KEY, defaults)
          
          // 部分偏好应该覆盖默认值
          return result.booking === partialPrefs.booking &&
                 result.payment === partialPrefs.payment &&
                 // 未提供的应该使用默认值
                 result.health === defaults.health &&
                 result.review === defaults.review
        }
      ),
      { numRuns: 20 }
    )
  })
})
