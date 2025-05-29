<template>
  <div class="change-password-container">
    <el-card class="change-password-card">
      <h2>🛠 修改密码</h2>
      <el-form :model="form" label-width="100px">
        <el-form-item label="旧密码">
          <el-input type="password" v-model="form.oldPassword" />
        </el-form-item>
        <el-form-item label="新密码">
          <el-input type="password" v-model="form.newPassword" />
        </el-form-item>
        <el-form-item label="确认新密码">
          <el-input type="password" v-model="form.confirmPassword" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleChangePassword">确认修改</el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { ElMessage } from 'element-plus'
import { useRouter } from 'vue-router'

const router = useRouter()

const form = ref({
  oldPassword: '',
  newPassword: '',
  confirmPassword: ''
})

const handleChangePassword = async () => {
  if (form.value.newPassword !== form.value.confirmPassword) {
    ElMessage.error('两次输入的新密码不一致')
    return
  }

  // 示例逻辑（真实应调用后端验证旧密码+修改）
  if (form.value.oldPassword === '123456') {
    ElMessage.success('密码修改成功，请重新登录')
    localStorage.removeItem('username')
    router.push('/login')
  } else {
    ElMessage.error('旧密码错误')
  }
}
</script>

<style scoped>
.change-password-container {
  display: flex;
  justify-content: center;
  align-items: center;
  height: 100vh;
}
.change-password-card {
  width: 450px;
}
</style>
