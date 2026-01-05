<script setup lang="ts">
import { AppButton } from '@/components/common'

const props = defineProps<{
  institutionName: string
  phone: string
  emergencyPhone?: string
  address?: string
}>()

// 拨打电话
const callPhone = (phone: string) => {
  window.location.href = `tel:${phone}`
}

// 打开导航
const openNavigation = () => {
  if (props.address) {
    // 尝试打开高德地图
    const encodedAddress = encodeURIComponent(props.address)
    window.open(`https://uri.amap.com/search?keyword=${encodedAddress}`, '_blank')
  }
}
</script>

<template>
  <div class="emergency-contact">
    <div class="contact-header">
      <span class="header-icon">🆘</span>
      <h3>紧急联系</h3>
    </div>

    <div class="contact-list">
      <!-- 紧急电话 -->
      <div v-if="emergencyPhone" class="contact-item emergency" @click="callPhone(emergencyPhone)">
        <div class="contact-icon">📞</div>
        <div class="contact-info">
          <span class="contact-label">24小时紧急热线</span>
          <span class="contact-value">{{ emergencyPhone }}</span>
        </div>
        <AppButton type="primary" size="sm">
          立即拨打
        </AppButton>
      </div>

      <!-- 客服电话 -->
      <div class="contact-item" @click="callPhone(phone)">
        <div class="contact-icon">☎️</div>
        <div class="contact-info">
          <span class="contact-label">{{ institutionName }}</span>
          <span class="contact-value">{{ phone }}</span>
        </div>
        <AppButton type="outline" size="sm">
          拨打
        </AppButton>
      </div>

      <!-- 机构地址 -->
      <div v-if="address" class="contact-item" @click="openNavigation">
        <div class="contact-icon">📍</div>
        <div class="contact-info">
          <span class="contact-label">机构地址</span>
          <span class="contact-value address">{{ address }}</span>
        </div>
        <AppButton type="outline" size="sm">
          导航
        </AppButton>
      </div>
    </div>

    <!-- 提示信息 -->
    <div class="contact-tips">
      <p>💡 如遇宠物健康紧急情况，请立即拨打紧急热线</p>
    </div>
  </div>
</template>

<style lang="scss" scoped>
@import '@/styles/variables.scss';

.emergency-contact {
  background: var(--color-surface);
  border-radius: var(--radius-xl);
  padding: 20px;
  border: 1px solid var(--color-border);
}

.contact-header {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 16px;
  
  .header-icon {
    font-size: 24px;
  }
  
  h3 {
    font-size: 18px;
    font-weight: 600;
    margin: 0;
  }
}

.contact-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.contact-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 16px;
  background: var(--color-surface);
  border: 1px solid var(--color-border);
  border-radius: var(--radius-lg);
  cursor: pointer;
  transition: all 0.2s ease;
  
  &:hover {
    border-color: var(--color-primary-light);
    background: var(--color-primary-light);
  }
  
  &.emergency {
    background: linear-gradient(135deg, #FEE2E2 0%, #FECACA 100%);
    border-color: #EF4444;
    
    &:hover {
      background: linear-gradient(135deg, #FECACA 0%, #FCA5A5 100%);
    }
    
    .contact-label {
      color: #DC2626;
      font-weight: 600;
    }
    
    .contact-value {
      color: #B91C1C;
    }
  }
}

.contact-icon {
  width: 48px;
  height: 48px;
  background: white;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 24px;
  flex-shrink: 0;
}

.contact-info {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 2px;
  min-width: 0;
  
  .contact-label {
    font-size: 12px;
    color: var(--color-text-secondary);
  }
  
  .contact-value {
    font-size: 16px;
    font-weight: 600;
    color: var(--color-text-primary);
    
    &.address {
      font-size: 14px;
      font-weight: 400;
      white-space: nowrap;
      overflow: hidden;
      text-overflow: ellipsis;
    }
  }
}

.contact-tips {
  margin-top: 16px;
  padding: 12px;
  background: rgba(59, 130, 246, 0.1);
  border-radius: var(--radius-md);
  
  p {
    font-size: 13px;
    color: var(--color-info);
    margin: 0;
  }
}
</style>
