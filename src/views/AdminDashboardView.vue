<template>
  <div class="admin-dashboard">
    <AdminNavBar />
    
    <!-- 顶部装饰 -->
    <div class="admin-header-decoration">
      <div class="wave-shape"></div>
    </div>
    

    
    <main class="main-content">
      <div class="container">
        <h1 class="page-title">管理员仪表板</h1>
        
        <!-- 统计卡片区域 -->
        <div class="stats-grid">
          <div class="stat-card" v-if="analyticsData">
            <div class="stat-icon primary">📊</div>
            <div class="stat-content">
              <h3>总用户数</h3>
              <p class="stat-value">{{ analyticsData.totalUsers || 0 }}</p>
              <span class="stat-change" :class="{ positive: analyticsData.userGrowth > 0, negative: analyticsData.userGrowth < 0 }">
                {{ analyticsData.userGrowth > 0 ? '+' : '' }}{{ analyticsData.userGrowth || 0 }}% 较上周
              </span>
            </div>
          </div>
          
          <div class="stat-card" v-if="analyticsData">
            <div class="stat-icon secondary">✍️</div>
            <div class="stat-content">
              <h3>总帖子数</h3>
              <p class="stat-value">{{ analyticsData.totalPosts || 0 }}</p>
              <span class="stat-change" :class="{ positive: analyticsData.postGrowth > 0, negative: analyticsData.postGrowth < 0 }">
                {{ analyticsData.postGrowth > 0 ? '+' : '' }}{{ analyticsData.postGrowth || 0 }}% 较上周
              </span>
            </div>
          </div>
          
          <div class="stat-card" v-if="analyticsData">
            <div class="stat-icon warning">⏳</div>
            <div class="stat-content">
              <h3>待审核帖子</h3>
              <p class="stat-value">{{ analyticsData.pendingPosts || 0 }}</p>
              <span class="stat-desc">需要尽快处理</span>
            </div>
          </div>
          
          <div class="stat-card" v-if="analyticsData">
            <div class="stat-icon success">👍</div>
            <div class="stat-content">
              <h3>今日活跃用户</h3>
              <p class="stat-value">{{ analyticsData.dailyActiveUsers || 0 }}</p>
              <span class="stat-desc">{{ formatDate(new Date()) }}</span>
            </div>
          </div>
        </div>
        
        <!-- 近期操作日志 -->
        <div class="card">
          <div class="card-header">
            <h2>近期操作日志</h2>
            <router-link to="/admin/logs" class="view-all-link">查看全部</router-link>
          </div>
          <div class="logs-table-wrapper">
            <table class="logs-table">
              <thead>
                <tr>
                  <th>时间</th>
                  <th>操作人</th>
                  <th>操作类型</th>
                  <th>操作内容</th>
                  <th>状态</th>
                </tr>
              </thead>
              <tbody>
                <tr v-for="log in recentLogs" :key="log.id">
                  <td>{{ formatLogTime(log.createdAt) }}</td>
                  <td>{{ log.operator || '系统' }}</td>
                  <td><span class="log-type" :class="getLogTypeClass(log.type)">{{ log.type }}</span></td>
                  <td>{{ log.content }}</td>
                  <td><span class="log-status" :class="log.success ? 'success' : 'failed'">{{ log.success ? '成功' : '失败' }}</span></td>
                </tr>
                <tr v-if="loadingLogs">
                  <td colspan="5" class="loading-cell">加载中...</td>
                </tr>
                <tr v-if="!loadingLogs && recentLogs.length === 0">
                  <td colspan="5" class="empty-cell">暂无操作日志</td>
                </tr>
              </tbody>
            </table>
          </div>
        </div>
        
        <!-- 快速操作 -->
        <div class="card">
          <div class="card-header">
            <h2>快速操作</h2>
          </div>
          <div class="quick-actions">
            <router-link to="/admin/review" class="quick-action-btn">
              <div class="action-icon">📝</div>
              <span>审核帖子</span>
            </router-link>
            <router-link to="/admin/users" class="quick-action-btn">
              <div class="action-icon">👥</div>
              <span>管理用户</span>
            </router-link>
            <router-link to="/admin/analytics" class="quick-action-btn">
              <div class="action-icon">📈</div>
              <span>查看分析</span>
            </router-link>
            <router-link to="/admin/settings" class="quick-action-btn">
              <div class="action-icon">⚙️</div>
              <span>系统设置</span>
            </router-link>
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
  name: 'AdminDashboard',
  components: {
    AdminNavBar
  },
  data() {
    return {
      analyticsData: null,
      recentLogs: [],
      loadingAnalytics: false,
      loadingLogs: false,
    };
  },

  mounted() {
    // 检查是否为管理员（通过API验证）
    this.checkAdminPermission();
    // 加载统计数据
    this.loadAnalyticsData();
    // 加载近期日志
    this.loadRecentLogs();
  },
  methods: {
    // 检查管理员权限
    async checkAdminPermission() {
      try {
        // 调用后端API验证用户是否为管理员
        const response = await api.admin.verifyAdmin();
        const isAdmin = response?.data?.isAdmin || response?.isAdmin;
        if (!isAdmin) {
          utils.showError('您没有管理员权限');
          this.$router.push('/admin/login');
        }
      } catch (error) {
        console.error('管理员权限验证失败:', error);
        utils.showError('权限验证失败，请先登录');
        this.$router.push('/admin/login');
      }
    },
    // 加载统计数据
  async loadAnalyticsData() {
    this.loadingAnalytics = true;
    try {
      const response = await api.admin.getAnalyticsOverview();
      this.analyticsData = response?.data || null;
    } catch (error) {
      console.error('加载统计数据失败:', error);
      utils.showError('获取统计数据失败，请检查后端连接');
      this.analyticsData = null;
    } finally {
      this.loadingAnalytics = false;
    }
  },
    // 加载近期日志
  async loadRecentLogs() {
    this.loadingLogs = true;
    try {
      const response = await api.admin.getLogs({ page: 1, pageSize: 10 });
      this.recentLogs = response?.data || [];
    } catch (error) {
      console.error('加载日志失败:', error);
      utils.showError('获取操作日志失败，请检查后端连接');
      this.recentLogs = [];
    } finally {
        this.loadingLogs = false;
      }
    },
    // 格式化日期
    formatDate(date) {
      return date.toLocaleDateString('zh-CN', {
        year: 'numeric',
        month: 'long',
        day: 'numeric'
      });
    },
    // 格式化日志时间
    formatLogTime(time) {
      if (!time) return '';
      const date = new Date(time);
      return date.toLocaleString('zh-CN', {
        month: '2-digit',
        day: '2-digit',
        hour: '2-digit',
        minute: '2-digit',
        second: '2-digit'
      });
    },
    // 获取日志类型样式类
    getLogTypeClass(type) {
      switch (type) {
        case '审核帖子': return 'type-review';
        case '用户管理': return 'type-user';
        case '系统设置': return 'type-setting';
        case '自动审核': return 'type-auto';
        default: return 'type-other';
      }
    }
  }
};
</script>

<style scoped>
.admin-dashboard {
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

/* 统计卡片样式 */
.stats-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(280px, 1fr));
  gap: 1.5rem;
  margin-bottom: 2rem;
}

.stat-card {
  background: white;
  border-radius: 12px;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.1);
  padding: 1.5rem;
  display: flex;
  align-items: center;
  gap: 1rem;
  transition: transform 0.2s ease, box-shadow 0.2s ease;
}

.stat-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 15px rgba(0, 0, 0, 0.15);
}

.stat-icon {
  width: 64px;
  height: 64px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 2rem;
}

.stat-icon.primary {
  background-color: #e6f7ff;
}

.stat-icon.secondary {
  background-color: #f6ffed;
}

.stat-icon.warning {
  background-color: #fffbe6;
}

.stat-icon.success {
  background-color: #f0f9ff;
}

.stat-content h3 {
  font-size: 0.9rem;
  color: #666;
  margin-bottom: 0.5rem;
  font-weight: 500;
}

.stat-value {
  font-size: 1.8rem;
  font-weight: 600;
  color: #333;
  margin-bottom: 0.25rem;
}

.stat-change {
  font-size: 0.8rem;
  font-weight: 500;
}

.stat-change.positive {
  color: #52c41a;
}

.stat-change.negative {
  color: #ff4d4f;
}

.stat-desc {
  font-size: 0.8rem;
  color: #999;
}

/* 卡片通用样式 */
.card {
  background: white;
  border-radius: 12px;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.1);
  margin-bottom: 2rem;
  overflow: hidden;
}

.card-header {
  padding: 1.5rem;
  border-bottom: 1px solid #f0f0f0;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.card-header h2 {
  font-size: 1.25rem;
  font-weight: 600;
  color: #333;
  margin: 0;
}

.view-all-link {
  color: #667eea;
  text-decoration: none;
  font-size: 0.9rem;
  transition: color 0.2s ease;
}

.view-all-link:hover {
  color: #764ba2;
  text-decoration: underline;
}

/* 日志表格样式 */
.logs-table-wrapper {
  overflow-x: auto;
}

.logs-table {
  width: 100%;
  border-collapse: collapse;
}

.logs-table th,
.logs-table td {
  padding: 1rem 1.5rem;
  text-align: left;
  border-bottom: 1px solid #f0f0f0;
}

.logs-table th {
  font-weight: 600;
  color: #666;
  font-size: 0.9rem;
  text-transform: uppercase;
}

.logs-table td {
  color: #333;
}

.log-type {
  padding: 0.25rem 0.75rem;
  border-radius: 4px;
  font-size: 0.8rem;
  font-weight: 500;
}

.log-type.type-review {
  background-color: #e6f7ff;
  color: #1890ff;
}

.log-type.type-user {
  background-color: #f6ffed;
  color: #52c41a;
}

.log-type.type-setting {
  background-color: #fff7e6;
  color: #fa8c16;
}

.log-type.type-auto {
  background-color: #f9f0ff;
  color: #722ed1;
}

.log-type.type-other {
  background-color: #f5f5f5;
  color: #666;
}

.log-status {
  padding: 0.25rem 0.75rem;
  border-radius: 4px;
  font-size: 0.8rem;
  font-weight: 500;
}

.log-status.success {
  background-color: #f6ffed;
  color: #52c41a;
}

.log-status.failed {
  background-color: #fff1f0;
  color: #ff4d4f;
}

.loading-cell,
.empty-cell {
  text-align: center !important;
  color: #999;
  font-style: italic;
}

/* 快速操作样式 */
.quick-actions {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: 1rem;
  padding: 1.5rem;
}

.quick-action-btn {
  background: white;
  border: 1px solid #e8e8e8;
  border-radius: 8px;
  padding: 1.5rem;
  text-align: center;
  text-decoration: none;
  color: #333;
  transition: all 0.2s ease;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 0.5rem;
}

.quick-action-btn:hover {
  background-color: #f5f5f5;
  border-color: #667eea;
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.action-icon {
  font-size: 2.5rem;
  margin-bottom: 0.5rem;
}



/* 响应式设计 */
@media (max-width: 768px) {
  .stats-grid {
    grid-template-columns: 1fr;
  }
  
  .quick-actions {
    grid-template-columns: 1fr;
  }
  
  .logs-table-wrapper {
    font-size: 0.9rem;
  }
  
  .logs-table th,
  .logs-table td {
    padding: 0.75rem;
  }
}
</style>