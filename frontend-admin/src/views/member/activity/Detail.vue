<template>
  <div class="page">
    <van-nav-bar title="活动详情" left-arrow @click-left="$router.replace('/member/activity/list')" />

    <div v-if="activity" style="padding: 12px 16px">
      <div class="card">
        <h3>{{ activity.title }}</h3>
        <van-tag :type="statusColor(activity.status)" style="margin: 8px 0">{{ activity.status }}</van-tag>
        <van-cell-group :border="false">
          <van-cell title="活动地点" :value="activity.location" icon="location-o" />
          <van-cell title="报名时间" :value="`${activity.registrationStartTime} ~ ${activity.registrationEndTime}`" icon="clock-o" />
          <van-cell title="活动时间" :value="`${activity.activityStartTime} ~ ${activity.activityEndTime}`" icon="calendar-o" />
          <van-cell title="参与人数" :value="`${activity.currentParticipants} / ${activity.maxParticipants}`" icon="friends-o" />
        </van-cell-group>
      </div>

      <div class="card" v-if="activity.content">
        <h4 style="margin-bottom: 8px">活动内容</h4>
        <p style="line-height: 1.8; white-space: pre-wrap">{{ activity.content }}</p>
      </div>

      <!-- 报名/签到按钮 -->
      <div v-if="checkInReg" class="card" style="text-align: center">
        <p v-if="checkInReg.checkInStatus === '已签到'" style="color: #07c160; font-weight: 600">✅ 已签到（+50 积分）</p>
        <van-button v-else type="success" round block @click="handleCheckIn" :loading="checkInLoading">签到（+50 积分）</van-button>
      </div>

      <div style="padding: 16px 0" v-if="!checkInReg">
        <van-button type="primary" round block @click="handleJoin" :loading="joinLoading" :disabled="activity.status !== '报名中'">
          {{ activity.status === '报名中' ? '立即报名' : '暂不可报名' }}
        </van-button>
      </div>
    </div>

    <van-loading v-else style="display: block; margin: 40px auto" />
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { showToast } from 'vant'
import request from '../../../utils/request'

const route = useRoute()
const activity = ref(null)
const checkInReg = ref(null)
const joinLoading = ref(false)
const checkInLoading = ref(false)

function statusColor(s) {
  return { '报名中': 'primary', '进行中': 'success', '已结束': 'default' }[s] || 'default'
}

onMounted(async () => {
  try {
    const res = await request.get(`/api/member/activity/${route.params.id}`)
    activity.value = res.data
    // 检查报名/签到状态
    try {
      const regRes = await request.get(`/api/member/activity/${route.params.id}/checkin-status`)
      checkInReg.value = regRes.data
    } catch (e) { checkInReg.value = null }
  } catch (e) { /* handled */ }
})

async function handleJoin() {
  joinLoading.value = true
  try {
    await request.post(`/api/member/activity/${route.params.id}/join`)
    showToast('报名成功')
    // 刷新状态
    const regRes = await request.get(`/api/member/activity/${route.params.id}/checkin-status`)
    checkInReg.value = regRes.data
    activity.value.currentParticipants++
  } catch (e) { /* handled */ } finally { joinLoading.value = false }
}

async function handleCheckIn() {
  checkInLoading.value = true
  try {
    await request.post(`/api/member/activity/${route.params.id}/checkin`)
    showToast('签到成功 +50积分')
    checkInReg.value.checkInStatus = '已签到'
  } catch (e) { /* handled */ } finally { checkInLoading.value = false }
}
</script>
