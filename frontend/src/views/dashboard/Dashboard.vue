<template>
  <div>
    <h2>首页</h2>
    <el-row :gutter="20" style="margin-top: 20px">
      <el-col :span="6">
        <el-card shadow="hover">
          <template #header>用户数量</template>
          <div style="font-size: 24px; text-align: center">--</div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover">
          <template #header>角色数量</template>
          <div style="font-size: 24px; text-align: center">--</div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover">
          <template #header>AI 模型</template>
          <div style="font-size: 24px; text-align: center">4</div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover">
          <template #header>协议接入</template>
          <div style="font-size: 24px; text-align: center">2</div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>
