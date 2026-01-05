<script setup lang="ts">
import { ref, computed } from 'vue'
import { Search, Filter, UserPlus, Edit, Lock, Unlock, Trash2, Eye, Download } from 'lucide-vue-next'
import { ElMessage, ElMessageBox } from 'element-plus'

const loading = ref(false)
const searchQuery = ref('')
const filterRole = ref<string | null>(null)
const filterStatus = ref<string | null>(null)
const currentPage = ref(1)
const pageSize = ref(20)

// 用户列表
const users = ref([
  { id: '1', name: '张三', phone: '138****1234', email: 'zhangsan@example.com', role: 'pet_owner', status: 'active', registerDate: '2025-01-10', lastLogin: '2025-01-15 14:30', orders: 5, avatar: '👤' },
  { id: '2', name: '李四', phone: '139****5678', email: 'lisi@example.com', role: 'pet_owner', status: 'active', registerDate: '2025-01-08', lastLogin: '2025-01-15 10:20', orders: 3, avatar: '👤' },
  { id: '3', name: '王经理', phone: '137****9012', email: 'wang@aichong.com', role: 'institution', status: 'active', registerDate: '2024-12-20', lastLogin: '2025-01-15 09:00', orders: 0, avatar: '🏠' },
  { id: '4', name: '赵六', phone: '136****3456', email: 'zhaoliu@example.com', role: 'pet_owner', status: 'banned', registerDate: '2024-11-15', lastLogin: '2024-12-01 16:45', orders: 1, avatar: '👤' },
  { id: '5', name: '管理员A', phone: '135****7890', email: 'admin@petfoster.com', role: 'admin', status: 'active', registerDate: '2024-01-01', lastLogin: '2025-01-15 08:00', orders: 0, avatar: '🛡️' }
])

const roleOptions = [
  { value: 'pet_owner', label: '宠物主人', color: '#1890ff' },
  { value: 'institution', label: '机构用户', color: '#52c41a' },
  { value: 'admin', label: '管理员', color: '#722ed1' }
]

const statusOptions = [
  { value: 'active', label: '正常', color: '#52c41a' },
  { value: 'banned', label: '已禁用', color: '#ff4d4f' },
  { value: 'pending', label: '待审核', color: '#faad14' }
]

const filteredUsers = computed(() => {
  let result = users.value
  
  if (filterRole.value) {
    result = result.filter(u => u.role === filterRole.value)
  }
  
  if (filterStatus.value) {
    result = result.filter(u => u.status === filterStatus.value)
  }
  
  if (searchQuery.value) {
    const query = searchQuery.value.toLowerCase()
    result = result.filter(u =>
      u.name.toLowerCase().includes(query) ||
      u.phone.includes(query) ||
      u.email.toLowerCase().includes(query)
    )
  }
  
  return result
})

const totalUsers = computed(() => users.value.length)
const activeUsers = computed(() => users.value.filter(u => u.status === 'active').length)

const getRoleLabel = (role: string) => roleOptions.find(r => r.value === role)?.label || role
const getRoleColor = (role: string) => roleOptions.find(r => r.value === role)?.color || '#999'
const getStatusLabel = (status: string) => statusOptions.find(s => s.value === status)?.label || status
const getStatusColor = (status: string) => statusOptions.find(s => s.value === status)?.color || '#999'

const viewUser = (user: typeof users.value[0]) => {
  ElMessage.info(`查看用户: ${user.name}`)
}

const editUser = (user: typeof users.value[0]) => {
  ElMessage.info(`编辑用户: ${user.name}`)
}

const toggleUserStatus = async (user: typeof users.value[0]) => {
  const action = user.status === 'active' ? '禁用' : '启用'
  try {
    await ElMessageBox.confirm(`确定要${action}用户 "${user.name}" 吗？`, `${action}用户`, {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    user.status = user.status === 'active' ? 'banned' : 'active'
    ElMessage.success(`已${action}用户`)
  } catch {
    // 取消
  }
}

const deleteUser = async (user: typeof users.value[0]) => {
  try {
    await ElMessageBox.confirm(`确定要删除用户 "${user.name}" 吗？此操作不可恢复。`, '删除用户', {
      confirmButtonText: '删除',
      cancelButtonText: '取消',
      type: 'warning'
    })
    const index = users.value.findIndex(u => u.id === user.id)
    if (index > -1) {
      users.value.splice(index, 1)
    }
    ElMessage.success('用户已删除')
  } catch {
    // 取消
  }
}

const exportUsers = () => {
  ElMessage.success('正在导出用户数据...')
}
</script>

<template>
  <div class="user-manage-view">
    <div class="page-header">
      <div class="header-left">
        <h1>👥 用户管理</h1>
        <p>管理平台所有用户</p>
      </div>
      <div class="header-actions">
        <button class="btn-export" @click="exportUsers">
          <Download :size="16" /> 导出
        </button>
        <button class="btn-add">
          <UserPlus :size="16" /> 添加用户
        </button>
      </div>
    </div>

    <!-- 统计卡片 -->
    <div class="stats-row">
      <div class="stat-card">
        <span class="stat-value">{{ totalUsers }}</span>
        <span class="stat-label">总用户数</span>
      </div>
      <div class="stat-card">
        <span class="stat-value">{{ activeUsers }}</span>
        <span class="stat-label">活跃用户</span>
      </div>
      <div class="stat-card">
        <span class="stat-value">{{ users.filter(u => u.role === 'pet_owner').length }}</span>
        <span class="stat-label">宠物主人</span>
      </div>
      <div class="stat-card">
        <span class="stat-value">{{ users.filter(u => u.role === 'institution').length }}</span>
        <span class="stat-label">机构用户</span>
      </div>
    </div>

    <!-- 筛选栏 -->
    <div class="filter-bar">
      <div class="search-box">
        <Search :size="16" />
        <input v-model="searchQuery" placeholder="搜索用户名、手机号、邮箱..." />
      </div>
      <div class="filters">
        <select v-model="filterRole">
          <option :value="null">全部角色</option>
          <option v-for="opt in roleOptions" :key="opt.value" :value="opt.value">{{ opt.label }}</option>
        </select>
        <select v-model="filterStatus">
          <option :value="null">全部状态</option>
          <option v-for="opt in statusOptions" :key="opt.value" :value="opt.value">{{ opt.label }}</option>
        </select>
      </div>
    </div>

    <!-- 用户表格 -->
    <div class="table-card">
      <table class="data-table">
        <thead>
          <tr>
            <th>用户</th>
            <th>联系方式</th>
            <th>角色</th>
            <th>状态</th>
            <th>注册时间</th>
            <th>最后登录</th>
            <th>订单数</th>
            <th>操作</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="user in filteredUsers" :key="user.id">
            <td class="user-cell">
              <span class="user-avatar">{{ user.avatar }}</span>
              <span class="user-name">{{ user.name }}</span>
            </td>
            <td class="contact-cell">
              <span class="phone">{{ user.phone }}</span>
              <span class="email">{{ user.email }}</span>
            </td>
            <td>
              <span class="role-badge" :style="{ background: getRoleColor(user.role) + '20', color: getRoleColor(user.role) }">
                {{ getRoleLabel(user.role) }}
              </span>
            </td>
            <td>
              <span class="status-badge" :style="{ background: getStatusColor(user.status) + '20', color: getStatusColor(user.status) }">
                {{ getStatusLabel(user.status) }}
              </span>
            </td>
            <td class="date-cell">{{ user.registerDate }}</td>
            <td class="date-cell">{{ user.lastLogin }}</td>
            <td class="orders-cell">{{ user.orders }}</td>
            <td class="action-cell">
              <button class="action-btn view" @click="viewUser(user)" title="查看">
                <Eye :size="16" />
              </button>
              <button class="action-btn edit" @click="editUser(user)" title="编辑">
                <Edit :size="16" />
              </button>
              <button class="action-btn" :class="user.status === 'active' ? 'ban' : 'unban'" @click="toggleUserStatus(user)" :title="user.status === 'active' ? '禁用' : '启用'">
                <Lock v-if="user.status === 'active'" :size="16" />
                <Unlock v-else :size="16" />
              </button>
              <button class="action-btn delete" @click="deleteUser(user)" title="删除">
                <Trash2 :size="16" />
              </button>
            </td>
          </tr>
        </tbody>
      </table>
    </div>

    <!-- 分页 -->
    <div class="pagination">
      <span class="total">共 {{ filteredUsers.length }} 条记录</span>
      <el-pagination
        v-model:current-page="currentPage"
        v-model:page-size="pageSize"
        :total="filteredUsers.length"
        :page-sizes="[10, 20, 50, 100]"
        layout="sizes, prev, pager, next"
      />
    </div>
  </div>
</template>

<style lang="scss" scoped>
.user-manage-view { max-width: 1400px; margin: 0 auto; padding: 24px; }

.page-header {
  display: flex; justify-content: space-between; align-items: flex-start; margin-bottom: 24px;
  .header-left {
    h1 { font-size: 26px; font-weight: 700; margin: 0 0 6px; }
    p { color: #6B6560; margin: 0; }
  }
  .header-actions { display: flex; gap: 12px; }
}

.btn-export, .btn-add {
  display: flex; align-items: center; gap: 8px; padding: 10px 20px;
  border: none; border-radius: 10px; font-size: 14px; cursor: pointer;
}
.btn-export { background: white; color: #6B6560; border: 1px solid #E5E5E5; }
.btn-add { background: #722ed1; color: white; }

.stats-row {
  display: grid; grid-template-columns: repeat(4, 1fr); gap: 16px; margin-bottom: 24px;
  .stat-card {
    background: white; padding: 20px; border-radius: 12px; text-align: center;
    .stat-value { display: block; font-size: 28px; font-weight: 700; color: #2D2A26; }
    .stat-label { font-size: 13px; color: #6B6560; }
  }
}

.filter-bar {
  display: flex; justify-content: space-between; align-items: center; margin-bottom: 20px; gap: 16px; flex-wrap: wrap;
  .search-box {
    display: flex; align-items: center; gap: 10px; padding: 10px 16px;
    background: white; border-radius: 10px; border: 1px solid #E5E5E5; flex: 1; max-width: 400px;
    input { border: none; outline: none; font-size: 14px; width: 100%; }
  }
  .filters { display: flex; gap: 12px;
    select { padding: 10px 16px; border: 1px solid #E5E5E5; border-radius: 10px; font-size: 14px; background: white; }
  }
}

.table-card { background: white; border-radius: 16px; overflow: hidden; }

.data-table {
  width: 100%; border-collapse: collapse;
  th, td { padding: 14px 16px; text-align: left; border-bottom: 1px solid #F0F0EF; }
  th { background: #F8F8F7; font-size: 13px; font-weight: 600; color: #6B6560; }
  tbody tr:hover { background: #FAFAF9; }
}

.user-cell {
  display: flex; align-items: center; gap: 10px;
  .user-avatar { font-size: 24px; }
  .user-name { font-weight: 600; }
}

.contact-cell {
  .phone { display: block; font-size: 14px; }
  .email { display: block; font-size: 12px; color: #6B6560; }
}

.role-badge, .status-badge {
  display: inline-block; padding: 4px 12px; border-radius: 12px; font-size: 12px; font-weight: 500;
}

.date-cell { font-size: 13px; color: #6B6560; }
.orders-cell { font-weight: 600; }

.action-cell { display: flex; gap: 8px; }
.action-btn {
  width: 32px; height: 32px; border: none; border-radius: 8px; cursor: pointer;
  display: flex; align-items: center; justify-content: center; transition: all 0.2s;
  &.view { background: #E8F4FD; color: #1890ff; }
  &.edit { background: #FFF7ED; color: #faad14; }
  &.ban { background: #FEE2E2; color: #ff4d4f; }
  &.unban { background: #DCFCE7; color: #52c41a; }
  &.delete { background: #FEE2E2; color: #ff4d4f; }
  &:hover { opacity: 0.8; }
}

.pagination {
  display: flex; justify-content: space-between; align-items: center; margin-top: 20px; padding: 16px;
  background: white; border-radius: 12px;
  .total { font-size: 13px; color: #6B6560; }
}
</style>
