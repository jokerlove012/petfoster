<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElDialog } from 'element-plus'
import { AppCard } from '@/components/common'
import { supportApi } from '@/api/support'

const router = useRouter()
const loading = ref(false)
const tickets = ref<any[]>([])
const selectedTicket = ref<any>(null)
const showDetail = ref(false)

const statusMap: Record<string, { text: string; class: string }> = {
  pending: { text: '待处理', class: 'status-pending' },
  awaiting_response: { text: '等待回复', class: 'status-waiting' },
  under_review: { text: '处理中', class: 'status-processing' },
  resolved: { text: '已解决', class: 'status-resolved' }
}

const categoryMap: Record<string, string> = {
  booking: '预约相关',
  payment: '支付退款',
  service: '服务问题',
  technical: '技术问题',
  complaint: '投诉建议',
  other: '其他问题'
}

const loadTickets = async () => {
  loading.value = true
  try {
    const res = await supportApi.getMyComplaints(1, 50)
    if (res.code === 200 && res.data) {
      tickets.value = res.data.list || []
    }
  } catch (error) {
    console.error('加载工单失败:', error)
  } finally {
    loading.value = false
  }
}

const viewDetail = (ticket: any) => {
  selectedTicket.value = ticket
  showDetail.value = true
}

const formatDate = (dateStr: string) => {
  if (!dateStr) return ''
  return new Date(dateStr).toLocaleString('zh-CN')
}

onMounted(() => {
  loadTickets()
})
</script>

<template>
  <div class="my-tickets-view">
    <div class="page-header">
      <h1>我的工单</h1>
      <button class="btn-primary" @click="router.push('/support/ticket')">
        + 提交新工单
      </button>
    </div>

    <div v-if="loading" class="loading">加载中...</div>

    <div v-else-if="tickets.length === 0" class="empty">
      <p>暂无工单记录</p>
      <button class="btn-primary" @click="router.push('/support/ticket')">提交工单</button>
    </div>

    <div v-else class="tickets-list">
      <AppCard v-for="ticket in tickets" :key="ticket.id" class="ticket-card" @click="viewDetail(ticket)">
        <div class="ticket-header">
          <span class="ticket-number">{{ ticket.complaintNumber }}</span>
          <span class="ticket-status" :class="statusMap[ticket.status]?.class">
            {{ statusMap[ticket.status]?.text || ticket.status }}
          </span>
        </div>
        <div class="ticket-title">{{ ticket.expectation || '无标题' }}</div>
        <div class="ticket-meta">
          <span class="ticket-category">{{ categoryMap[ticket.category] || ticket.category }}</span>
          <span class="ticket-date">{{ formatDate(ticket.createdAt) }}</span>
        </div>
      </AppCard>
    </div>

    <!-- 工单详情弹窗 -->
    <ElDialog v-model="showDetail" title="工单详情" width="600px">
      <div v-if="selectedTicket" class="ticket-detail">
        <div class="detail-row">
          <label>工单编号</label>
          <span>{{ selectedTicket.complaintNumber }}</span>
        </div>
        <div class="detail-row">
          <label>状态</label>
          <span class="ticket-status" :class="statusMap[selectedTicket.status]?.class">
            {{ statusMap[selectedTicket.status]?.text || selectedTicket.status }}
          </span>
        </div>
        <div class="detail-row">
          <label>问题分类</label>
          <span>{{ categoryMap[selectedTicket.category] || selectedTicket.category }}</span>
        </div>
        <div class="detail-row">
          <label>问题标题</label>
          <span>{{ selectedTicket.expectation || '无' }}</span>
        </div>
        <div class="detail-row">
          <label>问题描述</label>
          <p class="description">{{ selectedTicket.description }}</p>
        </div>
        <div v-if="selectedTicket.evidence?.length" class="detail-row">
          <label>附件</label>
          <div class="evidence-list">
            <img v-for="(url, idx) in selectedTicket.evidence" :key="idx" :src="url" class="evidence-img" />
          </div>
        </div>
        <div class="detail-row">
          <label>提交时间</label>
          <span>{{ formatDate(selectedTicket.createdAt) }}</span>
        </div>
        <div v-if="selectedTicket.resolution" class="detail-row">
          <label>处理结果</label>
          <div class="resolution">
            <p>{{ selectedTicket.resolution.decision }}</p>
            <p v-if="selectedTicket.resolution.remedies">补救措施: {{ selectedTicket.resolution.remedies }}</p>
          </div>
        </div>
      </div>
    </ElDialog>
  </div>
</template>

<style scoped>
.my-tickets-view {
  max-width: 800px;
  margin: 0 auto;
  padding: 24px;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
}

.page-header h1 {
  font-size: 24px;
  margin: 0;
}

.btn-primary {
  padding: 10px 20px;
  background: linear-gradient(135deg, #FF7F6B 0%, #FF8F5C 100%);
  color: white;
  border: none;
  border-radius: 8px;
  cursor: pointer;
  font-weight: 500;
}

.loading, .empty {
  text-align: center;
  padding: 60px 20px;
  color: #666;
}

.tickets-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.ticket-card {
  cursor: pointer;
  transition: transform 0.2s, box-shadow 0.2s;
}

.ticket-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0,0,0,0.1);
}

.ticket-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
}

.ticket-number {
  font-family: monospace;
  color: #666;
  font-size: 13px;
}

.ticket-status {
  padding: 4px 12px;
  border-radius: 20px;
  font-size: 12px;
  font-weight: 500;
}

.status-pending { background: #FEF3C7; color: #D97706; }
.status-waiting { background: #DBEAFE; color: #2563EB; }
.status-processing { background: #E0E7FF; color: #4F46E5; }
.status-resolved { background: #DCFCE7; color: #16A34A; }

.ticket-title {
  font-size: 16px;
  font-weight: 500;
  margin-bottom: 8px;
}

.ticket-meta {
  display: flex;
  gap: 16px;
  font-size: 13px;
  color: #888;
}

.ticket-detail .detail-row {
  margin-bottom: 16px;
}

.ticket-detail label {
  display: block;
  font-size: 13px;
  color: #888;
  margin-bottom: 4px;
}

.ticket-detail .description {
  margin: 0;
  line-height: 1.6;
  white-space: pre-wrap;
}

.evidence-list {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
}

.evidence-img {
  width: 100px;
  height: 100px;
  object-fit: cover;
  border-radius: 8px;
  cursor: pointer;
}

.resolution {
  background: #F0FDF4;
  padding: 12px;
  border-radius: 8px;
}

.resolution p {
  margin: 0 0 8px;
}

.resolution p:last-child {
  margin-bottom: 0;
}
</style>
