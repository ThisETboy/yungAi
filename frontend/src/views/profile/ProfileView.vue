<template>
  <div style="padding: 20px">
    <h2>个人中心</h2>

    <el-row :gutter="20">
      <!-- 基本信息 -->
      <el-col :span="12">
        <el-card>
          <template #header>
            <span style="font-weight:bold">基本信息</span>
          </template>
          <el-descriptions :column="1" border>
            <el-descriptions-item label="用户名">{{ userInfo.username }}</el-descriptions-item>
            <el-descriptions-item label="昵称">{{ userInfo.nickname || '-' }}</el-descriptions-item>
            <el-descriptions-item label="邮箱">{{ userInfo.email || '-' }}</el-descriptions-item>
            <el-descriptions-item label="手机号">{{ userInfo.phone || '-' }}</el-descriptions-item>
            <el-descriptions-item label="角色">
              <el-tag v-for="r in userInfo.roles" :key="r" size="small" style="margin-right:4px">{{ r }}</el-tag>
            </el-descriptions-item>
            <el-descriptions-item label="创建时间">{{ userInfo.createTime || '-' }}</el-descriptions-item>
          </el-descriptions>
          <el-button type="primary" style="margin-top:16px" @click="showEditDialog">编辑资料</el-button>
        </el-card>
      </el-col>

      <!-- 修改密码 -->
      <el-col :span="12">
        <el-card>
          <template #header>
            <span style="font-weight:bold">修改密码</span>
          </template>
          <el-form :model="pwdForm" label-width="100px" style="max-width:400px">
            <el-form-item label="原密码">
              <el-input v-model="pwdForm.oldPassword" type="password" show-password placeholder="请输入原密码" />
            </el-form-item>
            <el-form-item label="新密码">
              <el-input v-model="pwdForm.newPassword" type="password" show-password placeholder="请输入新密码" />
            </el-form-item>
            <el-form-item label="确认密码">
              <el-input v-model="pwdForm.confirmPassword" type="password" show-password placeholder="请再次输入新密码" />
            </el-form-item>
            <el-form-item>
              <el-button type="primary" @click="handleChangePassword" :loading="pwdLoading">修改密码</el-button>
            </el-form-item>
          </el-form>
        </el-card>
      </el-col>
    </el-row>

    <!-- 编辑资料弹窗 -->
    <el-dialog v-model="editDialogVisible" title="编辑资料" width="500px">
      <el-form :model="editForm" ref="editFormRef" label-width="80px">
        <el-form-item label="昵称">
          <el-input v-model="editForm.nickname" />
        </el-form-item>
        <el-form-item label="邮箱">
          <el-input v-model="editForm.email" />
        </el-form-item>
        <el-form-item label="手机号">
          <el-input v-model="editForm.phone" />
        </el-form-item>
        <el-form-item label="头像URL">
          <el-input v-model="editForm.avatar" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="editDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSaveInfo" :loading="saveLoading">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import request from '@/api/request'
import { useUserStore } from '@/store/user'

const userStore = useUserStore()
const userInfo = ref<any>({})
const pwdLoading = ref(false)
const saveLoading = ref(false)
const editDialogVisible = ref(false)
const editFormRef = ref()

const pwdForm = reactive({
  oldPassword: '',
  newPassword: '',
  confirmPassword: '',
})

const editForm = reactive({
  nickname: '',
  email: '',
  phone: '',
  avatar: '',
})

onMounted(async () => {
  try {
    const res: any = await request.get('/profile')
    userInfo.value = res
  } catch { /* ignore */ }
})

function showEditDialog() {
  editForm.nickname = userInfo.value.nickname || ''
  editForm.email = userInfo.value.email || ''
  editForm.phone = userInfo.value.phone || ''
  editForm.avatar = userInfo.value.avatar || ''
  editDialogVisible.value = true
}

async function handleSaveInfo() {
  saveLoading.value = true
  try {
    await request.put('/profile', editForm)
    ElMessage.success('资料更新成功')
    editDialogVisible.value = false
    // 刷新用户信息
    const res: any = await request.get('/profile')
    userInfo.value = res
    // 同步更新 store
    userStore.userInfo = { ...userInfo.value }
  } catch { /* ignore */ } finally { saveLoading.value = false }
}

async function handleChangePassword() {
  if (!pwdForm.oldPassword || !pwdForm.newPassword || !pwdForm.confirmPassword) {
    ElMessage.warning('请填写完整密码信息')
    return
  }
  if (pwdForm.newPassword !== pwdForm.confirmPassword) {
    ElMessage.warning('两次密码不一致')
    return
  }
  pwdLoading.value = true
  try {
    await request.put('/profile/password', null, {
      params: { oldPassword: pwdForm.oldPassword, newPassword: pwdForm.newPassword },
    })
    ElMessage.success('密码修改成功，请重新登录')
    pwdForm.oldPassword = ''
    pwdForm.newPassword = ''
    pwdForm.confirmPassword = ''
    // 退出登录
    await userStore.logout()
  } catch { /* ignore */ } finally { pwdLoading.value = false }
}
</script>
