<template>
  <div class="test-card" @click="handleClick">
    <div class="card-icon" :style="{ backgroundColor: iconBgColor }">
      <i :class="iconClass">/</i>
    </div>
    <h3 class="card-title">{{ title }}</h3>
    <p class="card-desc">{{ desc }}</p>
    <p class="card-meta">{{ time }} · {{ questions }}题</p>
    <button class="card-btn" :style="{ backgroundColor: btnBgColor }">开始测试</button>
  </div>
</template>

<script setup>
import { useRouter } from 'vue-router';

const router = useRouter();

const props = defineProps({
  title: {
    type: String,
    required: true,
  },
  desc: {
    type: String,
    required: true,
  },
  time: {
    type: String,
    required: true,
  },
  questions: {
    type: String,
    required: true,
  },
  route: {
    type: String,
    required: true,
  },
  iconClass: {
    type: String,
    required: true,
  },
  iconBgColor: {
    type: String,
    default: '#f0f0f0',
  },
  btnBgColor: {
    type: String,
    default: '#673ab7',
  },
});

const handleClick = () => {
  router.push({ name: props.route });
};
</script>

<style scoped>
.test-card {
  width: 300px;
  border: 1px solid #eee;
  border-radius: 8px;
  padding: 24px;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.05);
  cursor: pointer;
  transition: transform 0.3s, box-shadow 0.3s;
}

.test-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 5px 15px rgba(0, 0, 0, 0.1);
}

.card-icon {
  width: 48px;
  height: 48px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 16px;
}

.card-icon i {
  font-size: 24px;
  color: #fff;
}

.card-title {
  font-size: 20px;
  margin-bottom: 8px;
  color: #333;
}

.card-desc {
  font-size: 14px;
  color: #666;
  margin-bottom: 16px;
  line-height: 1.5;
}

.card-meta {
  font-size: 12px;
  color: #999;
  margin-bottom: 16px;
}

.card-btn {
  width: 100%;
  padding: 8px;
  border-radius: 4px;
  border: none;
  color: #fff;
  cursor: pointer;
}
</style>