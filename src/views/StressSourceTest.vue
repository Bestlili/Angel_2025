<template>
  <div class="test-page">
    <NavBar />
    <div class="test-container">
      <div class="test-header">
        <h2>压力源评估</h2>
        <p>识别你的主要压力来源，了解压力对你的影响，并获得针对性的应对建议。</p>
        
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

// 压力源测试题目数据
const questions = ref([
  {
    id: 1,
    text: '在过去一个月中，工作或学习给你带来的压力程度是：',
    options: [
      { value: 1, text: '几乎没有压力' },
      { value: 2, text: '轻度压力' },
      { value: 3, text: '中度压力' },
      { value: 4, text: '高度压力' },
      { value: 5, text: '极大压力，难以承受' }
    ]
  },
  {
    id: 2,
    text: '在过去一个月中，人际关系（家庭、朋友、同事等）给你带来的压力程度是：',
    options: [
      { value: 1, text: '几乎没有压力' },
      { value: 2, text: '轻度压力' },
      { value: 3, text: '中度压力' },
      { value: 4, text: '高度压力' },
      { value: 5, text: '极大压力，难以承受' }
    ]
  },
  {
    id: 3,
    text: '在过去一个月中，财务问题给你带来的压力程度是：',
    options: [
      { value: 1, text: '几乎没有压力' },
      { value: 2, text: '轻度压力' },
      { value: 3, text: '中度压力' },
      { value: 4, text: '高度压力' },
      { value: 5, text: '极大压力，难以承受' }
    ]
  },
  {
    id: 4,
    text: '在过去一个月中，健康状况给你带来的压力程度是：',
    options: [
      { value: 1, text: '几乎没有压力' },
      { value: 2, text: '轻度压力' },
      { value: 3, text: '中度压力' },
      { value: 4, text: '高度压力' },
      { value: 5, text: '极大压力，难以承受' }
    ]
  },
  {
    id: 5,
    text: '在过去一个月中，生活节奏和时间管理给你带来的压力程度是：',
    options: [
      { value: 1, text: '几乎没有压力' },
      { value: 2, text: '轻度压力' },
      { value: 3, text: '中度压力' },
      { value: 4, text: '高度压力' },
      { value: 5, text: '极大压力，难以承受' }
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
    testType: 'stress',
    answers: answers.value
  };
  
  try {
    // 调用后端接口提交答案
    // const res = await api.test.submitTest('stress', answers.value);
    // 由于是演示，模拟提交成功
    console.log('提交的数据:', submitData);
    
    // 分析压力源和压力水平（示例逻辑）
    // 计算各领域的压力分数
    const workPressure = Number(answers.value[0]);
    const relationshipPressure = Number(answers.value[1]);
    const financialPressure = Number(answers.value[2]);
    const healthPressure = Number(answers.value[3]);
    const timePressure = Number(answers.value[4]);
    
    // 计算总体压力水平
    const totalScore = answers.value.reduce((sum, val) => sum + Number(val), 0);
    const avgScore = totalScore / answers.value.length;
    
    // 判断主要压力源
    const pressureSources = [];
    if (workPressure >= 4) pressureSources.push('工作/学习');
    if (relationshipPressure >= 4) pressureSources.push('人际关系');
    if (financialPressure >= 4) pressureSources.push('财务问题');
    if (healthPressure >= 4) pressureSources.push('健康状况');
    if (timePressure >= 4) pressureSources.push('时间管理');
    
    // 如果没有特别突出的压力源，找出分数最高的两个
    if (pressureSources.length === 0) {
      const pressures = [
        { name: '工作/学习', value: workPressure },
        { name: '人际关系', value: relationshipPressure },
        { name: '财务问题', value: financialPressure },
        { name: '健康状况', value: healthPressure },
        { name: '时间管理', value: timePressure }
      ];
      pressures.sort((a, b) => b.value - a.value);
      pressureSources.push(pressures[0].name);
      if (pressures[1].value > 2) pressureSources.push(pressures[1].name);
    }
    
    // 判断总体压力水平
    let pressureLevel = '';
    let pressureDesc = '';
    let improvementTips = [];
    
    if (avgScore <= 1.5) {
      pressureLevel = '低压力水平';
      pressureDesc = '你目前的压力水平较低，各个方面都比较平衡。这是一个理想的状态，说明你有良好的压力管理能力，或者生活环境较为舒适。继续保持这种状态。';
      improvementTips = ['继续保持良好的生活习惯', '定期进行放松活动以维持低压力状态', '建立健康的应对机制，为未来可能的压力做好准备'];
    } else if (avgScore <= 2.5) {
      pressureLevel = '轻度压力';
      pressureDesc = '你目前处于轻度压力状态，压力在可接受的范围内，不会对生活和健康造成明显影响。适当的压力可以激发动力，但仍需注意观察压力的变化趋势。';
      improvementTips = ['学习简单的压力管理技巧如深呼吸、冥想', '保持规律的作息和适度运动', '培养兴趣爱好，丰富生活内容'];
    } else if (avgScore <= 3.5) {
      pressureLevel = '中度压力';
      pressureDesc = '你正经历中度压力，压力已经开始对日常生活产生一定影响。主要压力来源于' + pressureSources.join('和') + '。建议你采取积极的压力管理措施，防止压力进一步增加。';
      improvementTips = ['优先处理主要压力源', '学习时间管理和任务优先级排序', '增加社会支持，与亲友分享感受', '定期进行放松活动和运动'];
    } else if (avgScore <= 4.5) {
      pressureLevel = '高度压力';
      pressureDesc = '你正承受高度压力，压力已经显著影响了你的日常生活和身心健康。主要压力来源于' + pressureSources.join('和') + '。这种状态需要认真对待，积极采取措施缓解压力。';
      improvementTips = ['识别并解决主要压力源', '必要时寻求专业心理咨询', '实践放松技巧，如正念冥想、渐进式肌肉放松', '确保充足的休息和睡眠', '调整期望值，避免过度追求完美'];
    } else {
      pressureLevel = '严重压力';
      pressureDesc = '你正在经历严重的压力，已经对身心健康造成了显著负面影响。主要压力来源于' + pressureSources.join('和') + '。这种状态需要立即关注和专业干预，以防止压力导致更严重的健康问题。';
      improvementTips = ['立即寻求专业心理帮助或咨询', '考虑暂时减少工作/学习负担', '建立强大的社会支持网络', '学习和实践有效的压力管理技巧', '关注身体健康，确保营养和睡眠', '必要时与医生讨论压力对身体健康的影响'];
    }
    
    // 为主要压力源提供针对性建议
    let sourceSpecificTips = [];
    if (pressureSources.includes('工作/学习')) {
      sourceSpecificTips.push('评估工作/学习负担，适当调整期望值', '学习有效的时间管理技巧，避免拖延', '与上级/老师沟通，寻求合理的工作量安排', '考虑工作/学习方法的改进，提高效率');
    }
    if (pressureSources.includes('人际关系')) {
      sourceSpecificTips.push('学习有效沟通技巧，改善人际关系', '设定健康的边界，学会说"不"', '寻求关系咨询，改善重要关系', '参加社交活动，扩大社交圈');
    }
    if (pressureSources.includes('财务问题')) {
      sourceSpecificTips.push('制定合理的预算和财务计划', '寻求财务咨询或建议', '考虑增加收入来源或减少不必要开支', '学习基本的投资理财知识');
    }
    if (pressureSources.includes('健康状况')) {
      sourceSpecificTips.push('定期体检，关注身体健康', '遵循医生建议，积极治疗现有健康问题', '改善生活习惯，包括饮食、运动和睡眠', '学习管理慢性健康问题的技巧');
    }
    if (pressureSources.includes('时间管理')) {
      sourceSpecificTips.push('使用时间管理工具，如待办清单、日程表', '学习区分任务优先级，专注重要事项', '减少分心，提高工作/学习效率', '适当安排休息时间，避免过度劳累');
    }
    
    // 存储结果以便在结果页显示
    localStorage.setItem('testResult', JSON.stringify({
      type: 'stress',
      score: totalScore,
      avgScore: avgScore,
      answers: answers.value,
      pressureLevel: pressureLevel,
      pressureDesc: pressureDesc,
      mainPressureSources: pressureSources,
      generalTips: improvementTips,
      sourceSpecificTips: sourceSpecificTips
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
    const res = await api.test.getQuestions('stress');
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
  background-color: #dc3545;
  color: white;
}
</style>