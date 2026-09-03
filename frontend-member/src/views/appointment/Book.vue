<template>
  <div class="page">
    <van-nav-bar title="选择时段" left-arrow @click-left="$router.replace('/member/appointment/packages')" />

    <!-- 套餐信息 -->
    <div class="card" v-if="pkgInfo" style="background: linear-gradient(135deg, #3b9e5a 0%, #2e7d32 100%); color: #fff">
      <div style="display: flex; justify-content: space-between; align-items: center">
        <div>
          <p style="font-size: 12px; opacity: 0.8">您选择的套餐</p>
          <p style="font-size: 18px; font-weight: 600; margin-top: 4px">{{ pkgInfo.name }}</p>
        </div>
        <span style="font-size: 18px; font-weight: 700">{{ pkgInfo.price }} 积分</span>
      </div>
    </div>

    <!-- 日期选择（原生 input type=date，保证可选日期） -->
    <div class="card">
      <div style="font-size: 14px; color: #646566; margin-bottom: 8px">预约日期</div>
      <van-field
        readonly
        v-model="selectedDateStr"
        label="日期"
        placeholder="请选择日期"
        input-align="left"
      >
        <template #input>
          <input
            type="date"
            v-model="selectedDateStr"
            :min="minDateStr"
            :max="maxDateStr"
            @change="onDateChange"
            style="border: none; background: transparent; font-size: 16px; width: 100%; outline: none;"
          />
        </template>
        <template #right-icon>
          <van-icon name="calendar-o" />
        </template>
      </van-field>
      <p style="font-size: 12px; color: #969799; margin-top: 4px">可预约日期：{{ minDateStr }} ~ {{ maxDateStr }}</p>
    </div>

    <!-- 时段列表 -->
    <div v-if="slots.length" style="padding: 0 16px">
      <div class="card" v-for="slot in slots" :key="slot.id" @click="handleBook(slot)" style="cursor:pointer; display: flex; justify-content: space-between; align-items: center">
        <div>
          <p style="font-weight: 600">{{ slot.timeRange }}</p>
          <p style="font-size: 12px; color: #969799">{{ slot.appointDate }}</p>
        </div>
        <div style="text-align: right">
          <p style="font-size: 13px">剩余 <span style="color: #3b9e5a; font-weight: 600">{{ slot.maxCount - slot.currentCount }}</span> 个名额</p>
        </div>
      </div>
    </div>
    <van-empty v-else-if="selectedDateStr" description="该日期暂无可用时段" />
    <van-empty v-else description="请先选择日期查看可用时段" />
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { showConfirmDialog, showToast } from 'vant'
import request from '../../utils/request'

const route = useRoute()
const router = useRouter()
const slots = ref([])
const selectedDateStr = ref('')
const pkgInfo = ref(null)

// 原生 input[type=date] 的 min/max 用本地时区的 YYYY-MM-DD
function formatLocalDate(d) {
  const year = d.getFullYear()
  const month = String(d.getMonth() + 1).padStart(2, '0')
  const day = String(d.getDate()).padStart(2, '0')
  return `${year}-${month}-${day}`
}

const minDateStr = formatLocalDate(new Date(Date.now() - 30 * 24 * 3600 * 1000))
const maxDateStr = formatLocalDate(new Date(Date.now() + 90 * 24 * 3600 * 1000))

// 获取套餐信息
onMounted(async () => {
  try {
    const res = await request.get('/api/member/appointment/packages', { params: { pageNum: 1, pageSize: 100 } })
    if (res.data?.list) {
      pkgInfo.value = res.data.list.find(p => p.id === Number(route.params.packageId)) || null
    }
  } catch (e) { /* handled */ }
})

async function onDateChange() {
  const dateStr = selectedDateStr.value
  if (!dateStr) { slots.value = []; return }
  try {
    const res = await request.get('/api/member/appointment/slots', { params: { packageId: route.params.packageId, date: dateStr } })
    slots.value = res.data
  } catch (e) { /* handled */ }
}

async function handleBook(slot) {
  try {
    await showConfirmDialog({ title: '确认预约', message: `预约 ${slot.appointDate} ${slot.timeRange}？\n将从您的积分中扣除相应费用。` })
    await request.post('/api/member/appointment/book', { slotId: slot.id })
    showToast('预约成功')
    router.replace('/member/appointment/list')
  } catch (e) { /* handled or cancelled */ }
}
</script>