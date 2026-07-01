<template>
  <div style="padding: 20px">
    <h2>用户管理</h2>

    <!-- 搜索栏 -->
    <el-card style="margin-bottom: 20px">
      <el-form :inline="true" :model="queryParams">
        <el-form-item label="用户名">
          <el-input v-model="queryParams.username" placeholder="请输入用户名" clearable />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">查询</el-button>
          <el-button type="success" @click="handleAdd">新增</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 表格 -->
    <el-card>
      <el-table :data="tableData" border stripe v-loading="loading">
        <el-table-column prop="username" label="用户名" width="150" />
        <el-table-column prop="nickname" label="昵称" width="150" />
        <el-table-column prop="email" label="邮箱" width="200" />
        <el-table-column prop="phone" label="手机号" width="150" />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'danger'">
              {{ row.status === 1 ? '启用' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="roles" label="角色" min-width="150">
          <template #default="{ row }">
            <el-tag v-for="r in row.roles" :key="r" size="small" style="margin-right: 4px">{{ r }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="180" />
        <el-table-column label="操作" width="280" fixed="right">
          <template #default="{ row }">
            <el-button size="small" type="primary" @click="handleEdit(row)">编辑</el-button>
            <el-button size="small" type="warning" @click="handleAssignRole(row)">分配角色</el-button>
            <el-button size="small" type="danger" @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <el-pagination
        v-model:current-page="queryParams.current"
        v-model:page-size="queryParams.size"
        :total="total"
        :page-sizes="[10, 20, 50]"
        layout="total, sizes, prev, pager, next"
        style="margin-top: 16px; justify-content: flex-end"
        @size-change="fetchData"
        @current-change="fetchData"
      />
    </el-card>

    <!-- 新增/编辑弹窗 -->
    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="500px">
      <el-form :model="formData" :rules="formRules" ref="formRef" label-width="80px">
        <el-form-item label="用户名" prop="username">
          <el-input v-model="formData.username" :disabled="isEdit" />
        </el-form-item>
        <el-form-item label="密码" :prop="isEdit ? '' : 'password'">
          <el-input v-model="formData.password" type="password" :placeholder="isEdit ? '留空则不修改' : '请输入密码'" show-password />
        </el-form-item>
        <el-form-item label="昵称" prop="nickname">
          <el-input v-model="formData.nickname" />
        </el-form-item>
        <el-form-item label="邮箱" prop="email">
          <el-input v-model="formData.email" />
        </el-form-item>
        <el-form-item label="手机号" prop="phone">
          <el-input v-model="formData.phone" />
        </el-form-item>
        <el-form-item label="状态">
          <el-radio-group v-model="formData.status">
            <el-radio :value="1">启用</el-radio>
            <el-radio :value="0">禁用</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit" :loading="submitLoading">确定</el-button>
      </template>
    </el-dialog>

    <!-- 角色分配弹窗 -->
    <el-dialog v-model="roleDialogVisible" title="分配角色" width="450px">
      <el-transfer
        v-model="selectedRoleIds"
        :data="roleOptions"
        :titles="['可选角色', '已选角色']"
        :props="{ key: 'id', label: 'roleName' }"
        filter-placeholder="搜索角色"
        filter-position="left"
      />
      <template #footer>
        <el-button @click="roleDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitRoleAssignment" :loading="roleSubmitLoading">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox, type FormInstance, type FormRules } from 'element-plus'
import request from '@/api/request'

// ---- 角色选项 ----
const roleOptions = ref<{ id: number | string; roleName: string }[]>([])
const selectedRoleIds = ref<(number | string)[]>([])
const roleDialogVisible = ref(false)
const roleAssignUserId = ref<number | null>(null)
const roleSubmitLoading = ref(false)

// 加载角色列表
async function loadRoleOptions() {
  try {
    const res: any = await request.get('/roles')
    roleOptions.value = res.map((r: any) => ({ id: r.id, roleName: r.roleName }))
  } catch {
    // ignore
  }
}

// ---- 表格数据 ----
const tableData = ref<any[]>([])
const loading = ref(false)
const total = ref(0)
const queryParams = reactive({
  current: 1,
  size: 10,
  username: '',
})

// ---- 弹窗 ----
const dialogVisible = ref(false)
const dialogTitle = ref('')
const isEdit = ref(false)
const editId = ref<number | null>(null)
const submitLoading = ref(false)
const formRef = ref<FormInstance>()
const formData = reactive({
  username: '',
  password: '',
  nickname: '',
  email: '',
  phone: '',
  avatar: '',
  status: 1,
})

const formRules: FormRules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }],
}

// ---- 数据获取 ----
async function fetchData() {
  loading.value = true
  try {
    const res: any = await request.get('/users', {
      params: {
        current: queryParams.current,
        size: queryParams.size,
        username: queryParams.username || undefined,
      },
    })
    tableData.value = res.records || []
    total.value = Number(res.total) || 0
  } catch {
    // handled by interceptor
  } finally {
    loading.value = false
  }
}

function handleSearch() {
  queryParams.current = 1
  fetchData()
}

// ---- 新增 ----
function handleAdd() {
  dialogTitle.value = '新增用户'
  isEdit.value = false
  editId.value = null
  Object.assign(formData, { username: '', password: '', nickname: '', email: '', phone: '', avatar: '', status: 1 })
  dialogVisible.value = true
}

// ---- 编辑 ----
function handleEdit(row: any) {
  dialogTitle.value = '编辑用户'
  isEdit.value = true
  editId.value = row.id
  Object.assign(formData, {
    username: row.username,
    password: '',
    nickname: row.nickname,
    email: row.email,
    phone: row.phone,
    avatar: row.avatar,
    status: row.status,
  })
  dialogVisible.value = true
}

// ---- 提交 ----
async function handleSubmit() {
  if (!isEdit.value) {
    // 新增时需要校验密码
    const valid = await formRef.value?.validate().catch(() => false)
    if (!valid) return
  }
  submitLoading.value = true
  try {
    const data: Record<string, any> = { ...formData }
    if (isEdit.value && !data.password) {
      delete data.password
    }
    if (isEdit.value) {
      await request.put(`/users/${editId.value}`, data)
      ElMessage.success('更新成功')
    } else {
      await request.post('/users', data)
      ElMessage.success('创建成功')
    }
    dialogVisible.value = false
    fetchData()
  } catch {
    // handled by interceptor
  } finally {
    submitLoading.value = false
  }
}

// ---- 删除 ----
async function handleDelete(row: any) {
  try {
    await ElMessageBox.confirm(`确定删除用户 "${row.username}" 吗？`, '提示', { type: 'warning' })
    await request.delete(`/users/${row.id}`)
    ElMessage.success('删除成功')
    fetchData()
  } catch {
    // cancelled or failed
  }
}

// ---- 分配角色 ----
function handleAssignRole(row: any) {
  roleAssignUserId.value = row.id
  // 回显当前角色
  selectedRoleIds.value = row.roles?.map((r: any) => r.id || r) || []
  roleDialogVisible.value = true
  loadRoleOptions()
}

async function submitRoleAssignment() {
  if (!roleAssignUserId.value) return
  roleSubmitLoading.value = true
  try {
    await request.put(`/users/${roleAssignUserId.value}/roles`, selectedRoleIds.value)
    ElMessage.success('角色分配成功')
    roleDialogVisible.value = false
    fetchData()
  } catch {
    // handled by interceptor
  } finally {
    roleSubmitLoading.value = false
  }
}

onMounted(() => {
  fetchData()
})
</script>
