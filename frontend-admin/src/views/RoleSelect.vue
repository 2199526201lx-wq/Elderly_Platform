<template>
  <div class="role-select-page">
    <div class="role-header">
      <h1>AI 智能养老社区</h1>
      <p>请选择您的身份</p>
    </div>

    <div class="role-cards">
      <div class="role-card" @click="selectRole('member')">
        <div class="role-icon member-icon">
          <el-icon :size="56"><User /></el-icon>
        </div>
        <h2>会员登录</h2>
        <p>健康管理、AI咨询、体检预约、社区活动</p>
      </div>

      <div class="role-card" @click="selectRole('admin')">
        <div class="role-icon admin-icon">
          <el-icon :size="56"><Setting /></el-icon>
        </div>
        <h2>管理员登录</h2>
        <p>会员管理、数据概览、系统配置</p>
      </div>
    </div>
  </div>
</template>

<script setup>
import { useRouter } from 'vue-router'

const router = useRouter()

function selectRole(role) {
  if (role === 'member') {
    router.push('/member/login')
  } else {
    router.push('/admin/login')
  }
}
</script>

<style scoped>
.role-select-page {
  height: 100vh;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #3b9e5a 0%, #2e7d32 100%);
  color: #fff;
}

.role-header {
  text-align: center;
  margin-bottom: 48px;
}

.role-header h1 {
  font-size: 36px;
  font-weight: 700;
  margin-bottom: 8px;
}

.role-header p {
  font-size: 18px;
  opacity: 0.85;
}

.role-cards {
  display: flex;
  gap: 32px;
}

.role-card {
  background: rgba(255, 255, 255, 0.15);
  backdrop-filter: blur(10px);
  border: 1px solid rgba(255, 255, 255, 0.2);
  border-radius: 16px;
  padding: 40px 48px;
  text-align: center;
  cursor: pointer;
  transition: all 0.3s;
  width: 260px;
}

.role-card:hover {
  transform: translateY(-8px);
  background: rgba(255, 255, 255, 0.25);
  box-shadow: 0 16px 48px rgba(0, 0, 0, 0.2);
}

.role-icon {
  width: 96px;
  height: 96px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  margin: 0 auto 20px;
}

.member-icon {
  background: rgba(103, 194, 58, 0.3);
}

.admin-icon {
  background: rgba(230, 162, 60, 0.3);
}

.role-card h2 {
  font-size: 22px;
  margin-bottom: 8px;
}

.role-card p {
  font-size: 14px;
  opacity: 0.8;
  line-height: 1.5;
}
</style>
