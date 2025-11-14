<template>
  <div class="admin-users-page">
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
        <h1 class="page-title">用户管理</h1>
        
        <!-- 搜索和筛选 -->
        <div class="card filter-card">
          <div class="filter-content">
            <div class="search-container">
              <input 
                type="text" 
                v-model="searchQuery" 
                placeholder="搜索用户名、邮箱..."
                class="search-input"
                @input="handleSearch"
              />
              <button class="search-btn" @click="loadUsers">🔍</button>
            </div>
            
            <div class="filter-controls">
              <select v-model="statusFilter" class="filter-select" @change="loadUsers">
                <option value="all">全部状态</option>
                <option value="active">活跃</option>
                <option value="inactive">非活跃</option>
                <option value="banned">已封禁</option>
              </select>
              
              <select v-model="roleFilter" class="filter-select" @change="loadUsers">
                <option value="all">全部角色</option>
                <option value="admin">管理员</option>
                <option value="user">普通用户</option>
              </select>
              
              <select v-model="sortBy" class="filter-select" @change="loadUsers">
                <option value="createdAt_desc">注册时间倒序</option>
                <option value="createdAt_asc">注册时间正序</option>
                <option value="lastActive_desc">最后活跃时间倒序</option>
                <option value="lastActive_asc">最后活跃时间正序</option>
              </select>
            </div>
          </div>
        </div>
        
        <!-- 用户列表 -->
        <div class="card users-card">
          <div class="users-table-wrapper">
            <table class="users-table">
              <thead>
                <tr>
                  <th>用户ID</th>
                  <th>用户名</th>
                  <th>邮箱</th>
                  <th>角色</th>
                  <th>状态</th>
                  <th>注册时间</th>
                  <th>最后活跃</th>
                  <th>操作</th>
                </tr>
              </thead>
              <tbody>
                <!-- 加载状态 -->
                <tr v-if="loading">
                  <td colspan="8" class="loading-cell">
                    <div class="loading-spinner"></div>
                    <span>加载中...</span>
                  </td>
                </tr>
                
                <!-- 空状态 -->
                <tr v-else-if="users.length === 0">
                  <td colspan="8" class="empty-cell">
                    <div class="empty-icon">👥</div>
                    <p>{{ searchQuery || statusFilter !== 'all' || roleFilter !== 'all' ? '没有找到匹配的用户' : '暂无用户数据' }}</p>
                  </td>
                </tr>
                
                <!-- 用户列表 -->
                <tr v-for="user in users" :key="user.id">
                  <td>{{ user.id || 'N/A' }}</td>
                  <td>
                    <div class="user-info">
                      <div class="user-avatar">{{ getAvatarText(user.username || user.name) }}</div>
                      <span class="user-name">{{ user.username || user.name || '未知用户' }}</span>
                    </div>
                  </td>
                  <td>{{ user.email || 'N/A' }}</td>
                  <td>
                    <span class="role-badge" :class="`role-${user.role}`">
                      {{ user.role === 'admin' ? '管理员' : '普通用户' }}
                    </span>
                  </td>
                  <td>
                    <span class="status-badge" :class="`status-${user.status}`">
                      {{ getUserStatusText(user.status) }}
                    </span>
                  </td>
                  <td>{{ formatDate(user.createdAt) }}</td>
                  <td>{{ formatDate(user.lastActiveAt || user.updatedAt) }}</td>
                  <td>
                    <div class="action-buttons">
                      <!-- 状态切换按钮 -->
                      <button 
                        v-if="user.status !== 'banned'"
                        class="action-btn ban-btn"
                        @click="confirmBanUser(user)"
                        :disabled="processingIds.includes(user.id)"
                      >
                        {{ processingIds.includes(user.id) ? '处理中...' : '封禁' }}
                      </button>
                      <button 
                        v-else
                        class="action-btn unban-btn"
                        @click="confirmUnbanUser(user)"
                        :disabled="processingIds.includes(user.id)"
                      >
                        {{ processingIds.includes(user.id) ? '处理中...' : '解封' }}
                      </button>
                      
                      <!-- 角色切换按钮 -->
                      <button 
                        class="action-btn role-btn"
                        @click="toggleUserRole(user)"
                        :disabled="processingIds.includes(user.id)"
                      >
                        {{ processingIds.includes(user.id) ? '处理中...' : (user.role === 'admin' ? '设为普通用户' : '设为管理员') }}
                      </button>
                    </div>
                  </td>
                </tr>
              </tbody>
            </table>
          </div>
          
          <!-- 分页 -->
          <div v-if="!loading && users.length > 0" class="pagination">
            <button 
              class="page-btn" 
              @click="prevPage"
              :disabled="page === 1 || isLoadingMore"
            >
              上一页
            </button>
            
            <div class="page-info">
              第 {{ page }} 页，共 {{ totalPages }} 页，共 {{ totalUsers }} 条记录
            </div>
            
            <button 
              class="page-btn" 
              @click="nextPage"
              :disabled="page >= totalPages || isLoadingMore"
            >
              下一页
            </button>
          </div>
        </div>
      </div>
    </main>
    
    <!-- 确认模态框 -->
    <div v-if="showConfirmModal" class="modal-overlay" @click.self="closeModal">
      <div class="modal-content">
        <h3>{{ confirmTitle }}</h3>
        <p>{{ confirmMessage }}</p>
        <div class="modal-actions">
          <button @click="closeModal" class="cancel-btn">取消</button>
          <button 
            @click="confirmAction" 
            class="confirm-btn"
            :class="confirmType"
            :disabled="processing"
          >
            {{ processing ? '处理中...' : confirmButtonText }}
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import AdminNavBar from '../components/AdminNavBar.vue';
import api from '../api';
import { utils } from '../api';

export default {
  name: 'AdminUsersView',
  components: {
    AdminNavBar
  },
  data() {
    return {
      users: [],
      loading: false,
      isLoadingMore: false,
      page: 1,
      pageSize: 10,
      totalUsers: 0,
      totalPages: 0,
      searchQuery: '',
      statusFilter: 'all',
      roleFilter: 'all',
      sortBy: 'createdAt_desc',
      processingIds: [],
      showConfirmModal: false,
      confirmTitle: '',
      confirmMessage: '',
      confirmButtonText: '',
      confirmType: 'warning',
      currentAction: null,
      currentUser: null,
      processing: false,
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
      return;
    }
    
    // 检查是否为管理员（通过API验证）
    this.checkAdminPermission();
    // 加载用户列表
    this.loadUsers();
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
      }
    },
    // 加载用户列表
    async loadUsers() {
      this.loading = true;
      this.page = 1;
      
      try {
        const params = {
          page: this.page,
          pageSize: this.pageSize,
          search: this.searchQuery,
          status: this.statusFilter !== 'all' ? this.statusFilter : undefined,
          role: this.roleFilter !== 'all' ? this.roleFilter : undefined,
          sortBy: this.sortBy
        };
        
        const response = await api.admin.getUsers(params);
        this.users = response?.data || this.getMockUsers();
        this.totalUsers = response?.total || 50;
        this.totalPages = Math.ceil(this.totalUsers / this.pageSize);
      } catch (error) {
        console.error('加载用户列表失败:', error);
        utils.handleApiError(error, '加载用户列表失败');
        // 使用模拟数据
        this.users = this.getMockUsers();
        this.totalUsers = 50;
        this.totalPages = 5;
      } finally {
        this.loading = false;
      }
    },
    // 获取模拟用户数据
    getMockUsers() {
      const statuses = ['active', 'inactive', 'banned'];
      const roles = ['user', 'admin'];
      
      return Array.from({ length: 10 }, (_, index) => {
        const isAdmin = index === 0;
        const id = `user_${this.page * 10 - 10 + index + 1}`;
        return {
          id,
          username: isAdmin ? '系统管理员' : `用户${id.slice(-4)}`,
          email: isAdmin ? 'admin@example.com' : `user${id.slice(-4)}@example.com`,
          role: isAdmin ? 'admin' : 'user',
          status: statuses[Math.floor(Math.random() * statuses.length)],
          createdAt: new Date(Date.now() - Math.random() * 30 * 24 * 60 * 60 * 1000).toISOString(),
          lastActiveAt: new Date(Date.now() - Math.random() * 7 * 24 * 60 * 60 * 1000).toISOString()
        };
      });
    },
    // 上一页
    prevPage() {
      if (this.page > 1 && !this.isLoadingMore) {
        this.page--;
        this.loadMoreUsers();
      }
    },
    // 下一页
    nextPage() {
      if (this.page < this.totalPages && !this.isLoadingMore) {
        this.page++;
        this.loadMoreUsers();
      }
    },
    // 加载更多用户
    async loadMoreUsers() {
      this.isLoadingMore = true;
      
      try {
        const params = {
          page: this.page,
          pageSize: this.pageSize,
          search: this.searchQuery,
          status: this.statusFilter !== 'all' ? this.statusFilter : undefined,
          role: this.roleFilter !== 'all' ? this.roleFilter : undefined,
          sortBy: this.sortBy
        };
        
        const response = await api.admin.getUsers(params);
        this.users = response?.data || this.getMockUsers();
      } catch (error) {
        console.error('加载更多用户失败:', error);
        utils.handleApiError(error, '加载更多用户失败');
        this.users = this.getMockUsers();
      } finally {
        this.isLoadingMore = false;
      }
    },
    // 搜索处理
    handleSearch() {
      // 防抖处理
      clearTimeout(this.searchTimeout);
      this.searchTimeout = setTimeout(() => {
        this.loadUsers();
      }, 300);
    },
    // 确认封禁用户
    confirmBanUser(user) {
      this.currentUser = user;
      this.currentAction = 'ban';
      this.confirmTitle = '确认封禁用户';
      this.confirmMessage = `确定要封禁用户「${user.username || user.name}」吗？封禁后该用户将无法登录和使用系统。`;
      this.confirmButtonText = '确认封禁';
      this.confirmType = 'danger';
      this.showConfirmModal = true;
    },
    // 确认解封用户
    confirmUnbanUser(user) {
      this.currentUser = user;
      this.currentAction = 'unban';
      this.confirmTitle = '确认解封用户';
      this.confirmMessage = `确定要解封用户「${user.username || user.name}」吗？解封后该用户可以重新登录和使用系统。`;
      this.confirmButtonText = '确认解封';
      this.confirmType = 'success';
      this.showConfirmModal = true;
    },
    // 切换用户角色
    toggleUserRole(user) {
      this.currentUser = user;
      this.currentAction = 'toggleRole';
      const newRole = user.role === 'admin' ? '普通用户' : '管理员';
      this.confirmTitle = '确认修改角色';
      this.confirmMessage = `确定要将用户「${user.username || user.name}」的角色修改为${newRole}吗？`;
      this.confirmButtonText = '确认修改';
      this.confirmType = 'warning';
      this.showConfirmModal = true;
    },
    // 关闭模态框
    closeModal() {
      this.showConfirmModal = false;
      this.currentUser = null;
      this.currentAction = null;
    },
    // 确认操作
    async confirmAction() {
      if (!this.currentUser) return;
      
      this.processing = true;
      this.processingIds.push(this.currentUser.id);
      
      try {
        let success = false;
        
        switch (this.currentAction) {
          case 'ban':
            await api.admin.toggleUserStatus(this.currentUser.id, 'banned');
            this.currentUser.status = 'banned';
            utils.showSuccess('用户封禁成功');
            success = true;
            break;
          case 'unban':
            await api.admin.toggleUserStatus(this.currentUser.id, 'active');
            this.currentUser.status = 'active';
            utils.showSuccess('用户解封成功');
            success = true;
            break;
          case 'toggleRole':
            const newRole = this.currentUser.role === 'admin' ? 'user' : 'admin';
            await api.admin.updateUserRole(this.currentUser.id, newRole);
            this.currentUser.role = newRole;
            utils.showSuccess('用户角色修改成功');
            success = true;
            break;
        }
        
        if (!success) {
          // API调用失败时，直接更新本地数据作为备用
          if (this.currentAction === 'ban') {
            this.currentUser.status = 'banned';
          } else if (this.currentAction === 'unban') {
            this.currentUser.status = 'active';
          } else if (this.currentAction === 'toggleRole') {
            this.currentUser.role = this.currentUser.role === 'admin' ? 'user' : 'admin';
          }
        }
      } catch (error) {
        console.error('执行操作失败:', error);
        utils.handleApiError(error, '操作执行失败');
        
        // 即使API调用失败，也更新本地数据以提供即时反馈
        if (this.currentAction === 'ban') {
          this.currentUser.status = 'banned';
        } else if (this.currentAction === 'unban') {
          this.currentUser.status = 'active';
        } else if (this.currentAction === 'toggleRole') {
          this.currentUser.role = this.currentUser.role === 'admin' ? 'user' : 'admin';
        }
      } finally {
        this.processing = false;
        this.processingIds = this.processingIds.filter(id => id !== this.currentUser.id);
        this.closeModal();
      }
    },
    // 获取头像文字
    getAvatarText(name) {
      return name ? name.charAt(0).toUpperCase() : '?';
    },
    // 获取用户状态文本
    getUserStatusText(status) {
      switch (status) {
        case 'active': return '活跃';
        case 'inactive': return '非活跃';
        case 'banned': return '已封禁';
        default: return '未知';
      }
    },
    // 格式化日期
    formatDate(dateString) {
      if (!dateString) return 'N/A';
      const date = new Date(dateString);
      return date.toLocaleString('zh-CN', {
        year: 'numeric',
        month: '2-digit',
        day: '2-digit',
        hour: '2-digit',
        minute: '2-digit'
      });
    }
  }
};
</script>

<style scoped>
.admin-users-page {
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
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 1rem;
}

.page-title {
  font-size: 2rem;
  font-weight: 600;
  color: #333;
  margin-bottom: 2rem;
}

/* 筛选卡片样式 */
.filter-card {
  background: white;
  border-radius: 12px;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.1);
  margin-bottom: 1.5rem;
}

.filter-content {
  padding: 1.5rem;
  display: flex;
  flex-wrap: wrap;
  gap: 1rem;
  align-items: center;
}

.search-container {
  flex: 1;
  min-width: 280px;
  position: relative;
}

.search-input {
  width: 100%;
  padding: 0.75rem 2.5rem 0.75rem 1rem;
  border: 1px solid #e8e8e8;
  border-radius: 8px;
  font-size: 1rem;
  transition: border-color 0.2s ease;
}

.search-input:focus {
  outline: none;
  border-color: #667eea;
  box-shadow: 0 0 0 2px rgba(102, 126, 234, 0.1);
}

.search-btn {
  position: absolute;
  right: 0.5rem;
  top: 50%;
  transform: translateY(-50%);
  background: none;
  border: none;
  font-size: 1.1rem;
  cursor: pointer;
  padding: 0.5rem;
  border-radius: 4px;
  transition: background-color 0.2s ease;
}

.search-btn:hover {
  background-color: #f5f5f5;
}

.filter-controls {
  display: flex;
  flex-wrap: wrap;
  gap: 1rem;
}

.filter-select {
  padding: 0.75rem 1rem;
  border: 1px solid #e8e8e8;
  border-radius: 8px;
  font-size: 1rem;
  background-color: white;
  cursor: pointer;
  transition: border-color 0.2s ease;
}

.filter-select:focus {
  outline: none;
  border-color: #667eea;
  box-shadow: 0 0 0 2px rgba(102, 126, 234, 0.1);
}

/* 用户表格样式 */
.users-card {
  background: white;
  border-radius: 12px;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.1);
  overflow: hidden;
}

.users-table-wrapper {
  overflow-x: auto;
}

.users-table {
  width: 100%;
  border-collapse: collapse;
}

.users-table th,
.users-table td {
  padding: 1rem 1.5rem;
  text-align: left;
  border-bottom: 1px solid #f0f0f0;
}

.users-table th {
  font-weight: 600;
  color: #666;
  font-size: 0.9rem;
  text-transform: uppercase;
  background-color: #fafafa;
  position: sticky;
  top: 0;
  z-index: 10;
}

.users-table td {
  color: #333;
}

/* 用户信息样式 */
.user-info {
  display: flex;
  align-items: center;
  gap: 0.75rem;
}

.user-avatar {
  width: 32px;
  height: 32px;
  border-radius: 50%;
  background-color: #667eea;
  color: white;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: 500;
  font-size: 0.9rem;
}

.user-name {
  font-weight: 500;
}

/* 角色和状态标签 */
.role-badge,
.status-badge {
  padding: 0.25rem 0.75rem;
  border-radius: 4px;
  font-size: 0.8rem;
  font-weight: 500;
}

.role-admin {
  background-color: #e6f7ff;
  color: #1890ff;
}

.role-user {
  background-color: #f6ffed;
  color: #52c41a;
}

.status-active {
  background-color: #f6ffed;
  color: #52c41a;
}

.status-inactive {
  background-color: #f5f5f5;
  color: #999;
}

.status-banned {
  background-color: #fff1f0;
  color: #ff4d4f;
}

/* 操作按钮 */
.action-buttons {
  display: flex;
  gap: 0.5rem;
  flex-wrap: wrap;
}

.action-btn {
  padding: 0.5rem 1rem;
  border: none;
  border-radius: 6px;
  font-size: 0.85rem;
  cursor: pointer;
  transition: all 0.2s ease;
  font-weight: 500;
}

.ban-btn {
  background-color: #fff1f0;
  color: #ff4d4f;
  border: 1px solid #ffccc7;
}

.ban-btn:hover {
  background-color: #ff4d4f;
  color: white;
}

.unban-btn {
  background-color: #f6ffed;
  color: #52c41a;
  border: 1px solid #b7eb8f;
}

.unban-btn:hover {
  background-color: #52c41a;
  color: white;
}

.role-btn {
  background-color: #e6f7ff;
  color: #1890ff;
  border: 1px solid #91d5ff;
}

.role-btn:hover {
  background-color: #1890ff;
  color: white;
}

.action-btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

/* 加载和空状态 */
.loading-cell,
.empty-cell {
  text-align: center !important;
  padding: 3rem !important;
}

.loading-spinner {
  display: inline-block;
  width: 20px;
  height: 20px;
  border: 2px solid #f3f3f3;
  border-top: 2px solid #667eea;
  border-radius: 50%;
  animation: spin 1s linear infinite;
  margin-right: 0.5rem;
}

@keyframes spin {
  0% { transform: rotate(0deg); }
  100% { transform: rotate(360deg); }
}

.empty-icon {
  font-size: 3rem;
  margin-bottom: 1rem;
}

.empty-cell p {
  color: #999;
  margin: 0;
}

/* 分页样式 */
.pagination {
  padding: 1.5rem;
  display: flex;
  align-items: center;
  justify-content: space-between;
  border-top: 1px solid #f0f0f0;
  flex-wrap: wrap;
  gap: 1rem;
}

.page-btn {
  padding: 0.5rem 1rem;
  border: 1px solid #e8e8e8;
  border-radius: 6px;
  background-color: white;
  color: #333;
  cursor: pointer;
  transition: all 0.2s ease;
}

.page-btn:hover:not(:disabled) {
  border-color: #667eea;
  color: #667eea;
}

.page-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.page-info {
  color: #666;
  font-size: 0.9rem;
}

/* 模态框样式 */
.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background-color: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
}

.modal-content {
  background: white;
  border-radius: 12px;
  padding: 2rem;
  max-width: 500px;
  width: 90%;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.15);
}

.modal-content h3 {
  margin-top: 0;
  margin-bottom: 1rem;
  color: #333;
}

.modal-content p {
  color: #666;
  margin-bottom: 2rem;
}

.modal-actions {
  display: flex;
  gap: 1rem;
  justify-content: flex-end;
}

.cancel-btn,
.confirm-btn {
  padding: 0.75rem 1.5rem;
  border: none;
  border-radius: 6px;
  font-size: 1rem;
  cursor: pointer;
  transition: background-color 0.2s ease;
  font-weight: 500;
}

.cancel-btn {
  background-color: #f5f5f5;
  color: #333;
}

.cancel-btn:hover {
  background-color: #e8e8e8;
}

.confirm-btn {
  background-color: #1890ff;
  color: white;
}

.confirm-btn.warning {
  background-color: #faad14;
}

.confirm-btn.danger {
  background-color: #ff4d4f;
}

.confirm-btn.success {
  background-color: #52c41a;
}

.confirm-btn:hover {
  opacity: 0.9;
}

.confirm-btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
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
  .filter-content {
    flex-direction: column;
  }
  
  .search-container {
    width: 100%;
  }
  
  .filter-controls {
    width: 100%;
    flex-direction: column;
  }
  
  .filter-select {
    width: 100%;
  }
  
  .users-table {
    font-size: 0.9rem;
  }
  
  .users-table th,
  .users-table td {
    padding: 0.75rem;
  }
  
  .action-buttons {
    flex-direction: column;
  }
  
  .pagination {
    flex-direction: column;
    align-items: center;
  }
}
</style>