/**
 * 评分计算工具
 * 
 * 实现评价系统的评分计算逻辑
 */

import type { Review, ReviewRating } from '@/types/review'

/**
 * 计算综合平均评分
 */
export function calculateAverageRating(reviews: Review[]): number {
  if (reviews.length === 0) return 0
  
  const sum = reviews.reduce((acc, review) => acc + review.rating.overall, 0)
  return Math.round((sum / reviews.length) * 10) / 10
}

/**
 * 计算各维度平均评分
 */
export function calculateDimensionAverages(reviews: Review[]): ReviewRating {
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

/**
 * 计算评分分布
 */
export function calculateRatingDistribution(reviews: Review[]): Record<number, number> {
  const distribution: Record<number, number> = { 1: 0, 2: 0, 3: 0, 4: 0, 5: 0 }
  
  reviews.forEach(review => {
    const rating = Math.round(review.rating.overall)
    if (rating >= 1 && rating <= 5) {
      distribution[rating]++
    }
  })
  
  return distribution
}

/**
 * 计算评分分布百分比
 */
export function calculateRatingPercentages(reviews: Review[]): Record<number, number> {
  const distribution = calculateRatingDistribution(reviews)
  const total = reviews.length
  
  if (total === 0) {
    return { 1: 0, 2: 0, 3: 0, 4: 0, 5: 0 }
  }
  
  const percentages: Record<number, number> = {}
  for (let i = 1; i <= 5; i++) {
    percentages[i] = Math.round((distribution[i] / total) * 100)
  }
  
  return percentages
}

/**
 * 获取评分等级描述
 */
export function getRatingLevel(rating: number): { level: string; color: string } {
  if (rating >= 4.5) return { level: '极好', color: '#52c41a' }
  if (rating >= 4.0) return { level: '很好', color: '#73d13d' }
  if (rating >= 3.5) return { level: '较好', color: '#faad14' }
  if (rating >= 3.0) return { level: '一般', color: '#fa8c16' }
  if (rating >= 2.0) return { level: '较差', color: '#ff7a45' }
  return { level: '很差', color: '#ff4d4f' }
}

/**
 * 格式化评分显示
 */
export function formatRating(rating: number): string {
  return rating.toFixed(1)
}

/**
 * 验证评价内容长度
 */
export function validateReviewContent(
  content: string, 
  minLength = 10, 
  maxLength = 500
): { valid: boolean; error?: string } {
  const trimmed = content.trim()
  
  if (trimmed.length < minLength) {
    return { 
      valid: false, 
      error: `评价内容至少需要 ${minLength} 个字符` 
    }
  }
  
  if (trimmed.length > maxLength) {
    return { 
      valid: false, 
      error: `评价内容不能超过 ${maxLength} 个字符` 
    }
  }
  
  return { valid: true }
}

/**
 * 计算推荐指数
 * 基于评分和评价数量
 */
export function calculateRecommendationScore(
  averageRating: number, 
  reviewCount: number
): number {
  if (reviewCount === 0) return 0
  
  // 贝叶斯平均，考虑评价数量的影响
  const C = 10 // 置信度参数
  const m = 3.5 // 全局平均评分
  
  const score = (reviewCount * averageRating + C * m) / (reviewCount + C)
  return Math.round(score * 10) / 10
}
