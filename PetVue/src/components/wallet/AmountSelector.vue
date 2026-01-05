<template>
  <div class="amount-selector">
    <div class="preset-amounts">
      <div
        v-for="amount in presetAmounts"
        :key="amount"
        :class="['amount-option', { selected: selectedAmount === amount && !isCustom }]"
        @click="selectAmount(amount)"
      >
        <span class="value">{{ amount }}</span>
        <span class="unit">元</span>
      </div>
    </div>
    <div class="custom-amount">
      <el-input
        v-model="customAmount"
        placeholder="自定义金额"
        type="number"
        :min="minAmount"
        :max="maxAmount"
        @focus="isCustom = true"
        @input="onCustomInput"
      >
        <template #prepend>¥</template>
        <template #append>元</template>
      </el-input>
    </div>
    <div class="amount-tips" v-if="tips">
      <el-icon><InfoFilled /></el-icon>
      {{ tips }}
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, watch } from 'vue'
import { InfoFilled } from '@element-plus/icons-vue'

const props = withDefaults(defineProps<{
  modelValue: number
  presetAmounts?: number[]
  minAmount?: number
  maxAmount?: number
}>(), {
  presetAmounts: () => [50, 100, 200, 500, 1000, 2000],
  minAmount: 1,
  maxAmount: 50000
})

const emit = defineEmits<{
  (e: 'update:modelValue', value: number): void
}>()

const selectedAmount = ref(props.modelValue || props.presetAmounts[0])
const customAmount = ref('')
const isCustom = ref(false)

const tips = computed(() => {
  return `充值金额范围: ${props.minAmount} - ${props.maxAmount} 元`
})

function selectAmount(amount: number) {
  isCustom.value = false
  customAmount.value = ''
  selectedAmount.value = amount
  emit('update:modelValue', amount)
}

function onCustomInput() {
  const value = parseFloat(customAmount.value)
  if (!isNaN(value) && value >= props.minAmount && value <= props.maxAmount) {
    selectedAmount.value = value
    emit('update:modelValue', value)
  }
}

watch(() => props.modelValue, (val) => {
  if (val && !props.presetAmounts.includes(val)) {
    isCustom.value = true
    customAmount.value = val.toString()
  }
  selectedAmount.value = val
})
</script>

<style scoped lang="scss">
.amount-selector {
  .preset-amounts {
    display: grid;
    grid-template-columns: repeat(3, 1fr);
    gap: 12px;
    margin-bottom: 16px;
  }
  
  .amount-option {
    padding: 16px;
    text-align: center;
    border: 2px solid var(--el-border-color, #dcdfe6);
    border-radius: 12px;
    cursor: pointer;
    transition: all 0.2s;
    
    &:hover {
      border-color: var(--el-color-primary);
    }
    
    &.selected {
      border-color: var(--el-color-primary);
      background: var(--el-color-primary-light-9);
    }
    
    .value {
      font-size: 20px;
      font-weight: 700;
      color: var(--el-text-color-primary);
    }
    
    .unit {
      font-size: 14px;
      color: var(--el-text-color-secondary);
      margin-left: 2px;
    }
  }
  
  .custom-amount {
    margin-bottom: 12px;
  }
  
  .amount-tips {
    display: flex;
    align-items: center;
    gap: 4px;
    font-size: 13px;
    color: var(--el-text-color-secondary);
  }
}
</style>
