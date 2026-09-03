<template>
  <div class="page">
    <van-nav-bar title="健康指导" left-arrow @click-left="$router.replace('/member/home')" />
    <van-pull-refresh v-model="refreshing" @refresh="onRefresh">
      <van-list v-model:loading="loading" :finished="finished" finished-text="没有更多了" @load="loadData">
        <div class="card" v-for="item in list" :key="item.id" @click="markRead(item)" style="cursor:pointer">
          <div style="display: flex; justify-content: space-between; align-items: center">
            <van-tag :type="item.type === '数据小结' ? 'warning' : 'primary'">{{ item.type }}</van-tag>
            <span style="font-size: 12px; color: #969799">{{ item.createTime }}</span>
          </div>
          <p style="margin-top: 8px; line-height: 1.6; color: #323233" :class="{ 'read-text': item.isRead }">{{ item.content }}</p>
        </div>
      </van-list>

      <van-empty v-if="!loading && finished && list.length === 0" description="暂无健康指导，录入更多健康数据后可获取指导建议">
        <van-button round type="primary" to="/member/health/record">去录入健康数据</van-button>
      </van-empty>
    </van-pull-refresh>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import request from '../../utils/request'

const list = ref([])
const loading = ref(false)
const finished = ref(false)
const refreshing = ref(false)
const pageNum = ref(1)

async function loadData() {
  try {
    const res = await request.get('/api/member/health-guidance/list', { params: { pageNum: pageNum.value, pageSize: 10 } })
    if (refreshing.value) { list.value = []; refreshing.value = false }
    list.value.push(...res.data.list)
    if (list.value.length >= res.data.total) finished.value = true
    else pageNum.value++
  } catch (e) { finished.value = true } finally { loading.value = false }
}

async function markRead(item) {
  if (!item.isRead) {
    try {
      await request.put(`/api/member/health-guidance/read/${item.id}`)
      item.isRead = 1
    } catch (e) { /* handled */ }
  }
}

function onRefresh() { pageNum.value = 1; finished.value = false; loadData() }
</script>

<style scoped>
.read-text { color: #969799 !important; }
</style>
