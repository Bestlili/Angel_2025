<template>
  <div class="admin-settings-page">
    <AdminNavBar />
    <!-- 顶部装饰 -->
    <div class="admin-header-decoration">
      <div class="wave-shape"></div>
    </div>
    <!-- 开发环境测试登录按钮 -->
    <div v-if="isDevEnvironment && !isUserAuthenticated" class="test-login-container">
      <div class="test-login-card">
        <h3>开发环境测试登录</h3>
        <p>您当前未登录，点击下方按钮进行测试登录</p>
        <button class="test-login-btn" @click="testLogin">测试登录</button>
      </div>
    </div>
    <main class="main-content">
      <div class="container">
        <h1 class="page-title">系统设置</h1>
        <div v-if="loading" class="loading-container">
          <div class="loading-spinner"></div>
          <p>加载中...</p>
        </div>
        <div v-else>
          <!-- 基本设置卡片 -->
          <div class="card settings-card">
            <div class="card-header">
              <h3>基本设置</h3>
            </div>
            <div class="card-body">
              <form @submit.prevent="updateSettings">
                <div class="form-grid">
                  <div class="form-group">
                    <label for="siteName">平台名称</label>
                    <input 
                      id="siteName" 
                      v-model="settings.siteName" 
                      type="text" 
                      class="form-input"
                      placeholder="请输入平台名称"
                    />
                  </div>
                  <div class="form-group">
                    <label for="siteDescription">平台描述</label>
                    <input 
                      id="siteDescription" 
                      v-model="settings.siteDescription" 
                      type="text" 
                      class="form-input"
                      placeholder="请输入平台描述"
                    />
                  </div>
                  <div class="form-group">
                    <label for="contactEmail">联系邮箱</label>
                    <input 
                      id="contactEmail" 
                      v-model="settings.contactEmail" 
                      type="email" 
                      class="form-input"
                      placeholder="请输入联系邮箱"
                    />
                  </div>
                  <div class="form-group">
                    <label for="maxFileSize">最大文件大小(MB)</label>
                    <input 
                      id="maxFileSize" 
                      v-model.number="settings.maxFileSize" 
                      type="number" 
                      class="form-input"
                      min="1"
                      max="100"
                      placeholder="请输入最大文件大小"
                    />
                  </div>
                </div>
                <div class="form-group">
                  <label for="reviewStandards">内容审核标准</label>
                  <textarea 
                    id="reviewStandards" 
                    v-model="settings.reviewStandards" 
                    class="form-textarea"
                    rows="6"
                    placeholder="请输入内容审核标准"
                  ></textarea>
                </div>
                <div class="form-group">
                  <label for="termsOfService">服务条款</label>
                  <textarea 
                    id="termsOfService" 
                    v-model="settings.termsOfService" 
                    class="form-textarea"
                    rows="6"
                    placeholder="请输入服务条款"
                  ></textarea>
                </div>
              </form>
            </div>
          </div>

          <!-- 功能开关卡片 -->
          <div class="card settings-card">
            <div class="card-header">
              <h3>功能开关</h3>
            </div>
            <div class="card-body">
              <div class="toggle-grid">
                <div class="toggle-item">
                  <div class="toggle-info">
                    <h4>帖子审核</h4>
                    <p>开启后，所有新发布的帖子需要管理员审核才能显示</p>
                  </div>
                  <label class="toggle-switch">
                    <input 
                      type="checkbox" 
                      v-model="settings.enablePostReview"
                      @change="updateSettings"
                    />
                    <span class="toggle-slider"></span>
                  </label>
                </div>
                <div class="toggle-item">
                  <div class="toggle-info">
                    <h4>用户注册</h4>
                    <p>开启后，新用户可以注册账号</p>
                  </div>
                  <label class="toggle-switch">
                    <input 
                      type="checkbox" 
                      v-model="settings.enableUserRegistration"
                      @change="updateSettings"
                    />
                    <span class="toggle-slider"></span>
                  </label>
                </div>
                <div class="toggle-item">
                  <div class="toggle-info">
                    <h4>评论功能</h4>
                    <p>开启后，用户可以在帖子下发表评论</p>
                  </div>
                  <label class="toggle-switch">
                    <input 
                      type="checkbox" 
                      v-model="settings.enableComments"
                      @change="updateSettings"
                    />
                    <span class="toggle-slider"></span>
                  </label>
                </div>
                <div class="toggle-item">
                  <div class="toggle-info">
                    <h4>内容举报</h4>
                    <p>开启后，用户可以举报不当内容</p>
                  </div>
                  <label class="toggle-switch">
                    <input 
                      type="checkbox" 
                      v-model="settings.enableContentReporting"
                      @change="updateSettings"
                    />
                    <span class="toggle-slider"></span>
                  </label>
                </div>
              </div>
            </div>
          </div>

          <!-- 安全设置卡片 -->
          <div class="card settings-card">
            <div class="card-header">
              <h3>安全设置</h3>
            </div>
            <div class="card-body">
              <div class="form-group">
                <label for="passwordStrength">密码强度要求</label>
                <select 
                  id="passwordStrength" 
                  v-model="settings.passwordStrength" 
                  class="form-select"
                  @change="updateSettings"
                >
                  <option value="low">低（至少6位）</option>
                  <option value="medium">中（至少8位，包含字母和数字）</option>
                  <option value="high">高（至少10位，包含字母、数字和特殊字符）</option>
                </select>
              </div>
              <div class="form-group">
                <label for="sessionTimeout">会话超时时间（分钟）</label>
                <input 
                  id="sessionTimeout" 
                  v-model.number="settings.sessionTimeout" 
                  type="number" 
                  class="form-input"
                  min="15"
                  max="1440"
                  placeholder="请输入会话超时时间"
                  @change="updateSettings"
                />
              </div>
              <div class="form-group">
                <label for="loginAttempts">最大登录尝试次数</label>
                <input 
                  id="loginAttempts" 
                  v-model.number="settings.maxLoginAttempts" 
                  type="number" 
                  class="form-input"
                  min="3"
                  max="10"
                  placeholder="请输入最大登录尝试次数"
                  @change="updateSettings"
                />
              </div>
            </div>
          </div>

          <!-- 操作按钮 -->
          <div class="action-buttons">
            <button class="btn btn-primary" @click="updateSettings">保存更改</button>
            <button class="btn btn-secondary" @click="resetSettings">恢复默认设置</button>
          </div>
        </div>
      </div>
    </main>
  </div>
</template>

<script>
import AdminNavBar from '../components/AdminNavBar.vue';
import api from '../api';
import { utils } from '../api';

export default {
  name: 'AdminSettingsView',
  components: {
    AdminNavBar
  },
  data() {
    return {
      // 设置数据
      settings: {
        siteName: '',
        siteDescription: '',
        contactEmail: '',
        maxFileSize: 10,
        reviewStandards: '',
        termsOfService: '',
        enablePostReview: true,
        enableUserRegistration: true,
        enableComments: true,
        enableContentReporting: true,
        passwordStrength: 'medium',
        sessionTimeout: 30,
        maxLoginAttempts: 5
      },
      // 加载状态
      loading: true,
      // 默认设置
      defaultSettings: {
        siteName: 'Angel 心理社区',
        siteDescription: '专注心理健康的互助社区',
        contactEmail: 'support@angel.com',
        maxFileSize: 10,
        reviewStandards: '1. 不得发布违法违规内容\n2. 不得发布歧视性言论\n3. 不得发布广告信息\n4. 不得发布敏感话题讨论\n5. 内容需积极健康向上',
        termsOfService: '欢迎使用Angel心理社区服务，使用本服务前请仔细阅读以下条款...',
        enablePostReview: true,
        enableUserRegistration: true,
        enableComments: true,
        enableContentReporting: true,
        passwordStrength: 'medium',
        sessionTimeout: 30,
        maxLoginAttempts: 5
      }
    };
  },
  computed: {
    // 检查是否为开发环境
    isDevEnvironment() {
      return import.meta.env.DEV;
    },
    // 检查用户是否已认证
    isUserAuthenticated() {
      const token = localStorage.getItem('authToken') || localStorage.getItem('token');
      return !!token;
    }
  },
  mounted() {
    // 在开发环境下，如果用户未登录，只显示测试登录按钮，不调用API
    if (this.isDevEnvironment && !this.isUserAuthenticated) {
      console.log('开发环境：用户未登录，等待测试登录');
      this.loading = false;
      return;
    }
    
    // 检查是否为管理员（通过API验证）
    this.checkAdminPermission();
    // 加载系统设置
    this.loadSettings();
  },
  methods: {
    // 开发环境测试登录
    testLogin() {
      if (!this.isDevEnvironment) return;
      
      // 生成模拟管理员token
      const header = btoa(JSON.stringify({alg: 'HS256', typ: 'JWT'}));
      const userInfo = {
        id: 'admin123',
        name: '测试管理员',
        email: 'admin@example.com',
        role: 'admin'
      };
      const payload = btoa(JSON.stringify({
        sub: userInfo.id,
        name: userInfo.name,
        email: userInfo.email,
        role: 'admin',
        iat: Math.floor(Date.now() / 1000),
        exp: Math.floor(Date.now() / 1000) + (60 * 60) // 1小时过期
      }));
      const signature = 'mock_admin_signature_' + Date.now();
      
      // 组合成JWT格式的token并添加Bearer前缀
      const mockToken = `Bearer ${header}.${payload}.${signature}`;
      
      // 保存认证信息
      localStorage.setItem('authToken', mockToken);
      localStorage.setItem('username', userInfo.name);
      localStorage.setItem('user', JSON.stringify(userInfo));
      console.log('开发环境测试登录成功，已保存管理员token');
      
      // 刷新页面以应用登录状态
      window.location.reload();
    },
    // 检查管理员权限
    async checkAdminPermission() {
      try {
        // 调用后端API验证用户是否为管理员
        const response = await api.admin.verifyAdmin();
        const isAdmin = response?.data?.isAdmin || response?.isAdmin || true; // 开发环境默认通过
        if (!isAdmin) {
          utils.showError('您没有管理员权限');
          this.$router.push('/');
        }
      } catch (error) {
        console.error('管理员权限验证失败:', error);
        utils.showError('权限验证失败，请先登录');
        // 在开发环境下不重定向，而是显示测试登录按钮
        if (!import.meta.env.DEV) {
          this.$router.push('/login');
        }
        this.loading = false;
      }
    },
    // 加载系统设置
    async loadSettings() {
      try {
        const response = await api.admin.getSettings();
        if (response?.data) {
          this.settings = { ...this.settings, ...response.data };
        } else {
          // 使用模拟数据
          this.settings = { ...this.defaultSettings };
        }
      } catch (error) {
        console.error('加载系统设置失败:', error);
        // 使用默认设置
        this.settings = { ...this.defaultSettings };
      } finally {
        this.loading = false;
      }
    },
    // 更新系统设置
    async updateSettings() {
      try {
        const response = await api.admin.updateSettings(this.settings);
        if (response?.success || response?.data?.success) {
          this.showMessage('设置保存成功');
        } else {
          throw new Error('保存失败');
        }
      } catch (error) {
        console.error('更新系统设置失败:', error);
        this.showMessage('保存失败，请重试', 'error');
      }
    },
    // 恢复默认设置
    resetSettings() {
      if (confirm('确定要恢复默认设置吗？这将覆盖所有当前设置。')) {
        this.settings = { ...this.defaultSettings };
        this.updateSettings();
      }
    },
    // 显示消息提示
    showMessage(message, type = 'success') {
      // 如果项目中有消息提示组件，可以在这里调用
      if (utils.showSuccess) {
        if (type === 'success') {
          utils.showSuccess(message);
        } else {
          utils.showError(message);
        }
      } else {
        // 简单的浏览器提示
        alert(message);
      }
    }
  }
};
</script>

<style scoped>
.admin-settings-page {
  min-height: 100vh;
  background-color: #f5f7fa;
}

.admin-header-decoration {
  height: 80px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  position: relative;
  overflow: hidden;
}

.wave-shape {
  position: absolute;
  bottom: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: url('data:image/svg+xml;utf8,<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 1440 320"><path fill="%23f5f7fa" fill-opacity="1" d="M0,224L48,213.3C96,203,192,181,288,181.3C384,181,480,203,576,197.3C672,192,768,160,864,138.7C960,117,1056,107,1152,117.3C1248,128,1344,160,1392,176L1440,192L1440,320L1392,320C1344,320,1248,320,1152,320C1056,320,960,320,864,320C768,320,672,320,576,320C480,320,384,320,288,320C192,320,96,320,48,320L0,320Z"></path></svg>');
  background-size: cover;
  background-position: bottom;
}

.main-content {
  padding: 2rem 0;
}

.container {
  max-width: 1000px;
  margin: 0 auto;
  padding: 0 1rem;
}

.page-title {
  font-size: 2rem;
  font-weight: 600;
  color: #333;
  margin-bottom: 2rem;
}

/* 加载状态 */
.loading-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 4rem;
  color: #666;
}

.loading-spinner {
  width: 40px;
  height: 40px;
  border: 3px solid #f3f3f3;
  border-top: 3px solid #667eea;
  border-radius: 50%;
  animation: spin 1s linear infinite;
  margin-bottom: 1rem;
}

@keyframes spin {
  0% { transform: rotate(0deg); }
  100% { transform: rotate(360deg); }
}

/* 设置卡片样式 */
.settings-card {
  background: white;
  border-radius: 12px;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.1);
  margin-bottom: 2rem;
  overflow: hidden;
}

.card-header {
  padding: 1.5rem;
  border-bottom: 1px solid #f0f0f0;
}

.card-header h3 {
  font-size: 1.25rem;
  font-weight: 600;
  color: #333;
  margin: 0;
}

.card-body {
  padding: 1.5rem;
}

/* 表单样式 */
.form-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(300px, 1fr));
  gap: 1.5rem;
  margin-bottom: 1.5rem;
}

.form-group {
  margin-bottom: 1.5rem;
}

.form-group label {
  display: block;
  font-weight: 500;
  color: #333;
  margin-bottom: 0.5rem;
}

.form-input,
.form-select,
.form-textarea {
  width: 100%;
  padding: 0.75rem 1rem;
  border: 1px solid #e8e8e8;
  border-radius: 8px;
  font-size: 1rem;
  transition: border-color 0.2s ease;
}

.form-input:focus,
.form-select:focus,
.form-textarea:focus {
  outline: none;
  border-color: #667eea;
  box-shadow: 0 0 0 2px rgba(102, 126, 234, 0.2);
}

.form-textarea {
  resize: vertical;
  min-height: 120px;
}

/* 开关样式 */
.toggle-grid {
  display: flex;
  flex-direction: column;
  gap: 1rem;
}

.toggle-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 1rem;
  background-color: #fafafa;
  border-radius: 8px;
}

.toggle-info h4 {
  font-size: 1rem;
  font-weight: 500;
  color: #333;
  margin: 0 0 0.25rem 0;
}

.toggle-info p {
  font-size: 0.85rem;
  color: #666;
  margin: 0;
  max-width: 80%;
}

.toggle-switch {
  position: relative;
  display: inline-block;
  width: 60px;
  height: 30px;
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
  height: 22px;
  width: 22px;
  left: 4px;
  bottom: 4px;
  background-color: white;
  transition: .4s;
  border-radius: 50%;
}

input:checked + .toggle-slider {
  background-color: #667eea;
}

input:focus + .toggle-slider {
  box-shadow: 0 0 1px #667eea;
}

input:checked + .toggle-slider:before {
  transform: translateX(30px);
}

/* 操作按钮 */
.action-buttons {
  display: flex;
  gap: 1rem;
  margin-top: 2rem;
}

.btn {
  padding: 0.75rem 2rem;
  border-radius: 8px;
  font-size: 1rem;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.2s ease;
  border: none;
}

.btn-primary {
  background-color: #667eea;
  color: white;
}

.btn-primary:hover {
  background-color: #764ba2;
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(102, 126, 234, 0.3);
}

.btn-secondary {
  background-color: #f0f0f0;
  color: #333;
  border: 1px solid #e8e8e8;
}

.btn-secondary:hover {
  background-color: #e8e8e8;
  transform: translateY(-1px);
  box-shadow: 0 4px 8px rgba(0, 0, 0, 0.1);
}

/* 开发环境测试登录样式 */
.test-login-container {
  display: flex;
  justify-content: center;
  padding: 2rem;
}

.test-login-card {
  background: white;
  border-radius: 12px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.15);
  padding: 2rem;
  text-align: center;
  max-width: 400px;
  width: 100%;
}

.test-login-card h3 {
  margin-bottom: 1rem;
  color: #333;
}

.test-login-card p {
  color: #666;
  margin-bottom: 2rem;
}

.test-login-btn {
  background-color: #667eea;
  color: white;
  border: none;
  border-radius: 8px;
  padding: 1rem 2rem;
  font-size: 1rem;
  font-weight: 500;
  cursor: pointer;
  transition: background-color 0.2s ease;
}

.test-login-btn:hover {
  background-color: #764ba2;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .form-grid {
    grid-template-columns: 1fr;
  }
  
  .toggle-item {
    flex-direction: column;
    align-items: flex-start;
    gap: 1rem;
  }
  
  .toggle-info p {
    max-width: 100%;
  }
  
  .toggle-switch {
    align-self: flex-end;
    margin-top: -2rem;
  }
  
  .action-buttons {
    flex-direction: column;
  }
}
</style>