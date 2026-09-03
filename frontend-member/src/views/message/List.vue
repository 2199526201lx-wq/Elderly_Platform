<template>
  <div class="page">
    <van-nav-bar title="消息中心" />
    <van-empty v-if="!list.length" description="暂无消息" />
    <van-cell-group v-else inset style="margin-top: 12px">
      <van-cell
        v-for="msg in list"
        :key="msg.id"
        :title="msg.title"
        :label="msg.createTime"
        is-link
        :to="`/member/message/${msg.id}`"
      >
        <template #icon>
          <van-badge :dot="!msg.isRead" style="margin-right: 8px" />
        </template>
        <template #value>
          <van-tag :type="typeColor(msg.type)" size="medium">{{ msg.type }}</van-tag>
        </template>
      </van-cell>
    </van-cell-group>
    <van-pagination
      v-if="total > pageSize"
      v-model="pageNum"
      :total-items="total"
      :items-per-page="pageSize"
      @change="loadData"
      style="margin: 16px auto; width: fit-content"
    />
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import request from '../../utils/request'

const list = ref([])
const pageNum = ref(1)
const pageSize = 20
const total = ref(0)

function typeColor(type) {
  const map = { '预约': 'primary', '活动': 'success', '健康提醒': 'warning', '系统': 'default' }
  return map[type] || 'default'
}

async function loadData() {
  try {
    const res = await request.get('/api/member/message/list', { params: { pageNum: pageNum.value, pageSize } })
    list.value = res.data.list
    total.value = res.data.total
  } catch (e) { /* handled */ }
}

onMounted(loadData)
</script>
