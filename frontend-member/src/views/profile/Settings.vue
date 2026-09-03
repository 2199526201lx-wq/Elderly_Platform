<template>
  <div class="page">
    <van-nav-bar title="设置" left-arrow @click-left="$router.replace('/member/profile')" />

    <van-cell-group inset style="margin-top: 12px">
      <van-cell title="修改密码" is-link to="/member/profile/password" icon="lock" />
    </van-cell-group>

    <div style="padding: 24px 16px">
      <van-button round block type="danger" plain @click="handleLogout">退出登录</van-button>
    </div>
  </div>
</template>

<script setup>
import { useRouter } from 'vue-router'
import { showConfirmDialog } from 'vant'
import { useUserStore } from '../../stores/user'
import { logout as logoutApi } from '../../api/auth'

const router = useRouter()
const userStore = useUserStore()

async function handleLogout() {
  try {
    await showConfirmDialog({ title: '确认退出', message: '确定要退出登录吗？' })
    try {
      await logoutApi({
        accessToken: userStore.accessToken,
        refreshToken: userStore.refreshToken,
      })
    } catch (e) { /* 即使接口失败也执行本地登出 */ }
    userStore.logout()
    router.replace('/login')
  } catch (e) { /* cancelled */ }
}
</script>
