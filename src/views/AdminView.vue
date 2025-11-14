<template>
  <div class="admin-page">
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
        <div class="admin-layout">
          <!-- 左侧导航 -->
          <aside class="sidebar sidebar-left">
            <div class="card">
              <h3 class="card-title">管理员控制台</h3>
              <nav class="admin-nav">
                <a href="#" class="nav-item active" @click.prevent="activeTab = 'pending'">
                  <span class="nav-icon">⏳</span>
                  <span class="nav-text">待审核帖子</span>
                  <span v-if="pendingCount > 0" class="badge">{{ pendingCount }}</span>
                </a>
                <a href="#" class="nav-item" @click.prevent="activeTab = 'approved'">
                  <span class="nav-icon">✅</span>
                  <span class="nav-text">已通过帖子</span>
                </a>
                <a href="#" class="nav-item" @click.prevent="activeTab = 'rejected'">
                  <span class="nav-icon">❌</span>
                  <span class="nav-text">已拒绝帖子</span>
                </a>
              </nav>
              

            </div>
          </aside>
          
          <!-- 中间内容区域 -->
          <main class="content-center">
            <!-- 筛选和搜索 -->
            <div class="card filter-sort-card">
              <div class="filter-sort-bar">
                <div class="search-container">
                  <input 
                    type="text" 
                    v-model="searchQuery" 
                    placeholder="搜索帖子内容..."
                    class="search-input"
                    @input="handleSearch"
                  />
                  <button class="search-btn">🔍</button>
                </div>
                <select class="sort-select" v-model="sortBy" @change="loadPosts">
                  <option value="createdAt_desc">按时间倒序</option>
                  <option value="createdAt_asc">按时间正序</option>
                </select>
              </div>
            </div>
            
            <!-- 帖子列表 -->
            <div class="posts-container">
              <!-- 加载状态 -->
              <div v-if="loading" class="loading-container">
                <div class="loading-spinner"></div>
                <p>加载中...</p>
              </div>
              
              <!-- 空状态 -->
              <div v-else-if="posts.length === 0" class="empty-state">
                <div class="empty-icon">📝</div>
                <h3>暂无帖子</h3>
                <p v-if="activeTab === 'pending'">{{ searchQuery ? '没有找到匹配的待审核帖子' : '当前没有待审核的帖子' }}</p>
                <p v-else-if="activeTab === 'approved'">{{ searchQuery ? '没有找到匹配的已通过帖子' : '当前没有已通过的帖子' }}</p>
                <p v-else>{{ searchQuery ? '没有找到匹配的已拒绝帖子' : '当前没有已拒绝的帖子' }}</p>
              </div>
              
              <!-- 动态帖子列表 -->
              <template v-else>
                <article 
                  v-for="post in posts" 
                  :key="post.id || `post-${Math.random().toString(36).substr(2, 9)}`" 
                  :id="`post-${post.id || Math.random().toString(36).substr(2, 9)}`" 
                  class="card post-card"
                >
                <!-- 审核操作按钮 - 位于帖子顶部 -->
                <div class="review-actions">
                  <button 
                    class="btn btn-approve" 
                    @click="reviewPost(post.id, 'approved')"
                    :disabled="processingIds.includes(post.id)"
                    style="background-color: #4CAF50; color: white; padding: 8px 16px; border: none; border-radius: 4px; cursor: pointer; margin-right: 8px;"
                  >
                    <span v-if="processingIds.includes(post.id)">处理中...</span>
                    <span v-else>通过审核</span>
                  </button>
                  <button 
                    class="btn btn-reject" 
                    @click="showRejectModal(post)"
                    :disabled="processingIds.includes(post.id)"
                    style="background-color: #f44336; color: white; padding: 8px 16px; border: none; border-radius: 4px; cursor: pointer;"
                  >
                    拒绝审核
                  </button>
                </div>
                
                <header class="post-header">
                  <div class="user-info">
                    <div class="user-avatar">{{ (post?.author?.name || post?.username || '未知').charAt(0).toUpperCase() }}</div>
                    <div class="user-details">
                      <h4 class="user-name">{{ post?.author?.name || post?.username || '未知用户' }}</h4>
                      <p class="post-meta">{{ formatPostTime(post?.createdAt || post?.time || post.meta) }}</p>
                    </div>
                  </div>
                  
                  <!-- 审核状态标签 -->
                  <div class="status-badge" :class="getStatusClass(post.status)">
                    {{ getStatusText(post.status) }}
                  </div>
                </header>
                
                <!-- 帖子内容 -->
                <div class="post-content">
                  <p class="post-text">{{ post.content }}</p>
                  
                  <!-- 标签展示 -->
                  <div class="post-tags" v-if="post.tags && post.tags.length > 0">
                    <span v-for="tag in post.tags" :key="tag" class="tag">{{ tag }}</span>
                  </div>
                  
                  <!-- 图片展示 -->
                  <div v-if="post.images && post.images.length > 0" class="post-images">
                    <div 
                      v-for="(image, index) in post.images" 
                      :key="index" 
                      class="post-image"
                      @click="previewImage(image, index, post.images)"
                    >
                      <img :src="image" :alt="`帖子图片 ${index + 1}`" loading="lazy" />
                    </div>
                  </div>
                </div>
                
                <!-- 拒绝原因（已拒绝帖子显示） -->
                <div v-if="post.status === 'rejected' && post.rejectReason" class="reject-reason">
                  <strong>拒绝原因：</strong>{{ post.rejectReason }}
                </div>
                
                <footer class="post-engagement">
                  <span class="engagement-text">点赞数: {{ post.likes || 0 }}</span>
                  <span class="engagement-text">评论数: {{ post.commentsCount || 0 }}</span>
                  <span class="engagement-text">收藏数: {{ post.saves || 0 }}</span>
                </footer>
                </article>
              </template>
            </div>
            
            <!-- 加载更多 -->
            <div class="load-more-container" v-if="posts.length > 0">
              <button 
                v-if="hasMore" 
                @click="loadMorePosts"
                :disabled="isLoadingMore"
                class="load-more-btn"
              >
                <span v-if="isLoadingMore">
                  <div class="small-spinner"></div>
                  加载中...
                </span>
                <span v-else>加载更多</span>
              </button>
              <p v-else class="no-more-posts">没有更多帖子了</p>
            </div>
          </main>
        </div>
      </div>
    </main>
    
    <!-- 拒绝原因模态框 -->
    <div v-if="showModal" class="modal-overlay" @click.self="closeModal">
      <div class="modal-content">
        <h3>拒绝原因</h3>
        <textarea 
          v-model="rejectReason" 
          placeholder="请输入拒绝原因..."
          rows="4"
          maxlength="200"
          class="reason-input"
        ></textarea>
        <div class="char-count">{{ rejectReason.length }}/200</div>
        <div class="modal-actions">
          <button @click="closeModal" class="cancel-btn">取消</button>
          <button @click="confirmReject" class="confirm-btn" :disabled="!rejectReason.trim()">确认拒绝</button>
        </div>
      </div>
    </div>
    
    <!-- 图片预览模态框 -->
    <div v-if="previewImageUrl" class="image-preview-overlay" @click="closeImagePreview">
      <div class="image-preview-content">
        <img :src="previewImageUrl" :alt="'预览图片'" />
        <button class="close-preview-btn" @click="closeImagePreview">×</button>
        <div class="image-navigation" v-if="previewImages && previewImages.length > 1">
          <button @click="prevImage" class="nav-btn">←</button>
          <span class="image-counter">{{ previewCurrentIndex + 1 }}/{{ previewImages.length }}</span>
          <button @click="nextImage" class="nav-btn">→</button>
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
  name: 'AdminView',
  components: {
    AdminNavBar
  },
  data() {
    return {
        activeTab: 'pending',
        posts: [],
        loading: false,
        isLoadingMore: false,
        hasMore: true,
        page: 1,
        pageSize: 10,
        searchQuery: '',
        sortBy: 'createdAt_desc',
        pendingCount: 0,
        processingIds: [],
        showModal: false,
        rejectReason: '',
        currentPost: null,
        previewImageUrl: null,
        previewImages: [],
        previewCurrentIndex: 0,

      };
  },
  computed: {
    // 检查是否已登录
    checkLoginStatus() {
      return !!localStorage.getItem('authToken');
    },
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
    // 加载帖子列表
    this.loadPosts();
    // 初始获取待审核数量
    this.updatePendingCount();
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
        // 更灵活的权限验证逻辑，支持多种响应格式
        const isAdmin = response.success || response.code === 200 || 
                      response?.data?.isAdmin || response?.isAdmin;
        if (!isAdmin) {
          utils.showError('您没有管理员权限');
          this.$router.push('/');
        }
      } catch (error) {
        console.error('管理员权限验证失败:', error);
        utils.showError('权限验证失败，但在开发环境中允许继续访问');
        // 在开发环境下不重定向，便于开发和测试
        if (!import.meta.env.DEV) {
          this.$router.push('/login');
        }
      }
    },
    

    
    // 加载帖子
    async loadPosts() {
      this.loading = true;
      this.page = 1;
      this.posts = [];
      
      try {
        const status = this.activeTab;
        const params = {
          status,
          page: this.page,
          pageSize: this.pageSize,
          sortBy: this.sortBy,
          search: this.searchQuery
        };
        
        // 调用后端API获取帖子列表
        const response = await api.admin.getPosts(params);
        
        this.posts = response.data || [];
        this.hasMore = this.posts.length === this.pageSize;
        
        // 更新待审核数量
        if (status === 'pending') {
          this.updatePendingCount();
        }
      } catch (error) {
        console.error('获取帖子列表失败:', error);
        utils.handleApiError(error, '获取帖子列表失败');
      } finally {
        this.loading = false;
      }
    },
    
    // 加载更多帖子
    async loadMorePosts() {
      if (this.isLoadingMore || !this.hasMore) return;
      
      this.isLoadingMore = true;
      this.page++;
      
      try {
        const params = {
          status: this.activeTab,
          page: this.page,
          pageSize: this.pageSize,
          sortBy: this.sortBy,
          search: this.searchQuery
        };
        
        // 调用后端API加载更多帖子
        const response = await api.admin.getPosts(params);
        const newPosts = response.data || [];
        
        this.posts = [...this.posts, ...newPosts];
        this.hasMore = newPosts.length === this.pageSize;
      } catch (error) {
        console.error('加载更多帖子失败:', error);
        utils.handleApiError(error, '加载更多帖子失败');
        this.page--; // 回滚页码
      } finally {
        this.isLoadingMore = false;
      }
    },
    
    // 更新待审核数量
    async updatePendingCount() {
      try {
        const response = await api.admin.getPendingCount();
        this.pendingCount = response.count || 0;
      } catch (error) {
        console.error('获取待审核数量失败:', error);
        // 不影响主流程，静默处理错误
      }
    },
    

    
    // 审核帖子
    async reviewPost(postId, action, reason = '') {
      this.processingIds.push(postId);
      
      try {
        // 调用API审核帖子
        // 由于后端可能没有现成的API，这里模拟成功
        await this.performReview(postId, action, reason);
        
        // 更新本地数据
        const postIndex = this.posts.findIndex(p => p.id === postId);
        if (postIndex !== -1) {
          this.posts[postIndex].status = action;
          if (action === 'rejected' && reason) {
            this.posts[postIndex].rejectReason = reason;
          }
        }
        
        utils.showSuccess(action === 'approved' ? '帖子已通过审核' : '帖子已拒绝');
        
        // 如果是待审核列表，从列表中移除
        if (this.activeTab === 'pending') {
          this.posts.splice(postIndex, 1);
          this.pendingCount--;
        }
      } catch (error) {
        utils.handleApiError(error, action === 'approved' ? '审核通过失败' : '审核拒绝失败');
      } finally {
        this.processingIds = this.processingIds.filter(id => id !== postId);
        this.closeModal();
      }
    },
    
    // 执行审核操作（调用真实API）
    async performReview(postId, action, reason = '') {
      // 调用管理员审核API
      const response = await api.admin.reviewPost(postId, {
        action,
        reason
      });
      return response;
    },
    
    // 显示拒绝模态框
    showRejectModal(post) {
      this.currentPost = post;
      this.rejectReason = '';
      this.showModal = true;
    },
    
    // 关闭模态框
    closeModal() {
      this.showModal = false;
      this.currentPost = null;
      this.rejectReason = '';
    },
    
    // 确认拒绝
    confirmReject() {
      if (this.currentPost && this.rejectReason.trim()) {
        this.reviewPost(this.currentPost.id, 'rejected', this.rejectReason.trim());
      }
    },
    
    // 搜索处理
    handleSearch() {
      // 防抖处理
      clearTimeout(this.searchTimeout);
      this.searchTimeout = setTimeout(() => {
        this.loadPosts();
      }, 300);
    },
    
    // 格式化时间
    formatPostTime(time) {
      if (!time) return '';
      
      const date = new Date(time);
      const now = new Date();
      const diff = now - date;
      
      const minutes = Math.floor(diff / 60000);
      const hours = Math.floor(diff / 3600000);
      const days = Math.floor(diff / 86400000);
      
      if (minutes < 60) {
        return `${minutes}分钟前`;
      } else if (hours < 24) {
        return `${hours}小时前`;
      } else if (days < 30) {
        return `${days}天前`;
      } else {
        return date.toLocaleDateString();
      }
    },
    
    // 获取状态样式类
    getStatusClass(status) {
      switch (status) {
        case 'pending': return 'status-pending';
        case 'approved': return 'status-approved';
        case 'rejected': return 'status-rejected';
        default: return '';
      }
    },
    
    // 获取状态文本
    getStatusText(status) {
      // 检查status是否存在且为字符串类型
      if (!status || typeof status !== 'string') {
        return '未设置';
      }
      
      switch (status.toLowerCase()) {
        case 'pending': return '待审核';
        case 'approved': return '已审核';
        case 'rejected': return '已拒绝';
        case 'published': return '已发布';
        case 'draft': return '草稿';
        default: return status.charAt(0).toUpperCase() + status.slice(1);
      }
    },
    
    // 预览图片
    previewImage(image, index, images) {
      this.previewImageUrl = image;
      this.previewImages = images;
      this.previewCurrentIndex = index;
    },
    
    // 关闭图片预览
    closeImagePreview() {
      this.previewImageUrl = null;
      this.previewImages = [];
      this.previewCurrentIndex = 0;
    },
    
    // 上一张图片
    prevImage() {
      if (this.previewCurrentIndex > 0) {
        this.previewCurrentIndex--;
        this.previewImageUrl = this.previewImages[this.previewCurrentIndex];
      }
    },
    
    // 下一张图片
    nextImage() {
      if (this.previewCurrentIndex < this.previewImages.length - 1) {
        this.previewCurrentIndex++;
        this.previewImageUrl = this.previewImages[this.previewCurrentIndex];
      }
    }
  },
  watch: {
    // 监听标签变化，重新加载数据
    activeTab() {
      this.loadPosts();
    }
  }
};
</script>

<style scoped>
.admin-page {
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

.admin-layout {
  display: grid;
  grid-template-columns: 280px 1fr;
  gap: 2rem;
}

.sidebar {
  position: sticky;
  top: 2rem;
  height: fit-content;
}

.card {
  background: white;
  border-radius: 8px;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.1);
  padding: 1.5rem;
  margin-bottom: 1.5rem;
}

.card-title {
  font-size: 1.25rem;
  font-weight: 600;
  margin-bottom: 1rem;
  color: #333;
}

.admin-nav {
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
}

.nav-item {
  display: flex;
  align-items: center;
  gap: 0.75rem;
  padding: 0.75rem 1rem;
  border-radius: 6px;
  color: #666;
  text-decoration: none;
  transition: all 0.2s ease;
  position: relative;
}

.nav-item:hover {
  background-color: #f8f9fa;
  color: #667eea;
}

.nav-item.active {
  background-color: #4356b7;
  color: white;
  font-weight: 600;
  box-shadow: 0 2px 8px rgba(67, 86, 183, 0.3);
}

.nav-icon {
  font-size: 1.2rem;
}

/* 开发工具样式 */
.dev-tools {
  margin-top: 2rem;
  padding-top: 1rem;
  border-top: 1px solid #eee;
}

.mock-login-btn,
.mock-logout-btn {
  width: 100%;
  padding: 0.75rem;
  border: none;
  border-radius: 6px;
  font-size: 0.9rem;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.2s ease;
}

.mock-login-btn {
  background-color: #28a745;
  color: white;
}

.mock-login-btn:hover {
  background-color: #218838;
}

.mock-logout-btn {
  background-color: #dc3545;
  color: white;
}

.mock-logout-btn:hover {
  background-color: #c82333;
}

.badge {
  margin-left: auto;
  background-color: #ff4757;
  color: white;
  padding: 0.2rem 0.5rem;
  border-radius: 10px;
  font-size: 0.875rem;
  font-weight: 500;
}

.content-center {
  min-width: 0;
}

.filter-sort-bar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  flex-wrap: wrap;
  gap: 1rem;
}

.search-container {
  position: relative;
  flex: 1;
  max-width: 400px;
}

.search-input {
  width: 100%;
  padding: 0.75rem 2.5rem 0.75rem 1rem;
  border: 1px solid #ddd;
  border-radius: 6px;
  font-size: 1rem;
  transition: border-color 0.2s ease;
}

.search-input:focus {
  outline: none;
  border-color: #667eea;
  box-shadow: 0 0 0 3px rgba(102, 126, 234, 0.1);
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
  color: #666;
}

.sort-select {
  padding: 0.75rem 1rem;
  border: 1px solid #ddd;
  border-radius: 6px;
  font-size: 1rem;
  background: white;
  cursor: pointer;
  transition: border-color 0.2s ease;
}

.sort-select:focus {
  outline: none;
  border-color: #667eea;
  box-shadow: 0 0 0 3px rgba(102, 126, 234, 0.1);
}

.posts-container {
  display: flex;
  flex-direction: column;
  gap: 1.5rem;
}

.loading-container,
.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 3rem 1rem;
  text-align: center;
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

.empty-icon {
  font-size: 3rem;
  margin-bottom: 1rem;
}

.empty-state h3 {
  margin-bottom: 0.5rem;
  color: #333;
}

.empty-state p {
  color: #666;
}

.post-card {
  transition: transform 0.2s ease, box-shadow 0.2s ease;
}

.post-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 15px rgba(0, 0, 0, 0.1);
}

.post-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 1rem;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 1rem;
}

.user-avatar {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: 600;
  font-size: 1.1rem;
}

.user-name {
  font-weight: 600;
  color: #333;
  margin: 0;
}

.post-meta {
  font-size: 0.875rem;
  color: #666;
  margin: 0.25rem 0 0 0;
}

.status-badge {
  padding: 0.25rem 0.75rem;
  border-radius: 20px;
  font-size: 0.875rem;
  font-weight: 500;
}

.status-pending {
  background-color: #fff3cd;
  color: #856404;
}

.status-approved {
  background-color: #d4edda;
  color: #155724;
}

.status-rejected {
  background-color: #f8d7da;
  color: #721c24;
}

.post-content {
  margin-bottom: 1rem;
}

.post-text {
  line-height: 1.6;
  color: #333;
  margin-bottom: 1rem;
  white-space: pre-line;
}

.post-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 0.5rem;
  margin-bottom: 1rem;
}

.tag {
  padding: 0.25rem 0.75rem;
  background-color: #f1f3f4;
  border-radius: 16px;
  font-size: 0.875rem;
  color: #5f6368;
}

.post-images {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(150px, 1fr));
  gap: 0.5rem;
  margin-top: 1rem;
}

.post-image {
  position: relative;
  overflow: hidden;
  border-radius: 6px;
  cursor: pointer;
  aspect-ratio: 1;
}

.post-image img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 0.2s ease;
}

.post-image:hover img {
  transform: scale(1.05);
}

.review-actions {
  display: flex;
  gap: 1rem;
  margin: 1rem 0;
}

.approve-btn,
.reject-btn,
.cancel-btn,
.confirm-btn {
  padding: 0.5rem 1.5rem;
  border: none;
  border-radius: 6px;
  font-size: 1rem;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.2s ease;
}

.approve-btn {
  background-color: #28a745;
  color: white;
}

.approve-btn:hover:not(:disabled) {
  background-color: #218838;
}

.reject-btn,
.cancel-btn {
  background-color: #dc3545;
  color: white;
}

.reject-btn:hover:not(:disabled),
.cancel-btn:hover:not(:disabled) {
  background-color: #c82333;
}

.confirm-btn {
  background-color: #667eea;
  color: white;
}

.confirm-btn:hover:not(:disabled) {
  background-color: #5a67d8;
}

.approve-btn:disabled,
.reject-btn:disabled,
.confirm-btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.reject-reason {
  background-color: #f8d7da;
  border: 1px solid #f5c6cb;
  border-radius: 6px;
  padding: 1rem;
  margin: 1rem 0;
  color: #721c24;
  font-size: 0.9rem;
}

.post-engagement {
  display: flex;
  gap: 2rem;
  padding-top: 1rem;
  border-top: 1px solid #eee;
}

.engagement-text {
  color: #666;
  font-size: 0.9rem;
}

.load-more-container {
  text-align: center;
  padding: 2rem 0;
}

.load-more-btn {
  padding: 0.75rem 2rem;
  background-color: #667eea;
  color: white;
  border: none;
  border-radius: 6px;
  font-size: 1rem;
  cursor: pointer;
  transition: background-color 0.2s ease;
  display: inline-flex;
  align-items: center;
  gap: 0.5rem;
}

.load-more-btn:hover:not(:disabled) {
  background-color: #5a67d8;
}

.load-more-btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.small-spinner {
  width: 16px;
  height: 16px;
  border: 2px solid #fff;
  border-top: 2px solid transparent;
  border-radius: 50%;
  animation: spin 1s linear infinite;
}

.no-more-posts {
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
  border-radius: 8px;
  padding: 2rem;
  max-width: 500px;
  width: 90%;
  max-height: 80vh;
  overflow-y: auto;
}

.modal-content h3 {
  margin-top: 0;
  margin-bottom: 1rem;
  color: #333;
}

.reason-input {
  width: 100%;
  padding: 0.75rem;
  border: 1px solid #ddd;
  border-radius: 6px;
  font-size: 1rem;
  resize: vertical;
  font-family: inherit;
}

.reason-input:focus {
  outline: none;
  border-color: #667eea;
  box-shadow: 0 0 0 3px rgba(102, 126, 234, 0.1);
}

.char-count {
  text-align: right;
  font-size: 0.875rem;
  color: #666;
  margin-top: 0.5rem;
}

.modal-actions {
  display: flex;
  justify-content: flex-end;
  gap: 1rem;
  margin-top: 1.5rem;
}

/* 图片预览样式 */
.image-preview-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background-color: rgba(0, 0, 0, 0.9);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1100;
  cursor: pointer;
}

.image-preview-content {
  position: relative;
  max-width: 90%;
  max-height: 90vh;
  cursor: default;
}

.image-preview-content img {
  max-width: 100%;
  max-height: 80vh;
  object-fit: contain;
  border-radius: 4px;
}

.close-preview-btn {
  position: absolute;
  top: -30px;
  right: 0;
  background: none;
  border: none;
  color: white;
  font-size: 2rem;
  cursor: pointer;
  padding: 0;
  width: 30px;
  height: 30px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.image-navigation {
  position: absolute;
  bottom: -30px;
  left: 50%;
  transform: translateX(-50%);
  display: flex;
  align-items: center;
  gap: 1rem;
  color: white;
}

.nav-btn {
  background: rgba(255, 255, 255, 0.2);
  border: none;
  color: white;
  width: 30px;
  height: 30px;
  border-radius: 50%;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: background-color 0.2s ease;
}

.nav-btn:hover {
  background: rgba(255, 255, 255, 0.3);
}

.image-counter {
  font-size: 0.9rem;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .admin-layout {
    grid-template-columns: 1fr;
  }
  
  .sidebar {
    position: static;
    margin-bottom: 2rem;
  }
  
  .filter-sort-bar {
    flex-direction: column;
    align-items: stretch;
  }
  
  .search-container {
    max-width: none;
  }
  
  .review-actions {
    flex-direction: column;
  }
  
  .post-engagement {
    gap: 1rem;
    flex-wrap: wrap;
  }
}
  /* 测试登录相关样式 */
  .test-login-container {
    display: flex;
    justify-content: center;
    margin: 20px 0;
  }
  
  .test-login-card {
    background: #f9fafc;
    border: 1px solid #e1e5e9;
    border-radius: 8px;
    padding: 20px;
    text-align: center;
    width: 100%;
    max-width: 400px;
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  }
  
  .test-login-card h3 {
    margin: 0 0 10px 0;
    color: #333;
  }
  
  .test-login-card p {
    margin: 0 0 15px 0;
    color: #666;
  }
  
  .test-login-btn {
    background: linear-gradient(135deg, #4caf50, #45a049);
    color: white;
    border: none;
    border-radius: 6px;
    padding: 10px 20px;
    font-size: 16px;
    cursor: pointer;
    transition: all 0.3s ease;
  }
  
  .test-login-btn:hover {
    background: linear-gradient(135deg, #45a049, #4caf50);
    transform: translateY(-1px);
    box-shadow: 0 4px 12px rgba(76, 175, 80, 0.3);
  }
</style>