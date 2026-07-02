<template>
  <div style="padding: 20px">
    <h2>字典管理</h2>

    <el-tabs v-model="activeTab">
      <!-- 字典类型 -->
      <el-tab-pane label="字典类型" name="type">
        <el-card style="margin-bottom: 20px">
          <el-form :inline="true" :model="typeQuery">
            <el-form-item label="字典名称">
              <el-input v-model="typeQuery.dictName" placeholder="请输入字典名称" clearable />
            </el-form-item>
            <el-form-item>
              <el-button type="primary" @click="handleTypeSearch">查询</el-button>
              <el-button type="success" @click="handleTypeAdd">新增</el-button>
            </el-form-item>
          </el-form>
        </el-card>

        <el-card>
          <el-table :data="typeList" border stripe v-loading="typeLoading">
            <el-table-column prop="dictName" label="字典名称" width="180" />
            <el-table-column prop="dictType" label="字典类型" width="200" />
            <el-table-column prop="status" label="状态" width="80">
              <template #default="{ row }">
                <el-tag :type="row.status === 1 ? 'success' : 'danger'" size="small">
                  {{ row.status === 1 ? '启用' : '禁用' }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="remark" label="备注" min-width="200" show-overflow-tooltip />
            <el-table-column prop="createTime" label="创建时间" width="170" />
            <el-table-column label="操作" width="200" fixed="right">
              <template #default="{ row }">
                <el-button link type="primary" size="small" @click="handleDictData(row)">字典数据</el-button>
                <el-button link type="primary" size="small" @click="handleTypeEdit(row)">编辑</el-button>
                <el-button link type="danger" size="small" @click="handleTypeDelete(row)">删除</el-button>
              </template>
            </el-table-column>
          </el-table>
          <el-pagination
            v-model:current-page="typeQuery.current"
            v-model:page-size="typeQuery.size"
            :total="typeTotal"
            :page-sizes="[10, 20, 50]"
            layout="total, sizes, prev, pager, next"
            style="margin-top: 16px; justify-content: flex-end"
            @size-change="fetchTypes"
            @current-change="fetchTypes"
          />
        </el-card>
      </el-tab-pane>

      <!-- 字典数据 -->
      <el-tab-pane label="字典数据" name="data">
        <el-card style="margin-bottom: 20px" v-if="currentDictType">
          <el-form :inline="true">
            <el-form-item label="字典类型">
              <el-select v-model="currentDictType.id" placeholder="请选择字典类型" style="width: 200px">
                <el-option v-for="t in typeList" :key="t.id" :label="t.dictName" :value="t.id" />
              </el-select>
            </el-form-item>
            <el-form-item label="字典标签">
              <el-input v-model="dataQuery.dictLabel" placeholder="请输入字典标签" clearable />
            </el-form-item>
            <el-form-item>
              <el-button type="primary" @click="handleDataSearch">查询</el-button>
              <el-button type="success" @click="handleDataAdd">新增</el-button>
            </el-form-item>
          </el-form>
        </el-card>

        <el-card v-if="currentDictType">
          <el-table :data="dataList" border stripe v-loading="dataLoading">
            <el-table-column prop="dictLabel" label="字典标签" width="150" />
            <el-table-column prop="dictValue" label="字典值" width="120" />
            <el-table-column prop="dictSort" label="排序" width="80" />
            <el-table-column prop="listClass" label="样式" width="100">
              <template #default="{ row }">
                <el-tag v-if="row.listClass" :type="row.listClass === 'success' ? 'success' : row.listClass === 'danger' ? 'danger' : row.listClass === 'warning' ? 'warning' : 'info'" size="small">{{ row.listClass }}</el-tag>
                <span v-else>-</span>
              </template>
            </el-table-column>
            <el-table-column prop="isDefault" label="默认" width="80">
              <template #default="{ row }">
                <el-tag v-if="row.isDefault === 1" type="warning" size="small">是</el-tag>
                <span v-else>-</span>
              </template>
            </el-table-column>
            <el-table-column prop="status" label="状态" width="80">
              <template #default="{ row }">
                <el-tag :type="row.status === 1 ? 'success' : 'danger'" size="small">
                  {{ row.status === 1 ? '启用' : '禁用' }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column label="操作" width="150" fixed="right">
              <template #default="{ row }">
                <el-button link type="primary" size="small" @click="handleDataEdit(row)">编辑</el-button>
                <el-button link type="danger" size="small" @click="handleDataDelete(row)">删除</el-button>
              </template>
            </el-table-column>
          </el-table>
          <el-pagination
            v-if="currentDictType"
            v-model:current-page="dataQuery.current"
            v-model:page-size="dataQuery.size"
            :total="dataTotal"
            :page-sizes="[10, 20, 50]"
            layout="total, sizes, prev, pager, next"
            style="margin-top: 16px; justify-content: flex-end"
            @size-change="fetchDataList"
            @current-change="fetchDataList"
          />
        </el-card>
      </el-tab-pane>
    </el-tabs>

    <!-- 字典类型弹窗 -->
    <el-dialog v-model="typeDialogVisible" :title="typeDialogTitle" width="500px">
      <el-form :model="typeForm" ref="typeFormRef" label-width="100px">
        <el-form-item label="字典名称" prop="dictName">
          <el-input v-model="typeForm.dictName" placeholder="如：用户状态" />
        </el-form-item>
        <el-form-item label="字典类型" prop="dictType">
          <el-input v-model="typeForm.dictType" placeholder="如：sys_user_status" :disabled="isTypeEdit" />
        </el-form-item>
        <el-form-item label="状态">
          <el-radio-group v-model="typeForm.status">
            <el-radio :value="1">启用</el-radio>
            <el-radio :value="0">禁用</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="typeForm.remark" type="textarea" :rows="3" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="typeDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitType" :loading="typeSubmitLoading">确定</el-button>
      </template>
    </el-dialog>

    <!-- 字典数据弹窗 -->
    <el-dialog v-model="dataDialogVisible" :title="dataDialogTitle" width="500px">
      <el-form :model="dataForm" ref="dataFormRef" label-width="100px">
        <el-form-item label="字典标签" prop="dictLabel">
          <el-input v-model="dataForm.dictLabel" placeholder="如：启用" />
        </el-form-item>
        <el-form-item label="字典值" prop="dictValue">
          <el-input v-model="dataForm.dictValue" placeholder="如：1" />
        </el-form-item>
        <el-form-item label="排序">
          <el-input-number v-model="dataForm.dictSort" :min="0" />
        </el-form-item>
        <el-form-item label="回显样式">
          <el-select v-model="dataForm.listClass" placeholder="请选择" clearable style="width: 100%">
            <el-option label="success" value="success" />
            <el-option label="danger" value="danger" />
            <el-option label="warning" value="warning" />
            <el-option label="info" value="info" />
          </el-select>
        </el-form-item>
        <el-form-item label="是否默认">
          <el-radio-group v-model="dataForm.isDefault">
            <el-radio :value="1">是</el-radio>
            <el-radio :value="0">否</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="状态">
          <el-radio-group v-model="dataForm.status">
            <el-radio :value="1">启用</el-radio>
            <el-radio :value="0">禁用</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dataDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitData" :loading="dataSubmitLoading">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import request from '@/api/request'

const activeTab = ref('type')

// ---- 字典类型 ----
const typeList = ref<any[]>([])
const typeLoading = ref(false)
const typeTotal = ref(0)
const typeQuery = reactive({ current: 1, size: 10, dictName: '' })

const typeDialogVisible = ref(false)
const typeDialogTitle = ref('')
const isTypeEdit = ref(false)
const typeEditId = ref<number | null>(null)
const typeSubmitLoading = ref(false)
const typeFormRef = ref()
const typeForm = reactive({ id: null as number | null, dictName: '', dictType: '', status: 1, remark: '' })

async function fetchTypes() {
  typeLoading.value = true
  try {
    const res: any = await request.get('/dict/types', {
      params: { current: typeQuery.current, size: typeQuery.size, dictName: typeQuery.dictName || undefined },
    })
    typeList.value = res.records || []
    typeTotal.value = Number(res.total) || 0
  } catch { /* ignore */ } finally { typeLoading.value = false }
}

function handleTypeSearch() { typeQuery.current = 1; fetchTypes() }

function handleTypeAdd() {
  typeDialogTitle.value = '新增字典类型'
  isTypeEdit.value = false
  typeEditId.value = null
  Object.assign(typeForm, { id: null, dictName: '', dictType: '', status: 1, remark: '' })
  typeDialogVisible.value = true
}

function handleTypeEdit(row: any) {
  typeDialogTitle.value = '编辑字典类型'
  isTypeEdit.value = true
  typeEditId.value = row.id
  Object.assign(typeForm, { id: row.id, dictName: row.dictName, dictType: row.dictType, status: row.status, remark: row.remark || '' })
  typeDialogVisible.value = true
}

async function submitType() {
  typeSubmitLoading.value = true
  try {
    const data = { ...typeForm }
    if (isTypeEdit.value) {
      await request.put(`/dict/types/${typeEditId.value}`, data)
      ElMessage.success('更新成功')
    } else {
      await request.post('/dict/types', data)
      ElMessage.success('创建成功')
    }
    typeDialogVisible.value = false
    fetchTypes()
  } catch { /* ignore */ } finally { typeSubmitLoading.value = false }
}

async function handleTypeDelete(row: any) {
  try {
    await ElMessageBox.confirm(`确定删除字典类型 "${row.dictName}" 吗？`, '提示', { type: 'warning' })
    await request.delete(`/dict/types/${row.id}`)
    ElMessage.success('删除成功')
    fetchTypes()
  } catch { /* cancelled */ }
}

// ---- 字典数据 ----
const dataList = ref<any[]>([])
const dataLoading = ref(false)
const dataTotal = ref(0)
const dataQuery = reactive({ current: 1, size: 10, dictLabel: '' })
let currentDictTypeId = ref<number | null>(null)
const currentDictType = ref<any>(null)

const dataDialogVisible = ref(false)
const dataDialogTitle = ref('')
const dataEditId = ref<number | null>(null)
const dataSubmitLoading = ref(false)
const dataFormRef = ref()
const dataForm = reactive({ id: null as number | null, dictTypeId: 0, dictLabel: '', dictValue: '', dictSort: 0, cssClass: '', listClass: '', isDefault: 0, status: 1 })

function handleDictData(row: any) {
  activeTab.value = 'data'
  currentDictTypeId.value = row.id
  currentDictType.value = row
  dataQuery.current = 1
  dataQuery.dictLabel = ''
  fetchDataList()
}

async function fetchDataList() {
  if (!currentDictTypeId.value) return
  dataLoading.value = true
  try {
    const res: any = await request.get('/dict/data', {
      params: {
        current: dataQuery.current,
        size: dataQuery.size,
        dictTypeId: currentDictTypeId.value,
        dictLabel: dataQuery.dictLabel || undefined,
      },
    })
    dataList.value = res.records || []
    dataTotal.value = Number(res.total) || 0
  } catch { /* ignore */ } finally { dataLoading.value = false }
}

function handleDataSearch() { dataQuery.current = 1; fetchDataList() }

function handleDataAdd() {
  dataDialogTitle.value = '新增字典数据'
  dataEditId.value = null
  Object.assign(dataForm, { id: null, dictTypeId: currentDictTypeId.value, dictLabel: '', dictValue: '', dictSort: 0, cssClass: '', listClass: '', isDefault: 0, status: 1 })
  dataDialogVisible.value = true
}

function handleDataEdit(row: any) {
  dataDialogTitle.value = '编辑字典数据'
  dataEditId.value = row.id
  Object.assign(dataForm, { id: row.id, dictTypeId: row.dictTypeId, dictLabel: row.dictLabel, dictValue: row.dictValue, dictSort: row.dictSort, cssClass: row.cssClass || '', listClass: row.listClass || '', isDefault: row.isDefault, status: row.status })
  dataDialogVisible.value = true
}

async function submitData() {
  dataSubmitLoading.value = true
  try {
    const data = { ...dataForm }
    if (dataEditId.value) {
      await request.put(`/dict/data/${dataEditId.value}`, data)
      ElMessage.success('更新成功')
    } else {
      await request.post('/dict/data', data)
      ElMessage.success('创建成功')
    }
    dataDialogVisible.value = false
    fetchDataList()
  } catch { /* ignore */ } finally { dataSubmitLoading.value = false }
}

async function handleDataDelete(row: any) {
  try {
    await ElMessageBox.confirm(`确定删除字典数据 "${row.dictLabel}" 吗？`, '提示', { type: 'warning' })
    await request.delete(`/dict/data/${row.id}`)
    ElMessage.success('删除成功')
    fetchDataList()
  } catch { /* cancelled */ }
}

onMounted(() => { fetchTypes() })
</script>
