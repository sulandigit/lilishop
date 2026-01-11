<template>
  <div class="dashboard-container">
    <Card>
      <p slot="title">
        <Icon type="ios-home" />
        欢迎来到 Lilishop 管理后台
      </p>
      <div class="welcome-content">
        <p>您已成功登录系统！</p>
        <Button type="primary" @click="handleLogout">退出登录</Button>
      </div>
    </Card>
  </div>
</template>

<script>
import { logout } from '@/api/auth'

export default {
  name: 'Dashboard',
  methods: {
    async handleLogout() {
      try {
        await logout()
      } catch (e) {
        // 忽略错误
      }
      this.$store.dispatch('logout')
      this.$router.push('/login')
      this.$Message.success('已退出登录')
    }
  }
}
</script>

<style lang="less" scoped>
.dashboard-container {
  padding: 20px;
  min-height: 100vh;
  background: #f5f7f9;
}

.welcome-content {
  text-align: center;
  padding: 40px 0;
  
  p {
    font-size: 18px;
    color: #333;
    margin-bottom: 20px;
  }
}
</style>
