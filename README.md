# Angel 心理健康服务平台前端

Angel 是一个基于 Vue 3 + Vite 构建的心理健康服务平台前端项目，提供全面的心理健康相关功能和服务。

## 技术栈

- **Vue 3** - 渐进式 JavaScript 框架
- **Vite** - 下一代前端构建工具
- **Vue Router** - Vue.js 官方路由
- **Axios** - HTTP 客户端
- **Tailwind CSS** - 原子化 CSS 框架

## 功能模块

### 用户端
- **首页** - 平台介绍和主要功能入口
- **登录/注册** - 用户认证系统
- **心理测试** - 提供多种心理测试：
  - 职业测试
  - 情绪测试
  - 成长测试
  - 幸福测试
  - 人格测试
  - 人际关系测试
  - 睡眠测试
  - 压力源测试
- **心灵树洞** - 匿名倾诉和AI陪伴功能
- **社区论坛** - 用户交流和分享平台
- **个人中心** - 用户信息管理和测试结果查看

### 管理端
- **管理仪表盘** - 数据统计和概览
- **用户管理** - 用户信息和权限管理
- **内容管理** - 帖子和评论管理
- **测试管理** - 测试题目和结果管理
- **日志管理** - 系统操作日志查看
- **系统设置** - 平台配置和参数管理

## 快速开始

### 环境要求

- Node.js 16.x 或更高版本
- npm 8.x 或更高版本

### 安装步骤

1. **克隆项目**

```bash
# 替换为实际仓库地址
git clone <项目仓库地址>
cd Angel-fronted
```

2. **安装依赖**

```bash
npm install
```

3. **启动开发服务器**

```bash
npm run dev
```

服务器将在 http://localhost:5173 启动

4. **构建生产版本**

```bash
npm run build
```

构建产物将生成在 `dist/` 目录下

5. **预览生产版本**

```bash
npm run preview
```

预览服务器将在 http://localhost:4173 启动

## 配置说明

### API 配置

API 请求配置在 `src/api/` 目录下，可以通过环境变量配置 API 地址：

```env
# .env 文件
VITE_API_BASE_URL=http://your-backend-server:8080
```

### 代理配置

开发环境下的 API 代理配置在 `vite.config.js` 文件中：

```javascript
// vite.config.js
server: {
  proxy: {
    '/api': {
      target: 'http://localhost:8080', // 后端服务地址
      changeOrigin: true
    }
  }
}
```

## 部署指南

详细的部署说明请参考 [DEPLOYMENT_GUIDE.md](./DEPLOYMENT_GUIDE.md) 文件，包含以下部署方案：

- **Nginx 部署** - 传统服务器部署
- **Docker 部署** - 容器化部署
- **HTTPS 配置** - 安全传输配置
- **性能优化** - 缓存策略和压缩配置

## 项目结构

```
├── src/
│   ├── api/           # API 请求配置
│   ├── assets/        # 静态资源文件
│   ├── components/    # 通用组件
│   ├── router/        # 路由配置
│   ├── store/         # 状态管理
│   ├── views/         # 页面组件
│   ├── App.vue        # 根组件
│   └── main.js        # 入口文件
├── index.html         # HTML 模板
├── vite.config.js     # Vite 配置
├── tailwind.config.js # Tailwind CSS 配置
├── package.json       # 项目配置
└── DEPLOYMENT_GUIDE.md # 部署指南
```

## 开发注意事项

1. **代码风格** - 遵循 ESLint 规则
2. **组件命名** - 采用 PascalCase 命名
3. **API 调用** - 使用统一的 API 客户端
4. **路由配置** - 使用 Vue Router 4
5. **状态管理** - 使用 Pinia 或 Vuex

## 常见问题

### 依赖安装失败

```bash
# 清除缓存
npm cache clean --force
# 删除 node_modules 和 package-lock.json
rm -rf node_modules package-lock.json
# 重新安装
npm install
```

### 端口被占用

```bash
# 查找占用端口的进程
lsof -i :5173
# 终止进程
kill -9 <PID>
```

### API 请求失败

- 检查后端服务是否正常运行
- 检查 API 地址配置是否正确
- 检查 CORS 配置

## 联系方式

如有问题或建议，请联系项目团队。

---

版本：v1.0.0
更新日期：2023-10-15
