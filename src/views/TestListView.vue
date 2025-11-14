<template>
  <div class="home">
    <NavBar />
    <div class="tab-buttons">
      <button
        v-for="tab in tabs"
        :key="tab.value"
        :class="{ active: currentTab === tab.value }"
        @click="currentTab = tab.value"
      >
        {{ tab.label }}
      </button>
    </div>
    <div class="test-cards">
      <TestCard
        v-for="card in filteredCards"
        :key="card.route"
        v-bind="card"
      />
    </div>
  </div>
</template>

<script setup>
import TestCard from '../components/TestCard.vue';
import NavBar from '../components/NavBar.vue';

const tabs = [
  { label: '全部测试', value: 'all' },
  { label: '情绪健康', value: 'emotion' },
  { label: '性格特质', value: 'personality' },
  { label: '人际关系', value: 'relationship' },
  { label: '压力管理', value: 'stress' },
];

const cards = [
  {
    title: '情绪状态评估',
    desc: '通过专业量表评估当前情绪状态，了解压力、焦虑、抑郁等情绪指标，获得针对性建议。',
    time: '10分钟',
    questions: '20',
    route: 'emotionTest',
    iconClass: 'fa-solid fa-heart',
    iconBgColor: '#f8d7da',
    btnBgColor: '#6c757d',
    category: 'emotion',
  },
  {
    title: '性格特质分析',
    desc: '探索你的性格特质和优势，了解自己的行为模式和决策风格，更好地理解自己和他人。',
    time: '15分钟',
    questions: '30',
    route: 'personalityTest',
    iconClass: 'fa-solid fa-scale-unbalanced',
    iconBgColor: '#fce8d2',
    btnBgColor: '#dc3545',
    category: 'personality',
  },
  {
    title: '人际关系模式',
    desc: '评估你的人际交往模式和依恋类型，了解在关系中的优势与挑战，提升人际关系质量。',
    time: '12分钟',
    questions: '25',
    route: 'relationshipTest',
    iconClass: 'fa-solid fa-handshake',
    iconBgColor: '#d1e7dd',
    btnBgColor: '#28a745',
    category: 'relationship',
  },
  {
    title: '压力源评估',
    desc: '识别生活中的主要压力来源，了解压力对你的影响方式，学习有效的压力管理策略。',
    time: '8分钟',
    questions: '15',
    route: 'stressSourceTest',
    iconClass: 'fa-solid fa-bars',
    iconBgColor: '#cfe2ff',
    btnBgColor: '#007bff',
    category: 'stress',
  },
  {
    title: '睡眠质量评估',
    desc: '评估你的睡眠质量和睡眠习惯，了解影响睡眠的因素，获得改善睡眠的专业建议。',
    time: '5分钟',
    questions: '10',
    route: 'sleepTest',
    iconClass: 'fa-solid fa-moon',
    iconBgColor: '#e2d9f3',
    btnBgColor: '#6f42c1',
    category: 'emotion',
  },
  {
    title: '幸福感指数',
    desc: '从多个维度评估你的整体幸福感，探索提升幸福感的方法，培养积极心态。',
    time: '10分钟',
    questions: '22',
    route: 'happinessTest',
    iconClass: 'fa-solid fa-sun',
    iconBgColor: '#fff3cd',
    btnBgColor: '#c08b09',
    category: 'emotion',
  },
];

import { ref, computed } from 'vue';
const currentTab = ref('all');

const filteredCards = computed(() => {
  if (currentTab.value === 'all') {
    return cards;
  }
  return cards.filter((card) => card.category === currentTab.value);
});
</script>

<style scoped>
.home {
  padding: 20px;
}

.tab-buttons {
  display: flex;
  gap: 12px;
  margin: 20px 0;
  justify-content: center;
}

.tab-buttons button {
  padding: 8px 16px;
  border: 1px solid #ddd;
  border-radius: 20px;
  background-color: #fff;
  cursor: pointer;
  transition: all 0.3s;
}

.tab-buttons button.active {
  background-color:#87879e ;
  color: white;
  border-color: #673ab7;
}

.test-cards {
  display: flex;
  flex-wrap: wrap;
  justify-content: center;
  gap: 30px;
  padding: 0 20px;
  margin: 0 auto;
  max-width: 1200px;
}


</style>