# SmartHub — 企业级 AI 智能管理平台

> 基于 Spring Boot 3.4 + Vue 3 的全栈智能管理平台，集成多 AI 提供商、IoT 设备协议适配、动态 RBAC 权限管理。

## 📋 目录

- [环境准备](#环境准备)
- [技术栈](#技术栈)
- [项目结构](#项目结构)
- [快速开始](#快速开始)
- [数据库初始化](#数据库初始化)
- [API 文档](#api-文档)
- [模块说明](#模块说明)
- [配置说明](#配置说明)
- [日志说明](#日志说明)
- [Docker 部署](#docker-部署)
- [更新日志](#更新日志)

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
│       └── V4__add_new_permissions.sql  # 新增权限菜单
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

## 环境准备

> 本文档详细说明运行 SmartHub 所需的全部环境和依赖。按以下步骤准备即可。

### 一、必需环境

#### 1. 开发语言与构建工具

| 软件 | 最低版本 | 推荐版本 | 用途 | 下载链接 |
|------|---------|---------|------|---------|
| JDK | 17 | JDK 17 或 21 (Corretto/OpenJDK) | 后端编译运行 | [Oracle JDK](https://www.oracle.com/java/technologies/downloads/) / [OpenJDK](https://adoptium.net/) |
| Maven | 3.8 | 3.9+ | 后端构建 | [Apache Maven](https://maven.apache.org/download.cgi) |
| Node.js | 18 | 20 LTS | 前端构建运行 | [Node.js](https://nodejs.org/) |
| npm | 9+ | 10+ | 前端包管理 | 随 Node.js 一起安装 |

**验证安装：**
```bash
java -version       # 应显示 17.x 或更高
mvn -version        # 应显示 3.8+
node -v             # 应显示 18+
npm -v              # 应显示 9+
```

#### 2. 数据库

| 软件 | 最低版本 | 用途 | 默认端口 |
|------|---------|------|---------|
| MySQL | 8.0 | 主数据存储 | 3306 |

**安装步骤（Windows）：**
```bash
# 方式一：下载安装包
# 访问 https://dev.mysql.com/downloads/installer/ 下载 MySQL Installer
# 安装时选择 "Developer Default" 或 "Server only"
# 设置 root 密码为 root（与 application-dev.yml 一致）

# 方式二：使用 Docker
docker run --name smarthub-mysql \
  -e MYSQL_ROOT_PASSWORD=root \
  -e MYSQL_DATABASE=smarthub \
  -p 3306:3306 \
  -d mysql:8.0
```

**验证：**
```bash
mysql -u root -p -e "SELECT VERSION();"
# 应返回 8.0.x
```

#### 3. 缓存服务

| 软件 | 最低版本 | 用途 | 默认端口 |
|------|---------|------|---------|
| Redis | 6+ | 缓存 / 限流计数器 | 6379 |

**安装步骤：**
```bash
# 方式一：Windows 安装
# 访问 https://github.com/microsoftarchive/redis/releases 下载 Windows 版 MSI 安装包
# 安装后默认以 Windows 服务运行

# 方式二：使用 Docker
docker run --name smarthub-redis \
  -p 6379:6379 \
  -d redis:7-alpine

# 方式三：macOS
brew install redis
brew services start redis
```

**验证：**
```bash
redis-cli ping
# 应返回 PONG
```

---

### 二、可选环境

#### 1. 消息队列

| 软件 | 最低版本 | 用途 | 默认端口 |
|------|---------|------|---------|
| RabbitMQ | 3+ | 消息队列（预留） | 5672 (AMQP) / 15672 (管理界面) |

**安装步骤：**
```bash
# 使用 Docker
docker run --name smarthub-rabbitmq \
  -p 5672:5672 -p 15672:15672 \
  -e RABBITMQ_DEFAULT_USER=guest \
  -e RABBITMQ_DEFAULT_PASS=guest \
  -d rabbitmq:3-management-alpine
```

> 注：当前项目中 RabbitMQ 已配置但未实际使用，仅预留扩展。

#### 2. 搜索引擎

| 软件 | 最低版本 | 用途 | 默认端口 |
|------|---------|------|---------|
| Elasticsearch | 8.0 | 全文搜索（预留） | 9200 |
| Kibana | 8.0 | ES 可视化（预留） | 5601 |

**安装步骤：**
```bash
# 使用 Docker Compose 一键启动
docker run --name smarthub-es \
  -e "discovery.type=single-node" \
  -e "xpack.security.enabled=false" \
  -e "ES_JAVA_OPTS=-Xms512m -Xmx512m" \
  -p 9200:9200 \
  -d elasticsearch:8.15.0

docker run --name smarthub-kibana \
  -e ELASTICSEARCH_HOSTS=http://localhost:9200 \
  -p 5601:5601 \
  --depends-on smarthub-es \
  -d kibana:8.15.0
```

> 注：ES 和 Kibana 当前仅预留配置，实际业务未使用。

---

### 三、端口总览

| 服务 | 端口 | 用途 | 必需 |
|------|------|------|------|
| MySQL | 3306 | 数据库 | ✅ 必需 |
| Redis | 6379 | 缓存 / 限流 | ✅ 必需 |
| RabbitMQ | 5672 | 消息队列 | ⭕ 可选 |
| RabbitMQ Admin | 15672 | 管理界面 | ⭕ 可选 |
| Elasticsearch | 9200 | 搜索引擎 | ⭕ 可选 |
| Kibana | 5601 | 可视化 | ⭕ 可选 |
| 后端 API | 8080 | Spring Boot 服务 | ✅ 必需 |
| 前端开发 | 5173 | Vite 开发服务器 | ✅ 必需 |
| 前端生产 | 80 | Nginx 静态资源 | ⭕ 可选 |
| API 文档 | 8080/doc.html | Knife4j 文档 | ✅ 必需 |

---

### 四、快速一键启动（推荐）

如果安装了 **Docker + Docker Compose**，一条命令启动所有依赖：

```bash
cd smarthub
docker-compose up -d
```

这将自动启动：MySQL、Redis、RabbitMQ、Elasticsearch、Kibana。

等待所有服务就绪后（约 1-2 分钟）：
```bash
# 检查服务状态
docker-compose ps

# 查看 MySQL 是否就绪
docker-compose logs mysql | grep "ready for connections"
```

---

### 五、环境变量配置

生产环境部署时，以下配置通过环境变量注入（见 `application-prod.yml`）：

| 环境变量 | 说明 | 默认值 |
|---------|------|--------|
| `DB_HOST` | 数据库主机 | `localhost` |
| `DB_PORT` | 数据库端口 | `3306` |
| `DB_NAME` | 数据库名 | `smarthub` |
| `DB_USERNAME` | 数据库用户名 | `root` |
| `DB_PASSWORD` | 数据库密码 | （空） |
| `REDIS_HOST` | Redis 主机 | `localhost` |
| `REDIS_PASSWORD` | Redis 密码 | （空） |
| `JWT_SECRET` | JWT 签名密钥 | ⚠️ 必须修改 |
| `AI_ANTHROPIC_KEY` | Anthropic API Key | 占位符 |
| `AI_DASHSCOPE_KEY` | 阿里云 API Key | 占位符 |
| `AI_DEEPSEEK_KEY` | DeepSeek API Key | 占位符 |
| `LOG_PATH` | 日志输出目录 | `./logs` |

---

### 六、常见问题

**Q: MySQL 连接失败？**
```
A: 检查 MySQL 是否运行：mysql -u root -p
   确认 application-dev.yml 中的 url、username、password 与实际一致
   确认 MySQL 允许远程连接（如需）
```

**Q: Redis 连接失败？**
```
A: 检查 Redis 是否运行：redis-cli ping
   确认 application-dev.yml 中的 host、port 与实际一致
```

**Q: 后端启动报错 "Could not resolve placeholder 'rate-limit.enabled'"？**
```
A: 检查 application-dev.yml 中 rate-limit 配置块是否存在
   确认 spring.profiles.active=dev
```

**Q: 前端开发服务器端口 5173 被占用？**
```
A: 修改 vite.config.ts 中的 server.port 配置
   或使用 npx vite --port 3000 指定端口
```


## 启动项目

### 1. 后端启动

```bash
# 编译打包
mvn clean package -DskipTests

# 开发模式启动（默认使用 dev 配置）
mvn spring-boot:run
# 或
java -jar target/smarthub-0.0.1-SNAPSHOT.jar

# 后端运行在 http://localhost:8080
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

### 数据库初始化

项目使用 SQL 脚本初始化数据库，执行顺序如下：

```bash
# 1. 创建数据库和表结构
mysql -u root -p < src/main/resources/db/V1__init_schema.sql

# 2. 插入种子数据（管理员、角色、菜单、AI 模型配置）
mysql -u root -p < src/main/resources/db/V2__init_data.sql

# 3. 创建请求日志表
mysql -u root -p < src/main/resources/db/V3__create_request_log.sql

# 4. 新增权限菜单（协议管理、AI 模型配置、请求日志等）
mysql -u root -p < src/main/resources/db/V4__add_new_permissions.sql

# 5. 数据字典
mysql -u root -p < src/main/resources/db/V5__create_dict_tables.sql

# 6. 操作日志
mysql -u root -p < src/main/resources/db/V6__create_oper_log.sql

# 7. 文件信息
mysql -u root -p < src/main/resources/db/V7__create_sys_file.sql

# 8. 系统配置
mysql -u root -p < src/main/resources/db/V8__create_sys_config.sql
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
| `ai_model_config` | AI 模型配置 |
| `sys_dict_type` | 字典类型 |
| `sys_dict_data` | 字典数据 |
| `oper_log` | 操作日志 |
| `sys_file` | 文件信息 |
| `sys_config` | 系统配置 |

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
- 记录：IP、方法、URL、耗时、模块、操作用户、是否异常、User-Agent
- 自动脱敏：请求日志中 password/secret/token 等字段会被替换为 `***`
- 异步写入数据库，不阻塞业务
- 独立日志文件 `request.log`

### 新增权限标识

| 权限标识 | 说明 | 对应接口 |
|---------|------|---------|
| `sys:protocol:list` | 查看协议状态 | GET /api/protocol/status |
| `sys:protocol:send` | 向设备发送数据 | POST /api/protocol/send/{p}/{d} |
| `sys:protocol:start` | 启动协议 | POST /api/protocol/start-all |
| `sys:protocol:stop` | 停止协议 | POST /api/protocol/stop-all |
| `sys:ai:chat` | AI 聊天 | POST /api/ai/chat/stream, 会话管理等 |
| `sys:ai:model` | AI 模型管理 | GET/POST/DELETE /api/ai/models, 模型切换 |
| `sys:log:list` | 查看请求日志 | GET /api/logs |
| `sys:dict:type:list` | 字典类型查看 | GET /api/dict/types |
| `sys:dict:type:edit` | 字典类型编辑 | POST /api/dict/types |
| `sys:dict:type:delete` | 字典类型删除 | DELETE /api/dict/types/{id} |
| `sys:dict:data:list` | 字典数据查看 | GET /api/dict/data |
| `sys:dict:data:edit` | 字典数据编辑 | POST /api/dict/data |
| `sys:dict:data:delete` | 字典数据删除 | DELETE /api/dict/data/{id} |
| `sys:operlog:list` | 操作日志查看 | GET /api/oper-log |
| `sys:operlog:delete` | 操作日志删除 | DELETE /api/oper-log |
| `sys:config:list` | 系统配置查看 | GET /api/config |
| `sys:config:edit` | 系统配置编辑 | POST /api/config |
| `sys:config:delete` | 系统配置删除 | DELETE /api/config |
| `sys:cache:list` | 缓存查看 | GET /api/cache/* |
| `sys:cache:delete` | 缓存删除 | DELETE /api/cache/* |
| `sys:file:upload` | 文件上传 | POST /api/files/upload |

### 限流模块

- **@RateLimit 注解**: 基于 Redis 滑动窗口限流
- 支持按 IP/用户名/接口等维度限流（SpEL 表达式）
- 登录接口默认限流：每分钟 10 次
- 可通过 `rate-limit.enabled` 配置全局开关
- 双重开关：application.yml 配置 + Redis 运行时开关

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
| 请求日志 | `/logs` | `GET /api/logs` | 请求日志分页查询 + 详情 |
| AI 模型配置 | `/ai-models` | `GET/POST/DELETE /api/ai/models` | AI 模型 CRUD 管理 |
| 数据字典 | `/dict` | `GET/POST/DELETE /api/dict/*` | 字典类型 + 字典数据管理 |
| 操作日志 | `/oper-log` | `GET /api/oper-log` | 操作日志分页查询 + 详情 |
| 个人中心 | `/profile` | `GET/PUT /api/profile/*` | 修改密码 + 修改资料 |
| 文件管理 | `/files` | `POST /api/files/upload` | 文件上传 + 下载预览 |
| 系统配置 | `/config` | `GET/POST/DELETE /api/config` | 运行时配置管理 |
| 缓存管理 | `/cache` | `GET/DELETE /api/cache/*` | Redis 缓存可视化管理 |

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

## 更新日志

### v0.2.0 — 2026-07-01

**安全修复**
- 角色列表改为返回 VO（`RoleVO`），防止敏感字段泄露
- 协议管理、AI 模块所有接口补充 `@PreAuthorize` 权限控制
- 所有 Controller 的 `@RequestBody` 参数补全 `@Valid` 校验
- VO/DTO 补全 `@Schema` 字段级注解，完善 Swagger 文档
- 放行 `/error` 路径，解决 SSE 连接结束后 Tomcat 内部转发导致的 503 错误

**功能增强**
- 用户管理页新增"分配角色"功能（Transfer 穿梭框）
- 登录页新增表单校验（用户名/密码非空 + 长度限制）
- 角色管理页新增名称/编码搜索
- 菜单管理页新增名称搜索 + 高亮 + 树过滤
- Dashboard 首页增强：AI 模型状态表、协议状态表、最近操作日志（分页）

**新增页面**
- 请求日志管理页（`/logs`）— 支持分页、搜索、详情弹窗
- AI 模型配置管理页（`/ai-models`）— 支持 CRUD、分页、搜索
- 404 页面 — 独立展示，不再重定向到登录页

**新增接口**
- `GET /api/logs` — 请求日志分页查询
- `GET/POST/DELETE /api/ai/models` — AI 模型配置 CRUD

### v0.3.0 — 2026-07-02

**第一批：快速见效**
- 数据字典管理 — 字典类型 + 字典数据 CRUD，含 6 种常用字典种子数据
- 操作日志/审计日志 — `@OperateLog` 注解 + AOP 切面，自动记录业务操作
- 个人中心 — 修改密码、修改资料、修改头像

**第二批：按需推进**
- 文件上传下载 — 本地磁盘存储，支持拖拽上传、预览、下载
- 系统配置管理 — 运行时配置 CRUD，支持 Redis 缓存、热更新
- 缓存管理 — Redis 可视化管理，支持 Key 查询、查看、删除、清空
