# SmartHub — 企业级 AI 智能管理平台

> 基于 Spring Boot 3.4 + Vue 3 的全栈智能管理平台，集成多 AI 提供商、IoT 设备协议适配、动态 RBAC 权限管理。

## 📋 目录

- [技术栈](#技术栈)
- [项目结构](#项目结构)
- [快速开始](#快速开始)
- [数据库初始化](#数据库初始化)
- [API 文档](#api-文档)
- [模块说明](#模块说明)
- [配置说明](#配置说明)
- [日志说明](#日志说明)
- [Docker 部署](#docker-部署)

## 技术栈

### 后端

| 技术 | 版本 | 用途 |
|------|------|------|
| Spring Boot | 3.4.1 | 核心框架 |
| MyBatis-Plus | 3.5.7 | ORM / 分页 / 乐观锁 |
| Spring Security 6 | - | JWT 认证 / RBAC 权限 |
| MySQL | 8.0 | 主数据库 |
| Redis | 7 | 缓存 |
| RabbitMQ | 3 | 消息队列 |
| Elasticsearch | 8.15 | 搜索引擎 |
| Netty | 4.1.115 | TCP 协议适配 |
| Eclipse Paho | - | MQTT 协议适配 |
| SpringDoc / Knife4j | 2.7.0 / 4.5.0 | API 文档 |
| Lombok | 1.18.34 | 代码简化 |

### 前端

| 技术 | 版本 | 用途 |
|------|------|------|
| Vue 3 | 3.5.13 | 渐进式框架 |
| TypeScript | 5.7 | 类型安全 |
| Vite | 6.1 | 构建工具 |
| Element Plus | 2.9 | UI 组件库 |
| Pinia | 2.3 | 状态管理 |
| Vue Router | 4.5 | 路由管理 |
| Axios | 1.7 | HTTP 客户端 |
| markdown-it | 14.1 | Markdown 渲染 |

### 基础设施

```
docker-compose.yml
├── MySQL 8.0
├── Redis 7
├── RabbitMQ 3
├── Elasticsearch 8.15
├── Kibana 8.15
└── Nginx (前端静态资源)
```

## 项目结构

```
smarthub/
├── src/main/java/com/example/smarthub/
│   ├── common/                    # 公共基础设施
│   │   ├── base/BaseEntity.java   # 实体基类（id, createTime, deleted...）
│   │   ├── enums/ErrorCode.java   # 错误码枚举
│   │   ├── exception/             # 业务异常 + 全局异常处理
│   │   ├── response/R.java        # 统一响应封装
│   │   └── util/JwtUtil.java      # JWT 工具类
│   ├── config/                    # 配置类
│   │   ├── SecurityConfig.java    # Spring Security + JWT
│   │   ├── JwtAuthenticationFilter.java  # JWT 认证过滤器
│   │   ├── MybatisPlusConfig.java # MyBatis-Plus 分页/乐观锁
│   │   ├── MyBatisMetaHandler.java # 自动填充 createTime/updateBy
│   │   ├── RedisConfig.java       # Redis 序列化配置
│   │   ├── RabbitMQConfig.java    # RabbitMQ 配置
│   │   ├── ElasticsearchConfig.java # ES WebClient
│   │   └── SwaggerConfig.java     # API 文档分组
│   ├── module/
│   │   ├── system/                # 系统模块（RBAC）
│   │   │   ├── controller/        # Auth / User / Role / Menu
│   │   │   ├── service/           # 业务逻辑
│   │   │   ├── entity/            # 数据库实体
│   │   │   ├── dto/               # 请求数据传输对象
│   │   │   ├── vo/                # 视图对象
│   │   │   └── mapper/            # MyBatis-Plus Mapper
│   │   ├── ai/                    # AI 模块（适配器模式）
│   │   │   ├── adapter/           # 4 个提供商适配器
│   │   │   │   ├── OllamaAiAdapter.java
│   │   │   │   ├── DashScopeAiAdapter.java
│   │   │   │   ├── AnthropicAiAdapter.java
│   │   │   │   └── DeepSeekAiAdapter.java
│   │   │   ├── controller/        # AiChatController
│   │   │   ├── service/           # AiChatService
│   │   │   ├── entity/            # Conversation / Message / ModelConfig
│   │   │   ├── dto/               # ChatRequest
│   │   │   └── mapper/
│   │   ├── protocol/              # 协议模块（适配器模式）
│   │   │   ├── adapter/           # MQTT / TCP 适配器
│   │   │   ├── controller/        # ProtocolController
│   │   │   └── service/           # ProtocolService
│   │   └── monitor/               # 监控模块
│   │       ├── aspect/RequestLogAspect.java  # 请求日志 AOP 切面
│   │       ├── entity/RequestLog.java
│   │       ├── mapper/RequestLogMapper.java
│   │       └── service/RequestLogService.java
│   └── SmarthubApplication.java   # 应用入口
├── src/main/resources/
│   ├── application.yml            # 全局配置（入口）
│   ├── application-dev.yml        # 开发环境配置
│   ├── application-test.yml       # 测试环境配置
│   ├── application-prod.yml       # 生产环境配置
│   ├── logback-spring.xml         # 日志配置（分模块/分环境）
│   └── db/
│       ├── V1__init_schema.sql    # 数据库建表
│       ├── V2__init_data.sql      # 种子数据
│       └── V3__create_request_log.sql  # 请求日志表
└── frontend/                      # Vue 3 前端
    ├── src/
    │   ├── api/                   # Axios 封装 + 接口调用
    │   ├── router/                # 静态路由 + 动态路由
    │   ├── store/                 # Pinia 状态管理
    │   ├── views/                 # 页面组件
    │   │   ├── login/             # 登录页
    │   │   ├── dashboard/         # 仪表盘
    │   │   ├── system/            # 系统管理（用户/角色/菜单）
    │   │   └── ai/                # AI 功能（聊天/代码生成）
    │   ├── layouts/               # 布局组件
    │   └── types/                 # TypeScript 类型定义
    └── vite.config.ts             # Vite 配置
```

## 快速开始

### 前置条件

- JDK 17+
- Maven 3.8+
- Node.js 18+
- MySQL 8.0
- Redis
- RabbitMQ（可选）

### 1. 后端启动

```bash
# 编译打包
mvn clean package -DskipTests

# 启动（默认使用 dev 配置）
mvn spring-boot:run
# 或
java -jar target/smarthub-0.0.1-SNAPSHOT.jar

# 服务运行在 http://localhost:8080
```

### 2. 前端启动

```bash
cd frontend

# 安装依赖
npm install

# 开发模式启动（默认端口 5173）
npm run dev

# 生产构建
npm run build
```

### 3. 默认账号

| 用户名 | 密码 | 角色 |
|--------|------|------|
| admin | admin123 | 管理员（全部权限） |
| user | user123 | 普通用户（基础功能） |

## 数据库初始化

项目使用 SQL 脚本初始化数据库，执行顺序如下：

```bash
# 1. 创建数据库和表结构
mysql -u root -p < src/main/resources/db/V1__init_schema.sql

# 2. 插入种子数据（管理员、角色、菜单、AI 模型配置）
mysql -u root -p < src/main/resources/db/V2__init_data.sql

# 3. 创建请求日志表
mysql -u root -p < src/main/resources/db/V3__create_request_log.sql
```

### 数据库表清单

| 表名 | 说明 |
|------|------|
| `sys_user` | 用户表 |
| `sys_role` | 角色表 |
| `sys_menu` | 菜单表 |
| `sys_user_role` | 用户-角色关联 |
| `sys_role_permission` | 角色-菜单关联 |
| `ai_model_config` | AI 模型配置 |
| `ai_conversation` | AI 会话 |
| `ai_message` | AI 消息 |
| `request_log` | 请求日志 |

## API 文档

启动后端后访问：

- **Knife4j 增强文档**: http://localhost:8080/doc.html
- **Swagger UI**: http://localhost:8080/swagger-ui.html

API 按模块分为 6 组：认证管理、用户管理、角色管理、菜单管理、AI 功能、协议管理。

## 模块说明

### 系统模块（RBAC 权限管理）

采用经典的 RBAC 模型：
- **用户 ↔ 角色**: 多对多（`sys_user_role`）
- **角色 ↔ 菜单**: 多对多（`sys_role_permission`）
- **菜单**: 三级结构（目录 → 菜单 → 按钮），通过 `menu_type` 区分
- **认证流程**: 登录 → JWT → 过滤器解析 → 加载权限 → `@PreAuthorize` 校验
- **动态路由**: 前端根据后端返回的菜单树动态注册路由

### AI 模块

采用**适配器模式 + 工厂自动发现**：

```
AiModelAdapter (接口)
├── OllamaAiAdapter      (本地模型，无需 API Key)
├── DashScopeAiAdapter   (阿里云通义千问)
├── AnthropicAiAdapter   (Claude)
└── DeepSeekAiAdapter    (DeepSeek)
```

新增提供商只需实现 `AiModelAdapter` 接口并标注 `@Component`，无需修改其他代码。

**聊天流程**: 前端发送消息 → Controller → Service 选择适配器 → 调用 `chatStream()` → Flux 流式返回 → SSE 推送 → 消息持久化到 `ai_message` 表。

### 协议模块

```
ProtocolAdapter (接口)
├── MqttProtocolAdapter  (Eclipse Paho，连接 MQTT Broker)
└── TcpProtocolAdapter   (Netty NIO，TCP 服务端)
```

### 监控模块

- **RequestLogAspect**: AOP 切面自动拦截所有 Controller 请求
- 记录：IP、方法、URL、耗时、模块、操作用户、是否异常
- 异步写入数据库，不阻塞业务
- 独立日志文件 `request.log`

### 限流模块

- **@RateLimit 注解**: 基于 Redis 滑动窗口限流
- 支持按 IP/用户名/接口等维度限流（SpEL 表达式）
- 登录接口默认限流：每分钟 10 次
- 可通过 `rate-limit.enabled` 配置全局开关

## 前端页面一览

| 页面 | 路由 | 对应后端接口 | 功能 |
|------|------|-------------|------|
| 登录 | `/login` | `POST /api/auth/login` | 用户名密码登录 |
| 首页 | `/dashboard` | `GET /api/users`, `/roles`, `/ai/models`, `/protocol/status` | 统计概览 |
| 用户管理 | `/system/user` | `GET/POST/PUT/DELETE /api/users` | 用户 CRUD + 分页 + 搜索 |
| 角色管理 | `/system/role` | `GET/POST/DELETE /api/roles` | 角色 CRUD |
| 菜单管理 | `/system/menu` | `GET/POST/PUT/DELETE /api/menus` | 菜单树 CRUD |
| AI 聊天 | `/ai/chat` | `POST /api/ai/chat/stream`, `GET /api/ai/models` | SSE 流式聊天 + Markdown 渲染 |
| 代码生成 | `/ai/codegen` | `POST /api/ai/chat/stream` | AI 驱动代码生成 |
| 协议管理 | `/protocol` | `GET/POST /api/protocol/*` | 协议启停 + 设备数据收发 |

## 配置说明

### 多环境配置

通过 `spring.profiles.active` 切换环境：

| 环境 | 文件 | 特点 |
|------|------|------|
| dev | `application-dev.yml` | DEBUG 级别、SQL 打印、Knife4j 开启 |
| test | `application-test.yml` | 独立测试库、短 Token 过期 |
| prod | `application-prod.yml` | WARN 级别、环境变量注入、关闭文档 |

### AI 提供商配置

```yaml
ai:
  default-adapter: ollama        # 默认提供商
  adapters:
    anthropic:
      api-key: ${AI_ANTHROPIC_KEY}   # 从环境变量读取
  models:
    ollama: qwen2.5:7b
    anthropic: claude-sonnet-4-20250514
```

## 日志说明

日志采用 **Logback** 配置，支持按模块分文件、按日期+大小分割：

```
logs/
├── smarthub.log              # 主日志（INFO+）
├── smarthub-error.log        # 全局错误日志（ERROR+）
├── system.log                # 系统模块日志
├── system-error.log
├── ai.log                    # AI 模块日志
├── ai-error.log
├── protocol.log              # 协议模块日志
├── protocol-error.log
└── request.log               # 请求日志（AOP 切面）
```

- 按天分割，单文件超过 100MB 也会分割
- 保留最近 30 天，总容量上限 3GB
- 生产环境自动降低日志级别
- 日志路径可通过 `LOG_PATH` 环境变量覆盖

## Docker 部署

```bash
# 一键启动所有基础设施
docker-compose up -d

# 构建后端镜像
docker build -t smarthub-backend .

# 构建前端镜像（使用 Nginx）
docker build -f Dockerfile.nginx -t smarthub-frontend ./frontend
```

## 许可证

MIT
