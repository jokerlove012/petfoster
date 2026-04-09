<script setup lang="ts">
import { ref, computed, onMounted, watch } from 'vue'
import { Search, Filter, UserPlus, Edit, Lock, Unlock, Trash2, Eye, Download, X } from 'lucide-vue-next'
import { ElMessage, ElMessageBox } from 'element-plus'
import { adminApi } from '@/api/admin'

const loading = ref(false)
const searchQuery = ref('')
const filterRole = ref<string | null>(null)
const filterStatus = ref<string | null>(null)
const currentPage = ref(1)
const pageSize = ref(20)
const total = ref(0)

interface User {
  id: string
  name: string
  phone: string
  email: string
  role: string
  status: string
  createdAt: string
  updatedAt: string
  avatar?: string
}

// 用户列表
const users = ref<User[]>([])

const roleOptions = [
  { value: 'pet_owner', label: '宠物主人', color: '#1890ff' },
  { value: 'institution_staff', label: '机构用户', color: '#52c41a' },
  { value: 'admin', label: '管理员', color: '#722ed1' }
]

const statusOptions = [
  { value: 'active', label: '正常', color: '#52c41a' },
  { value: 'banned', label: '已禁用', color: '#ff4d4f' }
]

// 统计数据
const petOwnerCount = ref(0)
const institutionStaffCount = ref(0)
const totalUserCount = ref(0)

// 模态框状态
const viewModalVisible = ref(false)
const editModalVisible = ref(false)
const addModalVisible = ref(false)
const currentUser = ref<User | null>(null)
const formData = ref<any>({})

// 加载数据
const loadData = async () => {
  loading.value = true
  try {
    const res = await adminApi.getUsers(
      filterRole.value || undefined,
      searchQuery.value || undefined,
      currentPage.value,
      pageSize.value
    )
    console.log('用户API返回:', res)
    if (res.code === 200 && res.data) {
      users.value = res.data.list || []
      total.value = res.data.pagination?.total || 0
      
      // 单独加载所有用户来统计
      const allRes = await adminApi.getUsers(undefined, undefined, 1, 1000)
      if (allRes.code === 200 && allRes.data) {
        const allUsers = allRes.data.list || []
        totalUserCount.value = allUsers.length
        petOwnerCount.value = allUsers.filter(u => u.role === 'pet_owner').length
        institutionStaffCount.value = allUsers.filter(u => u.role === 'institution_staff').length
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

// 监听筛选变化
watch([filterRole, filterStatus, currentPage, pageSize], () => {
  loadData()
})

let searchTimeout: any
watch(searchQuery, () => {
  if (searchTimeout) clearTimeout(searchTimeout)
  searchTimeout = setTimeout(() => {
    currentPage.value = 1
    loadData()
  }, 500)
})

const filteredUsers = computed(() => users.value)

const totalUsers = computed(() => totalUserCount.value)
const activeUsers = computed(() => {
  return totalUserCount.value
})

const getRoleLabel = (role: string) => roleOptions.find(r => r.value === role)?.label || role
const getRoleColor = (role: string) => roleOptions.find(r => r.value === role)?.color || '#999'
const getStatusLabel = (status: string) => statusOptions.find(s => s.value === status)?.label || status
const getStatusColor = (status: string) => statusOptions.find(s => s.value === status)?.color || '#999'

const getAvatar = (role: string) => {
  if (role === 'institution_staff') return '🏠'
  if (role === 'admin') return '🛡️'
  return '👤'
}

const viewUser = async (user: User) => {
  try {
    const res = await adminApi.getUserDetail(user.id)
    if (res.code === 200) {
      currentUser.value = res.data
      viewModalVisible.value = true
    }
  } catch (error) {
    currentUser.value = user
    viewModalVisible.value = true
  }
}

const editUser = (user: User) => {
  currentUser.value = { ...user }
  formData.value = { ...user }
  editModalVisible.value = true
}

const toggleUserStatus = async (user: User) => {
  const action = user.status === 'active' ? '禁用' : '启用'
  const newStatus = user.status === 'active' ? 'banned' : 'active'
  try {
    await ElMessageBox.confirm(`确定要${action}用户 "${user.name}" 吗？`, `${action}用户`, {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    const res = await adminApi.toggleUserStatus(user.id, newStatus)
    if (res.code === 200) {
      ElMessage.success(`已${action}用户`)
      loadData()
    } else {
      ElMessage.error(res.message || '操作失败')
    }
  } catch (error: any) {
    if (error !== 'cancel') {
      console.error('操作失败:', error)
      ElMessage.error(error.message || '操作失败')
    }
  }
}

const deleteUser = async (user: User) => {
  try {
    await ElMessageBox.confirm(`确定要删除用户 "${user.name}" 吗？此操作不可恢复。`, '删除用户', {
      confirmButtonText: '删除',
      cancelButtonText: '取消',
      type: 'warning'
    })
    const res = await adminApi.deleteUser(user.id)
    if (res.code === 200) {
      ElMessage.success('用户已删除')
      loadData()
    } else {
      ElMessage.error(res.message || '删除失败')
    }
  } catch (error: any) {
    if (error !== 'cancel') {
      console.error('删除失败:', error)
      ElMessage.error(error.message || '删除失败')
    }
  }
}

const exportUsers = () => {
  ElMessage.success('正在导出用户数据...')
}

const addUser = () => {
  formData.value = {
    name: '',
    phone: '',
    email: '',
    role: 'pet_owner',
    status: 'active'
  }
  addModalVisible.value = true
}

const saveEdit = async () => {
  if (!currentUser.value) return
  try {
    const res = await adminApi.updateUser(currentUser.value.id, formData.value)
    if (res.code === 200) {
      ElMessage.success('用户信息已更新')
      editModalVisible.value = false
      loadData()
    } else {
      ElMessage.error(res.message || '更新失败')
    }
  } catch (error: any) {
    console.error('更新失败:', error)
    ElMessage.error(error.message || '更新失败')
  }
}

const saveAdd = async () => {
  try {
    const res = await adminApi.createUser(formData.value)
    if (res.code === 200) {
      ElMessage.success('用户创建成功')
      addModalVisible.value = false
      loadData()
    } else {
      ElMessage.error(res.message || '创建失败')
    }
  } catch (error: any) {
    console.error('创建失败:', error)
    ElMessage.error(error.message || '创建失败')
  }
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
        <button class="btn-add" @click="addUser">
          <UserPlus :size="16" /> 添加用户
        </button>
      </div>
    </div>

    <!-- 加载状态 -->
    <div v-if="loading" class="loading-state">加载中...</div>

    <template v-else>
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
          <span class="stat-value">{{ petOwnerCount }}</span>
          <span class="stat-label">宠物主人</span>
        </div>
        <div class="stat-card">
          <span class="stat-value">{{ institutionStaffCount }}</span>
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
        </div>
      </div>

      <!-- 用户表格 -->
      <div v-if="filteredUsers.length === 0" class="empty-state">
        <span class="empty-icon">👥</span>
        <p>暂无用户数据</p>
      </div>

      <div v-else class="table-card">
        <table class="data-table">
          <thead>
            <tr>
              <th>用户</th>
              <th>联系方式</th>
              <th>角色</th>
              <th>状态</th>
              <th>注册时间</th>
              <th>操作</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="user in filteredUsers" :key="user.id">
              <td class="user-cell">
                <span class="user-avatar">{{ getAvatar(user.role) }}</span>
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
              <td class="date-cell">{{ user.createdAt ? new Date(user.createdAt).toLocaleDateString('zh-CN') : '-' }}</td>
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
      <div v-if="total > 0" class="pagination">
        <span class="total">共 {{ total }} 条记录</span>
        <el-pagination
          v-model:current-page="currentPage"
          v-model:page-size="pageSize"
          :total="total"
          :page-sizes="[10, 20, 50, 100]"
          layout="sizes, prev, pager, next"
        />
      </div>
    </template>

    <!-- 查看用户详情模态框 -->
    <div v-if="viewModalVisible" class="modal-overlay" @click.self="viewModalVisible = false">
      <div class="modal">
        <div class="modal-header">
          <h3>用户详情</h3>
          <button class="modal-close" @click="viewModalVisible = false">
            <X :size="20" />
          </button>
        </div>
        <div class="modal-body" v-if="currentUser">
          <div class="detail-item">
            <span class="label">头像</span>
            <span class="avatar">{{ getAvatar(currentUser.role) }}</span>
          </div>
          <div class="detail-item">
            <span class="label">姓名</span>
            <span class="value">{{ currentUser.name }}</span>
          </div>
          <div class="detail-item">
            <span class="label">手机号</span>
            <span class="value">{{ currentUser.phone }}</span>
          </div>
          <div class="detail-item">
            <span class="label">邮箱</span>
            <span class="value">{{ currentUser.email }}</span>
          </div>
          <div class="detail-item">
            <span class="label">角色</span>
            <span class="role-badge" :style="{ background: getRoleColor(currentUser.role) + '20', color: getRoleColor(currentUser.role) }">
              {{ getRoleLabel(currentUser.role) }}
            </span>
          </div>
          <div class="detail-item">
            <span class="label">状态</span>
            <span class="status-badge" :style="{ background: getStatusColor(currentUser.status) + '20', color: getStatusColor(currentUser.status) }">
              {{ getStatusLabel(currentUser.status) }}
            </span>
          </div>
          <div class="detail-item">
            <span class="label">注册时间</span>
            <span class="value">{{ currentUser.createdAt ? new Date(currentUser.createdAt).toLocaleString('zh-CN') : '-' }}</span>
          </div>
          <div class="detail-item">
            <span class="label">更新时间</span>
            <span class="value">{{ currentUser.updatedAt ? new Date(currentUser.updatedAt).toLocaleString('zh-CN') : '-' }}</span>
          </div>
        </div>
        <div class="modal-footer">
          <button class="btn-cancel" @click="viewModalVisible = false">关闭</button>
        </div>
      </div>
    </div>

    <!-- 编辑用户模态框 -->
    <div v-if="editModalVisible" class="modal-overlay" @click.self="editModalVisible = false">
      <div class="modal">
        <div class="modal-header">
          <h3>编辑用户</h3>
          <button class="modal-close" @click="editModalVisible = false">
            <X :size="20" />
          </button>
        </div>
        <div class="modal-body">
          <div class="form-item">
            <label>姓名</label>
            <input v-model="formData.name" type="text" placeholder="请输入姓名" />
          </div>
          <div class="form-item">
            <label>手机号</label>
            <input v-model="formData.phone" type="text" placeholder="请输入手机号" />
          </div>
          <div class="form-item">
            <label>邮箱</label>
            <input v-model="formData.email" type="email" placeholder="请输入邮箱" />
          </div>
          <div class="form-item">
            <label>角色</label>
            <select v-model="formData.role">
              <option v-for="opt in roleOptions" :key="opt.value" :value="opt.value">{{ opt.label }}</option>
            </select>
          </div>
          <div class="form-item">
            <label>状态</label>
            <select v-model="formData.status">
              <option v-for="opt in statusOptions" :key="opt.value" :value="opt.value">{{ opt.label }}</option>
            </select>
          </div>
        </div>
        <div class="modal-footer">
          <button class="btn-cancel" @click="editModalVisible = false">取消</button>
          <button class="btn-confirm" @click="saveEdit">保存</button>
        </div>
      </div>
    </div>

    <!-- 添加用户模态框 -->
    <div v-if="addModalVisible" class="modal-overlay" @click.self="addModalVisible = false">
      <div class="modal">
        <div class="modal-header">
          <h3>添加用户</h3>
          <button class="modal-close" @click="addModalVisible = false">
            <X :size="20" />
          </button>
        </div>
        <div class="modal-body">
          <div class="form-item">
            <label>姓名</label>
            <input v-model="formData.name" type="text" placeholder="请输入姓名" />
          </div>
          <div class="form-item">
            <label>手机号</label>
            <input v-model="formData.phone" type="text" placeholder="请输入手机号" />
          </div>
          <div class="form-item">
            <label>邮箱</label>
            <input v-model="formData.email" type="email" placeholder="请输入邮箱" />
          </div>
          <div class="form-item">
            <label>角色</label>
            <select v-model="formData.role">
              <option v-for="opt in roleOptions" :key="opt.value" :value="opt.value">{{ opt.label }}</option>
            </select>
          </div>
        </div>
        <div class="modal-footer">
          <button class="btn-cancel" @click="addModalVisible = false">取消</button>
          <button class="btn-confirm" @click="saveAdd">添加</button>
        </div>
      </div>
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
}

.modal {
  background: white;
  border-radius: 16px;
  width: 90%;
  max-width: 500px;
  max-height: 90vh;
  overflow: hidden;
  display: flex;
  flex-direction: column;
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
  padding: 4px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #6B6560;
  &:hover {
    color: #2D2A26;
  }
}

.modal-body {
  padding: 24px;
  overflow-y: auto;
}

.modal-footer {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
  padding: 16px 24px;
  border-top: 1px solid #F0F0EF;
}

.btn-cancel, .btn-confirm {
  padding: 10px 24px;
  border: none;
  border-radius: 10px;
  font-size: 14px;
  cursor: pointer;
  transition: all 0.2s;
}

.btn-cancel {
  background: #F8F8F7;
  color: #6B6560;
  &:hover {
    background: #F0F0EF;
  }
}

.btn-confirm {
  background: #722ed1;
  color: white;
  &:hover {
    opacity: 0.9;
  }
}

.detail-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 0;
  border-bottom: 1px solid #F0F0EF;
  &:last-child {
    border-bottom: none;
  }
  .label {
    font-size: 14px;
    color: #6B6560;
  }
  .value {
    font-size: 14px;
    font-weight: 500;
    color: #2D2A26;
  }
  .avatar {
    font-size: 32px;
  }
}

.form-item {
  margin-bottom: 16px;
  &:last-child {
    margin-bottom: 0;
  }
  label {
    display: block;
    font-size: 14px;
    font-weight: 500;
    color: #2D2A26;
    margin-bottom: 8px;
  }
  input, select {
    width: 100%;
    padding: 10px 14px;
    border: 1px solid #E5E5E5;
    border-radius: 10px;
    font-size: 14px;
    outline: none;
    transition: all 0.2s;
    &:focus {
      border-color: #722ed1;
    }
  }
}
</style>
