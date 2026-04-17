<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { AppCard, AppButton } from '@/components/common'
import { adminApi } from '@/api/admin'

interface PendingInstitution {
  id: string
  name: string
  phone: string
  address: string
  status: 'pending' | 'active' | 'rejected'
  licenses?: string[]
  createdAt: string
  owner?: string
  license?: string
  documents?: string[]
}

const institutions = ref<PendingInstitution[]>([])
const loading = ref(false)
const activeTab = ref<'pending' | 'active' | 'rejected'>('pending')
const selectedInstitution = ref<PendingInstitution | null>(null)
const showDetail = ref(false)

// 加载机构列表
const loadInstitutions = async () => {
  loading.value = true
  try {
    const res = await adminApi.getInstitutions(activeTab.value)
    if (res.code === 200 && res.data) {
      institutions.value = res.data.list || []
    }
  } catch (error) {
    console.error('加载机构列表失败:', error)
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  loadInstitutions()
})

// 切换标签时重新加载
const changeTab = (tab: 'pending' | 'active' | 'rejected') => {
  activeTab.value = tab
  loadInstitutions()
}

const tabs = [
  { key: 'pending', label: '待审核' },
  { key: 'active', label: '已通过' },
  { key: 'rejected', label: '已拒绝' }
]

const filteredInstitutions = computed(() => institutions.value)

const viewDetail = (inst: PendingInstitution) => {
  selectedInstitution.value = inst
  showDetail.value = true
}

const getDocuments = (inst: PendingInstitution) => {
  if (inst.licenses && inst.licenses.length > 0) {
    return inst.licenses
  }
  if (inst.documents && inst.documents.length > 0) {
    return inst.documents
  }
  return []
}

const approveInstitution = async (inst: PendingInstitution) => {
  try {
    await ElMessageBox.confirm(
      `确定通过"${inst.name}"的入驻申请吗？`,
      '审核确认',
      { confirmButtonText: '通过', cancelButtonText: '取消', type: 'success' }
    )
    await adminApi.approveInstitution(inst.id)
    showDetail.value = false
    ElMessage.success('已通过审核')
    loadInstitutions()
  } catch {}
}

const rejectInstitution = async (inst: PendingInstitution) => {
  try {
    const { value: reason } = await ElMessageBox.prompt(
      '请输入拒绝原因',
      '拒绝申请',
      { confirmButtonText: '确认拒绝', cancelButtonText: '取消', inputPlaceholder: '拒绝原因...' }
    )
    await adminApi.rejectInstitution(inst.id, reason || '')
    showDetail.value = false
    ElMessage.success('已拒绝申请')
    loadInstitutions()
  } catch {}
}

const parseDate = (dateStr: any): Date | null => {
  if (!dateStr) return null
  try {
    let date: Date
    if (typeof dateStr === 'string') {
      if (dateStr.includes(' ')) {
        const [datePart, timePart] = dateStr.split(' ')
        const [year, month, day] = datePart.split('-')
        const [hour, minute, second] = timePart.split(':')
        date = new Date(
          parseInt(year),
          parseInt(month) - 1,
          parseInt(day),
          parseInt(hour) || 0,
          parseInt(minute) || 0,
          parseInt(second) || 0
        )
      } else if (dateStr.includes('-')) {
        const [year, month, day] = dateStr.split('-')
        date = new Date(
          parseInt(year),
          parseInt(month) - 1,
          parseInt(day)
        )
      } else {
        date = new Date(dateStr)
      }
    } else {
      date = new Date(dateStr)
    }
    
    if (isNaN(date.getTime())) {
      return null
    }
    return date
  } catch {
    return null
  }
}

const formatDate = (dateStr: any) => {
  const date = parseDate(dateStr)
  if (!date) return '-'
  return date.toLocaleDateString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit'
  })
}

const formatDateTime = (dateStr: any) => {
  const date = parseDate(dateStr)
  if (!date) return '-'
  return date.toLocaleString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit',
    second: '2-digit'
  })
}

const isImageUrl = (url: string) => {
  if (!url) return false
  return /\.(jpg|jpeg|png|gif|webp|svg)$/i.test(url) || url.startsWith('http')
}

const previewImage = (url: string) => {
  if (isImageUrl(url)) {
    window.open(url, '_blank')
  }
}
</script>

<template>
  <div class="institution-review-view">
    <div class="page-header">
      <h1>机构审核</h1>
      <p>审核寄养机构入驻申请</p>
    </div>

    <!-- 标签页 -->
    <div class="tabs">
      <button
        v-for="tab in tabs"
        :key="tab.key"
        class="tab-btn"
        :class="{ active: activeTab === tab.key }"
        @click="changeTab(tab.key as any)"
      >
        {{ tab.label }}
      </button>
    </div>

    <!-- 加载状态 -->
    <div v-if="loading" class="loading-state">加载中...</div>

    <!-- 机构列表 -->
    <div v-else class="institutions-list">
      <div v-if="filteredInstitutions.length === 0" class="empty-state">
        <span class="empty-icon">📋</span>
        <p>暂无{{ activeTab === 'pending' ? '待审核' : activeTab === 'active' ? '已通过' : '已拒绝' }}的机构</p>
      </div>

      <AppCard
        v-for="inst in filteredInstitutions"
        :key="inst.id"
        class="institution-card"
        shadow="sm"
      >
        <div class="card-main">
          <div class="inst-avatar">🏠</div>
          <div class="inst-info">
            <h3>{{ inst.name }}</h3>
            <p class="inst-owner">联系电话：{{ inst.phone }}</p>
            <p class="inst-address">{{ inst.address }}</p>
          </div>
          <div class="inst-meta">
            <span class="submit-date">提交于 {{ formatDate(inst.createdAt) }}</span>
          </div>
        </div>

        <div class="card-footer">
          <div class="documents">
            <span class="doc-label">已提交材料：</span>
            <span v-for="(doc, index) in getDocuments(inst)" :key="index" class="doc-tag">
              {{ index === 0 ? '资质证书' : index === 1 ? '经营许可证' : '材料' + (index + 1) }}
            </span>
          </div>
          <div class="card-actions">
            <AppButton type="outline" size="sm" @click="viewDetail(inst)">查看详情</AppButton>
            <template v-if="inst.status === 'pending'">
              <AppButton type="primary" size="sm" @click="approveInstitution(inst)">通过</AppButton>
              <AppButton type="ghost" size="sm" class="reject-btn" @click="rejectInstitution(inst)">拒绝</AppButton>
            </template>
          </div>
        </div>
      </AppCard>
    </div>

    <!-- 详情弹窗 -->
    <el-dialog v-model="showDetail" title="机构详情" width="600px">
      <div v-if="selectedInstitution" class="detail-content">
        <div class="detail-section">
          <h4>基本信息</h4>
          <div class="detail-row">
            <span class="label">机构名称</span>
            <span class="value">{{ selectedInstitution.name }}</span>
          </div>
          <div class="detail-row">
            <span class="label">负责人</span>
            <span class="value">{{ selectedInstitution.owner }}</span>
          </div>
          <div class="detail-row">
            <span class="label">联系电话</span>
            <span class="value">{{ selectedInstitution.phone }}</span>
          </div>
          <div class="detail-row">
            <span class="label">地址</span>
            <span class="value">{{ selectedInstitution.address }}</span>
          </div>
          <div class="detail-row">
            <span class="label">许可证号</span>
            <span class="value">{{ selectedInstitution.license }}</span>
          </div>
          <div class="detail-row">
            <span class="label">提交时间</span>
            <span class="value">{{ selectedInstitution.createdAt ? formatDateTime(selectedInstitution.createdAt) : '-' }}</span>
          </div>
        </div>

        <div class="detail-section">
          <h4>提交材料</h4>
          <div v-if="getDocuments(selectedInstitution).length === 0" class="empty-documents">
            <span>暂无提交的材料</span>
          </div>
          <div v-else class="documents-grid">
            <div v-for="(doc, index) in getDocuments(selectedInstitution)" :key="index" class="doc-item" @click="previewImage(doc)">
              <img v-if="isImageUrl(doc)" :src="doc" class="doc-image" alt="证书图片" />
              <span v-else class="doc-icon">📄</span>
              <span class="doc-label">{{ index === 0 ? '资质证书' : index === 1 ? '经营许可证' : '材料' + (index + 1) }}</span>
            </div>
          </div>
        </div>

        <div v-if="selectedInstitution.status === 'pending'" class="detail-actions">
          <AppButton type="ghost" @click="rejectInstitution(selectedInstitution)">拒绝申请</AppButton>
          <AppButton type="primary" @click="approveInstitution(selectedInstitution)">通过审核</AppButton>
        </div>
      </div>
    </el-dialog>
  </div>
</template>


<style lang="scss" scoped>
.institution-review-view {
  max-width: 1000px;
  margin: 0 auto;
}

.page-header {
  margin-bottom: 24px;

  h1 {
    font-family: var(--font-display);
    font-size: 28px;
    margin: 0 0 4px;
  }

  p {
    color: var(--color-text-secondary);
    margin: 0;
  }
}

.tabs {
  display: flex;
  gap: 8px;
  margin-bottom: 24px;
  padding-bottom: 16px;
  border-bottom: 1px solid var(--color-border);
}

.tab-btn {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 10px 16px;
  border: none;
  background: transparent;
  color: var(--color-text-secondary);
  font-size: 14px;
  font-weight: 500;
  border-radius: var(--radius-md);
  cursor: pointer;
  transition: all 150ms ease;

  &:hover {
    background: var(--color-neutral-100);
  }

  &.active {
    background: var(--color-primary);
    color: white;

    .tab-count {
      background: rgba(255, 255, 255, 0.2);
      color: white;
    }
  }

  .tab-count {
    padding: 2px 8px;
    background: var(--color-neutral-200);
    font-size: 12px;
    border-radius: 10px;
  }
}

.institutions-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.empty-state {
  text-align: center;
  padding: 60px 20px;

  .empty-icon {
    font-size: 48px;
    display: block;
    margin-bottom: 12px;
    opacity: 0.5;
  }

  p {
    color: var(--color-text-muted);
    margin: 0;
  }
}

.institution-card {
  .card-main {
    display: flex;
    gap: 16px;
    margin-bottom: 16px;
  }

  .inst-avatar {
    width: 64px;
    height: 64px;
    background: var(--color-primary-light);
    border-radius: var(--radius-lg);
    display: flex;
    align-items: center;
    justify-content: center;
    font-size: 32px;
    flex-shrink: 0;
  }

  .inst-info {
    flex: 1;

    h3 {
      font-size: 18px;
      margin: 0 0 8px;
    }

    p {
      font-size: 14px;
      color: var(--color-text-secondary);
      margin: 0 0 4px;
    }
  }

  .inst-meta {
    text-align: right;

    .submit-date {
      font-size: 13px;
      color: var(--color-text-muted);
    }
  }

  .card-footer {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding-top: 16px;
    border-top: 1px solid var(--color-border);

    @media (max-width: 640px) {
      flex-direction: column;
      gap: 12px;
      align-items: flex-start;
    }
  }

  .documents {
    display: flex;
    align-items: center;
    gap: 8px;
    flex-wrap: wrap;

    .doc-label {
      font-size: 13px;
      color: var(--color-text-muted);
    }

    .doc-tag {
      padding: 4px 10px;
      background: var(--color-neutral-100);
      font-size: 12px;
      border-radius: var(--radius-full);
    }
  }

  .card-actions {
    display: flex;
    gap: 8px;

    .reject-btn {
      color: var(--color-error);

      &:hover {
        background: rgba(239, 68, 68, 0.1);
      }
    }
  }
}

// 详情弹窗
.detail-content {
  .detail-section {
    margin-bottom: 24px;

    h4 {
      font-size: 16px;
      font-weight: 600;
      margin: 0 0 16px;
      padding-bottom: 8px;
      border-bottom: 1px solid var(--color-border);
    }
  }

  .detail-row {
    display: flex;
    padding: 8px 0;

    .label {
      width: 100px;
      color: var(--color-text-muted);
      flex-shrink: 0;
    }

    .value {
      flex: 1;
      color: var(--color-text-primary);
    }
  }

  .empty-documents {
    text-align: center;
    padding: 40px;
    color: var(--color-text-muted);
  }

  .documents-grid {
    display: grid;
    grid-template-columns: repeat(2, 1fr);
    gap: 16px;

    .doc-item {
      display: flex;
      flex-direction: column;
      align-items: center;
      gap: 12px;
      padding: 20px;
      background: var(--color-neutral-100);
      border-radius: var(--radius-md);
      font-size: 14px;
      cursor: pointer;
      transition: all 0.2s ease;

      &:hover {
        background: var(--color-primary-light);
      }

      .doc-image {
        width: 100%;
        max-height: 200px;
        object-fit: contain;
        border-radius: var(--radius-sm);
        background: white;
      }

      .doc-icon {
        font-size: 48px;
      }

      .doc-label {
        font-weight: 500;
        color: var(--color-text-primary);
      }
    }
  }

  .detail-actions {
    display: flex;
    justify-content: flex-end;
    gap: 12px;
    margin-top: 24px;
    padding-top: 20px;
    border-top: 1px solid var(--color-border);
  }
}
</style>
