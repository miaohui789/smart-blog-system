<template>
  <div class="system-version-container">
    <el-card shadow="never">
      <template #header>
        <div class="card-header">
          <span>系统版本管理</span>
          <el-button type="primary" :icon="Plus" @click="handleAdd">新增版本</el-button>
        </div>
      </template>

      <!-- 搜索栏 -->
      <el-form :inline="true" :model="queryParams" class="search-form">
        <el-form-item label="关键词">
          <el-input
            v-model="queryParams.keyword"
            placeholder="版本号/版本名称"
            clearable
            @keyup.enter="handleQuery"
          />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :icon="Search" @click="handleQuery">搜索</el-button>
          <el-button :icon="Refresh" @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>

      <!-- 表格 -->
      <el-table :data="tableData" v-loading="loading" border>
        <el-table-column type="selection" width="55" />
        <el-table-column prop="versionNumber" label="版本号" width="120" />
        <el-table-column prop="versionName" label="版本名称" width="150" />
        <el-table-column prop="releaseDate" label="发布日期" width="120" />
        <el-table-column prop="description" label="版本描述" show-overflow-tooltip />
        <el-table-column label="重大版本" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="row.isMajor ? 'danger' : 'info'" size="small">
              {{ row.isMajor ? '是' : '否' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="100" align="center">
          <template #default="{ row }">
            <el-switch
              v-model="row.status"
              :active-value="1"
              :inactive-value="0"
              @change="handleStatusChange(row)"
            />
          </template>
        </el-table-column>
        <el-table-column prop="sortOrder" label="排序" width="80" align="center" />
        <el-table-column label="操作" width="180" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" :icon="Edit" @click="handleEdit(row)">编辑</el-button>
            <el-button link type="danger" :icon="Delete" @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <el-pagination
        v-model:current-page="queryParams.current"
        v-model:page-size="queryParams.size"
        :total="total"
        :page-sizes="[10, 20, 50, 100]"
        layout="total, sizes, prev, pager, next, jumper"
        @size-change="handleQuery"
        @current-change="handleQuery"
        class="pagination"
      />
    </el-card>

    <!-- 新增/编辑对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="dialogTitle"
      width="800px"
      @close="handleDialogClose"
    >
      <el-form :model="form" :rules="rules" ref="formRef" label-width="100px">
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="版本号" prop="versionNumber">
              <el-input v-model="form.versionNumber" placeholder="如：1.0.0" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="版本名称" prop="versionName">
              <el-input v-model="form.versionName" placeholder="如：正式版上线" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="发布日期" prop="releaseDate">
              <el-date-picker
                v-model="form.releaseDate"
                type="date"
                placeholder="选择日期"
                style="width: 100%"
                value-format="YYYY-MM-DD"
              />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="排序" prop="sortOrder">
              <el-input-number v-model="form.sortOrder" :min="0" style="width: 100%" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="版本描述" prop="description">
          <el-input
            v-model="form.description"
            type="textarea"
            :rows="3"
            placeholder="请输入版本描述"
          />
        </el-form-item>
        <el-form-item label="新增功能">
          <div class="feature-list">
            <div v-for="(item, index) in form.featureList" :key="index" class="feature-item">
              <el-input v-model="form.featureList[index]" placeholder="输入功能描述" />
              <el-button :icon="Delete" circle @click="removeFeature(index)" />
            </div>
            <el-button :icon="Plus" @click="addFeature">添加功能</el-button>
          </div>
        </el-form-item>
        <el-form-item label="优化改进">
          <div class="feature-list">
            <div v-for="(item, index) in form.improvementList" :key="index" class="feature-item">
              <el-input v-model="form.improvementList[index]" placeholder="输入改进描述" />
              <el-button :icon="Delete" circle @click="removeImprovement(index)" />
            </div>
            <el-button :icon="Plus" @click="addImprovement">添加改进</el-button>
          </div>
        </el-form-item>
        <el-form-item label="修复问题">
          <div class="feature-list">
            <div v-for="(item, index) in form.bugFixList" :key="index" class="feature-item">
              <el-input v-model="form.bugFixList[index]" placeholder="输入问题描述" />
              <el-button :icon="Delete" circle @click="removeBugFix(index)" />
            </div>
            <el-button :icon="Plus" @click="addBugFix">添加问题</el-button>
          </div>
        </el-form-item>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="重大版本">
              <el-switch v-model="form.isMajor" :active-value="1" :inactive-value="0" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="状态">
              <el-switch v-model="form.status" :active-value="1" :inactive-value="0" />
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit" :loading="submitLoading">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Edit, Delete, Search, Refresh } from '@element-plus/icons-vue'
import {
  getSystemVersionPage,
  addSystemVersion,
  updateSystemVersion,
  deleteSystemVersion,
  updateSystemVersionStatus
} from '@/api/systemVersion'

const loading = ref(false)
const submitLoading = ref(false)
const dialogVisible = ref(false)
const dialogTitle = ref('')
const formRef = ref()
const tableData = ref([])
const total = ref(0)

const queryParams = reactive({
  current: 1,
  size: 10,
  keyword: ''
})

const form = reactive({
  id: null,
  versionNumber: '',
  versionName: '',
  releaseDate: '',
  description: '',
  featureList: [],
  improvementList: [],
  bugFixList: [],
  isMajor: 0,
  status: 1,
  sortOrder: 0
})

const rules = {
  versionNumber: [{ required: true, message: '请输入版本号', trigger: 'blur' }],
  versionName: [{ required: true, message: '请输入版本名称', trigger: 'blur' }],
  releaseDate: [{ required: true, message: '请选择发布日期', trigger: 'change' }]
}

onMounted(() => {
  handleQuery()
})

async function handleQuery() {
  loading.value = true
  try {
    const res = await getSystemVersionPage(queryParams)
    tableData.value = res.data.records
    total.value = res.data.total
  } finally {
    loading.value = false
  }
}

function handleReset() {
  queryParams.keyword = ''
  queryParams.current = 1
  handleQuery()
}

function handleAdd() {
  dialogTitle.value = '新增版本'
  resetForm()
  dialogVisible.value = true
}

function handleEdit(row) {
  dialogTitle.value = '编辑版本'
  form.id = row.id
  form.versionNumber = row.versionNumber
  form.versionName = row.versionName
  form.releaseDate = row.releaseDate
  form.description = row.description
  form.featureList = row.features ? JSON.parse(row.features) : []
  form.improvementList = row.improvements ? JSON.parse(row.improvements) : []
  form.bugFixList = row.bugFixes ? JSON.parse(row.bugFixes) : []
  form.isMajor = row.isMajor
  form.status = row.status
  form.sortOrder = row.sortOrder
  dialogVisible.value = true
}

async function handleDelete(row) {
  try {
    await ElMessageBox.confirm('确定要删除该版本吗？', '提示', {
      type: 'warning'
    })
    await deleteSystemVersion(row.id)
    ElMessage.success('删除成功')
    handleQuery()
  } catch (e) {
    if (e !== 'cancel') {
      ElMessage.error('删除失败')
    }
  }
}

async function handleStatusChange(row) {
  try {
    await updateSystemVersionStatus(row.id, row.status)
    ElMessage.success('状态更新成功')
  } catch (e) {
    row.status = row.status === 1 ? 0 : 1
    ElMessage.error('状态更新失败')
  }
}

async function handleSubmit() {
  await formRef.value.validate()
  submitLoading.value = true
  try {
    const data = {
      versionNumber: form.versionNumber,
      versionName: form.versionName,
      releaseDate: form.releaseDate,
      description: form.description,
      features: JSON.stringify(form.featureList.filter(item => item)),
      improvements: JSON.stringify(form.improvementList.filter(item => item)),
      bugFixes: JSON.stringify(form.bugFixList.filter(item => item)),
      isMajor: form.isMajor,
      status: form.status,
      sortOrder: form.sortOrder
    }
    
    if (form.id) {
      await updateSystemVersion(form.id, data)
      ElMessage.success('更新成功')
    } else {
      await addSystemVersion(data)
      ElMessage.success('新增成功')
    }
    
    dialogVisible.value = false
    handleQuery()
  } finally {
    submitLoading.value = false
  }
}

function handleDialogClose() {
  formRef.value?.resetFields()
  resetForm()
}

function resetForm() {
  form.id = null
  form.versionNumber = ''
  form.versionName = ''
  form.releaseDate = ''
  form.description = ''
  form.featureList = []
  form.improvementList = []
  form.bugFixList = []
  form.isMajor = 0
  form.status = 1
  form.sortOrder = 0
}

function addFeature() {
  form.featureList.push('')
}

function removeFeature(index) {
  form.featureList.splice(index, 1)
}

function addImprovement() {
  form.improvementList.push('')
}

function removeImprovement(index) {
  form.improvementList.splice(index, 1)
}

function addBugFix() {
  form.bugFixList.push('')
}

function removeBugFix(index) {
  form.bugFixList.splice(index, 1)
}
</script>

<style lang="scss" scoped>
.system-version-container {
  padding: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.search-form {
  margin-bottom: 20px;
}

.pagination {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}

.feature-list {
  width: 100%;
}

.feature-item {
  display: flex;
  gap: 10px;
  margin-bottom: 10px;
  
  .el-input {
    flex: 1;
  }
}
</style>
