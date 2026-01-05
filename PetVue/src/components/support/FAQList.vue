<script setup lang="ts">
import { ref, computed } from 'vue'
import { ElCollapse, ElCollapseItem, ElInput } from 'element-plus'

export interface FAQItem {
  id: string
  question: string
  answer: string
  category: string
  viewCount?: number
}

export interface FAQCategory {
  id: string
  name: string
  icon: string
}

const props = withDefaults(defineProps<{
  faqs: FAQItem[]
  categories?: FAQCategory[]
  showSearch?: boolean
  showCategories?: boolean
  showViewCount?: boolean
}>(), {
  showSearch: true,
  showCategories: true,
  showViewCount: false
})

const emit = defineEmits<{
  (e: 'view', faq: FAQItem): void
}>()

// 搜索关键词
const searchQuery = ref('')

// 当前选中的分类
const selectedCategory = ref<string | null>(null)

// 展开的FAQ项
const activeNames = ref<string[]>([])

// 过滤后的FAQ列表
const filteredFaqs = computed(() => {
  let result = props.faqs
  
  // 按分类筛选
  if (selectedCategory.value) {
    result = result.filter(faq => faq.category === selectedCategory.value)
  }
  
  // 按关键词搜索
  if (searchQuery.value.trim()) {
    const query = searchQuery.value.toLowerCase()
    result = result.filter(faq => 
      faq.question.toLowerCase().includes(query) ||
      faq.answer.toLowerCase().includes(query)
    )
  }
  
  return result
})

// 按分类分组的FAQ
const groupedFaqs = computed(() => {
  if (!props.categories || selectedCategory.value) {
    return null
  }
  
  const groups: Record<string, FAQItem[]> = {}
  
  for (const faq of filteredFaqs.value) {
    if (!groups[faq.category]) {
      groups[faq.category] = []
    }
    groups[faq.category].push(faq)
  }
  
  return groups
})

// 获取分类名称
const getCategoryName = (categoryId: string) => {
  const category = props.categories?.find(c => c.id === categoryId)
  return category?.name || categoryId
}

// 获取分类图标
const getCategoryIcon = (categoryId: string) => {
  const category = props.categories?.find(c => c.id === categoryId)
  return category?.icon || '📋'
}

// 选择分类
const selectCategory = (categoryId: string | null) => {
  selectedCategory.value = categoryId
  activeNames.value = []
}

// 处理FAQ展开
const handleChange = (names: string | string[]) => {
  const nameArray = Array.isArray(names) ? names : [names]
  
  // 记录查看
  for (const name of nameArray) {
    if (!activeNames.value.includes(name)) {
      const faq = props.faqs.find(f => f.id === name)
      if (faq) {
        emit('view', faq)
      }
    }
  }
  
  activeNames.value = nameArray
}

// 高亮搜索关键词
const highlightText = (text: string) => {
  if (!searchQuery.value.trim()) return text
  
  const query = searchQuery.value.trim()
  const regex = new RegExp(`(${query})`, 'gi')
  return text.replace(regex, '<mark>$1</mark>')
}

// 清除搜索
const clearSearch = () => {
  searchQuery.value = ''
}
</script>

<template>
  <div class="faq-list">
    <!-- 搜索框 -->
    <div v-if="showSearch" class="faq-search">
      <ElInput
        v-model="searchQuery"
        placeholder="搜索问题..."
        clearable
        @clear="clearSearch"
      >
        <template #prefix>
          <span class="search-icon">🔍</span>
        </template>
      </ElInput>
      <div v-if="searchQuery && filteredFaqs.length === 0" class="no-results">
        未找到相关问题，请尝试其他关键词
      </div>
      <div v-else-if="searchQuery" class="search-results-count">
        找到 {{ filteredFaqs.length }} 个相关问题
      </div>
    </div>
    
    <!-- 分类标签 -->
    <div v-if="showCategories && categories && categories.length > 0" class="faq-categories">
      <button 
        class="category-tag"
        :class="{ active: selectedCategory === null }"
        @click="selectCategory(null)"
      >
        全部
      </button>
      <button 
        v-for="cat in categories"
        :key="cat.id"
        class="category-tag"
        :class="{ active: selectedCategory === cat.id }"
        @click="selectCategory(cat.id)"
      >
        <span class="cat-icon">{{ cat.icon }}</span>
        {{ cat.name }}
      </button>
    </div>
    
    <!-- 分组显示 -->
    <template v-if="groupedFaqs && !searchQuery">
      <div 
        v-for="(faqs, categoryId) in groupedFaqs" 
        :key="categoryId"
        class="faq-group"
      >
        <h3 class="group-title">
          <span class="group-icon">{{ getCategoryIcon(categoryId) }}</span>
          {{ getCategoryName(categoryId) }}
        </h3>
        <ElCollapse v-model="activeNames" @change="handleChange">
          <ElCollapseItem 
            v-for="faq in faqs" 
            :key="faq.id"
            :name="faq.id"
          >
            <template #title>
              <div class="faq-title">
                <span v-html="highlightText(faq.question)"></span>
                <span v-if="showViewCount && faq.viewCount" class="view-count">
                  {{ faq.viewCount }} 次浏览
                </span>
              </div>
            </template>
            <div class="faq-answer" v-html="highlightText(faq.answer)"></div>
          </ElCollapseItem>
        </ElCollapse>
      </div>
    </template>
    
    <!-- 列表显示 -->
    <template v-else>
      <ElCollapse v-model="activeNames" @change="handleChange">
        <ElCollapseItem 
          v-for="faq in filteredFaqs" 
          :key="faq.id"
          :name="faq.id"
        >
          <template #title>
            <div class="faq-title">
              <span v-html="highlightText(faq.question)"></span>
              <span v-if="showViewCount && faq.viewCount" class="view-count">
                {{ faq.viewCount }} 次浏览
              </span>
            </div>
          </template>
          <div class="faq-answer" v-html="highlightText(faq.answer)"></div>
        </ElCollapseItem>
      </ElCollapse>
    </template>
    
    <!-- 空状态 -->
    <div v-if="filteredFaqs.length === 0 && !searchQuery" class="empty-state">
      <span class="empty-icon">📭</span>
      <p>暂无常见问题</p>
    </div>
  </div>
</template>

<style lang="scss" scoped>
.faq-list {
  width: 100%;
}

// 搜索框
.faq-search {
  margin-bottom: 20px;
  
  :deep(.el-input__wrapper) {
    border-radius: var(--radius-lg);
    padding: 4px 12px;
  }
  
  .search-icon {
    font-size: 16px;
  }
}

.no-results,
.search-results-count {
  margin-top: 8px;
  font-size: 13px;
  color: var(--color-text-secondary);
}

.no-results {
  color: var(--color-text-muted);
}

// 分类标签
.faq-categories {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  margin-bottom: 24px;
}

.category-tag {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  padding: 8px 16px;
  border: 1px solid var(--color-border);
  border-radius: var(--radius-full);
  background: var(--color-surface);
  font-size: 13px;
  color: var(--color-text-secondary);
  cursor: pointer;
  transition: all 0.2s;
  
  .cat-icon {
    font-size: 14px;
  }
  
  &:hover {
    border-color: var(--color-primary);
    color: var(--color-primary);
  }
  
  &.active {
    background: var(--color-primary);
    border-color: var(--color-primary);
    color: white;
  }
}

// 分组标题
.faq-group {
  margin-bottom: 32px;
  
  &:last-child {
    margin-bottom: 0;
  }
}

.group-title {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 18px;
  font-weight: 600;
  margin: 0 0 16px;
  color: var(--color-text-primary);
  
  .group-icon {
    font-size: 20px;
  }
}

// FAQ 项
.faq-title {
  display: flex;
  align-items: center;
  justify-content: space-between;
  width: 100%;
  padding-right: 12px;
  
  :deep(mark) {
    background: var(--color-primary-light);
    color: var(--color-primary);
    padding: 0 2px;
    border-radius: 2px;
  }
}

.view-count {
  font-size: 12px;
  color: var(--color-text-muted);
  font-weight: normal;
}

.faq-answer {
  color: var(--color-text-secondary);
  line-height: 1.7;
  
  :deep(mark) {
    background: var(--color-primary-light);
    color: var(--color-primary);
    padding: 0 2px;
    border-radius: 2px;
  }
}

// Element Plus 样式覆盖
:deep(.el-collapse) {
  border: none;
}

:deep(.el-collapse-item) {
  margin-bottom: 8px;
  border: 1px solid var(--color-border);
  border-radius: var(--radius-md);
  overflow: hidden;
  
  &:last-child {
    margin-bottom: 0;
  }
}

:deep(.el-collapse-item__header) {
  padding: 16px 20px;
  font-weight: 500;
  font-size: 15px;
  background: var(--color-surface);
  border: none;
  
  &:hover {
    background: var(--color-neutral-50);
  }
}

:deep(.el-collapse-item__wrap) {
  border: none;
}

:deep(.el-collapse-item__content) {
  padding: 0 20px 16px;
  background: var(--color-surface);
}

// 空状态
.empty-state {
  text-align: center;
  padding: 48px 20px;
  color: var(--color-text-muted);
  
  .empty-icon {
    font-size: 48px;
    display: block;
    margin-bottom: 12px;
  }
  
  p {
    margin: 0;
    font-size: 14px;
  }
}
</style>
