<template>
  <div class="login-page">
    <div class="login-header">
      <h1>AI 智能养老社区</h1>
      <p>会员 & 管理员统一登录平台</p>
    </div>

    <van-form @submit="handleLogin" class="login-form">
      <van-cell-group inset>
        <van-field
          v-model="form.phone"
          name="phone"
          label="手机号"
          placeholder="请输入手机号"
          type="tel"
          maxlength="11"
          :rules="[
            { required: true, message: '请输入手机号' },
            { pattern: /^1\d{10}$/, message: '手机号格式不正确' },
          ]"
        />
        <van-field
          v-model="form.password"
          name="password"
          label="密码"
          placeholder="请输入密码"
          type="password"
          :rules="[{ required: true, message: '请输入密码' }]"
        />
      </van-cell-group>

      <div class="login-actions">
        <van-button round block type="primary" native-type="submit" :loading="loading">
          登录
        </van-button>
        <div class="login-links">
          <router-link to="/register">注册会员</router-link>
          <router-link to="/forgot-password">忘记密码？</router-link>
        </div>
        <!-- 会员端与管理端登录分离：管理员请到管理端（5174）登录 -->
        <p style="text-align: center; margin-top: 12px; font-size: 13px; color: #59705f">
          管理员入口 →
          <a href="http://localhost:5174" style="color: #3b9e5a; text-decoration: underline">管理端登录</a>
        </p>
      </div>
    </van-form>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { showToast } from 'vant'
import { login } from '../../api/auth'
import { useUserStore } from '../../stores/user'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()
const loading = ref(false)

const form = ref({
  phone: '',
  password: '',
})

async function handleLogin() {
  loading.value = true
  try {
    const res = await login(form.value)
    const data = res.data
    const info = data.userInfo || {}

    showToast('登录成功')

    // 管理员账号：会员端不接待，提示后跳转到管理端登录入口
    if (info.role === 'ADMIN') {
      // 管理端登录地址（管理前端 dev 端口为 5174，其根路径会自动跳到 /admin/login）
      const adminPort = 5174
      const currentPort = window.location.port
      // 防止“跳转目标 === 当前来源”造成无限重载；若同源则仅提示，不再自跳转
      if (currentPort === String(adminPort)) {
        showToast('该账号是管理员，请登录管理端入口（当前页面需使用管理端 5173 会员端）')
        return
      }
      showToast('该账号是管理员，正在跳转管理端登录...')
      setTimeout(() => {
        window.location.href = 'http://localhost:' + adminPort
      }, 800)
      return
    }

    // 会员账号 → 保存令牌并进入会员端
    userStore.setTokens(data.accessToken, data.refreshToken)
    userStore.setUserInfo({
      id: info.id,
      phone: info.phone,
      realName: info.realName,
      role: info.role,
    })

    const redirect = route.query.redirect || '/member/home'
    router.replace(redirect)
  } catch (e) {
    // 错误已在拦截器中处理
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.login-page {
  min-height: 100vh;
  background: linear-gradient(135deg, #3b9e5a 0%, #2e7d32 100%);
  display: flex;
  flex-direction: column;
  justify-content: center;
  padding: 0 16px;
}

.login-header {
  text-align: center;
  color: #fff;
  margin-bottom: 40px;
}

.login-header h1 {
  font-size: 32px;
  font-weight: 700;
  margin-bottom: 8px;
}

.login-header p {
  font-size: 16px;
  opacity: 0.8;
}

.login-form {
  background: #fff;
  border-radius: 16px;
  padding: 24px 0;
}

.login-actions {
  padding: 24px 16px 0;
}

.login-links {
  display: flex;
  justify-content: space-between;
  margin-top: 16px;
  font-size: 14px;
}

.login-links a {
  color: #3b9e5a;
  text-decoration: none;
}
</style>