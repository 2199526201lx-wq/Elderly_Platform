<template>
  <div class="page-container">
    <div class="search-bar">
      <el-button type="primary" @click="showSendDialog = true">发送消息</el-button>
      <el-button @click="showBatchDialog = true">批量发送</el-button>
      <el-select v-model="query.type" placeholder="类型筛选" clearable style="width: 140px" @change="loadData">
        <el-option label="预约" value="预约" /><el-option label="活动" value="活动" />
        <el-option label="系统" value="系统" /><el-option label="健康提醒" value="健康提醒" />
      </el-select>
    </div>

    <el-table :data="list" border stripe>
      <el-table-column prop="id" label="ID" width="70" />
      <el-table-column prop="userId" label="用户ID" width="90" />
      <el-table-column prop="title" label="标题" width="200" />
      <el-table-column prop="type" label="类型" width="100">
        <template #default="{ row }"><el-tag size="small">{{ row.type }}</el-tag></template>
      </el-table-column>
      <el-table-column prop="isRead" label="已读" width="70">
        <template #default="{ row }"><el-tag :type="row.isRead ? 'success' : 'warning'" size="small">{{ row.isRead ? '是' : '否' }}</el-tag></template>
      </el-table-column>
      <el-table-column prop="content" label="内容" show-overflow-tooltip />
      <el-table-column prop="createTime" label="时间" width="180" />
      <el-table-column label="操作" width="80" fixed="right">
        <template #default="{ row }">
          <el-button size="small" type="danger" @click="handleDelete(row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-pagination style="margin-top: 16px; justify-content: flex-end"
      v-model:current-page="pageNum" :page-size="10" :total="total" layout="total, prev, pager, next"
      @current-change="loadData" />

    <!-- 单发消息弹窗 -->
    <el-dialog v-model="showSendDialog" title="发送消息" width="460px">
      <el-form label-width="80px">
        <el-form-item label="用户ID"><el-input-number v-model="sendForm.userId" :min="1" /></el-form-item>
        <el-form-item label="标题"><el-input v-model="sendForm.title" /></el-form-item>
        <el-form-item label="内容"><el-input v-model="sendForm.content" type="textarea" rows="3" /></el-form-item>
        <el-form-item label="类型">
          <el-select v-model="sendForm.type">
            <el-option label="预约" value="预约" /><el-option label="活动" value="活动" />
            <el-option label="系统" value="系统" /><el-option label="健康提醒" value="健康提醒" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showSendDialog = false">取消</el-button>
        <el-button type="primary" @click="doSend" :loading="sendLoading">发送</el-button>
      </template>
    </el-dialog>

    <!-- 批量发送弹窗 -->
    <el-dialog v-model="showBatchDialog" title="批量发送消息" width="460px">
      <el-form label-width="80px">
        <el-form-item label="用户ID列表"><el-input v-model="batchForm.userIds" type="textarea" rows="2" placeholder="用逗号分隔，如 1,2,3" /></el-form-item>
        <el-form-item label="标题"><el-input v-model="batchForm.title" /></el-form-item>
        <el-form-item label="内容"><el-input v-model="batchForm.content" type="textarea" rows="3" /></el-form-item>
        <el-form-item label="类型">
          <el-select v-model="batchForm.type">
            <el-option label="系统" value="系统" /><el-option label="活动" value="活动" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showBatchDialog = false">取消</el-button>
        <el-button type="primary" @click="doBatchSend" :loading="batchLoading">发送</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import request from '../../utils/request'

const list = ref([]); const total = ref(0); const pageNum = ref(1); const query = ref({ type: '' })
const showSendDialog = ref(false); const sendLoading = ref(false)
const sendForm = ref({ userId: 0, title: '', content: '', type: '系统' })
const showBatchDialog = ref(false); const batchLoading = ref(false)
const batchForm = ref({ userIds: '', title: '', content: '', type: '系统' })

async function loadData() {
  try {
    const res = await request.get('/api/admin/message', { params: { pageNum: pageNum.value, pageSize: 10, type: query.value.type || undefined } })
    list.value = res.data.list; total.value = res.data.total
  } catch (e) { /* handled */ }
}

async function doSend() {
  sendLoading.value = true
  try {
    await request.post('/api/admin/message', null, { params: sendForm.value })
    ElMessage.success('发送成功'); showSendDialog.value = false; loadData()
  } catch (e) { /* handled */ } finally { sendLoading.value = false }
}

async function doBatchSend() {
  batchLoading.value = true
  try {
    await request.post('/api/admin/message/batch', { userIds: batchForm.value.userIds.split(',').map(Number), title: batchForm.value.title, content: batchForm.value.content, type: batchForm.value.type })
    ElMessage.success('批量发送成功'); showBatchDialog.value = false; loadData()
  } catch (e) { /* handled */ } finally { batchLoading.value = false }
}

async function handleDelete(row) {
  await ElMessageBox.confirm('确定删除该消息？', '提示', { type: 'warning' })
  try { await request.delete(`/api/admin/message/${row.id}`); ElMessage.success('删除成功'); loadData() } catch (e) { /* handled */ }
}

onMounted(loadData)
</script>
