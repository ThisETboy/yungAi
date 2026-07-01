<template>
  <div>
    <h2>首页</h2>

    <!-- 统计卡片 -->
    <el-row :gutter="20" style="margin-top: 20px">
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card">
          <template #header>
            <div style="display:flex;align-items:center;gap:8px">
              <el-icon :size="20" color="#409eff"><User /></el-icon>
              <span>用户总数</span>
            </div>
          </template>
          <div style="font-size: 32px; text-align: center; color: #409eff; font-weight: bold">{{ stats.users }}</div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card">
          <template #header>
            <div style="display:flex;align-items:center;gap:8px">
              <el-icon :size="20" color="#67c23a"><Avatar /></el-icon>
              <span>角色数量</span>
            </div>
          </template>
          <div style="font-size: 32px; text-align: center; color: #67c23a; font-weight: bold">{{ stats.roles }}</div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card">
          <template #header>
            <div style="display:flex;align-items:center;gap:8px">
              <el-icon :size="20" color="#e6a23c"><Monitor /></el-icon>
              <span>AI 模型</span>
            </div>
          </template>
          <div style="font-size: 32px; text-align: center; color: #e6a23c; font-weight: bold">{{ stats.aiModels }}</div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card">
          <template #header>
            <div style="display:flex;align-items:center;gap:8px">
              <el-icon :size="20" color="#f56c6c"><Connection /></el-icon>
              <span>协议接入</span>
            </div>
          </template>
          <div style="font-size: 32px; text-align: center; color: #f56c6c; font-weight: bold">{{ stats.protocols }}</div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 第二行：AI 服务状态 + 协议状态 -->
    <el-row :gutter="20" style="margin-top: 20px">
      <el-col :span="12">
        <el-card shadow="hover">
          <template #header>
            <span style="font-weight:bold">AI 模型状态</span>
          </template>
          <el-table :data="aiModelList" border size="small" style="width:100%">
            <el-table-column prop="name" label="模型" width="120" />
            <el-table-column label="状态" width="100">
              <template #default="{ row }">
                <el-tag :type="row.available ? 'success' : 'danger'" size="small">
                  {{ row.available ? '可用' : '不可用' }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="detail" label="详情" />
          </el-table>
        </el-card>
      </el-col>
      <el-col :span="12">
        <el-card shadow="hover">
          <template #header>
            <span style="font-weight:bold">协议状态</span>
          </template>
          <el-table :data="protocolList" border size="small" style="width:100%">
            <el-table-column prop="name" label="协议" width="120" />
            <el-table-column label="状态" width="100">
              <template #default="{ row }">
                <el-tag :type="row.active ? 'success' : 'info'" size="small">
                  {{ row.active ? '运行中' : '未启动' }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="detail" label="详情" />
          </el-table>
        </el-card>
      </el-col>
    </el-row>

    <!-- 第三行：最近操作日志 -->
    <el-row :gutter="20" style="margin-top: 20px">
      <el-col :span="24">
        <el-card shadow="hover">
          <template #header>
            <span style="font-weight:bold">最近操作日志</span>
            <el-button size="small" style="float:right" @click="refreshLogs">刷新</el-button>
          </template>
          <el-table :data="recentLogs" border stripe size="small" v-loading="logsLoading">
            <el-table-column prop="method" label="方法" width="80" />
            <el-table-column prop="url" label="接口路径" min-width="200" show-overflow-tooltip />
            <el-table-column prop="statusCode" label="状态码" width="80" />
            <el-table-column prop="durationMs" label="耗时(ms)" width="90" />
            <el-table-column prop="ipAddress" label="IP" width="140" />
            <el-table-column prop="operator" label="操作人" width="100" />
            <el-table-column prop="module" label="模块" width="80" />
            <el-table-column prop="createTime" label="时间" width="170" />
          </el-table>
          <el-pagination
            v-if="logsTotal > 0"
            style="margin-top: 12px; justify-content: flex-end"
            :current-page="logPage"
            :page-size="logPageSize"
            :total="logsTotal"
            :page-sizes="[10, 20, 50]"
            layout="total, sizes, prev, pager, next"
            @current-change="fetchLogs"
            @size-change="(size: any) => { logPageSize = size; fetchLogs(1) }"
          />
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import request from '@/api/request'
import { User, Avatar, Monitor, Connection } from '@element-plus/icons-vue'

// ---- 统计数据 ----
const stats = ref({ users: 0, roles: 0, aiModels: 0, protocols: 0 })

// ---- AI 模型状态 ----
const aiModelList = ref<{ name: string; available: boolean; detail: string }[]>([])

// ---- 协议状态 ----
const protocolList = ref<{ name: string; active: boolean; detail: string }[]>([])

// ---- 操作日志 ----
const recentLogs = ref<any[]>([])
const logsLoading = ref(false)
const logsTotal = ref(0)
const logPage = ref(1)
const logPageSize = ref(10)

// ---- 数据获取 ----
onMounted(async () => {
  await Promise.all([fetchStats(), fetchAiModels(), fetchProtocolStatus()])
  fetchLogs()
})

async function fetchStats() {
  try {
    const [users, roles, aiModels, protocolStatus]: any = await Promise.all([
      request.get('/users', { params: { current: 1, size: 1 } }),
      request.get('/roles'),
      request.get('/ai/models'),
      request.get('/protocol/status'),
    ])
    stats.value.users = users?.total || 0
    stats.value.roles = Array.isArray(roles) ? roles.length : 0
    stats.value.aiModels = Object.keys(aiModels || {}).length
    stats.value.protocols = Object.values(protocolStatus || {}).filter(Boolean).length
  } catch {
    // ignore
  }
}

async function fetchAiModels() {
  try {
    const models: Record<string, boolean> = await request.get('/ai/models')
    aiModelList.value = Object.entries(models).map(([name, available]) => ({
      name,
      available,
      detail: available ? '正常' : '离线',
    }))
  } catch {
    // ignore
  }
}

async function fetchProtocolStatus() {
  try {
    const status: Record<string, boolean> = await request.get('/protocol/status')
    protocolList.value = Object.entries(status).map(([name, active]) => ({
      name,
      active,
      detail: active ? '运行中' : '未启动',
    }))
  } catch {
    // ignore
  }
}

async function fetchLogs(page = logPage.value) {
  logsLoading.value = true
  try {
    const res: any = await request.get('/logs', {
      params: {
        current: page,
        size: logPageSize.value,
      },
    })
    recentLogs.value = res.records || []
    logsTotal.value = Number(res.total) || 0
    logPage.value = page
  } catch {
    recentLogs.value = []
    logsTotal.value = 0
  } finally {
    logsLoading.value = false
  }
}

function refreshLogs() {
  fetchLogs(logPage.value)
}
</script>

<style scoped>
.stat-card :deep(.el-card__body) {
  padding: 20px;
}
</style>
