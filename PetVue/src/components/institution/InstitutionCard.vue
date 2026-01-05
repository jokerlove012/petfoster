<script setup lang="ts">
import { computed } from 'vue'
import type { InstitutionWithDistance } from '@/types/institution'
import { useInstitutionStore } from '@/stores'

interface Props {
  institution: InstitutionWithDistance
}

const props = defineProps<Props>()
const institutionStore = useInstitutionStore()

const isFavorite = computed(() => institutionStore.favorites.includes(props.institution.id))

const petTypeLabels: Record<string, string> = {
  dog: '🐕 狗狗',
  cat: '🐱 猫咪',
  other: '🐾 其他'
}

const petTypes = computed(() => 
  props.institution.petTypes.map(t => petTypeLabels[t] || t).join(' ')
)

const coverImage = computed(() => 
  props.institution.images[0] || 'https://images.unsplash.com/photo-1548199973-03cce0bbc87b?w=400'
)

const toggleFavorite = (e: Event) => {
  e.preventDefault()
  e.stopPropagation()
  institutionStore.toggleFavorite(props.institution.id)
}
</script>

<template>
  <router-link 
    :to="`/institutions/${institution.id}`" 
    class="institution-card"
  >
    <div class="card-image">
      <img :src="coverImage" :alt="institution.name" />
      <button 
        :class="['favorite-btn', { active: isFavorite }]"
        @click="toggleFavorite"
      >
        <svg width="20" height="20" viewBox="0 0 24 24" :fill="isFavorite ? 'currentColor' : 'none'" stroke="currentColor" stroke-width="2">
          <path d="M20.84 4.61a5.5 5.5 0 0 0-7.78 0L12 5.67l-1.06-1.06a5.5 5.5 0 0 0-7.78 7.78l1.06 1.06L12 21.23l7.78-7.78 1.06-1.06a5.5 5.5 0 0 0 0-7.78z"></path>
        </svg>
      </button>
      <div v-if="institution.verified" class="verified-badge">
        <svg width="14" height="14" viewBox="0 0 24 24" fill="currentColor">
          <path d="M9 16.17L4.83 12l-1.42 1.41L9 19 21 7l-1.41-1.41z"/>
        </svg>
        已认证
      </div>
    </div>
    
    <div class="card-content">
      <h3 class="card-title">{{ institution.name }}</h3>
      
      <div class="card-rating">
        <span class="rating-score">{{ institution.rating.toFixed(1) }}</span>
        <div class="rating-stars">
          <span v-for="i in 5" :key="i" :class="['star', { filled: i <= Math.round(institution.rating) }]">★</span>
        </div>
        <span class="review-count">({{ institution.reviewCount }}条评价)</span>
      </div>
      
      <p class="card-address">
        <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
          <path d="M21 10c0 7-9 13-9 13s-9-6-9-13a9 9 0 0 1 18 0z"></path>
          <circle cx="12" cy="10" r="3"></circle>
        </svg>
        {{ institution.address }}
      </p>
      
      <div class="card-tags">
        <span class="tag">{{ petTypes }}</span>
      </div>
      
      <div class="card-footer">
        <div class="price">
          <span class="price-value">¥{{ institution.minPrice || '--' }}</span>
          <span class="price-unit">/天起</span>
        </div>
        <div v-if="institution.distance !== undefined" class="distance">
          {{ institution.distance < 1 ? `${(institution.distance * 1000).toFixed(0)}m` : `${institution.distance.toFixed(1)}km` }}
        </div>
      </div>
    </div>
  </router-link>
</template>

<style lang="scss" scoped>
@import '@/styles/variables.scss';

.institution-card {
  display: block;
  background: var(--color-surface);
  border-radius: var(--radius-lg);
  overflow: hidden;
  box-shadow: var(--shadow-sm);
  text-decoration: none;
  transition: all 300ms cubic-bezier(0.33, 1, 0.68, 1);
  
  &:hover {
    transform: translateY(-4px);
    box-shadow: var(--shadow-lg);
  }
}

.card-image {
  position: relative;
  height: 160px;
  overflow: hidden;
  
  img {
    width: 100%;
    height: 100%;
    object-fit: cover;
    transition: transform 300ms ease;
  }
  
  .institution-card:hover & img {
    transform: scale(1.05);
  }
}

.favorite-btn {
  position: absolute;
  top: 12px;
  right: 12px;
  width: 36px;
  height: 36px;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.9);
  border: none;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  color: var(--color-text-muted);
  transition: all 200ms ease;
  
  &:hover {
    background: white;
    transform: scale(1.1);
  }
  
  &.active {
    color: var(--color-error);
  }
}

.verified-badge {
  position: absolute;
  bottom: 12px;
  left: 12px;
  display: flex;
  align-items: center;
  gap: 4px;
  padding: 4px 8px;
  background: var(--color-success);
  color: white;
  border-radius: var(--radius-sm);
  font-size: 12px;
  font-weight: 500;
}

.card-content {
  padding: 16px;
}

.card-title {
  font-family: var(--font-display);
  font-size: 16px;
  font-weight: 600;
  color: var(--color-text-primary);
  margin: 0 0 8px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.card-rating {
  display: flex;
  align-items: center;
  gap: 6px;
  margin-bottom: 8px;
  
  .rating-score {
    font-weight: 700;
    color: var(--color-primary);
  }
  
  .rating-stars {
    display: flex;
    
    .star {
      color: $neutral-300;
      font-size: 12px;
      
      &.filled {
        color: var(--color-warning);
      }
    }
  }
  
  .review-count {
    font-size: 12px;
    color: var(--color-text-muted);
  }
}

.card-address {
  display: flex;
  align-items: flex-start;
  gap: 4px;
  font-size: 13px;
  color: var(--color-text-secondary);
  margin: 0 0 12px;
  line-height: 1.4;
  
  svg {
    flex-shrink: 0;
    margin-top: 2px;
  }
}

.card-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
  margin-bottom: 12px;
  
  .tag {
    padding: 4px 8px;
    background: $neutral-100;
    border-radius: var(--radius-sm);
    font-size: 12px;
    color: var(--color-text-secondary);
  }
}

.card-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding-top: 12px;
  border-top: 1px solid var(--color-border);
}

.price {
  .price-value {
    font-size: 18px;
    font-weight: 700;
    color: var(--color-primary);
  }
  
  .price-unit {
    font-size: 12px;
    color: var(--color-text-muted);
  }
}

.distance {
  font-size: 13px;
  color: var(--color-text-secondary);
  display: flex;
  align-items: center;
  gap: 4px;
}
</style>
