# smarthub - 企业级 AI 智能管理平台

## 项目简介

一个集成了 AI 能力和多协议接入的企业级后台管理系统。支持 AI 智能问答（SSE 流式响应）、RBAC 权限管理、MQTT/TCP 协议接入。

## 技术栈

| 层级 | 技术 |
|---|---|
| 后端 | Spring Boot 3.4.1 + Java 17 + MyBatis-Plus 3.5.7 |
| 数据库 | MySQL 8.0 |
| 认证 | Spring Security + JWT (HS256) |
| AI | 策略模式 + 工厂注册 (Ollama/通义千问/Claude/DeepSeek) |
| 协议 | 策略模式 + 工厂注册 (MQTT/TCP) |
| 缓存 | Redis 7 (JSON 序列化) |
| 消息队列 | RabbitMQ 3 (@EnableRabbit) |
| 全文检索 | Elasticsearch 8.15 (WebClient) |
| API 文档 | Knife4j 4.5 (Swagger UI) |
| 前端 | Vue 3 + TypeScript + Element Plus + Pinia + Vite |
| 容器化 | Docker + docker-compose |

## 架构设计

### 1. AI 适配器（策略模式 + 工厂注册）

```
AiModelAdapter (接口)
├── getProviderName()       → "ollama" / "dashscope" / "anthropic" / "deepseek"
├── chatStream(request)     → SSE 流式响应
├── chatBlocking(request)   → 阻塞响应
└── isAvailable()           → 健康检查

AiAdapterFactory (自动注册中心)
├── @PostConstruct 扫描所有 AiModelAdapter 实现
├── ConcurrentHashMap 缓存
└── getAdapter(providerName) / getDefaultAdapter()
```

| 适配器 | 协议端点 | 认证方式 | 流式解析 |
|---|---|---|---|
| Ollama | POST `/api/chat` | 无 | newline-delimited JSON |
| DashScope | POST `/api/v1/services/aigc/text-generation/generation` | Bearer Token | SSE `data:` 前缀 |
| Anthropic | POST `/v1/messages` | x-api-key Header | event:data SSE |
| DeepSeek | POST `/v1/chat/completions` | Bearer Token | OpenAI 兼容 |

### 2. 协议适配器（策略模式 + 工厂注册）

```
ProtocolAdapter (接口)
├── getProtocolName()     → "mqtt" / "tcp"
├── start()               → 启动监听/连接
├── stop()                → 停止
├── send(deviceId, data)  → 发送数据
└── isAlive(deviceId)     → 心跳检测

ProtocolAdapterFactory (自动注册中心)
```

| 适配器 | 框架 | 关键特性 |
|---|---|---|
| MQTT | Eclipse Paho | MqttCallback (connectionLost/messageArrived/deliveryComplete)、自动重连、QoS |
| TCP | Netty | ServerBootstrap + Boss/Worker Group + ChannelPipeline + TcpServerHandler |

### 3. 认证授权流程

1. 用户登录 → `AuthController.login()` → `AuthenticationManager` 验证 → 签发 JWT
2. 返回 `accessToken`(2h) + `refreshToken`(7d)
3. 后续请求 `JwtAuthenticationFilter` 拦截验证 token
4. 从 DB 加载用户角色/权限 → 设置 `SecurityContext`
5. `SecurityConfig` 基于 `@PreAuthorize("hasAuthority('xxx')")` 做接口级鉴权

### 4. 动态路由

登录后调用 `/api/auth/info` 获取当前用户可见菜单树 → 前端通过 `router.addRoute()` 动态注册 → 侧边栏由后端菜单驱动

## 快速开始

### 方式一：Docker Compose 一键启动

```bash
docker compose up -d
```

会自动启动：MySQL、Redis、RabbitMQ、Elasticsearch、Kibana、后端、前端

### 方式二：手动分步启动

#### 1. 启动基础设施

```bash
docker compose up -d mysql redis rabbitmq elasticsearch kibana
```

#### 2. 等待服务就绪后，访问 Knife4j API 文档

```
http://localhost:8080/doc.html
```

#### 3. 启动前端

```bash
cd frontend
npm install
npm run dev
```

访问: http://localhost:5173

## 默认账号

| 用户名 | 密码 | 角色 |
|---|---|---|
| admin | admin123 | 管理员 (所有权限) |
| user | user123 | 普通用户 (基础功能) |

## API 端点一览

### 认证模块 (`/api/auth`)

| Method | Path | 说明 | 权限 |
|---|---|---|---|
| POST | `/api/auth/login` | 用户登录 | 公开 |
| POST | `/api/auth/logout` | 用户登出 | 已登录 |
| POST | `/api/auth/refresh` | 刷新 Token | 刷新 Token |
| GET | `/api/auth/info` | 当前用户信息+菜单树 | 已登录 |

### 用户管理 (`/api/users`)

| Method | Path | 说明 | 权限 |
|---|---|---|---|
| GET | `/api/users` | 用户列表(分页) | `sys:user:list` |
| GET | `/api/users/{id}` | 用户详情 | `sys:user:list` |
| POST | `/api/users` | 创建用户 | `sys:user:add` |
| PUT | `/api/users/{id}` | 更新用户 | `sys:user:edit` |
| DELETE | `/api/users/{id}` | 删除用户 | `sys:user:delete` |
| PUT | `/api/users/{id}/roles` | 分配角色 | `sys:user:edit` |

### 角色管理 (`/api/roles`)

| Method | Path | 说明 | 权限 |
|---|---|---|---|
| GET | `/api/roles` | 角色列表 | `sys:role:list` |
| POST | `/api/roles` | 创建/更新角色 | `sys:role:add/edit` |
| DELETE | `/api/roles/{id}` | 删除角色 | `sys:role:delete` |
| PUT | `/api/roles/{id}/menus` | 分配菜单权限 | `sys:role:assign` |

### 菜单管理 (`/api/menus`)

| Method | Path | 说明 | 权限 |
|---|---|---|---|
| GET | `/api/menus/tree` | 菜单树 | 已登录 |
| POST | `/api/menus` | 创建菜单 | `sys:menu:add` |
| PUT | `/api/menus/{id}` | 更新菜单 | `sys:menu:edit` |
| DELETE | `/api/menus/{id}` | 删除菜单 | `sys:menu:delete` |

### AI 功能 (`/api/ai`)

| Method | Path | 说明 | 权限 |
|---|---|---|---|
| POST | `/api/ai/chat/stream` | SSE 流式聊天 | 已登录 |
| GET | `/api/ai/models` | 获取可用 AI 模型 | 已登录 |
| POST | `/api/ai/switch` | 切换默认 AI 模型 | 已登录 |

### 协议管理 (`/api/protocol`)

| Method | Path | 说明 | 权限 |
|---|---|---|---|
| POST | `/api/protocol/send/{protocol}/{deviceId}` | 向设备发送数据 | 已登录 |
| GET | `/api/protocol/status` | 获取所有协议状态 | 已登录 |
| POST | `/api/protocol/start-all` | 启动所有协议 | 已登录 |
| POST | `/api/protocol/stop-all` | 停止所有协议 | 已登录 |

## 扩展指南

### 新增 AI 模型（只需 3 步）

```java
// 1. 实现 AiModelAdapter 接口
@Component
public class XxxAiAdapter implements AiModelAdapter {
    @Override
    public String getProviderName() { return "xxx"; }

    @Override
    public Flux<String> chatStream(String model, String systemPrompt, String userMessage) {
        // 实现流式调用逻辑
    }

    @Override
    public String chatBlocking(String model, String systemPrompt, String userMessage) {
        // 实现阻塞调用逻辑
    }

    @Override
    public boolean isAvailable() { return true; }
}

// 2. @Component 已标注，Spring 自动注册到 AiAdapterFactory

// 3. 在 application.yml 添加配置
ai:
  adapters:
    xxx:
      api-key: your-key
      base-url: https://xxx.api
```

### 新增协议（只需 3 步）

```java
// 1. 实现 ProtocolAdapter 接口
@Component
public class XxxProtocolAdapter implements ProtocolAdapter {
    @Override
    public String getProtocolName() { return "xxx"; }

    @Override
    public void start() throws Exception { /* 启动连接 */ }

    @Override
    public void stop() { /* 断开连接 */ }

    @Override
    public boolean send(String deviceId, byte[] data) { /* 发送数据 */ }

    @Override
    public boolean isAlive(String deviceId) { return true; }
}

// 2. @Component 已标注，Spring 自动注册到 ProtocolAdapterFactory

// 3. 在 application.yml 添加配置
protocol:
  xxx:
    host: localhost
    port: 8888
```

## 项目结构

```
smarthub/
├── pom.xml                              # Maven 依赖配置
├── Dockerfile                           # 后端多阶段构建镜像
├── docker-compose.yml                   # 全量基础设施编排
├── README.md                            # 本文档
│
├── src/main/java/com/example/smarthub/
│   ├── SmarthubApplication.java         # 启动类
│   ├── common/                          # 公共模块
│   │   ├── base/BaseEntity.java         # 实体基类 (ID/时间/逻辑删除)
│   │   ├── enums/ErrorCode.java         # 错误码枚举 (14种)
│   │   ├── exception/
│   │   │   ├── BizException.java        # 业务异常
│   │   │   └── GlobalExceptionHandler.java  # 全局异常处理
│   │   ├── response/R.java             # 统一响应封装
│   │   └── util/JwtUtil.java           # JWT 工具类
│   ├── config/                          # 配置类
│   │   ├── SecurityConfig.java          # Spring Security + JWT 过滤器链
│   │   ├── JwtAuthenticationFilter.java  # JWT 认证过滤器
│   │   ├── MybatisPlusConfig.java       # MyBatis-Plus 分页/乐观锁
│   │   ├── MyBatisMetaHandler.java      # 自动填充 createTime/updateTime
│   │   ├── RedisConfig.java             # Redis JSON 序列化
│   │   ├── RabbitMQConfig.java          # RabbitMQ + @EnableRabbit
│   │   ├── ElasticsearchConfig.java     # ES WebClient
│   │   └── SwaggerConfig.java           # Knife4j API 分组
│   └── module/
│       ├── system/                      # 系统管理模块 (RBAC)
│       │   ├── controller/              # Auth/User/Role/Menu
│       │   ├── service/impl/            # 业务逻辑
│       │   ├── mapper/                  # MyBatis Mapper
│       │   ├── entity/                  # 实体类
│       │   ├── dto/                     # 请求对象
│       │   └── vo/                      # 视图对象
│       ├── ai/                          # AI 模块
│       │   ├── adapter/                 # AI 适配器 (策略模式)
│       │   │   ├── AiModelAdapter.java  # 接口
│       │   │   ├── AiAdapterFactory.java # 自动注册中心
│       │   │   ├── ollama/              # Ollama 本地模型
│       │   │   ├── dashscope/           # 通义千问
│       │   │   ├── anthropic/           # Claude
│       │   │   └── deepseek/            # DeepSeek
│       │   ├── controller/              # AiChatController
│       │   ├── service/                 # AiChatService
│       │   ├── entity/                  # 会话/消息/模型配置
│       │   ├── mapper/                  # AI 数据访问
│       │   └── dto/                     # 聊天请求
│       └── protocol/                    # 协议模块
│           ├── adapter/                 # 协议适配器 (策略模式)
│           │   ├── ProtocolAdapter.java # 接口
│           │   ├── ProtocolAdapterFactory.java # 自动注册中心
│           │   ├── mqtt/                # MQTT (Eclipse Paho)
│           │   └── tcp/                 # TCP (Netty)
│           ├── controller/              # ProtocolController
│           └── service/                 # ProtocolService
│
├── src/main/resources/
│   ├── application.yml                  # 主配置 (profile: dev)
│   ├── application-dev.yml              # 开发环境配置
│   ├── application-prod.yml             # 生产环境配置
│   ├── application-test.yml             # 测试环境配置
│   └── db/
│       ├── V1__init_schema.sql          # 9 张建表 SQL
│       └── V2__init_data.sql           # 种子数据
│
├── frontend/                            # Vue 3 前端项目
│   ├── package.json
│   ├── vite.config.ts                   # Vite + 代理配置
│   └── src/
│       ├── api/                         # Axios 请求封装
│       │   ├── request.ts               # 拦截器 (JWT/错误处理)
│       │   ├── auth.ts                  # 认证 API
│       │   ├── menu.ts                  # 菜单 API
│       │   └── ai.ts                    # AI 聊天 API (SSE)
│       ├── views/                       # 页面组件
│       │   ├── login/Login.vue          # 登录页
│       │   ├── dashboard/Dashboard.vue  # 首页统计
│       │   ├── system/                  # 系统管理页 (待完善)
│       │   └── ai/                      # AI 功能页 (待完善)
│       ├── layouts/DefaultLayout.vue    # 主布局 (侧边栏+头部)
│       ├── router/                      # 路由配置
│       │   ├── index.ts                 # 常量路由 + 守卫
│       │   └── dynamic.ts               # 动态路由注册
│       ├── store/user.ts                # Pinia 用户状态
│       ├── types/                       # TypeScript 类型
│       └── utils/auth.ts                # Token 工具函数
│
└── generator/template/                  # [预留] AI 代码生成 Velocity 模板
```

## 数据库表

| 表名 | 说明 |
|---|---|
| sys_user | 用户表 |
| sys_role | 角色表 |
| sys_menu | 菜单表 (支持目录/菜单/按钮三级) |
| sys_user_role | 用户角色关联 |
| sys_role_permission | 角色菜单关联 |
| ai_model_config | AI 模型配置 |
| ai_conversation | AI 会话 |
| ai_message | AI 消息记录 |

## 注意事项

1. **数据库初始化**：`docker-compose.yml` 会自动将 `db/` 目录挂载到 `/docker-entrypoint-initdb.d`，首次启动 MySQL 时自动执行建表 SQL
2. **AI 模型配置**：默认使用 Ollama（本地），如需切换到其他模型，在 `application.yml` 中修改 `ai.default-adapter`
3. **协议启动**：MQTT 和 TCP 协议需要在应用启动后手动调用 `/api/protocol/start-all` 启动
4. **前端页面**：用户管理/角色管理/菜单管理/AI 聊天/AI 代码生成页面目前为骨架界面，核心后端 API 已完整实现
