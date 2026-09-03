<template>
  <div class="page-container">
    <div class="search-bar">
      <el-button type="primary" @click="openDialog()">新增活动</el-button>
      <el-input v-model="query.title" placeholder="活动标题" clearable style="width: 200px" @clear="loadData" @keyup.enter="loadData" />
      <el-select v-model="query.status" placeholder="状态" clearable style="width: 120px" @change="loadData">
        <el-option label="草稿" value="草稿" /><el-option label="报名中" value="报名中" />
        <el-option label="进行中" value="进行中" /><el-option label="已结束" value="已结束" />
      </el-select>
      <el-button @click="loadData">搜索</el-button>
    </div>

    <el-table :data="list" border stripe>
      <el-table-column prop="id" label="ID" width="70" />
      <el-table-column prop="title" label="标题" width="220" />
      <el-table-column prop="location" label="地点" width="140" />
      <el-table-column prop="status" label="状态" width="90">
        <template #default="{ row }">
          <el-tag :type="{ '草稿': 'info', '报名中': 'primary', '进行中': 'success', '已结束': 'warning' }[row.status]" size="small">{{ row.status }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="activityStartTime" label="活动时间" width="170" />
      <el-table-column prop="currentParticipants" label="人数" width="100">
        <template #default="{ row }">{{ row.currentParticipants }}/{{ row.maxParticipants }}</template>
      </el-table-column>
      <el-table-column label="操作" fixed="right" width="240">
        <template #default="{ row }">
          <el-button size="small" @click="openDialog(row)">编辑</el-button>
          <el-button size="small" @click="showRegistrations(row)">报名列表</el-button>
          <el-button size="small" type="danger" @click="handleDelete(row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-pagination style="margin-top: 16px; justify-content: flex-end"
      v-model:current-page="pageNum" :page-size="10" :total="total" layout="total, prev, pager, next"
      @current-change="loadData" />

    <el-dialog v-model="dialogVisible" :title="editingId ? '编辑活动' : '新增活动'" width="640px">
      <el-form :model="form" label-width="120px">
        <el-form-item label="标题"><el-input v-model="form.title" /></el-form-item>
        <el-form-item label="地点"><el-input v-model="form.location" /></el-form-item>
        <el-form-item label="封面URL"><el-input v-model="form.coverUrl" /></el-form-item>
        <el-form-item label="活动内容"><el-input v-model="form.content" type="textarea" rows="3" /></el-form-item>
        <el-form-item label="报名时间">
          <el-date-picker v-model="form.regTime" type="datetimerange" range-separator="至" start-placeholder="开始" end-placeholder="结束" value-format="YYYY-MM-DDTHH:mm:ss" />
        </el-form-item>
        <el-form-item label="活动时间">
          <el-date-picker v-model="form.actTime" type="datetimerange" range-separator="至" start-placeholder="开始" end-placeholder="结束" value-format="YYYY-MM-DDTHH:mm:ss" />
        </el-form-item>
        <el-form-item label="人数上限"><el-input-number v-model="form.maxParticipants" :min="1" /></el-form-item>
        <!-- 状态由报名/活动时间自动推导，管理员只需设置时间即可，不可手动改状态 -->
        <el-form-item label="状态提示">
          <el-tag type="info" size="small">状态由报名/活动时间自动推导</el-tag>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSave" :loading="saving">保存</el-button>
      </template>
    </el-dialog>

    <!-- 报名列表弹窗 -->
    <el-dialog v-model="regVisible" title="报名列表" width="600px">
      <el-table :data="registrations" border stripe size="small">
        <el-table-column prop="userId" label="用户ID" width="90" />
        <el-table-column prop="checkInStatus" label="签到状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.checkInStatus === '已签到' ? 'success' : 'info'" size="small">{{ row.checkInStatus }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="checkInTime" label="签到时间" /><el-table-column prop="createTime" label="报名时间" />
      </el-table>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import request from '../../utils/request'

const list = ref([]); const total = ref(0); const pageNum = ref(1)
const query = ref({ title: '', status: '' })
const dialogVisible = ref(false); const editingId = ref(null); const saving = ref(false)
const regVisible = ref(false); const registrations = ref([])

const form = ref({ title: '', location: '', coverUrl: '', content: '', regTime: null, actTime: null, maxParticipants: 50, status: '草稿' })

function openDialog(row) {
  if (row) {
    editingId.value = row.id
    form.value = { ...row, regTime: [row.registrationStartTime, row.registrationEndTime], actTime: [row.activityStartTime, row.activityEndTime] }
  } else {
    editingId.value = null
    form.value = { title: '', location: '', coverUrl: '', content: '', regTime: null, actTime: null, maxParticipants: 50, status: '草稿' }
  }
  dialogVisible.value = true
}

async function loadData() {
  try {
    const res = await request.get('/api/admin/activity', { params: { pageNum: pageNum.value, pageSize: 10, ...query.value } })
    list.value = res.data.list; total.value = res.data.total
  } catch (e) { /* handled */ }
}

async function handleSave() {
  saving.value = true
  try {
    const data = { ...form.value, registrationStartTime: form.value.regTime?.[0], registrationEndTime: form.value.regTime?.[1], activityStartTime: form.value.actTime?.[0], activityEndTime: form.value.actTime?.[1] }
    delete data.regTime; delete data.actTime
    if (editingId.value) await request.put(`/api/admin/activity/${editingId.value}`, data)
    else await request.post('/api/admin/activity', data)
    ElMessage.success('保存成功'); dialogVisible.value = false; loadData()
  } catch (e) { /* handled */ } finally { saving.value = false }
}

async function handleDelete(row) {
  await ElMessageBox.confirm('确定删除该活动？', '提示', { type: 'warning' })
  try { await request.delete(`/api/admin/activity/${row.id}`); ElMessage.success('删除成功'); loadData() } catch (e) { /* handled */ }
}

async function showRegistrations(row) {
  try {
    const res = await request.get(`/api/admin/activity/${row.id}/registrations`)
    registrations.value = res.data; regVisible.value = true
  } catch (e) { /* handled */ }
}

onMounted(loadData)
</script>
