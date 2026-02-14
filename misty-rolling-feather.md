# 基于 MCP 协议的 AI 交互式智能修正系统 - 实现计划

## 一、项目背景

### 1.1 核心目标
实现基于 MCP 协议的智能排课修正系统，使教务人员能够通过自然语言输入（如"张三老师周五家中有事，请把他的课调到周四下午，并确保教室没被占用"），AI 自动解析意图、生成操作序列、预览调整效果、人工确认后最终执行。

### 1.2 技术栈
- **后端系统**：Spring Boot 3.5.9 + PostgreSQL + Redis
- **MCP 服务器**：Python + FastMCP 框架 + httpx
- **缓存**：Redis（预览数据暂存）
- **认证**：Token + 拦截器 + 角色权限控制

---

## 二、系统架构设计

### 2.1 总体架构图

```
用户（教务管理员）
    ↓ 自然语言输入
AI 交互层（Claude）
    ↓ 工具调用
Python MCP 服务器（FastMCP）
    ↓ HTTP + Token
Spring Boot 后端服务（8080端口）
    ↓ 数据持久化
PostgreSQL + Redis
```

### 2.2 MCP 工具分类

**A. 认证管理工具（1个）**
- `authenticate_user` - 用户登录认证，获取并缓存 Token

**B. 信息查询工具（6个）**
- `query_teacher_by_name` - 根据姓名查询教师信息
- `query_teacher_schedule` - 查询教师课表
- `query_classroom_availability` - 查询教室时间段可用性
- `search_classroom_by_capacity` - 根据容量搜索教室
- `query_semester_info` - 查询学期信息
- `check_schedule_conflicts` - 检查课程调整冲突（核心）

**C. 课表调整工具（3个）**
- `preview_schedule_adjustment` - 预览调整（status=0，Redis 暂存）
- `confirm_schedule_adjustment` - 确认执行（status=1，数据库持久化）
- `cancel_preview` - 取消预览

**D. 冲突检测工具（1个）**
- `intelligent_conflict_detector` - 批量检测冲突并给出解决建议

---

## 三、核心实现方案

### 3.1 两阶段预览机制

**预览阶段（status=0）**
1. 调用 `/v1/schedule/update` 接口，设置 `status=0`
2. 将调整数据暂存到 Redis：`preview:{user_uuid}:{preview_id}`
3. 返回 `preview_id` 和变更说明
4. 前端展示预览效果

**确认阶段（status=1）**
1. 从 Redis 读取预览数据
2. 再次调用 `/v1/schedule/update` 接口，设置 `status=1`
3. 删除 Redis 预观数据
4. 返回最终执行结果

### 3.2 完整交互流程示例

**场景**："张三老师周五家中有事，请把他的课调到周四下午，并确保教室没被占用"

```
1. authenticate_user("admin", "xxx", "ACADEMIC_ADMIN")
   ↓ 获取 token

2. query_teacher_by_name(token, "张三")
   ↓ 获取张三的 teacher_uuid

3. query_teacher_schedule(token, teacher_uuid, semester_uuid)
   ↓ 发现张三周五第3-4节有"高等数学"课

4. search_classroom_by_capacity(token, min_capacity=60)
   ↓ 找到可用教室列表

5. check_schedule_conflicts(token, schedule_uuid, new_day_of_week=4, ...)
   ↓ 检测周四下午是否有冲突

6. preview_schedule_adjustment(token, schedule_uuid, new_day_of_week=4, ...)
   ↓ 生成预览方案，返回 preview_id

7. [前端展示预览效果]

8. confirm_schedule_adjustment(token, preview_id)
   ↓ 正式执行调整
```

---

## 四、项目结构设计

```
mcp-schedule-server/
├── main.py                          # FastMCP 服务器入口
├── config.py                        # 配置管理
├── requirements.txt                  # 依赖
│
├── tools/                           # MCP 工具实现
│   ├── __init__.py
│   ├── auth_tools.py                # 认证工具
│   ├── query_tools.py               # 查询工具（6个）
│   ├── adjustment_tools.py          # 调整工具（3个）
│   └── conflict_tools.py            # 冲突检测工具
│
├── services/                        # 业务逻辑层
│   ├── http_client.py               # HTTP 客户端封装
│   ├── token_manager.py             # Token 管理
│   ├── redis_client.py             # Redis 客户端
│   └── preview_manager.py           # 预览管理器
│
├── models/                          # 数据模型
│   ├── requests.py                  # 请求模型
│   └── responses.py                 # 响应模型
│
└── utils/                           # 工具函数
    ├── logger.py                    # 日志
    ├── exceptions.py               # 自定义异常
    └── helpers.py                  # 辅助函数
```

---

## 五、关键实现细节

### 5.1 Redis 预观数据结构

**Key 格式**：`preview:{user_uuid}:{preview_id}`

**Value 结构（JSON）**：
```json
{
  "schedule_uuid": "原课程UUID",
  "user_uuid": "操作用户UUID",
  "original": {
    "day_of_week": 5,
    "section_start": 3,
    "section_end": 4,
    "classroom_name": "A101"
  },
  "adjusted": {
    "day_of_week": 4,
    "section_start": 5,
    "section_end": 6,
    "classroom_name": "B202"
  },
  "status": "pending",
  "reason": "张三老师周五家中有事",
  "created_at": "2025-02-15T10:30:00",
  "expires_at": "2025-02-15T11:30:00",
  "impacts": {
    "affected_students": 45,
    "affected_classes": ["计科2101", "计科2102"]
  }
}
```

### 5.2 HTTP 客户端封装要点

- **统一请求方法**：`_request(method, path, token, **kwargs)`
- **错误处理**：
  - 200：成功返回数据
  - 401：Token 过期 → `TokenExpiredError`
  - 403：权限不足 → `PermissionDeniedError`
  - 404：资源不存在 → `NotFoundError`
  - 500+：服务器错误 → `APIError`
- **连接池**：httpx.AsyncClient 复用连接
- **超时控制**：默认 30 秒

### 5.3 Token 管理方案

**存储结构**：
- Key：`mcp:token:{user_uuid}`
- Value：`{token, user_type, login_time, expires_at}`
- TTL：43200 秒（12 小时）

**自动刷新机制**：
1. 每次工具调用前验证 Token
2. 剩余有效期 < 1 小时时，尝试刷新
3. 刷新失败则提示用户重新登录

### 5.4 冲突检测逻辑

**检测维度**：
1. **教师时间冲突**：教师在新时间是否有其他课
2. **教室时间冲突**：教室在新时间是否被占用
3. **班级时间冲突**：关联的行政班学生是否有其他课

**返回格式**：
```json
{
  "success": true,
  "has_conflict": false,
  "conflicts": [],
  "conflict_details": {
    "teacher_conflict": false,
    "classroom_conflict": false,
    "class_conflict": false
  }
}
```

---

## 六、实现优先级

### Phase 1: 基础设施（优先级最高）

| 文件路径 | 功能说明 | 预计工作量 |
|---------|---------|-----------|
| `C:\mcp-schedule-server\config.py` | 配置管理（API URL、Redis URL） | 1小时 |
| `C:\mcp-schedule-server\services\http_client.py` | HTTP 客户端封装 | 3小时 |
| `C:\mcp-schedule-server\services\redis_client.py` | Redis 客户端封装 | 2小时 |
| `C:\mcp-schedule-server\services\token_manager.py` | Token 管理器 | 2小时 |

### Phase 2: 核心工具（次优先级）

| 文件路径 | 功能说明 | 预计工作量 |
|---------|---------|-----------|
| `C:\mcp-schedule-server\tools\auth_tools.py` | 认证工具（1个） | 2小时 |
| `C:\mcp-schedule-server\tools\query_tools.py` | 查询工具（6个） | 6小时 |
| `C:\mcp-schedule-server\tools\adjustment_tools.py` | 调整工具（3个） | 8小时 |
| `C:\mcp-schedule-server\tools\conflict_tools.py` | 冲突检测工具（1个） | 4小时 |

### Phase 3: 业务逻辑（第三优先级）

| 文件路径 | 功能说明 | 预计工作量 |
|---------|---------|-----------|
| `C:\mcp-schedule-server\services\preview_manager.py` | 预览管理器 | 4小时 |
| `C:\mcp-schedule-server\utils\helpers.py` | 辅助函数 | 2小时 |
| `C:\mcp-schedule-server\utils\exceptions.py` | 自定义异常 | 1小时 |

### Phase 4: 集成与测试（最后）

| 文件路径 | 功能说明 | 预计工作量 |
|---------|---------|-----------|
| `C:\mcp-schedule-server\main.py` | 主服务器入口 | 2小时 |
| `C:\mcp-schedule-server\tests\test_adjustment_flow.py` | 集成测试 | 4小时 |
| `C:\mcp-schedule-server\README.md` | 使用文档 | 2小时 |

**总工作量估算**：2-3 天

---

## 七、关键技术文件参考

### 7.1 Spring Boot 后端接口

| 功能 | 接口路径 | 文件位置 |
|-----|---------|---------|
| 登录认证 | `POST /v1/auth/login` | `src/main/java/io/github/flashlack1314/smartschedulecorev2/controller/AuthController.java` |
| 查询教师课表 | `GET /v1/schedule/timetable/teacher` | `src/main/java/io/github/flashlack1314/smartschedulecorev2/controller/ScheduleController.java` |
| 查询教室课表 | `GET /v1/schedule/timetable/classroom` | 同上 |
| 更新排课 | `PUT /v1/schedule/update` | 同上 |
| 分页查询排课 | `GET /v1/schedule/getPage` | 同上 |

### 7.2 数据模型参考

| 实体 | 文件位置 | 关键字段 |
|-----|---------|---------|
| ScheduleDO | `src/main/java/io/github/flashlack1314/smartschedulecorev2/model/entity/ScheduleDO.java` | schedule_uuid, day_of_week, section_start, section_end, weeks_json, is_locked, status |
| ScheduleInfoDTO | `src/main/java/io/github/flashlack1314/smartschedulecorev2/model/dto/base/ScheduleInfoDTO.java` | 用于前端展示的完整课表信息 |
| AddScheduleVO | `src/main/java/io/github/flashlack1314/smartschedulecorev2/model/vo/AddScheduleVO.java` | 添加/更新排课的请求参数 |

### 7.3 认证机制参考

| 组件 | 文件位置 | 功能 |
|-----|---------|------|
| AuthInterceptor | `src/main/java/io/github/flashlack1314/smartschedulecorev2/interceptor/AuthInterceptor.java` | Token 验证和权限检查 |
| TokenService | `src/main/java/io/github/flashlack1314/smartschedulecorev2/service/TokenService.java` | Token 生成、验证、刷新 |
| RequireRole | `src/main/java/io/github/flashlack1314/smartschedulecorev2/annotation/RequireRole.java` | 权限注解 |
| UserType | `src/main/java/io/github/flashlack1314/smartschedulecorev2/enums/UserType.java` | 4种用户类型枚举 |

---

## 八、测试策略

### 8.1 单元测试重点

1. **HTTP 客户端测试**：模拟各种响应状态码（200, 401, 403, 404, 500）
2. **Token 管理测试**：存储、验证、过期、刷新逻辑
3. **Redis 客户端测试**：连接、读写、过期、删除
4. **工具函数测试**：每个 MCP 工具的正常和异常流程

### 8.2 集成测试场景

**完整调课流程测试**：
```
登录 → 查询教师 → 查询课表 → 检测冲突 → 预览调整 → 确认执行 → 验证结果
```

**冲突检测测试**：
```
创建冲突场景 → 调用冲突检测工具 → 验证检测结果 → 验证解决建议
```

**权限控制测试**：
```
不同用户类型调用工具 → 验证权限控制 → 验证错误提示
```

### 8.3 测试覆盖率目标

- **工具层**：> 90% 覆盖率
- **服务层**：> 80% 覆盖率
- **关键流程**：100% 覆盖率（登录、查询、调整、确认）

---

## 九、部署方案

### 9.1 Docker 部署

**Dockerfile**：
```dockerfile
FROM python:3.11-slim
WORKDIR /app
COPY requirements.txt .
RUN pip install --no-cache-dir -r requirements.txt
COPY . .
ENV PYTHONUNBUFFERED=1
CMD ["python", "main.py"]
```

**docker-compose.yml**：
```yaml
services:
  mcp-server:
    build: .
    ports:
      - "8000:8000"
    environment:
      - REDIS_URL=redis://redis:6379/0
      - API_BASE_URL=http://smart-schedule:8080
    depends_on:
      - redis

  redis:
    image: redis:7-alpine
    ports:
      - "6379:6379"
```

### 9.2 环境变量配置

| 变量名 | 说明 | 默认值 |
|-------|------|-------|
| `REDIS_URL` | Redis 连接 URL | `redis://localhost:6379/0` |
| `API_BASE_URL` | 后端 API 地址 | `http://localhost:8080` |
| `API_TIMEOUT` | API 请求超时（秒） | `30` |
| `LOG_LEVEL` | 日志级别 | `INFO` |
| `PREVIEW_TTL` | 预观数据过期时间（秒） | `3600` |

---

## 十、安全与性能优化

### 10.1 安全措施

1. **Token 加密存储**：Redis 中存储的 Token 使用 AES 加密
2. **操作审计**：所有调整操作记录审计日志
3. **权限细化**：不同用户类型严格限制可用工具
4. **防重放攻击**：每个预览 ID 只能确认一次
5. **敏感词过滤**：reason 字段过滤 SQL 注入和 XSS

### 10.2 性能优化

1. **连接池复用**：httpx 连接池，Redis 连接池
2. **并发请求**：批量查询使用 `asyncio.gather()`
3. **缓存策略**：教师信息、教室信息缓存 5 分钟
4. **分页查询**：大数据量查询使用分页
5. **批量操作**：批量确认时使用事务

---

## 十一、验证测试方案

### 11.1 端到端测试场景

**场景1：简单调课**
```
输入："把张三老师周五第3-4节的课调到周四下午5-6节"
预期：
  1. AI 调用查询工具获取张三老师信息和课表
  2. AI 检测周四下午5-6节是否冲突
  3. AI 生成预览方案
  4. 用户确认后执行
  5. 验证课表已更新
```

**场景2：带冲突检测的调课**
```
输入："把张三老师周五第3-4节的课调到周四下午5-6节，如果冲突则调到周五晚上"
预期：
  1. AI 检测到周四下午5-6节有冲突
  2. AI 自动调整为周五晚上7-8节
  3. AI 生成预览方案并说明冲突和调整原因
  4. 用户确认后执行
```

**场景3：批量调课**
```
输入："把张三老师周五的所有课都调到周四"
预期：
  1. AI 查询张三老师周五的所有课（可能多节）
  2. AI 批量检测冲突
  3. AI 生成预览方案（包含多个调整）
  4. 用户确认后批量执行
```

### 11.2 验证检查项

- [ ] 所有工具返回结构化数据（JSON 格式）
- [ ] 预览数据正确存储到 Redis 并 1 小时过期
- [ ] 确认后预览数据从 Redis 删除
- [ ] Token 过期后自动提示重新登录
- [ ] 权限不足时返回 403 错误
- [ ] 冲突检测工具能正确识别三种冲突类型
- [ ] 日志记录所有工具调用和结果

---

## 十二、后续扩展方向

### 12.1 短期扩展（1-2周）

1. **历史记录查询**：查询调整历史、恢复误操作
2. **智能推荐**：根据教师偏好、教室使用率推荐最优调整方案
3. **批量操作**：支持批量预览、批量确认、批量取消

### 12.2 长期扩展（1-2月）

1. **自然语言生成报告**：自动生成调整说明文档
2. **多轮对话优化**：支持追问、澄清、修改需求
3. **冲突自动解决**：AI 自动搜索可用时间段并执行调整
4. **移动端支持**：适配移动端界面

---

## 总结

本计划实现了基于 MCP 协议的完整智能排课修正系统，包括：

✅ **10+ 原子化 MCP 工具**：覆盖认证、查询、调整、冲突检测
✅ **两阶段预览机制**：Redis 暂存 + 数据库持久化，安全可控
✅ **智能冲突检测**：多维度检测 + AI 建议解决方案
✅ **完善 Token 管理**：自动刷新、权限验证、过期处理
✅ **生产级架构**：异步 IO、连接池、错误处理、日志、监控

用户可通过自然语言输入，AI 自动规划工具调用序列，预览调整效果，确认后执行，实现真正的"AI 智能排课修正"。

**预计实现时间**：2-3 天
**关键路径**：基础设施 → 核心工具 → 业务逻辑 → 集成测试
