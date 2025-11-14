# Angel 后端项目部署文档

## 目录
1. [简介](#简介)
2. [环境要求](#环境要求)
3. [数据库配置](#数据库配置)
4. [项目构建](#项目构建)
5. [运行项目](#运行项目)
6. [部署到服务器](#部署到服务器)
7. [常见问题](#常见问题)

## 简介

本文档详细介绍了如何部署 Angel 后端项目。该项目是一个基于 Spring Boot 的 Web 应用程序，使用 MySQL 作为数据库，MyBatis-Plus 作为 ORM 框架，并集成了 AI 功能模块。

## 环境要求

在部署之前，请确保您的服务器或本地环境满足以下要求：

- Java JDK 17 或更高版本
- Maven 3.6 或更高版本
- MySQL 8.0 或更高版本
- 至少 2GB 内存

### 安装 Java 17

```bash
# Ubuntu/Debian
sudo apt update
sudo apt install openjdk-17-jdk

# CentOS/RHEL
sudo yum install java-17-openjdk-devel

# Windows (使用 Chocolatey)
choco install openjdk17

# Windows (手动安装)
# 1. 访问 Oracle 官网或 AdoptOpenJDK 下载 JDK 17
# 2. 运行安装程序
# 3. 配置环境变量 JAVA_HOME 指向安装目录
# 4. 将 %JAVA_HOME%\bin 添加到 PATH 环境变量
```

### 安装 Maven

```bash
# Ubuntu/Debian
sudo apt install maven

# CentOS/RHEL
sudo yum install maven

# Windows (使用 Chocolatey)
choco install maven

# Windows (手动安装)
# 1. 访问 Apache Maven 官网下载最新版本
# 2. 解压到指定目录
# 3. 配置环境变量 MAVEN_HOME 指向解压目录
# 4. 将 %MAVEN_HOME%\bin 添加到 PATH 环境变量
```

## 数据库配置

### 安装 MySQL

```bash
# Ubuntu/Debian
sudo apt install mysql-server

# CentOS/RHEL
sudo yum install mysql-server

# Windows
# 1. 访问 MySQL 官网下载 MySQL Installer
# 2. 运行安装程序，选择合适的安装类型（推荐 Server only）
# 3. 按照安装向导完成安装
# 4. 在安装过程中会提示设置 root 密码
```

### 创建数据库

连接到 MySQL 并创建项目所需的数据库：

```sql
CREATE DATABASE angel CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

### 数据库连接配置

项目默认配置如下，GitHub 仓库中不包含真实的配置文件，以保护敏感信息。您需要在本地创建配置文件：

1. 复制示例配置文件：
   ```bash
   # Linux/macOS
   cp src/main/resources/application.properties.example src/main/resources/application.properties
   
   # Windows
   copy src\main\resources\application.properties.example src\main\resources\application.properties
   ```

2. 修改 [src/main/resources/application.properties](file:///Users/lizixuan/Desktop/Angel/Angel-backed/src/main/resources/application.properties) 中的配置：

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/angel?useUnicode=true&characterEncoding=utf8&zeroDateTimeBehavior=convertToNull&transformedBitIsBoolean=true&allowMultiQueries=true&useSSL=false&allowPublicKeyRetrieval=true
spring.datasource.username=your_database_username
spring.datasource.password=your_database_password
```

请根据实际情况修改数据库地址、用户名和密码。

## 项目构建

### 克隆项目

```bash
git clone <项目仓库地址>
cd Angel-backed
```

### 构建项目

使用 Maven 进行项目构建：

```bash
# Linux/macOS
./mvnw clean package

# Windows
mvnw.cmd clean package
```

构建完成后，会在 `target` 目录下生成可执行的 JAR 文件：`Angel-backed-0.0.1-SNAPSHOT.jar`

## 运行项目

### 开发环境运行

```bash
# Linux/macOS
./mvnw spring-boot:run

# Windows
mvnw.cmd spring-boot:run
```

### 生产环境运行

```bash
java -jar target/Angel-backed-0.0.1-SNAPSHOT.jar
```

### 后台运行

```bash
# Linux
nohup java -jar target/Angel-backed-0.0.1-SNAPSHOT.jar > app.log 2>&1 &

# Windows (使用 PowerShell)
Start-Process java -ArgumentList "-jar", "target/Angel-backed-0.0.1-SNAPSHOT.jar" -WindowStyle Hidden

# Windows (使用批处理文件)
# 创建 run.bat 文件，内容如下：
# @echo off
# java -jar target/Angel-backed-0.0.1-SNAPSHOT.jar
# pause
```

## 部署到服务器

### 使用 systemd 管理服务 (Linux)

1. 创建服务文件：

```bash
sudo vim /etc/systemd/system/angel.service
```

2. 添加以下内容：

```ini
[Unit]
Description=Angel Backend Service
After=network.target

[Service]
Type=simple
User=www-data
WorkingDirectory=/path/to/your/app
ExecStart=/usr/bin/java -jar /path/to/your/app/target/Angel-backed-0.0.1-SNAPSHOT.jar
Restart=always
RestartSec=10

[Install]
WantedBy=multi-user.target
```

注意：请将 `/path/to/your/app` 替换为实际的应用路径。

3. 启动服务：

```bash
# 重新加载 systemd 配置
sudo systemctl daemon-reload

# 启动服务
sudo systemctl start angel

# 设置开机自启
sudo systemctl enable angel

# 查看服务状态
sudo systemctl status angel
```

### Windows 服务部署

在 Windows 上可以使用 winsw 工具将应用程序注册为系统服务：

1. 下载 winsw:
   - 访问 [WinSW 发布页面](https://github.com/winsw/winsw/releases) 下载最新版本
   - 重命名为 `angel-service.exe`

2. 创建配置文件 `angel-service.xml`：

```xml
<service>
  <id>AngelBackend</id>
  <name>Angel Backend Service</name>
  <description>Angel 后端服务</description>
  <executable>java</executable>
  <arguments>-jar "C:\path\to\your\app\target\Angel-backed-0.0.1-SNAPSHOT.jar"</arguments>
  <logmode>rotate</logmode>
</service>
```

3. 安装并启动服务：

```cmd
# 以管理员身份打开命令提示符
angel-service.exe install
angel-service.exe start
```

注意：请将 `C:\path\to\your\app` 替换为实际的应用路径。

### 使用 nginx 作为反向代理

1. 安装 nginx：

```bash
# Ubuntu/Debian
sudo apt install nginx

# CentOS/RHEL
sudo yum install nginx

# Windows
# 1. 访问 nginx 官网下载 Windows 版本
# 2. 解压到指定目录（例如 C:\nginx\）
```

2. 创建 nginx 配置文件：

```bash
# Linux
sudo vim /etc/nginx/sites-available/angel
```

对于 Windows 系统，配置文件通常位于 nginx 安装目录下的 conf\nginx.conf：

```cmd
notepad C:\nginx\conf\nginx.conf
```

3. 添加配置内容：

```nginx
server {
    listen 80;
    server_name your-domain.com;  # 替换为你的域名

    location / {
        proxy_pass http://localhost:8080;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
    }
}
```

4. 启用配置：

```bash
# Linux
sudo ln -s /etc/nginx/sites-available/angel /etc/nginx/sites-enabled/
sudo nginx -t
sudo systemctl reload nginx
```

在 Windows 上启动 nginx：

```cmd
# 进入 nginx 安装目录
 cd C:\nginx
# 启动 nginx
start nginx

# 重新加载配置
nginx -s reload

# 停止 nginx
nginx -s stop
```

## AI 功能配置

项目支持 OpenAI 和火山引擎两种 AI 提供商，相关配置在 [application.properties](file:///Users/lizixuan/Desktop/Angel/Angel-backed/src/main/resources/application.properties) 文件中：

```properties
# AI API 配置
ai.api.key=cddee0f6-1bb3-40b0-a82b-68faf21cbd2a
ai.api.endpoint=https://api.openai.com/v1/chat/completions
ai.model=gpt-3.5-turbo
ai.temperature=0.7
# AI提供商: openai 或 volcengine
ai.provider=volcengine

# 火山引擎AI配置
ai.volcengine.api.key=cddee0f6-1bb3-40b0-a82b-68faf21cbd2a
ai.volcengine.base.url=https://ark.cn-beijing.volces.com/api/v3
ai.volcengine.model=doubao-seed-1-6-251015
```

请根据实际需求修改相关配置。

更多关于 AI 模型接入的信息，请参考 [README_AI_MODEL.md](file:///Users/lizixuan/Desktop/Angel/Angel-backed/README_AI_MODEL.md)。

## 常见问题

### 1. 端口被占用

如果默认端口 8080 被占用，可以通过以下方式修改端口：

```bash
java -jar target/Angel-backed-0.0.1-SNAPSHOT.jar --server.port=8081
```

或者在 [application.properties](file:///Users/lizixuan/Desktop/Angel/Angel-backed/src/main/resources/application.properties) 中添加：

```properties
server.port=8081
```

### 2. 数据库连接失败

请检查以下几点：
1. MySQL 服务是否正在运行
2. 数据库地址、用户名、密码是否正确
3. 防火墙是否阻止了数据库连接

### 3. 内存不足

如果服务器内存较小，可以限制 JVM 内存使用：

```bash
# Linux
java -Xmx512m -jar target/Angel-backed-0.0.1-SNAPSHOT.jar
```

在 Windows 上也可以使用相同命令：

```cmd
java -Xmx512m -jar target/Angel-backed-0.0.1-SNAPSHOT.jar
```

### 4. 日志查看

应用日志默认输出到控制台，如果使用 nohup 启动，则会记录在 app.log 文件中。

在 Windows 上，如果使用后台运行方式，日志可能会保存在日志文件中，或者可以通过 PowerShell 命令查看进程输出。