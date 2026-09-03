<template>
  <div class="home-page">
    <!-- 顶部欢迎栏 -->
    <div class="welcome-bar">
      <div class="welcome-text">
        <h2>{{ greeting }}，{{ userStore.userInfo?.realName || '会员' }}</h2>
        <p>祝您身体健康，生活愉快</p>
      </div>
    </div>

    <!-- 功能宫格 -->
    <van-grid :column-num="3" :border="false" class="feature-grid">
      <van-grid-item icon="orders-o" text="体检预约" to="/member/appointment/packages" />
      <van-grid-item icon="todo-list-o" text="健康评测" to="/member/health/assessment" />
      <van-grid-item icon="chat" text="AI 咨询" to="/member/health/chat" />
      <van-grid-item icon="chart-trending-o" text="健康记录" to="/member/health/history" />
      <van-grid-item icon="friends-o" text="社区活动" to="/member/activity/list" />
      <van-grid-item icon="bar-chart-o" text="健康趋势" to="/member/health/trend" />
      <van-grid-item icon="bookmark-o" text="健康指导" to="/member/health/guidance" />
      <van-grid-item icon="balance-o" text="积分明细" to="/member/points" />
      <van-grid-item icon="clock-o" text="预约记录" to="/member/appointment/list" />
    </van-grid>

    <!-- 快捷操作 -->
    <div class="card">
      <van-cell title="我的预约" is-link to="/member/appointment/list" icon="orders-o" />
      <van-cell title="我的活动" is-link to="/member/activity/list" icon="friends-o" />
      <van-cell title="健康历史" is-link to="/member/health/history" icon="chart-trending-o" />
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { useUserStore } from '../../stores/user'

const userStore = useUserStore()

const greeting = computed(() => {
  const h = new Date().getHours()
  if (h < 6) return '夜深了'
  if (h < 9) return '早上好'
  if (h < 12) return '上午好'
  if (h < 14) return '中午好'
  if (h < 18) return '下午好'
  return '晚上好'
})
</script>

<style scoped>
.home-page {
  min-height: 100vh;
  background: #eef5ee;
}

.welcome-bar {
  background: linear-gradient(135deg, #3b9e5a 0%, #2e7d32 100%);
  color: #fff;
  padding: 40px 20px 30px;
}

.welcome-text h2 {
  font-size: 22px;
  font-weight: 600;
  margin-bottom: 4px;
}

.welcome-text p {
  font-size: 14px;
  opacity: 0.85;
}

.feature-grid {
  background: #fff;
  margin: 12px 16px;
  border-radius: 12px;
  overflow: hidden;
  padding: 8px 0;
}

.feature-grid :deep(.van-grid-item__content) {
  padding: 12px 8px;
  background: transparent;
}

.feature-grid :deep(.van-icon) {
  font-size: 28px;
  color: #3b9e5a;
}

.card {
  background: #fff;
  margin: 12px 16px;
  border-radius: 12px;
  overflow: hidden;
}
</style>
