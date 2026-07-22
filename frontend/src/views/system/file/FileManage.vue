<template>
  <div style="padding: 20px">
    <h2>文件管理</h2>

    <el-card style="margin-bottom: 20px">
      <el-upload
        action=""
        :auto-upload="false"
        :on-change="handleUpload"
        :limit="5"
        accept="image/*,.pdf,.doc,.docx,.xls,.xlsx,.zip,.rar"
        drag
      >
        <el-icon class="el-icon--upload"><UploadFilled /></el-icon>
        <div>拖拽文件到此处或 <strong>点击上传</strong></div>
        <template #tip>
          <div style="color:#909399;font-size:12px;margin-top:4px">
            支持图片、PDF、Word、Excel、压缩包，单文件最大 10MB
          </div>
        </template>
      </el-upload>
    </el-card>

    <el-card>
      <el-table :data="fileList" border stripe v-loading="loading">
        <el-table-column prop="fileName" label="文件名" min-width="200" show-overflow-tooltip />
        <el-table-column prop="fileType" label="类型" width="120" />
        <el-table-column prop="fileSize" label="大小" width="100">
          <template #default="{ row }">
            {{ formatSize(row.fileSize) }}
          </template>
        </el-table-column>
        <el-table-column prop="uploadBy" label="上传人" width="100" />
        <el-table-column prop="createTime" label="上传时间" width="170" />
        <el-table-column label="操作" width="150">
          <template #default="{ row }">
            <el-button link type="primary" size="small" @click="handleDownload(row)">下载</el-button>
            <el-button link type="primary" size="small" @click="handlePreview(row)">预览</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 预览弹窗 -->
    <el-dialog v-model="previewVisible" :title="previewFileName" width="80%">
      <img v-if="isImage" :src="previewUrl" style="max-width:100%" />
      <iframe v-else :src="previewUrl" style="width:100%;height:600px;border:none" />
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { ElMessage } from 'element-plus'
import { UploadFilled } from '@element-plus/icons-vue'
import { getFileList } from '@/api/file'

const fileList = ref<any[]>([])
const loading = ref(false)
const previewVisible = ref(false)
const previewUrl = ref('')
const previewFileName = ref('')

const isImage = computed(() => {
  return /\.(jpg|jpeg|png|gif|webp|bmp|svg)$/i.test(previewUrl.value)
})

function formatSize(bytes: number): string {
  if (!bytes) return '0 B'
  const units = ['B', 'KB', 'MB', 'GB']
  let i = 0
  let size = bytes
  while (size >= 1024 && i < units.length - 1) {
    size /= 1024
    i++
  }
  return size.toFixed(1) + ' ' + units[i]
}

async function loadFileList() {
  loading.value = true
  try {
    const res = await getFileList({ current: 1, size: 10 })
    fileList.value = res.records || []
  } catch (err) {
    ElMessage.error('加载文件列表失败')
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  loadFileList()
})

async function handleUpload(file: any) {
  loading.value = true
  try {
    const formData = new FormData()
    formData.append('file', file.raw)
    const token = localStorage.getItem('token')
    const res = await fetch('/api/files/upload', {
      method: 'POST',
      headers: { 'Authorization': token ? `Bearer ${token}` : '' },
      body: formData,
    })
    if (!res.ok) {
      const err = await res.json()
      throw new Error(err.message || '上传失败')
    }
    await res.json()
    ElMessage.success('上传成功')
    loadFileList()
  } catch (err: any) {
    ElMessage.error(err.message || '上传失败')
  } finally {
    loading.value = false
  }
}

function handleDownload(row: any) {
  window.open(row.fileUrl, '_blank')
}

function handlePreview(row: any) {
  previewUrl.value = row.fileUrl
  previewFileName.value = row.fileName
  previewVisible.value = true
}
</script>
