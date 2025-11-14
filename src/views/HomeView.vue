<template>
  <div class="home">
    <NavBar />
    <header class="hero-section">
      <div class="container">
        <h1>心灵守护 · 智汇未来</h1>
        <p class="subtitle">基于AI的智能心理咨询平台</p>
        <div class="hero-buttons">
          <button class="btn-primary" @click="handleEnterTreehole">进入树洞</button>
          <router-link to="#features" class="btn-secondary">了解更多</router-link>
        </div>
      </div>
    </header>

    <section id="features" class="features-section">
      <div class="container">
        <h2 class="section-title">核心功能</h2>
        <div class="features-grid">
          <div class="feature-card">
            <div class="feature-icon">
              <i class="fa-solid fa-robot"></i>
            </div>
            <h3>AI心理评估</h3>
            <p>通过专业量表和AI分析，为您提供精准的心理状态评估和个性化建议</p>
          </div>
          <div class="feature-card">
            <div class="feature-icon">
              <i class="fa-solid fa-comments"></i>
            </div>
            <h3>智能树洞聊天</h3>
            <p>24小时在线的AI心理咨询服务，让您随时倾诉心事，获得专业回应</p>
          </div>
          <div class="feature-card">
            <div class="feature-icon">
              <i class="fa-solid fa-chart-line"></i>
            </div>
            <h3>数据追踪分析</h3>
            <p>持续记录您的心理状态变化，提供可视化的健康趋势报告</p>
          </div>
          <div class="feature-card">
            <div class="feature-icon">
              <i class="fa-solid fa-lock"></i>
            </div>
            <h3>隐私安全保障</h3>
            <p>采用端到端加密技术，确保您的所有数据和对话内容严格保密</p>
          </div>
        </div>
      </div>
    </section>

    <section class="about-section">
      <div class="container">
        <h2 class="section-title">关于我们</h2>
        <div class="about-content">
          <div class="about-text">
            <p>Angel是一款基于人工智能技术的心理咨询平台，致力于为用户提供专业、便捷、安全的心理健康服务。</p>
            <p>我们的使命是打破心理咨询的地域和时间限制，让每个人都能随时随地获得高质量的心理支持。通过先进的AI算法和专业的心理学知识，我们为用户提供个性化的心理评估、情绪管理建议和心理疏导服务。</p>
            <p>平台集成了多种心理测评量表，包括情绪状态、性格特质、人际关系、压力管理等多个维度，帮助用户全面了解自己的心理状态，预防潜在的心理健康问题。</p>
          </div>
          <div class="about-image">
            <div class="image-placeholder">
              <i class="fa-solid fa-heart-pulse"></i>
            </div>
          </div>
        </div>
      </div>
    </section>

    <section class="testimonials-section">
      <div class="container">
        <h2 class="section-title">用户心声</h2>
        <div class="testimonials-slider">
          <div class="testimonial">
            <p class="quote">"在我情绪低落的时候，这个平台给了我很大的帮助。AI咨询师的回应专业且温暖，让我感到被理解和支持。"</p>
            <div class="user-info">
              <div class="user-avatar">{{ isLoggedIn ? userInitial : 'A' }}</div>
              <span class="user-name">{{ isLoggedIn ? userName : '匿名用户' }}</span>
            </div>
          </div>
          <div class="testimonial">
            <p class="quote">"心理测评功能很准，让我对自己有了更深入的了解。平台界面简洁，使用体验非常好。"</p>
            <div class="user-info">
              <div class="user-avatar">{{ isLoggedIn ? userInitial : 'B' }}</div>
              <span class="user-name">{{ isLoggedIn ? userName : '匿名用户' }}</span>
            </div>
          </div>
        </div>
      </div>
    </section>

    <section class="cta-section">
      <div class="container">
        <h2>开始您的心理健康之旅</h2>
        <p>立即注册，免费体验AI心理咨询服务</p>
        <router-link to="/register" class="btn-primary">免费注册</router-link>
      </div>
    </section>

    <footer class="footer">
      <div class="container">
        <div class="footer-content">
          <div class="footer-info">
            <div class="logo-container">
              <h3 class="logo">Angel</h3>
              <div class="logo-icon">👼</div>
            </div>
            <p class="slogan">让AI守护您的心灵健康</p>
            <p class="footer-description">专业、温暖、安全的心理健康服务平台，
            随时随地为您提供心理支持。</p>
          </div>
          <div class="footer-links">
            <div class="link-group">
              <h4 class="link-title">平台服务</h4>
              <ul>
                <li><a href="#" class="footer-link">关于我们</a></li>
                <li><a href="#" class="footer-link">服务条款</a></li>
                <li><a href="#" class="footer-link">隐私政策</a></li>
                <li><a href="#" class="footer-link">联系我们</a></li>
              </ul>
            </div>
            <div class="link-group">
              <h4 class="link-title">关注我们</h4>
              <div class="social-icons">
                <a href="#" class="social-icon" aria-label="微信"><i class="fa fa-weixin"></i></a>
                <a href="#" class="social-icon" aria-label="微博"><i class="fa fa-weibo"></i></a>
                <a href="#" class="social-icon" aria-label="邮箱"><i class="fa fa-envelope"></i></a>
              </div>
              <p class="contact-email">support@angel-ai.com</p>
            </div>
          </div>
        </div>
        <div class="footer-bottom">
          <div class="divider"></div>
          <p class="copyright">&copy; {{ new Date().getFullYear() }} Angel. 保留所有权利。</p>
          <div class="footer-note">
            <small>本平台提供的服务不能替代专业医疗建议或治疗</small>
          </div>
        </div>
      </div>
    </footer>
  </div>
</template>

<script setup>
import NavBar from '../components/NavBar.vue';
import { useRouter } from 'vue-router';
import { ref, onMounted } from 'vue';

const router = useRouter();
const isLoggedIn = ref(false);
const userName = ref('');
const userInitial = ref('');

// 初始化用户信息
onMounted(() => {
  const token = localStorage.getItem('authToken');
  const storedUser = localStorage.getItem('user');
  
  if (token) {
    if (storedUser) {
      // 优先从localStorage的user对象获取信息
      try {
        const userData = JSON.parse(storedUser);
        isLoggedIn.value = true;
        userName.value = userData.name || userData.username || '用户';
        // 获取用户名首字母作为头像显示
        userInitial.value = userName.value.charAt(0).toUpperCase();
      } catch (e) {
        console.error('解析用户数据失败', e);
      }
    } else {
      // 如果没有user对象，尝试从token中解析
      try {
        const jwtToken = token.replace('Bearer ', '');
        const payload = jwtToken.split('.')[1];
        if (payload) {
          const decodedPayload = JSON.parse(atob(payload));
          isLoggedIn.value = true;
          userName.value = decodedPayload.name || decodedPayload.username || decodedPayload.sub || '用户';
          // 获取用户名首字母作为头像显示
          userInitial.value = userName.value.charAt(0).toUpperCase();
        }
      } catch (error) {
        console.error('解析token失败:', error);
      }
    }
  }
});

// 处理进入树洞按钮点击事件
const handleEnterTreehole = () => {
  // 验证是否登录 - 从localStorage检查token
  const token = localStorage.getItem('authToken');
    if (token) {
    // 已登录，正常跳转到树洞页面
    router.push('/treehole');
  } else {
    // 未登录，不跳转并提示用户
    alert('请先登录后再进入树洞');
    // 可选：跳转到登录页
    // router.push('/login');
  }
};
</script>

<style scoped>
.home {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
  background-color: #f8f9fa;
}

/* 英雄区 */
.hero-section {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  padding: 80px 0 100px;
  text-align: center;
  position: relative;
  overflow: hidden;
}

.hero-section::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: url('data:image/svg+xml,<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 100 100" preserveAspectRatio="none"><path d="M0,0 L100,0 L100,100 L0,100 Z" fill="rgba(255,255,255,0.05)"/></svg>');
  background-size: cover;
}

.hero-section .container {
  position: relative;
  z-index: 1;
}

.hero-section h1 {
  font-size: 3.5rem;
  font-weight: 700;
  margin-bottom: 20px;
  text-shadow: 0 2px 10px rgba(0,0,0,0.1);
}

.hero-section .subtitle {
  font-size: 1.5rem;
  margin-bottom: 40px;
  opacity: 0.9;
}

.hero-buttons {
  display: flex;
  gap: 20px;
  justify-content: center;
  flex-wrap: wrap;
}

.btn-primary,
.btn-secondary {
  padding: 12px 30px;
  border-radius: 30px;
  font-size: 1rem;
  font-weight: 600;
  text-decoration: none;
  transition: all 0.3s ease;
  display: inline-block;
}

.btn-primary {
  background-color: white;
  color: #667eea;
  box-shadow: 0 4px 15px rgba(0,0,0,0.1);
}

.btn-primary:hover {
  transform: translateY(-3px);
  box-shadow: 0 6px 20px rgba(0,0,0,0.15);
}

.btn-secondary {
  background-color: transparent;
  color: white;
  border: 2px solid white;
}

.btn-secondary:hover {
  background-color: white;
  color: #667eea;
  transform: translateY(-3px);
}

/* 通用容器 */
.container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 20px;
}

/* 核心功能区 */
.features-section {
  padding: 100px 0;
  background-color: white;
}

.section-title {
  text-align: center;
  font-size: 2.5rem;
  margin-bottom: 60px;
  color: #333;
  position: relative;
}

.section-title::after {
  content: '';
  position: absolute;
  bottom: -15px;
  left: 50%;
  transform: translateX(-50%);
  width: 80px;
  height: 4px;
  background: linear-gradient(90deg, #667eea 0%, #764ba2 100%);
  border-radius: 2px;
}

.features-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
  gap: 30px;
}

.feature-card {
  background-color: #f8f9fa;
  padding: 30px;
  border-radius: 15px;
  text-align: center;
  transition: all 0.3s ease;
  border: 1px solid #e9ecef;
}

.feature-card:hover {
  transform: translateY(-10px);
  box-shadow: 0 15px 30px rgba(0,0,0,0.1);
  border-color: #667eea;
}

.feature-icon {
  width: 80px;
  height: 80px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  margin: 0 auto 20px;
  color: white;
  font-size: 2rem;
  box-shadow: 0 10px 20px rgba(102, 126, 234, 0.2);
}

.feature-card h3 {
  font-size: 1.5rem;
  margin-bottom: 15px;
  color: #333;
}

.feature-card p {
  color: #666;
  line-height: 1.6;
}

/* 关于我们区 */
.about-section {
  padding: 100px 0;
  background-color: #f8f9fa;
}

.about-content {
  display: flex;
  flex-wrap: wrap;
  gap: 50px;
  align-items: center;
}

.about-text,
.about-image {
  flex: 1;
  min-width: 300px;
}

.about-text p {
  margin-bottom: 20px;
  line-height: 1.8;
  color: #555;
  font-size: 1.1rem;
}

.about-image {
  display: flex;
  justify-content: center;
}

.image-placeholder {
  width: 300px;
  height: 300px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  font-size: 5rem;
  box-shadow: 0 15px 30px rgba(102, 126, 234, 0.3);
}

/* 用户心声区 */
.testimonials-section {
  padding: 100px 0;
  background-color: white;
}

.testimonials-slider {
  display: flex;
  gap: 30px;
  overflow-x: auto;
  padding-bottom: 20px;
}

.testimonial {
  flex: 0 0 400px;
  background-color: #f8f9fa;
  padding: 40px;
  border-radius: 15px;
  position: relative;
}

.testimonial::before {
  content: '"';
  position: absolute;
  top: 10px;
  left: 20px;
  font-size: 5rem;
  color: rgba(102, 126, 234, 0.1);
  font-family: Georgia, serif;
}

.quote {
  font-size: 1.2rem;
  line-height: 1.8;
  color: #555;
  margin-bottom: 30px;
  padding-left: 20px;
  position: relative;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 15px;
}

.user-avatar {
  width: 50px;
  height: 50px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  font-weight: bold;
  font-size: 1.2rem;
}

.user-name {
  color: #666;
  font-size: 1rem;
}

/* 行动召唤区 */
.cta-section {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  padding: 80px 0;
  text-align: center;
}

.cta-section h2 {
  font-size: 2.5rem;
  margin-bottom: 20px;
}

.cta-section p {
  font-size: 1.2rem;
  margin-bottom: 30px;
  opacity: 0.9;
}

.cta-section .btn-primary {
  background-color: white;
  color: #667eea;
}

/* 页脚美化 */
.footer {
  background: linear-gradient(135deg, #3a1c71, #d76d77, #ffaf7b);
  color: white;
  padding: 60px 0 40px;
  position: relative;
  overflow: hidden;
}

.footer::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background-image: 
    radial-gradient(circle at 25% 25%, rgba(255,255,255,0.1) 0%, transparent 50%),
    radial-gradient(circle at 75% 75%, rgba(255,255,255,0.1) 0%, transparent 50%);
  z-index: 0;
}

.footer-content {
  display: flex;
  flex-wrap: wrap;
  justify-content: space-between;
  gap: 60px;
  margin-bottom: 50px;
  position: relative;
  z-index: 1;
  max-width: 1200px;
  margin-left: auto;
  margin-right: auto;
}

.footer-info {
  flex: 1;
  min-width: 280px;
}

.logo-container {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 16px;
}

.logo {
  font-size: 2rem;
  font-weight: 700;
  color: white;
  margin: 0;
  text-shadow: 0 2px 10px rgba(0,0,0,0.2);
}

.logo-icon {
  font-size: 2.5rem;
  animation: float 3s ease-in-out infinite;
}

.slogan {
  font-size: 1.1rem;
  margin-bottom: 16px;
  font-weight: 500;
  opacity: 0.95;
}

.footer-description {
  opacity: 0.8;
  line-height: 1.6;
  max-width: 400px;
}

.footer-links {
  display: flex;
  gap: 60px;
  flex-wrap: wrap;
}

.link-group {
  min-width: 180px;
}

.link-title {
  font-size: 1.1rem;
  font-weight: 600;
  margin-bottom: 20px;
  color: white;
  position: relative;
  padding-bottom: 8px;
}

.link-title::after {
  content: '';
  position: absolute;
  bottom: 0;
  left: 0;
  width: 40px;
  height: 3px;
  background: linear-gradient(90deg, #fff, transparent);
  border-radius: 3px;
}

.link-group ul {
  list-style: none;
  padding: 0;
  margin: 0;
}

.link-group li {
  margin-bottom: 12px;
}

.footer-link {
  color: white;
  text-decoration: none;
  opacity: 0.8;
  transition: all 0.3s ease;
  display: inline-block;
  position: relative;
  padding-left: 5px;
}

.footer-link::before {
  content: '→';
  position: absolute;
  left: -15px;
  opacity: 0;
  transition: all 0.3s ease;
}

.footer-link:hover {
  opacity: 1;
  transform: translateX(5px);
}

.footer-link:hover::before {
  opacity: 1;
  left: -10px;
}

.social-icons {
  display: flex;
  gap: 12px;
  margin: 20px 0;
}

.social-icon {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 40px;
  height: 40px;
  border-radius: 50%;
  background-color: rgba(255,255,255,0.1);
  color: white;
  text-decoration: none;
  font-size: 1.2rem;
  transition: all 0.3s ease;
  backdrop-filter: blur(10px);
  border: 1px solid rgba(255,255,255,0.2);
}

.social-icon:hover {
  transform: translateY(-3px) scale(1.05);
  background-color: rgba(255,255,255,0.2);
  box-shadow: 0 5px 15px rgba(0,0,0,0.1);
}

.contact-email {
  opacity: 0.8;
  font-size: 0.95rem;
  margin-top: 10px;
  transition: opacity 0.3s ease;
}

.contact-email:hover {
  opacity: 1;
  cursor: pointer;
}

.footer-bottom {
  text-align: center;
  padding-top: 30px;
  position: relative;
  z-index: 1;
}

.divider {
  height: 1px;
  background: linear-gradient(90deg, transparent, rgba(255,255,255,0.3), transparent);
  margin-bottom: 25px;
}

.copyright {
  margin: 0 0 10px 0;
  font-size: 0.9rem;
  opacity: 0.8;
}

.footer-note {
  font-size: 0.8rem;
  opacity: 0.7;
}

/* 动画效果 */
@keyframes float {
  0%, 100% { transform: translateY(0); }
  50% { transform: translateY(-8px); }
}

/* 响应式优化 */
@media (max-width: 768px) {
  .footer-content {
    flex-direction: column;
    align-items: center;
    text-align: center;
    gap: 40px;
  }
  
  .footer-info {
    max-width: 100%;
  }
  
  .footer-links {
    gap: 30px;
  }
  
  .link-group {
    text-align: center;
  }
  
  .link-title::after {
    left: 50%;
    transform: translateX(-50%);
  }
  
  .footer-link::before {
    display: none;
  }
  
  .footer-link:hover {
    transform: translateX(0);
  }
}

/* 响应式设计 */
@media (max-width: 768px) {
  .hero-section h1 {
    font-size: 2.5rem;
  }
  
  .hero-section .subtitle {
    font-size: 1.2rem;
  }
  
  .section-title {
    font-size: 2rem;
  }
  
  .about-content {
    flex-direction: column;
  }
  
  .testimonial {
    flex: 0 0 100%;
  }
  
  .footer-content {
    flex-direction: column;
    align-items: center;
    text-align: center;
  }
}

@media (max-width: 480px) {
  .hero-section h1 {
    font-size: 2rem;
  }
  
  .hero-buttons {
    flex-direction: column;
    align-items: center;
  }
  
  .btn-primary,
  .btn-secondary {
    width: 200px;
    text-align: center;
  }
  
  .section-title {
    font-size: 1.8rem;
  }
  
  .image-placeholder {
    width: 200px;
    height: 200px;
    font-size: 3rem;
  }
}
</style>