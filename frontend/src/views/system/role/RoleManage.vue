<template>
  <div style="padding: 20px">
    <h2>角色管理</h2>

    <el-card style="margin-bottom: 20px">
      <el-form :inline="true" :model="queryParams">
        <el-form-item label="角色名称">
          <el-input v-model="queryParams.roleName" placeholder="请输入角色名称" clearable />
        </el-form-item>
        <el-form-item label="角色编码">
          <el-input v-model="queryParams.roleCode" placeholder="请输入角色编码" clearable />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">查询</el-button>
          <el-button @click="handleReset">重置</el-button>
          <el-button type="success" @click="handleAdd">新增角色</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card>
      <el-table :data="tableData" border stripe v-loading="loading">
        <el-table-column prop="roleCode" label="角色编码" width="150" />
        <el-table-column prop="roleName" label="角色名称" width="150" />
        <el-table-column prop="description" label="描述" min-width="200" />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'danger'">
              {{ row.status === 1 ? '启用' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="180" />
        <el-table-column label="操作" width="220" fixed="right">
          <template #default="{ row }">
            <el-button size="small" type="primary" @click="handleEdit(row)">编辑</el-button>
            <el-button size="small" type="danger" @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 新增/编辑弹窗 -->
    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="500px">
      <el-form :model="formData" :rules="formRules" ref="formRef" label-width="80px">
        <el-form-item label="角色编码" prop="roleCode">
          <el-input v-model="formData.roleCode" :disabled="isEdit" />
        </el-form-item>
        <el-form-item label="角色名称" prop="roleName">
          <el-input v-model="formData.roleName" />
        </el-form-item>
        <el-form-item label="描述">
          <el-input v-model="formData.description" type="textarea" :rows="3" />
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
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox, type FormInstance, type FormRules } from 'element-plus'
import request from '@/api/request'

const tableData = ref<any[]>([])
const loading = ref(false)

const queryParams = reactive({
  roleName: '',
  roleCode: '',
})

const dialogVisible = ref(false)
const dialogTitle = ref('')
const isEdit = ref(false)
const editId = ref<number | null>(null)
const submitLoading = ref(false)
const formRef = ref<FormInstance>()

const formData = reactive({
  id: null as number | null,
  roleCode: '',
  roleName: '',
  description: '',
  status: 1,
})

const formRules: FormRules = {
  roleCode: [{ required: true, message: '请输入角色编码', trigger: 'blur' }],
  roleName: [{ required: true, message: '请输入角色名称', trigger: 'blur' }],
}

async function fetchData() {
  loading.value = true
  try {
    const allRoles: any[] = await request.get('/roles')
    // 前端过滤
    tableData.value = allRoles.filter((r: any) => {
      const matchName = !queryParams.roleName || r.roleName?.includes(queryParams.roleName)
      const matchCode = !queryParams.roleCode || r.roleCode?.includes(queryParams.roleCode)
      return matchName && matchCode
    })
  } catch {
    // handled by interceptor
  } finally {
    loading.value = false
  }
}

function handleSearch() {
  fetchData()
}

function handleReset() {
  queryParams.roleName = ''
  queryParams.roleCode = ''
  fetchData()
}

function handleAdd() {
  dialogTitle.value = '新增角色'
  isEdit.value = false
  editId.value = null
  Object.assign(formData, { roleCode: '', roleName: '', description: '', status: 1 })
  dialogVisible.value = true
}

function handleEdit(row: any) {
  dialogTitle.value = '编辑角色'
  isEdit.value = true
  editId.value = row.id
  Object.assign(formData, {
    roleCode: row.roleCode,
    roleName: row.roleName,
    description: row.description,
    status: row.status,
  })
  dialogVisible.value = true
}

async function handleSubmit() {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return
  submitLoading.value = true
  try {
    const data = { ...formData }
    if (isEdit.value) {
      data.id = editId.value
    }
    await request.post('/roles', data)
    ElMessage.success(isEdit.value ? '更新成功' : '创建成功')
    dialogVisible.value = false
    fetchData()
  } catch {
    // handled by interceptor
  } finally {
    submitLoading.value = false
  }
}

async function handleDelete(row: any) {
  try {
    await ElMessageBox.confirm(`确定删除角色 "${row.roleName}" 吗？`, '提示', { type: 'warning' })
    await request.delete(`/roles/${row.id}`)
    ElMessage.success('删除成功')
    fetchData()
  } catch {
    // cancelled
  }
}

onMounted(() => {
  fetchData()
})
</script>
