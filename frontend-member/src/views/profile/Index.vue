<template>
  <div class="profile-page">
    <div class="profile-header">
      <van-image round width="64" height="64" :src="userInfo.avatar || ''" style="background: #eee">
        <template #error>
          <van-icon name="user-o" size="32" color="#969799" />
        </template>
      </van-image>
      <div class="profile-info">
        <h3>{{ userInfo.realName || '会员' }}</h3>
        <p>{{ userInfo.phone || '' }}</p>
        <van-tag type="primary" size="medium">{{ userInfo.memberLevel || '普通' }}</van-tag>
      </div>
    </div>

    <div class="card" style="display: flex; justify-content: space-around; text-align: center">
      <div>
        <p style="font-size: 24px; font-weight: 700; color: #3b9e5a">{{ userInfo.points || 0 }}</p>
        <p style="font-size: 12px; color: #969799">积分</p>
      </div>
      <div>
        <p style="font-size: 24px; font-weight: 700; color: #3b9e5a">{{ userInfo.memberLevel || '普通' }}</p>
        <p style="font-size: 12px; color: #969799">等级</p>
      </div>
    </div>

    <van-cell-group inset style="margin-top: 12px">
      <van-cell title="个人信息" is-link to="/member/profile/edit" icon="user-o" />
      <van-cell title="我的预约" is-link to="/member/appointment/list" icon="orders-o" />
      <van-cell title="我的活动" is-link to="/member/activity/list?my=1" icon="friends-o" />
      <van-cell title="积分明细" is-link to="/member/points" icon="balance-o" />
      <van-cell title="健康记录" is-link to="/member/health/history" icon="chart-trending-o" />
    </van-cell-group>

    <van-cell-group inset style="margin-top: 12px">
      <van-cell title="设置" is-link to="/member/profile/settings" icon="setting-o" />
    </van-cell-group>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import request from '../../utils/request'

const userInfo = ref({})

onMounted(async () => {
  try {
    const res = await request.get('/api/member/profile')
    userInfo.value = res.data
  } catch (e) { /* handled */ }
})
</script>

<style scoped>
.profile-page {
  min-height: 100vh;
  background: #eef5ee;
}
.profile-header {
  background: linear-gradient(135deg, #3b9e5a 0%, #2e7d32 100%);
  padding: 40px 20px 30px;
  display: flex;
  align-items: center;
  gap: 16px;
  color: #fff;
}
.profile-info h3 {
  font-size: 20px;
  margin-bottom: 4px;
}
.profile-info p {
  font-size: 14px;
  opacity: 0.8;
  margin-bottom: 4px;
}
</style>
