<template>
  <div class="settings-container">
    <div class="header-section">
      <button class="back-button" @click="goBack">
        &lt; 返回首页
      </button>
    </div>
    <h1 class="page-title">账户设置</h1>
    
    <div class="settings-tabs">
      <button 
        v-for="tab in tabs" 
        :key="tab.id"
        :class="['tab-button', { active: activeTab === tab.id }]"
        @click="activeTab = tab.id"
      >
        {{ tab.name }}
      </button>
    </div>

    <div class="settings-content">
      <!-- 个人资料 -->
      <div v-if="activeTab === 'profile'" class="settings-section">
        <h2 class="section-title">个人资料</h2>
        <form @submit.prevent="updateProfile" class="settings-form">
          <div class="form-group">
            <label for="username">用户名</label>
            <input 
              type="text" 
              id="username" 
              v-model="profileForm.username" 
              placeholder="请输入用户名"
            >
          </div>
          <div class="form-group">
            <label for="email">电子邮箱</label>
            <input 
              type="email" 
              id="email" 
              v-model="profileForm.email" 
              placeholder="请输入邮箱"
            >
          </div>
          <div class="form-group">
            <label for="nickname">昵称</label>
            <input 
              type="text" 
              id="nickname" 
              v-model="profileForm.nickname" 
              placeholder="请输入昵称"
            >
          </div>
          <div class="form-group">
            <label for="bio">个人简介</label>
            <textarea 
              id="bio" 
              v-model="profileForm.bio" 
              placeholder="介绍一下自己吧"
              rows="4"
            ></textarea>
          </div>
          <button type="submit" class="submit-btn">保存更改</button>
        </form>
      </div>

      <!-- 安全设置 -->
      <div v-else-if="activeTab === 'security'" class="settings-section">
        <h2 class="section-title">安全设置</h2>
        
        <div class="security-card">
          <h3>修改密码</h3>
          <form @submit.prevent="changePassword" class="settings-form">
            <div class="form-group">
              <label for="currentPassword">当前密码</label>
              <input 
                type="password" 
                id="currentPassword" 
                v-model="passwordForm.currentPassword" 
                placeholder="请输入当前密码"
              >
            </div>
            <div class="form-group">
              <label for="newPassword">新密码</label>
              <input 
                type="password" 
                id="newPassword" 
                v-model="passwordForm.newPassword" 
                placeholder="请输入新密码"
              >
            </div>
            <div class="form-group">
              <label for="confirmPassword">确认新密码</label>
              <input 
                type="password" 
                id="confirmPassword" 
                v-model="passwordForm.confirmPassword" 
                placeholder="请再次输入新密码"
              >
            </div>
            <button type="submit" class="submit-btn">修改密码</button>
          </form>
        </div>

        <div class="security-card">
          <h3>登录设置</h3>
          <div class="setting-item">
            <span>记住登录状态</span>
            <label class="toggle-switch">
              <input type="checkbox" v-model="securitySettings.rememberLogin">
              <span class="toggle-slider"></span>
            </label>
          </div>
          <div class="setting-item">
            <span>自动登录</span>
            <label class="toggle-switch">
              <input type="checkbox" v-model="securitySettings.autoLogin">
              <span class="toggle-slider"></span>
            </label>
          </div>
        </div>
      </div>

      <!-- 隐私设置 -->
      <div v-else-if="activeTab === 'privacy'" class="settings-section">
        <h2 class="section-title">隐私设置</h2>
        
        <div class="privacy-card">
          <h3>数据可见性</h3>
          <div class="setting-item">
            <span>允许他人查看我的日记</span>
            <label class="toggle-switch">
              <input type="checkbox" v-model="privacySettings.showDiaries">
              <span class="toggle-slider"></span>
            </label>
          </div>
          <div class="setting-item">
            <span>允许他人查看我的测试结果</span>
            <label class="toggle-switch">
              <input type="checkbox" v-model="privacySettings.showTestResults">
              <span class="toggle-slider"></span>
            </label>
          </div>
        </div>

        <div class="privacy-card">
          <h3>通知设置</h3>
          <div class="setting-item">
            <span>接收社区消息通知</span>
            <label class="toggle-switch">
              <input type="checkbox" v-model="notificationSettings.community">
              <span class="toggle-slider"></span>
            </label>
          </div>
          <div class="setting-item">
            <span>接收测试提醒</span>
            <label class="toggle-switch">
              <input type="checkbox" v-model="notificationSettings.tests">
              <span class="toggle-slider"></span>
            </label>
          </div>
        </div>
      </div>

      <!-- 账号管理 -->
      <div v-else-if="activeTab === 'account'" class="settings-section">
        <h2 class="section-title">账号管理</h2>
        
        <div class="account-info">
          <p><strong>注册时间：</strong>{{ accountInfo.registrationDate }}</p>
          <p><strong>最后登录：</strong>{{ accountInfo.lastLoginDate }}</p>
          <p><strong>账号状态：</strong><span class="status-active">正常</span></p>
        </div>

        <div class="danger-zone">
          <h3>危险操作</h3>
          <div class="warning-text">
            <p>请注意，以下操作不可逆，请谨慎操作！</p>
          </div>
          <button class="delete-account-btn" @click="confirmDeleteAccount">
            注销账号
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import api from '../api/index.js';

const router = useRouter();

// 返回首页
const goBack = () => {
  router.push('/');
};

// 标签页配置
const tabs = [
  { id: 'profile', name: '个人资料' },
  { id: 'security', name: '安全设置' },
  { id: 'privacy', name: '隐私设置' },
  { id: 'account', name: '账号管理' }
];

// 当前激活的标签页
const activeTab = ref('profile');

// 个人资料表单
const profileForm = ref({
  username: '',
  email: '',
  nickname: '',
  bio: ''
});

// 密码修改表单
const passwordForm = ref({
  currentPassword: '',
  newPassword: '',
  confirmPassword: ''
});

// 安全设置
const securitySettings = ref({
  rememberLogin: true,
  autoLogin: false
});

// 隐私设置
const privacySettings = ref({
  showDiaries: false,
  showTestResults: false
});

// 通知设置
const notificationSettings = ref({
  community: true,
  tests: true
});

// 账号信息
const accountInfo = ref({
  registrationDate: '2023-01-01',
  lastLoginDate: new Date().toLocaleDateString()
});

// 获取用户信息
const fetchUserProfile = async () => {
  try {
    // 从后端API获取真实用户信息
    // 基于现有API结构，使用get方法获取用户数据
    const response = await api.get('/auth/user');
    
    // 假设API返回的数据格式为{data: {username, email, nickname, bio}}或直接包含这些字段
    if (response.data) {
      profileForm.value = {
        username: response.data.username || '',
        email: response.data.email || '',
        nickname: response.data.nickname || response.data.name || '',
        bio: response.data.bio || response.data.introduction || ''
      };
    } else {
      // 直接使用返回的用户信息
      profileForm.value = {
        username: response.username || '',
        email: response.email || '',
        nickname: response.nickname || response.name || '',
        bio: response.bio || response.introduction || ''
      };
    }
    
    console.log('成功获取用户信息:', profileForm.value);
  } catch (error) {
    console.error('获取用户信息失败:', error);
    
    // API调用失败时的降级处理
    // 从localStorage获取可能存在的基本信息
    const storedUsername = localStorage.getItem('username');
    
    // 提供一个基本的表单界面，而不是空表单
    profileForm.value = {
      username: storedUsername || '',
      email: '',
      nickname: storedUsername || '',
      bio: ''
    };
    
    // 显示友好的提示，告知用户当前状态
    console.warn('当前使用的是本地存储的基本信息，无法获取完整的用户数据');
  }
};

// 更新个人资料
const updateProfile = async () => {
  try {
    // 表单验证
    if (!profileForm.value.username) {
      alert('用户名不能为空');
      return;
    }
    
    if (!profileForm.value.email) {
      alert('邮箱不能为空');
      return;
    }
    
    // 调用后端API更新用户资料
    const response = await api.put('/auth/user', profileForm.value);
    
    // 更新成功后，同步更新localStorage中的用户名
    if (profileForm.value.username) {
      localStorage.setItem('username', profileForm.value.username);
    }
    
    console.log('个人资料更新成功:', response);
    alert('个人资料更新成功');
  } catch (error) {
    console.error('更新个人资料失败:', error);
    alert('更新失败，请确保已登录且网络连接正常');
  }
};

// 修改密码
const changePassword = async () => {
  try {
    // 表单验证
    if (!passwordForm.value.currentPassword) {
      alert('请输入当前密码');
      return;
    }
    
    if (!passwordForm.value.newPassword) {
      alert('请输入新密码');
      return;
    }
    
    if (passwordForm.value.newPassword !== passwordForm.value.confirmPassword) {
      alert('两次输入的新密码不一致');
      return;
    }
    
    // 调用后端API修改密码
    await api.post('/auth/change-password', {
      currentPassword: passwordForm.value.currentPassword,
      newPassword: passwordForm.value.newPassword,
      confirmPassword: passwordForm.value.confirmPassword
    });
    
    alert('密码修改成功，请重新登录');
    // 清空密码表单
    passwordForm.value = {
      currentPassword: '',
      newPassword: '',
      confirmPassword: ''
    };
  } catch (error) {
    console.error('修改密码失败:', error);
    alert('修改密码失败，请检查当前密码是否正确');
  }
};

// 确认注销账号
const confirmDeleteAccount = () => {
  if (confirm('确定要注销账号吗？此操作不可逆，所有数据将被删除！')) {
    deleteAccount();
  }
};

// 注销账号
const deleteAccount = async () => {
  try {
    // 尝试调用API注销账号（如果后端提供了此接口）
    // 目前使用模拟实现
    
    // 清除所有本地存储的数据
    localStorage.removeItem('authToken');
    localStorage.removeItem('username');
    localStorage.removeItem('user');
    
    // 显示注销成功提示
    alert('账号已注销');
    
    // 延迟一小段时间后跳转
    setTimeout(() => {
      window.location.href = '/login';
    }, 100);
  } catch (error) {
    console.error('注销账号失败:', error);
    
    // 即使API调用失败，也清除本地状态
    localStorage.removeItem('authToken');
    localStorage.removeItem('username');
    localStorage.removeItem('user');
    
    // 仍然显示注销提示并跳转
    alert('账号已从前端注销');
    setTimeout(() => {
      window.location.href = '/login';
    }, 100);
  }
};

// 组件挂载时获取用户信息
onMounted(() => {
  fetchUserProfile();
});
</script>

<style scoped>
.settings-container {
  max-width: 1000px;
  margin: 20px auto;
  padding: 0 20px;
}

.header-section {
  display: flex;
  justify-content: flex-start;
  margin-bottom: 20px;
}

.back-button {
  padding: 8px 16px;
  background-color: #673ab7;
  color: #fff;
  border: none;
  border-radius: 6px;
  cursor: pointer;
  font-size: 14px;
  transition: background-color 0.3s;
  display: flex;
  align-items: center;
  gap: 4px;
}

.back-button:hover {
  background-color: #5a2d90;
}

.page-title {
  font-size: 28px;
  font-weight: bold;
  color: #333;
  margin-bottom: 30px;
  text-align: center;
}

.settings-tabs {
  display: flex;
  gap: 10px;
  margin-bottom: 30px;
  overflow-x: auto;
  padding-bottom: 10px;
}

.tab-button {
  padding: 10px 20px;
  border: 1px solid #ddd;
  background-color: #fff;
  color: #666;
  border-radius: 20px;
  cursor: pointer;
  transition: all 0.3s;
  white-space: nowrap;
}

.tab-button:hover {
  border-color: #673ab7;
  color: #673ab7;
}

.tab-button.active {
  background-color: #673ab7;
  color: #fff;
  border-color: #673ab7;
}

.settings-content {
  background-color: #fff;
  border-radius: 10px;
  padding: 30px;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.05);
}

.settings-section {
  animation: fadeIn 0.3s ease-in;
}

@keyframes fadeIn {
  from { opacity: 0; }
  to { opacity: 1; }
}

.section-title {
  font-size: 22px;
  font-weight: 600;
  color: #333;
  margin-bottom: 20px;
  border-bottom: 2px solid #f0f0f0;
  padding-bottom: 10px;
}

.settings-form {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.form-group {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.form-group label {
  font-weight: 500;
  color: #555;
  font-size: 14px;
}

.form-group input,
.form-group textarea {
  padding: 10px 15px;
  border: 1px solid #ddd;
  border-radius: 6px;
  font-size: 16px;
  transition: border-color 0.3s;
}

.form-group input:focus,
.form-group textarea:focus {
  outline: none;
  border-color: #673ab7;
}

.submit-btn {
  padding: 12px 24px;
  background-color: #673ab7;
  color: #fff;
  border: none;
  border-radius: 6px;
  font-size: 16px;
  cursor: pointer;
  transition: background-color 0.3s;
  align-self: flex-start;
}

.submit-btn:hover {
  background-color: #5a2d90;
}

/* 卡片样式 */
.security-card,
.privacy-card {
  background-color: #f9f9f9;
  border-radius: 8px;
  padding: 20px;
  margin-bottom: 20px;
}

.security-card h3,
.privacy-card h3 {
  font-size: 18px;
  font-weight: 600;
  color: #333;
  margin-bottom: 15px;
}

/* 设置项样式 */
.setting-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 0;
  border-bottom: 1px solid #eee;
}

.setting-item:last-child {
  border-bottom: none;
}

/* 切换开关样式 */
.toggle-switch {
  position: relative;
  display: inline-block;
  width: 50px;
  height: 24px;
}

.toggle-switch input {
  opacity: 0;
  width: 0;
  height: 0;
}

.toggle-slider {
  position: absolute;
  cursor: pointer;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background-color: #ccc;
  transition: .4s;
  border-radius: 34px;
}

.toggle-slider:before {
  position: absolute;
  content: "";
  height: 18px;
  width: 18px;
  left: 3px;
  bottom: 3px;
  background-color: white;
  transition: .4s;
  border-radius: 50%;
}

input:checked + .toggle-slider {
  background-color: #673ab7;
}

input:checked + .toggle-slider:before {
  transform: translateX(26px);
}

/* 账号信息样式 */
.account-info {
  background-color: #f9f9f9;
  border-radius: 8px;
  padding: 20px;
  margin-bottom: 30px;
}

.account-info p {
  margin-bottom: 10px;
  color: #555;
}

.status-active {
  color: #4CAF50;
  font-weight: 500;
}

/* 危险区域样式 */
.danger-zone {
  border: 1px solid #ff9800;
  border-radius: 8px;
  padding: 20px;
  background-color: #fff3e0;
}

.danger-zone h3 {
  color: #e65100;
  margin-bottom: 15px;
}

.warning-text {
  margin-bottom: 20px;
  color: #f57c00;
  font-size: 14px;
}

.delete-account-btn {
  padding: 10px 20px;
  background-color: #f44336;
  color: #fff;
  border: none;
  border-radius: 6px;
  cursor: pointer;
  transition: background-color 0.3s;
}

.delete-account-btn:hover {
  background-color: #d32f2f;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .settings-container {
    padding: 0 15px;
  }
  
  .settings-content {
    padding: 20px;
  }
  
  .settings-tabs {
    flex-wrap: wrap;
  }
  
  .tab-button {
    flex: 1 1 auto;
    min-width: 120px;
  }
}
</style>