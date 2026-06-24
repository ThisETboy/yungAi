<template>
  <div>
    <h2>首页</h2>
    <el-row :gutter="20" style="margin-top: 20px">
      <el-col :span="6">
        <el-card shadow="hover">
          <template #header>用户数量</template>
          <div style="font-size: 24px; text-align: center; color: #409eff">{{ stats.users }}</div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover">
          <template #header>角色数量</template>
          <div style="font-size: 24px; text-align: center; color: #67c23a">{{ stats.roles }}</div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover">
          <template #header>AI 模型</template>
          <div style="font-size: 24px; text-align: center; color: #e6a23c">{{ stats.aiModels }}</div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover">
          <template #header>协议接入</template>
          <div style="font-size: 24px; text-align: center; color: #f56c6c">{{ stats.protocols }}</div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import request from '@/api/request'

const stats = ref({ users: 0, roles: 0, aiModels: 0, protocols: 0 })

onMounted(async () => {
  try {
    const [users, roles, aiModels, protocolStatus] = await Promise.all([
      request.get('/users', { params: { current: 1, size: 1 } }),
      request.get('/roles'),
      request.get('/ai/models'),
      request.get('/protocol/status'),
    ])
    stats.value.users = users?.total || 0
    stats.value.roles = Array.isArray(roles) ? roles.length : 0
    stats.value.aiModels = Object.keys(aiModels || {}).length
    // 统计在线协议数量
    stats.value.protocols = Object.values(protocolStatus || {}).filter(Boolean).length
  } catch {
    // ignore dashboard errors
  }
})
</script>
