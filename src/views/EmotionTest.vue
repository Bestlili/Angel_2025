<template>
  <div class="test-page">
    <NavBar />
    <div class="test-container">
      <div class="test-header">
        <h2>情绪状态评估</h2>
        <p>请根据以下题目如实作答，完成后将为你生成情绪状态报告。</p>
        
        <!-- 进度条 -->
        <div class="progress-container">
          <div class="progress-bar">
            <div class="progress-fill" :style="{ width: progressPercentage + '%' }"></div>
          </div>
          <div class="progress-text">{{ currentQuestionIndex + 1 }}/{{ questions.length }} 题</div>
        </div>
      </div>

      <!-- 当前题目 -->
      <div v-if="currentQuestion" class="question">
        <div class="question-header">
          <span class="question-number">{{ currentQuestionIndex + 1 }}. </span>
          <span class="question-text">{{ currentQuestion.text }}</span>
        </div>
        <div class="options">
          <label v-for="option in currentQuestion.options" :key="option.value" class="option">
            <input 
              type="radio" 
              :name="'q' + currentQuestionIndex" 
              :value="option.value"
              v-model="currentAnswer"
            >
            <span class="option-text">{{ option.text }}</span>
          </label>
        </div>
      </div>

      <!-- 题目导航 -->
      <div class="navigation-buttons">
        <button class="nav-btn prev-btn" @click="prevQuestion" :disabled="currentQuestionIndex === 0">
          上一题
        </button>
        <button class="nav-btn next-btn" @click="nextQuestion" :disabled="!currentAnswer">
          {{ isLastQuestion ? '提交测试' : '下一题' }}
        </button>
      </div>

      <!-- 题目索引 -->
      <div class="question-indices">
        <div 
          v-for="(q, index) in questions" 
          :key="index"
          class="question-index"
          :class="{ 
            'active': index === currentQuestionIndex,
            'answered': answers[index] !== null
          }"
          @click="goToQuestion(index)"
        >
          {{ index + 1 }}
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import NavBar from '../components/NavBar.vue';
import api from '../api/index';

const router = useRouter();

// 示例题目数据（实际应从后端获取）
const questions = ref([
  {
    id: 1,
    text: '你最近是否经常感到紧张或焦虑？',
    options: [
      { value: 1, text: '完全没有' },
      { value: 2, text: '偶尔' },
      { value: 3, text: '经常' },
      { value: 4, text: '总是' }
    ]
  },
  {
    id: 2,
    text: '你是否感到情绪低落或沮丧？',
    options: [
      { value: 1, text: '完全没有' },
      { value: 2, text: '偶尔' },
      { value: 3, text: '经常' },
      { value: 4, text: '总是' }
    ]
  },
  {
    id: 3,
    text: '你是否对平时感兴趣的事情失去了兴趣？',
    options: [
      { value: 1, text: '完全没有' },
      { value: 2, text: '偶尔' },
      { value: 3, text: '经常' },
      { value: 4, text: '总是' }
    ]
  },
  {
    id: 4,
    text: '你是否感到精力不足或容易疲劳？',
    options: [
      { value: 1, text: '完全没有' },
      { value: 2, text: '偶尔' },
      { value: 3, text: '经常' },
      { value: 4, text: '总是' }
    ]
  },
  {
    id: 5,
    text: '你是否能够专注于工作或日常活动？',
    options: [
      { value: 4, text: '完全可以' },
      { value: 3, text: '大部分时候可以' },
      { value: 2, text: '偶尔可以' },
      { value: 1, text: '几乎不能' }
    ]
  }
]);

// 当前问题索引
const currentQuestionIndex = ref(0);
// 当前问题的答案
const currentAnswer = ref(null);
// 所有问题的答案
const answers = ref(Array(questions.value.length).fill(null));

// 计算当前问题
const currentQuestion = computed(() => {
  return questions.value[currentQuestionIndex.value];
});

// 判断是否是最后一题
const isLastQuestion = computed(() => {
  return currentQuestionIndex.value === questions.value.length - 1;
});

// 计算进度百分比
const progressPercentage = computed(() => {
  const answeredCount = answers.value.filter(a => a !== null).length;
  return (answeredCount / questions.value.length) * 100;
});

// 下一题
const nextQuestion = async () => {
  if (!currentAnswer.value) return;
  
  // 保存当前答案
  answers.value[currentQuestionIndex.value] = currentAnswer.value;
  
  if (isLastQuestion.value) {
    // 如果是最后一题，提交测试
    await submitTest();
  } else {
    // 否则前进到下一题
    currentQuestionIndex.value++;
    // 设置当前题目的已有答案（如果有）
    currentAnswer.value = answers.value[currentQuestionIndex.value];
  }
};

// 上一题
const prevQuestion = () => {
  if (currentQuestionIndex.value > 0) {
    // 保存当前答案
    answers.value[currentQuestionIndex.value] = currentAnswer.value;
    // 前进到上一题
    currentQuestionIndex.value--;
    // 设置当前题目的已有答案
    currentAnswer.value = answers.value[currentQuestionIndex.value];
  }
};

// 跳转到指定题目
const goToQuestion = (index) => {
  if (index !== currentQuestionIndex.value) {
    // 保存当前答案
    answers.value[currentQuestionIndex.value] = currentAnswer.value;
    // 跳转到指定题目
    currentQuestionIndex.value = index;
    // 设置当前题目的已有答案
    currentAnswer.value = answers.value[currentQuestionIndex.value];
  }
};

// 提交测试
const submitTest = async () => {
  // 确保所有题目都已回答
  if (answers.value.some(answer => answer === null)) {
    alert('请回答所有问题后再提交');
    return;
  }
  
  // 构建提交数据
  const submitData = {
    testType: 'emotion',
    answers: answers.value
  };
  
  try {
    // 调用后端接口提交答案
    // const res = await api.post('/emotion-test/submit', submitData);
    // 由于是演示，模拟提交成功
    console.log('提交的数据:', submitData);
    
    // 计算总分（示例）
    const totalScore = answers.value.reduce((sum, val) => sum + Number(val), 0);
    
    // 存储结果以便在结果页显示
    localStorage.setItem('testResult', JSON.stringify({
      type: 'emotion',
      score: totalScore,
      answers: answers.value
    }));
    
    // 跳转到结果页
    router.push('/tests/result');
  } catch (error) {
    console.error('测试提交失败:', error);
    alert('提交失败，请稍后重试');
  }
};

// 组件挂载时
onMounted(() => {
  // 实际项目中应该从后端获取题目
  // fetchQuestions();
});

// 从后端获取题目（使用统一的API方法）
const fetchQuestions = async () => {
  try {
    const res = await api.test.getQuestions('emotion');
    questions.value = res.questions;
    answers.value = Array(questions.value.length).fill(null);
  } catch (error) {
    console.error('获取题目失败:', error);
    alert('获取题目失败，请稍后重试');
  }
};
</script>

<style scoped>
.test-page {
  padding: 20px;
  min-height: 100vh;
  background-color: #f8f9fa;
}

.test-container {
  max-width: 800px;
  margin: 0 auto;
  background-color: white;
  border-radius: 12px;
  padding: 30px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.08);
}

.test-header {
  text-align: center;
  margin-bottom: 30px;
}

.test-header h2 {
  color: #333;
  margin-bottom: 10px;
  font-size: 28px;
}

.test-header p {
  color: #666;
  margin-bottom: 20px;
}

.progress-container {
  margin-top: 20px;
}

.progress-bar {
  width: 100%;
  height: 10px;
  background-color: #e9ecef;
  border-radius: 5px;
  overflow: hidden;
  margin-bottom: 10px;
}

.progress-fill {
  height: 100%;
  background-color: #673ab7;
  transition: width 0.3s ease;
}

.progress-text {
  text-align: center;
  font-size: 14px;
  color: #666;
}

.question {
  margin-bottom: 30px;
  padding: 20px;
  border: 1px solid #e9ecef;
  border-radius: 8px;
  background-color: #fafafa;
}

.question-header {
  margin-bottom: 20px;
  font-size: 18px;
  line-height: 1.5;
}

.question-number {
  font-weight: bold;
  color: #673ab7;
  margin-right: 8px;
}

.options {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.option {
  display: flex;
  align-items: center;
  padding: 12px 16px;
  border: 1px solid #ddd;
  border-radius: 6px;
  cursor: pointer;
  transition: all 0.2s;
  background-color: white;
}

.option:hover {
  background-color: #f8f9fa;
  border-color: #673ab7;
}

.option input[type="radio"] {
  margin-right: 12px;
}

.navigation-buttons {
  display: flex;
  justify-content: space-between;
  margin-top: 30px;
  margin-bottom: 20px;
}

.nav-btn {
  padding: 12px 24px;
  border: none;
  border-radius: 6px;
  cursor: pointer;
  font-size: 16px;
  transition: background-color 0.3s;
}

.prev-btn {
  background-color: #e9ecef;
  color: #495057;
}

.prev-btn:hover:not(:disabled) {
  background-color: #dee2e6;
}

.next-btn {
  background-color: #673ab7;
  color: white;
}

.next-btn:hover:not(:disabled) {
  background-color: #562d91;
}

.nav-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.question-indices {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  justify-content: center;
  margin-top: 20px;
}

.question-index {
  width: 36px;
  height: 36px;
  border-radius: 50%;
  background-color: #e9ecef;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  transition: all 0.2s;
  font-size: 14px;
}

.question-index:hover {
  background-color: #dee2e6;
}

.question-index.active {
  background-color: #673ab7;
  color: white;
}

.question-index.answered {
  background-color: #28a745;
  color: white;
}
</style>