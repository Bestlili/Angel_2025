<template>
  <NavBar />
  <div class="auth-container">
    <div class="auth-card">
      <h2>注册</h2>
      <form @submit.prevent="handleRegister">
        <!-- 用户名输入 -->
        <div class="form-group">
          <label>用户名</label>
          <input
            type="text"
            v-model="registerForm.username"
            placeholder="请设置用户名"
            :class="{ error: errors.username }"
          >
          <p class="error-text">{{ errors.username }}</p>
        </div>

        <!-- 邮箱输入 -->
        <div class="form-group">
          <label>邮箱</label>
          <input
            type="email"
            v-model="registerForm.email"
            placeholder="请输入邮箱"
            :class="{ error: errors.email }"
          >
          <p class="error-text">{{ errors.email }}</p>
        </div>

        <!-- 密码输入 -->
        <div class="form-group">
          <label>密码</label>
          <input
            type="password"
            v-model="registerForm.password"
            placeholder="请设置密码（至少6位）"
            :class="{ error: errors.password }"
          >
          <p class="error-text">{{ errors.password }}</p>
        </div>

        <!-- 确认密码 -->
        <div class="form-group">
          <label>确认密码</label>
          <input
            type="password"
            v-model="registerForm.confirmPassword"
            placeholder="请再次输入密码"
            :class="{ error: errors.confirmPassword }"
          >
          <p class="error-text">{{ errors.confirmPassword }}</p>
        </div>

        <!-- 注册按钮 -->
        <button type="submit" class="auth-btn" :disabled="isLoading">
          <span v-if="!isLoading">注册</span>
          <span v-if="isLoading">注册中...</span>
        </button>

        <!-- 切换到登录 -->
        <p class="toggle-link">
          已有账号？<router-link to="/login">去登录</router-link>
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
const errors = reactive({
  username: '',
  email: '',
  password: '',
  confirmPassword: ''
});

// 注册表单数据
const registerForm = reactive({
  username: '',
  email: '',
  password: '',
  confirmPassword: ''
});

// 表单验证
const validateForm = () => {
  let isValid = true;
  const emailReg = /^[^\s@]+@[^\s@]+\.[^\s@]+$/; // 邮箱格式正则

  // 用户名验证
  if (!registerForm.username.trim()) {
    errors.username = '请输入用户名';
    isValid = false;
  } else if (registerForm.username.length < 3) {
    errors.username = '用户名长度不能少于3位';
    isValid = false;
  }

  // 邮箱验证
  if (!registerForm.email.trim()) {
    errors.email = '请输入邮箱';
    isValid = false;
  } else if (!emailReg.test(registerForm.email)) {
    errors.email = '请输入有效的邮箱地址';
    isValid = false;
  }

  // 密码验证
  if (!registerForm.password) {
    errors.password = '请设置密码';
    isValid = false;
  } else if (registerForm.password.length < 6) {
    errors.password = '密码长度不能少于6位';
    isValid = false;
  }

  // 确认密码验证
  if (!registerForm.confirmPassword) {
    errors.confirmPassword = '请再次输入密码';
    isValid = false;
  } else if (registerForm.confirmPassword !== registerForm.password) {
    errors.confirmPassword = '两次输入的密码不一致';
    isValid = false;
  }

  return isValid;
};

// 注册逻辑
const handleRegister = async () => {
  if (!validateForm()) return;

  isLoading.value = true;
  try {
    // 调用后端注册接口
    await api.post('/auth/register', {
      username: registerForm.username,
      email: registerForm.email,
      password: registerForm.password
    });

    // 注册成功：跳转到登录页
    router.push({
      path: '/login',
      query: { message: '注册成功，请登录' }
    });
  } catch (err) {
    // 处理注册错误（如邮箱已被注册）
    const errMsg = err.response?.data?.message;
    if (errMsg.includes('邮箱')) {
      errors.email = errMsg;
    } else if (errMsg.includes('用户名')) {
      errors.username = errMsg;
    } else {
      alert(errMsg || '注册失败，请稍后再试');
    }
  } finally {
    isLoading.value = false;
  }
};
</script>

<style scoped>
/* 样式与登录组件一致，可复用（实际开发中建议提取为公共样式） */
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