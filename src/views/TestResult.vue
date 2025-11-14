<template>
  <div class="result-page">
    <NavBar />
    <div class="result-container">
      <!-- 加载状态 -->
      <div v-if="loading" class="loading">
        <div class="loading-spinner"></div>
        <p>正在分析您的测试结果...</p>
      </div>

      <!-- 无结果状态 -->
      <div v-else-if="!result" class="no-result">
        <h2>暂无测试结果</h2>
        <p>请先完成一次心理测试</p>
        <router-link to="/tests" class="back-btn">返回测试列表</router-link>
      </div>

      <!-- 结果展示 -->
      <div v-else class="result-content">
        <!-- 情绪测试结果 -->
        <div v-if="result.type === 'emotion'" class="emotion-result">
          <div class="result-header">
            <div class="result-icon emotion-icon">
              <i class="fa-solid fa-heart"></i>
            </div>
            <h2>情绪状态评估结果</h2>
          </div>

          <div class="score-section">
            <div class="score-circle">
              <div class="score-number">{{ result.score }}</div>
              <div class="score-label">总分</div>
            </div>
            <div class="score-description">
              <p>{{ getEmotionDescription(result.score) }}</p>
            </div>
          </div>

          <div class="analysis-section">
            <h3>详细分析</h3>
            <div class="analysis-content">
              {{ getEmotionAnalysis(result.score) }}
            </div>
          </div>

          <div class="recommendations-section">
            <h3>建议与改善</h3>
            <ul class="recommendations-list">
              <li v-for="(rec, index) in getEmotionRecommendations(result.score)" :key="index">
                {{ rec }}
              </li>
            </ul>
          </div>
        </div>

        <!-- 人格测试结果 -->
        <div v-else-if="result.type === 'personality'" class="personality-result">
          <div class="result-header">
            <div class="result-icon personality-icon">
              <i class="fa-solid fa-scale-unbalanced"></i>
            </div>
            <h2>性格特质分析结果</h2>
          </div>

          <div class="personality-type-section">
            <div class="type-card">
              <h3>{{ result.personalityType }}</h3>
              <p>{{ result.personalityDesc }}</p>
            </div>
          </div>

          <div class="strengths-section">
            <h3>你的优势</h3>
            <ul class="strengths-list">
              <li v-for="(strength, index) in getPersonalityStrengths(result.personalityType)" :key="index">
                {{ strength }}
              </li>
            </ul>
          </div>

          <div class="growth-section">
            <h3>发展建议</h3>
            <ul class="growth-list">
              <li v-for="(item, index) in getPersonalityGrowth(result.personalityType)" :key="index">
                {{ item }}
              </li>
            </ul>
          </div>
        </div>

        <!-- 人际关系测试结果 -->
        <div v-else-if="result.type === 'relationship'" class="relationship-result">
          <div class="result-header">
            <div class="result-icon relationship-icon">
              <i class="fa-solid fa-handshake"></i>
            </div>
            <h2>人际关系质量评估结果</h2>
          </div>

          <div class="score-section">
            <div class="score-circle">
              <div class="score-number">{{ result.score }}</div>
              <div class="score-label">人际关系健康指数</div>
            </div>
            <div class="score-description">
              <p>{{ result.relationshipType }}</p>
            </div>
          </div>

          <div class="analysis-section">
            <h3>详细分析</h3>
            <div class="analysis-content">
              {{ result.relationshipDesc }}
            </div>
          </div>

          <div class="recommendations-section">
            <h3>改善建议</h3>
            <ul class="recommendations-list">
              <li v-for="(tip, index) in result.improvementTips" :key="index">
                {{ tip }}
              </li>
            </ul>
          </div>
        </div>

        <!-- 职业测试结果 -->
        <div v-else-if="result.type === 'career'" class="career-result">
          <div class="result-header">
            <div class="result-icon career-icon">
              <i class="fa-solid fa-briefcase"></i>
            </div>
            <h2>职业倾向分析结果</h2>
          </div>

          <div class="personality-type-section">
            <div class="type-card">
              <h3>{{ result.careerType }}</h3>
              <p>{{ result.careerDesc }}</p>
            </div>
          </div>

          <div class="recommendations-section">
            <h3>适合的职业方向</h3>
            <ul class="recommendations-list">
              <li v-for="(job, index) in result.suitableJobs" :key="index">
                {{ job }}
              </li>
            </ul>
          </div>

          <div class="recommendations-section">
            <h3>职业发展建议</h3>
            <ul class="recommendations-list">
              <li v-for="(tip, index) in result.developmentTips" :key="index">
                {{ tip }}
              </li>
            </ul>
          </div>
        </div>

        <!-- 成长潜能测试结果 -->
        <div v-else-if="result.type === 'growth'" class="growth-result">
          <div class="result-header">
            <div class="result-icon growth-icon">
              <i class="fa-solid fa-seedling"></i>
            </div>
            <h2>成长潜能评估结果</h2>
          </div>

          <div class="score-section">
            <div class="score-circle">
              <div class="score-number">{{ result.score }}</div>
              <div class="score-label">成长潜能指数</div>
            </div>
            <div class="score-description">
              <p>{{ result.growthType }}</p>
            </div>
          </div>

          <div class="analysis-section">
            <h3>详细分析</h3>
            <div class="analysis-content">
              {{ result.growthDesc }}
            </div>
          </div>

          <div class="recommendations-section">
            <h3>优势领域</h3>
            <ul class="recommendations-list">
              <li v-for="(area, index) in result.strengthAreas" :key="index">
                {{ area }}
              </li>
            </ul>
          </div>

          <div class="recommendations-section">
            <h3>发展建议</h3>
            <ul class="recommendations-list">
              <li v-for="(tip, index) in result.developmentTips" :key="index">
                {{ tip }}
              </li>
            </ul>
          </div>
        </div>

        <!-- 幸福感测试结果 -->
        <div v-else-if="result.type === 'happiness'" class="happiness-result">
          <div class="result-header">
            <div class="result-icon happiness-icon">
              <i class="fa-solid fa-smile"></i>
            </div>
            <h2>幸福感评估结果</h2>
          </div>

          <div class="score-section">
            <div class="score-circle">
              <div class="score-number">{{ 25 - result.score }}</div>
              <div class="score-label">幸福感指数</div>
            </div>
            <div class="score-description">
              <p>{{ result.happinessLevel }}</p>
            </div>
          </div>

          <div class="analysis-section">
            <h3>详细分析</h3>
            <div class="analysis-content">
              {{ result.happinessDesc }}
            </div>
          </div>

          <div class="recommendations-section">
            <h3>提升建议</h3>
            <ul class="recommendations-list">
              <li v-for="(tip, index) in result.improvementTips" :key="index">
                {{ tip }}
              </li>
            </ul>
          </div>
        </div>

        <!-- 睡眠质量测试结果 -->
        <div v-else-if="result.type === 'sleep'" class="sleep-result">
          <div class="result-header">
            <div class="result-icon sleep-icon">
              <i class="fa-solid fa-moon"></i>
            </div>
            <h2>睡眠质量评估结果</h2>
          </div>

          <div class="score-section">
            <div class="score-circle">
              <div class="score-number">{{ 25 - result.score }}</div>
              <div class="score-label">睡眠质量指数</div>
            </div>
            <div class="score-description">
              <p>{{ result.sleepQuality }}</p>
            </div>
          </div>

          <div class="analysis-section">
            <h3>详细分析</h3>
            <div class="analysis-content">
              {{ result.sleepDesc }}
            </div>
          </div>

          <div class="recommendations-section">
            <h3>改善建议</h3>
            <ul class="recommendations-list">
              <li v-for="(tip, index) in result.improvementTips" :key="index">
                {{ tip }}
              </li>
            </ul>
          </div>
        </div>

        <!-- 压力源测试结果 -->
        <div v-else-if="result.type === 'stress'" class="stress-result">
          <div class="result-header">
            <div class="result-icon stress-icon">
              <i class="fa-solid fa-fire"></i>
            </div>
            <h2>压力源评估结果</h2>
          </div>

          <div class="score-section">
            <div class="score-circle">
              <div class="score-number">{{ result.avgScore.toFixed(1) }}</div>
              <div class="score-label">压力指数</div>
            </div>
            <div class="score-description">
              <p>{{ result.pressureLevel }}</p>
            </div>
          </div>

          <div v-if="result.mainPressureSources && result.mainPressureSources.length > 0" class="analysis-section">
            <h3>主要压力源</h3>
            <div class="sources-list">
              <span v-for="(source, index) in result.mainPressureSources" :key="index" class="source-tag">
                {{ source }}
              </span>
            </div>
          </div>

          <div class="analysis-section">
            <h3>详细分析</h3>
            <div class="analysis-content">
              {{ result.pressureDesc }}
            </div>
          </div>

          <div v-if="result.generalTips && result.generalTips.length > 0" class="recommendations-section">
            <h3>一般减压建议</h3>
            <ul class="recommendations-list">
              <li v-for="(tip, index) in result.generalTips" :key="index">
                {{ tip }}
              </li>
            </ul>
          </div>

          <div v-if="result.sourceSpecificTips && result.sourceSpecificTips.length > 0" class="recommendations-section">
            <h3>针对性建议</h3>
            <ul class="recommendations-list">
              <li v-for="(tip, index) in result.sourceSpecificTips" :key="index">
                {{ tip }}
              </li>
            </ul>
          </div>
        </div>

        <!-- 通用操作按钮 -->
        <div class="action-buttons">
          <router-link to="/tests" class="btn primary-btn">
            查看更多测试
          </router-link>
          <button @click="saveResult" class="btn secondary-btn">
            保存结果
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import NavBar from '../components/NavBar.vue';

const router = useRouter();
const loading = ref(true);
const result = ref(null);

// 获取情绪测试描述
const getEmotionDescription = (score) => {
  if (score <= 8) return '情绪状态良好';
  if (score <= 12) return '情绪状态一般';
  if (score <= 16) return '情绪状态较差';
  return '情绪状态需要关注';
};

// 获取情绪测试分析
const getEmotionAnalysis = (score) => {
  if (score <= 8) {
    return '您当前的情绪状态非常健康，保持了良好的心理平衡。您能够积极应对生活中的各种挑战，情绪稳定，对生活充满热情。这种积极的情绪状态有助于您的身心健康，提高工作效率和生活质量。';
  } else if (score <= 12) {
    return '您的情绪状态基本正常，但可能在某些时候会感到压力或焦虑。这是一种常见的情绪状态，可能与工作压力、生活变化等因素有关。适当的自我调节和休息可以帮助您恢复更好的情绪状态。';
  } else if (score <= 16) {
    return '您最近的情绪状态可能受到了一些负面影响，表现出较高的压力水平或情绪低落。建议您关注自己的情绪变化，尝试一些放松技巧，如深呼吸、冥想或适当的运动。如持续感到不适，建议寻求专业帮助。';
  } else {
    return '您当前的情绪状态需要特别关注，可能存在明显的压力、焦虑或抑郁症状。这些情绪可能会影响您的日常生活和工作。强烈建议您寻求专业心理辅导或咨询，同时也可以尝试与亲友交流，分享您的感受。';
  }
};

// 获取情绪改善建议
const getEmotionRecommendations = (score) => {
  if (score <= 8) {
    return [
      '继续保持积极的生活态度和健康的生活方式',
      '定期进行体育锻炼，保持身心健康',
      '培养兴趣爱好，丰富生活内容',
      '保持良好的社交关系，与亲友保持联系'
    ];
  } else if (score <= 12) {
    return [
      '学习一些压力管理技巧，如深呼吸、冥想等',
      '保证充足的睡眠和合理的饮食',
      '适当安排休息时间，避免过度劳累',
      '多参与户外活动，接触大自然'
    ];
  } else {
    return [
      '建议寻求专业心理咨询师的帮助',
      '尝试写日记，记录和表达自己的情绪',
      '适当增加体育锻炼，如散步、瑜伽等',
      '减少咖啡因和酒精的摄入',
      '确保充分休息，建立规律的作息时间'
    ];
  }
};

// 获取性格优势
const getPersonalityStrengths = (type) => {
  switch (type) {
    case '外向型':
      return [
        '善于与人沟通和交流',
        '充满活力和热情',
        '具有良好的社交能力',
        '在团队中能够发挥积极作用',
        '思维敏捷，反应迅速'
      ];
    case '平衡型':
      return [
        '适应能力强，能够灵活应对不同情况',
        '既能够独立思考，也善于团队合作',
        '情绪稳定，心态平和',
        '具有良好的自我调节能力',
        '能够理解和接纳不同的观点'
      ];
    case '内向型':
      return [
        '思维深刻，善于分析和思考',
        '专注力强，能够深入研究问题',
        '做事认真负责，注重细节',
        '具有良好的自我认知',
        '能够给予他人真诚的关心和支持'
      ];
    default:
      return [];
  }
};

// 获取性格发展建议
const getPersonalityGrowth = (type) => {
  switch (type) {
    case '外向型':
      return [
        '适当留出独处时间，进行自我反思',
        '学会倾听他人的意见和感受',
        '在做出决定前，尝试更多地思考和分析',
        '培养耐心，学会等待合适的时机'
      ];
    case '平衡型':
      return [
        '在保持平衡的同时，培养自己的特长和优势',
        '设定明确的目标，增强行动力',
        '在需要的时候，勇于表达自己的观点和立场',
        '持续学习和成长，不断提升自己'
      ];
    case '内向型':
      return [
        '尝试多参与社交活动，扩大社交圈',
        '学会更有效地表达自己的想法和感受',
        '勇于尝试新事物，走出舒适区',
        '在团队中主动承担更多责任，展示自己的能力'
      ];
    default:
      return [];
  }
};

// 保存结果
const saveResult = () => {
  // 实际项目中可以调用API保存结果到后端
  // 这里模拟保存成功
  alert('测试结果已保存');
};

// 加载测试结果
const loadResult = () => {
  try {
    const savedResult = localStorage.getItem('testResult');
    if (savedResult) {
      result.value = JSON.parse(savedResult);
    }
  } catch (error) {
    console.error('加载测试结果失败:', error);
  } finally {
    loading.value = false;
  }
};

// 组件挂载时加载结果
onMounted(() => {
  loadResult();
});
</script>

<style scoped>
.result-page {
  padding: 20px;
  min-height: 100vh;
  background-color: #f8f9fa;
}

.result-container {
  max-width: 900px;
  margin: 0 auto;
  background-color: white;
  border-radius: 12px;
  padding: 30px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.08);
}

.loading {
  text-align: center;
  padding: 60px 0;
}

.loading-spinner {
  width: 50px;
  height: 50px;
  border: 5px solid #f3f3f3;
  border-top: 5px solid #673ab7;
  border-radius: 50%;
  animation: spin 1s linear infinite;
  margin: 0 auto 20px;
}

@keyframes spin {
  0% { transform: rotate(0deg); }
  100% { transform: rotate(360deg); }
}

.no-result {
  text-align: center;
  padding: 60px 0;
}

.no-result h2 {
  color: #333;
  margin-bottom: 10px;
}

.no-result p {
  color: #666;
  margin-bottom: 30px;
}

.result-content {
  animation: fadeIn 0.5s ease-in;
}

@keyframes fadeIn {
  from { opacity: 0; }
  to { opacity: 1; }
}

.result-header {
  text-align: center;
  margin-bottom: 40px;
}

.result-icon {
  width: 80px;
  height: 80px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  margin: 0 auto 20px;
  font-size: 32px;
  color: white;
}

.emotion-icon {
  background-color: #f8d7da;
}

.personality-icon {
  background-color: #fce8d2;
}

.relationship-icon {
  background-color: #d1ecf1;
}

.career-icon {
  background-color: #d4edda;
}

.growth-icon {
  background-color: #e7e7e7;
}

.happiness-icon {
  background-color: #fff3cd;
}

.sleep-icon {
  background-color: #6c757d;
}

.stress-icon {
  background-color: #f5c6cb;
}

.result-header h2 {
  color: #333;
  font-size: 28px;
}

.score-section {
  display: flex;
  align-items: center;
  margin-bottom: 40px;
  padding: 30px;
  background-color: #f8f9fa;
  border-radius: 12px;
}

.score-circle {
  width: 120px;
  height: 120px;
  border-radius: 50%;
  background-color: #673ab7;
  color: white;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  margin-right: 30px;
  box-shadow: 0 4px 15px rgba(103, 58, 183, 0.3);
}

.score-number {
  font-size: 48px;
  font-weight: bold;
  line-height: 1;
}

.score-label {
  font-size: 16px;
  margin-top: 5px;
}

.score-description {
  flex: 1;
}

.score-description p {
  font-size: 18px;
  color: #666;
  line-height: 1.6;
}

.analysis-section,
.recommendations-section,
.personality-type-section,
.strengths-section,
.growth-section {
  margin-bottom: 40px;
}

h3 {
  color: #333;
  margin-bottom: 20px;
  font-size: 20px;
  border-left: 4px solid #673ab7;
  padding-left: 15px;
}

.analysis-content {
  line-height: 1.8;
  color: #666;
  background-color: #f8f9fa;
  padding: 20px;
  border-radius: 8px;
}

.recommendations-list,
.strengths-list,
.growth-list {
  list-style: none;
  padding: 0;
}

.recommendations-list li,
.strengths-list li,
.growth-list li {
  padding: 12px 0 12px 30px;
  border-bottom: 1px solid #e9ecef;
  position: relative;
  color: #666;
}

.recommendations-list li:last-child,
.strengths-list li:last-child,
.growth-list li:last-child {
  border-bottom: none;
}

.recommendations-list li:before,
.strengths-list li:before,
.growth-list li:before {
  content: '✓';
  position: absolute;
  left: 0;
  color: #673ab7;
  font-weight: bold;
}

.type-card {
  background-color: #f8f9fa;
  padding: 30px;
  border-radius: 12px;
  text-align: center;
}

.type-card h3 {
  color: #dc3545;
  border: none;
  padding: 0;
  margin-bottom: 15px;
  font-size: 28px;
}

.type-card p {
  line-height: 1.8;
  color: #666;
}

.action-buttons {
  display: flex;
  justify-content: center;
  gap: 20px;
  margin-top: 50px;
}

.btn {
  padding: 12px 30px;
  border: none;
  border-radius: 6px;
  font-size: 16px;
  cursor: pointer;
  transition: all 0.3s;
  text-decoration: none;
  font-weight: 500;
}

.primary-btn {
  background-color: #673ab7;
  color: white;
}

.primary-btn:hover {
  background-color: #562d91;
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(103, 58, 183, 0.3);
}

.secondary-btn {
  background-color: #e9ecef;
  color: #495057;
}

.secondary-btn:hover {
  background-color: #dee2e6;
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.back-btn {
  display: inline-block;
  padding: 12px 24px;
  background-color: #673ab7;
  color: white;
  text-decoration: none;
  border-radius: 6px;
  transition: background-color 0.3s;
}

.back-btn:hover {
  background-color: #562d91;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .score-section {
    flex-direction: column;
    text-align: center;
  }
  
  .score-circle {
    margin-right: 0;
    margin-bottom: 20px;
  }
  
  .action-buttons {
    flex-direction: column;
    align-items: center;
  }
  
  .btn {
    width: 100%;
    max-width: 300px;
    text-align: center;
  }
}
</style>