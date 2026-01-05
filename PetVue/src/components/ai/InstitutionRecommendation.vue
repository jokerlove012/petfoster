<template>
  <div class="institution-recommendation">
    <div class="recommendation-header">
      <Sparkles :size="16" />
      <span>为您推荐</span>
    </div>
    <div class="recommendation-list">
      <div
        v-for="inst in institutions"
        :key="inst.id"
        class="recommendation-card"
        @click="$emit('select', inst)"
      >
        <div class="card-image">
          <img :src="inst.image || defaultImage" :alt="inst.name" />
          <div class="rating-badge">
            <Star :size="12" />
            {{ inst.rating.toFixed(1) }}
          </div>
        </div>
        <div class="card-info">
          <h4>{{ inst.name }}</h4>
          <p class="distance">
            <MapPin :size="12" />
            {{ inst.distance }}km
          </p>
          <p class="price">
            <span class="price-value">¥{{ inst.price }}</span>/天起
          </p>
        </div>
      </div>
    </div>
    <button class="view-more-btn" @click="$emit('viewMore')">
      查看更多机构
      <ChevronRight :size="16" />
    </button>
  </div>
</template>

<script setup lang="ts">
import { Sparkles, Star, MapPin, ChevronRight } from 'lucide-vue-next'

interface RecommendedInstitution {
  id: string
  name: string
  image?: string
  rating: number
  distance: number
  price: number
}

defineProps<{
  institutions: RecommendedInstitution[]
}>()

defineEmits<{
  (e: 'select', institution: RecommendedInstitution): void
  (e: 'viewMore'): void
}>()

const defaultImage = 'https://via.placeholder.com/120x80?text=Pet+Foster'
</script>

<style scoped lang="scss">
.institution-recommendation {
  background: var(--color-surface);
  border: 1px solid var(--color-border);
  border-radius: var(--radius-lg);
  overflow: hidden;
}

.recommendation-header {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 10px 12px;
  background: linear-gradient(
    135deg,
    var(--color-primary-light) 0%,
    var(--color-accent-light) 100%
  );
  color: var(--color-primary);
  font-size: 13px;
  font-weight: 600;
}

.recommendation-list {
  padding: 12px;
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.recommendation-card {
  display: flex;
  gap: 10px;
  padding: 8px;
  background: var(--color-neutral-50);
  border-radius: var(--radius-md);
  cursor: pointer;
  transition: all 0.2s;

  &:hover {
    background: var(--color-primary-light);
  }
}

.card-image {
  position: relative;
  flex-shrink: 0;

  img {
    width: 60px;
    height: 60px;
    border-radius: var(--radius-sm);
    object-fit: cover;
  }

  .rating-badge {
    position: absolute;
    bottom: 4px;
    left: 4px;
    display: flex;
    align-items: center;
    gap: 2px;
    padding: 2px 6px;
    background: rgba(0, 0, 0, 0.7);
    border-radius: 10px;
    color: #ffc107;
    font-size: 10px;
    font-weight: 600;
  }
}

.card-info {
  flex: 1;
  min-width: 0;

  h4 {
    margin: 0 0 4px;
    font-size: 13px;
    font-weight: 600;
    color: var(--color-text-primary);
    white-space: nowrap;
    overflow: hidden;
    text-overflow: ellipsis;
  }

  .distance {
    display: flex;
    align-items: center;
    gap: 4px;
    margin: 0 0 2px;
    font-size: 11px;
    color: var(--color-text-muted);
  }

  .price {
    margin: 0;
    font-size: 11px;
    color: var(--color-text-secondary);

    .price-value {
      font-size: 14px;
      font-weight: 700;
      color: var(--color-primary);
    }
  }
}

.view-more-btn {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 4px;
  width: 100%;
  padding: 10px;
  background: transparent;
  border: none;
  border-top: 1px solid var(--color-border);
  color: var(--color-primary);
  font-size: 13px;
  cursor: pointer;
  transition: background 0.2s;

  &:hover {
    background: var(--color-primary-light);
  }
}
</style>
