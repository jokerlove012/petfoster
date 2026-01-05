<script setup lang="ts">
import { ref, computed } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElUpload, ElForm, ElFormItem, ElInput, ElSelect, ElOption } from 'element-plus'
import { AppCard } from '@/components/common'
import { supportApi } from '@/api/support'
import { uploadApi } from '@/api/upload'
import type { UploadFile } from 'element-plus'

const router = useRouter()

// 表单数据
const formData = ref({
  category: '',
  subject: '',
  description: '',
  contactPhone: '',
  contactEmail: ''
})

// 附件列表
const attachments = ref<UploadFile[]>([])

// 提交状态
const isSubmitting = ref(false)
const isSubmitted = ref(false)
const ticketNumber = ref('')

// 工单分类
const categories = [
  { value: 'booking', label: '预约相关', icon: '📋' },
  { value: 'payment', label: '支付退款', icon: '💰' },
  { value: 'service', label: '服务问题', icon: '🐾' },
  { value: 'technical', label: '技术问题', icon: '🔧' },
  { value: 'complaint', label: '投诉建议', icon: '📢' },
  { value: 'other', label: '其他问题', icon: '❓' }
]

// 表单验证规则
const rules = {
  category: [{ required: true, message: '请选择问题分类', trigger: 'change' }],
  subject: [
    { required: true, message: '请输入问题标题', trigger: 'blur' },
    { min: 5, max: 100, message: '标题长度在 5 到 100 个字符', trigger: 'blur' }
  ],
  description: [
    { required: true, message: '请描述您的问题', trigger: 'blur' },
    { min: 20, max: 2000, message: '描述长度在 20 到 2000 个字符', trigger: 'blur' }
  ]
}

// 表单引用
const formRef = ref()

// 是否可以提交
const canSubmit = computed(() => {
  return formData.value.category && 
         formData.value.subject.length >= 5 && 
         formData.value.description.length >= 20
})

// 处理文件上传
const handleUploadChange = (file: UploadFile, fileList: UploadFile[]) => {
  // 限制文件大小 (5MB)
  if (file.raw && file.raw.size > 5 * 1024 * 1024) {
    ElMessage.warning('文件大小不能超过 5MB')
    fileList.pop()
    return
  }
  
  // 限制文件数量
  if (fileList.length > 5) {
    ElMessage.warning('最多上传 5 个附件')
    fileList.pop()
    return
  }
  
  attachments.value = fileList
}

// 移除文件
const handleRemove = (file: UploadFile) => {
  const index = attachments.value.findIndex(f => f.uid === file.uid)
  if (index > -1) {
    attachments.value.splice(index, 1)
  }
}

// 生成工单号
const generateTicketNumber = () => {
  const date = new Date()
  const dateStr = date.toISOString().slice(0, 10).replace(/-/g, '')
  const random = Math.random().toString(36).substring(2, 8).toUpperCase()
  return `TK${dateStr}${random}`
}

// 提交工单
const handleSubmit = async () => {
  try {
    await formRef.value?.validate()
    
    isSubmitting.value = true
    
    // 先上传附件
    const uploadedUrls: string[] = []
    console.log('附件数量:', attachments.value.length)
    console.log('附件列表:', attachments.value)
    
    for (const file of attachments.value) {
      console.log('处理文件:', file.name, 'raw:', file.raw)
      if (file.raw) {
        try {
          console.log('开始上传文件:', file.name)
          const uploadRes = await uploadApi.upload(file.raw)
          console.log('上传结果:', uploadRes)
          if (uploadRes.code === 200 && uploadRes.data?.url) {
            uploadedUrls.push(uploadRes.data.url)
            console.log('上传成功, URL:', uploadRes.data.url)
          }
        } catch (e) {
          console.error('上传附件失败:', e)
        }
      }
    }
    
    console.log('所有上传的URL:', uploadedUrls)
    
    const res = await supportApi.submitComplaint({
      category: formData.value.category,
      subject: formData.value.subject,
      description: formData.value.description,
      attachments: uploadedUrls.length > 0 ? uploadedUrls : undefined,
      contactPhone: formData.value.contactPhone || undefined,
      contactEmail: formData.value.contactEmail || undefined
    })
    
    if (res.code === 200 && res.data) {
      ticketNumber.value = res.data.complaintNumber
      isSubmitted.value = true
      ElMessage.success('工单提交成功')
    } else {
      ElMessage.error(res.message || '提交失败')
    }
  } catch (error: any) {
    ElMessage.error(error.message || '提交失败，请重试')
  } finally {
    isSubmitting.value = false
  }
}

// 返回帮助中心
const goBack = () => {
  router.push('/help')
}

// 继续提交
const submitAnother = () => {
  formData.value = {
    category: '',
    subject: '',
    description: '',
    contactPhone: '',
    contactEmail: ''
  }
  attachments.value = []
  isSubmitted.value = false
  ticketNumber.value = ''
}
</script>

<template>
  <div class="ticket-submit-view">
    <!-- 提交成功 -->
    <template v-if="isSubmitted">
      <AppCard class="success-card" shadow="md" padding="lg">
        <div class="success-content">
          <div class="success-icon">✅</div>
          <h2>工单提交成功</h2>
          <p class="ticket-number">工单号：<strong>{{ ticketNumber }}</strong></p>
          <p class="success-message">
            我们已收到您的问题，客服人员将在 24 小时内与您联系。
            <br>您可以在"我的工单"中查看处理进度。
          </p>
          <div class="success-actions">
            <button class="btn-secondary" @click="goBack">返回帮助中心</button>
            <button class="btn-primary" @click="submitAnother">继续提交</button>
          </div>
        </div>
      </AppCard>
    </template>
    
    <!-- 提交表单 -->
    <template v-else>
      <div class="page-header">
        <button class="back-btn" @click="goBack">← 返回</button>
        <h1>提交工单</h1>
        <p>请详细描述您遇到的问题，我们将尽快为您处理</p>
      </div>
      
      <AppCard shadow="sm" padding="lg">
        <ElForm
          ref="formRef"
          :model="formData"
          :rules="rules"
          label-position="top"
          class="ticket-form"
        >
          <!-- 问题分类 -->
          <ElFormItem label="问题分类" prop="category">
            <ElSelect 
              v-model="formData.category" 
              placeholder="请选择问题分类"
              class="full-width"
            >
              <ElOption
                v-for="cat in categories"
                :key="cat.value"
                :label="cat.label"
                :value="cat.value"
              >
                <span class="category-option">
                  <span class="cat-icon">{{ cat.icon }}</span>
                  {{ cat.label }}
                </span>
              </ElOption>
            </ElSelect>
          </ElFormItem>
          
          <!-- 问题标题 -->
          <ElFormItem label="问题标题" prop="subject">
            <ElInput
              v-model="formData.subject"
              placeholder="请简要描述您的问题"
              maxlength="100"
              show-word-limit
            />
          </ElFormItem>
          
          <!-- 问题描述 -->
          <ElFormItem label="问题描述" prop="description">
            <ElInput
              v-model="formData.description"
              type="textarea"
              placeholder="请详细描述您遇到的问题，包括时间、操作步骤等信息"
              :rows="6"
              maxlength="2000"
              show-word-limit
            />
          </ElFormItem>
          
          <!-- 附件上传 -->
          <ElFormItem label="附件（可选）">
            <ElUpload
              v-model:file-list="attachments"
              action="#"
              :auto-upload="false"
              :limit="5"
              :on-change="handleUploadChange"
              :on-remove="handleRemove"
              accept="image/*,.pdf,.doc,.docx"
              class="upload-area"
            >
              <div class="upload-trigger">
                <span class="upload-icon">📎</span>
                <span>点击或拖拽上传附件</span>
                <span class="upload-hint">支持图片、PDF、Word，最多5个，单个不超过5MB</span>
              </div>
            </ElUpload>
          </ElFormItem>
          
          <!-- 联系方式 -->
          <div class="contact-section">
            <h3>联系方式（可选）</h3>
            <div class="contact-fields">
              <ElFormItem label="手机号码">
                <ElInput
                  v-model="formData.contactPhone"
                  placeholder="方便我们联系您"
                  maxlength="11"
                />
              </ElFormItem>
              <ElFormItem label="邮箱地址">
                <ElInput
                  v-model="formData.contactEmail"
                  placeholder="接收处理结果通知"
                />
              </ElFormItem>
            </div>
          </div>
          
          <!-- 提交按钮 -->
          <div class="form-actions">
            <button 
              type="button"
              class="btn-primary"
              :disabled="!canSubmit || isSubmitting"
              @click="handleSubmit"
            >
              <span v-if="isSubmitting">提交中...</span>
              <span v-else>提交工单</span>
            </button>
          </div>
        </ElForm>
      </AppCard>
      
      <!-- 提示信息 -->
      <div class="tips-section">
        <h4>💡 温馨提示</h4>
        <ul>
          <li>请尽量详细描述问题，以便我们更快定位和解决</li>
          <li>如涉及订单问题，请提供订单号</li>
          <li>工作时间（9:00-21:00）提交的工单将优先处理</li>
          <li>紧急问题请直接拨打客服热线：400-XXX-XXXX</li>
        </ul>
      </div>
    </template>
  </div>
</template>

<style lang="scss" scoped>
.ticket-submit-view {
  max-width: 700px;
  margin: 0 auto;
}

.page-header {
  margin-bottom: 24px;
  
  .back-btn {
    background: none;
    border: none;
    color: var(--color-text-secondary);
    font-size: 14px;
    cursor: pointer;
    padding: 0;
    margin-bottom: 12px;
    
    &:hover {
      color: var(--color-primary);
    }
  }
  
  h1 {
    font-family: var(--font-display);
    font-size: 28px;
    margin: 0 0 8px;
  }
  
  p {
    color: var(--color-text-secondary);
    margin: 0;
  }
}

.ticket-form {
  .full-width {
    width: 100%;
  }
}

.category-option {
  display: flex;
  align-items: center;
  gap: 8px;
  
  .cat-icon {
    font-size: 16px;
  }
}

// 上传区域
.upload-area {
  width: 100%;
  
  :deep(.el-upload) {
    width: 100%;
  }
  
  :deep(.el-upload-dragger) {
    width: 100%;
    border: 2px dashed var(--color-border);
    border-radius: var(--radius-md);
    padding: 24px;
    
    &:hover {
      border-color: var(--color-primary);
    }
  }
}

.upload-trigger {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
  color: var(--color-text-secondary);
  
  .upload-icon {
    font-size: 32px;
  }
  
  .upload-hint {
    font-size: 12px;
    color: var(--color-text-muted);
  }
}

// 联系方式
.contact-section {
  margin-top: 24px;
  padding-top: 24px;
  border-top: 1px solid var(--color-border);
  
  h3 {
    font-size: 16px;
    font-weight: 600;
    margin: 0 0 16px;
  }
}

.contact-fields {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 16px;
  
  @media (max-width: 640px) {
    grid-template-columns: 1fr;
  }
}

// 提交按钮
.form-actions {
  margin-top: 32px;
  text-align: center;
}

.btn-primary {
  padding: 14px 48px;
  border: none;
  border-radius: var(--radius-lg);
  background: linear-gradient(135deg, var(--color-primary) 0%, #FF8F5C 100%);
  color: white;
  font-size: 16px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s;
  box-shadow: 0 4px 14px 0 rgba(255, 107, 53, 0.39);
  
  &:hover:not(:disabled) {
    transform: translateY(-2px);
    box-shadow: 0 6px 20px 0 rgba(255, 107, 53, 0.5);
  }
  
  &:disabled {
    opacity: 0.6;
    cursor: not-allowed;
  }
}

.btn-secondary {
  padding: 14px 32px;
  border: 2px solid var(--color-primary);
  border-radius: var(--radius-lg);
  background: var(--color-surface);
  color: var(--color-primary);
  font-size: 16px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.2s;
  
  &:hover {
    background: var(--color-primary-light);
  }
}

// 提示信息
.tips-section {
  margin-top: 24px;
  padding: 16px 20px;
  background: var(--color-primary-light);
  border-radius: var(--radius-md);
  
  h4 {
    font-size: 14px;
    font-weight: 600;
    margin: 0 0 12px;
  }
  
  ul {
    margin: 0;
    padding-left: 20px;
    
    li {
      font-size: 13px;
      color: var(--color-text-secondary);
      line-height: 1.8;
    }
  }
}

// 成功页面
.success-card {
  text-align: center;
}

.success-content {
  padding: 24px 0;
  
  .success-icon {
    font-size: 64px;
    margin-bottom: 16px;
  }
  
  h2 {
    font-family: var(--font-display);
    font-size: 24px;
    margin: 0 0 16px;
  }
  
  .ticket-number {
    font-size: 16px;
    color: var(--color-text-secondary);
    margin: 0 0 12px;
    
    strong {
      color: var(--color-primary);
      font-family: var(--font-mono, monospace);
    }
  }
  
  .success-message {
    color: var(--color-text-secondary);
    line-height: 1.6;
    margin: 0 0 24px;
  }
}

.success-actions {
  display: flex;
  justify-content: center;
  gap: 16px;
}
</style>
