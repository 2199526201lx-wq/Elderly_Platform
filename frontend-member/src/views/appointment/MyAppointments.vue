<template>
  <div class="page">
    <van-nav-bar title="我的预约" left-arrow @click-left="$router.replace('/member/home')" />

    <van-tabs v-model:active="activeTab" @change="onTabChange">
      <van-tab title="全部" name="" />
      <van-tab title="待确认" name="待确认" />
      <van-tab title="已确认" name="已确认" />
      <van-tab title="已完成" name="已完成" />
      <van-tab title="已取消" name="已取消" />
    </van-tabs>

    <van-list v-model:loading="loading" :finished="finished" finished-text="没有更多了" @load="loadData">
      <div class="card" v-for="item in list" :key="item.id">
        <div style="display: flex; justify-content: space-between">
          <span style="font-size: 12px; color: #969799">{{ item.createTime }}</span>
          <van-tag :type="statusColor(item.status)">{{ item.status }}</van-tag>
        </div>
        <p style="margin-top: 8px">套餐 #{{ item.packageId }} <span v-if="item.packageName" style="color: #3b9e5a">({{ item.packageName }})</span></p>
        <div v-if="item.status === '待确认' || item.status === '已确认'" style="margin-top: 8px">
          <van-button size="mini" plain type="danger" @click="handleCancel(item)">取消预约</van-button>
        </div>
        <div v-if="item.reportUrl" style="margin-top: 8px">
          <van-tag type="success">体检报告已上传</van-tag>
        </div>
      </div>
    </van-list>

    <van-empty v-if="!loading && finished && list.length === 0" description="您当前还没有预约记录">
      <van-button round type="primary" to="/member/appointment/packages">快去预约体检吧</van-button>
    </van-empty>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { showConfirmDialog, showToast } from 'vant'
import request from '../../utils/request'

const list = ref([])
const loading = ref(false)
const finished = ref(false)
const activeTab = ref('')
const pageNum = ref(1)

function statusColor(s) {
  return { '待确认': 'warning', '已确认': 'primary', '已完成': 'success', '已取消': 'default' }[s] || 'default'
}

async function loadData() {
  loading.value = true
  try {
    const params = { pageNum: pageNum.value, pageSize: 10 }
    if (activeTab.value) params.status = activeTab.value
    const res = await request.get('/api/member/appointment/list', { params })
    // 按 id 去重，防止因分页或重复请求导致重复预约
    const seenIds = new Set(list.value.map(i => i.id))
    const fresh = (res.data.list || []).filter(i => !seenIds.has(i.id))
    list.value.push(...fresh)
    if (list.value.length >= res.data.total) finished.value = true
    else pageNum.value++
  } catch (e) { finished.value = true } finally { loading.value = false }
}

function onTabChange() { list.value = []; pageNum.value = 1; finished.value = false; loadData() }

async function handleCancel(item) {
  try {
    await showConfirmDialog({ title: '确认取消', message: '取消后积分将退回到您的账户' })
    await request.post(`/api/member/appointment/${item.id}/cancel`)
    showToast('已取消')
    item.status = '已取消'
  } catch (e) { /* handled or cancelled */ }
}
</script>
