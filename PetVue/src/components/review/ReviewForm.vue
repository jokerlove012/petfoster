<script setup lang="ts">
import { ref, computed } from 'vue'
import type { ReviewRating, CreateReviewData } from '@/types/review'
import { Star, Upload, X, Eye, EyeOff } from 'lucide-vue-next'

const props = defineProps<{
  bookingId: string
  institutionName?: string
}>()

const emit = defineEmits<{
  submit: [data: CreateReviewData]
  cancel: []
}>()

// 评分维度
const ratingDimensions = [
  { key: 'overall', label: '综合评分', description: '整体服务体验' },
  { key: 'environment', label: '环境设施', description: '场地环境和设施条件' },
  { key: 'service', label: '服务质量', description: '工作人员服务态度' },
  { key: 'hygiene', label: '卫生状况', description: '清洁和卫生情况' },
  { key: 'communication', label: '沟通反馈', description: '信息沟通及时性' }
] as const

// 表单数据
const rating = ref<ReviewRating>({
  overall: 0,
  environment: 0,
  service: 0,
  hygiene: 0,
  communication: 0
})

const content = ref('')
const images = ref<string[]>([])
const isAnonymous = ref(false)
const submitting = ref(false)

// 评价内容验证
const contentMinLength = 10
const contentMaxLength = 500

const contentError = computed(() => {
  if (content.value.length === 0) return ''
  if (content.value.length < contentMinLength) {
    return `评价内容至少需要 ${contentMinLength} 个字符`
  }
  if (content.value.length > contentMaxLength) {
    return `评价内容不能超过 ${contentMaxLength} 个字符`
  }
  return ''
})

const isContentValid = computed(() => {
  return content.value.length >= contentMinLength && content.value.length <= contentMaxLength
})

// 评分验证
const isRatingValid = computed(() => {
  return Object.values(rating.value).every(v => v >= 1 && v <= 5)
})

// 表单是否有效
const isFormValid = computed(() => {
  return isRatingValid.value && isContentValid.value
})

// 设置评分
const setRating = (dimension: keyof ReviewRating, value: number) => {
  rating.value[dimension] = value
}

// 悬浮评分
const hoverRating = ref<{ dimension: string; value: number } | null>(null)

const getDisplayRating = (dimension: keyof ReviewRating) => {
  if (hoverRating.value?.dimension === dimension) {
    return hoverRating.value.value
  }
  return rating.value[dimension]
}

// 图片上传
const handleImageUpload = (event: Event) => {
  const input = event.target as HTMLInputElement
  if (!input.files) return
  
  const files = Array.from(input.files)
  files.forEach(file => {
    if (images.value.length >= 9) return
    
    const reader = new FileReader()
    reader.onload = (e) => {
      if (e.target?.result) {
        images.value.push(e.target.result as string)
      }
    }
    reader.readAsDataURL(file)
  })
  
  input.value = ''
}

const removeImage = (index: number) => {
  images.value.splice(index, 1)
}

// 提交评价
const handleSubmit = async () => {
  if (!isFormValid.value || submitting.value) return
  
  submitting.value = true
  
  try {
    const data: CreateReviewData = {
      bookingId: props.bookingId,
      rating: { ...rating.value },
      content: content.value.trim(),
      images: images.value,
      isAnonymous: isAnonymous.value
    }
    
    emit('submit', data)
  } finally {
    submitting.value = false
  }
}

// 取消
const handleCancel = () => {
  emit('cancel')
}
</script>

<template>
  <div class="review-form">
    <div class="form-header">
      <h3>发表评价</h3>
      <p v-if="institutionName">为 {{ institutionName }} 的服务评分</p>
    </div>

    <!-- 评分区域 -->
    <div class="rating-section">
      <div 
        v-for="dim in ratingDimensions"
        :key="dim.key"
        class="rating-item"
      >
        <div class="rating-label">
          <span class="label-text">{{ dim.label }}</span>
          <span class="label-desc">{{ dim.description }}</span>
        </div>
        <div class="rating-stars">
          <button
            v-for="star in 5"
            :key="star"
            type="button"
            class="star-btn"
            :class="{ active: star <= getDisplayRating(dim.key) }"
            @click="setRating(dim.key, star)"
            @mouseenter="hoverRating = { dimension: dim.key, value: star }"
            @mouseleave="hoverRating = null"
          >
            <Star :size="24" :fill="star <= getDisplayRating(dim.key) ? 'currentColor' : 'none'" />
          </button>
          <span class="rating-value" v-if="rating[dim.key] > 0">
            {{ rating[dim.key] }} 分
          </span>
        </div>
      </div>
    </div>

    <!-- 评价内容 -->
    <div class="content-section">
      <label class="section-label">评价内容</label>
      <textarea
        v-model="content"
        class="content-input"
        :class="{ error: contentError }"
        placeholder="分享您的寄养体验，帮助其他宠物主人做出选择..."
        rows="5"
        :maxlength="contentMaxLength"
      ></textarea>
      <div class="content-footer">
        <span v-if="contentError" class="error-text">{{ contentError }}</span>
        <span class="char-count" :class="{ warning: content.length > contentMaxLength * 0.9 }">
          {{ content.length }}/{{ contentMaxLength }}
        </span>
      </div>
    </div>

    <!-- 图片上传 -->
    <div class="image-section">
      <label class="section-label">上传图片（可选，最多9张）</label>
      <div class="image-grid">
        <div 
          v-for="(img, index) in images"
          :key="index"
          class="image-item"
        >
          <img :src="img" alt="评价图片" />
          <button type="button" class="remove-btn" @click="removeImage(index)">
            <X :size="16" />
          </button>
        </div>
        <label v-if="images.length < 9" class="upload-btn">
          <Upload :size="24" />
          <span>上传图片</span>
          <input 
            type="file" 
            accept="image/*" 
            multiple 
            @change="handleImageUpload"
            hidden
          />
        </label>
      </div>
    </div>

    <!-- 匿名选项 -->
    <div class="anonymous-section">
      <label class="anonymous-toggle">
        <input type="checkbox" v-model="isAnonymous" />
        <span class="toggle-icon">
          <EyeOff v-if="isAnonymous" :size="18" />
          <Eye v-else :size="18" />
        </span>
        <span class="toggle-text">
          {{ isAnonymous ? '匿名评价' : '公开评价' }}
        </span>
      </label>
      <p class="anonymous-hint">
        {{ isAnonymous ? '您的用户名将不会显示在评价中' : '您的用户名将显示在评价中' }}
      </p>
    </div>

    <!-- 操作按钮 -->
    <div class="form-actions">
      <button type="button" class="btn-cancel" @click="handleCancel">
        取消
      </button>
      <button 
        type="button" 
        class="btn-submit"
        :disabled="!isFormValid || submitting"
        @click="handleSubmit"
      >
        {{ submitting ? '提交中...' : '提交评价' }}
      </button>
    </div>
  </div>
</template>

<style lang="scss" scoped>
@import '@/styles/variables.scss';

.review-form {
  background: var(--color-surface);
  border-radius: var(--radius-xl);
  padding: 24px;
}

.form-header {
  margin-bottom: 24px;
  
  h3 {
    font-size: 20px;
    font-weight: 600;
    margin: 0 0 4px;
  }
  
  p {
    font-size: 14px;
    color: var(--color-text-secondary);
    margin: 0;
  }
}

// 评分区域
.rating-section {
  display: flex;
  flex-direction: column;
  gap: 16px;
  margin-bottom: 24px;
}

.rating-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  
  @media (max-width: 480px) {
    flex-direction: column;
    align-items: flex-start;
  }
}

.rating-label {
  display: flex;
  flex-direction: column;
  gap: 2px;
  
  .label-text {
    font-size: 14px;
    font-weight: 500;
  }
  
  .label-desc {
    font-size: 12px;
    color: var(--color-text-secondary);
  }
}

.rating-stars {
  display: flex;
  align-items: center;
  gap: 4px;
}

.star-btn {
  background: none;
  border: none;
  padding: 4px;
  cursor: pointer;
  color: var(--color-border);
  transition: all 0.15s;
  
  &:hover,
  &.active {
    color: #FFB800;
    transform: scale(1.1);
  }
}

.rating-value {
  font-size: 14px;
  font-weight: 500;
  color: var(--color-primary);
  margin-left: 8px;
  min-width: 40px;
}

// 评价内容
.content-section {
  margin-bottom: 24px;
}

.section-label {
  display: block;
  font-size: 14px;
  font-weight: 500;
  margin-bottom: 8px;
}

.content-input {
  width: 100%;
  padding: 12px 16px;
  border: 1px solid var(--color-border);
  border-radius: var(--radius-lg);
  font-size: 14px;
  line-height: 1.6;
  resize: vertical;
  transition: border-color 0.2s;
  
  &:focus {
    outline: none;
    border-color: var(--color-primary);
  }
  
  &.error {
    border-color: var(--color-error);
  }
  
  &::placeholder {
    color: var(--color-text-tertiary);
  }
}

.content-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: 8px;
}

.error-text {
  font-size: 12px;
  color: var(--color-error);
}

.char-count {
  font-size: 12px;
  color: var(--color-text-secondary);
  
  &.warning {
    color: var(--color-warning);
  }
}

// 图片上传
.image-section {
  margin-bottom: 24px;
}

.image-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(100px, 1fr));
  gap: 12px;
}

.image-item {
  position: relative;
  aspect-ratio: 1;
  border-radius: var(--radius-md);
  overflow: hidden;
  
  img {
    width: 100%;
    height: 100%;
    object-fit: cover;
  }
  
  .remove-btn {
    position: absolute;
    top: 4px;
    right: 4px;
    width: 24px;
    height: 24px;
    background: rgba(0, 0, 0, 0.6);
    border: none;
    border-radius: 50%;
    color: white;
    cursor: pointer;
    display: flex;
    align-items: center;
    justify-content: center;
    
    &:hover {
      background: rgba(0, 0, 0, 0.8);
    }
  }
}

.upload-btn {
  aspect-ratio: 1;
  border: 2px dashed var(--color-border);
  border-radius: var(--radius-md);
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 8px;
  cursor: pointer;
  color: var(--color-text-secondary);
  transition: all 0.2s;
  
  &:hover {
    border-color: var(--color-primary);
    color: var(--color-primary);
  }
  
  span {
    font-size: 12px;
  }
}

// 匿名选项
.anonymous-section {
  margin-bottom: 24px;
  padding: 16px;
  background: var(--color-background);
  border-radius: var(--radius-lg);
}

.anonymous-toggle {
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
  
  input {
    display: none;
  }
  
  .toggle-icon {
    color: var(--color-text-secondary);
  }
  
  .toggle-text {
    font-size: 14px;
    font-weight: 500;
  }
}

.anonymous-hint {
  font-size: 12px;
  color: var(--color-text-secondary);
  margin: 8px 0 0;
}

// 操作按钮
.form-actions {
  display: flex;
  gap: 12px;
  justify-content: flex-end;
}

.btn-cancel {
  padding: 12px 24px;
  background: var(--color-background);
  border: 1px solid var(--color-border);
  border-radius: var(--radius-lg);
  font-size: 14px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.2s;
  
  &:hover {
    background: var(--color-border);
  }
}

.btn-submit {
  padding: 12px 32px;
  background: var(--color-primary);
  border: none;
  border-radius: var(--radius-lg);
  color: white;
  font-size: 14px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.2s;
  
  &:hover:not(:disabled) {
    background: var(--color-primary-dark);
  }
  
  &:disabled {
    opacity: 0.5;
    cursor: not-allowed;
  }
}
</style>
