// src/api/treehole.js
import { apiClient, utils } from './index';

/**
 * 心灵树洞API模块
 * 提供与心灵树洞功能相关的所有API调用
 */

// 获取树洞配置
export const getTreeholeConfig = async () => {
  try {
    // 检查认证状态
    if (!utils.isAuthenticated()) {
      throw new Error('用户未认证');
    }
    
    const response = await apiClient.get('/treehole/config');
    return response.data;
  } catch (error) {
    throw utils.handleApiError(error, '获取树洞配置失败');
  }
};

// 创建会话
export const createSession = async () => {
  try {
    // 检查认证状态
    if (!utils.isAuthenticated()) {
      throw new Error('用户未认证');
    }
    
    const response = await apiClient.post('/treehole/session', {}, {
      headers: {
        'Content-Type': 'application/json'
      }
    });
    return response.data;
  } catch (error) {
    throw utils.handleApiError(error, '创建会话失败');
  }
};

// 获取会话列表
export const getSessions = async () => {
  try {
    // 检查认证状态
    if (!utils.isAuthenticated()) {
      throw new Error('用户未认证');
    }
    
    const response = await apiClient.get('/treehole/sessions');
    return response.data;
  } catch (error) {
    throw utils.handleApiError(error, '获取会话列表失败');
  }
};

// 与AI聊天
export const chatWithAI = async ({ message, sessionId }) => {
  try {
    // 检查认证状态
    if (!utils.isAuthenticated()) {
      throw new Error('用户未认证');
    }
    
    const response = await apiClient.post('/treehole/chat', {
      message,
      sessionId
    }, {
      headers: {
        'Content-Type': 'application/json'
      }
    });
    return response.data;
  } catch (error) {
    throw utils.handleApiError(error, '发送消息失败');
  }
};

// 获取会话历史
export const getSessionHistory = async (sessionId) => {
  try {
    // 检查认证状态
    if (!utils.isAuthenticated()) {
      throw new Error('用户未认证');
    }
    
    if (!sessionId) {
      throw new Error('无效的会话ID');
    }
    
    const response = await apiClient.get(`/treehole/session/${sessionId}`);
    return response.data;
  } catch (error) {
    throw utils.handleApiError(error, '获取会话历史失败');
  }
};

// 删除会话
export const deleteSession = async (sessionId) => {
  try {
    // 检查认证状态
    if (!utils.isAuthenticated()) {
      throw new Error('用户未认证');
    }
    
    if (!sessionId) {
      throw new Error('无效的会话ID');
    }
    
    const response = await apiClient.delete(`/treehole/session/${sessionId}`);
    return response.data;
  } catch (error) {
    throw utils.handleApiError(error, '删除会话失败');
  }
};

// 获取快速回复建议
export const getQuickReplies = async (message) => {
  try {
    // 检查认证状态
    if (!utils.isAuthenticated()) {
      throw new Error('用户未认证');
    }
    
    const response = await apiClient.post('/treehole/quick-replies', {
      message
    });
    return response.data;
  } catch (error) {
    // 快速回复失败不抛出异常，返回空数组
    console.warn('获取快速回复失败:', error);
    return { replies: [] };
  }
};