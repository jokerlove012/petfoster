<script setup lang="ts">
import { ref, computed, watch, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore, useBookingStore } from '@/stores'
import type { ServicePackage } from '@/types/institution'
import type { Pet } from '@/types/user'
import type { CreateBookingData, EmergencyContact } from '@/types/booking'
import { AppButton, AppInput } from '@/components/common'
import BookingCalendar from './BookingCalendar.vue'

const props = defineProps<{
  institutionId: string
  institutionName: string
  servicePackage: ServicePackage
  prefillData?: {
    petId?: string
    emergencyContact?: { name: string; phone: string; relationship: string }
    specialRequirements?: string
  }
}>()

const emit = defineEmits<{
  (e: 'submit', data: CreateBookingData): void
  (e: 'cancel'): void
}>()

const router = useRouter()
const authStore = useAuthStore()
const bookingStore = useBookingStore()

// 表单步骤
const currentStep = ref(1)
const totalSteps = 3

// 日期选择 - 支持 Date 对象或字符串
const selectedDates = ref<{ start: Date | string | null; end: Date | string | null }>({
  start: null,
  end: null
})

// 宠物选择
const selectedPetId = ref('')
const userPets = computed(() => {
  const user = authStore.user as any
  return user?.pets || []
})

// 紧急联系人
const emergencyContact = ref<EmergencyContact>({
  name: '',
  phone: '',
  relationship: ''
})

// 特殊需求
const specialRequirements = ref('')

// 表单验证
const errors = ref<Record<string, string>>({})

// 计算天数和价格
const totalDays = computed(() => {
  if (!selectedDates.value.start || !selectedDates.value.end) return 0
  const start = new Date(selectedDates.value.start)
  const end = new Date(selectedDates.value.end)
  const diffTime = Math.abs(end.getTime() - start.getTime())
  return Math.ceil(diffTime / (1000 * 60 * 60 * 24)) + 1
})

const totalPrice = computed(() => {
  return totalDays.value * props.servicePackage.pricePerDay
})

// 获取选中的宠物
const selectedPet = computed(() => {
  return userPets.value.find((p: Pet) => p.id === selectedPetId.value)
})

// 步骤验证
const isStep1Valid = computed(() => {
  return selectedDates.value.start && selectedDates.value.end && totalDays.value > 0
})

const isStep2Valid = computed(() => {
  return selectedPetId.value !== ''
})

const isStep3Valid = computed(() => {
  return (
    emergencyContact.value.name.trim() !== '' &&
    emergencyContact.value.phone.trim() !== '' &&
    emergencyContact.value.relationship.trim() !== '' &&
    /^1[3-9]\d{9}$/.test(emergencyContact.value.phone)
  )
})

const canProceed = computed(() => {
  switch (currentStep.value) {
    case 1: return isStep1Valid.value
    case 2: return isStep2Valid.value
    case 3: return isStep3Valid.value
    default: return false
  }
})

// 步骤导航
const nextStep = () => {
  if (currentStep.value < totalSteps && canProceed.value) {
    currentStep.value++
  }
}

const prevStep = () => {
  if (currentStep.value > 1) {
    currentStep.value--
  }
}

// 日期选择处理
const handleDateSelect = (dates: { start: Date; end: Date }) => {
  selectedDates.value = dates
}

// 验证表单
const validateForm = (): boolean => {
  errors.value = {}
  
  if (!selectedDates.value.start || !selectedDates.value.end) {
    errors.value.dates = '请选择寄养日期'
  }
  
  if (!selectedPetId.value) {
    errors.value.pet = '请选择宠物'
  }
  
  if (!emergencyContact.value.name.trim()) {
    errors.value.contactName = '请输入紧急联系人姓名'
  }
  
  if (!emergencyContact.value.phone.trim()) {
    errors.value.contactPhone = '请输入紧急联系人电话'
  } else if (!/^1[3-9]\d{9}$/.test(emergencyContact.value.phone)) {
    errors.value.contactPhone = '请输入有效的手机号码'
  }
  
  if (!emergencyContact.value.relationship.trim()) {
    errors.value.contactRelation = '请输入与宠主的关系'
  }
  
  return Object.keys(errors.value).length === 0
}

// 提交预约
const handleSubmit = () => {
  if (!validateForm()) return
  
  const startDate = typeof selectedDates.value.start === 'string' 
    ? selectedDates.value.start 
    : selectedDates.value.start!.toISOString().split('T')[0]
  const endDate = typeof selectedDates.value.end === 'string'
    ? selectedDates.value.end
    : selectedDates.value.end!.toISOString().split('T')[0]
    
  const bookingData: CreateBookingData = {
    institutionId: props.institutionId,
    servicePackageId: props.servicePackage.id,
    petId: selectedPetId.value,
    startDate,
    endDate,
    specialRequirements: specialRequirements.value || undefined,
    emergencyContact: emergencyContact.value
  }
  
  emit('submit', bookingData)
}

// 格式化日期显示
const formatDate = (date: Date | string | null) => {
  if (!date) return '未选择'
  const d = typeof date === 'string' ? new Date(date) : date
  return d.toLocaleDateString('zh-CN', {
    year: 'numeric',
    month: 'long',
    day: 'numeric'
  })
}

// 格式化价格
const formatPrice = (price: number) => {
  return `¥${price.toFixed(2)}`
}

// 初始化预填充数据
onMounted(() => {
  if (props.prefillData) {
    if (props.prefillData.petId) {
      selectedPetId.value = props.prefillData.petId
    }
    if (props.prefillData.emergencyContact) {
      emergencyContact.value = { ...props.prefillData.emergencyContact }
    }
    if (props.prefillData.specialRequirements) {
      specialRequirements.value = props.prefillData.specialRequirements
    }
  }
})
</script>

<template>
  <div class="booking-form">
    <!-- 步骤指示器 -->
    <div class="step-indicator">
      <div 
        v-for="step in totalSteps" 
        :key="step"
        class="step"
        :class="{ 
          active: currentStep === step,
          completed: currentStep > step 
        }"
      >
        <div class="step-number">
          <span v-if="currentStep > step">✓</span>
          <span v-else>{{ step }}</span>
        </div>
        <span class="step-label">
          {{ step === 1 ? '选择日期' : step === 2 ? '选择宠物' : '确认信息' }}
        </span>
      </div>
      <div class="step-line"></div>
    </div>

    <!-- 服务套餐信息 -->
    <div class="package-summary">
      <div class="package-header">
        <h3>{{ servicePackage.name }}</h3>
        <span class="package-price">{{ formatPrice(servicePackage.pricePerDay) }}/天</span>
      </div>
      <p class="package-desc">{{ servicePackage.description }}</p>
      <div class="package-features">
        <span v-for="feature in servicePackage.features" :key="feature" class="feature-tag">
          {{ feature }}
        </span>
      </div>
    </div>

    <!-- 步骤内容 -->
    <div class="step-content">
      <!-- 步骤1: 选择日期 -->
      <div v-if="currentStep === 1" class="step-panel">
        <h4>选择寄养日期</h4>
        <p class="step-hint">请选择入住和离店日期</p>
        
        <BookingCalendar 
          :institution-id="institutionId"
          @select="handleDateSelect"
        />
        
        <div v-if="selectedDates.start && selectedDates.end" class="date-summary">
          <div class="date-item">
            <span class="date-label">入住日期</span>
            <span class="date-value">{{ formatDate(selectedDates.start) }}</span>
          </div>
          <div class="date-arrow">→</div>
          <div class="date-item">
            <span class="date-label">离店日期</span>
            <span class="date-value">{{ formatDate(selectedDates.end) }}</span>
          </div>
        </div>
        
        <div v-if="errors.dates" class="error-message">{{ errors.dates }}</div>
      </div>

      <!-- 步骤2: 选择宠物 -->
      <div v-if="currentStep === 2" class="step-panel">
        <h4>选择寄养宠物</h4>
        <p class="step-hint">请选择需要寄养的宠物</p>
        
        <div v-if="userPets.length === 0" class="empty-pets">
          <div class="empty-icon">🐾</div>
          <p>您还没有添加宠物信息</p>
          <p class="empty-hint">预约时需要选择宠物，请先添加宠物信息</p>
        </div>
        
        <div v-else class="pet-list">
          <div 
            v-for="pet in userPets" 
            :key="pet.id"
            class="pet-card"
            :class="{ selected: selectedPetId === pet.id }"
            @click="selectedPetId = pet.id"
          >
            <div class="pet-avatar">
              {{ pet.species === 'dog' ? '🐕' : pet.species === 'cat' ? '🐱' : '🐾' }}
            </div>
            <div class="pet-info">
              <span class="pet-name">{{ pet.name }}</span>
              <span class="pet-details">
                {{ pet.breed }} · {{ pet.age }}岁 · {{ pet.weight }}kg
              </span>
            </div>
            <div class="pet-check">
              <span v-if="selectedPetId === pet.id">✓</span>
            </div>
          </div>
        </div>
        
        <div v-if="errors.pet" class="error-message">{{ errors.pet }}</div>
      </div>

      <!-- 步骤3: 确认信息 -->
      <div v-if="currentStep === 3" class="step-panel">
        <h4>确认预约信息</h4>
        <p class="step-hint">请填写紧急联系人信息并确认预约</p>
        
        <!-- 紧急联系人 -->
        <div class="form-section">
          <h5>紧急联系人</h5>
          <div class="form-row">
            <div class="form-group">
              <label>姓名 <span class="required">*</span></label>
              <AppInput 
                v-model="emergencyContact.name" 
                placeholder="请输入联系人姓名"
                :error="errors.contactName"
              />
            </div>
            <div class="form-group">
              <label>电话 <span class="required">*</span></label>
              <AppInput 
                v-model="emergencyContact.phone" 
                placeholder="请输入手机号码"
                :error="errors.contactPhone"
              />
            </div>
          </div>
          <div class="form-group">
            <label>与宠主关系 <span class="required">*</span></label>
            <AppInput 
              v-model="emergencyContact.relationship" 
              placeholder="如：本人、配偶、父母、朋友"
              :error="errors.contactRelation"
            />
          </div>
        </div>
        
        <!-- 特殊需求 -->
        <div class="form-section">
          <h5>特殊需求（选填）</h5>
          <textarea 
            v-model="specialRequirements"
            class="special-requirements"
            placeholder="请填写宠物的特殊饮食、用药、习惯等需求..."
            rows="3"
          ></textarea>
        </div>
        
        <!-- 预约摘要 -->
        <div class="booking-summary">
          <h5>预约摘要</h5>
          <div class="summary-item">
            <span class="summary-label">寄养机构</span>
            <span class="summary-value">{{ institutionName }}</span>
          </div>
          <div class="summary-item">
            <span class="summary-label">服务套餐</span>
            <span class="summary-value">{{ servicePackage.name }}</span>
          </div>
          <div class="summary-item">
            <span class="summary-label">寄养宠物</span>
            <span class="summary-value">{{ selectedPet?.name || '-' }}</span>
          </div>
          <div class="summary-item">
            <span class="summary-label">入住日期</span>
            <span class="summary-value">{{ formatDate(selectedDates.start) }}</span>
          </div>
          <div class="summary-item">
            <span class="summary-label">离店日期</span>
            <span class="summary-value">{{ formatDate(selectedDates.end) }}</span>
          </div>
          <div class="summary-item">
            <span class="summary-label">寄养天数</span>
            <span class="summary-value">{{ totalDays }} 天</span>
          </div>
          <div class="summary-divider"></div>
          <div class="summary-item total">
            <span class="summary-label">预计总价</span>
            <span class="summary-value price">{{ formatPrice(totalPrice) }}</span>
          </div>
        </div>
      </div>
    </div>

    <!-- 底部操作栏 -->
    <div class="form-actions">
      <div class="price-display" v-if="totalDays > 0">
        <span class="price-label">预计费用</span>
        <span class="price-value">{{ formatPrice(totalPrice) }}</span>
        <span class="price-detail">{{ totalDays }}天 × {{ formatPrice(servicePackage.pricePerDay) }}</span>
      </div>
      
      <div class="action-buttons">
        <AppButton 
          v-if="currentStep > 1" 
          type="outline" 
          @click="prevStep"
        >
          上一步
        </AppButton>
        <AppButton 
          v-if="currentStep < totalSteps" 
          type="primary" 
          :disabled="!canProceed"
          @click="nextStep"
        >
          下一步
        </AppButton>
        <AppButton 
          v-if="currentStep === totalSteps" 
          type="primary"
          :disabled="!canProceed"
          @click="handleSubmit"
        >
          提交预约
        </AppButton>
      </div>
    </div>
  </div>
</template>


<style lang="scss" scoped>
@import '@/styles/variables.scss';

.booking-form {
  background: var(--color-surface);
  border-radius: var(--radius-xl);
  overflow: hidden;
}

// 步骤指示器
.step-indicator {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 24px 32px;
  background: linear-gradient(135deg, var(--color-primary-light), var(--color-accent-light));
  position: relative;
}

.step {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
  z-index: 1;
  
  .step-number {
    width: 36px;
    height: 36px;
    border-radius: 50%;
    background: white;
    color: var(--color-text-secondary);
    display: flex;
    align-items: center;
    justify-content: center;
    font-weight: 600;
    font-size: 14px;
    transition: all 0.3s ease;
  }
  
  .step-label {
    font-size: 13px;
    color: var(--color-text-secondary);
    font-weight: 500;
  }
  
  &.active {
    .step-number {
      background: var(--color-primary);
      color: white;
      box-shadow: 0 4px 12px rgba(255, 107, 53, 0.3);
    }
    
    .step-label {
      color: var(--color-primary);
    }
  }
  
  &.completed {
    .step-number {
      background: var(--color-success);
      color: white;
    }
    
    .step-label {
      color: var(--color-success);
    }
  }
}

.step-line {
  position: absolute;
  top: 42px;
  left: 15%;
  right: 15%;
  height: 2px;
  background: rgba(255, 255, 255, 0.5);
}

// 套餐摘要
.package-summary {
  padding: 20px 24px;
  background: var(--color-surface);
  border-bottom: 1px solid var(--color-border);
}

.package-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
  
  h3 {
    font-size: 18px;
    font-weight: 600;
    margin: 0;
  }
  
  .package-price {
    font-size: 18px;
    font-weight: 700;
    color: var(--color-primary);
  }
}

.package-desc {
  font-size: 14px;
  color: var(--color-text-secondary);
  margin: 0 0 12px;
}

.package-features {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.feature-tag {
  padding: 4px 10px;
  background: var(--color-primary-light);
  color: var(--color-primary);
  font-size: 12px;
  border-radius: 12px;
}

// 步骤内容
.step-content {
  padding: 24px;
  min-height: 400px;
}

.step-panel {
  h4 {
    font-size: 20px;
    font-weight: 600;
    margin: 0 0 8px;
  }
  
  .step-hint {
    font-size: 14px;
    color: var(--color-text-secondary);
    margin: 0 0 24px;
  }
}

// 日期摘要
.date-summary {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 24px;
  padding: 20px;
  background: var(--color-surface);
  border: 1px solid var(--color-border);
  border-radius: var(--radius-lg);
  margin-top: 20px;
}

.date-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 4px;
  
  .date-label {
    font-size: 12px;
    color: var(--color-text-secondary);
  }
  
  .date-value {
    font-size: 16px;
    font-weight: 600;
    color: var(--color-text-primary);
  }
}

.date-arrow {
  font-size: 20px;
  color: var(--color-primary);
}

// 宠物列表
.empty-pets {
  text-align: center;
  padding: 48px 24px;
  
  .empty-icon {
    font-size: 48px;
    margin-bottom: 16px;
  }
  
  p {
    color: var(--color-text-secondary);
    margin: 0 0 16px;
  }
}

.pet-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.pet-card {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 16px;
  background: var(--color-surface);
  border: 2px solid var(--color-border);
  border-radius: var(--radius-lg);
  cursor: pointer;
  transition: all 0.2s ease;
  
  &:hover {
    border-color: var(--color-primary-light);
  }
  
  &.selected {
    border-color: var(--color-primary);
    background: var(--color-primary-light);
  }
}

.pet-avatar {
  width: 56px;
  height: 56px;
  background: linear-gradient(135deg, var(--color-primary-light), var(--color-accent-light));
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 28px;
}

.pet-info {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 4px;
  
  .pet-name {
    font-size: 16px;
    font-weight: 600;
  }
  
  .pet-details {
    font-size: 13px;
    color: var(--color-text-secondary);
  }
}

.pet-check {
  width: 24px;
  height: 24px;
  border-radius: 50%;
  background: var(--color-primary);
  color: white;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 14px;
  opacity: 0;
  
  .pet-card.selected & {
    opacity: 1;
  }
}

// 表单区域
.form-section {
  margin-bottom: 24px;
  
  h5 {
    font-size: 16px;
    font-weight: 600;
    margin: 0 0 16px;
    color: var(--color-text-primary);
  }
}

.form-row {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 16px;
  
  @media (max-width: 640px) {
    grid-template-columns: 1fr;
  }
}

.form-group {
  margin-bottom: 16px;
  
  label {
    display: block;
    font-size: 14px;
    font-weight: 500;
    color: var(--color-text-secondary);
    margin-bottom: 8px;
    
    .required {
      color: var(--color-error);
    }
  }
}

.special-requirements {
  width: 100%;
  padding: 12px 16px;
  border: 2px solid var(--color-border);
  border-radius: var(--radius-md);
  font-size: 14px;
  font-family: inherit;
  resize: vertical;
  transition: border-color 0.2s;
  
  &:focus {
    outline: none;
    border-color: var(--color-primary);
  }
  
  &::placeholder {
    color: var(--color-text-tertiary);
  }
}

// 预约摘要
.booking-summary {
  background: var(--color-surface);
  border: 1px solid var(--color-border);
  border-radius: var(--radius-lg);
  padding: 20px;
  
  h5 {
    font-size: 16px;
    font-weight: 600;
    margin: 0 0 16px;
  }
}

.summary-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 8px 0;
  
  .summary-label {
    font-size: 14px;
    color: var(--color-text-secondary);
  }
  
  .summary-value {
    font-size: 14px;
    font-weight: 500;
    color: var(--color-text-primary);
  }
  
  &.total {
    padding-top: 16px;
    
    .summary-label {
      font-size: 16px;
      font-weight: 600;
      color: var(--color-text-primary);
    }
    
    .summary-value.price {
      font-size: 24px;
      font-weight: 700;
      color: var(--color-primary);
    }
  }
}

.summary-divider {
  height: 1px;
  background: var(--color-border);
  margin: 12px 0;
}

// 错误消息
.error-message {
  color: var(--color-error);
  font-size: 13px;
  margin-top: 8px;
  display: flex;
  align-items: center;
  gap: 4px;
  
  &::before {
    content: '⚠';
  }
}

// 底部操作栏
.form-actions {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20px 24px;
  background: var(--color-surface);
  border-top: 1px solid var(--color-border);
  
  @media (max-width: 640px) {
    flex-direction: column;
    gap: 16px;
  }
}

.price-display {
  display: flex;
  flex-direction: column;
  
  .price-label {
    font-size: 12px;
    color: var(--color-text-secondary);
  }
  
  .price-value {
    font-size: 24px;
    font-weight: 700;
    color: var(--color-primary);
  }
  
  .price-detail {
    font-size: 12px;
    color: var(--color-text-tertiary);
  }
}

.action-buttons {
  display: flex;
  gap: 12px;
}
</style>
