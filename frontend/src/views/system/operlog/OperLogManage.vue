<template>
  <div style="padding: 20px">
    <h2>操作日志</h2>

    <!-- 搜索栏 -->
    <el-card style="margin-bottom: 20px">
      <el-form :inline="true" :model="queryParams">
        <el-form-item label="模块标题">
          <el-input v-model="queryParams.title" placeholder="请输入模块标题" clearable />
        </el-form-item>
        <el-form-item label="操作人">
          <el-input v-model="queryParams.operName" placeholder="请输入操作人" clearable />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">查询</el-button>
          <el-button type="danger" @click="handleClean">清空</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 日志列表 -->
    <el-card>
      <el-table :data="tableData" border stripe v-loading="loading" size="small">
        <el-table-column prop="title" label="模块" width="120" />
        <el-table-column prop="businessType" label="类型" width="80">
          <template #default="{ row }">
            <el-tag :type="bizTypeTag(row.businessType)" size="small">{{ bizTypeLabel(row.businessType) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="methodName" label="方法" min-width="180" show-overflow-tooltip />
        <el-table-column prop="operName" label="操作人" width="100" />
        <el-table-column prop="operUrl" label="请求URL" width="200" show-overflow-tooltip />
        <el-table-column prop="status" label="状态" width="70">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'danger'" size="small">
              {{ row.status === 1 ? '成功' : '失败' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="operTime" label="操作时间" width="170" />
        <el-table-column label="操作" width="120">
          <template #default="{ row }">
            <el-button link type="primary" size="small" @click="handleDetail(row)">详情</el-button>
            <el-button link type="danger" size="small" @click="handleDelete(row)">删除</el-button>
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

    <!-- 详情弹窗 -->
    <el-dialog v-model="detailVisible" title="日志详情" width="700px">
      <el-descriptions :column="2" border v-if="detailRow">
        <el-descriptions-item label="模块标题">{{ detailRow.title }}</el-descriptions-item>
        <el-descriptions-item label="业务类型">{{ bizTypeLabel(detailRow.businessType) }}</el-descriptions-item>
        <el-descriptions-item label="方法名称" :span="2">{{ detailRow.methodName }}</el-descriptions-item>
        <el-descriptions-item label="操作人">{{ detailRow.operName }}</el-descriptions-item>
        <el-descriptions-item label="操作IP">{{ detailRow.operIp }}</el-descriptions-item>
        <el-descriptions-item label="请求URL" :span="2">{{ detailRow.operUrl }}</el-descriptions-item>
        <el-descriptions-item label="请求参数" :span="2">
          <el-input v-model="detailRow.operParam" type="textarea" :rows="3" readonly />
        </el-descriptions-item>
        <el-descriptions-item label="返回结果" :span="2">
          <el-input v-model="detailRow.jsonResult" type="textarea" :rows="3" readonly />
        </el-descriptions-item>
        <el-descriptions-item label="状态">{{ detailRow.status === 1 ? '成功' : '失败' }}</el-descriptions-item>
        <el-descriptions-item label="操作时间">{{ detailRow.operTime }}</el-descriptions-item>
        <el-descriptions-item label="错误信息" :span="2" v-if="detailRow.errorMsg">
          <el-input v-model="detailRow.errorMsg" type="textarea" :rows="3" readonly />
        </el-descriptions-item>
      </el-descriptions>
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
  title: '',
  operName: '',
})

const detailVisible = ref(false)
const detailRow = ref<any>(null)

const bizTypeMap: Record<number, { label: string; type: string }> = {
  0: { label: '其他', type: 'info' },
  1: { label: '新增', type: 'success' },
  2: { label: '修改', type: 'warning' },
  3: { label: '删除', type: 'danger' },
  4: { label: '导入', type: 'success' },
  5: { label: '导出', type: 'warning' },
  6: { label: '授权', type: 'info' },
}

function bizTypeLabel(type: number): string {
  return bizTypeMap[type]?.label || '其他'
}

function bizTypeTag(type: number): string {
  return bizTypeMap[type]?.type || 'info'
}

async function fetchData() {
  loading.value = true
  try {
    const res: any = await request.get('/oper-log', {
      params: {
        current: queryParams.current,
        size: queryParams.size,
        title: queryParams.title || undefined,
        operName: queryParams.operName || undefined,
      },
    })
    tableData.value = res.records || []
    total.value = Number(res.total) || 0
  } catch { /* ignore */ } finally { loading.value = false }
}

function handleSearch() { queryParams.current = 1; fetchData() }

function handleReset() {
  queryParams.title = ''
  queryParams.operName = ''
  queryParams.current = 1
  fetchData()
}

async function handleClean() {
  try {
    await ElMessageBox.confirm('确定清空所有操作日志吗？', '提示', { type: 'warning' })
    await request.delete('/oper-log/clean')
    ElMessage.success('清空成功')
    fetchData()
  } catch { /* cancelled */ }
}

function handleDetail(row: any) {
  detailRow.value = row
  detailVisible.value = true
}

async function handleDelete(row: any) {
  try {
    await ElMessageBox.confirm(`确定删除该操作日志吗？`, '提示', { type: 'warning' })
    await request.delete(`/oper-log/${row.id}`)
    ElMessage.success('删除成功')
    fetchData()
  } catch { /* cancelled */ }
}

onMounted(() => { fetchData() })
</script>
