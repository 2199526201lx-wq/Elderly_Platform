<template>
  <div class="page">
    <van-nav-bar title="健康评测" left-arrow @click-left="$router.replace('/member/home')" />
    <van-pull-refresh v-model="refreshing" @refresh="onRefresh">
      <van-list v-model:loading="loading" :finished="finished" @load="loadData">
        <div class="card" v-for="item in list" :key="item.id" @click="$router.push(`/member/health/assessment/${item.id}`)" style="cursor:pointer">
          <h4>{{ item.title }}</h4>
          <p style="color: #969799; font-size: 13px; margin-top: 4px">{{ item.description }}</p>
          <van-tag type="primary" style="margin-top: 8px">满分 {{ item.totalScore }} 分</van-tag>
        </div>
      </van-list>
    </van-pull-refresh>

    <div style="padding: 0 16px 16px">
      <van-button plain type="primary" block @click="$router.push('/member/health/assessment/result-history')">查看评测历史</van-button>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import request from '../../../utils/request'

const list = ref([])
const loading = ref(false)
const finished = ref(true)
const refreshing = ref(false)

async function loadData() {
  try {
    const res = await request.get('/api/member/assessment/all')
    list.value = res.data
  } catch (e) { /* handled */ } finally { loading.value = false; finished.value = true }
}

function onRefresh() { refreshing.value = false; loadData() }
</script>
