import { ref, onMounted, onUnmounted } from 'vue'

/**
 * 自动刷新 composable
 * @param fetchFn 刷新数据的函数
 * @param interval 刷新间隔（毫秒），默认 10 秒
 */
export function useAutoRefresh(fetchFn: () => Promise<void>, interval = 10000) {
  const isAutoRefresh = ref(true)
  let timer: ReturnType<typeof setInterval> | null = null

  const startAutoRefresh = () => {
    if (timer) return
    timer = setInterval(async () => {
      if (isAutoRefresh.value) {
        await fetchFn()
      }
    }, interval)
  }

  const stopAutoRefresh = () => {
    if (timer) {
      clearInterval(timer)
      timer = null
    }
  }

  const toggleAutoRefresh = () => {
    isAutoRefresh.value = !isAutoRefresh.value
  }

  // 页面可见性变化时控制刷新
  const handleVisibilityChange = () => {
    if (document.hidden) {
      stopAutoRefresh()
    } else {
      startAutoRefresh()
    }
  }

  onMounted(() => {
    startAutoRefresh()
    document.addEventListener('visibilitychange', handleVisibilityChange)
  })

  onUnmounted(() => {
    stopAutoRefresh()
    document.removeEventListener('visibilitychange', handleVisibilityChange)
  })

  return {
    isAutoRefresh,
    startAutoRefresh,
    stopAutoRefresh,
    toggleAutoRefresh
  }
}
