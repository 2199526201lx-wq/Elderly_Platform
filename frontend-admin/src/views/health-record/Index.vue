<template>
  <div class="page-container">
    <h2 style="margin-bottom: 16px">健康档案查询</h2>
    <div class="search-bar">
      <el-input v-model="queryUserId" placeholder="输入会员ID" clearable style="width: 200px" />
      <el-button type="primary" @click="loadData">查询</el-button>
      <el-button @click="showTrend = true" :disabled="!queryUserId">查看趋势</el-button>
    </div>

    <el-table :data="list" border stripe>
      <el-table-column prop="id" label="ID" width="70" />
      <el-table-column prop="recordedTime" label="记录时间" width="180" />
      <el-table-column prop="systolic" label="收缩压(mmHg)" width="130" />
      <el-table-column prop="diastolic" label="舒张压(mmHg)" width="130" />
      <el-table-column prop="bloodSugar" label="血糖(mmol/L)" width="130" />
      <el-table-column prop="heartRate" label="心率(次/分)" width="120" />
      <el-table-column prop="weight" label="体重(kg)" width="100" />
      <el-table-column prop="bmi" label="BMI" width="80" />
    </el-table>

    <el-pagination style="margin-top: 16px; justify-content: flex-end"
      v-model:current-page="pageNum" :page-size="10" :total="total" layout="total, prev, pager, next"
      @current-change="loadData" />

    <!-- 趋势弹窗：折线图 -->
    <el-dialog v-model="showTrend" title="健康趋势（折线图）" width="760px">
      <div v-if="trendSeries && trendSeries.points.length" style="padding: 8px 0">
        <el-descriptions :column="5" border size="small" v-if="trend" style="margin-bottom: 16px">
          <el-descriptions-item label="收缩压均值">{{ trend.systolicAvg }}</el-descriptions-item>
          <el-descriptions-item label="舒张压均值">{{ trend.diastolicAvg }}</el-descriptions-item>
          <el-descriptions-item label="血糖均值">{{ trend.bloodSugarAvg }}</el-descriptions-item>
          <el-descriptions-item label="心率均值">{{ trend.heartRateAvg }}</el-descriptions-item>
          <el-descriptions-item label="BMI均值">{{ trend.bmiAvg }}</el-descriptions-item>
        </el-descriptions>

        <el-radio-group v-model="activeMetric" style="margin-bottom: 12px">
          <el-radio-button value="systolic">收缩压</el-radio-button>
          <el-radio-button value="diastolic">舒张压</el-radio-button>
          <el-radio-button value="bloodSugar">血糖</el-radio-button>
          <el-radio-button value="heartRate">心率</el-radio-button>
          <el-radio-button value="bmi">BMI</el-radio-button>
        </el-radio-group>

        <!-- 内联 SVG 折线图（无第三方依赖） -->
        <svg :viewBox="`0 0 ${W} ${H}`" style="width: 100%; height: 280px; background: #f8fdf8; border-radius: 8px">
          <!-- 横网格线 + Y 轴刻度数值 -->
          <g v-for="t in yTicks" :key="'t' + t.value">
            <line :x1="padL" :x2="W - padR" :y1="t.y" :y2="t.y" stroke="#dcebdc" stroke-dasharray="3 3" />
            <text :x="padL - 8" :y="t.y + 4" text-anchor="end" font-size="11" fill="#909399">{{ t.text }}</text>
          </g>

          <!-- Y 轴 -->
          <line :x1="padL" :y1="padT" :x2="padL" :y2="H - padB" stroke="#c0c4cc" stroke-width="1" />

          <!-- 折线 -->
          <polyline :points="trendSeries.points" fill="none" stroke="#3b9e5a" stroke-width="2.5" stroke-linejoin="round" />
          <g v-for="p in trendSeries.pointsArr" :key="'pt' + p.i">
            <circle :cx="p.x" :cy="p.y" r="4" fill="#3b9e5a" />
          </g>

          <!-- X 轴 -->
          <line :x1="padL" :y1="H - padB" :x2="W - padR" :y2="H - padB" stroke="#c0c4cc" stroke-width="1" />
          <g v-for="t in xTicks" :key="'x' + t.i">
            <line :x1="t.x" :y1="H - padB" :x2="t.x" :y2="H - padB + 4" stroke="#c0c4cc" stroke-width="1" />
            <text :x="t.x" :y="H - padB + 16" text-anchor="middle" font-size="11" fill="#909399">{{ t.text }}</text>
          </g>
        </svg>
        <p style="font-size: 13px; color: #59705f; margin-top: 8px">
          当前指标：{{ metricLabel }}，范围 {{ trendSeries.min }} ~ {{ trendSeries.max }}
        </p>
      </div>
      <el-empty v-else description="暂无数据" />
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'
import { ElMessage } from 'element-plus'
import request from '../../utils/request'

const queryUserId = ref('')
const list = ref([])
const total = ref(0)
const pageNum = ref(1)
const showTrend = ref(false)
const trend = ref(null)
const activeMetric = ref('systolic')

const W = 700, H = 260, padL = 44, padR = 16, padT = 18, padB = 20

const METRICS = {
  systolic: '收缩压(mmHg)',
  diastolic: '舒张压(mmHg)',
  bloodSugar: '血糖(mmol/L)',
  heartRate: '心率(次/分)',
  bmi: 'BMI',
}

const metricLabel = computed(() => METRICS[activeMetric.value] || '—')

// Y 轴刻度：在折线数据的最小~最大值之间拉出 5 档刻度（带数值标签）
const yTicks = computed(() => {
  const s = trendSeries.value
  if (!s) return []
  const ticks = []
  const step = s.range / 4
  for (let i = 0; i <= 4; i++) {
    const value = s.min + step * i
    const y = padT + ((H - padT - padB) * (4 - i)) / 4
    ticks.push({ y, text: formatNum(value) })
  }
  return ticks
})

// X 轴刻度：按最早/中段/最近三个时间点打刻度
const xTicks = computed(() => {
  const rows = [...list.value].slice().reverse()
  const times = rows
    .map(r => (r.recordedTime ? String(r.recordedTime).slice(0, 10) : ''))
    .filter(t => t)
  if (times.length < 1) return []
  const innerW = W - padL - padR
  const n = times.length
  const labelIdx = n <= 1 ? [0] : [0, Math.floor(n / 2), n - 1]
  return labelIdx.map((idx, k) => ({
    i: k,
    x: padL + (innerW * idx) / (n - 1),
    text: times[idx],
  }))
})

function formatNum(v) {
  return Number.isInteger(v) ? String(v) : Number(v.toFixed(1))
}

// 按时序升序排列的测量值，构建当前选中指标的折线
const trendSeries = computed(() => {
  const rows = [...list.value].slice().reverse() // 表默认按时间倒序，这里反转为由旧到新
  const vals = rows
    .map(r => r[activeMetric.value])
    .filter(v => v != null && !isNaN(Number(v)))
    .map(Number)
  if (vals.length < 2) return null
  const min = Math.min(...vals), max = Math.max(...vals)
  const range = (max - min) || 1
  const innerW = W - padL - padR, innerH = H - padT - padB
  const n = vals.length
  const pointsArr = vals.map((v, i) => ({
    x: padL + (innerW * i) / (n - 1),
    y: padT + innerH - ((v - min) / range) * innerH,
    i,
  }))
  return {
    points: pointsArr.map(p => `${p.x},${p.y}`).join(' '),
    pointsArr,
    min,
    max,
    range,
  }
})

async function loadData() {
  if (!queryUserId.value) { ElMessage.warning('请输入会员ID'); return }
  try {
    const res = await request.get('/api/admin/health-record', { params: { userId: queryUserId.value, pageNum: 1, pageSize: 50 } })
    list.value = res.data.list || []
    total.value = res.data.total || 0
  } catch (e) { /* handled */ }
  try {
    const tRes = await request.get('/api/admin/health-record/trend', { params: { userId: queryUserId.value, months: 6 } })
    trend.value = tRes.data
  } catch (e) { /* ignore */ }
}
</script>