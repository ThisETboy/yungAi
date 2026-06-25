<template>
  <div style="padding: 20px">
    <h2>AI 聊天</h2>

    <el-row :gutter="20" style="margin-bottom: 16px">
      <el-col :span="12">
        <el-select v-model="selectedProvider" placeholder="选择模型" style="width: 200px; margin-right: 12px">
          <el-option
            v-for="(available, name) in models"
            :key="name"
            :label="`${name} (${available ? '可用' : '不可用'})`"
            :value="name"
            :disabled="!available"
          />
        </el-select>
      </el-col>
      <el-col :span="12" style="text-align: right">
        <el-button type="primary" @click="clearChat">清空对话</el-button>
      </el-col>
    </el-row>

    <el-card style="height: calc(100vh - 220px); display: flex; flex-direction: column">
      <!-- 消息列表 -->
      <div ref="messagesRef" class="messages" v-loading="streaming">
        <div v-if="messages.length === 0" class="empty">
          <el-empty description="开始一段对话吧" :image-size="80" />
        </div>
        <div v-for="(msg, idx) in messages" :key="idx" class="message-item" :class="msg.role">
          <div class="avatar">
            <el-icon v-if="msg.role === 'user'"><User /></el-icon>
            <el-icon v-else><MagicStick /></el-icon>
          </div>
          <div class="bubble">
            <div v-if="msg.role === 'user'" class="content">{{ msg.content }}</div>
            <div v-else class="content markdown-body" v-html="renderMarkdown(msg.content)" />
          </div>
        </div>
        <!-- 打字指示器 -->
        <div v-if="streaming" class="message-item assistant">
          <div class="avatar"><el-icon><Loading /></el-icon></div>
          <div class="bubble">
            <div class="content typing">思考中...</div>
          </div>
        </div>
      </div>

      <!-- 输入框 -->
      <div class="input-area">
        <el-input
          v-model="inputText"
          type="textarea"
          :rows="3"
          placeholder="输入消息... (Ctrl+Enter 发送)"
          @keydown.ctrl.enter="sendMessage"
        />
        <el-button type="primary" @click="sendMessage" :loading="streaming" style="margin-top: 8px; width: 100%">
          {{ streaming ? '发送中...' : '发送' }}
        </el-button>
      </div>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, nextTick, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { User, MagicStick, Loading } from '@element-plus/icons-vue'
import MarkdownIt from 'markdown-it'
import { getAvailableModels } from '@/api/ai'

const md = new MarkdownIt({ html: false, breaks: true })

const messages = ref<{ role: string; content: string }[]>([])
const inputText = ref('')
const streaming = ref(false)
const selectedProvider = ref('ollama')
const models = ref<Record<string, boolean>>({})
const messagesRef = ref<HTMLElement>()

// 渲染 Markdown
function renderMarkdown(text: string): string {
  return md.render(text)
}

// 滚动到底部
function scrollToBottom() {
  nextTick(() => {
    if (messagesRef.value) {
      messagesRef.value.scrollTop = messagesRef.value.scrollHeight
    }
  })
}

// 发送消息（SSE 流式）
async function sendMessage() {
  const text = inputText.value.trim()
  if (!text || streaming.value) return
  inputText.value = ''

  // 添加用户消息
  messages.value.push({ role: 'user', content: text })
  scrollToBottom()

  streaming.value = true

  try {
    // 从 localStorage 获取 JWT token
    const token = localStorage.getItem('token')
    const response = await fetch('/api/ai/chat/stream', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        'Authorization': token ? `Bearer ${token}` : '',
      },
      body: JSON.stringify({
        message: text,
        provider: selectedProvider.value,
      }),
    })

    if (!response.ok) {
      throw new Error(`HTTP ${response.status}`)
    }

    const reader = response.body!.getReader()
    const decoder = new TextDecoder()
    let assistantContent = ''

    // 先放一个空的 assistant 消息
    messages.value.push({ role: 'assistant', content: '' })

    while (true) {
      const { done, value } = await reader.read()
      if (done) break

      const chunk = decoder.decode(value, { stream: true })
      // SSE 格式: event: message\ndata: xxx
      const lines = chunk.split('\n')
      for (const line of lines) {
        if (line.startsWith('data:')) {
          const data = line.slice(5).trim()
          if (data === '{"status":"complete"}') continue
          try {
            const parsed = JSON.parse(data)
            // 不同提供商的 SSE 格式可能不同，尝试多种解析方式
            const text = parsed.content || parsed.text || parsed.delta?.text || data
            assistantContent += text
          } catch {
            assistantContent += data
          }
        }
      }

      // 实时更新最后一条 assistant 消息
      messages.value[messages.value.length - 1] = {
        role: 'assistant',
        content: assistantContent,
      }
      scrollToBottom()
    }
  } catch (err: any) {
    ElMessage.error(err.message || '请求失败')
    messages.value.push({ role: 'assistant', content: '抱歉，发生了错误。' })
  } finally {
    streaming.value = false
  }
}

function clearChat() {
  messages.value = []
}

// 加载可用模型
onMounted(async () => {
  try {
    models.value = await getAvailableModels()
    const firstAvailable = Object.entries(models.value).find(([, v]) => v)
    if (firstAvailable) {
      selectedProvider.value = firstAvailable[0]
    }
  } catch {
    // ignore
  }
})
</script>

<style scoped>
.messages {
  flex: 1;
  overflow-y: auto;
  padding: 16px;
}

.empty {
  display: flex;
  align-items: center;
  justify-content: center;
  height: 100%;
}

.message-item {
  display: flex;
  margin-bottom: 16px;
  align-items: flex-start;
}

.message-item.user {
  flex-direction: row-reverse;
}

.avatar {
  width: 36px;
  height: 36px;
  border-radius: 50%;
  background: #409eff;
  color: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.message-item.assistant .avatar {
  background: #67c23a;
}

.bubble {
  margin: 0 12px;
  max-width: 70%;
}

.content {
  padding: 10px 14px;
  border-radius: 8px;
  line-height: 1.6;
  word-break: break-word;
}

.message-item.user .content {
  background: #ecf5ff;
}

.message-item.assistant .content {
  background: #f5f7fa;
}

.typing {
  color: #909399;
  font-style: italic;
}

.input-area {
  padding: 16px;
  border-top: 1px solid #ebeef5;
}
</style>
