<template>
  <el-container class="admin-layout">
    <el-aside :width="isCollapse ? '64px' : '220px'" class="aside">
      <div class="logo" @click="$router.push('/admin/dashboard')">
        <el-icon :size="24"><Monitor /></el-icon>
        <span v-show="!isCollapse">管理后台</span>
      </div>
      <el-menu :default-active="$route.path" router :collapse="isCollapse" background-color="#2c4431" text-color="#bfcbd9" active-text-color="#3b9e5a">
        <el-menu-item index="/admin/dashboard">
          <el-icon><DataAnalysis /></el-icon><span>仪表盘</span>
        </el-menu-item>
        <el-menu-item index="/admin/members">
          <el-icon><User /></el-icon><span>会员管理</span>
        </el-menu-item>
        <el-menu-item index="/admin/health-record">
          <el-icon><Document /></el-icon><span>健康档案</span>
        </el-menu-item>
        <el-sub-menu index="/admin/appointment">
          <template #title>
            <el-icon><Calendar /></el-icon><span>体检管理</span>
          </template>
          <el-menu-item index="/admin/appointment/packages">套餐管理</el-menu-item>
          <el-menu-item index="/admin/appointment/slots">时段管理</el-menu-item>
          <el-menu-item index="/admin/appointment/list">预约管理</el-menu-item>
        </el-sub-menu>
        <el-menu-item index="/admin/assessment">
          <el-icon><List /></el-icon><span>评测管理</span>
        </el-menu-item>
        <el-menu-item index="/admin/activity">
          <el-icon><Tickets /></el-icon><span>活动管理</span>
        </el-menu-item>
        <el-menu-item index="/admin/message">
          <el-icon><ChatDotSquare /></el-icon><span>消息管理</span>
        </el-menu-item>
        <el-menu-item index="/admin/config">
          <el-icon><Setting /></el-icon><span>系统配置</span>
        </el-menu-item>
      </el-menu>
    </el-aside>

    <el-container>
      <el-header class="header">
        <el-icon class="collapse-btn" @click="isCollapse = !isCollapse"><Fold /></el-icon>
        <el-breadcrumb separator="/">
          <el-breadcrumb-item :to="{ path: '/admin/dashboard' }">首页</el-breadcrumb-item>
          <el-breadcrumb-item>{{ $route.name || '' }}</el-breadcrumb-item>
        </el-breadcrumb>
        <div class="header-right">
          <el-dropdown @command="handleCommand">
            <span class="user-info">
              <el-icon><UserFilled /></el-icon>
              {{ userStore.userInfo?.realName || '管理员' }}
              <el-icon class="el-icon--right"><ArrowDown /></el-icon>
            </span>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="logout">退出登录</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </el-header>

      <el-main class="main">
        <router-view />
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '../stores/user'
import { ElMessageBox } from 'element-plus'
import request from '../utils/request'

const router = useRouter()
const userStore = useUserStore()
const isCollapse = ref(false)

async function handleCommand(cmd) {
  if (cmd === 'logout') {
    await ElMessageBox.confirm('确定要退出登录吗？', '提示', { type: 'warning' })
    try { await request.post('/api/auth/logout', { accessToken: userStore.accessToken, refreshToken: userStore.refreshToken }) } catch (e) { /* ignore */ }
    userStore.logout()
    router.push('/')  // 回到角色选择页
  }
}
</script>

<style scoped>
.admin-layout { height: 100vh; }
.aside { background: #2c4431; transition: width 0.3s; overflow: hidden; }
.logo { height: 60px; display: flex; align-items: center; justify-content: center; gap: 8px; color: #fff; font-size: 18px; font-weight: 600; cursor: pointer; border-bottom: 1px solid rgba(255,255,255,0.1); }
.el-menu { border-right: none; }
.header { display: flex; align-items: center; gap: 16px; background: #fff; border-bottom: 1px solid #e6e6e6; padding: 0 20px; }
.collapse-btn { font-size: 20px; cursor: pointer; }
.header-right { margin-left: auto; }
.user-info { display: flex; align-items: center; gap: 4px; cursor: pointer; font-size: 14px; }
.main { background: #eef5ee; }
</style>
