<script setup lang="ts">
import { ref, computed } from 'vue'
import type { Booking } from '@/types/booking'
import { calculateRefund, getRefundPolicy, type RefundCalculation } from '@/utils/refundCalculator'
import { AppButton } from '@/components/common'

const props = defineProps<{
  order: Booking
  visible: boolean
}>()

const emit = defineEmits<{
  (e: 'close'): void
  (e: 'confirm', reason: string): void
}>()

const cancelReason = ref('')
const isSubmitting = ref(false)

// 取消原因选项
const reasonOptions = [
  '行程变更，无法按时送达',
  '找到了其他寄养方式',
  '宠物身体不适，不宜寄养',
  '价格原因',
  '其他原因'
]

const selectedReason = ref('')
const customReason = ref('')

// 计算退款信息
const refundInfo = computed<RefundCalculation>(() => {
  return calculateRefund({
    totalPrice: props.order.totalPrice,
    startDate: props.order.startDate,
    endDate: props.order.endDate
  })
})

// 最终取消原因
const finalReason = computed(() => {
  if (selectedReason.value === '其他原因') {
    return customReason.value
  }
  return selectedReason.value
})

// 是否可以提交
const canSubmit = computed(() => {
  if (!selectedReason.value) return false
  if (selectedReason.value === '其他原因' && !customReason.value.trim()) return false
  return true
})

// 选择原因
const selectReason = (reason: string) => {
  selectedReason.value = reason
  if (reason !== '其他原因') {
    customReason.value = ''
  }
}

// 确认取消
const handleConfirm = async () => {
  if (!canSubmit.value || isSubmitting.value) return
  
  isSubmitting.value = true
  
  try {
    // 直接触发确认事件，由父组件调用真实API
    emit('confirm', finalReason.value)
  } finally {
    isSubmitting.value = false
  }
}

// 关闭弹窗
const handleClose = () => {
  selectedReason.value = ''
  customReason.value = ''
  emit('close')
}
</script>

<template>
  <Teleport to="body">
    <div v-if="visible" class="modal-overlay" @click="handleClose">
      <div class="modal-content" @click.stop>
        <!-- 头部 -->
        <div class="modal-header">
          <h3>取消订单</h3>
          <button class="close-btn" @click="handleClose">×</button>
        </div>

        <!-- 退款信息 -->
        <div class="refund-info">
          <div class="refund-header">
            <span class="refund-icon">💰</span>
            <div class="refund-text">
              <span class="refund-label">预计退款金额</span>
              <span class="refund-amount">¥{{ refundInfo.refundAmount.toFixed(2) }}</span>
            </div>
          </div>
          <p class="refund-reason">{{ refundInfo.reason }}</p>
          
          <div v-if="refundInfo.cancellationFee > 0" class="fee-info">
            <span>手续费：¥{{ refundInfo.cancellationFee.toFixed(2) }}</span>
          </div>
        </div>

        <!-- 取消原因 -->
        <div class="reason-section">
          <h4>请选择取消原因</h4>
          <div class="reason-list">
            <div 
              v-for="reason in reasonOptions"
              :key="reason"
              class="reason-item"
              :class="{ selected: selectedReason === reason }"
              @click="selectReason(reason)"
            >
              <span class="reason-radio">
                <span v-if="selectedReason === reason" class="radio-dot"></span>
              </span>
              <span class="reason-text">{{ reason }}</span>
            </div>
          </div>
          
          <!-- 自定义原因输入 -->
          <div v-if="selectedReason === '其他原因'" class="custom-reason">
            <textarea 
              v-model="customReason"
              placeholder="请输入具体原因..."
              rows="3"
            ></textarea>
          </div>
        </div>

        <!-- 退款政策 -->
        <div class="policy-section">
          <h4>退款政策</h4>
          <ul>
            <li v-for="(policy, index) in getRefundPolicy()" :key="index">
              {{ policy }}
            </li>
          </ul>
        </div>

        <!-- 操作按钮 -->
        <div class="modal-actions">
          <AppButton type="outline" @click="handleClose">
            再想想
          </AppButton>
          <AppButton 
            type="primary" 
            :disabled="!canSubmit"
            :loading="isSubmitting"
            @click="handleConfirm"
          >
            确认取消
          </AppButton>
        </div>
      </div>
    </div>
  </Teleport>
</template>

<style lang="scss" scoped>
@import '@/styles/variables.scss';

.modal-overlay {
  position: fixed;
  inset: 0;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 24px;
  z-index: 1000;
  animation: fadeIn 0.2s ease;
}

@keyframes fadeIn {
  from { opacity: 0; }
  to { opacity: 1; }
}

.modal-content {
  background: white;
  border-radius: var(--radius-xl);
  width: 100%;
  max-width: 420px;
  max-height: 90vh;
  overflow-y: auto;
  animation: slideUp 0.3s ease;
}

@keyframes slideUp {
  from {
    opacity: 0;
    transform: translateY(20px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

// 头部
.modal-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20px 24px;
  border-bottom: 1px solid var(--color-border);
  
  h3 {
    font-size: 18px;
    font-weight: 600;
    margin: 0;
  }
  
  .close-btn {
    width: 32px;
    height: 32px;
    border: none;
    background: var(--color-surface);
    border-radius: 50%;
    font-size: 20px;
    color: var(--color-text-secondary);
    cursor: pointer;
    display: flex;
    align-items: center;
    justify-content: center;
    
    &:hover {
      background: var(--color-border);
    }
  }
}

// 退款信息
.refund-info {
  padding: 20px 24px;
  background: linear-gradient(135deg, var(--color-primary-light), var(--color-accent-light));
  margin: 16px;
  border-radius: var(--radius-lg);
}

.refund-header {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 8px;
  
  .refund-icon {
    font-size: 32px;
  }
  
  .refund-text {
    display: flex;
    flex-direction: column;
    
    .refund-label {
      font-size: 13px;
      color: var(--color-text-secondary);
    }
    
    .refund-amount {
      font-size: 28px;
      font-weight: 700;
      color: var(--color-primary);
    }
  }
}

.refund-reason {
  font-size: 13px;
  color: var(--color-text-secondary);
  margin: 0;
}

.fee-info {
  margin-top: 8px;
  padding-top: 8px;
  border-top: 1px dashed rgba(0, 0, 0, 0.1);
  font-size: 13px;
  color: var(--color-text-secondary);
}

// 取消原因
.reason-section {
  padding: 0 24px 20px;
  
  h4 {
    font-size: 14px;
    font-weight: 600;
    margin: 0 0 12px;
  }
}

.reason-list {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.reason-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px 16px;
  background: var(--color-surface);
  border: 1px solid var(--color-border);
  border-radius: var(--radius-md);
  cursor: pointer;
  transition: all 0.2s;
  
  &:hover {
    border-color: var(--color-primary-light);
  }
  
  &.selected {
    border-color: var(--color-primary);
    background: var(--color-primary-light);
  }
}

.reason-radio {
  width: 18px;
  height: 18px;
  border: 2px solid var(--color-border);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  
  .reason-item.selected & {
    border-color: var(--color-primary);
  }
  
  .radio-dot {
    width: 10px;
    height: 10px;
    background: var(--color-primary);
    border-radius: 50%;
  }
}

.reason-text {
  font-size: 14px;
  color: var(--color-text-primary);
}

.custom-reason {
  margin-top: 12px;
  
  textarea {
    width: 100%;
    padding: 12px;
    border: 1px solid var(--color-border);
    border-radius: var(--radius-md);
    font-size: 14px;
    font-family: inherit;
    resize: none;
    
    &:focus {
      outline: none;
      border-color: var(--color-primary);
    }
  }
}

// 退款政策
.policy-section {
  padding: 0 24px 20px;
  
  h4 {
    font-size: 14px;
    font-weight: 600;
    margin: 0 0 8px;
  }
  
  ul {
    margin: 0;
    padding-left: 20px;
    
    li {
      font-size: 12px;
      color: var(--color-text-secondary);
      padding: 4px 0;
    }
  }
}

// 操作按钮
.modal-actions {
  display: flex;
  gap: 12px;
  padding: 16px 24px;
  border-top: 1px solid var(--color-border);
  
  :deep(.app-button) {
    flex: 1;
  }
}
</style>
