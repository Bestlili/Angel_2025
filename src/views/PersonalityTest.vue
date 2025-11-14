<template>
  <div class="test-page">
    <NavBar />
    <div class="test-container">
      <div class="test-header">
        <h2>性格特质分析</h2>
        <p>探索你的性格特质和优势，了解自己的行为模式和决策风格。</p>
        
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

// 人格测试题目数据
const questions = ref([
  {
    id: 1,
    text: '在社交场合中，你更倾向于：',
    options: [
      { value: 1, text: '主动与人交流，享受热闹氛围' },
      { value: 2, text: '观察为主，选择性交流' },
      { value: 3, text: '更愿意独处或与少数亲密朋友交流' },
      { value: 4, text: '尽量避免社交活动' }
    ]
  },
  {
    id: 2,
    text: '当需要做重要决定时，你通常会：',
    options: [
      { value: 1, text: '凭直觉快速做出决定' },
      { value: 2, text: '权衡利弊后做出决定' },
      { value: 3, text: '收集大量信息后仔细分析' },
      { value: 4, text: '寻求他人建议后再决定' }
    ]
  },
  {
    id: 3,
    text: '对于工作和生活，你更喜欢：',
    options: [
      { value: 1, text: '有明确的计划和安排' },
      { value: 2, text: '有大致方向，但保持灵活性' },
      { value: 3, text: '随机应变，享受变化' },
      { value: 4, text: '走一步看一步，不做太多规划' }
    ]
  },
  {
    id: 4,
    text: '当你面对压力时，你通常会：',
    options: [
      { value: 1, text: '积极寻找解决办法' },
      { value: 2, text: '向朋友或家人寻求支持' },
      { value: 3, text: '独自思考解决问题' },
      { value: 4, text: '暂时回避，等压力减轻再处理' }
    ]
  },
  {
    id: 5,
    text: '在团队合作中，你更倾向于：',
    options: [
      { value: 1, text: '担任领导角色，组织协调' },
      { value: 2, text: '提供创意和想法' },
      { value: 3, text: '认真执行分配的任务' },
      { value: 4, text: '支持和配合他人工作' }
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
    testType: 'personality',
    answers: answers.value
  };
  
  try {
    // 调用后端接口提交答案
    // const res = await api.post('/personality-test/submit', submitData);
    // 由于是演示，模拟提交成功
    console.log('提交的数据:', submitData);
    
    // 分析性格类型（示例逻辑）
    const totalScore = answers.value.reduce((sum, val) => sum + Number(val), 0);
    let personalityType = '';
    let personalityDesc = '';
    
    if (totalScore <= 8) {
      personalityType = '外向型';
      personalityDesc = '你是一个开朗、热情的人，喜欢社交活动，善于与人沟通。你精力充沛，富有创造力，在团队中通常是活跃分子。';
    } else if (totalScore <= 12) {
      personalityType = '平衡型';
      personalityDesc = '你在不同场合下能够灵活调整自己的行为方式，既能够享受社交活动，也能够独处思考。你适应性强，善于平衡各种关系。';
    } else {
      personalityType = '内向型';
      personalityDesc = '你是一个深思熟虑、注重内心世界的人。你喜欢独处，善于思考，在做决定时通常会认真分析。你重视深度交流而非广度。';
    }
    
    // 存储结果以便在结果页显示
    localStorage.setItem('testResult', JSON.stringify({
      type: 'personality',
      score: totalScore,
      answers: answers.value,
      personalityType: personalityType,
      personalityDesc: personalityDesc
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
</script>

<style scoped>
/* 样式与EmotionTest保持一致 */
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
  background-color: #dc3545;
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
  color: #dc3545;
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
  border-color: #dc3545;
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
  background-color: #dc3545;
  color: white;
}

.next-btn:hover:not(:disabled) {
  background-color: #c82333;
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
  background-color: #dc3545;
  color: white;
}

.question-index.answered {
  background-color: #28a745;
  color: white;
}
</style>