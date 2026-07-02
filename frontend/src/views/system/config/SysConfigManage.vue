<template>
  <div style="padding: 20px">
    <h2>系统配置</h2>

    <el-card style="margin-bottom: 20px">
      <el-form :inline="true" :model="queryParams">
        <el-form-item label="配置名称">
          <el-input v-model="queryParams.configName" placeholder="请输入配置名称" clearable />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">查询</el-button>
          <el-button type="success" @click="handleAdd">新增</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card>
      <el-table :data="tableData" border stripe v-loading="loading">
        <el-table-column prop="configName" label="配置名称" width="180" />
        <el-table-column prop="configKey" label="配置键" width="250" show-overflow-tooltip />
        <el-table-column prop="configValue" label="配置值" min-width="200" show-overflow-tooltip />
        <el-table-column prop="configType" label="类型" width="80">
          <template #default="{ row }">
            <el-tag :type="row.configType === 1 ? 'warning' : 'info'" size="small">
              {{ row.configType === 1 ? '系统' : '自定义' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="remark" label="备注" min-width="150" show-overflow-tooltip />
        <el-table-column label="操作" width="150" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" size="small" @click="handleEdit(row)">编辑</el-button>
            <el-button link type="danger" size="small" @click="handleDelete(row)" :disabled="row.configType === 1">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

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

    <!-- 编辑弹窗 -->
    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="600px">
      <el-form :model="formData" ref="formRef" label-width="120px">
        <el-form-item label="配置名称">
          <el-input v-model="formData.configName" />
        </el-form-item>
        <el-form-item label="配置键">
          <el-input v-model="formData.configKey" :disabled="isEdit" placeholder="如 sys.name" />
        </el-form-item>
        <el-form-item label="配置值">
          <el-input v-model="formData.configValue" type="textarea" :rows="3" />
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="formData.remark" />
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
import { ElMessage, ElMessageBox } from 'element-plus'
import request from '@/api/request'

const tableData = ref<any[]>([])
const loading = ref(false)
const total = ref(0)

const queryParams = reactive({
  current: 1,
  size: 10,
  configName: '',
})

const dialogVisible = ref(false)
const dialogTitle = ref('')
const isEdit = ref(false)
const editId = ref<number | null>(null)
const submitLoading = ref(false)
const formRef = ref()

const formData = reactive({
  id: null as number | null,
  configName: '',
  configKey: '',
  configValue: '',
  configType: 0,
  remark: '',
})

async function fetchData() {
  loading.value = true
  try {
    const res: any = await request.get('/config', {
      params: {
        current: queryParams.current,
        size: queryParams.size,
        configName: queryParams.configName || undefined,
      },
    })
    tableData.value = res.records || []
    total.value = Number(res.total) || 0
  } catch { /* ignore */ } finally { loading.value = false }
}

function handleSearch() { queryParams.current = 1; fetchData() }

function handleReset() {
  queryParams.configName = ''
  queryParams.current = 1
  fetchData()
}

function handleAdd() {
  dialogTitle.value = '新增配置'
  isEdit.value = false
  editId.value = null
  Object.assign(formData, { id: null, configName: '', configKey: '', configValue: '', configType: 0, remark: '' })
  dialogVisible.value = true
}

function handleEdit(row: any) {
  dialogTitle.value = '编辑配置'
  isEdit.value = true
  editId.value = row.id
  Object.assign(formData, {
    id: row.id, configName: row.configName, configKey: row.configKey,
    configValue: row.configValue, configType: row.configType, remark: row.remark || '',
  })
  dialogVisible.value = true
}

async function handleSubmit() {
  submitLoading.value = true
  try {
    const data = { ...formData }
    if (isEdit.value) {
      await request.put(`/config/${editId.value}`, data)
      ElMessage.success('更新成功')
    } else {
      await request.post('/config', data)
      ElMessage.success('创建成功')
    }
    dialogVisible.value = false
    fetchData()
  } catch { /* ignore */ } finally { submitLoading.value = false }
}

async function handleDelete(row: any) {
  try {
    await ElMessageBox.confirm(`确定删除配置 "${row.configName}" 吗？`, '提示', { type: 'warning' })
    await request.delete(`/config/${row.id}`)
    ElMessage.success('删除成功')
    fetchData()
  } catch { /* cancelled */ }
}

onMounted(() => { fetchData() })
</script>
