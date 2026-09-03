<template>
  <div class="page">
    <van-nav-bar title="消息详情" left-arrow @click-left="$router.replace('/member/message')" />
    <div class="card" v-if="msg">
      <h3 style="margin-bottom: 8px">{{ msg.title }}</h3>
      <van-tag :type="msg.type === '健康提醒' ? 'warning' : 'primary'" size="medium">{{ msg.type }}</van-tag>
      <p style="color: #969799; font-size: 12px; margin: 8px 0">{{ msg.createTime }}</p>
      <p style="line-height: 1.8; margin-top: 12px">{{ msg.content }}</p>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import request from '../../../utils/request'

const route = useRoute()
const msg = ref(null)

onMounted(async () => {
  try {
    const res = await request.get(`/api/member/message/${route.params.id}`)
    msg.value = res.data
    // 标记已读
    await request.put(`/api/member/message/read/${route.params.id}`)
  } catch (e) { /* handled */ }
})
</script>
