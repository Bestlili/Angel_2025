<template>
  <div class="treehole-wrapper">
    <NavBar />
    <div class="treehole-container">
      <div class="chat-header">
        <h1>心灵树洞</h1>
        <div class="session-actions">
          <button @click="createNewSession" class="new-session-btn" :disabled="loading">
            📝 新对话
          </button>
          <button @click="showSessionList = !showSessionList" class="sessions-btn">
            📋 对话列表
          </button>
        </div>
      </div>
      
      <!-- 会话列表侧边栏 -->
      <div v-if="showSessionList" class="session-list-sidebar">
        <div class="sidebar-header">
          <h3>我的对话</h3>
          <button @click="showSessionList = false" class="close-btn">✕</button>
        </div>
        <div class="sessions-container">
          <div 
            v-for="session in sessions" 
            :key="session.sessionId"
            class="session-item"
            :class="{ active: session.sessionId === currentSessionId }"
            @click="switchSession(session.sessionId)"
            :data-session-id="session.sessionId"
          >
            <div class="session-info">
              <div class="session-title">{{ session.title || '未命名对话' }}</div>
              <div class="session-time">{{ formatDate(session.createdAt || session.lastMessageAt) }}</div>
              <div v-if="session.messages && session.messages.length > 0" class="session-preview">
                {{ getLastMessagePreview(session) }}
              </div>
            </div>
            <button 
              v-if="isAuthenticated"
              @click.stop="deleteSession(session.sessionId)" 
              class="delete-session-btn"
              title="删除会话"
            >
              🗑️
            </button>
          </div>
          <div v-if="sessions.length === 0" class="empty-sessions">
            <p>暂无对话记录</p>
            <button @click="createNewSession" class="start-btn">开始新对话</button>
          </div>
        </div>
      </div>
      
      <!-- 聊天内容区域 -->
      <div class="chat-content" @scroll="handleScroll">
        <div v-if="messages.length === 0 && !loading" class="empty-chat">
          <p>开始你的心灵之旅吧...</p>
          <p class="hint">请放心倾诉，这里是您的私密空间</p>
          <button @click="createNewSession" class="start-btn">开始对话</button>
        </div>
        <div 
          v-for="(message, index) in messages" 
          :key="message.messageId || index" 
          class="message" 
          :class="[
            message.role || message.sender,
            { 'offline-message': message.offline },
            { 'error-message': message.error }
          ]"
        >
          <div v-if="(message.role || message.sender) === 'assistant' || (message.role || message.sender) === 'bot'" class="bot-avatar">🤖</div>
          <div class="message-content">
            <div class="message-text" v-if="(message.role || message.sender) !== 'assistant' && (message.role || message.sender) !== 'bot' || !(message.content || message.text).includes('###')">{{ message.content || message.text }}</div>
            <div class="message-text markdown-content" v-else v-html="renderMarkdown(message.content || message.text)"></div>
          </div>
          <div v-if="(message.role || message.sender) === 'user'" class="user-avatar">👤</div>
          <span v-if="message.offline" class="offline-badge">离线</span>
          <span v-if="message.error" class="error-badge">错误</span>
        </div>
        <div v-if="loading" class="loading-indicator">
          <div class="loading-dots"></div>
          <span>正在思考...</span>
        </div>
      </div>
      
      <!-- 快速回复区域已删除 -->
      
      <!-- 输入区域 -->
      <!-- 未登录提示条 -->
      <div v-if="!isAuthenticated" class="auth-prompt">
        <span>您尚未登录，当前使用离线模式</span>
        <button @click="showLoginPrompt" class="login-btn">登录</button>
      </div>
      
      <div class="input-area">
        <textarea 
          v-model="inputText" 
          @keyup.enter.ctrl="sendMessage"
          @keyup.enter.meta="sendMessage"
          placeholder="输入消息...（Ctrl/Cmd + Enter 发送）" 
          class="input-field" 
          :disabled="loading"
          rows="3"
          maxlength="1000"
        ></textarea>
        <div class="message-counter" :class="{ warning: inputText.length > 900 }">
          {{ inputText.length }}/1000
        </div>
        <button @click="sendMessage" class="send-button" :disabled="loading || !inputText.trim()">
          发送
        </button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, nextTick, watch } from 'vue'
import NavBar from '../components/NavBar.vue'
import { useTreeholeStore } from '../store/treeholeStore'
import { utils } from '../api'
import { marked } from 'marked'

// 获取store实例
const treeholeStore = useTreeholeStore()

// 响应式数据
const inputText = ref('')
const showSessionList = ref(false)
const hasScrolled = ref(false) // 用于跟踪是否手动滚动过
const lastScrollTop = ref(0) // 记录上次滚动位置

// 计算属性
const messages = computed(() => {
  const allMessages = treeholeStore.state.messages || [];
  const currentId = treeholeStore.state.currentSessionId;
  
  // 确保消息数组存在且只返回当前会话的消息
  if (!Array.isArray(allMessages) || !currentId) {
    return [];
  }
  
  // 过滤出当前会话的消息并按时间戳排序，确保历史记录正确显示
  return allMessages
    .filter(msg => msg.sessionId === currentId)
    .sort((a, b) => {
      const timeA = a.timestamp instanceof Date ? a.timestamp.getTime() : new Date(a.timestamp).getTime();
      const timeB = b.timestamp instanceof Date ? b.timestamp.getTime() : new Date(b.timestamp).getTime();
      return timeA - timeB; // 按时间升序排列，确保历史记录按时间顺序显示
    });
})
// 快速回复计算属性已删除
const sessions = computed(() => treeholeStore.state.sessions || [])
const currentSessionId = computed(() => treeholeStore.state.currentSessionId)
const loading = computed(() => treeholeStore.state.loading)
const isOnline = computed(() => treeholeStore.state.isOnline)
const config = computed(() => treeholeStore.state.config || {})
const isAuthenticated = computed(() => utils.isAuthenticated())

// 发送消息
const sendMessage = async () => {
  if (!inputText.value.trim() || loading.value) return
  
  const message = inputText.value.trim()
  inputText.value = ''
  
  // 快速回复相关代码已删除
  
  // 重置滚动跟踪状态，确保下次有新消息时会自动滚动
  hasScrolled.value = false
  
  try {
    // 检查消息长度限制
    const maxLength = config.value.maxMessageLength || 1000
    if (message.length > maxLength) {
      utils.showWarning(`消息长度不能超过${maxLength}个字符`)
      return
    }
    
    // 发送消息
    await treeholeStore.sendMessage(message)
    
    // 发送成功后滚动到底部
    await nextTick()
    scrollToBottom()
  } catch (error) {
    console.error('发送消息失败:', error)
    utils.showError('发送消息失败，请稍后重试')
  }
}

// 快速回复处理函数已删除

// 创建新会话
const createNewSession = async () => {
  try {
    // 重置状态
    hasScrolled.value = false
    
    const result = await treeholeStore.createSession()
    showSessionList.value = false
    
    // 检查是否是离线模式创建的临时会话
    if (result?.offline || (treeholeStore.state.currentSessionId && String(treeholeStore.state.currentSessionId).startsWith('temp_'))) {
      utils.showWarning('已创建离线会话')
    } else {
      utils.showSuccess('新会话创建成功')
    }
    
    // 创建成功后滚动到底部
    await nextTick()
    scrollToBottom()
    return result
  } catch (error) {
    console.error('创建新会话失败:', error)
    utils.showError('创建新会话失败，请稍后重试')
  }
}

// 切换会话
const switchSession = async (sessionId) => {
  if (loading.value) return
  
  try {
    // 重置状态
    hasScrolled.value = false
    
    await treeholeStore.getSessionHistory(sessionId)
    showSessionList.value = false
    
    // 切换成功后滚动到底部
    await nextTick()
    scrollToBottom()
    
    return true
  } catch (error) {
    console.error('切换会话失败:', error)
    utils.showError('切换会话失败，请稍后重试')
  }
}

// 删除会话
const deleteSession = async (sessionId) => {
  // 确认删除
  const confirmed = confirm('确定要删除该会话吗？此操作不可撤销。')
  if (!confirmed) return
  
  try {
    // 显示加载状态
    treeholeStore.state.loading = true
    
    const result = await treeholeStore.deleteSession(sessionId)
    
    // 检查是否删除成功（现在result是对象，检查success属性）
    if (result && result.success) {
      // 成功删除后添加动画效果（通过CSS类实现）
      const sessionElement = document.querySelector(`[data-session-id="${sessionId}"]`)
      if (sessionElement) {
        sessionElement.classList.add('session-deleting')
        
        // 等待动画完成后再更新状态
        setTimeout(() => {
          // 如果删除的是当前会话
          if (sessionId === currentSessionId.value) {
            // 检查是否还有其他会话
            const remainingSessions = sessions.value.filter(s => s.sessionId !== sessionId)
            if (remainingSessions.length > 0) {
              // 切换到第一个会话
              switchSession(remainingSessions[0].sessionId)
            } else {
              // 如果没有其他会话，自动创建新会话
              createNewSession()
            }
          }
        }, 300)
      } else {
        // 如果找不到元素，直接处理状态切换
        if (sessionId === currentSessionId.value) {
          const remainingSessions = sessions.value.filter(s => s.sessionId !== sessionId)
          if (remainingSessions.length > 0) {
            await switchSession(remainingSessions[0].sessionId)
          } else {
            await createNewSession()
          }
        }
      }
    }
  } catch (error) {
    console.error('删除会话失败:', error)
    utils.showError('删除会话失败，请稍后重试')
  } finally {
    // 隐藏加载状态
    treeholeStore.state.loading = false
  }
}

// 格式化日期 - 优先使用数据库中的实际时间
const formatDate = (dateString) => {
  if (!dateString) return ''
  
  // 尝试直接使用日期对象或字符串
  const date = dateString instanceof Date ? dateString : new Date(dateString)
  
  // 检查日期是否有效
  if (isNaN(date.getTime())) {
    console.warn('无效的日期格式:', dateString)
    return ''
  }
  
  const now = new Date()
  const isToday = date.toDateString() === now.toDateString()
  const isYesterday = new Date(now - 86400000).toDateString() === date.toDateString()
  
  // 格式化小时和分钟
  const hours = date.getHours().toString().padStart(2, '0')
  const minutes = date.getMinutes().toString().padStart(2, '0')
  
  if (isToday) {
    // 今天的消息只显示时间
    return `${hours}:${minutes}`
  } else if (isYesterday) {
    // 昨天的消息显示"昨天"和时间
    return `昨天 ${hours}:${minutes}`
  } else {
    // 其他日期显示具体日期和时间
    return `${date.getMonth() + 1}月${date.getDate()}日 ${hours}:${minutes}`
  }
}

// 渲染Markdown内容
const renderMarkdown = (text) => {
  if (!text) return ''
  // 配置marked选项
  marked.setOptions({
    breaks: true, // 将换行符转换为<br>
    gfm: true, // 使用GitHub风格的Markdown
    headerIds: false, // 不生成header IDs
    mangle: false // 不转换链接文本
  })
  return marked.parse(text)
}

// 滚动到底部
const scrollToBottom = () => {
  nextTick(() => {
    const chatContent = document.querySelector('.chat-content')
    if (chatContent && !hasScrolled.value) {
      chatContent.scrollTop = chatContent.scrollHeight
    }
  })
}

// 处理滚动事件，检测用户是否手动滚动
const handleScroll = (event) => {
  const container = event.target
  const scrollTop = container.scrollTop
  const scrollHeight = container.scrollHeight
  const clientHeight = container.clientHeight
  
  // 记录上次滚动位置
  lastScrollTop.value = scrollTop
  
  // 如果用户向上滚动了一定距离，标记为已滚动
  if (scrollHeight - scrollTop - clientHeight > 50) {
    hasScrolled.value = true
  } else {
    // 如果用户滚动到底部，重置标记
    hasScrolled.value = false
  }
}

// 显示登录提示
const showLoginPrompt = () => {
  if (confirm('提示：登录后可以保存对话历史并使用更多功能。是否前往登录？')) {
    // 这里可以重定向到登录页面
    window.location.href = '/login';
  }
};

// 组件挂载时初始化
onMounted(async () => {
  try {
    // 初始化时获取配置
    await treeholeStore.getConfig()
    
    // 获取会话列表
    await treeholeStore.getSessions()
    
    // 如果没有会话，创建一个新会话
    if (sessions.value.length === 0) {
      await treeholeStore.createSession()
      // 新创建会话后也加载其历史记录
      if (treeholeStore.state.currentSessionId) {
        await treeholeStore.getSessionHistory(treeholeStore.state.currentSessionId)
      }
    } else {
      // 否则使用第一个会话，确保使用sessionId
      const firstSession = sessions.value[0]
      await treeholeStore.getSessionHistory(firstSession.sessionId)
    }
    
    // 初始化完成后滚动到底部
    await nextTick()
    scrollToBottom()
    
    // 未登录时显示登录提示
    if (!isAuthenticated.value) {
      setTimeout(showLoginPrompt, 1000);
    }
  } catch (error) {
    console.error('初始化树洞过程中发生错误:', error)
    utils.showError('初始化失败，请稍后重试')
  }
})

// 监听认证状态变化
watch(() => isAuthenticated.value, (newValue) => {
  if (newValue) {
    utils.showSuccess('登录成功，可以使用更多功能了')
    // 刷新会话列表
    treeholeStore.getSessions().catch(err => console.warn('刷新会话列表失败:', err))
  }
})

// 监听网络状态变化
watch(() => isOnline.value, (isOnline) => {
  if (isOnline) {
    // 网络恢复时的处理
    utils.showInfo('网络已恢复')
  } else {
    utils.showWarning('网络连接已断开，将使用离线模式')
  }
})

// 监听消息变化，自动滚动到底部
watch(() => messages.value, () => {
  // 只有在用户没有手动滚动的情况下才自动滚动
  if (!hasScrolled.value) {
    scrollToBottom()
  }
}, { deep: true })

// 获取会话的最后一条消息预览
const getLastMessagePreview = (session) => {
  if (!session || !Array.isArray(session.messages) || session.messages.length === 0) return ''
  const lastMsg = session.messages[session.messages.length - 1]
  return (lastMsg?.text || '').length > 20 ? (lastMsg.text.substring(0, 20) + '...') : lastMsg.text
}
</script>

<style scoped>
/* 确保用户消息头像在右侧 */
.message-content {
  flex: 1;
}

.message.user {
  flex-direction: row-reverse;
  justify-content: flex-start;
}

.message.user .message-content {
  margin-right: 10px;
  margin-left: 0;
}

.message.bot .message-content {
  margin-left: 10px;
  margin-right: 0;
}

.markdown-content {
  line-height: 1.6;
}

.markdown-content h1,
.markdown-content h2,
.markdown-content h3 {
  margin: 1rem 0 0.5rem 0;
  color: #1f2937;
}

.markdown-content h1 {
  font-size: 1.5rem;
  font-weight: 700;
}

.markdown-content h2 {
  font-size: 1.25rem;
  font-weight: 600;
}

.markdown-content h3 {
  font-size: 1.1rem;
  font-weight: 600;
}

.markdown-content p {
  margin: 0.5rem 0;
}

.markdown-content ul,
.markdown-content ol {
  padding-left: 1.5rem;
  margin: 0.5rem 0;
}

.markdown-content li {
  margin: 0.25rem 0;
}

.markdown-content strong {
  font-weight: 600;
}

.markdown-content em {
  font-style: italic;
}

.treehole-wrapper {
  min-height: 100vh;
  background: linear-gradient(135deg, #6366f1 0%, #8b5cf6 50%, #d946ef 100%);
  background-size: 400% 400%;
  animation: gradientAnimation 15s ease infinite;
  padding: 20px;
  position: relative;
  overflow: hidden;
}

.treehole-wrapper::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: url("data:image/svg+xml,%3Csvg width='60' height='60' viewBox='0 0 60 60' xmlns='http://www.w3.org/2000/svg'%3E%3Cg fill='none' fill-rule='evenodd'%3E%3Cg fill='%23ffffff' fill-opacity='0.05'%3E%3Cpath d='M36 34v-4h-2v4h-4v2h4v4h2v-4h4v-2h-4zm0-30V0h-2v4h-4v2h4v4h2V6h4V4h-4zM6 34v-4H4v4H0v2h4v4h2v-4h4v-2H6zM6 4V0H4v4H0v2h4v4h2V6h4V4H6z'/%3E%3C/g%3E%3C/g%3E%3C/svg%3E");
  z-index: 0;
}

@keyframes gradientAnimation {
  0% { background-position: 0% 50%; }
  50% { background-position: 100% 50%; }
  100% { background-position: 0% 50%; }
}

.treehole-container {
  max-width: 900px;
  margin: 0 auto;
  background: rgba(255, 255, 255, 0.95);
  border-radius: 20px;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.1), 0 0 0 1px rgba(255, 255, 255, 0.1);
  overflow: hidden;
  display: flex;
  flex-direction: column;
  height: calc(100vh - 40px);
  position: relative;
  backdrop-filter: blur(10px);
  border: 1px solid rgba(255, 255, 255, 0.2);
}

/* 未登录提示条样式 */
.auth-prompt {
  background: linear-gradient(135deg, #fbbf24 0%, #f59e0b 100%);
  color: white;
  padding: 10px 20px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 14px;
  animation: slideDown 0.3s ease;
}

@keyframes slideDown {
  from {
    transform: translateY(-100%);
    opacity: 0;
  }
  to {
    transform: translateY(0);
    opacity: 1;
  }
}

.auth-prompt .login-btn {
  background: white;
  color: #f59e0b;
  border: none;
  padding: 6px 16px;
  border-radius: 20px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s ease;
}

.auth-prompt .login-btn:hover {
  background: #fef3c7;
  transform: translateY(-1px);
}

.chat-header {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  padding: 20px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.1);
}

.chat-header h1 {
  margin: 0;
  font-size: 24px;
  font-weight: 600;
}

.session-actions {
  display: flex;
  gap: 10px;
}

.new-session-btn,
.sessions-btn {
  background: rgba(255, 255, 255, 0.2);
  border: 1px solid rgba(255, 255, 255, 0.3);
  color: white;
  padding: 8px 16px;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.3s ease;
  font-size: 14px;
}

.new-session-btn:hover,
.sessions-btn:hover {
  background: rgba(255, 255, 255, 0.3);
  transform: translateY(-2px);
}

.new-session-btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
  transform: none;
}

/* 会话列表侧边栏 */
.session-list-sidebar {
  position: absolute;
  top: 0;
  left: 0;
  width: 300px;
  height: 100%;
  background: white;
  box-shadow: 2px 0 20px rgba(0, 0, 0, 0.1);
  z-index: 100;
  display: flex;
  flex-direction: column;
  animation: slideIn 0.3s ease;
}

@keyframes slideIn {
  from {
    transform: translateX(-100%);
  }
  to {
    transform: translateX(0);
  }
}

.sidebar-header {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  padding: 20px;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.sidebar-header h3 {
  margin: 0;
  font-size: 18px;
}

.close-btn {
  background: none;
  border: none;
  color: white;
  font-size: 20px;
  cursor: pointer;
  padding: 0;
  width: 30px;
  height: 30px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 50%;
  transition: background 0.3s ease;
}

.close-btn:hover {
  background: rgba(255, 255, 255, 0.2);
}

.sessions-container {
  flex: 1;
  overflow-y: auto;
  padding: 10px;
}

.session-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 15px;
  margin-bottom: 10px;
  border-radius: 12px;
  background: #f8f9fa;
  cursor: pointer;
  transition: all 0.3s ease;
  border: 2px solid transparent;
}

.session-item:hover {
  background: #e9ecef;
  transform: translateX(5px);
}

.session-item.active {
  border-color: #667eea;
  background: #e8f0fe;
}

.session-info {
  flex: 1;
  min-width: 0;
}

.session-title {
  font-weight: 500;
  color: #333;
  margin-bottom: 5px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.session-time {
  font-size: 12px;
  color: #666;
}

.delete-session-btn {
  background: none;
  border: none;
  cursor: pointer;
  padding: 8px;
  border-radius: 4px;
  font-size: 16px;
  transition: all 0.2s ease;
  color: #666;
}

.delete-session-btn:hover {
  background-color: #ffebee;
  color: #f44336;
  transform: scale(1.1);
}

.delete-session-btn:active {
  transform: scale(0.95);
}

/* 删除动画效果 */
.session-deleting {
  animation: deleteSession 0.3s ease-out forwards;
}

@keyframes deleteSession {
  0% {
    opacity: 1;
    transform: translateX(0);
    max-height: 200px;
  }
  50% {
    opacity: 0.5;
    transform: translateX(50px);
  }
  100% {
    opacity: 0;
    transform: translateX(-100px);
    max-height: 0;
    margin: 0;
    padding: 0;
    overflow: hidden;
  }
}

/* 会话项过渡效果 */
.session-item {
  transition: all 0.3s ease;
}

/* 加载状态下的禁用样式 */
.delete-session-btn:disabled,
.treehole-container.loading .delete-session-btn {
  opacity: 0.5;
  cursor: not-allowed;
  transform: none !important;
}

.empty-sessions {
  text-align: center;
  color: #999;
  padding: 40px 20px;
  font-style: italic;
}

/* 聊天内容区域 */
.chat-content {
  flex: 1;
  overflow-y: auto;
  padding: 20px;
  background: #f5f7fa;
  display: flex;
  flex-direction: column;
  gap: 15px;
}

.empty-chat {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  color: #999;
}

.empty-chat p {
  margin-bottom: 20px;
  font-size: 16px;
}

.start-btn {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  border: none;
  padding: 12px 24px;
  border-radius: 8px;
  cursor: pointer;
  font-size: 16px;
  transition: transform 0.3s ease;
}

.start-btn:hover {
  transform: scale(1.05);
}

.message {
  display: flex;
  align-items: flex-start;
  gap: 10px;
  max-width: 80%;
}

.message.bot,
.message.assistant {
  align-self: flex-start;
}

.message.user {
  align-self: flex-end;
  flex-direction: row-reverse;
}

.bot-avatar,
.user-avatar {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 20px;
  flex-shrink: 0;
}

.message-text {
  background: white;
  padding: 12px 16px;
  border-radius: 18px;
  line-height: 1.5;
  word-wrap: break-word;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.message.bot .message-text,
.message.assistant .message-text {
  background: #e8f0fe;
  border-bottom-left-radius: 4px;
}

.message.user .message-text {
  background: #667eea;
  color: white;
  border-bottom-right-radius: 4px;
}

.loading-indicator {
  align-self: flex-start;
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 12px 16px;
  background: #e8f0fe;
  border-radius: 18px;
  border-bottom-left-radius: 4px;
  color: #667eea;
  font-size: 14px;
}

.loading-dots {
  display: flex;
  gap: 4px;
}

.loading-dots::before,
.loading-dots::after,
.loading-dots span {
  content: '';
  width: 8px;
  height: 8px;
  background: #667eea;
  border-radius: 50%;
  animation: loading 1.4s infinite ease-in-out both;
}

.loading-dots::before {
  animation-delay: -0.32s;
}

.loading-dots span {
  animation-delay: -0.16s;
}

@keyframes loading {
  0%, 80%, 100% {
    transform: scale(0);
    opacity: 0.5;
  }
  40% {
    transform: scale(1);
    opacity: 1;
  }
}

/* 快速回复区域 */
.quick-replies {
  padding: 15px 20px;
  background: white;
  border-top: 1px solid #e0e0e0;
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
}

.quick-reply-btn {
  background: #f0f4ff;
  border: 1px solid #d1d5db;
  padding: 8px 16px;
  border-radius: 20px;
  cursor: pointer;
  font-size: 14px;
  color: #4b5563;
  transition: all 0.3s ease;
}

.quick-reply-btn:hover:not(:disabled) {
  background: #e0e7ff;
  border-color: #667eea;
  color: #667eea;
  transform: translateY(-2px);
}

.quick-reply-btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
  transform: none;
}

/* 输入区域 */
.input-area {
  padding: 20px;
  background: white;
  border-top: 1px solid #e0e0e0;
  display: flex;
  gap: 10px;
  align-items: center;
}

.input-field {
  flex: 1;
  padding: 12px 16px;
  border: 2px solid #e0e0e0;
  border-radius: 24px;
  font-size: 16px;
  outline: none;
  transition: border-color 0.3s ease;
}

.input-field:focus {
  border-color: #667eea;
}

.input-field:disabled {
  background: #f5f5f5;
  cursor: not-allowed;
}

.send-button {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  border: none;
  padding: 12px 24px;
  border-radius: 24px;
  cursor: pointer;
  font-size: 16px;
  font-weight: 500;
  transition: all 0.3s ease;
}

.send-button:hover:not(:disabled) {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(102, 126, 234, 0.4);
}

.send-button:disabled {
  opacity: 0.6;
  cursor: not-allowed;
  transform: none;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .treehole-wrapper {
    padding: 10px;
  }
  
  .treehole-container {
    height: calc(100vh - 20px);
  }
  
  .chat-header {
    padding: 15px;
  }
  
  .chat-header h1 {
    font-size: 20px;
  }
  
  .session-list-sidebar {
    width: 100%;
  }
  
  .message {
    max-width: 90%;
  }
  
  .input-field {
    font-size: 14px;
  }
  
  .send-button {
    padding: 10px 16px;
    font-size: 14px;
  }
}
</style>
