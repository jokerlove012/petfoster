<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useInstitutionStore } from '@/stores'
import { institutionApi } from '@/api/institution'
import type { Institution } from '@/types/institution'
import InstitutionCard from '@/components/institution/InstitutionCard.vue'
import InstitutionCardSkeleton from '@/components/institution/InstitutionCardSkeleton.vue'
import { AppButton } from '@/components/common'

const institutionStore = useInstitutionStore()

const loading = ref(false)
const favoriteInstitutions = ref<Institution[]>([])

const favoriteIds = computed(() => institutionStore.favorites)
const isEmpty = computed(() => favoriteIds.value.length === 0)

const fetchFavorites = async () => {
  if (isEmpty.value) {
    favoriteInstitutions.value = []
    return
  }
  
  loading.value = true
  try {
    // 获取所有收藏机构的详情
    const promises = favoriteIds.value.map(id => institutionApi.getDetail(id))
    const results = await Promise.all(promises)
    
    favoriteInstitutions.value = results
      .filter(res => res.code === 200 && res.data)
      .map(res => res.data as Institution)
  } catch (error) {
    console.error('Failed to fetch favorites:', error)
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  fetchFavorites()
})
</script>

<template>
  <div class="favorites-view">
    <div class="page-header">
      <h1>我的收藏</h1>
      <p>您收藏的寄养机构</p>
    </div>
    
    <!-- 加载状态 -->
    <div v-if="loading" class="favorites-grid">
      <InstitutionCardSkeleton v-for="i in 3" :key="i" />
    </div>
    
    <!-- 收藏列表 -->
    <div v-else-if="favoriteInstitutions.length > 0" class="favorites-grid">
      <InstitutionCard
        v-for="inst in favoriteInstitutions"
        :key="inst.id"
        :institution="inst"
      />
    </div>
    
    <!-- 空状态 -->
    <div v-else class="empty-state">
      <div class="empty-icon">💝</div>
      <h3>暂无收藏</h3>
      <p>浏览机构时点击爱心图标即可收藏</p>
      <router-link to="/institutions">
        <AppButton type="primary">去看看</AppButton>
      </router-link>
    </div>
  </div>
</template>

<style lang="scss" scoped>
.favorites-view {
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

.favorites-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
  gap: 24px;
}

.empty-state {
  text-align: center;
  padding: 80px 20px;
  
  .empty-icon {
    font-size: 64px;
    margin-bottom: 16px;
  }
  
  h3 {
    font-size: 20px;
    color: var(--color-text-primary);
    margin: 0 0 8px;
  }
  
  p {
    color: var(--color-text-secondary);
    margin: 0 0 24px;
  }
}
</style>
