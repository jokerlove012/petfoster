<template>
  <div class="lazy-image" :class="{ loaded: isLoaded, error: hasError }">
    <img
      v-if="shouldLoad"
      :src="src"
      :alt="alt"
      @load="onLoad"
      @error="onError"
    />
    <div v-if="!isLoaded && !hasError" class="placeholder">
      <div class="skeleton-shimmer"></div>
    </div>
    <div v-if="hasError" class="error-placeholder">
      <ImageOff :size="24" />
      <span>加载失败</span>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, onUnmounted } from 'vue'
import { ImageOff } from 'lucide-vue-next'

const props = withDefaults(
  defineProps<{
    src: string
    alt?: string
    lazy?: boolean
  }>(),
  {
    alt: '',
    lazy: true
  }
)

const isLoaded = ref(false)
const hasError = ref(false)
const shouldLoad = ref(!props.lazy)
const containerRef = ref<HTMLElement | null>(null)

let observer: IntersectionObserver | null = null

function onLoad() {
  isLoaded.value = true
}

function onError() {
  hasError.value = true
}

onMounted(() => {
  if (!props.lazy) {
    shouldLoad.value = true
    return
  }

  // 使用 IntersectionObserver 实现懒加载
  if ('IntersectionObserver' in window) {
    observer = new IntersectionObserver(
      (entries) => {
        entries.forEach((entry) => {
          if (entry.isIntersecting) {
            shouldLoad.value = true
            observer?.disconnect()
          }
        })
      },
      {
        rootMargin: '50px'
      }
    )

    const el = document.querySelector('.lazy-image')
    if (el) {
      observer.observe(el)
    }
  } else {
    // 不支持 IntersectionObserver 时直接加载
    shouldLoad.value = true
  }
})

onUnmounted(() => {
  observer?.disconnect()
})
</script>

<style scoped lang="scss">
.lazy-image {
  position: relative;
  width: 100%;
  height: 100%;
  overflow: hidden;
  background: var(--color-neutral-100);

  img {
    width: 100%;
    height: 100%;
    object-fit: cover;
    opacity: 0;
    transition: opacity 0.3s ease;
  }

  &.loaded img {
    opacity: 1;
  }
}

.placeholder {
  position: absolute;
  inset: 0;
  overflow: hidden;
}

.skeleton-shimmer {
  width: 100%;
  height: 100%;
  background: linear-gradient(
    90deg,
    var(--color-neutral-100) 25%,
    var(--color-neutral-200) 50%,
    var(--color-neutral-100) 75%
  );
  background-size: 200% 100%;
  animation: shimmer 1.5s infinite;
}

@keyframes shimmer {
  0% {
    background-position: 200% 0;
  }
  100% {
    background-position: -200% 0;
  }
}

.error-placeholder {
  position: absolute;
  inset: 0;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 8px;
  color: var(--color-text-muted);
  font-size: 12px;
}
</style>
