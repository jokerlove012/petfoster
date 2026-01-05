<template>
  <div class="accounts-page">
    <div class="page-header">
      <el-button :icon="ArrowLeft" text @click="goBack" />
      <h1>提现账户</h1>
    </div>
    
    <div class="accounts-list" v-loading="walletStore.loading">
      <template v-if="walletStore.withdrawalAccounts.length">
        <WithdrawalAccountCard
          v-for="account in walletStore.withdrawalAccounts"
          :key="account.id"
          :account="account"
          :show-actions="true"
          @set-default="handleSetDefault(account.id)"
          @delete="handleDelete(account.id)"
        />
      </template>
      <el-empty v-else description="暂无提现账户" />
    </div>
    
    <div class="add-buttons">
      <el-button type="primary" :icon="Plus" @click="showAddDialog('bank_card')">
        添加银行卡
      </el-button>
      <el-button :icon="Plus" @click="showAddDialog('alipay')">
        添加支付宝
      </el-button>
    </div>
    
    <!-- 添加账户弹窗 -->
    <el-dialog v-model="addDialogVisible" :title="addDialogTitle" width="90%" :max-width="500">
      <el-form :model="accountForm" :rules="formRules" ref="formRef" label-position="top">
        <el-form-item label="账户名称（持卡人姓名）" prop="accountName">
          <el-input v-model="accountForm.accountName" placeholder="请输入真实姓名" />
        </el-form-item>
        <el-form-item v-if="accountType === 'bank_card'" label="银行名称" prop="bankName">
          <el-input v-model="accountForm.bankName" placeholder="请输入银行名称" />
        </el-form-item>
        <el-form-item :label="accountType === 'bank_card' ? '银行卡号' : '支付宝账号'" prop="accountNumber">
          <el-input v-model="accountForm.accountNumber" :placeholder="accountType === 'bank_card' ? '请输入银行卡号' : '请输入支付宝账号'" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="addDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="handleAddAccount">确认添加</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ArrowLeft, Plus } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import type { FormInstance, FormRules } from 'element-plus'
import { useWalletStore } from '@/stores/wallet'
import WithdrawalAccountCard from '@/components/wallet/WithdrawalAccountCard.vue'
import type { WithdrawalAccountType } from '@/types/wallet'

const router = useRouter()
const walletStore = useWalletStore()

const addDialogVisible = ref(false)
const accountType = ref<WithdrawalAccountType>('bank_card')
const submitting = ref(false)
const formRef = ref<FormInstance>()

const accountForm = ref({
  accountName: '',
  accountNumber: '',
  bankName: ''
})

const addDialogTitle = computed(() => 
  accountType.value === 'bank_card' ? '添加银行卡' : '添加支付宝账户'
)

const formRules: FormRules = {
  accountName: [{ required: true, message: '请输入账户名称', trigger: 'blur' }],
  accountNumber: [{ required: true, message: '请输入账号', trigger: 'blur' }],
  bankName: [{ required: true, message: '请输入银行名称', trigger: 'blur' }]
}

onMounted(() => {
  walletStore.fetchWithdrawalAccounts()
})

function goBack() {
  router.back()
}

function showAddDialog(type: WithdrawalAccountType) {
  accountType.value = type
  accountForm.value = { accountName: '', accountNumber: '', bankName: '' }
  addDialogVisible.value = true
}

async function handleAddAccount() {
  if (!formRef.value) return
  
  await formRef.value.validate(async (valid) => {
    if (!valid) return
    
    submitting.value = true
    try {
      const result = await walletStore.addWithdrawalAccount({
        type: accountType.value,
        accountName: accountForm.value.accountName,
        accountNumber: accountForm.value.accountNumber,
        bankName: accountType.value === 'bank_card' ? accountForm.value.bankName : undefined
      })
      
      if (result) {
        ElMessage.success('添加成功')
        addDialogVisible.value = false
      } else {
        ElMessage.error('添加失败')
      }
    } finally {
      submitting.value = false
    }
  })
}

async function handleSetDefault(id: string) {
  const success = await walletStore.setDefaultAccount(id)
  if (success) {
    ElMessage.success('设置成功')
  }
}

async function handleDelete(id: string) {
  try {
    await ElMessageBox.confirm('确定要删除该账户吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    const success = await walletStore.deleteWithdrawalAccount(id)
    if (success) {
      ElMessage.success('删除成功')
    }
  } catch {
    // 用户取消
  }
}
</script>

<style scoped lang="scss">
.accounts-page {
  padding: 20px;
  max-width: 600px;
  margin: 0 auto;
}

.page-header {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 20px;
  
  h1 {
    font-size: 20px;
    font-weight: 600;
  }
}

.accounts-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
  min-height: 200px;
}

.add-buttons {
  display: flex;
  gap: 12px;
  margin-top: 24px;
  
  .el-button {
    flex: 1;
  }
}
</style>
