# Dify 智能调课助手接口测试文档

## 概述

本文档基于 `DifyChatController` 和 `dify-workflow-smart-schedule.yml` 编写，提供最简测试用例以验证接口连通性。

**接口基础路径**: `/v1/dify/chat`
**认证要求**: 需要 Token 认证，仅限系统管理员和教务管理员访问
**请求头**: `Authorization: Bearer {token}`

---

## 接口列表

| 接口 | 方法 | 路径 | 说明 |
|------|------|------|------|
| 发送消息 | POST | `/message` | 发送消息（阻塞模式） |
| 流式发送消息 | GET | `/message/stream` | 发送消息（SSE 流式） |
| 获取会话列表 | GET | `/conversations` | 获取用户所有会话 |
| 获取会话消息 | GET | `/conversations/{conversationId}/messages` | 获取会话历史消息 |
| 删除会话 | DELETE | `/conversations/{conversationId}` | 删除指定会话 |
| 重命名会话 | PUT | `/conversations/{conversationId}/name` | 重命名会话 |

---

## 测试用例

### 前置条件

1. 服务已启动，默认端口 `8080`
2. 已获取有效 Token（系统管理员或教务管理员）
3. 数据库已初始化（至少包含教师、学期数据）

---

### 用例 1：发送消息 - 查询教师课表

**目的**: 验证阻塞模式消息发送功能

**请求**:
```http
POST /v1/dify/chat/message HTTP/1.1
Host: localhost:8080
Authorization: Bearer {your_token}
Content-Type: application/json

{
  "query": "张老师周一有什么课？",
  "conversationId": null,
  "semesterUuid": null
}
```

**预期响应**:
```json
{
  "code": 200,
  "message": "消息发送成功",
  "data": {
    "message_id": "xxx",
    "conversation_id": "yyy",
    "mode": "chat",
    "answer": "...(AI回复内容)",
    "created_at": 1710000000
  }
}
```

---

### 用例 2：流式发送消息 - SSE 模式

**目的**: 验证 SSE 流式消息功能（推荐用于长时间操作）

**请求**:
```http
GET /v1/dify/chat/message/stream?query=张老师周一有什么课&conversationId=&semesterUuid= HTTP/1.1
Host: localhost:8080
Authorization: Bearer {your_token}
Accept: text/event-stream
```

**预期响应**: SSE 事件流
```
event: workflow_started
data: {"task_id": "xxx", "workflow_run_id": "yyy", ...}

event: message
data: {"answer": "张老师周一...", ...}

event: message_end
data: {"metadata": {"usage": {"total_tokens": 100}}}
```

---

### 用例 3：获取会话列表

**目的**: 验证会话列表查询功能

**请求**:
```http
GET /v1/dify/chat/conversations HTTP/1.1
Host: localhost:8080
Authorization: Bearer {your_token}
```

**预期响应**:
```json
{
  "code": 200,
  "message": "获取会话列表成功",
  "data": [
    {
      "id": "conv-xxx",
      "name": "智能调课助手V8-带认证",
      "status": "normal",
      "created_at": 1710000000
    }
  ]
}
```

---

### 用例 4：获取会话历史消息

**目的**: 验证历史消息查询功能

**请求**:
```http
GET /v1/dify/chat/conversations/{conversation_id}/messages HTTP/1.1
Host: localhost:8080
Authorization: Bearer {your_token}
```

**预期响应**:
```json
{
  "code": 200,
  "message": "获取会话消息成功",
  "data": [
    {
      "id": "msg-xxx",
      "conversation_id": "conv-xxx",
      "query": "张老师周一有什么课？",
      "answer": "...",
      "created_at": 1710000000
    }
  ]
}
```

---

### 用例 5：删除会话

**目的**: 验证会话删除功能

**请求**:
```http
DELETE /v1/dify/chat/conversations/{conversation_id} HTTP/1.1
Host: localhost:8080
Authorization: Bearer {your_token}
```

**预期响应**:
```json
{
  "code": 200,
  "message": "删除会话成功",
  "data": null
}
```

---

### 用例 6：重命名会话

**目的**: 验证会话重命名功能

**请求**:
```http
PUT /v1/dify/chat/conversations/{conversation_id}/name?name=调课咨询 HTTP/1.1
Host: localhost:8080
Authorization: Bearer {your_token}
```

**预期响应**:
```json
{
  "code": 200,
  "message": "重命名会话成功",
  "data": null
}
```

---

## cURL 测试命令

将 `{token}` 和 `{conversation_id}` 替换为实际值：

```bash
# 1. 发送消息（阻塞模式）
curl -X POST "http://localhost:8080/v1/dify/chat/message" \
  -H "Authorization: Bearer {token}" \
  -H "Content-Type: application/json" \
  -d '{"query": "张老师周一有什么课？"}'

# 2. 流式发送消息（SSE）
curl -N "http://localhost:8080/v1/dify/chat/message/stream?query=张老师周一有什么课" \
  -H "Authorization: Bearer {token}"

# 3. 获取会话列表
curl "http://localhost:8080/v1/dify/chat/conversations" \
  -H "Authorization: Bearer {token}"

# 4. 获取会话消息
curl "http://localhost:8080/v1/dify/chat/conversations/{conversation_id}/messages" \
  -H "Authorization: Bearer {token}"

# 5. 删除会话
curl -X DELETE "http://localhost:8080/v1/dify/chat/conversations/{conversation_id}" \
  -H "Authorization: Bearer {token}"

# 6. 重命名会话
curl -X PUT "http://localhost:8080/v1/dify/chat/conversations/{conversation_id}/name?name=调课咨询" \
  -H "Authorization: Bearer {token}"
```

---

## Dify 工作流支持的意图

根据 `dify-workflow-smart-schedule.yml`，智能调课助手支持以下意图：

| 意图 | 示例查询 | 调用的 MCP 工具 |
|------|----------|----------------|
| 查询教师课表 | "张三老师周五有什么课？" | queryTeacherScheduleByTime |
| 检查时间槽 | "周四下午第5-6节有空教室吗？" | checkTimeSlotAvailability |
| 预览调课方案 | "帮我把李四老师的课从周一调到周三" | previewScheduleChangeByInfo |
| 确认调课 | "确认调课" | confirmScheduleChange |
| 取消预览 | "取消" | cancelPreview |
| 选择排课 | "选择第一个" | previewBySelectionCode |

---

## 错误响应示例

```json
{
  "code": 401,
  "message": "未授权访问",
  "data": null
}
```

```json
{
  "code": 403,
  "message": "权限不足",
  "data": null
}
```
