<template>
  <div class="diary-page">
    <NavBar />
    
    <!-- 顶部装饰 -->
    <div class="diary-header-decoration">
      <!-- 装饰性波浪效果 -->
      <div class="wave-shape"></div>
    </div>
    
    <main class="main-content">
      <!-- 左侧主内容区 -->
      <div class="diary-main">
        <!-- 日期显示 -->
        <div class="date-display">
          <div class="date-circle">
            <span class="date-day">{{ new Date().getDate() }}</span>
            <span class="date-month">{{ months[new Date().getMonth()] }}</span>
          </div>
          <div class="date-text">
            <h2 class="current-date">{{ formattedDate }}</h2>
            <span class="weekday">{{ weekday }}</span>
          </div>
          <button class="calendar-btn" @click="openCalendar">
            📅
            <span class="calendar-tooltip">选择日期</span>
          </button>
        </div>
        
        <!-- 心情选择卡片 -->
        <div class="card mood-card glass-effect">
          <h3 class="card-title">{{ cardEmojis.mood }} 今日心情</h3>
          <div class="mood-selector">
            <button 
              v-for="mood in moods" 
              :key="mood.id"
              :class="['mood-option', { active: selectedMood === mood.id }]"
              :style="selectedMood === mood.id ? { backgroundColor: mood.color, color: '#fff' } : { '--mood-color': mood.color }"
              @click="selectMood(mood.id)"
              :title="mood.name"
            >
              <div class="mood-icon">
                {{ mood.icon }}
              </div>
              <span class="mood-name">{{ mood.name }}</span>
            </button>
          </div>
        </div>
        
        <!-- 日记内容卡片 -->
        <div class="card content-card glass-effect">
          <h3 class="card-title">{{ cardEmojis.journal }} 记录感受</h3>
          <div class="editor-wrapper">
            <textarea
              v-model="diaryContent"
              class="diary-editor"
              :placeholder="placeholderText"
              rows="10"
              @focus="editorFocused = true"
              @blur="editorFocused = false"
            ></textarea>
            <div class="editor-footer">
              <div class="editor-actions">
                <button class="editor-btn" @click="addEmoji" title="添加表情">😊</button>
                <button class="editor-btn" @click="addImage" title="添加图片">🖼️</button>
                <button class="editor-btn" @click="addTag" title="添加标签">🏷️</button>
              </div>
              <!-- 标签列表 -->
              <div v-if="tags.length > 0" class="tags-container">
                <span 
                  v-for="(tag, index) in tags" 
                  :key="index" 
                  class="tag"
                >
                  #{{ tag }}
                  <button class="tag-remove" @click="removeTag(index)">×</button>
                </span>
              </div>
              <!-- 标签输入框 -->
              <div v-if="showTagInput" class="tag-input-container">
                <input
                  id="tag-input"
                  v-model="tagInput"
                  type="text"
                  class="tag-input"
                  placeholder="输入标签（最多5个）"
                  maxlength="20"
                  @keydown="handleTagKeyPress"
                  @blur="confirmAddTag"
                />
              </div>
              <div class="char-count-wrapper">
                <span class="char-count" :class="{ warning: diaryContent.length > 1800 }">
                  {{ diaryContent.length }}/2000
                </span>
              </div>
            </div>
          </div>
        </div>
        
        <!-- 操作按钮 -->
        <div class="action-buttons">
          <button 
            v-if="editingDiaryId" 
            class="btn btn-outline" 
            @click="cancelEdit"
          >
            取消编辑
          </button>
          <button 
            class="btn btn-secondary" 
            @click="draftDiary"
          >
            保存草稿
          </button>
          <button 
            class="btn btn-primary" 
            @click="saveDiary"
            :disabled="!diaryContent.trim()"
          >
            <span class="btn-icon">{{ editingDiaryId ? '📝' : '✍️' }}</span>
            <span>{{ editingDiaryId ? '更新日记' : '发布日记' }}</span>
          </button>
        </div>
      </div>
      
      <!-- 右侧边栏 -->
      <div class="sidebar">
        <!-- 心情统计卡片 -->
        <div class="card mood-stats-card glass-effect">
          <h3 class="card-title">{{ cardEmojis.stats }} 心情统计</h3>
          <div class="mood-stats">
            <div 
              v-for="mood in moods" 
              :key="mood.id"
              class="mood-stat-item"
            >
              <div class="mood-stat-icon" :style="{ color: mood.color }">{{ mood.icon }}</div>
              <div class="mood-stat-bar">
                <div 
                  class="mood-stat-progress" 
                  :style="{ width: getMoodPercentage(mood.id) + '%', backgroundColor: mood.color }"
                ></div>
              </div>
              <div class="mood-stat-count">{{ getMoodCount(mood.id) }}</div>
            </div>
          </div>
        </div>
        
        <!-- 最近日记/草稿列表 -->
        <div class="card recent-diaries-card glass-effect">
          <div class="card-header">
            <h3 class="card-title">{{ cardEmojis.history }} {{ showDrafts ? '草稿箱' : '最近记录' }}</h3>
            <button 
              class="toggle-drafts-btn"
              @click="toggleDrafts"
              :title="showDrafts ? '查看最近日记' : '查看草稿'"
            >
              {{ showDrafts ? '📝' : '📂' }}
            </button>
          </div>
          <div class="recent-diaries-list">
            <div 
              v-for="diary in recentDiaries" 
              :key="diary.id"
              class="diary-entry hover-lift"
            >
              <div class="diary-meta">
                <span class="entry-date">{{ diary.date }}</span>
                <span class="entry-mood" :title="diary.moodName">
                  {{ diary.moodIcon }}
                </span>
                <span v-if="diary.isDraft" class="entry-draft-badge">草稿</span>
              </div>
              <p class="diary-excerpt">{{ diary.excerpt }}</p>
              <!-- 标签列表 -->
              <div v-if="diary.tags && diary.tags.length > 0" class="diary-tags-small">
                <span 
                  v-for="(tag, index) in diary.tags" 
                  :key="index" 
                  class="tag-small"
                >
                  #{{ tag }}
                </span>
              </div>
              <!-- 操作按钮 -->
              <div class="diary-actions">
                <button 
                  class="action-btn edit-btn"
                  @click="editDiary(diary)"
                  title="编辑"
                >
                  ✏️
                </button>
                <button 
                  class="action-btn delete-btn"
                  @click="deleteDiary(diary.id)"
                  title="删除"
                >
                  🗑️
                </button>
              </div>
            </div>
            
            <!-- 空状态 -->
            <div v-if="recentDiaries.length === 0" class="empty-state">
              <div class="empty-icon">{{ showDrafts ? '📂' : '📝' }}</div>
              <p class="empty-text">{{ showDrafts ? '暂无草稿' : '暂无记录' }}</p>
              <p class="empty-subtext">{{ showDrafts ? '你可以将日记保存为草稿' : '开始记录你的第一篇日记吧' }}</p>
            </div>
          </div>
        </div>
        
        <!-- 心情提示卡片 -->
        <div class="card mood-tips-card glass-effect">
          <h3 class="card-title">{{ cardEmojis.tips }} 心情小贴士</h3>
          <div class="tips-content">
            <p class="tip-text">{{ currentTip }}</p>
          </div>
        </div>
      </div>
    </main>
    
    <!-- 成功提示 -->
    <div 
      v-if="showToast" 
      class="toast" 
      :class="{ success: toastType === 'success', error: toastType === 'error', info: toastType === 'info' }"
    >
      <span class="toast-icon">{{ toastIcon }}</span>
      <span class="toast-message">{{ toastMessage }}</span>
    </div>
    
    <!-- 日历模态框 -->
    <div v-if="showCalendarModal" class="modal-overlay" @click="closeCalendar">
      <div class="calendar-modal" @click.stop>
        <div class="calendar-header">
          <h3>选择日期</h3>
          <button class="close-btn" @click="closeCalendar">×</button>
        </div>
        <div class="calendar-content">
          <!-- 月份导航 -->
          <div class="calendar-nav">
            <button class="nav-btn" @click="changeMonth(-1)">‹</button>
            <span class="current-month">{{ currentYear }}年 {{ months[currentMonth] }}</span>
            <button class="nav-btn" @click="changeMonth(1)">›</button>
          </div>
          
          <!-- 星期标题 -->
          <div class="weekdays">
            <div v-for="day in weekdays" :key="day" class="weekday-cell">{{ day }}</div>
          </div>
          
          <!-- 日期网格 -->
          <div class="calendar-grid">
            <div 
              v-for="(date, index) in calendarDays" 
              :key="index"
              :class="['date-cell', { 
                'other-month': !date.isCurrentMonth, 
                'selected': date.date && formatDateForComparison(date.date) === selectedDate, 
                'today': date.date && formatDateForComparison(date.date) === formatDateForComparison(new Date()) 
              }]"
              @click="selectDate(date.date)"
            >
              {{ date.day }}
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, watch } from 'vue';
import NavBar from '../components/NavBar.vue';
import api from '../api/index.js';

// 响应式状态
const selectedMood = ref(1); // 默认选择开心
const diaryContent = ref('');
const showToast = ref(false);
const toastMessage = ref('');
const toastType = ref('success');
const editorFocused = ref(false);
const currentTipIndex = ref(0);
const tags = ref([]);
const tagInput = ref('');
const showTagInput = ref(false);
const selectedDate = ref(''); // 选中的日期
const showCalendarModal = ref(false); // 日历模态框显示状态
const editingDiaryId = ref(null); // 当前编辑的日记ID
const showDrafts = ref(false); // 是否显示草稿列表

// 日历相关状态
const currentMonth = ref(new Date().getMonth());
const currentYear = ref(new Date().getFullYear());
const weekdays = ['日', '一', '二', '三', '四', '五', '六'];

// 心情数据
const moods = [
  { id: 1, name: '开心', icon: '😊', color: '#4CAF50' },
  { id: 2, name: '平静', icon: '😌', color: '#2196F3' },
  { id: 3, name: '难过', icon: '😢', color: '#9C27B0' },
  { id: 4, name: '愤怒', icon: '😠', color: '#F44336' },
  { id: 5, name: '惊喜', icon: '😲', color: '#FF9800' },
  { id: 6, name: '焦虑', icon: '😰', color: '#607D8B' }
];

// 月份数组
const months = ['一月', '二月', '三月', '四月', '五月', '六月', '七月', '八月', '九月', '十月', '十一月', '十二月'];

// 卡片标题emoji
const cardEmojis = {
  mood: '❤️',
  journal: '📝',
  stats: '📊',
  history: '📚',
  tips: '💡'
};

// 心情提示语
const moodTips = [
  '表达情绪有助于心理健康，勇敢记录你的感受吧！',
  '每天花几分钟记录，可以更好地了解自己的情绪变化。',
  '尝试写下三件今天让你感恩的小事，提升幸福感。',
  '接纳所有情绪，无论是积极还是消极的，它们都是你的一部分。',
  '深呼吸，慢慢来，给自己一些时间和空间。'
];

// 日记数据
const recentDiaries = ref([]);

// 心情统计数据
const moodStats = ref({
  1: 0, // 开心
  2: 0, // 平静
  3: 0, // 难过
  4: 0, // 愤怒
  5: 0, // 惊喜
  6: 0 // 焦虑
});

// 计算属性
const currentMood = computed(() => {
  return moods.find(m => m.id === selectedMood.value) || moods[0];
});

const formattedDate = computed(() => {
  const date = selectedDate.value ? new Date(selectedDate.value) : new Date();
  const year = date.getFullYear();
  const month = String(date.getMonth() + 1).padStart(2, '0');
  const day = String(date.getDate()).padStart(2, '0');
  return `${year}年${month}月${day}日`;
});

const weekday = computed(() => {
  const weekdays = ['周日', '周一', '周二', '周三', '周四', '周五', '周六'];
  const date = selectedDate.value ? new Date(selectedDate.value) : new Date();
  return weekdays[date.getDay()];
});

// 日历天数计算
const calendarDays = computed(() => {
  const days = [];
  const firstDay = new Date(currentYear.value, currentMonth.value, 1);
  const lastDay = new Date(currentYear.value, currentMonth.value + 1, 0);
  const daysInMonth = lastDay.getDate();
  const startingDayOfWeek = firstDay.getDay(); // 0 是星期日
  
  // 添加上个月的日期
  const prevMonthLastDay = new Date(currentYear.value, currentMonth.value, 0).getDate();
  for (let i = startingDayOfWeek; i > 0; i--) {
    days.push({
      day: prevMonthLastDay - i + 1,
      isCurrentMonth: false,
      date: new Date(currentYear.value, currentMonth.value - 1, prevMonthLastDay - i + 1)
    });
  }
  
  // 添加当月的日期
  for (let day = 1; day <= daysInMonth; day++) {
    days.push({
      day,
      isCurrentMonth: true,
      date: new Date(currentYear.value, currentMonth.value, day)
    });
  }
  
  // 添加下个月的日期以填充网格
  const remainingDays = 42 - days.length; // 6行7列 = 42个单元格
  for (let day = 1; day <= remainingDays; day++) {
    days.push({
      day,
      isCurrentMonth: false,
      date: new Date(currentYear.value, currentMonth.value + 1, day)
    });
  }
  
  return days;
});

const placeholderText = computed(() => {
  return `今天为什么感到${currentMood.value.name}呢？记录一下你的想法...`;
});

const toastIcon = computed(() => {
  switch (toastType.value) {
    case 'success': return '✅';
    case 'error': return '❌';
    case 'info': return 'ℹ️';
    default: return '✅';
  }
});

const currentTip = computed(() => {
  return moodTips[currentTipIndex.value];
});

// 方法
const selectMood = (moodId) => {
  selectedMood.value = moodId;
};

const openCalendar = () => {
  // 如果已经选中了日期，设置日历显示选中日期的月份
  if (selectedDate.value) {
    const date = new Date(selectedDate.value);
    currentMonth.value = date.getMonth();
    currentYear.value = date.getFullYear();
  } else {
    // 否则显示当前月份
    const now = new Date();
    currentMonth.value = now.getMonth();
    currentYear.value = now.getFullYear();
  }
  showCalendarModal.value = true;
};

const closeCalendar = () => {
  showCalendarModal.value = false;
};

const changeMonth = (direction) => {
  let newMonth = currentMonth.value + direction;
  let newYear = currentYear.value;
  
  if (newMonth < 0) {
    newMonth = 11;
    newYear--;
  } else if (newMonth > 11) {
    newMonth = 0;
    newYear++;
  }
  
  currentMonth.value = newMonth;
  currentYear.value = newYear;
};

const selectDate = (date) => {
  if (!date) return;
  
  // 格式化日期为YYYY-MM-DD格式
  const year = date.getFullYear();
  const month = String(date.getMonth() + 1).padStart(2, '0');
  const day = String(date.getDate()).padStart(2, '0');
  selectedDate.value = `${year}-${month}-${day}`;
  
  // 加载选中日期的日记
  loadDiariesByDate(selectedDate.value);
  
  // 关闭日历模态框
  showCalendarModal.value = false;
};

const formatDateForComparison = (date) => {
  const d = new Date(date);
  const year = d.getFullYear();
  const month = String(d.getMonth() + 1).padStart(2, '0');
  const day = String(d.getDate()).padStart(2, '0');
  return `${year}-${month}-${day}`;
};

// 取消编辑
const cancelEdit = () => {
  editingDiaryId.value = null;
  diaryContent.value = '';
  tags.value = [];
  selectedMood.value = 1;
  showToastMessage('已取消编辑', 'info');
};

const addEmoji = () => {
  // TODO: 实现表情选择器功能
  showToastMessage('表情选择器即将推出', 'info');
};
// 添加标签功能
const addTag = () => {
  showTagInput.value = !showTagInput.value;
  if (showTagInput.value) {
    setTimeout(() => {
      const input = document.getElementById('tag-input');
      if (input) input.focus();
    }, 100);
  }
};

// 确认添加标签
const confirmAddTag = () => {
  const tag = tagInput.value.trim();
  if (tag && tags.value.length < 5) {
    if (!tags.value.includes(tag)) {
      tags.value.push(tag);
      tagInput.value = '';
      showToastMessage('标签添加成功', 'success');
    } else {
      showToastMessage('该标签已存在', 'info');
    }
  } else if (tags.value.length >= 5) {
    showToastMessage('最多添加5个标签', 'error');
  }
  showTagInput.value = false;
};

// 删除标签
const removeTag = (index) => {
  tags.value.splice(index, 1);
};

// 处理标签输入框的回车事件
const handleTagKeyPress = (e) => {
  if (e.key === 'Enter') {
    confirmAddTag();
  } else if (e.key === 'Escape') {
    showTagInput.value = false;
    tagInput.value = '';
  }
};

const showToastMessage = (message, type = 'success') => {
  toastMessage.value = message;
  toastType.value = type;
  showToast.value = true;
  
  setTimeout(() => {
    // 淡出动画
    showToast.value = false;
  }, 3000);
};

// 检查token是否有效
const isTokenValid = () => {
  const token = localStorage.getItem('authToken');
  // 基础检查
  if (!token || typeof token !== 'string' || token.trim() === '') {
    return false;
  }
  
  // 检查是否是模拟token
  if (token.includes('mock_token_') || (token.startsWith('Bearer ') && token.substring(7).includes('mock_token_'))) {
    console.warn('检测到模拟token，视为无效');
    // 可选：自动清除模拟token
    localStorage.removeItem('authToken');
    return false;
  }
  
  // 检查是否是有效的JWT格式（通常包含三个部分）
  const tokenParts = token.startsWith('Bearer ') ? token.substring(7).split('.') : token.split('.');
  return tokenParts.length === 3;
};

// 加载最近的日记
const loadRecentDiaries = async () => {
  try {
    // 再次检查token有效性
    if (!isTokenValid()) {
      recentDiaries.value = [];
      console.log('未登录或token无效，不加载日记数据');
      return;
    }
    
    console.log('开始加载最近日记，携带认证信息');
    const response = await api.diary.getRecent();
    
    // 处理响应数据
    if (response.code === 0 || response.status === 200) {
      // 格式化日期显示
      recentDiaries.value = (response.data || []).map(diary => {
        // 解析tags字段
        let parsedTags = [];
        try {
          parsedTags = diary.tags ? JSON.parse(diary.tags) : [];
        } catch (e) {
          parsedTags = [];
        }
        
        return {
          ...diary,
          date: formatDiaryDate(diary),
          excerpt: diary.content.length > 100 ? diary.content.substring(0, 100) + '...' : diary.content,
          tags: parsedTags
        };
      });
    } else {
      recentDiaries.value = [];
    }
    
  } catch (error) {
    console.error('加载最近日记失败:', error);
    // 加载失败时确保数据保持空状态
    recentDiaries.value = [];
    
    // 仅在发生401错误时清除token
    if (error.response && error.response.status === 401) {
      localStorage.removeItem('authToken');
      console.log('检测到401错误，已清除token');
      showToastMessage('登录已过期，请重新登录', 'error');
    } else {
      // 其他错误
      showToastMessage('获取最近日记失败', 'error');
    }
  }
};

// 加载特定日期日记
const loadDiariesByDate = async (date) => {
  try {
    const response = await api.diary.getByDate(date);
    if (response.code === 0) {
      // 格式化日期显示
      recentDiaries.value = response.data.map(diary => {
        // 解析tags字段
        let parsedTags = [];
        try {
          parsedTags = diary.tags ? JSON.parse(diary.tags) : [];
        } catch (e) {
          parsedTags = [];
        }
        
        return {
          ...diary,
          date: formatDiaryDate(diary),
          excerpt: diary.content.length > 100 ? diary.content.substring(0, 100) + '...' : diary.content,
          tags: parsedTags
        };
      });
      showToastMessage(`已加载${date}的日记`, 'success');
    }
  } catch (error) {
    showToastMessage('获取指定日期日记失败', 'error');
    console.error('Failed to load diaries by date:', error);
  }
};

// 加载草稿列表
const loadDrafts = async () => {
  // 检查是否有有效的token
  if (!isTokenValid()) {
    showToastMessage('请先登录再查看草稿', 'error');
    return;
  }
  
  try {
    const response = await api.diary.getDrafts();
    if (response.code === 0) {
      // 格式化草稿显示
      recentDiaries.value = response.data.map(diary => {
        // 解析tags字段
        let parsedTags = [];
        try {
          parsedTags = diary.tags ? JSON.parse(diary.tags) : [];
        } catch (e) {
          parsedTags = [];
        }
        
        return {
          ...diary,
          date: formatDiaryDate(diary),
          excerpt: diary.content.length > 100 ? diary.content.substring(0, 100) + '...' : diary.content,
          tags: parsedTags,
          isDraft: true
        };
      });
      showDrafts.value = true;
      showToastMessage('已加载草稿列表', 'success');
    }
  } catch (error) {
    console.error('Failed to load drafts:', error);
    
    // 特殊处理401错误
    if (error.response && error.response.status === 401) {
      // 清除可能无效的token
      localStorage.removeItem('authToken');
      showToastMessage('登录已过期，请重新登录', 'error');
    } else {
      showToastMessage('获取草稿列表失败', 'error');
    }
  }
};

// 切换显示草稿/最近日记
const toggleDrafts = async () => {
  if (showDrafts.value) {
    // 切换回最近日记
    await loadRecentDiaries();
  } else {
    // 切换到草稿列表
    await loadDrafts();
  }
  showDrafts.value = !showDrafts.value;
};

// 更新日记
const updateDiary = async (id, data) => {
  try {
    const response = await api.diary.update(id, {
      ...data,
      tags: JSON.stringify(data.tags) // 按要求转换为JSON数组格式字符串
    });
    
    if (response.code === 0) {
      // 刷新数据
      if (showDrafts.value) {
        await loadDrafts();
      } else {
        await loadRecentDiaries();
      }
      await loadMoodStats();
      
      // 重置编辑状态
      editingDiaryId.value = null;
      diaryContent.value = '';
      tags.value = [];
      showToastMessage('日记更新成功', 'success');
    } else {
      showToastMessage(response.message || '更新失败', 'error');
    }
  } catch (error) {
    showToastMessage('更新日记失败', 'error');
    console.error('Failed to update diary:', error);
  }
};

// 删除日记
const deleteDiary = async (id) => {
  if (confirm('确定要删除这篇日记吗？')) {
    try {
      const response = await api.diary.delete(id);
      
      if (response.code === 0) {
        // 刷新数据
        if (showDrafts.value) {
          await loadDrafts();
        } else {
          await loadRecentDiaries();
        }
        await loadMoodStats();
        showToastMessage('日记删除成功', 'success');
      } else {
        showToastMessage(response.message || '删除失败', 'error');
      }
    } catch (error) {
      showToastMessage('删除日记失败', 'error');
      console.error('Failed to delete diary:', error);
    }
  }
};

// 编辑日记
const editDiary = (diary) => {
  editingDiaryId.value = diary.id;
  diaryContent.value = diary.content;
  selectedMood.value = diary.moodId;
  // 解析tags字段
  try {
    tags.value = diary.tags ? JSON.parse(diary.tags) : [];
  } catch (e) {
    tags.value = [];
  }
  showToastMessage('开始编辑日记', 'info');
  // 滚动到编辑器
  setTimeout(() => {
    const editor = document.querySelector('.diary-editor');
    if (editor) {
      editor.scrollIntoView({ behavior: 'smooth' });
      editor.focus();
    }
  }, 100);
};

// 加载心情统计数据
const loadMoodStats = async () => {
  try {
    // 再次检查token有效性
    if (!isTokenValid()) {
      moodStats.value = {
        1: 0, // 开心
        2: 0, // 平静
        3: 0, // 难过
        4: 0, // 愤怒
        5: 0, // 惊喜
        6: 0  // 焦虑
      };
      console.log('未登录或token无效，不加载心情统计数据');
      return;
    }
    
    console.log('开始加载心情统计，携带认证信息');
    const response = await api.diary.getStats();
    
    // 处理响应数据
    if (response.code === 0 || response.status === 200) {
      moodStats.value = response.data || {
        1: 0, // 开心
        2: 0, // 平静
        3: 0, // 难过
        4: 0, // 愤怒
        5: 0, // 惊喜
        6: 0  // 焦虑
      };
    } else {
      moodStats.value = {
        1: 0, // 开心
        2: 0, // 平静
        3: 0, // 难过
        4: 0, // 愤怒
        5: 0, // 惊喜
        6: 0  // 焦虑
      };
    }
    
  } catch (error) {
    console.error('加载心情统计失败:', error);
    // 加载失败时确保数据保持默认状态
    moodStats.value = {
      1: 0, // 开心
      2: 0, // 平静
      3: 0, // 难过
      4: 0, // 愤怒
      5: 0, // 惊喜
      6: 0  // 焦虑
    };
    
    // 仅在发生401错误时清除token
    if (error.response && error.response.status === 401) {
      localStorage.removeItem('authToken');
      console.log('检测到401错误，已清除token');
    } else {
      // 其他错误时保持默认数据
      showToastMessage('获取心情统计失败', 'error');
    }
  }
};

// 格式化日记日期显示
const formatDiaryDate = (diaryData) => {
  // 如果传入的是对象，优先使用date字段（数据库中的date），否则使用传入的日期字符串
  const dateField = typeof diaryData === 'object' && diaryData !== null ? diaryData.date : diaryData;
  const date = new Date(dateField);
  const today = new Date();
  const yesterday = new Date(today);
  yesterday.setDate(yesterday.getDate() - 1);
  
  // 检查是否是今天
  if (date.toDateString() === today.toDateString()) {
    return '今天';
  }
  
  // 检查是否是昨天
  if (date.toDateString() === yesterday.toDateString()) {
    return '昨天';
  }
  
  // 检查是否是本周
  const weekDiff = Math.floor((today - date) / (1000 * 60 * 60 * 24 * 7));
  if (weekDiff === 0) {
    const weekdays = ['周日', '周一', '周二', '周三', '周四', '周五', '周六'];
    return weekdays[date.getDay()];
  }
  
  // 检查是否是上周
  if (weekDiff === 1) {
    return '上周';
  }
  
  // 检查是否是本月
  if (date.getMonth() === today.getMonth() && date.getFullYear() === today.getFullYear()) {
    return `${today.getDate() - date.getDate()}天前`;
  }
  
  // 其他情况显示具体日期
  return `${date.getMonth() + 1}月${date.getDate()}日`;
};

// 保存日记
const saveDiary = async () => {
  // 检查是否已登录并有有效的token
  const token = localStorage.getItem('authToken');
        if (!token || token.trim() === '') {
    showToastMessage('请先登录再保存日记', 'error');
    // 不强制跳转，让用户决定是否登录
    return;
  }
  
  if (!diaryContent.value.trim()) {
    showToastMessage('请输入日记内容', 'error');
    return;
  }
  
  try {
    // 验证tags格式
    if (!Array.isArray(tags.value)) {
      showToastMessage('标签格式错误', 'error');
      return;
    }
    
    // 确保tags不超过5个
    if (tags.value.length > 5) {
      showToastMessage('最多添加5个标签', 'error');
      return;
    }
    
    const diaryData = {
      content: diaryContent.value,
      moodId: selectedMood.value,
      isDraft: false,
      tags: JSON.stringify(tags.value), // 按要求转换为JSON数组格式字符串
      date: selectedDate.value // 添加选中的日期
    };
    
    console.log('即将发送保存日记请求，携带认证信息');
    
    let response;
    if (editingDiaryId.value) {
      // 更新现有日记
      response = await api.diary.update(editingDiaryId.value, diaryData);
    } else {
      // 创建新日记
      response = await api.diary.save(diaryData);
    }
    
    if (response.code === 0) {
      // 刷新数据
      if (showDrafts.value) {
        await loadDrafts();
      } else {
        await loadRecentDiaries();
      }
      await loadMoodStats();
      
      // 清空内容
      diaryContent.value = '';
      tags.value = [];
      editingDiaryId.value = null;
      showToastMessage(editingDiaryId.value ? '日记更新成功' : '日记保存成功', 'success');
    } else {
      showToastMessage(response.message || '保存失败', 'error');
    }
  } catch (error) {
    console.error('保存日记失败:', error);
    
    // 根据错误类型提供更具体的提示
    if (error.response && error.response.status === 401) {
      showToastMessage('登录已过期，请重新登录', 'error');
      localStorage.removeItem('authToken');
      // 不强制跳转，让用户决定是否登录
    } else {
      showToastMessage('保存日记失败', 'error');
    }
  }
};

// 保存草稿
const draftDiary = async () => {
  // 检查是否已登录并有有效的token
  const token = localStorage.getItem('token');
  if (!token || token.trim() === '') {
    showToastMessage('请先登录再保存草稿', 'error');
    // 不强制跳转，让用户决定是否登录
    return;
  }
  
  if (!diaryContent.value.trim()) {
    showToastMessage('请输入日记内容', 'error');
    return;
  }
  
  try {
    // 验证tags格式
    if (!Array.isArray(tags.value)) {
      showToastMessage('标签格式错误', 'error');
      return;
    }
    
    console.log('即将发送保存草稿请求，携带认证信息');
    
    const response = await api.diary.save({
      content: diaryContent.value,
      moodId: selectedMood.value,
      isDraft: true,
      tags: JSON.stringify(tags.value), // 按要求转换为JSON数组格式字符串
      date: selectedDate.value // 添加选中的日期
    });
    
    if (response.code === 0) {
      // 如果当前正在查看草稿列表，刷新列表
      if (showDrafts.value) {
        await loadDrafts();
      }
      showToastMessage('日记已保存到草稿', 'success');
    } else {
      showToastMessage(response.message || '保存失败', 'error');
    }
  } catch (error) {
    console.error('保存草稿失败:', error);
    
    // 根据错误类型提供更具体的提示
    if (error.response && error.response.status === 401) {
      showToastMessage('登录已过期，请重新登录', 'error');
      localStorage.removeItem('authToken');
      setTimeout(() => {
        window.location.href = '/login';
      }, 1000);
    } else {
      showToastMessage('保存草稿失败', 'error');
    }
  }
};

// 添加图片功能
const addImage = async () => {
  try {
    // 创建文件选择器
    const input = document.createElement('input');
    input.type = 'file';
    input.accept = 'image/*';
    input.onchange = async (e) => {
      const file = e.target.files[0];
      if (file) {
        const formData = new FormData();
        formData.append('file', file);
        
        const response = await api.upload.image(formData);
        if (response.code === 0) {
          // 在内容中插入图片标记
          diaryContent.value += `\n![图片](${response.data})\n`;
          showToastMessage('图片上传成功', 'success');
        } else {
          showToastMessage(response.message || '上传失败', 'error');
        }
      }
    };
    input.click();
  } catch (error) {
    showToastMessage('图片上传失败', 'error');
    console.error('Failed to upload image:', error);
  }
};

// 获取心情统计
const getMoodCount = (moodId) => {
  return moodStats.value[moodId] || 0;
};

// 获取心情百分比
const getMoodPercentage = (moodId) => {
  const total = Object.values(moodStats.value).reduce((a, b) => a + b, 0);
  if (total === 0) return 0;
  return Math.round((moodStats.value[moodId] || 0) / total * 100);
};

// 切换提示语
const rotateTips = () => {
  currentTipIndex.value = (currentTipIndex.value + 1) % moodTips.length;
};

// 监听心情变化
watch(selectedMood, (newMood) => {
  // 当心情变化时，可以添加一些视觉反馈
  console.log(`心情切换为: ${moods.find(m => m.id === newMood)?.name}`);
});

// 组件挂载时执行
onMounted(async () => {
  // 首先检查并清除可能存在的无效token或模拟token
  const token = localStorage.getItem('authToken');
    if (token) {
    // 检查是否是模拟token
    if (token.includes('mock_token_') || (token.startsWith('Bearer ') && token.substring(7).includes('mock_token_'))) {
      localStorage.removeItem('authToken');
      console.log('已清除模拟token');
    } 
    // 检查是否是无效格式的token
    else if (!isTokenValid()) {
      localStorage.removeItem('authToken');
      console.log('已清除无效格式token');
    }
  }
  
  // 初始化数据为默认值，避免未定义状态
  recentDiaries.value = [];
  moodStats.value = {
    1: 0, // 开心
    2: 0, // 平静
    3: 0, // 难过
    4: 0, // 愤怒
    5: 0, // 惊喜
    6: 0  // 焦虑
  };
  
  // 如果已登录，加载数据
  const currentToken = localStorage.getItem('authToken');
  if (currentToken && isTokenValid()) {
    try {
      console.log('使用有效token加载数据');
      // 并行加载数据以提高性能
      await Promise.all([
        loadRecentDiaries(),
        loadMoodStats()
      ]);
    } catch (error) {
      console.error('数据加载失败:', error);
      // 加载失败时确保数据保持默认状态
      recentDiaries.value = [];
      moodStats.value = {
        1: 0, // 开心
        2: 0, // 平静
        3: 0, // 难过
        4: 0, // 愤怒
        5: 0, // 惊喜
        6: 0  // 焦虑
      };
      
      // 如果是401错误，清除token
      if (error.response && error.response.status === 401) {
        localStorage.removeItem('authToken');
        console.log('因401错误清除token');
      }
    }
  } else {
    // 如果未登录，显示提示但不强制跳转
    // 允许用户访问页面，但需要登录才能查看和操作数据
    showToastMessage('您当前未登录，可以先了解功能，登录后查看个人日记', 'info');
  }
  
  // 每30秒切换一次提示语
  setInterval(rotateTips, 30000);
});

// 监听tags变化，确保不超过5个
watch(tags, (newTags) => {
  if (newTags.length > 5) {
    showToastMessage('最多添加5个标签', 'error');
    tags.value = newTags.slice(0, 5);
  }
});
</script>

<style scoped>
/* 导入字体 */
@import url('https://fonts.googleapis.com/css2?family=Inter:wght@300;400;500;600;700&display=swap');

/* 全局样式 */
.diary-page {
  min-height: 100vh;
  background: linear-gradient(135deg, #f5f7fa 0%, #c3cfe2 100%);
  font-family: 'Inter', -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif;
  position: relative;
  overflow-x: hidden;
}

/* 顶部装饰 */
.diary-header-decoration {
  height: 200px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  position: relative;
  overflow: hidden;
  box-shadow: 0 4px 20px rgba(102, 126, 234, 0.3);
}

/* 波浪效果 */
.wave-shape {
  position: absolute;
  bottom: 0;
  left: 0;
  width: 100%;
  height: 100px;
  background: url('data:image/svg+xml,<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 1440 100" preserveAspectRatio="none"><path fill="%23f5f7fa" fill-opacity="1" d="M0,64L60,58.7C120,53,240,43,360,42.7C480,43,600,53,720,64C840,75,960,85,1080,80C1200,75,1320,53,1380,42.7L1440,32L1440,100L1380,100C1320,100,1200,100,1080,100C960,100,840,100,720,100C600,100,480,100,360,100C240,100,120,100,60,100L0,100Z"></path></svg>');
  background-size: cover;
  background-position: center;
}

/* 主内容区域 */
.main-content {
  max-width: 1200px;
  margin: 0 auto;
  padding: 20px;
  display: grid;
  grid-template-columns: 1fr 350px;
  gap: 30px;
  position: relative;
  top: -80px;
  z-index: 10;
}

/* 日期显示 */
.date-display {
  background: white;
  padding: 25px;
  border-radius: 20px;
  box-shadow: 0 10px 30px rgba(0, 0, 0, 0.08);
  margin-bottom: 30px;
  display: flex;
  align-items: center;
  gap: 20px;
  transition: transform 0.3s ease, box-shadow 0.3s ease;
}

.date-display:hover {
  transform: translateY(-2px);
  box-shadow: 0 15px 35px rgba(0, 0, 0, 0.12);
}

.date-circle {
  width: 80px;
  height: 80px;
  border-radius: 50%;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  color: white;
  font-weight: 600;
  box-shadow: 0 4px 15px rgba(102, 126, 234, 0.3);
}

.date-day {
  font-size: 28px;
  line-height: 1;
}

.date-month {
  font-size: 14px;
  opacity: 0.9;
  margin-top: 2px;
}

.date-text h2 {
  font-size: 22px;
  font-weight: 600;
  color: #333;
  margin: 0 0 5px 0;
}

.weekday {
  color: #666;
  font-size: 16px;
  font-weight: 500;
}

.calendar-btn {
  margin-left: auto;
  background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%);
  border: none;
  border-radius: 50%;
  width: 50px;
  height: 50px;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  color: white;
  font-size: 20px;
  position: relative;
  box-shadow: 0 4px 15px rgba(240, 147, 251, 0.4);
  transition: transform 0.3s ease, box-shadow 0.3s ease;
}

.calendar-btn:hover {
  transform: scale(1.1);
  box-shadow: 0 6px 20px rgba(240, 147, 251, 0.5);
}

.calendar-tooltip {
  position: absolute;
  bottom: -30px;
  left: 50%;
  transform: translateX(-50%);
  background: rgba(0, 0, 0, 0.8);
  color: white;
  padding: 4px 10px;
  border-radius: 6px;
  font-size: 12px;
  white-space: nowrap;
  opacity: 0;
  pointer-events: none;
  transition: opacity 0.3s ease;
}

.calendar-btn:hover .calendar-tooltip {
  opacity: 1;
}

/* 日历模态框样式 */
.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
  animation: fadeIn 0.3s ease;
}

.calendar-modal {
  background: white;
  border-radius: 20px;
  padding: 0;
  width: 90%;
  max-width: 400px;
  box-shadow: 0 20px 40px rgba(0, 0, 0, 0.2);
  animation: slideUp 0.3s ease;
  overflow: hidden;
}

.calendar-header {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  padding: 20px;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.calendar-header h3 {
  margin: 0;
  font-size: 18px;
  font-weight: 600;
}

.close-btn {
  background: none;
  border: none;
  color: white;
  font-size: 24px;
  cursor: pointer;
  padding: 0;
  width: 30px;
  height: 30px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 50%;
  transition: background-color 0.3s ease;
}

.close-btn:hover {
  background: rgba(255, 255, 255, 0.2);
}

.calendar-content {
  padding: 20px;
}

.calendar-nav {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.nav-btn {
  background: linear-gradient(135deg, #f5f7fa 0%, #e9ecef 100%);
  border: none;
  border-radius: 50%;
  width: 36px;
  height: 36px;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  font-size: 18px;
  font-weight: bold;
  color: #666;
  transition: all 0.3s ease;
}

.nav-btn:hover {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  transform: scale(1.1);
}

.current-month {
  font-size: 16px;
  font-weight: 600;
  color: #333;
}

.weekdays {
  display: grid;
  grid-template-columns: repeat(7, 1fr);
  gap: 5px;
  margin-bottom: 10px;
}

.weekday-cell {
  text-align: center;
  font-weight: 600;
  color: #666;
  font-size: 14px;
  padding: 8px 0;
}

.calendar-grid {
  display: grid;
  grid-template-columns: repeat(7, 1fr);
  gap: 5px;
}

.date-cell {
  aspect-ratio: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 50%;
  cursor: pointer;
  font-size: 14px;
  font-weight: 500;
  transition: all 0.3s ease;
  position: relative;
}

.date-cell:not(.other-month):hover {
  background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%);
  color: white;
  transform: scale(1.1);
}

.date-cell.other-month {
  color: #ccc;
  cursor: default;
}

.date-cell.selected {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  font-weight: 600;
}

.date-cell.today {
  border: 2px solid #667eea;
}

.date-cell.today.selected {
  border-color: white;
}

@keyframes fadeIn {
  from {
    opacity: 0;
  }
  to {
    opacity: 1;
  }
}

@keyframes slideUp {
  from {
    opacity: 0;
    transform: translateY(30px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

/* 卡片样式 */
.card {
  background: white;
  border-radius: 20px;
  padding: 25px;
  margin-bottom: 30px;
  transition: all 0.3s ease;
  border: 1px solid rgba(255, 255, 255, 0.18);
  box-shadow: 0 8px 32px rgba(31, 38, 135, 0.07);
}

.card:hover {
  transform: translateY(-5px);
  box-shadow: 0 15px 35px rgba(31, 38, 135, 0.15);
}
/* 卡片头部 */
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 25px;
}

/* 卡片标题 */
.card-title {
  font-size: 19px;
  font-weight: 600;
  color: #333;
  display: flex;
  align-items: center;
  gap: 10px;
  margin: 0;
}

/* 切换草稿按钮 */
.toggle-drafts-btn {
  background: transparent;
  border: none;
  font-size: 1.2rem;
  cursor: pointer;
  padding: 8px;
  border-radius: 8px;
  transition: all 0.3s ease;
}

.toggle-drafts-btn:hover {
  background: rgba(0, 0, 0, 0.1);
  transform: rotate(15deg);
}

/* 玻璃拟态效果 */
.glass-effect {
  backdrop-filter: blur(10px);
  background: rgba(255, 255, 255, 0.85);
  border: 1px solid rgba(255, 255, 255, 0.2);
}

/* 心情选择器 */
.mood-selector {
  display: flex;
  flex-wrap: wrap;
  gap: 15px;
}

.mood-option {
  flex: 1;
  min-width: calc(33.333% - 15px);
  background: linear-gradient(135deg, #f8f9fa 0%, #e9ecef 100%);
  border: 3px solid transparent;
  border-radius: 15px;
  padding: 20px 10px;
  cursor: pointer;
  text-align: center;
  transition: all 0.3s ease;
  position: relative;
  overflow: hidden;
}

.mood-option::before {
  content: '';
  position: absolute;
  top: 0;
  left: -100%;
  width: 100%;
  height: 100%;
  background: linear-gradient(90deg, transparent, rgba(255, 255, 255, 0.2), transparent);
  transition: left 0.5s ease;
}

.mood-option:hover::before {
  left: 100%;
}

.mood-option.active {
  background: var(--mood-color);
  transform: translateY(-3px);
  box-shadow: 0 8px 20px rgba(0, 0, 0, 0.15);
}

.mood-icon {
  font-size: 36px;
  margin-bottom: 10px;
  transition: transform 0.3s ease;
}

.mood-option:hover .mood-icon {
  transform: scale(1.1);
}

.mood-name {
  font-size: 15px;
  color: #555;
  font-weight: 500;
  transition: all 0.3s ease;
}

.mood-option.active .mood-name {
  color: white;
}

/* 编辑器样式 */
.editor-wrapper {
  position: relative;
}

.diary-editor {
  width: 100%;
  min-height: 220px;
  padding: 20px;
  border: 2px solid #e0e0e0;
  border-radius: 15px;
  font-size: 16px;
  line-height: 1.7;
  resize: vertical;
  font-family: 'Inter', inherit;
  background: white;
  transition: all 0.3s ease;
  box-shadow: inset 0 2px 5px rgba(0, 0, 0, 0.05);
}

.diary-editor:focus {
  outline: none;
  border-color: #667eea;
  box-shadow: 0 0 0 3px rgba(102, 126, 234, 0.1);
  transform: translateY(-1px);
}

.editor-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: 15px;
  padding-top: 15px;
  border-top: 1px solid #e0e0e0;
  flex-wrap: wrap;
  gap: 10px;
}

/* 标签容器 */
.tags-container {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  margin-bottom: 10px;
  width: 100%;
}

/* 标签样式 */
.tag {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  padding: 4px 12px;
  border-radius: 20px;
  font-size: 14px;
  display: inline-flex;
  align-items: center;
  gap: 6px;
  transition: transform 0.2s ease;
}

.tag:hover {
  transform: scale(1.05);
}

/* 标签移除按钮 */
.tag-remove {
  background: none;
  border: none;
  color: white;
  font-size: 16px;
  line-height: 1;
  cursor: pointer;
  padding: 0;
  margin: 0;
  width: 16px;
  height: 16px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 50%;
  transition: background-color 0.2s ease;
}

.tag-remove:hover {
  background-color: rgba(255, 255, 255, 0.2);
}

/* 标签输入框容器 */
.tag-input-container {
  width: 100%;
  margin-bottom: 10px;
}

/* 标签输入框 */
.tag-input {
  width: 100%;
  padding: 8px 12px;
  border: 2px solid #667eea;
  border-radius: 8px;
  font-size: 14px;
  outline: none;
  transition: border-color 0.3s ease;
}

.tag-input:focus {
  border-color: #764ba2;
  box-shadow: 0 0 0 3px rgba(102, 126, 234, 0.1);
}

.editor-actions {
  display: flex;
  gap: 8px;
}

.editor-btn {
  background: linear-gradient(135deg, #f5f7fa 0%, #e9ecef 100%);
  border: none;
  border-radius: 50%;
  width: 36px;
  height: 36px;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  font-size: 16px;
  transition: all 0.3s ease;
}

.editor-btn:hover {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  transform: translateY(-2px);
}

.char-count-wrapper {
  position: relative;
}

.char-count {
  font-size: 14px;
  color: #999;
  font-weight: 500;
  transition: color 0.3s ease;
}

.char-count.warning {
  color: #ff9800;
  font-weight: 600;
}

/* 操作按钮 */
.action-buttons {
  display: flex;
  gap: 15px;
  margin-bottom: 30px;
  justify-content: center;
  flex-wrap: wrap;
}

/* 取消编辑按钮 */
.btn-outline {
  background: transparent;
  border: 2px solid #667eea;
  color: #667eea;
  transition: all 0.3s ease;
}

.btn-outline:hover {
  background: rgba(102, 126, 234, 0.1);
  transform: translateY(-1px);
}

.btn {
  padding: 14px 28px;
  border-radius: 12px;
  font-size: 16px;
  font-weight: 600;
  cursor: pointer;
  border: none;
  transition: all 0.3s ease;
  display: flex;
  align-items: center;
  gap: 8px;
  position: relative;
  overflow: hidden;
}

.btn::before {
  content: '';
  position: absolute;
  top: 0;
  left: -100%;
  width: 100%;
  height: 100%;
  background: linear-gradient(90deg, transparent, rgba(255, 255, 255, 0.3), transparent);
  transition: left 0.5s ease;
}

.btn:hover::before {
  left: 100%;
}

.btn-primary {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  box-shadow: 0 4px 15px rgba(102, 126, 234, 0.3);
}

.btn-primary:hover:not(:disabled) {
  transform: translateY(-2px);
  box-shadow: 0 6px 20px rgba(102, 126, 234, 0.4);
}

.btn-primary:disabled {
  background: #ccc;
  cursor: not-allowed;
  transform: none;
  box-shadow: none;
}

.btn-secondary {
  background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%);
  color: white;
  box-shadow: 0 4px 15px rgba(240, 147, 251, 0.3);
}

.btn-secondary:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 20px rgba(240, 147, 251, 0.4);
}

.btn-icon {
  font-size: 18px;
}

/* 心情统计 */
.mood-stats {
  display: flex;
  flex-direction: column;
  gap: 15px;
}

.mood-stat-item {
  display: flex;
  align-items: center;
  gap: 12px;
}

.mood-stat-icon {
  font-size: 24px;
  width: 30px;
  text-align: center;
}

.mood-stat-bar {
  flex: 1;
  height: 10px;
  background: #f0f0f0;
  border-radius: 5px;
  overflow: hidden;
  position: relative;
}

.mood-stat-progress {
  height: 100%;
  border-radius: 5px;
  transition: width 0.8s ease;
  position: relative;
}

.mood-stat-progress::after {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: linear-gradient(90deg, transparent, rgba(255, 255, 255, 0.3), transparent);
  animation: shimmer 2s infinite;
}

@keyframes shimmer {
  0% { transform: translateX(-100%); }
  100% { transform: translateX(100%); }
}

.mood-stat-count {
  font-size: 14px;
  font-weight: 600;
  color: #666;
  min-width: 20px;
  text-align: right;
}

/* 最近日记 */
.recent-diaries-list {
  max-height: 350px;
  overflow-y: auto;
  padding-right: 5px;
}

.recent-diaries-list::-webkit-scrollbar {
  width: 6px;
}

.recent-diaries-list::-webkit-scrollbar-track {
  background: #f1f1f1;
  border-radius: 3px;
}

.recent-diaries-list::-webkit-scrollbar-thumb {
  background: #c1c1c1;
  border-radius: 3px;
}

.recent-diaries-list::-webkit-scrollbar-thumb:hover {
  background: #a8a8a8;
}

.diary-entry {
  padding: 15px;
  border: 1px solid #f0f0f0;
  border-radius: 12px;
  margin-bottom: 15px;
  transition: all 0.3s ease;
  background: white;
}

/* 草稿标记 */
.entry-draft-badge {
  background: #ff9800;
  color: white;
  padding: 2px 8px;
  border-radius: 12px;
  font-size: 0.75rem;
  font-weight: 500;
  margin-left: 8px;
}

/* 小标签样式 */
.diary-tags-small {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
  margin-top: 10px;
}

.tag-small {
  background: #f0f2f5;
  color: #666;
  padding: 2px 8px;
  border-radius: 12px;
  font-size: 0.75rem;
  font-weight: 500;
  display: inline-block;
}

/* 日记操作按钮 */
.diary-actions {
  display: flex;
  gap: 8px;
  margin-top: 12px;
}

.action-btn {
  background: transparent;
  border: none;
  font-size: 0.9rem;
  cursor: pointer;
  padding: 6px;
  border-radius: 6px;
  transition: all 0.3s ease;
  opacity: 0.7;
}

.action-btn:hover {
  opacity: 1;
  background: rgba(0, 0, 0, 0.1);
}

.edit-btn:hover {
  background: rgba(76, 175, 80, 0.1);
}

.delete-btn:hover {
  background: rgba(244, 67, 54, 0.1);
}

.diary-entry:hover {
  border-color: #667eea;
  box-shadow: 0 4px 15px rgba(102, 126, 234, 0.15);
}

.diary-meta {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 10px;
}

.entry-date {
  font-size: 13px;
  color: #999;
  font-weight: 500;
}

.entry-mood {
  font-size: 20px;
}

.diary-excerpt {
  font-size: 15px;
  line-height: 1.6;
  color: #333;
  margin: 0;
  font-weight: 400;
}

/* 悬浮提升效果 */
.hover-lift {
  transition: transform 0.3s ease, box-shadow 0.3s ease;
}

.hover-lift:hover {
  transform: translateY(-3px);
}

/* 空状态 */
.empty-state {
  text-align: center;
  padding: 40px 20px;
  color: #999;
}

.empty-icon {
  font-size: 48px;
  margin-bottom: 15px;
  opacity: 0.7;
}

.empty-text {
  font-size: 18px;
  font-weight: 500;
  margin: 0 0 8px 0;
}

.empty-subtext {
  font-size: 14px;
  opacity: 0.8;
  margin: 0;
}

/* 心情提示 */
.tips-content {
  position: relative;
  padding: 15px;
  background: linear-gradient(135deg, #f0f9ff 0%, #e0f2fe 100%);
  border-radius: 12px;
  border-left: 4px solid #3b82f6;
}

.tip-text {
  font-size: 15px;
  line-height: 1.6;
  color: #333;
  margin: 0;
  font-style: italic;
}

/* Toast提示 */
.toast {
  position: fixed;
  top: 30px;
  right: 30px;
  color: white;
  padding: 16px 22px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  gap: 12px;
  z-index: 1001;
  box-shadow: 0 6px 20px rgba(0, 0, 0, 0.15);
  animation: slideInRight 0.3s ease-out, fadeOut 0.3s ease-in 2.7s;
  max-width: 350px;
}

.toast.success {
  background: linear-gradient(135deg, #10b981 0%, #059669 100%);
}

.toast.error {
  background: linear-gradient(135deg, #ef4444 0%, #dc2626 100%);
}

.toast.info {
  background: linear-gradient(135deg, #3b82f6 0%, #2563eb 100%);
}

.toast-icon {
  font-size: 24px;
  flex-shrink: 0;
}

.toast-message {
  font-size: 16px;
  font-weight: 500;
}

@keyframes slideInRight {
  from {
    transform: translateX(100%);
    opacity: 0;
  }
  to {
    transform: translateX(0);
    opacity: 1;
  }
}

@keyframes fadeOut {
  from {
    opacity: 1;
  }
  to {
    opacity: 0;
    transform: translateY(-10px);
  }
}

/* 响应式设计 */
@media (max-width: 1024px) {
  .main-content {
    grid-template-columns: 1fr;
  }
  
  .date-display {
    flex-wrap: wrap;
  }
  
  .calendar-btn {
    margin-left: 0;
    margin-top: 10px;
  }
}

@media (max-width: 768px) {
  .main-content {
    padding: 15px;
    top: -60px;
  }
  
  .diary-header-decoration {
    height: 150px;
  }
  
  .wave-shape {
    height: 70px;
  }
  
  .date-display {
    padding: 20px;
  }
  
  .date-circle {
    width: 70px;
    height: 70px;
  }
  
  .date-day {
    font-size: 24px;
  }
  
  .card {
    padding: 20px;
  }
  
  .mood-option {
    min-width: calc(50% - 15px);
  }
  
  .action-buttons {
    flex-direction: column;
  }
  
  .btn {
    justify-content: center;
    width: 100%;
  }
}

@media (max-width: 480px) {
  .date-display {
    flex-direction: column;
    text-align: center;
  }
  
  .mood-option {
    min-width: 100%;
  }
  
  .toast {
    right: 15px;
    left: 15px;
    max-width: none;
  }
}
</style>