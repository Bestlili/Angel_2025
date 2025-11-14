<template>
  <nav class="admin-navbar">
    <router-link to="/" class="admin-logo">
      <div class="logo-circle"></div>
      <span class="logo-text">Angel 管理后台</span>
    </router-link>
    
    <div class="admin-nav-links">
      <router-link 
        to="/admin/review" 
        class="admin-nav-link"
        :class="{ active: currentRoute === '/admin/review' }"
      >
        内容审核
      </router-link>
      <router-link 
        to="/admin/users" 
        class="admin-nav-link"
        :class="{ active: currentRoute.includes('/admin/users') }"
      >
        用户管理
      </router-link>
      <router-link 
        to="/admin/analytics" 
        class="admin-nav-link"
        :class="{ active: currentRoute.includes('/admin/analytics') }"
      >
        数据分析
      </router-link>
      <router-link 
        to="/admin/settings" 
        class="admin-nav-link"
        :class="{ active: currentRoute.includes('/admin/settings') }"
      >
        系统设置
      </router-link>
    </div>
    
    <div class="admin-user-info">
      <span class="admin-user-name">管理员</span>
      <router-link to="/" class="home-btn">返回首页</router-link>
      <button class="logout-btn" @click="logout">退出登录</button>
    </div>
  </nav>
</template>

<script>
export default {
  name: 'AdminNavBar',
  data() {
    return {
      currentRoute: ''
    };
  },
  mounted() {
    this.updateCurrentRoute();
    window.addEventListener('popstate', this.updateCurrentRoute);
  },
  beforeUnmount() {
    window.removeEventListener('popstate', this.updateCurrentRoute);
  },
  methods: {
    updateCurrentRoute() {
      this.currentRoute = this.$route.path;
    },
    logout() {
      // 清除所有相关的认证信息
      localStorage.removeItem('userInfo');
      localStorage.removeItem('authToken');
      localStorage.removeItem('token');
      localStorage.removeItem('username');
      localStorage.removeItem('user');
      // 重定向到登录页面
      this.$router.push('/login');
    }
  }
};
</script>

<style scoped>
.admin-navbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 24px;
  height: 64px;
  background-color: #ffffff;
  border-bottom: 1px solid #e8e8e8;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
}

.admin-logo {
  display: flex;
  align-items: center;
  cursor: pointer;
}

.logo-circle {
  width: 32px;
  height: 32px;
  background-color: #1890ff;
  border-radius: 50%;
  margin-right: 8px;
}

.logo-text {
  font-size: 18px;
  font-weight: 600;
  color: #1890ff;
}

.admin-nav-links {
  display: flex;
  gap: 16px;
}

.admin-nav-link {
  padding: 8px 16px;
  font-size: 14px;
  color: #333333;
  text-decoration: none;
  border-radius: 4px;
  transition: all 0.3s ease;
}

.admin-nav-link:hover {
  color: #1890ff;
  background-color: #f0f7ff;
}

.admin-nav-link.active {
  color: #ffffff;
  background-color: #1890ff;
}

.admin-user-info {
  display: flex;
  align-items: center;
  gap: 12px;
}

.admin-user-name {
  font-size: 14px;
  color: #666666;
}

.logout-btn {
  padding: 6px 12px;
  font-size: 12px;
  color: #666666;
  background-color: #f5f5f5;
  border: 1px solid #d9d9d9;
  border-radius: 4px;
  cursor: pointer;
  transition: all 0.3s ease;
}

.home-btn {
  padding: 6px 12px;
  font-size: 12px;
  color: #1890ff;
  background-color: #e6f7ff;
  border: 1px solid #91d5ff;
  border-radius: 4px;
  text-decoration: none;
  transition: all 0.3s ease;
}

.home-btn:hover {
  color: #096dd9;
  border-color: #096dd9;
  background-color: #bae7ff;
}

.logout-btn:hover {
  color: #ff4d4f;
  border-color: #ff4d4f;
  background-color: #fff1f0;
}
</style>