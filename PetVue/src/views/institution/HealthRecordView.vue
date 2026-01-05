<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { Calendar, Camera, Upload, Save, Clock, Activity, Utensils, Heart, AlertCircle } from 'lucide-vue-next'
import { ElMessage, ElMessageBox } from 'element-plus'
import api from '@/api/index'

const router = useRouter()
const route = useRoute()
const loading = ref(false)
const activeTab = ref<'today' | 'history'>('today')

// 今日待记录宠物（从进行中的订单获取）
const todayPets = ref<any[]>([])

// 加载今日待记录宠物
const loadTodayPets = async () => {
  try {
    const res = await api.get('/institution/bookings?page=1&pageSize=100')
    if (res.code === 200 && res.data) {
      const bookings = res.data.list || res.data || []
      // 筛选进行中的订单
      const inProgressBookings = bookings.filter((b: any) => b.status === 'in_progress')
      
      // 检查每个订单今日是否已记录
      const petsWithStatus = await Promise.all(inProgressBookings.map(async (booking: any) => {
        let recorded = false
        try {
          const recordsRes = await api.get(`/health-records/booking/${booking.id}`)
          if (recordsRes.code === 200 && recordsRes.data) {
            const today = new Date().toISOString().split('T')[0]
            recorded = recordsRes.data.some((r: any) => r.date && r.date.startsWith(today))
          }
        } catch (e) {
          // 忽略错误
        }
        return {
          id: booking.id,
          name: booking.petName || '未知',
          type: booking.petBreed || booking.petSpecies || '',
          avatar: booking.petSpecies === 'cat' ? '🐱' : '🐕',
          room: '-',
          owner: booking.userName || '未知',
          checkInDate: booking.startDate || '',
          recorded
        }
      }))
      
      todayPets.value = petsWithStatus
    }
  } catch (error) {
    console.error('加载待记录宠物失败:', error)
  }
}

// 当前编辑的健康记录
const currentRecord = ref({
  petId: '',
  petName: '',
  date: new Date().toISOString().split('T')[0],
  feedingStatus: 'normal',
  feedingAmount: '',
  feedingNotes: '',
  activityLevel: 'normal',
  activityNotes: '',
  healthStatus: 'healthy',
  healthNotes: '',
  mood: 'happy',
  moodNotes: '',
  temperature: '',
  weight: '',
  abnormalSigns: [] as string[],
  photos: [] as string[],
  videos: [] as string[]
})

// 历史记录
const historyRecords = ref<any[]>([])

// 加载历史记录
const loadHistoryRecords = async () => {
  try {
    // 获取所有进行中订单的健康记录
    const res = await api.get('/institution/bookings?page=1&pageSize=100')
    if (res.code === 200 && res.data) {
      const bookings = res.data.list || res.data || []
      const allRecords: any[] = []
      
      for (const booking of bookings) {
        try {
          const recordsRes = await api.get(`/health-records/booking/${booking.id}`)
          if (recordsRes.code === 200 && recordsRes.data) {
            recordsRes.data.forEach((record: any) => {
              allRecords.push({
                id: record.id,
                bookingId: booking.id,
                petName: booking.petName || '未知',
                date: record.date ? record.date.split('T')[0] : '',
                feedingStatus: record.feedingStatus || 'normal',
                activityLevel: record.activityLevel || 'normal',
                healthStatus: record.healthObservations ? 'attention' : 'healthy',
                hasMedia: record.photos && record.photos.length > 0
              })
            })
          }
        } catch (e) {
          // 忽略错误
        }
      }
      
      // 按日期排序
      allRecords.sort((a, b) => new Date(b.date).getTime() - new Date(a.date).getTime())
      historyRecords.value = allRecords
    }
  } catch (error) {
    console.error('加载历史记录失败:', error)
  }
}

const showRecordForm = ref(false)

const pendingCount = computed(() => todayPets.value.filter(p => !p.recorded).length)
const recordedCount = computed(() => todayPets.value.filter(p => p.recorded).length)

const feedingOptions = [
  { value: 'normal', label: '正常', color: '#52c41a' },
  { value: 'more', label: '食量增加', color: '#1890ff' },
  { value: 'less', label: '食量减少', color: '#faad14' },
  { value: 'none', label: '未进食', color: '#ff4d4f' }
]

const activityOptions = [
  { value: 'active', label: '活跃', color: '#52c41a' },
  { value: 'normal', label: '正常', color: '#1890ff' },
  { value: 'low', label: '低迷', color: '#faad14' },
  { value: 'inactive', label: '不活动', color: '#ff4d4f' }
]

const healthOptions = [
  { value: 'healthy', label: '健康', color: '#52c41a' },
  { value: 'attention', label: '需关注', color: '#faad14' },
  { value: 'sick', label: '生病', color: '#ff4d4f' }
]

const moodOptions = [
  { value: 'happy', label: '开心 😊' },
  { value: 'normal', label: '正常 😐' },
  { value: 'anxious', label: '焦虑 😟' },
  { value: 'sad', label: '低落 😢' }
]

const abnormalOptions = [
  '呕吐', '腹泻', '咳嗽', '打喷嚏', '流鼻涕', '眼睛异常', '皮肤问题', '食欲不振', '精神萎靡', '其他'
]

const openRecordForm = (pet: typeof todayPets.value[0]) => {
  currentRecord.value = {
    petId: pet.id,
    petName: pet.name,
    date: new Date().toISOString().split('T')[0],
    feedingStatus: 'normal',
    feedingAmount: '',
    feedingNotes: '',
    activityLevel: 'normal',
    activityNotes: '',
    healthStatus: 'healthy',
    healthNotes: '',
    mood: 'happy',
    moodNotes: '',
    temperature: '',
    weight: '',
    abnormalSigns: [],
    photos: [],
    videos: []
  }
  showRecordForm.value = true
}

const saveRecord = async () => {
  if (!currentRecord.value.petId) {
    ElMessage.warning('请选择宠物')
    return
  }
  
  loading.value = true
  try {
    // 调用API保存健康记录
    await api.post(`/health-records/booking/${currentRecord.value.petId}`, {
      feedingStatus: currentRecord.value.feedingStatus,
      activityLevel: currentRecord.value.activityLevel,
      healthObservations: currentRecord.value.healthNotes || currentRecord.value.abnormalSigns.join(', '),
      mood: currentRecord.value.mood,
      weight: currentRecord.value.weight ? parseFloat(currentRecord.value.weight) : null,
      temperature: currentRecord.value.temperature ? parseFloat(currentRecord.value.temperature) : null,
      photos: currentRecord.value.photos,
      abnormalDetails: currentRecord.value.abnormalSigns.join(', ')
    })
    
    const pet = todayPets.value.find(p => p.id === currentRecord.value.petId)
    if (pet) {
      pet.recorded = true
    }
    
    showRecordForm.value = false
    ElMessage.success('健康记录已保存')
    
    // 如果有异常情况，提示是否通知主人
    if (currentRecord.value.abnormalSigns.length > 0 || currentRecord.value.healthStatus !== 'healthy') {
      await ElMessageBox.confirm('检测到异常情况，是否立即通知宠物主人？', '异常提醒', {
        confirmButtonText: '立即通知',
        cancelButtonText: '稍后处理',
        type: 'warning'
      })
      ElMessage.success('已通知宠物主人')
    }
    
    // 刷新历史记录
    loadHistoryRecords()
  } catch (error: any) {
    if (error !== 'cancel') {
      ElMessage.error('保存失败')
    }
  } finally {
    loading.value = false
  }
}

// 文件上传引用
const photoInputRef = ref<HTMLInputElement | null>(null)
const videoInputRef = ref<HTMLInputElement | null>(null)
const uploadingPhoto = ref(false)
const uploadingVideo = ref(false)

const triggerPhotoUpload = () => {
  photoInputRef.value?.click()
}

const triggerVideoUpload = () => {
  videoInputRef.value?.click()
}

const handlePhotoUpload = async (event: Event) => {
  const input = event.target as HTMLInputElement
  if (!input.files || input.files.length === 0) return
  
  uploadingPhoto.value = true
  try {
    for (const file of Array.from(input.files)) {
      const formData = new FormData()
      formData.append('file', file)
      
      const res = await api.post('/upload', formData, {
        headers: { 'Content-Type': 'multipart/form-data' }
      })
      
      if (res.code === 200 && res.data) {
        currentRecord.value.photos.push(res.data.url || res.data)
        ElMessage.success(`照片 ${file.name} 上传成功`)
      }
    }
  } catch (error) {
    ElMessage.error('照片上传失败')
  } finally {
    uploadingPhoto.value = false
    input.value = ''
  }
}

const handleVideoUpload = async (event: Event) => {
  const input = event.target as HTMLInputElement
  if (!input.files || input.files.length === 0) return
  
  uploadingVideo.value = true
  try {
    for (const file of Array.from(input.files)) {
      const formData = new FormData()
      formData.append('file', file)
      
      const res = await api.post('/upload', formData, {
        headers: { 'Content-Type': 'multipart/form-data' }
      })
      
      if (res.code === 200 && res.data) {
        currentRecord.value.videos.push(res.data.url || res.data)
        ElMessage.success(`视频 ${file.name} 上传成功`)
      }
    }
  } catch (error) {
    ElMessage.error('视频上传失败')
  } finally {
    uploadingVideo.value = false
    input.value = ''
  }
}

const removePhoto = (index: number) => {
  currentRecord.value.photos.splice(index, 1)
}

const removeVideo = (index: number) => {
  currentRecord.value.videos.splice(index, 1)
}

const viewHistory = (record: typeof historyRecords.value[0]) => {
  // 健康记录详情暂时使用消息提示，后续可扩展为弹窗显示
  ElMessage.info(`查看健康记录: ${record.petName} - ${record.date}`)
}

const getStatusColor = (status: string, type: 'feeding' | 'activity' | 'health') => {
  const options = type === 'feeding' ? feedingOptions : type === 'activity' ? activityOptions : healthOptions
  return options.find(o => o.value === status)?.color || '#999'
}

const getStatusLabel = (status: string, type: 'feeding' | 'activity' | 'health') => {
  const options = type === 'feeding' ? feedingOptions : type === 'activity' ? activityOptions : healthOptions
  return options.find(o => o.value === status)?.label || status
}

onMounted(() => {
  loadTodayPets()
  loadHistoryRecords()
})
</script>

<template>
  <div class="health-record-view">
    <div class="page-header">
      <div class="header-left">
        <h1>💊 健康记录管理</h1>
        <p>记录宠物每日健康状态</p>
      </div>
      <div class="header-stats">
        <div class="stat-item pending">
          <span class="stat-value">{{ pendingCount }}</span>
          <span class="stat-label">待记录</span>
        </div>
        <div class="stat-item completed">
          <span class="stat-value">{{ recordedCount }}</span>
          <span class="stat-label">已完成</span>
        </div>
      </div>
    </div>

    <!-- 标签页 -->
    <div class="tabs">
      <button class="tab-btn" :class="{ active: activeTab === 'today' }" @click="activeTab = 'today'">
        今日记录
      </button>
      <button class="tab-btn" :class="{ active: activeTab === 'history' }" @click="activeTab = 'history'">
        历史记录
      </button>
    </div>

    <!-- 今日待记录 -->
    <div v-if="activeTab === 'today'" class="today-section">
      <div class="pet-grid">
        <div v-for="pet in todayPets" :key="pet.id" class="pet-card" :class="{ recorded: pet.recorded }">
          <div class="pet-avatar">{{ pet.avatar }}</div>
          <div class="pet-info">
            <span class="pet-name">{{ pet.name }}</span>
            <span class="pet-type">{{ pet.type }} · {{ pet.room }}</span>
            <span class="pet-owner">主人: {{ pet.owner }}</span>
          </div>
          <div class="pet-status">
            <span v-if="pet.recorded" class="status-badge recorded">✓ 已记录</span>
            <span v-else class="status-badge pending">待记录</span>
          </div>
          <button v-if="!pet.recorded" class="record-btn" @click="openRecordForm(pet)">
            <Activity :size="16" /> 记录
          </button>
          <button v-else class="view-btn" @click="openRecordForm(pet)">
            查看/编辑
          </button>
        </div>
      </div>
    </div>

    <!-- 历史记录 -->
    <div v-if="activeTab === 'history'" class="history-section">
      <div class="history-list">
        <div v-for="record in historyRecords" :key="record.id" class="history-item" @click="viewHistory(record)">
          <div class="history-date">
            <Calendar :size="16" />
            {{ record.date }}
          </div>
          <div class="history-pet">{{ record.petName }}</div>
          <div class="history-status">
            <span class="status-tag" :style="{ background: getStatusColor(record.feedingStatus, 'feeding') + '20', color: getStatusColor(record.feedingStatus, 'feeding') }">
              {{ getStatusLabel(record.feedingStatus, 'feeding') }}
            </span>
            <span class="status-tag" :style="{ background: getStatusColor(record.activityLevel, 'activity') + '20', color: getStatusColor(record.activityLevel, 'activity') }">
              {{ getStatusLabel(record.activityLevel, 'activity') }}
            </span>
            <span class="status-tag" :style="{ background: getStatusColor(record.healthStatus, 'health') + '20', color: getStatusColor(record.healthStatus, 'health') }">
              {{ getStatusLabel(record.healthStatus, 'health') }}
            </span>
          </div>
          <div class="history-media">
            <Camera v-if="record.hasMedia" :size="16" />
          </div>
        </div>
      </div>
    </div>

    <!-- 记录表单弹窗 -->
    <el-dialog v-model="showRecordForm" :title="`记录 ${currentRecord.petName} 的健康状态`" width="700px" :close-on-click-modal="false">
      <div class="record-form">
        <div class="form-section">
          <h4><Utensils :size="18" /> 饮食情况</h4>
          <div class="option-group">
            <button v-for="opt in feedingOptions" :key="opt.value" class="option-btn" :class="{ active: currentRecord.feedingStatus === opt.value }" :style="currentRecord.feedingStatus === opt.value ? { background: opt.color + '20', borderColor: opt.color, color: opt.color } : {}" @click="currentRecord.feedingStatus = opt.value">
              {{ opt.label }}
            </button>
          </div>
          <el-input v-model="currentRecord.feedingNotes" type="textarea" placeholder="饮食备注（可选）" :rows="2" />
        </div>

        <div class="form-section">
          <h4><Activity :size="18" /> 活动状态</h4>
          <div class="option-group">
            <button v-for="opt in activityOptions" :key="opt.value" class="option-btn" :class="{ active: currentRecord.activityLevel === opt.value }" :style="currentRecord.activityLevel === opt.value ? { background: opt.color + '20', borderColor: opt.color, color: opt.color } : {}" @click="currentRecord.activityLevel = opt.value">
              {{ opt.label }}
            </button>
          </div>
          <el-input v-model="currentRecord.activityNotes" type="textarea" placeholder="活动备注（可选）" :rows="2" />
        </div>

        <div class="form-section">
          <h4><Heart :size="18" /> 健康状况</h4>
          <div class="option-group">
            <button v-for="opt in healthOptions" :key="opt.value" class="option-btn" :class="{ active: currentRecord.healthStatus === opt.value }" :style="currentRecord.healthStatus === opt.value ? { background: opt.color + '20', borderColor: opt.color, color: opt.color } : {}" @click="currentRecord.healthStatus = opt.value">
              {{ opt.label }}
            </button>
          </div>
        </div>

        <div class="form-section">
          <h4>😊 情绪状态</h4>
          <div class="option-group">
            <button v-for="opt in moodOptions" :key="opt.value" class="option-btn" :class="{ active: currentRecord.mood === opt.value }" @click="currentRecord.mood = opt.value">
              {{ opt.label }}
            </button>
          </div>
        </div>

        <div class="form-section">
          <h4><AlertCircle :size="18" /> 异常症状（多选）</h4>
          <div class="checkbox-group">
            <label v-for="sign in abnormalOptions" :key="sign" class="checkbox-item">
              <input type="checkbox" :value="sign" v-model="currentRecord.abnormalSigns" />
              {{ sign }}
            </label>
          </div>
        </div>

        <div class="form-section">
          <h4><Camera :size="18" /> 照片/视频</h4>
          <div class="media-upload">
            <input 
              ref="photoInputRef" 
              type="file" 
              accept="image/*" 
              multiple 
              style="display: none" 
              @change="handlePhotoUpload"
            />
            <button class="upload-btn" :disabled="uploadingPhoto" @click="triggerPhotoUpload">
              <Camera :size="20" /> {{ uploadingPhoto ? '上传中...' : '拍照/上传照片' }}
            </button>
            <input 
              ref="videoInputRef" 
              type="file" 
              accept="video/*" 
              multiple 
              style="display: none" 
              @change="handleVideoUpload"
            />
            <button class="upload-btn" :disabled="uploadingVideo" @click="triggerVideoUpload">
              <Upload :size="20" /> {{ uploadingVideo ? '上传中...' : '上传视频' }}
            </button>
          </div>
          <div v-if="currentRecord.photos.length || currentRecord.videos.length" class="media-preview">
            <div v-for="(photo, index) in currentRecord.photos" :key="photo" class="media-item">
              <img :src="photo" alt="照片" class="preview-img" />
              <button class="remove-btn" @click="removePhoto(index)">×</button>
            </div>
            <div v-for="(video, index) in currentRecord.videos" :key="video" class="media-item video">
              <span>🎬 视频{{ index + 1 }}</span>
              <button class="remove-btn" @click="removeVideo(index)">×</button>
            </div>
          </div>
        </div>

        <div class="form-section inline">
          <div class="inline-field">
            <label>体温 (°C)</label>
            <el-input v-model="currentRecord.temperature" placeholder="38.5" />
          </div>
          <div class="inline-field">
            <label>体重 (kg)</label>
            <el-input v-model="currentRecord.weight" placeholder="5.2" />
          </div>
        </div>
      </div>

      <template #footer>
        <button class="btn-cancel" @click="showRecordForm = false">取消</button>
        <button class="btn-save" :disabled="loading" @click="saveRecord">
          <Save :size="16" /> {{ loading ? '保存中...' : '保存记录' }}
        </button>
      </template>
    </el-dialog>
  </div>
</template>


<style lang="scss" scoped>
.health-record-view { max-width: 1200px; margin: 0 auto; padding: 24px; }

.page-header {
  display: flex; justify-content: space-between; align-items: flex-start; margin-bottom: 24px;
  .header-left {
    h1 { font-size: 26px; font-weight: 700; margin: 0 0 6px; }
    p { color: #6B6560; margin: 0; }
  }
  .header-stats {
    display: flex; gap: 16px;
    .stat-item {
      text-align: center; padding: 12px 24px; border-radius: 12px;
      &.pending { background: #FEF3C7; }
      &.completed { background: #DCFCE7; }
      .stat-value { display: block; font-size: 24px; font-weight: 700; }
      .stat-label { font-size: 12px; color: #6B6560; }
    }
  }
}

.tabs {
  display: flex; gap: 8px; margin-bottom: 24px;
  .tab-btn {
    padding: 10px 20px; border: none; background: white; border-radius: 8px;
    font-size: 14px; color: #6B6560; cursor: pointer; transition: all 0.2s;
    &:hover { background: #F0F0EF; }
    &.active { background: #52c41a; color: white; }
  }
}

.pet-grid {
  display: grid; grid-template-columns: repeat(auto-fill, minmax(300px, 1fr)); gap: 16px;
}

.pet-card {
  display: flex; align-items: center; gap: 12px; padding: 16px;
  background: white; border-radius: 12px; box-shadow: 0 2px 8px rgba(0,0,0,0.04);
  &.recorded { opacity: 0.7; }
  .pet-avatar { font-size: 36px; }
  .pet-info { flex: 1;
    .pet-name { display: block; font-weight: 600; font-size: 16px; }
    .pet-type, .pet-owner { display: block; font-size: 12px; color: #6B6560; }
  }
  .status-badge {
    font-size: 12px; padding: 4px 10px; border-radius: 12px;
    &.recorded { background: #DCFCE7; color: #16A34A; }
    &.pending { background: #FEF3C7; color: #D97706; }
  }
  .record-btn, .view-btn {
    display: flex; align-items: center; gap: 6px; padding: 8px 16px;
    border: none; border-radius: 8px; font-size: 13px; cursor: pointer;
  }
  .record-btn { background: #52c41a; color: white; }
  .view-btn { background: #F0F0EF; color: #6B6560; }
}

.history-list { display: flex; flex-direction: column; gap: 12px; }
.history-item {
  display: flex; align-items: center; gap: 16px; padding: 16px;
  background: white; border-radius: 12px; cursor: pointer; transition: all 0.2s;
  &:hover { box-shadow: 0 4px 12px rgba(0,0,0,0.08); }
  .history-date { display: flex; align-items: center; gap: 6px; font-size: 14px; color: #6B6560; min-width: 120px; }
  .history-pet { font-weight: 600; min-width: 80px; }
  .history-status { flex: 1; display: flex; gap: 8px; }
  .status-tag { font-size: 12px; padding: 4px 10px; border-radius: 12px; }
  .history-media { color: #9A958F; }
}

.record-form {
  .form-section {
    margin-bottom: 24px;
    h4 { display: flex; align-items: center; gap: 8px; font-size: 15px; margin: 0 0 12px; }
    &.inline { display: flex; gap: 16px;
      .inline-field { flex: 1;
        label { display: block; font-size: 13px; color: #6B6560; margin-bottom: 6px; }
      }
    }
  }
  .option-group { display: flex; gap: 10px; margin-bottom: 12px; flex-wrap: wrap; }
  .option-btn {
    padding: 8px 16px; border: 1px solid #E5E5E5; background: white;
    border-radius: 8px; font-size: 13px; cursor: pointer; transition: all 0.2s;
    &:hover { border-color: #1890ff; }
    &.active { border-color: #1890ff; background: #E8F4FD; color: #1890ff; }
  }
  .checkbox-group { display: flex; flex-wrap: wrap; gap: 12px; }
  .checkbox-item {
    display: flex; align-items: center; gap: 6px; font-size: 13px;
    input { width: 16px; height: 16px; }
  }
  .media-upload { display: flex; gap: 12px; margin-bottom: 12px; }
  .upload-btn {
    display: flex; align-items: center; gap: 8px; padding: 12px 20px;
    border: 2px dashed #E5E5E5; background: #FAFAF9; border-radius: 8px;
    font-size: 13px; color: #6B6560; cursor: pointer;
    &:hover { border-color: #1890ff; color: #1890ff; }
  }
  .media-preview { display: flex; flex-wrap: wrap; gap: 12px; margin-top: 12px; }
  .media-item { 
    position: relative; 
    background: #F0F0EF; 
    border-radius: 8px; 
    overflow: hidden;
    &.video {
      display: flex; align-items: center; gap: 8px;
      padding: 8px 12px; font-size: 13px;
    }
    .preview-img {
      width: 80px; height: 80px; object-fit: cover; display: block;
    }
    .remove-btn {
      position: absolute; top: 2px; right: 2px;
      width: 20px; height: 20px; border: none;
      background: rgba(0,0,0,0.5); color: white;
      border-radius: 50%; cursor: pointer; font-size: 14px;
      display: flex; align-items: center; justify-content: center;
      &:hover { background: #ff4d4f; }
    }
  }
}

.btn-cancel, .btn-save {
  padding: 10px 24px; border: none; border-radius: 8px; font-size: 14px; cursor: pointer;
}
.btn-cancel { background: #F0F0EF; color: #6B6560; margin-right: 12px; }
.btn-save { display: inline-flex; align-items: center; gap: 6px; background: #52c41a; color: white; &:disabled { opacity: 0.6; } }
</style>
