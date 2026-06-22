<template>
  <div>
    <h2>用户管理</h2>
    <el-alert title="用户管理页面 - 待实现完整 CRUD 界面" type="info" :closable="false" style="margin-bottom: 20px" />
    <el-table :data="[]" border>
      <el-table-column prop="username" label="用户名" />
      <el-table-column prop="nickname" label="昵称" />
      <el-table-column label="操作">
        <template #default>
          <el-button size="small">编辑</el-button>
          <el-button size="small" type="danger">删除</el-button>
        </template>
      </el-table-column>
    </el-table>
  </div>
</template>
