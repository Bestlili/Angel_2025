<template>
  <nav class="navbar">
    <div class="logo">
      <div class="logo-circle"></div>
      <span class="logo-text">Angel</span>
    </div>
    <div class="nav-links">
      <router-link to="/" class="nav-link">首页</router-link>
      <a href="#" class="nav-link" @click.prevent="handleEnterTreehole">树洞</a>
      <router-link to="/diary" class="nav-link">心情日记</router-link>
      <router-link to="/tests" class="nav-link">心理测试</router-link>
      <router-link to="/community" class="nav-link">社区聊天</router-link>
      <router-link to="/admin" class="nav-link" v-if="isLoggedIn || isDevEnvironment">管理界面</router-link>
    </div>
    <!-- 登录状态检查 -->
    <div class="user-menu" v-if="isLoggedIn">
      <span class="user-name">{{ userName }}</span>
      <router-link to="/account-settings" class="settings-btn">设置</router-link>
      <button class="logout-btn" @click="handleLogout">登出</button>
    </div>
    <!-- 未登录状态 -->
    <div class="auth-buttons" v-else-if="!isAuthPage">
      <router-link to="/login" class="login-btn">登录</router-link>
      <router-link to="/register" class="register-btn">注册</router-link>
    </div>
    <div class="auth-buttons" v-else-if="$route.path === '/login'">
      <router-link to="/login" class="login-btn active">当前已在登录页</router-link>
    </div>
    <div class="auth-buttons" v-else>
      <router-link to="/register" class="register-btn active">当前已在注册页</router-link>
    </div>
  </nav>
</template>

<script setup>
import { useRouter } from 'vue-router';
import { computed, ref, onMounted, onUnmounted } from 'vue';
import api from '../api/index.js';

const router = useRouter();
const isLoggedIn = ref(false);
const userName = ref('');

// 计算当前是否在登录或注册页面
const isAuthPage = computed(() => {
  const path = router.currentRoute.value.path;
  return path === '/login' || path === '/register';
});

// 计算是否为开发环境
const isDevEnvironment = computed(() => {
  return import.meta.env.DEV;
});

// 检查登录状态 - 从localStorage检测登录状态
const checkLoginStatus = () => {
  // 检查localStorage中是否有token来判断登录状态
  // 这是一个简单的前端判断，实际权限验证由后端控制
  const token = localStorage.getItem('authToken');
  isLoggedIn.value = !!token; // 如果有token，则认为已登录
  
  // 如果已登录，尝试从token中解析用户名
  if (isLoggedIn.value) {
    try {
      // 从Bearer token中提取JWT部分
      const jwtToken = token.replace('Bearer ', '');
      // 解码JWT的payload部分（中间部分）
      const payload = jwtToken.split('.')[1];
      if (payload) {
        // Base64解码并解析JSON
        const decodedPayload = JSON.parse(atob(payload));
        // 尝试获取用户名，支持多种可能的字段名，优先使用实际用户名
        userName.value = decodedPayload.name || decodedPayload.username || decodedPayload.sub || '用户';
      } else {
        // 如果无法解析payload，尝试从localStorage中获取用户名
        userName.value = localStorage.getItem('username') || '用户';
      }
    } catch (error) {
      console.error('解析token失败:', error);
      // 发生错误时，尝试从localStorage中获取用户名
      userName.value = localStorage.getItem('username') || '用户';
    }
  }
};

// 登出处理
const handleLogout = async () => {
  try {
    // 尝试调用后端登出接口
    const response = await api.logout();
    console.log('后端登出成功:', response);
    
    // 后端登出成功后，清除前端状态
    localStorage.removeItem('authToken');
    localStorage.removeItem('username');
    localStorage.removeItem('user'); // 清除可能存在的用户信息
    isLoggedIn.value = false;
    userName.value = '';
    
    // 显示友好的登出提示
    alert(response.message || '已成功登出');
    
    // 延迟一小段时间后跳转，确保用户看到提示
    setTimeout(() => {
      window.location.href = '/login';
    }, 100);
  } catch (err) {
    // 捕获API调用错误
    console.error('登出API调用失败:', err);
    
    // 即使后端登出失败，也确保清除前端状态
    localStorage.removeItem('authToken');
    localStorage.removeItem('username');
    localStorage.removeItem('user');
    isLoggedIn.value = false;
    userName.value = '';
    
    // 显示登出提示，即使API调用失败
    alert('已从前段登出');
    
    // 跳转到登录页
    setTimeout(() => {
      window.location.href = '/login';
    }, 100);
  }
};

// 组件挂载时检查登录状态
onMounted(() => {
  // 立即检查一次登录状态
  checkLoginStatus();
  
  // 使用watch监听路由变化，确保更可靠地检测登录状态
  router.afterEach(() => {
    // 路由变化后延迟一小段时间再检查，确保localStorage已更新
    setTimeout(() => {
      checkLoginStatus();
    }, 100);
  });
  
  // 额外添加一个定时器，定期检查登录状态，确保UI与实际登录状态同步
  const statusCheckInterval = setInterval(() => {
    checkLoginStatus();
  }, 2000); // 每2秒检查一次
  
  // 组件卸载时清除定时器
  // 注意：在setup中，需要通过onUnmounted来清理
  onUnmounted(() => {
    clearInterval(statusCheckInterval);
  });
});

// 处理进入树洞按钮点击事件
const handleEnterTreehole = () => {
  // 验证是否登录 - 从localStorage检查token
  const token = localStorage.getItem('authToken');
      if (token) {
    // 已登录，正常跳转到树洞页面
    router.push('/treehole');
  } else {
    // 未登录，不跳转并提示用户
    alert('请先登录后再进入树洞');
    // 可选：跳转到登录页
    // router.push('/login');
  }
};
</script>

<style scoped>
.navbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px 40px;
  background-color: #fff;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  position: relative;
  z-index: 1000;
}

.logo {
  display: flex;
  align-items: center;
  gap: 8px;
}

.logo-circle {
  width: 32px;
  height: 32px;
  border-radius: 50%;
  background: linear-gradient(45deg, #9c27b0, #673ab7);
}

.logo-text {
  font-size: 20px;
  font-weight: bold;
  color: #333;
}

.nav-links {
  display: flex;
  gap: 32px;
}

.nav-link {
  text-decoration: none;
  color: #666;
  font-size: 16px;
  transition: color 0.3s;
}

.nav-link:hover {
  color: #673ab7;
}

.auth-buttons {
  display: flex;
  gap: 16px;
}

.login-btn,
.register-btn {
  padding: 8px 16px;
  border-radius: 20px;
  border: none;
  cursor: pointer;
  font-size: 14px;
}

.login-btn {
  background-color: #fff;
  color: #666;
  border: 1px solid #ddd;
}

.login-btn.active {
  background-color: #f0f0f0;
  color: #673ab7;
  border-color: #673ab7;
  cursor: default;
}

.register-btn {
  background-color: #673ab7;
  color: #fff;
}

.register-btn.active {
  background-color: #5a2d90;
  cursor: default;
}

/* 用户菜单样式 */
.user-menu {
  display: flex;
  align-items: center;
  gap: 16px;
}

.user-name {
  font-size: 14px;
  color: #673ab7;
  font-weight: 500;
}

.settings-btn {
  padding: 6px 12px;
  border-radius: 16px;
  border: 1px solid #ddd;
  background-color: #fff;
  color: #666;
  font-size: 14px;
  text-decoration: none;
  transition: all 0.3s;
}

.settings-btn:hover {
  background-color: #f5f5f5;
  border-color: #673ab7;
  color: #673ab7;
}

.logout-btn {
  padding: 6px 12px;
  border-radius: 16px;
  border: 1px solid #ddd;
  background-color: #fff;
  color: #666;
  font-size: 14px;
  cursor: pointer;
  transition: all 0.3s;
}

.logout-btn:hover {
  background-color: #f5f5f5;
  border-color: #673ab7;
  color: #673ab7;
}
</style>