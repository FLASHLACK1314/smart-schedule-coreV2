# DifyChat API 前端文档

## 1. 概述

智能调课助手是排课系统的对话式 AI 窗口，基于 Dify 工作流编排应用实现。前端通过本 API 与后端交互，实现智能问答、排课辅助等功能。

**基路径**：`/v1/dify/chat`

**认证方式**：Bearer Token

**权限要求**：仅限 `SYSTEM_ADMIN`（系统管理员）和 `ACADEMIC_ADMIN`（教务管理员）访问

### 端点总览

| 方法 | 路径 | 说明 | 响应类型 |
|------|------|------|----------|
| POST | `/message` | 阻塞发送消息 | `ChatMessageResponse` |
| GET | `/message/stream` | SSE 流式消息 | `text/event-stream` |
| GET | `/conversations` | 获取会话列表 | `List<Conversation>` |
| GET | `/conversations/{conversationId}/messages` | 获取历史消息 | `List<Message>` |
| DELETE | `/conversations/{conversationId}` | 删除会话 | `Void` |
| PUT | `/conversations/{conversationId}/name` | 重命名会话 | `Void` |

### 学期上下文（重要）

智能调课助手的 AI 回答依赖 MCP 工具查询排课数据（如查询教师课表、排课冲突、教室占用等）。**所有排课数据均按学期组织**，因此在发送消息时应传入 `semesterUuid` 参数。

- **传入 semesterUuid**：MCP 工具返回该学期的精确数据，AI 可以给出有意义的回答
- **不传 semesterUuid**：MCP 工具查询所有学期数据，结果可能混乱或为空。服务端会记录警告日志

> 前端应在用户选择学期后，将 `semesterUuid` 与后续所有聊天请求关联。获取学期列表请参考 [第5节 学期 API 参考](#5-学期-api-参考)。

---

## 2. 认证说明

所有接口均需在请求头携带 Bearer Token：

```
Authorization: Bearer <your_token_here>
```

### 401 未授权响应示例

```json
{
  "code": 401,
  "message": "未授权访问",
  "data": null
}
```

---

## 3. TypeScript 类型定义

```typescript
// ==================== 请求体 ====================

/**
 * 聊天请求体
 */
interface DifyChatRequest {
  /** 用户消息内容（必填） */
  query: string;
  /** 会话ID（可选，首次为空，后续对话传入之前返回的会话ID） */
  conversationId?: string;
  /** 学期UUID（强烈建议传入，MCP 工具需要此参数查询排课数据，不传则查询所有学期） */
  semesterUuid?: string;
}

// ==================== 学期相关类型 ====================

/**
 * 学期信息（用于获取学期列表并选择 semesterUuid）
 */
interface SemesterInfoDTO {
  /** 学期UUID */
  semesterUuid: string;
  /** 学期名称（如 "2024-2025学年第2学期"） */
  semesterName: string;
  /** 学期周数 */
  semesterWeeks: number;
  /** 学期开始日期（ISO 格式：YYYY-MM-DD） */
  startDate: string;
  /** 学期结束日期（ISO 格式：YYYY-MM-DD） */
  endDate: string;
}

/**
 * 分页响应包装
 */
interface PageDTO<T> {
  /** 总记录数 */
  total: number;
  /** 当前页码 */
  page: number;
  /** 每页数量 */
  size: number;
  /** 数据列表 */
  records: T[];
}

// ==================== 响应体 ====================

/**
 * 统一响应包装
 */
interface BaseResponse<T> {
  code: number;
  message: string;
  data: T;
}

/**
 * 聊天消息响应（Dify 官方格式）
 */
interface ChatMessageResponse {
  /** 消息唯一标识 */
  message_id: string;
  /** 会话ID */
  conversation_id: string;
  /** 响应模式（chat/completion） */
  mode: string;
  /** AI 回答内容 */
  answer: string;
  /** 元数据 */
  metadata?: {
    usage?: {
      total_tokens: number;
    };
  };
  /** 创建时间戳（Unix 秒） */
  created_at: number;
}

/**
 * 会话对象
 */
interface Conversation {
  /** 会话ID */
  id: string;
  /** 会话名称 */
  name: string;
  /** 输入参数 */
  inputs: Record<string, any>;
  /** 会话状态（normal/completed 等） */
  status: string;
  /** 创建时间戳（Unix 秒） */
  created_at: number;
  /** 更新时间戳（Unix 秒） */
  updated_at: number;
}

/**
 * 消息对象
 */
interface Message {
  /** 消息ID */
  id: string;
  /** 会话ID */
  conversation_id: string;
  /** 用户提问内容 */
  query: string;
  /** AI 回答内容 */
  answer: string;
  /** 反馈信息 */
  feedback?: any;
  /** 创建时间戳（Unix 秒） */
  created_at: number;
}

// ==================== SSE 事件类型 ====================

/**
 * SSE 事件基础结构
 */
interface BaseSSEEvent {
  /** 请求跟踪ID */
  task_id: string;
}

/**
 * 工作流开始事件
 */
interface WorkflowStartedEvent extends BaseSSEEvent {
  /** 工作流运行ID */
  workflow_run_id: string;
  /** 详细数据 */
  data: {
    id: string;
    workflow_id: string;
  };
}

/**
 * 节点开始事件
 */
interface NodeStartedEvent extends BaseSSEEvent {
  data: {
    /** 节点ID */
    node_id: string;
    /** 节点类型 */
    node_type: string;
    /** 节点标题 */
    title: string;
    /** 节点索引 */
    index: number;
  };
}

/**
 * 节点完成事件
 */
interface NodeFinishedEvent extends BaseSSEEvent {
  data: {
    /** 节点ID */
    node_id: string;
    /** 节点类型 */
    node_type: string;
    /** 执行状态 */
    status: string;
    /** 输出结果 */
    outputs?: Record<string, any>;
    /** 错误信息 */
    error?: string;
    /** 耗时（秒） */
    elapsed_time: number;
  };
}

/**
 * 消息片段事件
 */
interface MessageEvent extends BaseSSEEvent {
  /** 消息内容片段 */
  answer: string;
  /** 消息ID */
  message_id: string;
  /** 会话ID */
  conversation_id: string;
}

/**
 * 消息完成事件
 */
interface MessageEndEvent extends BaseSSEEvent {
  /** 消息ID */
  message_id: string;
  /** 会话ID */
  conversation_id: string;
  /** 元数据 */
  metadata: {
    usage?: {
      total_tokens: number;
    };
    retriever_resources?: any[];
  };
}

/**
 * 工作流完成事件
 */
interface WorkflowFinishedEvent extends BaseSSEEvent {
  data: {
    /** 执行状态 */
    status: string;
    /** 输出结果 */
    outputs?: Record<string, any>;
    /** 耗时（秒） */
    elapsed_time: number;
    /** 总 Token 数 */
    total_tokens: number;
  };
}

/**
 * 心跳事件
 */
interface PingEvent extends BaseSSEEvent {}

/**
 * 错误事件
 */
interface ErrorEvent extends BaseSSEEvent {
  /** HTTP 状态码 */
  status: number;
  /** 错误码 */
  code: string;
  /** 错误消息 */
  message: string;
}
```

---

## 4. API 端点详细说明

### 4.1 阻塞发送消息

**适用场景**：简单对话、快速响应场景

**请求**

```http
POST /v1/dify/chat/message
Content-Type: application/json
Authorization: Bearer <token>
```

**请求体**

```json
{
  "query": "帮我查询张老师这学期的课程安排",
  "conversationId": "550e8400-e29b-41d4-a716-446655440000",
  "semesterUuid": "semester-uuid-2024-spring"
}
```

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| query | string | 是 | 用户消息内容 |
| conversationId | string | 否 | 会话ID，首次为空，后续对话传入 |
| semesterUuid | string | 否 | 学期UUID，用于 MCP 工具调用时指定学期上下文 |

> **重要说明：会话自动恢复**
>
> 当 `conversationId` 为空时，服务端不会创建新会话，而是自动查询 `sc_dify_conversation` 表中该用户最近更新的会话并继续。
> - 首次使用的用户（表中无记录）：将创建新的 Dify 会话
> - 已有历史会话的用户：将自动继续最近一次的对话
> - 如需强制新建会话：请先调用 `DELETE /conversations/{conversationId}` 删除旧会话，再发送消息
>
> **重要说明：学期关联**
>
> `semesterUuid` 虽然标记为可选，但对于涉及排课数据查询的对话（如查课表、查冲突、查教室），**必须传入**。
> 该值会作为 `inputs.semester_uuid` 传递给 Dify 工作流，MCP 工具依赖此参数过滤数据。

**响应**

```json
{
  "code": 200,
  "message": "消息发送成功",
  "data": {
    "message_id": "msg-abc123",
    "conversation_id": "550e8400-e29b-41d4-a716-446655440000",
    "mode": "chat",
    "answer": "张老师本学期共有3门课程...",
    "metadata": {
      "usage": {
        "total_tokens": 256
      }
    },
    "created_at": 1711089600
  }
}
```

---

### 4.2 SSE 流式消息

**适用场景**：涉及 MCP 工具调用等长时间操作，避免超时

**请求**

```http
GET /v1/dify/chat/message/stream?query=帮我查询张老师这学期的课程安排&conversation_id=550e8400-e29b-41d4-a716-446655440000&semester_uuid=semester-uuid-2024-spring
Authorization: Bearer <token>
Accept: text/event-stream
```

**Query 参数**

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| query | string | 是 | 用户消息内容（需 URL 编码） |
| conversation_id | string | 否 | 会话ID |
| semester_uuid | string | 否 | 学期UUID |

> **注意**：SSE 流式接口的响应**不是** `BaseResponse` 包装格式，而是直接返回 `text/event-stream` 类型的原始 SSE 事件流。前端解析时无需处理 `code`/`message`/`data` 层级。

> **重要说明：会话自动恢复**
>
> 当 `conversation_id` 为空时，服务端不会创建新会话，而是自动查询 `sc_dify_conversation` 表中该用户最近更新的会话并继续。
> - 首次使用的用户（表中无记录）：将创建新的 Dify 会话
> - 已有历史会话的用户：将自动继续最近一次的对话
> - 如需强制新建会话：请先调用 `DELETE /conversations/{conversationId}` 删除旧会话，再发送消息
>
> **重要说明：学期关联**
>
> `semester_uuid` 虽然标记为可选，但对于涉及排课数据查询的对话（如查课表、查冲突、查教室），**必须传入**。
> 该值会作为 `inputs.semester_uuid` 传递给 Dify 工作流，MCP 工具依赖此参数过滤数据。

**响应**

Content-Type: `text/event-stream`

**超时时间**：5 分钟（300,000ms）

**事件流示例**

```
event: workflow_started
data: {"task_id":"uuid-123","workflow_run_id":"run-abc","data":{"id":"wf-001","workflow_id":"w-main"}}

event: node_started
data: {"task_id":"uuid-123","data":{"node_id":"n1","node_type":"start","title":"开始","index":0}}

event: node_finished
data: {"task_id":"uuid-123","data":{"node_id":"n1","status":"succeeded","elapsed_time":0.1}}

event: message
data: {"task_id":"uuid-123","answer":"张老师","message_id":"msg-1","conversation_id":"conv-1"}

event: message
data: {"task_id":"uuid-123","answer":"本学期共有3门课程","message_id":"msg-1","conversation_id":"conv-1"}

event: message_end
data: {"task_id":"uuid-123","message_id":"msg-1","conversation_id":"conv-1","metadata":{"usage":{"total_tokens":256}}}

event: ping
data: {"task_id":"uuid-123"}
```

---

### 4.3 获取会话列表

**请求**

```http
GET /v1/dify/chat/conversations
Authorization: Bearer <token>
```

**响应**

```json
{
  "code": 200,
  "message": "获取会话列表成功",
  "data": [
    {
      "id": "550e8400-e29b-41d4-a716-446655440000",
      "name": "课程查询对话",
      "inputs": {},
      "status": "normal",
      "created_at": 1711089600,
      "updated_at": 1711093200
    },
    {
      "id": "660e8400-e29b-41d4-a716-446655440001",
      "name": "排课助手对话",
      "inputs": {},
      "status": "completed",
      "created_at": 1711003200,
      "updated_at": 1711006800
    }
  ]
}
```

**注意**：最多返回 20 条记录，按更新时间倒序排列。

---

### 4.4 获取历史消息

**请求**

```http
GET /v1/dify/chat/conversations/{conversationId}/messages
Authorization: Bearer <token>
```

**路径参数**

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| conversationId | string | 是 | 会话ID |

**响应**

```json
{
  "code": 200,
  "message": "获取会话消息成功",
  "data": [
    {
      "id": "msg-abc123",
      "conversation_id": "550e8400-e29b-41d4-a716-446655440000",
      "query": "帮我查询张老师这学期的课程安排",
      "answer": "张老师本学期共有3门课程...",
      "feedback": null,
      "created_at": 1711089600
    },
    {
      "id": "msg-def456",
      "conversation_id": "550e8400-e29b-41d4-a716-446655440000",
      "query": "帮我调整一下周二的课",
      "answer": "好的，我来帮您处理...",
      "feedback": null,
      "created_at": 1711093200
    }
  ]
}
```

**注意**：最多返回 100 条记录。

---

### 4.5 删除会话

**请求**

```http
DELETE /v1/dify/chat/conversations/{conversationId}
Authorization: Bearer <token>
```

**路径参数**

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| conversationId | string | 是 | 会话ID |

**响应**

```json
{
  "code": 200,
  "message": "删除会话成功",
  "data": null
}
```

---

### 4.6 重命名会话

**请求**

```http
PUT /v1/dify/chat/conversations/{conversationId}/name?name=新名称
Authorization: Bearer <token>
```

**路径参数**

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| conversationId | string | 是 | 会话ID |

**Query 参数**

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| name | string | 是 | 新名称 |

**响应**

```json
{
  "code": 200,
  "message": "重命名会话成功",
  "data": null
}
```

---

## 5. 学期 API 参考

智能调课助手需要 `semesterUuid` 参数来查询排课数据。前端应先通过学期 API 获取学期列表，供用户选择。

### 5.1 获取学期分页列表

**请求**

```http
GET /v1/semester/getPage?page=1&size=10
Authorization: Bearer <token>
```

**Query 参数**

| 参数 | 类型 | 必填 | 默认值 | 说明 |
|------|------|------|--------|------|
| page | int | 否 | 1 | 页码 |
| size | int | 否 | 10 | 每页数量 |
| semester_name | string | 否 | - | 学期名称（模糊搜索） |

**权限**：SYSTEM_ADMIN、ACADEMIC_ADMIN、TEACHER、STUDENT

**响应**

```json
{
  "code": 200,
  "message": "获取学期信息成功",
  "data": {
    "total": 2,
    "page": 1,
    "size": 10,
    "records": [
      {
        "semesterUuid": "a1b2c3d4e5f6a7b8c9d0e1f2a3b4c5d6",
        "semesterName": "2024-2025学年第2学期",
        "semesterWeeks": 18,
        "startDate": "2025-02-17",
        "endDate": "2025-06-20"
      }
    ]
  }
}
```

### 5.2 获取单个学期信息

**请求**

```http
GET /v1/semester/get?semester_uuid=a1b2c3d4e5f6a7b8c9d0e1f2a3b4c5d6
Authorization: Bearer <token>
```

**Query 参数**

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| semester_uuid | string | 是 | 学期UUID |

**权限**：SYSTEM_ADMIN、ACADEMIC_ADMIN、TEACHER、STUDENT

**响应**

```json
{
  "code": 200,
  "message": "获取学期信息成功",
  "data": {
    "semesterUuid": "a1b2c3d4e5f6a7b8c9d0e1f2a3b4c5d6",
    "semesterName": "2024-2025学年第2学期",
    "semesterWeeks": 18,
    "startDate": "2025-02-17",
    "endDate": "2025-06-20"
  }
}
```

### 学期选择与对话关联流程

1. 前端调用 `GET /v1/semester/getPage` 获取学期列表
2. 用户选择一个学期（或前端根据系统时间自动匹配当前学期）
3. 将选中的 `semesterUuid` 保存到组件状态或 localStorage
4. 在每次调用 `/message` 或 `/message/stream` 时传入 `semesterUuid`（阻塞接口）/ `semester_uuid`（SSE 接口）
5. 当用户切换学期时，建议同时新建会话（不传 `conversationId`），避免不同学期的上下文混淆

---

## 6. SSE 事件类型详解

### 事件类型总览

| 事件名 | 说明 | 触发时机 |
|--------|------|----------|
| `workflow_started` | 工作流开始 | Dify 开始处理请求时 |
| `node_started` | 节点开始 | 工作流中某个节点开始执行 |
| `node_finished` | 节点完成 | 工作流中某个节点执行结束 |
| `message` | 消息片段 | AI 生成回答时逐字返回 |
| `message_end` | 消息完成 | 消息生成完毕（Chatflow 模式） |
| `workflow_finished` | 工作流完成 | 整个工作流执行结束（可能替代 message_end） |

> **注意**：`workflow_finished` 事件**不包含** `conversation_id` 和 `message_id` 字段。会话信息由 `message_end` 事件提供。由于服务端使用 CAS 保护，两者中只有一个会触发完成逻辑。前端应优先从 `message_end` 获取会话ID。
| `ping` | 心跳 | 保持连接活跃 |
| `error` | 错误 | 发生错误时 |

### 事件字段详解

#### workflow_started

```json
{
  "task_id": "uuid-for-tracking",
  "workflow_run_id": "run-abc123",
  "data": {
    "id": "workflow-run-id",
    "workflow_id": "workflow-main-id"
  }
}
```

#### node_started

```json
{
  "task_id": "uuid-for-tracking",
  "data": {
    "node_id": "node-001",
    "node_type": "start|llm|tool|http-request|...",
    "title": "节点显示名称",
    "index": 0
  }
}
```

#### node_finished

```json
{
  "task_id": "uuid-for-tracking",
  "data": {
    "node_id": "node-001",
    "node_type": "llm",
    "status": "succeeded|failed",
    "outputs": {
      "text": "输出内容"
    },
    "error": null,
    "elapsed_time": 1.5
  }
}
```

#### message

```json
{
  "task_id": "uuid-for-tracking",
  "answer": "本次追加的回答文本片段",
  "message_id": "msg-id",
  "conversation_id": "conv-id"
}
```

**注意**：`answer` 字段是增量文本，前端需要拼接显示。

#### message_end

```json
{
  "task_id": "uuid-for-tracking",
  "message_id": "msg-id",
  "conversation_id": "conv-id",
  "metadata": {
    "usage": {
      "total_tokens": 256
    },
    "retriever_resources": []
  }
}
```

#### workflow_finished

```json
{
  "task_id": "uuid-for-tracking",
  "data": {
    "status": "succeeded|failed",
    "outputs": {
      "text": "最终输出"
    },
    "elapsed_time": 5.2,
    "total_tokens": 512
  }
}
```

> **注意**：此事件不包含 `conversation_id` 和 `message_id`。如需获取会话ID，请从 `message_end` 事件中读取。

#### ping

```json
{
  "task_id": "uuid-for-tracking"
}
```

#### error

```json
{
  "task_id": "uuid-for-tracking",
  "status": 500,
  "code": "internal_error",
  "message": "错误描述信息"
}
```

---

## 7. 前端集成要点

### 新建对话流程

1. 前端调用 `GET /v1/semester/getPage` 获取学期列表
2. 用户选择目标学期，获取 `semesterUuid`
3. 首次调用聊天接口时**不传** `conversationId`（或 `conversation_id`），但**传入** `semesterUuid`
4. 从响应中获取新的 `conversation_id`
5. 后续对话传入该 `conversation_id` 和相同的 `semesterUuid`

### 继续对话流程

1. 使用之前返回的 `conversation_id`
2. 传入与之前相同的 `semesterUuid`，确保上下文一致
3. 服务端会自动关联历史上下文

### 会话自动回退

当 `conversationId` 为空时，服务端会自动查询 `sc_dify_conversation` 表中该用户最近更新的会话并继续。
这是通过 `DifyConversationDAO.getLatestConversation(userUuid, userType)` 实现的。

**会话关联表结构**（`sc_dify_conversation`）：

| 字段 | 类型 | 说明 |
|------|------|------|
| conversation_uuid | varchar(32) | 主键 |
| user_uuid | varchar(32) | 用户UUID |
| user_type | varchar(32) | 用户类型（ACADEMIC_ADMIN 等） |
| dify_conversation_id | varchar(128) | Dify 返回的会话ID |
| created_at | timestamp | 创建时间 |
| updated_at | timestamp | 最后对话时间（用于排序） |

如果希望强制新建会话，建议先调用删除会话接口，再发送消息。

> **注意**：自动恢复的会话可能关联的是之前的学期。如果用户切换了学期，建议新建会话以避免跨学期上下文混淆。

### SSE 处理代码示例

#### 方式一：使用 EventSource（推荐简单场景）

```typescript
function sendMessageStream(query: string, semesterUuid: string, conversationId?: string) {
  const params = new URLSearchParams({
    query,
    semester_uuid: semesterUuid,
    ...(conversationId && { conversation_id: conversationId })
  });

  const eventSource = new EventSource(
    `/v1/dify/chat/message/stream?${params}`,
    { headers: { Authorization: `Bearer ${token}` } }
  );

  let fullAnswer = '';

  eventSource.addEventListener('message', (event) => {
    const data = JSON.parse(event.data);
    fullAnswer += data.answer;
    // 更新 UI 显示
    updateAnswerDisplay(fullAnswer);
  });

  eventSource.addEventListener('message_end', (event) => {
    const data = JSON.parse(event.data);
    console.log('会话ID:', data.conversation_id);
    console.log('Token 使用:', data.metadata?.usage?.total_tokens);
    eventSource.close();
  });

  eventSource.addEventListener('error', (event) => {
    const data = JSON.parse(event.data);
    console.error('错误:', data.message);
    eventSource.close();
  });

  eventSource.onerror = () => {
    console.error('连接错误');
    eventSource.close();
  };
}
```

**注意**：标准 `EventSource` API 不支持自定义 headers，Bearer Token 无法通过此方式传递。推荐使用方式二（fetch + ReadableStream）。

#### 方式二：使用 fetch + ReadableStream（推荐）

```typescript
async function sendMessageStream(
  token: string,
  query: string,
  semesterUuid: string,
  conversationId?: string,
  onMessage: (answer: string, fullAnswer: string) => void,
  onEnd: (conversationId: string, messageId: string) => void,
  onError: (error: string) => void
) {
  const params = new URLSearchParams({
    query,
    semester_uuid: semesterUuid,
    ...(conversationId && { conversation_id: conversationId })
  });

  const response = await fetch(`/v1/dify/chat/message/stream?${params}`, {
    headers: { Authorization: `Bearer ${token}` }
  });

  if (!response.ok) {
    throw new Error(`HTTP ${response.status}`);
  }

  const reader = response.body!.getReader();
  const decoder = new TextDecoder();
  let buffer = '';
  let fullAnswer = '';

  while (true) {
    const { done, value } = await reader.read();
    if (done) break;

    buffer += decoder.decode(value, { stream: true });
    const lines = buffer.split('\n');
    buffer = lines.pop() || '';

    let currentEvent = '';

    for (const line of lines) {
      if (line.startsWith('event:')) {
        currentEvent = line.slice(6).trim();
      } else if (line.startsWith('data:')) {
        const data = JSON.parse(line.slice(5).trim());

        switch (currentEvent) {
          case 'message':
            fullAnswer += data.answer || '';
            onMessage(data.answer || '', fullAnswer);
            break;
          case 'message_end':
            onEnd(data.conversation_id, data.message_id);
            return;
          case 'workflow_finished':
            // workflow_finished 事件不包含 conversation_id 和 message_id
            // 如果之前 message_end 已触发，此处不会执行（CAS 保护）
            onEnd('', '');
            return;
          case 'error':
            onError(data.message);
            return;
          case 'ping':
            // 心跳，忽略
            break;
          default:
            // 其他事件：workflow_started, node_started, node_finished
            console.log(`事件 [${currentEvent}]:`, data);
        }
      }
    }
  }
}
```

#### React Hook 示例

```typescript
import { useState, useCallback } from 'react';

interface UseDifyChatOptions {
  token: string;
  semesterUuid: string;
}

export function useDifyChat({ token, semesterUuid }: UseDifyChatOptions) {
  const [conversationId, setConversationId] = useState<string | null>(null);
  const [isLoading, setIsLoading] = useState(false);
  const [answer, setAnswer] = useState('');

  const sendMessage = useCallback(async (query: string) => {
    setIsLoading(true);
    setAnswer('');

    try {
      const params = new URLSearchParams({ query, semester_uuid: semesterUuid });
      if (conversationId) params.set('conversation_id', conversationId);

      const response = await fetch(`/v1/dify/chat/message/stream?${params}`, {
        headers: { Authorization: `Bearer ${token}` }
      });

      const reader = response.body!.getReader();
      const decoder = new TextDecoder();
      let buffer = '';

      while (true) {
        const { done, value } = await reader.read();
        if (done) break;

        buffer += decoder.decode(value, { stream: true });
        const lines = buffer.split('\n');
        buffer = lines.pop() || '';

        let currentEvent = '';
        for (const line of lines) {
          if (line.startsWith('event:')) {
            currentEvent = line.slice(6).trim();
          } else if (line.startsWith('data:')) {
            const data = JSON.parse(line.slice(5).trim());

            if (currentEvent === 'message') {
              setAnswer(prev => prev + (data.answer || ''));
            } else if (currentEvent === 'message_end') {
              setConversationId(data.conversation_id);
              setIsLoading(false);
            } else if (currentEvent === 'workflow_finished') {
              // workflow_finished 不包含 conversation_id，由 message_end 提供
              setIsLoading(false);
            } else if (currentEvent === 'error') {
              throw new Error(data.message);
            }
          }
        }
      }
    } catch (error) {
      console.error('发送消息失败:', error);
      setIsLoading(false);
      throw error;
    }
  }, [token, conversationId, semesterUuid]);

  const resetConversation = useCallback(() => {
    setConversationId(null);
    setAnswer('');
  }, []);

  return { sendMessage, resetConversation, conversationId, isLoading, answer };
}
```

---

## 8. 错误处理

### 统一响应格式

```typescript
interface BaseResponse<T> {
  code: number;
  message: string;
  data: T | null;
}
```

### 常见错误码

| 错误码 | 说明 | 处理建议 |
|--------|------|----------|
| 200 | 成功 | - |
| 401 | 未授权 | 检查 Token 是否有效或已过期 |
| 403 | 权限不足 | 检查用户角色是否为系统管理员或教务管理员 |
| 500 | 服务器内部错误 | 联系后端开发人员 |
| `OPERATION_FAILED` | 业务操作失败 | 查看具体错误消息 |

### SSE 错误事件处理

当 SSE 流中收到 `error` 事件时：

```json
{
  "task_id": "uuid",
  "status": 500,
  "code": "internal_error",
  "message": "AI 服务暂时不可用，请稍后重试"
}
```

前端应：
1. 关闭 SSE 连接
2. 显示错误提示
3. 允许用户重试

### MCP 工具相关错误

当 `semesterUuid` 未传入且 AI 尝试调用 MCP 工具时，可能出现以下情况：

- AI 回复"未查询到相关数据"（因为查了所有学期或无数据）
- 服务端日志记录警告："semesterUuid 为空，未传递给 Dify"

**前端处理建议**：
- 在用户首次打开聊天窗口时提示选择学期
- 如果 AI 回复提示无数据，引导用户检查是否选择了正确的学期

---

## 9. 最佳实践

### 选择阻塞还是流式

| 场景 | 推荐方式 | 原因 |
|------|----------|------|
| 简单查询 | 阻塞 (`/message`) | 响应快，实现简单 |
| MCP 工具调用 | 流式 (`/message/stream`) | 可能耗时较长，避免超时 |
| 需要实时反馈 | 流式 (`/message/stream`) | 用户可以看到逐字输出 |

### Token 管理

- 每次请求都需要有效的 Token
- Token 过期后需重新登录获取

### 会话管理

- 前端应持久化 `conversation_id`（如 localStorage）
- 提供新建会话和查看历史会话的功能
- 定期清理过期或无用的会话

### 性能优化

- 对于流式响应，使用防抖更新 UI
- 避免在 SSE 连接期间频繁发起其他请求
- 合理设置超时时间和重连策略

### 学期管理

- 在聊天界面顶部提供学期选择器，让用户明确当前操作的学期
- 默认选择当前学期（根据系统时间与学期日期范围判断）
- 切换学期时自动新建会话，避免跨学期上下文混淆
- 将用户选择的学期UUID持久化到 localStorage，下次打开时恢复

### 推荐前端交互流程

1. 用户打开聊天窗口
2. 前端调用 `GET /v1/semester/getPage` 获取学期列表
3. 用户选择（或自动匹配）当前学期，获取 `semesterUuid`
4. 用户输入消息，前端调用 `GET /v1/dify/chat/message/stream?query=...&semester_uuid=...`
5. 流式渲染 AI 回复
6. 用户继续对话，保持 `conversation_id` 和 `semester_uuid` 不变
7. 用户切换学期时，清除 `conversationId` 并使用新的 `semesterUuid`