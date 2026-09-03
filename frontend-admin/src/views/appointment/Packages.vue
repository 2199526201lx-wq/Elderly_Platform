<template>
  <div class="page-container">
    <div class="search-bar">
      <el-button type="primary" @click="showCreateDialog = true">新增套餐</el-button>
    </div>

    <el-table :data="list" border stripe>
      <el-table-column prop="id" label="ID" width="70" />
      <el-table-column prop="name" label="套餐名称" width="200" />
      <el-table-column prop="price" label="价格(积分)" width="110" />
      <el-table-column prop="suitablePeople" label="适合人群" width="150" />
      <el-table-column prop="description" label="描述" show-overflow-tooltip />
      <el-table-column prop="status" label="状态" width="80">
        <template #default="{ row }">
          <el-tag :type="row.status === '启用' ? 'success' : 'danger'" size="small">{{ row.status }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" fixed="right" width="200">
        <template #default="{ row }">
          <el-button size="small" @click="editItem(row)">编辑</el-button>
          <el-button size="small" type="danger" @click="handleDelete(row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-pagination style="margin-top: 16px; justify-content: flex-end"
      v-model:current-page="pageNum" :page-size="10" :total="total" layout="total, prev, pager, next"
      @current-change="loadData" />

    <!-- 新增/编辑弹窗 -->
    <el-dialog v-model="showCreateDialog" :title="editingId ? '编辑套餐' : '新增套餐'" width="560px" @closed="resetForm">
      <el-form :model="form" label-width="100px">
        <el-form-item label="套餐名称"><el-input v-model="form.name" /></el-form-item>
        <el-form-item label="价格(积分)"><el-input-number v-model="form.price" :min="0" /></el-form-item>
        <el-form-item label="适合人群"><el-input v-model="form.suitablePeople" /></el-form-item>
        <el-form-item label="描述"><el-input v-model="form.description" type="textarea" rows="3" /></el-form-item>
        <el-form-item label="封面URL"><el-input v-model="form.coverUrl" /></el-form-item>
        <el-form-item label="检查项目"><el-input v-model="form.itemsStr" type="textarea" rows="2" placeholder="JSON 数组，如 [&quot;血常规&quot;, &quot;尿常规&quot;]" /></el-form-item>
        <el-form-item label="状态">
          <el-select v-model="form.status"><el-option label="启用" value="启用" /><el-option label="禁用" value="禁用" /></el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showCreateDialog = false">取消</el-button>
        <el-button type="primary" @click="handleSave" :loading="saving">保存</el-button>
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
const showCreateDialog = ref(false)
const editingId = ref(null)
const saving = ref(false)

const form = ref({ name: '', price: 0, suitablePeople: '', description: '', coverUrl: '', itemsStr: '', status: '启用' })

function resetForm() { editingId.value = null; form.value = { name: '', price: 0, suitablePeople: '', description: '', coverUrl: '', itemsStr: '', status: '启用' } }
function editItem(row) {
  editingId.value = row.id
  form.value = { ...row, itemsStr: row.items || '' }
  showCreateDialog.value = true
}

async function loadData() {
  try {
    const res = await request.get('/api/admin/appointment/package', { params: { pageNum: pageNum.value, pageSize: 10 } })
    list.value = res.data.list; total.value = res.data.total
  } catch (e) { /* handled */ }
}

async function handleSave() {
  saving.value = true
  try {
    const data = { ...form.value, items: form.value.itemsStr }
    delete data.itemsStr
    if (editingId.value) {
      await request.put(`/api/admin/appointment/package/${editingId.value}`, data)
    } else {
      await request.post('/api/admin/appointment/package', data)
    }
    ElMessage.success('保存成功'); showCreateDialog.value = false; loadData()
  } catch (e) { /* handled */ } finally { saving.value = false }
}

async function handleDelete(row) {
  await ElMessageBox.confirm('确定删除该套餐？', '提示', { type: 'warning' })
  try { await request.delete(`/api/admin/appointment/package/${row.id}`); ElMessage.success('删除成功'); loadData() } catch (e) { /* handled */ }
}

onMounted(loadData)
</script>
