<template>
  <div class="map-marker-card">
    <button class="close-btn" @click="$emit('close')">×</button>
    
    <div class="card-header">
      <div class="inst-image">
        <img 
          v-if="institution.image" 
          :src="institution.image" 
          :alt="institution.name"
        />
        <div v-else class="image-placeholder">🏠</div>
      </div>
      <div class="inst-info">
        <h3>{{ institution.name }}</h3>
        <div class="inst-rating">
          <Star class="star-icon" />
          <span>{{ institution.rating?.toFixed(1) || 'N/A' }}</span>
        </div>
      </div>
    </div>
    
    <div class="card-body">
      <div class="info-row">
        <MapPin class="info-icon" />
        <span>{{ institution.address }}</span>
      </div>
      <div v-if="distance" class="info-row">
        <Navigation class="info-icon" />
        <span>距您 {{ distance }}</span>
      </div>
      <div v-if="institution.priceRange" class="info-row">
        <DollarSign class="info-icon" />
        <span>{{ institution.priceRange }}</span>
      </div>
      <div v-if="institution.petTypes?.length" class="pet-types">
        <span v-if="institution.petTypes.includes('cat')" class="pet-tag">🐱 猫咪</span>
        <span v-if="institution.petTypes.includes('dog')" class="pet-tag">🐕 狗狗</span>
        <span v-if="institution.petTypes.includes('small')" class="pet-tag">🐰 小宠</span>
      </div>
    </div>
    
    <div class="card-footer">
      <el-button @click="$emit('navigate', institution)">
        <Navigation class="btn-icon" />
        导航
      </el-button>
      <el-button type="primary" @click="$emit('view-detail', institution)">
        查看详情
      </el-button>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { Star, MapPin, Navigation, DollarSign } from 'lucide-vue-next'
import { formatDistance, calculateDistance, type Coordinate } from '@/utils/mapService'

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
  institution: Institution
  userLocation?: Coordinate | null
}>()

defineEmits<{
  (e: 'close'): void
  (e: 'navigate', institution: Institution): void
  (e: 'view-detail', institution: Institution): void
}>()

const distance = computed(() => {
  if (props.institution.distance) {
    return formatDistance(props.institution.distance)
  }
  if (props.userLocation) {
    const dist = calculateDistance(props.userLocation, props.institution.location)
    return formatDistance(dist)
  }
  return null
})
</script>

<style scoped lang="scss">
.map-marker-card {
  position: absolute;
  bottom: 24px;
  left: 50%;
  transform: translateX(-50%);
  width: 360px;
  max-width: calc(100% - 48px);
  background: white;
  border-radius: 16px;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.15);
  z-index: 100;
  overflow: hidden;
}

.close-btn {
  position: absolute;
  top: 12px;
  right: 12px;
  width: 28px;
  height: 28px;
  border: none;
  background: rgba(0, 0, 0, 0.05);
  border-radius: 50%;
  font-size: 18px;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #666;
  z-index: 1;
  
  &:hover {
    background: rgba(0, 0, 0, 0.1);
  }
}

.card-header {
  display: flex;
  gap: 16px;
  padding: 16px;
  background: linear-gradient(135deg, #fff9f5 0%, #fff0e8 100%);
}

.inst-image {
  width: 72px;
  height: 72px;
  border-radius: 12px;
  overflow: hidden;
  flex-shrink: 0;
  
  img {
    width: 100%;
    height: 100%;
    object-fit: cover;
  }
  
  .image-placeholder {
    width: 100%;
    height: 100%;
    background: white;
    display: flex;
    align-items: center;
    justify-content: center;
    font-size: 32px;
  }
}

.inst-info {
  flex: 1;
  min-width: 0;
  
  h3 {
    font-size: 17px;
    font-weight: 600;
    margin: 0 0 8px;
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
    padding-right: 24px;
  }
}

.inst-rating {
  display: flex;
  align-items: center;
  gap: 4px;
  color: #ff9500;
  font-weight: 600;
  
  .star-icon {
    width: 16px;
    height: 16px;
    fill: currentColor;
  }
}

.card-body {
  padding: 16px;
}

.info-row {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 10px;
  font-size: 14px;
  color: #666;
  
  .info-icon {
    width: 16px;
    height: 16px;
    color: #999;
    flex-shrink: 0;
  }
  
  span {
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
  }
}

.pet-types {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
  margin-top: 12px;
}

.pet-tag {
  padding: 4px 10px;
  background: #f5f5f5;
  border-radius: 12px;
  font-size: 12px;
  color: #666;
}

.card-footer {
  display: flex;
  gap: 12px;
  padding: 16px;
  border-top: 1px solid #eee;
  
  .el-button {
    flex: 1;
  }
  
  .btn-icon {
    width: 14px;
    height: 14px;
    margin-right: 4px;
  }
}
</style>
