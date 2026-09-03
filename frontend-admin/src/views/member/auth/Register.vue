<template>
  <div class="login-page">
    <div class="login-card">
      <el-button text @click="$router.push('/member/login')" style="margin-bottom: 16px">
        <el-icon><ArrowLeft /></el-icon> 返回
      </el-button>
      <h2>会员注册</h2>
      <el-form ref="formRef" :model="form" :rules="rules" style="margin-top: 24px">
        <el-form-item prop="phone">
          <el-input v-model="form.phone" placeholder="手机号" :prefix-icon="Iphone" size="large" />
        </el-form-item>
        <el-form-item prop="code">
          <el-input v-model="form.code" placeholder="验证码" size="large">
            <template #append>
              <el-button :disabled="countdown > 0" @click="sendCode">{{ countdown > 0 ? countdown + 's' : '获取验证码' }}</el-button>
            </template>
          </el-input>
        </el-form-item>
        <el-form-item prop="password">
          <el-input v-model="form.password" placeholder="密码（≥8位，含字母和数字）" type="password" :prefix-icon="Lock" size="large" show-password />
        </el-form-item>
        <el-form-item prop="confirmPassword">
          <el-input v-model="form.confirmPassword" placeholder="确认密码" type="password" size="large" show-password />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" size="large" style="width: 100%" :loading="loading" @click="handleRegister">注 册</el-button>
        </el-form-item>
      </el-form>
      <div style="text-align: center">
        <el-button text type="primary" @click="$router.push('/member/login')">已有账号？去登录</el-button>
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

const router = useRouter()
const loading = ref(false)
const countdown = ref(0)
const formRef = ref()
let timer = null

const form = ref({ phone: '', code: '', password: '', confirmPassword: '' })
const rules = {
  phone: [{ required: true, message: '请输入手机号', trigger: 'blur' }],
  code: [{ required: true, message: '请输入验证码', trigger: 'blur' }],
  password: [{ required: true, message: '请设置密码', trigger: 'blur' },
    { validator: (r, v, cb) => v && v.length >= 8 && /[a-zA-Z]/.test(v) && /\d/.test(v) ? cb() : cb(new Error('密码至少8位，含字母和数字')), trigger: 'blur' }],
  confirmPassword: [{ required: true, message: '请确认密码', trigger: 'blur' },
    { validator: (r, v, cb) => v === form.value.password ? cb() : cb(new Error('两次密码不一致')), trigger: 'blur' }],
}

async function sendCode() {
  if (!form.value.phone || !/^1\d{10}$/.test(form.value.phone)) { ElMessage.warning('请先输入正确的手机号'); return }
  try {
    await request.post('/api/sms/send', { phone: form.value.phone })
    ElMessage.success('验证码已发送')
    countdown.value = 60; timer = setInterval(() => { countdown.value--; if (countdown.value <= 0) clearInterval(timer) }, 1000)
  } catch (e) { /* handled */ }
}

async function handleRegister() {
  await formRef.value.validate()
  loading.value = true
  try {
    await request.post('/api/auth/register', { phone: form.value.phone, code: form.value.code, password: form.value.password })
    ElMessage.success('注册成功'); router.replace('/member/login')
  } catch (e) { /* handled */ } finally { loading.value = false }
}
</script>

<style scoped>
.login-page { height: 100vh; display: flex; align-items: center; justify-content: center; background: linear-gradient(135deg, #3b9e5a 0%, #2e7d32 100%); }
.login-card { background: #fff; padding: 40px; border-radius: 12px; width: 440px; box-shadow: 0 8px 32px rgba(0,0,0,0.1); }
.login-card h2 { text-align: center; color: #303133; margin-bottom: 8px; }
</style>
