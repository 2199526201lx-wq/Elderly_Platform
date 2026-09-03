<template>
  <div class="chat-page">
    <van-nav-bar title="AI 健康咨询" left-arrow @click-left="$router.replace('/member/home')" />

    <!-- 会话选择 -->
    <div class="session-bar" @click="showSessions = true">
      <span>{{ currentSessionName || '选择会话' }}</span>
      <van-icon name="arrow" />
    </div>

    <!-- 消息区域 -->
    <div class="chat-messages" ref="msgContainer">
      <div v-for="msg in messages" :key="msg.messageId" :class="['msg', msg.role === 'USER' ? 'msg-user' : 'msg-ai']">
        <div class="msg-bubble">{{ msg.content }}</div>
      </div>
      <div v-if="aiTyping" class="msg msg-ai">
        <div class="msg-bubble">{{ streamingText }}<span class="cursor">|</span></div>
      </div>
    </div>

    <!-- 输入区域 -->
    <div class="chat-input">
      <van-field v-model="inputText" placeholder="输入您的问题..." @keyup.enter="sendMessage" :disabled="aiTyping">
        <template #button>
          <van-button size="small" type="primary" :disabled="!inputText.trim() || aiTyping" @click="sendMessage">发送</van-button>
        </template>
      </van-field>
    </div>

    <!-- 会话列表弹窗 -->
    <van-popup v-model:show="showSessions" position="bottom" round :style="{ maxHeight: '60vh' }">
      <div style="padding: 16px">
        <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 12px">
          <h4>我的会话</h4>
          <van-button size="mini" type="primary" @click="createSession">+ 新会话</van-button>
        </div>
        <van-cell-group>
          <van-cell
            v-for="s in sessions" :key="s.sessionID"
            :title="s.sessionName || '新会话'"
            :label="s.updateTime"
            clickable
            @click="switchSession(s.sessionID, s.sessionName)"
          />
        </van-cell-group>
        <van-empty v-if="!sessions.length" description="暂无会话，点击上方新建" image="search" />
      </div>
    </van-popup>
  </div>
</template>

<script setup>
import { ref, onMounted, nextTick } from 'vue'
import { showToast } from 'vant'
import request from '../../utils/request'
import { useUserStore } from '../../stores/user'

const userStore = useUserStore()

const sessions = ref([])
const currentSessionId = ref(null)
const currentSessionName = ref('')
const messages = ref([])
const inputText = ref('')
const aiTyping = ref(false)
const streamingText = ref('')
const showSessions = ref(false)
const msgContainer = ref(null)
let msgIdCounter = 0

function scrollToBottom() {
  nextTick(() => {
    if (msgContainer.value) msgContainer.value.scrollTop = msgContainer.value.scrollHeight
  })
}

async function loadSessions() {
  try {
    const res = await request.get('/api/member/ai/sessionlist')
    sessions.value = res.data
  } catch (e) { /* handled */ }
}

async function createSession() {
  try {
    const res = await request.post('/api/member/ai/session')
    currentSessionId.value = res.data.id
    currentSessionName.value = '新会话'
    messages.value = []
    showSessions.value = false
    await loadSessions()
  } catch (e) { /* handled */ }
}

async function switchSession(id, name) {
  currentSessionId.value = id
  currentSessionName.value = name || '新会话'
  showSessions.value = false
  try {
    const res = await request.get(`/api/member/ai/messages/${id}`)
    messages.value = res.data
    scrollToBottom()
  } catch (e) { /* handled */ }
}

async function sendMessage() {
  const text = inputText.value.trim()
  if (!text || !currentSessionId.value || aiTyping.value) return

  // 添加用户消息到界面（使用递增计数器，避免 key 冲突）
  const userMsgId = ++msgIdCounter
  messages.value.push({ messageId: userMsgId, role: 'USER', content: text })
  inputText.value = ''
  aiTyping.value = true
  streamingText.value = ''
  scrollToBottom()

  try {
    const response = await fetch('/api/member/ai/stream', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        'Authorization': `Bearer ${userStore.accessToken}`,
      },
      body: JSON.stringify({ sessionId: currentSessionId.value, content: text }),
    })

    if (!response.ok) {
      throw new Error('AI 服务响应异常')
    }

    const reader = response.body.getReader()
    const decoder = new TextDecoder()
    let fullText = ''
    let buffer = ''

    while (true) {
      const { done, value } = await reader.read()
      if (done) break
      buffer += decoder.decode(value, { stream: true })

      // 按 SSE 事件分割（\n\n 分隔事件）
      const events = buffer.split('\n\n')
      buffer = events.pop() || ''

      for (const event of events) {
        const lines = event.split('\n')
        let eventName = ''
        let data = ''
        for (const line of lines) {
          if (line.startsWith('event:')) {
            eventName = line.substring(6).trim()
          } else if (line.startsWith('data:')) {
            data = line.substring(5).trim()
          }
        }

        if (eventName === 'message' && data) {
          fullText += data
          streamingText.value = fullText
          scrollToBottom()
        } else if (eventName === 'done') {
          // done 事件：AI 回复结束
        } else if (eventName === 'error') {
          showToast(data || 'AI 服务出错')
          aiTyping.value = false
          streamingText.value = ''
        }
      }
    }

    // AI 回复完成，只插入一次
    if (fullText) {
      const aiMsgId = ++msgIdCounter
      messages.value.push({ messageId: aiMsgId, role: 'AI', content: fullText })
    }
  } catch (e) {
    showToast('AI 响应失败，请重试')
  } finally {
    aiTyping.value = false
    streamingText.value = ''
    scrollToBottom()
    await loadSessions()
  }
}

onMounted(async () => {
  await loadSessions()
  if (sessions.value.length > 0) {
    switchSession(sessions.value[0].sessionID, sessions.value[0].sessionName)
  } else {
    showSessions.value = true
  }
})
</script>

<style scoped>
.chat-page {
  display: flex;
  flex-direction: column;
  height: 100vh;
  background: #eef5ee;
}
.session-bar {
  background: #fff;
  padding: 10px 16px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 14px;
  color: #3b9e5a;
  border-bottom: 1px solid #eee;
}
.chat-messages {
  flex: 1;
  overflow-y: auto;
  padding: 12px 16px;
}
.msg {
  margin-bottom: 12px;
  display: flex;
}
.msg-user {
  justify-content: flex-end;
}
.msg-ai {
  justify-content: flex-start;
}
.msg-bubble {
  max-width: 80%;
  padding: 10px 14px;
  border-radius: 12px;
  font-size: 15px;
  line-height: 1.6;
  word-break: break-word;
}
.msg-user .msg-bubble {
  background: #3b9e5a;
  color: #fff;
  border-bottom-right-radius: 4px;
}
.msg-ai .msg-bubble {
  background: #fff;
  color: #323233;
  border-bottom-left-radius: 4px;
}
.cursor {
  animation: blink 0.8s infinite;
}
@keyframes blink {
  0%, 100% { opacity: 1; }
  50% { opacity: 0; }
}
.chat-input {
  background: #fff;
  border-top: 1px solid #eee;
  padding: 8px 0;
}
</style>
