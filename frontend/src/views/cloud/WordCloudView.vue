<template>
  <div style="padding: 20px">
    <!-- 标题和工具栏 -->
    <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 20px">
      <h2>词云中心</h2>
      <div>
        <el-button type="primary" @click="handleExtractKeywords">AI 提取关键词</el-button>
        <el-button @click="exportImage">导出图片</el-button>
      </div>
    </div>

    <!-- 筛选栏 -->
    <el-card style="margin-bottom: 20px">
      <el-form :inline="true">
        <el-form-item label="分类筛选">
          <el-radio-group v-model="selectedCategory" @change="handleCategoryChange">
            <el-radio-button label="">全部</el-radio-button>
            <el-radio-button
              v-for="cat in categories"
              :key="cat"
              :label="cat"
            >{{ cat }}</el-radio-button>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="时间范围">
          <el-date-picker
            v-model="dateRange"
            type="daterange"
            range-separator="至"
            start-placeholder="开始日期"
            end-placeholder="结束日期"
            :shortcuts="dateShortcuts"
            @change="handleTimeChange"
          />
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 词云图表 -->
    <el-card style="margin-bottom: 20px; height: 500px">
      <div ref="wordCloudChart" style="width: 100%; height: 100%"></div>
    </el-card>

    <!-- 热词管理表格 -->
    <el-card>
      <h3>热词管理</h3>
      <el-table :data="wordList" border stripe style="width: 100%" v-loading="loading">
        <el-table-column prop="word" label="词语" width="150" />
        <el-table-column prop="category" label="分类" width="100" />
        <el-table-column prop="popularity" label="热度" width="100">
          <template #default="{ row }">
            <el-progress :percentage="row.popularity" :color="getProgressColor(row.popularity)" />
          </template>
        </el-table-column>
        <el-table-column prop="source" label="来源" width="100">
          <template #default="{ row }">
            <el-tag :type="row.source === 1 ? 'warning' : 'info'">
              {{ row.source === 1 ? 'AI' : '手动' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="180" />
        <el-table-column label="操作" width="200">
          <template #default="{ row }">
            <el-button size="small" @click="handleEdit(row)">编辑</el-button>
            <el-button size="small" type="danger" @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 新增/编辑对话框 -->
    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="500px">
      <el-form :model="formData" label-width="80px">
        <el-form-item label="词语" required>
          <el-input v-model="formData.word" placeholder="请输入词语" />
        </el-form-item>
        <el-form-item label="分类" required>
          <el-select v-model="formData.category" placeholder="请选择分类">
            <el-option
              v-for="cat in categories"
              :key="cat"
              :label="cat"
              :value="cat"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="热度" required>
          <el-slider v-model="formData.popularity" :min="0" :max="100" show-input />
        </el-form-item>
        <el-form-item label="颜色">
          <el-color-picker v-model="formData.color" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSave">保存</el-button>
      </template>
    </el-dialog>

    <!-- AI 提取关键词对话框 -->
    <el-dialog v-model="aiDialogVisible" title="AI 提取关键词" width="600px">
      <el-input
        v-model="extractText"
        type="textarea"
        :rows="10"
        placeholder="请输入文本内容，AI 将自动提取关键词"
      />
      <div style="margin-top: 15px">
        <el-button type="primary" @click="handleAiExtract" :loading="extracting">提取关键词</el-button>
      </div>
      <div v-if="extractedKeywords.length > 0" style="margin-top: 20px">
        <h4>提取结果：</h4>
        <el-table :data="extractedKeywords" border stripe>
          <el-table-column prop="word" label="关键词" />
          <el-table-column prop="popularity" label="热度" width="100" />
          <el-table-column label="操作" width="100">
            <template #default="{ row }">
              <el-button size="small" type="primary" @click="addExtractedKeyword(row)">加入词云</el-button>
            </template>
          </el-table-column>
        </el-table>
      </div>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, onUnmounted, nextTick } from 'vue'
import * as echarts from 'echarts'
import 'echarts-wordcloud'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getWordCloud, getCategories, addWord, updateWord, deleteWord, extractKeywords } from '@/api/cloud'
import type { WordItem } from '@/types/cloud'

// ---- 状态定义 ----
const wordCloudChart = ref<HTMLElement | null>(null)
const chartInstance = ref<echarts.ECharts | null>(null)
const wordList = ref<WordItem[]>([])
const categories = ref<string[]>([])
const selectedCategory = ref('')
const dateRange = ref<[Date, Date] | null>(null)
const loading = ref(false)
const extracting = ref(false)

// 对话框状态
const dialogVisible = ref(false)
const aiDialogVisible = ref(false)
const dialogTitle = ref('新增热词')
const formData = ref({
  id: null as number | null,
  word: '',
  category: '',
  popularity: 50,
  color: '',
})
const extractText = ref('')
const extractedKeywords = ref<any[]>([])

// 日期快捷选项
const dateShortcuts = [
  {
    text: '今日',
    value: () => {
      const now = new Date()
      return [now, now]
    },
  },
  {
    text: '最近一周',
    value: () => {
      const end = new Date()
      const start = new Date()
      start.setTime(start.getTime() - 3600 * 1000 * 24 * 7)
      return [start, end]
    },
  },
  {
    text: '最近一月',
    value: () => {
      const end = new Date()
      const start = new Date()
      start.setTime(start.getTime() - 3600 * 1000 * 24 * 30)
      return [start, end]
    },
  },
]

// ---- 初始化 ----
onMounted(async () => {
  await initChart()
  await fetchData()
  window.addEventListener('resize', handleResize)
})

onUnmounted(() => {
  window.removeEventListener('resize', handleResize)
  if (chartInstance.value) {
    chartInstance.value.dispose()
  }
})

// ---- 数据获取 ----
async function fetchData() {
  loading.value = true
  try {
    const [words, cats] = await Promise.all([
      getWordCloud(selectedCategory.value),
      getCategories(),
    ])
    wordList.value = words || []
    categories.value = cats || []
    updateChart()
  } catch (error) {
    console.error('Failed to fetch data:', error)
  } finally {
    loading.value = false
  }
}

// ---- 图表初始化 ----
async function initChart() {
  await nextTick()
  // Wait for layout to settle
  await new Promise(resolve => setTimeout(resolve, 100))

  if (!wordCloudChart.value) {
    console.error('wordCloudChart element not found')
    return
  }

  const width = wordCloudChart.value.clientWidth
  const height = wordCloudChart.value.clientHeight
  console.log('Initializing chart, container size:', width, 'x', height)

  if (width === 0 || height === 0) {
    console.warn('Chart container has zero size, will retry in 500ms')
    setTimeout(() => initChart(), 500)
    return
  }

  try {
    chartInstance.value = echarts.init(wordCloudChart.value)

    chartInstance.value.setOption({
      tooltip: {
        show: true,
        formatter: (params: any) => {
          const data = params.data
          return `
            <strong>${data.name}</strong><br/>
            分类：${data.category}<br/>
            热度：${data.value}<br/>
            来源：${data.source === 1 ? 'AI 提取' : '手动添加'}
          `
        },
      },
      series: [
        {
          type: 'wordCloud',
          gridSize: 8,
          sizeRange: [12, 50],
          rotationRange: [-90, 90],
          shape: 'circle',
          drawOutOfBound: false,
          textStyle: {
            fontFamily: 'sans-serif',
            fontWeight: 'bold',
          },
          emphasis: {
            focus: 'self',
            textStyle: {
              shadowBlur: 10,
              shadowColor: '#333',
            },
          },
          data: [],
        },
      ],
    })

    console.log('Chart initialized successfully')

    chartInstance.value.on('click', (params: any) => {
      if (params.data) {
        selectedCategory.value = params.data.category
        fetchData()
      }
    })
  } catch (error) {
    console.error('Failed to initialize chart:', error)
  }
}

// ---- 更新图表 ----
function updateChart() {
  if (!chartInstance.value) return

  const data = wordList.value.map((item) => ({
    name: item.word,
    value: item.popularity,
    category: item.category,
    source: item.source,
    color: item.color || getRandomColor(item.category),
  }))

  chartInstance.value?.setOption({
    series: [
      {
        data,
      },
    ],
  })
}

// ---- 事件处理 ----
function handleCategoryChange() {
  fetchData()
}

function handleTimeChange() {
  // TODO: 实现时间范围筛选逻辑
  fetchData()
}

function handleResize() {
  chartInstance.value?.resize()
}

function getProgressColor(percentage: number): string {
  if (percentage >= 80) return '#67c23a'
  if (percentage >= 60) return '#e6a23c'
  return '#f56c6c'
}

function getRandomColor(category: string): string {
  const colors: Record<string, string[]> = {
    '科技': ['#FF6B6B', '#4ECDC4', '#45B7D1'],
    '娱乐': ['#FFEAA7', '#DDA0DD', '#96CEB4'],
    '体育': ['#FF6B6B', '#4ECDC4', '#45B7D1'],
    '财经': ['#FFEAA7', '#DDA0DD', '#98D8C8'],
  }
  const catColors = colors[category] || ['#FF6B6B', '#4ECDC4', '#45B7D1']
  return catColors[Math.floor(Math.random() * catColors.length)]
}

// ---- 热词管理 ----
function handleEdit(row: WordItem) {
  dialogTitle.value = '编辑热词'
  formData.value = {
    id: row.id ?? null,
    word: row.word,
    category: row.category,
    popularity: row.popularity,
    color: row.color || '',
  }
  dialogVisible.value = true
}

async function handleSave() {
  try {
    if (formData.value.id) {
      await updateWord(formData.value.id, formData.value)
      ElMessage.success('编辑成功')
    } else {
      await addWord(formData.value)
      ElMessage.success('新增成功')
    }
    dialogVisible.value = false
    await fetchData()
  } catch (error) {
    ElMessage.error('操作失败')
  }
}

async function handleDelete(row: WordItem) {
  try {
    await ElMessageBox.confirm(`确定要删除热词「${row.word}」吗？`, '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning',
    })
    await deleteWord(row.id!)
    ElMessage.success('删除成功')
    await fetchData()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('删除失败')
    }
  }
}

// ---- AI 关键词提取 ----
function handleExtractKeywords() {
  aiDialogVisible.value = true
  extractText.value = ''
  extractedKeywords.value = []
}

async function handleAiExtract() {
  if (!extractText.value.trim()) {
    ElMessage.warning('请输入文本内容')
    return
  }

  extracting.value = true
  try {
    const res = await extractKeywords(extractText.value, 20)
    extractedKeywords.value = res || []
    ElMessage.success('关键词提取成功')
  } catch (error) {
    ElMessage.error('关键词提取失败')
  } finally {
    extracting.value = false
  }
}

function addExtractedKeyword(item: any) {
  const newWord = {
    word: item.word,
    category: '科技', // TODO: 让用户选择分类
    popularity: item.popularity,
    source: 1,
  }
  addWord(newWord).then(() => {
    ElMessage.success('已添加到词云')
    fetchData()
  }).catch(() => {
    ElMessage.error('添加失败')
  })
}

// ---- 导出图片 ----
function exportImage() {
  if (!chartInstance.value) return
  const url = chartInstance.value.getDataURL({
    type: 'png',
    pixelRatio: 2,
    backgroundColor: '#fff',
  })
  const link = document.createElement('a')
  link.download = `词云_${new Date().getTime()}.png`
  link.href = url
  link.click()
}
</script>

<style scoped>
.el-card {
  margin-bottom: 20px;
}

h3 {
  margin-bottom: 15px;
  color: #303133;
}

.el-form-item {
  margin-bottom: 0;
}
</style>
