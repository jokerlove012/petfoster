/**
 * Property-Based Tests for Review System
 * 
 * Tests the correctness properties of review text validation and rating calculation.
 */

import { describe, it, expect } from 'vitest'
import * as fc from 'fast-check'
import type { ReviewRating, Review } from '@/types/review'

// 评价内容长度限制
const CONTENT_MIN_LENGTH = 10
const CONTENT_MAX_LENGTH = 500

/**
 * 验证评价内容长度
 */
function validateReviewContent(content: string): { valid: boolean; error?: string } {
  const trimmed = content.trim()
  
  if (trimmed.length < CONTENT_MIN_LENGTH) {
    return { 
      valid: false, 
      error: `评价内容至少需要 ${CONTENT_MIN_LENGTH} 个字符` 
    }
  }
  
  if (trimmed.length > CONTENT_MAX_LENGTH) {
    return { 
      valid: false, 
      error: `评价内容不能超过 ${CONTENT_MAX_LENGTH} 个字符` 
    }
  }
  
  return { valid: true }
}

/**
 * 计算平均评分
 */
function calculateAverageRating(reviews: Review[]): number {
  if (reviews.length === 0) return 0
  
  const sum = reviews.reduce((acc, review) => acc + review.rating.overall, 0)
  return Math.round((sum / reviews.length) * 10) / 10
}

/**
 * 计算多维度平均评分
 */
function calculateDimensionAverages(reviews: Review[]): ReviewRating {
  if (reviews.length === 0) {
    return { overall: 0, environment: 0, service: 0, hygiene: 0, communication: 0 }
  }
  
  const dimensions: (keyof ReviewRating)[] = ['overall', 'environment', 'service', 'hygiene', 'communication']
  const result: ReviewRating = { overall: 0, environment: 0, service: 0, hygiene: 0, communication: 0 }
  
  for (const dim of dimensions) {
    const sum = reviews.reduce((acc, review) => acc + review.rating[dim], 0)
    result[dim] = Math.round((sum / reviews.length) * 10) / 10
  }
  
  return result
}

// 评分生成器 (1-5)
const ratingValueArb = fc.integer({ min: 1, max: 5 })

const reviewRatingArb = fc.record({
  overall: ratingValueArb,
  environment: ratingValueArb,
  service: ratingValueArb,
  hygiene: ratingValueArb,
  communication: ratingValueArb
}) as fc.Arbitrary<ReviewRating>

// 评价生成器
const reviewArb = fc.record({
  id: fc.uuid(),
  bookingId: fc.uuid(),
  userId: fc.uuid(),
  institutionId: fc.uuid(),
  rating: reviewRatingArb,
  content: fc.string({ minLength: CONTENT_MIN_LENGTH, maxLength: CONTENT_MAX_LENGTH }),
  images: fc.array(fc.string(), { maxLength: 9 }),
  isAnonymous: fc.boolean(),
  createdAt: fc.date().map(d => d.toISOString()),
  updatedAt: fc.date().map(d => d.toISOString())
}) as fc.Arbitrary<Review>

describe('Review Text Validation Properties', () => {
  /**
   * **Feature: pet-foster-platform, Property 6: Review text length validation**
   * **Validates: Requirements 5.3**
   * 
   * Review text must be between 10-500 characters.
   * Validation should reject text outside this range.
   */
  it('rejects content shorter than minimum length', () => {
    fc.assert(
      fc.property(
        fc.string({ minLength: 0, maxLength: CONTENT_MIN_LENGTH - 1 }),
        (content) => {
          const result = validateReviewContent(content)
          return !result.valid && result.error !== undefined
        }
      ),
      { numRuns: 100 }
    )
  })

  it('accepts content within valid length range', () => {
    fc.assert(
      fc.property(
        fc.string({ minLength: CONTENT_MIN_LENGTH, maxLength: CONTENT_MAX_LENGTH }),
        (content) => {
          // 确保内容不全是空白
          if (content.trim().length < CONTENT_MIN_LENGTH) {
            return true // 跳过全空白的情况
          }
          const result = validateReviewContent(content)
          return result.valid
        }
      ),
      { numRuns: 100 }
    )
  })

  it('rejects content longer than maximum length', () => {
    fc.assert(
      fc.property(
        // 生成非空白字符确保 trim 后仍然超长
        fc.stringOf(fc.char().filter(c => c.trim().length > 0), { minLength: CONTENT_MAX_LENGTH + 1, maxLength: CONTENT_MAX_LENGTH + 100 }),
        (content) => {
          const result = validateReviewContent(content)
          return !result.valid && result.error !== undefined
        }
      ),
      { numRuns: 100 }
    )
  })

  it('trims whitespace before validation', () => {
    fc.assert(
      fc.property(
        fc.string({ minLength: CONTENT_MIN_LENGTH, maxLength: CONTENT_MAX_LENGTH }),
        fc.string({ minLength: 0, maxLength: 10 }).filter(s => /^\s*$/.test(s)),
        (content, whitespace) => {
          if (content.trim().length < CONTENT_MIN_LENGTH) {
            return true // 跳过
          }
          const paddedContent = whitespace + content + whitespace
          const result = validateReviewContent(paddedContent)
          return result.valid
        }
      ),
      { numRuns: 50 }
    )
  })
})

describe('Average Rating Calculation Properties', () => {
  /**
   * **Feature: pet-foster-platform, Property 7: Average rating calculation**
   * **Validates: Requirements 5.4**
   * 
   * Average rating should be calculated correctly from all reviews.
   * Result should be between 1-5 (or 0 for no reviews).
   */
  it('returns 0 for empty review list', () => {
    const avg = calculateAverageRating([])
    expect(avg).toBe(0)
  })

  it('returns exact rating for single review', () => {
    fc.assert(
      fc.property(
        reviewArb,
        (review) => {
          const avg = calculateAverageRating([review])
          return avg === review.rating.overall
        }
      ),
      { numRuns: 100 }
    )
  })

  it('average is within valid rating range (1-5)', () => {
    fc.assert(
      fc.property(
        fc.array(reviewArb, { minLength: 1, maxLength: 100 }),
        (reviews) => {
          const avg = calculateAverageRating(reviews)
          return avg >= 1 && avg <= 5
        }
      ),
      { numRuns: 100 }
    )
  })

  it('average is between min and max ratings', () => {
    fc.assert(
      fc.property(
        fc.array(reviewArb, { minLength: 1, maxLength: 50 }),
        (reviews) => {
          const avg = calculateAverageRating(reviews)
          const ratings = reviews.map(r => r.rating.overall)
          const min = Math.min(...ratings)
          const max = Math.max(...ratings)
          
          return avg >= min && avg <= max
        }
      ),
      { numRuns: 100 }
    )
  })

  it('average is rounded to one decimal place', () => {
    fc.assert(
      fc.property(
        fc.array(reviewArb, { minLength: 1, maxLength: 50 }),
        (reviews) => {
          const avg = calculateAverageRating(reviews)
          const decimalPlaces = (avg.toString().split('.')[1] || '').length
          return decimalPlaces <= 1
        }
      ),
      { numRuns: 100 }
    )
  })

  it('dimension averages are all within valid range', () => {
    fc.assert(
      fc.property(
        fc.array(reviewArb, { minLength: 1, maxLength: 50 }),
        (reviews) => {
          const avgs = calculateDimensionAverages(reviews)
          const dimensions: (keyof ReviewRating)[] = ['overall', 'environment', 'service', 'hygiene', 'communication']
          
          return dimensions.every(dim => avgs[dim] >= 1 && avgs[dim] <= 5)
        }
      ),
      { numRuns: 100 }
    )
  })

  it('adding a 5-star review increases or maintains average', () => {
    fc.assert(
      fc.property(
        fc.array(reviewArb, { minLength: 1, maxLength: 50 }),
        (reviews) => {
          const originalAvg = calculateAverageRating(reviews)
          
          // 创建一个5星评价
          const fiveStarReview: Review = {
            ...reviews[0],
            id: 'new-review',
            rating: { overall: 5, environment: 5, service: 5, hygiene: 5, communication: 5 }
          }
          
          const newAvg = calculateAverageRating([...reviews, fiveStarReview])
          
          return newAvg >= originalAvg
        }
      ),
      { numRuns: 100 }
    )
  })

  it('adding a 1-star review decreases or maintains average', () => {
    fc.assert(
      fc.property(
        fc.array(reviewArb, { minLength: 1, maxLength: 50 }),
        (reviews) => {
          const originalAvg = calculateAverageRating(reviews)
          
          // 创建一个1星评价
          const oneStarReview: Review = {
            ...reviews[0],
            id: 'new-review',
            rating: { overall: 1, environment: 1, service: 1, hygiene: 1, communication: 1 }
          }
          
          const newAvg = calculateAverageRating([...reviews, oneStarReview])
          
          return newAvg <= originalAvg
        }
      ),
      { numRuns: 100 }
    )
  })
})
