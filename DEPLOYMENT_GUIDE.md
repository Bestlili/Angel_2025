# Angel 心理健康服务平台前端部署指南

## 项目简介

Angel 是一个基于 Vue 3 + Vite 构建的心理健康服务平台前端项目，包含以下主要功能模块：
- 首页展示
- 用户登录/注册
- 心理测试（职业、情绪、成长、幸福、人格、人际关系、睡眠、压力源等）
- 心灵树洞
- 社区论坛
- 个人中心
- 管理后台

## 环境要求

- Node.js 16.x 或更高版本
  - **Windows用户**: 从[nodejs.org](https://nodejs.org/)下载Windows安装程序并遵循向导安装
- npm 8.x 或更高版本

## 本地开发环境搭建

### 1. 克隆项目代码

```bash
# 替换为实际的仓库地址
git clone <项目仓库地址>
cd Angel-fronted
```

### 2. 安装项目依赖

```bash
npm install
```

### 3. 项目结构说明

```
├── src/
│   ├── api/           # API请求配置
│   ├── assets/        # 静态资源
│   ├── components/    # Vue组件
│   ├── router/        # 路由配置
│   ├── store/         # 状态管理
│   ├── views/         # 页面组件
│   ├── App.vue        # 根组件
│   └── main.js        # 入口文件
├── index.html         # HTML模板
├── vite.config.js     # Vite配置
├── package.json       # 项目配置
└── README.md          # 项目说明
```

### 4. 主要模块说明

#### 1. API 配置
API请求配置在 `src/api/` 目录下，主要配置文件包括：
- `index.js`: 全局API请求配置
- `treehole.js`: 心灵树洞API

**关键配置：**
```javascript
// src/api/index.js
baseURL: '/api'  // API请求前缀
```

#### 2. 路由配置
路由配置在 `src/router/index.js` 文件中，使用 Vue Router 4 实现单页应用路由。

#### 3. Vite 配置
Vite配置文件为 `vite.config.js`，主要配置包括：
- 开发服务器端口（默认：5173）
- API代理配置

**代理配置：**
```javascript
// vite.config.js
server: {
  port: 5173,
  proxy: {
    '/api': {
      target: 'http://localhost:8080', // 后端服务地址
      changeOrigin: true
    }
  }
}
```

### 5. 启动开发服务器

```bash
npm run dev
```

开发服务器将在 http://localhost:5173 启动

## 生产环境构建

### 1. 构建项目

```bash
npm run build
```

构建产物将生成在 `dist/` 目录下，包含优化后的静态文件。

### 2. 预览构建结果

```bash
npm run preview
```

预览服务器将在 http://localhost:4173 启动

### 3. 构建注意事项

- 确保已正确配置 `vite.config.js` 中的代理设置
- 构建前检查环境变量配置
- 如需自定义构建输出目录，可在 `vite.config.js` 中配置 `build.outDir`

## 部署方案

### 方案一：Nginx 部署

#### 1. 安装 Nginx

##### Linux 系统
```bash
# Ubuntu/Debian
sudo apt update
sudo apt install nginx

# CentOS/RHEL
sudo yum install nginx
```

##### Windows 系统
1. 从[nginx.org/en/download.html](https://nginx.org/en/download.html)下载Windows版本的Nginx压缩包
2. 解压到合适的目录（如 `C:\nginx`）
3. 通过CMD或PowerShell进入解压目录即可运行Nginx

#### 2. 创建 Nginx 配置文件

##### Linux 系统
```bash
sudo nano /etc/nginx/conf.d/angel.conf
```

##### Windows 系统
1. 打开Nginx解压目录下的 `conf\nginx.conf` 文件
2. 在 `http` 块中添加或修改 `server` 配置

**配置内容：**
```nginx
server {
    listen 80;
    server_name your-domain.com;  # 替换为您的域名或IP
    root /path/to/Angel-fronted/dist;  # 替换为构建后的dist目录路径
    index index.html;

    # 处理单页应用路由
    location / {
        try_files $uri $uri/ /index.html;
    }

    # API代理配置
    location /api {
        proxy_pass http://your-backend-server:8080;  # 替换为后端服务地址
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
    }
}
```

#### 3. 启动 Nginx

##### Linux 系统
```bash
sudo systemctl start nginx
sudo systemctl enable nginx
```

##### Windows 系统
```bash
# 启动Nginx
cd C:\nginx
start nginx.exe

# 停止Nginx
nginx.exe -s stop

# 重新加载配置
nginx.exe -s reload
```

#### 4. 验证部署

在浏览器中访问 `http://your-domain.com` 或 `http://your-ip-address`

### 方案二：Docker 部署

#### 1. 安装 Docker

##### Linux/macOS
参考[Docker官方文档](https://docs.docker.com/engine/install/)进行安装

##### Windows
1. 从[docker.com](https://www.docker.com/products/docker-desktop)下载Docker Desktop安装程序
2. 安装时勾选启用WSL2（需要Windows 10 2004以上版本）
3. 启动Docker Desktop并登录Docker Hub账户

#### 2. 创建 Dockerfile

在项目根目录创建 `Dockerfile`：

```dockerfile
# 构建阶段
FROM node:16-alpine as builder
WORKDIR /app
COPY package*.json ./
RUN npm install
COPY . .
RUN npm run build

# 运行阶段
FROM nginx:alpine
COPY --from=builder /app/dist /usr/share/nginx/html

# 复制自定义Nginx配置
COPY nginx.conf /etc/nginx/conf.d/default.conf

EXPOSE 80
CMD ["nginx", "-g", "daemon off;"]
```

#### 2. 创建 Nginx 配置文件

在项目根目录创建 `nginx.conf`：

```nginx
server {
    listen 80;
    root /usr/share/nginx/html;
    index index.html;

    location / {
        try_files $uri $uri/ /index.html;
    }

    location /api {
        proxy_pass http://your-backend-server:8080;  # 替换为后端服务地址
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
    }
}
```

#### 3. 构建 Docker 镜像

```bash
docker build -t angel-fronted .
```

#### 4. 运行 Docker 容器

```bash
docker run -d -p 80:80 angel-fronted
```

#### 5. 验证部署

在浏览器中访问 `http://your-domain.com` 或 `http://your-ip-address`

## 配置说明

### API 地址配置

在 `src/api/index.js` 中配置 API 基础地址：

```javascript
// src/api/index.js
baseURL: process.env.VITE_APP_API_BASE_URL || '/api'
```

或在 `vite.config.js` 中配置代理地址：

```javascript
// vite.config.js
server: {
  proxy: {
    '/api': {
      target: 'http://localhost:8080', // 替换为实际后端地址
      changeOrigin: true
    }
  }
}
```

### 环境变量配置

可以创建 `.env` 文件配置环境变量：

```env
# API基础地址
VITE_APP_API_BASE_URL=http://your-backend-server:8080
# 应用基础路径
VITE_APP_BASE_URL=/
```

**Windows用户注意**：
- 使用文本编辑器（如Notepad++或Visual Studio Code）创建 `.env` 文件时，确保文件扩展名不是 `.env.txt`
- 在PowerShell中创建 `.env` 文件的命令：`New-Item -Name ".env" -ItemType File -Value "VITE_APP_API_BASE_URL=http://your-backend-server:8080"`

## 部署后的注意事项

1. **API 地址配置**
   - 确保生产环境中的 API 地址正确配置
   - 可考虑通过环境变量动态配置 API 地址

2. **HTTPS 配置**
   - 生产环境建议配置 HTTPS
   - 可通过 Nginx 配置 SSL 证书

3. **性能优化**
   - 考虑启用 Gzip 压缩
   - 配置浏览器缓存策略

4. **监控与日志**
   - 配置适当的访问日志
   - 考虑添加错误监控

## 常见问题排查

### 1. 安装依赖失败

#### Linux/macOS
```bash
# 清除npm缓存
npm cache clean --force
# 删除node_modules和package-lock.json
rm -rf node_modules package-lock.json
# 重新安装依赖
npm install
```

#### Windows
```bash
# 清除npm缓存
npm cache clean --force
# 删除node_modules和package-lock.json
rd /s /q node_modules package-lock.json
# 重新安装依赖
npm install
```

### 2. PowerShell 命令执行被禁止

如果在Windows PowerShell中执行npm命令时遇到"无法加载文件 ... 因为在此系统上禁止运行脚本"的错误，可以通过以下命令解决：

```bash
Set-ExecutionPolicy RemoteSigned -Scope CurrentUser
```

### 2. 开发服务器无法启动

##### Linux/macOS
```bash
# 检查端口是否被占用
lsof -i :5173
# 终止占用端口的进程
kill -9 <PID>
```

##### Windows
```bash
# 检查端口是否被占用
netstat -ano | findstr :5173
# 终止占用端口的进程
 taskkill /PID <PID> /F
```

- 更换端口：修改 `vite.config.js` 中的 `server.port` 配置

### 3. API 请求失败

- 检查 `vite.config.js` 中的代理配置是否正确
- 检查后端服务是否正常运行
- 检查 CORS 配置

### 4. 页面空白或路由错误

- 确保 Nginx 配置了正确的 `try_files` 指令
- 检查 `router/index.js` 中的路由配置

### 5. 构建失败

- 确保 Node.js 版本符合要求
- 删除 `node_modules` 并重新安装依赖

## 维护与更新

### 1. 拉取最新代码

```bash
git pull
```

### 2. 重新安装依赖

```bash
npm install
```

### 3. 重新构建

```bash
npm run build
```

### 4. 重新部署

##### Linux Nginx 部署
```bash
# 复制新的dist目录到部署路径
cp -r dist/* /usr/share/nginx/html/
```

##### Windows Nginx 部署
```bash
# 复制新的dist目录到Nginx的html目录
xcopy /E /Y dist\* C:\nginx\html\
```

##### Docker 部署
```bash
# 重新构建镜像
docker build -t angel-fronted .
# 停止旧容器
docker stop <container-id>
# 运行新容器
docker run -d -p 80:80 angel-fronted
```

## 管理后台

访问管理后台：`http://your-domain.com/admin`

## 联系信息

如果遇到部署问题，请联系项目开发人员。

---

版本：v1.0.0
更新日期：2023-10-15