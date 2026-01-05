<script setup lang="ts">
import { ref, computed } from 'vue'
import { useRouter } from 'vue-router'
import { ArrowLeft, Calendar, User, Search, Plus, Minus } from 'lucide-vue-next'
import { ElMessage } from 'element-plus'

const router = useRouter()

// 表单数据
const form = ref({
  customerId: '',
  customerName: '',
  customerPhone: '',
  petName: '',
  petType: 'dog',
  petBreed: '',
  serviceId: '',
  roomId: '',
  startDate: '',
  endDate: '',
  specialRequirements: '',
  emergencyContact: '',
  emergencyPhone: ''
})

// 服务套餐
const services = ref([
  { id: '1', name: '标准寄养', price: 98, unit: '天', description: '基础寄养服务，包含喂食、清洁' },
  { id: '2', name: '豪华寄养', price: 168, unit: '天', description: '独立房间，专人照顾，每日视频' },
  { id: '3', name: 'VIP套房', price: 268, unit: '天', description: '超大空间，24小时监控，美容服务' },
  { id: '4', name: '日托服务', price: 68, unit: '次', description: '白天托管，晚间接回' }
])

// 可用房间
const rooms = ref([
  { id: 'A01', name: 'A01', type: '标准间', status: 'available' },
  { id: 'A02', name: 'A02', type: '标准间', status: 'available' },
  { id: 'B01', name: 'B01', type: '豪华间', status: 'available' },
  { id: 'B02', name: 'B02', type: '豪华间', status: 'occupied' },
  { id: 'C01', name: 'C01', type: 'VIP套房', status: 'available' }
])

const availableRooms = computed(() => rooms.value.filter(r => r.status === 'available'))
const selectedService = computed(() => services.value.find(s => s.id === form.value.serviceId))

// 计算总价
const totalDays = computed(() => {
  if (!form.value.startDate || !form.value.endDate) return 0
  const start = new Date(form.value.startDate)
  const end = new Date(form.value.endDate)
  return Math.ceil((end.getTime() - start.getTime()) / (1000 * 60 * 60 * 24)) + 1
})

const totalPrice = computed(() => {
  if (!selectedService.value) return 0
  return selectedService.value.price * (totalDays.value || 1)
})

// 搜索客户
const searchCustomer = () => {
  if (form.value.customerPhone) {
    form.value.customerName = '张三'
    form.value.customerId = 'C001'
    ElMessage.success('已找到客户信息')
  }
}

// 提交订单
const submitOrder = async () => {
  if (!form.value.customerName || !form.value.petName || !form.value.serviceId || !form.value.startDate) {
    ElMessage.warning('请填写完整信息')
    return
  }
  ElMessage.success('订单创建成功')
  router.push('/institution/orders')
}

const goBack = () => router.back()
</script>

<template>
  <div class="order-create">
    <div class="page-header">
      <button class="back-btn" @click="goBack"><ArrowLeft :size="20" /> 返回</button>
      <h1>➕ 新增订单</h1>
    </div>

    <div class="form-container">
      <!-- 客户信息 -->
      <div class="form-section">
        <h3><User :size="18" /> 客户信息</h3>
        <div class="form-grid">
          <div class="form-item">
            <label>手机号码</label>
            <div class="search-input">
              <input v-model="form.customerPhone" placeholder="输入手机号搜索" />
              <button @click="searchCustomer"><Search :size="16" /></button>
            </div>
          </div>
          <div class="form-item">
            <label>客户姓名</label>
            <input v-model="form.customerName" placeholder="客户姓名" />
          </div>
        </div>
      </div>

      <!-- 宠物信息 -->
      <div class="form-section">
        <h3>🐾 宠物信息</h3>
        <div class="form-grid">
          <div class="form-item">
            <label>宠物名称</label>
            <input v-model="form.petName" placeholder="宠物名称" />
          </div>
          <div class="form-item">
            <label>宠物类型</label>
            <select v-model="form.petType">
              <option value="dog">狗狗</option>
              <option value="cat">猫咪</option>
              <option value="other">其他</option>
            </select>
          </div>
          <div class="form-item">
            <label>品种</label>
            <input v-model="form.petBreed" placeholder="品种" />
          </div>
        </div>
      </div>

      <!-- 服务选择 -->
      <div class="form-section">
        <h3>📦 服务套餐</h3>
        <div class="service-list">
          <div v-for="service in services" :key="service.id" class="service-card"
            :class="{ selected: form.serviceId === service.id }" @click="form.serviceId = service.id">
            <div class="service-info">
              <span class="service-name">{{ service.name }}</span>
              <span class="service-desc">{{ service.description }}</span>
            </div>
            <div class="service-price">¥{{ service.price }}/{{ service.unit }}</div>
          </div>
        </div>
      </div>

      <!-- 预约时间 -->
      <div class="form-section">
        <h3><Calendar :size="18" /> 预约时间</h3>
        <div class="form-grid">
          <div class="form-item">
            <label>开始日期</label>
            <input v-model="form.startDate" type="date" />
          </div>
          <div class="form-item">
            <label>结束日期</label>
            <input v-model="form.endDate" type="date" />
          </div>
          <div class="form-item">
            <label>房间</label>
            <select v-model="form.roomId">
              <option value="">请选择房间</option>
              <option v-for="room in availableRooms" :key="room.id" :value="room.id">
                {{ room.name }} ({{ room.type }})
              </option>
            </select>
          </div>
        </div>
      </div>

      <!-- 特殊要求 -->
      <div class="form-section">
        <h3>📝 特殊要求</h3>
        <textarea v-model="form.specialRequirements" placeholder="请输入特殊照顾要求..." rows="3"></textarea>
      </div>

      <!-- 费用汇总 -->
      <div class="summary-section">
        <div class="summary-row">
          <span>服务套餐</span>
          <span>{{ selectedService?.name || '-' }}</span>
        </div>
        <div class="summary-row">
          <span>寄养天数</span>
          <span>{{ totalDays }} 天</span>
        </div>
        <div class="summary-row total">
          <span>订单总额</span>
          <span class="price">¥{{ totalPrice }}</span>
        </div>
      </div>

      <div class="form-actions">
        <button class="btn-cancel" @click="goBack">取消</button>
        <button class="btn-submit" @click="submitOrder">创建订单</button>
      </div>
    </div>
  </div>
</template>

<style lang="scss" scoped>
.order-create { max-width: 900px; margin: 0 auto; padding: 24px; }

.page-header {
  display: flex; align-items: center; gap: 16px; margin-bottom: 24px;
  .back-btn { display: flex; align-items: center; gap: 6px; padding: 8px 12px; background: white; border: none; border-radius: 8px; cursor: pointer; }
  h1 { font-size: 24px; font-weight: 700; margin: 0; }
}

.form-container { background: white; border-radius: 16px; padding: 24px; }

.form-section {
  margin-bottom: 28px; padding-bottom: 24px; border-bottom: 1px solid #F0EFED;
  h3 { display: flex; align-items: center; gap: 8px; font-size: 16px; font-weight: 600; margin: 0 0 16px; }
}

.form-grid { display: grid; grid-template-columns: repeat(3, 1fr); gap: 16px;
  @media (max-width: 768px) { grid-template-columns: 1fr; }
}

.form-item {
  label { display: block; font-size: 13px; color: #6B6560; margin-bottom: 6px; }
  input, select, textarea {
    width: 100%; padding: 10px 14px; border: 1px solid #E8E6E3; border-radius: 8px; font-size: 14px;
    &:focus { border-color: #722ed1; outline: none; }
  }
  .search-input { display: flex; gap: 8px;
    input { flex: 1; }
    button { padding: 10px 14px; background: #722ed1; color: white; border: none; border-radius: 8px; cursor: pointer; }
  }
}

.service-list { display: grid; grid-template-columns: repeat(2, 1fr); gap: 12px;
  @media (max-width: 600px) { grid-template-columns: 1fr; }
}

.service-card {
  display: flex; justify-content: space-between; align-items: center; padding: 16px;
  border: 2px solid #E8E6E3; border-radius: 12px; cursor: pointer; transition: all 0.2s;
  &:hover { border-color: #722ed1; }
  &.selected { border-color: #722ed1; background: #F9F5FF; }
  .service-info { .service-name { display: block; font-weight: 600; margin-bottom: 4px; } .service-desc { font-size: 12px; color: #9A958F; } }
  .service-price { font-size: 18px; font-weight: 700; color: #722ed1; }
}

.summary-section {
  background: #F8F8F7; border-radius: 12px; padding: 20px; margin-bottom: 24px;
  .summary-row { display: flex; justify-content: space-between; padding: 8px 0; font-size: 14px;
    &.total { border-top: 1px solid #E8E6E3; margin-top: 8px; padding-top: 16px; font-weight: 600;
      .price { font-size: 20px; color: #722ed1; }
    }
  }
}

.form-actions {
  display: flex; justify-content: flex-end; gap: 12px;
  button { padding: 12px 28px; border-radius: 10px; font-size: 14px; font-weight: 500; cursor: pointer; }
  .btn-cancel { background: #F8F8F7; border: none; color: #6B6560; }
  .btn-submit { background: #722ed1; border: none; color: white; }
}
</style>
