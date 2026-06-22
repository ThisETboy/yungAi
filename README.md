# xuexi - 企业级 AI 智能管理平台

## 项目简介

一个集成了 AI 能力和多协议接入的企业级后台管理系统。支持 AI 智能问答、AI 辅助代码生成、RBAC 权限管理、MQTT/TCP 协议接入。

## 技术栈

| 层级 | 技术 |
|---|---|
| 后端 | Spring Boot 3.4.1 + Java 17 + MyBatis-Plus |
| 数据库 | MySQL 8.0 |
| 认证 | Spring Security + JWT |
| AI | 策略模式 + 工厂注册 (Ollama/通义千问/Claude/DeepSeek) |
| 协议 | 策略模式 + 工厂注册 (MQTT/TCP) |
| 缓存 | Redis 7 |
| 消息队列 | RabbitMQ 3 |
| 全文检索 | Elasticsearch 8.15 |
| 前端 | Vue 3 + TypeScript + Element Plus + Pinia |
| 容器化 | Docker + docker-compose |

## 快速开始

### 1. 启动基础设施

```bash
docker compose up -d mysql redis rabbitmq
```

### 2. 导入数据库

```bash
mysql -uroot -proot < src/main/resources/db/V1__init_schema.sql
mysql -uroot -proot xuexi < src/main/resources/db/V2__init_data.sql
```

### 3. 启动后端

```bash
./mvnw spring-boot:run
```

访问 API 文档: http://localhost:8080/doc.html

### 4. 启动前端

```bash
cd frontend
npm install
npm run dev
```

访问: http://localhost:5173

### 5. 一键启动（全量）

```bash
docker compose up -d
```

## 默认账号

| 用户名 | 密码 | 角色 |
|---|---|---|
| admin | admin123 | 管理员 |
| user | user123 | 普通用户 |

## 扩展指南

### 新增 AI 模型

只需 3 步：
1. 创建实现类 ` XxxAiAdapter implements AiModelAdapter`
2. 标注 `@Component`
3. 在 `application.yml` 添加配置

### 新增协议

同样只需 3 步：
1. 创建实现类 ` XxxProtocolAdapter implements ProtocolAdapter`
2. 标注 `@Component`
3. 在 `application.yml` 添加配置

## 项目结构

```
src/main/java/com/example/xuexi/
├── common/              # 公共模块
├── config/              # 配置类
├── module/
│   ├── system/          # 系统管理 (RBAC)
│   ├── ai/              # AI 功能 (聊天/模型适配/代码生成)
│   └── protocol/        # 协议管理 (MQTT/TCP)
└── generator/           # 代码生成模板
```
