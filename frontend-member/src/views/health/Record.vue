<template>
  <div class="page">
    <van-nav-bar title="健康数据录入" left-arrow @click-left="$router.replace('/member/home')" />
    <van-form @submit="handleSubmit" class="record-form">
      <van-cell-group inset>
        <van-field v-model="form.systolic" label="收缩压" type="digit" placeholder="mmHg（90-180）" input-align="right" :rules="[{ required: true, message: '请输入' }]" />
        <van-field v-model="form.diastolic" label="舒张压" type="digit" placeholder="mmHg（60-120）" input-align="right" :rules="[{ required: true, message: '请输入' }]" />
        <van-field v-model="form.bloodSugar" label="血糖" type="number" placeholder="mmol/L" input-align="right" :rules="[{ required: true, message: '请输入' }]" />
        <van-field v-model="form.heartRate" label="心率" type="digit" placeholder="次/分" input-align="right" :rules="[{ required: true, message: '请输入' }]" />
        <van-field v-model="form.weight" label="体重" type="number" placeholder="kg" input-align="right" :rules="[{ required: true, message: '请输入' }]" />
      </van-cell-group>
      <div style="padding: 16px">
        <van-button round block type="primary" native-type="submit" :loading="loading">提交记录</van-button>
      </div>
    </van-form>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { showToast } from 'vant'
import request from '../../utils/request'

const router = useRouter()
const loading = ref(false)
const form = ref({ systolic: '', diastolic: '', bloodSugar: '', heartRate: '', weight: '' })

async function handleSubmit() {
  loading.value = true
  try {
    await request.post('/api/member/healthrecord/record', {
      systolic: Number(form.value.systolic),
      diastolic: Number(form.value.diastolic),
      bloodSugar: Number(form.value.bloodSugar),
      heartRate: Number(form.value.heartRate),
      weight: Number(form.value.weight),
      recordedTime: new Date().toISOString().slice(0, 19), // ISO 格式 yyyy-MM-dd'T'HH:mm:ss，与后端 Jackson 默认解析一致
    })
    showToast('记录成功')
    router.replace('/member/health/history')
  } catch (e) { /* handled */ } finally { loading.value = false }
}
</script>

<style scoped>
.record-form { padding-top: 12px; }
</style>
