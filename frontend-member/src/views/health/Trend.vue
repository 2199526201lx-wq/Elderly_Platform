<template>
  <div class="page">
    <van-nav-bar title="健康趋势" left-arrow @click-left="$router.replace('/member/home')" />
    <div class="card" v-if="stats && metrics.length > 0">
      <h4 style="margin-bottom: 12px">近 {{ months }} 个月健康数据统计</h4>
      <van-cell-group inset>
        <van-cell v-for="item in metrics" :key="item.label" :title="item.label">
          <template #value>
            <span style="color: #3b9e5a; font-weight: 600">{{ item.avg }}</span>
            <span style="color: #969799; font-size: 12px; margin-left: 8px">
              ({{ item.min }} ~ {{ item.max }})
            </span>
          </template>
        </van-cell>
      </van-cell-group>
    </div>
    <van-empty v-else description="暂无健康数据">
      <van-button round type="primary" to="/member/health/record">快去录入健康数据吧</van-button>
    </van-empty>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import request from '../../utils/request'

const months = 6
const stats = ref(null)

const metrics = computed(() => {
  if (!stats.value) return []
  const s = stats.value
  return [
    { label: '收缩压 mmHg', avg: s.systolicAvg, min: s.systolicMin, max: s.systolicMax },
    { label: '舒张压 mmHg', avg: s.diastolicAvg, min: s.diastolicMin, max: s.diastolicMax },
    { label: '血糖 mmol/L', avg: s.bloodSugarAvg, min: s.bloodSugarMin, max: s.bloodSugarMax },
    { label: '心率 次/分', avg: s.heartRateAvg, min: s.heartRateMin, max: s.heartRateMax },
    { label: 'BMI', avg: s.bmiAvg, min: s.bmiMin, max: s.bmiMax },
  ].filter(m => m.avg != null)
})

onMounted(async () => {
  try {
    const res = await request.get('/api/member/healthrecord/analyze', { params: { months } })
    stats.value = res.data
  } catch (e) { /* handled */ }
})
</script>
