<template>
  <div style="padding: 20px">
    <h2>AI 聊天</h2>
    <el-alert title="AI 聊天界面 - 待实现 SSE 流式响应" type="info" :closable="false" style="margin-bottom: 20px" />
    <div class="chat-box">
      <div class="messages">暂无消息</div>
      <el-input v-model="input" placeholder="输入消息..." style="margin-top: 10px" />
      <el-button type="primary" @click="sendMessage" style="margin-top: 10px">发送</el-button>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { sendChatSSE } from '@/api/ai'

const input = ref('')

async function sendMessage() {
  if (!input.value.trim()) return
  console.log('Sending:', input.value)
  // TODO: Implement SSE streaming
  input.value = ''
}
</script>

<style scoped>
.chat-box {
  border: 1px solid #ddd;
  border-radius: 4px;
  padding: 20px;
  min-height: 400px;
}
.messages {
  min-height: 300px;
  white-space: pre-wrap;
}
</style>
