<template>
  <div class="page-container">
    <el-page-header @back="$router.replace('/admin/members')" title="返回" :content="`会员详情 #${userId}`" />

    <div v-if="member" style="margin-top: 20px">
      <el-descriptions :column="3" border>
        <el-descriptions-item label="ID">{{ member.id }}</el-descriptions-item>
        <el-descriptions-item label="手机号">{{ member.phone }}</el-descriptions-item>
        <el-descriptions-item label="姓名">{{ member.realName || '-' }}</el-descriptions-item>
        <el-descriptions-item label="性别">{{ member.gender || '-' }}</el-descriptions-item>
        <el-descriptions-item label="出生日期">{{ member.birthDate || '-' }}</el-descriptions-item>
        <el-descriptions-item label="身高">{{ member.height ? member.height + 'cm' : '-' }}</el-descriptions-item>
        <el-descriptions-item label="等级">{{ member.memberLevel }}</el-descriptions-item>
        <el-descriptions-item label="积分">{{ member.points }}</el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag :type="member.status === '启用' ? 'success' : 'danger'">{{ member.status }}</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="紧急联系人">{{ member.emergencyContact || '-' }}</el-descriptions-item>
        <el-descriptions-item label="注册时间">{{ member.createTime }}</el-descriptions-item>
      </el-descriptions>

      <el-tabs v-model="activeTab" style="margin-top: 20px">
        <el-tab-pane label="健康记录" name="health">
          <el-table :data="healthList" border stripe size="small">
            <el-table-column prop="recordedTime" label="记录时间" width="180" />
            <el-table-column prop="systolic" label="收缩压" /><el-table-column prop="diastolic" label="舒张压" />
            <el-table-column prop="bloodSugar" label="血糖" /><el-table-column prop="heartRate" label="心率" />
            <el-table-column prop="weight" label="体重" /><el-table-column prop="bmi" label="BMI" />
          </el-table>
        </el-tab-pane>
        <el-tab-pane label="预约记录" name="appointment">
          <el-table :data="appointments" border stripe size="small">
            <el-table-column prop="id" label="ID" width="70" />
            <el-table-column prop="packageId" label="套餐ID" width="100" />
            <el-table-column prop="status" label="状态" width="100">
              <template #default="{ row }">
                <el-tag :type="{ '待确认': 'warning', '已确认': 'primary', '已完成': 'success', '已取消': 'info' }[row.status]" size="small">{{ row.status }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="reportUrl" label="报告" width="100">
              <template #default="{ row }">
                <el-tag v-if="row.reportUrl" type="success" size="small">已上传</el-tag>
                <span v-else style="color: #909399">无</span>
              </template>
            </el-table-column>
            <el-table-column prop="createTime" label="创建时间" />
          </el-table>
        </el-tab-pane>
      </el-tabs>
    </div>

    <el-empty v-else description="加载中..." />
  </div>
</template>

<script setup>
import { ref, onMounted, watch } from 'vue'
import { useRoute } from 'vue-router'
import request from '../../utils/request'

const route = useRoute()
const userId = route.params.id
const member = ref(null)
const healthList = ref([])
const appointments = ref([])
const activeTab = ref('health')

onMounted(async () => {
  try {
    const res = await request.get(`/api/admin/members/${userId}`)
    member.value = res.data
    // 加载健康记录
    const hrRes = await request.get('/api/admin/health-record', { params: { userId, pageNum: 1, pageSize: 50 } })
    healthList.value = hrRes.data.list || []
    // 加载预约记录
    const apRes = await request.get('/api/admin/appointment', { params: { pageNum: 1, pageSize: 50 } })
    appointments.value = (apRes.data.list || []).filter(a => a.userId == userId)
  } catch (e) { /* handled */ }
})
</script>
