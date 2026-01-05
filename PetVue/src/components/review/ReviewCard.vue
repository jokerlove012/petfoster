<script setup lang="ts">
import { computed } from 'vue'
import type { Review } from '@/types/review'
import { Star, User, MessageSquare, Image } from 'lucide-vue-next'
import { formatRating, getRatingLevel } from '@/utils/ratingCalculator'

const props = defineProps<{
  review: Review
  showReply?: boolean
}>()

// 格式化日期
const formattedDate = computed(() => {
  const date = new Date(props.review.createdAt)
  return date.toLocaleDateString('zh-CN', {
    year: 'numeric',
    month: 'long',
    day: 'numeric'
  })
})

// 评分等级
const ratingLevel = computed(() => getRatingLevel(props.review.rating.overall))

// 维度评分
const dimensionRatings = computed(() => [
  { label: '环境', value: props.review.rating.environment },
  { label: '服务', value: props.review.rating.service },
  { label: '卫生', value: props.review.rating.hygiene },
  { label: '沟通', value: props.review.rating.communication }
])

// 用户显示名
const displayName = computed(() => {
  if (props.review.isAnonymous) {
    return '匿名用户'
  }
  return props.review.userId.slice(0, 8) + '***'
})
</script>

<template>
  <div class="review-card">
    <!-- 用户信息 -->
    <div class="review-header">
      <div class="user-info">
        <div class="user-avatar">
          <User :size="20" />
        </div>
        <span class="user-name">{{ displayName }}</span>
        <span class="review-date">{{ formattedDate }}</span>
      </div>
      <div class="overall-rating" :style="{ color: ratingLevel.color }">
        <Star :size="16" fill="currentColor" />
        <span class="rating-value">{{ formatRating(review.rating.overall) }}</span>
        <span class="rating-level">{{ ratingLevel.level }}</span>
      </div>
    </div>

    <!-- 维度评分 -->
    <div class="dimension-ratings">
      <div 
        v-for="dim in dimensionRatings"
        :key="dim.label"
        class="dimension-item"
      >
        <span class="dim-label">{{ dim.label }}</span>
        <div class="dim-stars">
          <Star 
            v-for="star in 5"
            :key="star"
            :size="12"
            :fill="star <= dim.value ? '#FFB800' : 'none'"
            :stroke="star <= dim.value ? '#FFB800' : 'currentColor'"
          />
        </div>
      </div>
    </div>

    <!-- 评价内容 -->
    <div class="review-content">
      <p>{{ review.content }}</p>
    </div>

    <!-- 评价图片 -->
    <div v-if="review.images && review.images.length > 0" class="review-images">
      <div 
        v-for="(img, index) in review.images"
        :key="index"
        class="image-item"
      >
        <img :src="img" :alt="`评价图片 ${index + 1}`" />
      </div>
      <div v-if="review.images.length > 0" class="image-count">
        <Image :size="14" />
        <span>{{ review.images.length }}</span>
      </div>
    </div>

    <!-- 商家回复 -->
    <div v-if="showReply && review.reply" class="review-reply">
      <div class="reply-header">
        <MessageSquare :size="14" />
        <span>商家回复</span>
        <span class="reply-date">
          {{ new Date(review.reply.repliedAt).toLocaleDateString('zh-CN') }}
        </span>
      </div>
      <p class="reply-content">{{ review.reply.content }}</p>
    </div>
  </div>
</template>

<style lang="scss" scoped>
@import '@/styles/variables.scss';

.review-card {
  background: var(--color-surface);
  border-radius: var(--radius-lg);
  padding: 16px;
  border: 1px solid var(--color-border);
}

.review-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 12px;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 8px;
}

.user-avatar {
  width: 32px;
  height: 32px;
  background: var(--color-background);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  color: var(--color-text-secondary);
}

.user-name {
  font-size: 14px;
  font-weight: 500;
}

.review-date {
  font-size: 12px;
  color: var(--color-text-secondary);
}

.overall-rating {
  display: flex;
  align-items: center;
  gap: 4px;
  
  .rating-value {
    font-size: 16px;
    font-weight: 600;
  }
  
  .rating-level {
    font-size: 12px;
    opacity: 0.8;
  }
}

.dimension-ratings {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
  margin-bottom: 12px;
  padding: 12px;
  background: var(--color-background);
  border-radius: var(--radius-md);
}

.dimension-item {
  display: flex;
  align-items: center;
  gap: 6px;
  
  .dim-label {
    font-size: 12px;
    color: var(--color-text-secondary);
    min-width: 28px;
  }
  
  .dim-stars {
    display: flex;
    gap: 2px;
    color: var(--color-border);
  }
}

.review-content {
  margin-bottom: 12px;
  
  p {
    font-size: 14px;
    line-height: 1.6;
    color: var(--color-text-primary);
    margin: 0;
  }
}

.review-images {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  margin-bottom: 12px;
  position: relative;
}

.image-item {
  width: 80px;
  height: 80px;
  border-radius: var(--radius-md);
  overflow: hidden;
  
  img {
    width: 100%;
    height: 100%;
    object-fit: cover;
  }
}

.image-count {
  position: absolute;
  bottom: 4px;
  right: 4px;
  display: flex;
  align-items: center;
  gap: 4px;
  padding: 4px 8px;
  background: rgba(0, 0, 0, 0.6);
  border-radius: var(--radius-sm);
  color: white;
  font-size: 12px;
}

.review-reply {
  padding: 12px;
  background: var(--color-primary-light);
  border-radius: var(--radius-md);
  border-left: 3px solid var(--color-primary);
}

.reply-header {
  display: flex;
  align-items: center;
  gap: 6px;
  margin-bottom: 8px;
  font-size: 12px;
  color: var(--color-primary);
  font-weight: 500;
  
  .reply-date {
    color: var(--color-text-secondary);
    font-weight: 400;
    margin-left: auto;
  }
}

.reply-content {
  font-size: 13px;
  line-height: 1.5;
  color: var(--color-text-primary);
  margin: 0;
}
</style>
