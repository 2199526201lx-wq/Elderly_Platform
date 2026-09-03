<template>
  <div class="page-container">
    <div class="search-bar">
      <el-select v-model="query.status" placeholder="状态筛选" clearable style="width: 140px" @change="loadData">
        <el-option label="待确认" value="待确认" /><el-option label="已确认" value="已确认" />
        <el-option label="已完成" value="已完成" /><el-option label="已取消" value="已取消" />
      </el-select>
      <el-button type="primary" @click="loadData">搜索</el-button>
    </div>

    <el-table :data="list" border stripe>
      <el-table-column prop="id" label="ID" width="70" />
      <el-table-column prop="userId" label="会员ID" width="90" />
      <el-table-column prop="packageId" label="套餐ID" width="90" />
      <el-table-column prop="slotId" label="时段ID" width="90" />
      <el-table-column prop="status" label="状态" width="100">
        <template #default="{ row }">
          <el-tag :type="{ '待确认': 'warning', '已确认': 'primary', '已完成': 'success', '已取消': 'info' }[row.status]" size="small">{{ row.status }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="reportUrl" label="报告" width="100">
        <template #default="{ row }">
          <el-tag v-if="row.reportUrl" type="success" size="small">已上传</el-tag>
          <span v-else style="color: #909399">未上传</span>
        </template>
      </el-table-column>
      <el-table-column prop="createTime" label="创建时间" width="180" />
      <el-table-column label="操作" fixed="right" width="260">
        <template #default="{ row }">
          <el-button v-if="row.status === '待确认'" size="small" type="success" @click="handleConfirm(row)">确认</el-button>
          <el-button v-if="row.status !== '已取消' && row.status !== '已完成'" size="small" type="danger" @click="handleCancel(row)">取消</el-button>
          <el-button v-if="row.status === '已确认' && !row.reportUrl" size="small" type="primary" @click="openReportDialog(row)">上传报告</el-button>
          <el-button v-if="row.reportUrl" size="small" type="info" @click="window.open(row.reportUrl)">查看报告</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-pagination style="margin-top: 16px; justify-content: flex-end"
      v-model:current-page="pageNum" :page-size="10" :total="total" layout="total, prev, pager, next"
      @current-change="loadData" />

    <!-- 上传报告弹窗：填写报告文件链接 -->
    <el-dialog v-model="reportVisible" title="上传体检报告" width="460px">
      <el-form label-width="90px">
        <el-form-item label="预约记录">
          <span>{{ reportRow?.id }}（套餐ID {{ reportRow?.packageId }}）</span>
        </el-form-item>
        <el-form-item label="报告链接">
          <el-input v-model="reportUrl" placeholder="输入报告文件的访问地址（/report/xxx.pdf 或完整URL）" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="reportVisible = false">取消</el-button>
        <el-button type="primary" @click="doUploadReport" :loading="reportLoading">确认上传</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import request from '../../utils/request'
import { useUserStore } from '../../stores/user'

const userStore = useUserStore()
const list = ref([])
const total = ref(0)
const pageNum = ref(1)
const query = ref({ status: '' })

// 上传报告弹窗
const reportVisible = ref(false)
const reportLoading = ref(false)
const reportRow = ref(null)
const reportUrl = ref('')

function openReportDialog(row) {
  reportRow.value = row
  reportUrl.value = row.reportUrl || ''
  reportVisible.value = true
}

async function doUploadReport() {
  if (!reportUrl.value || !String(reportUrl.value).trim()) {
    ElMessage.warning('请输入报告链接')
    return
  }
  reportLoading.value = true
  try {
    await request.post(`/api/admin/appointment/${reportRow.value.id}/report`, null, {
      params: { reportUrl: reportUrl.value.trim(), adminId: userStore.userInfo?.id },
    })
    ElMessage.success('上传成功')
    reportVisible.value = false
    loadData()
  } catch (e) { /* handled */ } finally { reportLoading.value = false }
}

async function loadData() {
  try {
    const res = await request.get('/api/admin/appointment', { params: { pageNum: pageNum.value, pageSize: 10, status: query.value.status || undefined } })
    list.value = res.data.list; total.value = res.data.total
  } catch (e) { /* handled */ }
}

async function handleConfirm(row) {
  try { await request.put(`/api/admin/appointment/${row.id}/confirm`); ElMessage.success('已确认'); loadData() } catch (e) { /* handled */ }
}

async function handleCancel(row) {
  await ElMessageBox.confirm('确定取消该预约？', '提示', { type: 'warning' })
  try { await request.put(`/api/admin/appointment/${row.id}/cancel`); ElMessage.success('已取消'); loadData() } catch (e) { /* handled */ }
}

onMounted(loadData)
</script>
