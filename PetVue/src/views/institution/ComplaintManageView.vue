<script setup lang="ts">
import { ref, computed } from 'vue'
import { MessageSquare, Search, Eye, Send, Clock, CheckCircle, AlertTriangle } from 'lucide-vue-next'
import { ElMessage, ElMessageBox } from 'element-plus'

const searchKeyword = ref('')
const statusFilter = ref('all')

// 投诉统计
const complaintStats = ref({
  pending: 3,
  processing: 2,
  resolved: 45,
  total: 50
})

// 投诉列表
const complaintList = ref([
  { id: 'CP001', orderId: 'ORD20250115001', user: '张三', phone: '138****1234', type: '服务质量', 
    content: '宠物寄养期间照顾不周，毛发打结严重', status: 'pending', createTime: '2025-01-15 10:30', reply: '' },
  { id: 'CP002', orderId: 'ORD20250114002', user: '李四', phone: '139****5678', type: '环境卫生',
    content: '寄养环境卫生条件不达标，有异味', status: 'processing', createTime: '2025-01-14 15:20', reply: '正在核实情况' },
  { id: 'CP003', orderId: 'ORD20250113003', user: '王五', phone: '137****9012', type: '服务态度',
    content: '工作人员态度冷淡，沟通不及时', status: 'resolved', createTime: '2025-01-13 09:15', 
    reply: '已对相关员工进行培训，感谢您的反馈', resolveTime: '2025-01-13 16:00' },
  { id: 'CP004', orderId: 'ORD20250112004', user: '赵六', phone: '136****3456', type: '费用问题',
    content: '实际收费与预约时不符', status: 'resolved', createTime: '2025-01-12 14:00',
    reply: '已核实并退还差价，给您带来不便深表歉意', resolveTime: '2025-01-12 18:30' }
])

const filteredList = computed(() => {
  return complaintList.value.filter(item => {
    const matchSearch = !searchKeyword.value || item.orderId.includes(searchKeyword.value) || item.user.includes(searchKeyword.value)
    const matchStatus = statusFilter.value === 'all' || item.status === statusFilter.value
    return matchSearch && matchStatus
  })
})

const getStatusLabel = (status: string) => {
  const map: Record<string, string> = { pending: '待处理', processing: '处理中', resolved: '已解决' }
  return map[status] || status
}

const getStatusType = (status: string) => {
  const map: Record<string, string> = { pending: 'danger', processing: 'warning', resolved: 'success' }
  return map[status] || 'info'
}

const viewDetail = (item: typeof complaintList.value[0]) => {
  ElMessage.info(`查看投诉详情: ${item.id}`)
}

const replyComplaint = async (item: typeof complaintList.value[0]) => {
  const { value } = await ElMessageBox.prompt('请输入回复内容', '回复投诉', { 
    inputType: 'textarea', inputPattern: /.+/, inputErrorMessage: '请输入回复内容' 
  })
  item.reply = value
  item.status = 'resolved'
  ElMessage.success('回复已发送')
}
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
      <div class="stat-card pending" @click="statusFilter = 'pending'">
        <Clock :size="24" />
        <div class="stat-info">
          <span class="stat-value">{{ complaintStats.pending }}</span>
          <span class="stat-label">待处理</span>
        </div>
      </div>
      <div class="stat-card processing" @click="statusFilter = 'processing'">
        <AlertTriangle :size="24" />
        <div class="stat-info">
          <span class="stat-value">{{ complaintStats.processing }}</span>
          <span class="stat-label">处理中</span>
        </div>
      </div>
      <div class="stat-card resolved" @click="statusFilter = 'resolved'">
        <CheckCircle :size="24" />
        <div class="stat-info">
          <span class="stat-value">{{ complaintStats.resolved }}</span>
          <span class="stat-label">已解决</span>
        </div>
      </div>
      <div class="stat-card total" @click="statusFilter = 'all'">
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
        <input v-model="searchKeyword" placeholder="搜索订单号/用户名" />
      </div>
      <div class="status-tabs">
        <button v-for="s in ['all', 'pending', 'processing', 'resolved']" :key="s"
          :class="{ active: statusFilter === s }" @click="statusFilter = s">
          {{ { all: '全部', pending: '待处理', processing: '处理中', resolved: '已解决' }[s] }}
        </button>
      </div>
    </div>

    <!-- 投诉列表 -->
    <div class="complaint-list">
      <div v-for="item in filteredList" :key="item.id" class="complaint-card">
        <div class="card-header">
          <span class="complaint-id">{{ item.id }}</span>
          <el-tag :type="getStatusType(item.status)" size="small">{{ getStatusLabel(item.status) }}</el-tag>
        </div>
        <div class="card-body">
          <div class="info-row"><span class="label">订单号:</span><span>{{ item.orderId }}</span></div>
          <div class="info-row"><span class="label">投诉人:</span><span>{{ item.user }} ({{ item.phone }})</span></div>
          <div class="info-row"><span class="label">投诉类型:</span><span class="type-tag">{{ item.type }}</span></div>
          <div class="content-box">
            <span class="label">投诉内容:</span>
            <p>{{ item.content }}</p>
          </div>
          <div v-if="item.reply" class="reply-box">
            <span class="label">回复:</span>
            <p>{{ item.reply }}</p>
          </div>
          <div class="time-info">投诉时间: {{ item.createTime }}</div>
        </div>
        <div class="card-actions">
          <button class="btn-view" @click="viewDetail(item)"><Eye :size="14" /> 详情</button>
          <button v-if="item.status !== 'resolved'" class="btn-reply" @click="replyComplaint(item)">
            <Send :size="14" /> 回复
          </button>
        </div>
      </div>
    </div>
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
  &.processing { color: #faad14; }
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
    .content-box, .reply-box { margin: 12px 0; .label { font-size: 14px; color: #9A958F; } p { margin: 6px 0 0; font-size: 14px; color: #2D2A26; line-height: 1.6; } }
    .reply-box { background: #F8F8F7; padding: 12px; border-radius: 8px; }
    .time-info { font-size: 12px; color: #9A958F; }
  }
  .card-actions { display: flex; gap: 10px; margin-top: 16px; padding-top: 16px; border-top: 1px solid #F0EFED;
    button { display: flex; align-items: center; gap: 6px; padding: 8px 16px; border: none; border-radius: 8px; font-size: 13px; cursor: pointer;
      &.btn-view { background: #F8F8F7; color: #6B6560; }
      &.btn-reply { background: #722ed1; color: white; }
    }
  }
}
</style>
