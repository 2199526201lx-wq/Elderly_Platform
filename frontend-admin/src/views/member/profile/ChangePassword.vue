<template>
  <div style="padding: 20px">
    <el-page-header @back="$router.replace('/member/profile/settings')" title="返回" content="修改密码" />
    <el-card style="margin-top: 16px; max-width: 500px">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="80px">
        <el-form-item label="原密码" prop="oldPassword">
          <el-input v-model="form.oldPassword" type="password" show-password />
        </el-form-item>
        <el-form-item label="新密码" prop="newPassword">
          <el-input v-model="form.newPassword" type="password" placeholder="≥8位，含字母和数字" show-password />
        </el-form-item>
        <el-form-item label="确认密码" prop="confirmPassword">
          <el-input v-model="form.confirmPassword" type="password" show-password />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSubmit" :loading="loading" style="width: 120px">确认修改</el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { useUserStore } from '../../../stores/user'
import request from '../../../utils/request'

const router = useRouter()
const userStore = useUserStore()
const loading = ref(false)
const formRef = ref()

const form = ref({ oldPassword: '', newPassword: '', confirmPassword: '' })
const rules = {
  oldPassword: [{ required: true, message: '请输入原密码', trigger: 'blur' }],
  newPassword: [{ required: true, message: '请输入新密码', trigger: 'blur' },
    { validator: (r, v, cb) => v && v.length >= 8 && /[a-zA-Z]/.test(v) && /\d/.test(v) ? cb() : cb(new Error('密码至少8位，含字母和数字')), trigger: 'blur' }],
  confirmPassword: [{ required: true, message: '请确认密码', trigger: 'blur' },
    { validator: (r, v, cb) => v === form.value.newPassword ? cb() : cb(new Error('两次密码不一致')), trigger: 'blur' }],
}

async function handleSubmit() {
  await formRef.value.validate()
  loading.value = true
  try {
    await request.post('/api/auth/change-password', { oldPassword: form.value.oldPassword, newPassword: form.value.newPassword })
    ElMessage.success('密码修改成功，请重新登录')
    userStore.logout()
    router.replace('/')
  } catch (e) { /* handled */ } finally { loading.value = false }
}
</script>
