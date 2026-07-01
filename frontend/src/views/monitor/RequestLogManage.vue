<template>
  <div style="padding: 20px">
    <h2>请求日志</h2>

    <!-- 搜索栏 -->
    <el-card style="margin-bottom: 20px">
      <el-form :inline="true" :model="queryParams">
        <el-form-item label="接口路径">
          <el-input v-model="queryParams.url" placeholder="请输入接口路径" clearable />
        </el-form-item>
        <el-form-item label="模块">
          <el-select v-model="queryParams.module" placeholder="全部" clearable style="width: 120px">
            <el-option label="system" value="system" />
            <el-option label="ai" value="ai" />
            <el-option label="protocol" value="protocol" />
            <el-option label="monitor" value="monitor" />
          </el-select>
        </el-form-item>
        <el-form-item label="操作人">
          <el-input v-model="queryParams.operator" placeholder="请输入操作人" clearable style="width: 150px" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">查询</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 日志列表 -->
    <el-card>
      <el-table :data="tableData" border stripe v-loading="loading" size="small">
        <el-table-column prop="method" label="方法" width="70" />
        <el-table-column prop="url" label="接口路径" min-width="220" show-overflow-tooltip />
        <el-table-column prop="statusCode" label="状态" width="70">
          <template #default="{ row }">
            <el-tag :type="row.statusCode < 400 ? 'success' : 'danger'" size="small">{{ row.statusCode }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="durationMs" label="耗时(ms)" width="90" />
        <el-table-column prop="ipAddress" label="IP" width="140" />
        <el-table-column prop="operator" label="操作人" width="100" />
        <el-table-column prop="module" label="模块" width="80" />
        <el-table-column prop="isError" label="异常" width="70">
          <template #default="{ row }">
            <el-tag :type="row.isError === 1 ? 'danger' : 'info'" size="small">
              {{ row.isError === 1 ? '是' : '否' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="时间" width="170" />
        <el-table-column label="操作" width="80">
          <template #default="{ row }">
            <el-button link type="primary" size="small" @click="handleDetail(row)">详情</el-button>
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

    <!-- 详情弹窗 -->
    <el-dialog v-model="detailVisible" title="日志详情" width="700px">
      <el-descriptions :column="2" border v-if="detailRow">
        <el-descriptions-item label="请求方法">{{ detailRow.method }}</el-descriptions-item>
        <el-descriptions-item label="状态码">{{ detailRow.statusCode }}</el-descriptions-item>
        <el-descriptions-item label="接口路径" :span="2">{{ detailRow.url }}</el-descriptions-item>
        <el-descriptions-item label="请求参数" :span="2">
          <el-input v-model="detailRow.params" type="textarea" :rows="4" readonly />
        </el-descriptions-item>
        <el-descriptions-item label="IP 地址">{{ detailRow.ipAddress }}</el-descriptions-item>
        <el-descriptions-item label="操作人">{{ detailRow.operator }}</el-descriptions-item>
        <el-descriptions-item label="User Agent" :span="2">
          <el-input v-model="detailRow.userAgent" type="textarea" :rows="2" readonly />
        </el-descriptions-item>
        <el-descriptions-item label="模块">{{ detailRow.module }}</el-descriptions-item>
        <el-descriptions-item label="耗时">{{ detailRow.durationMs }}ms</el-descriptions-item>
        <el-descriptions-item label="创建时间">{{ detailRow.createTime }}</el-descriptions-item>
        <el-descriptions-item label="更新时间">{{ detailRow.updateTime }}</el-descriptions-item>
        <el-descriptions-item label="是否异常">{{ detailRow.isError === 1 ? '是' : '否' }}</el-descriptions-item>
        <el-descriptions-item label="异常信息" :span="2" v-if="detailRow.errorMsg">
          <el-input v-model="detailRow.errorMsg" type="textarea" :rows="3" readonly />
        </el-descriptions-item>
      </el-descriptions>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import request from '@/api/request'

const tableData = ref<any[]>([])
const loading = ref(false)
const total = ref(0)

const queryParams = reactive({
  current: 1,
  size: 10,
  url: '',
  module: '',
  operator: '',
})

const detailVisible = ref(false)
const detailRow = ref<any>(null)

async function fetchData() {
  loading.value = true
  try {
    const res: any = await request.get('/logs', {
      params: {
        current: queryParams.current,
        size: queryParams.size,
        url: queryParams.url || undefined,
        module: queryParams.module || undefined,
        operator: queryParams.operator || undefined,
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
  queryParams.url = ''
  queryParams.module = ''
  queryParams.operator = ''
  queryParams.current = 1
  fetchData()
}

function handleDetail(row: any) {
  detailRow.value = row
  detailVisible.value = true
}

onMounted(() => {
  fetchData()
})
</script>
