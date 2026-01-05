<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ArrowLeft, Calendar, User, Phone, MapPin, Clock, FileText, Edit, CheckCircle, XCircle, MessageSquare } from 'lucide-vue-next'
import { ElMessage, ElMessageBox } from 'element-plus'
import api from '@/api/index'

const router = useRouter()
const route = useRoute()

const orderId = computed(() => route.params.id as string)
const loading = ref(false)

// 订单详情
const order = ref({
  id: '',
  orderNumber: '',
  status: '',
  createdAt: '',
  pet: { name: '', type: 'dog', breed: '', age: '', weight: '', avatar: '🐕' },
  owner: { name: '', phone: '', email: '' },
  service: { name: '', price: 0, unit: '天' },
  room: '-',
  startDate: '',
  endDate: '',
  days: 0,
  totalAmount: 0,
  paidAmount: 0,
  paymentStatus: '',
  specialRequirements: '',
  emergencyContact: '',
  emergencyPhone: '',
  healthRecords: [] as any[],
  timeline: [] as any[]
})

// 加载订单详情
const loadOrderDetail = async () => {
  loading.value = true
  try {
    const res = await api.get(`/bookings/${orderId.value}`)
    if (res.code === 200 && res.data) {
      const data = res.data
      order.value = {
        id: data.orderNumber || data.id,
        orderNumber: data.orderNumber || '',
        status: data.status || 'pending',
        createdAt: data.createdAt || '',
        pet: {
          name: data.petName || '未知',
          type: data.petSpecies || 'dog',
          breed: data.petBreed || '',
          age: data.petAge ? `${data.petAge}岁` : '',
          weight: data.petWeight ? `${data.petWeight}kg` : '',
          avatar: data.petSpecies === 'cat' ? '🐱' : '🐕'
        },
        owner: {
          name: data.userName || '未知',
          phone: data.userPhone || '',
          email: ''
        },
        service: {
          name: data.packageName || '标准套餐',
          price: data.basePrice || 0,
          unit: '天'
        },
        room: '-',
        startDate: data.startDate || '',
        endDate: data.endDate || '',
        days: data.totalDays || 0,
        totalAmount: data.totalPrice || 0,
        paidAmount: data.totalPrice || 0,
        paymentStatus: data.paymentStatus || 'unpaid',
        specialRequirements: data.specialRequirements || '',
        emergencyContact: data.emergencyContact?.name || '',
        emergencyPhone: data.emergencyContact?.phone || '',
        healthRecords: [],
        timeline: buildTimeline(data)
      }
    }
  } catch (error) {
    console.error('加载订单详情失败:', error)
    ElMessage.error('加载订单详情失败')
  } finally {
    loading.value = false
  }
}

// 构建时间线
const buildTimeline = (data: any) => {
  const timeline = []
  if (data.createdAt) {
    timeline.push({ time: formatDateTime(data.createdAt), event: '订单创建', type: 'create' })
  }
  if (data.paidAt) {
    timeline.push({ time: formatDateTime(data.paidAt), event: '支付完成', type: 'payment' })
  }
  if (data.checkInTime) {
    timeline.push({ time: formatDateTime(data.checkInTime), event: '宠物入住', type: 'checkin' })
  }
  if (data.checkOutTime) {
    timeline.push({ time: formatDateTime(data.checkOutTime), event: '宠物离店', type: 'checkout' })
  }
  return timeline
}

const formatDateTime = (dateStr: string) => {
  if (!dateStr) return ''
  const date = new Date(dateStr)
  return date.toLocaleString('zh-CN', { year: 'numeric', month: '2-digit', day: '2-digit', hour: '2-digit', minute: '2-digit' })
}

const getStatusText = (status: string) => {
  const map: Record<string, string> = { pending: '待确认', confirmed: '已确认', ongoing: '进行中', in_progress: '进行中', completed: '已完成', cancelled: '已取消' }
  return map[status] || status
}

const getStatusClass = (status: string) => {
  const map: Record<string, string> = { pending: 'warning', confirmed: 'info', ongoing: 'success', in_progress: 'success', completed: 'default', cancelled: 'danger' }
  return map[status] || 'default'
}

const confirmOrder = async () => {
  await ElMessageBox.confirm('确认接受此预约？', '确认预约')
  try {
    await api.post(`/institution/bookings/${orderId.value}/confirm`)
    order.value.status = 'confirmed'
    ElMessage.success('订单已确认')
  } catch (error) {
    ElMessage.error('操作失败')
  }
}

const cancelOrder = async () => {
  await ElMessageBox.confirm('确认取消此订单？', '取消订单', { type: 'warning' })
  try {
    await api.post(`/institution/bookings/${orderId.value}/reject`, { reason: '机构取消' })
    order.value.status = 'cancelled'
    ElMessage.success('订单已取消')
  } catch (error) {
    ElMessage.error('操作失败')
  }
}

const goBack = () => router.back()
const goToHealth = () => router.push(`/institution/health/${orderId.value}`)
const contactOwner = () => {
  if (order.value.owner.phone) {
    ElMessage.info(`正在拨打 ${order.value.owner.phone}`)
  }
}

onMounted(() => {
  loadOrderDetail()
})
</script>

<template>
  <div class="order-detail">
    <div class="page-header">
      <button class="back-btn" @click="goBack"><ArrowLeft :size="20" /> 返回</button>
      <h1>📋 订单详情</h1>
      <span class="order-id">#{{ order.id }}</span>
      <span class="status-badge" :class="getStatusClass(order.status)">{{ getStatusText(order.status) }}</span>
    </div>

    <div class="detail-grid">
      <!-- 左侧主要信息 -->
      <div class="main-content">
        <!-- 宠物信息 -->
        <div class="info-card">
          <h3>🐾 宠物信息</h3>
          <div class="pet-profile">
            <span class="pet-avatar">{{ order.pet.avatar }}</span>
            <div class="pet-info">
              <span class="pet-name">{{ order.pet.name }}</span>
              <span class="pet-meta">{{ order.pet.breed }} · {{ order.pet.age }} · {{ order.pet.weight }}</span>
            </div>
          </div>
        </div>

        <!-- 服务信息 -->
        <div class="info-card">
          <h3><Calendar :size="18" /> 服务信息</h3>
          <div class="info-grid">
            <div class="info-item"><span class="label">服务套餐</span><span class="value">{{ order.service.name }}</span></div>
            <div class="info-item"><span class="label">房间号</span><span class="value">{{ order.room }}</span></div>
            <div class="info-item"><span class="label">入住日期</span><span class="value">{{ order.startDate }}</span></div>
            <div class="info-item"><span class="label">离店日期</span><span class="value">{{ order.endDate }}</span></div>
            <div class="info-item"><span class="label">寄养天数</span><span class="value">{{ order.days }} 天</span></div>
            <div class="info-item"><span class="label">订单金额</span><span class="value price">¥{{ order.totalAmount }}</span></div>
          </div>
        </div>

        <!-- 特殊要求 -->
        <div class="info-card">
          <h3><FileText :size="18" /> 特殊要求</h3>
          <p class="requirements">{{ order.specialRequirements || '无特殊要求' }}</p>
        </div>

        <!-- 健康记录 -->
        <div class="info-card">
          <div class="card-header">
            <h3>💊 健康记录</h3>
            <button class="btn-add" @click="goToHealth"><Edit :size="14" /> 添加记录</button>
          </div>
          <div class="health-list">
            <div v-for="(record, index) in order.healthRecords" :key="index" class="health-item">
              <span class="health-date">{{ record.date }}</span>
              <span class="health-content">{{ record.content }}</span>
              <span class="health-operator">{{ record.operator }}</span>
            </div>
          </div>
        </div>
      </div>

      <!-- 右侧信息 -->
      <div class="side-content">
        <!-- 客户信息 -->
        <div class="info-card">
          <h3><User :size="18" /> 客户信息</h3>
          <div class="owner-info">
            <div class="info-row"><span class="label">姓名</span><span>{{ order.owner.name }}</span></div>
            <div class="info-row"><span class="label">电话</span><span>{{ order.owner.phone }}</span></div>
            <div class="info-row"><span class="label">紧急联系人</span><span>{{ order.emergencyContact }}</span></div>
          </div>
          <button class="btn-contact" @click="contactOwner"><Phone :size="14" /> 联系客户</button>
        </div>

        <!-- 订单时间线 -->
        <div class="info-card">
          <h3><Clock :size="18" /> 订单进度</h3>
          <div class="timeline">
            <div v-for="(item, index) in order.timeline" :key="index" class="timeline-item">
              <span class="timeline-dot"></span>
              <div class="timeline-content">
                <span class="timeline-event">{{ item.event }}</span>
                <span class="timeline-time">{{ item.time }}</span>
              </div>
            </div>
          </div>
        </div>

        <!-- 操作按钮 -->
        <div class="action-buttons">
          <button v-if="order.status === 'pending'" class="btn-confirm" @click="confirmOrder">
            <CheckCircle :size="16" /> 确认订单
          </button>
          <button v-if="order.status !== 'completed' && order.status !== 'cancelled'" class="btn-cancel" @click="cancelOrder">
            <XCircle :size="16" /> 取消订单
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<style lang="scss" scoped>
.order-detail { max-width: 1200px; margin: 0 auto; padding: 24px; }

.page-header {
  display: flex; align-items: center; gap: 16px; margin-bottom: 24px; flex-wrap: wrap;
  .back-btn { display: flex; align-items: center; gap: 6px; padding: 8px 12px; background: white; border: none; border-radius: 8px; cursor: pointer; }
  h1 { font-size: 24px; font-weight: 700; margin: 0; }
  .order-id { font-size: 14px; color: #9A958F; }
  .status-badge { padding: 4px 12px; border-radius: 20px; font-size: 12px; font-weight: 500;
    &.success { background: #E8F8E8; color: #52c41a; }
    &.warning { background: #FFF8E6; color: #faad14; }
    &.danger { background: #FEE2E2; color: #ff4d4f; }
    &.info { background: #E6F4FF; color: #1890ff; }
    &.default { background: #F0EFED; color: #6B6560; }
  }
}

.detail-grid { display: grid; grid-template-columns: 1fr 360px; gap: 24px;
  @media (max-width: 900px) { grid-template-columns: 1fr; }
}

.info-card {
  background: white; border-radius: 14px; padding: 20px; margin-bottom: 20px;
  h3 { display: flex; align-items: center; gap: 8px; font-size: 15px; font-weight: 600; margin: 0 0 16px; }
  .card-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 16px;
    h3 { margin: 0; }
    .btn-add { display: flex; align-items: center; gap: 4px; padding: 6px 12px; background: #F3EEFF; color: #722ed1; border: none; border-radius: 6px; font-size: 12px; cursor: pointer; }
  }
}

.pet-profile {
  display: flex; align-items: center; gap: 16px;
  .pet-avatar { font-size: 48px; }
  .pet-info { .pet-name { display: block; font-size: 18px; font-weight: 600; } .pet-meta { font-size: 13px; color: #9A958F; } }
}

.info-grid { display: grid; grid-template-columns: repeat(3, 1fr); gap: 16px;
  @media (max-width: 600px) { grid-template-columns: repeat(2, 1fr); }
  .info-item { .label { display: block; font-size: 12px; color: #9A958F; margin-bottom: 4px; } .value { font-size: 14px; font-weight: 500; &.price { color: #722ed1; font-size: 16px; } } }
}

.requirements { font-size: 14px; color: #2D2A26; line-height: 1.6; margin: 0; }

.health-list { display: flex; flex-direction: column; gap: 12px; }
.health-item { display: flex; gap: 12px; padding: 12px; background: #F8F8F7; border-radius: 8px; font-size: 13px;
  .health-date { color: #722ed1; font-weight: 500; white-space: nowrap; }
  .health-content { flex: 1; color: #2D2A26; }
  .health-operator { color: #9A958F; }
}

.owner-info { margin-bottom: 16px;
  .info-row { display: flex; justify-content: space-between; padding: 8px 0; font-size: 14px; border-bottom: 1px solid #F0EFED;
    .label { color: #9A958F; }
  }
}

.btn-contact { width: 100%; padding: 10px; background: #722ed1; color: white; border: none; border-radius: 8px; font-size: 13px; cursor: pointer; display: flex; align-items: center; justify-content: center; gap: 6px; }

.timeline { padding-left: 20px; border-left: 2px solid #E8E6E3; }
.timeline-item { position: relative; padding-bottom: 16px;
  .timeline-dot { position: absolute; left: -25px; top: 4px; width: 10px; height: 10px; background: #722ed1; border-radius: 50%; }
  .timeline-content { .timeline-event { display: block; font-size: 14px; font-weight: 500; } .timeline-time { font-size: 12px; color: #9A958F; } }
}

.action-buttons { display: flex; flex-direction: column; gap: 12px;
  button { width: 100%; padding: 12px; border: none; border-radius: 10px; font-size: 14px; cursor: pointer; display: flex; align-items: center; justify-content: center; gap: 8px;
    &.btn-confirm { background: #52c41a; color: white; }
    &.btn-cancel { background: #F8F8F7; color: #ff4d4f; }
  }
}
</style>
