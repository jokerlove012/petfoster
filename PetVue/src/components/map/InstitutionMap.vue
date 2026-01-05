<template>
  <div class="institution-map">
    <!-- 搜索栏 -->
    <div class="map-search-bar">
      <el-input
        v-model="searchKeyword"
        placeholder="搜索机构名称或地址"
        clearable
        @keyup.enter="handleSearch"
      >
        <template #prefix>
          <Search class="search-icon" />
        </template>
      </el-input>
      <el-button type="primary" @click="handleSearch" :loading="searching">
        搜索
      </el-button>
      <el-button @click="locateMe" :loading="locating">
        <MapPin class="btn-icon" />
        定位
      </el-button>
    </div>

    <!-- 筛选条件 -->
    <div class="map-filters">
      <el-select v-model="filterRadius" placeholder="搜索范围" @change="handleRadiusChange">
        <el-option label="1公里内" :value="1000" />
        <el-option label="3公里内" :value="3000" />
        <el-option label="5公里内" :value="5000" />
        <el-option label="10公里内" :value="10000" />
      </el-select>
      <el-select v-model="filterPetType" placeholder="宠物类型" clearable>
        <el-option label="猫咪" value="cat" />
        <el-option label="狗狗" value="dog" />
        <el-option label="小宠" value="small" />
      </el-select>
    </div>

    <!-- 地图容器 -->
    <div class="map-container">
      <div ref="mapRef" class="map-view" :class="{ loading: mapLoading }">
        <div v-if="mapLoading" class="map-loading">
          <Loader2 class="loading-icon" />
          <span>地图加载中...</span>
        </div>
      </div>

      <!-- 机构列表侧边栏 -->
      <div class="map-sidebar" :class="{ collapsed: sidebarCollapsed }">
        <div class="sidebar-toggle" @click="sidebarCollapsed = !sidebarCollapsed">
          <ChevronLeft v-if="!sidebarCollapsed" />
          <ChevronRight v-else />
        </div>
        <div class="sidebar-content">
          <div class="sidebar-header">
            <h3>附近机构 ({{ institutions.length }})</h3>
          </div>
          <div class="institution-list">
            <div
              v-for="inst in institutions"
              :key="inst.id"
              class="institution-item"
              :class="{ active: selectedId === inst.id }"
              @click="selectInstitution(inst)"
            >
              <div class="inst-info">
                <h4>{{ inst.name }}</h4>
                <p class="inst-address">{{ inst.address }}</p>
                <div class="inst-meta">
                  <span class="inst-rating">
                    <Star class="star-icon" />
                    {{ inst.rating?.toFixed(1) || 'N/A' }}
                  </span>
                  <span class="inst-distance">{{ formatDist(inst.distance) }}</span>
                </div>
              </div>
            </div>
            <div v-if="institutions.length === 0 && !searching" class="empty-list">
              <MapPin class="empty-icon" />
              <p>暂无附近机构</p>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 选中机构卡片 -->
    <MapMarkerCard
      v-if="selectedInstitution"
      :institution="selectedInstitution"
      :user-location="userLocation"
      @close="selectedInstitution = null"
      @navigate="handleNavigate"
      @view-detail="handleViewDetail"
    />
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, onUnmounted, watch } from 'vue'
import { useRouter } from 'vue-router'
import { Search, MapPin, Star, ChevronLeft, ChevronRight, Loader2 } from 'lucide-vue-next'
import {
  MapService,
  getCurrentPosition,
  formatDistance,
  openNavigation,
  type Coordinate,
  type MapMarker
} from '@/utils/mapService'
import MapMarkerCard from './MapMarkerCard.vue'

interface Institution {
  id: string
  name: string
  address: string
  location: Coordinate
  rating?: number
  distance?: number
  petTypes?: string[]
  priceRange?: string
  image?: string
}

const props = defineProps<{
  initialCenter?: Coordinate
  initialInstitutions?: Institution[]
}>()

const emit = defineEmits<{
  (e: 'select', institution: Institution): void
  (e: 'search', keyword: string): void
}>()

const router = useRouter()
const mapRef = ref<HTMLElement>()
const mapService = new MapService()

// 状态
const mapLoading = ref(true)
const searching = ref(false)
const locating = ref(false)
const sidebarCollapsed = ref(false)
const searchKeyword = ref('')
const filterRadius = ref(5000)
const filterPetType = ref('')
const userLocation = ref<Coordinate | null>(null)
const selectedId = ref<string | null>(null)
const selectedInstitution = ref<Institution | null>(null)
const institutions = ref<Institution[]>([])

// 格式化距离
const formatDist = (meters?: number) => {
  if (!meters) return ''
  return formatDistance(meters)
}

// 初始化地图
const initMap = async () => {
  if (!mapRef.value) return

  try {
    const center = props.initialCenter || userLocation.value || { lng: 116.397428, lat: 39.90923 }
    await mapService.init(mapRef.value.id || 'map-container', { center, zoom: 14 })
    mapLoading.value = false

    // 添加初始机构标记
    if (props.initialInstitutions?.length) {
      institutions.value = props.initialInstitutions
      addMarkers(props.initialInstitutions)
    }
  } catch (error) {
    console.error('Failed to init map:', error)
    mapLoading.value = false
  }
}

// 添加标记点
const addMarkers = (insts: Institution[]) => {
  mapService.clearMarkers()
  insts.forEach((inst) => {
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
  mapService.setCenter(inst.location, 16)
  emit('select', inst)
}

// 搜索
const handleSearch = async () => {
  if (!searchKeyword.value.trim()) return

  searching.value = true
  emit('search', searchKeyword.value)

  try {
    // TODO: 对接真实API
    // const res = await institutionApi.searchNearby({
    //   keyword: searchKeyword.value,
    //   location: userLocation.value,
    //   radius: filterRadius.value
    // })
    // institutions.value = res.data
    // addMarkers(res.data)
    
    // 暂时显示空结果
    institutions.value = []
  } finally {
    searching.value = false
  }
}

// 定位
const locateMe = async () => {
  locating.value = true
  try {
    const pos = await getCurrentPosition()
    userLocation.value = pos
    mapService.setCenter(pos, 15)
  } catch (error) {
    console.error('Location error:', error)
  } finally {
    locating.value = false
  }
}

// 范围变化
const handleRadiusChange = () => {
  if (searchKeyword.value) {
    handleSearch()
  }
}

// 导航
const handleNavigate = (inst: Institution) => {
  openNavigation(inst.location, inst.name, userLocation.value || undefined)
}

// 查看详情
const handleViewDetail = (inst: Institution) => {
  router.push(`/institution/${inst.id}`)
}

// 监听筛选条件
watch(filterPetType, () => {
  if (filterPetType.value) {
    const filtered = institutions.value.filter(
      (inst) => inst.petTypes?.includes(filterPetType.value)
    )
    addMarkers(filtered)
  } else {
    addMarkers(institutions.value)
  }
})

onMounted(() => {
  // 设置地图容器 ID
  if (mapRef.value) {
    mapRef.value.id = 'map-container-' + Date.now()
  }
  initMap()
})

onUnmounted(() => {
  mapService.destroy()
})
</script>

<style scoped lang="scss">
.institution-map {
  display: flex;
  flex-direction: column;
  height: 100%;
  min-height: 500px;
}

.map-search-bar {
  display: flex;
  gap: 12px;
  padding: 16px;
  background: var(--bg-color, #fff);
  border-bottom: 1px solid var(--border-color, #eee);

  .el-input {
    flex: 1;
    max-width: 400px;
  }

  .search-icon,
  .btn-icon {
    width: 16px;
    height: 16px;
  }
}

.map-filters {
  display: flex;
  gap: 12px;
  padding: 12px 16px;
  background: var(--bg-color, #fff);
  border-bottom: 1px solid var(--border-color, #eee);

  .el-select {
    width: 140px;
  }
}

.map-container {
  flex: 1;
  display: flex;
  position: relative;
  overflow: hidden;
}

.map-view {
  flex: 1;
  min-height: 400px;
  background: #f5f5f5;

  &.loading {
    display: flex;
    align-items: center;
    justify-content: center;
  }
}

.map-loading {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 12px;
  color: #999;

  .loading-icon {
    font-size: 32px;
    animation: spin 1s linear infinite;
  }
}

@keyframes spin {
  from {
    transform: rotate(0deg);
  }
  to {
    transform: rotate(360deg);
  }
}

.map-sidebar {
  width: 320px;
  background: var(--bg-color, #fff);
  border-left: 1px solid var(--border-color, #eee);
  transition: width 0.3s ease;
  position: relative;

  &.collapsed {
    width: 0;

    .sidebar-content {
      opacity: 0;
      pointer-events: none;
    }
  }
}

.sidebar-toggle {
  position: absolute;
  left: -24px;
  top: 50%;
  transform: translateY(-50%);
  width: 24px;
  height: 48px;
  background: var(--bg-color, #fff);
  border: 1px solid var(--border-color, #eee);
  border-right: none;
  border-radius: 4px 0 0 4px;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  color: #666;

  &:hover {
    background: #f5f5f5;
  }
}

.sidebar-content {
  height: 100%;
  display: flex;
  flex-direction: column;
  transition: opacity 0.2s ease;
}

.sidebar-header {
  padding: 16px;
  border-bottom: 1px solid var(--border-color, #eee);

  h3 {
    margin: 0;
    font-size: 16px;
    font-weight: 600;
  }
}

.institution-list {
  flex: 1;
  overflow-y: auto;
}

.institution-item {
  padding: 16px;
  border-bottom: 1px solid var(--border-color, #eee);
  cursor: pointer;
  transition: background 0.2s ease;

  &:hover {
    background: #f9f9f9;
  }

  &.active {
    background: #fff7f5;
    border-left: 3px solid var(--primary-color, #ff6b35);
  }
}

.inst-info {
  h4 {
    margin: 0 0 8px;
    font-size: 15px;
    font-weight: 600;
  }
}

.inst-address {
  margin: 0 0 8px;
  font-size: 13px;
  color: #666;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.inst-meta {
  display: flex;
  align-items: center;
  gap: 16px;
  font-size: 13px;
}

.inst-rating {
  display: flex;
  align-items: center;
  gap: 4px;
  color: #ff9500;

  .star-icon {
    width: 14px;
    height: 14px;
    fill: currentColor;
  }
}

.inst-distance {
  color: #999;
}

.empty-list {
  padding: 48px 16px;
  text-align: center;
  color: #999;

  .empty-icon {
    width: 48px;
    height: 48px;
    margin-bottom: 12px;
    opacity: 0.5;
  }

  p {
    margin: 0;
  }
}

@media (max-width: 768px) {
  .map-search-bar {
    flex-wrap: wrap;

    .el-input {
      width: 100%;
      max-width: none;
    }
  }

  .map-sidebar {
    position: absolute;
    right: 0;
    top: 0;
    bottom: 0;
    z-index: 10;
    box-shadow: -2px 0 8px rgba(0, 0, 0, 0.1);
  }
}
</style>
