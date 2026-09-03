<template>
  <div class="page-container">
    <h2 style="margin-bottom: 20px">数据概览</h2>
    <el-row :gutter="20">
      <el-col :span="6" v-for="item in cards" :key="item.label">
        <el-card shadow="hover">
          <div style="display: flex; align-items: center; gap: 16px">
            <el-icon :size="48" :style="{ color: item.color }">
              <component :is="item.icon" />
            </el-icon>
            <div>
              <p style="font-size: 13px; color: #909399">{{ item.label }}</p>
              <p style="font-size: 32px; font-weight: 700; color: #303133">{{ summary[item.key] ?? '-' }}</p>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, onMounted, shallowRef } from 'vue'
import { User, Calendar, Tickets, Clock } from '@element-plus/icons-vue'
import request from '../../utils/request'

const summary = ref({})

const cards = [
  { key: 'memberTotal', label: '会员总数', icon: 'User', color: '#3b9e5a' },
  { key: 'memberToday', label: '今日新增会员', icon: 'User', color: '#67C23A' },
  { key: 'appointmentToday', label: '今日预约', icon: 'Calendar', color: '#E6A23C' },
  { key: 'registrationToday', label: '今日报名', icon: 'Tickets', color: '#E6A23C' },
  { key: 'appointmentPending', label: '待确认预约', icon: 'Clock', color: '#F56C6C' },
]

onMounted(async () => {
  try {
    const res = await request.get('/api/admin/dashboard/summary')
    summary.value = res.data
  } catch (e) { /* handled */ }
})
</script>
