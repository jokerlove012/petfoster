<script setup lang="ts">
import { ref, reactive, watch } from 'vue'
import { AppButton, AppInput } from '@/components/common'
import type { ServicePackage } from '@/types/institution'

const props = defineProps<{
  initialData?: ServicePackage | null
}>()

const emit = defineEmits<{
  (e: 'save', data: Partial<ServicePackage>): void
  (e: 'cancel'): void
}>()

const form = reactive({
  name: '',
  description: '',
  pricePerDay: 0,
  features: [] as string[],
  petTypes: ['dog', 'cat'] as ('dog' | 'cat' | 'other')[],
  maxWeight: undefined as number | undefined
})

const newFeature = ref('')
const errors = ref<Record<string, string>>({})

// 初始化表单数据
watch(() => props.initialData, (data) => {
  if (data) {
    form.name = data.name
    form.description = data.description
    form.pricePerDay = data.pricePerDay
    form.features = [...data.features]
    form.petTypes = [...data.petTypes]
    form.maxWeight = data.maxWeight
  } else {
    form.name = ''
    form.description = ''
    form.pricePerDay = 0
    form.features = []
    form.petTypes = ['dog', 'cat']
    form.maxWeight = undefined
  }
}, { immediate: true })

const addFeature = () => {
  const feature = newFeature.value.trim()
  if (!feature) {
    return // 输入为空时不做任何操作
  }
  if (form.features.includes(feature)) {
    newFeature.value = ''
    return // 已存在的特色不重复添加
  }
  form.features.push(feature)
  newFeature.value = ''
}

const removeFeature = (index: number) => {
  form.features.splice(index, 1)
}

const togglePetType = (type: 'dog' | 'cat' | 'other') => {
  const index = form.petTypes.indexOf(type)
  if (index === -1) {
    form.petTypes.push(type)
  } else if (form.petTypes.length > 1) {
    form.petTypes.splice(index, 1)
  }
}

const validate = (): boolean => {
  errors.value = {}
  
  if (!form.name.trim()) {
    errors.value.name = '请输入套餐名称'
  }
  
  if (!form.description.trim()) {
    errors.value.description = '请输入套餐描述'
  }
  
  if (form.pricePerDay <= 0) {
    errors.value.pricePerDay = '请输入有效的价格'
  }
  
  if (form.features.length === 0) {
    errors.value.features = '请至少添加一项服务特色'
  }
  
  return Object.keys(errors.value).length === 0
}

const handleSubmit = () => {
  if (!validate()) return
  
  emit('save', {
    name: form.name,
    description: form.description,
    pricePerDay: form.pricePerDay,
    features: form.features,
    petTypes: form.petTypes,
    maxWeight: form.maxWeight
  })
}
</script>

<template>
  <div class="package-form">
    <div class="form-group">
      <label>套餐名称 <span class="required">*</span></label>
      <AppInput 
        v-model="form.name" 
        placeholder="如：标准寄养、VIP寄养"
        :error="errors.name"
      />
    </div>

    <div class="form-group">
      <label>套餐描述 <span class="required">*</span></label>
      <textarea 
        v-model="form.description"
        class="textarea"
        placeholder="简要描述套餐特点..."
        rows="2"
      ></textarea>
      <span v-if="errors.description" class="error-text">{{ errors.description }}</span>
    </div>

    <div class="form-group">
      <label>每日价格 <span class="required">*</span></label>
      <div class="price-input">
        <span class="price-prefix">¥</span>
        <input 
          v-model.number="form.pricePerDay"
          type="number"
          min="0"
          class="price-field"
          placeholder="0"
        />
        <span class="price-suffix">/天</span>
      </div>
      <span v-if="errors.pricePerDay" class="error-text">{{ errors.pricePerDay }}</span>
    </div>

    <div class="form-group">
      <label>服务特色 <span class="required">*</span></label>
      <div class="features-input">
        <input 
          v-model="newFeature"
          type="text"
          class="feature-field"
          placeholder="输入特色服务，按回车添加"
          @keyup.enter="addFeature"
        />
        <button type="button" class="add-feature-btn" @click="addFeature">添加</button>
      </div>
      <div class="features-list">
        <span 
          v-for="(feature, index) in form.features" 
          :key="index"
          class="feature-tag"
        >
          {{ feature }}
          <button class="remove-btn" @click="removeFeature(index)">×</button>
        </span>
      </div>
      <span v-if="errors.features" class="error-text">{{ errors.features }}</span>
    </div>

    <div class="form-group">
      <label>接受宠物类型</label>
      <div class="pet-type-options">
        <button 
          type="button"
          class="pet-type-btn"
          :class="{ active: form.petTypes.includes('dog') }"
          @click="togglePetType('dog')"
        >
          🐕 狗狗
        </button>
        <button 
          type="button"
          class="pet-type-btn"
          :class="{ active: form.petTypes.includes('cat') }"
          @click="togglePetType('cat')"
        >
          🐱 猫咪
        </button>
        <button 
          type="button"
          class="pet-type-btn"
          :class="{ active: form.petTypes.includes('other') }"
          @click="togglePetType('other')"
        >
          🐾 其他
        </button>
      </div>
    </div>

    <div class="form-group">
      <label>最大体重限制（可选）</label>
      <div class="weight-input">
        <input 
          v-model.number="form.maxWeight"
          type="number"
          min="0"
          class="weight-field"
          placeholder="不限制"
        />
        <span class="weight-suffix">kg</span>
      </div>
    </div>

    <div class="form-actions">
      <AppButton type="ghost" @click="$emit('cancel')">取消</AppButton>
      <AppButton type="primary" @click="handleSubmit">保存</AppButton>
    </div>
  </div>
</template>


<style lang="scss" scoped>
.package-form {
  padding: 8px 0;
}

.form-group {
  margin-bottom: 20px;

  label {
    display: block;
    font-size: 14px;
    font-weight: 500;
    color: var(--color-text-primary);
    margin-bottom: 8px;

    .required {
      color: var(--color-error);
    }
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
  transition: border-color 200ms ease;

  &:focus {
    outline: none;
    border-color: var(--color-primary);
  }
}

.price-input,
.weight-input {
  display: flex;
  align-items: center;
  border: 2px solid var(--color-border);
  border-radius: var(--radius-md);
  overflow: hidden;
  transition: border-color 200ms ease;

  &:focus-within {
    border-color: var(--color-primary);
  }
}

.price-prefix,
.price-suffix,
.weight-suffix {
  padding: 12px;
  background: var(--color-neutral-100);
  color: var(--color-text-secondary);
  font-size: 14px;
}

.price-field,
.weight-field {
  flex: 1;
  padding: 12px;
  border: none;
  font-size: 16px;
  font-weight: 600;
  outline: none;

  &::-webkit-inner-spin-button,
  &::-webkit-outer-spin-button {
    -webkit-appearance: none;
    margin: 0;
  }
}

.features-input {
  display: flex;
  gap: 8px;
  margin-bottom: 12px;

  .feature-field {
    flex: 1;
    padding: 10px 12px;
    border: 2px solid var(--color-border);
    border-radius: var(--radius-md);
    font-size: 14px;
    outline: none;

    &:focus {
      border-color: var(--color-primary);
    }
  }
}

.features-list {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  min-height: 32px;
}

.feature-tag {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 6px 12px;
  background: var(--color-primary-light);
  color: var(--color-primary);
  font-size: 13px;
  border-radius: var(--radius-full);

  .remove-btn {
    display: flex;
    align-items: center;
    justify-content: center;
    width: 16px;
    height: 16px;
    border: none;
    background: var(--color-primary);
    color: white;
    border-radius: 50%;
    font-size: 12px;
    cursor: pointer;
    line-height: 1;

    &:hover {
      background: var(--color-primary-dark, #e55a2b);
    }
  }
}

.add-feature-btn {
  padding: 8px 16px;
  border: 2px solid var(--color-primary);
  background: transparent;
  color: var(--color-primary);
  border-radius: var(--radius-md);
  font-size: 14px;
  font-weight: 500;
  cursor: pointer;
  transition: all 200ms ease;
  white-space: nowrap;

  &:hover {
    background: var(--color-primary-light);
  }
}

.pet-type-options {
  display: flex;
  gap: 12px;
}

.pet-type-btn {
  padding: 10px 16px;
  border: 2px solid var(--color-border);
  background: white;
  border-radius: var(--radius-md);
  font-size: 14px;
  cursor: pointer;
  transition: all 200ms ease;

  &:hover {
    border-color: var(--color-primary-light);
  }

  &.active {
    border-color: var(--color-primary);
    background: var(--color-primary-light);
    color: var(--color-primary);
  }
}

.error-text {
  display: block;
  margin-top: 6px;
  font-size: 13px;
  color: var(--color-error);
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
