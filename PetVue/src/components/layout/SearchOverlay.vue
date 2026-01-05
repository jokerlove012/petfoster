<script setup lang="ts">
import { ref, computed, watch, onMounted, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import { institutionApi } from '@/api/institution'
import type { Institution } from '@/types/institution'

const props = defineProps<{
  visible: boolean
}>()

const emit = defineEmits<{
  (e: 'close'): void
}>()

const router = useRouter()
const searchQuery = ref('')
const searchResults = ref<Institution[]>([])
const loading = ref(false)
const searchInputRef = ref<HTMLInputElement | null>(null)

// 热门搜索
const hotKeywords = ['宠物寄养', '猫咪', '狗狗', '上门喂养', '长期寄养']

// 搜索防抖
let searchTimeout: ReturnType<typeof setTimeout> | null = null

const handleSearch = async () => {
  if (!searchQuery.value.trim()) {
    searchResults.value = []
    return
  }

  loading.value = true
  try {
    const res = await institutionApi.getList({ keyword: searchQuery.value, pageSize: 5 })
    if (res.code === 200 && res.data) {
      searchResults.value = res.data.list
    }
  } catch (error) {
    console.error('Search failed:', error)
  } finally {
    loading.value = false
  }
}

watch(searchQuery, () => {
  if (searchTimeout) clearTimeout(searchTimeout)
  searchTimeout = setTimeout(handleSearch, 300)
})

watch(() => props.visible, (visible) => {
  if (visible) {
    setTimeout(() => searchInputRef.value?.focus(), 100)
  } else {
    searchQuery.value = ''
    searchResults.value = []
  }
})

const selectKeyword = (keyword: string) => {
  searchQuery.value = keyword
}

const goToInstitution = (id: string) => {
  emit('close')
  router.push(`/institutions/${id}`)
}

const goToSearch = () => {
  emit('close')
  router.push({ path: '/institutions', query: { keyword: searchQuery.value } })
}

// ESC 关闭
const handleKeydown = (e: KeyboardEvent) => {
  if (e.key === 'Escape') {
    emit('close')
  }
}

onMounted(() => {
  document.addEventListener('keydown', handleKeydown)
})

onUnmounted(() => {
  document.removeEventListener('keydown', handleKeydown)
  if (searchTimeout) clearTimeout(searchTimeout)
})
</script>

<template>
  <Transition name="overlay">
    <div v-if="visible" class="search-overlay" @click.self="$emit('close')">
      <div class="search-container">
        <div class="search-header">
          <div class="search-input-wrapper">
            <svg class="search-icon" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <circle cx="11" cy="11" r="8"></circle>
              <line x1="21" y1="21" x2="16.65" y2="16.65"></line>
            </svg>
            <input
              ref="searchInputRef"
              v-model="searchQuery"
              type="text"
              class="search-input"
              placeholder="搜索寄养机构..."
              @keyup.enter="goToSearch"
            />
            <button v-if="searchQuery" class="clear-btn" @click="searchQuery = ''">
              <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <line x1="18" y1="6" x2="6" y2="18"></line>
                <line x1="6" y1="6" x2="18" y2="18"></line>
              </svg>
            </button>
          </div>
          <button class="close-btn" @click="$emit('close')">取消</button>
        </div>

        <div class="search-body">
          <!-- 热门搜索 -->
          <div v-if="!searchQuery" class="hot-keywords">
            <h4>热门搜索</h4>
            <div class="keyword-tags">
              <span
                v-for="keyword in hotKeywords"
                :key="keyword"
                class="keyword-tag"
                @click="selectKeyword(keyword)"
              >
                {{ keyword }}
              </span>
            </div>
          </div>

          <!-- 搜索结果 -->
          <div v-else class="search-results">
            <div v-if="loading" class="loading-state">
              <span class="loading-spinner"></span>
              <span>搜索中...</span>
            </div>

            <div v-else-if="searchResults.length === 0" class="empty-state">
              <p>未找到相关机构</p>
            </div>

            <div v-else class="result-list">
              <div
                v-for="institution in searchResults"
                :key="institution.id"
                class="result-item"
                @click="goToInstitution(institution.id)"
              >
                <div class="result-icon">🏠</div>
                <div class="result-info">
                  <span class="result-name">{{ institution.name }}</span>
                  <span class="result-address">{{ institution.address }}</span>
                </div>
                <div class="result-rating">
                  <span class="rating-star">★</span>
                  <span>{{ institution.rating.toFixed(1) }}</span>
                </div>
              </div>

              <button class="view-more-btn" @click="goToSearch">
                查看全部结果
                <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                  <polyline points="9 18 15 12 9 6"></polyline>
                </svg>
              </button>
            </div>
          </div>
        </div>
      </div>
    </div>
  </Transition>
</template>


<style lang="scss" scoped>
.search-overlay {
  position: fixed;
  inset: 0;
  background: rgba(0, 0, 0, 0.5);
  backdrop-filter: blur(4px);
  z-index: 2000;
  display: flex;
  justify-content: center;
  padding-top: 80px;
}

.search-container {
  width: 100%;
  max-width: 600px;
  background: var(--color-surface);
  border-radius: var(--radius-xl);
  box-shadow: var(--shadow-xl);
  overflow: hidden;
  max-height: calc(100vh - 160px);
  display: flex;
  flex-direction: column;
}

.search-header {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 16px;
  border-bottom: 1px solid var(--color-border);
}

.search-input-wrapper {
  flex: 1;
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px 16px;
  background: var(--color-neutral-100);
  border-radius: var(--radius-lg);

  .search-icon {
    color: var(--color-text-muted);
    flex-shrink: 0;
  }

  .search-input {
    flex: 1;
    border: none;
    background: none;
    font-size: 16px;
    color: var(--color-text-primary);
    outline: none;

    &::placeholder {
      color: var(--color-text-muted);
    }
  }

  .clear-btn {
    border: none;
    background: none;
    padding: 4px;
    cursor: pointer;
    color: var(--color-text-muted);
    display: flex;
    align-items: center;
    justify-content: center;

    &:hover {
      color: var(--color-text-secondary);
    }
  }
}

.close-btn {
  border: none;
  background: none;
  color: var(--color-text-secondary);
  font-size: 15px;
  cursor: pointer;
  padding: 8px 12px;

  &:hover {
    color: var(--color-primary);
  }
}

.search-body {
  flex: 1;
  overflow-y: auto;
  padding: 20px;
}

.hot-keywords {
  h4 {
    font-size: 14px;
    font-weight: 600;
    color: var(--color-text-secondary);
    margin: 0 0 12px;
  }

  .keyword-tags {
    display: flex;
    flex-wrap: wrap;
    gap: 10px;
  }

  .keyword-tag {
    padding: 8px 16px;
    background: var(--color-neutral-100);
    color: var(--color-text-primary);
    border-radius: var(--radius-full);
    font-size: 14px;
    cursor: pointer;
    transition: all 150ms ease;

    &:hover {
      background: var(--color-primary);
      color: white;
    }
  }
}

.loading-state {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 12px;
  padding: 40px;
  color: var(--color-text-muted);

  .loading-spinner {
    width: 20px;
    height: 20px;
    border: 2px solid var(--color-border);
    border-top-color: var(--color-primary);
    border-radius: 50%;
    animation: spin 0.8s linear infinite;
  }
}

@keyframes spin {
  to { transform: rotate(360deg); }
}

.empty-state {
  text-align: center;
  padding: 40px;
  color: var(--color-text-muted);
}

.result-list {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.result-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px;
  border-radius: var(--radius-md);
  cursor: pointer;
  transition: background 150ms ease;

  &:hover {
    background: var(--color-neutral-100);
  }

  .result-icon {
    width: 40px;
    height: 40px;
    background: var(--color-primary-light);
    border-radius: var(--radius-md);
    display: flex;
    align-items: center;
    justify-content: center;
    font-size: 20px;
  }

  .result-info {
    flex: 1;
    min-width: 0;

    .result-name {
      display: block;
      font-size: 15px;
      font-weight: 500;
      color: var(--color-text-primary);
      margin-bottom: 2px;
    }

    .result-address {
      display: block;
      font-size: 13px;
      color: var(--color-text-muted);
      white-space: nowrap;
      overflow: hidden;
      text-overflow: ellipsis;
    }
  }

  .result-rating {
    display: flex;
    align-items: center;
    gap: 4px;
    font-size: 14px;
    font-weight: 500;
    color: var(--color-text-primary);

    .rating-star {
      color: var(--color-warning);
    }
  }
}

.view-more-btn {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 6px;
  width: 100%;
  padding: 12px;
  margin-top: 8px;
  border: 1px dashed var(--color-border);
  background: none;
  color: var(--color-primary);
  font-size: 14px;
  border-radius: var(--radius-md);
  cursor: pointer;
  transition: all 150ms ease;

  &:hover {
    background: var(--color-primary-light);
    border-color: var(--color-primary);
  }
}

// 动画
.overlay-enter-active,
.overlay-leave-active {
  transition: all 250ms ease;

  .search-container {
    transition: all 250ms ease;
  }
}

.overlay-enter-from,
.overlay-leave-to {
  opacity: 0;

  .search-container {
    transform: translateY(-20px);
    opacity: 0;
  }
}

@media (max-width: 640px) {
  .search-overlay {
    padding-top: 0;
  }

  .search-container {
    max-width: 100%;
    max-height: 100vh;
    border-radius: 0;
  }
}
</style>
