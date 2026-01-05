<template>
  <div class="browsing-history-view">
    <div class="page-header">
      <h1 class="page-title">浏览历史</h1>
      <button 
        v-if="filteredHistory.length > 0"
        class="clear-btn"
        @click="handleClearHistory"
      >
        <Trash2 :size="16" />
        清空历史
      </button>
    </div>

    <EmptyState
      v-if="filteredHistory.length === 0"
      icon="history"
      title="暂无浏览记录"
      description="您最近30天内没有浏览过任何机构"
      actionText="去发现机构"
      @action="goToSearch"
    />

    <div v-else class="history-list">
      <div 
        v-for="item in filteredHistory" 
        :key="item.id"
        class="history-item"
      >
        <div class="visit-date">
          {{ formatVisitDate(item.visitedAt) }}
        </div>
        <InstitutionCard
          v-if="getInstitution(item.id)"
          :institution="getInstitution(item.id)!"
          @click="viewInstitution(item.id)"
        />
        <div v-else class="institution-unavailable">
          <AlertCircle :size="20" />
          <span>该机构信息暂不可用</span>
        </div>
        <div class="item-actions">
          <button class="action-btn" @click="rebookInstitution(item.id)">
            <Calendar :size="16" />
            重新预约
          </button>
          <button class="action-btn" @click="removeFromHistory(item.id)">
            <X :size="16" />
            移除
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { Trash2, Calendar, X, AlertCircle } from 'lucide-vue-next'
import { useInstitutionStore } from '@/stores/institution'
import { useRebooking } from '@/composables/useRebooking'
import InstitutionCard from '@/components/institution/InstitutionCard.vue'
import EmptyState from '@/components/common/EmptyState.vue'
import type { InstitutionWithDistance } from '@/types/institution'
import { institutionApi } from '@/api/institution'

const router = useRouter()
const institutionStore = useInstitutionStore()
const { setRebookingData, createFromInstitution } = useRebooking()

const institutionsMap = ref<Map<string, InstitutionWithDistance>>(new Map())
const loading = ref(false)

// 过滤30天内的浏览历史
const filteredHistory = computed(() => {
  const thirtyDaysAgo = new Date()
  thirtyDaysAgo.setDate(thirtyDaysAgo.getDate() - 30)
  
  return institutionStore.browsingHistory
    .filter(item => new Date(item.visitedAt) >= thirtyDaysAgo)
    .sort((a, b) => new Date(b.visitedAt).getTime() - new Date(a.visitedAt).getTime())
})

function getInstitution(id: string): InstitutionWithDistance | undefined {
  return institutionsMap.value.get(id)
}

function formatVisitDate(dateStr: string): string {
  const date = new Date(dateStr)
  const now = new Date()
  const diffMs = now.getTime() - date.getTime()
  const diffDays = Math.floor(diffMs / (1000 * 60 * 60 * 24))
  
  if (diffDays === 0) {
    return '今天 ' + date.toLocaleTimeString('zh-CN', { hour: '2-digit', minute: '2-digit' })
  } else if (diffDays === 1) {
    return '昨天 ' + date.toLocaleTimeString('zh-CN', { hour: '2-digit', minute: '2-digit' })
  } else if (diffDays < 7) {
    return `${diffDays}天前`
  } else {
    return date.toLocaleDateString('zh-CN', { month: 'short', day: 'numeric' })
  }
}

function viewInstitution(id: string) {
  router.push(`/institution/${id}`)
}

function rebookInstitution(id: string) {
  const institution = getInstitution(id)
  // 设置重新预约数据
  setRebookingData(createFromInstitution(id, institution?.name))
  // 跳转到预约页面
  router.push(`/booking/create/${id}`)
}

function removeFromHistory(id: string) {
  const index = institutionStore.browsingHistory.findIndex(h => h.id === id)
  if (index > -1) {
    institutionStore.browsingHistory.splice(index, 1)
  }
}

function handleClearHistory() {
  if (confirm('确定要清空所有浏览历史吗？')) {
    institutionStore.clearHistory()
  }
}

function goToSearch() {
  router.push('/institutions')
}

async function loadInstitutions() {
  loading.value = true
  try {
    const ids = filteredHistory.value.map(h => h.id)
    if (ids.length === 0) return
    
    // 获取所有机构信息
    const response = await institutionApi.search({})
    if (response.data) {
      response.data.items.forEach(inst => {
        institutionsMap.value.set(inst.id, inst)
      })
    }
  } catch (error) {
    console.error('Failed to load institutions:', error)
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  institutionStore.init()
  loadInstitutions()
})
</script>

<style scoped lang="scss">
.browsing-history-view {
  padding: 24px;
  max-width: 800px;
  margin: 0 auto;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
}

.page-title {
  font-size: 24px;
  font-weight: 700;
  color: var(--color-text-primary);
}

.clear-btn {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 8px 16px;
  background: transparent;
  border: 1px solid var(--color-border);
  border-radius: var(--radius-md);
  color: var(--color-text-secondary);
  font-size: 14px;
  cursor: pointer;
  transition: all 0.2s ease;

  &:hover {
    border-color: var(--color-error);
    color: var(--color-error);
  }
}

.history-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.history-item {
  background: var(--color-surface);
  border-radius: var(--radius-lg);
  padding: 16px;
  box-shadow: var(--shadow-sm);
  border: 1px solid var(--color-border);
}

.visit-date {
  font-size: 12px;
  color: var(--color-text-muted);
  margin-bottom: 12px;
}

.institution-unavailable {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 16px;
  background: var(--color-neutral-100);
  border-radius: var(--radius-md);
  color: var(--color-text-muted);
  font-size: 14px;
}

.item-actions {
  display: flex;
  gap: 12px;
  margin-top: 12px;
  padding-top: 12px;
  border-top: 1px solid var(--color-border);
}

.action-btn {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 8px 12px;
  background: transparent;
  border: none;
  border-radius: var(--radius-sm);
  color: var(--color-text-secondary);
  font-size: 13px;
  cursor: pointer;
  transition: all 0.2s ease;

  &:hover {
    background: var(--color-neutral-100);
    color: var(--color-primary);
  }
}

@media (max-width: 640px) {
  .browsing-history-view {
    padding: 16px;
  }

  .page-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 12px;
  }

  .item-actions {
    flex-wrap: wrap;
  }
}
</style>
