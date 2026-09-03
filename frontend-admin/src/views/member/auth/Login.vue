<template>
  <div class="login-page">
    <div class="login-card">
      <el-button text @click="$router.push('/')" style="margin-bottom: 16px">
        <el-icon><ArrowLeft /></el-icon> 返回
      </el-button>
      <h2>会员登录</h2>
      <el-form ref="formRef" :model="form" :rules="rules" style="margin-top: 24px">
        <el-form-item prop="phone">
          <el-input v-model="form.phone" placeholder="手机号" :prefix-icon="Iphone" size="large" />
        </el-form-item>
        <el-form-item prop="password">
          <el-input v-model="form.password" placeholder="密码" type="password" :prefix-icon="Lock" size="large" show-password />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" size="large" style="width: 100%" :loading="loading" @click="handleLogin">登 录</el-button>
        </el-form-item>
      </el-form>
      <div style="display: flex; justify-content: space-between; font-size: 14px">
        <el-button text type="primary" @click="$router.push('/member/register')">注册账号</el-button>
        <el-button text type="primary" @click="$router.push('/member/forgot-password')">忘记密码？</el-button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { Iphone, Lock, ArrowLeft } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import request from '../../../utils/request'
import { useUserStore } from '../../../stores/user'

const router = useRouter()
const userStore = useUserStore()
const loading = ref(false)
const formRef = ref()
const form = ref({ phone: '', password: '' })
const rules = {
  phone: [{ required: true, message: '请输入手机号', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }],
}

async function handleLogin() {
  await formRef.value.validate()
  loading.value = true
  try {
    const res = await request.post('/api/auth/login', { ...form.value, role: 'MEMBER' })
    const data = res.data
    userStore.setTokens(data.accessToken, data.refreshToken)
    const info = data.userInfo || {}
    userStore.setUserInfo({ id: info.id, phone: info.phone, realName: info.realName, role: info.role })
    ElMessage.success('登录成功')
    router.replace('/member/home')
  } catch (e) { /* handled */ } finally { loading.value = false }
}
</script>

<style scoped>
.login-page { height: 100vh; display: flex; align-items: center; justify-content: center; background: linear-gradient(135deg, #3b9e5a 0%, #2e7d32 100%); }
.login-card { background: #fff; padding: 40px; border-radius: 12px; width: 400px; box-shadow: 0 8px 32px rgba(0,0,0,0.1); }
.login-card h2 { text-align: center; color: #303133; margin-bottom: 8px; }
</style>
