<template>
  <div style="padding: 20px">
    <h2>菜单管理</h2>

    <el-card style="margin-bottom: 20px">
      <el-button type="success" @click="handleAdd(null)">新增根菜单</el-button>
    </el-card>

    <el-card>
      <el-table
        :data="tableData"
        border
        row-key="id"
        :tree-props="{ children: 'children' }"
        v-loading="loading"
        style="width: 100%"
      >
        <el-table-column prop="menuName" label="菜单名称" min-width="200" />
        <el-table-column prop="menuType" label="类型" width="80">
          <template #default="{ row }">
            <el-tag :type="row.menuType === 1 ? 'warning' : row.menuType === 2 ? 'success' : 'info'" size="small">
              {{ row.menuType === 1 ? '目录' : row.menuType === 2 ? '菜单' : '按钮' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="routePath" label="路由路径" width="180" />
        <el-table-column prop="component" label="组件路径" width="200" />
        <el-table-column prop="icon" label="图标" width="80" />
        <el-table-column prop="sortOrder" label="排序" width="80" />
        <el-table-column prop="perms" label="权限标识" width="200" />
        <el-table-column prop="visible" label="可见" width="80">
          <template #default="{ row }">
            <el-tag :type="row.visible === 1 ? 'success' : 'danger'" size="small">
              {{ row.visible === 1 ? '是' : '否' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="220">
          <template #default="{ row }">
            <el-button size="small" type="primary" @click="handleAdd(row)">新增子项</el-button>
            <el-button size="small" type="warning" @click="handleEdit(row)">编辑</el-button>
            <el-button size="small" type="danger" @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 新增/编辑弹窗 -->
    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="550px">
      <el-form :model="formData" :rules="formRules" ref="formRef" label-width="100px">
        <el-form-item label="父级菜单">
          <el-tree-select
            v-model="formData.parentId"
            :data="menuTreeOptions"
            placeholder="顶级菜单"
            check-strictly
            :render-after-expand="false"
            style="width: 100%"
            clearable
          />
        </el-form-item>
        <el-form-item label="菜单类型" prop="menuType">
          <el-radio-group v-model="formData.menuType">
            <el-radio :value="1">目录</el-radio>
            <el-radio :value="2">菜单</el-radio>
            <el-radio :value="3">按钮</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="菜单名称" prop="menuName">
          <el-input v-model="formData.menuName" />
        </el-form-item>
        <el-form-item label="路由路径" prop="routePath">
          <el-input v-model="formData.routePath" placeholder="/system/user" />
        </el-form-item>
        <el-form-item label="组件路径" prop="component">
          <el-input v-model="formData.component" placeholder="system/user/UserManage" />
        </el-form-item>
        <el-form-item label="图标">
          <el-input v-model="formData.icon" placeholder="user" />
        </el-form-item>
        <el-form-item label="排序">
          <el-input-number v-model="formData.sortOrder" :min="0" />
        </el-form-item>
        <el-form-item label="权限标识">
          <el-input v-model="formData.perms" placeholder="sys:user:list" />
        </el-form-item>
        <el-form-item label="是否显示">
          <el-radio-group v-model="formData.visible">
            <el-radio :value="1">显示</el-radio>
            <el-radio :value="0">隐藏</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="状态">
          <el-radio-group v-model="formData.status">
            <el-radio :value="1">启用</el-radio>
            <el-radio :value="0">禁用</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit" :loading="submitLoading">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox, type FormInstance, type FormRules } from 'element-plus'
import request from '@/api/request'

const tableData = ref<any[]>([])
const loading = ref(false)
const menuTreeOptions = ref<any[]>([])

const dialogVisible = ref(false)
const dialogTitle = ref('')
const submitLoading = ref(false)
const formRef = ref<FormInstance>()

const formData = reactive({
  id: null as number | null,
  parentId: 0,
  menuType: 2,
  menuName: '',
  routePath: '',
  component: '',
  icon: '',
  sortOrder: 0,
  perms: '',
  visible: 1,
  status: 1,
})

const formRules: FormRules = {
  menuType: [{ required: true, message: '请选择菜单类型', trigger: 'change' }],
  menuName: [{ required: true, message: '请输入菜单名称', trigger: 'blur' }],
}

// 构建树形选择器选项
function buildTreeOptions(menus: any[]): any[] {
  return menus.map(m => ({
    value: m.id,
    label: m.menuName,
    children: m.children ? buildTreeOptions(m.children) : [],
  }))
}

async function fetchData() {
  loading.value = true
  try {
    const res: any = await request.get('/menus/tree')
    tableData.value = res || []
    menuTreeOptions.value = buildTreeOptions(res || [])
  } catch {
    // handled by interceptor
  } finally {
    loading.value = false
  }
}

function handleAdd(row: any | null) {
  dialogTitle.value = row ? `新增 "${row.menuName}" 的子菜单` : '新增根菜单'
  Object.assign(formData, {
    id: null,
    parentId: row ? row.id : 0,
    menuType: 2,
    menuName: '',
    routePath: '',
    component: '',
    icon: '',
    sortOrder: 0,
    perms: '',
    visible: 1,
    status: 1,
  })
  dialogVisible.value = true
}

function handleEdit(row: any) {
  dialogTitle.value = '编辑菜单'
  Object.assign(formData, {
    id: row.id,
    parentId: row.parentId,
    menuType: row.menuType,
    menuName: row.menuName,
    routePath: row.routePath,
    component: row.component,
    icon: row.icon,
    sortOrder: row.sortOrder,
    perms: row.perms,
    visible: row.visible,
    status: row.status,
  })
  dialogVisible.value = true
}

async function handleSubmit() {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return
  submitLoading.value = true
  try {
    const data = { ...formData }
    if (data.id) {
      await request.put(`/menus/${data.id}`, data)
      ElMessage.success('更新成功')
    } else {
      await request.post('/menus', data)
      ElMessage.success('创建成功')
    }
    dialogVisible.value = false
    fetchData()
  } catch {
    // handled by interceptor
  } finally {
    submitLoading.value = false
  }
}

async function handleDelete(row: any) {
  try {
    await ElMessageBox.confirm(`确定删除菜单 "${row.menuName}" 吗？`, '提示', { type: 'warning' })
    await request.delete(`/menus/${row.id}`)
    ElMessage.success('删除成功')
    fetchData()
  } catch {
    // cancelled
  }
}

onMounted(() => {
  fetchData()
})
</script>
