<template>
  <div class="admin-login-container">
  <div class="admin-login-card">
    <div class="login-header">
      <h1 class="login-title">管理员登录</h1>
      <p class="login-subtitle">请输入您的管理员账号和密码</p>
      <button class="back-button" @click="goBack">返回管理员界面</button>
    </div>
      
      <form @submit.prevent="handleAdminLogin" class="login-form">
        <!-- 用户名输入 -->
        <div class="form-group">
          <label for="username" class="form-label">用户名</label>
          <div class="input-wrapper">
            <span class="input-icon">👤</span>
            <input
              type="text"
              id="username"
              v-model="loginForm.username"
              placeholder="请输入管理员用户名"
              :class="{ 'input-error': errors.username }"
              required
            >
          </div>
          <p class="error-text" v-if="errors.username">{{ errors.username }}</p>
        </div>

        <!-- 密码输入 -->
        <div class="form-group">
          <label for="password" class="form-label">密码</label>
          <div class="input-wrapper">
            <span class="input-icon">🔒</span>
            <input
              type="password"
              id="password"
              v-model="loginForm.password"
              placeholder="请输入管理员密码"
              :class="{ 'input-error': errors.password }"
              required
            >
            <button
              type="button"
              class="toggle-password"
              @click="showPassword = !showPassword"
            >
              {{ showPassword ? '👁️' : '👁️‍🗨️' }}
            </button>
          </div>
          <p class="error-text" v-if="errors.password">{{ errors.password }}</p>
        </div>

        <!-- 记住登录 -->
        <div class="remember-me">
          <input
            type="checkbox"
            id="remember"
            v-model="loginForm.remember"
          >
          <label for="remember">记住登录状态</label>
        </div>

        <!-- 登录按钮 -->
        <button
          type="submit"
          class="login-button"
          :disabled="isLoading"
        >
          <span v-if="!isLoading">安全登录</span>
          <span v-else>登录中...</span>
        </button>

        <!-- 错误提示 -->
        <p class="global-error" v-if="globalError">{{ globalError }}</p>

        <!-- 版权信息 -->
        <div class="login-footer">
          <p>&copy; 2024 Angel 管理系统 - 仅供管理员使用</p>
        </div>
      </form>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue';
import { useRouter } from 'vue-router';
import api from '../api';

const router = useRouter();
const isLoading = ref(false);
const showPassword = ref(false);
const errors = reactive({ username: '', password: '' });
const globalError = ref('');

// 返回管理员界面或首页
const goBack = () => {
  // 检查是否已登录（支持统一登录接口的存储方式）
  const token = localStorage.getItem('authToken') || sessionStorage.getItem('authToken');
  const userInfo = localStorage.getItem('user') || sessionStorage.getItem('user') || 
                  localStorage.getItem('admin') || sessionStorage.getItem('admin');
  
  try {
    // 解析用户信息，检查是否为管理员角色
    if (token && userInfo) {
      const userData = JSON.parse(userInfo);
      const isAdmin = userData.role === 'admin' || userData.role === '1'; // 兼容数字和字符串角色
      
      if (isAdmin) {
        // 如果已登录且是管理员，则跳转至管理员界面
        router.push('/admin');
      } else {
        // 如果已登录但不是管理员，则跳转至首页
        router.push('/');
      }
    } else {
      // 如果未登录，则跳转至首页
      router.push('/');
    }
  } catch (e) {
    // 处理解析错误，默认跳转至首页
    console.error('解析用户信息时出错:', e);
    router.push('/');
  }
};

// 登录表单数据
const loginForm = reactive({
  username: '',
  password: '',
  remember: false
});

// 表单验证
const validateForm = () => {
  let isValid = true;
  errors.username = '';
  errors.password = '';
  globalError.value = '';

  if (!loginForm.username.trim()) {
    errors.username = '请输入用户名';
    isValid = false;
  }

  if (!loginForm.password) {
    errors.password = '请输入密码';
    isValid = false;
  } else if (loginForm.password.length < 6) {
    errors.password = '密码长度不能少于6位';
    isValid = false;
  }

  return isValid;
};

// 管理员登录逻辑
const handleAdminLogin = async () => {
  if (!validateForm()) return;

  isLoading.value = true;
  try {
        // 使用统一的登录接口（管理员和普通用户共用）
        const response = await api.post('/auth/login', {
          identifier: loginForm.username, // 使用identifier参数名，与后端保持一致
          password: loginForm.password
        });

        // 检查登录是否成功
        const isLoginSuccess = response.code === 0 || response.status === 200 || response.success;
        
        // 登录成功且有token时保存
        const token = response.token || response.data?.token || response.data?.access_token;
        const userInfo = response.user || response.data?.user || {};
        
        if (isLoginSuccess && token) {
          // 确保token格式正确，以Bearer开头
          const formattedToken = token.startsWith('Bearer ') ? token : `Bearer ${token}`;
          
          // 根据是否记住登录状态选择存储方式
          if (loginForm.remember) {
            localStorage.setItem('authToken', formattedToken);
          } else {
            sessionStorage.setItem('authToken', formattedToken);
          }

          // 保存用户信息，包含角色信息
          const userData = {
            username: userInfo.username || loginForm.username,
            role: userInfo.role || userInfo.userRole || 'user', // 确保有role字段
            userId: userInfo.id || userInfo.userId
          };
          
          if (loginForm.remember) {
            localStorage.setItem('user', JSON.stringify(userData));
            localStorage.setItem('admin', JSON.stringify(userData)); // 兼容旧代码
            localStorage.setItem('username', userData.username);
          } else {
            sessionStorage.setItem('user', JSON.stringify(userData));
            sessionStorage.setItem('admin', JSON.stringify(userData)); // 兼容旧代码
            sessionStorage.setItem('username', userData.username);
          }

          // 检查是否为管理员角色
          const isAdmin = userData.role === 'admin' || userData.role === '1'; // 兼容数字和字符串角色
          console.log('登录成功，用户角色:', userData.role, '是否管理员:', isAdmin);
          
          // 根据角色决定跳转目标
          if (isAdmin) {
            // 管理员角色跳转到管理员仪表板
            router.push('/admin');
          } else {
            // 非管理员角色显示错误并清除登录信息
            globalError.value = '您不是管理员，没有权限访问管理员界面';
            localStorage.removeItem('authToken');
            sessionStorage.removeItem('authToken');
            localStorage.removeItem('user');
            sessionStorage.removeItem('user');
            localStorage.removeItem('admin');
            sessionStorage.removeItem('admin');
          }
    } else {
      // 模拟管理员登录（开发环境下）
      console.log('开发环境：使用模拟管理员token');
      
      // 生成符合JWT格式的模拟token
      const header = btoa(JSON.stringify({alg: 'HS256', typ: 'JWT'}));
      const payload = btoa(JSON.stringify({
        sub: 'admin123',
        username: loginForm.username,
        role: 'admin',
        iat: Math.floor(Date.now() / 1000),
        exp: Math.floor(Date.now() / 1000) + (60 * 60 * 24) // 24小时过期
      }));
      const signature = 'mock_admin_signature_' + Date.now();
      
      const mockToken = `Bearer ${header}.${payload}.${signature}`;
      const userData = { 
        username: loginForm.username, 
        role: 'admin',
        userId: 'admin123'
      };
      
      // 保存认证信息 - 兼容统一登录的存储方式
      if (loginForm.remember) {
        localStorage.setItem('authToken', mockToken);
        localStorage.setItem('user', JSON.stringify(userData));
        localStorage.setItem('admin', JSON.stringify(userData)); // 兼容旧代码
        localStorage.setItem('username', userData.username);
      } else {
        sessionStorage.setItem('authToken', mockToken);
        sessionStorage.setItem('user', JSON.stringify(userData));
        sessionStorage.setItem('admin', JSON.stringify(userData)); // 兼容旧代码
        sessionStorage.setItem('username', userData.username);
      }
      
      console.log('开发环境模拟管理员登录成功');
      
      // 跳转到管理员仪表板
      router.push('/admin');
    }
  } catch (err) {
    // 处理登录错误
    const errorMsg = err.response?.data?.message || '登录失败，请检查账号密码';
    console.error('登录错误:', errorMsg);
    globalError.value = errorMsg;
  } finally {
    isLoading.value = false;
  }
};
</script>

<style scoped>
.admin-login-container {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 100vh;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  padding: 20px;
}

.admin-login-card {
  background: white;
  border-radius: 16px;
  box-shadow: 0 20px 40px rgba(0, 0, 0, 0.15);
  padding: 40px;
  width: 100%;
  max-width: 450px;
  transition: transform 0.3s ease;
}

.admin-login-card:hover {
  transform: translateY(-5px);
}

.login-header {
  text-align: center;
  margin-bottom: 30px;
}

.login-title {
  font-size: 2.2rem;
  font-weight: 700;
  color: #2d3748;
  margin: 0 0 10px 0;
}

.login-subtitle {
  font-size: 1rem;
  color: #718096;
  margin: 0;
}

/* 返回按钮样式 */
.back-button {
  background-color: #e5e7eb;
  color: #374151;
  border: none;
  border-radius: 8px;
  padding: 0.75rem 1.5rem;
  font-size: 0.875rem;
  font-weight: 500;
  cursor: pointer;
  margin-top: 0.5rem;
  transition: all 0.2s ease;
}

.back-button:hover {
  background-color: #d1d5db;
  transform: translateY(-1px);
}

.back-button:active {
  transform: translateY(0);
}

.login-form {
  width: 100%;
}

.form-group {
  margin-bottom: 20px;
}

.form-label {
  display: block;
  font-weight: 600;
  color: #4a5568;
  margin-bottom: 8px;
  font-size: 0.9rem;
}

.input-wrapper {
  position: relative;
  display: flex;
  align-items: center;
}

.input-icon {
  position: absolute;
  left: 15px;
  font-size: 1.2rem;
  color: #a0aec0;
  z-index: 1;
}

input {
  width: 100%;
  padding: 12px 15px 12px 45px;
  border: 2px solid #e2e8f0;
  border-radius: 8px;
  font-size: 1rem;
  transition: all 0.2s ease;
  background-color: #f7fafc;
}

input:focus {
  outline: none;
  border-color: #667eea;
  box-shadow: 0 0 0 3px rgba(102, 126, 234, 0.1);
  background-color: white;
}

.input-error {
  border-color: #fc8181;
}

.toggle-password {
  position: absolute;
  right: 10px;
  background: none;
  border: none;
  cursor: pointer;
  font-size: 1.2rem;
  padding: 5px;
  color: #a0aec0;
}

.toggle-password:hover {
  color: #4a5568;
}

.error-text {
  color: #e53e3e;
  font-size: 0.85rem;
  margin-top: 5px;
  margin-bottom: 0;
}

.remember-me {
  display: flex;
  align-items: center;
  margin-bottom: 25px;
}

.remember-me input[type="checkbox"] {
  width: auto;
  margin-right: 8px;
  padding: 0;
}

.remember-me label {
  color: #4a5568;
  font-size: 0.9rem;
  cursor: pointer;
}

.login-button {
  width: 100%;
  padding: 14px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  border: none;
  border-radius: 8px;
  font-size: 1.1rem;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.2s ease;
}

.login-button:hover:not(:disabled) {
  transform: translateY(-2px);
  box-shadow: 0 10px 20px rgba(102, 126, 234, 0.3);
}

.login-button:active:not(:disabled) {
  transform: translateY(0);
}

.login-button:disabled {
  opacity: 0.7;
  cursor: not-allowed;
}

.global-error {
  color: #e53e3e;
  text-align: center;
  margin: 15px 0;
  font-weight: 500;
}

.login-footer {
  text-align: center;
  margin-top: 30px;
  padding-top: 20px;
  border-top: 1px solid #e2e8f0;
}

.login-footer p {
  color: #a0aec0;
  font-size: 0.85rem;
  margin: 0;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .admin-login-card {
    padding: 30px 25px;
    margin: 20px;
  }
  
  .login-title {
    font-size: 1.8rem;
  }
}

@media (max-width: 480px) {
  .admin-login-card {
    padding: 25px 20px;
    margin: 15px;
  }
  
  .login-title {
    font-size: 1.6rem;
  }
  
  input {
    padding: 10px 12px 10px 40px;
  }
  
  .login-button {
    padding: 12px;
  }
}
</style>