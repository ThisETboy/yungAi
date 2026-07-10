<template>
  <div style="padding: 20px">
    <h2>登录日志</h2>

    <el-card style="margin-bottom: 20px">
      <el-form :inline="true" :model="queryParams">
        <el-form-item label="用户名">
          <el-input v-model="queryParams.username" placeholder="请输入用户名" clearable />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="queryParams.status" placeholder="全部" clearable style="width: 120px">
            <el-option label="成功" :value="1" />
            <el-option label="失败" :value="0" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">查询</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card>
      <el-table :data="tableData" border stripe v-loading="loading" size="small">
        <el-table-column prop="username" label="用户名" width="120" />
        <el-table-column prop="ipAddress" label="IP地址" width="140" />
        <el-table-column prop="status" label="状态" width="80">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'danger'" size="small">
              {{ row.status === 1 ? '成功' : '失败' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="message" label="提示信息" min-width="200" show-overflow-tooltip />
        <el-table-column prop="loginTime" label="登录时间" width="170" />
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
  username: '',
  status: null as number | null,
})

async function fetchData() {
  loading.value = true
  try {
    const res: any = await request.get('/auth/login-logs', {
      params: {
        current: queryParams.current,
        size: queryParams.size,
        username: queryParams.username || undefined,
        status: queryParams.status !== null ? queryParams.status : undefined,
      },
    })
    tableData.value = res.records || []
    total.value = Number(res.total) || 0
  } catch { /* ignore */ } finally { loading.value = false }
}

function handleSearch() { queryParams.current = 1; fetchData() }

function handleReset() {
  queryParams.username = ''
  queryParams.status = null
  queryParams.current = 1
  fetchData()
}

onMounted(() => { fetchData() })
</script>
