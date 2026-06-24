<template>
  <div style="padding: 20px">
    <h2>AI 代码生成</h2>

    <el-row :gutter="20">
      <el-col :span="10">
        <el-card>
          <template #header>功能描述</template>
          <el-input
            v-model="desc"
            type="textarea"
            :rows="8"
            placeholder="描述你要生成的功能，例如：&#10;- 生成一个用户列表页面&#10;- 生成一个角色管理的增删改查接口&#10;- 生成一个文件上传功能"
          />
          <el-select v-model="selectedProvider" placeholder="选择模型" style="width: 100%; margin: 12px 0" clearable>
            <el-option
              v-for="(available, name) in models"
              :key="name"
              :label="`${name} (${available ? '可用' : '不可用'})`"
              :value="name"
              :disabled="!available"
            />
          </el-select>
          <el-button type="primary" @click="generate" :loading="loading" style="width: 100%">生成代码</el-button>
        </el-card>
      </el-col>
      <el-col :span="14">
        <el-card>
          <template #header>
            生成结果
            <el-button v-if="result" size="small" type="primary" @click="copyResult" style="float: right">复制</el-button>
          </template>
          <div v-loading="loading" class="result-area">
            <pre v-if="result">{{ result }}</pre>
            <el-empty v-else description="输入功能描述后点击生成" />
          </div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { getAvailableModels } from '@/api/ai'

const desc = ref('')
const result = ref('')
const loading = ref(false)
const selectedProvider = ref('')
const models = ref<Record<string, boolean>>({})

async function generate() {
  if (!desc.value.trim()) {
    ElMessage.warning('请输入功能描述')
    return
  }
  loading.value = true
  result.value = ''
  try {
    // 调用 AI 聊天接口，将描述作为系统提示词+用户消息
    const response = await fetch('/api/ai/chat/stream', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({
        message: `请根据以下描述生成完整的前端+后端代码，包括 Controller、Service、Entity、Mapper、Vue 页面。请输出完整可运行的代码：\n\n${desc.value}`,
        provider: selectedProvider.value || undefined,
        systemPrompt: '你是一个全栈开发工程师，擅长生成 Spring Boot + Vue 3 前后端代码。请输出完整的、可直接使用的代码。',
      }),
    })

    if (!response.ok) throw new Error(`HTTP ${response.status}`)

    const reader = response.body!.getReader()
    const decoder = new TextDecoder()

    while (true) {
      const { done, value } = await reader.read()
      if (done) break
      const chunk = decoder.decode(value, { stream: true })
      const lines = chunk.split('\n')
      for (const line of lines) {
        if (line.startsWith('data:')) {
          const data = line.slice(5).trim()
          if (data === '{"status":"complete"}') continue
          try {
            const parsed = JSON.parse(data)
            const text = parsed.content || parsed.text || parsed.delta?.text || data
            result.value += text
          } catch {
            result.value += data
          }
        }
      }
    }
  } catch (err: any) {
    ElMessage.error(err.message || '生成失败')
    result.value = `错误: ${err.message}`
  } finally {
    loading.value = false
  }
}

function copyResult() {
  navigator.clipboard.writeText(result.value).then(() => {
    ElMessage.success('已复制到剪贴板')
  })
}

onMounted(async () => {
  try {
    models.value = await getAvailableModels()
    const firstAvailable = Object.entries(models.value).find(([, v]) => v)
    if (firstAvailable) selectedProvider.value = firstAvailable[0]
  } catch {
    // ignore
  }
})
</script>

<style scoped>
.result-area {
  min-height: 300px;
  background: #1e1e1e;
  color: #d4d4d4;
  padding: 16px;
  border-radius: 4px;
  overflow-x: auto;
}
.result-area pre {
  margin: 0;
  white-space: pre-wrap;
  word-break: break-word;
  font-family: 'Courier New', monospace;
  font-size: 14px;
  line-height: 1.6;
}
</style>
