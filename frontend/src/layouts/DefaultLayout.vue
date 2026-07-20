<template>
  <el-container style="height: 100vh">
    <el-aside width="220px" style="background-color: #304156">
      <div class="logo">smarthub</div>
      <el-menu :default-active="activeMenu" background-color="#304156" text-color="#bfcbd9" active-text-color="#409EFF" router>
        <el-menu-item index="/dashboard">
          <el-icon><HomeFilled /></el-icon>
          <span>首页</span>
        </el-menu-item>

        <!-- 动态菜单 -->
        <template v-for="menu in userStore.menus" :key="menu.id">
          <!-- 一级菜单 -->
          <el-sub-menu v-if="menu.menuType === 1 && menu.children?.length > 0" :index="String(menu.id)">
            <template #title>
              <el-icon><component :is="getIcon(menu.icon)" /></el-icon>
              <span>{{ menu.menuName }}</span>
            </template>
            <el-menu-item
              v-for="child in getVisibleChildren(menu)"
              :key="child.id"
              :index="child.routePath || String(child.id)"
            >
              <el-icon v-if="child.icon"><component :is="getIcon(child.icon)" /></el-icon>
              <span>{{ child.menuName }}</span>
            </el-menu-item>
          </el-sub-menu>

          <!-- 单独的一级菜单（无子菜单） -->
          <el-menu-item v-else-if="menu.menuType === 1 && !menu.children?.length" :index="menu.routePath || String(menu.id)">
            <el-icon v-if="menu.icon"><component :is="getIcon(menu.icon)" /></el-icon>
            <span>{{ menu.menuName }}</span>
          </el-menu-item>
        </template>
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
import { HomeFilled, Setting, MagicStick, Connection, User, Monitor, FolderOpened, Collection, Document, Refresh, Timer, ChatDotRound, Link, DataLine, UserFilled, Cpu, Avatar, TrendCharts } from '@element-plus/icons-vue'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

const activeMenu = computed(() => route.path)

const iconMap: Record<string, any> = {
  'setting': Setting,
  'magic': MagicStick,
  'connection': Connection,
  'user': User,
  'monitor': Monitor,
  'folder-opened': FolderOpened,
  'collection': Collection,
  'document': Document,
  'refresh': Refresh,
  'time': Timer,
  'chat': ChatDotRound,
  'code': Cpu,
  'role': Avatar,
  'link': Link,
  'menu': TrendCharts,
  'user-filled': UserFilled,
  'chart': TrendCharts,
  'data-line': DataLine,
}

function getIcon(icon: string | undefined): any {
  if (!icon) return null
  return iconMap[icon] || null
}

function getVisibleChildren(menu: any): any[] {
  if (!menu.children) return []
  return menu.children.filter((child: any) => child.menuType === 2)
}

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
