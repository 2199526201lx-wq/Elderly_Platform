<template>
  <div class="login-container">
    <div class="login-card">
      <el-button text @click="$router.push('/')" style="margin-bottom: 16px">
        <el-icon><ArrowLeft /></el-icon> 返回
      </el-button>
      <h2>管理员登录</h2>
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
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { Iphone, Lock, ArrowLeft } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import request from '../../utils/request'
import { useUserStore } from '../../stores/user'

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
  try {
    await formRef.value.validate()
  } catch (e) {
    ElMessage.warning('请填写手机号和密码')
    return
  }
  loading.value = true
  try {
    const res = await request.post('/api/auth/login', form.value)
    const data = res.data
    const info = data.userInfo || {}
    // 非管理员不允许进入管理端
    if (info.role !== 'ADMIN') {
      ElMessage.error('该账号不是管理员，请使用会员端登录')
      return
    }
    userStore.setTokens(data.accessToken, data.refreshToken)
    userStore.setUserInfo({ id: info.id, phone: info.phone, realName: info.realName, role: info.role })
    ElMessage.success('登录成功')
    router.push('/admin/dashboard')
  } catch (e) {
    const msg = e.response?.data?.message || e.message || '登录失败'
    ElMessage.error(msg)
  } finally { loading.value = false }
}
</script>

<style scoped>
.login-container { height: 100vh; display: flex; align-items: center; justify-content: center; background: linear-gradient(135deg, #3b9e5a 0%, #2e7d32 100%); }
.login-card { background: #fff; padding: 40px; border-radius: 12px; width: 400px; box-shadow: 0 8px 32px rgba(0,0,0,0.1); }
.login-card h2 { text-align: center; color: #303133; margin-bottom: 8px; }
</style>
