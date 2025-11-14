<template>
  <div class="test-page">
    <NavBar />
    <div class="test-container">
      <div class="test-header">
        <h2>人际关系模式</h2>
        <p>评估你的人际交往模式和依恋类型，了解在关系中的优势与挑战。</p>
        
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

// 人际关系测试题目数据
const questions = ref([
  {
    id: 1,
    text: '在亲密关系中，你如何看待依赖对方？',
    options: [
      { value: 1, text: '非常舒适，认为依赖是关系的自然部分' },
      { value: 2, text: '基本舒适，但也希望保持一定独立性' },
      { value: 3, text: '有些不安，担心过度依赖会失去自我' },
      { value: 4, text: '非常不安，倾向于保持情感距离' }
    ]
  },
  {
    id: 2,
    text: '当与他人发生冲突时，你通常会：',
    options: [
      { value: 1, text: '直接沟通，希望尽快解决问题' },
      { value: 2, text: '尝试理解对方观点，寻找共同点' },
      { value: 3, text: '避免冲突，尽量保持和谐' },
      { value: 4, text: '倾向于回避，等待问题自然解决' }
    ]
  },
  {
    id: 3,
    text: '你如何看待与他人分享自己的情感和想法？',
    options: [
      { value: 1, text: '很乐意分享，认为这是建立信任的方式' },
      { value: 2, text: '选择性分享，取决于关系的亲密程度' },
      { value: 3, text: '不太愿意分享，担心被误解或伤害' },
      { value: 4, text: '很少分享，更喜欢将情感保留在内心' }
    ]
  },
  {
    id: 4,
    text: '当朋友或伴侣需要你的帮助时，你通常会：',
    options: [
      { value: 1, text: '立即提供帮助，不求回报' },
      { value: 2, text: '尽量提供帮助，但也考虑自己的能力' },
      { value: 3, text: '在不影响自己的情况下提供帮助' },
      { value: 4, text: '谨慎对待，担心被过度依赖' }
    ]
  },
  {
    id: 5,
    text: '你如何处理与他人的分别或关系结束？',
    options: [
      { value: 1, text: '感到难过但能够接受，相信时间会治愈' },
      { value: 2, text: '会有失落感，但能够逐步适应' },
      { value: 3, text: '很难接受，需要很长时间恢复' },
      { value: 4, text: '倾向于避免深入关系，以减少分别的痛苦' }
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
    testType: 'relationship',
    answers: answers.value
  };
  
  try {
    // 调用后端接口提交答案
    // const res = await api.test.submitTest('relationship', answers.value);
    // 由于是演示，模拟提交成功
    console.log('提交的数据:', submitData);
    
    // 分析人际关系类型（示例逻辑）
    const totalScore = answers.value.reduce((sum, val) => sum + Number(val), 0);
    let relationshipType = '';
    let relationshipDesc = '';
    
    if (totalScore <= 8) {
      relationshipType = '安全型';
      relationshipDesc = '你在人际关系中表现出安全感和信任感，能够舒适地依赖他人，也允许他人依赖自己。你能够有效地表达情感，处理冲突，建立健康、稳定的关系。';
    } else if (totalScore <= 12) {
      relationshipType = '平衡型';
      relationshipDesc = '你在人际关系中既注重亲密性，也重视独立性。你能够建立良好的关系，但有时可能会在情感表达或冲突处理上有所保留。总体而言，你的人际关系模式是健康的。';
    } else {
      relationshipType = '谨慎型';
      relationshipDesc = '你在人际关系中可能表现出一定的谨慎和自我保护倾向。你可能不太容易完全信任他人，或者在情感表达上有所保留。这可能与过去的经历有关，有意识地尝试开放自己可能会帮助你建立更深入的关系。';
    }
    
    // 存储结果以便在结果页显示
    localStorage.setItem('testResult', JSON.stringify({
      type: 'relationship',
      score: totalScore,
      answers: answers.value,
      relationshipType: relationshipType,
      relationshipDesc: relationshipDesc
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
    const res = await api.test.getQuestions('relationship');
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
  background-color: #28a745;
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
  color: #28a745;
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
  border-color: #28a745;
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
  background-color: #28a745;
  color: white;
}

.next-btn:hover:not(:disabled) {
  background-color: #218838;
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
  background-color: #28a745;
  color: white;
}

.question-index.answered {
  background-color: #28a745;
  color: white;
}
</style>