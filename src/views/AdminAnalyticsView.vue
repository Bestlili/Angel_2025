<template>
  <div class="admin-analytics-view">
    <!-- 导航栏 -->
    <AdminNavBar />
    
    <!-- 主内容区域 -->
    <main class="main-content">
      <div class="page-header">
        <h1>数据分析</h1>
        <p>查看平台用户增长和内容发布趋势</p>
      </div>
      
      <!-- 权限错误提示 -->
      <div v-if="!isAdmin" class="permission-error">
        <div class="error-message">
          <p>您没有权限访问此页面</p>
          <button @click="handleBack" class="back-button">返回</button>
        </div>
      </div>
      
      <!-- 加载状态 -->
      <div v-if="isLoading" class="loading-container">
        <div class="loading-spinner"></div>
        <p>加载中...</p>
      </div>
      
      <!-- 主要内容 -->
      <div v-if="isAdmin && !isLoading" class="content-container">
        <!-- 时间筛选 -->
        <div class="filter-section">
          <div class="filter-card">
            <div class="filter-title">时间范围</div>
            <select v-model="period" @change="handlePeriodChange" class="period-select">
              <option value="7days">近7天</option>
              <option value="30days">近30天</option>
            </select>
          </div>
        </div>
        
        <!-- 统计概览 -->
        <div class="stats-grid">
          <div class="stat-card">
            <div class="stat-icon user-icon"></div>
            <div class="stat-content">
              <div class="stat-value">{{ analyticsData.totalUsers || 0 }}</div>
              <div class="stat-label">总用户数</div>
            </div>
          </div>
          <div class="stat-card">
            <div class="stat-icon post-icon"></div>
            <div class="stat-content">
              <div class="stat-value">{{ analyticsData.totalPosts || 0 }}</div>
              <div class="stat-label">总帖子数</div>
            </div>
          </div>
          <div class="stat-card">
            <div class="stat-icon pending-icon"></div>
            <div class="stat-content">
              <div class="stat-value">{{ analyticsData.pendingPosts || 0 }}</div>
              <div class="stat-label">待审核帖子</div>
            </div>
          </div>
          <div class="stat-card">
            <div class="stat-icon active-icon"></div>
            <div class="stat-content">
              <div class="stat-value">{{ analyticsData.activeUsers || 0 }}</div>
              <div class="stat-label">活跃用户</div>
            </div>
          </div>
        </div>
        
        <!-- 图表区域 -->
        <div class="charts-container">
          <!-- 用户增长趋势 -->
          <div class="chart-card">
            <div class="card-header">
              <h3>用户增长趋势</h3>
            </div>
            <div class="chart-content simple-chart">
              <div class="chart-stats">
                <div class="stat-item">
                  <span class="stat-label">总新增用户</span>
                  <span class="stat-value">{{ getTotalNewUsers() }}</span>
                </div>
                <div class="stat-item">
                  <span class="stat-label">日均新增用户</span>
                  <span class="stat-value">{{ getAverageDailyNewUsers() }}</span>
                </div>
              </div>
              <div class="data-table">
                <div class="table-header">
                  <span>日期</span>
                  <span>新增用户数</span>
                </div>
                <div v-for="item in userGrowthData.slice(-7)" :key="item.date" class="table-row">
                  <span>{{ item.date }}</span>
                  <span class="text-primary">{{ item.count }}</span>
                </div>
              </div>
            </div>
          </div>
          
          <!-- 帖子发布趋势 -->
          <div class="chart-card">
            <div class="card-header">
              <h3>帖子发布趋势</h3>
            </div>
            <div class="chart-content simple-chart">
              <div class="chart-stats">
                <div class="stat-item">
                  <span class="stat-label">总发布量</span>
                  <span class="stat-value">{{ getTotalPosts() }}</span>
                </div>
                <div class="stat-item">
                  <span class="stat-label">通过率</span>
                  <span class="stat-value">{{ getApprovalRate() }}</span>
                </div>
              </div>
              <div class="data-table">
                <div class="table-header">
                  <span>日期</span>
                  <span>已通过</span>
                  <span>已拒绝</span>
                </div>
                <div v-for="item in postTrendsData.slice(-7)" :key="item.date" class="table-row">
                  <span>{{ item.date }}</span>
                  <span class="text-success">{{ item.approvedCount || 0 }}</span>
                  <span class="text-error">{{ item.rejectedCount || 0 }}</span>
                </div>
              </div>
            </div>
          </div>
        </div>
        
        <!-- 详细统计 -->
        <div class="detailed-stats">
          <div class="stats-card">
            <div class="card-header">
              <h3>平台活跃度</h3>
            </div>
            <div class="stats-content">
              <div class="stat-row">
                <span class="stat-name">7日活跃率</span>
                <span class="stat-value">{{ analyticsData.activeRate || '0%' }}</span>
              </div>
              <div class="stat-row">
                <span class="stat-name">平均每用户发帖</span>
                <span class="stat-value">{{ analyticsData.avgPostsPerUser || '0' }}</span>
              </div>
              <div class="stat-row">
                <span class="stat-name">审核平均耗时</span>
                <span class="stat-value">{{ analyticsData.avgReviewTime || '0分钟' }}</span>
              </div>
            </div>
          </div>
        </div>
      </div>
    </main>
  </div>
</template>

<script>
import api from '../api';
import { utils } from '../api';
import AdminNavBar from '../components/AdminNavBar.vue';

export default {
  name: 'AdminAnalyticsView',
  components: {
    AdminNavBar
  },
  data() {
    return {
      isAdmin: false,
      isLoading: true,
      period: '7days',
      analyticsData: {},
      userGrowthData: [],
      postTrendsData: []
    };
  },
  async mounted() {
    // 检查管理员权限
    this.checkAdminPermission();
    
    // 加载数据
    await this.loadAnalyticsData();
  },
  methods: {
    async checkAdminPermission() {
      try {
          const response = await api.admin.verifyAdmin();
          this.isAdmin = response.data?.isAdmin || response.isAdmin || false;
        } catch (error) {
        console.error('验证管理员权限失败:', error);
        this.isAdmin = false;
        // 处理401和403错误
        if (error.response && error.response.status === 401) {
          this.$router.push('/login');
        } else if (error.response && error.response.status === 403) {
          this.showErrorMessage('您没有权限访问此页面');
        }
      }
    },
    
    async loadAnalyticsData() {
      try {
        this.isLoading = true;
        // 并行加载所有数据
        const [analyticsRes, userGrowthRes, postTrendsRes] = await Promise.all([
          api.admin.getAnalyticsOverview(),
          api.admin.getUserGrowth({ period: this.period }),
          api.admin.getPostTrends({ period: this.period })
        ]);
        
        this.analyticsData = analyticsRes.data || {};
        this.userGrowthData = userGrowthRes.data || [];
        this.postTrendsData = postTrendsRes.data || [];
      } catch (error) {
        console.error('加载分析数据失败:', error);
        utils.showError('获取分析数据失败，请检查后端连接');
        this.analyticsData = {};
        this.userGrowthData = [];
        this.postTrendsData = [];
      } finally {
        this.isLoading = false;
      }
    },
    
    handlePeriodChange() {
      this.loadAnalyticsData();
    },
    

    
    // 计算简单的数据统计
    getTotalNewUsers() {
      return this.userGrowthData.reduce((sum, item) => sum + item.count, 0);
    },
    
    getAverageDailyNewUsers() {
      if (this.userGrowthData.length === 0) return 0;
      return Math.round(this.getTotalNewUsers() / this.userGrowthData.length);
    },
    
    getTotalPosts() {
      return this.postTrendsData.reduce((sum, item) => sum + (item.approvedCount || 0) + (item.rejectedCount || 0), 0);
    },
    
    getApprovalRate() {
      const total = this.getTotalPosts();
      if (total === 0) return '0%';
      const approved = this.postTrendsData.reduce((sum, item) => sum + (item.approvedCount || 0), 0);
      return `${Math.round((approved / total) * 100)}%`;
    },
    
    handleBack() {
      this.$router.push('/');
    },
    
    showErrorMessage(message) {
      // 简单的错误提示，实际项目中可以使用更好的提示组件
      alert(message);
    }
  }
};
</script>

<style scoped>
.admin-analytics-view {
  min-height: 100vh;
  background-color: #f5f5f5;
}

.main-content {
  padding: 20px;
  max-width: 1400px;
  margin: 0 auto;
}

.page-header {
  margin-bottom: 30px;
}

.page-header h1 {
  margin: 0;
  font-size: 28px;
  font-weight: 600;
  color: #333;
}

.page-header p {
  margin-top: 8px;
  color: #666;
  font-size: 16px;
}

.permission-error {
  display: flex;
  justify-content: center;
  align-items: center;
  height: 400px;
}

.error-message {
  text-align: center;
  padding: 40px;
  background: white;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.error-message p {
  font-size: 18px;
  color: #666;
  margin-bottom: 20px;
}

.back-button {
  padding: 10px 24px;
  background: #1890ff;
  color: white;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  font-size: 16px;
  transition: background-color 0.3s;
}

.back-button:hover {
  background: #40a9ff;
}

.loading-container {
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  height: 400px;
}

.loading-spinner {
  width: 40px;
  height: 40px;
  border: 4px solid #f3f3f3;
  border-top: 4px solid #1890ff;
  border-radius: 50%;
  animation: spin 1s linear infinite;
  margin-bottom: 16px;
}

@keyframes spin {
  0% { transform: rotate(0deg); }
  100% { transform: rotate(360deg); }
}

.content-container {
  display: flex;
  flex-direction: column;
  gap: 24px;
}

.filter-section {
  display: flex;
  gap: 16px;
}

.filter-card {
  background: white;
  padding: 20px;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  display: flex;
  align-items: center;
  gap: 16px;
}

.filter-title {
  font-weight: 500;
  color: #333;
}

.period-select {
  padding: 8px 16px;
  border: 1px solid #d9d9d9;
  border-radius: 4px;
  font-size: 14px;
  cursor: pointer;
  background: white;
}

.stats-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
  gap: 20px;
}

.stat-card {
  background: white;
  padding: 24px;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  display: flex;
  align-items: center;
  gap: 16px;
  transition: transform 0.2s, box-shadow 0.2s;
}

.stat-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
}

.stat-icon {
  width: 48px;
  height: 48px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
}

.user-icon {
  background: #e6f7ff;
  color: #1890ff;
}

.post-icon {
  background: #f6ffed;
  color: #52c41a;
}

.pending-icon {
  background: #fff7e6;
  color: #fa8c16;
}

.active-icon {
  background: #f0f5ff;
  color: #722ed1;
}

.stat-content {
  flex: 1;
}

.stat-value {
  font-size: 28px;
  font-weight: 600;
  color: #333;
  line-height: 1.2;
}

.stat-label {
  font-size: 14px;
  color: #666;
  margin-top: 4px;
}

.charts-container {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(500px, 1fr));
  gap: 24px;
}

.chart-card {
  background: white;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  overflow: hidden;
}

.card-header {
  padding: 20px 24px 16px;
  border-bottom: 1px solid #f0f0f0;
}

.card-header h3 {
  margin: 0;
  font-size: 18px;
  font-weight: 600;
  color: #333;
}

.chart-content {
  height: 300px;
}

.simple-chart {
  padding: 20px;
  height: auto;
  min-height: 300px;
}

.chart-stats {
  display: flex;
  gap: 20px;
  margin-bottom: 20px;
}

.stat-item {
  flex: 1;
  text-align: center;
}

.stat-label {
  display: block;
  font-size: 14px;
  color: #666;
  margin-bottom: 8px;
}

.stat-value {
  display: block;
  font-size: 24px;
  font-weight: bold;
  color: #1890ff;
}

.data-table {
  border: 1px solid #f0f0f0;
  border-radius: 4px;
  overflow: hidden;
}

.table-header {
  display: flex;
  background-color: #fafafa;
  padding: 12px 16px;
  font-weight: 500;
  color: #333;
  border-bottom: 1px solid #f0f0f0;
}

.table-header span {
  flex: 1;
}

.table-row {
  display: flex;
  padding: 12px 16px;
  border-bottom: 1px solid #f0f0f0;
}

.table-row:last-child {
  border-bottom: none;
}

.table-row span {
  flex: 1;
}

.text-primary {
  color: #1890ff;
}

.text-success {
  color: #52c41a;
}

.text-error {
  color: #ff4d4f;
}

.detailed-stats {
  background: white;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  overflow: hidden;
}

.stats-content {
  padding: 20px 24px;
}

.stat-row {
  display: flex;
  justify-content: space-between;
  padding: 12px 0;
  border-bottom: 1px solid #f0f0f0;
}

.stat-row:last-child {
  border-bottom: none;
}

.stat-name {
  color: #666;
  font-size: 14px;
}

.stat-row .stat-value {
  font-size: 16px;
  font-weight: 500;
  color: #333;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .main-content {
    padding: 16px;
  }
  
  .charts-container {
    grid-template-columns: 1fr;
  }
  
  .stats-grid {
    grid-template-columns: 1fr 1fr;
  }
  
  .chart-stats {
    flex-direction: column;
    gap: 12px;
  }
  
  .simple-chart {
    padding: 16px;
  }
}

@media (max-width: 480px) {
  .stats-grid {
    grid-template-columns: 1fr;
  }
  
  .page-header h1 {
    font-size: 24px;
  }
  
  .stat-value {
    font-size: 24px;
  }
}
</style>