<script setup lang="ts">
import { ref, computed } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage, ElUpload, ElForm, ElFormItem, ElInput, ElSelect, ElOption } from 'element-plus'
import { AppCard } from '@/components/common'
import type { UploadFile } from 'element-plus'

const router = useRouter()
const route = useRoute()

// 从路由获取预填信息
const bookingId = ref(route.query.bookingId as string || '')
const institutionId = ref(route.query.institutionId as string || '')
const institutionName = ref(route.query.institutionName as string || '')

// 表单数据
const formData = ref({
  category: '',
  bookingOrderNumber: bookingId.value,
  institutionName: institutionName.value,
  description: '',
  expectation: ''
})

// 证据附件
const evidenceFiles = ref<UploadFile[]>([])

// 提交状态
const isSubmitting = ref(false)
const isSubmitted = ref(false)
const complaintNumber = ref('')

// 投诉分类
const categories = [
  { value: 'service_quality', label: '服务质量问题', desc: '服务态度差、不专业等' },
  { value: 'pet_safety', label: '宠物安全问题', desc: '宠物受伤、生病等' },
  { value: 'communication', label: '沟通问题', desc: '联系不上、回复不及时等' },
  { value: 'facility', label: '设施环境问题', desc: '环境脏乱、设施损坏等' },
  { value: 'price', label: '价格争议', desc: '乱收费、价格不透明等' },
  { value: 'other', label: '其他问题', desc: '其他投诉事项' }
]

// 表单验证规则
const rules = {
  category: [{ required: true, message: '请选择投诉类型', trigger: 'change' }],
  bookingOrderNumber: [{ required: true, message: '请输入订单号', trigger: 'blur' }],
  description: [
    { required: true, message: '请描述投诉内容', trigger: 'blur' },
    { min: 30, max: 2000, message: '描述长度在 30 到 2000 个字符', trigger: 'blur' }
  ]
}

// 表单引用
const formRef = ref()

// 是否可以提交
const canSubmit = computed(() => {
  return formData.value.category && 
         formData.value.bookingOrderNumber && 
         formData.value.description.length >= 30 &&
         evidenceFiles.value.length > 0
})

// 处理文件上传
const handleUploadChange = (file: UploadFile, fileList: UploadFile[]) => {
  // 限制文件大小 (10MB)
  if (file.raw && file.raw.size > 10 * 1024 * 1024) {
    ElMessage.warning('文件大小不能超过 10MB')
    fileList.pop()
    return
  }
  
  // 限制文件数量
  if (fileList.length > 10) {
    ElMessage.warning('最多上传 10 个证据文件')
    fileList.pop()
    return
  }
  
  evidenceFiles.value = fileList
}

// 移除文件
const handleRemove = (file: UploadFile) => {
  const index = evidenceFiles.value.findIndex(f => f.uid === file.uid)
  if (index > -1) {
    evidenceFiles.value.splice(index, 1)
  }
}

// 生成投诉编号
const generateComplaintNumber = () => {
  const date = new Date()
  const dateStr = date.toISOString().slice(0, 10).replace(/-/g, '')
  const random = Math.random().toString(36).substring(2, 8).toUpperCase()
  return `CP${dateStr}${random}`
}

// 提交投诉
const handleSubmit = async () => {
  try {
    await formRef.value?.validate()
    
    if (evidenceFiles.value.length === 0) {
      ElMessage.warning('请上传至少一个证据文件')
      return
    }
    
    isSubmitting.value = true
    
    // TODO: 对接真实API
    // const res = await complaintApi.submit(formData.value)
    // complaintNumber.value = res.data.complaintNumber
    
    complaintNumber.value = generateComplaintNumber()
    isSubmitted.value = true
    
    ElMessage.success('投诉提交成功')
  } catch {
    // 验证失败
  } finally {
    isSubmitting.value = false
  }
}

// 返回帮助中心
const goBack = () => {
  router.push('/help')
}

// 查看投诉进度（暂时跳转到帮助中心）
const viewProgress = () => {
  router.push('/help')
}
</script>

<template>
  <div class="complaint-form-view">
    <!-- 提交成功 -->
    <template v-if="isSubmitted">
      <AppCard class="success-card" shadow="md" padding="lg">
        <div class="success-content">
          <div class="success-icon">📋</div>
          <h2>投诉已提交</h2>
          <p class="complaint-number">投诉编号：<strong>{{ complaintNumber }}</strong></p>
          <div class="process-info">
            <h4>处理流程</h4>
            <div class="process-steps">
              <div class="step active">
                <span class="step-num">1</span>
                <span class="step-text">已提交</span>
              </div>
              <div class="step-line"></div>
              <div class="step">
                <span class="step-num">2</span>
                <span class="step-text">机构回复</span>
              </div>
              <div class="step-line"></div>
              <div class="step">
                <span class="step-num">3</span>
                <span class="step-text">平台审核</span>
              </div>
              <div class="step-line"></div>
              <div class="step">
                <span class="step-num">4</span>
                <span class="step-text">处理完成</span>
              </div>
            </div>
          </div>
          <p class="success-message">
            我们已通知相关机构，机构需在 48 小时内进行回复。
            <br>平台将在收到回复后进行审核处理。
          </p>
          <div class="success-actions">
            <button class="btn-secondary" @click="goBack">返回</button>
            <button class="btn-primary" @click="viewProgress">查看进度</button>
          </div>
        </div>
      </AppCard>
    </template>
    
    <!-- 投诉表单 -->
    <template v-else>
      <div class="page-header">
        <button class="back-btn" @click="goBack">← 返回</button>
        <h1>提交投诉</h1>
        <p>请如实填写投诉信息，我们将认真处理您的反馈</p>
      </div>
      
      <!-- 重要提示 -->
      <div class="warning-banner">
        <span class="warning-icon">⚠️</span>
        <div class="warning-content">
          <strong>温馨提示</strong>
          <p>投诉需提供真实有效的证据材料，恶意投诉将承担相应责任。如有紧急情况，请直接拨打客服热线。</p>
        </div>
      </div>
      
      <AppCard shadow="sm" padding="lg">
        <ElForm
          ref="formRef"
          :model="formData"
          :rules="rules"
          label-position="top"
          class="complaint-form"
        >
          <!-- 投诉类型 -->
          <ElFormItem label="投诉类型" prop="category">
            <div class="category-grid">
              <div 
                v-for="cat in categories"
                :key="cat.value"
                class="category-item"
                :class="{ selected: formData.category === cat.value }"
                @click="formData.category = cat.value"
              >
                <span class="cat-label">{{ cat.label }}</span>
                <span class="cat-desc">{{ cat.desc }}</span>
              </div>
            </div>
          </ElFormItem>
          
          <!-- 订单信息 -->
          <div class="order-section">
            <h3>订单信息</h3>
            <div class="order-fields">
              <ElFormItem label="订单号" prop="bookingOrderNumber">
                <ElInput
                  v-model="formData.bookingOrderNumber"
                  placeholder="请输入相关订单号"
                />
              </ElFormItem>
              <ElFormItem label="机构名称">
                <ElInput
                  v-model="formData.institutionName"
                  placeholder="被投诉机构名称"
                />
              </ElFormItem>
            </div>
          </div>
          
          <!-- 投诉内容 -->
          <ElFormItem label="投诉内容" prop="description">
            <ElInput
              v-model="formData.description"
              type="textarea"
              placeholder="请详细描述您遇到的问题，包括时间、经过、造成的影响等"
              :rows="6"
              maxlength="2000"
              show-word-limit
            />
          </ElFormItem>
          
          <!-- 证据上传 -->
          <ElFormItem label="证据材料（必填）" required>
            <ElUpload
              v-model:file-list="evidenceFiles"
              action="#"
              :auto-upload="false"
              :limit="10"
              :on-change="handleUploadChange"
              :on-remove="handleRemove"
              accept="image/*,video/*,.pdf"
              list-type="picture-card"
              class="evidence-upload"
            >
              <div class="upload-trigger">
                <span class="upload-icon">+</span>
                <span class="upload-text">上传证据</span>
              </div>
            </ElUpload>
            <div class="upload-tips">
              支持图片、视频、PDF，最多10个文件，单个不超过10MB
            </div>
          </ElFormItem>
          
          <!-- 期望处理方式 -->
          <ElFormItem label="期望处理方式（可选）">
            <ElInput
              v-model="formData.expectation"
              type="textarea"
              placeholder="请描述您期望的处理结果，如退款、道歉、赔偿等"
              :rows="3"
              maxlength="500"
            />
          </ElFormItem>
          
          <!-- 提交按钮 -->
          <div class="form-actions">
            <p class="submit-notice">
              提交投诉即表示您同意平台对投诉内容进行调查处理
            </p>
            <button 
              type="button"
              class="btn-primary"
              :disabled="!canSubmit || isSubmitting"
              @click="handleSubmit"
            >
              <span v-if="isSubmitting">提交中...</span>
              <span v-else>提交投诉</span>
            </button>
          </div>
        </ElForm>
      </AppCard>
    </template>
  </div>
</template>

<style lang="scss" scoped>
.complaint-form-view {
  max-width: 800px;
  margin: 0 auto;
}

.page-header {
  margin-bottom: 20px;
  
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

// 警告横幅
.warning-banner {
  display: flex;
  gap: 12px;
  padding: 16px;
  background: #FEF3C7;
  border: 1px solid #F59E0B;
  border-radius: var(--radius-md);
  margin-bottom: 20px;
  
  .warning-icon {
    font-size: 24px;
  }
  
  .warning-content {
    strong {
      display: block;
      color: #92400E;
      margin-bottom: 4px;
    }
    
    p {
      margin: 0;
      font-size: 13px;
      color: #92400E;
      line-height: 1.5;
    }
  }
}

// 分类选择
.category-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 12px;
  
  @media (max-width: 640px) {
    grid-template-columns: repeat(2, 1fr);
  }
}

.category-item {
  padding: 16px;
  border: 2px solid var(--color-border);
  border-radius: var(--radius-md);
  cursor: pointer;
  transition: all 0.2s;
  
  .cat-label {
    display: block;
    font-weight: 600;
    font-size: 14px;
    margin-bottom: 4px;
  }
  
  .cat-desc {
    font-size: 12px;
    color: var(--color-text-muted);
  }
  
  &:hover {
    border-color: var(--color-primary);
  }
  
  &.selected {
    border-color: var(--color-primary);
    background: var(--color-primary-light);
    
    .cat-label {
      color: var(--color-primary);
    }
  }
}

// 订单信息
.order-section {
  margin: 24px 0;
  padding: 20px;
  background: var(--color-neutral-50);
  border-radius: var(--radius-md);
  
  h3 {
    font-size: 16px;
    font-weight: 600;
    margin: 0 0 16px;
  }
}

.order-fields {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 16px;
  
  @media (max-width: 640px) {
    grid-template-columns: 1fr;
  }
}

// 证据上传
.evidence-upload {
  :deep(.el-upload--picture-card) {
    width: 100px;
    height: 100px;
    border-radius: var(--radius-md);
  }
  
  :deep(.el-upload-list__item) {
    width: 100px;
    height: 100px;
    border-radius: var(--radius-md);
  }
}

.upload-trigger {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  height: 100%;
  
  .upload-icon {
    font-size: 24px;
    color: var(--color-text-muted);
  }
  
  .upload-text {
    font-size: 12px;
    color: var(--color-text-muted);
    margin-top: 4px;
  }
}

.upload-tips {
  margin-top: 8px;
  font-size: 12px;
  color: var(--color-text-muted);
}

// 提交按钮
.form-actions {
  margin-top: 32px;
  text-align: center;
  
  .submit-notice {
    font-size: 12px;
    color: var(--color-text-muted);
    margin: 0 0 16px;
  }
}

.btn-primary {
  padding: 14px 48px;
  border: none;
  border-radius: var(--radius-lg);
  background: linear-gradient(135deg, #EF4444 0%, #F87171 100%);
  color: white;
  font-size: 16px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s;
  box-shadow: 0 4px 14px 0 rgba(239, 68, 68, 0.39);
  
  &:hover:not(:disabled) {
    transform: translateY(-2px);
    box-shadow: 0 6px 20px 0 rgba(239, 68, 68, 0.5);
  }
  
  &:disabled {
    opacity: 0.6;
    cursor: not-allowed;
  }
}

.btn-secondary {
  padding: 14px 32px;
  border: 2px solid var(--color-border);
  border-radius: var(--radius-lg);
  background: var(--color-surface);
  color: var(--color-text-secondary);
  font-size: 16px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.2s;
  
  &:hover {
    border-color: var(--color-primary);
    color: var(--color-primary);
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
  
  .complaint-number {
    font-size: 16px;
    color: var(--color-text-secondary);
    margin: 0 0 24px;
    
    strong {
      color: var(--color-error);
      font-family: var(--font-mono, monospace);
    }
  }
}

// 处理流程
.process-info {
  margin: 24px 0;
  padding: 20px;
  background: var(--color-neutral-50);
  border-radius: var(--radius-md);
  
  h4 {
    font-size: 14px;
    margin: 0 0 16px;
  }
}

.process-steps {
  display: flex;
  align-items: center;
  justify-content: center;
}

.step {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
  
  .step-num {
    width: 28px;
    height: 28px;
    border-radius: 50%;
    background: var(--color-neutral-200);
    color: var(--color-text-secondary);
    display: flex;
    align-items: center;
    justify-content: center;
    font-size: 12px;
    font-weight: 600;
  }
  
  .step-text {
    font-size: 12px;
    color: var(--color-text-muted);
  }
  
  &.active {
    .step-num {
      background: var(--color-primary);
      color: white;
    }
    
    .step-text {
      color: var(--color-primary);
      font-weight: 500;
    }
  }
}

.step-line {
  width: 40px;
  height: 2px;
  background: var(--color-neutral-200);
  margin: 0 8px;
  margin-bottom: 24px;
}

.success-message {
  color: var(--color-text-secondary);
  line-height: 1.6;
  margin: 0 0 24px;
}

.success-actions {
  display: flex;
  justify-content: center;
  gap: 16px;
}
</style>
