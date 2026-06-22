<template>
  <div style="padding: 20px">
    <h2>AI 代码生成</h2>
    <el-alert title="代码生成界面 - 待实现" type="info" :closable="false" style="margin-bottom: 20px" />
    <el-input v-model="desc" type="textarea" :rows="4" placeholder="描述你要生成的功能..." />
    <el-button type="primary" style="margin-top: 10px" @click="generate">生成代码</el-button>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
const desc = ref('')
function generate() {
  console.log('Generate:', desc.value)
}
</script>
