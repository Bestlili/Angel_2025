<template>
  <div class="test-page">
    <NavBar />
    <div class="test-container">
      <div class="test-header">
        <h2>职业倾向评估</h2>
        <p>探索你的职业兴趣、能力倾向和工作价值观，找到最适合你的职业方向。</p>
        
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

// 职业测试题目数据
const questions = ref([
  {
    id: 1,
    text: '在工作中，你更倾向于：',
    options: [
      { value: 1, text: '与数据和事实打交道，解决复杂问题' },
      { value: 2, text: '与他人合作，帮助和指导他人' },
      { value: 3, text: '创造和设计新的产品或服务' },
      { value: 4, text: '组织和管理项目，确保任务完成' }
    ]
  },
  {
    id: 2,
    text: '你最享受的工作环境是：',
    options: [
      { value: 1, text: '安静、独立的空间，专注于研究和分析' },
      { value: 2, text: '充满互动和团队合作的开放环境' },
      { value: 3, text: '充满创意和活力的工作室氛围' },
      { value: 4, text: '结构化、有序的办公室环境' }
    ]
  },
  {
    id: 3,
    text: '你更喜欢什么样的工作任务？',
    options: [
      { value: 1, text: '需要深度思考和精确分析的任务' },
      { value: 2, text: '涉及与人沟通和解决人际问题的任务' },
      { value: 3, text: '允许自由发挥和创新的任务' },
      { value: 4, text: '明确目标和流程的任务' }
    ]
  },
  {
    id: 4,
    text: '在团队中，你通常扮演什么角色？',
    options: [
      { value: 1, text: '提供分析和解决方案的思考者' },
      { value: 2, text: '协调人际关系，确保团队和谐的协调者' },
      { value: 3, text: '提出创新想法和解决方案的创新者' },
      { value: 4, text: '组织和管理，确保任务按时完成的执行者' }
    ]
  },
  {
    id: 5,
    text: '你认为工作中最重要的回报是什么？',
    options: [
      { value: 1, text: '解决复杂问题带来的成就感' },
      { value: 2, text: '对他人生活产生积极影响' },
      { value: 3, text: '创造性表达和实现自己的想法' },
      { value: 4, text: '稳定的职业发展和物质回报' }
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
    testType: 'career',
    answers: answers.value
  };
  
  try {
    // 调用后端接口提交答案
    // const res = await api.test.submitTest('career', answers.value);
    // 由于是演示，模拟提交成功
    console.log('提交的数据:', submitData);
    
    // 分析职业倾向（示例逻辑）
    const typeCounts = { 1: 0, 2: 0, 3: 0, 4: 0 };
    answers.value.forEach(answer => {
      typeCounts[answer]++;
    });
    
    let maxCount = 0;
    let dominantType = 1;
    for (let i = 1; i <= 4; i++) {
      if (typeCounts[i] > maxCount) {
        maxCount = typeCounts[i];
        dominantType = i;
      }
    }
    
    let careerType = '';
    let careerDesc = '';
    let recommendedFields = [];
    
    switch (dominantType) {
      case 1:
        careerType = '分析型';
        careerDesc = '你倾向于逻辑思考、分析和解决复杂问题。你注重精确性和细节，喜欢与数据和概念打交道。';
        recommendedFields = ['科学研究', '数据分析', '工程', '技术研发'];
        break;
      case 2:
        careerType = '社会型';
        careerDesc = '你关注他人的需求，善于与人交往和沟通。你享受帮助和指导他人，追求对社会产生积极影响。';
        recommendedFields = ['教育', '医疗健康', '咨询', '社会工作'];
        break;
      case 3:
        careerType = '艺术型';
        careerDesc = '你富有创造力和想象力，喜欢表达和创新。你倾向于非结构化的工作环境，追求个人表达和独特性。';
        recommendedFields = ['设计', '艺术', '媒体', '创意写作'];
        break;
      case 4:
        careerType = '管理型';
        careerDesc = '你善于组织、规划和管理资源。你注重效率和成果，具有良好的决策能力和领导才能。';
        recommendedFields = ['企业管理', '项目管理', '市场营销', '金融'];
        break;
    }
    
    // 存储结果以便在结果页显示
    localStorage.setItem('testResult', JSON.stringify({
      type: 'career',
      answers: answers.value,
      careerType: careerType,
      careerDesc: careerDesc,
      recommendedFields: recommendedFields
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

// 从后端获取题目
const fetchQuestions = async () => {
  try {
    const res = await api.test.getQuestions('career');
    questions.value = res.questions;
    answers.value = Array(questions.value.length).fill(null);
  } catch (error) {
    console.error('获取题目失败:', error);
    alert('获取题目失败，请稍后重试');
  }
};
</script>

<style scoped>
/* 样式与其他测试页面保持一致，但使用不同的主题色 */
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
  background-color: #17a2b8;
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
  color: #17a2b8;
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
  border-color: #17a2b8;
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
  background-color: #17a2b8;
  color: white;
}

.next-btn:hover:not(:disabled) {
  background-color: #138496;
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
  background-color: #17a2b8;
  color: white;
}

.question-index.answered {
  background-color: #17a2b8;
  color: white;
}
</style>