import { createApp } from 'vue'
import { createPinia } from 'pinia'
import ElementPlus from 'element-plus'
import 'element-plus/dist/index.css'
import zhCn from 'element-plus/dist/locale/zh-cn.mjs'

import App from './App.vue'
import router from './router'
import './styles/index.scss'

async function bootstrap() {
  const app = createApp(App)
  const pinia = createPinia()

  app.use(pinia)
  app.use(router)
  app.use(ElementPlus, {
    locale: zhCn,
  })

  // 从 localStorage 恢复登录状态
  const { useAuthStore } = await import('./stores/auth')
  const authStore = useAuthStore()
  authStore.initFromStorage()

  app.mount('#app')
}

bootstrap()
