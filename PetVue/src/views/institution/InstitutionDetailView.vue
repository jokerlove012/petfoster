<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { useInstitutionStore, useAuthStore } from '@/stores'
import { institutionApi, type InstitutionDetail } from '@/api/institution'
import { AppButton, AppCard } from '@/components/common'
import { openNavigation } from '@/utils/mapService'
import type { ServicePackage } from '@/types/institution'
import type { Review } from '@/types/review'

const route = useRoute()
const router = useRouter()
const institutionStore = useInstitutionStore()
const authStore = useAuthStore()

const loading = ref(true)
const institution = ref<InstitutionDetail | null>(null)
const activeTab = ref('services')

const institutionId = computed(() => route.params.id as string)
const isFavorite = computed(() => institutionStore.favorites.includes(institutionId.value))
const isAuthenticated = computed(() => authStore.isAuthenticated)

const fetchInstitution = async () => {
  loading.value = true
  try {
    const res = await institutionApi.getDetail(institutionId.value)
    if (res.code === 200 && res.data) {
      institution.value = res.data
    }
  } catch (error) {
    console.error('Failed to fetch institution:', error)
    ElMessage.error('加载机构信息失败')
  } finally {
    loading.value = false
  }
}

const toggleFavorite = () => {
  if (!isAuthenticated.value) {
    ElMessage.warning('请先登录')
    router.push({ name: 'login', query: { redirect: route.fullPath } })
    return
  }
  institutionStore.toggleFavorite(institutionId.value)
  ElMessage.success(isFavorite.value ? '已取消收藏' : '已添加收藏')
}

const handleBooking = (pkg: ServicePackage) => {
  if (!isAuthenticated.value) {
    ElMessage.warning('请先登录')
    router.push({ name: 'login', query: { redirect: route.fullPath } })
    return
  }
  router.push({
    name: 'bookingCreate',
    params: { institutionId: institutionId.value },
    query: { packageId: pkg.id }
  })
}

// 点击地址跳转到地图导航
const handleAddressClick = () => {
  if (institution.value) {
    openNavigation(
      { lng: institution.value.longitude, lat: institution.value.latitude },
      institution.value.name
    )
  }
}

// 点击电话拨打
const handlePhoneClick = () => {
  if (institution.value?.phone) {
    window.location.href = `tel:${institution.value.phone}`
  }
}

const formatRating = (rating: number) => rating.toFixed(1)

onMounted(() => {
  fetchInstitution()
})
</script>

<template>
  <div class="institution-detail-view">
    <!-- 加载状态 -->
    <div v-if="loading" class="loading-state">
      <el-skeleton :rows="10" animated />
    </div>
    
    <template v-else-if="institution">
      <!-- 头部信息 -->
      <div class="detail-header">
        <div class="header-images">
          <img 
            :src="institution.images[0] || 'https://images.unsplash.com/photo-1548199973-03cce0bbc87b?w=800'" 
            :alt="institution.name"
            class="main-image"
          />
        </div>
        
        <div class="header-info">
          <div class="info-top">
            <div>
              <h1>{{ institution.name }}</h1>
              <div class="rating-row">
                <span class="rating-score">{{ formatRating(institution.rating) }}</span>
                <div class="rating-stars">
                  <span v-for="i in 5" :key="i" :class="['star', { filled: i <= Math.round(institution.rating) }]">★</span>
                </div>
                <span class="review-count">{{ institution.reviewCount }}条评价</span>
              </div>
            </div>
            <button :class="['favorite-btn', { active: isFavorite }]" @click="toggleFavorite">
              <svg width="24" height="24" viewBox="0 0 24 24" :fill="isFavorite ? 'currentColor' : 'none'" stroke="currentColor" stroke-width="2">
                <path d="M20.84 4.61a5.5 5.5 0 0 0-7.78 0L12 5.67l-1.06-1.06a5.5 5.5 0 0 0-7.78 7.78l1.06 1.06L12 21.23l7.78-7.78 1.06-1.06a5.5 5.5 0 0 0 0-7.78z"></path>
              </svg>
            </button>
          </div>
          
          <p class="description">{{ institution.description }}</p>
          
          <div class="info-items">
            <div class="info-item clickable" @click="handleAddressClick">
              <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <path d="M21 10c0 7-9 13-9 13s-9-6-9-13a9 9 0 0 1 18 0z"></path>
                <circle cx="12" cy="10" r="3"></circle>
              </svg>
              <span>{{ institution.address }}</span>
              <svg class="nav-icon" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <polygon points="3 11 22 2 13 21 11 13 3 11"></polygon>
              </svg>
            </div>
            <div class="info-item clickable" @click="handlePhoneClick">
              <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <path d="M22 16.92v3a2 2 0 0 1-2.18 2 19.79 19.79 0 0 1-8.63-3.07 19.5 19.5 0 0 1-6-6 19.79 19.79 0 0 1-3.07-8.67A2 2 0 0 1 4.11 2h3a2 2 0 0 1 2 1.72 12.84 12.84 0 0 0 .7 2.81 2 2 0 0 1-.45 2.11L8.09 9.91a16 16 0 0 0 6 6l1.27-1.27a2 2 0 0 1 2.11-.45 12.84 12.84 0 0 0 2.81.7A2 2 0 0 1 22 16.92z"></path>
              </svg>
              <span>{{ institution.phone }}</span>
            </div>
          </div>
          
          <div class="pet-types">
            <span v-if="institution.petTypes.includes('dog')" class="pet-tag">🐕 接受狗狗</span>
            <span v-if="institution.petTypes.includes('cat')" class="pet-tag">🐱 接受猫咪</span>
            <span v-if="institution.petTypes.includes('other')" class="pet-tag">🐾 接受其他</span>
          </div>
          
          <div class="features">
            <span v-for="feature in institution.features" :key="feature" class="feature-tag">
              {{ feature }}
            </span>
          </div>
        </div>
      </div>
      
      <!-- 标签页 -->
      <el-tabs v-model="activeTab" class="detail-tabs">
        <el-tab-pane label="服务套餐" name="services">
          <div class="packages-grid">
            <AppCard 
              v-for="pkg in institution.servicePackages" 
              :key="pkg.id"
              class="package-card"
              shadow="sm"
            >
              <h3>{{ pkg.name }}</h3>
              <p class="pkg-desc">{{ pkg.description }}</p>
              <ul class="pkg-features">
                <li v-for="feature in pkg.features" :key="feature">✓ {{ feature }}</li>
              </ul>
              <div class="pkg-footer">
                <div class="pkg-price">
                  <span class="price-value">¥{{ pkg.pricePerDay }}</span>
                  <span class="price-unit">/天</span>
                </div>
                <AppButton type="primary" size="sm" @click="handleBooking(pkg)">
                  立即预约
                </AppButton>
              </div>
            </AppCard>
          </div>
        </el-tab-pane>
        
        <el-tab-pane label="用户评价" name="reviews">
          <div v-if="institution.recentReviews?.length" class="reviews-list">
            <div v-for="review in institution.recentReviews" :key="review.id" class="review-item">
              <div class="review-header">
                <div class="reviewer-info">
                  <div class="avatar">{{ review.isAnonymous ? '匿' : '用' }}</div>
                  <div>
                    <div class="reviewer-name">{{ review.isAnonymous ? '匿名用户' : '用户' }}</div>
                    <div class="review-date">{{ new Date(review.createdAt).toLocaleDateString() }}</div>
                  </div>
                </div>
                <div class="review-rating">
                  <span class="rating-value">{{ review.rating.overall.toFixed(1) }}</span>
                  <span class="rating-stars">
                    <span v-for="i in 5" :key="i" :class="['star', { filled: i <= review.rating.overall }]">★</span>
                  </span>
                </div>
              </div>
              <p class="review-content">{{ review.content }}</p>
              <div v-if="review.images?.length" class="review-images">
                <img v-for="(img, idx) in review.images" :key="idx" :src="img" alt="评价图片" />
              </div>
              <div v-if="review.reply" class="review-reply">
                <strong>商家回复：</strong>{{ review.reply.content }}
              </div>
            </div>
          </div>
          <div v-else class="empty-reviews">
            <p>暂无评价</p>
          </div>
        </el-tab-pane>
      </el-tabs>
    </template>
    
    <!-- 错误状态 -->
    <div v-else class="error-state">
      <h2>机构信息加载失败</h2>
      <AppButton type="primary" @click="fetchInstitution">重试</AppButton>
    </div>
  </div>
</template>

<style lang="scss" scoped>
@import '@/styles/variables.scss';

.institution-detail-view {
  max-width: 1000px;
  margin: 0 auto;
}

.loading-state,
.error-state {
  padding: 40px;
  text-align: center;
}

.detail-header {
  display: grid;
  grid-template-columns: 400px 1fr;
  gap: 32px;
  margin-bottom: 32px;
  
  @media (max-width: 768px) {
    grid-template-columns: 1fr;
  }
}

.header-images {
  .main-image {
    width: 100%;
    height: 300px;
    object-fit: cover;
    border-radius: var(--radius-lg);
  }
}

.header-info {
  .info-top {
    display: flex;
    justify-content: space-between;
    align-items: flex-start;
    margin-bottom: 16px;
  }
  
  h1 {
    font-family: var(--font-display);
    font-size: 28px;
    margin: 0 0 8px;
  }
  
  .rating-row {
    display: flex;
    align-items: center;
    gap: 8px;
    
    .rating-score {
      font-size: 20px;
      font-weight: 700;
      color: var(--color-primary);
    }
    
    .rating-stars .star {
      color: $neutral-300;
      &.filled { color: var(--color-warning); }
    }
    
    .review-count {
      color: var(--color-text-muted);
      font-size: 14px;
    }
  }
  
  .description {
    color: var(--color-text-secondary);
    line-height: 1.6;
    margin-bottom: 16px;
  }
  
  .info-items {
    display: flex;
    flex-direction: column;
    gap: 8px;
    margin-bottom: 16px;
  }
  
  .info-item {
    display: flex;
    align-items: center;
    gap: 8px;
    color: var(--color-text-secondary);
    font-size: 14px;
    
    &.clickable {
      cursor: pointer;
      padding: 8px 12px;
      margin: -8px -12px;
      border-radius: var(--radius-md);
      transition: all 0.2s ease;
      
      &:hover {
        background: var(--color-primary-light);
        color: var(--color-primary);
        
        svg {
          color: var(--color-primary);
        }
      }
      
      .nav-icon {
        margin-left: auto;
        opacity: 0.5;
      }
      
      &:hover .nav-icon {
        opacity: 1;
      }
    }
  }
  
  .pet-types, .features {
    display: flex;
    flex-wrap: wrap;
    gap: 8px;
    margin-bottom: 12px;
  }
  
  .pet-tag, .feature-tag {
    padding: 6px 12px;
    background: var(--color-primary-light);
    color: var(--color-primary);
    border-radius: var(--radius-full);
    font-size: 13px;
  }
  
  .feature-tag {
    background: $neutral-100;
    color: var(--color-text-secondary);
  }
}

.favorite-btn {
  width: 48px;
  height: 48px;
  border-radius: 50%;
  border: 2px solid var(--color-border);
  background: white;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  color: var(--color-text-muted);
  transition: all 200ms ease;
  
  &:hover {
    border-color: var(--color-error);
    color: var(--color-error);
  }
  
  &.active {
    border-color: var(--color-error);
    color: var(--color-error);
    background: rgba(239, 68, 68, 0.1);
  }
}

.detail-tabs {
  background: var(--color-surface);
  border-radius: var(--radius-lg);
  padding: 20px;
}

.packages-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
  gap: 20px;
  align-items: stretch;
}

.package-card {
  display: flex;
  flex-direction: column;
  height: 100%;
  min-height: 320px;
  
  :deep(.app-card__body) {
    display: flex;
    flex-direction: column;
    height: 100%;
  }
  
  h3 {
    font-size: 18px;
    margin: 0 0 8px;
    min-height: 27px;
  }
  
  .pkg-desc {
    color: var(--color-text-secondary);
    font-size: 14px;
    margin-bottom: 16px;
    min-height: 42px;
  }
  
  .pkg-features {
    list-style: none;
    padding: 0;
    margin: 0 0 16px;
    flex: 1;
    min-height: 120px;
    
    li {
      padding: 4px 0;
      font-size: 14px;
      color: var(--color-text-secondary);
    }
  }
  
  .pkg-footer {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding-top: 16px;
    border-top: 1px solid var(--color-border);
    margin-top: auto;
  }
  
  .pkg-price {
    .price-value {
      font-size: 24px;
      font-weight: 700;
      color: var(--color-primary);
    }
    .price-unit {
      font-size: 14px;
      color: var(--color-text-muted);
    }
  }
}

.reviews-list {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.review-item {
  padding: 20px;
  background: $neutral-50;
  border-radius: var(--radius-md);
}

.review-header {
  display: flex;
  justify-content: space-between;
  margin-bottom: 12px;
}

.reviewer-info {
  display: flex;
  gap: 12px;
  
  .avatar {
    width: 40px;
    height: 40px;
    border-radius: 50%;
    background: var(--color-primary);
    color: white;
    display: flex;
    align-items: center;
    justify-content: center;
    font-weight: 600;
  }
  
  .reviewer-name {
    font-weight: 500;
  }
  
  .review-date {
    font-size: 12px;
    color: var(--color-text-muted);
  }
}

.review-rating {
  display: flex;
  align-items: center;
  gap: 4px;
  
  .rating-value {
    font-weight: 600;
    color: var(--color-primary);
  }
  
  .star {
    color: $neutral-300;
    font-size: 12px;
    &.filled { color: var(--color-warning); }
  }
}

.review-content {
  color: var(--color-text-primary);
  line-height: 1.6;
  margin-bottom: 12px;
}

.review-images {
  display: flex;
  gap: 8px;
  margin-bottom: 12px;
  
  img {
    width: 80px;
    height: 80px;
    object-fit: cover;
    border-radius: var(--radius-sm);
  }
}

.review-reply {
  padding: 12px;
  background: white;
  border-radius: var(--radius-sm);
  font-size: 14px;
  color: var(--color-text-secondary);
}

.empty-reviews {
  text-align: center;
  padding: 40px;
  color: var(--color-text-muted);
}
</style>
