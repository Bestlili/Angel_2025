<template>
  <div class="test-page">
    <NavBar />
    <div class="test-container">
      <div class="test-header">
        <h2>睡眠质量评估</h2>
        <p>了解你的睡眠状况，找出影响睡眠质量的因素，并获得改善建议。</p>
        
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

// 睡眠质量测试题目数据
const questions = ref([
  {
    id: 1,
    text: '在过去一个月里，你通常需要多长时间才能入睡？',
    options: [
      { value: 1, text: '15分钟以内' },
      { value: 2, text: '16-30分钟' },
      { value: 3, text: '31-60分钟' },
      { value: 4, text: '60分钟以上' },
      { value: 5, text: '很难入睡，常常躺很久才能睡着' }
    ]
  },
  {
    id: 2,
    text: '在过去一个月里，你每晚醒来的次数是多少？',
    options: [
      { value: 1, text: '从不醒来' },
      { value: 2, text: '偶尔醒来一次' },
      { value: 3, text: '醒来2-3次' },
      { value: 4, text: '醒来4次或更多' },
      { value: 5, text: '频繁醒来，难以再次入睡' }
    ]
  },
  {
    id: 3,
    text: '在过去一个月里，你早上起床时的感受如何？',
    options: [
      { value: 1, text: '非常精神，休息充分' },
      { value: 2, text: '比较精神，休息良好' },
      { value: 3, text: '一般，不特别疲惫' },
      { value: 4, text: '有些疲惫' },
      { value: 5, text: '非常疲惫，感觉没有休息好' }
    ]
  },
  {
    id: 4,
    text: '在过去一个月里，你平均每晚的睡眠时间是多少？',
    options: [
      { value: 1, text: '7-8小时' },
      { value: 2, text: '6-7小时' },
      { value: 3, text: '5-6小时' },
      { value: 4, text: '4-5小时' },
      { value: 5, text: '少于4小时' }
    ]
  },
  {
    id: 5,
    text: '在过去一个月里，你对自己的睡眠质量总体评价如何？',
    options: [
      { value: 1, text: '非常好' },
      { value: 2, text: '良好' },
      { value: 3, text: '一般' },
      { value: 4, text: '较差' },
      { value: 5, text: '非常差' }
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
    testType: 'sleep',
    answers: answers.value
  };
  
  try {
    // 调用后端接口提交答案
    // const res = await api.test.submitTest('sleep', answers.value);
    // 由于是演示，模拟提交成功
    console.log('提交的数据:', submitData);
    
    // 分析睡眠质量（示例逻辑）
    // 注意这里分数越低表示睡眠质量越好
    const totalScore = answers.value.reduce((sum, val) => sum + Number(val), 0);
    let sleepQuality = '';
    let sleepDesc = '';
    let improvementTips = [];
    
    if (totalScore <= 7) {
      sleepQuality = '优质睡眠';
      sleepDesc = '你的睡眠质量非常好，入睡快，睡眠时间充足，睡眠过程稳定，起床后感觉精神饱满。继续保持良好的睡眠习惯，这对身心健康非常有益。';
      improvementTips = ['继续保持规律的作息时间', '保持良好的睡眠环境', '定期进行适量运动以维持睡眠质量'];
    } else if (totalScore <= 11) {
      sleepQuality = '良好睡眠';
      sleepDesc = '你的睡眠质量整体良好，可能偶尔会有一些小问题，但不影响日常生活。稍微注意一些细节可以让你的睡眠质量更加理想。';
      improvementTips = ['避免睡前使用电子设备', '睡前避免咖啡因和大量进食', '建立固定的睡前放松习惯', '确保卧室温度和光线适宜'];
    } else if (totalScore <= 15) {
      sleepQuality = '一般睡眠';
      sleepDesc = '你的睡眠质量处于一般水平，可能存在入睡较慢、夜间醒来或睡眠不足的情况。这些问题可能开始影响你的日常生活和工作效率，值得关注和改善。';
      improvementTips = ['制定并严格遵守规律的作息时间表', '改善睡眠环境（温度、光线、噪音）', '睡前进行放松活动，如冥想、深呼吸或轻度拉伸', '限制白天的咖啡因摄入', '睡前2-3小时避免剧烈运动'];
    } else if (totalScore <= 19) {
      sleepQuality = '较差睡眠';
      sleepDesc = '你的睡眠质量较差，存在较为明显的入睡困难、频繁醒来或睡眠不足等问题。这些问题可能已经对你的白天精力、情绪和健康产生了负面影响。';
      improvementTips = ['考虑咨询医生或睡眠专家', '建立完整的睡前放松流程', '避免酒精和尼古丁，它们会干扰睡眠质量', '白天增加适量的户外活动和日光暴露', '睡前避免饮水过多以减少夜间醒来'];
    } else {
      sleepQuality = '糟糕睡眠';
      sleepDesc = '你的睡眠质量非常差，存在严重的入睡困难、频繁醒来或睡眠不足等问题。这些问题已经显著影响了你的日常生活、工作效率和身心健康，需要积极寻求专业帮助。';
      improvementTips = ['立即咨询医生或睡眠专家', '考虑进行睡眠评估', '完全避免睡前使用电子设备', '尝试冥想或其他放松技巧', '建立严格的睡眠-觉醒时间表，包括周末', '检查是否有睡眠呼吸暂停等潜在睡眠障碍'];
    }
    
    // 存储结果以便在结果页显示
    localStorage.setItem('testResult', JSON.stringify({
      type: 'sleep',
      score: totalScore,
      answers: answers.value,
      sleepQuality: sleepQuality,
      sleepDesc: sleepDesc,
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
    const res = await api.test.getQuestions('sleep');
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
  background-color: #4169e1;
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
  color: #4169e1;
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
  border-color: #4169e1;
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
  background-color: #4169e1;
  color: white;
}

.next-btn:hover:not(:disabled) {
  background-color: #3656c0;
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
  background-color: #4169e1;
  color: white;
}

.question-index.answered {
  background-color: #4169e1;
  color: white;
}
</style>