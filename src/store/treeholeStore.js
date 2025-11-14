// store/treeholeStore.js
import { reactive, provide, inject, watch } from 'vue';
import * as treeholeApi from '../api/treehole';
import { utils, apiClient } from '../api'; // 导入工具函数和API客户端，用于处理认证和错误

// 模拟回复数据
const mockReplies = [
  '我理解你的感受，这确实很不容易。能和我多分享一些吗？',
  '谢谢你的分享。我在这里听你倾诉，不会做出评判。',
  '听起来你正在经历一些挑战。记得要对自己好一点。',
  '你的想法很有价值。继续说下去，我很感兴趣。',
  '我能感受到你的情绪。有时候表达出来已经是很大的一步了。',
  '这种感觉一定很复杂。你是怎么应对的呢？',
  '你并不孤单，很多人都经历过类似的情绪。',
  '谢谢你信任我，愿意分享这些。',
  '我在这里陪伴你，无论你想聊什么。',
  '你的感受是完全合理的。允许自己有这样的情绪。'
];

// 获取随机模拟回复
function getRandomMockReply() {
  const randomIndex = Math.floor(Math.random() * mockReplies.length);
  return mockReplies[randomIndex];
}

// 从本地存储加载数据
const loadFromLocalStorage = () => {
  try {
    const savedData = localStorage.getItem('treeholeLocalData');
    if (savedData) {
      const parsed = JSON.parse(savedData);
      // 处理时间戳
      if (Array.isArray(parsed.sessions)) {
        parsed.sessions = parsed.sessions.map(session => ({
          ...session,
          createdAt: session.createdAt ? new Date(session.createdAt) : new Date(),
          updatedAt: session.updatedAt ? new Date(session.updatedAt) : new Date()
        }));
      }
      if (Array.isArray(parsed.messages)) {
        parsed.messages = parsed.messages.map(msg => ({
          ...msg,
          timestamp: msg.timestamp ? new Date(msg.timestamp) : new Date()
        }));
      }
      return {
        sessions: parsed.sessions || [],
        currentSessionId: parsed.currentSessionId || null,
        messages: parsed.messages || []
      };
    }
  } catch (error) {
    console.warn('加载本地存储数据失败:', error);
  }
  return { sessions: [], currentSessionId: null, messages: [] };
};

// 保存数据到本地存储
const saveToLocalStorage = (data) => {
  try {
    localStorage.setItem('treeholeLocalData', JSON.stringify(data));
  } catch (error) {
    console.warn('保存到本地存储失败:', error);
  }
};

// 初始化状态，合并本地存储和默认值
const initialLocalData = loadFromLocalStorage();
const treeholeState = reactive({
  config: {},
  sessions: initialLocalData.sessions,
  currentSessionId: initialLocalData.currentSessionId,
  messages: initialLocalData.messages,
  quickReplies: [],
  loading: false,
  isOnline: navigator.onLine // 追踪网络状态
});

// 监听网络状态变化
if (typeof window !== 'undefined') {
  window.addEventListener('online', () => {
    treeholeState.isOnline = true;
    // 网络恢复时尝试更新会话列表
    if (utils.isAuthenticated()) {
      treeholeStore.getSessions().catch(err => console.warn('网络恢复后更新会话列表失败:', err));
    }
  });
  
  window.addEventListener('offline', () => {
    treeholeState.isOnline = false;
  });
}

// 检查认证状态
const checkAuthStatus = () => {
  if (!utils.isAuthenticated()) {
    console.warn('用户未认证，某些功能可能受限');
    return false;
  }
  return true;
};

// 状态管理方法
const treeholeStore = {
  // 状态
  state: treeholeState,
  
  // 检查认证状态
  checkAuthStatus() {
    return checkAuthStatus();
  },
  
  // 获取配置
  async getConfig() {
    try {
      // 检查认证状态
      checkAuthStatus();
      
      const response = await treeholeApi.getTreeholeConfig();
      treeholeState.config = response; // 配置已在API层处理格式一致性
      return treeholeState.config;
    } catch (error) {
      // 不抛出错误，使用默认配置
      console.warn('获取配置失败，使用默认配置:', error);
      // 设置默认配置
      treeholeState.config = {
        maxMessageLength: 1000,
        cooldownTime: 1000
      };
      return treeholeState.config;
    }
  },

  // 创建会话
  async createSession() {
    // 生成临时会话ID（无论是否联网都先准备好）
    const tempSessionId = `temp_${Date.now()}`;
    
    try {
      treeholeState.loading = true;
      
      // 检查认证状态和网络状态
      const isAuthenticated = checkAuthStatus();
      const canUseOnlineFeatures = isAuthenticated && treeholeState.isOnline;
      
      // 如果已认证且在线，尝试创建真实会话
      if (canUseOnlineFeatures) {
        try {
          const response = await treeholeApi.createSession();
          
          // 确保正确保存会话ID
          if (response.sessionId) {
            treeholeState.currentSessionId = response.sessionId;
            // 初始化消息列表，处理时间戳
            treeholeState.messages = [{
              content: response.initialMessage || '你好！我是你的心灵树洞助手，有什么可以帮到你的吗？',
              text: response.initialMessage || '你好！我是你的心灵树洞助手，有什么可以帮到你的吗？', // 兼容旧字段
              role: 'assistant',
              sender: 'assistant', // 兼容旧字段
              timestamp: response.timestamp ? new Date(response.timestamp) : new Date(),
              sessionId: response.sessionId
            }];
            
            // 重新获取会话列表
            await this.getSessions().catch(err => console.warn('更新会话列表失败:', err));
            utils.showSuccess('会话创建成功');
            
            return {
              ...response,
              offline: false
            };
          }
        } catch (apiError) {
          console.warn('创建真实会话失败，切换到离线模式:', apiError);
          utils.showWarning('创建在线会话失败，已切换到离线模式');
        }
      }
      
      // 未认证、离线或API失败时，创建临时会话
      treeholeState.currentSessionId = tempSessionId;
      treeholeState.messages = []; // 清空现有消息
      
      // 创建本地临时会话对象
      const tempSession = {
        sessionId: tempSessionId,
        title: '未命名对话',
        createdAt: new Date(),
        updatedAt: new Date(),
        offline: true
      };
      
      // 添加到会话列表
      treeholeState.sessions.push(tempSession);
      // 保存到本地存储
      saveToLocalStorage({
        sessions: treeholeState.sessions,
        currentSessionId: treeholeState.currentSessionId,
        messages: treeholeState.messages
      });
      
      // 根据状态生成不同的欢迎消息
      let welcomeText = '欢迎来到心灵树洞！';
      if (!isAuthenticated) {
        welcomeText += '\n您尚未登录，现在使用的是离线模式。登录后可以保存您的对话历史。';
      } else if (!treeholeState.isOnline) {
        welcomeText += '\n当前网络连接不可用，已为您启用离线模式。';
      } else {
        welcomeText += '\n由于系统原因，您现在使用的是离线模式。';
      }
      welcomeText += '\n我会在这里认真倾听您的想法和感受。';
      
      const welcomeMessage = {
        content: welcomeText,
            text: welcomeText, // 兼容旧字段
            role: 'assistant',
            sender: 'assistant', // 兼容旧字段
            timestamp: new Date(),
            sessionId: tempSessionId,
            offline: true
      };
      treeholeState.messages.push(welcomeMessage);
      
      // 显示适当的提示
      if (!isAuthenticated) {
        utils.showInfo('您未登录，正在使用离线模式');
      } else if (!treeholeState.isOnline) {
        utils.showWarning('网络连接失败，已启用离线模式');
      }
      
      // 返回临时会话信息
      return {
        sessionId: tempSessionId,
        initialMessage: welcomeMessage.text,
        timestamp: welcomeMessage.timestamp,
        offline: true
      };
    } catch (error) {
      console.error('创建会话时发生意外错误:', error);
      
      // 极端情况下的错误处理，确保至少有一个临时会话可用
      treeholeState.currentSessionId = tempSessionId;
      treeholeState.messages = [{
          content: '欢迎来到心灵树洞！\n系统出现问题，已为您启用离线模式。\n请放心与我交流，我会认真倾听。',
          text: '欢迎来到心灵树洞！\n系统出现问题，已为您启用离线模式。\n请放心与我交流，我会认真倾听。', // 兼容旧字段
          role: 'assistant',
          sender: 'assistant', // 兼容旧字段
          timestamp: new Date(),
          sessionId: tempSessionId,
          offline: true
        }];
      
      utils.showError('系统错误，但已为您创建离线会话');
      
      // 不抛出错误，返回临时会话信息
      return {
        sessionId: tempSessionId,
        timestamp: new Date(),
        offline: true
      };
    } finally {
      treeholeState.loading = false;
    }
  },

  // 获取会话列表
  async getSessions() {
    try {
      // 检查认证状态
      if (!checkAuthStatus()) {
        console.warn('用户未认证，使用本地存储的会话列表');
        // 未认证用户使用本地存储的会话列表，不再清空
        return treeholeState.sessions;
      }
      
      const response = await treeholeApi.getSessions();
      const sessionsData = response.data || response;
      
      // 确保sessionsData是数组
        if (!Array.isArray(sessionsData)) {
          console.error('会话列表格式错误:', sessionsData);
          // 发生错误时保留本地存储的会话列表
          return treeholeState.sessions;
        }
      
      // 处理每个会话的时间戳和字段名映射
      treeholeState.sessions = sessionsData.map(session => ({
        ...session,
        sessionId: session.sessionId || session.id, // 兼容id字段
        title: session.title || session.sessionName || '未命名对话', // 从title读取，兼容旧的sessionName字段
        createdAt: session.createdAt ? new Date(session.createdAt) : new Date(),
        updatedAt: session.updatedAt ? new Date(session.updatedAt) : new Date()
      }));
      
      // 按更新时间倒序排列
      treeholeState.sessions.sort((a, b) => b.updatedAt - a.updatedAt);
      
      return treeholeState.sessions;
    } catch (error) {
      const errorMessage = utils.handleApiError(error, '获取会话列表失败');
      console.error('获取会话列表失败:', errorMessage);
      // 发生错误时不抛出异常，保留本地存储的会话列表
      return treeholeState.sessions;
    }
  },

  // 发送消息
  async sendMessage(message) {
    try {
      // 验证消息内容
      if (!message || typeof message !== 'string' || message.trim() === '') {
        utils.showWarning('请输入有效的消息内容');
        return null;
      }
      
      // 检查消息长度限制
      const maxLength = treeholeState.config.maxMessageLength || 1000;
      if (message.length > maxLength) {
        utils.showWarning(`消息长度不能超过${maxLength}个字符`);
        return null;
      }
      
      // 确保有活动会话
      let sessionId = treeholeState.currentSessionId;
      if (!sessionId) {
        const newSession = await this.createSession();
        sessionId = newSession.sessionId;
      }
      
      treeholeState.loading = true;
      const trimmedMessage = message.trim();
      
      // 添加用户消息到本地，包含会话ID
      const userMessage = {
        content: trimmedMessage,
        text: trimmedMessage, // 兼容旧字段
        role: 'user',
        sender: 'user', // 兼容旧字段
        timestamp: new Date(),
        sessionId: sessionId
      };
      treeholeState.messages.push(userMessage);
      
      // 立即保存用户消息到本地存储，确保消息不会丢失
      try {
        saveToLocalStorage({
          sessions: [...treeholeState.sessions],
          currentSessionId: treeholeState.currentSessionId,
          messages: [...treeholeState.messages]
        });
      } catch (storageError) {
        console.warn('保存用户消息到本地存储失败，但消息已添加到当前会话:', storageError);
      }

      // 检查是否可以使用在线功能
      const isAuthenticated = checkAuthStatus();
      const isTempSession = sessionId && String(sessionId).startsWith('temp_');
      const canUseOnlineFeatures = isAuthenticated && !isTempSession && treeholeState.isOnline;
      
      if (canUseOnlineFeatures) {
        try {
          // 获取当前会话的最近10条消息作为上下文
          const currentSessionMessages = treeholeState.messages
            .filter(msg => msg.sessionId === sessionId)
            .sort((a, b) => new Date(a.timestamp) - new Date(b.timestamp))
            .slice(-10); // 获取最近的10条消息
          
          // 格式化上下文消息，只包含必要信息
          const context = currentSessionMessages.map(msg => ({
            sender: msg.sender,
            text: msg.text,
            timestamp: msg.timestamp
          }));
          
          // 调用API发送消息，包含上下文信息
          const response = await treeholeApi.chatWithAI({
            message: trimmedMessage,
            sessionId: sessionId,
            context: context
          });
          
          // 添加AI回复到本地，处理时间戳格式
          const botMessage = {
            content: response.reply || getRandomMockReply(),
            text: response.reply || getRandomMockReply(), // 兼容旧字段
            role: 'assistant',
            sender: 'assistant', // 兼容旧字段
            timestamp: response.timestamp ? new Date(response.timestamp) : new Date(),
            sessionId: sessionId,
            messageId: response.messageId || response.id,
            offline: false
          };
          treeholeState.messages.push(botMessage);
          
          // 更新会话列表的最后活跃时间
          this.getSessions().catch(err => console.warn('更新会话列表失败:', err));
          
          // 保存消息到本地存储
          saveToLocalStorage({
            sessions: treeholeState.sessions,
            currentSessionId: treeholeState.currentSessionId,
            messages: treeholeState.messages
          });
          
          return {
            reply: response.reply,
            messageId: response.messageId,
            timestamp: response.timestamp,
            offline: false
          };
        } catch (apiError) {
          console.warn('在线消息发送失败，使用离线回复:', apiError);
          // 检查是否是网络错误或超时
          if (apiError.code === 'ECONNABORTED' || !apiError.response) {
            // 临时更新网络状态
            treeholeState.isOnline = false;
            // 显示网络问题提示
            utils.showWarning('检测到网络问题，暂时切换到离线模式');
          }
          // 继续执行离线回复逻辑
        }
      }
      
      // 离线模式响应逻辑
      // 生成更丰富的离线回复，根据消息内容类型提供不同的响应
      let offlineReply;
      
      // 基于消息内容的简单分析，提供更相关的回复
      if (trimmedMessage.includes('压力') || trimmedMessage.includes('紧张') || trimmedMessage.includes('焦虑')) {
        offlineReply = [
          "我理解您感到压力。深呼吸，试着放松一下。",
          "压力是生活的一部分，但重要的是我们如何应对。您愿意分享更多细节吗？",
          "感到压力是很正常的。记住要照顾好自己，适当休息。",
          "压力有时会让人感到窒息，但请相信这是暂时的。您已经在寻求帮助，这很棒！"
        ];
      } else if (trimmedMessage.includes('难过') || trimmedMessage.includes('伤心') || trimmedMessage.includes('难过')) {
        offlineReply = [
          "我能感受到您的难过。允许自己有这样的情绪是正常的。",
          "悲伤是心灵的自然反应。您并不孤单，我在这里陪伴您。",
          "感到难过没关系，这是治愈过程的一部分。我会认真倾听您的感受。",
          "您的感受是有效的。请记住，时间会慢慢抚平伤痛。"
        ];
      } else if (trimmedMessage.includes('开心') || trimmedMessage.includes('高兴') || trimmedMessage.includes('快乐')) {
        offlineReply = [
          "听到您开心的消息真好！能和我分享更多细节吗？",
          "快乐是如此珍贵，我很高兴您现在有这样的感受。",
          "您的积极情绪感染了我！继续保持这样的状态。",
          "看到您开心我也很开心。快乐需要被珍惜和分享。"
        ];
      } else if (trimmedMessage.includes('迷茫') || trimmedMessage.includes('困惑') || trimmedMessage.includes('不知道')) {
        offlineReply = [
          "感到迷茫是成长的一部分。也许我们可以一起梳理思路。",
          "困惑时不妨给自己一些时间和空间。有时候答案会自然浮现。",
          "迷茫并不可怕，它常常是新开始的信号。您想从哪里说起呢？",
          "面对未知确实令人不安。让我们一步步来，找出可能的方向。"
        ];
      } else {
        // 默认回复
        offlineReply = [
          "我理解您的感受，能再多说一些吗？",
          "您的分享对我很重要，我在认真倾听。",
          "感谢您的信任。继续说下去，我很感兴趣。",
          "我在这里陪伴您，无论您想聊什么话题。",
          "您的想法很有价值，我很欣赏您愿意表达出来。"
        ];
      }
      
      const randomReply = offlineReply[Math.floor(Math.random() * offlineReply.length)];
      const timestamp = new Date();
      
      // 添加离线回复到状态中
      // 在API调用失败或网络离线时显示提示信息
      const isApiError = apiError !== undefined;
      const showOfflineNotice = !treeholeState.isOnline || (isApiError && (apiError.code === 'ECONNABORTED' || !apiError.response));
      
      const offlineMessage = {
        content: randomReply + (showOfflineNotice ? "\n\n(当前处于离线模式，您的消息将在网络恢复后处理)" : ""),
        text: randomReply + (showOfflineNotice ? "\n\n(当前处于离线模式，您的消息将在网络恢复后处理)" : ""), // 兼容旧字段
        role: 'assistant',
        sender: 'assistant', // 兼容旧字段
        timestamp,
        sessionId,
        offline: true
      };
      
      // 确保消息添加成功
      treeholeState.messages.push(offlineMessage);
      
      // 更新会话的最后活动时间
      const sessionIndex = treeholeState.sessions.findIndex(s => s.sessionId === sessionId);
      if (sessionIndex !== -1) {
        treeholeState.sessions[sessionIndex].updatedAt = timestamp;
        // 重新排序会话列表
        treeholeState.sessions.sort((a, b) => b.updatedAt - a.updatedAt);
      }
      
      // 保存到本地存储，确保数据完整性
      try {
        saveToLocalStorage({
          sessions: [...treeholeState.sessions], // 创建副本确保数据快照
          currentSessionId: treeholeState.currentSessionId,
          messages: [...treeholeState.messages] // 创建副本确保数据快照
        });
      } catch (storageError) {
        console.warn('保存到本地存储失败，但消息已添加到当前会话:', storageError);
      }
      
      return { reply: randomReply, offline: true };
    } catch (error) {
      console.error('发送消息过程中发生意外错误:', error);
      
      // 添加友好的错误提示消息
      const errorMessage = utils.handleApiError(error, '抱歉，发送消息时出现问题');
      treeholeState.messages.push({
            content: errorMessage || '抱歉，发送消息失败，请稍后重试。',
            text: errorMessage || '抱歉，发送消息失败，请稍后重试。', // 兼容旧字段
            role: 'assistant',
            sender: 'assistant', // 兼容旧字段
            timestamp: new Date(),
            sessionId: treeholeState.currentSessionId || 'local_session',
            error: true
          });
      
      // 显示错误提示，但不影响用户继续使用
      utils.showError(errorMessage);
      
      return null; // 失败时返回null而不是抛出错误，避免上层调用崩溃
    } finally {
      treeholeState.loading = false;
    }
  },

  // 获取会话历史
  async getSessionHistory(sessionId) {
    try {
      // 验证会话ID - 现在支持数字类型
      if (!sessionId) {
        console.warn('无效的会话ID，使用本地会话');
        
        // 使用本地会话作为后备
        const tempSessionId = `temp_${Date.now()}`;
        treeholeState.currentSessionId = tempSessionId;
        treeholeState.messages = [{
          content: '会话信息无效，已为您创建临时会话。\n我是您的AI助手，有什么想和我聊聊的吗？',
          text: '会话信息无效，已为您创建临时会话。\n我是您的AI助手，有什么想和我聊聊的吗？', // 兼容旧字段
          role: 'assistant',
          sender: 'assistant', // 兼容旧字段
          timestamp: new Date(),
          sessionId: tempSessionId,
          offline: true
        }];
        
        utils.showWarning('会话信息无效，已创建临时会话');
        return treeholeState.messages;
      }
      
      treeholeState.loading = true;
      
      // 检查是否是临时会话或无法使用在线功能
      const isTempSession = String(sessionId).startsWith('temp_');
      const isAuthenticated = checkAuthStatus();
      const canUseOnlineFeatures = isAuthenticated && !isTempSession && treeholeState.isOnline;
      
      if (!canUseOnlineFeatures) {
        console.log('临时会话或未登录或离线，使用本地响应');
        
        // 从本地存储加载该会话的历史消息
        const allLocalData = loadFromLocalStorage();
        const sessionMessages = allLocalData.messages.filter(msg => msg.sessionId === sessionId);
        
        treeholeState.currentSessionId = sessionId;
        
        // 无论是否有历史消息，都只显示当前会话的消息
        treeholeState.messages = sessionMessages;
        
        if (sessionMessages.length > 0) {
          // 如果有历史消息，直接返回
          return treeholeState.messages;
        } else {
          // 没有历史消息时，添加会话切换提示
          // 构建适当的提示消息
          let welcomeText = '切换到会话。';
          if (!isAuthenticated) {
            welcomeText += '\n您尚未登录，使用的是离线模式。';
          } else if (!treeholeState.isOnline) {
            welcomeText += '\n网络连接不可用，已启用离线模式。';
          } else if (isTempSession) {
            welcomeText += '\n这是一个临时会话，不会保存到服务器。';
          }
          welcomeText += '\n我会在这里倾听您的想法和感受。';
          
          treeholeState.messages = [{
            content: welcomeText,
            text: welcomeText, // 兼容旧字段
            role: 'assistant',
            sender: 'assistant', // 兼容旧字段
            timestamp: new Date(),
            sessionId,
            offline: true
          }];
        }
        
        if (!isAuthenticated) {
          utils.showInfo('您未登录，正在使用离线模式');
        }
        
        return treeholeState.messages;
      }
      
      // 已认证用户尝试获取真实会话历史
      try {
        const response = await treeholeApi.getSessionHistory(sessionId);
        const historyData = response.data || response || [];
        
        // 确保historyData是数组
        if (!Array.isArray(historyData)) {
          console.error('会话历史格式错误:', historyData);
          // 从本地存储获取备用数据
          const allLocalData = loadFromLocalStorage();
          const sessionMessages = allLocalData.messages.filter(msg => msg.sessionId === sessionId);
          treeholeState.messages = sessionMessages;
          treeholeState.currentSessionId = sessionId;
          utils.showError('获取会话历史失败，使用本地备份');
          return sessionMessages;
        }
        
        // 处理每条消息并确保使用原始数据库时间戳
        const processedMessages = historyData.map(msg => ({
          content: msg.content || msg.text || '', // 从content读取，兼容旧的text字段
          text: msg.content || msg.text || '', // 兼容旧字段
          role: msg.role || msg.sender || 'assistant', // 从role读取，兼容旧的sender字段，默认为assistant
          sender: msg.role || msg.sender || 'assistant', // 兼容旧字段
          timestamp: msg.timestamp ? new Date(msg.timestamp) : new Date(), // 使用API返回的原始时间戳
          sessionId: sessionId, // 确保每条消息都有正确的会话ID
          messageId: msg.messageId || msg.id,
          sequence: msg.sequence, // 添加sequence字段
          metadata: msg.metadata, // 添加metadata字段
          offline: false,
          originalTimestamp: msg.timestamp || null // 保存原始时间戳字符串，用于确保显示数据库中的实际时间
        }));
        
        // 更新状态
        treeholeState.currentSessionId = sessionId;
        
        // 更新消息列表为当前会话的历史消息（即使为空）
        // 这确保切换到新会话时，消息列表会正确更新
        treeholeState.messages = processedMessages;
        
        if (processedMessages.length > 0) {
          console.log(`成功加载会话 ${sessionId} 的 ${processedMessages.length} 条历史消息`);
        } else {
          console.log(`会话 ${sessionId} 没有历史消息`);
        }
        
        // 保存到本地存储
        saveToLocalStorage({
          sessions: treeholeState.sessions,
          currentSessionId: treeholeState.currentSessionId,
          messages: treeholeState.messages
        });
        
        // 如果没有消息，添加一个欢迎消息
        if (treeholeState.messages.length === 0) {
          treeholeState.messages.push({
          content: '欢迎回来！继续我们的对话吧。',
          text: '欢迎回来！继续我们的对话吧。', // 兼容旧字段
          role: 'assistant',
          sender: 'assistant', // 兼容旧字段
          timestamp: new Date(),
          sessionId: sessionId
        });
        }
        
        return treeholeState.messages;
      } catch (apiError) {
        console.warn('获取会话历史失败，切换到离线模式:', apiError);
        
        treeholeState.currentSessionId = sessionId;
        treeholeState.messages = [{
          content: '无法加载历史消息，可能是网络问题或会话已过期。\n已为您启用离线模式，您可以继续交流。',
          text: '无法加载历史消息，可能是网络问题或会话已过期。\n已为您启用离线模式，您可以继续交流。', // 兼容旧字段
          role: 'assistant',
          sender: 'assistant', // 兼容旧字段
          timestamp: new Date(),
          sessionId,
          offline: true
        }];
        
        utils.showWarning('加载历史消息失败，已启用离线模式');
        return treeholeState.messages;
      }
    } catch (error) {
      console.error('获取会话历史过程中发生意外错误:', error);
      
      // 提供最后防线的错误处理
      const fallbackSessionId = sessionId || `temp_${Date.now()}`;
      treeholeState.currentSessionId = fallbackSessionId;
      treeholeState.messages = [{
        content: '加载会话历史时发生错误。\n已为您创建安全会话，我会继续为您提供支持。',
        text: '加载会话历史时发生错误。\n已为您创建安全会话，我会继续为您提供支持。', // 兼容旧字段
        role: 'assistant',
        sender: 'assistant',
        timestamp: new Date(),
        sessionId: fallbackSessionId,
        offline: true
      }];
      
      utils.showError('加载会话历史失败，但您仍可以使用离线模式');
      
      // 不抛出错误，返回本地消息
      return treeholeState.messages;
    } finally {
      treeholeState.loading = false;
    }
  },

  // 删除会话
  async deleteSession(sessionId) {
    try {
      // 验证会话ID - 现在支持数字类型
      if (!sessionId) {
        throw new Error('无效的会话ID');
      }
      
      // 检查用户是否已认证（仅检查，不阻止操作）
      const isAuthenticated = checkAuthStatus();
      let isApiSuccess = false;
      let apiErrorMessage = null;
      
      // 只有已认证用户才尝试API删除
      if (isAuthenticated) {
        try {
          await treeholeApi.deleteSession(sessionId);
          isApiSuccess = true;
          console.log('会话在服务器上删除成功');
        } catch (apiError) {
          // 处理认证过期错误
          if (apiError.response && apiError.response.status === 401) {
            utils.showWarning('认证已过期，请重新登录');
            apiErrorMessage = '认证已过期';
          } else {
            apiErrorMessage = apiError.message || '服务器删除失败';
            console.warn('服务器删除会话失败，将在本地删除:', apiError);
            // 显示服务器删除失败的具体原因
            utils.showWarning(`数据库删除失败: ${apiErrorMessage}。会话仅在本地删除。`);
          }
        }
      } else {
        // 未登录用户提示
        utils.showInfo('您未登录，会话仅在本地删除');
      }
      
      // 本地删除会话数据 - 无论API是否成功
      // 从会话列表中移除
      const sessionIndex = treeholeState.sessions.findIndex(s => s.sessionId === sessionId);
      if (sessionIndex !== -1) {
        treeholeState.sessions.splice(sessionIndex, 1);
      }
      
      // 从消息列表中移除该会话的所有消息
      treeholeState.messages = treeholeState.messages.filter(msg => msg.sessionId !== sessionId);
      
      // 如果删除的是当前会话，清空当前会话ID
      if (treeholeState.currentSessionId === sessionId) {
        treeholeState.currentSessionId = null;
      }
      
      // 保存到本地存储
      saveToLocalStorage({
        sessions: treeholeState.sessions,
        currentSessionId: treeholeState.currentSessionId,
        messages: treeholeState.messages
      });
      
      // 根据API调用结果显示不同的成功提示
      if (isApiSuccess) {
        utils.showSuccess('会话已成功删除（本地和服务器）');
      } else if (isAuthenticated && apiErrorMessage) {
        // 已登录但API删除失败的情况
        utils.showWarning('会话已在本地删除，但数据库删除失败');
      } else if (!isAuthenticated) {
        // 未登录的情况
        utils.showInfo('会话已在本地删除，登录后可同步到服务器');
      }
      
      // 返回操作结果状态和API调用状态
      return {
        success: true,
        apiDeleted: isApiSuccess,
        message: isApiSuccess ? '会话已完全删除' : '会话仅在本地删除'
      };
    } catch (error) {
      const errorMessage = utils.handleApiError(error, '删除会话失败');
      console.error('删除会话失败:', errorMessage);
      
      // 错误处理：保持会话在列表中并显示错误信息
      utils.showError(`删除会话失败: ${errorMessage}`);
      
      // 返回详细的错误状态
      return {
        success: false,
        apiDeleted: false,
        message: errorMessage,
        error: error
      };
    }
  },

  // 获取快速回复
  async getQuickReplies() {
    try {
      // 默认快速回复选项
      const defaultQuickReplies = [
        '我最近感觉压力很大',
        '我和朋友吵架了',
        '我对未来感到迷茫',
        '我需要一些建议',
        '我想找人倾诉',
        '我感到焦虑不安'
      ];
      
      // 检查认证状态
      if (!checkAuthStatus()) {
        console.warn('用户未认证，使用默认快速回复');
        treeholeState.quickReplies = defaultQuickReplies;
        return treeholeState.quickReplies;
      }
      
      // 尝试从服务器获取快速回复
      if (treeholeState.isOnline) {
        try {
          const response = await treeholeApi.getQuickReplies();
          
          // 确保response是数组且不为空
          if (Array.isArray(response) && response.length > 0) {
            treeholeState.quickReplies = response;
            return treeholeState.quickReplies;
          } else {
            console.warn('获取的快速回复为空或格式错误，使用默认回复');
          }
        } catch (apiError) {
          console.warn('获取快速回复失败，使用默认回复:', apiError);
        }
      }
      
      // 如果在线获取失败或离线，使用默认快速回复
      treeholeState.quickReplies = defaultQuickReplies;
      return treeholeState.quickReplies;
    } catch (error) {
      console.error('获取快速回复过程中发生意外错误:', error);
      
      // 设置安全的默认快速回复
      treeholeState.quickReplies = [
        '我想找人聊聊',
        '我需要一些建议',
        '今天心情不太好',
        '能听我倾诉吗'
      ];
      return treeholeState.quickReplies;
    }
  },

  // 清空当前会话消息
  clearMessages() {
    treeholeState.messages = [];
  },

  // 切换会话
  async switchSession(sessionId) {
    if (!sessionId) {
      throw new Error('会话ID不能为空');
    }
    
    // 检查会话是否存在于本地
    const sessionExists = treeholeState.sessions.some(s => s.sessionId === sessionId);
    if (!sessionExists) {
      // 如果本地没有该会话，尝试从本地存储中恢复
      const savedData = loadFromLocalStorage();
      if (savedData && savedData.sessions) {
        const savedSession = savedData.sessions.find(s => s.sessionId === sessionId);
        if (savedSession && !treeholeState.sessions.find(s => s.sessionId === sessionId)) {
          treeholeState.sessions.push(savedSession);
        }
      }
    }
    
    // 更新当前会话ID
    treeholeState.currentSessionId = sessionId;
    
    // 保存当前会话ID到本地存储
    saveToLocalStorage({
      sessions: treeholeState.sessions,
      currentSessionId: treeholeState.currentSessionId,
      messages: treeholeState.messages
    });
    
    // 获取会话历史记录
    try {
      await this.getSessionHistory(sessionId);
      return true;
    } catch (error) {
      // 即使getSessionHistory抛出错误，这里也捕获并处理
      console.error('切换会话过程中发生错误:', error);
      
      // 创建临时会话作为最终后备
      const tempSessionId = `temp_${Date.now()}`;
      treeholeState.currentSessionId = tempSessionId;
      treeholeState.messages = [{
        content: '切换会话失败，已为您创建临时会话。\n我会继续在这里倾听您的想法和感受。',
        text: '切换会话失败，已为您创建临时会话。\n我会继续在这里倾听您的想法和感受。', // 兼容旧字段
        role: 'assistant',
        sender: 'assistant', // 兼容旧字段
        timestamp: new Date(),
        sessionId: tempSessionId,
        offline: true
      }];
      
      utils.showWarning('已创建临时会话');
      // 返回成功，确保用户体验不中断
      return true;
    }
  }
};

// 添加显示信息提示的方法（如果utils中没有）
const showInfo = (message) => {
  if (utils.showInfo) {
    utils.showInfo(message);
  } else {
    // 回退到使用showToast或简单的console.log
    if (utils.showToast) {
      utils.showToast(message, 'info');
    } else {
      console.log('Info:', message);
    }
  }
};

// 将showInfo添加到utils对象中，供store内部使用
utils.showInfo = showInfo;

// 创建提供器
const TreeholeStoreSymbol = Symbol('TreeholeStore');

export function provideTreeholeStore(app) {
  app.provide(TreeholeStoreSymbol, treeholeStore);
  
  // 检查认证状态（不再尝试挂载应用，挂载已在main.js中完成）
  if (!utils.isAuthenticated()) {
    console.log('用户未认证，心灵树洞功能将以受限模式运行');
  }
}

// 创建注入器
export function useTreeholeStore() {
  const store = inject(TreeholeStoreSymbol);
  if (!store) {
    console.error('TreeholeStore未正确提供，请确保在应用入口处调用provideTreeholeStore');
    // 返回一个安全的空store，避免应用崩溃
    return {
      state: {
        config: {},
        sessions: [],
        currentSessionId: null,
        messages: [],
        quickReplies: [],
        loading: false,
        isOnline: navigator.onLine
      },
      async createSession() {
        const tempId = `temp_${Date.now()}`;
        return { sessionId: tempId, offline: true };
      },
      async sendMessage() {
        console.warn('TreeholeStore未正确初始化，无法发送消息');
        return null;
      }
    };
  }
  return store;
}

export default treeholeStore;