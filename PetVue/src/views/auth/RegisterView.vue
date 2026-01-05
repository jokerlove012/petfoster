<script setup lang="ts">
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { authApi } from '@/api/auth'
import type { UserRole } from '@/types/user'

const router = useRouter()

const form = reactive({
  name: '',
  phone: '',
  password: '',
  confirmPassword: '',
  role: 'pet_owner' as UserRole,
  agreement: false
})

const validateConfirmPassword = (_rule: any, value: string, callback: Function) => {
  if (value !== form.password) {
    callback(new Error('两次输入的密码不一致'))
  } else {
    callback()
  }
}

const rules = {
  name: [
    { required: true, message: '请输入姓名', trigger: 'blur' },
    { min: 2, max: 20, message: '姓名长度在2-20个字符', trigger: 'blur' }
  ],
  phone: [
    { required: true, message: '请输入手机号', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '请输入有效的手机号', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, max: 20, message: '密码长度在6-20个字符', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请确认密码', trigger: 'blur' },
    { validator: validateConfirmPassword, trigger: 'blur' }
  ],
  agreement: [
    { 
      validator: (_rule: any, value: boolean, callback: Function) => {
        if (!value) {
          callback(new Error('请阅读并同意用户协议'))
        } else {
          callback()
        }
      }, 
      trigger: 'change' 
    }
  ]
}

const loading = ref(false)
const formRef = ref()

const handleRegister = async () => {
  if (!formRef.value) return
  
  try {
    await formRef.value.validate()
  } catch {
    return
  }

  loading.value = true
  
  try {
    const res = await authApi.register({
      phone: form.phone,
      password: form.password,
      role: form.role,
      name: form.name
    })
    
    if (res.code === 200) {
      ElMessage.success('注册成功，请登录')
      router.push('/login')
    } else {
      ElMessage.error(res.message || '注册失败')
    }
  } catch (error: any) {
    ElMessage.error(error.response?.data?.message || '注册失败，请稍后重试')
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
      
      <h1 class="auth-title">创建账户</h1>
      <p class="auth-subtitle">加入我们，为您的爱宠找到温馨的家</p>
      
      <el-form 
        ref="formRef"
        :model="form" 
        :rules="rules"
        class="auth-form"
        @submit.prevent="handleRegister"
      >
        <el-form-item prop="role" class="role-item">
          <div class="role-selector">
            <div 
              :class="['role-option', { active: form.role === 'pet_owner' }]"
              @click="form.role = 'pet_owner'"
            >
              <span class="role-icon">🐕</span>
              <span class="role-label">宠物主人</span>
              <span class="role-desc">寻找寄养服务</span>
            </div>
            <div 
              :class="['role-option', { active: form.role === 'institution_staff' }]"
              @click="form.role = 'institution_staff'"
            >
              <span class="role-icon">🏠</span>
              <span class="role-label">寄养机构</span>
              <span class="role-desc">提供寄养服务</span>
            </div>
          </div>
        </el-form-item>

        <el-form-item prop="name">
          <el-input
            v-model="form.name"
            placeholder="姓名"
            size="large"
            prefix-icon="User"
          />
        </el-form-item>
        
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
        
        <el-form-item prop="confirmPassword">
          <el-input
            v-model="form.confirmPassword"
            type="password"
            placeholder="确认密码"
            size="large"
            prefix-icon="Lock"
            show-password
          />
        </el-form-item>
        
        <el-form-item prop="agreement">
          <el-checkbox v-model="form.agreement">
            我已阅读并同意
            <a href="#" class="link">《用户协议》</a>
            和
            <a href="#" class="link">《隐私政策》</a>
          </el-checkbox>
        </el-form-item>
        
        <el-form-item>
          <el-button
            type="primary"
            size="large"
            class="submit-btn"
            :loading="loading"
            @click="handleRegister"
          >
            注册
          </el-button>
        </el-form-item>
      </el-form>
      
      <div class="auth-footer">
        <span>已有账户？</span>
        <router-link to="/login">立即登录</router-link>
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
  max-width: 480px;
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

.role-item {
  margin-bottom: 24px !important;
}

.role-selector {
  display: flex;
  gap: 16px;
  width: 100%;
}

.role-option {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 16px;
  border: 2px solid var(--color-border);
  border-radius: var(--radius-lg);
  cursor: pointer;
  transition: all 200ms ease;
  
  &:hover {
    border-color: var(--color-primary);
  }
  
  &.active {
    border-color: var(--color-primary);
    background: var(--color-primary-light);
  }
  
  .role-icon {
    font-size: 32px;
    margin-bottom: 8px;
  }
  
  .role-label {
    font-weight: 600;
    color: var(--color-text-primary);
    margin-bottom: 4px;
  }
  
  .role-desc {
    font-size: 12px;
    color: var(--color-text-muted);
  }
}

.link {
  color: var(--color-primary);
  
  &:hover {
    text-decoration: underline;
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
</style>
