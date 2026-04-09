<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { FileCheck, Search, Eye, Check, X, Clock, Building2, FileText } from 'lucide-vue-next'
import { ElMessage, ElMessageBox } from 'element-plus'
import { adminApi } from '@/api/admin'

const searchKeyword = ref('')
const statusFilter = ref('pending')
const loading = ref(false)
const currentPage = ref(1)
const pageSize = ref(20)
const total = ref(0)
const viewModalVisible = ref(false)
const currentInstitution = ref<InstitutionReview | null>(null)

interface InstitutionReview {
  id: string
  name: string
  phone: string
  address: string
  status: 'pending' | 'active' | 'rejected'
  licenses?: string[]
  createdAt: string
  owner?: string
  rejectReason?: string
  description?: string
  businessLicense?: string
  idCard?: string
  contactPhone?: string
}

// 审核统计
const reviewStats = ref({
  pending: 0,
  approved: 0,
  rejected: 0,
  total: 0
})

// 待审核列表
const reviewList = ref<InstitutionReview[]>([])

// 加载数据
const loadData = async () => {
  loading.value = true
  try {
    const [institutionsRes, dashboardRes] = await Promise.all([
      adminApi.getInstitutions(statusFilter.value, currentPage.value, pageSize.value),
      adminApi.getDashboardStats()
    ])
    
    if (institutionsRes.code === 200 && institutionsRes.data) {
      reviewList.value = institutionsRes.data.list || []
      total.value = institutionsRes.data.pagination?.total || 0
    }
    
    if (dashboardRes.code === 200 && dashboardRes.data) {
      reviewStats.value = {
        pending: dashboardRes.data.pendingInstitutions || 0,
        approved: dashboardRes.data.activeInstitutions || 0,
        rejected: dashboardRes.data.rejectedInstitutions || 0,
        total: dashboardRes.data.totalInstitutions || 0
      }
    }
  } catch (error) {
    console.error('加载数据失败:', error)
    ElMessage.error('加载数据失败')
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  loadData()
})

const filteredList = computed(() => {
  return reviewList.value.filter(item => {
    const matchSearch = !searchKeyword.value || item.name.includes(searchKeyword.value) || item.id.includes(searchKeyword.value)
    return matchSearch
  })
})

const getStatusLabel = (status: string) => {
  const map: Record<string, string> = { pending: '待审核', active: '已通过', rejected: '已拒绝' }
  return map[status] || status
}

const getStatusType = (status: string) => {
  const map: Record<string, string> = { pending: 'warning', active: 'success', rejected: 'danger' }
  return map[status] || 'info'
}

const viewDetail = async (item: InstitutionReview) => {
  try {
    const res = await adminApi.getInstitutionDetail(item.id)
    if (res.code === 200) {
      currentInstitution.value = res.data
    } else {
      currentInstitution.value = item
    }
  } catch (error) {
    currentInstitution.value = item
  }
  viewModalVisible.value = true
}

const approveInstitution = async (item: InstitutionReview) => {
  try {
    await ElMessageBox.confirm(`确认通过 "${item.name}" 的资质审核？`, '确认通过', { type: 'success' })
    await adminApi.approveInstitution(item.id)
    ElMessage.success('审核已通过')
    loadData()
  } catch (error) {
    console.error('审核失败:', error)
  }
}

const rejectInstitution = async (item: InstitutionReview) => {
  try {
    const { value } = await ElMessageBox.prompt('请输入拒绝原因', '拒绝审核', { inputPattern: /.+/, inputErrorMessage: '请输入拒绝原因' })
    await adminApi.rejectInstitution(item.id, value || '')
    ElMessage.success('已拒绝该申请')
    loadData()
  } catch (error) {
    console.error('拒绝失败:', error)
  }
}
</script>

<template>
  <div class="qualification-review">
    <div class="page-header">
      <div class="header-left">
        <h1>📋 资质审核</h1>
        <p>审核机构入驻申请</p>
      </div>
    </div>

    <!-- 加载状态 -->
    <div v-if="loading" class="loading-state">加载中...</div>

    <template v-else>
      <!-- 统计卡片 -->
      <div class="stats-grid">
        <div class="stat-card pending" @click="statusFilter = 'pending'; loadData()">
          <Clock :size="24" />
          <div class="stat-info">
            <span class="stat-value">{{ reviewStats.pending }}</span>
            <span class="stat-label">待审核</span>
          </div>
        </div>
        <div class="stat-card approved" @click="statusFilter = 'active'; loadData()">
          <Check :size="24" />
          <div class="stat-info">
            <span class="stat-value">{{ reviewStats.approved }}</span>
            <span class="stat-label">已通过</span>
          </div>
        </div>
        <div class="stat-card rejected" @click="statusFilter = 'rejected'; loadData()">
          <X :size="24" />
          <div class="stat-info">
            <span class="stat-value">{{ reviewStats.rejected }}</span>
            <span class="stat-label">已拒绝</span>
          </div>
        </div>
        <div class="stat-card total" @click="statusFilter = ''; loadData()">
          <Building2 :size="24" />
          <div class="stat-info">
            <span class="stat-value">{{ reviewStats.total }}</span>
            <span class="stat-label">总申请</span>
          </div>
        </div>
      </div>

      <!-- 筛选栏 -->
      <div class="filter-bar">
        <div class="search-box">
          <Search :size="18" />
          <input v-model="searchKeyword" placeholder="搜索机构名称/ID" />
        </div>
        <div class="status-tabs">
          <button v-for="s in ['pending', 'active', 'rejected', '']" :key="s || 'all'" 
            :class="{ active: statusFilter === s }" @click="statusFilter = s; loadData()">
            {{ { pending: '待审核', active: '已通过', rejected: '已拒绝', '': '全部' }[s || ''] }}
          </button>
        </div>
      </div>

      <!-- 审核列表 -->
      <div v-if="filteredList.length === 0" class="empty-state">
        <span class="empty-icon">📋</span>
        <p>暂无{{ statusFilter === 'pending' ? '待审核' : statusFilter === 'active' ? '已通过' : statusFilter === 'rejected' ? '已拒绝' : '' }}的机构</p>
      </div>
      
      <div v-else class="review-list">
        <div v-for="item in filteredList" :key="item.id" class="review-card">
          <div class="card-header">
            <div class="institution-info">
              <Building2 :size="20" />
              <span class="name">{{ item.name }}</span>
            </div>
            <el-tag :type="getStatusType(item.status)" size="small">{{ getStatusLabel(item.status) }}</el-tag>
          </div>
          <div class="card-body">
            <div class="info-grid">
              <div class="info-item"><span class="label">申请ID:</span><span>{{ item.id }}</span></div>
              <div class="info-item"><span class="label">联系人:</span><span>{{ item.owner || '-' }}</span></div>
              <div class="info-item"><span class="label">电话:</span><span>{{ item.phone }}</span></div>
            </div>
            <div class="address"><span class="label">地址:</span>{{ item.address }}</div>
            <div class="documents" v-if="item.licenses && item.licenses.length">
              <span class="label">已提交资料:</span>
              <div class="doc-tags">
                <span v-for="doc in item.licenses" :key="doc" class="doc-tag"><FileText :size="12" /> {{ doc }}</span>
              </div>
            </div>
            <div class="apply-time">申请时间: {{ item.createdAt ? new Date(item.createdAt).toLocaleString('zh-CN') : '-' }}</div>
            <div v-if="item.rejectReason" class="reject-reason">拒绝原因: {{ item.rejectReason }}</div>
          </div>
          <div class="card-actions">
            <button class="btn-view" @click="viewDetail(item)"><Eye :size="14" /> 查看详情</button>
            <template v-if="item.status === 'pending'">
              <button class="btn-approve" @click="approveInstitution(item)"><Check :size="14" /> 通过</button>
              <button class="btn-reject" @click="rejectInstitution(item)"><X :size="14" /> 拒绝</button>
            </template>
          </div>
        </div>
      </div>

      <!-- 分页 -->
      <div v-if="total > 0" class="pagination">
        <span class="total">共 {{ total }} 条记录</span>
        <el-pagination
          v-model:current-page="currentPage"
          v-model:page-size="pageSize"
          :total="total"
          :page-sizes="[10, 20, 50, 100]"
          layout="sizes, prev, pager, next"
          @size-change="loadData"
          @current-change="loadData"
        />
      </div>
    </template>

    <!-- 查看机构详情模态框 -->
    <div v-if="viewModalVisible" class="modal-overlay" @click.self="viewModalVisible = false">
      <div class="modal">
        <div class="modal-header">
          <h3>机构详情</h3>
          <button class="modal-close" @click="viewModalVisible = false">
            <X :size="20" />
          </button>
        </div>
        <div class="modal-body" v-if="currentInstitution">
          <div class="detail-item">
            <span class="label">机构名称</span>
            <span class="value">{{ currentInstitution.name }}</span>
          </div>
          <div class="detail-item">
            <span class="label">负责人</span>
            <span class="value">{{ currentInstitution.owner || '-' }}</span>
          </div>
          <div class="detail-item">
            <span class="label">联系电话</span>
            <span class="value">{{ currentInstitution.phone || currentInstitution.contactPhone || '-' }}</span>
          </div>
          <div class="detail-item">
            <span class="label">地址</span>
            <span class="value">{{ currentInstitution.address }}</span>
          </div>
          <div class="detail-item">
            <span class="label">状态</span>
            <el-tag :type="getStatusType(currentInstitution.status)" size="small">{{ getStatusLabel(currentInstitution.status) }}</el-tag>
          </div>
          <div class="detail-item" v-if="currentInstitution.description">
            <span class="label">机构简介</span>
            <span class="value">{{ currentInstitution.description }}</span>
          </div>
          <div class="detail-item" v-if="currentInstitution.licenses && currentInstitution.licenses.length">
            <span class="label">已提交资料</span>
            <div class="doc-tags">
              <span v-for="doc in currentInstitution.licenses" :key="doc" class="doc-tag"><FileText :size="12" /> {{ doc }}</span>
            </div>
          </div>
          <div class="detail-item">
            <span class="label">申请时间</span>
            <span class="value">{{ currentInstitution.createdAt ? new Date(currentInstitution.createdAt).toLocaleString('zh-CN') : '-' }}</span>
          </div>
          <div class="detail-item" v-if="currentInstitution.rejectReason">
            <span class="label">拒绝原因</span>
            <span class="value reject-reason">{{ currentInstitution.rejectReason }}</span>
          </div>
        </div>
        <div class="modal-footer">
          <button class="btn-cancel" @click="viewModalVisible = false">关闭</button>
          <template v-if="currentInstitution && currentInstitution.status === 'pending'">
            <button class="btn-reject" @click="rejectInstitution(currentInstitution); viewModalVisible = false">拒绝</button>
            <button class="btn-approve" @click="approveInstitution(currentInstitution); viewModalVisible = false">通过</button>
          </template>
        </div>
      </div>
    </div>
  </div>
</template>

<style lang="scss" scoped>
.qualification-review { max-width: 1200px; margin: 0 auto; padding: 24px; }

.page-header {
  margin-bottom: 24px;
  h1 { font-size: 26px; font-weight: 700; margin: 0 0 6px; }
  p { color: #6B6560; margin: 0; }
}

.loading-state, .empty-state {
  text-align: center;
  padding: 60px 20px;
  color: #9A958F;
  .empty-icon {
    font-size: 48px;
    display: block;
    margin-bottom: 12px;
    opacity: 0.5;
  }
  p { margin: 0; }
}

.stats-grid {
  display: grid; grid-template-columns: repeat(4, 1fr); gap: 16px; margin-bottom: 24px;
  @media (max-width: 768px) { grid-template-columns: repeat(2, 1fr); }
}

.stat-card {
  display: flex; align-items: center; gap: 16px; padding: 20px;
  background: white; border-radius: 14px; cursor: pointer; transition: transform 0.2s;
  &:hover { transform: translateY(-2px); }
  &.pending { color: #faad14; }
  &.approved { color: #52c41a; }
  &.rejected { color: #ff4d4f; }
  &.total { color: #722ed1; }
  .stat-info {
    .stat-value { display: block; font-size: 24px; font-weight: 700; color: #2D2A26; }
    .stat-label { font-size: 13px; color: #6B6560; }
  }
}

.filter-bar {
  display: flex; gap: 16px; margin-bottom: 20px; flex-wrap: wrap;
  .search-box {
    display: flex; align-items: center; gap: 10px; padding: 10px 16px;
    background: white; border-radius: 10px; flex: 1; min-width: 200px;
    input { border: none; outline: none; flex: 1; font-size: 14px; }
    color: #9A958F;
  }
  .status-tabs {
    display: flex; background: white; border-radius: 10px; padding: 4px;
    button {
      padding: 8px 16px; border: none; background: transparent; font-size: 13px;
      color: #6B6560; cursor: pointer; border-radius: 6px;
      &.active { background: #722ed1; color: white; }
    }
  }
}

.review-list { display: flex; flex-direction: column; gap: 16px; }

.review-card {
  background: white; border-radius: 14px; padding: 20px;
  .card-header {
    display: flex; justify-content: space-between; align-items: center; margin-bottom: 16px;
    .institution-info { display: flex; align-items: center; gap: 10px; color: #722ed1;
      .name { font-size: 16px; font-weight: 600; color: #2D2A26; }
    }
  }
  .card-body {
    .info-grid { display: grid; grid-template-columns: repeat(4, 1fr); gap: 12px; margin-bottom: 12px;
      @media (max-width: 768px) { grid-template-columns: repeat(2, 1fr); }
      .info-item { font-size: 14px; .label { color: #9A958F; margin-right: 6px; } }
    }
    .address, .apply-time { font-size: 14px; margin-bottom: 8px; .label { color: #9A958F; margin-right: 6px; } }
    .documents { margin: 12px 0;
      .label { font-size: 14px; color: #9A958F; }
      .doc-tags { display: flex; flex-wrap: wrap; gap: 8px; margin-top: 8px; }
      .doc-tag { display: flex; align-items: center; gap: 4px; padding: 4px 10px; background: #F3EEFF; color: #722ed1; border-radius: 6px; font-size: 12px; }
    }
    .reject-reason { color: #ff4d4f; font-size: 14px; margin-top: 8px; }
  }
  .card-actions {
    display: flex; gap: 10px; margin-top: 16px; padding-top: 16px; border-top: 1px solid #F0EFED;
    button {
      display: flex; align-items: center; gap: 6px; padding: 8px 16px;
      border: none; border-radius: 8px; font-size: 13px; cursor: pointer;
      &.btn-view { background: #F8F8F7; color: #6B6560; }
      &.btn-approve { background: #52c41a; color: white; }
      &.btn-reject { background: #ff4d4f; color: white; }
    }
  }
}

.pagination {
  display: flex; justify-content: space-between; align-items: center; margin-top: 24px; padding: 16px;
  background: white; border-radius: 12px;
  .total { font-size: 13px; color: #6B6560; }
}

.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  justify-content: center;
  align-items: center;
  z-index: 1000;
}

.modal {
  background: white;
  border-radius: 16px;
  width: 90%;
  max-width: 600px;
  max-height: 80vh;
  overflow-y: auto;
}

.modal-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20px 24px;
  border-bottom: 1px solid #F0F0EF;
  h3 {
    margin: 0;
    font-size: 18px;
    font-weight: 600;
  }
}

.modal-close {
  background: none;
  border: none;
  cursor: pointer;
  color: #9A958F;
  display: flex;
  align-items: center;
  justify-content: center;
  &:hover {
    color: #6B6560;
  }
}

.modal-body {
  padding: 24px;
}

.detail-item {
  display: flex;
  flex-direction: column;
  padding: 12px 0;
  border-bottom: 1px solid #F0F0EF;
  &:last-child {
    border-bottom: none;
  }
  .label {
    font-size: 14px;
    color: #9A958F;
    margin-bottom: 8px;
  }
  .value {
    font-size: 14px;
    font-weight: 500;
    color: #2D2A26;
  }
  .reject-reason {
    color: #ff4d4f;
  }
}

.doc-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  margin-top: 8px;
}

.doc-tag {
  display: flex;
  align-items: center;
  gap: 4px;
  padding: 4px 10px;
  background: #F3EEFF;
  color: #722ed1;
  border-radius: 6px;
  font-size: 12px;
}

.modal-footer {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
  padding: 16px 24px;
  border-top: 1px solid #F0F0EF;
}

.btn-cancel {
  padding: 10px 24px;
  border: 1px solid #E5E5E5;
  background: white;
  color: #6B6560;
  border-radius: 8px;
  font-size: 14px;
  cursor: pointer;
  &:hover {
    background: #F8F8F7;
  }
}

.btn-approve {
  padding: 10px 24px;
  border: none;
  background: #52c41a;
  color: white;
  border-radius: 8px;
  font-size: 14px;
  cursor: pointer;
  &:hover {
    background: #389e0d;
  }
}

.btn-reject {
  padding: 10px 24px;
  border: none;
  background: #ff4d4f;
  color: white;
  border-radius: 8px;
  font-size: 14px;
  cursor: pointer;
  &:hover {
    background: #d9363e;
  }
}
</style>
