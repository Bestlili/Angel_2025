<template>
  <div class="admin-page">
    <AdminNavBar />
    
    <main class="main-content">
      <div class="container">
        <h1 class="page-title">操作日志</h1>
        
        <!-- 筛选搜索区域 -->
        <div class="card filter-card">
          <div class="filter-content">
            <div class="search-input-wrapper">
              <input 
                type="text" 
                v-model="searchQuery" 
                @input="handleSearch"
                placeholder="搜索操作内容、操作者..."
                class="search-input"
              />
            </div>
            
            <div class="filter-row">
              <div class="filter-item">
                <label>操作类型：</label>
                <select v-model="filterOptions.type" @change="loadLogs" class="filter-select">
                  <option value="">全部</option>
                  <option value="post_review">帖子审核</option>
                  <option value="user_management">用户管理</option>
                  <option value="settings_update">设置更新</option>
                  <option value="system_operation">系统操作</option>
                </select>
              </div>
              
              <div class="filter-item">
                <label>日期范围：</label>
                <input 
                  type="date" 
                  v-model="filterOptions.startDate" 
                  @change="loadLogs"
                  class="date-input"
                />
                <span class="date-separator">至</span>
                <input 
                  type="date" 
                  v-model="filterOptions.endDate" 
                  @change="loadLogs"
                  class="date-input"
                />
              </div>
            </div>
          </div>
        </div>
        
        <!-- 日志列表 -->
        <div class="card logs-card">
          <div class="logs-header">
            <h3>操作记录列表</h3>
            <div class="logs-stats">
              共 {{ totalLogs }} 条记录
            </div>
          </div>
          
          <div class="logs-list">
            <div 
              v-if="loading" 
              class="loading-state"
            >
              <div class="loading-spinner"></div>
              <span>加载中...</span>
            </div>
            
            <div 
              v-else-if="logs.length === 0" 
              class="empty-state"
            >
              暂无操作记录
            </div>
            
            <div 
              v-for="log in logs" 
              :key="log.id"
              class="log-item"
              @click="viewLogDetails(log)"
            >
              <!-- 日志内容 -->
              <div class="log-content">
                <div class="log-main">
                  <span class="log-action">{{ log.action }}</span>
                  <span class="log-time">{{ formatTime(log.createdAt) }}</span>
                </div>
                <div class="log-details">
                  <span class="log-operator">{{ log.operator || '系统' }}</span>
                  <span class="log-result" :class="getResultClass(log.result)">
                    {{ log.result === 'success' ? '成功' : '失败' }}
                  </span>
                </div>
                <div class="log-description">{{ log.description }}</div>
              </div>
              <div class="log-arrow">→</div>
            </div>
          </div>
          
          <!-- 分页控件 -->
          <div v-if="!loading && totalLogs > pageSize" class="pagination-controls">
            <button 
              class="pagination-btn"
              :disabled="currentPage === 1"
              @click="changePage(currentPage - 1)"
            >
              上一页
            </button>
            
            <div class="pagination-info">
              第 {{ currentPage }} 页，共 {{ Math.ceil(totalLogs / pageSize) }} 页
            </div>
            
            <button 
              class="pagination-btn"
              :disabled="currentPage >= Math.ceil(totalLogs / pageSize)"
              @click="changePage(currentPage + 1)"
            >
              下一页
            </button>
          </div>
        </div>
      </div>
    </main>
    
    <!-- 详情模态框 -->
    <div v-if="showDetails" class="modal-overlay" @click="closeDetails">
      <div class="modal-content" @click.stop>
        <div class="modal-header">
          <h3>操作详情</h3>
          <button class="modal-close" @click="closeDetails">×</button>
        </div>
        
        <div v-if="selectedLog" class="modal-body">
          <div class="detail-row">
            <span class="detail-label">操作类型：</span>
            <span class="detail-value">{{ selectedLog.action }}</span>
          </div>
          <div class="detail-row">
            <span class="detail-label">操作时间：</span>
            <span class="detail-value">{{ formatFullTime(selectedLog.createdAt) }}</span>
          </div>
          <div class="detail-row">
            <span class="detail-label">操作者：</span>
            <span class="detail-value">{{ selectedLog.operator || '系统' }}</span>
          </div>
          <div class="detail-row">
            <span class="detail-label">操作结果：</span>
            <span class="detail-value" :class="getResultClass(selectedLog.result)">
              {{ selectedLog.result === 'success' ? '成功' : '失败' }}
            </span>
          </div>
          <div class="detail-row detail-row-full">
            <span class="detail-label">操作描述：</span>
            <div class="detail-value">{{ selectedLog.description }}</div>
          </div>
          <div v-if="selectedLog.additionalInfo" class="detail-row detail-row-full">
            <span class="detail-label">附加信息：</span>
            <pre class="detail-value">{{ formatAdditionalInfo(selectedLog.additionalInfo) }}</pre>
          </div>
        </div>
        
        <div class="modal-footer">
          <button class="btn btn-primary" @click="closeDetails">关闭</button>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import api from '../api';
import { utils } from '../api';
import AdminNavBar from '../components/AdminNavBar.vue';

export default {
  name: 'AdminLogsView',
  components: {
    AdminNavBar
  },
  data() {
    return {
      logs: [],
      loading: false,
      currentPage: 1,
      pageSize: 20,
      totalLogs: 0,
      searchQuery: '',
      filterOptions: {
        type: '',
        startDate: '',
        endDate: ''
      },
      showDetails: false,
      selectedLog: null
    };
  },
  computed: {
    isAuthenticated() {
      return localStorage.getItem('authToken') !== null;
    },
    isDevelopment() {
      return process.env.NODE_ENV === 'development';
    }
  },
  async mounted() {
    // 检查管理员权限
    this.checkAdminPermission();
    
    // 加载日志
    this.loadLogs();
  },
  methods: {
    async checkAdminPermission() {
      if (!this.isAuthenticated && this.isDevelopment) {
        // 开发环境自动测试登录
        await this.testLogin();
      }
      
      try {
        const response = await api.admin.verifyAdmin();
        if (!response.data?.isAdmin) {
          this.$router.push('/');
        }
      } catch (error) {
        console.error('管理员权限验证失败:', error);
        this.$router.push('/');
      }
    },
    
    async testLogin() {
      try {
        // 模拟管理员登录
        const mockToken = 'mock_admin_token_for_development';
        localStorage.setItem('authToken', mockToken);
        localStorage.setItem('token', mockToken);
        localStorage.setItem('username', 'admin');
        localStorage.setItem('user', JSON.stringify({ role: 'admin' }));
        console.log('开发环境测试登录成功');
      } catch (error) {
        console.error('开发环境测试登录失败:', error);
      }
    },
    
    async loadLogs() {
      this.loading = true;
      try {
        const params = {
          page: this.currentPage,
          pageSize: this.pageSize,
          query: this.searchQuery,
          ...this.filterOptions
        };
        
        const response = await api.admin.getLogs(params);
        this.logs = response.data?.logs || [];
        this.totalLogs = response.data?.total || 0;
      } catch (error) {
        console.error('加载操作日志失败:', error);
        this.$message.error('加载操作日志失败');
      } finally {
        this.loading = false;
      }
    },
    
    handleSearch() {
      this.currentPage = 1;
      // 添加防抖
      clearTimeout(this.searchTimer);
      this.searchTimer = setTimeout(() => {
        this.loadLogs();
      }, 300);
    },
    
    changePage(page) {
      this.currentPage = page;
      this.loadLogs();
    },
    
    viewLogDetails(log) {
      this.selectedLog = log;
      this.showDetails = true;
    },
    
    closeDetails() {
      this.showDetails = false;
      this.selectedLog = null;
    },
    
    formatTime(timeString) {
      if (!timeString) return '';
      const date = new Date(timeString);
      return `${date.getMonth() + 1}/${date.getDate()} ${date.getHours()}:${String(date.getMinutes()).padStart(2, '0')}`;
    },
    
    formatFullTime(timeString) {
      if (!timeString) return '';
      const date = new Date(timeString);
      return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')} ${date.getHours()}:${String(date.getMinutes()).padStart(2, '0')}:${String(date.getSeconds()).padStart(2, '0')}`;
    },
    
    getResultClass(result) {
      return result === 'success' ? 'text-success' : 'text-error';
    },
    
    formatAdditionalInfo(info) {
      if (typeof info === 'string') {
        return info;
      }
      return JSON.stringify(info, null, 2);
    }
  }
};
</script>

<style scoped>
.admin-page {
  display: flex;
  min-height: 100vh;
  background-color: #f5f5f5;
}

.main-content {
  flex: 1;
  padding: 20px;
  margin-left: 240px;
}

.container {
  max-width: 1200px;
  margin: 0 auto;
}

.page-title {
  font-size: 24px;
  font-weight: bold;
  margin-bottom: 20px;
  color: #333;
}

/* 卡片样式 */
.card {
  background: white;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  margin-bottom: 20px;
  overflow: hidden;
}

/* 筛选卡片 */
.filter-card {
  padding: 20px;
}

.filter-content {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.search-input-wrapper {
  width: 100%;
}

.search-input {
  width: 100%;
  padding: 10px 16px;
  border: 1px solid #ddd;
  border-radius: 4px;
  font-size: 14px;
  transition: border-color 0.3s;
}

.search-input:focus {
  outline: none;
  border-color: #1890ff;
}

.filter-row {
  display: flex;
  gap: 20px;
  align-items: center;
  flex-wrap: wrap;
}

.filter-item {
  display: flex;
  align-items: center;
  gap: 8px;
}

.filter-item label {
  font-size: 14px;
  color: #666;
}

.filter-select,
.date-input {
  padding: 8px 12px;
  border: 1px solid #ddd;
  border-radius: 4px;
  font-size: 14px;
  background: white;
  cursor: pointer;
}

.date-separator {
  color: #999;
}

/* 日志卡片 */
.logs-card {
  overflow: hidden;
}

.logs-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20px;
  border-bottom: 1px solid #f0f0f0;
}

.logs-header h3 {
  margin: 0;
  font-size: 18px;
  font-weight: 500;
  color: #333;
}

.logs-stats {
  font-size: 14px;
  color: #999;
}

/* 日志列表 */
.logs-list {
  max-height: 600px;
  overflow-y: auto;
}

.loading-state,
.empty-state {
  text-align: center;
  padding: 40px;
  color: #999;
  font-size: 14px;
}

.loading-spinner {
  width: 20px;
  height: 20px;
  border: 2px solid #f3f3f3;
  border-top: 2px solid #1890ff;
  border-radius: 50%;
  animation: spin 1s linear infinite;
  margin: 0 auto 10px;
}

@keyframes spin {
  0% { transform: rotate(0deg); }
  100% { transform: rotate(360deg); }
}

/* 日志项 */
.log-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px 20px;
  border-bottom: 1px solid #f0f0f0;
  cursor: pointer;
  transition: background-color 0.2s;
}

.log-item:hover {
  background-color: #fafafa;
}

.log-content {
  flex: 1;
}

.log-main {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
}

.log-action {
  font-size: 16px;
  font-weight: 500;
  color: #333;
}

.log-time {
  font-size: 14px;
  color: #999;
}

.log-details {
  display: flex;
  gap: 16px;
  margin-bottom: 8px;
}

.log-operator {
  font-size: 14px;
  color: #666;
}

.log-result {
  font-size: 14px;
  font-weight: 500;
}

.text-success {
  color: #52c41a;
}

.text-error {
  color: #ff4d4f;
}

.log-description {
  font-size: 14px;
  color: #666;
  line-height: 1.5;
}

.log-arrow {
  font-size: 20px;
  color: #999;
}

/* 分页控件 */
.pagination-controls {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px 20px;
  border-top: 1px solid #f0f0f0;
}

.pagination-btn {
  padding: 6px 16px;
  border: 1px solid #d9d9d9;
  border-radius: 4px;
  background: white;
  color: #666;
  font-size: 14px;
  cursor: pointer;
  transition: all 0.3s;
}

.pagination-btn:hover:not(:disabled) {
  border-color: #1890ff;
  color: #1890ff;
}

.pagination-btn:disabled {
  cursor: not-allowed;
  opacity: 0.5;
}

.pagination-info {
  font-size: 14px;
  color: #666;
}

/* 模态框 */
.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background-color: rgba(0, 0, 0, 0.5);
  display: flex;
  justify-content: center;
  align-items: center;
  z-index: 1000;
}

.modal-content {
  background: white;
  border-radius: 8px;
  width: 90%;
  max-width: 600px;
  max-height: 80vh;
  overflow: hidden;
  display: flex;
  flex-direction: column;
}

.modal-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20px;
  border-bottom: 1px solid #f0f0f0;
}

.modal-header h3 {
  margin: 0;
  font-size: 18px;
  font-weight: 500;
  color: #333;
}

.modal-close {
  background: none;
  border: none;
  font-size: 24px;
  color: #999;
  cursor: pointer;
  padding: 0;
  width: 30px;
  height: 30px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 4px;
  transition: all 0.3s;
}

.modal-close:hover {
  background-color: #f0f0f0;
  color: #333;
}

.modal-body {
  padding: 20px;
  overflow-y: auto;
  flex: 1;
}

.detail-row {
  display: flex;
  margin-bottom: 16px;
  align-items: flex-start;
}

.detail-row-full {
  flex-direction: column;
}

.detail-label {
  font-weight: 500;
  color: #666;
  width: 100px;
  flex-shrink: 0;
}

.detail-row-full .detail-label {
  margin-bottom: 8px;
  width: auto;
}

.detail-value {
  color: #333;
  flex: 1;
  word-break: break-all;
}

.detail-value pre {
  margin: 0;
  font-family: inherit;
  white-space: pre-wrap;
  background-color: #f5f5f5;
  padding: 10px;
  border-radius: 4px;
  font-size: 14px;
}

.modal-footer {
  display: flex;
  justify-content: flex-end;
  padding: 20px;
  border-top: 1px solid #f0f0f0;
}

.btn {
  padding: 8px 20px;
  border-radius: 4px;
  font-size: 14px;
  cursor: pointer;
  transition: all 0.3s;
  border: none;
}

.btn-primary {
  background-color: #1890ff;
  color: white;
}

.btn-primary:hover {
  background-color: #40a9ff;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .main-content {
    margin-left: 0;
    padding: 16px;
  }
  
  .filter-row {
    flex-direction: column;
    align-items: stretch;
    gap: 12px;
  }
  
  .filter-item {
    flex-direction: column;
    align-items: stretch;
  }
  
  .log-main {
    flex-direction: column;
    align-items: flex-start;
    gap: 8px;
  }
  
  .log-details {
    flex-wrap: wrap;
  }
  
  .pagination-controls {
    flex-direction: column;
    gap: 12px;
  }
  
  .modal-content {
    width: 95%;
    margin: 20px;
  }
  
  .detail-row {
    flex-direction: column;
  }
  
  .detail-label {
    width: auto;
    margin-bottom: 4px;
  }
}
</style>