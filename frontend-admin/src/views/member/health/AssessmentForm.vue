<template>
  <div class="page">
    <van-nav-bar title="答题" left-arrow @click-left="$router.replace('/member/health/assessment')" />

    <div v-if="questions.length" style="padding: 12px 16px">
      <div v-for="(q, qi) in questions" :key="q.id" class="card">
        <p style="font-weight: 600; margin-bottom: 12px">{{ qi + 1 }}. {{ q.content }}</p>

        <!-- 单选 -->
        <van-radio-group v-if="q.type === '单选'" v-model="answers[q.id]">
          <van-radio v-for="(opt, oi) in parseOptions(q.options)" :key="oi" :name="opt.text" style="margin-bottom: 10px">
            {{ opt.text }}
          </van-radio>
        </van-radio-group>

        <!-- 多选 -->
        <van-checkbox-group v-else-if="q.type === '多选'" v-model="answers[q.id]">
          <van-checkbox v-for="(opt, oi) in parseOptions(q.options)" :key="oi" :name="opt.text" shape="square" style="margin-bottom: 10px">
            {{ opt.text }}
          </van-checkbox>
        </van-checkbox-group>

        <!-- 文本 -->
        <van-field v-else v-model="answers[q.id]" type="textarea" rows="2" placeholder="请输入您的回答" autosize />
      </div>

      <van-button round block type="primary" @click="handleSubmit" :loading="submitting" style="margin-top: 12px">
        提交评测
      </van-button>
    </div>

    <van-loading v-else style="display: block; margin: 40px auto" />
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { showToast } from 'vant'
import request from '../../../utils/request'

const route = useRoute()
const router = useRouter()
const questions = ref([])
const answers = ref({})
const submitting = ref(false)

function parseOptions(optStr) {
  if (!optStr) return []
  try { return JSON.parse(optStr) } catch { return [] }
}

onMounted(async () => {
  try {
    const res = await request.get(`/api/member/assessment/${route.params.id}`)
    questions.value = res.data
    // 初始化答案
    res.data.forEach(q => {
      if (q.type === '多选') answers.value[q.id] = []
      else answers.value[q.id] = ''
    })
  } catch (e) { /* handled */ }
})

async function handleSubmit() {
  submitting.value = true
  try {
    const answerItems = questions.value.map(q => {
      const item = { questionId: q.id }
      if (q.type === '单选') {
        item.selectedTexts = answers.value[q.id] ? [answers.value[q.id]] : []
      } else if (q.type === '多选') {
        item.selectedTexts = answers.value[q.id] || []
      } else {
        item.text = answers.value[q.id] || ''
        item.selectedTexts = []
      }
      return item
    })

    const res = await request.post('/api/member/assessment/submit', {
      questionnaireId: Number(route.params.id),
      answers: answerItems,
    })

    showToast('评测完成')
    router.replace(`/member/health/assessment/result/${res.data.id}`)
  } catch (e) { /* handled */ } finally { submitting.value = false }
}
</script>
