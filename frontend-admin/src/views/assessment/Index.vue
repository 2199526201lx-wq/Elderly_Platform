<template>
  <div class="page-container">
    <div class="search-bar">
      <el-button type="primary" @click="openDialog()">新增问卷</el-button>
      <el-select v-model="query.status" placeholder="状态筛选" clearable style="width: 140px" @change="loadData">
        <el-option label="草稿" value="草稿" /><el-option label="已发布" value="已发布" />
      </el-select>
    </div>

    <el-table :data="list" border stripe>
      <el-table-column prop="id" label="ID" width="70" />
      <el-table-column prop="title" label="标题" width="250" />
      <el-table-column prop="totalScore" label="满分" width="80" />
      <el-table-column prop="passScore" label="及格线" width="80" />
      <el-table-column prop="status" label="状态" width="100">
        <template #default="{ row }">
          <el-tag :type="row.status === '已发布' ? 'success' : 'info'" size="small">{{ row.status }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="description" label="描述" show-overflow-tooltip />
      <el-table-column prop="createTime" label="创建时间" width="180" />
      <el-table-column label="操作" fixed="right" width="300">
        <template #default="{ row }">
          <el-button size="small" @click="openDialog(row)">编辑</el-button>
          <el-button size="small" type="primary" @click="$router.push(`/admin/assessment/${row.id}/questions`)">管理题目</el-button>
          <el-button size="small" :type="row.status === '已发布' ? 'warning' : 'success'" @click="toggleStatus(row)">
            {{ row.status === '已发布' ? '下架' : '发布' }}
          </el-button>
          <el-button size="small" type="danger" @click="handleDelete(row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-pagination style="margin-top: 16px; justify-content: flex-end"
      v-model:current-page="pageNum" :page-size="10" :total="total" layout="total, prev, pager, next"
      @current-change="loadData" />

    <el-dialog v-model="dialogVisible" :title="editingId ? '编辑问卷' : '新增问卷'" width="560px">
      <el-form :model="form" label-width="100px">
        <el-form-item label="标题"><el-input v-model="form.title" /></el-form-item>
        <el-form-item label="描述"><el-input v-model="form.description" type="textarea" rows="2" /></el-form-item>
        <el-form-item label="满分"><el-input-number v-model="form.totalScore" :min="0" /></el-form-item>
        <el-form-item label="及格线"><el-input-number v-model="form.passScore" :min="0" :max="100" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSave" :loading="saving">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import request from '../../utils/request'

const list = ref([]); const total = ref(0); const pageNum = ref(1); const query = ref({ status: '' })
const dialogVisible = ref(false); const editingId = ref(null); const saving = ref(false)
const form = ref({ title: '', description: '', totalScore: 100, passScore: 60 })

function openDialog(row) {
  if (row) { editingId.value = row.id; form.value = { ...row } }
  else { editingId.value = null; form.value = { title: '', description: '', totalScore: 100, passScore: 60 } }
  dialogVisible.value = true
}

async function loadData() {
  try {
    const res = await request.get('/api/admin/assessment', { params: { pageNum: pageNum.value, pageSize: 10, status: query.value.status || undefined } })
    list.value = res.data.list; total.value = res.data.total
  } catch (e) { /* handled */ }
}

async function handleSave() {
  // 校验：满分必须大于及格分
  const ts = Number(form.value.totalScore)
  const ps = Number(form.value.passScore)
  if (isNaN(ts) || isNaN(ps)) {
    ElMessage.warning('请填写满分和及格线')
    return
  }
  if (ts <= ps) {
    ElMessage.warning('满分必须大于及格分，请调整后保存')
    return
  }
  saving.value = true
  try {
    if (editingId.value) await request.put(`/api/admin/assessment/${editingId.value}`, form.value)
    else await request.post('/api/admin/assessment', form.value)
    ElMessage.success('保存成功'); dialogVisible.value = false; loadData()
  } catch (e) { /* handled */ } finally { saving.value = false }
}

async function toggleStatus(row) {
  const newStatus = row.status === '已发布' ? '草稿' : '已发布'
  try { await request.put(`/api/admin/assessment/status/${row.id}`, null, { params: { status: newStatus } }); ElMessage.success('状态已更新'); loadData() } catch (e) { /* handled */ }
}

async function handleDelete(row) {
  await ElMessageBox.confirm('确定删除该问卷？', '提示', { type: 'warning' })
  try { await request.delete(`/api/admin/assessment/${row.id}`); ElMessage.success('删除成功'); loadData() } catch (e) { /* handled */ }
}

onMounted(loadData)
</script>
