<template>
  <div class="page">
    <van-nav-bar title="健康记录" left-arrow @click-left="$router.replace('/member/profile')" />
    <div style="padding: 12px 16px">
      <van-button round block type="primary" icon="plus" to="/member/health/record">新增健康记录</van-button>
    </div>
    <van-pull-refresh v-model="refreshing" @refresh="onRefresh">
      <van-list v-model:loading="loading" :finished="finished" finished-text="没有更多了" @load="loadData">
        <div class="card" v-for="item in list" :key="item.id">
          <p style="font-size: 12px; color: #969799; margin-bottom: 8px">{{ item.recordedTime }}</p>
          <van-grid :column-num="3" :border="false" style="padding: 0">
            <van-grid-item>
              <template #default>
                <div style="text-align: center">
                  <div style="font-size: 16px; font-weight: 600; color: #323233">{{ item.systolic != null ? `${item.systolic}/${item.diastolic}` : '-' }}</div>
                  <div style="font-size: 12px; color: #969799; margin-top: 2px">血压 mmHg</div>
                </div>
              </template>
            </van-grid-item>
            <van-grid-item>
              <template #default>
                <div style="text-align: center">
                  <div style="font-size: 16px; font-weight: 600; color: #323233">{{ item.bloodSugar != null ? String(item.bloodSugar) : '-' }}</div>
                  <div style="font-size: 12px; color: #969799; margin-top: 2px">血糖 mmol/L</div>
                </div>
              </template>
            </van-grid-item>
            <van-grid-item>
              <template #default>
                <div style="text-align: center">
                  <div style="font-size: 16px; font-weight: 600; color: #323233">{{ item.heartRate != null ? String(item.heartRate) : '-' }}</div>
                  <div style="font-size: 12px; color: #969799; margin-top: 2px">心率 次/分</div>
                </div>
              </template>
            </van-grid-item>
            <van-grid-item>
              <template #default>
                <div style="text-align: center">
                  <div style="font-size: 16px; font-weight: 600; color: #323233">{{ item.weight != null ? String(item.weight) : '-' }}</div>
                  <div style="font-size: 12px; color: #969799; margin-top: 2px">体重 kg</div>
                </div>
              </template>
            </van-grid-item>
            <van-grid-item>
              <template #default>
                <div style="text-align: center">
                  <div style="font-size: 16px; font-weight: 600; color: #323233">{{ item.bmi != null ? String(item.bmi) : '-' }}</div>
                  <div style="font-size: 12px; color: #969799; margin-top: 2px">BMI</div>
                </div>
              </template>
            </van-grid-item>
          </van-grid>
        </div>
      </van-list>

      <van-empty v-if="!loading && finished && list.length === 0" description="暂无健康记录">
        <van-button round type="primary" to="/member/health/record">快去录入健康数据吧</van-button>
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
    const res = await request.get('/api/member/healthrecord/list', { params: { pageNum: pageNum.value, pageSize: 10 } })
    if (refreshing.value) { list.value = []; refreshing.value = false }
    list.value.push(...res.data.list)
    if (list.value.length >= res.data.total) finished.value = true
    else pageNum.value++
  } catch (e) { finished.value = true } finally { loading.value = false }
}

function onRefresh() { pageNum.value = 1; finished.value = false; loadData() }
</script>
