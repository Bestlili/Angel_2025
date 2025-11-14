<template>
  <div class="community-page">
    <NavBar />
    
    <!-- 顶部装饰 -->
    <div class="community-header-decoration">
      <!-- 装饰性波浪效果 -->
      <div class="wave-shape"></div>
    </div>
    
    <main class="main-content">
      <div class="container">
        <div class="community-layout">
          <!-- 左侧社区导航 -->
          <aside class="sidebar sidebar-left">
            <div class="card">
              <h3 class="card-title">社区导航</h3>
              <nav class="community-nav">
                <a href="#" class="nav-item active">
                  <span class="nav-icon">🏠</span>
                  <span class="nav-text">首页</span>
                </a>
                <a href="#" class="nav-item">
                  <span class="nav-icon">🔥</span>
                  <span class="nav-text">热门讨论</span>
                </a>
                <a href="#" class="nav-item">
                  <span class="nav-icon">💖</span>
                  <span class="nav-text">情绪支持</span>
                </a>
                <a href="#" class="nav-item">
                  <span class="nav-icon">💡</span>
                  <span class="nav-text">经验分享</span>
                </a>
                <a href="#" class="nav-item">
                  <span class="nav-icon">📚</span>
                  <span class="nav-text">学习资源</span>
                </a>
                <a href="#" class="nav-item">
                  <span class="nav-icon">🏠</span>
                  <span class="nav-text">线下活动</span>
                </a>
                <a href="#" class="nav-item">
                  <span class="nav-icon">🚩</span>
                  <span class="nav-text">求助中心</span>
                </a>
              </nav>
              
              <!-- 未登录提示 -->
              <div class="login-prompt">
                <div class="login-info">
                  <p class="login-status">{{ userName }}</p>
                </div>
              </div>
            </div>
          </aside>
          
          <!-- 中间内容区域 -->
          <main class="content-center">
            <!-- 筛选排序栏 -->
            <div class="card filter-sort-card">
              <div class="filter-sort-bar">
                <div class="filter-tabs">
                  <button class="filter-tab active">推荐</button>
                  <button class="filter-tab">最新</button>
                  <button class="filter-tab">热门</button>
                  <button class="filter-tab">关注</button>
                </div>
                <div class="sort-and-refresh">
                  <select class="sort-select">
                    <option>按时间排序</option>
                    <option>按热度排序</option>
                    <option>按回复排序</option>
                  </select>
                  <!-- 刷新按钮 -->
                  <button
                    @click="refreshPosts"
                    :disabled="loading"
                    class="refresh-btn"
                    title="刷新帖子列表"
                  >
                    <span v-if="loading">
                      <div class="small-spinner"></div>
                    </span>
                    <span v-else>🔄</span>
                  </button>
                </div>
              </div>
            </div>
            
            <!-- 发布框 -->
            <div class="post-create-card card">
              <div class="user-avatar">{{ isLoggedIn ? userInitial : '' }}</div>
              <div class="post-input-container">
                <textarea 
                  v-model="postContent"
                  placeholder="分享你的心情和想法..."
                  class="post-input"
                  rows="3"
                  maxlength="500"
                  :disabled="!isLoggedIn"
                ></textarea>
                
                <!-- 字数统计 -->
                <div class="char-count">{{ postContent.length }}/500</div>
                
                <!-- 图片预览 -->
                <div v-if="selectedImages.length > 0" class="image-preview-container">
                  <div v-for="image in selectedImages" :key="image.id" class="preview-item">
                    <div class="preview-thumb" v-if="!image.uploading">
                      <img :src="image.url" alt="预览" />
                      <button @click="removeImage(image.id)" class="remove-image-btn">
                        <svg width="16" height="16" viewBox="0 0 16 16" fill="none">
                          <path d="M12 4L4 12M4 4L12 12" stroke="white" stroke-width="2" stroke-linecap="round"/>
                        </svg>
                      </button>
                    </div>
                    <div v-else class="uploading-thumb">
                      <div class="upload-spinner"></div>
                      <p>上传中...</p>
                    </div>
                  </div>
                </div>
              </div>
              <div class="post-actions">
                <div class="post-tools">
                  <label 
                    class="tool-btn" 
                    :title="`已选择 ${selectedImages.length}/9 张图片`"
                    :class="{ 'disabled': selectedImages.length >= 9 || !isLoggedIn }"
                  >
                    <input 
                      type="file" 
                      multiple 
                      accept="image/*" 
                      @change="handleImageSelect"
                      style="display: none;"
                      :disabled="selectedImages.length >= 9 || !isLoggedIn"
                    />
                    📷
                    图片
                  </label>

                </div>
                <button 
                  class="publish-btn" 
                  :disabled="!isLoggedIn || (!postContent.trim() && selectedImages.length === 0)"
                  @click="publishPost"
                >
                  发布
                </button>
              </div>
            </div>
            
            <!-- 帖子列表 -->
            
            <!-- 帖子列表 -->
            <div class="posts-container">
              <!-- 加载状态 -->
              <div v-if="loading" class="loading-container">
                <div class="loading-spinner"></div>
                <p>加载中...</p>
              </div>
              
              <!-- 空状态 -->
              <div v-else-if="!posts || posts.length === 0" class="empty-state">
                <div class="empty-icon">📝</div>
                <h3>暂无帖子</h3>
                <p>社区还没有内容，快来发布第一条帖子吧！</p>
                <button class="create-post-btn" @click="focusPostInput" v-if="isLoggedIn">
                  发布帖子
                </button>
                <p v-else class="login-hint">登录后即可发布帖子</p>
              </div>
              
              <!-- 动态帖子列表 -->
              <template v-else>
                
                <!-- 简化的帖子列表渲染 -->
                <div 
                  v-for="(post, index) in posts" 
                  :key="post?.id || `post-${index}`" 
                  :id="`post-${post?.id || index}`" 
                  class="card post-card modern-post"
                  style="background: #fff; padding: 20px; margin-bottom: 20px; border-radius: 8px; border: 1px solid #ddd; box-shadow: 0 2px 4px rgba(0,0,0,0.1);"
                >
                  <!-- 帖子基本信息 -->
                  <div style="display: flex; justify-content: space-between; margin-bottom: 15px;">
                    <div style="display: flex; align-items: center;">
                      <div style="width: 40px; height: 40px; background: linear-gradient(45deg, #9c27b0, #673ab7); color: white; border-radius: 50%; display: flex; align-items: center; justify-content: center; font-weight: bold; margin-right: 12px;">
                        {{ (post?.author?.name || post?.username || '未知').charAt(0).toUpperCase() }}
                      </div>
                      <div>
                        <h4 style="margin: 0; font-size: 16px;">{{ post?.author?.name || post?.username || '未知用户' }}</h4>
                        <p style="margin: 0; font-size: 14px; color: #666;">{{ formatPostTime(post?.createdAt) || '未知时间' }}</p>
                      </div>
                    </div>
                  </div>
                  
                  <!-- 帖子内容 -->
                  <div style="margin-bottom: 15px; font-size: 16px; line-height: 1.5;">
                    {{ post?.content || '无内容' }}
                  </div>
                  
                  <!-- 帖子互动信息 -->
                  <div style="display: flex; gap: 20px; font-size: 14px; color: #666;">
                    <span>❤️ {{ post?.likes || 0 }} 赞</span>
                    <span>💬 {{ post?.commentsCount || 0 }} 评论</span>
                  </div>
                </div>
              </template>
            </div>
            
            <!-- 加载更多 -->
            <div class="load-more-container" v-if="posts && posts.filter(post => post && typeof post === 'object').length > 0">
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
          
          <!-- 右侧边栏 -->
          <aside class="sidebar sidebar-right">
            <!-- 热门话题 -->
            <div class="card">
              <h3 class="card-title">热门话题</h3>
              <div class="topics-list">
                <template v-if="hotTopics.length > 0">
                  <a 
                    v-for="topic in hotTopics" 
                    :key="topic.id" 
                    href="#" 
                    class="topic-item"
                  >
                    <p class="topic-name">{{ topic.name }}</p>
                    <div class="topic-meta" v-if="topic.isHot">
                      <span class="topic-participants">{{ topic.participants }}人参与</span>
                      <span class="topic-badge">热门</span>
                    </div>
                    <span v-else class="topic-participants">{{ topic.participants }}人参与</span>
                  </a>
                </template>
                <template v-else>
                  <a href="#" class="topic-item">
                    <p class="topic-name"># 如何应对职场压力</p>
                    <div class="topic-meta">
                      <span class="topic-participants">128人参与</span>
                      <span class="topic-badge">热门</span>
                    </div>
                  </a>
                  <a href="#" class="topic-item">
                    <p class="topic-name"># 正念冥想体验</p>
                    <span class="topic-participants">93人参与</span>
                  </a>
                  <a href="#" class="topic-item">
                    <p class="topic-name"># 改善睡眠质量的方法</p>
                    <span class="topic-participants">76人参与</span>
                  </a>
                  <a href="#" class="topic-item">
                    <p class="topic-name"># 建立健康边界</p>
                    <span class="topic-participants">63人参与</span>
                  </a>
                  <a href="#" class="topic-item">
                    <p class="topic-name"># 自我关怀小技巧</p>
                    <span class="topic-participants">52人参与</span>
                  </a>
                </template>
              </div>
              <button class="view-all-btn">查看全部</button>
            </div>
            

          </aside>
        </div>
      </div>
    </main>
    
    <!-- 页脚 -->
    <footer class="footer">
      <div class="container">
        <div class="footer-content">
          <div class="footer-info">
            <div class="logo-container">
              <h3 class="logo">Angel</h3>
              <div class="logo-icon">👼</div>
            </div>
            <p class="slogan">让AI守护您的心灵健康</p>
            <p class="footer-description">专业、温暖、安全的心理健康服务平台，
            随时随地为您提供心理支持。</p>
          </div>
          <div class="footer-links">
            <div class="link-group">
              <h4 class="link-title">平台服务</h4>
              <ul>
                <li><a href="#" class="footer-link">关于我们</a></li>
                <li><a href="#" class="footer-link">服务条款</a></li>
                <li><a href="#" class="footer-link">隐私政策</a></li>
                <li><a href="#" class="footer-link">联系我们</a></li>
              </ul>
            </div>
            <div class="link-group">
              <h4 class="link-title">关注我们</h4>
              <div class="social-icons">
                <a href="#" class="social-icon" aria-label="微信"><i class="fa fa-weixin"></i></a>
                <a href="#" class="social-icon" aria-label="微博"><i class="fa fa-weibo"></i></a>
                <a href="#" class="social-icon" aria-label="邮箱"><i class="fa fa-envelope"></i></a>
              </div>
              <p class="contact-email">support@angel-ai.com</p>
            </div>
          </div>
        </div>
        <div class="footer-bottom">
          <div class="divider"></div>
          <p class="copyright">&copy; {{ new Date().getFullYear() }} Angel. 保留所有权利。</p>
          <div class="footer-note">
            <small>本平台提供的服务不能替代专业医疗建议或治疗</small>
          </div>
        </div>
      </div>
    </footer>
    
    <!-- 提示弹窗 -->
    <div 
      v-if="showToast" 
      class="toast" 
      :class="toastType"
      role="alert"
    >
      <span class="toast-icon">{{ toastIcon }}</span>
      <span class="toast-message">{{ toastMessage }}</span>
    </div>
    
    <!-- 图片预览组件 -->
    <div v-if="showImagePreview" class="image-preview-modal" @click="closeImagePreview">
      <div class="preview-overlay"></div>
      <div class="preview-content" @click.stop>
        <button class="preview-close-btn" @click="closeImagePreview">
          <svg width="24" height="24" viewBox="0 0 24 24" fill="white">
            <path d="M18 6L6 18M6 6L18 18" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
          </svg>
        </button>
        
        <button 
          v-if="allPreviewImages.length > 1" 
          class="preview-nav-btn preview-prev"
          @click="changePreviewImage('prev')"
        >
          <svg width="24" height="24" viewBox="0 0 24 24" fill="white">
            <path d="M15 18L9 12L15 6" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
          </svg>
        </button>
        
        <div class="preview-image-container">
          <img 
            :src="allPreviewImages[currentPreviewIndex]" 
            :alt="`预览图片 ${currentPreviewIndex + 1}`"
            class="preview-image"
          />
        </div>
        
        <button 
          v-if="allPreviewImages.length > 1" 
          class="preview-nav-btn preview-next"
          @click="changePreviewImage('next')"
        >
          <svg width="24" height="24" viewBox="0 0 24 24" fill="white">
            <path d="M9 18L15 12L9 6" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
          </svg>
        </button>
        
        <div v-if="allPreviewImages.length > 1" class="preview-indicator">
          {{ currentPreviewIndex + 1 }} / {{ allPreviewImages.length }}
        </div>
      </div>
    </div>
  </div>
</template>
<style scoped>
/* 现有样式... */

/* 发布框增强样式 */
.char-count {
  text-align: right;
  font-size: 12px;
  color: #999;
  margin-top: 4px;
}

.image-preview-container.post-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  margin-top: 16px;
  padding-top: 12px;
  border-top: 1px dashed var(--border-color);
}

.preview-item {
  position: relative;
  width: 80px;
  height: 80px;
}

.preview-thumb {
  width: 100%;
  height: 100%;
  border-radius: 6px;
  overflow: hidden;
  position: relative;
}

.preview-thumb img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.remove-image-btn {
  position: absolute;
  top: 4px;
  right: 4px;
  background-color: rgba(0, 0, 0, 0.6);
  border: none;
  border-radius: 50%;
  width: 24px;
  height: 24px;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  transition: background-color 0.2s;
}

.remove-image-btn:hover {
  background-color: rgba(0, 0, 0, 0.8);
}

.uploading-thumb {
  width: 100%;
  height: 100%;
  background-color: #f0f0f0;
  border-radius: 6px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
}

.upload-spinner {
  width: 20px;
  height: 20px;
  border: 2px solid #e0e0e0;
  border-top: 2px solid #4f46e5;
  border-radius: 50%;
  animation: spin 1s linear infinite;
}

.tool-btn {
  display: flex;
  align-items: center;
  gap: 4px;
  opacity: 1;
  transition: opacity 0.2s;
}

.tool-btn.disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

/* 加载状态 */
.loading-container {
  text-align: center;
  padding: 40px 0;
}

.loading-spinner {
  width: 32px;
  height: 32px;
  border: 3px solid #e0e0e0;
  border-top: 3px solid #4f46e5;
  border-radius: 50%;
  animation: spin 1s linear infinite;
  margin: 0 auto 12px;
}

.small-spinner {
  display: inline-block;
  width: 16px;
  height: 16px;
  border: 2px solid #e0e0e0;
  border-top: 2px solid #4f46e5;
  border-radius: 50%;
  animation: spin 1s linear infinite;
  margin-right: 8px;
  vertical-align: middle;
}

@keyframes spin {
  0% { transform: rotate(0deg); }
  100% { transform: rotate(360deg); }
}

/* 刷新按钮和排序区域样式 */
.sort-and-refresh {
  display: flex;
  align-items: center;
  gap: 0.5rem;
}

.refresh-btn {
  width: 36px;
  height: 36px;
  border: none;
  background: #f5f5f5;
  border-radius: 50%;
  cursor: pointer;
  font-size: 1.2rem;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.3s ease;
  padding: 0;
}

.refresh-btn:hover:not(:disabled) {
  background: #e8e8e8;
  transform: rotate(180deg);
}

.refresh-btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.small-spinner {
  width: 16px;
  height: 16px;
  border: 2px solid #f3f3f3;
  border-top: 2px solid #4CAF50;
  border-radius: 50%;
  animation: spin 1s linear infinite;
}

/* 空状态 */
.empty-state {
  text-align: center;
  padding: 40px 20px;
  color: #666;
}

.create-post-btn {
  margin-top: 20px;
  padding: 10px 24px;
  background-color: #4f46e5;
  color: white;
  border: none;
  border-radius: 8px;
  font-size: 16px;
  font-weight: 500;
  cursor: pointer;
  transition: background-color 0.2s;
}

.create-post-btn:hover {
  background-color: #4338ca;
}

.login-hint {
  margin-top: 15px;
  color: #9ca3af;
  font-size: 14px;
}

.empty-icon {
  font-size: 48px;
  margin-bottom: 16px;
}

.empty-state h3 {
  margin-bottom: 8px;
  color: #333;
}

/* 帖子选项菜单 */
.post-owner-menu {
  position: relative;
}

.post-options-menu {
  position: absolute;
  top: 100%;
  right: 0;
  background: white;
  border-radius: 6px;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.1);
  min-width: 120px;
  z-index: 100;
  overflow: hidden;
}

.post-options-menu button {
  width: 100%;
  padding: 10px 16px;
  text-align: left;
  border: none;
  background: none;
  cursor: pointer;
  transition: background-color 0.2s;
}

.post-options-menu button:hover {
  background-color: #f5f5f5;
}

.post-options-menu button.danger {
  color: #ef4444;
}

.post-options-menu button.danger:hover {
  background-color: #fee2e2;
}

/* 编辑模式 */
.post-edit {
  padding: 16px;
  background-color: #f9fafb;
  border-radius: 6px;
  margin: 16px 0;
}

.post-edit textarea {
  width: 100%;
  padding: 12px;
  border: 1px solid #ddd;
  border-radius: 6px;
  resize: vertical;
  min-height: 100px;
}

.edit-actions {
  display: flex;
  gap: 12px;
  justify-content: flex-end;
  margin-top: 12px;
}

.cancel-btn, .save-btn {
  padding: 8px 16px;
  border-radius: 6px;
  border: none;
  cursor: pointer;
  transition: background-color 0.2s;
}

.cancel-btn {
  background-color: #f3f4f6;
  color: #333;
}

.cancel-btn:hover {
  background-color: #e5e7eb;
}

.save-btn {
  background-color: #4f46e5;
  color: white;
}

.save-btn:hover {
  background-color: #4338ca;
}

/* 标签样式 */
.post-tags {
  margin: 12px 0;
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
}

.tag {
  background-color: #eef2ff;
  color: #4338ca;
  padding: 4px 12px;
  border-radius: 16px;
  font-size: 12px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.2s ease;
}

.tag:hover {
  background-color: #ddd6fe;
  transform: translateY(-1px);
}

/* 现代化帖子卡片样式 */
.modern-post {
  border-radius: 12px;
  border: 1px solid #e5e7eb;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.05);
  margin-bottom: 16px;
  overflow: hidden;
  transition: box-shadow 0.3s ease;
}

.modern-post:hover {
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

/* 现代化用户头像 */
.modern-avatar {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: 600;
  font-size: 16px;
  background: linear-gradient(135deg, #4f46e5, #3b82f6);
  color: white;
  border: 2px solid white;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

/* 图片展示 */
.post-images {
  margin-top: 12px;
  margin-bottom: 12px;
}

.single-image {
  aspect-ratio: 16 / 9;
  border-radius: 8px;
  overflow: hidden;
}

.single-image img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.image-grid {
  display: grid;
  gap: 4px;
  border-radius: 8px;
  overflow: hidden;
}

.image-grid-2 {
  grid-template-columns: 1fr 1fr;
  grid-template-rows: 300px;
}

.image-grid-3 {
  grid-template-columns: 2fr 1fr;
  grid-template-rows: 150px 150px;
}

.image-grid-3 .grid-image:nth-child(1) {
  grid-column: 1;
  grid-row: 1 / 3;
}

.image-grid-3 .grid-image:nth-child(2) {
  grid-column: 2;
  grid-row: 1;
}

.image-grid-3 .grid-image:nth-child(3) {
  grid-column: 2;
  grid-row: 2;
}

.image-grid-4 {
  grid-template-columns: 1fr 1fr;
  grid-template-rows: 1fr 1fr;
  height: 300px;
}

.grid-image {
  overflow: hidden;
  position: relative;
}

.grid-image img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 0.3s ease;
}

.grid-image:hover img {
  transform: scale(1.05);
}

.image-overlay {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background-color: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  font-size: 24px;
  font-weight: 600;
}

/* 互动按钮增强 */
.modern-engagement {
  display: flex;
  justify-content: space-between;
  padding: 8px 16px;
  border-top: 1px solid #f3f4f6;
}

.engagement-btn {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 8px 16px;
  border-radius: 20px;
  background: transparent;
  border: none;
  color: #6b7280;
  font-size: 14px;
  cursor: pointer;
  transition: all 0.2s ease;
}

.engagement-btn:hover {
  background-color: #f3f4f6;
  color: #374151;
  transform: translateY(-1px);
}

.engagement-btn.active {
  color: #ef4444;
  background-color: #fef2f2;
}

.engagement-btn.active:hover {
  background-color: #fee2e2;
}

.engagement-btn:nth-child(4).active {
  color: #f59e0b;
  background-color: #fffbeb;
}

.engagement-btn:nth-child(4).active:hover {
  background-color: #fef3c7;
}

/* 标签样式增强 */
.post-tags {
  margin: 12px 0;
}

.tag {
  background-color: #eef2ff;
  color: #4338ca;
  padding: 4px 12px;
  border-radius: 16px;
  font-size: 12px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.2s ease;
}

.tag:hover {
  background-color: #ddd6fe;
  transform: translateY(-1px);
}

/* 响应式调整 */
@media (max-width: 768px) {
  .image-grid-2, .image-grid-3, .image-grid-4 {
    height: 200px;
  }
  
  .modern-engagement {
    padding: 8px;
  }
  
  .engagement-btn {
    padding: 6px 12px;
    font-size: 13px;
  }
}

/* 评论区域 */
.comments-section {
  margin-top: 16px;
  padding-top: 16px;
  border-top: 1px solid #e5e7eb;
}

.comments-list {
  max-height: 400px;
  overflow-y: auto;
  margin-bottom: 16px;
}

.comment-item {
  padding: 12px 0;
  border-bottom: 1px solid #f3f4f6;
}

.comment-item:last-child {
  border-bottom: none;
}

.comment-header {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 8px;
}

.comment-avatar {
  width: 32px;
  height: 32px;
  background-color: #4f46e5;
  color: white;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 14px;
  font-weight: 600;
}

.comment-info {
  flex: 1;
}

.comment-author {
  font-weight: 600;
  font-size: 14px;
}

.comment-time {
  font-size: 12px;
  color: #6b7280;
}

.comment-content {
  margin-left: 42px;
  font-size: 14px;
  line-height: 1.5;
}

.comment-actions {
  margin-left: 42px;
  margin-top: 6px;
}

.comment-like-btn {
  background: none;
  border: none;
  font-size: 12px;
  color: #6b7280;
  cursor: pointer;
  display: flex;
  align-items: center;
  gap: 4px;
}

.no-comments {
  text-align: center;
  padding: 20px 0;
  color: #6b7280;
  font-size: 14px;
}

.comment-input-container {
  margin-top: 16px;
}

.comment-input {
  width: 100%;
  padding: 10px;
  border: 1px solid #ddd;
  border-radius: 6px;
  resize: vertical;
  font-size: 14px;
}

.comment-submit-btn {
  margin-top: 8px;
  padding: 6px 16px;
  background-color: #4f46e5;
  color: white;
  border: none;
  border-radius: 6px;
  cursor: pointer;
  float: right;
  transition: background-color 0.2s;
}

.comment-submit-btn:hover:not(:disabled) {
  background-color: #4338ca;
}

.comment-submit-btn:disabled {
  background-color: #9ca3af;
  cursor: not-allowed;
}

/* 关注按钮 */
.follow-btn {
  padding: 6px 16px;
  border: 1px solid #3730a3;
  border-radius: 20px;
  background-color: transparent;
  color: #3730a3;
  cursor: pointer;
  font-size: 14px;
  transition: all 0.2s;
  font-weight: 500;
}

.follow-btn:hover {
  background-color: #3730a3;
  color: white;
}

.follow-btn.following {
  background-color: #3730a3;
  color: white;
}

.follow-btn.following:hover {
  background-color: transparent;
  color: #3730a3;
}

/* 加载更多增强 */
.load-more-btn {
  transition: all 0.2s;
  display: inline-flex;
  align-items: center;
}

.load-more-btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.no-more-posts {
  text-align: center;
  color: #6b7280;
  font-size: 14px;
  padding: 20px 0;
}

/* 图片预览模态框 */
.image-preview-modal {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  z-index: 1000;
  display: flex;
  align-items: center;
  justify-content: center;
}

.preview-overlay {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background-color: rgba(0, 0, 0, 0.9);
}

.preview-content {
  position: relative;
  max-width: 90%;
  max-height: 90%;
  display: flex;
  align-items: center;
  justify-content: center;
}

.preview-close-btn {
  position: absolute;
  top: -40px;
  right: -20px;
  background: none;
  border: none;
  color: white;
  font-size: 24px;
  cursor: pointer;
  width: 40px;
  height: 40px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 50%;
  transition: background-color 0.2s;
}

.preview-close-btn:hover {
  background-color: rgba(255, 255, 255, 0.1);
}

.preview-nav-btn {
  position: absolute;
  top: 50%;
  transform: translateY(-50%);
  background: none;
  border: none;
  color: white;
  font-size: 32px;
  cursor: pointer;
  width: 50px;
  height: 50px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 50%;
  background-color: rgba(0, 0, 0, 0.3);
  transition: background-color 0.2s;
}

.preview-nav-btn:hover {
  background-color: rgba(0, 0, 0, 0.5);
}

.preview-prev {
  left: -60px;
}

.preview-next {
  right: -60px;
}

.preview-image-container {
  max-width: 100%;
  max-height: 80vh;
  overflow: hidden;
  border-radius: 8px;
}

.preview-image {
  max-width: 100%;
  max-height: 80vh;
  object-fit: contain;
}

.preview-indicator {
  position: absolute;
  bottom: -40px;
  left: 50%;
  transform: translateX(-50%);
  background-color: rgba(0, 0, 0, 0.6);
  color: white;
  padding: 6px 12px;
  border-radius: 20px;
  font-size: 14px;
}

/* 响应式调整 */
@media (max-width: 768px) {
  .post-images {
    grid-template-columns: repeat(auto-fill, minmax(100px, 1fr));
  }
  
  .preview-nav-btn {
    width: 40px;
    height: 40px;
    font-size: 24px;
  }
  
  .preview-prev {
    left: 10px;
  }
  
  .preview-next {
    right: 10px;
  }
  
  .preview-close-btn {
    top: 10px;
    right: 10px;
  }
}
</style>

<script setup>

import { ref, computed, watch, onMounted, onUnmounted, nextTick } from 'vue';
import { useRouter } from 'vue-router';
import axios from 'axios';
import NavBar from '../components/NavBar.vue';
import api, { utils } from '../api';



const router = useRouter();

// 导航到登录页面
const navigateToLogin = () => {
  showToastMessage('请先登录', 'info');
  router.push('/login');
};

// 响应式状态
const postContent = ref('');
const showToast = ref(false);
const toastMessage = ref('');
const toastType = ref('success');
const mobileActiveNav = ref(0);
const isLoggedIn = ref(false);
const userName = ref('');
const userInitial = ref('');
const recommendedUsers = ref([]);
const hotTopics = ref([]);
const posts = ref([]); // 先定义posts变量
const comments = ref({}); // 用于存储每个帖子的评论数据
const selectedFilter = ref('推荐');
const selectedSort = ref('按时间排序');
const loading = ref(false);
const hasMore = ref(true);
const currentPage = ref(1);
const commentInputs = ref({});
const showComments = ref({});
const uploadingImages = ref([]);
const selectedImages = ref([]);
const showImagePreview = ref(false);
const previewImages = ref([]);
const searchQuery = ref('');
const isLoadingMore = ref(false);
const editingPostId = ref(null);
const editingContent = ref('');
const showPostMenu = ref(null);
const currentPreviewIndex = ref(0);
const allPreviewImages = ref([]);



// 不再使用mockPosts初始化，而是通过loadPosts函数从API获取数据
// 这样可以确保页面显示的是最新的真实数据


// 格式化帖子时间显示
const formatPostTime = (timeString) => {
  if (!timeString) return '';
  
  const date = new Date(timeString);
  if (isNaN(date.getTime())) return timeString;
  
  const now = new Date();
  const diffInSeconds = Math.floor((now - date) / 1000);
  
  // 小于1分钟
  if (diffInSeconds < 60) {
    return '刚刚';
  }
  // 小于1小时
  else if (diffInSeconds < 3600) {
    return `${Math.floor(diffInSeconds / 60)}分钟前`;
  }
  // 小于24小时
  else if (diffInSeconds < 86400) {
    return `${Math.floor(diffInSeconds / 3600)}小时前`;
  }
  // 小于7天
  else if (diffInSeconds < 604800) {
    return `${Math.floor(diffInSeconds / 86400)}天前`;
  }
  // 大于等于7天，显示具体日期
  else {
    const year = date.getFullYear();
    const month = String(date.getMonth() + 1).padStart(2, '0');
    const day = String(date.getDate()).padStart(2, '0');
    
    // 如果是今年，只显示月日
    if (year === now.getFullYear()) {
      return `${month}-${day}`;
    }
    // 不是今年，显示完整日期
    return `${year}-${month}-${day}`;
  }
};

// 修改后的计算属性，不再过滤帖子状态
const validPosts = computed(() => {
  // 确保posts.value是数组
  if (!Array.isArray(posts.value)) {
    return [];
  }
  
  // 只过滤掉无效的帖子对象，不再过滤状态
  const filtered = posts.value.filter(post => {
    return post && typeof post === 'object';
  });
  
  
  
  return filtered;
});

// 初始化用户信息
const initializeUser = () => {
  
  
  // 从多个可能的存储位置获取token
  const token = localStorage.getItem('authToken') || localStorage.getItem('token');
  const storedUser = localStorage.getItem('user');
  const storedUsername = localStorage.getItem('username');
  const tokenSource = localStorage.getItem('authToken') ? 'authToken' : (localStorage.getItem('token') ? 'token' : 'none');
  
  // 详细日志记录当前localStorage状态
  
  
  
  
  // 主要逻辑：如果有token，就认为用户已登录，即使token解析失败
  if (token && token.trim() !== '') {
    isLoggedIn.value = true;
    
    
    // 尝试按优先级设置用户名
    // 1. 首先尝试从storedUser解析
    if (storedUser) {
      try {
        const userData = JSON.parse(storedUser);
        userName.value = userData.name || userData.username || '用户';
        
      } catch (e) {
        console.error('解析user数据失败:', e);
        // 如果解析失败，回退到下一个方法
        userName.value = storedUsername || '用户';
        
      }
    } else {
      // 2. 如果没有storedUser，尝试使用storedUsername
      if (storedUsername) {
        userName.value = storedUsername;
        
      } else {
        // 3. 最后尝试从token解析
        try {
          parseTokenForUserInfo(token);
        } catch (e) {
          console.error('从token解析用户名失败:', e);
          userName.value = '用户';
        }
      }
    }
    
    // 设置用户头像首字母
    userInitial.value = userName.value.charAt(0).toUpperCase();
    
  } else {
    isLoggedIn.value = false;
    userName.value = '';
    userInitial.value = '';
    
  }
  
  
};

// 从token中解析用户信息的辅助函数
const parseTokenForUserInfo = (token) => {
  try {
    // 标准化token格式处理
    const cleanToken = token.replace(/^Bearer\s*/i, '');
    
    
    // 检查token是否符合JWT格式（三部分，用点分隔）
    const parts = cleanToken.split('.');
    if (parts.length !== 3) {
      console.warn('token不是标准JWT格式，尝试提取用户名');
      // 如果不是JWT格式，但我们已经将isLoggedIn设为true，只需要设置用户名
      userName.value = storedUsername || '用户';
      return;
    }
    
    const payload = parts[1];
    if (payload) {
      // 处理可能的padding问题
      const paddedPayload = payload.padEnd(payload.length + (4 - payload.length % 4) % 4, '=');
      const decodedPayload = JSON.parse(atob(paddedPayload));
      
      // 优先级: name > username > sub > email > storedUsername > 默认值
      const tokenUsername = decodedPayload.name || decodedPayload.username || decodedPayload.sub || decodedPayload.email || '';
      userName.value = tokenUsername || localStorage.getItem('username') || '用户';
      
    }
  } catch (error) {
    console.error('解析token失败，详细错误:', error.message);
    // 即使解析失败，也保持isLoggedIn为true（如果之前已设置）
    // 只需要确保userName有一个合理的值
    userName.value = localStorage.getItem('username') || '用户';
  }
};

// 加载推荐用户
const loadRecommendedUsers = async () => {
  try {
    const response = await api.community.getRecommendedUsers();
    const isSuccess = response.code === 200 || 
                      response.code === 0 || 
                      response.success === true || 
                      response.success === 'success' ||
                      response.success === 'true';
    if (isSuccess) {
      recommendedUsers.value = response.data || [];
    }
  } catch (error) {
    console.error('加载推荐用户失败:', error);
    // 保留原有模拟数据作为备用
  }
};

// 加载热门话题
const loadHotTopics = async () => {
  try {
    const response = await api.community.getHotTopics();
    const isSuccess = response.code === 200 || 
                      response.code === 0 || 
                      response.success === true || 
                      response.success === 'success' ||
                      response.success === 'true';
    if (isSuccess) {
      hotTopics.value = response.data || [];
    }
  } catch (error) {
    console.error('加载热门话题失败:', error);
    // 保留原有模拟数据作为备用
  }
};

// 创建临时的测试帖子数据生成函数
const createTestPost = (id, content, authorName) => ({
  id,
  content,
  author: {
    id: `user${id}`,
    name: authorName,
    avatar: '',
    isFollowing: false
  },
  meta: `测试数据 · 模拟帖子`,
  likes: Math.floor(Math.random() * 100),
  commentsCount: Math.floor(Math.random() * 20),
  isLiked: false,
  isSaved: false,
  tags: ['#测试', '#示例'],
  images: [],
  createdAt: new Date().toISOString()
});

// 创建初始的测试帖子列表
const initialTestPosts = [
  createTestPost(1, '这是第一条测试帖子内容，用于调试社区功能。', '测试用户1'),
  createTestPost(2, '这是第二条测试帖子，包含一些示例文本。', '测试用户2'),
  createTestPost(3, '社区功能正在开发中，这个帖子用于演示UI效果。', '测试用户3')
];

// 增强的测试帖子数据生成函数，确保包含所有必要字段
const createEnhancedTestPost = (id) => ({
  id: `test-${id}`,
  content: `这是测试帖子 #${id}，用于调试社区功能。后端返回数据但界面可能没有显示的问题。`,
  author: {
    id: `user-${id}`,
    name: `测试用户${id}`,
    username: `testuser${id}`,
    isFollowing: false
  },
  likes: Math.floor(Math.random() * 100),
  commentsCount: Math.floor(Math.random() * 20),
  isLiked: false,
  isSaved: false,
  tags: ['#测试', '#调试'],
  images: [],
  createdAt: new Date(Date.now() - id * 60000).toISOString(), // 每个帖子间隔1分钟
  meta: '测试数据'
});

// 创建增强的初始测试帖子列表
const enhancedTestPosts = Array.from({ length: 5 }, (_, i) => createEnhancedTestPost(i + 1));

const loadPosts = async (page = 1) => {
    
    // 正常加载帖子数据，优先使用API返回的数据
  if (loading.value || (page > 1 && !hasMore.value)) {
    
    return;
  }
  
  try {
    loading.value = page === 1;
    isLoadingMore.value = page > 1;
    
    
    
    
    
    // 准备请求参数，严格按照文档规范
    const params = {
      page
    };
    
    // 根据过滤条件映射到文档规范的filter参数
    if (selectedFilter.value === '推荐') {
      params.filter = 'latest';
    } else if (selectedFilter.value === '热门') {
      params.filter = 'popular';
    }
    
    // 根据排序条件映射到文档规范的sort参数
    if (selectedSort.value === '按热度排序') {
      params.sort = 'hot';
    } else if (selectedSort.value === '按时间排序') {
      params.sort = 'time';
    } else if (selectedSort.value === '按回复数排序') {
      params.sort = 'reply';
    }
    
    let data;
    try {
      // 使用axios调用后端接口，确保baseURL已设置为'/api'
      
      const response = await axios.get('/api/community/posts', { params });
      
      
      data = response.data;
      
      
      // 开发环境下，只有当API返回的数据为空时才添加测试数据
      if (import.meta.env.DEV) {
        
        // 确保data.posts是数组
        if (!data.posts || !Array.isArray(data.posts)) {
          data.posts = [];
        }
        // 只有当API返回的数据为空时，才添加测试数据
        if (data.posts.length === 0) {
          
          data.posts = enhancedTestPosts;
        }
      }
      
    } catch (fetchError) {
      console.error('API请求或响应解析失败:', fetchError);
      // 在API请求失败时，使用测试数据作为备用
      
      data = { posts: enhancedTestPosts, hasMore: false, total: enhancedTestPosts.length };
    }
    
    // 处理响应数据 - 使用API返回的数据
    let newPosts = [];
    
    // 详细日志记录API响应数据
    
    
    
    
    // 处理嵌套的数据结构 {code, message, data: {posts, hasMore, total}}
    // 同时支持code=0和code=200两种成功状态
    const responseData = data && (data.code === 0 || data.code === 200) && data.data ? data.data : data;
    
    
    
    
    
    // 优先使用API返回的数据，如果数据有效
    if (responseData && Array.isArray(responseData.posts)) {
      
      
      // 临时日志，帮助调试
      
      
      
      newPosts = responseData.posts;
      hasMore.value = responseData.hasMore !== false;
      
      // 开发环境下，只在API返回数据为空时才使用测试数据
      if (import.meta.env.DEV) {
        
        // 确保有内容显示
        if (newPosts.length === 0) {
          
          newPosts = enhancedTestPosts;
        }
      }
    } else {
      // 当API返回无效数据时，使用增强的测试数据作为备用
      
      newPosts = enhancedTestPosts;
      hasMore.value = false;
    }
    
    
    
    
    // 预处理帖子数据：解析JSON字符串字段
    newPosts = newPosts.map(post => {
      if (!post || typeof post !== 'object') return null;
      
      // 深拷贝避免修改原始数据
      const processedPost = { ...post };
      
      // 解析images字段（JSON字符串转数组）
      if (processedPost.images && typeof processedPost.images === 'string') {
        try {
          processedPost.images = JSON.parse(processedPost.images);
        } catch (e) {
          console.warn(`解析帖子${processedPost.id || '未知ID'}的images字段失败:`, e);
          processedPost.images = [];
        }
      } else if (!Array.isArray(processedPost.images)) {
        processedPost.images = [];
      }
      
      // 解析tags字段（JSON字符串转数组）
      if (processedPost.tags && typeof processedPost.tags === 'string') {
        try {
          processedPost.tags = JSON.parse(processedPost.tags);
        } catch (e) {
          console.warn(`解析帖子${processedPost.id || '未知ID'}的tags字段失败:`, e);
          processedPost.tags = [];
        }
      } else if (!Array.isArray(processedPost.tags)) {
        processedPost.tags = [];
      }
      
      // 确保author对象存在
      if (!processedPost.author && processedPost.username) {
        processedPost.author = {
          id: processedPost.userId,
          name: processedPost.username,
          username: processedPost.username
        };
      }
      
      return processedPost;
    }).filter(post => post !== null); // 过滤掉无效的帖子对象
    
    // 过滤有效的帖子对象并确保包含必要的用户信息和状态字段
    newPosts = newPosts.filter(post => {
      if (!post || typeof post !== 'object') return false;
      
      // 确保帖子有有效的用户信息
      if (!post.author && !post.username) {
        console.warn('帖子缺少用户信息:', post.id || '未知ID');
        return false;
      }
      
      // 根据要求，不再进行前端状态审核过滤，显示所有后端传来的帖子
      
      // 确保每个帖子都有必要的状态字段，默认为合理的值
      if (post.isLiked === undefined) {
        post.isLiked = false;
      }
      if (post.isSaved === undefined) {
        post.isSaved = false;
      }
      if (post.likes === undefined) {
        post.likes = 0;
      }
      if (post.commentsCount === undefined) {
        post.commentsCount = post.comments || 0;
      }
      
      // 确保createdAt字段存在
      if (!post.createdAt) {
        post.createdAt = new Date().toISOString();
      }
      
      // 确保用户信息完整
      if (!post.author) {
        post.author = {
          id: post.userId || 0,
          name: post.username || '未知用户',
          username: post.username || 'unknown_user'
        };
      }
      
      return true;
    });
    
    
    
    // 按发布时间降序排序，最新的帖子排在前面
    newPosts.sort((a, b) => {
      const dateA = new Date(a.createdAt || a.time || 0).getTime();
      const dateB = new Date(b.createdAt || b.time || 0).getTime();
      return dateB - dateA; // 降序排序
    });
    
    // 更新帖子列表
    if (page === 1) {
      posts.value = newPosts;
    } else {
      
      
      
      posts.value = [...posts.value, ...newPosts];
      
      
      
    }
    
    currentPage.value = page;
    
    
    
    
  } catch (error) {
    console.error('加载帖子失败 - 详细错误:', error);
    
    // 分析错误类型
    let errorMsg = '加载帖子失败，请稍后重试';
    
    if (error.message && error.message.includes('HTTP error')) {
      errorMsg = `网络错误: ${error.message}`;
    }
    
    // 显示错误消息
    showToastMessage(errorMsg, 'error');
    
    // 当API调用失败时，显示模拟数据，避免白屏
    if (page === 1) {
      
      posts.value = initialTestPosts;
    }
    
    // 开发环境下的详细调试信息
    if (import.meta.env.DEV) {
      console.warn('=== 开发环境API调试信息 ===');
      console.warn('代理配置: /api -> http://localhost:8080');
      console.warn('实际请求URL:', `/api/community/posts?page=${page}&${selectedSort.value === '按热度排序' ? 'sort=hot' : 'sort=time'}`);
      console.warn('错误类型:', error.message);
      console.warn('建议操作:');
      console.warn('1. 确保后端服务在 http://localhost:8080 正常运行');
      console.warn('2. 检查 /api/community/posts 接口是否存在且可访问');
      console.warn('3. 确认是否需要登录认证');
      console.warn('4. 检查网络连接是否正常');
    }
  } finally {
    loading.value = false;
    isLoadingMore.value = false;
    
  }
};

// 切换筛选
const changeFilter = (filter) => {
  if (selectedFilter.value === filter) return;
  selectedFilter.value = filter;
  currentPage.value = 1;
  hasMore.value = true;
  loadPosts(1);
};

// 切换排序
const changeSort = (event) => {
  selectedSort.value = event.target.value;
  loadPosts(1);
};

// 聚焦到发布输入框
const focusPostInput = () => {
  const postInput = document.querySelector('#post-content');
  if (postInput) {
    postInput.focus();
    // 滚动到发布框位置
    postInput.scrollIntoView({ behavior: 'smooth', block: 'start' });
  }
};

// 处理图片选择
const handleImageSelect = (event) => {
  const files = event.target.files;
  if (!files || files.length === 0) return;
  
  // 限制最多上传9张图片
  const remainingSlots = 9 - selectedImages.value.length;
  const filesToAdd = Math.min(files.length, remainingSlots);
  
  for (let i = 0; i < filesToAdd; i++) {
    const file = files[i];
    
    // 检查文件类型
    if (!file.type.startsWith('image/')) {
      showToastMessage('请选择有效的图片文件', 'error');
      continue;
    }
    
    // 检查文件大小（限制为5MB）
    if (file.size > 5 * 1024 * 1024) {
      showToastMessage('图片大小不能超过5MB', 'error');
      continue;
    }
    
    // 读取图片预览
    const reader = new FileReader();
    reader.onload = (e) => {
      const imageUrl = e.target.result;
      const imageData = {
        id: Date.now() + i,
        file,
        url: imageUrl,
        uploading: false
      };
      selectedImages.value.push(imageData);
      previewImages.value = selectedImages.value.map(img => img.url);
    };
    reader.readAsDataURL(file);
  }
  
  // 清空input，允许重复选择同一文件
  event.target.value = '';
};

// 移除图片
const removeImage = (imageId) => {
  selectedImages.value = selectedImages.value.filter(img => img.id !== imageId);
  previewImages.value = selectedImages.value.map(img => img.url);
};

// 发布帖子
const publishPost = async () => {
  // 使用统一的登录状态检查
  if (!utils.isAuthenticated()) {
    console.warn('检测到未登录状态，跳转到登录页');
    utils.showWarning('请先登录再发布帖子');
    navigateToLogin();
    return;
  }
  
  if (!postContent.value.trim() && selectedImages.value.length === 0) {
    utils.showError('请输入内容或选择图片');
    return;
  }
  
  // 将postData定义在try-catch外部，确保在catch块中也能访问
  let postData = null;
  
  try {
    // 上传图片
    const uploadedImageUrls = [];
    if (selectedImages.value.length > 0) {
      for (const image of selectedImages.value) {
        image.uploading = true;
        const formData = new FormData();
        formData.append('image', image.file);
        
        try {
          // 调用真实上传API，axios拦截器会自动添加token
          const response = await api.upload.image(formData);
          // 确保response存在且有正确的成功状态
          if (response && (response.code === 200 || response.success)) {
            uploadedImageUrls.push(response.data?.url || image.url);
          } else {
            throw new Error(response?.message || '图片上传失败');
          }
        } catch (uploadError) {
          // 记录上传失败的具体图片信息
          console.error(`图片上传失败: ${image.file?.name || '未知文件'}`, uploadError);
          // 向上抛出错误，中断整个发布流程
          throw new Error(`图片${image.file?.name || '上传'}失败: ${uploadError.message || '未知错误'}`);
        }
        
        image.uploading = false;
      }
    }
    
    // 构建帖子数据，严格按照文档规范
    postData = {
      content: postContent.value.trim(),
      images: uploadedImageUrls,
      tags: extractTags(postContent.value)
    };
    
    
    // 使用apiClient发送请求，确保通过正确的拦截器处理token
    const response = await api.community.publishPost(postData.content, postData.images, postData.tags);
    
    
    
    // 更健壮的成功判断逻辑
    const isSuccess = response.code === 200 || 
                      response.code === 0 || 
                      response.success === true || 
                      response.success === 'success' ||
                      response.success === 'true' ||
                      (response && typeof response === 'object' && response.id); // 有些API可能直接返回创建的对象
    
    if (isSuccess) {
      const createdPost = response.data || response;
      // 确保posts.value是数组
      if (!Array.isArray(posts.value)) {
        posts.value = [];
      }
      
      // 创建新帖子对象，确保包含所有必要字段
      const newPost = {
        ...createdPost,
        // 确保必要字段存在
        id: createdPost.id || `temp_${Date.now()}_${Math.random().toString(36).substr(2, 9)}`, // 确保有唯一ID
        content: createdPost.content || postContent.value.trim(),
        images: createdPost.images || uploadedImageUrls,
        likes: createdPost.likes || 0,
        commentsCount: createdPost.commentsCount || 0,
        isLiked: createdPost.isLiked || false,
        isSaved: createdPost.isSaved || false,
        meta: `刚刚 · 分享`, // 添加meta信息
        createdAt: new Date().toISOString(), // 添加创建时间
        author: {
          // 确保author对象完整
          id: createdPost.author?.id || 'current_user',
          name: createdPost.author?.name || userName.value || '当前用户',
          isFollowing: createdPost.author?.isFollowing || false
        },
        tags: extractTags(postContent.value) // 确保有tags字段
      };
      
      // 只有当帖子有内容或图片时才添加到列表
      if (newPost.content || (Array.isArray(newPost.images) && newPost.images.length > 0)) {
        posts.value.unshift(newPost);
        
      }
      
      utils.showSuccess('发布成功');
      
      // 清空输入
      postContent.value = '';
      selectedImages.value = [];
      previewImages.value = [];
    } else {
      // 使用统一的错误处理
      throw new Error(response?.message || '发布失败');
    }
  } catch (error) {
      // 使用统一的错误处理函数
      utils.handleApiError(error, {
        showMessage: true,
        navigateToLogin: () => {
          // 清除无效token
          utils.clearToken();
          initializeUser();
          setTimeout(() => {
            navigateToLogin();
          }, 500);
        }
      });
      
      // 开发环境下添加更多调试信息
      if (import.meta.env.DEV) {
        
        
        
      }
    }
};

// 提取标签
const extractTags = (content) => {
  const tagRegex = /#(\S+)/g;
  const matches = content.match(tagRegex);
  return matches || [];
};

// 点赞评论
const likeComment = async (commentId, postId) => {
  if (!isLoggedIn.value) {
    navigateToLogin();
    return;
  }
  
  // 找到对应的评论
  const comment = comments[postId]?.find(c => c.id === commentId);
  if (!comment) return;
  
  // 乐观更新UI
  const wasLiked = comment.isLiked || false;
  const oldLikesCount = comment.likes || 0;
  comment.isLiked = !wasLiked;
  comment.likes = wasLiked ? oldLikesCount - 1 : oldLikesCount + 1;
  
  try {
    // 调用真实评论点赞API
    const response = await api.community.likeComment(commentId);
    
    if (!(response.code === 200 || response.success)) {
      // API调用失败，回滚UI更新
      comment.isLiked = wasLiked;
      comment.likes = oldLikesCount;
      throw new Error(response.message || '操作失败');
    }
    
    showToastMessage(comment.isLiked ? '点赞成功' : '取消点赞', 'success');
  } catch (error) {
    console.error('评论点赞失败:', error);
    // 确保UI已回滚
    comment.isLiked = wasLiked;
    comment.likes = oldLikesCount;
    showToastMessage(error.message || '操作失败，请重试', 'error');
  }
};

// 点赞帖子
const likePost = async (postId) => {
  if (!isLoggedIn.value) {
    navigateToLogin();
    return;
  }
  
  const post = posts.value.find(p => p.id === postId);
  if (!post) return;
  
  // 乐观更新UI
  const wasLiked = post.isLiked;
  const oldLikesCount = post.likes;
  post.isLiked = !wasLiked;
  post.likes = wasLiked ? oldLikesCount - 1 : oldLikesCount + 1;
  
  try {
    // 使用axios直接调用后端接口，按照文档规范
    const response = await axios.post(`/api/community/posts/${postId}/like`, {}, {
      headers: {
        'Authorization': `Bearer ${localStorage.getItem('authToken') || localStorage.getItem('token')}`
      }
    });
    
    // 检查响应是否成功，考虑多种成功情况
    const isSuccess = 
      response.status === 200 ||
      response.data?.code === 200 || 
      response.data?.code === 0 || 
      response.data?.success === true || 
      response.data?.success === 'success' || 
      response.data?.success === 'true';
    
    if (isSuccess) {
      // 使用API返回的点赞状态和数量更新帖子
      if (response.data?.isLiked !== undefined) {
        post.isLiked = response.data.isLiked;
      }
      if (response.data?.likes !== undefined) {
        post.likes = response.data.likes;
      }
      
      showToastMessage(post.isLiked ? '点赞成功' : '取消点赞', 'success');
    } else {
      // API调用失败，回滚UI更新
      post.isLiked = wasLiked;
      post.likes = oldLikesCount;
      throw new Error(response.data?.message || '操作失败');
    }
  } catch (error) {
    console.error('点赞失败:', error);
    // 确保UI已回滚
    post.isLiked = wasLiked;
    post.likes = oldLikesCount;
    showToastMessage(error.message || '操作失败，请重试', 'error');
  }
};

// 切换评论显示
const toggleComments = async (postId) => {
  if (!isLoggedIn.value) {
    navigateToLogin();
    return;
  }
  
  showComments.value[postId] = !showComments.value[postId];
  
  if (showComments.value[postId]) {
    // 初始化评论输入框
    if (!commentInputs.value[postId]) {
      commentInputs.value[postId] = '';
    }
    
    // 从API获取评论数据
    try {
      const response = await api.community.getComments(postId);
      if (response.code === 200 || response.success) {
        comments.value[postId] = response.data || [];
      }
    } catch (error) {
      console.error('获取评论失败:', error);
      // 出错时清空评论数据，避免显示错误内容
      comments.value[postId] = [];
    }
    
    // 滚动到评论区域
    await nextTick();
    const commentSection = document.getElementById(`comments-section-${postId}`);
    if (commentSection) {
      commentSection.scrollIntoView({ behavior: 'smooth', block: 'center' });
    }
  }
};

// 发布评论
const postComment = async (postId) => {
  const content = commentInputs.value[postId]?.trim();
  if (!content) {
    showToastMessage('请输入评论内容', 'error');
    return;
  }
  
  try {
    // 调用真实评论API
    const response = await api.community.addComment(postId, { content });
    
    if (!(response.code === 200 || response.success)) {
      throw new Error(response.message || '评论发布失败');
    }
    
    // 获取返回的评论数据
    const newComment = response.data;
    
    // 添加到评论数据
    if (!comments[postId]) {
      comments[postId] = [];
    }
    comments[postId].push(newComment);
    
    // 更新帖子评论数
    const post = posts.value.find(p => p.id === postId);
    if (post) {
      post.commentsCount += 1;
    }
    
    // 清空输入并显示成功提示
    commentInputs.value[postId] = '';
    showToastMessage('评论成功', 'success');
  } catch (error) {
    console.error('评论失败:', error);
    showToastMessage(error.message || '评论失败，请重试', 'error');
  }
};

// 关注用户
const followUser = async (userId, postId) => {
  if (!isLoggedIn.value) {
    navigateToLogin();
    return;
  }
  
  try {
    let userToUpdate = null;
    let wasFollowing = false;
    
    // 查找要更新的用户信息
    if (postId) {
      // 从帖子中查找用户
      const post = posts.value.find(p => p.id === postId);
      if (post && post.author && post.author.id === userId) {
        userToUpdate = post.author;
        wasFollowing = post.author.isFollowing || false;
        post.author.isFollowing = !wasFollowing; // 乐观更新
      }
    } else {
      // 从推荐用户列表中查找用户
      const user = recommendedUsers.value.find(u => u.id === userId);
      if (user) {
        userToUpdate = user;
        wasFollowing = user.isFollowing || false;
        user.isFollowing = !wasFollowing; // 乐观更新
      }
    }
    
    if (!userToUpdate) {
      throw new Error('用户不存在');
    }
    
    // 调用真实关注API
    const response = await api.community.toggleFollow(userId);
    
    // 检查响应是否成功
    const isSuccess = 
      response.code === 200 || 
      response.code === 0 || 
      response.success === true || 
      response.success === 'success' || 
      response.success === 'true';
    
    if (!isSuccess) {
      // API调用失败，回滚UI更新
      userToUpdate.isFollowing = wasFollowing;
      throw new Error(response.message || '操作失败');
    }
    
    showToastMessage(
      userToUpdate.isFollowing ? '关注成功' : '取消关注', 
      'success'
    );
  } catch (error) {
    console.error('关注操作失败:', error);
    // 确保UI已回滚
    if (postId) {
      const post = posts.value.find(p => p.id === postId);
      if (post && post.author && post.author.id === userId) {
        post.author.isFollowing = wasFollowing; // 回滚到原始状态
      }
    } else {
      const user = recommendedUsers.value.find(u => u.id === userId);
      if (user) {
        // 回滚推荐用户列表中的状态
        const index = recommendedUsers.value.findIndex(u => u.id === userId);
        if (index > -1) {
          // 创建一个新数组以触发响应式更新
          const updatedUsers = [...recommendedUsers.value];
          updatedUsers[index] = {...updatedUsers[index], isFollowing: wasFollowing}; // 回滚到原始状态
          recommendedUsers.value = updatedUsers;
        }
      }
    }
    showToastMessage(error.message || '操作失败，请重试', 'error');
  }
};

// 分享帖子
const sharePost = async (postId) => {
  if (!isLoggedIn.value) {
    navigateToLogin();
    return;
  }
  
  try {
    // 构建分享链接
    const shareUrl = `${window.location.origin}/community/post/${postId}`;
    
    // 使用Web Share API（如果支持）
    if (navigator.share) {
      await navigator.share({
        title: '分享一个有趣的帖子',
        text: '来看看这个来自Angel社区的帖子',
        url: shareUrl
      });
    } else {
      // 降级方案：复制到剪贴板
      await navigator.clipboard.writeText(shareUrl);
      showToastMessage('链接已复制到剪贴板', 'success');
    }
  } catch (error) {
    console.error('分享失败:', error);
    showToastMessage('分享失败，请重试', 'error');
  }
};

// 收藏帖子
const savePost = async (postId) => {
  if (!isLoggedIn.value) {
    navigateToLogin();
    return;
  }
  
  const post = posts.value.find(p => p.id === postId);
  if (!post) return;
  
  // 乐观更新UI
  const wasSaved = post.isSaved;
  post.isSaved = !wasSaved;
  
  try {
    // 使用axios直接调用后端接口，按照文档规范
    const response = await axios.post(`/api/community/posts/${postId}/save`, {}, {
      headers: {
        'Authorization': `Bearer ${localStorage.getItem('authToken') || localStorage.getItem('token')}`
      }
    });
    
    // 检查响应是否成功，考虑多种成功情况
    const isSuccess = 
      response.status === 200 ||
      response.data?.code === 200 || 
      response.data?.code === 0 || 
      response.data?.success === true || 
      response.data?.success === 'success' || 
      response.data?.success === 'true';
    
    if (isSuccess) {
      // 使用API返回的收藏状态更新帖子
      if (response.data?.isSaved !== undefined) {
        post.isSaved = response.data.isSaved;
      }
      
      showToastMessage(post.isSaved ? '收藏成功' : '取消收藏', 'success');
    } else {
      // API调用失败，回滚UI更新
      post.isSaved = wasSaved;
      throw new Error(response.data?.message || '操作失败');
    }
  } catch (error) {
    console.error('收藏操作失败:', error);
    // 确保UI已回滚
    post.isSaved = wasSaved;
    showToastMessage(error.message || '操作失败，请重试', 'error');
  }
};

// 编辑帖子
const editPost = (postId) => {
  const post = posts.value.find(p => p.id === postId);
  if (!post) return;
  
  editingPostId.value = postId;
  editingContent.value = post.content;
  
  // 滚动到帖子位置
  nextTick(() => {
    const postElement = document.getElementById(`post-${postId}`);
    if (postElement) {
      postElement.scrollIntoView({ behavior: 'smooth' });
    }
  });
};

// 保存编辑
const saveEdit = async (postId) => {
  if (!editingContent.value.trim()) {
    showToastMessage('内容不能为空', 'error');
    return;
  }
  
  try {
    // 构建编辑数据，严格按照文档规范
    const editData = {
      content: editingContent.value.trim(),
      tags: extractTags(editingContent.value)
    };
    
    
    
    // 使用axios直接调用后端接口，按照文档规范
    const response = await axios.put(`/api/community/posts/${postId}`, editData, {
      headers: {
        'Authorization': `Bearer ${localStorage.getItem('authToken') || localStorage.getItem('token')}`,
        'Content-Type': 'application/json'
      }
    });
    
    // 检查响应是否成功
    const isSuccess = response.status === 200 || response.data?.code === 200 || response.data?.success;
    
    if (isSuccess) {
      // 更新本地状态
      const post = posts.value.find(p => p.id === postId);
      if (post) {
        post.content = editingContent.value;
        post.tags = extractTags(editingContent.value);
      }
      
      showToastMessage('编辑成功', 'success');
      cancelEdit();
    } else {
      throw new Error(response.data?.message || '编辑失败');
    }
  } catch (error) {
    console.error('编辑失败:', error);
    showToastMessage(error.message || '编辑失败，请重试', 'error');
  }
};

// 取消编辑
const cancelEdit = () => {
  editingPostId.value = null;
  editingContent.value = '';
};

// 预览图片
const previewImage = (image, index, allImages) => {
  currentPreviewIndex.value = index;
  allPreviewImages.value = allImages;
  showImagePreview.value = true;
  
  // 阻止背景滚动
  document.body.style.overflow = 'hidden';
};

// 关闭图片预览
const closeImagePreview = () => {
  showImagePreview.value = false;
  document.body.style.overflow = 'auto';
};

// 切换预览图片
const changePreviewImage = (direction) => {
  if (direction === 'next' && currentPreviewIndex.value < allPreviewImages.value.length - 1) {
    currentPreviewIndex.value++;
  } else if (direction === 'prev' && currentPreviewIndex.value > 0) {
    currentPreviewIndex.value--;
  }
};

// 删除帖子
const deletePost = async (postId) => {
  if (!confirm('确定要删除这条帖子吗？')) {
    return;
  }
  
  try {
    // 使用axios直接调用后端接口，按照文档规范
    const response = await axios.delete(`/api/community/posts/${postId}`, {
      headers: {
        'Authorization': `Bearer ${localStorage.getItem('authToken') || localStorage.getItem('token')}`
      }
    });
    
    // 检查响应是否成功
    const isSuccess = response.status === 200 || response.data?.code === 200 || response.data?.success;
    
    if (isSuccess) {
      // 更新本地状态
      posts.value = posts.value.filter(p => p.id !== postId);
      showToastMessage('删除成功', 'success');
    } else {
      throw new Error(response.data?.message || '删除失败');
    }
  } catch (error) {
    console.error('删除失败:', error);
    showToastMessage(error.message || '删除失败，请重试', 'error');
  }
};

// 加载更多帖子
const loadMorePosts = () => {
  if (hasMore.value && !isLoadingMore.value) {
    loadPosts(currentPage.value + 1);
  }
};

// 刷新帖子列表
const refreshPosts = () => {
  
  // 重置分页状态
  currentPage.value = 1;
  hasMore.value = true;
  // 重新加载第一页
  loadPosts(1);
};

// 初始化
const initPage = async () => {
  
  // 先初始化用户信息
  initializeUser();
  
  // 立即加载帖子，不再延迟，确保内容快速显示
  
  try {
    await loadPosts(1); // 明确传递page=1参数
    
    
    // 只有当成功加载到帖子时，才显示加载成功的提示（可选）
    if (posts.value.length > 0) {
      
    }
  } catch (error) {
    console.error('初始化加载帖子失败:', error);
    showToastMessage('加载社区内容时遇到问题，请稍后刷新重试', 'error');
  }
  
  // 即使帖子加载失败，也尝试加载其他内容，保证页面其他部分正常显示
  try {
    await Promise.all([loadRecommendedUsers(), loadHotTopics()]);
  } catch (error) {
    console.error('加载推荐用户或热门话题失败:', error);
  }
  
  
};

// 定义unwatch变量以便在onUnmounted中使用
let unwatch;

// 组件挂载时初始化
onMounted(async () => {
  await initPage();
  
  // 添加路由监听器，当路由切换到社区页面时重新初始化用户状态
  // 这解决了账号切换后登录状态不更新的问题
  unwatch = router.afterEach((to) => {
    if (to.path.includes('/community')) {
      
      initializeUser();
    }
  });
});

// 组件卸载时清理监听器（正确放置在顶层）
onUnmounted(() => {
  
  if (unwatch) {
    unwatch();
  }
});
  
  // 移动端导航项
  const mobileNavItems = [
    { icon: '🔥', text: '热门' },
    { icon: '💖', text: '情绪' },
    { icon: '💡', text: '分享' },
    { icon: '📚', text: '资源' },
    { icon: '🏠', text: '活动' }
  ];

// 计算属性
const toastIcon = computed(() => {
  switch (toastType.value) {
    case 'success': return '✓';
    case 'error': return '!';
    case 'info': return 'i';
    default: return '✓';
  }
});

// 监听toast状态变化，自动移除
watch(showToast, (newVal) => {
  if (newVal) {
    setTimeout(() => {
      showToast.value = false;
    }, 3000);
  }
});

// 导航方法已在前面定义

const showToastMessage = (message, type = 'success') => {
  toastMessage.value = message;
  toastType.value = type;
  showToast.value = true;
};
</script>

<style scoped>
    /* 导入字体 */
    @import url('https://fonts.googleapis.com/css2?family=Inter:wght@300;400;500;600;700&display=swap');
    
    :root {
      --primary-color: #6a5acd;
      --secondary-color: #9370db;
      --text-color: #333;
      --text-light: #666;
      --background-color: #f5f5f5;
      --card-background: #ffffff;
      --border-color: #eaeaea;
      --shadow-color: rgba(0, 0, 0, 0.08);
      --success-color: #4caf50;
      --warning-color: #ff9800;
      --error-color: #f44336;
      --border-radius: 12px;
      --transition: all 0.3s ease;
    }

.community-page {
  font-family: 'Inter', sans-serif;
  color: var(--text-color);
  background-color: var(--background-color);
  min-height: 100vh;
}

/* 社区布局 */
.community-layout {
  display: grid;
  grid-template-columns: 1fr 3fr 1fr;
  gap: 24px;
  max-width: 1400px;
  margin: 0 auto;
  padding: 20px 0;
}

/* 侧边栏样式 */
.sidebar {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.sidebar-left {
  position: sticky;
  top: 20px;
  height: fit-content;
}

.sidebar-right {
  position: sticky;
  top: 20px;
  height: fit-content;
}

/* 中间内容区域 */
.content-center {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

/* 容器样式 */
.container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 20px;
}

/* 卡片样式 */
.card {
  background-color: var(--card-background);
  border-radius: var(--border-radius);
  box-shadow: 0 4px 12px var(--shadow-color);
  padding: 20px;
  transition: var(--transition);
}

.card:hover {
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.12);
  transform: translateY(-2px);
}

/* 卡片标题 */
.card-title {
  font-size: 18px;
  font-weight: 600;
  margin-bottom: 16px;
  color: var(--primary-color);
  border-bottom: 2px solid var(--primary-color);
  padding-bottom: 8px;
}

/* 社区导航 */
.community-nav {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.nav-item {
  display: flex;
  align-items: center;
  padding: 12px 16px;
  border-radius: 8px;
  color: var(--text-color);
  text-decoration: none;
  transition: var(--transition);
  gap: 12px;
}

.nav-item:hover {
  background-color: rgba(106, 90, 205, 0.1);
  color: var(--primary-color);
}

.nav-item.active {
  background-color: var(--primary-color);
  color: white;
}

.nav-icon {
  font-size: 20px;
  width: 24px;
  text-align: center;
}

.nav-text {
  font-size: 15px;
  font-weight: 500;
}

/* 用户状态提示 */
.login-prompt {
  padding: 16px;
  margin-top: 20px;
  border-top: 1px solid var(--border-color);
}

.login-info {
  text-align: center;
}

.login-status {
  font-size: 14px;
  font-weight: 500;
  color: var(--primary-color, #673ab7);
  margin-bottom: 8px;
}

/* 认证按钮 */
.auth-buttons {
  display: flex;
  gap: 8px;
  margin-top: 8px;
}

.auth-btn {
  padding: 6px 12px;
  border: none;
  border-radius: 6px;
  font-size: 13px;
  cursor: pointer;
  transition: var(--transition);
}

.login-btn {
  background-color: transparent;
  color: var(--primary-color);
  border: 1px solid var(--primary-color);
}

.login-btn:hover {
  background-color: rgba(106, 90, 205, 0.1);
}

.register-btn {
  background-color: var(--primary-color);
  color: white;
}

.register-btn:hover {
  background-color: var(--secondary-color);
}

/* 筛选排序栏 */
.filter-sort-bar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  flex-wrap: wrap;
  gap: 16px;
}

.filter-tabs {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
}

.filter-tab {
  padding: 8px 16px;
  border: 1px solid var(--border-color);
  border-radius: 20px;
  background-color: transparent;
  color: var(--text-color);
  cursor: pointer;
  transition: var(--transition);
  font-size: 14px;
}

.filter-tab:hover {
  border-color: var(--primary-color);
  color: var(--primary-color);
}

.filter-tab.active {
  background-color: var(--primary-color);
  color: white;
  border-color: var(--primary-color);
}

.sort-select {
  padding: 8px 12px;
  border: 1px solid var(--border-color);
  border-radius: 6px;
  background-color: white;
  color: var(--text-color);
  cursor: pointer;
  font-size: 14px;
}

/* 发布框 */
.post-create-card {
  padding: 20px;
  margin-bottom: 20px;
}

.post-input-container {
  margin: 16px 0;
}

.post-input {
  width: 100%;
  border: 1px solid var(--border-color);
  border-radius: 8px;
  padding: 12px;
  font-size: 15px;
  resize: vertical;
  min-height: 100px;
  transition: var(--transition);
}

.post-input:focus {
  outline: none;
  border-color: var(--primary-color);
  box-shadow: 0 0 0 3px rgba(106, 90, 205, 0.1);
}

.post-actions {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.post-tools {
  display: flex;
  gap: 12px;
}

.tool-btn {
  background: none;
  border: none;
  font-size: 20px;
  cursor: pointer;
  padding: 8px;
  border-radius: 50%;
  transition: var(--transition);
  color: var(--text-light);
}

.tool-btn:hover {
  background-color: rgba(0, 0, 0, 0.05);
  color: var(--primary-color);
}

.publish-btn {
  background-color: var(--primary-color);
  color: white;
  border: none;
  padding: 10px 24px;
  border-radius: 24px;
  font-size: 16px;
  font-weight: 600;
  cursor: pointer;
  transition: var(--transition);
  text-shadow: 0 1px 2px rgba(0, 0, 0, 0.2);
  box-shadow: 0 2px 8px rgba(106, 90, 205, 0.3);
}

.publish-btn:hover:not(:disabled) {
  background-color: var(--secondary-color);
  transform: translateY(-1px);
}

.publish-btn:disabled {
  background-color: #ccc;
  color: #333;
  cursor: not-allowed;
  opacity: 0.9; /* 增加不透明度提高可见度 */
  text-shadow: 0 1px 1px rgba(255, 255, 255, 0.3); /* 白色文字阴影增强对比度 */
}

/* 确保启用状态下按钮更加突出和可见 */
.publish-btn:not(:disabled) {
  opacity: 1;
  box-shadow: 0 2px 8px rgba(106, 90, 205, 0.3);
}

/* 帖子列表 */
.posts-container {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.post-card {
  background-color: var(--card-background);
  border-radius: var(--border-radius);
  box-shadow: 0 4px 12px var(--shadow-color);
  padding: 20px;
  transition: var(--transition);
  margin-bottom: 20px;
}

.post-card:hover {
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.12);
  transform: translateY(-2px);
}

.post-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 12px;
}

.user-avatar {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  background: linear-gradient(45deg, #9c27b0, #673ab7);
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  font-weight: 600;
  font-size: 16px;
}

.user-details {
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.user-name {
  font-size: 15px;
  font-weight: 600;
  margin: 0;
  color: var(--primary-color);
}

.post-meta {
  font-size: 13px;
  color: var(--text-light);
  margin: 0;
}

.more-btn {
  background: none;
  border: none;
  cursor: pointer;
  padding: 4px;
  border-radius: 4px;
  transition: var(--transition);
  color: var(--text-light);
}

.more-btn:hover {
  background-color: rgba(106, 90, 205, 0.1);
  color: var(--primary-color);
}

.post-content {
  margin-bottom: 16px;
  padding: 12px;
  background-color: rgba(255, 255, 255, 0.5);
  border-radius: 8px;
}

.post-text {
  font-size: 15px;
  line-height: 1.6;
  margin: 0;
  color: var(--text-color);
  padding: 8px 0;
}

.post-engagement {
  display: flex;
  gap: 24px;
  padding-top: 16px;
  border-top: 1px solid var(--border-color);
  justify-content: space-between;
}

.engagement-btn {
  display: flex;
  align-items: center;
  gap: 6px;
  background: none;
  border: none;
  font-size: 14px;
  color: var(--text-light);
  cursor: pointer;
  padding: 6px 0;
  transition: var(--transition);
}

.engagement-btn:hover {
  color: var(--primary-color);
}

.engagement-icon {
  font-size: 18px;
}

.engagement-count {
  font-weight: 500;
}

/* 加载更多 */
.load-more-container {
  display: flex;
  justify-content: center;
  margin: 32px 0;
}

.load-more-btn {
  background-color: transparent;
  color: var(--primary-color);
  border: 1px solid var(--primary-color);
  padding: 12px 24px;
  border-radius: 24px;
  font-size: 15px;
  cursor: pointer;
  transition: all 0.3s ease;
  font-weight: 500;
}

.load-more-btn:hover {
  background-color: var(--primary-color);
  color: white;
  text-shadow: 0 1px 2px rgba(0,0,0,0.2);
  font-weight: 600;
}

.load-more-btn:focus {
  outline: none;
  box-shadow: 0 0 0 3px rgba(106, 90, 205, 0.3);
  transform: translateY(-2px);
}

.load-more-btn:active {
  transform: translateY(1px);
  box-shadow: 0 2px 8px rgba(106, 90, 205, 0.2);
}

/* 话题列表 */
.topics-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
  margin-bottom: 16px;
}

.topic-item {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  padding: 12px;
  border-radius: 8px;
  text-decoration: none;
  color: var(--text-color);
  transition: var(--transition);
  background-color: rgba(0, 0, 0, 0.02);
}

.topic-item:hover {
  background-color: rgba(106, 90, 205, 0.1);
  color: var(--primary-color);
}

.topic-name {
  font-size: 14px;
  font-weight: 500;
  margin: 0;
  flex: 1;
}

.topic-meta {
  display: flex;
  align-items: center;
  gap: 8px;
}

.topic-participants {
  font-size: 12px;
  color: var(--text-light);
}

.topic-badge {
  background-color: #ff5252;
  color: white;
  font-size: 10px;
  padding: 2px 6px;
  border-radius: 10px;
  font-weight: 500;
}

.view-all-btn {
  width: 100%;
  padding: 8px;
  border: 1px solid #3730a3;
  border-radius: 6px;
  background-color: transparent;
  color: #3730a3;
  font-size: 14px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.3s ease;
}

.view-all-btn:hover {
  background-color: #3730a3;
  color: white;
  transform: translateY(-1px);
  box-shadow: 0 2px 8px rgba(55, 48, 163, 0.2);
}

/* 推荐用户 */
.recommended-users {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.recommended-user {
  display: flex;
  align-items: center;
  gap: 12px;
}

.recommended-user .user-avatar {
  width: 48px;
  height: 48px;
}

.user-bio {
  font-size: 13px;
  color: var(--text-light);
  margin: 0;
}

.follow-btn {
  background-color: transparent;
  color: #3730a3;
  border: 1px solid #3730a3;
  padding: 6px 12px;
  border-radius: 16px;
  font-size: 13px;
  cursor: pointer;
  transition: var(--transition);
  margin-left: auto;
  font-weight: 500;
}

.follow-btn:hover {
  background-color: #3730a3;
  color: white;
}

/* 社区公告 */
.announcement-card {
  background: linear-gradient(135deg, #6a5acd, #9370db);
  color: white;
}

.announcement-card .card-title {
  border-bottom-color: rgba(255, 255, 255, 0.3);
  color: white;
}

.announcement-content p {
  font-size: 14px;
  line-height: 1.6;
  margin-bottom: 12px;
}

.announcement-link {
  background-color: rgba(255, 255, 255, 0.2);
  color: white;
  border: 1px solid rgba(255, 255, 255, 0.3);
  padding: 8px 16px;
  border-radius: 16px;
  font-size: 13px;
  cursor: pointer;
  transition: var(--transition);
}

.announcement-link:hover {
  background-color: rgba(255, 255, 255, 0.3);
}

/* Toast提示 */
.toast-container {
  position: fixed;
  top: 20px;
  right: 20px;
  z-index: 1000;
  max-width: 300px;
}

.toast {
  background-color: white;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.15);
  border-radius: 8px;
  padding: 16px;
  margin-bottom: 12px;
  display: flex;
  align-items: center;
  gap: 12px;
  animation: slideIn 0.3s ease;
}

.toast-success {
  border-left: 4px solid var(--success-color);
}

.toast-error {
  border-left: 4px solid var(--error-color);
}

.toast-warning {
  border-left: 4px solid var(--warning-color);
}

.toast-info {
  border-left: 4px solid var(--primary-color);
}

.toast-icon {
  font-size: 20px;
  width: 24px;
  text-align: center;
}

.toast-message {
  font-size: 14px;
  line-height: 1.4;
}

@keyframes slideIn {
  from {
    transform: translateX(100%);
    opacity: 0;
  }
  to {
    transform: translateX(0);
    opacity: 1;
  }
}

/* 页脚美化 */
.footer {
  background: linear-gradient(135deg, #3a1c71, #d76d77, #ffaf7b);
  color: white;
  padding: 60px 0 40px;
  position: relative;
  overflow: hidden;
  margin-top: 60px;
}

.footer::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background-image: 
    radial-gradient(circle at 25% 25%, rgba(255,255,255,0.1) 0%, transparent 50%),
    radial-gradient(circle at 75% 75%, rgba(255,255,255,0.1) 0%, transparent 50%);
  z-index: 0;
}

.footer-content {
  display: flex;
  flex-wrap: wrap;
  justify-content: space-between;
  gap: 60px;
  margin-bottom: 50px;
  position: relative;
  z-index: 1;
  max-width: 1200px;
  margin-left: auto;
  margin-right: auto;
  padding: 0 20px;
}

.footer-info {
  flex: 1;
  min-width: 280px;
}

.logo-container {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 16px;
}

.logo {
  font-size: 2rem;
  font-weight: 700;
  color: white;
  margin: 0;
  text-shadow: 0 2px 10px rgba(0,0,0,0.2);
}

.logo-icon {
  font-size: 2.5rem;
  animation: float 3s ease-in-out infinite;
}

.slogan {
  font-size: 1.1rem;
  margin-bottom: 16px;
  font-weight: 500;
  opacity: 0.95;
}

.footer-description {
  opacity: 0.8;
  line-height: 1.6;
  max-width: 400px;
}

.footer-links {
  display: flex;
  gap: 60px;
  flex-wrap: wrap;
}

.link-group {
  min-width: 180px;
}

.link-title {
  font-size: 1.1rem;
  font-weight: 600;
  margin-bottom: 20px;
  color: white;
  position: relative;
  padding-bottom: 8px;
}

.link-title::after {
  content: '';
  position: absolute;
  bottom: 0;
  left: 0;
  width: 40px;
  height: 3px;
  background: linear-gradient(90deg, #fff, transparent);
  border-radius: 3px;
}

.link-group ul {
  list-style: none;
  padding: 0;
  margin: 0;
}

.link-group li {
  margin-bottom: 12px;
}

.footer-link {
  color: white;
  text-decoration: none;
  opacity: 0.8;
  transition: all 0.3s ease;
  display: inline-block;
  position: relative;
  padding-left: 5px;
}

.footer-link::before {
  content: '→';
  position: absolute;
  left: -15px;
  opacity: 0;
  transition: all 0.3s ease;
}

.footer-link:hover {
  opacity: 1;
  transform: translateX(5px);
}

.footer-link:hover::before {
  opacity: 1;
  left: -10px;
}

.social-icons {
  display: flex;
  gap: 12px;
  margin: 20px 0;
}

.social-icon {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 40px;
  height: 40px;
  border-radius: 50%;
  background-color: rgba(255,255,255,0.1);
  color: white;
  text-decoration: none;
  font-size: 1.2rem;
  transition: all 0.3s ease;
  backdrop-filter: blur(10px);
  border: 1px solid rgba(255,255,255,0.2);
}

.social-icon:hover {
  transform: translateY(-3px) scale(1.05);
  background-color: rgba(255,255,255,0.2);
  box-shadow: 0 5px 15px rgba(0,0,0,0.1);
}

.contact-email {
  opacity: 0.8;
  font-size: 0.95rem;
  margin-top: 10px;
  transition: opacity 0.3s ease;
}

.contact-email:hover {
  opacity: 1;
  cursor: pointer;
}

.footer-bottom {
  text-align: center;
  padding-top: 30px;
  position: relative;
  z-index: 1;
}

.divider {
  height: 1px;
  background: linear-gradient(90deg, transparent, rgba(255,255,255,0.3), transparent);
  margin-bottom: 25px;
}

.copyright {
  margin: 0 0 10px 0;
  font-size: 0.9rem;
  opacity: 0.8;
}

.footer-note {
  font-size: 0.8rem;
  opacity: 0.7;
}

/* 动画效果 */
@keyframes float {
  0%, 100% { transform: translateY(0); }
  50% { transform: translateY(-8px); }
}

/* 响应式优化 */
@media (max-width: 768px) {
  .footer-content {
    flex-direction: column;
    align-items: center;
    text-align: center;
    gap: 40px;
  }
  
  .footer-info {
    max-width: 100%;
  }
  
  .footer-links {
    gap: 30px;
  }
  
  .link-group {
    text-align: center;
  }
  
  .link-title::after {
    left: 50%;
    transform: translateX(-50%);
  }
  
  .footer-link::before {
    display: none;
  }
  
  .footer-link:hover {
    transform: translateX(0);
  }
}

/* 响应式设计 */
@media (max-width: 1200px) {
  .community-layout {
    grid-template-columns: 1fr 2.5fr 1fr;
  }
}

@media (max-width: 992px) {
  .community-layout {
    grid-template-columns: 1fr 2fr;
  }
  
  .sidebar-right {
    display: none;
  }
}

@media (max-width: 768px) {
  .community-layout {
    grid-template-columns: 1fr;
  }
  
  .sidebar-left {
    display: none;
  }
  
  .filter-sort-bar {
    flex-direction: column;
    align-items: stretch;
    gap: 12px;
  }
  
  .filter-tabs {
    justify-content: center;
  }
  
  .post-engagement {
    justify-content: space-around;
    gap: 10px;
  }
  
  .engagement-btn {
    flex-direction: column;
    gap: 2px;
  }
  
  .engagement-text {
    font-size: 12px;
  }
}

@media (max-width: 480px) {
  .card {
    padding: 16px;
  }
  
  .post-create-card .user-avatar {
    width: 32px;
    height: 32px;
  }
  
  .post-actions {
    flex-direction: column;
    align-items: stretch;
    gap: 12px;
  }
  
  .post-tools {
    justify-content: center;
  }
  
  .publish-btn {
    width: 100%;
  }
}
</style>
