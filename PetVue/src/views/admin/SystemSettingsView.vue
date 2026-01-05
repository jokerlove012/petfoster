<script setup lang="ts">
import { ref } from 'vue'
import { Settings, Bell, Shield, Database, Mail, Globe, Save, RefreshCw } from 'lucide-vue-next'
import { ElMessage } from 'element-plus'

const activeTab = ref('general')

// 通用设置
const generalSettings = ref({
  siteName: '宠物寄养平台',
  siteDescription: '专业的宠物寄养服务平台',
  contactEmail: 'support@petfoster.com',
  contactPhone: '400-888-8888',
  maintenanceMode: false,
  registrationOpen: true
})

// 通知设置
const notificationSettings = ref({
  emailNotification: true,
  smsNotification: true,
  pushNotification: true,
  orderNotify: true,
  refundNotify: true,
  complaintNotify: true,
  systemNotify: true
})

// 安全设置
const securitySettings = ref({
  passwordMinLength: 8,
  passwordRequireSpecial: true,
  sessionTimeout: 30,
  maxLoginAttempts: 5,
  twoFactorAuth: false,
  ipWhitelist: ''
})

// 平台费率设置
const feeSettings = ref({
  platformFeeRate: 10,
  minWithdrawAmount: 100,
  withdrawFeeRate: 0.5,
  settlementCycle: 7
})

const saveSettings = () => {
  ElMessage.success('设置已保存')
}

const resetSettings = () => {
  ElMessage.info('设置已重置为默认值')
}
</script>

<template>
  <div class="system-settings">
    <div class="page-header">
      <div class="header-left">
        <h1>⚙️ 系统设置</h1>
        <p>管理平台全局配置</p>
      </div>
      <div class="header-actions">
        <button class="btn-reset" @click="resetSettings"><RefreshCw :size="16" /> 重置</button>
        <button class="btn-save" @click="saveSettings"><Save :size="16" /> 保存设置</button>
      </div>
    </div>

    <div class="settings-layout">
      <!-- 侧边导航 -->
      <div class="settings-nav">
        <button :class="{ active: activeTab === 'general' }" @click="activeTab = 'general'">
          <Globe :size="18" /> 通用设置
        </button>
        <button :class="{ active: activeTab === 'notification' }" @click="activeTab = 'notification'">
          <Bell :size="18" /> 通知设置
        </button>
        <button :class="{ active: activeTab === 'security' }" @click="activeTab = 'security'">
          <Shield :size="18" /> 安全设置
        </button>
        <button :class="{ active: activeTab === 'fee' }" @click="activeTab = 'fee'">
          <Database :size="18" /> 费率设置
        </button>
      </div>

      <!-- 设置内容 -->
      <div class="settings-content">
        <!-- 通用设置 -->
        <div v-if="activeTab === 'general'" class="settings-panel">
          <h3>通用设置</h3>
          <div class="form-group">
            <label>平台名称</label>
            <input v-model="generalSettings.siteName" type="text" />
          </div>
          <div class="form-group">
            <label>平台描述</label>
            <textarea v-model="generalSettings.siteDescription" rows="3"></textarea>
          </div>
          <div class="form-group">
            <label>联系邮箱</label>
            <input v-model="generalSettings.contactEmail" type="email" />
          </div>
          <div class="form-group">
            <label>联系电话</label>
            <input v-model="generalSettings.contactPhone" type="text" />
          </div>
          <div class="form-group switch-group">
            <label>维护模式</label>
            <el-switch v-model="generalSettings.maintenanceMode" />
            <span class="hint">开启后用户将无法访问平台</span>
          </div>
          <div class="form-group switch-group">
            <label>开放注册</label>
            <el-switch v-model="generalSettings.registrationOpen" />
          </div>
        </div>

        <!-- 通知设置 -->
        <div v-if="activeTab === 'notification'" class="settings-panel">
          <h3>通知渠道</h3>
          <div class="form-group switch-group">
            <label>邮件通知</label>
            <el-switch v-model="notificationSettings.emailNotification" />
          </div>
          <div class="form-group switch-group">
            <label>短信通知</label>
            <el-switch v-model="notificationSettings.smsNotification" />
          </div>
          <div class="form-group switch-group">
            <label>推送通知</label>
            <el-switch v-model="notificationSettings.pushNotification" />
          </div>
          <h3 style="margin-top: 24px;">通知类型</h3>
          <div class="form-group switch-group">
            <label>订单通知</label>
            <el-switch v-model="notificationSettings.orderNotify" />
          </div>
          <div class="form-group switch-group">
            <label>退款通知</label>
            <el-switch v-model="notificationSettings.refundNotify" />
          </div>
          <div class="form-group switch-group">
            <label>投诉通知</label>
            <el-switch v-model="notificationSettings.complaintNotify" />
          </div>
          <div class="form-group switch-group">
            <label>系统通知</label>
            <el-switch v-model="notificationSettings.systemNotify" />
          </div>
        </div>

        <!-- 安全设置 -->
        <div v-if="activeTab === 'security'" class="settings-panel">
          <h3>密码策略</h3>
          <div class="form-group">
            <label>最小密码长度</label>
            <el-input-number v-model="securitySettings.passwordMinLength" :min="6" :max="20" />
          </div>
          <div class="form-group switch-group">
            <label>要求特殊字符</label>
            <el-switch v-model="securitySettings.passwordRequireSpecial" />
          </div>
          <h3 style="margin-top: 24px;">会话安全</h3>
          <div class="form-group">
            <label>会话超时(分钟)</label>
            <el-input-number v-model="securitySettings.sessionTimeout" :min="5" :max="120" />
          </div>
          <div class="form-group">
            <label>最大登录尝试次数</label>
            <el-input-number v-model="securitySettings.maxLoginAttempts" :min="3" :max="10" />
          </div>
          <div class="form-group switch-group">
            <label>双因素认证</label>
            <el-switch v-model="securitySettings.twoFactorAuth" />
          </div>
        </div>

        <!-- 费率设置 -->
        <div v-if="activeTab === 'fee'" class="settings-panel">
          <h3>平台费率</h3>
          <div class="form-group">
            <label>平台服务费率(%)</label>
            <el-input-number v-model="feeSettings.platformFeeRate" :min="0" :max="30" :precision="1" />
            <span class="hint">从每笔订单中收取的服务费比例</span>
          </div>
          <div class="form-group">
            <label>最低提现金额(元)</label>
            <el-input-number v-model="feeSettings.minWithdrawAmount" :min="10" :max="1000" />
          </div>
          <div class="form-group">
            <label>提现手续费率(%)</label>
            <el-input-number v-model="feeSettings.withdrawFeeRate" :min="0" :max="5" :precision="2" />
          </div>
          <div class="form-group">
            <label>结算周期(天)</label>
            <el-input-number v-model="feeSettings.settlementCycle" :min="1" :max="30" />
            <span class="hint">机构收入自动结算的周期</span>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<style lang="scss" scoped>
.system-settings { max-width: 1200px; margin: 0 auto; padding: 24px; }

.page-header {
  display: flex; justify-content: space-between; align-items: flex-start; margin-bottom: 24px;
  .header-left {
    h1 { font-size: 26px; font-weight: 700; margin: 0 0 6px; }
    p { color: #6B6560; margin: 0; }
  }
  .header-actions {
    display: flex; gap: 12px;
    button {
      display: flex; align-items: center; gap: 8px; padding: 10px 20px;
      border: none; border-radius: 10px; font-size: 14px; cursor: pointer;
      &.btn-reset { background: #F8F8F7; color: #6B6560; }
      &.btn-save { background: #722ed1; color: white; }
    }
  }
}

.settings-layout {
  display: grid; grid-template-columns: 220px 1fr; gap: 24px;
  @media (max-width: 768px) { grid-template-columns: 1fr; }
}

.settings-nav {
  display: flex; flex-direction: column; gap: 8px;
  @media (max-width: 768px) { flex-direction: row; flex-wrap: wrap; }
  button {
    display: flex; align-items: center; gap: 12px; padding: 14px 18px;
    background: white; border: none; border-radius: 12px; font-size: 14px;
    color: #6B6560; cursor: pointer; text-align: left;
    &.active { background: #722ed1; color: white; }
    &:hover:not(.active) { background: #F8F8F7; }
  }
}

.settings-content {
  background: white; border-radius: 16px; padding: 32px;
}

.settings-panel {
  h3 { font-size: 16px; font-weight: 600; margin: 0 0 20px; color: #2D2A26; }
  .form-group {
    margin-bottom: 20px;
    label { display: block; font-size: 14px; color: #6B6560; margin-bottom: 8px; }
    input, textarea {
      width: 100%; padding: 12px 16px; border: 1px solid #E8E6E3; border-radius: 10px;
      font-size: 14px; outline: none;
      &:focus { border-color: #722ed1; }
    }
    textarea { resize: vertical; }
    .hint { display: block; font-size: 12px; color: #9A958F; margin-top: 6px; }
    &.switch-group {
      display: flex; align-items: center; gap: 16px;
      label { margin-bottom: 0; min-width: 140px; }
      .hint { margin-top: 0; margin-left: auto; }
    }
  }
}
</style>
