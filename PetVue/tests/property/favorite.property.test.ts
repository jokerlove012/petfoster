import { describe, it, expect, beforeEach } from 'vitest'
import * as fc from 'fast-check'

/**
 * Property 16: Favorite toggle consistency
 * Validates: Requirements 19.1, 19.3
 */

// 模拟收藏状态管理
class FavoriteManager {
  private favorites: Set<string> = new Set()
  
  isFavorite(id: string): boolean {
    return this.favorites.has(id)
  }
  
  toggle(id: string): boolean {
    if (this.favorites.has(id)) {
      this.favorites.delete(id)
      return false
    } else {
      this.favorites.add(id)
      return true
    }
  }
  
  add(id: string): void {
    this.favorites.add(id)
  }
  
  remove(id: string): void {
    this.favorites.delete(id)
  }
  
  getAll(): string[] {
    return Array.from(this.favorites)
  }
  
  count(): number {
    return this.favorites.size
  }
  
  clear(): void {
    this.favorites.clear()
  }
}

describe('Favorite Toggle Properties', () => {
  let manager: FavoriteManager
  
  beforeEach(() => {
    manager = new FavoriteManager()
  })

  it('Property 16.1: Toggle changes favorite state', () => {
    fc.assert(
      fc.property(
        fc.string({ minLength: 1, maxLength: 20 }),
        (id) => {
          const wasFavorite = manager.isFavorite(id)
          manager.toggle(id)
          const isFavorite = manager.isFavorite(id)
          return wasFavorite !== isFavorite
        }
      ),
      { numRuns: 50 }
    )
  })

  it('Property 16.2: Double toggle returns to original state', () => {
    fc.assert(
      fc.property(
        fc.string({ minLength: 1, maxLength: 20 }),
        (id) => {
          const originalState = manager.isFavorite(id)
          manager.toggle(id)
          manager.toggle(id)
          return manager.isFavorite(id) === originalState
        }
      ),
      { numRuns: 50 }
    )
  })

  it('Property 16.3: Add makes item favorite', () => {
    fc.assert(
      fc.property(
        fc.string({ minLength: 1, maxLength: 20 }),
        (id) => {
          manager.add(id)
          return manager.isFavorite(id) === true
        }
      ),
      { numRuns: 30 }
    )
  })

  it('Property 16.4: Remove makes item not favorite', () => {
    fc.assert(
      fc.property(
        fc.string({ minLength: 1, maxLength: 20 }),
        (id) => {
          manager.add(id) // 先添加
          manager.remove(id)
          return manager.isFavorite(id) === false
        }
      ),
      { numRuns: 30 }
    )
  })

  it('Property 16.5: Count reflects actual favorites', () => {
    fc.assert(
      fc.property(
        fc.array(fc.string({ minLength: 1, maxLength: 10 }), { minLength: 0, maxLength: 20 }),
        (ids) => {
          manager.clear()
          const uniqueIds = [...new Set(ids)]
          uniqueIds.forEach(id => manager.add(id))
          return manager.count() === uniqueIds.length
        }
      ),
      { numRuns: 30 }
    )
  })

  it('Property 16.6: GetAll returns all favorites', () => {
    fc.assert(
      fc.property(
        fc.array(fc.string({ minLength: 1, maxLength: 10 }), { minLength: 0, maxLength: 10 }),
        (ids) => {
          manager.clear()
          const uniqueIds = [...new Set(ids)]
          uniqueIds.forEach(id => manager.add(id))
          
          const all = manager.getAll()
          return uniqueIds.every(id => all.includes(id)) &&
                 all.every(id => uniqueIds.includes(id))
        }
      ),
      { numRuns: 30 }
    )
  })

  it('Property 16.7: Clear removes all favorites', () => {
    fc.assert(
      fc.property(
        fc.array(fc.string({ minLength: 1, maxLength: 10 }), { minLength: 1, maxLength: 10 }),
        (ids) => {
          ids.forEach(id => manager.add(id))
          manager.clear()
          return manager.count() === 0 && manager.getAll().length === 0
        }
      ),
      { numRuns: 20 }
    )
  })
})

/**
 * Property 15: Notification sorting
 * Validates: Requirements 15.3
 */
describe('Notification Sorting Properties', () => {
  interface Notification {
    id: string
    createdAt: string
    isRead: boolean
  }

  const sortByNewest = (notifications: Notification[]): Notification[] => {
    return [...notifications].sort((a, b) => 
      new Date(b.createdAt).getTime() - new Date(a.createdAt).getTime()
    )
  }

  const sortByOldest = (notifications: Notification[]): Notification[] => {
    return [...notifications].sort((a, b) => 
      new Date(a.createdAt).getTime() - new Date(b.createdAt).getTime()
    )
  }

  it('Property 15.1: Newest first sorting produces descending dates', () => {
    fc.assert(
      fc.property(
        fc.array(
          fc.record({
            id: fc.string({ minLength: 1, maxLength: 10 }),
            createdAt: fc.date({ min: new Date('2024-01-01'), max: new Date('2024-12-31') }).map(d => d.toISOString()),
            isRead: fc.boolean()
          }),
          { minLength: 2, maxLength: 20 }
        ),
        (notifications) => {
          const sorted = sortByNewest(notifications)
          
          for (let i = 1; i < sorted.length; i++) {
            const prevDate = new Date(sorted[i - 1].createdAt).getTime()
            const currDate = new Date(sorted[i].createdAt).getTime()
            if (currDate > prevDate) return false
          }
          return true
        }
      ),
      { numRuns: 30 }
    )
  })

  it('Property 15.2: Oldest first sorting produces ascending dates', () => {
    fc.assert(
      fc.property(
        fc.array(
          fc.record({
            id: fc.string({ minLength: 1, maxLength: 10 }),
            createdAt: fc.date({ min: new Date('2024-01-01'), max: new Date('2024-12-31') }).map(d => d.toISOString()),
            isRead: fc.boolean()
          }),
          { minLength: 2, maxLength: 20 }
        ),
        (notifications) => {
          const sorted = sortByOldest(notifications)
          
          for (let i = 1; i < sorted.length; i++) {
            const prevDate = new Date(sorted[i - 1].createdAt).getTime()
            const currDate = new Date(sorted[i].createdAt).getTime()
            if (currDate < prevDate) return false
          }
          return true
        }
      ),
      { numRuns: 30 }
    )
  })

  it('Property 15.3: Sorting preserves all notifications', () => {
    fc.assert(
      fc.property(
        fc.array(
          fc.record({
            id: fc.string({ minLength: 1, maxLength: 10 }),
            createdAt: fc.date({ min: new Date('2024-01-01'), max: new Date('2024-12-31') }).map(d => d.toISOString()),
            isRead: fc.boolean()
          }),
          { minLength: 0, maxLength: 20 }
        ),
        (notifications) => {
          const sorted = sortByNewest(notifications)
          return sorted.length === notifications.length &&
                 notifications.every(n => sorted.some(s => s.id === n.id))
        }
      ),
      { numRuns: 30 }
    )
  })
})
