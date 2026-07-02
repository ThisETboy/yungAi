<template>
  <div style="padding: 20px">
    <h2>缓存管理</h2>

    <el-card style="margin-bottom: 20px">
      <el-form :inline="true">
        <el-form-item label="Key 匹配">
          <el-input v-model="keyPattern" placeholder="如 sys:config:*" clearable style="width:200px" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="fetchKeys">查询</el-button>
          <el-button type="danger" @click="handleClear">清空全部</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card>
      <el-table :data="keyList" border stripe v-loading="loading" size="small">
        <el-table-column prop="key" label="Key" min-width="250" show-overflow-tooltip />
        <el-table-column label="操作" width="200">
          <template #default="{ row }">
            <el-button link type="primary" size="small" @click="handleGetValue(row)">查看</el-button>
            <el-button link type="danger" size="small" @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 查看值弹窗 -->
    <el-dialog v-model="valueVisible" title="缓存值" width="600px">
      <el-input v-model="cachedValue" type="textarea" :rows="10" readonly />
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import request from '@/api/request'

const keyList = ref<{ key: string }[]>([])
const loading = ref(false)
const keyPattern = ref('')
const valueVisible = ref(false)
const cachedValue = ref('')

async function fetchKeys() {
  loading.value = true
  try {
    const keys: string[] = await request.get('/cache/keys', {
      params: { pattern: keyPattern.value || undefined },
    })
    keyList.value = keys.map(k => ({ key: k }))
  } catch { /* ignore */ } finally { loading.value = false }
}

async function handleGetValue(row: any) {
  try {
    const val: any = await request.get(`/cache/value/${encodeURIComponent(row.key)}`)
    cachedValue.value = typeof val === 'object' ? JSON.stringify(val, null, 2) : String(val)
    valueVisible.value = true
  } catch { /* ignore */ }
}

async function handleDelete(row: any) {
  try {
    await ElMessageBox.confirm(`确定删除缓存 Key "${row.key}" 吗？`, '提示', { type: 'warning' })
    await request.delete(`/cache/keys/${encodeURIComponent(row.key)}`)
    ElMessage.success('删除成功')
    fetchKeys()
  } catch { /* cancelled */ }
}

async function handleClear() {
  try {
    await ElMessageBox.confirm('确定清空所有缓存吗？此操作不可恢复！', '警告', { type: 'warning' })
    await request.delete('/cache/clear')
    ElMessage.success('清空成功')
    fetchKeys()
  } catch { /* cancelled */ }
}

onMounted(() => { fetchKeys() })
</script>
