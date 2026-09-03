<template>
  <div class="page-container">
    <h2 style="margin-bottom: 16px">系统配置</h2>

    <el-table :data="list" border stripe>
      <el-table-column prop="id" label="ID" width="70" />
      <el-table-column prop="configKey" label="配置键" width="260" />
      <el-table-column prop="description" label="说明" width="200" />
      <el-table-column prop="configValue" label="配置值" show-overflow-tooltip />
      <el-table-column label="操作" width="100" fixed="right">
        <template #default="{ row }">
          <el-button size="small" @click="openEdit(row)">编辑</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-dialog v-model="editVisible" title="编辑配置" width="500px">
      <el-form :model="form" label-width="100px">
        <el-form-item label="配置键"><el-input :model-value="form.configKey" disabled /></el-form-item>
        <el-form-item label="说明"><el-input :model-value="form.description" disabled /></el-form-item>
        <el-form-item label="配置值"><el-input v-model="form.configValue" type="textarea" rows="4" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="editVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSave" :loading="saving">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import request from '../../utils/request'

const list = ref([])
const editVisible = ref(false); const saving = ref(false)
const form = ref({ id: null, configKey: '', description: '', configValue: '' })

async function loadData() {
  try { const res = await request.get('/api/admin/config'); list.value = res.data } catch (e) { /* handled */ }
}

function openEdit(row) {
  form.value = { ...row }
  editVisible.value = true
}

async function handleSave() {
  saving.value = true
  try {
    await request.put(`/api/admin/config/${form.value.id}`, null, { params: { configValue: form.value.configValue } })
    ElMessage.success('保存成功'); editVisible.value = false; loadData()
  } catch (e) { /* handled */ } finally { saving.value = false }
}

onMounted(loadData)
</script>
