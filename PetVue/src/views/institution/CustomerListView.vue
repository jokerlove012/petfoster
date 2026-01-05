<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { AppCard, AppButton } from '@/components/common'
import { institutionManageApi } from '@/api/institution'

const router = useRouter()

// 客户数据
const customers = ref<any[]>([])
const loading = ref(false)
const pagination = ref({ page: 1, pageSize: 20, total: 0 })

// 加载客户列表
const loadCustomers = async () => {
  loading.value = true
  try {
    const res = await institutionManageApi.getCustomers(pagination.value.page, pagination.value.pageSize)
    if (res.code === 200 && res.data) {
      customers.value = res.data.list || []
      pagination.value.total = res.data.pagination?.total || 0
    }
  } catch (error) {
    console.error('加载客户列表失败:', error)
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  loadCustomers()
})

const searchQuery = ref('')
const sortBy = ref<'orders' | 'spent' | 'recent'>('recent')

const filteredCustomers = computed(() => {
  let result = [...customers.value]
  
  // 搜索过滤
  if (searchQuery.value) {
    const query = searchQuery.value.toLowerCase()
    result = result.filter(c => 
      c.name.toLowerCase().includes(query) ||
      c.phone.includes(query) ||
      c.pets.some(p => p.name.toLowerCase().includes(query))
    )
  }
  
  // 排序
  if (sortBy.value === 'orders') {
    result.sort((a, b) => b.totalOrders - a.totalOrders)
  } else if (sortBy.value === 'spent') {
    result.sort((a, b) => b.totalSpent - a.totalSpent)
  } else {
    result.sort((a, b) => new Date(b.lastVisit).getTime() - new Date(a.lastVisit).getTime())
  }
  
  return result
})

const formatDate = (dateStr: string) => {
  return new Date(dateStr).toLocaleDateString('zh-CN')
}

const formatPrice = (price: number) => `¥${price.toLocaleString()}`

const viewCustomerOrders = (customerId: string) => {
  router.push({ path: '/institution/orders', query: { customerId } })
}
</script>

<template>
  <div class="customer-list-view">
    <div class="page-header">
      <div class="header-info">
        <h1>客户管理</h1>
        <p>共 {{ customers.length }} 位客户</p>
      </div>
    </div>

    <!-- 搜索和筛选 -->
    <div class="filters-bar">
      <div class="search-box">
        <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
          <circle cx="11" cy="11" r="8"></circle>
          <line x1="21" y1="21" x2="16.65" y2="16.65"></line>
        </svg>
        <input 
          v-model="searchQuery" 
          type="text" 
          placeholder="搜索客户姓名、电话或宠物名..."
        />
      </div>
      
      <div class="sort-options">
        <span class="sort-label">排序：</span>
        <select v-model="sortBy">
          <option value="recent">最近访问</option>
          <option value="orders">订单数量</option>
          <option value="spent">消费金额</option>
        </select>
      </div>
    </div>

    <!-- 客户列表 -->
    <div class="customers-list">
      <AppCard
        v-for="customer in filteredCustomers"
        :key="customer.id"
        class="customer-card"
        shadow="sm"
      >
        <div class="customer-main">
          <div class="customer-avatar">
            {{ customer.name.charAt(0) }}
          </div>
          
          <div class="customer-info">
            <div class="customer-header">
              <h3>{{ customer.name }}</h3>
              <span class="customer-phone">{{ customer.phone }}</span>
            </div>
            
            <div class="customer-tags">
              <span v-for="tag in customer.tags" :key="tag" class="tag">{{ tag }}</span>
            </div>
            
            <div class="customer-pets">
              <span class="pets-label">宠物：</span>
              <span v-for="(pet, index) in customer.pets" :key="pet.id" class="pet-item">
                {{ pet.species === 'dog' ? '🐕' : '🐱' }} {{ pet.name }}
                <span v-if="index < customer.pets.length - 1">、</span>
              </span>
            </div>
          </div>
        </div>

        <div class="customer-stats">
          <div class="stat-item">
            <span class="stat-value">{{ customer.totalOrders }}</span>
            <span class="stat-label">订单数</span>
          </div>
          <div class="stat-item">
            <span class="stat-value">{{ formatPrice(customer.totalSpent) }}</span>
            <span class="stat-label">总消费</span>
          </div>
          <div class="stat-item">
            <span class="stat-value">{{ formatDate(customer.lastVisit) }}</span>
            <span class="stat-label">最近访问</span>
          </div>
        </div>

        <div class="customer-actions">
          <AppButton type="outline" size="sm" @click="viewCustomerOrders(customer.id)">
            查看订单
          </AppButton>
          <AppButton type="ghost" size="sm">
            发送消息
          </AppButton>
        </div>
      </AppCard>

      <div v-if="filteredCustomers.length === 0" class="empty-state">
        <span class="empty-icon">👥</span>
        <p>未找到匹配的客户</p>
      </div>
    </div>
  </div>
</template>

<style lang="scss" scoped>
.customer-list-view {
  max-width: 900px;
  margin: 0 auto;
}

.page-header {
  margin-bottom: 24px;

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

.filters-bar {
  display: flex;
  gap: 16px;
  margin-bottom: 24px;

  @media (max-width: 640px) {
    flex-direction: column;
  }
}

.search-box {
  flex: 1;
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px 16px;
  background: var(--color-surface);
  border: 1px solid var(--color-border);
  border-radius: var(--radius-lg);

  svg {
    color: var(--color-text-muted);
  }

  input {
    flex: 1;
    border: none;
    background: none;
    font-size: 14px;
    outline: none;

    &::placeholder {
      color: var(--color-text-muted);
    }
  }
}

.sort-options {
  display: flex;
  align-items: center;
  gap: 8px;

  .sort-label {
    font-size: 14px;
    color: var(--color-text-secondary);
  }

  select {
    padding: 10px 12px;
    border: 1px solid var(--color-border);
    border-radius: var(--radius-md);
    font-size: 14px;
    background: var(--color-surface);
    cursor: pointer;
  }
}

.customers-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.customer-card {
  .customer-main {
    display: flex;
    gap: 16px;
    margin-bottom: 16px;
  }

  .customer-avatar {
    width: 56px;
    height: 56px;
    background: var(--color-primary);
    color: white;
    border-radius: 50%;
    display: flex;
    align-items: center;
    justify-content: center;
    font-size: 20px;
    font-weight: 600;
    flex-shrink: 0;
  }

  .customer-info {
    flex: 1;
  }

  .customer-header {
    display: flex;
    align-items: center;
    gap: 12px;
    margin-bottom: 8px;

    h3 {
      font-size: 18px;
      font-weight: 600;
      margin: 0;
    }

    .customer-phone {
      font-size: 14px;
      color: var(--color-text-muted);
    }
  }

  .customer-tags {
    display: flex;
    gap: 8px;
    margin-bottom: 8px;

    .tag {
      padding: 2px 8px;
      background: var(--color-primary-light);
      color: var(--color-primary);
      font-size: 12px;
      border-radius: var(--radius-full);
    }
  }

  .customer-pets {
    font-size: 14px;
    color: var(--color-text-secondary);

    .pets-label {
      color: var(--color-text-muted);
    }
  }
}

.customer-stats {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 16px;
  padding: 16px 0;
  border-top: 1px solid var(--color-border);
  border-bottom: 1px solid var(--color-border);
  margin-bottom: 16px;

  .stat-item {
    text-align: center;

    .stat-value {
      display: block;
      font-size: 18px;
      font-weight: 600;
      color: var(--color-text-primary);
    }

    .stat-label {
      font-size: 12px;
      color: var(--color-text-muted);
    }
  }
}

.customer-actions {
  display: flex;
  gap: 8px;
}

.empty-state {
  text-align: center;
  padding: 60px 20px;

  .empty-icon {
    font-size: 48px;
    display: block;
    margin-bottom: 12px;
    opacity: 0.5;
  }

  p {
    color: var(--color-text-muted);
    margin: 0;
  }
}
</style>
