<template>
  <div class="home-page">
    <div class="welcome-bar">
      <h2>{{ greeting }}，{{ userStore.userInfo?.realName || '会员' }}</h2>
      <p>祝您身体健康，生活愉快</p>
    </div>

    <div style="padding: 20px">
      <h3 style="margin-bottom: 16px; color: #303133">功能入口</h3>
      <el-row :gutter="16">
        <el-col :span="8" v-for="item in features" :key="item.label">
          <el-card shadow="hover" class="feature-card" @click="$router.push(item.to)" style="cursor: pointer; margin-bottom: 16px">
            <div style="display: flex; align-items: center; gap: 12px">
              <el-icon :size="32" :style="{ color: item.color }"><component :is="item.icon" /></el-icon>
              <div>
                <p style="font-weight: 600; font-size: 15px">{{ item.label }}</p>
                <p style="font-size: 12px; color: #909399">{{ item.desc }}</p>
              </div>
            </div>
          </el-card>
        </el-col>
      </el-row>
    </div>
  </div>
</template>

<script setup>
import { computed, shallowRef } from 'vue'
import { Calendar, List, ChatDotSquare, TrendCharts, Tickets, DataLine, Collection, Coin, Clock } from '@element-plus/icons-vue'
import { useUserStore } from '../../../stores/user'

const userStore = useUserStore()

const greeting = computed(() => {
  const h = new Date().getHours()
  if (h < 6) return '夜深了'; if (h < 9) return '早上好'; if (h < 12) return '上午好'
  if (h < 14) return '中午好'; if (h < 18) return '下午好'; return '晚上好'
})

const features = [
  { label: '体检预约', desc: '选择套餐，在线预约', icon: 'Calendar', color: '#3b9e5a', to: '/member/appointment/packages' },
  { label: '健康评测', desc: 'AI 智能评分与建议', icon: 'List', color: '#67C23A', to: '/member/health/assessment' },
  { label: 'AI 咨询', desc: '智能健康问答', icon: 'ChatDotSquare', color: '#E6A23C', to: '/member/health/chat' },
  { label: '健康记录', desc: '查看我的健康数据', icon: 'TrendCharts', color: '#F56C6C', to: '/member/health/history' },
  { label: '社区活动', desc: '报名参与社区活动', icon: 'Tickets', color: '#909399', to: '/member/activity/list' },
  { label: '健康趋势', desc: '查看数据变化', icon: 'DataLine', color: '#3b9e5a', to: '/member/health/trend' },
  { label: '健康指导', desc: 'AI 个性化建议', icon: 'Collection', color: '#67C23A', to: '/member/health/guidance' },
  { label: '积分明细', desc: '查看积分变动', icon: 'Coin', color: '#E6A23C', to: '/member/points' },
  { label: '预约记录', desc: '查看我的预约', icon: 'Clock', color: '#F56C6C', to: '/member/appointment/list' },
]
</script>

<style scoped>
.home-page { min-height: 100vh; background: #eef5ee; }
.welcome-bar { background: linear-gradient(135deg, #3b9e5a 0%, #2e7d32 100%); color: #fff; padding: 32px 24px; }
.welcome-bar h2 { font-size: 24px; margin-bottom: 4px; }
.welcome-bar p { font-size: 14px; opacity: 0.85; }
.feature-card:hover { border-color: #3b9e5a; }
</style>
