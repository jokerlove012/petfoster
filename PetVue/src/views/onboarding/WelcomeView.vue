<template>
  <div class="welcome-view">
    <!-- 轮播引导 -->
    <div class="welcome-slides">
      <transition name="slide-fade" mode="out-in">
        <div :key="currentSlide" class="slide">
          <div class="slide-visual">
            <span class="slide-emoji">{{ slides[currentSlide].emoji }}</span>
          </div>
          <h2>{{ slides[currentSlide].title }}</h2>
          <p>{{ slides[currentSlide].description }}</p>
        </div>
      </transition>
      
      <!-- 指示器 -->
      <div class="slide-indicators">
        <span 
          v-for="(_, index) in slides" 
          :key="index"
          :class="['indicator', { active: index === currentSlide }]"
          @click="goToSlide(index)"
        />
      </div>
      
      <!-- 导航按钮 -->
      <div class="slide-nav">
        <button 
          v-if="currentSlide > 0" 
          class="nav-btn prev"
          @click="prevSlide"
        >
          上一步
        </button>
        <button 
          v-if="currentSlide < slides.length - 1" 
          class="nav-btn next"
          @click="nextSlide"
        >
          下一步
        </button>
        <button 
          v-if="currentSlide === slides.length - 1" 
          class="nav-btn start"
          @click="startUsing"
        >
          开始使用
        </button>
      </div>
      
      <!-- 跳过按钮 -->
      <button 
        v-if="currentSlide < slides.length - 1"
        class="skip-btn"
        @click="skipOnboarding"
      >
        跳过引导
      </button>
    </div>
    
    <!-- 底部信息 -->
    <div class="welcome-footer">
      <p>已有账号？<router-link to="/login">立即登录</router-link></p>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { useRouter } from 'vue-router'

const router = useRouter()

const slides = [
  {
    emoji: '🐾',
    title: '欢迎来到 PetHome',
    description: '专业的宠物寄养平台，为您的爱宠找到温馨的临时家园'
  },
  {
    emoji: '🔍',
    title: '轻松搜索',
    description: '根据位置、价格、评分等条件，快速找到合适的寄养机构'
  },
  {
    emoji: '📱',
    title: '实时追踪',
    description: '随时查看宠物状态，接收每日照片和视频更新'
  },
  {
    emoji: '🛡️',
    title: '安全保障',
    description: '所有机构经过严格认证，提供宠物意外保险，让您放心托付'
  },
  {
    emoji: '🎉',
    title: '准备好了吗？',
    description: '立即注册，开启安心寄养之旅'
  }
]

const currentSlide = ref(0)

const nextSlide = () => {
  if (currentSlide.value < slides.length - 1) {
    currentSlide.value++
  }
}

const prevSlide = () => {
  if (currentSlide.value > 0) {
    currentSlide.value--
  }
}

const goToSlide = (index: number) => {
  currentSlide.value = index
}

const startUsing = () => {
  markOnboardingComplete()
  router.push('/register')
}

const skipOnboarding = () => {
  markOnboardingComplete()
  router.push('/')
}

const markOnboardingComplete = () => {
  localStorage.setItem('onboarding_complete', 'true')
}
</script>

<style scoped lang="scss">
.welcome-view {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
  background: linear-gradient(165deg, #fff9f5 0%, #fff0e8 50%, #ffe8dc 100%);
}

.welcome-slides {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 40px 24px;
  position: relative;
}

.slide {
  text-align: center;
  max-width: 400px;
}

.slide-visual {
  width: 160px;
  height: 160px;
  margin: 0 auto 32px;
  background: white;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 12px 40px rgba(255, 107, 53, 0.15);
}

.slide-emoji {
  font-size: 72px;
}

.slide h2 {
  font-size: 28px;
  font-weight: 700;
  color: var(--color-text-primary, #1a1a1a);
  margin: 0 0 16px;
}

.slide p {
  font-size: 16px;
  line-height: 1.6;
  color: var(--color-text-secondary, #666);
  margin: 0;
}

.slide-indicators {
  display: flex;
  gap: 10px;
  margin-top: 40px;
}

.indicator {
  width: 10px;
  height: 10px;
  border-radius: 50%;
  background: rgba(255, 107, 53, 0.2);
  cursor: pointer;
  transition: all 0.3s ease;
  
  &.active {
    width: 28px;
    border-radius: 5px;
    background: var(--color-primary, #ff6b35);
  }
}

.slide-nav {
  display: flex;
  gap: 16px;
  margin-top: 40px;
}

.nav-btn {
  padding: 14px 32px;
  border-radius: 12px;
  font-size: 16px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.2s ease;
  
  &.prev {
    background: white;
    border: 2px solid var(--color-border, #e5e5e5);
    color: var(--color-text-secondary, #666);
    
    &:hover {
      border-color: var(--color-primary, #ff6b35);
      color: var(--color-primary, #ff6b35);
    }
  }
  
  &.next,
  &.start {
    background: var(--color-primary, #ff6b35);
    border: none;
    color: white;
    
    &:hover {
      background: #e55a2b;
      transform: translateY(-2px);
    }
  }
  
  &.start {
    padding: 14px 48px;
  }
}

.skip-btn {
  position: absolute;
  top: 24px;
  right: 24px;
  padding: 8px 16px;
  background: transparent;
  border: none;
  color: var(--color-text-tertiary, #999);
  font-size: 14px;
  cursor: pointer;
  
  &:hover {
    color: var(--color-text-secondary, #666);
  }
}

.welcome-footer {
  padding: 24px;
  text-align: center;
  
  p {
    font-size: 14px;
    color: var(--color-text-secondary, #666);
    margin: 0;
    
    a {
      color: var(--color-primary, #ff6b35);
      font-weight: 500;
      
      &:hover {
        text-decoration: underline;
      }
    }
  }
}

// 过渡动画
.slide-fade-enter-active,
.slide-fade-leave-active {
  transition: all 0.3s ease;
}

.slide-fade-enter-from {
  opacity: 0;
  transform: translateX(30px);
}

.slide-fade-leave-to {
  opacity: 0;
  transform: translateX(-30px);
}
</style>
