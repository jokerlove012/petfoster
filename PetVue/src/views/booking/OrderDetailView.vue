<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { useAutoRefresh } from '@/composables/useAutoRefresh'
import type { Booking, BookingStatus } from '@/types/booking'
import { AppButton } from '@/components/common'
import { reviewApi } from '@/api/review'
import { bookingApi } from '@/api/booking'
import { healthApi } from '@/api/health'

const route = useRoute()
const router = useRouter()

const orderId = computed(() => route.params.id as string)
const order = ref<Booking | null>(null)
const loading = ref(false)
const showCancelModal = ref(false)
const showReviewModal = ref(false)
const cancelReason = ref('')
const submittingReview = ref(false)

// 扩展订单信息
const institutionInfo = ref({ name: '', address: '', phone: '' })
const petInfo = ref({ name: '', species: 'dog', breed: '', age: 0, weight: 0 })
const packageInfo = ref({ name: '', features: [] as string[] })

// 评价表单
const reviewForm = ref({
  rating: 5,
  content: '',
  tags: [] as string[]
})
const reviewTags = ['环境干净', '服务周到', '宠物开心', '价格合理', '沟通顺畅', '专业护理']

// 宠物状态记录类型
interface PetStatusRecord {
  id: string
  time: string
  type: 'feeding' | 'walking' | 'health' | 'play' | 'rest' | 'photo'
  title: string
  content: string
  images?: string[]
  healthData?: {
    temperature?: number
    weight?: number
    appetite?: 'good' | 'normal' | 'poor'
    mood?: 'happy' | 'normal' | 'anxious'
  }
}

// 宠物状态记录（后续可从API获取）
const petStatusRecords = ref<PetStatusRecord[]>([])

// 获取状态类型配置
const getStatusTypeConfig = (type: PetStatusRecord['type']) => {
  const configs = {
    feeding: { icon: '🍽️', label: '喂食', color: '#F59E0B' },
    walking: { icon: '🚶', label: '遛弯', color: '#22C55E' },
    health: { icon: '💊', label: '健康', color: '#3B82F6' },
    play: { icon: '🎾', label: '玩耍', color: '#EC4899' },
    rest: { icon: '😴', label: '休息', color: '#8B5CF6' },
    photo: { icon: '📷', label: '照片', color: '#06B6D4' }
  }
  return configs[type]
}

// 获取食欲文字
const getAppetiteText = (appetite: string) => {
  const map: Record<string, string> = { 
    good: '良好', 
    normal: '正常', 
    poor: '较差',
    more: '良好',
    less: '较差'
  }
  return map[appetite] || appetite
}

// 获取订单状态配置
const getStatusConfig = (status: BookingStatus) => {
  const configs: Record<BookingStatus, { label: string; color: string; bgColor: string; icon: string }> = {
    pending: { label: '待确认', color: '#F59E0B', bgColor: 'rgba(245, 158, 11, 0.1)', icon: '⏳' },
    confirmed: { label: '已确认', color: '#3B82F6', bgColor: 'rgba(59, 130, 246, 0.1)', icon: '✅' },
    in_progress: { label: '进行中', color: '#22C55E', bgColor: 'rgba(34, 197, 94, 0.1)', icon: '🏠' },
    completed: { label: '已完成', color: '#6B7280', bgColor: 'rgba(107, 114, 128, 0.1)', icon: '🎉' },
    cancelled: { label: '已取消', color: '#EF4444', bgColor: 'rgba(239, 68, 68, 0.1)', icon: '❌' }
  }
  return configs[status]
}

// 加载订单详情
const loadOrder = async () => {
  try {
    const res = await bookingApi.getDetail(orderId.value)
    if (res.code === 200 && res.data) {
      order.value = res.data
      // 从返回数据中提取扩展信息
      const data = res.data as any
      institutionInfo.value = {
        name: data.institutionName || '未知机构',
        address: data.institutionAddress || '',
        phone: data.institutionPhone || ''
      }
      petInfo.value = {
        name: data.petName || '未知宠物',
        species: data.petSpecies || 'dog',
        breed: data.petBreed || '',
        age: data.petAge || 0,
        weight: data.petWeight || 0
      }
      packageInfo.value = {
        name: data.packageName || '标准套餐',
        features: data.packageFeatures || []
      }
      
      // 加载健康记录
      await loadHealthRecords()
    }
  } catch (error) {
    console.error('Failed to load order:', error)
  }
}

// 首次加载
const initialLoad = async () => {
  loading.value = true
  try {
    await loadOrder()
  } finally {
    loading.value = false
  }
}

// 自动刷新（进行中的订单5秒刷新，其他10秒）
const { isAutoRefresh } = useAutoRefresh(loadOrder, 5000)

// 加载健康记录
const loadHealthRecords = async () => {
  try {
    const res = await healthApi.getByBookingId(orderId.value)
    if (res.code === 200 && res.data) {
      // 转换后端数据格式为前端 PetStatusRecord 格式
      petStatusRecords.value = res.data.map((record: any) => {
        // 根据记录内容确定类型
        let type: PetStatusRecord['type'] = 'health'
        if (record.feedingStatus) type = 'feeding'
        else if (record.activityLevel === 'high' || record.activityLevel === 'inactive') type = 'walking'
        else if (record.photos?.length > 0) type = 'photo'
        
        // 格式化时间 - 优先使用 createdAt，完整显示时间
        const date = new Date(record.createdAt || record.date)
        const timeStr = date.toLocaleString('zh-CN', { 
          year: 'numeric',
          month: '2-digit', 
          day: '2-digit', 
          hour: '2-digit', 
          minute: '2-digit' 
        })
        
        // 构建标题
        let title = '健康记录'
        if (record.feedingStatus) title = '喂食记录'
        else if (record.activityLevel) title = '活动记录'
        
        // 构建内容 - 显示所有字段
        const contents: string[] = []
        if (record.feedingStatus) contents.push(`喂食: ${getFeedingText(record.feedingStatus)}`)
        if (record.activityLevel) contents.push(`活动: ${getActivityText(record.activityLevel)}`)
        if (record.mood) contents.push(`心情: ${getMoodText(record.mood)}`)
        if (record.healthObservations) contents.push(`观察: ${record.healthObservations}`)
        if (record.abnormalDetails) contents.push(`异常: ${record.abnormalDetails}`)
        if (record.medications && record.medications.length > 0) contents.push(`用药: ${record.medications.join(', ')}`)
        
        return {
          id: record.id,
          time: timeStr,
          type,
          title,
          content: contents.join('；') || '暂无详细记录',
          images: record.photos || [],
          healthData: {
            temperature: record.temperature,
            weight: record.weight,
            appetite: record.feedingStatus === 'normal' ? 'normal' : 
                      record.feedingStatus === 'reduced' || record.feedingStatus === 'less' ? 'poor' : 
                      record.feedingStatus === 'refused' || record.feedingStatus === 'none' ? 'poor' : 'good',
            mood: record.mood === 'happy' ? 'happy' : 
                  record.mood === 'anxious' ? 'anxious' : 
                  record.mood === 'stressed' ? 'anxious' : 'normal'
          }
        } as PetStatusRecord
      })
    }
  } catch (error) {
    console.error('Failed to load health records:', error)
  }
}

// 获取喂食状态文字
const getFeedingText = (status: string) => {
  const map: Record<string, string> = { 
    normal: '正常', 
    reduced: '食量减少', 
    increased: '食量增加', 
    refused: '拒食',
    more: '食量增加',
    less: '食量减少',
    none: '未进食',
    good: '食欲很好',
    poor: '食欲不佳'
  }
  return map[status] || status
}

// 获取活动量文字
const getActivityText = (level: string) => {
  const map: Record<string, string> = { 
    high: '活跃', 
    normal: '正常', 
    low: '低迷', 
    inactive: '不活动',
    active: '活跃'
  }
  return map[level] || level
}

// 获取心情文字
const getMoodText = (mood: string) => {
  const map: Record<string, string> = { 
    happy: '开心', 
    calm: '平静', 
    anxious: '焦虑', 
    stressed: '紧张',
    normal: '正常',
    tired: '疲惫',
    sad: '低落'
  }
  return map[mood] || mood
}

// 模拟数据已移除，使用从API获取的数据

const canCancel = computed(() => order.value && ['pending', 'confirmed'].includes(order.value.status))
const canTrack = computed(() => order.value?.status === 'in_progress')
const canReview = computed(() => order.value?.status === 'completed')
const canComplaint = computed(() => order.value?.status === 'completed')

// 跳转到投诉页面
const goToComplaint = () => {
  router.push({
    path: '/support/complaint',
    query: {
      bookingId: order.value?.orderNumber,
      institutionId: order.value?.institutionId,
      institutionName: institutionInfo.value.name
    }
  })
}

const openCancelModal = () => { showCancelModal.value = true }
const confirmCancel = async () => {
  if (!cancelReason.value.trim()) { alert('请填写取消原因'); return }
  try {
    await bookingApi.cancel(orderId.value, cancelReason.value)
    showCancelModal.value = false
    ElMessage.success('订单已取消')
    loadOrder()
  } catch (error) {
    ElMessage.error('取消订单失败')
  }
}

// 评价功能
const openReviewModal = () => { showReviewModal.value = true }
const toggleTag = (tag: string) => {
  const idx = reviewForm.value.tags.indexOf(tag)
  if (idx > -1) {
    reviewForm.value.tags.splice(idx, 1)
  } else {
    reviewForm.value.tags.push(tag)
  }
}
const submitReview = async () => {
  if (!reviewForm.value.content.trim()) {
    ElMessage.warning('请填写评价内容')
    return
  }
  
  submittingReview.value = true
  try {
    // 将单一评分转换为详细评分对象
    const ratingValue = reviewForm.value.rating
    await reviewApi.create({
      bookingId: orderId.value,
      rating: {
        overall: ratingValue,
        environment: ratingValue,
        service: ratingValue,
        hygiene: ratingValue,
        communication: ratingValue
      },
      content: reviewForm.value.content,
      images: []
    })
    showReviewModal.value = false
    ElMessage.success('评价提交成功，感谢您的反馈！')
    // 重置表单
    reviewForm.value = { rating: 5, content: '', tags: [] }
  } catch (error) {
    ElMessage.error('评价提交失败，请稍后重试')
  } finally {
    submittingReview.value = false
  }
}

const contactInstitution = () => { 
  if (institutionInfo.value.phone) {
    window.location.href = `tel:${institutionInfo.value.phone}` 
  }
}

const formatDate = (dateStr: string) => new Date(dateStr).toLocaleDateString('zh-CN', { year: 'numeric', month: 'long', day: 'numeric' })
const formatDateTime = (dateStr: string) => new Date(dateStr).toLocaleString('zh-CN')

onMounted(() => { initialLoad() })
</script>

<template>
  <div class="order-detail-view">
    <div v-if="loading" class="loading-state">
      <div class="loading-spinner"></div>
      <p>加载中...</p>
    </div>

    <template v-else-if="order">
      <!-- 状态头部 -->
      <div class="status-header" :style="{ background: getStatusConfig(order.status).bgColor }">
        <div class="status-icon">{{ getStatusConfig(order.status).icon }}</div>
        <div class="status-info">
          <h2 :style="{ color: getStatusConfig(order.status).color }">{{ getStatusConfig(order.status).label }}</h2>
          <p v-if="order.status === 'in_progress'">宠物正在寄养中，您可以随时查看状态</p>
        </div>
      </div>

      <!-- 宠物状态追踪（进行中和已完成订单显示健康记录） -->
      <div v-if="canTrack || (order.status === 'completed' && petStatusRecords.length > 0)" class="tracking-section">
        <div class="tracking-pet-info">
          <div class="pet-avatar-small">{{ petInfo.species === 'dog' ? '🐕' : '🐱' }}</div>
          <div class="pet-brief">
            <span class="pet-name">{{ petInfo.name }}</span>
            <span class="pet-status">{{ order.status === 'in_progress' ? '状态良好 · 正在寄养中' : '寄养已完成' }}</span>
          </div>
          <AppButton v-if="order.status === 'in_progress'" type="outline" size="sm" @click="contactInstitution">📞 联系机构</AppButton>
        </div>
        <div class="tracking-timeline" v-if="petStatusRecords.length > 0">
          <div v-for="record in petStatusRecords" :key="record.id" class="timeline-item">
            <div class="timeline-icon" :style="{ background: getStatusTypeConfig(record.type).color }">{{ getStatusTypeConfig(record.type).icon }}</div>
            <div class="timeline-content">
              <div class="timeline-header">
                <span class="timeline-title">{{ record.title }}</span>
                <span class="timeline-time">{{ record.time }}</span>
              </div>
              <p class="timeline-text">{{ record.content }}</p>
              <div v-if="record.healthData" class="health-data">
                <div class="health-item" v-if="record.healthData.temperature"><span class="health-label">体温</span><span class="health-value">{{ record.healthData.temperature }}°C</span></div>
                <div class="health-item" v-if="record.healthData.weight"><span class="health-label">体重</span><span class="health-value">{{ record.healthData.weight }}kg</span></div>
                <div class="health-item" v-if="record.healthData.appetite"><span class="health-label">食欲</span><span class="health-value">{{ getAppetiteText(record.healthData.appetite) }}</span></div>
                <div class="health-item" v-if="record.healthData.mood"><span class="health-label">心情</span><span class="health-value">{{ getMoodText(record.healthData.mood) }}</span></div>
              </div>
              <div v-if="record.images?.length" class="timeline-images">
                <img v-for="(img, idx) in record.images" :key="idx" :src="img" alt="宠物照片" />
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- 快捷操作 -->
      <div class="quick-actions" v-if="canReview">
        <AppButton type="primary" size="lg" @click="openReviewModal">⭐ 去评价</AppButton>
        <AppButton type="outline" size="lg" @click="goToComplaint">📋 投诉</AppButton>
      </div>

      <!-- 订单信息 -->
      <div class="info-section">
        <h3>订单信息</h3>
        <div class="info-card">
          <div class="info-row"><span class="info-label">订单编号</span><span class="info-value mono">{{ order.orderNumber }}</span></div>
          <div class="info-row"><span class="info-label">下单时间</span><span class="info-value">{{ formatDateTime(order.createdAt) }}</span></div>
          <div class="info-row" v-if="order.paidAt"><span class="info-label">支付时间</span><span class="info-value">{{ formatDateTime(order.paidAt) }}</span></div>
        </div>
      </div>

      <!-- 寄养信息 -->
      <div class="info-section">
        <h3>寄养信息</h3>
        <div class="info-card">
          <div class="info-row"><span class="info-label">寄养机构</span><span class="info-value">{{ institutionInfo.name }}</span></div>
          <div class="info-row"><span class="info-label">服务套餐</span><span class="info-value">{{ packageInfo.name }}</span></div>
          <div class="info-row"><span class="info-label">入住日期</span><span class="info-value">{{ formatDate(order.startDate) }}</span></div>
          <div class="info-row"><span class="info-label">离店日期</span><span class="info-value">{{ formatDate(order.endDate) }}</span></div>
          <div class="info-row"><span class="info-label">寄养天数</span><span class="info-value">{{ order.totalDays }} 天</span></div>
        </div>
      </div>

      <!-- 宠物信息 -->
      <div class="info-section">
        <h3>宠物信息</h3>
        <div class="pet-card">
          <div class="pet-avatar">{{ petInfo.species === 'dog' ? '🐕' : '🐱' }}</div>
          <div class="pet-info">
            <span class="pet-name">{{ petInfo.name }}</span>
            <span class="pet-details">{{ petInfo.breed }} · {{ petInfo.age }}岁 · {{ petInfo.weight }}kg</span>
          </div>
        </div>
      </div>

      <!-- 费用明细 -->
      <div class="info-section">
        <h3>费用明细</h3>
        <div class="info-card">
          <div class="info-row"><span class="info-label">套餐单价</span><span class="info-value">¥{{ order.basePrice }}/天</span></div>
          <div class="info-row"><span class="info-label">寄养天数</span><span class="info-value">× {{ order.totalDays }} 天</span></div>
          <div class="info-row total"><span class="info-label">订单总额</span><span class="info-value price">¥{{ order.totalPrice.toFixed(2) }}</span></div>
        </div>
      </div>

      <!-- 联系机构 -->
      <div class="info-section">
        <h3>机构联系方式</h3>
        <div class="contact-card">
          <div class="contact-item" @click="contactInstitution">
            <span class="contact-icon">📞</span>
            <div class="contact-info"><span class="contact-label">客服电话</span><span class="contact-value">{{ institutionInfo.phone }}</span></div>
            <span class="contact-action">拨打</span>
          </div>
        </div>
      </div>

      <!-- 底部操作 -->
      <div class="bottom-actions" v-if="canCancel">
        <AppButton type="outline" @click="openCancelModal">取消订单</AppButton>
      </div>
    </template>

    <!-- 取消弹窗 -->
    <div v-if="showCancelModal" class="modal-overlay" @click="showCancelModal = false">
      <div class="modal-content" @click.stop>
        <h3>取消订单</h3>
        <textarea v-model="cancelReason" placeholder="请输入取消原因..." rows="4"></textarea>
        <div class="modal-actions">
          <AppButton type="outline" @click="showCancelModal = false">再想想</AppButton>
          <AppButton type="primary" @click="confirmCancel">确认取消</AppButton>
        </div>
      </div>
    </div>

    <!-- 评价弹窗 -->
    <div v-if="showReviewModal" class="modal-overlay" @click="showReviewModal = false">
      <div class="review-modal" @click.stop>
        <div class="review-header">
          <h3>⭐ 评价订单</h3>
          <button class="close-btn" @click="showReviewModal = false">×</button>
        </div>
        <div class="review-body">
          <!-- 评分 -->
          <div class="rating-section">
            <label>整体评分</label>
            <div class="star-rating">
              <span 
                v-for="i in 5" 
                :key="i" 
                class="star" 
                :class="{ active: i <= reviewForm.rating }"
                @click="reviewForm.rating = i"
              >★</span>
            </div>
            <span class="rating-text">{{ ['', '很差', '较差', '一般', '满意', '非常满意'][reviewForm.rating] }}</span>
          </div>
          <!-- 标签选择 -->
          <div class="tags-section">
            <label>选择标签</label>
            <div class="tag-list">
              <span 
                v-for="tag in reviewTags" 
                :key="tag" 
                class="tag-item"
                :class="{ selected: reviewForm.tags.includes(tag) }"
                @click="toggleTag(tag)"
              >{{ tag }}</span>
            </div>
          </div>
          <!-- 评价内容 -->
          <div class="content-section">
            <label>评价内容</label>
            <textarea 
              v-model="reviewForm.content" 
              placeholder="分享您的寄养体验，帮助其他宠物主人做出选择..."
              rows="4"
            ></textarea>
          </div>
        </div>
        <div class="review-footer">
          <AppButton type="outline" @click="showReviewModal = false">取消</AppButton>
          <AppButton type="primary" :loading="submittingReview" @click="submitReview">提交评价</AppButton>
        </div>
      </div>
    </div>

  </div>
</template>

<style scoped lang="scss">
.order-detail-view {
  min-height: 100vh;
  background: #f5f5f5;
  padding-bottom: 100px;
}

.loading-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  min-height: 60vh;
  color: #666;

  .loading-spinner {
    width: 40px;
    height: 40px;
    border: 3px solid #e0e0e0;
    border-top-color: #3b82f6;
    border-radius: 50%;
    animation: spin 1s linear infinite;
  }

  p {
    margin-top: 16px;
  }
}

@keyframes spin {
  to { transform: rotate(360deg); }
}

.status-header {
  display: flex;
  align-items: center;
  padding: 24px 20px;
  gap: 16px;

  .status-icon {
    font-size: 48px;
  }

  .status-info {
    h2 {
      font-size: 20px;
      font-weight: 600;
      margin: 0 0 4px;
    }

    p {
      font-size: 14px;
      color: #666;
      margin: 0;
    }
  }
}

.quick-actions {
  display: flex;
  gap: 12px;
  padding: 16px 20px;
  background: #fff;
  margin-bottom: 12px;
}

.tracking-section {
  background: #fff;
  margin-bottom: 12px;
}

.tracking-pet-info {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 16px 20px;
  background: linear-gradient(135deg, #fef3c7, #fde68a);

  .pet-avatar-small {
    width: 48px;
    height: 48px;
    background: #fff;
    border-radius: 50%;
    display: flex;
    align-items: center;
    justify-content: center;
    font-size: 24px;
  }

  .pet-brief {
    flex: 1;
    display: flex;
    flex-direction: column;
    gap: 2px;

    .pet-name {
      font-size: 16px;
      font-weight: 600;
      color: #333;
    }

    .pet-status {
      font-size: 13px;
      color: #666;
    }
  }
}

.tracking-timeline {
  padding: 20px;

  .timeline-item {
    display: flex;
    gap: 12px;
    padding-bottom: 20px;
    position: relative;

    &:not(:last-child)::before {
      content: '';
      position: absolute;
      left: 18px;
      top: 40px;
      bottom: 0;
      width: 2px;
      background: #e0e0e0;
    }

    .timeline-icon {
      width: 36px;
      height: 36px;
      border-radius: 50%;
      display: flex;
      align-items: center;
      justify-content: center;
      font-size: 16px;
      flex-shrink: 0;
    }

    .timeline-content {
      flex: 1;
      min-width: 0;

      .timeline-header {
        display: flex;
        justify-content: space-between;
        align-items: center;
        margin-bottom: 8px;

        .timeline-title {
          font-size: 15px;
          font-weight: 600;
          color: #333;
        }

        .timeline-time {
          font-size: 12px;
          color: #999;
        }
      }

      .timeline-text {
        font-size: 14px;
        color: #666;
        line-height: 1.5;
        margin: 0 0 12px;
      }
    }
  }
}

.health-data {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 8px;
  background: #f0f9ff;
  border-radius: 8px;
  padding: 12px;
  margin-bottom: 12px;

  .health-item {
    display: flex;
    flex-direction: column;
    gap: 2px;

    .health-label {
      font-size: 12px;
      color: #666;
    }

    .health-value {
      font-size: 14px;
      font-weight: 600;
      color: #3b82f6;
    }
  }
}

.timeline-images {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;

  img {
    width: 80px;
    height: 80px;
    object-fit: cover;
    border-radius: 8px;
  }
}

.info-section {
  background: #fff;
  margin-bottom: 12px;
  padding: 20px;

  h3 {
    font-size: 16px;
    font-weight: 600;
    color: #333;
    margin: 0 0 16px;
  }
}

.info-card {
  background: #fafafa;
  border-radius: 12px;
  padding: 16px;
}

.info-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 0;
  border-bottom: 1px solid #eee;

  &:last-child {
    border-bottom: none;
  }

  &.total {
    padding-top: 16px;
    margin-top: 8px;
    border-top: 1px dashed #ddd;
    border-bottom: none;
  }

  .info-label {
    font-size: 14px;
    color: #666;
  }

  .info-value {
    font-size: 14px;
    color: #333;
    font-weight: 500;

    &.mono {
      font-family: monospace;
    }

    &.price {
      font-size: 18px;
      color: #f56c6c;
      font-weight: 600;
    }
  }
}

.pet-card {
  display: flex;
  align-items: center;
  gap: 16px;
  background: #fafafa;
  border-radius: 12px;
  padding: 16px;

  .pet-avatar {
    width: 60px;
    height: 60px;
    background: linear-gradient(135deg, #fef3c7, #fde68a);
    border-radius: 50%;
    display: flex;
    align-items: center;
    justify-content: center;
    font-size: 32px;
  }

  .pet-info {
    display: flex;
    flex-direction: column;
    gap: 4px;

    .pet-name {
      font-size: 18px;
      font-weight: 600;
      color: #333;
    }

    .pet-details {
      font-size: 14px;
      color: #666;
    }
  }
}

.contact-card {
  .contact-item {
    display: flex;
    align-items: center;
    gap: 12px;
    padding: 16px;
    background: #fafafa;
    border-radius: 12px;
    cursor: pointer;
    transition: background 0.2s;

    &:hover {
      background: #f0f0f0;
    }

    .contact-icon {
      font-size: 24px;
    }

    .contact-info {
      flex: 1;
      display: flex;
      flex-direction: column;
      gap: 2px;

      .contact-label {
        font-size: 12px;
        color: #999;
      }

      .contact-value {
        font-size: 16px;
        color: #333;
        font-weight: 500;
      }
    }

    .contact-action {
      font-size: 14px;
      color: #3b82f6;
      font-weight: 500;
    }
  }
}

.bottom-actions {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  padding: 16px 20px;
  background: #fff;
  box-shadow: 0 -2px 10px rgba(0, 0, 0, 0.05);
  display: flex;
  gap: 12px;
}

.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
  padding: 20px;
}

.modal-content {
  background: #fff;
  border-radius: 16px;
  padding: 24px;
  width: 100%;
  max-width: 400px;

  h3 {
    font-size: 18px;
    font-weight: 600;
    margin: 0 0 16px;
    text-align: center;
  }

  textarea {
    width: 100%;
    border: 1px solid #ddd;
    border-radius: 8px;
    padding: 12px;
    font-size: 14px;
    resize: none;
    margin-bottom: 16px;

    &:focus {
      outline: none;
      border-color: #3b82f6;
    }
  }

  .modal-actions {
    display: flex;
    gap: 12px;
  }
}

// 评价弹窗样式
.review-modal {
  background: #fff;
  border-radius: 16px;
  width: 100%;
  max-width: 480px;
  max-height: 90vh;
  overflow-y: auto;
}

.review-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20px 24px;
  border-bottom: 1px solid #eee;

  h3 {
    margin: 0;
    font-size: 18px;
    font-weight: 600;
  }

  .close-btn {
    width: 32px;
    height: 32px;
    border: none;
    background: #f5f5f5;
    border-radius: 50%;
    font-size: 20px;
    cursor: pointer;
    display: flex;
    align-items: center;
    justify-content: center;
    color: #666;

    &:hover {
      background: #eee;
    }
  }
}

.review-body {
  padding: 24px;

  label {
    display: block;
    font-size: 14px;
    font-weight: 600;
    color: #333;
    margin-bottom: 12px;
  }
}

.rating-section {
  margin-bottom: 24px;
  text-align: center;

  .star-rating {
    display: flex;
    justify-content: center;
    gap: 8px;
    margin-bottom: 8px;

    .star {
      font-size: 36px;
      color: #ddd;
      cursor: pointer;
      transition: all 0.2s;

      &.active {
        color: #fbbf24;
      }

      &:hover {
        transform: scale(1.1);
      }
    }
  }

  .rating-text {
    font-size: 14px;
    color: #666;
  }
}

.tags-section {
  margin-bottom: 24px;

  .tag-list {
    display: flex;
    flex-wrap: wrap;
    gap: 10px;
  }

  .tag-item {
    padding: 8px 16px;
    background: #f5f5f5;
    border: 1px solid #e0e0e0;
    border-radius: 20px;
    font-size: 13px;
    color: #666;
    cursor: pointer;
    transition: all 0.2s;

    &:hover {
      border-color: #3b82f6;
    }

    &.selected {
      background: #eff6ff;
      border-color: #3b82f6;
      color: #3b82f6;
    }
  }
}

.content-section {
  textarea {
    width: 100%;
    padding: 12px 16px;
    border: 1px solid #e0e0e0;
    border-radius: 12px;
    font-size: 14px;
    resize: none;
    line-height: 1.6;

    &:focus {
      outline: none;
      border-color: #3b82f6;
    }

    &::placeholder {
      color: #999;
    }
  }
}

.review-footer {
  display: flex;
  gap: 12px;
  padding: 16px 24px;
  border-top: 1px solid #eee;
  justify-content: flex-end;
}
</style>
