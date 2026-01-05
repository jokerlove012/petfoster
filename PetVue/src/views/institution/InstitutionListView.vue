<script setup lang="ts">
import { ref, onMounted, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useInstitutionStore } from '@/stores'
import { institutionApi } from '@/api/institution'
import type { SearchFilters, InstitutionWithDistance } from '@/types/institution'
import SearchFiltersComponent from '@/components/institution/SearchFilters.vue'
import InstitutionCard from '@/components/institution/InstitutionCard.vue'
import InstitutionCardSkeleton from '@/components/institution/InstitutionCardSkeleton.vue'

const route = useRoute()
const router = useRouter()
const institutionStore = useInstitutionStore()

const loading = ref(false)
const institutions = ref<InstitutionWithDistance[]>([])
const pagination = ref({
  page: 1,
  pageSize: 12,
  total: 0,
  totalPages: 0
})

const filters = ref<SearchFilters>({
  keyword: (route.query.keyword as string) || '',
  petType: (route.query.petType as any) || undefined,
  minRating: route.query.minRating ? parseFloat(route.query.minRating as string) : undefined,
  maxPrice: route.query.maxPrice ? parseFloat(route.query.maxPrice as string) : undefined,
  sortBy: (route.query.sortBy as any) || 'rating'
})

const fetchInstitutions = async () => {
  loading.value = true
  
  try {
    const res = await institutionApi.search({
      ...filters.value,
      page: pagination.value.page,
      pageSize: pagination.value.pageSize
    })
    
    if (res.code === 200 && res.data) {
      institutions.value = res.data.list
      pagination.value = {
        ...pagination.value,
        ...res.data.pagination
      }
      institutionStore.setInstitutions(res.data.list)
    }
  } catch (error) {
    console.error('Failed to fetch institutions:', error)
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  pagination.value.page = 1
  
  // 更新 URL 参数
  const query: Record<string, string> = {}
  if (filters.value.keyword) query.keyword = filters.value.keyword
  if (filters.value.petType) query.petType = filters.value.petType
  if (filters.value.minRating) query.minRating = filters.value.minRating.toString()
  if (filters.value.maxPrice) query.maxPrice = filters.value.maxPrice.toString()
  if (filters.value.sortBy) query.sortBy = filters.value.sortBy
  
  router.replace({ query })
  fetchInstitutions()
}

const handlePageChange = (page: number) => {
  pagination.value.page = page
  fetchInstitutions()
  window.scrollTo({ top: 0, behavior: 'smooth' })
}

onMounted(() => {
  fetchInstitutions()
})

// 监听路由变化
watch(() => route.query, () => {
  filters.value = {
    keyword: (route.query.keyword as string) || '',
    petType: (route.query.petType as any) || undefined,
    minRating: route.query.minRating ? parseFloat(route.query.minRating as string) : undefined,
    maxPrice: route.query.maxPrice ? parseFloat(route.query.maxPrice as string) : undefined,
    sortBy: (route.query.sortBy as any) || 'rating'
  }
}, { deep: true })
</script>

<template>
  <div class="institution-list-view">
    <div class="page-header">
      <h1>寻找寄养机构</h1>
      <p>为您的爱宠找到最合适的寄养服务</p>
    </div>
    
    <SearchFiltersComponent 
      v-model="filters" 
      @search="handleSearch"
    />
    
    <div class="results-info">
      <span v-if="!loading">共找到 {{ pagination.total }} 家机构</span>
      <span v-else>搜索中...</span>
    </div>
    
    <!-- 加载状态 -->
    <div v-if="loading" class="institution-grid">
      <InstitutionCardSkeleton v-for="i in 6" :key="i" />
    </div>
    
    <!-- 结果列表 -->
    <div v-else-if="institutions.length > 0" class="institution-grid">
      <InstitutionCard
        v-for="inst in institutions"
        :key="inst.id"
        :institution="inst"
      />
    </div>
    
    <!-- 空状态 -->
    <div v-else class="empty-state">
      <div class="empty-icon">🔍</div>
      <h3>未找到符合条件的机构</h3>
      <p>试试调整筛选条件或搜索其他关键词</p>
    </div>
    
    <!-- 分页 -->
    <div v-if="pagination.totalPages > 1" class="pagination-wrapper">
      <el-pagination
        v-model:current-page="pagination.page"
        :page-size="pagination.pageSize"
        :total="pagination.total"
        layout="prev, pager, next"
        @current-change="handlePageChange"
      />
    </div>
  </div>
</template>

<style lang="scss" scoped>
@import '@/styles/variables.scss';

.institution-list-view {
  max-width: 1200px;
  margin: 0 auto;
}

.page-header {
  margin-bottom: 24px;
  
  h1 {
    font-family: var(--font-display);
    font-size: 28px;
    font-weight: 700;
    color: var(--color-text-primary);
    margin: 0 0 8px;
  }
  
  p {
    color: var(--color-text-secondary);
    margin: 0;
  }
}

.results-info {
  margin-bottom: 16px;
  color: var(--color-text-secondary);
  font-size: 14px;
}

.institution-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
  gap: 24px;
}

.empty-state {
  text-align: center;
  padding: 60px 20px;
  
  .empty-icon {
    font-size: 64px;
    margin-bottom: 16px;
  }
  
  h3 {
    font-size: 18px;
    color: var(--color-text-primary);
    margin: 0 0 8px;
  }
  
  p {
    color: var(--color-text-secondary);
    margin: 0;
  }
}

.pagination-wrapper {
  display: flex;
  justify-content: center;
  margin-top: 40px;
}

@media (max-width: 768px) {
  .institution-grid {
    grid-template-columns: 1fr;
  }
}
</style>
