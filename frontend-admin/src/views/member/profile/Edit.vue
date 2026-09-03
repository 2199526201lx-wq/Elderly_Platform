<template>
  <div class="page">
    <van-nav-bar title="编辑个人信息" left-arrow @click-left="$router.replace('/member/profile')" />

    <van-form @submit="handleSubmit" style="padding-top: 12px">
      <van-cell-group inset>
        <van-field v-model="form.realName" label="真实姓名" placeholder="请输入" />
        <van-field name="gender" label="性别">
          <template #input>
            <van-radio-group v-model="form.gender" direction="horizontal">
              <van-radio name="男">男</van-radio>
              <van-radio name="女">女</van-radio>
            </van-radio-group>
          </template>
        </van-field>
        <van-field v-model="birthDateStr" label="出生日期" is-link readonly @click="showDatePicker = true" placeholder="请选择" />
        <van-field v-model="form.height" label="身高(cm)" type="number" placeholder="请输入" />
        <van-field v-model="form.emergencyContact" label="紧急联系人" type="tel" placeholder="紧急联系人电话" maxlength="11" />
        <van-field v-model="form.avatar" label="头像URL" placeholder="头像图片链接" />
      </van-cell-group>

      <div style="padding: 16px">
        <van-button round block type="primary" native-type="submit" :loading="loading">保存</van-button>
      </div>
    </van-form>

    <van-popup v-model:show="showDatePicker" round position="bottom">
      <van-date-picker
        v-model="datePickerValue"
        title="选择出生日期"
        :min-date="new Date(1930, 0, 1)"
        :max-date="new Date()"
        @confirm="onDateConfirm"
        @cancel="showDatePicker = false"
      />
    </van-popup>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { showToast } from 'vant'
import request from '../../../utils/request'

const router = useRouter()
const loading = ref(false)
const showDatePicker = ref(false)
const birthDateStr = ref('')
const datePickerValue = ref(['2000', '01', '01'])

const form = ref({
  realName: '',
  gender: '',
  height: '',
  emergencyContact: '',
  avatar: '',
})

function onDateConfirm({ selectedValues }) {
  birthDateStr.value = selectedValues.join('-')
  showDatePicker.value = false
}

onMounted(async () => {
  try {
    const res = await request.get('/api/member/profile')
    const d = res.data
    form.value.realName = d.realName || ''
    form.value.gender = d.gender || ''
    form.value.height = d.height ? String(d.height) : ''
    form.value.emergencyContact = d.emergencyContact || ''
    form.value.avatar = d.avatar || ''
    if (d.birthDate) {
      birthDateStr.value = d.birthDate.substring(0, 10)
      datePickerValue.value = birthDateStr.value.split('-')
    }
  } catch (e) { /* handled */ }
})

async function handleSubmit() {
  loading.value = true
  try {
    await request.put('/api/member/profile', {
      realName: form.value.realName || null,
      gender: form.value.gender || null,
      birthDate: birthDateStr.value || null,
      height: form.value.height ? Number(form.value.height) : null,
      emergencyContact: form.value.emergencyContact || null,
      avatar: form.value.avatar || null,
    })
    showToast('保存成功')
    router.replace('/member/profile')
  } catch (e) { /* handled */ } finally { loading.value = false }
}
</script>
