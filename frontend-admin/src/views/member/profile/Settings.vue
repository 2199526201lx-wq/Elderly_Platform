<template>
  <div style="padding: 20px">
    <el-page-header @back="$router.replace('/member/profile')" title="返回" content="设置" />
    <el-card style="margin-top: 16px">
      <el-menu>
        <el-menu-item index="password" @click="$router.push('/member/profile/password')">
          <el-icon><Lock /></el-icon>修改密码
        </el-menu-item>
      </el-menu>
    </el-card>
    <div style="padding: 24px 0">
      <el-button type="danger" round style="width: 200px" @click="handleLogout">退出登录</el-button>
    </div>
  </div>
</template>

<script setup>
import { useRouter } from 'vue-router'
import { Lock } from '@element-plus/icons-vue'
import { ElMessageBox } from 'element-plus'
import { useUserStore } from '../../../stores/user'
import request from '../../../utils/request'

const router = useRouter()
const userStore = useUserStore()

async function handleLogout() {
  try {
    await ElMessageBox.confirm('确定要退出登录吗？', '提示', { type: 'warning' })
    try { await request.post('/api/auth/logout', { accessToken: userStore.accessToken, refreshToken: userStore.refreshToken }) } catch (e) { /* ignore */ }
    userStore.logout()
    router.replace('/')
  } catch (e) { /* cancelled */ }
}
</script>
