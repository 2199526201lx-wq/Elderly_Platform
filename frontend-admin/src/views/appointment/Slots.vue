<template>
  <div class="page-container">
    <h2 style="margin-bottom: 16px">批量生成预约时段</h2>

    <el-form :model="form" label-width="100px" style="max-width: 600px">
      <el-form-item label="选择套餐">
        <el-select v-model="form.packageId" placeholder="请选择套餐" filterable>
          <el-option v-for="pkg in packages" :key="pkg.id" :label="pkg.name" :value="pkg.id" />
        </el-select>
      </el-form-item>
      <el-form-item label="日期范围">
        <el-date-picker v-model="form.dateRange" type="daterange" range-separator="至" start-placeholder="开始日期" end-placeholder="结束日期" value-format="YYYY-MM-DD" />
      </el-form-item>
      <el-form-item label="时间段">
        <el-select v-model="form.timeRanges" multiple filterable allow-create default-first-option placeholder="输入后回车，如 09:00-10:00">
          <el-option v-for="t in defaultTimes" :key="t" :label="t" :value="t" />
        </el-select>
      </el-form-item>
      <el-form-item label="每时段最大人数">
        <el-input-number v-model="form.maxCount" :min="1" :max="100" />
      </el-form-item>
      <el-form-item>
        <el-button type="primary" @click="handleGenerate" :loading="generating">生成时段</el-button>
      </el-form-item>
    </el-form>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import request from '../../utils/request'

const packages = ref([])
const generating = ref(false)
const defaultTimes = ['08:00-09:00', '09:00-10:00', '10:00-11:00', '11:00-12:00', '14:00-15:00', '15:00-16:00', '16:00-17:00']

const form = ref({ packageId: null, dateRange: null, timeRanges: [], maxCount: 10 })

onMounted(async () => {
  try {
    const res = await request.get('/api/admin/appointment/package', { params: { pageNum: 1, pageSize: 100 } })
    packages.value = res.data.list
  } catch (e) { /* handled */ }
})

async function handleGenerate() {
  if (!form.value.packageId || !form.value.dateRange || !form.value.timeRanges.length) {
    ElMessage.warning('请填写完整信息'); return
  }
  generating.value = true
  try {
    const params = new URLSearchParams()
    params.append('packageId', form.value.packageId)
    params.append('startDate', form.value.dateRange[0])
    params.append('endDate', form.value.dateRange[1])
    params.append('maxCount', form.value.maxCount)
    form.value.timeRanges.forEach(t => params.append('timeRanges', t))
    await request.post('/api/admin/appointment/slot/generate', params)
    ElMessage.success('时段生成成功')
  } catch (e) { /* handled */ } finally { generating.value = false }
}
</script>
