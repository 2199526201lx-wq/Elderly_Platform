<template>
  <div class="auth-page">
    <van-nav-bar title="找回密码" left-arrow @click-left="$router.replace('/login')" />

    <van-form @submit="handleReset" class="auth-form">
      <van-cell-group inset>
        <van-field
          v-model="form.phone"
          label="手机号"
          placeholder="请输入注册手机号"
          type="tel"
          maxlength="11"
          :rules="[
            { required: true, message: '请输入手机号' },
            { pattern: /^1\d{10}$/, message: '手机号格式不正确' },
          ]"
        />
        <van-field
          v-model="form.code"
          label="验证码"
          placeholder="请输入验证码"
          maxlength="6"
          :rules="[{ required: true, message: '请输入验证码' }]"
        >
          <template #button>
            <van-button size="small" type="primary" :disabled="countdown > 0" @click="handleSendCode">
              {{ countdown > 0 ? `${countdown}s` : '获取验证码' }}
            </van-button>
          </template>
        </van-field>
        <van-field
          v-model="form.newPassword"
          label="新密码"
          placeholder="请设置新密码（≥8位，含字母和数字）"
          type="password"
          :rules="[
            { required: true, message: '请设置新密码' },
            { validator: (v) => v && v.length >= 8 && /[a-zA-Z]/.test(v) && /\d/.test(v), message: '密码至少8位，包含字母和数字' },
          ]"
        />
      </van-cell-group>

      <div class="auth-submit">
        <van-button round block type="primary" native-type="submit" :loading="loading">
          重置密码
        </van-button>
      </div>
    </van-form>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { showToast } from 'vant'
import { sendSmsCode, resetPassword } from '../../api/auth'

const router = useRouter()
const loading = ref(false)
const countdown = ref(0)
let timer = null

const form = ref({
  phone: '',
  code: '',
  newPassword: '',
})

async function handleSendCode() {
  if (!form.value.phone || !/^1\d{10}$/.test(form.value.phone)) {
    showToast('请先输入正确的手机号')
    return
  }
  try {
    await sendSmsCode(form.value.phone)
    showToast('验证码已发送')
    countdown.value = 60
    timer = setInterval(() => {
      countdown.value--
      if (countdown.value <= 0) clearInterval(timer)
    }, 1000)
  } catch (e) {
    // 拦截器已处理
  }
}

async function handleReset() {
  loading.value = true
  try {
    await resetPassword({
      phone: form.value.phone,
      code: form.value.code,
      newPassword: form.value.newPassword,
    })
    showToast('密码重置成功，请重新登录')
    router.replace('/login')
  } catch (e) {
    // 拦截器已处理
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.auth-page {
  min-height: 100vh;
  background: #eef5ee;
}
.auth-form {
  padding-top: 24px;
}
.auth-submit {
  padding: 24px 16px 0;
}
</style>
