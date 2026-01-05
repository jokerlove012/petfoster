import { ref } from 'vue'
import type { Booking } from '@/types/booking'

/**
 * 重新预约数据
 */
export interface RebookingData {
  institutionId: string
  institutionName?: string
  servicePackageId?: string
  petId?: string
  emergencyContact?: {
    name: string
    phone: string
    relationship: string
  }
  specialRequirements?: string
}

const REBOOKING_KEY = 'pet_foster_rebooking'

// 全局状态
const rebookingData = ref<RebookingData | null>(null)

/**
 * 重新预约功能 composable
 */
export function useRebooking() {
  /**
   * 设置重新预约数据
   */
  function setRebookingData(data: RebookingData) {
    rebookingData.value = data
    try {
      sessionStorage.setItem(REBOOKING_KEY, JSON.stringify(data))
    } catch (e) {
      console.error('Failed to save rebooking data:', e)
    }
  }

  /**
   * 从历史订单创建重新预约数据
   */
  function createFromOrder(order: Booking): RebookingData {
    return {
      institutionId: order.institutionId,
      servicePackageId: order.servicePackageId,
      petId: order.petId,
      emergencyContact: order.emergencyContact
        ? { ...order.emergencyContact }
        : undefined,
      specialRequirements: order.specialRequirements
    }
  }

  /**
   * 从机构ID创建重新预约数据（用于浏览历史）
   */
  function createFromInstitution(
    institutionId: string,
    institutionName?: string
  ): RebookingData {
    return {
      institutionId,
      institutionName
    }
  }

  /**
   * 获取重新预约数据
   */
  function getRebookingData(): RebookingData | null {
    if (rebookingData.value) {
      return rebookingData.value
    }

    try {
      const stored = sessionStorage.getItem(REBOOKING_KEY)
      if (stored) {
        rebookingData.value = JSON.parse(stored)
        return rebookingData.value
      }
    } catch (e) {
      console.error('Failed to load rebooking data:', e)
    }

    return null
  }

  /**
   * 清除重新预约数据
   */
  function clearRebookingData() {
    rebookingData.value = null
    try {
      sessionStorage.removeItem(REBOOKING_KEY)
    } catch (e) {
      console.error('Failed to clear rebooking data:', e)
    }
  }

  /**
   * 检查是否有重新预约数据
   */
  function hasRebookingData(): boolean {
    return getRebookingData() !== null
  }

  return {
    rebookingData,
    setRebookingData,
    createFromOrder,
    createFromInstitution,
    getRebookingData,
    clearRebookingData,
    hasRebookingData
  }
}
