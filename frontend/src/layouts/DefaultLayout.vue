<template>
  <el-container style="height: 100vh">
    <el-aside width="220px" style="background-color: #304156">
      <div class="logo">smarthub</div>
      <el-menu :default-active="activeMenu" background-color="#304156" text-color="#bfcbd9" active-text-color="#409EFF" router>
        <el-menu-item index="/dashboard">
          <el-icon><HomeFilled /></el-icon>
          <span>首页</span>
        </el-menu-item>
        <el-sub-menu index="system">
          <template #title>
            <el-icon><Setting /></el-icon>
            <span>系统管理</span>
          </template>
          <el-menu-item index="/system/user">用户管理</el-menu-item>
          <el-menu-item index="/system/role">角色管理</el-menu-item>
          <el-menu-item index="/system/menu">菜单管理</el-menu-item>
        </el-sub-menu>
        <el-sub-menu index="ai">
          <template #title>
            <el-icon><MagicStick /></el-icon>
            <span>AI 功能</span>
          </template>
          <el-menu-item index="/ai/chat">AI 聊天</el-menu-item>
          <el-menu-item index="/ai/codegen">代码生成</el-menu-item>
        </el-sub-menu>
      </el-menu>
    </el-aside>
    <el-container>
      <el-header style="display: flex; align-items: center; justify-content: space-between; background: #fff">
        <span>欢迎使用 smarthub AI 管理平台</span>
        <el-button type="danger" size="small" @click="handleLogout">退出登录</el-button>
      </el-header>
      <el-main>
        <router-view />
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useUserStore } from '@/store/user'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

const activeMenu = computed(() => route.path)

function handleLogout() {
  userStore.logout()
  router.push('/login')
}
</script>

<style scoped>
.logo {
  height: 60px;
  line-height: 60px;
  text-align: center;
  color: #fff;
  font-size: 20px;
  font-weight: bold;
  background-color: #2b2f3a;
}
</style>
