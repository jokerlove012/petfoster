<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { User, Lock, Bell, Camera, Save, PawPrint, Plus, Edit, Trash2, X, Key } from 'lucide-vue-next'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useAuthStore } from '@/stores/auth'
import { authApi } from '@/api/auth'
import { uploadApi } from '@/api/upload'
import { walletApi } from '@/api/wallet'
import api from '@/api/index'

const authStore = useAuthStore()
const activeTab = ref('basic')
const loading = ref(false)
const avatarInput = ref<HTMLInputElement | null>(null)

// 用户信息
const userInfo = ref({
  avatar: '',
  nickname: '',
  realName: '',
  phone: '',
  email: '',
  gender: 'male',
  birthday: '',
  address: ''
})

// 密码修改
const passwordForm = ref({ oldPassword: '', newPassword: '', confirmPassword: '' })

// 支付密码修改
const payPasswordForm = ref({ oldPassword: '', newPassword: '', confirmPassword: '' })
const hasPayPassword = ref(false)
const showSetPayPassword = ref(false)

// 通知设置
const notificationSettings = ref({
  orderNotify: true, promotionNotify: false, systemNotify: true, smsNotify: true, emailNotify: false
})

// 宠物列表
const pets = ref<any[]>([])
const showPetDialog = ref(false)
const editingPet = ref<any>(null)
const petForm = ref({
  name: '', species: 'dog', breed: '', age: 1, weight: 5, gender: 'male',
  vaccinated: true, neutered: false, healthNotes: ''
})

// 加载钱包信息
const loadWalletInfo = async () => {
  try {
    const res = await walletApi.getWallet()
    if (res.code === 200 && res.data) {
      hasPayPassword.value = (res.data as any).hasWithdrawPassword || false
    }
  } catch (error) {
    console.error('加载钱包信息失败:', error)
  }
}

// 加载用户资料
const loadProfile = async () => {
  try {
    loading.value = true
    const res = await authApi.getProfile()
    if (res.data) {
      const user = res.data as any
      userInfo.value = {
        avatar: user.avatar || '',
        nickname: user.name || '',
        realName: user.name || '',
        phone: user.phone || '',
        email: user.email || '',
        gender: user.gender || 'male',
        birthday: user.birthday || '',
        address: user.address || ''
      }
      // 加载宠物列表
      if (user.pets) {
        pets.value = user.pets
      }
      // 更新 authStore
      if (authStore.user) {
        ;(authStore.user as any).pets = user.pets
        authStore.user = { ...authStore.user, ...user }
      }
    }
    await loadWalletInfo()
  } catch (error: any) {
    console.error('加载用户资料失败:', error)
  } finally {
    loading.value = false
  }
}

// 初始化
onMounted(() => {
  loadProfile()
})

const resetPetForm = () => {
  petForm.value = { name: '', species: 'dog', breed: '', age: 1, weight: 5, gender: 'male', vaccinated: true, neutered: false, healthNotes: '' }
  editingPet.value = null
}

const openAddPetDialog = () => {
  resetPetForm()
  showPetDialog.value = true
}

const openEditPetDialog = (pet: any) => {
  editingPet.value = pet
  petForm.value = { ...pet }
  showPetDialog.value = true
}

const savePet = async () => {
  if (!petForm.value.name || !petForm.value.breed) {
    ElMessage.warning('请填写宠物名称和品种')
    return
  }
  try {
    if (editingPet.value) {
      // 更新宠物
      await api.put(`/pets/${editingPet.value.id}`, petForm.value)
      const index = pets.value.findIndex(p => p.id === editingPet.value.id)
      if (index > -1) { pets.value[index] = { ...petForm.value, id: editingPet.value.id } }
      ElMessage.success('宠物信息已更新')
    } else {
      // 添加宠物
      const res = await api.post('/pets', petForm.value)
      if (res.data) {
        pets.value.push(res.data)
      }
      ElMessage.success('宠物添加成功')
    }
    // 同步到 authStore
    if (authStore.user) {
      ;(authStore.user as any).pets = [...pets.value]
    }
    showPetDialog.value = false
    resetPetForm()
  } catch (error: any) {
    ElMessage.error(error.message || '操作失败')
  }
}

const deletePet = async (pet: any) => {
  await ElMessageBox.confirm(`确定要删除 ${pet.name} 吗？`, '删除宠物', { type: 'warning' })
  try {
    await api.delete(`/pets/${pet.id}`)
    pets.value = pets.value.filter(p => p.id !== pet.id)
    // 同步到 authStore
    if (authStore.user) {
      ;(authStore.user as any).pets = [...pets.value]
    }
    ElMessage.success('宠物已删除')
  } catch (error: any) {
    ElMessage.error(error.message || '删除失败')
  }
}

const getPetIcon = (species: string) => species === 'dog' ? '🐕' : species === 'cat' ? '🐱' : '🐾'

const saveBasicInfo = async () => {
  try {
    loading.value = true
    const res = await authApi.updateProfile({
      name: userInfo.value.realName,
      nickname: userInfo.value.nickname,
      email: userInfo.value.email,
      avatar: userInfo.value.avatar,
      gender: userInfo.value.gender,
      birthday: userInfo.value.birthday,
      address: userInfo.value.address
    } as any)
    if (res.data) {
      // 更新 authStore
      if (authStore.user) {
        authStore.user = { ...authStore.user, ...res.data }
      }
      ElMessage.success('个人信息已保存')
    }
  } catch (error: any) {
    ElMessage.error(error.message || '保存失败')
  } finally {
    loading.value = false
  }
}
const changePassword = async () => {
  if (passwordForm.value.newPassword !== passwordForm.value.confirmPassword) { ElMessage.error('两次输入的密码不一致'); return }
  if (passwordForm.value.newPassword.length < 6) { ElMessage.error('密码长度不能少于6位'); return }
  try {
    await authApi.changePassword(passwordForm.value.oldPassword, passwordForm.value.newPassword)
    ElMessage.success('密码修改成功')
    passwordForm.value = { oldPassword: '', newPassword: '', confirmPassword: '' }
  } catch (error: any) {
    ElMessage.error(error.message || '密码修改失败')
  }
}

const setPayPassword = async () => {
  if (payPasswordForm.value.newPassword !== payPasswordForm.value.confirmPassword) { 
    ElMessage.error('两次输入的密码不一致')
    return 
  }
  if (payPasswordForm.value.newPassword.length < 6) { 
    ElMessage.error('密码长度不能少于6位')
    return 
  }
  try {
    loading.value = true
    await walletApi.setWithdrawPassword(payPasswordForm.value.newPassword)
    ElMessage.success(hasPayPassword.value ? '支付密码修改成功' : '支付密码设置成功')
    hasPayPassword.value = true
    payPasswordForm.value = { oldPassword: '', newPassword: '', confirmPassword: '' }
    showSetPayPassword.value = false
  } catch (error: any) {
    ElMessage.error(error.message || '操作失败')
  } finally {
    loading.value = false
  }
}

const saveNotificationSettings = () => { ElMessage.success('通知设置已保存') }

const uploadAvatar = () => {
  if (!avatarInput.value) {
    const input = document.createElement('input')
    input.type = 'file'
    input.accept = 'image/*'
    input.onchange = async (e) => {
      const file = (e.target as HTMLInputElement).files?.[0]
      if (file) {
        await handleAvatarUpload(file)
      }
    }
    avatarInput.value = input
  }
  avatarInput.value.click()
}

const handleAvatarUpload = async (file: File) => {
  try {
    loading.value = true
    const res = await uploadApi.upload(file)
    if (res.code === 200 && res.data) {
      userInfo.value.avatar = res.data.url
      // 头像上传后自动保存到后端
      await saveBasicInfo()
      ElMessage.success('头像上传成功')
    } else {
      ElMessage.error(res.message || '头像上传失败')
    }
  } catch (error: any) {
    ElMessage.error(error.message || '头像上传失败')
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <div class="profile-view">
    <div class="page-header">
      <h1>👤 个人资料</h1>
      <p>管理您的账户信息</p>
    </div>

    <div class="profile-layout">
      <div class="profile-nav">
        <button :class="{ active: activeTab === 'basic' }" @click="activeTab = 'basic'"><User :size="18" /> 基本信息</button>
        <button :class="{ active: activeTab === 'pets' }" @click="activeTab = 'pets'"><PawPrint :size="18" /> 我的宠物</button>
        <button :class="{ active: activeTab === 'security' }" @click="activeTab = 'security'"><Lock :size="18" /> 账户安全</button>
        <button :class="{ active: activeTab === 'notification' }" @click="activeTab = 'notification'"><Bell :size="18" /> 通知设置</button>
      </div>

      <div class="profile-content">
        <!-- 基本信息 -->
        <div v-if="activeTab === 'basic'" class="content-panel">
          <div class="avatar-section">
            <div class="avatar-wrapper" @click="uploadAvatar">
              <img v-if="userInfo.avatar" :src="userInfo.avatar" class="avatar-img" />
              <div v-else class="avatar">{{ userInfo.nickname ? userInfo.nickname.charAt(0) : 'U' }}</div>
              <div class="avatar-overlay"><Camera :size="20" /></div>
            </div>
            <span class="avatar-hint">点击更换头像</span>
          </div>
          <div class="form-section">
            <div class="form-group"><label>昵称</label><input v-model="userInfo.nickname" type="text" /></div>
            <div class="form-group"><label>真实姓名</label><input v-model="userInfo.realName" type="text" /></div>
            <div class="form-group"><label>手机号码</label>
              <div class="input-with-action"><input v-model="userInfo.phone" disabled /><button class="btn-change">更换</button></div>
            </div>
            <div class="form-group"><label>邮箱</label><input v-model="userInfo.email" type="email" /></div>
            <div class="form-group"><label>性别</label>
              <div class="radio-group">
                <label><input type="radio" v-model="userInfo.gender" value="male" /> 男</label>
                <label><input type="radio" v-model="userInfo.gender" value="female" /> 女</label>
              </div>
            </div>
            <div class="form-group"><label>生日</label><input v-model="userInfo.birthday" type="date" /></div>
            <div class="form-group"><label>地址</label><input v-model="userInfo.address" type="text" /></div>
            <button class="btn-save" @click="saveBasicInfo"><Save :size="16" /> 保存修改</button>
          </div>
        </div>

        <!-- 我的宠物 -->
        <div v-if="activeTab === 'pets'" class="content-panel">
          <div class="pets-header">
            <h3>🐾 我的宠物</h3>
            <button class="btn-add-pet" @click="openAddPetDialog"><Plus :size="16" /> 添加宠物</button>
          </div>
          <div v-if="pets.length === 0" class="empty-pets">
            <PawPrint :size="48" />
            <p>还没有添加宠物</p>
            <button class="btn-add-first" @click="openAddPetDialog">添加第一只宠物</button>
          </div>
          <div v-else class="pets-grid">
            <div v-for="pet in pets" :key="pet.id" class="pet-card">
              <div class="pet-avatar">{{ getPetIcon(pet.species) }}</div>
              <div class="pet-info">
                <span class="pet-name">{{ pet.name }}</span>
                <span class="pet-breed">{{ pet.breed }}</span>
                <div class="pet-details">
                  <span>{{ pet.age }}岁</span>
                  <span>{{ pet.weight }}kg</span>
                  <span>{{ pet.gender === 'male' ? '♂' : '♀' }}</span>
                </div>
                <div class="pet-tags">
                  <span v-if="pet.vaccinated" class="tag success">已免疫</span>
                  <span v-if="pet.neutered" class="tag info">已绝育</span>
                </div>
              </div>
              <div class="pet-actions">
                <button @click="openEditPetDialog(pet)"><Edit :size="14" /></button>
                <button class="danger" @click="deletePet(pet)"><Trash2 :size="14" /></button>
              </div>
            </div>
          </div>
        </div>

        <!-- 账户安全 -->
        <div v-if="activeTab === 'security'" class="content-panel">
          <h3>修改登录密码</h3>
          <div class="form-section" style="margin-bottom: 40px;">
            <div class="form-group"><label>当前密码</label><input v-model="passwordForm.oldPassword" type="password" /></div>
            <div class="form-group"><label>新密码</label><input v-model="passwordForm.newPassword" type="password" /></div>
            <div class="form-group"><label>确认新密码</label><input v-model="passwordForm.confirmPassword" type="password" /></div>
            <button class="btn-save" @click="changePassword"><Lock :size="16" /> 修改密码</button>
          </div>

          <h3>支付密码</h3>
          <div class="form-section">
            <p class="pay-password-hint">
              {{ hasPayPassword ? '您已设置支付密码，用于提现等敏感操作验证' : '您还未设置支付密码，设置后可用于提现等敏感操作' }}
            </p>
            <button class="btn-save" @click="showSetPayPassword = true">
              <Key :size="16" /> {{ hasPayPassword ? '修改支付密码' : '设置支付密码' }}
            </button>
          </div>
        </div>

        <!-- 通知设置 -->
        <div v-if="activeTab === 'notification'" class="content-panel">
          <h3>通知偏好</h3>
          <div class="setting-list">
            <div class="setting-item"><div class="setting-info"><span class="setting-title">订单通知</span><span class="setting-desc">接收订单状态变更通知</span></div><el-switch v-model="notificationSettings.orderNotify" /></div>
            <div class="setting-item"><div class="setting-info"><span class="setting-title">促销通知</span><span class="setting-desc">接收优惠活动和促销信息</span></div><el-switch v-model="notificationSettings.promotionNotify" /></div>
            <div class="setting-item"><div class="setting-info"><span class="setting-title">系统通知</span><span class="setting-desc">接收系统公告和更新通知</span></div><el-switch v-model="notificationSettings.systemNotify" /></div>
            <div class="setting-item"><div class="setting-info"><span class="setting-title">短信通知</span><span class="setting-desc">通过短信接收重要通知</span></div><el-switch v-model="notificationSettings.smsNotify" /></div>
            <div class="setting-item"><div class="setting-info"><span class="setting-title">邮件通知</span><span class="setting-desc">通过邮件接收通知</span></div><el-switch v-model="notificationSettings.emailNotify" /></div>
          </div>
          <button class="btn-save" @click="saveNotificationSettings"><Save :size="16" /> 保存设置</button>
        </div>
      </div>
    </div>

    <el-dialog v-model="showSetPayPassword" :title="hasPayPassword ? '重置支付密码' : '设置支付密码'" width="90%" :max-width="450">
      <div class="pay-password-form">
        <div class="form-group">
          <label>新支付密码</label>
          <el-input v-model="payPasswordForm.newPassword" type="password" show-password placeholder="请输入新支付密码（至少6位）" />
        </div>
        <div class="form-group">
          <label>确认新支付密码</label>
          <el-input v-model="payPasswordForm.confirmPassword" type="password" show-password placeholder="请再次输入新支付密码" />
        </div>
      </div>
      <template #footer>
        <el-button @click="showSetPayPassword = false">取消</el-button>
        <el-button type="primary" :loading="loading" @click="setPayPassword">确认</el-button>
      </template>
    </el-dialog>

    <!-- 添加/编辑宠物弹窗 -->
    <div v-if="showPetDialog" class="dialog-overlay" @click.self="showPetDialog = false">
      <div class="dialog-content">
        <div class="dialog-header">
          <h3>{{ editingPet ? '编辑宠物' : '添加宠物' }}</h3>
          <button class="btn-close" @click="showPetDialog = false"><X :size="20" /></button>
        </div>
        <div class="dialog-body">
          <div class="form-row">
            <div class="form-group"><label>宠物名称 *</label><input v-model="petForm.name" placeholder="请输入宠物名称" /></div>
            <div class="form-group"><label>宠物类型</label>
              <select v-model="petForm.species">
                <option value="dog">狗狗</option><option value="cat">猫咪</option><option value="other">其他</option>
              </select>
            </div>
          </div>
          <div class="form-row">
            <div class="form-group"><label>品种 *</label><input v-model="petForm.breed" placeholder="如：金毛、布偶猫" /></div>
            <div class="form-group"><label>性别</label>
              <select v-model="petForm.gender"><option value="male">公</option><option value="female">母</option></select>
            </div>
          </div>
          <div class="form-row">
            <div class="form-group"><label>年龄(岁)</label><el-input-number v-model="petForm.age" :min="0" :max="30" /></div>
            <div class="form-group"><label>体重(kg)</label><el-input-number v-model="petForm.weight" :min="0" :max="100" :precision="1" /></div>
          </div>
          <div class="form-row checkboxes">
            <label class="checkbox-label"><input type="checkbox" v-model="petForm.vaccinated" /> 已完成免疫</label>
            <label class="checkbox-label"><input type="checkbox" v-model="petForm.neutered" /> 已绝育</label>
          </div>
          <div class="form-group"><label>健康备注</label><textarea v-model="petForm.healthNotes" placeholder="如有特殊健康情况请备注" rows="3"></textarea></div>
        </div>
        <div class="dialog-footer">
          <button class="btn-cancel" @click="showPetDialog = false">取消</button>
          <button class="btn-confirm" @click="savePet">{{ editingPet ? '保存修改' : '添加宠物' }}</button>
        </div>
      </div>
    </div>
  </div>
</template>

<style lang="scss" scoped>
.profile-view { max-width: 1000px; margin: 0 auto; padding: 24px; }
.page-header { margin-bottom: 24px; h1 { font-size: 26px; font-weight: 700; margin: 0 0 6px; } p { color: #6B6560; margin: 0; } }

.profile-layout { display: grid; grid-template-columns: 200px 1fr; gap: 24px;
  @media (max-width: 768px) { grid-template-columns: 1fr; }
}

.profile-nav { display: flex; flex-direction: column; gap: 8px;
  @media (max-width: 768px) { flex-direction: row; flex-wrap: wrap; }
  button { display: flex; align-items: center; gap: 10px; padding: 12px 16px; background: white; border: none; border-radius: 10px; font-size: 14px; color: #6B6560; cursor: pointer; text-align: left;
    &.active { background: #722ed1; color: white; }
    &:hover:not(.active) { background: #F8F8F7; }
  }
}

.profile-content { background: white; border-radius: 16px; padding: 32px; }
.content-panel { h3 { font-size: 16px; font-weight: 600; margin: 0 0 20px; } }

.avatar-section { display: flex; flex-direction: column; align-items: center; margin-bottom: 32px;
  .avatar-wrapper { position: relative; cursor: pointer;
    .avatar { width: 100px; height: 100px; border-radius: 50%; background: linear-gradient(135deg, #722ed1, #9254de); display: flex; align-items: center; justify-content: center; font-size: 36px; color: white; font-weight: 600; }
    .avatar-img { width: 100px; height: 100px; border-radius: 50%; object-fit: cover; }
    .avatar-overlay { position: absolute; inset: 0; background: rgba(0,0,0,0.5); border-radius: 50%; display: flex; align-items: center; justify-content: center; color: white; opacity: 0; transition: opacity 0.2s; }
    &:hover .avatar-overlay { opacity: 1; }
  }
  .avatar-hint { font-size: 12px; color: #9A958F; margin-top: 8px; }
}

.pay-password-hint {
  color: #9A958F;
  font-size: 13px;
  margin-bottom: 16px;
}

.pay-password-form {
  .form-group {
    margin-bottom: 20px;
    label {
      display: block;
      font-size: 14px;
      color: #6B6560;
      margin-bottom: 8px;
    }
  }
}

.form-section { max-width: 400px; }
.form-group { margin-bottom: 20px;
  label { display: block; font-size: 14px; color: #6B6560; margin-bottom: 8px; }
  input, select, textarea { width: 100%; padding: 12px 16px; border: 1px solid #E8E6E3; border-radius: 10px; font-size: 14px;
    &:focus { border-color: #722ed1; outline: none; }
    &:disabled { background: #F8F8F7; }
  }
  .input-with-action { display: flex; gap: 8px; input { flex: 1; } .btn-change { padding: 0 16px; background: #F8F8F7; border: none; border-radius: 8px; font-size: 13px; color: #722ed1; cursor: pointer; } }
  .radio-group { display: flex; gap: 24px; label { display: flex; align-items: center; gap: 6px; font-size: 14px; cursor: pointer; } }
}

.btn-save { display: flex; align-items: center; gap: 8px; padding: 12px 24px; background: #722ed1; color: white; border: none; border-radius: 10px; font-size: 14px; cursor: pointer; &:hover { background: #531dab; } }

.setting-list { margin-bottom: 24px; }
.setting-item { display: flex; justify-content: space-between; align-items: center; padding: 16px 0; border-bottom: 1px solid #F0EFED;
  .setting-info { .setting-title { display: block; font-size: 14px; font-weight: 500; margin-bottom: 4px; } .setting-desc { font-size: 12px; color: #9A958F; } }
}

// 宠物管理样式
.pets-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 24px;
  h3 { margin: 0; }
  .btn-add-pet { display: flex; align-items: center; gap: 6px; padding: 10px 18px; background: #722ed1; color: white; border: none; border-radius: 10px; font-size: 14px; cursor: pointer; }
}

.empty-pets { text-align: center; padding: 60px 20px; color: #9A958F;
  p { margin: 16px 0; }
  .btn-add-first { padding: 12px 24px; background: #722ed1; color: white; border: none; border-radius: 10px; font-size: 14px; cursor: pointer; }
}

.pets-grid { display: grid; grid-template-columns: repeat(auto-fill, minmax(280px, 1fr)); gap: 16px; }

.pet-card { display: flex; gap: 16px; padding: 20px; background: #F8F8F7; border-radius: 14px; position: relative;
  .pet-avatar { font-size: 48px; }
  .pet-info { flex: 1;
    .pet-name { display: block; font-size: 18px; font-weight: 600; margin-bottom: 4px; }
    .pet-breed { font-size: 14px; color: #6B6560; }
    .pet-details { display: flex; gap: 12px; margin-top: 8px; font-size: 13px; color: #9A958F; }
    .pet-tags { display: flex; gap: 8px; margin-top: 10px;
      .tag { padding: 2px 8px; border-radius: 4px; font-size: 11px;
        &.success { background: #E8F8E8; color: #52c41a; }
        &.info { background: #E6F4FF; color: #1890ff; }
      }
    }
  }
  .pet-actions { position: absolute; top: 12px; right: 12px; display: flex; gap: 6px;
    button { width: 28px; height: 28px; border: none; background: white; border-radius: 6px; cursor: pointer; display: flex; align-items: center; justify-content: center; color: #6B6560;
      &:hover { background: #E8E6E3; }
      &.danger:hover { background: #FEE2E2; color: #ff4d4f; }
    }
  }
}

// 弹窗样式
.dialog-overlay { position: fixed; inset: 0; background: rgba(0,0,0,0.5); display: flex; align-items: center; justify-content: center; z-index: 1000; padding: 20px; }
.dialog-content { background: white; border-radius: 16px; width: 100%; max-width: 500px; max-height: 90vh; overflow-y: auto; }
.dialog-header { display: flex; justify-content: space-between; align-items: center; padding: 20px 24px; border-bottom: 1px solid #F0EFED;
  h3 { margin: 0; font-size: 18px; }
  .btn-close { background: none; border: none; cursor: pointer; color: #9A958F; padding: 4px; }
}
.dialog-body { padding: 24px;
  .form-row { display: grid; grid-template-columns: 1fr 1fr; gap: 16px; margin-bottom: 16px;
    &.checkboxes { display: flex; gap: 24px; }
  }
  .form-group { margin-bottom: 0; }
  .checkbox-label { display: flex; align-items: center; gap: 8px; font-size: 14px; cursor: pointer; }
}
.dialog-footer { display: flex; justify-content: flex-end; gap: 12px; padding: 16px 24px; border-top: 1px solid #F0EFED;
  button { padding: 10px 20px; border-radius: 8px; font-size: 14px; cursor: pointer; }
  .btn-cancel { background: #F8F8F7; border: none; color: #6B6560; }
  .btn-confirm { background: #722ed1; border: none; color: white; }
}
</style>
