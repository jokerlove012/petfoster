<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { AppCard, AppButton } from '@/components/common'
import BookingForm from '@/components/booking/BookingForm.vue'
import { institutionApi } from '@/api/institution'
import { bookingApi } from '@/api/booking'
import { useRebooking } from '@/composables/useRebooking'
import type { Institution, ServicePackage } from '@/types/institution'
import type { CreateBookingData } from '@/types/booking'

const route = useRoute()
const router = useRouter()
const { getRebookingData, clearRebookingData } = useRebooking()

const institutionId = route.params.institutionId as string
const packageId = route.query.packageId as string

const loading = ref(true)
const submitting = ref(false)
const institution = ref<Institution | null>(null)
const servicePackages = ref<ServicePackage[]>([])
const selectedPackage = ref<ServicePackage | null>(null)

// 预填充数据
const prefillData = ref<{
  petId?: string
  emergencyContact?: { name: string; phone: string; relationship: string }
  specialRequirements?: string
} | null>(null)

// 加载机构信息
const loadInstitution = async () => {
  try {
    loading.value = true
    const res = await institutionApi.getDetail(institutionId)
    if (res.code === 200 && res.data) {
      institution.value = res.data
      servicePackages.value = res.data.servicePackages || []
      
      // 检查是否有重新预约数据
      const rebookingData = getRebookingData()
      if (rebookingData && rebookingData.institutionId === institutionId) {
        // 设置预填充数据
        prefillData.value = {
          petId: rebookingData.petId,
          emergencyContact: rebookingData.emergencyContact,
          specialRequirements: rebookingData.specialRequirements
        }
        
        // 如果有指定套餐ID，选中它
        if (rebookingData.servicePackageId) {
          selectedPackage.value = servicePackages.value.find(
            p => p.id === rebookingData.servicePackageId
          ) || null
        }
        
        // 清除重新预约数据
        clearRebookingData()
      }
      
      // 如果有指定套餐ID（URL参数），选中它
      if (!selectedPackage.value && packageId) {
        selectedPackage.value = servicePackages.value.find(p => p.id === packageId) || null
      }
      
      // 如果没有选中套餐且有套餐列表，默认选第一个
      if (!selectedPackage.value && servicePackages.value.length > 0) {
        selectedPackage.value = servicePackages.value[0]
      }
    }
  } catch (error) {
    console.error('Failed to load institution:', error)
    ElMessage.error('加载机构信息失败')
  } finally {
    loading.value = false
  }
}

// 选择套餐
const selectPackage = (pkg: ServicePackage) => {
  selectedPackage.value = pkg
}

// 提交预约
const handleSubmit = async (data: CreateBookingData) => {
  try {
    submitting.value = true
    const res = await bookingApi.create(data)
    if (res.code === 200 && res.data) {
      ElMessage.success('预约提交成功')
      // 跳转到支付页面
      router.push(`/booking/payment/${res.data.id}`)
    } else {
      ElMessage.error(res.message || '预约失败')
    }
  } catch (error: any) {
    console.error('Booking failed:', error)
    ElMessage.error(error.response?.data?.message || '预约失败，请稍后重试')
  } finally {
    submitting.value = false
  }
}

// 取消预约
const handleCancel = () => {
  router.back()
}

// 格式化价格
const formatPrice = (price: number) => `¥${price.toFixed(0)}`

onMounted(() => {
  if (!institutionId) {
    ElMessage.error('缺少机构信息')
    router.push('/institutions')
    return
  }
  loadInstitution()
})
</script>

<template>
  <div class="booking-create-view">
    <div class="page-header">
      <h1>创建预约</h1>
      <p>填写预约信息，完成寄养预订</p>
    </div>
    
    <!-- 加载状态 -->
    <div v-if="loading" class="loading-state">
      <el-skeleton :rows="10" animated />
    </div>
    
    <!-- 主内容 -->
    <template v-else-if="institution">
      <!-- 如果没有选择套餐，显示套餐选择 -->
      <div v-if="!selectedPackage" class="package-selection">
        <AppCard shadow="md" padding="lg">
          <h3>选择服务套餐</h3>
          <p class="hint">请选择一个服务套餐继续预约</p>
          
          <div class="package-list">
            <div 
              v-for="pkg in servicePackages" 
              :key="pkg.id"
              class="package-item"
              @click="selectPackage(pkg)"
            >
              <div class="package-info">
                <h4>{{ pkg.name }}</h4>
                <p>{{ pkg.description }}</p>
                <div class="package-features">
                  <span v-for="feature in pkg.features" :key="feature" class="feature">
                    {{ feature }}
                  </span>
                </div>
              </div>
              <div class="package-price">
                <span class="price">{{ formatPrice(pkg.pricePerDay) }}</span>
                <span class="unit">/天</span>
              </div>
            </div>
          </div>
          
          <div class="back-action">
            <router-link to="/institutions">
              <AppButton type="outline">返回机构列表</AppButton>
            </router-link>
          </div>
        </AppCard>
      </div>
      
      <!-- 预约表单 -->
      <div v-else class="booking-form-container">
        <BookingForm
          :institution-id="institutionId"
          :institution-name="institution.name"
          :service-package="selectedPackage"
          :prefill-data="prefillData"
          @submit="handleSubmit"
          @cancel="handleCancel"
        />
        
        <!-- 更换套餐 -->
        <div v-if="servicePackages.length > 1" class="change-package">
          <AppButton type="text" size="sm" @click="selectedPackage = null">
            更换服务套餐
          </AppButton>
        </div>
      </div>
    </template>
    
    <!-- 错误状态 -->
    <div v-else class="error-state">
      <AppCard shadow="md" padding="lg">
        <div class="error-content">
          <span class="error-icon">😕</span>
          <h3>无法加载机构信息</h3>
          <p>请稍后重试或返回机构列表</p>
          <router-link to="/institutions">
            <AppButton type="primary">返回机构列表</AppButton>
          </router-link>
        </div>
      </AppCard>
    </div>
  </div>
</template>


<style lang="scss" scoped>
.booking-create-view {
  max-width: 900px;
  margin: 0 auto;
  padding-bottom: 40px;
}

.page-header {
  margin-bottom: 24px;
  
  h1 {
    font-family: var(--font-display);
    font-size: 28px;
    margin: 0 0 8px;
  }
  
  p {
    color: var(--color-text-secondary);
    margin: 0;
  }
}

.loading-state {
  padding: 40px;
  background: var(--color-surface);
  border-radius: var(--radius-xl);
}

// 套餐选择
.package-selection {
  h3 {
    font-size: 20px;
    font-weight: 600;
    margin: 0 0 8px;
  }
  
  .hint {
    color: var(--color-text-secondary);
    margin: 0 0 24px;
  }
}

.package-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
  margin-bottom: 24px;
}

.package-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20px;
  background: var(--color-surface);
  border: 2px solid var(--color-border);
  border-radius: var(--radius-lg);
  cursor: pointer;
  transition: all 0.2s ease;
  
  &:hover {
    border-color: var(--color-primary);
    background: var(--color-primary-light);
  }
}

.package-info {
  flex: 1;
  
  h4 {
    font-size: 18px;
    font-weight: 600;
    margin: 0 0 8px;
  }
  
  p {
    font-size: 14px;
    color: var(--color-text-secondary);
    margin: 0 0 12px;
  }
}

.package-features {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  
  .feature {
    padding: 4px 10px;
    background: var(--color-primary-light);
    color: var(--color-primary);
    font-size: 12px;
    border-radius: 12px;
  }
}

.package-price {
  text-align: right;
  
  .price {
    font-size: 28px;
    font-weight: 700;
    color: var(--color-primary);
  }
  
  .unit {
    font-size: 14px;
    color: var(--color-text-secondary);
  }
}

.back-action {
  text-align: center;
  padding-top: 16px;
  border-top: 1px solid var(--color-border);
}

// 预约表单容器
.booking-form-container {
  position: relative;
}

.change-package {
  text-align: center;
  margin-top: 16px;
}

// 错误状态
.error-state {
  .error-content {
    text-align: center;
    padding: 48px 24px;
    
    .error-icon {
      font-size: 64px;
      display: block;
      margin-bottom: 16px;
    }
    
    h3 {
      font-size: 20px;
      margin: 0 0 8px;
    }
    
    p {
      color: var(--color-text-secondary);
      margin: 0 0 24px;
    }
  }
}

@media (max-width: 640px) {
  .package-item {
    flex-direction: column;
    align-items: flex-start;
    gap: 16px;
  }
  
  .package-price {
    text-align: left;
  }
}
</style>
