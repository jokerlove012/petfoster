import { ref, onMounted, onUnmounted } from 'vue'

export type DeviceType = 'mobile' | 'tablet' | 'desktop'

const BREAKPOINTS = {
  mobile: 768,
  tablet: 1024
}

export function useDevice() {
  const deviceType = ref<DeviceType>('desktop')
  const isMobile = ref(false)
  const isTablet = ref(false)
  const isDesktop = ref(true)
  const windowWidth = ref(typeof window !== 'undefined' ? window.innerWidth : 1200)

  const updateDevice = () => {
    windowWidth.value = window.innerWidth
    
    if (windowWidth.value < BREAKPOINTS.mobile) {
      deviceType.value = 'mobile'
      isMobile.value = true
      isTablet.value = false
      isDesktop.value = false
    } else if (windowWidth.value < BREAKPOINTS.tablet) {
      deviceType.value = 'tablet'
      isMobile.value = false
      isTablet.value = true
      isDesktop.value = false
    } else {
      deviceType.value = 'desktop'
      isMobile.value = false
      isTablet.value = false
      isDesktop.value = true
    }
  }

  onMounted(() => {
    updateDevice()
    window.addEventListener('resize', updateDevice)
  })

  onUnmounted(() => {
    window.removeEventListener('resize', updateDevice)
  })

  return {
    deviceType,
    isMobile,
    isTablet,
    isDesktop,
    windowWidth
  }
}
