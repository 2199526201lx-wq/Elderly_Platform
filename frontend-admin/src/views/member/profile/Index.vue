<template>
  <div class="profile-page">
    <div class="profile-header">
      <el-avatar :size="64" :src="userInfo.avatar" icon="UserFilled" />
      <div class="profile-info">
        <h3>{{ userInfo.realName || '会员' }}</h3>
        <p>{{ userInfo.phone || '' }}</p>
        <el-tag type="primary" size="small">{{ userInfo.memberLevel || '普通' }}</el-tag>
      </div>
    </div>

    <el-row :gutter="16" style="margin: 16px 20px">
      <el-col :span="12">
        <el-card shadow="hover" style="text-align: center">
          <p style="font-size: 28px; font-weight: 700; color: #3b9e5a">{{ userInfo.points || 0 }}</p>
          <p style="font-size: 13px; color: #909399">积分余额</p>
        </el-card>
      </el-col>
      <el-col :span="12">
        <el-card shadow="hover" style="text-align: center">
          <p style="font-size: 28px; font-weight: 700; color: #67C23A">{{ userInfo.memberLevel || '普通' }}</p>
          <p style="font-size: 13px; color: #909399">会员等级</p>
        </el-card>
      </el-col>
    </el-row>

    <div style="padding: 0 20px">
      <el-card shadow="never">
        <el-menu :default-active="$route.path" router>
          <el-menu-item index="/member/profile/edit"><el-icon><Edit /></el-icon>个人信息</el-menu-item>
          <el-menu-item index="/member/appointment/list"><el-icon><Calendar /></el-icon>我的预约</el-menu-item>
          <el-menu-item index="/member/activity/list"><el-icon><Tickets /></el-icon>我的活动</el-menu-item>
          <el-menu-item index="/member/points"><el-icon><Coin /></el-icon>积分明细</el-menu-item>
          <el-menu-item index="/member/health/history"><el-icon><TrendCharts /></el-icon>健康记录</el-menu-item>
          <el-menu-item index="/member/profile/settings"><el-icon><Setting /></el-icon>设置</el-menu-item>
        </el-menu>
      </el-card>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, shallowRef } from 'vue'
import { Edit, Calendar, Tickets, Coin, TrendCharts, Setting } from '@element-plus/icons-vue'
import request from '../../../utils/request'

const userInfo = ref({})

onMounted(async () => {
  try { const res = await request.get('/api/member/profile'); userInfo.value = res.data } catch (e) { /* handled */ }
})
</script>

<style scoped>
.profile-page { min-height: 100vh; background: #eef5ee; }
.profile-header {
  background: linear-gradient(135deg, #3b9e5a 0%, #2e7d32 100%);
  padding: 32px 24px; display: flex; align-items: center; gap: 16px; color: #fff;
}
.profile-info h3 { font-size: 20px; margin-bottom: 4px; }
.profile-info p { font-size: 14px; opacity: 0.85; margin-bottom: 4px; }
</style>
