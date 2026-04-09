<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { Star, MessageSquare, ThumbsUp, Clock, Filter, Search } from 'lucide-vue-next'
import { ElMessage } from 'element-plus'
import api from '@/api/index'

const activeTab = ref<'pending' | 'replied' | 'all'>('pending')
const searchQuery = ref('')
const filterRating = ref<number | null>(null)
const loading = ref(false)

// 评价列表
const reviews = ref<any[]>([])

// 获取综合评分
const getOverallRating = (review: any) => {
  if (!review.rating) return 0
  if (typeof review.rating === 'number') return review.rating
  return review.rating.overall || 0
}

// 获取各维度评分
const getServiceRating = (review: any) => {
  if (!review.rating) return 0
  if (typeof review.rating === 'number') return review.rating
  return review.rating.service || review.rating.overall || 0
}

const getCleanRating = (review: any) => {
  if (!review.rating) return 0
  if (typeof review.rating === 'number') return review.rating
  return review.rating.hygiene || review.rating.environment || review.rating.overall || 0
}

const getCommunicationRating = (review: any) => {
  if (!review.rating) return 0
  if (typeof review.rating === 'number') return review.rating
  return review.rating.communication || review.rating.overall || 0
}

// 加载评价列表
const loadReviews = async () => {
  loading.value = true
  try {
    const res = await api.get('/institution/reviews')
    if (res.code === 200 && res.data) {
      reviews.value = res.data.list || res.data || []
    }
  } catch (error) {
    console.error('Failed to load reviews:', error)
  } finally {
    loading.value = false
  }
}

const replyContent = ref('')
const replyingId = ref<string | null>(null)

const filteredReviews = computed(() => {
  let result = reviews.value
  
  if (activeTab.value === 'pending') {
    result = result.filter(r => !r.replied)
  } else if (activeTab.value === 'replied') {
    result = result.filter(r => r.replied)
  }
  
  if (filterRating.value) {
    result = result.filter(r => Math.round(getOverallRating(r)) === filterRating.value)
  }
  
  if (searchQuery.value) {
    const query = searchQuery.value.toLowerCase()
    result = result.filter(r => 
      (r.userName || '').toLowerCase().includes(query) ||
      (r.petName || '').toLowerCase().includes(query) ||
      (r.content || '').toLowerCase().includes(query)
    )
  }
  
  return result
})

const pendingCount = computed(() => reviews.value.filter(r => !r.replied).length)
const averageRating = computed(() => {
  if (reviews.value.length === 0) return 0
  const total = reviews.value.reduce((sum, r) => sum + getOverallRating(r), 0)
  return (total / reviews.value.length).toFixed(1)
})

const startReply = (reviewId: string) => {
  replyingId.value = reviewId
  replyContent.value = ''
}

const cancelReply = () => {
  replyingId.value = null
  replyContent.value = ''
}

const submitReply = async (review: typeof reviews.value[0]) => {
  if (!replyContent.value.trim()) {
    ElMessage.warning('请输入回复内容')
    return
  }
  
  try {
    await api.post(`/institution/reviews/${review.id}/reply`, { content: replyContent.value })
    review.reply = replyContent.value
    review.replied = true
    replyingId.value = null
    replyContent.value = ''
    ElMessage.success('回复成功')
  } catch (error) {
    ElMessage.error('回复失败')
  }
}

const renderStars = (rating: number) => {
  const r = Math.round(rating || 0)
  return '★'.repeat(Math.min(r, 5)) + '☆'.repeat(Math.max(5 - r, 0))
}

onMounted(() => {
  loadReviews()
})
</script>

<template>
  <div class="review-manage-view">
    <div class="page-header">
      <div class="header-left">
        <h1>⭐ 评价管理</h1>
        <p>查看和回复客户评价</p>
      </div>
      <div class="header-stats">
        <div class="stat-item rating">
          <span class="stat-value">{{ averageRating }}</span>
          <span class="stat-label">平均评分</span>
        </div>
        <div class="stat-item pending">
          <span class="stat-value">{{ pendingCount }}</span>
          <span class="stat-label">待回复</span>
        </div>
        <div class="stat-item total">
          <span class="stat-value">{{ reviews.length }}</span>
          <span class="stat-label">总评价</span>
        </div>
      </div>
    </div>

    <!-- 筛选栏 -->
    <div class="filter-bar">
      <div class="tabs">
        <button class="tab-btn" :class="{ active: activeTab === 'pending' }" @click="activeTab = 'pending'">
          待回复 <span class="count">{{ pendingCount }}</span>
        </button>
        <button class="tab-btn" :class="{ active: activeTab === 'replied' }" @click="activeTab = 'replied'">
          已回复
        </button>
        <button class="tab-btn" :class="{ active: activeTab === 'all' }" @click="activeTab = 'all'">
          全部
        </button>
      </div>
      <div class="filters">
        <div class="search-box">
          <Search :size="16" />
          <input v-model="searchQuery" placeholder="搜索评价..." />
        </div>
        <div class="rating-filter">
          <Filter :size="16" />
          <select v-model="filterRating">
            <option :value="null">全部评分</option>
            <option :value="5">5星</option>
            <option :value="4">4星</option>
            <option :value="3">3星</option>
            <option :value="2">2星</option>
            <option :value="1">1星</option>
          </select>
        </div>
      </div>
    </div>

    <!-- 评价列表 -->
    <div class="reviews-list">
      <div v-for="review in filteredReviews" :key="review.id" class="review-card">
        <div class="review-header">
          <div class="user-info">
            <span class="user-avatar">{{ review.userAvatar }}</span>
            <div class="user-detail">
              <span class="user-name">{{ review.userName }}</span>
              <span class="pet-name">宠物: {{ review.petName }}</span>
            </div>
          </div>
          <div class="review-meta">
            <span class="review-date"><Clock :size="14" /> {{ review.date }}</span>
            <span class="order-id">订单: {{ review.orderId }}</span>
          </div>
        </div>

        <div class="review-ratings">
          <div class="rating-item">
            <span class="rating-label">综合评分</span>
            <span class="rating-stars" :class="{ low: getOverallRating(review) <= 2 }">{{ renderStars(getOverallRating(review)) }}</span>
          </div>
          <div class="rating-item">
            <span class="rating-label">服务质量</span>
            <span class="rating-stars">{{ renderStars(getServiceRating(review)) }}</span>
          </div>
          <div class="rating-item">
            <span class="rating-label">环境卫生</span>
            <span class="rating-stars">{{ renderStars(getCleanRating(review)) }}</span>
          </div>
          <div class="rating-item">
            <span class="rating-label">沟通效率</span>
            <span class="rating-stars">{{ renderStars(getCommunicationRating(review)) }}</span>
          </div>
        </div>

        <div class="review-content">
          <p>{{ review.content }}</p>
          <div v-if="review.images.length" class="review-images">
            <span v-for="img in review.images" :key="img" class="image-thumb">📷</span>
          </div>
        </div>

        <div class="review-footer">
          <span class="helpful"><ThumbsUp :size="14" /> {{ review.helpful }} 人觉得有帮助</span>
        </div>

        <!-- 已回复 -->
        <div v-if="review.replied && review.reply" class="reply-section replied">
          <div class="reply-header">
            <span class="reply-label">🏠 商家回复</span>
          </div>
          <p class="reply-content">{{ typeof review.reply === 'object' ? review.reply.content : review.reply }}</p>
        </div>

        <!-- 回复输入框 -->
        <div v-else-if="replyingId === review.id" class="reply-section editing">
          <textarea v-model="replyContent" placeholder="输入回复内容..." rows="3"></textarea>
          <div class="reply-actions">
            <button class="btn-cancel" @click="cancelReply">取消</button>
            <button class="btn-submit" @click="submitReply(review)">提交回复</button>
          </div>
        </div>

        <!-- 回复按钮 -->
        <div v-else class="reply-action">
          <button class="btn-reply" @click="startReply(review.id)">
            <MessageSquare :size="16" /> 回复评价
          </button>
        </div>
      </div>

      <div v-if="filteredReviews.length === 0" class="empty-state">
        <span class="empty-icon">📝</span>
        <p>暂无评价</p>
      </div>
    </div>
  </div>
</template>

<style lang="scss" scoped>
.review-manage-view { max-width: 1000px; margin: 0 auto; padding: 24px; }

.page-header {
  display: flex; justify-content: space-between; align-items: flex-start; margin-bottom: 24px;
  .header-left {
    h1 { font-size: 26px; font-weight: 700; margin: 0 0 6px; }
    p { color: #6B6560; margin: 0; }
  }
  .header-stats {
    display: flex; gap: 16px;
    .stat-item {
      text-align: center; padding: 12px 20px; border-radius: 12px; background: white;
      box-shadow: 0 2px 8px rgba(0,0,0,0.04);
      &.rating { border-left: 4px solid #faad14; }
      &.pending { border-left: 4px solid #ff4d4f; }
      &.total { border-left: 4px solid #1890ff; }
      .stat-value { display: block; font-size: 24px; font-weight: 700; }
      .stat-label { font-size: 12px; color: #6B6560; }
    }
  }
}

.filter-bar {
  display: flex; justify-content: space-between; align-items: center; margin-bottom: 24px; flex-wrap: wrap; gap: 16px;
  .tabs { display: flex; gap: 8px; }
  .tab-btn {
    display: flex; align-items: center; gap: 6px;
    padding: 10px 16px; border: none; background: white; border-radius: 8px;
    font-size: 14px; color: #6B6560; cursor: pointer;
    .count { background: #F0F0EF; padding: 2px 8px; border-radius: 10px; font-size: 12px; }
    &.active { background: #faad14; color: white; .count { background: rgba(255,255,255,0.3); } }
  }
  .filters { display: flex; gap: 12px; }
  .search-box {
    display: flex; align-items: center; gap: 8px; padding: 8px 14px;
    background: white; border-radius: 8px; border: 1px solid #E5E5E5;
    input { border: none; outline: none; font-size: 14px; width: 150px; }
  }
  .rating-filter {
    display: flex; align-items: center; gap: 8px; padding: 8px 14px;
    background: white; border-radius: 8px; border: 1px solid #E5E5E5;
    select { border: none; outline: none; font-size: 14px; background: transparent; }
  }
}

.reviews-list { display: flex; flex-direction: column; gap: 16px; }

.review-card {
  background: white; border-radius: 16px; padding: 20px; box-shadow: 0 2px 8px rgba(0,0,0,0.04);
  .review-header {
    display: flex; justify-content: space-between; align-items: flex-start; margin-bottom: 16px;
    .user-info { display: flex; align-items: center; gap: 12px;
      .user-avatar { font-size: 32px; }
      .user-detail { display: flex; flex-direction: column;
        .user-name { font-weight: 600; }
        .pet-name { font-size: 12px; color: #6B6560; }
      }
    }
    .review-meta { text-align: right;
      .review-date { display: flex; align-items: center; gap: 4px; font-size: 13px; color: #6B6560; }
      .order-id { font-size: 12px; color: #9A958F; }
    }
  }
  .review-ratings {
    display: flex; gap: 24px; margin-bottom: 16px; flex-wrap: wrap;
    .rating-item {
      .rating-label { display: block; font-size: 12px; color: #6B6560; margin-bottom: 4px; }
      .rating-stars { color: #faad14; font-size: 14px; &.low { color: #ff4d4f; } }
    }
  }
  .review-content {
    p { margin: 0 0 12px; line-height: 1.6; }
    .review-images { display: flex; gap: 8px; .image-thumb { font-size: 24px; } }
  }
  .review-footer { margin-top: 12px; padding-top: 12px; border-top: 1px solid #F0F0EF;
    .helpful { display: flex; align-items: center; gap: 6px; font-size: 13px; color: #6B6560; }
  }
}

.reply-section {
  margin-top: 16px; padding: 16px; background: #F8F8F7; border-radius: 12px;
  &.replied {
    .reply-header { margin-bottom: 8px; .reply-label { font-size: 13px; font-weight: 600; color: #52c41a; } }
    .reply-content { margin: 0; font-size: 14px; color: #2D2A26; }
  }
  &.editing {
    textarea { width: 100%; padding: 12px; border: 1px solid #E5E5E5; border-radius: 8px; font-size: 14px; resize: none; }
    .reply-actions { display: flex; justify-content: flex-end; gap: 12px; margin-top: 12px; }
  }
}

.reply-action { margin-top: 16px; }
.btn-reply {
  display: flex; align-items: center; gap: 6px; padding: 8px 16px;
  border: 1px solid #1890ff; background: white; color: #1890ff;
  border-radius: 8px; font-size: 13px; cursor: pointer;
  &:hover { background: #E8F4FD; }
}

.btn-cancel, .btn-submit {
  padding: 8px 20px; border: none; border-radius: 8px; font-size: 13px; cursor: pointer;
}
.btn-cancel { background: #F0F0EF; color: #6B6560; }
.btn-submit { background: #1890ff; color: white; }

.empty-state { text-align: center; padding: 60px; .empty-icon { font-size: 48px; } p { color: #9A958F; } }
</style>
