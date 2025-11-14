<template>
  <div class="test-page">
    <NavBar />
    <div class="test-container">
      <div class="test-header">
        <h2>幸福感评估</h2>
        <p>探索你当前的幸福状态，了解影响你幸福感的因素和提升方向。</p>
        
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

// 幸福感测试题目数据
const questions = ref([
  {
    id: 1,
    text: '在过去的一个月里，你有多少次感到真正的快乐或满足？',
    options: [
      { value: 1, text: '几乎每天' },
      { value: 2, text: '大多数日子' },
      { value: 3, text: '有时' },
      { value: 4, text: '很少' },
      { value: 5, text: '几乎从不' }
    ]
  },
  {
    id: 2,
    text: '你对自己的生活总体满意度如何？',
    options: [
      { value: 1, text: '非常满意' },
      { value: 2, text: '相当满意' },
      { value: 3, text: '一般' },
      { value: 4, text: '不太满意' },
      { value: 5, text: '非常不满意' }
    ]
  },
  {
    id: 3,
    text: '你觉得自己的生活有意义和目标感吗？',
    options: [
      { value: 1, text: '非常有意义和目标' },
      { value: 2, text: '比较有意义和目标' },
      { value: 3, text: '一般' },
      { value: 4, text: '意义和目标感不强' },
      { value: 5, text: '完全没有意义和目标' }
    ]
  },
  {
    id: 4,
    text: '你对未来的期望和展望是怎样的？',
    options: [
      { value: 1, text: '非常乐观' },
      { value: 2, text: '比较乐观' },
      { value: 3, text: '中性' },
      { value: 4, text: '比较悲观' },
      { value: 5, text: '非常悲观' }
    ]
  },
  {
    id: 5,
    text: '在人际关系中，你感到被爱和被接纳的程度如何？',
    options: [
      { value: 1, text: '非常被爱和接纳' },
      { value: 2, text: '比较被爱和接纳' },
      { value: 3, text: '一般' },
      { value: 4, text: '不太被爱和接纳' },
      { value: 5, text: '几乎不被爱和接纳' }
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
    testType: 'happiness',
    answers: answers.value
  };
  
  try {
    // 调用后端接口提交答案
    // const res = await api.test.submitTest('happiness', answers.value);
    // 由于是演示，模拟提交成功
    console.log('提交的数据:', submitData);
    
    // 分析幸福感水平（示例逻辑）
    // 注意这里分数越低表示幸福感越高（因为1分代表最积极的回答）
    const totalScore = answers.value.reduce((sum, val) => sum + Number(val), 0);
    let happinessLevel = '';
    let happinessDesc = '';
    let improvementTips = [];
    
    if (totalScore <= 8) {
      happinessLevel = '高幸福感';
      happinessDesc = '你当前处于较高的幸福状态，对生活总体满意，有明确的生活目标，对未来充满希望，并且在人际关系中感受到爱与接纳。继续保持积极的心态和生活方式。';
      improvementTips = ['培养感恩之心，记录每天的美好时刻', '保持健康的生活习惯', '继续深化和维护良好的人际关系'];
    } else if (totalScore <= 12) {
      happinessLevel = '中等幸福感';
      happinessDesc = '你的幸福感处于中等水平，总体上对生活较为满意，但在某些方面可能还有提升空间。通过有意识地关注积极因素和进行适当调整，你可以进一步提高幸福感。';
      improvementTips = ['增加令人愉悦的活动', '设定可实现的目标', '改善人际关系质量', '学习压力管理技巧'];
    } else if (totalScore <= 18) {
      happinessLevel = '较低幸福感';
      happinessDesc = '你当前的幸福感水平偏低，可能在生活满意度、目标感、未来期望或人际关系方面遇到了一些挑战。这是一个重要的信号，表明你需要关注自己的心理健康并采取积极措施。';
      improvementTips = ['寻求社会支持，与亲友分享感受', '培养兴趣爱好，增加生活乐趣', '考虑咨询专业心理帮助', '建立规律的作息和运动习惯'];
    } else {
      happinessLevel = '低幸福感';
      happinessDesc = '你当前的幸福感水平较低，这可能是暂时的状态，也可能反映了更深层次的问题。建议你认真对待这种感受，并积极寻求帮助和支持，采取实际行动来改善你的心理健康状况。';
      improvementTips = ['立即寻求专业心理帮助', '与信任的人建立连接', '设定小而具体的目标', '关注自我照顾，包括充足的休息、健康饮食和适量运动'];
    }
    
    // 存储结果以便在结果页显示
    localStorage.setItem('testResult', JSON.stringify({
      type: 'happiness',
      score: totalScore,
      answers: answers.value,
      happinessLevel: happinessLevel,
      happinessDesc: happinessDesc,
      improvementTips: improvementTips
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
    const res = await api.test.getQuestions('happiness');
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
  background-color: #ffc107;
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
  color: #ffc107;
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
  border-color: #ffc107;
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
  background-color: #ffc107;
  color: #212529;
}

.next-btn:hover:not(:disabled) {
  background-color: #e0a800;
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
  background-color: #ffc107;
  color: #212529;
}

.question-index.answered {
  background-color: #ffc107;
  color: #212529;
}
</style>