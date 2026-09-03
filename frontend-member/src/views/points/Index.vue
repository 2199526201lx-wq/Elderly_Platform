<template>
  <div class="page">
    <van-nav-bar title="积分明细" left-arrow @click-left="$router.replace('/member/profile')" />
    <van-list v-model:loading="loading" :finished="finished" finished-text="没有更多了" @load="loadData">
      <van-cell-group inset>
        <van-cell v-for="item in list" :key="item.id">
          <template #title>
            <span>{{ item.description || item.type }}</span>
          </template>
          <template #label>
            <span style="font-size: 12px; color: #969799">{{ item.createTime }}</span>
          </template>
          <template #value>
            <span :style="{ color: item.changeAmount > 0 ? '#07c160' : '#ee0a24', fontWeight: 600 }">
              {{ item.changeAmount > 0 ? '+' : '' }}{{ item.changeAmount }}
            </span>
          </template>
        </van-cell>
      </van-cell-group>
    </van-list>

    <van-empty v-if="!loading && finished && list.length === 0" description="暂无积分记录" />
  </div>
</template>

<script setup>
import { ref } from 'vue'
import request from '../../utils/request'

const list = ref([])
const loading = ref(false)
const finished = ref(false)
const pageNum = ref(1)

async function loadData() {
  try {
    const res = await request.get('/api/member/points', { params: { pageNum: pageNum.value, pageSize: 20 } })
    list.value.push(...res.data.list)
    if (list.value.length >= res.data.total) finished.value = true
    else pageNum.value++
  } catch (e) { finished.value = true } finally { loading.value = false }
}
</script>
