<template>
  <div class="member-layout">
    <el-container>
      <el-header class="member-header">
        <div class="header-left">
          <h3 style="cursor: pointer" @click="$router.push('/member/home')">AI 智能养老</h3>
        </div>
        <el-menu :default-active="activeMenu" mode="horizontal" router :ellipsis="false" class="nav-menu">
          <el-menu-item index="/member/home">首页</el-menu-item>
          <el-menu-item index="/member/message">
            消息
            <el-badge v-if="unreadCount > 0" :value="unreadCount" :max="99" style="margin-left: 4px" />
          </el-menu-item>
          <el-menu-item index="/member/profile">我的</el-menu-item>
        </el-menu>
        <div class="header-right">
          <el-dropdown @command="handleCommand">
            <span style="cursor: pointer; display: flex; align-items: center; gap: 4px">
              <el-icon><UserFilled /></el-icon>
              {{ userStore.userInfo?.realName || '会员' }}
              <el-icon><ArrowDown /></el-icon>
            </span>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="profile">个人信息</el-dropdown-item>
                <el-dropdown-item command="logout" divided>退出登录</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </el-header>

      <el-main class="member-main">
        <router-view />
      </el-main>
    </el-container>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { UserFilled, ArrowDown } from '@element-plus/icons-vue'
import { ElMessageBox } from 'element-plus'
import { useUserStore } from '../../../stores/user'
import request from '../../../utils/request'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()
const unreadCount = ref(0)

const activeMenu = computed(() => {
  const path = route.path
  if (path.startsWith('/member/message')) return '/member/message'
  if (path.startsWith('/member/profile')) return '/member/profile'
  return '/member/home'
})

async function handleCommand(cmd) {
  if (cmd === 'profile') router.push('/member/profile')
  else if (cmd === 'logout') {
    await ElMessageBox.confirm('确定退出登录？', '提示', { type: 'warning' })
    try { await request.post('/api/auth/logout', { accessToken: userStore.accessToken, refreshToken: userStore.refreshToken }) } catch (e) { /* ignore */ }
    userStore.logout(); router.push('/')
  }
}

onMounted(async () => {
  try { const res = await request.get('/api/member/message/unread/count', { _silent: true }); unreadCount.value = res.data || 0 } catch (e) { /* ignore */ }
})
</script>

<style scoped>
.member-layout { min-height: 100vh; background: #eef5ee; }
.member-header {
  display: flex; align-items: center; background: #fff;
  border-bottom: 1px solid #e6e6e6; padding: 0 20px; gap: 16px;
}
.header-left h3 { color: #3b9e5a; white-space: nowrap; }
.nav-menu { flex: 1; border-bottom: none; }
.header-right { margin-left: auto; white-space: nowrap; }
.member-main { padding: 0; }
</style>
