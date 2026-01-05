<script setup lang="ts">
import { ref, reactive } from 'vue'
import { ElMessage } from 'element-plus'
import { AppButton, AppInput } from '@/components/common'

const emit = defineEmits<{
  (e: 'submit', data: any): void
  (e: 'cancel'): void
}>()

const form = reactive({
  date: new Date().toISOString().split('T')[0],
  feeding: {
    breakfast: true,
    lunch: true,
    dinner: true,
    snacks: '',
    appetite: 'normal' as 'good' | 'normal' | 'poor'
  },
  activity: {
    walkDuration: 30,
    playTime: 20,
    mood: 'happy' as 'happy' | 'normal' | 'tired' | 'anxious'
  },
  health: {
    temperature: '',
    weight: '',
    abnormal: false,
    abnormalNote: '',
    medication: ''
  },
  notes: '',
  images: [] as string[]
})

const appetiteOptions = [
  { value: 'good', label: '食欲很好', icon: '😋' },
  { value: 'normal', label: '正常', icon: '😊' },
  { value: 'poor', label: '食欲不佳', icon: '😔' }
]

const moodOptions = [
  { value: 'happy', label: '开心活泼', icon: '🥳' },
  { value: 'normal', label: '正常', icon: '😊' },
  { value: 'tired', label: '疲惫', icon: '😴' },
  { value: 'anxious', label: '焦虑', icon: '😰' }
]

const handleSubmit = () => {
  if (form.health.abnormal && !form.health.abnormalNote) {
    ElMessage.warning('请填写异常情况说明')
    return
  }
  
  emit('submit', { ...form })
  ElMessage.success('健康记录已保存')
}

const handleImageUpload = () => {
  // TODO: 对接真实文件上传API
  // 使用 el-upload 组件或调用后端上传接口
  ElMessage.info('请使用文件选择器上传图片')
}

const removeImage = (index: number) => {
  form.images.splice(index, 1)
}
</script>

<template>
  <div class="health-record-form">
    <div class="form-header">
      <h3>记录健康状况</h3>
      <p>{{ form.date }}</p>
    </div>

    <!-- 喂食记录 -->
    <div class="form-section">
      <h4>🍽️ 喂食记录</h4>
      <div class="meal-checks">
        <label class="checkbox-item">
          <input type="checkbox" v-model="form.feeding.breakfast" />
          <span>早餐</span>
        </label>
        <label class="checkbox-item">
          <input type="checkbox" v-model="form.feeding.lunch" />
          <span>午餐</span>
        </label>
        <label class="checkbox-item">
          <input type="checkbox" v-model="form.feeding.dinner" />
          <span>晚餐</span>
        </label>
      </div>
      
      <div class="form-group">
        <label>食欲状况</label>
        <div class="option-buttons">
          <button
            v-for="opt in appetiteOptions"
            :key="opt.value"
            type="button"
            class="option-btn"
            :class="{ active: form.feeding.appetite === opt.value }"
            @click="form.feeding.appetite = opt.value as any"
          >
            <span class="opt-icon">{{ opt.icon }}</span>
            <span>{{ opt.label }}</span>
          </button>
        </div>
      </div>

      <div class="form-group">
        <label>零食/加餐</label>
        <AppInput v-model="form.feeding.snacks" placeholder="如有额外喂食请记录" />
      </div>
    </div>

    <!-- 活动记录 -->
    <div class="form-section">
      <h4>🏃 活动记录</h4>
      <div class="form-row">
        <div class="form-group">
          <label>散步时长（分钟）</label>
          <input type="number" v-model.number="form.activity.walkDuration" class="number-input" min="0" />
        </div>
        <div class="form-group">
          <label>玩耍时长（分钟）</label>
          <input type="number" v-model.number="form.activity.playTime" class="number-input" min="0" />
        </div>
      </div>

      <div class="form-group">
        <label>精神状态</label>
        <div class="option-buttons">
          <button
            v-for="opt in moodOptions"
            :key="opt.value"
            type="button"
            class="option-btn"
            :class="{ active: form.activity.mood === opt.value }"
            @click="form.activity.mood = opt.value as any"
          >
            <span class="opt-icon">{{ opt.icon }}</span>
            <span>{{ opt.label }}</span>
          </button>
        </div>
      </div>
    </div>

    <!-- 健康观察 -->
    <div class="form-section">
      <h4>🏥 健康观察</h4>
      <div class="form-row">
        <div class="form-group">
          <label>体温（°C）</label>
          <AppInput v-model="form.health.temperature" placeholder="如：38.5" />
        </div>
        <div class="form-group">
          <label>体重（kg）</label>
          <AppInput v-model="form.health.weight" placeholder="如：12.5" />
        </div>
      </div>

      <div class="form-group">
        <label class="checkbox-item abnormal-check">
          <input type="checkbox" v-model="form.health.abnormal" />
          <span>发现异常情况</span>
        </label>
        <textarea
          v-if="form.health.abnormal"
          v-model="form.health.abnormalNote"
          class="textarea"
          placeholder="请详细描述异常情况..."
          rows="2"
        ></textarea>
      </div>

      <div class="form-group">
        <label>用药记录</label>
        <AppInput v-model="form.health.medication" placeholder="如有用药请记录" />
      </div>
    </div>

    <!-- 备注和图片 -->
    <div class="form-section">
      <h4>📝 备注</h4>
      <textarea
        v-model="form.notes"
        class="textarea"
        placeholder="其他需要记录的内容..."
        rows="2"
      ></textarea>

      <div class="image-upload">
        <div class="image-list">
          <div v-for="(img, index) in form.images" :key="index" class="image-item">
            <img :src="img" alt="记录图片" />
            <button class="remove-btn" @click="removeImage(index)">×</button>
          </div>
          <button class="upload-btn" @click="handleImageUpload">
            <span class="upload-icon">📷</span>
            <span>添加照片</span>
          </button>
        </div>
      </div>
    </div>

    <div class="form-actions">
      <AppButton type="ghost" @click="$emit('cancel')">取消</AppButton>
      <AppButton type="primary" @click="handleSubmit">保存记录</AppButton>
    </div>
  </div>
</template>


<style lang="scss" scoped>
.health-record-form {
  max-height: 70vh;
  overflow-y: auto;
}

.form-header {
  margin-bottom: 24px;
  
  h3 {
    font-size: 20px;
    margin: 0 0 4px;
  }
  
  p {
    color: var(--color-text-secondary);
    margin: 0;
  }
}

.form-section {
  margin-bottom: 24px;
  padding-bottom: 24px;
  border-bottom: 1px solid var(--color-border);
  
  &:last-of-type {
    border-bottom: none;
  }
  
  h4 {
    font-size: 16px;
    font-weight: 600;
    margin: 0 0 16px;
  }
}

.form-group {
  margin-bottom: 16px;
  
  > label {
    display: block;
    font-size: 14px;
    font-weight: 500;
    color: var(--color-text-secondary);
    margin-bottom: 8px;
  }
}

.form-row {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 16px;
}

.meal-checks {
  display: flex;
  gap: 24px;
  margin-bottom: 16px;
}

.checkbox-item {
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
  
  input[type="checkbox"] {
    width: 18px;
    height: 18px;
    accent-color: var(--color-primary);
  }
  
  span {
    font-size: 14px;
  }
  
  &.abnormal-check {
    margin-bottom: 12px;
    
    span {
      color: var(--color-error);
      font-weight: 500;
    }
  }
}

.option-buttons {
  display: flex;
  gap: 10px;
  flex-wrap: wrap;
}

.option-btn {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 10px 14px;
  border: 2px solid var(--color-border);
  background: white;
  border-radius: var(--radius-md);
  font-size: 13px;
  cursor: pointer;
  transition: all 150ms ease;
  
  .opt-icon {
    font-size: 16px;
  }
  
  &:hover {
    border-color: var(--color-primary-light);
  }
  
  &.active {
    border-color: var(--color-primary);
    background: var(--color-primary-light);
    color: var(--color-primary);
  }
}

.number-input {
  width: 100%;
  padding: 12px;
  border: 2px solid var(--color-border);
  border-radius: var(--radius-md);
  font-size: 14px;
  outline: none;
  
  &:focus {
    border-color: var(--color-primary);
  }
}

.textarea {
  width: 100%;
  padding: 12px;
  border: 2px solid var(--color-border);
  border-radius: var(--radius-md);
  font-size: 14px;
  font-family: inherit;
  resize: vertical;
  outline: none;
  
  &:focus {
    border-color: var(--color-primary);
  }
}

.image-upload {
  margin-top: 16px;
}

.image-list {
  display: flex;
  gap: 12px;
  flex-wrap: wrap;
}

.image-item {
  position: relative;
  width: 80px;
  height: 80px;
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
    width: 20px;
    height: 20px;
    border: none;
    background: rgba(0, 0, 0, 0.6);
    color: white;
    border-radius: 50%;
    font-size: 14px;
    cursor: pointer;
    display: flex;
    align-items: center;
    justify-content: center;
  }
}

.upload-btn {
  width: 80px;
  height: 80px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 4px;
  border: 2px dashed var(--color-border);
  background: transparent;
  border-radius: var(--radius-md);
  cursor: pointer;
  transition: all 150ms ease;
  
  .upload-icon {
    font-size: 24px;
  }
  
  span:last-child {
    font-size: 11px;
    color: var(--color-text-muted);
  }
  
  &:hover {
    border-color: var(--color-primary);
    background: var(--color-primary-light);
  }
}

.form-actions {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
  margin-top: 24px;
  padding-top: 20px;
  border-top: 1px solid var(--color-border);
}
</style>
