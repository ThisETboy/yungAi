<template>
  <div style="padding: 20px">
    <h2>AI 模型配置</h2>

    <!-- 搜索栏 -->
    <el-card style="margin-bottom: 20px">
      <el-form :inline="true" :model="queryParams">
        <el-form-item label="提供商">
          <el-select v-model="queryParams.provider" placeholder="全部" clearable style="width: 150px">
            <el-option label="ollama" value="ollama" />
            <el-option label="dashscope" value="dashscope" />
            <el-option label="anthropic" value="anthropic" />
            <el-option label="deepseek" value="deepseek" />
            <el-option label="agnes" value="agnes" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">查询</el-button>
          <el-button type="success" @click="handleAdd">新增配置</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 表格 -->
    <el-card>
      <el-table :data="tableData" border stripe v-loading="loading">
        <el-table-column prop="modelName" label="模型名称" min-width="180" />
        <el-table-column prop="provider" label="提供商" width="120" />
        <el-table-column prop="endpointModel" label="端点模型" width="150" />
        <el-table-column prop="enabled" label="状态" width="80">
          <template #default="{ row }">
            <el-tag :type="row.enabled === 1 ? 'success' : 'danger'" size="small">
              {{ row.enabled === 1 ? '启用' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="sortOrder" label="排序" width="80" />
        <el-table-column label="操作" width="180" fixed="right">
          <template #default="{ row }">
            <el-button size="small" type="primary" @click="handleEdit(row)">编辑</el-button>
            <el-button size="small" type="danger" @click="handleDelete(row)">删除</el-button>
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

    <!-- 新增/编辑弹窗 -->
    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="500px">
      <el-form :model="formData" ref="formRef" label-width="100px">
        <el-form-item label="模型名称">
          <el-input v-model="formData.modelName" placeholder="如 Qwen 2.5 7B (本地)" />
        </el-form-item>
        <el-form-item label="提供商">
          <el-select v-model="formData.provider" placeholder="请选择" style="width: 100%">
            <el-option label="ollama" value="ollama" />
            <el-option label="dashscope" value="dashscope" />
            <el-option label="anthropic" value="anthropic" />
            <el-option label="deepseek" value="deepseek" />
            <el-option label="agnes" value="agnes" />
          </el-select>
        </el-form-item>
        <el-form-item label="端点模型">
          <el-input v-model="formData.endpointModel" placeholder="API 调用时使用的模型标识" />
        </el-form-item>
        <el-form-item label="状态">
          <el-radio-group v-model="formData.enabled">
            <el-radio :value="1">启用</el-radio>
            <el-radio :value="0">禁用</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="排序">
          <el-input-number v-model="formData.sortOrder" :min="0" />
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
  provider: '',
})

const dialogVisible = ref(false)
const dialogTitle = ref('')
const isEdit = ref(false)
const editId = ref<number | null>(null)
const submitLoading = ref(false)
const formRef = ref()

const formData = reactive({
  id: null as number | null,
  modelName: '',
  provider: '',
  endpointModel: '',
  enabled: 1,
  sortOrder: 0,
})

async function fetchData() {
  loading.value = true
  try {
    const res: any = await request.get('/ai/model-configs', {
      params: {
        current: queryParams.current,
        size: queryParams.size,
        provider: queryParams.provider || undefined,
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

function handleReset() {
  queryParams.provider = ''
  queryParams.current = 1
  fetchData()
}

function handleAdd() {
  dialogTitle.value = '新增 AI 模型配置'
  isEdit.value = false
  editId.value = null
  Object.assign(formData, { id: null, modelName: '', provider: '', endpointModel: '', enabled: 1, sortOrder: 0 })
  dialogVisible.value = true
}

function handleEdit(row: any) {
  dialogTitle.value = '编辑 AI 模型配置'
  isEdit.value = true
  editId.value = row.id
  Object.assign(formData, {
    id: row.id,
    modelName: row.modelName,
    provider: row.provider,
    endpointModel: row.endpointModel,
    enabled: row.enabled,
    sortOrder: row.sortOrder,
  })
  dialogVisible.value = true
}

async function handleSubmit() {
  submitLoading.value = true
  try {
    const data = { ...formData }
    if (isEdit.value) {
      await request.put(`/ai/model-configs/${editId.value}`, data)
      ElMessage.success('更新成功')
    } else {
      await request.post('/ai/model-configs', data)
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

async function handleDelete(row: any) {
  try {
    await ElMessageBox.confirm(`确定删除模型配置 "${row.modelName}" 吗？`, '提示', { type: 'warning' })
    await request.delete(`/ai/model-configs/${row.id}`)
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
