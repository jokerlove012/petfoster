<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox, ElDialog, ElInput } from 'element-plus'
import { AppCard } from '@/components/common'
import { adminApi } from '@/api/admin'

interface Complaint {
  id: string
  complaintNumber: string
  category: string
  petOwnerName: string
  petOwnerPhone: string
  institutionName: string
  institutionId: string
  bookingOrderNumber: string
  description: string
  evidence: string[]
  expectation: string
  status: 'pending' | 'awaiting_response' | 'under_review' | 'resolved'
  institutionResponse?: string
  resolution?: {
    decision: string
    remedies: string
    resolvedBy: string
    resolvedAt: string
  }
  createdAt: string
  updatedAt: string
}

const complaints = ref<Complaint[]>([])
const loading = ref(false)
const filterStatus = ref<string>('all')
const statusCounts = ref({ all: 0, pending: 0, awaiting_response: 0, under_review: 0, resolved: 0 })
const currentComplaint = ref<Complaint | null>(null)
const showDetailDialog = ref(false)
const showResolveDialog = ref(false)
const resolveForm = ref({ decision: '', remedies: '' })

const loadComplaints = async () => {
  loading.value = true
  try {
    const res = await adminApi.getComplaints(filterStatus.value)
    if (res.data?.list) {
      complaints.value = res.data.list
    }
  } catch (error) {
    console.error('加载投诉列表失败:', error)
  } finally {
    loading.value = false
  }
}

const loadStats = async () => {
  try {
    const res = await adminApi.getComplaintStats()
    if (res.data) {
      statusCounts.value = {
        all: res.data.all || 0,
        pending: res.data.pending || 0,
        awaiting_response: res.data.awaiting_response || 0,
        under_review: res.data.under_review || 0,
        resolved: res.data.resolved || 0
      }
    }
  } catch (error) {
    console.error('加载统计失败:', error)
  }
}

const filteredComplaints = computed(() => complaints.value)

const getCategoryName = (category: string) => {
  const map: Record<string, string> = {
    service_quality: '服务质量', pet_safety: '宠物安全', communication: '沟通问题',
    facility: '设施环境', price: '价格争议', other: '其他'
  }
  return map[category] || category
}

const getStatusInfo = (status: string) => {
  const map: Record<string, { label: string; class: string }> = {
    pending: { label: '待处理', class: 'warning' },
    awaiting_response: { label: '等待机构回复', class: 'info' },
    under_review: { label: '审核中', class: 'primary' },
    resolved: { label: '已解决', class: 'success' }
  }
  return map[status] || { label: status, class: '' }
}

const viewDetail = async (complaint: Complaint) => {
  currentComplaint.value = complaint
  showDetailDialog.value = true
}

const urgeResponse = async (complaint: Complaint) => {
  try {
    await ElMessageBox.confirm(`确定要催促「${complaint.institutionName}」回复吗？`, '催促回复',
      { confirmButtonText: '确定', cancelButtonText: '取消' })
    await adminApi.urgeComplaintResponse(complaint.id)
    ElMessage.success('已发送催促通知')
  } catch { /* cancelled */ }
}

const startResolve = (complaint: Complaint) => {
  currentComplaint.value = complaint
  resolveForm.value = { decision: '', remedies: '' }
  showResolveDialog.value = true
}

const submitResolve = async () => {
  if (!resolveForm.value.decision.trim()) {
    ElMessage.warning('请输入处理决定')
    return
  }
  if (!currentComplaint.value) return
  try {
    await adminApi.resolveComplaint(currentComplaint.value.id, resolveForm.value.decision, resolveForm.value.remedies)
    showResolveDialog.value = false
    ElMessage.success('投诉已处理完成')
    loadComplaints()
    loadStats()
  } catch (error) {
    ElMessage.error('处理失败')
  }
}

const changeFilter = (status: string) => {
  filterStatus.value = status
  loadComplaints()
}

// 判断是否为图片
const isImage = (file: string) => {
  const ext = file.split('.').pop()?.toLowerCase()
  return ['jpg', 'jpeg', 'png', 'gif', 'webp', 'bmp'].includes(ext || '')
}

// 获取文件完整URL
const getFileUrl = (file: string) => {
  if (file.startsWith('http')) return file
  return `http://localhost:8080${file}`
}

// 获取文件名
const getFileName = (file: string) => {
  return file.split('/').pop() || file
}

// 预览图片
const previewImage = (file: string) => {
  window.open(getFileUrl(file), '_blank')
}

onMounted(() => {
  loadComplaints()
  loadStats()
})
</script>

<template>
  <div class="complaint-manage-view">
    <div class="page-header">
      <div class="header-info">
        <h1>投诉管理</h1>
        <p>处理用户投诉，维护平台秩序</p>
      </div>
    </div>

    <div class="status-tabs">
      <button class="status-tab" :class="{ active: filterStatus === 'all' }" @click="changeFilter('all')">
        全部 <span class="count">{{ statusCounts.all }}</span>
      </button>
      <button class="status-tab" :class="{ active: filterStatus === 'pending' }" @click="changeFilter('pending')">
        待处理 <span class="count warning">{{ statusCounts.pending }}</span>
      </button>
      <button class="status-tab" :class="{ active: filterStatus === 'awaiting_response' }" @click="changeFilter('awaiting_response')">
        等待回复 <span class="count">{{ statusCounts.awaiting_response }}</span>
      </button>
      <button class="status-tab" :class="{ active: filterStatus === 'under_review' }" @click="changeFilter('under_review')">
        审核中 <span class="count">{{ statusCounts.under_review }}</span>
      </button>
      <button class="status-tab" :class="{ active: filterStatus === 'resolved' }" @click="changeFilter('resolved')">
        已解决 <span class="count success">{{ statusCounts.resolved }}</span>
      </button>
    </div>

    <div class="complaint-list" v-loading="loading">
      <AppCard v-for="complaint in filteredComplaints" :key="complaint.id" class="complaint-card" shadow="sm">
        <div class="complaint-header">
          <div class="complaint-info">
            <span class="complaint-number">{{ complaint.complaintNumber }}</span>
            <span class="complaint-category">{{ getCategoryName(complaint.category) }}</span>
            <span class="complaint-status" :class="getStatusInfo(complaint.status).class">
              {{ getStatusInfo(complaint.status).label }}
            </span>
          </div>
          <span class="complaint-time">{{ complaint.createdAt }}</span>
        </div>
        <div class="complaint-body">
          <div class="parties">
            <div class="party">
              <span class="party-label">投诉人</span>
              <span class="party-value">{{ complaint.petOwnerName }} ({{ complaint.petOwnerPhone }})</span>
            </div>
            <div class="party">
              <span class="party-label">被投诉机构</span>
              <span class="party-value">{{ complaint.institutionName }}</span>
            </div>
            <div class="party">
              <span class="party-label">订单号</span>
              <span class="party-value">{{ complaint.bookingOrderNumber }}</span>
            </div>
          </div>
          <div class="complaint-desc"><p>{{ complaint.description }}</p></div>
          <div v-if="complaint.institutionResponse" class="institution-response">
            <span class="response-label">机构回复：</span>
            <p>{{ complaint.institutionResponse }}</p>
          </div>
          <div v-if="complaint.resolution" class="resolution-info">
            <span class="resolution-label">处理结果：</span>
            <p>{{ complaint.resolution.decision }}</p>
            <span class="resolution-meta">{{ complaint.resolution.resolvedBy }} · {{ complaint.resolution.resolvedAt }}</span>
          </div>
        </div>
        <div class="complaint-actions">
          <button class="action-btn" @click="viewDetail(complaint)">查看详情</button>
          <button v-if="complaint.status === 'pending'" class="action-btn primary" @click="urgeResponse(complaint)">催促回复</button>
          <button v-if="complaint.status === 'awaiting_response' || complaint.status === 'pending'" class="action-btn primary" @click="startResolve(complaint)">处理投诉</button>
        </div>
      </AppCard>
      <div v-if="!loading && filteredComplaints.length === 0" class="empty-state">
        <span class="empty-icon">📭</span>
        <p>暂无投诉记录</p>
      </div>
    </div>

    <ElDialog v-model="showDetailDialog" title="投诉详情" width="600px">
      <div v-if="currentComplaint" class="detail-content">
        <div class="detail-section">
          <h4>基本信息</h4>
          <div class="detail-grid">
            <div class="detail-item"><span class="label">投诉编号</span><span class="value">{{ currentComplaint.complaintNumber }}</span></div>
            <div class="detail-item"><span class="label">投诉类型</span><span class="value">{{ getCategoryName(currentComplaint.category) }}</span></div>
            <div class="detail-item"><span class="label">投诉人</span><span class="value">{{ currentComplaint.petOwnerName }}</span></div>
            <div class="detail-item"><span class="label">联系电话</span><span class="value">{{ currentComplaint.petOwnerPhone }}</span></div>
            <div class="detail-item"><span class="label">被投诉机构</span><span class="value">{{ currentComplaint.institutionName }}</span></div>
            <div class="detail-item"><span class="label">相关订单</span><span class="value">{{ currentComplaint.bookingOrderNumber }}</span></div>
          </div>
        </div>
        <div class="detail-section"><h4>投诉内容</h4><p class="detail-text">{{ currentComplaint.description }}</p></div>
        <div class="detail-section">
          <h4>证据材料</h4>
          <div class="evidence-list" v-if="currentComplaint.evidence?.length">
            <div v-for="(file, index) in currentComplaint.evidence" :key="index" class="evidence-item">
              <img 
                v-if="isImage(file)" 
                :src="getFileUrl(file)" 
                class="evidence-image" 
                @click="previewImage(file)"
              />
              <a v-else :href="getFileUrl(file)" target="_blank" class="evidence-link">📎 {{ getFileName(file) }}</a>
            </div>
          </div>
          <div v-else class="no-evidence">暂无证据材料</div>
        </div>
        <div v-if="currentComplaint.expectation" class="detail-section"><h4>期望处理</h4><p class="detail-text">{{ currentComplaint.expectation }}</p></div>
        <div v-if="currentComplaint.institutionResponse" class="detail-section"><h4>机构回复</h4><p class="detail-text">{{ currentComplaint.institutionResponse }}</p></div>
      </div>
    </ElDialog>

    <ElDialog v-model="showResolveDialog" title="处理投诉" width="500px">
      <div class="resolve-form">
        <div class="form-item">
          <label>处理决定 <span class="required">*</span></label>
          <ElInput v-model="resolveForm.decision" type="textarea" :rows="4" placeholder="请输入处理决定和理由" />
        </div>
        <div class="form-item">
          <label>补偿措施（可选）</label>
          <ElInput v-model="resolveForm.remedies" placeholder="如：退款、优惠券等" />
        </div>
      </div>
      <template #footer>
        <button class="btn-cancel" @click="showResolveDialog = false">取消</button>
        <button class="btn-confirm" @click="submitResolve">确认处理</button>
      </template>
    </ElDialog>
  </div>
</template>


<style lang="scss" scoped>
.complaint-manage-view { max-width: 1000px; margin: 0 auto; }
.page-header { margin-bottom: 24px;
  h1 { font-family: var(--font-display); font-size: 28px; margin: 0 0 4px; }
  p { color: var(--color-text-secondary); margin: 0; }
}
.status-tabs { display: flex; gap: 8px; margin-bottom: 20px; flex-wrap: wrap; }
.status-tab {
  padding: 10px 16px; border: 1px solid var(--color-border); border-radius: var(--radius-full);
  background: var(--color-surface); font-size: 14px; color: var(--color-text-secondary); cursor: pointer;
  .count { margin-left: 4px; padding: 2px 6px; background: var(--color-neutral-200); border-radius: 10px; font-size: 12px;
    &.warning { background: #FEF3C7; color: #92400E; }
    &.success { background: #DCFCE7; color: #166534; }
  }
  &:hover { border-color: var(--color-primary); color: var(--color-primary); }
  &.active { background: var(--color-primary); border-color: var(--color-primary); color: white;
    .count { background: rgba(255, 255, 255, 0.2); color: white; }
  }
}
.complaint-card { margin-bottom: 16px; }
.complaint-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 12px; }
.complaint-info { display: flex; align-items: center; gap: 12px; }
.complaint-number { font-family: var(--font-mono, monospace); font-size: 14px; font-weight: 600; }
.complaint-category { padding: 4px 8px; background: var(--color-neutral-100); border-radius: var(--radius-sm); font-size: 12px; }
.complaint-status { padding: 4px 10px; border-radius: var(--radius-full); font-size: 12px; font-weight: 500;
  &.warning { background: #FEF3C7; color: #92400E; }
  &.info { background: #DBEAFE; color: #1E40AF; }
  &.primary { background: var(--color-primary-light); color: var(--color-primary); }
  &.success { background: #DCFCE7; color: #166534; }
}
.complaint-time { font-size: 13px; color: var(--color-text-muted); }
.complaint-body { margin-bottom: 16px; }
.parties { display: flex; gap: 24px; margin-bottom: 12px; flex-wrap: wrap; }
.party { .party-label { font-size: 12px; color: var(--color-text-muted); display: block; margin-bottom: 2px; }
  .party-value { font-size: 14px; }
}
.complaint-desc { padding: 12px; background: var(--color-neutral-50); border-radius: var(--radius-md);
  p { margin: 0; font-size: 14px; line-height: 1.6; color: var(--color-text-secondary); }
}
.institution-response, .resolution-info { margin-top: 12px; padding: 12px; border-radius: var(--radius-md);
  .response-label, .resolution-label { font-size: 12px; font-weight: 600; display: block; margin-bottom: 4px; }
  p { margin: 0; font-size: 14px; line-height: 1.6; }
}
.institution-response { background: #DBEAFE; .response-label { color: #1E40AF; } }
.resolution-info { background: #DCFCE7; .resolution-label { color: #166534; }
  .resolution-meta { display: block; margin-top: 8px; font-size: 12px; color: #166534; }
}
.complaint-actions { display: flex; gap: 8px; padding-top: 12px; border-top: 1px solid var(--color-border); }
.action-btn { padding: 8px 16px; border: 1px solid var(--color-border); border-radius: var(--radius-md);
  background: var(--color-surface); font-size: 13px; color: var(--color-text-secondary); cursor: pointer;
  &:hover { border-color: var(--color-primary); color: var(--color-primary); }
  &.primary { background: var(--color-primary); border-color: var(--color-primary); color: white;
    &:hover { background: var(--color-primary-dark); }
  }
}
.empty-state { text-align: center; padding: 48px 20px; color: var(--color-text-muted);
  .empty-icon { font-size: 48px; display: block; margin-bottom: 12px; }
  p { margin: 0; }
}
.detail-content { .detail-section { margin-bottom: 20px; &:last-child { margin-bottom: 0; }
    h4 { font-size: 14px; font-weight: 600; margin: 0 0 12px; color: var(--color-text-secondary); }
  }
}
.detail-grid { display: grid; grid-template-columns: 1fr 1fr; gap: 12px; }
.detail-item { .label { display: block; font-size: 12px; color: var(--color-text-muted); margin-bottom: 2px; }
  .value { font-size: 14px; }
}
.detail-text { margin: 0; font-size: 14px; line-height: 1.6; color: var(--color-text-secondary); }
.evidence-list { display: flex; flex-wrap: wrap; gap: 12px; }
.evidence-item { 
  .evidence-image {
    width: 120px;
    height: 120px;
    object-fit: cover;
    border-radius: var(--radius-md);
    cursor: pointer;
    border: 1px solid var(--color-border);
    transition: transform 0.2s;
    &:hover {
      transform: scale(1.05);
      box-shadow: 0 4px 12px rgba(0,0,0,0.15);
    }
  }
  .evidence-link {
    display: block;
    padding: 8px 12px;
    background: var(--color-neutral-100);
    border-radius: var(--radius-sm);
    font-size: 13px;
    color: var(--color-primary);
    text-decoration: none;
    &:hover { background: var(--color-neutral-200); }
  }
}
.no-evidence { color: var(--color-text-muted); font-size: 13px; }
.resolve-form { .form-item { margin-bottom: 16px; &:last-child { margin-bottom: 0; }
    label { display: block; font-size: 14px; font-weight: 500; margin-bottom: 8px;
      .required { color: var(--color-error); }
    }
  }
}
.btn-cancel, .btn-confirm { padding: 10px 20px; border-radius: var(--radius-md); font-size: 14px; cursor: pointer; }
.btn-cancel { border: 1px solid var(--color-border); background: var(--color-surface); color: var(--color-text-secondary); margin-right: 8px;
  &:hover { border-color: var(--color-text-secondary); }
}
.btn-confirm { border: none; background: var(--color-primary); color: white;
  &:hover { background: var(--color-primary-dark); }
}
</style>
