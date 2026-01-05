<template>
  <div class="map-search-page">
    <!-- 左侧机构列表 -->
    <div class="sidebar" :class="{ collapsed: sidebarCollapsed }">
      <div class="sidebar-header">
        <div class="header-title">
          <MapPin class="title-icon" />
          <h2>附近机构</h2>
        </div>
        <span class="count-badge">{{ institutions.length }}</span>
      </div>

      <!-- 搜索和筛选 -->
      <div class="search-section">
        <el-input
          v-model="searchKeyword"
          placeholder="搜索机构名称"
          clearable
          @keyup.enter="handleSearch"
        >
          <template #prefix>
            <Search class="input-icon" />
          </template>
        </el-input>
        
        <div class="filter-row">
          <el-select v-model="filterRadius" placeholder="范围" size="small">
            <el-option label="不限" :value="0" />
            <el-option label="5公里" :value="5000" />
            <el-option label="10公里" :value="10000" />
            <el-option label="20公里" :value="20000" />
            <el-option label="30公里" :value="30000" />
            <el-option label="50公里" :value="50000" />
          </el-select>
          <el-select v-model="filterPetType" placeholder="宠物类型" size="small" clearable>
            <el-option label="猫咪" value="cat" />
            <el-option label="狗狗" value="dog" />
            <el-option label="其他" value="other" />
          </el-select>
        </div>
      </div>

      <!-- 机构列表 -->
      <div class="institution-list" v-loading="loading">
        <div
          v-for="inst in filteredInstitutions"
          :key="inst.id"
          class="institution-card"
          :class="{ active: selectedId === inst.id }"
          @click="selectInstitution(inst)"
        >
          <div class="card-image">
            <img :src="inst.image || defaultImage" :alt="inst.name" />
            <div class="rating-badge" v-if="inst.rating">
              <Star class="star-icon" />
              {{ inst.rating.toFixed(1) }}
            </div>
          </div>
          <div class="card-content">
            <h3 class="inst-name">{{ inst.name }}</h3>
            <p class="inst-address">
              <MapPin class="addr-icon" />
              {{ inst.address }}
            </p>
            <div class="inst-tags">
              <el-tag 
                v-for="type in inst.petTypes?.slice(0, 2)" 
                :key="type" 
                size="small"
                :type="getPetTypeTag(type)"
              >
                {{ getPetTypeName(type) }}
              </el-tag>
            </div>
            <div class="inst-footer">
              <span class="price">{{ inst.priceRange || '¥58起/天' }}</span>
              <span class="distance" v-if="inst.distance">
                {{ formatDistance(inst.distance) }}
              </span>
            </div>
          </div>
        </div>

        <el-empty v-if="!loading && filteredInstitutions.length === 0" description="暂无机构" />
      </div>

      <!-- 折叠按钮 -->
      <div class="collapse-btn" @click="sidebarCollapsed = !sidebarCollapsed">
        <ChevronLeft v-if="!sidebarCollapsed" />
        <ChevronRight v-else />
      </div>
    </div>

    <!-- 右侧地图 -->
    <div class="map-area">
      <!-- 地图工具栏 -->
      <div class="map-toolbar">
        <el-button @click="locateMe" :loading="locating" circle>
          <Navigation class="btn-icon" />
        </el-button>
        <el-button @click="zoomIn" circle>
          <Plus class="btn-icon" />
        </el-button>
        <el-button @click="zoomOut" circle>
          <Minus class="btn-icon" />
        </el-button>
      </div>

      <!-- 地图容器 -->
      <div ref="mapRef" class="map-container">
        <div v-if="mapLoading" class="map-loading">
          <Loader2 class="loading-spinner" />
          <span>地图加载中...</span>
        </div>
      </div>

      <!-- 选中机构详情卡片 -->
      <transition name="slide-up">
        <div v-if="selectedInstitution" class="detail-card">
          <button class="close-btn" @click="selectedInstitution = null">
            <X />
          </button>
          <div class="detail-header">
            <img :src="selectedInstitution.image || defaultImage" class="detail-image" />
            <div class="detail-info">
              <h3>{{ selectedInstitution.name }}</h3>
              <div class="detail-rating">
                <Star class="star-icon filled" />
                <span>{{ selectedInstitution.rating?.toFixed(1) || 'N/A' }}</span>
                <span class="review-count">{{ selectedInstitution.reviewCount || 0 }}条评价</span>
              </div>
              <p class="detail-address">{{ selectedInstitution.address }}</p>
            </div>
          </div>
          <div class="detail-actions">
            <el-button type="primary" @click="viewDetail(selectedInstitution)">
              <Eye class="btn-icon" />
              查看详情
            </el-button>
            <el-button @click="openNavigation(selectedInstitution)">
              <Navigation class="btn-icon" />
              导航前往
            </el-button>
            <el-button @click="callInstitution(selectedInstitution)">
              <Phone class="btn-icon" />
              电话咨询
            </el-button>
          </div>
        </div>
      </transition>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import {
  MapPin, Search, Star, ChevronLeft, ChevronRight, Loader2,
  Navigation, Plus, Minus, X, Eye, Phone
} from 'lucide-vue-next'
import { institutionApi } from '@/api/institution'
import {
  MapService,
  getCurrentPosition,
  formatDistance as formatDist,
  openNavigation as openNav,
  type Coordinate,
  type MapMarker
} from '@/utils/mapService'

interface Institution {
  id: string
  name: string
  address: string
  location: Coordinate
  rating?: number
  reviewCount?: number
  distance?: number
  petTypes?: string[]
  priceRange?: string
  image?: string
  phone?: string
}

const router = useRouter()
const mapRef = ref<HTMLElement>()
const mapService = new MapService()

// 状态
const loading = ref(false)
const mapLoading = ref(true)
const locating = ref(false)
const sidebarCollapsed = ref(false)
const searchKeyword = ref('')
const filterRadius = ref(0) // 默认不限
const filterPetType = ref('')
const selectedId = ref<string | null>(null)
const selectedInstitution = ref<Institution | null>(null)
const institutions = ref<Institution[]>([])
const userLocation = ref<Coordinate | null>(null)

const defaultImage = 'https://images.unsplash.com/photo-1548199973-03cce0bbc87b?w=400'

// 筛选后的机构列表
const filteredInstitutions = computed(() => {
  let result = institutions.value

  if (searchKeyword.value) {
    const keyword = searchKeyword.value.toLowerCase()
    result = result.filter(inst => 
      inst.name.toLowerCase().includes(keyword) ||
      inst.address.toLowerCase().includes(keyword)
    )
  }

  if (filterPetType.value) {
    result = result.filter(inst => inst.petTypes?.includes(filterPetType.value))
  }

  return result
})

// 格式化距离
const formatDistance = (meters: number) => formatDist(meters)

// 获取宠物类型名称
const getPetTypeName = (type: string) => {
  const map: Record<string, string> = { cat: '猫咪', dog: '狗狗', other: '其他' }
  return map[type] || type
}

// 获取宠物类型标签样式
const getPetTypeTag = (type: string) => {
  const map: Record<string, string> = { cat: 'warning', dog: 'success', other: 'info' }
  return map[type] || ''
}

// 初始化地图
const initMap = async () => {
  if (!mapRef.value) return

  try {
    mapRef.value.id = 'map-' + Date.now()
    // 广州市中心坐标（天河区）
    const center = userLocation.value || { lng: 113.3245, lat: 23.1065 }
    await mapService.init(mapRef.value.id, { center, zoom: 12 })
    mapLoading.value = false
    addMarkers(institutions.value)
  } catch (error) {
    console.error('Failed to init map:', error)
    mapLoading.value = false
  }
}

// 加载机构数据
const loadInstitutions = async () => {
  loading.value = true
  try {
    const res = await institutionApi.search({ page: 1, pageSize: 50 })
    if (res.code === 200 && res.data) {
      const list = res.data.list || res.data.items || []
      institutions.value = list.map((inst: any) => ({
        id: inst.id,
        name: inst.name,
        address: inst.address,
        location: { lng: inst.longitude, lat: inst.latitude },
        rating: inst.rating,
        reviewCount: inst.reviewCount,
        distance: inst.distance || 0,
        petTypes: inst.petTypes,
        priceRange: '¥58-288/天',
        image: inst.images?.[0] || inst.logo,
        phone: inst.phone
      }))
      if (!mapLoading.value) {
        addMarkers(institutions.value)
      }
    }
  } catch (error) {
    console.error('Failed to load institutions:', error)
  } finally {
    loading.value = false
  }
}

// 添加地图标记
const addMarkers = (insts: Institution[]) => {
  mapService.clearMarkers()
  insts.forEach(inst => {
    // 跳过无效经纬度的机构
    if (!inst.location || isNaN(inst.location.lng) || isNaN(inst.location.lat)) {
      return
    }
    const marker: MapMarker = {
      id: inst.id,
      position: inst.location,
      title: inst.name,
      data: inst as unknown as Record<string, unknown>
    }
    mapService.addMarker(marker, () => selectInstitution(inst))
  })
  if (insts.length > 0) {
    mapService.fitView()
  }
}

// 选择机构
const selectInstitution = (inst: Institution) => {
  selectedId.value = inst.id
  selectedInstitution.value = inst
  // 检查经纬度是否有效
  if (inst.location && !isNaN(inst.location.lng) && !isNaN(inst.location.lat)) {
    mapService.setCenter(inst.location, 15)
  }
}

// 搜索
const handleSearch = () => {
  addMarkers(filteredInstitutions.value)
}

// 定位
const locateMe = async () => {
  locating.value = true
  try {
    const pos = await getCurrentPosition()
    userLocation.value = pos
    mapService.setCenter(pos, 14)
    ElMessage.success('定位成功')
  } catch (error) {
    ElMessage.error('定位失败，请检查定位权限')
  } finally {
    locating.value = false
  }
}

// 缩放
const zoomIn = () => mapService.zoomIn?.()
const zoomOut = () => mapService.zoomOut?.()

// 查看详情
const viewDetail = (inst: Institution) => {
  router.push(`/institutions/${inst.id}`)
}

// 导航
const openNavigation = (inst: Institution) => {
  openNav(inst.location, inst.name, userLocation.value || undefined)
}

// 电话咨询
const callInstitution = (inst: Institution) => {
  if (inst.phone) {
    window.location.href = `tel:${inst.phone}`
  } else {
    ElMessage.info('暂无联系电话')
  }
}

onMounted(() => {
  loadInstitutions()
  initMap()
})

onUnmounted(() => {
  mapService.destroy()
})
</script>


<style scoped lang="scss">
.map-search-page {
  display: flex;
  height: calc(100vh - 60px);
  background: #f5f7fa;
}

// 左侧边栏
.sidebar {
  width: 380px;
  background: #fff;
  display: flex;
  flex-direction: column;
  box-shadow: 2px 0 12px rgba(0, 0, 0, 0.08);
  position: relative;
  z-index: 10;
  transition: width 0.3s ease, margin 0.3s ease;

  &.collapsed {
    width: 0;
    overflow: hidden;

    .collapse-btn {
      left: 0;
      border-radius: 0 8px 8px 0;
    }
  }
}

.sidebar-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 20px;
  border-bottom: 1px solid #eef2f7;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: #fff;

  .header-title {
    display: flex;
    align-items: center;
    gap: 10px;

    .title-icon {
      width: 22px;
      height: 22px;
    }

    h2 {
      margin: 0;
      font-size: 18px;
      font-weight: 600;
    }
  }

  .count-badge {
    background: rgba(255, 255, 255, 0.2);
    padding: 4px 12px;
    border-radius: 20px;
    font-size: 14px;
  }
}

.search-section {
  padding: 16px;
  border-bottom: 1px solid #eef2f7;

  .el-input {
    margin-bottom: 12px;
  }

  .filter-row {
    display: flex;
    gap: 10px;

    .el-select {
      flex: 1;
    }
  }
}

.input-icon {
  width: 16px;
  height: 16px;
  color: #999;
}

// 机构列表
.institution-list {
  flex: 1;
  overflow-y: auto;
  padding: 12px;

  &::-webkit-scrollbar {
    width: 6px;
  }

  &::-webkit-scrollbar-thumb {
    background: #ddd;
    border-radius: 3px;
  }
}

.institution-card {
  background: #fff;
  border-radius: 12px;
  overflow: hidden;
  margin-bottom: 12px;
  cursor: pointer;
  border: 2px solid transparent;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
  transition: all 0.2s ease;

  &:hover {
    box-shadow: 0 4px 16px rgba(0, 0, 0, 0.1);
    transform: translateY(-2px);
  }

  &.active {
    border-color: #667eea;
    box-shadow: 0 4px 16px rgba(102, 126, 234, 0.3);
  }
}

.card-image {
  position: relative;
  height: 120px;
  overflow: hidden;

  img {
    width: 100%;
    height: 100%;
    object-fit: cover;
  }

  .rating-badge {
    position: absolute;
    top: 10px;
    right: 10px;
    background: rgba(0, 0, 0, 0.7);
    color: #fff;
    padding: 4px 10px;
    border-radius: 20px;
    font-size: 13px;
    display: flex;
    align-items: center;
    gap: 4px;

    .star-icon {
      width: 14px;
      height: 14px;
      color: #ffc107;
      fill: #ffc107;
    }
  }
}

.card-content {
  padding: 14px;
}

.inst-name {
  margin: 0 0 8px;
  font-size: 16px;
  font-weight: 600;
  color: #333;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.inst-address {
  display: flex;
  align-items: flex-start;
  gap: 4px;
  margin: 0 0 10px;
  font-size: 13px;
  color: #888;
  line-height: 1.4;

  .addr-icon {
    width: 14px;
    height: 14px;
    flex-shrink: 0;
    margin-top: 2px;
  }
}

.inst-tags {
  display: flex;
  gap: 6px;
  margin-bottom: 10px;

  .el-tag {
    border-radius: 4px;
  }
}

.inst-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;

  .price {
    color: #ff6b35;
    font-weight: 600;
    font-size: 15px;
  }

  .distance {
    color: #999;
    font-size: 13px;
  }
}

// 折叠按钮
.collapse-btn {
  position: absolute;
  right: -28px;
  top: 50%;
  transform: translateY(-50%);
  width: 28px;
  height: 56px;
  background: #fff;
  border-radius: 0 8px 8px 0;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  box-shadow: 2px 0 8px rgba(0, 0, 0, 0.1);
  color: #666;
  transition: all 0.2s;

  &:hover {
    background: #f5f5f5;
    color: #667eea;
  }
}

// 地图区域
.map-area {
  flex: 1;
  position: relative;
}

.map-container {
  width: 100%;
  height: 100%;
  background: #e8ecf1;
}

.map-loading {
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 12px;
  color: #666;

  .loading-spinner {
    width: 40px;
    height: 40px;
    animation: spin 1s linear infinite;
  }
}

@keyframes spin {
  from { transform: rotate(0deg); }
  to { transform: rotate(360deg); }
}

// 地图工具栏
.map-toolbar {
  position: absolute;
  top: 20px;
  right: 20px;
  z-index: 100;
  display: flex;
  flex-direction: column;
  gap: 8px;

  .el-button {
    width: 44px;
    height: 44px;
    background: #fff;
    box-shadow: 0 2px 12px rgba(0, 0, 0, 0.15);

    &:hover {
      background: #f5f5f5;
    }
  }

  .btn-icon {
    width: 20px;
    height: 20px;
  }
}

// 详情卡片
.detail-card {
  position: absolute;
  bottom: 20px;
  left: 20px;
  right: 20px;
  background: #fff;
  border-radius: 16px;
  padding: 20px;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.15);
  z-index: 100;

  .close-btn {
    position: absolute;
    top: 12px;
    right: 12px;
    width: 32px;
    height: 32px;
    border: none;
    background: #f5f5f5;
    border-radius: 50%;
    cursor: pointer;
    display: flex;
    align-items: center;
    justify-content: center;
    color: #666;
    transition: all 0.2s;

    &:hover {
      background: #eee;
      color: #333;
    }
  }
}

.detail-header {
  display: flex;
  gap: 16px;
  margin-bottom: 16px;
}

.detail-image {
  width: 100px;
  height: 100px;
  border-radius: 12px;
  object-fit: cover;
}

.detail-info {
  flex: 1;

  h3 {
    margin: 0 0 8px;
    font-size: 18px;
    font-weight: 600;
  }
}

.detail-rating {
  display: flex;
  align-items: center;
  gap: 6px;
  margin-bottom: 8px;
  font-size: 14px;

  .star-icon {
    width: 16px;
    height: 16px;
    color: #ffc107;

    &.filled {
      fill: #ffc107;
    }
  }

  .review-count {
    color: #999;
    margin-left: 4px;
  }
}

.detail-address {
  margin: 0;
  font-size: 13px;
  color: #666;
}

.detail-actions {
  display: flex;
  gap: 12px;

  .el-button {
    flex: 1;
    display: flex;
    align-items: center;
    justify-content: center;
    gap: 6px;
  }

  .btn-icon {
    width: 16px;
    height: 16px;
  }
}

// 动画
.slide-up-enter-active,
.slide-up-leave-active {
  transition: all 0.3s ease;
}

.slide-up-enter-from,
.slide-up-leave-to {
  opacity: 0;
  transform: translateY(20px);
}

// 响应式
@media (max-width: 768px) {
  .sidebar {
    position: absolute;
    left: 0;
    top: 0;
    bottom: 0;
    width: 320px;
    z-index: 200;

    &.collapsed {
      transform: translateX(-100%);
    }
  }

  .collapse-btn {
    right: -40px;
    width: 40px;
  }

  .detail-card {
    left: 10px;
    right: 10px;
    bottom: 10px;
    padding: 16px;
  }

  .detail-header {
    flex-direction: column;
    align-items: center;
    text-align: center;
  }

  .detail-actions {
    flex-wrap: wrap;

    .el-button {
      flex: 1 1 calc(50% - 6px);
    }
  }
}
</style>
