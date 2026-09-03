<template>
  <div class="page">
    <van-nav-bar title="体检预约" left-arrow @click-left="$router.replace('/member/home')" />
    <van-list v-model:loading="loading" :finished="finished" @load="loadData">
      <div class="card" v-for="pkg in list" :key="pkg.id" @click="$router.push(`/member/appointment/book/${pkg.id}`)" style="cursor:pointer">
        <h4>{{ pkg.name }}</h4>
        <p style="color: #969799; font-size: 13px; margin: 4px 0">{{ pkg.suitablePeople }}</p>
        <p style="font-size: 13px; color: #646566; margin: 4px 0">{{ pkg.description }}</p>
        <div style="display: flex; justify-content: space-between; align-items: center; margin-top: 8px">
          <span style="font-size: 20px; font-weight: 700; color: #ee0a24">{{ pkg.price }} 积分</span>
          <van-button size="small" type="primary" round>立即预约</van-button>
        </div>
      </div>
    </van-list>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import request from '../../../utils/request'

const list = ref([])
const loading = ref(false)
const finished = ref(false)
const pageNum = ref(1)

async function loadData() {
  try {
    const res = await request.get('/api/member/appointment/packages', { params: { pageNum: pageNum.value, pageSize: 10 } })
    list.value.push(...res.data.list)
    if (list.value.length >= res.data.total) finished.value = true
    else pageNum.value++
  } catch (e) { finished.value = true } finally { loading.value = false }
}
</script>
