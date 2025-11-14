// 导入axios用于HTTP请求
import axios from 'axios';

// 创建axios实例
const apiClient = axios.create({
  baseURL: '/api', // 设置基础URL为'/api'，这样请求会自动加上这个前缀
  timeout: 20000, // 请求超时时间增加到20秒
  headers: {
    'Content-Type': 'application/json'
  },
  withCredentials: true // 允许携带cookie，让后端管理会话
});

// 心理测试相关API
const testAPI = {
  // 获取测试题目
  getQuestions: async (testType) => {
    try {
      const response = await apiClient.get(`/${testType}-test/questions`);
      return response.data;
    } catch (error) {
      console.error(`获取${testType}测试题目失败:`, error);
      throw error;
    }
  },
  
  // 提交测试答案
  submitTest: async (testType, answers) => {
    try {
      const response = await apiClient.post(`/${testType}-test/submit`, {
        testType,
        answers
      });
      return response.data;
    } catch (error) {
      console.error(`提交${testType}测试答案失败:`, error);
      throw error;
    }
  },
  
  // 获取测试结果
  getTestResult: async (testId) => {
    try {
      const response = await apiClient.get(`/test-results/${testId}`);
      return response.data;
    } catch (error) {
      console.error('获取测试结果失败:', error);
      throw error;
    }
  },
  
  // 获取用户的测试历史
  getTestHistory: async () => {
    try {
      const response = await apiClient.get('/test-history');
      return response.data;
    } catch (error) {
      console.error('获取测试历史失败:', error);
      throw error;
    }
  },
  
  // 保存测试结果
  saveTestResult: async (resultData) => {
    try {
      const response = await apiClient.post('/test-results/save', resultData);
      return response.data;
    } catch (error) {
      console.error('保存测试结果失败:', error);
      throw error;
    }
  }
};

// 验证token格式是否正确且有效
const isValidTokenFormat = (token) => {
  // 基础验证：token应该是非空字符串
  if (!token || typeof token !== 'string' || token.trim() === '') {
    return false;
  }
  
  // 提取实际的token值（去掉Bearer前缀）
  const actualToken = token.startsWith('Bearer ') ? token.substring(7) : token;
  
  // 在开发环境中允许使用测试token
  if (import.meta.env.DEV && (actualToken.includes('mock_token_') || actualToken.includes('test_token_'))) {
    console.log('开发环境：允许使用测试token进行调试');
    return true; // 开发环境允许测试token
  }
  
  // 检查是否是有效的JWT格式（包含三个部分，用点分隔）
  // 但不强制要求，以便兼容不同的认证方案
  const tokenParts = actualToken.split('.');
  if (tokenParts.length !== 3) {
    console.log('Token格式不是标准JWT，但仍尝试使用');
    // 不返回false，而是继续使用该token
  }
  
  return true;
};

// 检查token是否过期
const isTokenExpired = (token) => {
  // 开发环境中对测试token跳过严格的过期检查
  const actualToken = token.startsWith('Bearer ') ? token.substring(7) : token;
  if (import.meta.env.DEV && (actualToken.includes('mock_token_') || actualToken.includes('test_token_'))) {
    console.log('开发环境：对测试token跳过过期检查');
    return false; // 测试token永不过期
  }
  
  try {
    // 提取实际的token值
    
    // 解码JWT的payload部分
    const base64Url = actualToken.split('.')[1];
    // 如果token格式不是标准JWT，跳过过期检查
    if (!base64Url) {
      console.log('Token格式不是标准JWT，跳过过期检查');
      return false;
    }
    
    try {
      const base64 = base64Url.replace(/-/g, '+').replace(/_/g, '/');
      const payload = JSON.parse(window.atob(base64));
      
      // 检查exp字段是否存在
      if (!payload.exp) {
        console.log('Token中没有过期时间信息');
        return false;
      }
      
      // 检查是否过期
      const now = Date.now() / 1000; // 当前时间戳（秒）
      const isExpired = payload.exp < now;
      
      if (isExpired) {
        console.log('Token已过期');
      }
      
      return isExpired;
    } catch (decodeError) {
      console.log('无法解码token，跳过过期检查');
      return false;
    }
  } catch (error) {
    console.log('检查token过期时间时出错:', error);
    // 在开发环境中，如果解析失败，我们认为token未过期
    return false;
  }
};

// 请求拦截器 - 添加Bearer token认证
apiClient.interceptors.request.use(
  config => {
    // 详细记录请求信息
    console.log('API请求:', {
      url: config.url,
      method: config.method,
      baseURL: config.baseURL,
      hasData: !!config.data,
      requestType: config.url.includes('community') ? '社区相关' : '其他请求'
    });
    
    // 首先检查用户是否已认证（使用isAuthenticated函数检查token是否存在且有效）
    if (isAuthenticated()) {
      // 只在用户已认证的情况下获取token
      let token = localStorage.getItem('authToken') || localStorage.getItem('token');
      
      // 清理token格式，移除任何可能重复的Bearer前缀
      token = token.trim();
      // 关键修复：根据后端要求，判断是否需要添加Bearer前缀
      // 开发环境下允许更灵活的token格式处理
      let formattedToken;
      
      // 检查token是否已经包含Bearer前缀
      if (token.startsWith('Bearer ')) {
        formattedToken = token; // 已经有Bearer前缀，直接使用
      } else {
        // 后端可能不期望Bearer前缀，或者期望不同的格式
        // 这里做一个智能判断：如果token看起来像JWT（包含三个点分隔的部分），不添加Bearer前缀
        if (token.split('.').length === 3 && token.length > 50) {
          formattedToken = token; // JWT格式，可能不需要Bearer前缀
        } else {
          formattedToken = token; // 简单token，直接使用
        }
      }
      
      // 确保headers对象存在
      if (!config.headers) {
        config.headers = {};
      }
      
      // 设置Authorization头
      config.headers['Authorization'] = formattedToken;
      
      console.log('请求拦截器 - 添加token:', {
        hasToken: true,
        tokenSource: localStorage.getItem('authToken') ? 'authToken' : 'token',
        tokenFormatted: formattedToken.startsWith('Bearer '),
        tokenLength: formattedToken.length,
        // 不记录完整token内容，只记录前10位和后10位作为标识
        tokenPreview: formattedToken.length > 20 ? 
          formattedToken.substring(0, 10) + '...' + formattedToken.substring(formattedToken.length - 10) : 
          formattedToken
      });
    } else {
      console.warn('请求拦截器 - 用户未认证或token无效，不添加token到请求头');
    }
    
    return config;
  },
  error => {
    console.error('请求拦截器错误:', error);
    return Promise.reject(error);
  }
);

// 响应拦截器 - 处理通用错误和登录响应
apiClient.interceptors.response.use(
  response => {
    // 详细记录响应信息
    console.log('API响应成功:', {
      url: response.config?.url,
      status: response.status,
      statusText: response.statusText,
      hasData: !!response.data,
      responseType: response.config?.responseType || 'json'
    });
    
    // 统一处理响应数据
    const res = response.data;
    console.log('响应数据结构:', {
      hasCode: res.code !== undefined,
      hasData: res.data !== undefined,
      hasMessage: res.message !== undefined,
      hasSuccess: res.success !== undefined
    });
    
    // 处理常见的响应格式
    if (res.token || res.data || res.code === 200 || res.code === 0 || response.status === 200) {
      return res;
    }
    
    return res;
  },
  error => {
    // 详细记录错误信息
    console.error('=== API响应错误详情 ===');
    
    if (!error.response) {
      // 无响应情况（网络错误、超时等）
      console.error('无响应错误:', {
        message: error.message,
        code: error.code,
        requestConfig: error.config,
        isNetworkError: true
      });
      
      // 区分超时错误和其他网络错误
      if (error.code === 'ECONNABORTED') {
        error.message = '请求超时，请稍后重试';
      } else {
        error.message = '网络连接失败，请检查网络设置';
      }
    } else {
      // 有响应的错误
      const { response } = error;
      console.error('HTTP错误响应:', {
        url: response.config?.url,
        method: response.config?.method,
        status: response.status,
        statusText: response.statusText,
        responseData: response.data,
        headers: response.headers
      });
      
      // 详细记录Authorization头
      if (error.config?.headers?.Authorization) {
        console.error('请求时使用的Authorization头:', error.config.headers.Authorization);
      }
      
      const errorMessage = response.data?.message || `请求失败 (${response.status})`;
      error.message = errorMessage;
    }
    
    // 处理401未授权错误
    if (error.response && error.response.status === 401) {
      console.error('401未授权错误 - 详细分析:');
      console.error('- 当前localStorage中的token:', localStorage.getItem('authToken') ? '存在' : '不存在');
      console.error('- 请求URL:', error.config?.url);
      console.error('- 后端返回的错误数据:', error.response.data);
      
      // 触发重新登录提示
      const event = new CustomEvent('show-toast', {
        detail: { message: '认证失败，请检查登录状态', type: 'error' }
      });
      window.dispatchEvent(event);
    }
    
    // 处理其他常见错误
    const status = error.response?.status;
    switch (status) {
      case 403:
        error.message = '没有权限执行此操作';
        break;
      case 404:
        error.message = '请求的资源不存在';
        break;
      case 500:
        error.message = '服务器内部错误，请稍后再试';
        break;
    }
    
    return Promise.reject(error);
  }
);

// 尝试捕获全局WebSocket错误，避免控制台被错误信息淹没
if (typeof window !== 'undefined') {
  // 监听全局错误事件
  window.addEventListener('error', (event) => {
    if (event.message && event.message.includes('WebSocket')) {
      // 静默处理WebSocket错误，不抛出到控制台
      console.debug('捕获到WebSocket错误:', event.message);
      // 可选：阻止事件冒泡
      // event.preventDefault();
    }
  }, { passive: true });
  
  // 监听全局unhandledrejection事件
  window.addEventListener('unhandledrejection', (event) => {
    if (event.reason && event.reason.message && event.reason.message.includes('WebSocket')) {
      console.debug('捕获到未处理的WebSocket拒绝:', event.reason.message);
      // 可选：阻止事件冒泡
      // event.preventDefault();
    }
  }, { passive: true });
}

// 定义API服务
// 管理员相关API
const adminAPI = {
  // 管理员权限验证
  verifyAdmin: async () => {
    try {
      const response = await apiClient.get('/admin/verify');
      return response.data;
    } catch (error) {
      console.error('验证管理员权限失败:', error);
      throw error;
    }
  },
  
  // 获取帖子列表（支持分页、筛选、排序）
  getPosts: async (params) => {
    try {
      const response = await apiClient.get('/admin/posts', { params });
      return response.data;
    } catch (error) {
      utils.handleApiError(error, '获取帖子列表失败');
      throw error;
    }
  },
  
  // 获取待审核数量
  getPendingCount: async () => {
    try {
      const response = await apiClient.get('/admin/posts/pending-count');
      return response.data;
    } catch (error) {
      utils.handleApiError(error, '获取待审核数量失败');
      throw error;
    }
  },
  
  // 审核帖子
  reviewPost: async (postId, data) => {
    try {
      // 将PUT方法改为POST方法
      const response = await apiClient.post(`/admin/posts/${postId}/review`, data);
      return response.data;
    } catch (error) {
      utils.handleApiError(error, '审核帖子失败');
      throw error;
    }
  },
  
  // 删除帖子
  deletePost: async (postId) => {
    try {
      const response = await apiClient.delete(`/admin/posts/${postId}`);
      return response.data;
    } catch (error) {
      utils.handleApiError(error, '删除帖子失败');
      throw error;
    }
  },
  
  // 获取用户列表
  getUsers: async (params) => {
    try {
      const response = await apiClient.get('/admin/users', { params });
      return response.data;
    } catch (error) {
      utils.handleApiError(error, '获取用户列表失败');
      throw error;
    }
  },
  
  // 切换用户状态
  toggleUserStatus: async (userId, status) => {
    try {
      const response = await apiClient.put(`/admin/users/${userId}/status`, { status });
      return response.data;
    } catch (error) {
      utils.handleApiError(error, '更新用户状态失败');
      throw error;
    }
  },
  
  // 更新用户角色
  updateUserRole: async (userId, role) => {
    try {
      const response = await apiClient.put(`/admin/users/${userId}/role`, { role });
      return response.data;
    } catch (error) {
      utils.handleApiError(error, '更新用户角色失败');
      throw error;
    }
  },
  
  // 获取平台统计数据
  getAnalyticsOverview: async () => {
    try {
      const response = await apiClient.get('/admin/analytics/overview');
      return response.data; // 返回response.data，与其他API方法保持一致
    } catch (error) {
      utils.handleApiError(error, '获取统计数据失败');
      throw error;
    }
  },
  
  // 获取用户增长趋势
  getUserGrowth: async (params) => {
    try {
      const response = await apiClient.get('/admin/analytics/user-growth', { params });
      return response; // 返回完整的response对象
    } catch (error) {
      utils.handleApiError(error, '获取用户增长趋势失败');
      throw error;
    }
  },
  
  // 获取帖子发布趋势
  getPostTrends: async (params) => {
    try {
      const response = await apiClient.get('/admin/analytics/post-trends', { params });
      return response; // 返回完整的response对象
    } catch (error) {
      utils.handleApiError(error, '获取帖子发布趋势失败');
      throw error;
    }
  },
  
  // 获取系统设置
  getSettings: async () => {
    try {
      const response = await apiClient.get('/admin/settings');
      return response.data;
    } catch (error) {
      utils.handleApiError(error, '获取系统设置失败');
      throw error;
    }
  },
  
  // 更新系统设置
  updateSettings: async (settings) => {
    try {
      const response = await apiClient.put('/admin/settings', settings);
      return response.data;
    } catch (error) {
      utils.handleApiError(error, '更新系统设置失败');
      throw error;
    }
  },
  
  // 获取操作日志
  getLogs: async (params) => {
    try {
      const response = await apiClient.get('/admin/logs', { params });
      return response.data;
    } catch (error) {
      utils.handleApiError(error, '获取操作日志失败');
      throw error;
    }
  }
};

// 社区相关API
const communityAPI = {
  // 获取帖子列表
  getPosts: (params) => apiClient.get('/community/posts', { params }),
  
  // 获取待审核帖子
  getPendingPosts: (params) => apiClient.get('/community/posts/pending', { params }),
  
  // 审核帖子
  reviewPost: (postId, data) => apiClient.post(`/community/posts/${postId}/review`, data),
  
  // 创建帖子 - 接受三个独立参数
  publishPost: async (content, images = [], tags = []) => {
    try {
      // 验证必要字段
      if (!content || typeof content !== 'string' || content.trim() === '') {
        throw new Error('帖子内容不能为空');
      }
      
      // 构建请求数据，确保只包含需要的字段
      const postData = {
        content: content.trim(),
        images: images && Array.isArray(images) ? images : [],
        tags: tags && Array.isArray(tags) ? tags : [],
        status: 'pending' // 新发布的帖子默认状态为待审核
      };
      
      console.log('发布帖子请求数据:', postData);
      
      const response = await apiClient.post('/community/posts', postData);
      
      // 验证响应
      if (response.code === 0 || response.code === 200 || response.success) {
        console.log('发布帖子成功:', response.data || response);
        return response;
      } else {
        const errorMessage = response.message || '发布帖子失败';
        console.error('发布帖子失败:', errorMessage);
        throw new Error(errorMessage);
      }
    } catch (error) {
      // 使用统一的错误处理
      utils.handleApiError(error, {
        showMessage: true,
        errorType: '发布帖子'
      });
      throw error;
    }
  },
  
  // 更新帖子
  updatePost: (id, data) => apiClient.put(`/community/posts/${id}`, data),
  
  // 删除帖子
  deletePost: (id) => apiClient.delete(`/community/posts/${id}`),
  
  // 点赞帖子
  toggleLike: async (id) => {
    try {
      const response = await apiClient.post(`/community/posts/${id}/like`);
      console.log('点赞API响应:', response.data);
      
      // 验证响应
      if (response.code === 0 || response.code === 200 || response.success) {
        // 返回包含新点赞状态和数量的对象
        return {
          ...response,
          data: {
            ...(response.data || {}),
            // 确保返回isLiked状态和likes数量
            isLiked: response.data?.isLiked !== undefined ? response.data.isLiked : 
                     response.code === 0 || response.success,
            likes: response.data?.likes !== undefined ? response.data.likes : 
                   parseInt(response.data?.count) || 0
          }
        };
      } else {
        const errorMessage = response.message || '点赞操作失败';
        console.error('点赞失败:', errorMessage);
        throw new Error(errorMessage);
      }
    } catch (error) {
      // 使用统一的错误处理
      utils.handleApiError(error, {
        showMessage: true,
        errorType: '点赞操作'
      });
      throw error;
    }
  },
  
  // 收藏帖子
  toggleSave: (id) => apiClient.post(`/community/posts/${id}/save`),
  
  // 获取评论列表
  getComments: (postId) => apiClient.get(`/community/posts/${postId}/comments`),
  
  // 添加评论
  addComment: (postId, data) => apiClient.post(`/community/posts/${postId}/comments`, data),
  
  // 点赞评论
  likeComment: (id) => apiClient.post(`/community/comments/${id}/like`),
  
  // 关注/取消关注用户
  toggleFollow: (id) => apiClient.post(`/community/users/${id}/follow`),
  
  // 获取推荐用户
  getRecommendedUsers: () => apiClient.get('/community/users/recommended'),
  
  // 获取热门话题
  getHotTopics: () => apiClient.get('/community/topics/hot'),
  
  // 获取所有帖子（管理员使用）
  getAllPosts: (params) => apiClient.get('/community/posts/all', { params })

};

// 保存token到localStorage
const saveToken = (token) => {
  if (token) {
    // 清理token格式，移除任何可能重复的Bearer前缀
    const tokenWithoutBearer = token.startsWith('Bearer ') ? token.substring(7).trim() : token;
    localStorage.setItem('token', tokenWithoutBearer);
    console.log('Token已保存到localStorage');
    return true;
  }
  return false;
};

// 从localStorage获取token
const getToken = () => {
  const token = localStorage.getItem('token');
  return token ? `Bearer ${token}` : null;
};

// 清除token
const clearToken = () => {
  localStorage.removeItem('token');
  localStorage.removeItem('authToken'); // 兼容其他可能的token存储
  console.log('Token已从localStorage清除');
};

// 检查用户是否已登录
const isAuthenticated = () => {
  // 同时检查authToken和token两个可能的键名
  const token = localStorage.getItem('authToken') || localStorage.getItem('token');
  return token && !isTokenExpired(token);
};

// 全局错误处理工具函数
const handleApiError = (error, options = {}) => {
  // 支持旧格式参数（字符串）和新格式参数（对象）
  const customMessage = typeof options === 'string' ? options : options?.showMessage || '操作失败';
  const showMessage = typeof options === 'string' || options?.showMessage !== false;
  const navigateToLogin = options?.navigateToLogin;
  
  console.error('API错误处理:', { error, options });
  
  let errorMessage = customMessage;
  
  // 分类处理不同类型的错误
  if (!error.response) {
    // 网络错误、请求被取消等
    // 区分超时和其他网络错误
    if (error.code === 'ECONNABORTED') {
      errorMessage = '请求超时，请稍后重试';
    } else {
      errorMessage = '网络连接失败，请检查您的网络设置';
    }
    
    // 触发全局提示事件，但不抛出错误，允许调用方决定如何处理
    if (showMessage) {
      showToast(errorMessage, 'error');
    }
    return errorMessage;
  }
  
  const { response } = error;
  
  // 根据状态码处理特定错误
  switch (response.status) {
    case 401:
      // 未授权错误 - 增加更多调试信息
      console.error('===== 401错误详细分析 =====');
      console.error('- 请求URL:', response.config?.url);
      console.error('- 请求方法:', response.config?.method);
      console.error('- 请求是否携带Token:', !!response.config?.headers?.Authorization);
      console.error('- 当前localStorage中的authToken:', localStorage.getItem('authToken') ? '存在' : '不存在');
      console.error('- 当前localStorage中的token:', localStorage.getItem('token') ? '存在' : '不存在');
      console.error('- 后端返回数据:', response.data);
      console.error('===========================');
      
      // 未授权错误
      errorMessage = '认证失败，请检查登录状态';
      
      // 触发全局提示事件
      if (showMessage) {
        showToast(errorMessage, 'error');
      }
      
      const token = localStorage.getItem('authToken') || localStorage.getItem('token');
      const hasValidToken = token && !isTokenExpired(token);
      
      // 改进的token处理逻辑：
      // 1. 如果没有token或token已过期，清除并导航到登录页
      // 2. 如果有有效token但后端仍然返回401，不要立即清除token
      //    这可能是后端问题或token格式问题，而不是token无效
      if (!hasValidToken) {
        console.log('没有有效token，清除并准备跳转到登录页');
        clearToken();
      } else {
        console.log('存在有效token但后端返回401，可能是后端问题或token格式问题，不立即清除token');
      }
      
      // 使用自定义的导航函数或默认导航
      // 只有在没有有效token时才导航到登录页
      if (navigateToLogin && typeof navigateToLogin === 'function') {
        // 如果提供了自定义导航函数，只有在没有有效token时才调用它
        if (!hasValidToken) {
          console.log('使用自定义导航到登录页');
          navigateToLogin();
        }
      } else if (!hasValidToken) {
        console.log('没有有效token，准备跳转到登录页');
        // 延迟重定向到登录页面，让用户看到提示
        setTimeout(() => {
          window.location.href = '/login';
        }, 1500);
      }
      break;
      
    case 403:
      // 禁止访问
      errorMessage = '您没有权限执行此操作';
      showToast(errorMessage, 'error');
      break;
      
    case 404:
      // 资源不存在
      errorMessage = '请求的资源不存在';
      showToast(errorMessage, 'error');
      break;
      
    case 500:
    case 502:
    case 503:
    case 504:
      // 服务器错误
      errorMessage = '服务器暂时不可用，请稍后再试';
      showToast(errorMessage, 'error');
      break;
      
    default:
      // 其他错误，尝试从响应中获取错误消息 - 确保不会尝试读取null对象的属性
      errorMessage = (response.data && response.data.message) || 
                    (response.data && response.data.error) || 
                    (response.data && typeof response.data.code !== 'undefined' ? `错误代码: ${response.data.code}` : null) ||
                    error.message || 
                    customMessage;
      
      showToast(errorMessage, 'error');
  }
  
  throw new Error(errorMessage);
};

// 显示提示消息的工具函数
const showToast = (message, type = 'info') => {
  try {
    // 尝试使用自定义事件触发全局提示
    const event = new CustomEvent('show-toast', {
      detail: { message, type }
    });
    window.dispatchEvent(event);
    
    // 降级处理：如果没有自定义提示组件，使用浏览器原生alert
  } catch (e) {
    console.warn('无法触发自定义提示事件，使用原生alert');
    if (type === 'error') {
      alert(`错误: ${message}`);
    } else {
      alert(message);
    }
  }
};

// 显示成功提示
const showSuccess = (message) => {
  showToast(message, 'success');
};

// 显示错误提示
const showError = (message) => {
  showToast(message, 'error');
};

// 显示警告提示
const showWarning = (message) => {
  showToast(message, 'warning');
};

// 创建utils对象以便命名导出
const utils = {
  isAuthenticated,
  getToken,
  saveToken,
  clearToken,
  handleApiError,
  showToast,
  showSuccess,
  showError,
  showWarning
};

// 导入treehole相关API
import * as treeholeAPI from './treehole';

// 命名导出utils
// 默认导出
export default {
  // POST请求方法
  post: (url, data) => apiClient.post(url, data),
  
  // GET请求方法
  get: (url, params) => apiClient.get(url, { params }),
  
  // PUT请求方法
  put: (url, data) => apiClient.put(url, data),
  
  // DELETE请求方法
  delete: (url) => apiClient.delete(url),
  
  // 工具方法
  utils: utils,
  
  // 认证相关方法
  auth: {
    // 登录方法
    login: async (identifier, password) => {
      try {
        console.log('执行登录请求:', { identifier });
        const response = await apiClient.post('/auth/login', {
          identifier,
          password
        });
        
        // 处理登录响应
        if (response.code === 0 || response.code === 200) {
          // 尝试从不同的响应结构中获取token
          const token = response.data?.token || response.token;
          
          if (token) {
            // 保存token
            saveToken(token);
            console.log('登录成功，token已保存');
            return response;
          } else {
            console.error('登录响应中未找到token');
            throw new Error('登录失败：未获取到认证信息');
          }
        } else {
          const errorMessage = response.message || '登录失败';
          console.error('登录失败:', errorMessage);
          throw new Error(errorMessage);
        }
      } catch (error) {
        console.error('登录请求发生错误:', error);
        // 增强错误处理
        if (!error.response) {
          throw new Error('网络连接失败，请检查网络设置');
        }
        
        const errorMessage = error.response.data?.message || error.message || '登录失败，请稍后重试';
        throw new Error(errorMessage);
      }
    },
    
    // 注册方法
    register: async (userData) => {
      try {
        const response = await apiClient.post('/auth/register', userData);
        
        if (response.code === 0 || response.code === 200) {
          return response;
        } else {
          throw new Error(response.message || '注册失败');
        }
      } catch (error) {
        console.error('注册请求发生错误:', error);
        const errorMessage = error.response?.data?.message || error.message || '注册失败，请稍后重试';
        throw new Error(errorMessage);
      }
    }
  },
  
  // 登出方法
  logout: async () => {
    try {
      // 调用后端登出接口
      const response = await apiClient.post('/auth/logout', {});
      
      // 清除本地token
      clearToken();
      
      // 统一处理后端返回格式 {code, message, data}
      if (response.data) {
        // 如果返回数据在data属性中
        return response.data;
      } else if (response.code !== undefined) {
        // 如果直接返回了标准格式
        return response;
      }
      
      // 返回默认成功响应
      return { code: 0, message: '登出成功', data: null };
    } catch (error) {
      // 增强错误处理
      console.error('登出请求失败:', error);
      
      // 检查是否是网络错误或服务器不可用
      if (!error.response) {
        // 网络错误的情况下，前端仍然可以继续登出流程
        throw new Error('无法连接到服务器，但您仍可从前端登出');
      }
      
      // 检查是否是404错误（接口不存在）
      if (error.response.status === 404) {
        throw new Error('登出接口不存在，请联系管理员');
      }
      
      // 其他错误情况下，尝试获取服务器返回的错误信息
      const errorMessage = error.response.data?.message || '登出失败，请稍后重试';
      throw new Error(errorMessage);
    }
  },
  
  // 心理测试相关API
  test: testAPI,
  
  // 社区相关API
  community: communityAPI,
  
  // 树洞相关API
  treehole: treeholeAPI,
  
  // 管理员相关API
  admin: adminAPI,
  
  // 日记相关方法
  diary: {
    // 保存日记
    save: (data) => apiClient.post('/diary', data),
    
    // 获取最近日记
    getRecent: (limit = 10) => apiClient.get('/diary/recent', { params: { limit } }),
    
    // 获取心情统计
    getStats: () => apiClient.get('/diary/stats'),
    
    // 获取特定日期日记
    getByDate: (date) => apiClient.get('/diary', { params: { date } }),
    
    // 更新日记
    update: (id, data) => apiClient.put(`/diary/${id}`, data),
    
    // 删除日记
    delete: (id) => apiClient.delete(`/diary/${id}`),
    
    // 获取草稿列表
    getDrafts: () => apiClient.get('/diary/drafts')
  },
  
  // 文件上传方法
  upload: {
    // 上传图片
    image: async (formData) => {
      try {
        // 调试信息：打印formData内容
        console.log('准备上传图片，FormData内容检查:');
        for (let [key, value] of formData.entries()) {
          console.log(`- ${key}:`, value instanceof File ? `${value.name} (${value.size} bytes, ${value.type})` : value);
        }
        
        // 检查localStorage中是否有token
        const token = localStorage.getItem('authToken') || localStorage.getItem('token');
        console.log('图片上传前token检查:', {
          hasAuthToken: !!localStorage.getItem('authToken'),
          hasToken: !!localStorage.getItem('token'),
          isTokenExpired: token ? isTokenExpired(token) : true
        });
        
        // 不要手动设置Content-Type，让axios自动处理multipart/form-data边界
        const response = await apiClient.post('/upload/image', formData, {
          headers: {
            // 移除手动设置的Content-Type，让axios自动处理
          },
          // 添加上传进度监控
          onUploadProgress: (progressEvent) => {
            if (progressEvent.total) {
              const percentCompleted = Math.round((progressEvent.loaded * 100) / progressEvent.total);
              console.log(`图片上传进度: ${percentCompleted}%`);
            }
          }
        });
        
        console.log('图片上传成功，响应:', response.data);
        return response.data;
      } catch (error) {
        console.error('=== 图片上传失败详情 ===');
        console.error('- 错误类型:', error.name);
        console.error('- 错误消息:', error.message);
        
        if (error.response) {
          console.error('- 响应状态:', error.response.status);
          console.error('- 响应数据:', JSON.stringify(error.response.data));
          console.error('- 响应头:', error.response.headers);
          
          // 特别处理401错误，添加更多诊断信息
          if (error.response.status === 401) {
            console.error('上传图片时遇到401未授权错误:');
            console.error('- 请求URL:', error.config?.url);
            console.error('- 请求是否携带Authorization头:', !!error.config?.headers?.Authorization);
            console.error('- 当前localStorage中的token状态:', {
              authToken: localStorage.getItem('authToken') ? '存在' : '不存在',
              token: localStorage.getItem('token') ? '存在' : '不存在'
            });
          }
        } else if (error.request) {
          console.error('- 请求已发送但未收到响应:', error.request);
        } else {
          console.error('- 请求配置错误:', error.config);
        }
        
        // 使用我们修改过的handleApiError来处理错误
        // 这样可以避免在有有效token的情况下错误地跳转到登录页面
        utils.handleApiError(error, '图片上传失败');
        
        // 无论handleApiError是否成功处理，都重新抛出原始错误
        // 这样调用者（如CommunityView.vue中的publishPost函数）能够感知到上传失败
        throw error;
      }
    }
  }
};

// 命名导出utils和apiClient
export { utils, apiClient };