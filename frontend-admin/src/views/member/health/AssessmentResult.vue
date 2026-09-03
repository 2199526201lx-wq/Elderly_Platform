<template>
  <div class="page">
    <van-nav-bar title="评测结果" left-arrow @click-left="$router.replace('/member/health/assessment')" />
    <div v-if="result" style="padding: 12px 16px">
      <!-- 评分卡片 -->
      <div class="card" style="text-align: center">
        <p style="color: #969799; font-size: 13px">AI 综合评分</p>
        <p style="font-size: 48px; font-weight: 700; color: #3b9e5a; margin: 8px 0">{{ result.aiScore }}</p>
        <van-tag :type="result.aiScore >= 90 ? 'success' : result.aiScore >= 60 ? 'primary' : 'danger'" size="large">
          {{ result.aiScore >= 90 ? '优秀' : result.aiScore >= 80 ? '良好' : result.aiScore >= 60 ? '及格' : '需关注' }}
        </van-tag>
        <p style="color: #969799; font-size: 12px; margin-top: 8px">规则分：{{ result.ruleScore }}</p>
      </div>

      <!-- AI 建议 -->
      <div class="card">
        <h4 style="margin-bottom: 8px">AI 建议</h4>
        <p style="line-height: 1.8; color: #323233">{{ result.aiSuggestion }}</p>
      </div>
    </div>
    <van-loading v-else style="display: block; margin: 40px auto" />
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import request from '../../../utils/request'

const route = useRoute()
const result = ref(null)

onMounted(async () => {
  try {
    const res = await request.get(`/api/member/assessment/result/${route.params.id}`)
    result.value = res.data
  } catch (e) { /* handled */ }
})
</script>
