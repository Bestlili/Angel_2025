<template>
  <NavBar />
  <div class="auth-container">
    <div class="auth-card">
      <h2>登录</h2>
      <form @submit.prevent="handleLogin">
        <!-- 用户名/邮箱输入 -->
        <div class="form-group">
          <label>用户名或邮箱</label>
          <input
            type="text"
            v-model="loginForm.account"
            placeholder="请输入用户名或邮箱"
            :class="{ error: errors.account }"
          >
          <p class="error-text">{{ errors.account }}</p>
        </div>

        <!-- 密码输入 -->
        <div class="form-group">
          <label>密码</label>
          <input
            type="password"
            v-model="loginForm.password"
            placeholder="请输入密码"
            :class="{ error: errors.password }"
          >
          <p class="error-text">{{ errors.password }}</p>
        </div>

        <!-- 登录按钮 -->
        <button type="submit" class="auth-btn" :disabled="isLoading">
          <span v-if="!isLoading">登录</span>
          <span v-if="isLoading">登录中...</span>
        </button>

        <!-- 切换到注册 -->
        <p class="toggle-link">
          还没有账号？<router-link to="/register">去注册</router-link>
        </p>
      </form>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue';
import { useRouter } from 'vue-router';
import api from '../api/index';
import NavBar from '../components/NavBar.vue';

const router = useRouter();
const isLoading = ref(false);
const errors = reactive({ account: '', password: '' });

// 登录表单数据
const loginForm = reactive({
  account: '', // 用户名或邮箱
  password: ''
});

// 表单验证
const validateForm = () => {
  let isValid = true;
  errors.account = '';
  errors.password = '';

  if (!loginForm.account.trim()) {
    errors.account = '请输入用户名或邮箱';
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

// 登录逻辑
const handleLogin = async () => {
  if (!validateForm()) return;

  isLoading.value = true;
  try {
    // 调用后端登录接口
    const response = await api.post('/auth/login', {
      identifier: loginForm.account, // 使用identifier字段，匹配后端API要求
      password: loginForm.password
    });

    // 检查登录是否真正成功（根据后端返回的状态码或标志）
    const isLoginSuccess = response.code === 0 || response.status === 200 || response.success;
    
    // 登录成功且有token时才保存 - 检查多个可能的token位置
    const token = response.token || response.data?.token || response.data?.access_token;
    
    // 处理登录成功情况
    if (isLoginSuccess) {
      // 检查是否有token，如果有则保存
      if (token) {
        // 确保token格式正确，以Bearer开头
        const formattedToken = token.startsWith('Bearer ') ? token : `Bearer ${token}`;
        localStorage.setItem('authToken', formattedToken);
        console.log('登录成功，已保存token到authToken');
      }
      
      // 尝试从响应中获取用户信息
      let userInfo = response.user || response.data?.user;
      
      // 如果没有用户信息，尝试从token中解析
      if (!userInfo && token) {
        try {
          // 从Bearer token中提取JWT部分
          const jwtToken = token.replace('Bearer ', '');
          // 解码JWT的payload部分（中间部分）
          const payload = jwtToken.split('.')[1];
          if (payload) {
            // Base64解码并解析JSON
            const decodedPayload = JSON.parse(atob(payload));
            // 尝试获取用户名，支持多种可能的字段名
            const username = decodedPayload.name || decodedPayload.username || decodedPayload.sub || loginForm.account;
            userInfo = {
              id: decodedPayload.sub || decodedPayload.id,
              name: username,
              email: decodedPayload.email
            };
          }
        } catch (parseError) {
          console.error('解析token中的用户信息失败:', parseError);
        }
      }
      
      // 如果仍然没有用户信息，使用输入的账号作为用户名
      if (!userInfo) {
        userInfo = { name: loginForm.account };
      }
      
      // 保存用户信息到localStorage
      localStorage.setItem('username', userInfo.name);
      localStorage.setItem('user', JSON.stringify(userInfo));
      console.log('用户信息已保存到localStorage:', userInfo);
    } else if (import.meta.env.DEV) {
      // 开发环境下，如果没有真正的token，使用模拟token
      console.log('开发环境：使用模拟token');
      // 生成符合JWT格式的模拟token（三部分，用点分隔）
      const header = btoa(JSON.stringify({alg: 'HS256', typ: 'JWT'}));
      const userInfo = {
        id: 'user123',
        name: loginForm.account,
        email: 'test@example.com'
      };
      const payload = btoa(JSON.stringify({
        sub: userInfo.id,
        name: userInfo.name,
        email: userInfo.email,
        iat: Math.floor(Date.now() / 1000),
        exp: Math.floor(Date.now() / 1000) + (60 * 60) // 1小时过期
      }));
      const signature = 'mock_signature_' + Date.now();
      
      // 组合成JWT格式的token并添加Bearer前缀
      const mockToken = `Bearer ${header}.${payload}.${signature}`;
      
      // 保存所有认证信息
      localStorage.setItem('authToken', mockToken);
      localStorage.setItem('username', userInfo.name);
      localStorage.setItem('user', JSON.stringify(userInfo));
      console.log('使用开发环境模拟token（JWT格式）到authToken:', mockToken);
    } else {
      console.error('登录未成功，不保存token');
      throw new Error('登录失败：' + (response.message || '未知错误'));
    }
    
    // 显示登录成功提示
    alert('登录成功！');
    // 重新加载页面，让NavBar组件可以检测到登录状态
    window.location.href = '/';
  } catch (err) {
    // 处理错误（如账号密码错误）
    errors.account = err.response?.data?.message || '登录失败，请检查账号密码';
    console.error('登录错误:', err);
  } finally {
    isLoading.value = false;
  }
};
</script>

<style scoped>
.auth-container {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: calc(100vh - 72px); /* 减去导航栏高度 */
  padding: 20px;
  margin-top: 0;
}

.auth-card {
  width: 100%;
  max-width: 420px;
  padding: 36px;
  border-radius: 16px;
  box-shadow: 0 8px 32px rgba(103, 58, 183, 0.1);
  background: #ffffff;
  position: relative;
  overflow: hidden;
}

.auth-card::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  height: 4px;
  background: linear-gradient(90deg, #673ab7, #9c27b0);
}

.auth-card::after {
  content: '';
  position: absolute;
  top: 20px;
  right: -20px;
  width: 80px;
  height: 80px;
  border-radius: 50%;
  background: rgba(103, 58, 183, 0.05);
  z-index: 0;
}

h2 {
  text-align: center;
  margin-bottom: 32px;
  color: #333;
  font-size: 24px;
  font-weight: 600;
  position: relative;
  z-index: 1;
}

.form-group {
  margin-bottom: 24px;
  position: relative;
  z-index: 1;
}

label {
  display: block;
  margin-bottom: 8px;
  color: #555;
  font-size: 14px;
  font-weight: 500;
}

input {
  width: 100%;
  padding: 14px 16px;
  border: 1px solid #e1e5e9;
  border-radius: 8px;
  font-size: 16px;
  box-sizing: border-box;
  transition: all 0.3s ease;
  background: #f9fafc;
}

input:focus {
  outline: none;
  border-color: #673ab7;
  box-shadow: 0 0 0 3px rgba(103, 58, 183, 0.1);
  background: #ffffff;
}

input.error {
  border-color: #dc3545;
  box-shadow: 0 0 0 3px rgba(220, 53, 69, 0.1);
}

.error-text {
  margin: 6px 0 0;
  color: #dc3545;
  font-size: 12px;
}

.auth-btn {
  width: 100%;
  padding: 14px;
  background: linear-gradient(135deg, #673ab7, #9c27b0);
  color: white;
  border: none;
  border-radius: 8px;
  font-size: 16px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.3s ease;
  position: relative;
  z-index: 1;
  overflow: hidden;
}

.auth-btn:hover:not(:disabled) {
  transform: translateY(-2px);
  box-shadow: 0 6px 16px rgba(103, 58, 183, 0.3);
}

.auth-btn:active:not(:disabled) {
  transform: translateY(0);
}

.auth-btn:disabled {
  background: linear-gradient(135deg, #9d7ed8, #b39ddb);
  cursor: not-allowed;
  transform: none;
  box-shadow: none;
}

.toggle-link {
  text-align: center;
  margin-top: 24px;
  color: #666;
  font-size: 14px;
  position: relative;
  z-index: 1;
}

.toggle-link a {
  color: #673ab7;
  text-decoration: none;
  font-weight: 500;
  transition: all 0.3s ease;
  position: relative;
}

.toggle-link a:hover {
  color: #9c27b0;
}

.toggle-link a::after {
  content: '';
  position: absolute;
  left: 0;
  bottom: -2px;
  width: 0;
  height: 2px;
  background: #673ab7;
  transition: width 0.3s ease;
}

.toggle-link a:hover::after {
  width: 100%;
}
</style>