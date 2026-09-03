<template>
  <div class="page-container">
    <div class="search-bar">
      <el-input v-model="query.phone" placeholder="手机号" clearable style="width: 180px" @clear="loadData" @keyup.enter="loadData" />
      <el-input v-model="query.realName" placeholder="真实姓名" clearable style="width: 180px" @clear="loadData" @keyup.enter="loadData" />
      <el-select v-model="query.status" placeholder="状态" clearable style="width: 120px" @change="loadData">
        <el-option label="启用" value="启用" /><el-option label="禁用" value="禁用" />
      </el-select>
      <el-button type="primary" @click="loadData">搜索</el-button>
    </div>

    <el-table :data="list" border stripe>
      <el-table-column prop="id" label="ID" width="70" />
      <el-table-column prop="phone" label="手机号" width="130" />
      <el-table-column prop="realName" label="姓名" width="120" />
      <el-table-column prop="gender" label="性别" width="70" />
      <el-table-column prop="memberLevel" label="等级" width="100" />
      <el-table-column prop="points" label="积分" width="80" />
      <el-table-column prop="status" label="状态" width="80">
        <template #default="{ row }">
          <el-tag :type="row.status === '启用' ? 'success' : 'danger'" size="small">{{ row.status }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="createTime" label="注册时间" width="180" />
      <el-table-column label="操作" fixed="right" width="280">
        <template #default="{ row }">
          <el-button size="small" @click="$router.push(`/admin/members/${row.id}`)">详情</el-button>
          <el-button size="small" :type="row.status === '启用' ? 'danger' : 'success'" @click="toggleStatus(row)">
            {{ row.status === '启用' ? '禁用' : '启用' }}
          </el-button>
          <el-button size="small" @click="showPointsDialog(row)">调整积分</el-button>
          <el-button size="small" @click="showResetDialog(row)">重置密码</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-pagination style="margin-top: 16px; justify-content: flex-end"
      v-model:current-page="pageNum" :page-size="10" :total="total" layout="total, prev, pager, next"
      @current-change="loadData" />

    <!-- 调整积分弹窗 -->
    <el-dialog v-model="pointsVisible" title="调整积分" width="400px">
      <el-form label-width="80px">
        <el-form-item label="会员">
          <span>{{ currentMember?.phone }} ({{ currentMember?.realName }})</span>
        </el-form-item>
        <el-form-item label="积分变动">
          <el-input-number v-model="pointsForm.amount" :precision="0" />
          <span style="margin-left: 8px; color: #909399">正数增加，负数扣减</span>
        </el-form-item>
        <el-form-item label="原因">
          <el-input v-model="pointsForm.reason" placeholder="可选" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="pointsVisible = false">取消</el-button>
        <el-button type="primary" @click="doAdjustPoints" :loading="pointsLoading">确定</el-button>
      </template>
    </el-dialog>

    <!-- 重置密码弹窗 -->
    <el-dialog v-model="resetVisible" title="重置密码" width="400px">
      <el-form label-width="80px">
        <el-form-item label="会员">
          <span>{{ currentMember?.phone }} ({{ currentMember?.realName }})</span>
        </el-form-item>
        <el-form-item label="新密码">
          <el-input v-model="newPassword" placeholder="不填则系统自动生成" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="resetVisible = false">取消</el-button>
        <el-button type="primary" @click="doResetPassword" :loading="resetLoading">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import request from '../../utils/request'

const list = ref([])
const total = ref(0)
const pageNum = ref(1)
const query = ref({ phone: '', realName: '', status: '' })

// 积分调整
const pointsVisible = ref(false)
const pointsLoading = ref(false)
const currentMember = ref(null)
const pointsForm = ref({ amount: 0, reason: '' })

// 重置密码
const resetVisible = ref(false)
const resetLoading = ref(false)
const newPassword = ref('')

async function loadData() {
  try {
    const res = await request.get('/api/admin/members', { params: { pageNum: pageNum.value, pageSize: 10, ...query.value } })
    list.value = res.data.list
    total.value = res.data.total
  } catch (e) { /* handled */ }
}

async function toggleStatus(row) {
  const newStatus = row.status === '启用' ? '禁用' : '启用'
  await ElMessageBox.confirm(`确定${newStatus}该会员？`, '提示')
  try {
    await request.put(`/api/admin/members/status/${row.id}`, null, { params: { status: newStatus } })
    ElMessage.success(`${newStatus}成功`)
    row.status = newStatus
  } catch (e) { /* handled */ }
}

function showPointsDialog(row) {
  currentMember.value = row
  pointsForm.value = { amount: 0, reason: '' }
  pointsVisible.value = true
}

async function doAdjustPoints() {
  pointsLoading.value = true
  try {
    await request.put(`/api/admin/members/points/${currentMember.value.id}`, null, {
      params: { amount: pointsForm.value.amount, reason: pointsForm.value.reason || undefined }
    })
    ElMessage.success('积分调整成功')
    pointsVisible.value = false
    loadData()
  } catch (e) { /* handled */ } finally { pointsLoading.value = false }
}

function showResetDialog(row) {
  currentMember.value = row
  newPassword.value = ''
  resetVisible.value = true
}

async function doResetPassword() {
  resetLoading.value = true
  try {
    await request.put(`/api/admin/members/password/${currentMember.value.id}`, null, {
      params: { newPassword: newPassword.value || undefined }
    })
    ElMessage.success('密码已重置')
    resetVisible.value = false
  } catch (e) { /* handled */ } finally { resetLoading.value = false }
}

onMounted(loadData)
</script>
