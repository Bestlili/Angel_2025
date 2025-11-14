<template>
  <div class="test-page">
    <NavBar />
    <div class="test-container">
      <div class="test-header">
        <h2>成长潜能评估</h2>
        <p>探索你的学习能力、适应能力和个人成长动力，了解你的发展潜力和成长方向。</p>
        
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

// 成长潜能测试题目数据
const questions = ref([
  {
    id: 1,
    text: '面对新的挑战或困难时，你通常的反应是：',
    options: [
      { value: 1, text: '感到兴奋，将其视为成长的机会' },
      { value: 2, text: '有些紧张，但愿意尝试' },
      { value: 3, text: '感到压力，希望能避免' },
      { value: 4, text: '非常不安，尽量回避' }
    ]
  },
  {
    id: 2,
    text: '当你犯了错误时，你会：',
    options: [
      { value: 1, text: '分析错误原因，从中学习' },
      { value: 2, text: '接受反馈并尝试改进' },
      { value: 3, text: '感到沮丧，但会继续前进' },
      { value: 4, text: '尽量避免承认错误' }
    ]
  },
  {
    id: 3,
    text: '你对学习新技能或知识的态度是：',
    options: [
      { value: 1, text: '非常热衷，主动寻找学习机会' },
      { value: 2, text: '有兴趣，会在需要时学习' },
      { value: 3, text: '一般，只有必要时才学习' },
      { value: 4, text: '不太感兴趣，更喜欢已掌握的知识' }
    ]
  },
  {
    id: 4,
    text: '当你需要适应新的环境或变化时，你通常：',
    options: [
      { value: 1, text: '快速适应并积极投入' },
      { value: 2, text: '逐渐适应，保持开放态度' },
      { value: 3, text: '需要时间，感到有些不适' },
      { value: 4, text: '很难适应，怀念过去' }
    ]
  },
  {
    id: 5,
    text: '你设定个人目标的方式是：',
    options: [
      { value: 1, text: '设定具有挑战性但可实现的目标' },
      { value: 2, text: '设定明确但相对保守的目标' },
      { value: 3, text: '很少设定具体目标，随遇而安' },
      { value: 4, text: '避免设定目标，担心失败' }
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
    testType: 'growth',
    answers: answers.value
  };
  
  try {
    // 调用后端接口提交答案
    // const res = await api.test.submitTest('growth', answers.value);
    // 由于是演示，模拟提交成功
    console.log('提交的数据:', submitData);
    
    // 分析成长潜能（示例逻辑）
    // 注意这里分数越低表示成长潜能越高（因为1分代表最积极的回答）
    const totalScore = answers.value.reduce((sum, val) => sum + Number(val), 0);
    let growthLevel = '';
    let growthDesc = '';
    let strengths = [];
    let developmentAreas = [];
    
    if (totalScore <= 8) {
      growthLevel = '高成长型';
      growthDesc = '你展现出极强的成长潜能。你拥抱挑战，从错误中学习，主动寻求新知识和技能，并能快速适应变化。你的心态和行为方式非常有利于持续成长和发展。';
      strengths = ['拥抱挑战', '持续学习', '快速适应', '积极心态'];
      developmentAreas = ['设定更长远的发展目标', '培养指导他人成长的能力'];
    } else if (totalScore <= 12) {
      growthLevel = '平衡成长型';
      growthDesc = '你具有良好的成长潜能。你能够应对挑战，接受反馈，并在需要时学习新技能。你对变化保持相对开放的态度，但有时可能需要更多的动力来主动寻求成长机会。';
      strengths = ['适应能力', '学习意愿', '接受反馈'];
      developmentAreas = ['增强主动学习意识', '培养面对挑战的积极心态'];
    } else {
      growthLevel = '待发展型';
      growthDesc = '你可能在某些方面限制了自己的成长潜能。你可能对挑战和变化感到不安，在学习新技能时缺乏动力，或者难以从错误中有效学习。不过，成长心态是可以培养的，通过有意识的努力，你可以显著提升自己的发展潜力。';
      strengths = ['稳定性', '对熟悉领域的深入理解'];
      developmentAreas = ['培养成长型思维', '逐步面对挑战', '建立学习习惯', '增强适应变化的能力'];
    }
    
    // 存储结果以便在结果页显示
    localStorage.setItem('testResult', JSON.stringify({
      type: 'growth',
      score: totalScore,
      answers: answers.value,
      growthLevel: growthLevel,
      growthDesc: growthDesc,
      strengths: strengths,
      developmentAreas: developmentAreas
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
    const res = await api.test.getQuestions('growth');
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
  background-color: #6f42c1;
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
  color: #6f42c1;
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
  border-color: #6f42c1;
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
  background-color: #6f42c1;
  color: white;
}

.next-btn:hover:not(:disabled) {
  background-color: #5a32a3;
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
  background-color: #6f42c1;
  color: white;
}

.question-index.answered {
  background-color: #6f42c1;
  color: white;
}
</style>