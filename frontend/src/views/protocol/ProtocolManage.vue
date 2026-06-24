<template>
  <div style="padding: 20px">
    <h2>协议管理</h2>

    <!-- 协议状态卡片 -->
    <el-row :gutter="20" style="margin-bottom: 20px">
      <el-col :span="12" v-for="(status, name) in protocolStatus" :key="name">
        <el-card shadow="hover">
          <template #header>
            <span>{{ name.toUpperCase() }} 协议</span>
            <el-tag :type="status ? 'success' : 'danger'" size="small" style="float: right">
              {{ status ? '已启动' : '未启动' }}
            </el-tag>
          </template>
          <div style="text-align: center">
            <el-button type="success" size="small" @click="startProtocol(name)" :disabled="status">
              启动
            </el-button>
            <el-button type="danger" size="small" @click="stopProtocol(name)" :disabled="!status">
              停止
            </el-button>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 发送数据 -->
    <el-card>
      <template #header>向设备发送数据</template>
      <el-form :inline="true" :model="sendForm">
        <el-form-item label="协议">
          <el-select v-model="sendForm.protocol">
            <el-option label="MQTT" value="mqtt" />
            <el-option label="TCP" value="tcp" />
          </el-select>
        </el-form-item>
        <el-form-item label="设备ID">
          <el-input v-model="sendForm.deviceId" placeholder="device-001" />
        </el-form-item>
        <el-form-item label="数据">
          <el-input v-model="sendForm.data" placeholder="Hello Device" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSend" :loading="sending">发送</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 批量操作 -->
    <el-card style="margin-top: 20px">
      <template #header">批量操作</template>
      <el-button type="success" @click="startAll" :loading="loading">启动所有协议</el-button>
      <el-button type="danger" @click="stopAll" :loading="loading">停止所有协议</el-button>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import request from '@/api/request'

const protocolStatus = ref<Record<string, boolean>>({})
const loading = ref(false)
const sending = ref(false)

const sendForm = ref({
  protocol: 'mqtt',
  deviceId: '',
  data: '',
})

async function fetchStatus() {
  try {
    protocolStatus.value = await request.get('/protocol/status')
  } catch {
    // ignore
  }
}

async function startAll() {
  loading.value = true
  try {
    await request.post('/protocol/start-all')
    ElMessage.success('所有协议已启动')
    fetchStatus()
  } catch {
    // handled by interceptor
  } finally {
    loading.value = false
  }
}

async function stopAll() {
  loading.value = true
  try {
    await request.post('/protocol/stop-all')
    ElMessage.success('所有协议已停止')
    fetchStatus()
  } catch {
    // handled by interceptor
  } finally {
    loading.value = false
  }
}

async function startProtocol(name: string) {
  loading.value = true
  try {
    await request.post('/protocol/start-all')
    ElMessage.success(`${name} 协议已启动`)
    fetchStatus()
  } catch {
    // handled by interceptor
  } finally {
    loading.value = false
  }
}

async function stopProtocol(name: string) {
  loading.value = true
  try {
    await request.post('/protocol/stop-all')
    ElMessage.success(`${name} 协议已停止`)
    fetchStatus()
  } catch {
    // handled by interceptor
  } finally {
    loading.value = false
  }
}

async function handleSend() {
  if (!sendForm.value.deviceId || !sendForm.value.data) {
    ElMessage.warning('请填写设备ID和数据')
    return
  }
  sending.value = true
  try {
    await request.post(`/protocol/send/${sendForm.value.protocol}/${sendForm.value.deviceId}`, sendForm.value.data)
    ElMessage.success('发送成功')
  } catch {
    // handled by interceptor
  } finally {
    sending.value = false
  }
}

onMounted(() => {
  fetchStatus()
})
</script>
