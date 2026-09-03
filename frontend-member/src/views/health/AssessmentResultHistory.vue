<template>
  <div class="page">
    <van-nav-bar title="评测历史" left-arrow @click-left="$router.replace('/member/health/assessment')" />
    <van-list v-model:loading="loading" :finished="finished" finished-text="没有更多了" @load="loadData">
      <div class="card" v-for="item in list" :key="item.id" @click="$router.push(`/member/health/assessment/result/${item.id}`)" style="cursor:pointer">
        <div style="display: flex; justify-content: space-between; align-items: center">
          <h4>评测 #{{ item.id }}</h4>
          <span style="font-size: 24px; font-weight: 700; color: #3b9e5a">{{ item.aiScore }} 分</span>
        </div>
        <p style="color: #969799; font-size: 12px; margin-top: 4px">{{ item.createTime }}</p>
        <p style="margin-top: 8px; font-size: 13px; color: #646566">
          规则分：{{ item.ruleScore }}
          <van-tag :type="item.aiScore >= 90 ? 'success' : item.aiScore >= 60 ? 'primary' : 'danger'" size="medium" style="margin-left: 8px">
            {{ item.aiScore >= 90 ? '优秀' : item.aiScore >= 80 ? '良好' : item.aiScore >= 60 ? '及格' : '需关注' }}
          </van-tag>
        </p>
      </div>
    </van-list>

    <van-empty v-if="!loading && list.length === 0" description="暂无评测记录">
      <van-button round type="primary" to="/member/health/assessment">去参与健康评测</van-button>
    </van-empty>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import request from '../../utils/request'

const list = ref([])
const loading = ref(false)
const finished = ref(false)

async function loadData() {
  try {
    const res = await request.get('/api/member/assessment/history')
    list.value = res.data
    finished.value = true
  } catch (e) { finished.value = true } finally { loading.value = false }
}
</script>
