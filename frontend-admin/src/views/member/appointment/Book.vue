<template>
  <div class="page">
    <van-nav-bar title="选择时段" left-arrow @click-left="$router.replace('/member/appointment/packages')" />

    <!-- 日期选择 -->
    <van-calendar v-model="selectedDate" poppable="false" @confirm="onDateConfirm" :min-date="today" :max-date="maxDate" :show-confirm="false" style="margin-bottom: 12px" />

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
    <van-empty v-else-if="selectedDate" description="该日期暂无可用时段" />
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { showConfirmDialog, showToast } from 'vant'
import request from '../../../utils/request'

const route = useRoute()
const router = useRouter()
const slots = ref([])
const selectedDate = ref(null)
// min-date 放宽到 30 天前，max-date 放宽到 90 天后，
// 避免管理员之前生成的时段落在日历窗口外而选不到
const today = new Date(Date.now() - 30 * 24 * 3600 * 1000)
const maxDate = new Date(Date.now() + 90 * 24 * 3600 * 1000)

// 用本地时区格式化 YYYY-MM-DD，避免 toISOString() 的 UTC 偏移导致日期错位
function formatLocalDate(d) {
  const year = d.getFullYear()
  const month = String(d.getMonth() + 1).padStart(2, '0')
  const day = String(d.getDate()).padStart(2, '0')
  return `${year}-${month}-${day}`
}

async function onDateConfirm(date) {
  const dateStr = formatLocalDate(date)
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
