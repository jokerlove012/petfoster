<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { MessageSquare, Search, Eye, Send, Clock, CheckCircle, AlertTriangle } from 'lucide-vue-next'
import { ElMessage, ElMessageBox, ElDialog, ElInput } from 'element-plus'
import { institutionManageApi } from '@/api/institution'

const searchKeyword = ref('')
const statusFilter = ref('all')
const loading = ref(false)

// 投诉统计
const complaintStats = ref({
  pending: 0,
  awaiting_response: 0,
  under_review: 0,
  resolved: 0,
  total: 0
})

// 投诉列表
const complaintList = ref<any[]>([])

// 详情弹窗
const showDetailDialog = ref(false)
const currentComplaint = ref<any>(null)

// 回复弹窗
const showReplyDialog = ref(false)
const replyContent = ref('')

const loadStats = async () => {
  try {
    const res = await institutionManageApi.getComplaintStats()
    if (res.data) {
      complaintStats.value = {
        pending: res.data.pending || 0,
        awaiting_response: res.data.awaiting_response || 0,
        under_review: res.data.under_review || 0,
        resolved: res.data.resolved || 0,
        total: res.data.total || 0
      }
    }
  } catch (error) {
    console.error('加载投诉统计失败:', error)
  }
}

const loadComplaints = async () => {
  loading.value = true
  try {
    const res = await institutionManageApi.getComplaints(statusFilter.value === 'all' ? undefined : statusFilter.value)
    if (res.data?.list) {
      complaintList.value = res.data.list
    }
  } catch (error) {
    console.error('加载投诉列表失败:', error)
    ElMessage.error('加载投诉列表失败')
  } finally {
    loading.value = false
  }
}

const filteredList = computed(() => {
  if (!searchKeyword.value) return complaintList.value
  return complaintList.value.filter(item => {
    const search = searchKeyword.value.toLowerCase()
    return (item.complaintNumber?.toLowerCase().includes(search) ||
            item.petOwnerName?.toLowerCase().includes(search) ||
            item.bookingOrderNumber?.toLowerCase().includes(search))
  })
})

const getStatusLabel = (status: string) => {
  const map: Record<string, string> = { 
    pending: '待处理', 
    awaiting_response: '等待平台审核', 
    under_review: '审核中', 
    resolved: '已解决' 
  }
  return map[status] || status
}

const getStatusType = (status: string) => {
  const map: Record<string, string> = { 
    pending: 'danger', 
    awaiting_response: 'info', 
    under_review: 'warning', 
    resolved: 'success' 
  }
  return map[status] || 'info'
}

const getCategoryName = (category: string) => {
  const map: Record<string, string> = {
    service_quality: '服务质量', 
    pet_safety: '宠物安全', 
    communication: '沟通问题',
    facility: '设施环境', 
    price: '价格争议', 
    other: '其他'
  }
  return map[category] || category
}

const viewDetail = (item: any) => {
  currentComplaint.value = item
  showDetailDialog.value = true
}

const openReplyDialog = (item: any) => {
  currentComplaint.value = item
  replyContent.value = ''
  showReplyDialog.value = true
}

const submitReply = async () => {
  if (!replyContent.value.trim()) {
    ElMessage.warning('请输入回复内容')
    return
  }
  
  try {
    await institutionManageApi.replyComplaint(currentComplaint.value.id, replyContent.value)
    showReplyDialog.value = false
    ElMessage.success('回复已发送')
    loadComplaints()
    loadStats()
  } catch (error: any) {
    console.error('回复失败:', error)
    ElMessage.error(error.message || '回复失败')
  }
}

const changeFilter = (status: string) => {
  statusFilter.value = status
  loadComplaints()
}

onMounted(() => {
  loadStats()
  loadComplaints()
})
</script>

<template>
  <div class="complaint-manage">
    <div class="page-header">
      <div class="header-left">
        <h1>📢 投诉管理</h1>
        <p>处理客户投诉与反馈</p>
      </div>
    </div>

    <!-- 统计卡片 -->
    <div class="stats-grid">
      <div class="stat-card pending" @click="changeFilter('pending')">
        <Clock :size="24" />
        <div class="stat-info">
          <span class="stat-value">{{ complaintStats.pending }}</span>
          <span class="stat-label">待处理</span>
        </div>
      </div>
      <div class="stat-card awaiting" @click="changeFilter('awaiting_response')">
        <AlertTriangle :size="24" />
        <div class="stat-info">
          <span class="stat-value">{{ complaintStats.awaiting_response + complaintStats.under_review }}</span>
          <span class="stat-label">处理中</span>
        </div>
      </div>
      <div class="stat-card resolved" @click="changeFilter('resolved')">
        <CheckCircle :size="24" />
        <div class="stat-info">
          <span class="stat-value">{{ complaintStats.resolved }}</span>
          <span class="stat-label">已解决</span>
        </div>
      </div>
      <div class="stat-card total" @click="changeFilter('all')">
        <MessageSquare :size="24" />
        <div class="stat-info">
          <span class="stat-value">{{ complaintStats.total }}</span>
          <span class="stat-label">总投诉</span>
        </div>
      </div>
    </div>

    <!-- 筛选栏 -->
    <div class="filter-bar">
      <div class="search-box">
        <Search :size="18" />
        <input v-model="searchKeyword" placeholder="搜索投诉编号/投诉人/订单号" />
      </div>
      <div class="status-tabs">
        <button v-for="s in ['all', 'pending', 'awaiting_response', 'under_review', 'resolved']" :key="s"
          :class="{ active: statusFilter === s }" @click="changeFilter(s)">
          {{ { all: '全部', pending: '待处理', awaiting_response: '等待审核', under_review: '审核中', resolved: '已解决' }[s] }}
        </button>
      </div>
    </div>

    <!-- 投诉列表 -->
    <div class="complaint-list" v-loading="loading">
      <div v-for="item in filteredList" :key="item.id" class="complaint-card">
        <div class="card-header">
          <span class="complaint-id">{{ item.complaintNumber }}</span>
          <el-tag :type="getStatusType(item.status)" size="small">{{ getStatusLabel(item.status) }}</el-tag>
        </div>
        <div class="card-body">
          <div class="info-row"><span class="label">订单号:</span><span>{{ item.bookingOrderNumber || '-' }}</span></div>
          <div class="info-row"><span class="label">投诉人:</span><span>{{ item.petOwnerName || '-' }} ({{ item.petOwnerPhone || '-' }})</span></div>
          <div class="info-row"><span class="label">投诉类型:</span><span class="type-tag">{{ getCategoryName(item.category) }}</span></div>
          <div class="content-box">
            <span class="label">投诉内容:</span>
            <p>{{ item.description }}</p>
          </div>
          <div v-if="item.institutionResponse" class="reply-box">
            <span class="label">回复:</span>
            <p>{{ item.institutionResponse }}</p>
          </div>
          <div v-if="item.resolution" class="resolution-box">
            <span class="label">处理结果:</span>
            <p>{{ item.resolution.decision }}</p>
            <span class="resolution-meta" v-if="item.resolution.resolvedAt">{{ item.resolution.resolvedAt }}</span>
          </div>
          <div class="time-info">投诉时间: {{ item.createdAt }}</div>
        </div>
        <div class="card-actions">
          <button class="btn-view" @click="viewDetail(item)"><Eye :size="14" /> 详情</button>
          <button v-if="item.status === 'pending'" class="btn-reply" @click="openReplyDialog(item)">
            <Send :size="14" /> 回复
          </button>
        </div>
      </div>
      <div v-if="!loading && filteredList.length === 0" class="empty-state">
        <span class="empty-icon">📭</span>
        <p>暂无投诉记录</p>
      </div>
    </div>

    <!-- 详情弹窗 -->
    <ElDialog v-model="showDetailDialog" title="投诉详情" width="600px">
      <div v-if="currentComplaint" class="detail-content">
        <div class="detail-section">
          <h4>基本信息</h4>
          <div class="detail-grid">
            <div class="detail-item"><span class="label">投诉编号</span><span class="value">{{ currentComplaint.complaintNumber }}</span></div>
            <div class="detail-item"><span class="label">投诉类型</span><span class="value">{{ getCategoryName(currentComplaint.category) }}</span></div>
            <div class="detail-item"><span class="label">投诉人</span><span class="value">{{ currentComplaint.petOwnerName }}</span></div>
            <div class="detail-item"><span class="label">联系电话</span><span class="value">{{ currentComplaint.petOwnerPhone }}</span></div>
            <div class="detail-item"><span class="label">订单号</span><span class="value">{{ currentComplaint.bookingOrderNumber || '-' }}</span></div>
            <div class="detail-item"><span class="label">状态</span><span class="value"><el-tag :type="getStatusType(currentComplaint.status)" size="small">{{ getStatusLabel(currentComplaint.status) }}</el-tag></span></div>
          </div>
        </div>
        <div class="detail-section"><h4>投诉内容</h4><p class="detail-text">{{ currentComplaint.description }}</p></div>
        <div v-if="currentComplaint.expectation" class="detail-section"><h4>期望处理</h4><p class="detail-text">{{ currentComplaint.expectation }}</p></div>
        <div v-if="currentComplaint.institutionResponse" class="detail-section"><h4>机构回复</h4><p class="detail-text">{{ currentComplaint.institutionResponse }}</p></div>
        <div v-if="currentComplaint.resolution" class="detail-section">
          <h4>处理结果</h4>
          <p class="detail-text">{{ currentComplaint.resolution.decision }}</p>
          <p v-if="currentComplaint.resolution.remedies" class="detail-text remedies">补偿措施: {{ currentComplaint.resolution.remedies }}</p>
          <span v-if="currentComplaint.resolution.resolvedAt" class="resolution-meta">{{ currentComplaint.resolution.resolvedAt }}</span>
        </div>
      </div>
    </ElDialog>

    <!-- 回复弹窗 -->
    <ElDialog v-model="showReplyDialog" title="回复投诉" width="500px">
      <div class="reply-form">
        <div class="form-item">
          <label>回复内容 <span class="required">*</span></label>
          <ElInput v-model="replyContent" type="textarea" :rows="6" placeholder="请输入您的回复内容" />
        </div>
      </div>
      <template #footer>
        <button class="btn-cancel" @click="showReplyDialog = false">取消</button>
        <button class="btn-confirm" @click="submitReply">确认回复</button>
      </template>
    </ElDialog>
  </div>
</template>

<style lang="scss" scoped>
.complaint-manage { max-width: 1000px; margin: 0 auto; padding: 24px; }

.page-header { margin-bottom: 24px;
  h1 { font-size: 26px; font-weight: 700; margin: 0 0 6px; }
  p { color: #6B6560; margin: 0; }
}

.stats-grid {
  display: grid; grid-template-columns: repeat(4, 1fr); gap: 16px; margin-bottom: 24px;
  @media (max-width: 768px) { grid-template-columns: repeat(2, 1fr); }
}

.stat-card {
  display: flex; align-items: center; gap: 16px; padding: 20px;
  background: white; border-radius: 14px; cursor: pointer; transition: transform 0.2s;
  &:hover { transform: translateY(-2px); }
  &.pending { color: #ff4d4f; }
  &.awaiting { color: #faad14; }
  &.resolved { color: #52c41a; }
  &.total { color: #722ed1; }
  .stat-info { .stat-value { display: block; font-size: 24px; font-weight: 700; color: #2D2A26; } .stat-label { font-size: 13px; color: #6B6560; } }
}

.filter-bar {
  display: flex; gap: 16px; margin-bottom: 20px; flex-wrap: wrap;
  .search-box { display: flex; align-items: center; gap: 10px; padding: 10px 16px; background: white; border-radius: 10px; flex: 1; min-width: 200px;
    input { border: none; outline: none; flex: 1; font-size: 14px; } color: #9A958F;
  }
  .status-tabs { display: flex; background: white; border-radius: 10px; padding: 4px;
    button { padding: 8px 16px; border: none; background: transparent; font-size: 13px; color: #6B6560; cursor: pointer; border-radius: 6px;
      &.active { background: #722ed1; color: white; }
    }
  }
}

.complaint-list { display: flex; flex-direction: column; gap: 16px; }

.complaint-card {
  background: white; border-radius: 14px; padding: 20px;
  .card-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 16px;
    .complaint-id { font-weight: 600; color: #2D2A26; }
  }
  .card-body {
    .info-row { display: flex; margin-bottom: 10px; font-size: 14px; .label { width: 80px; color: #9A958F; } .type-tag { background: #F3EEFF; color: #722ed1; padding: 2px 8px; border-radius: 4px; font-size: 12px; } }
    .content-box, .reply-box, .resolution-box { margin: 12px 0; .label { font-size: 14px; color: #9A958F; } p { margin: 6px 0 0; font-size: 14px; color: #2D2A26; line-height: 1.6; } }
    .reply-box { background: #DBEAFE; padding: 12px; border-radius: 8px; .label { color: #1E40AF; } }
    .resolution-box { background: #DCFCE7; padding: 12px; border-radius: 8px; .label { color: #166534; }
      .resolution-meta { display: block; margin-top: 8px; font-size: 12px; color: #166534; }
    }
    .time-info { font-size: 12px; color: #9A958F; }
  }
  .card-actions { display: flex; gap: 10px; margin-top: 16px; padding-top: 16px; border-top: 1px solid #F0EFED;
    button { display: flex; align-items: center; gap: 6px; padding: 8px 16px; border: none; border-radius: 8px; font-size: 13px; cursor: pointer;
      &.btn-view { background: #F8F8F7; color: #6B6560; }
      &.btn-reply { background: #722ed1; color: white; }
    }
  }
}

.empty-state { text-align: center; padding: 48px 20px; color: #9A958F;
  .empty-icon { font-size: 48px; display: block; margin-bottom: 12px; }
  p { margin: 0; }
}

.detail-content { .detail-section { margin-bottom: 20px; &:last-child { margin-bottom: 0; }
    h4 { font-size: 14px; font-weight: 600; margin: 0 0 12px; color: #6B6560; }
  }
}
.detail-grid { display: grid; grid-template-columns: 1fr 1fr; gap: 12px; }
.detail-item { .label { display: block; font-size: 12px; color: #9A958F; margin-bottom: 2px; }
  .value { font-size: 14px; }
}
.detail-text { margin: 0; font-size: 14px; line-height: 1.6; color: #2D2A26;
  &.remedies { color: #722ed1; margin-top: 8px; }
}
.reply-form { .form-item { margin-bottom: 16px;
    label { display: block; font-size: 14px; font-weight: 500; margin-bottom: 8px;
      .required { color: #ff4d4f; }
    }
  }
}
.btn-cancel, .btn-confirm { padding: 10px 20px; border-radius: 8px; font-size: 14px; cursor: pointer; }
.btn-cancel { border: 1px solid #E5E4E1; background: white; color: #6B6560; margin-right: 8px; }
.btn-confirm { border: none; background: #722ed1; color: white; }
</style>
