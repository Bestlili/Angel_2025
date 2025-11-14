# AI模型接入说明

## 火山引擎Doubao-Seed-1.6模型接入

本项目已接入火山引擎的Doubao-Seed-1.6 AI模型，以下是配置和使用说明。

### 1. 配置说明

在 `application.properties` 文件中添加以下配置：

```properties
# 火山引擎AI配置
ai.volcengine.api.key=YOUR_API_KEY_HERE
ai.volcengine.base.url=https://ark.cn-beijing.volces.com/api/v3
ai.volcengine.model=doubao-seed-1-6-251015
```

并将AI提供商设置为火山引擎：
```properties
ai.provider=volcengine
```

### 2. 获取API Key

1. 访问[火山引擎控制台](https://console.volcengine.com/)
2. 创建或选择已有项目
3. 在AI服务中找到方舟推理服务
4. 创建推理接入点并获取API Key

### 3. 测试接口

项目提供以下测试接口：

#### 3.1 测试普通调用

POST `/api/test/volcengine-ai`

请求体示例：
```json
{
  "message": "你好，今天天气怎么样？",
  "context": []
}
```

#### 3.2 测试流式调用

POST `/api/test/volcengine-ai/stream`

请求体示例：
```json
{
  "message": "给我讲一个有趣的故事",
  "context": [
    {
      "role": "user",
      "content": "你好"
    },
    {
      "role": "assistant",
      "content": "你好！有什么我可以帮你的吗？"
    }
  ]
}
```

### 4. 切换AI提供商

通过修改 `ai.provider` 配置项可以在不同AI模型间切换：
- `openai`: 使用OpenAI的GPT模型（默认）
- `volcengine`: 使用火山引擎的Doubao-Seed模型

### 5. 代码结构

- `VolcengineAIUtil.java`: 火山引擎AI调用工具类
- `AIUtil.java`: 统一AI调用入口，支持多种AI提供商
- `AIModelTestController.java`: AI模型测试接口控制器

### 6. 注意事项

1. 请确保网络可以访问火山引擎的服务地址
2. API Key需要妥善保管，不要泄露
3. 根据实际需求调整模型参数和调用方式