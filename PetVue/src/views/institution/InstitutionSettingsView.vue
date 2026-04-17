<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { Save, Upload, MapPin, Phone, Clock, Camera, Shield, Bell, CreditCard, CheckCircle, XCircle, AlertCircle } from 'lucide-vue-next'
import { ElMessage } from 'element-plus'
import { institutionManageApi } from '@/api/institution'
import { uploadApi } from '@/api/upload'
import { geocode } from '@/utils/mapService'

const logoInputRef = ref<HTMLInputElement | null>(null)
const photosInputRef = ref<HTMLInputElement | null>(null)
const certificateInputRef = ref<HTMLInputElement | null>(null)
const licenseInputRef = ref<HTMLInputElement | null>(null)
const institutionPhotos = ref<string[]>([])
const certificateFile = ref<string>('')
const licenseFile = ref<string>('')

const loading = ref(false)
const activeTab = ref<'basic' | 'service' | 'notification' | 'payment'>('basic')

// 机构状态
const hasInstitution = ref(false)
const institutionStatus = ref<'pending' | 'active' | 'rejected' | null>(null)

// 基本信息
const basicInfo = ref({
  name: '',
  logo: '',
  description: '',
  address: '',
  phone: '',
  mobile: '',
  email: '',
  businessHours: {
    weekday: { start: '08:00', end: '20:00' },
    weekend: { start: '09:00', end: '18:00' }
  },
  location: { lat: 0, lng: 0 }
})

// 服务设置
const serviceSettings = ref({
  acceptedPets: ['dog', 'cat'],
  maxPetWeight: 50,
  maxCapacity: 30,
  minBookingDays: 1,
  maxBookingDays: 365,
  advanceBookingDays: 365,
  cancellationPolicy: '48hours',
  checkInTime: '14:00',
  checkOutTime: '12:00',
  emergencyContact: '',
  veterinaryPartner: ''
})

// 通知设置
const notificationSettings = ref({
  newOrder: true,
  orderConfirm: true,
  orderCancel: true,
  newReview: true,
  complaint: true,
  systemNotice: true,
  emailNotification: true,
  smsNotification: false,
  pushNotification: true
})

// 支付设置
const paymentSettings = ref({
  bankName: '',
  accountName: '',
  accountNumber: '',
  autoWithdraw: false,
  withdrawThreshold: 1000
})

// 状态显示配置
const statusConfig = computed(() => {
  switch (institutionStatus.value) {
    case 'pending':
      return { icon: AlertCircle, text: '审核中', color: '#faad14', bg: '#fffbe6', desc: '您的机构资料正在审核中，请耐心等待管理员审核' }
    case 'active':
      return { icon: CheckCircle, text: '已通过', color: '#52c41a', bg: '#f6ffed', desc: '您的机构已通过审核，可以正常运营' }
    case 'rejected':
      return { icon: XCircle, text: '已拒绝', color: '#ff4d4f', bg: '#fff2f0', desc: '您的机构申请被拒绝，请修改资料后重新提交' }
    default:
      return null
  }
})

// 加载机构资料
const loadProfile = async () => {
  loading.value = true
  try {
    const res = await institutionManageApi.getProfile()
    if (res.code === 200 && res.data) {
      hasInstitution.value = res.data.hasInstitution
      institutionStatus.value = res.data.status || null
      
      if (res.data.institution) {
        const inst = res.data.institution
        basicInfo.value.name = inst.name || ''
        basicInfo.value.logo = inst.logo || ''
        basicInfo.value.description = inst.description || ''
        basicInfo.value.address = inst.address || ''
        basicInfo.value.phone = inst.phone || ''
        basicInfo.value.email = inst.email || ''
        if (inst.images && Array.isArray(inst.images)) {
          institutionPhotos.value = inst.images
        }
      }
    }
  } catch (error) {
    console.error('加载机构资料失败:', error)
  } finally {
    loading.value = false
  }
}

// 加载机构设置（已有机构时）
const loadSettings = async () => {
  if (!hasInstitution.value || institutionStatus.value !== 'active') return
  
  try {
    const res = await institutionManageApi.getSettings()
    if (res.code === 200 && res.data) {
      const data = res.data
      if (data.basicInfo) basicInfo.value = { ...basicInfo.value, ...data.basicInfo }
      if (data.images && Array.isArray(data.images)) {
        institutionPhotos.value = data.images
      }
      if (data.serviceSettings) serviceSettings.value = { ...serviceSettings.value, ...data.serviceSettings }
      if (data.notificationSettings) notificationSettings.value = { ...notificationSettings.value, ...data.notificationSettings }
      if (data.paymentSettings) paymentSettings.value = { ...paymentSettings.value, ...data.paymentSettings }
    }
  } catch (error) {
    console.error('加载设置失败:', error)
  }
}

const petTypeOptions = [
  { value: 'dog', label: '🐕 狗狗' },
  { value: 'cat', label: '🐱 猫咪' },
  { value: 'bird', label: '🐦 鸟类' },
  { value: 'rabbit', label: '🐰 兔子' },
  { value: 'hamster', label: '🐹 仓鼠' },
  { value: 'other', label: '🐾 其他' }
]

const cancellationOptions = [
  { value: 'flexible', label: '灵活 - 入住前24小时可免费取消' },
  { value: '48hours', label: '标准 - 入住前48小时可免费取消' },
  { value: 'strict', label: '严格 - 入住前7天可免费取消' }
]

// 提交申请/保存设置
const saveSettings = async () => {
  // 验证必填字段
  if (!basicInfo.value.name) {
    ElMessage.warning('请填写机构名称')
    return
  }
  if (!basicInfo.value.address) {
    ElMessage.warning('请填写机构地址')
    return
  }
  if (!basicInfo.value.phone) {
    ElMessage.warning('请填写联系电话')
    return
  }

  loading.value = true
  try {
    if (!hasInstitution.value || institutionStatus.value === 'rejected') {
      // 申请创建机构
      const licenses = []
      if (certificateFile.value) licenses.push(certificateFile.value)
      if (licenseFile.value) licenses.push(licenseFile.value)
      
      await institutionManageApi.applyInstitution({
        name: basicInfo.value.name,
        description: basicInfo.value.description,
        logo: basicInfo.value.logo,
        images: institutionPhotos.value,
        address: basicInfo.value.address,
        phone: basicInfo.value.phone,
        email: basicInfo.value.email,
        businessHours: basicInfo.value.businessHours,
        petTypes: serviceSettings.value.acceptedPets,
        licenses: licenses.length > 0 ? licenses : undefined
      })
      ElMessage.success('机构申请已提交，请等待管理员审核')
      hasInstitution.value = true
      institutionStatus.value = 'pending'
      // 重新加载资料以更新页面
      await loadProfile()
    } else {
      // 更新设置
      await institutionManageApi.updateSettings({
        basicInfo: basicInfo.value,
        images: institutionPhotos.value,
        serviceSettings: serviceSettings.value,
        notificationSettings: notificationSettings.value,
        paymentSettings: paymentSettings.value
      })
      ElMessage.success('设置已保存')
      // 重新加载资料和设置以更新页面
      await loadProfile()
      await loadSettings()
    }
  } catch (error: any) {
    ElMessage.error(error.message || '操作失败')
  } finally {
    loading.value = false
  }
}

const uploadLogo = () => {
  logoInputRef.value?.click()
}

const handleLogoChange = async (e: Event) => {
  const input = e.target as HTMLInputElement
  if (!input.files?.length) return
  
  const file = input.files[0]
  if (!file.type.startsWith('image/')) {
    ElMessage.warning('请选择图片文件')
    return
  }
  
  try {
    const res = await uploadApi.upload(file)
    if (res.code === 200 && res.data) {
      basicInfo.value.logo = res.data.url
      ElMessage.success('Logo上传成功')
    }
  } catch (error) {
    ElMessage.error('上传失败')
  }
  input.value = ''
}

const uploadCertificate = () => {
  certificateInputRef.value?.click()
}

const handleCertificateChange = async (e: Event) => {
  const input = e.target as HTMLInputElement
  if (!input.files?.length) return
  
  const file = input.files[0]
  if (!file.type.startsWith('image/')) {
    ElMessage.warning('请选择图片文件')
    return
  }
  
  try {
    const res = await uploadApi.upload(file)
    if (res.code === 200 && res.data) {
      certificateFile.value = res.data.url
      ElMessage.success('证书上传成功')
    }
  } catch (error) {
    ElMessage.error('上传失败')
  }
  input.value = ''
}

const uploadLicense = () => {
  licenseInputRef.value?.click()
}

const handleLicenseChange = async (e: Event) => {
  const input = e.target as HTMLInputElement
  if (!input.files?.length) return
  
  const file = input.files[0]
  if (!file.type.startsWith('image/')) {
    ElMessage.warning('请选择图片文件')
    return
  }
  
  try {
    const res = await uploadApi.upload(file)
    if (res.code === 200 && res.data) {
      licenseFile.value = res.data.url
      ElMessage.success('经营许可证上传成功')
    }
  } catch (error) {
    ElMessage.error('上传失败')
  }
  input.value = ''
}

const uploadPhotos = () => {
  photosInputRef.value?.click()
}

const handlePhotosChange = async (e: Event) => {
  const input = e.target as HTMLInputElement
  if (!input.files?.length) return
  
  const files = Array.from(input.files).filter(f => f.type.startsWith('image/'))
  if (files.length === 0) {
    ElMessage.warning('请选择图片文件')
    return
  }
  
  if (institutionPhotos.value.length + files.length > 9) {
    ElMessage.warning('最多上传9张照片')
    return
  }
  
  try {
    const res = await uploadApi.uploadMultiple(files)
    if (res.code === 200 && res.data) {
      institutionPhotos.value.push(...res.data.map(r => r.url))
      ElMessage.success(`成功上传${res.data.length}张照片`)
    }
  } catch (error) {
    ElMessage.error('上传失败')
  }
  input.value = ''
}

const removePhoto = (index: number) => {
  institutionPhotos.value.splice(index, 1)
}

onMounted(async () => {
  await loadProfile()
  await loadSettings()
})
</script>

<template>
  <div class="settings-view">
    <div class="page-header">
      <h1>🏠 机构资料</h1>
      <p>{{ hasInstitution ? '管理您的机构信息和服务配置' : '填写机构资料，申请入驻平台' }}</p>
    </div>

    <!-- 审核状态提示 -->
    <div v-if="statusConfig" class="status-banner" :style="{ background: statusConfig.bg, borderColor: statusConfig.color }">
      <component :is="statusConfig.icon" :size="24" :style="{ color: statusConfig.color }" />
      <div class="status-content">
        <span class="status-title" :style="{ color: statusConfig.color }">{{ statusConfig.text }}</span>
        <span class="status-desc">{{ statusConfig.desc }}</span>
      </div>
    </div>

    <div class="settings-layout">
      <!-- 侧边导航 -->
      <div class="settings-nav">
        <button class="nav-item" :class="{ active: activeTab === 'basic' }" @click="activeTab = 'basic'">
          <Shield :size="18" /> 基本信息
        </button>
        <button v-if="institutionStatus === 'active'" class="nav-item" :class="{ active: activeTab === 'service' }" @click="activeTab = 'service'">
          <Clock :size="18" /> 服务设置
        </button>
        <button v-if="institutionStatus === 'active'" class="nav-item" :class="{ active: activeTab === 'notification' }" @click="activeTab = 'notification'">
          <Bell :size="18" /> 通知设置
        </button>
        <button v-if="institutionStatus === 'active'" class="nav-item" :class="{ active: activeTab === 'payment' }" @click="activeTab = 'payment'">
          <CreditCard :size="18" /> 收款设置
        </button>
      </div>

      <!-- 设置内容 -->
      <div class="settings-content">
        <!-- 基本信息 -->
        <div v-if="activeTab === 'basic'" class="settings-panel">
          <h2>基本信息</h2>
          
          <div class="form-group">
            <label>机构Logo</label>
            <input ref="logoInputRef" type="file" accept="image/*" style="display:none" @change="handleLogoChange" />
            <div class="logo-upload" @click="uploadLogo">
              <img v-if="basicInfo.logo" :src="basicInfo.logo" class="logo-preview" />
              <div v-else class="logo-placeholder">
                <Camera :size="32" />
                <span>点击上传</span>
              </div>
            </div>
          </div>

          <div class="form-group">
            <label>机构名称</label>
            <el-input v-model="basicInfo.name" placeholder="请输入机构名称" />
          </div>

          <div class="form-group">
            <label>机构简介</label>
            <el-input v-model="basicInfo.description" type="textarea" :rows="4" placeholder="介绍您的机构特色和服务..." />
          </div>

          <div class="form-row">
            <div class="form-group">
              <label><Phone :size="16" /> 联系电话</label>
              <el-input v-model="basicInfo.phone" placeholder="固定电话" />
            </div>
            <div class="form-group">
              <label>手机号码</label>
              <el-input v-model="basicInfo.mobile" placeholder="手机号码" />
            </div>
          </div>

          <div class="form-group">
            <label><MapPin :size="16" /> 详细地址</label>
            <el-input v-model="basicInfo.address" placeholder="请输入详细地址" />
          </div>

          <div class="form-group">
            <label>营业时间</label>
            <div class="business-hours">
              <div class="hours-row">
                <span class="day-label">工作日</span>
                <el-time-select v-model="basicInfo.businessHours.weekday.start" :max-time="basicInfo.businessHours.weekday.end" placeholder="开始时间" start="06:00" step="00:30" end="23:30" />
                <span>至</span>
                <el-time-select v-model="basicInfo.businessHours.weekday.end" :min-time="basicInfo.businessHours.weekday.start" placeholder="结束时间" start="06:00" step="00:30" end="23:30" />
              </div>
              <div class="hours-row">
                <span class="day-label">周末</span>
                <el-time-select v-model="basicInfo.businessHours.weekend.start" :max-time="basicInfo.businessHours.weekend.end" placeholder="开始时间" start="06:00" step="00:30" end="23:30" />
                <span>至</span>
                <el-time-select v-model="basicInfo.businessHours.weekend.end" :min-time="basicInfo.businessHours.weekend.start" placeholder="结束时间" start="06:00" step="00:30" end="23:30" />
              </div>
            </div>
          </div>

          <div class="form-group">
            <label>资质证书</label>
            <input ref="certificateInputRef" type="file" accept="image/*" style="display:none" @change="handleCertificateChange" />
            <div class="document-upload" @click="uploadCertificate">
              <img v-if="certificateFile" :src="certificateFile" class="doc-preview" />
              <div v-else class="doc-placeholder">
                <Upload :size="24" />
                <span>上传资质证书</span>
              </div>
            </div>
          </div>

          <div class="form-group">
            <label>经营许可证</label>
            <input ref="licenseInputRef" type="file" accept="image/*" style="display:none" @change="handleLicenseChange" />
            <div class="document-upload" @click="uploadLicense">
              <img v-if="licenseFile" :src="licenseFile" class="doc-preview" />
              <div v-else class="doc-placeholder">
                <Upload :size="24" />
                <span>上传经营许可证</span>
              </div>
            </div>
          </div>

          <div class="form-group">
            <label>机构照片</label>
            <input ref="photosInputRef" type="file" accept="image/*" multiple style="display:none" @change="handlePhotosChange" />
            <div class="photos-grid">
              <div v-for="(photo, index) in institutionPhotos" :key="index" class="photo-item">
                <img :src="photo" />
                <button class="remove-btn" @click="removePhoto(index)">×</button>
              </div>
              <div v-if="institutionPhotos.length < 9" class="photos-upload" @click="uploadPhotos">
                <Upload :size="24" />
                <span>上传照片</span>
              </div>
            </div>
            <p class="form-hint">最多上传9张机构环境照片</p>
          </div>
        </div>

        <!-- 服务设置 -->
        <div v-if="activeTab === 'service'" class="settings-panel">
          <h2>服务设置</h2>

          <div class="form-group">
            <label>接受宠物类型</label>
            <div class="checkbox-group">
              <label v-for="opt in petTypeOptions" :key="opt.value" class="checkbox-item">
                <input type="checkbox" :value="opt.value" v-model="serviceSettings.acceptedPets" />
                {{ opt.label }}
              </label>
            </div>
          </div>

          <div class="form-row">
            <div class="form-group">
              <label>最大宠物体重 (kg)</label>
              <el-input-number v-model="serviceSettings.maxPetWeight" :min="5" :max="100" />
            </div>
            <div class="form-group">
              <label>最大接待数量</label>
              <el-input-number v-model="serviceSettings.maxCapacity" :min="1" :max="100" />
            </div>
          </div>

          <div class="form-row">
            <div class="form-group">
              <label>最短预约天数</label>
              <el-input-number v-model="serviceSettings.minBookingDays" :min="1" :max="30" />
            </div>
            <div class="form-group">
              <label>最长预约天数</label>
              <el-input-number v-model="serviceSettings.maxBookingDays" :min="1" :max="365" />
            </div>
          </div>

          <div class="form-row">
            <div class="form-group">
              <label>入住时间</label>
              <el-time-select v-model="serviceSettings.checkInTime" placeholder="入住时间" start="06:00" step="00:30" end="23:30" />
            </div>
            <div class="form-group">
              <label>退房时间</label>
              <el-time-select v-model="serviceSettings.checkOutTime" placeholder="退房时间" start="06:00" step="00:30" end="23:30" />
            </div>
          </div>

          <div class="form-group">
            <label>取消政策</label>
            <el-select v-model="serviceSettings.cancellationPolicy" style="width: 100%">
              <el-option v-for="opt in cancellationOptions" :key="opt.value" :value="opt.value" :label="opt.label" />
            </el-select>
          </div>

          <div class="form-row">
            <div class="form-group">
              <label>紧急联系电话</label>
              <el-input v-model="serviceSettings.emergencyContact" placeholder="24小时紧急联系电话" />
            </div>
            <div class="form-group">
              <label>合作宠物医院</label>
              <el-input v-model="serviceSettings.veterinaryPartner" placeholder="合作宠物医院名称" />
            </div>
          </div>
        </div>

        <!-- 通知设置 -->
        <div v-if="activeTab === 'notification'" class="settings-panel">
          <h2>通知设置</h2>

          <div class="notification-section">
            <h3>订单通知</h3>
            <div class="switch-group">
              <div class="switch-item">
                <span>新订单提醒</span>
                <el-switch v-model="notificationSettings.newOrder" />
              </div>
              <div class="switch-item">
                <span>订单确认通知</span>
                <el-switch v-model="notificationSettings.orderConfirm" />
              </div>
              <div class="switch-item">
                <span>订单取消通知</span>
                <el-switch v-model="notificationSettings.orderCancel" />
              </div>
            </div>
          </div>

          <div class="notification-section">
            <h3>其他通知</h3>
            <div class="switch-group">
              <div class="switch-item">
                <span>新评价提醒</span>
                <el-switch v-model="notificationSettings.newReview" />
              </div>
              <div class="switch-item">
                <span>投诉通知</span>
                <el-switch v-model="notificationSettings.complaint" />
              </div>
              <div class="switch-item">
                <span>系统公告</span>
                <el-switch v-model="notificationSettings.systemNotice" />
              </div>
            </div>
          </div>

          <div class="notification-section">
            <h3>通知渠道</h3>
            <div class="switch-group">
              <div class="switch-item">
                <span>邮件通知</span>
                <el-switch v-model="notificationSettings.emailNotification" />
              </div>
              <div class="switch-item">
                <span>短信通知</span>
                <el-switch v-model="notificationSettings.smsNotification" />
              </div>
              <div class="switch-item">
                <span>App推送</span>
                <el-switch v-model="notificationSettings.pushNotification" />
              </div>
            </div>
          </div>
        </div>

        <!-- 收款设置 -->
        <div v-if="activeTab === 'payment'" class="settings-panel">
          <h2>收款设置</h2>

          <div class="form-group">
            <label>开户银行</label>
            <el-input v-model="paymentSettings.bankName" placeholder="请输入开户银行" />
          </div>

          <div class="form-group">
            <label>账户名称</label>
            <el-input v-model="paymentSettings.accountName" placeholder="请输入账户名称" />
          </div>

          <div class="form-group">
            <label>银行账号</label>
            <el-input v-model="paymentSettings.accountNumber" placeholder="请输入银行账号" />
          </div>

          <div class="form-group">
            <label>自动提现</label>
            <div class="switch-item">
              <span>达到阈值自动提现</span>
              <el-switch v-model="paymentSettings.autoWithdraw" />
            </div>
          </div>

          <div v-if="paymentSettings.autoWithdraw" class="form-group">
            <label>提现阈值 (元)</label>
            <el-input-number v-model="paymentSettings.withdrawThreshold" :min="100" :max="100000" :step="100" />
          </div>
        </div>

        <!-- 保存按钮 -->
        <div class="settings-footer">
          <button class="btn-save" :disabled="loading || institutionStatus === 'pending'" @click="saveSettings">
            <Save :size="16" /> {{ loading ? '提交中...' : (hasInstitution && institutionStatus !== 'rejected' ? '保存设置' : '提交申请') }}
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<style lang="scss" scoped>
.settings-view { max-width: 1200px; margin: 0 auto; padding: 24px; }

.page-header { margin-bottom: 24px;
  h1 { font-size: 26px; font-weight: 700; margin: 0 0 6px; }
  p { color: #6B6560; margin: 0; }
}

.status-banner {
  display: flex; align-items: center; gap: 16px; padding: 16px 20px;
  border: 1px solid; border-radius: 12px; margin-bottom: 24px;
  .status-content { display: flex; flex-direction: column; gap: 4px; }
  .status-title { font-size: 16px; font-weight: 600; }
  .status-desc { font-size: 14px; color: #6B6560; }
}

.settings-layout { display: grid; grid-template-columns: 240px 1fr; gap: 24px; }

.settings-nav {
  display: flex; flex-direction: column; gap: 8px;
  .nav-item {
    display: flex; align-items: center; gap: 10px; padding: 14px 18px;
    border: none; background: white; border-radius: 10px; text-align: left;
    font-size: 14px; color: #6B6560; cursor: pointer; transition: all 0.2s;
    &:hover { background: #F8F8F7; }
    &.active { background: #722ed1; color: white; }
  }
}

.settings-content { background: white; border-radius: 16px; padding: 24px; }

.settings-panel {
  h2 { font-size: 18px; font-weight: 600; margin: 0 0 24px; padding-bottom: 16px; border-bottom: 1px solid #F0F0EF; }
  h3 { font-size: 15px; font-weight: 600; margin: 0 0 16px; color: #2D2A26; }
}

.form-group { margin-bottom: 20px;
  label { display: flex; align-items: center; gap: 6px; font-size: 14px; font-weight: 500; margin-bottom: 8px; color: #2D2A26; }
}

.form-row { display: grid; grid-template-columns: 1fr 1fr; gap: 20px; }

.logo-upload {
  width: 120px; height: 120px; border: 2px dashed #E5E5E5; border-radius: 12px;
  display: flex; flex-direction: column; align-items: center; justify-content: center;
  cursor: pointer; color: #9A958F; transition: all 0.2s; overflow: hidden;
  &:hover { border-color: #722ed1; color: #722ed1; }
  span { font-size: 12px; margin-top: 8px; }
  .logo-preview { width: 100%; height: 100%; object-fit: cover; }
}

.photos-grid {
  display: grid; grid-template-columns: repeat(auto-fill, minmax(100px, 1fr)); gap: 12px;
  .photo-item {
    position: relative; aspect-ratio: 1; border-radius: 8px; overflow: hidden;
    img { width: 100%; height: 100%; object-fit: cover; }
    .remove-btn {
      position: absolute; top: 4px; right: 4px; width: 24px; height: 24px;
      background: rgba(0,0,0,0.6); color: white; border: none; border-radius: 50%;
      cursor: pointer; font-size: 16px; line-height: 1;
      &:hover { background: #ff4d4f; }
    }
  }
}

.photos-upload {
  aspect-ratio: 1; border: 2px dashed #E5E5E5; border-radius: 8px;
  display: flex; flex-direction: column; align-items: center; justify-content: center;
  cursor: pointer; color: #9A958F; transition: all 0.2s; min-height: 100px;
  &:hover { border-color: #722ed1; color: #722ed1; }
  span { font-size: 12px; margin-top: 8px; }
}

.document-upload {
  width: 100%; height: 160px; border: 2px dashed #E5E5E5; border-radius: 12px;
  display: flex; flex-direction: column; align-items: center; justify-content: center;
  cursor: pointer; color: #9A958F; transition: all 0.2s; overflow: hidden;
  &:hover { border-color: #722ed1; color: #722ed1; }
  .doc-preview { width: 100%; height: 100%; object-fit: cover; }
  .doc-placeholder { display: flex; flex-direction: column; align-items: center; gap: 8px; }
}

.form-hint { font-size: 12px; color: #9A958F; margin-top: 8px; }

.business-hours {
  .hours-row { display: flex; align-items: center; gap: 12px; margin-bottom: 12px;
    .day-label { width: 60px; font-size: 14px; color: #6B6560; }
  }
}

.checkbox-group { display: flex; flex-wrap: wrap; gap: 16px; }
.checkbox-item { display: flex; align-items: center; gap: 8px; font-size: 14px;
  input { width: 18px; height: 18px; }
}

.notification-section { margin-bottom: 32px; }
.switch-group { display: flex; flex-direction: column; gap: 16px; }
.switch-item { display: flex; justify-content: space-between; align-items: center; padding: 12px 16px; background: #F8F8F7; border-radius: 10px;
  span { font-size: 14px; }
}

.settings-footer { margin-top: 32px; padding-top: 24px; border-top: 1px solid #F0F0EF; text-align: right; }
.btn-save {
  display: inline-flex; align-items: center; gap: 8px; padding: 12px 32px;
  background: #722ed1; color: white; border: none; border-radius: 10px;
  font-size: 15px; font-weight: 500; cursor: pointer;
  &:disabled { opacity: 0.6; }
  &:hover:not(:disabled) { background: #531dab; }
}

@media (max-width: 768px) {
  .settings-layout { grid-template-columns: 1fr; }
  .settings-nav { flex-direction: row; overflow-x: auto; }
  .form-row { grid-template-columns: 1fr; }
}
</style>
