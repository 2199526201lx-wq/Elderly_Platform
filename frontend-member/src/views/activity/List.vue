<template>
  <div class="page">
    <van-nav-bar :title="myOnly ? '我的活动' : '社区活动'" left-arrow @click-left="$router.replace('/member/home')" />
    <van-tabs v-if="!myOnly" v-model:active="activeTab" @change="onTabChange" style="margin-bottom: 0">
      <van-tab title="全部" name="" />
      <van-tab title="报名中" name="报名中" />
    </van-tabs>
    <van-pull-refresh v-model="refreshing" @refresh="onRefresh">
      <van-list v-model:loading="loading" :finished="finished" finished-text="没有更多了" @load="loadData">
        <div class="card" v-for="item in list" :key="item.id" @click="$router.push(`/member/activity/${item.id}`)" style="cursor:pointer">
          <h4>{{ item.title }}</h4>
          <p style="font-size: 13px; color: #969799; margin: 4px 0">
            <van-icon name="location-o" /> {{ item.location }}
          </p>
          <p style="font-size: 12px; color: #969799">
            活动时间：{{ item.activityStartTime }} ~ {{ item.activityEndTime }}
          </p>
          <div style="display: flex; justify-content: space-between; align-items: center; margin-top: 8px">
            <van-tag :type="statusColor(item.status)">{{ item.status }}</van-tag>
            <span style="font-size: 12px; color: #969799">{{ item.currentParticipants }}/{{ item.maxParticipants }} 人</span>
          </div>
        </div>
      </van-list>

      <van-empty v-if="!loading && finished && list.length === 0" description="{{ myOnly ? '暂无参加的活动，快去报名吧' : '暂无社区活动，敬请期待' }}" />
    </van-pull-refresh>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRoute } from 'vue-router'
import request from '../../utils/request'

const route = useRoute()
const myOnly = ref(route.query.my === '1')

const list = ref([])
const loading = ref(false)
const finished = ref(false)
const refreshing = ref(false)
const pageNum = ref(1)
const activeTab = ref('')

function statusColor(s) {
  return { '报名中': 'primary', '进行中': 'success', '已结束': 'default', '草稿': 'warning' }[s] || 'default'
}

async function loadData() {
  loading.value = true
  try {
    const apiPath = myOnly.value ? '/api/member/activity/my' : '/api/member/activity/list'
    const params = { pageNum: pageNum.value, pageSize: 10 }
    if (activeTab.value) params.status = activeTab.value
    const res = await request.get(apiPath, { params })
    if (refreshing.value) { list.value = []; refreshing.value = false }
    // 按 id 去重，防止因分页或重复请求导致重复活动
    const seenIds = new Set(list.value.map(i => i.id))
    const fresh = (res.data.list || []).filter(i => !seenIds.has(i.id))
    list.value.push(...fresh)
    if (list.value.length >= res.data.total) finished.value = true
    else pageNum.value++
  } catch (e) { finished.value = true } finally { loading.value = false }
}

function onRefresh() { pageNum.value = 1; finished.value = false; loadData() }
function onTabChange() { list.value = []; pageNum.value = 1; finished.value = false; loadData() }
</script>
