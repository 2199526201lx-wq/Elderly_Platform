<template>
  <div style="padding: 20px">
    <h3 style="margin-bottom: 16px">消息中心</h3>
    <el-table :data="list" stripe>
      <el-table-column prop="title" label="标题" width="200" />
      <el-table-column prop="type" label="类型" width="100">
        <template #default="{ row }">
          <el-tag size="small" :type="typeColor(row.type)">{{ row.type }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="isRead" label="已读" width="80">
        <template #default="{ row }"><el-tag :type="row.isRead ? 'success' : 'warning'" size="small">{{ row.isRead ? '是' : '否' }}</el-tag></template>
      </el-table-column>
      <el-table-column prop="content" label="内容" show-overflow-tooltip />
      <el-table-column prop="createTime" label="时间" width="180" />
      <el-table-column label="操作" width="80">
        <template #default="{ row }"><el-button text type="primary" @click="$router.push(`/member/message/${row.id}`)">查看</el-button></template>
      </el-table-column>
    </el-table>
    <el-pagination style="margin-top: 16px; justify-content: flex-end"
      v-model:current-page="pageNum" :page-size="20" :total="total" layout="total, prev, pager, next"
      @current-change="loadData" />
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import request from '../../../utils/request'

const list = ref([])
const pageNum = ref(1)
const total = ref(0)

function typeColor(t) {
  return { '预约': '', '活动': 'success', '健康提醒': 'warning', '系统': 'info' }[t] || 'info'
}

async function loadData() {
  try {
    const res = await request.get('/api/member/message/list', { params: { pageNum: pageNum.value, pageSize: 20 } })
    list.value = res.data.list; total.value = res.data.total
  } catch (e) { /* handled */ }
}

onMounted(loadData)
</script>
