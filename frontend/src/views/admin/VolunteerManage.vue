<template>
  <div>
    <el-card shadow="never" class="table-card">
      <div class="toolbar">
        <span class="table-title">救助团队名单</span>
        <el-button type="primary" color="#ff9d4d" icon="Plus" @click="openAddDialog">添加救助员</el-button>
      </div>

      <el-table :data="volunteerList" v-loading="loading" style="width: 100%" stripe>
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="username" label="姓名/昵称" width="150" />
        <el-table-column prop="phone" label="手机号" width="150" />
        <el-table-column prop="idCard" label="身份证号" />
        <el-table-column prop="createTime" label="加入时间" width="180">
          <template #default="scope">
            {{ formatTime(scope.row.createTime) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="120" fixed="right">
          <template #default="scope">
            <el-popconfirm title="确定要移除该救助员吗？" @confirm="handleDelete(scope.row.id)">
              <template #reference>
                <el-button type="danger" link>移除</el-button>
              </template>
            </el-popconfirm>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-dialog v-model="dialogVisible" title="新增救助员" width="400px" destroy-on-close>
      <el-form :model="addForm" :rules="rules" ref="addFormRef" label-width="80px">
        <el-form-item label="姓名" prop="username">
          <el-input v-model="addForm.username" placeholder="请输入救助员姓名" />
        </el-form-item>
        <el-form-item label="手机号" prop="phone">
          <el-input v-model="addForm.phone" placeholder="请输入真实手机号" />
        </el-form-item>
        <el-form-item label="身份证号" prop="idCard">
          <el-input v-model="addForm.idCard" placeholder="请输入身份证号" />
        </el-form-item>
        <el-alert title="初始登录密码默认为: 123456" type="info" show-icon :closable="false" style="margin-top: 10px;" />
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button type="primary" color="#ff9d4d" @click="submitAdd" :loading="submitLoading">确定添加</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import axios from 'axios'

const loading = ref(false)
const volunteerList = ref([])
const dialogVisible = ref(false)
const submitLoading = ref(false)
const addFormRef = ref(null)
const addForm = ref({ username: '', phone: '', idCard: '' })

const validateIdCard = (rule, value, callback) => {
  const reg = /^[1-9]\d{5}(18|19|20)\d{2}(0[1-9]|1[0-2])(0[1-9]|[12]\d|3[01])\d{3}[\dXx]$/
  if (value === '') callback(new Error('请输入身份证号'))
  else if (!reg.test(value)) callback(new Error('身份证号格式不正确'))
  else callback()
}
const rules = {
  username: [{ required: true, message: '姓名不能为空', trigger: 'blur' }],
  phone: [
    { required: true, message: '手机号不能为空', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号格式', trigger: 'blur' }
  ],
  idCard: [{ required: true, validator: validateIdCard, trigger: 'blur' }]
}

const formatTime = (timeStr) => {
  if (!timeStr) return ''
  return timeStr.replace('T', ' ')
}

const fetchVolunteers = async () => {
  loading.value = true
  try {
    const res = await axios.get('http://localhost:8080/api/user/volunteers')
    if (res.data.code === 200) {
      volunteerList.value = res.data.data
    } else {
      ElMessage.error(res.data.message)
    }
  } catch (error) {
    ElMessage.error('获取列表失败，请检查网络')
  } finally {
    loading.value = false
  }
}

const openAddDialog = () => {
  addForm.value = { username: '', phone: '', idCard: '' }
  dialogVisible.value = true
}

const submitAdd = async () => {
  if (!addFormRef.value) return
  await addFormRef.value.validate(async (valid) => {
    if (valid) {
      submitLoading.value = true
      try {
        const res = await axios.post('http://localhost:8080/api/user/addVolunteer', addForm.value)
        if (res.data.code === 200) {
          ElMessage.success('添加成功！')
          dialogVisible.value = false
          fetchVolunteers()
        } else {
          ElMessage.error(res.data.message)
        }
      } catch (error) {
        ElMessage.error('添加失败，请检查网络')
      } finally {
        submitLoading.value = false
      }
    }
  })
}

const handleDelete = async (id) => {
  try {
    const res = await axios.delete(`http://localhost:8080/api/user/delete/${id}`)
    if (res.data.code === 200) {
      ElMessage.success('移除成功')
      fetchVolunteers()
    } else {
      ElMessage.error(res.data.message)
    }
  } catch (error) {
    ElMessage.error('删除失败')
  }
}

onMounted(() => {
  fetchVolunteers()
})
</script>

<style scoped>
.table-card { border-radius: 12px; border: none; box-shadow: 0 4px 12px rgba(0,0,0,0.05); }
.toolbar { display: flex; justify-content: space-between; align-items: center; margin-bottom: 20px; }
.table-title { font-size: 18px; font-weight: bold; color: #333; }
</style>