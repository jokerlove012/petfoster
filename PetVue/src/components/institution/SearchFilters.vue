<script setup lang="ts">
import { reactive, watch } from 'vue'
import type { SearchFilters, PetType } from '@/types/institution'

interface Props {
  modelValue: SearchFilters
}

const props = defineProps<Props>()
const emit = defineEmits<{
  'update:modelValue': [value: SearchFilters]
  search: []
}>()

const filters = reactive<SearchFilters>({ ...props.modelValue })

watch(() => props.modelValue, (newVal) => {
  Object.assign(filters, newVal)
}, { deep: true })

const updateFilters = () => {
  emit('update:modelValue', { ...filters })
}

const handleSearch = () => {
  updateFilters()
  emit('search')
}

const resetFilters = () => {
  filters.keyword = ''
  filters.petType = undefined
  filters.minRating = undefined
  filters.maxPrice = undefined
  filters.sortBy = 'rating'
  updateFilters()
  emit('search')
}

const petTypeOptions: { value: PetType | ''; label: string }[] = [
  { value: '', label: '全部宠物' },
  { value: 'dog', label: '🐕 狗狗' },
  { value: 'cat', label: '🐱 猫咪' },
  { value: 'other', label: '🐾 其他' }
]

const ratingOptions = [
  { value: undefined, label: '不限评分' },
  { value: 4.5, label: '4.5分以上' },
  { value: 4.0, label: '4.0分以上' },
  { value: 3.5, label: '3.5分以上' }
]

const priceOptions = [
  { value: undefined, label: '不限价格' },
  { value: 100, label: '100元以下' },
  { value: 200, label: '200元以下' },
  { value: 300, label: '300元以下' }
]

const sortOptions = [
  { value: 'rating', label: '评分最高' },
  { value: 'reviewCount', label: '评价最多' },
  { value: 'price', label: '价格最低' },
  { value: 'distance', label: '距离最近' }
]
</script>

<template>
  <div class="search-filters">
    <div class="search-bar">
      <el-input
        v-model="filters.keyword"
        placeholder="搜索机构名称、地址..."
        size="large"
        clearable
        @keyup.enter="handleSearch"
      >
        <template #prefix>
          <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <circle cx="11" cy="11" r="8"></circle>
            <line x1="21" y1="21" x2="16.65" y2="16.65"></line>
          </svg>
        </template>
      </el-input>
      <el-button type="primary" size="large" @click="handleSearch">搜索</el-button>
    </div>
    
    <div class="filter-row">
      <div class="filter-group">
        <label>宠物类型</label>
        <el-select 
          v-model="filters.petType" 
          placeholder="全部宠物"
          clearable
          @change="updateFilters"
        >
          <el-option
            v-for="opt in petTypeOptions"
            :key="opt.value"
            :value="opt.value || undefined"
            :label="opt.label"
          />
        </el-select>
      </div>
      
      <div class="filter-group">
        <label>最低评分</label>
        <el-select 
          v-model="filters.minRating" 
          placeholder="不限评分"
          clearable
          @change="updateFilters"
        >
          <el-option
            v-for="opt in ratingOptions"
            :key="String(opt.value)"
            :value="opt.value"
            :label="opt.label"
          />
        </el-select>
      </div>
      
      <div class="filter-group">
        <label>价格范围</label>
        <el-select 
          v-model="filters.maxPrice" 
          placeholder="不限价格"
          clearable
          @change="updateFilters"
        >
          <el-option
            v-for="opt in priceOptions"
            :key="String(opt.value)"
            :value="opt.value"
            :label="opt.label"
          />
        </el-select>
      </div>
      
      <div class="filter-group">
        <label>排序方式</label>
        <el-select 
          v-model="filters.sortBy" 
          @change="updateFilters"
        >
          <el-option
            v-for="opt in sortOptions"
            :key="opt.value"
            :value="opt.value"
            :label="opt.label"
          />
        </el-select>
      </div>
      
      <el-button text @click="resetFilters">重置筛选</el-button>
    </div>
  </div>
</template>

<style lang="scss" scoped>
@import '@/styles/variables.scss';

.search-filters {
  background: var(--color-surface);
  border-radius: var(--radius-lg);
  padding: 20px;
  margin-bottom: 24px;
  box-shadow: var(--shadow-sm);
}

.search-bar {
  display: flex;
  gap: 12px;
  margin-bottom: 16px;
  
  .el-input {
    flex: 1;
  }
}

.filter-row {
  display: flex;
  flex-wrap: wrap;
  gap: 16px;
  align-items: flex-end;
}

.filter-group {
  display: flex;
  flex-direction: column;
  gap: 6px;
  
  label {
    font-size: 13px;
    color: var(--color-text-secondary);
  }
  
  .el-select {
    width: 140px;
  }
}

@media (max-width: 768px) {
  .filter-row {
    flex-direction: column;
    align-items: stretch;
  }
  
  .filter-group {
    .el-select {
      width: 100%;
    }
  }
}
</style>
