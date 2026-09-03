<template>
  <div class="page">
    <van-nav-bar title="修改密码" left-arrow @click-left="$router.replace('/member/profile/settings')" />

    <van-form @submit="handleSubmit" style="padding-top: 12px">
      <van-cell-group inset>
        <van-field v-model="form.oldPassword" label="原密码" type="password" placeholder="请输入原密码" :rules="[{ required: true, message: '请输入原密码' }]" />
        <van-field v-model="form.newPassword" label="新密码" type="password" placeholder="≥8位，含字母和数字"
          :rules="[
            { required: true, message: '请输入新密码' },
            { validator: (v) => v && v.length >= 8 && /[a-zA-Z]/.test(v) && /\d/.test(v), message: '密码至少8位，包含字母和数字' },
          ]"
        />
        <van-field v-model="form.confirmPassword" label="确认密码" type="password" placeholder="请再次输入新密码"
          :rules="[
            { required: true, message: '请确认密码' },
            { validator: (v) => v === form.newPassword || '两次密码不一致' },
          ]"
        />
      </van-cell-group>

      <div style="padding: 16px">
        <van-button round block type="primary" native-type="submit" :loading="loading">确认修改</van-button>
      </div>
    </van-form>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { showToast } from 'vant'
import { changePassword } from '../../api/auth'
import { useUserStore } from '../../stores/user'

const router = useRouter()
const userStore = useUserStore()
const loading = ref(false)

const form = ref({
  oldPassword: '',
  newPassword: '',
  confirmPassword: '',
})

async function handleSubmit() {
  loading.value = true
  try {
    await changePassword({
      oldPassword: form.value.oldPassword,
      newPassword: form.value.newPassword,
    })
    showToast('密码修改成功，请重新登录')
    userStore.logout()
    router.replace('/member/login')
  } catch (e) { /* handled */ } finally { loading.value = false }
}
</script>
