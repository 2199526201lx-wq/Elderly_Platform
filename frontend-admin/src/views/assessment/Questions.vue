<template>
  <div class="page-container">
    <el-page-header @back="$router.replace('/admin/assessment')" title="返回" :content="`题目管理 — 问卷 #${questionnaireId}`" />

    <div class="search-bar" style="margin-top: 16px">
      <el-button type="primary" @click="openDialog()">添加题目</el-button>
    </div>

    <el-table :data="list" border stripe>
      <el-table-column prop="id" label="ID" width="70" />
      <el-table-column prop="sortOrder" label="排序" width="70" />
      <el-table-column prop="content" label="题目内容" show-overflow-tooltip />
      <el-table-column prop="type" label="类型" width="80" />
      <el-table-column prop="scoreMode" label="计分模式" width="100">
        <template #default="{ row }">
          <el-tag :type="row.scoreMode === '计分' ? 'primary' : 'info'" size="small">{{ row.scoreMode }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="maxScore" label="满分" width="70" />
      <el-table-column label="操作" fixed="right" width="160">
        <template #default="{ row }">
          <el-button size="small" @click="openDialog(row)">编辑</el-button>
          <el-button size="small" type="danger" @click="handleDelete(row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-dialog v-model="dialogVisible" :title="editingId ? '编辑题目' : '添加题目'" width="680px">
      <el-form :model="form" label-width="100px">
        <el-form-item label="题目内容"><el-input v-model="form.content" /></el-form-item>
        <el-form-item label="类型">
          <el-select v-model="form.type" @change="onTypeChange">
            <el-option label="单选" value="单选" /><el-option label="多选" value="多选" /><el-option label="文本" value="文本" />
          </el-select>
        </el-form-item>
        <el-form-item label="计分模式">
          <el-select v-model="form.scoreMode" :disabled="form.type === '文本'">
            <el-option label="计分" value="计分" /><el-option label="非计分" value="非计分" />
          </el-select>
        </el-form-item>
        <el-form-item label="满分">
          <el-input-number v-model="form.maxScore" :min="0" />
          <span style="margin-left: 8px; font-size: 12px; color: #909399">及格分 {{ passScore }}，满分必须大于及格分</span>
        </el-form-item>
        <el-form-item label="排序"><el-input-number v-model="form.sortOrder" :min="0" /></el-form-item>

        <!-- 选择题选项编辑器 -->
        <el-form-item label="选项" v-if="form.type !== '文本'">
          <div style="width: 100%">
            <div
              v-for="(opt, idx) in form.options"
              :key="idx"
              style="display: flex; align-items: center; gap: 8px; margin-bottom: 8px"
            >
              <span style="width: 32px; color: #2e7d32; font-weight: 700">A{{ String.fromCharCode(65 + idx) }}</span>
              <el-input v-model="opt.text" placeholder="选项内容" style="flex: 1" />
              <el-input-number v-model="opt.score" :min="0" placeholder="分值" style="width: 110px" />
              <el-input v-model="opt.meaning" placeholder="选项含义（可选）" style="flex: 1" />
              <el-button size="small" type="danger" :disabled="form.options.length <= 2" @click="removeOption(idx)">删除</el-button>
            </div>
            <el-button size="small" type="primary" plain @click="addOption">+ 添加选项</el-button>
          </div>
        </el-form-item>
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
import { useRoute } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import request from '../../utils/request'

const route = useRoute()
const questionnaireId = route.params.id

const list = ref([])
const dialogVisible = ref(false)
const editingId = ref(null)
const saving = ref(false)
const passScore = ref(60)

const form = ref({
  content: '',
  type: '单选',
  scoreMode: '计分',
  maxScore: 10,
  sortOrder: 0,
  options: [],
})

function onTypeChange() {
  if (form.value.type === '文本') {
    form.value.scoreMode = '非计分'
    form.value.options = []
  } else {
    form.value.scoreMode = form.value.scoreMode || '计分'
  }
}

function addOption() {
  form.value.options.push({ text: '', score: 0, meaning: '' })
}

function removeOption(idx) {
  if (form.value.options.length <= 2) {
    ElMessage.warning('选项不得低于两个')
    return
  }
  form.value.options.splice(idx, 1)
}

function openDialog(row) {
  if (row) {
    editingId.value = row.id
    const opts = parseOptions(row.options)
    form.value = {
      content: row.content || '',
      type: row.type || '单选',
      scoreMode: row.scoreMode || '计分',
      maxScore: row.maxScore ?? 10,
      sortOrder: row.sortOrder ?? 0,
      options: opts,
    }
    if (row.type === '文本') form.value.options = []
  } else {
    editingId.value = null
    form.value = { content: '', type: '单选', scoreMode: '计分', maxScore: 10, sortOrder: 0, options: [ { text: '', score: 0, meaning: '' }, { text: '', score: 0, meaning: '' } ] }
  }
  dialogVisible.value = true
}

function parseOptions(str) {
  if (!str) return [ { text: '', score: 0, meaning: '' }, { text: '', score: 0, meaning: '' } ]
  try {
    const arr = typeof str === 'string' ? JSON.parse(str) : str
    if (!Array.isArray(arr)) return [ { text: '', score: 0, meaning: '' }, { text: '', score: 0, meaning: '' } ]
    return arr.map(o => ({ text: o.text ?? '', score: o.score ?? 0, meaning: o.meaning ?? '' }))
  } catch (e) {
    ElMessage.warning('选项数据格式有误')
    return [ { text: '', score: 0, meaning: '' }, { text: '', score: 0, meaning: '' } ]
  }
}

async function loadData() {
  try {
    const res = await request.get(`/api/admin/assessment/${questionnaireId}/questions`)
    list.value = res.data || []
  } catch (e) { /* handled */ }
  try {
    const q = await request.get(`/api/admin/assessment/${questionnaireId}`)
    passScore.value = q.data?.passScore ?? 60
  } catch (e) { /* ignore */ }
}

async function handleSave() {
  // 满分必须大于及格分
  if (Number(form.value.maxScore) <= Number(passScore.value)) {
    ElMessage.warning(`满分必须大于及格分（当前及格分 ${passScore.value}），无法保存`)
    return
  }
  // 选择题至少两个选项
  if (form.value.type !== '文本') {
    if (form.value.options.length < 2) {
      ElMessage.warning('选项不得低于两个')
      return
    }
    for (const o of form.value.options) {
      if (!o.text || !String(o.text).trim()) {
        ElMessage.warning('选项内容不能为空')
        return
      }
    }
  }
  saving.value = true
  try {
    const data = { ...form.value }
    delete data.options
    // 选项按字母顺序序列化，options 存 JSON 数组字符串
    data.options = JSON.stringify(form.value.options)
    if (editingId.value) await request.put(`/api/admin/assessment/question/${editingId.value}`, data)
    else await request.post(`/api/admin/assessment/${questionnaireId}/question`, data)
    ElMessage.success('保存成功'); dialogVisible.value = false; loadData()
  } catch (e) { /* handled */ } finally { saving.value = false }
}

async function handleDelete(row) {
  await ElMessageBox.confirm('确定删除该题目？', '提示', { type: 'warning' })
  try { await request.delete(`/api/admin/assessment/question/${row.id}`); ElMessage.success('删除成功'); loadData() } catch (e) { /* handled */ }
}

onMounted(loadData)
</script>