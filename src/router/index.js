import { createRouter, createWebHistory } from 'vue-router';
import HomeView from '../views/HomeView.vue';
import TestListView from '../views/TestListView.vue';
import EmotionTest from '../views/EmotionTest.vue';
import PersonalityTest from '../views/PersonalityTest.vue';
import RelationshipTest from '../views/RelationshipTest.vue';
import StressSourceTest from '../views/StressSourceTest.vue';
import SleepTest from '../views/SleepTest.vue';
import HappinessTest from '../views/HappinessTest.vue';
import TestResult from '../views/TestResult.vue';
import AdminView from '../views/AdminView.vue';
import AdminDashboardView from '../views/AdminDashboardView.vue';
import AdminUsersView from '../views/AdminUsersView.vue';
import AdminAnalyticsView from '../views/AdminAnalyticsView.vue';
import AdminSettingsView from '../views/AdminSettingsView.vue';
import AdminLogsView from '../views/AdminLogsView.vue';

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/',
      name: 'home',
      component: HomeView,
    },
    {
      path: '/tests',
      name: 'tests',
      component: TestListView,
    },
    {
        path:'/login',
        name:'login',
        component: () => import('../views/LoginView.vue')
    },
    {
        path:'/register',
        name:'register',
        component: () => import('../views/RegisterView.vue')
    },
    {
        path:'/treehole',
        name:'treehole',
        component: () => import('../views/TreeholeView.vue')
    },
    {
        path:'/diary',
        name:'diary',
        component: () => import('../views/DiaryView.vue')
    },
    {
      path: '/emotion-test',
      name: 'emotionTest',
      component: EmotionTest,
    },
    {
      path: '/personality-test',
      name: 'personalityTest',
      component: PersonalityTest,
    },
    {
      path: '/relationship-test',
      name: 'relationshipTest',
      component: RelationshipTest,
    },
    {
      path: '/stress-source-test',
      name: 'stressSourceTest',
      component: StressSourceTest,
    },
    {
      path: '/sleep-test',
      name: 'sleepTest',
      component: SleepTest,
    },
    {
      path: '/happiness-test',
      name: 'happinessTest',
      component: HappinessTest,
    },
    {
      path: '/tests/result',
      name: 'testResult',
      component: TestResult,
    },
    {
        path:'/community',
        name:'community',
        component: () => import('../views/CommunityView.vue')
    },
    {
        path:'/account-settings',
        name:'accountSettings',
        component: () => import('../views/AccountSettings.vue')
    },
    // 管理员登录页面（无需权限）

    // 管理员相关路由
    {
        path:'/admin',
        name:'admin',
        component: AdminDashboardView,
        meta: { requiresAdmin: true }
    },
    {
        path:'/admin/review',
        name:'adminReview',
        component: AdminView,
        meta: { requiresAdmin: true }
    },
    {
        path:'/admin/users',
        name:'adminUsers',
        component: AdminUsersView,
        meta: { requiresAdmin: true }
    },
    {
        path:'/admin/analytics',
        name:'adminAnalytics',
        component: AdminAnalyticsView,
        meta: { requiresAdmin: true }
    },
    {
        path:'/admin/settings',
        name:'adminSettings',
        component: AdminSettingsView,
        meta: { requiresAdmin: true }
    },
    {
        path:'/admin/logs',
        name:'adminLogs',
        component: AdminLogsView,
        meta: { requiresAdmin: true }
    },
  ],
});

// 导航守卫 - 检查管理员权限
router.beforeEach((to, from, next) => {
  // 检查路由是否需要管理员权限
  if (to.meta.requiresAdmin) {
    // 获取token
    const token = localStorage.getItem('authToken') || sessionStorage.getItem('authToken');
    // 支持user和admin两种存储键，兼容统一登录接口
    const userInfo = localStorage.getItem('user') || sessionStorage.getItem('user') || 
                    localStorage.getItem('admin') || sessionStorage.getItem('admin');
    
    // 检查是否有token和用户信息
    if (token && userInfo) {
      try {
        const user = JSON.parse(userInfo);
        // 验证是否为管理员角色，支持字符串'admin'和数字'1'两种形式
        if (user.role === 'admin' || user.role === '1') {
          next(); // 允许访问
          return;
        } else {
          // 非管理员用户访问管理员路径，显示提示并返回首页
          alert('您不是管理员，没有权限访问管理界面');
          next('/');
          return;
        }
      } catch (e) {
        console.error('解析用户信息失败:', e);
      }
    }
    
    // 未登录用户访问管理员路径，重定向到统一登录页面
    next('/login');
  } else if (to.path === '/admin/login') {
    // 管理员登录页面已合并到统一登录界面，重定向到统一登录页面
    next('/login');
  } else {
    // 不需要管理员权限的路由，直接通过
    next();
  }
});

export default router;