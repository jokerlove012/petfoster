<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { AppButton, AppCard } from '@/components/common'
import ServicePackageForm from '@/components/institution/ServicePackageForm.vue'
import type { ServicePackage } from '@/types/institution'
import { institutionManageApi } from '@/api/institution'
import api from '@/api/index'

const packages = ref<ServicePackage[]>([])
const showForm = ref(false)
const editingPackage = ref<ServicePackage | null>(null)
const loading = ref(false)

// 加载套餐列表
const loadPackages = async () => {
  loading.value = true
  try {
    const res = await institutionManageApi.getPackages()
    if (res.code === 200 && res.data) {
      packages.value = res.data || []
    }
  } catch (error) {
    console.error('Failed to load packages:', error)
  } finally {
    loading.value = false
  }
}

const openCreateForm = () => {
  editingPackage.value = null
  showForm.value = true
}

const openEditForm = (pkg: ServicePackage) => {
  editingPackage.value = { ...pkg }
  showForm.value = true
}

const closeForm = () => {
  showForm.value = false
  editingPackage.value = null
}

const handleSave = async (data: Partial<ServicePackage>) => {
  try {
    if (editingPackage.value) {
      // 更新
      const res = await institutionManageApi.updatePackage(editingPackage.value.id, data)
      const index = packages.value.findIndex(p => p.id === editingPackage.value!.id)
      if (index !== -1 && res.data) {
        packages.value[index] = res.data
      }
      ElMessage.success('套餐更新成功')
    } else {
      // 新增
      const res = await institutionManageApi.createPackage(data)
      if (res.data) {
        packages.value.push(res.data)
      }
      ElMessage.success('套餐创建成功')
    }
    closeForm()
  } catch (error: any) {
    ElMessage.error(error.message || '操作失败')
  }
}

const toggleStatus = async (pkg: ServicePackage) => {
  try {
    await api.put(`/institution/packages/${pkg.id}/status`, { isActive: !pkg.isActive })
    pkg.isActive = !pkg.isActive
    ElMessage.success(pkg.isActive ? '套餐已上架' : '套餐已下架')
  } catch (error) {
    ElMessage.error('操作失败')
  }
}

const deletePackage = async (pkg: ServicePackage) => {
  try {
    await ElMessageBox.confirm(
      `确定要删除套餐"${pkg.name}"吗？此操作不可恢复。`,
      '删除确认',
      { confirmButtonText: '删除', cancelButtonText: '取消', type: 'warning' }
    )
    await institutionManageApi.deletePackage(pkg.id)
    packages.value = packages.value.filter(p => p.id !== pkg.id)
    ElMessage.success('套餐已删除')
  } catch {
    // 取消删除
  }
}

const formatPrice = (price: number) => `¥${price}`

onMounted(() => {
  loadPackages()
})
</script>

<template>
  <div class="service-manage-view">
    <div class="page-header">
      <div class="header-info">
        <h1>服务套餐管理</h1>
        <p>管理您的寄养服务套餐</p>
      </div>
      <AppButton type="primary" @click="openCreateForm">
        <span>+ 新增套餐</span>
      </AppButton>
    </div>

    <div class="packages-grid">
      <AppCard 
        v-for="pkg in packages" 
        :key="pkg.id" 
        class="package-card"
        :class="{ inactive: !pkg.isActive }"
        shadow="sm"
      >
        <div class="card-header">
          <h3>{{ pkg.name }}</h3>
          <span class="status-badge" :class="pkg.isActive ? 'active' : 'inactive'">
            {{ pkg.isActive ? '已上架' : '已下架' }}
          </span>
        </div>
        
        <p class="description">{{ pkg.description }}</p>
        
        <div class="price-row">
          <span class="price">{{ formatPrice(pkg.pricePerDay) }}</span>
          <span class="unit">/天</span>
        </div>
        
        <div class="features">
          <span v-for="feature in pkg.features" :key="feature" class="feature-tag">
            {{ feature }}
          </span>
        </div>
        
        <div class="pet-types">
          <span v-if="pkg.petTypes.includes('dog')" class="pet-tag">🐕 狗狗</span>
          <span v-if="pkg.petTypes.includes('cat')" class="pet-tag">🐱 猫咪</span>
        </div>
        
        <div class="card-actions">
          <AppButton type="outline" size="sm" @click="openEditForm(pkg)">编辑</AppButton>
          <AppButton 
            :type="pkg.isActive ? 'ghost' : 'primary'" 
            size="sm" 
            @click="toggleStatus(pkg)"
          >
            {{ pkg.isActive ? '下架' : '上架' }}
          </AppButton>
          <AppButton type="ghost" size="sm" class="delete-btn" @click="deletePackage(pkg)">
            删除
          </AppButton>
        </div>
      </AppCard>

      <!-- 添加新套餐卡片 -->
      <div class="add-card" @click="openCreateForm">
        <span class="add-icon">+</span>
        <span class="add-text">添加新套餐</span>
      </div>
    </div>

    <!-- 编辑表单弹窗 -->
    <el-dialog
      v-model="showForm"
      :title="editingPackage ? '编辑套餐' : '新增套餐'"
      width="560px"
      :close-on-click-modal="false"
    >
      <ServicePackageForm
        :initial-data="editingPackage"
        @save="handleSave"
        @cancel="closeForm"
      />
    </el-dialog>
  </div>
</template>


<style lang="scss" scoped>
.service-manage-view {
  max-width: 1200px;
  margin: 0 auto;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;

  .header-info {
    h1 {
      font-family: var(--font-display);
      font-size: 28px;
      margin: 0 0 4px;
    }

    p {
      color: var(--color-text-secondary);
      margin: 0;
    }
  }
}

.packages-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(320px, 1fr));
  gap: 20px;
}

.package-card {
  display: flex;
  flex-direction: column;
  transition: all 200ms ease;

  &.inactive {
    opacity: 0.7;
  }

  .card-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 12px;

    h3 {
      font-size: 20px;
      font-weight: 600;
      margin: 0;
    }
  }

  .status-badge {
    padding: 4px 10px;
    border-radius: var(--radius-full);
    font-size: 12px;
    font-weight: 500;

    &.active {
      background: var(--color-success-light, #dcfce7);
      color: var(--color-success);
    }

    &.inactive {
      background: var(--color-neutral-200);
      color: var(--color-text-muted);
    }
  }

  .description {
    color: var(--color-text-secondary);
    font-size: 14px;
    margin: 0 0 16px;
  }

  .price-row {
    margin-bottom: 16px;

    .price {
      font-size: 28px;
      font-weight: 700;
      color: var(--color-primary);
    }

    .unit {
      font-size: 14px;
      color: var(--color-text-muted);
    }
  }

  .features {
    display: flex;
    flex-wrap: wrap;
    gap: 8px;
    margin-bottom: 12px;

    .feature-tag {
      padding: 4px 10px;
      background: var(--color-primary-light);
      color: var(--color-primary);
      font-size: 12px;
      border-radius: 12px;
    }
  }

  .pet-types {
    display: flex;
    gap: 12px;
    margin-bottom: 16px;

    .pet-tag {
      font-size: 13px;
      color: var(--color-text-secondary);
    }
  }

  .card-actions {
    display: flex;
    gap: 8px;
    margin-top: auto;
    padding-top: 16px;
    border-top: 1px solid var(--color-border);

    .delete-btn {
      margin-left: auto;
      color: var(--color-error);

      &:hover {
        background: rgba(239, 68, 68, 0.1);
      }
    }
  }
}

.add-card {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  min-height: 280px;
  border: 2px dashed var(--color-border);
  border-radius: var(--radius-lg);
  cursor: pointer;
  transition: all 200ms ease;

  &:hover {
    border-color: var(--color-primary);
    background: var(--color-primary-light);

    .add-icon,
    .add-text {
      color: var(--color-primary);
    }
  }

  .add-icon {
    font-size: 48px;
    color: var(--color-text-muted);
    margin-bottom: 8px;
  }

  .add-text {
    font-size: 16px;
    color: var(--color-text-muted);
  }
}
</style>
