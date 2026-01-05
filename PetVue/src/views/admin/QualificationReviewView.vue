<script setup lang="ts">
import { ref, computed } from 'vue'
import { FileCheck, Search, Eye, Check, X, Clock, Building2, FileText } from 'lucide-vue-next'
import { ElMessage, ElMessageBox } from 'element-plus'

const searchKeyword = ref('')
const statusFilter = ref('pending')

// 审核统计
const reviewStats = ref({
  pending: 12,
  approved: 156,
  rejected: 23,
  total: 191
})

// 待审核列表
const reviewList = ref([
  { id: 'INS001', name: '爱宠之家旗舰店', contact: '张经理', phone: '138****1234', address: '北京市朝阳区xxx路123号', 
    license: 'BJ2025001', applyTime: '2025-01-15 10:30', status: 'pending',
    documents: ['营业执照', '卫生许可证', '从业资格证'] },
  { id: 'INS002', name: '萌宠乐园', contact: '李店长', phone: '139****5678', address: '上海市浦东新区xxx街456号',
    license: 'SH2025002', applyTime: '2025-01-14 15:20', status: 'pending',
    documents: ['营业执照', '卫生许可证'] },
  { id: 'INS003', name: '温馨宠物之家', contact: '王女士', phone: '137****9012', address: '广州市天河区xxx大道789号',
    license: 'GZ2025003', applyTime: '2025-01-13 09:15', status: 'approved',
    documents: ['营业执照', '卫生许可证', '从业资格证', '消防证明'] },
  { id: 'INS004', name: '宠爱有家', contact: '赵先生', phone: '136****3456', address: '深圳市南山区xxx路321号',
    license: 'SZ2025004', applyTime: '2025-01-12 14:00', status: 'rejected', rejectReason: '证件不齐全',
    documents: ['营业执照'] }
])

const filteredList = computed(() => {
  return reviewList.value.filter(item => {
    const matchSearch = !searchKeyword.value || item.name.includes(searchKeyword.value) || item.id.includes(searchKeyword.value)
    const matchStatus = statusFilter.value === 'all' || item.status === statusFilter.value
    return matchSearch && matchStatus
  })
})

const getStatusLabel = (status: string) => {
  const map: Record<string, string> = { pending: '待审核', approved: '已通过', rejected: '已拒绝' }
  return map[status] || status
}

const getStatusType = (status: string) => {
  const map: Record<string, string> = { pending: 'warning', approved: 'success', rejected: 'danger' }
  return map[status] || 'info'
}

const viewDetail = (item: typeof reviewList.value[0]) => {
  ElMessage.info(`查看机构详情: ${item.name}`)
}

const approveInstitution = async (item: typeof reviewList.value[0]) => {
  await ElMessageBox.confirm(`确认通过 "${item.name}" 的资质审核？`, '确认通过', { type: 'success' })
  item.status = 'approved'
  ElMessage.success('审核已通过')
}

const rejectInstitution = async (item: typeof reviewList.value[0]) => {
  const { value } = await ElMessageBox.prompt('请输入拒绝原因', '拒绝审核', { inputPattern: /.+/, inputErrorMessage: '请输入拒绝原因' })
  item.status = 'rejected'
  item.rejectReason = value
  ElMessage.success('已拒绝该申请')
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

    <!-- 统计卡片 -->
    <div class="stats-grid">
      <div class="stat-card pending" @click="statusFilter = 'pending'">
        <Clock :size="24" />
        <div class="stat-info">
          <span class="stat-value">{{ reviewStats.pending }}</span>
          <span class="stat-label">待审核</span>
        </div>
      </div>
      <div class="stat-card approved" @click="statusFilter = 'approved'">
        <Check :size="24" />
        <div class="stat-info">
          <span class="stat-value">{{ reviewStats.approved }}</span>
          <span class="stat-label">已通过</span>
        </div>
      </div>
      <div class="stat-card rejected" @click="statusFilter = 'rejected'">
        <X :size="24" />
        <div class="stat-info">
          <span class="stat-value">{{ reviewStats.rejected }}</span>
          <span class="stat-label">已拒绝</span>
        </div>
      </div>
      <div class="stat-card total" @click="statusFilter = 'all'">
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
        <button v-for="s in ['pending', 'approved', 'rejected', 'all']" :key="s" 
          :class="{ active: statusFilter === s }" @click="statusFilter = s">
          {{ { pending: '待审核', approved: '已通过', rejected: '已拒绝', all: '全部' }[s] }}
        </button>
      </div>
    </div>

    <!-- 审核列表 -->
    <div class="review-list">
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
            <div class="info-item"><span class="label">联系人:</span><span>{{ item.contact }}</span></div>
            <div class="info-item"><span class="label">电话:</span><span>{{ item.phone }}</span></div>
            <div class="info-item"><span class="label">执照号:</span><span>{{ item.license }}</span></div>
          </div>
          <div class="address"><span class="label">地址:</span>{{ item.address }}</div>
          <div class="documents">
            <span class="label">已提交资料:</span>
            <div class="doc-tags">
              <span v-for="doc in item.documents" :key="doc" class="doc-tag"><FileText :size="12" /> {{ doc }}</span>
            </div>
          </div>
          <div class="apply-time">申请时间: {{ item.applyTime }}</div>
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
  </div>
</template>

<style lang="scss" scoped>
.qualification-review { max-width: 1200px; margin: 0 auto; padding: 24px; }

.page-header {
  margin-bottom: 24px;
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
</style>
