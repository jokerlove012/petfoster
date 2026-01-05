<template>
  <div class="data-table">
    <!-- 工具栏 -->
    <div v-if="showToolbar" class="table-toolbar">
      <div class="toolbar-left">
        <slot name="toolbar-left">
          <el-input
            v-if="searchable"
            v-model="searchQuery"
            placeholder="搜索..."
            clearable
            class="search-input"
            @input="handleSearch"
          >
            <template #prefix>
              <Search class="search-icon" />
            </template>
          </el-input>
        </slot>
      </div>
      <div class="toolbar-right">
        <slot name="toolbar-right" />
      </div>
    </div>

    <!-- 表格 -->
    <div class="table-container" :class="{ loading: loading }">
      <table>
        <thead>
          <tr>
            <th v-if="selectable" class="col-checkbox">
              <input 
                type="checkbox" 
                :checked="isAllSelected"
                :indeterminate="isIndeterminate"
                @change="toggleSelectAll"
              />
            </th>
            <th 
              v-for="col in columns" 
              :key="col.key"
              :class="[
                `col-${col.key}`,
                { sortable: col.sortable, sorted: sortKey === col.key }
              ]"
              :style="{ width: col.width }"
              @click="col.sortable && handleSort(col.key)"
            >
              <span class="th-content">
                {{ col.label }}
                <span v-if="col.sortable" class="sort-icon">
                  <ChevronUp 
                    v-if="sortKey === col.key && sortOrder === 'asc'" 
                    class="icon active" 
                  />
                  <ChevronDown 
                    v-else-if="sortKey === col.key && sortOrder === 'desc'" 
                    class="icon active" 
                  />
                  <ChevronsUpDown v-else class="icon" />
                </span>
              </span>
            </th>
            <th v-if="$slots.actions" class="col-actions">操作</th>
          </tr>
        </thead>
        <tbody>
          <tr 
            v-for="(row, index) in displayData" 
            :key="getRowKey(row, index)"
            :class="{ selected: isSelected(row) }"
            @click="handleRowClick(row)"
          >
            <td v-if="selectable" class="col-checkbox">
              <input 
                type="checkbox" 
                :checked="isSelected(row)"
                @change="toggleSelect(row)"
                @click.stop
              />
            </td>
            <td 
              v-for="col in columns" 
              :key="col.key"
              :class="`col-${col.key}`"
            >
              <slot :name="`cell-${col.key}`" :row="row" :value="row[col.key]">
                {{ formatCell(row[col.key], col) }}
              </slot>
            </td>
            <td v-if="$slots.actions" class="col-actions">
              <slot name="actions" :row="row" :index="index" />
            </td>
          </tr>
          <tr v-if="displayData.length === 0">
            <td :colspan="totalColumns" class="empty-cell">
              <slot name="empty">
                <EmptyState type="data" size="sm" />
              </slot>
            </td>
          </tr>
        </tbody>
      </table>
      
      <!-- 加载遮罩 -->
      <div v-if="loading" class="loading-overlay">
        <Loader2 class="loading-icon" />
      </div>
    </div>

    <!-- 分页 -->
    <div v-if="pagination && totalItems > 0" class="table-pagination">
      <div class="pagination-info">
        共 {{ totalItems }} 条，每页
        <select v-model="pageSizeModel" @change="handlePageSizeChange">
          <option v-for="size in pageSizes" :key="size" :value="size">{{ size }}</option>
        </select>
        条
      </div>
      <div class="pagination-controls">
        <button 
          class="page-btn" 
          :disabled="currentPage === 1"
          @click="goToPage(1)"
        >
          <ChevronsLeft class="icon" />
        </button>
        <button 
          class="page-btn" 
          :disabled="currentPage === 1"
          @click="goToPage(currentPage - 1)"
        >
          <ChevronLeft class="icon" />
        </button>
        <span class="page-info">{{ currentPage }} / {{ totalPages }}</span>
        <button 
          class="page-btn" 
          :disabled="currentPage === totalPages"
          @click="goToPage(currentPage + 1)"
        >
          <ChevronRight class="icon" />
        </button>
        <button 
          class="page-btn" 
          :disabled="currentPage === totalPages"
          @click="goToPage(totalPages)"
        >
          <ChevronsRight class="icon" />
        </button>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, watch } from 'vue'
import { 
  Search, ChevronUp, ChevronDown, ChevronsUpDown,
  ChevronLeft, ChevronRight, ChevronsLeft, ChevronsRight,
  Loader2
} from 'lucide-vue-next'
import EmptyState from './EmptyState.vue'

interface Column {
  key: string
  label: string
  width?: string
  sortable?: boolean
  formatter?: (value: unknown, row: Record<string, unknown>) => string
}

interface Props {
  data: Record<string, unknown>[]
  columns: Column[]
  rowKey?: string
  loading?: boolean
  selectable?: boolean
  searchable?: boolean
  showToolbar?: boolean
  pagination?: boolean
  pageSize?: number
  pageSizes?: number[]
  totalItems?: number
  serverSide?: boolean
}

const props = withDefaults(defineProps<Props>(), {
  rowKey: 'id',
  loading: false,
  selectable: false,
  searchable: true,
  showToolbar: true,
  pagination: true,
  pageSize: 10,
  pageSizes: () => [10, 20, 50, 100],
  totalItems: 0,
  serverSide: false
})

const emit = defineEmits<{
  (e: 'row-click', row: Record<string, unknown>): void
  (e: 'selection-change', selected: Record<string, unknown>[]): void
  (e: 'sort-change', key: string, order: 'asc' | 'desc'): void
  (e: 'page-change', page: number): void
  (e: 'page-size-change', size: number): void
  (e: 'search', query: string): void
}>()

// 状态
const searchQuery = ref('')
const sortKey = ref('')
const sortOrder = ref<'asc' | 'desc'>('asc')
const selectedRows = ref<Record<string, unknown>[]>([])
const currentPage = ref(1)
const pageSizeModel = ref(props.pageSize)

// 计算属性
const totalColumns = computed(() => {
  let count = props.columns.length
  if (props.selectable) count++
  return count + 1 // +1 for actions
})

const totalPages = computed(() => {
  const total = props.serverSide ? props.totalItems : filteredData.value.length
  return Math.ceil(total / pageSizeModel.value) || 1
})

const filteredData = computed(() => {
  let result = [...props.data]
  
  // 搜索过滤
  if (searchQuery.value && !props.serverSide) {
    const query = searchQuery.value.toLowerCase()
    result = result.filter(row => 
      props.columns.some(col => 
        String(row[col.key] || '').toLowerCase().includes(query)
      )
    )
  }
  
  // 排序
  if (sortKey.value && !props.serverSide) {
    result.sort((a, b) => {
      const aVal = a[sortKey.value]
      const bVal = b[sortKey.value]
      let comparison = 0
      
      if (aVal === null || aVal === undefined) comparison = 1
      else if (bVal === null || bVal === undefined) comparison = -1
      else if (typeof aVal === 'number' && typeof bVal === 'number') {
        comparison = aVal - bVal
      } else {
        comparison = String(aVal).localeCompare(String(bVal))
      }
      
      return sortOrder.value === 'asc' ? comparison : -comparison
    })
  }
  
  return result
})

const displayData = computed(() => {
  if (props.serverSide) return props.data
  
  const start = (currentPage.value - 1) * pageSizeModel.value
  const end = start + pageSizeModel.value
  return filteredData.value.slice(start, end)
})

const isAllSelected = computed(() => {
  return displayData.value.length > 0 && 
    displayData.value.every(row => isSelected(row))
})

const isIndeterminate = computed(() => {
  const selectedCount = displayData.value.filter(row => isSelected(row)).length
  return selectedCount > 0 && selectedCount < displayData.value.length
})

// 方法
const getRowKey = (row: Record<string, unknown>, index: number) => {
  return row[props.rowKey] as string || index
}

const formatCell = (value: unknown, col: Column) => {
  if (col.formatter) {
    return col.formatter(value, {} as Record<string, unknown>)
  }
  if (value === null || value === undefined) return '-'
  return String(value)
}

const isSelected = (row: Record<string, unknown>) => {
  const key = row[props.rowKey]
  return selectedRows.value.some(r => r[props.rowKey] === key)
}

const toggleSelect = (row: Record<string, unknown>) => {
  const key = row[props.rowKey]
  const index = selectedRows.value.findIndex(r => r[props.rowKey] === key)
  
  if (index > -1) {
    selectedRows.value.splice(index, 1)
  } else {
    selectedRows.value.push(row)
  }
  
  emit('selection-change', selectedRows.value)
}

const toggleSelectAll = () => {
  if (isAllSelected.value) {
    selectedRows.value = []
  } else {
    selectedRows.value = [...displayData.value]
  }
  emit('selection-change', selectedRows.value)
}

const handleSort = (key: string) => {
  if (sortKey.value === key) {
    sortOrder.value = sortOrder.value === 'asc' ? 'desc' : 'asc'
  } else {
    sortKey.value = key
    sortOrder.value = 'asc'
  }
  emit('sort-change', sortKey.value, sortOrder.value)
}

const handleSearch = () => {
  currentPage.value = 1
  emit('search', searchQuery.value)
}

const handleRowClick = (row: Record<string, unknown>) => {
  emit('row-click', row)
}

const goToPage = (page: number) => {
  currentPage.value = page
  emit('page-change', page)
}

const handlePageSizeChange = () => {
  currentPage.value = 1
  emit('page-size-change', pageSizeModel.value)
}

// 监听数据变化重置页码
watch(() => props.data, () => {
  if (!props.serverSide) {
    currentPage.value = 1
  }
})

// 导出排序方法供外部使用
export interface SortResult<T> {
  data: T[]
  key: string
  order: 'asc' | 'desc'
}

export function sortData<T extends Record<string, unknown>>(
  data: T[],
  key: string,
  order: 'asc' | 'desc'
): T[] {
  return [...data].sort((a, b) => {
    const aVal = a[key]
    const bVal = b[key]
    let comparison = 0
    
    if (aVal === null || aVal === undefined) comparison = 1
    else if (bVal === null || bVal === undefined) comparison = -1
    else if (typeof aVal === 'number' && typeof bVal === 'number') {
      comparison = aVal - bVal
    } else {
      comparison = String(aVal).localeCompare(String(bVal))
    }
    
    return order === 'asc' ? comparison : -comparison
  })
}
</script>

<style scoped lang="scss">
.data-table {
  background: white;
  border-radius: 12px;
  overflow: hidden;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
}

.table-toolbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px 20px;
  border-bottom: 1px solid #eee;
  gap: 16px;
}

.toolbar-left {
  display: flex;
  gap: 12px;
}

.search-input {
  width: 240px;
  
  .search-icon {
    width: 16px;
    height: 16px;
    color: #999;
  }
}

.table-container {
  position: relative;
  overflow-x: auto;
  
  &.loading {
    min-height: 200px;
  }
}

table {
  width: 100%;
  border-collapse: collapse;
}

th, td {
  padding: 14px 16px;
  text-align: left;
  border-bottom: 1px solid #eee;
}

th {
  background: #fafafa;
  font-weight: 600;
  font-size: 13px;
  color: var(--color-text-secondary, #666);
  white-space: nowrap;
  
  &.sortable {
    cursor: pointer;
    user-select: none;
    
    &:hover {
      background: #f5f5f5;
    }
  }
  
  &.sorted {
    color: var(--color-primary, #ff6b35);
  }
}

.th-content {
  display: flex;
  align-items: center;
  gap: 4px;
}

.sort-icon {
  display: flex;
  
  .icon {
    width: 14px;
    height: 14px;
    color: #ccc;
    
    &.active {
      color: var(--color-primary, #ff6b35);
    }
  }
}

td {
  font-size: 14px;
  color: var(--color-text-primary, #1a1a1a);
}

tr {
  transition: background 0.2s ease;
  
  &:hover {
    background: #fafafa;
  }
  
  &.selected {
    background: rgba(255, 107, 53, 0.05);
  }
}

.col-checkbox {
  width: 48px;
  text-align: center;
  
  input[type="checkbox"] {
    width: 16px;
    height: 16px;
    cursor: pointer;
  }
}

.col-actions {
  width: 120px;
  text-align: right;
}

.empty-cell {
  text-align: center;
  padding: 40px;
}

.loading-overlay {
  position: absolute;
  inset: 0;
  background: rgba(255, 255, 255, 0.8);
  display: flex;
  align-items: center;
  justify-content: center;
  
  .loading-icon {
    width: 32px;
    height: 32px;
    color: var(--color-primary, #ff6b35);
    animation: spin 1s linear infinite;
  }
}

@keyframes spin {
  from { transform: rotate(0deg); }
  to { transform: rotate(360deg); }
}

.table-pagination {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px 20px;
  border-top: 1px solid #eee;
}

.pagination-info {
  font-size: 13px;
  color: var(--color-text-secondary, #666);
  
  select {
    margin: 0 4px;
    padding: 4px 8px;
    border: 1px solid #ddd;
    border-radius: 4px;
    font-size: 13px;
  }
}

.pagination-controls {
  display: flex;
  align-items: center;
  gap: 8px;
}

.page-btn {
  width: 32px;
  height: 32px;
  border: 1px solid #ddd;
  border-radius: 6px;
  background: white;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.2s ease;
  
  &:hover:not(:disabled) {
    border-color: var(--color-primary, #ff6b35);
    color: var(--color-primary, #ff6b35);
  }
  
  &:disabled {
    opacity: 0.5;
    cursor: not-allowed;
  }
  
  .icon {
    width: 16px;
    height: 16px;
  }
}

.page-info {
  font-size: 13px;
  color: var(--color-text-secondary, #666);
  min-width: 60px;
  text-align: center;
}
</style>
