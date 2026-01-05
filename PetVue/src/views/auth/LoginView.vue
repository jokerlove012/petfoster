<script setup lang="ts">
import { ref, reactive } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { useAuthStore } from '@/stores'
import { authApi } from '@/api/auth'
import type { UserRole } from '@/types/user'

const router = useRouter()
const route = useRoute()
const authStore = useAuthStore()

const form = reactive({
  phone: '',
  password: '',
  role: 'pet_owner' as UserRole
})

const rules = {
  phone: [
    { required: true, message: '请输入手机号', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '请输入有效的手机号', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, message: '密码至少6个字符', trigger: 'blur' }
  ],
  role: [
    { required: true, message: '请选择登录角色', trigger: 'change' }
  ]
}

const roleOptions = [
  { value: 'pet_owner', label: '宠物主人', icon: '🐕', desc: '寻找寄养服务' },
  { value: 'institution_staff', label: '寄养机构', icon: '🏠', desc: '管理机构服务' },
  { value: 'admin', label: '管理员', icon: '👨‍💼', desc: '平台管理' }
]

const loading = ref(false)
const formRef = ref<InstanceType<typeof import('element-plus')['ElForm']>>()

// 根据角色获取跳转路径
const getRedirectPath = (role: UserRole): string => {
  switch (role) {
    case 'institution_staff':
      return '/institution/dashboard'
    case 'admin':
      return '/admin/dashboard'
    default:
      return '/'
  }
}

const handleLogin = async () => {
  console.log('handleLogin called', form)
  
  if (!formRef.value) {
    console.log('formRef is null, submitting anyway')
  }
  
  // 简单验证
  if (!form.phone || !form.password) {
    ElMessage.error('请填写手机号和密码')
    return
  }
  
  if (!/^1[3-9]\d{9}$/.test(form.phone)) {
    ElMessage.error('请输入有效的手机号')
    return
  }

  loading.value = true
  
  try {
    console.log('calling authApi.login with:', { phone: form.phone, password: form.password, role: form.role })
    const res = await authApi.login({
      phone: form.phone,
      password: form.password,
      role: form.role
    })
    
    console.log('login response', res)
    
    if (res.code === 200 && res.data) {
      authStore.setUser(res.data.user)
      authStore.setToken(res.data.token)
      
      ElMessage.success('登录成功')
      
      // 根据角色跳转到对应页面
      const redirect = route.query.redirect as string
      router.push(redirect || getRedirectPath(form.role))
    } else {
      ElMessage.error(res.message || '登录失败')
    }
  } catch (error: any) {
    console.error('login error', error)
    ElMessage.error(error.response?.data?.message || '登录失败，请稍后重试')
  } finally {
    loading.value = false
  }
}
</script>


<template>
  <div class="auth-view">
    <div class="auth-card">
      <div class="logo">
        <span class="logo-icon">🐾</span>
        <span class="logo-text">宠物寄养</span>
      </div>
      
      <h1 class="auth-title">欢迎回来</h1>
      <p class="auth-subtitle">登录您的账户继续使用</p>
      
      <el-form 
        ref="formRef"
        :model="form" 
        :rules="rules"
        class="auth-form"
        @submit.prevent="handleLogin"
      >
        <el-form-item prop="phone">
          <el-input
            v-model="form.phone"
            placeholder="手机号"
            size="large"
            prefix-icon="Phone"
            maxlength="11"
          />
        </el-form-item>
        
        <el-form-item prop="password">
          <el-input
            v-model="form.password"
            type="password"
            placeholder="密码"
            size="large"
            prefix-icon="Lock"
            show-password
          />
        </el-form-item>

        <el-form-item prop="role" class="role-select">
          <div class="role-label">选择登录身份</div>
          <div class="role-options">
            <div 
              v-for="option in roleOptions" 
              :key="option.value"
              class="role-option"
              :class="{ active: form.role === option.value }"
              @click="form.role = option.value as UserRole"
            >
              <span class="role-icon">{{ option.icon }}</span>
              <span class="role-name">{{ option.label }}</span>
              <span class="role-desc">{{ option.desc }}</span>
            </div>
          </div>
        </el-form-item>
        
        <el-form-item>
          <el-button
            type="primary"
            size="large"
            class="submit-btn"
            :loading="loading"
            native-type="button"
            @click.stop="handleLogin"
          >
            登录
          </el-button>
        </el-form-item>
      </el-form>
      
      <div class="auth-footer">
        <span>还没有账户？</span>
        <router-link to="/register">立即注册</router-link>
      </div>
    </div>
  </div>
</template>


<style lang="scss" scoped>
.auth-view {
  min-height: calc(100vh - 64px);
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, var(--color-primary-light) 0%, var(--color-accent-light) 100%);
  padding: 20px;
  margin: -24px;
}

.auth-card {
  background: white;
  border-radius: var(--radius-xl);
  padding: 40px;
  width: 100%;
  max-width: 420px;
  box-shadow: var(--shadow-xl);
}

.logo {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  margin-bottom: 24px;
  
  &-icon {
    font-size: 32px;
  }
  
  &-text {
    font-family: var(--font-display);
    font-size: 24px;
    font-weight: 700;
    color: var(--color-primary);
  }
}

.auth-title {
  font-family: var(--font-display);
  font-size: 1.75rem;
  font-weight: 700;
  color: var(--color-text-primary);
  margin-bottom: 8px;
  text-align: center;
}

.auth-subtitle {
  color: var(--color-text-secondary);
  text-align: center;
  margin-bottom: 32px;
}

.auth-form {
  .el-form-item {
    margin-bottom: 20px;
  }
}

.role-select {
  .role-label {
    font-size: 14px;
    font-weight: 500;
    color: var(--color-text-secondary);
    margin-bottom: 12px;
  }
}

.role-options {
  display: flex;
  gap: 12px;
  width: 100%;
}

.role-option {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 4px;
  padding: 16px 8px;
  border: 2px solid var(--color-border);
  border-radius: var(--radius-lg);
  cursor: pointer;
  transition: all 200ms ease;
  
  &:hover {
    border-color: var(--color-primary-light);
    background: var(--color-primary-light);
  }
  
  &.active {
    border-color: var(--color-primary);
    background: var(--color-primary-light);
    
    .role-icon {
      transform: scale(1.1);
    }
    
    .role-name {
      color: var(--color-primary);
    }
  }
  
  .role-icon {
    font-size: 24px;
    transition: transform 200ms ease;
  }
  
  .role-name {
    font-size: 13px;
    font-weight: 600;
    color: var(--color-text-primary);
  }
  
  .role-desc {
    font-size: 11px;
    color: var(--color-text-muted);
    text-align: center;
  }
}

.submit-btn {
  width: 100%;
  height: 48px;
  font-size: 16px;
  font-weight: 600;
}

.auth-footer {
  text-align: center;
  margin-top: 24px;
  color: var(--color-text-secondary);
  
  a {
    color: var(--color-primary);
    font-weight: 600;
    margin-left: 4px;
  }
}

@media (max-width: 480px) {
  .role-options {
    flex-direction: column;
  }
  
  .role-option {
    flex-direction: row;
    justify-content: flex-start;
    gap: 12px;
    padding: 12px 16px;
    
    .role-desc {
      display: none;
    }
  }
}
</style>
