# 基于 MCP 协议的 AI 交互式智能修正系统

## 一、项目背景

### 1.1 核心目标
实现基于 MCP 协议的智能排课修正系统，使教务人员能够通过自然语言输入（如"张三老师周五家中有事，请把他的课调到周四下午"），AI 自动解析意图、生成操作序列、预览调整效果、人工确认后最终执行。

### 1.2 技术栈
- **后端系统**：Spring Boot 3.5.9 + PostgreSQL + Redis
- **MCP 服务器**：Python + FastMCP 框架 + httpx
- **缓存**：Redis（预览数据暂存）
- **认证**：Token + 拦截器 + 角色权限控制

### 1.3 毕设演示意义
- 填补自动化排课与人工经验之间的鸿沟
- 实现"所说即所得"的智能化管理
- 展示 AI + MCP 协议在实际业务场景中的应用

---

## 二、系统架构

### 2.1 总体架构

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

**A. 认证工具（1个）**
- `authenticate_user` - 用户登录认证，获取并缓存 Token

**B. 查询工具（6个）**
- `query_teacher_by_name` - 根据姓名查询教师信息
- `query_teacher_schedule` - 查询教师课表
- `query_classroom_availability` - 查询教室时间段可用性
- `search_classroom_by_capacity` - 根据容量搜索教室
- `query_semester_info` - 查询学期信息
- `check_schedule_conflicts` - 检查课程调整冲突（核心）

**C. 调整工具（3个）**
- `preview_schedule_adjustment` - 预览调整（status=0，Redis 暂存）
- `confirm_schedule_adjustment` - 确认执行（status=1，数据库持久化）
- `cancel_preview` - 取消预览

---

## 三、核心机制：两阶段预览

### 3.1 预览阶段（status=0）

1. 调用 `/v1/schedule/update` 接口，设置 `status=0`
2. 将调整数据暂存到 Redis：`preview:{user_uuid}:{preview_id}`
3. 返回 `preview_id` 和变更说明
4. 前端展示预览效果

### 3.2 确认阶段（status=1）

1. 从 Redis 读取预览数据
2. 再次调用 `/v1/schedule/update` 接口，设置 `status=1`
3. 删除 Redis 预观数据
4. 返回最终执行结果

### 3.3 Redis 预观数据结构

**Key 格式**：`preview:{user_uuid}:{preview_id}`

**Value（JSON）**：
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
  "expires_at": "2025-02-15T11:30:00"
}
```

---

## 四、完整交互流程示例

**场景**："张三老师周五家中有事，请把他的课调到周四下午"

```
1. authenticate_user("admin", "xxx", "ACADEMIC_ADMIN")
   ↓ 返回 token

2. query_teacher_by_name(token, "张三")
   ↓ 返回张三的 teacher_uuid

3. query_teacher_schedule(token, teacher_uuid, semester_uuid)
   ↓ 返回张三周五第3-4节有"高等数学"课

4. search_classroom_by_capacity(token, min_capacity=60)
   ↓ 返回可用教室列表

5. check_schedule_conflicts(token, schedule_uuid, new_day_of_week=4, ...)
   ↓ 返回周四下午是否有冲突

6. preview_schedule_adjustment(token, schedule_uuid, new_day_of_week=4, ...)
   ↓ 返回 preview_id 和预览数据

7. [前端展示预览效果]

8. confirm_schedule_adjustment(token, preview_id)
   ↓ 正式执行调整
```

---

## 五、项目结构

```
mcp-schedule-server/
├── main.py                          # FastMCP 服务器入口
├── config.py                        # 配置管理
├── requirements.txt                  # 依赖：fastmcp, httpx, redis
│
├── tools/                           # MCP 工具实现
│   ├── auth_tools.py                # 认证工具
│   ├── query_tools.py               # 查询工具（6个）
│   ├── adjustment_tools.py          # 调整工具（3个）
│   └── conflict_tools.py            # 冲突检测工具
│
├── services/
│   ├── http_client.py               # HTTP 客户端封装
│   ├── token_manager.py             # Token 管理
│   ├── redis_client.py             # Redis 客户端
│   └── preview_manager.py           # 预览管理器
│
└── utils/
    ├── logger.py                    # 日志
    └── exceptions.py               # 自定义异常
```

---

## 六、关键代码示例

### 6.1 配置管理（config.py）

```python
import os
from dataclasses import dataclass

@dataclass
class Config:
    """配置管理"""
    # 后端 API 配置
    API_BASE_URL: str = "http://localhost:8080"
    API_TIMEOUT: int = 30

    # Redis 配置
    REDIS_URL: str = "redis://localhost:6379/0"
    PREVIEW_TTL: int = 3600  # 预觽数据1小时过期

    # Token 配置
    TOKEN_PREFIX: str = "mcp:token:"
    TOKEN_EXPIRATION: int = 43200  # 12小时

settings = Config(
    API_BASE_URL=os.getenv("API_BASE_URL", "http://localhost:8080"),
    REDIS_URL=os.getenv("REDIS_URL", "redis://localhost:6379/0")
)
```

### 6.2 HTTP 客户端（services/http_client.py）

```python
import httpx
from typing import Dict, Any
from utils.exceptions import TokenExpiredError, APIError

class ScheduleAPIClient:
    """排课系统 HTTP 客户端"""

    def __init__(self, base_url: str):
        self.base_url = base_url
        self.client = httpx.AsyncClient(timeout=30.0)

    async def _request(self, method: str, path: str, token: str = None, **kwargs):
        """统一请求方法"""
        url = f"{self.base_url}{path}"
        headers = kwargs.pop("headers", {})

        if token:
            headers["Authorization"] = token

        response = await self.client.request(method, url, headers=headers, **kwargs)

        if response.status_code == 200:
            return response.json()
        elif response.status_code == 401:
            raise TokenExpiredError("Token已过期")
        else:
            raise APIError(f"API错误: {response.status_code}")

    async def get(self, path: str, token: str = None, **kwargs):
        return await self._request("GET", path, token, **kwargs)

    async def post(self, path: str, token: str = None, **kwargs):
        return await self._request("POST", path, token, **kwargs)

    async def put(self, path: str, token: str = None, **kwargs):
        return await self._request("PUT", path, token, **kwargs)
```

### 6.3 认证工具（tools/auth_tools.py）

```python
from fastmcp import FastMCP
from services.http_client import ScheduleAPIClient
from services.token_manager import TokenManager
import json

def register(mcp: FastMCP, api_client: ScheduleAPIClient, token_manager: TokenManager):
    @mcp.tool()
    async def authenticate_user(
        user_type: str,
        user_name: str,
        password: str
    ) -> str:
        """
        用户登录验证工具

        参数：
        - user_type: 用户类型（STUDENT/TEACHER/ACADEMIC_ADMIN/SYSTEM_ADMIN）
        - user_name: 用户名/学号/工号
        - password: 密码

        返回：登录结果和 Token
        """
        payload = {
            "userType": user_type,
            "userName": user_name,
            "password": password
        }

        try:
            result = await api_client.post("/v1/auth/login", json=payload)

            if result.get("code") == 200:
                token = result["data"]["token"]
                user_uuid = result["data"]["user_uuid"]

                # 存储 Token 到 Redis
                await token_manager.store_token(user_uuid, token, user_type)

                return f"✅ 登录成功！Token: {token[:16]}..., 用户UUID: {user_uuid}"
            else:
                return f"❌ 登录失败: {result.get('message')}"

        except Exception as e:
            return f"⚠️ 登录异常: {str(e)}"
```

### 6.4 查询工具示例（tools/query_tools.py）

```python
def register(mcp: FastMCP, api_client: ScheduleAPIClient, token_manager: TokenManager):
    @mcp.tool()
    async def query_teacher_by_name(
        token: str,
        teacher_name: str
    ) -> str:
        """
        根据教师姓名查询教师信息

        参数：
        - token: 认证 Token
        - teacher_name: 教师姓名

        返回：教师信息列表（JSON 格式）
        """
        try:
            # 验证 Token
            user_info = await token_manager.validate_token(token)
            if not user_info:
                return "❌ Token 无效或已过期"

            # 调用后端 API
            result = await api_client.get(
                "/v1/teacher/getByName",
                token=token,
                params={"teacher_name": teacher_name}
            )

            if result.get("code") == 200:
                teachers = result["data"]
                return json.dumps(teachers, ensure_ascii=False, indent=2)
            else:
                return f"❌ 查询失败: {result.get('message')}"

        except Exception as e:
            return f"⚠️ 查询异常: {str(e)}"

    @mcp.tool()
    async def query_teacher_schedule(
        token: str,
        teacher_uuid: str,
        semester_uuid: str
    ) -> str:
        """
        查询教师课表

        返回：该教师在指定学期的所有课程（JSON 格式）
        """
        try:
            result = await api_client.get(
                "/v1/schedule/timetable/teacher",
                token=token,
                params={
                    "teacher_uuid": teacher_uuid,
                    "semester_uuid": semester_uuid
                }
            )

            if result.get("code") == 200:
                schedules = result["data"]
                return json.dumps(schedules, ensure_ascii=False, indent=2)
            else:
                return f"❌ 查询失败: {result.get('message')}"

        except Exception as e:
            return f"⚠️ 查询异常: {str(e)}"
```

### 6.5 调整工具示例（tools/adjustment_tools.py）

```python
from services.preview_manager import PreviewManager
import uuid
import time

def register(mcp: FastMCP, api_client: ScheduleAPIClient,
            token_manager: TokenManager, preview_manager: PreviewManager):

    @mcp.tool()
    async def preview_schedule_adjustment(
        token: str,
        schedule_uuid: str,
        new_day_of_week: int,
        new_section_start: int,
        new_section_end: int,
        new_classroom_uuid: str,
        reason: str
    ) -> str:
        """
        预览课表调整（status=0，不立即生效）

        参数：
        - token: 认证 Token
        - schedule_uuid: 要调整的课程 UUID
        - new_day_of_week: 新的星期几（1-7）
        - new_section_start: 新的起始节次
        - new_section_end: 新的结束节次
        - new_classroom_uuid: 新的教室 UUID
        - reason: 调整理由

        返回：预览 ID 和变更说明
        """
        try:
            # 1. 查询原始课程信息
            original = await api_client.get(
                "/v1/schedule/get",
                token=token,
                params={"schedule_uuid": schedule_uuid}
            )

            # 2. 调用更新接口（status=0）
            await api_client.put(
                "/v1/schedule/update",
                token=token,
                json={
                    "schedule_uuid": schedule_uuid,
                    "day_of_week": new_day_of_week,
                    "section_start": new_section_start,
                    "section_end": new_section_end,
                    "classroom_uuid": new_classroom_uuid,
                    "status": 0  # 预览状态
                }
            )

            # 3. 存储预览数据到 Redis
            user_info = await token_manager.validate_token(token)
            preview_id = f"{uuid.uuid4().hex[:8]}_{int(time.time())}"

            await preview_manager.create_preview(
                user_uuid=user_info["user_uuid"],
                schedule_uuid=schedule_uuid,
                original_data=original["data"],
                adjusted_data={
                    "day_of_week": new_day_of_week,
                    "section_start": new_section_start,
                    "section_end": new_section_end,
                    "classroom_uuid": new_classroom_uuid
                },
                reason=reason
            )

            return f"""
✅ 预览生成成功！

预览 ID: {preview_id}

变更内容：
- 时间：周{original['data']['day_of_week']} 第{original['data']['section_start']}-{original['data']['section_end']}节
  → 周{new_day_of_week} 第{new_section_start}-{new_section_end}节
- 教室：{original['data']['classroom_name']} → 新教室

请在前端确认后调用 confirm_schedule_adjustment() 执行调整
"""

        except Exception as e:
            return f"⚠️ 预览生成失败: {str(e)}"

    @mcp.tool()
    async def confirm_schedule_adjustment(
        token: str,
        preview_id: str
    ) -> str:
        """
        确认预览的调整方案，正式执行（status=1）
        """
        try:
            user_info = await token_manager.validate_token(token)

            # 1. 从 Redis 读取预览数据
            preview_data = await preview_manager.get_preview(
                user_info["user_uuid"],
                preview_id
            )

            if not preview_data:
                return "❌ 预览不存在或已过期"

            # 2. 再次调用更新接口（status=1）
            await api_client.put(
                "/v1/schedule/update",
                token=token,
                json={
                    "schedule_uuid": preview_data["schedule_uuid"],
                    "day_of_week": preview_data["adjusted"]["day_of_week"],
                    "section_start": preview_data["adjusted"]["section_start"],
                    "section_end": preview_data["adjusted"]["section_end"],
                    "classroom_uuid": preview_data["adjusted"]["classroom_uuid"],
                    "status": 1  # 正式执行
                }
            )

            # 3. 删除 Redis 预觽数据
            await preview_manager.cancel_preview(user_info["user_uuid"], preview_id)

            return "✅ 课程调整已正式生效！"

        except Exception as e:
            return f"⚠️ 确认执行失败: {str(e)}"
```

### 6.6 主服务器入口（main.py）

```python
import asyncio
from fastmcp import FastMCP
from services.http_client import ScheduleAPIClient
from services.redis_client import RedisClient
from services.token_manager import TokenManager
from services.preview_manager import PreviewManager
from tools import auth_tools, query_tools, adjustment_tools
from config import settings

async def main():
    # 初始化客户端
    redis_client = RedisClient(settings.REDIS_URL)
    await redis_client.connect()

    api_client = ScheduleAPIClient(settings.API_BASE_URL)
    token_manager = TokenManager(redis_client)
    preview_manager = PreviewManager(redis_client)

    # 创建 FastMCP 服务器
    mcp = FastMCP("schedule-adjustment-server")

    # 注册工具
    auth_tools.register(mcp, api_client, token_manager)
    query_tools.register(mcp, api_client, token_manager)
    adjustment_tools.register(mcp, api_client, token_manager, preview_manager)

    # 启动服务器
    await mcp.run()

if __name__ == "__main__":
    asyncio.run(main())
```

---

## 七、演示场景

### 场景1：简单调课

**输入**："把张三老师周五第3-4节的课调到周四下午5-6节"

**AI 执行流程**：
1. 调用 `authenticate_user` 登录
2. 调用 `query_teacher_by_name` 查询张三老师信息
3. 调用 `query_teacher_schedule` 查询张三周五的课
4. 调用 `search_classroom_by_capacity` 搜索可用教室
5. 调用 `check_schedule_conflicts` 检测周四下午是否有冲突
6. 调用 `preview_schedule_adjustment` 生成预览
7. 前端展示预览效果
8. 用户确认后调用 `confirm_schedule_adjustment` 执行

**预期输出**：
- 显示变更对比（原课程 vs 新课程）
- 显示影响范围（影响多少学生、哪些班级）
- 显示冲突检测结果
- 确认后执行并返回成功提示

### 场景2：带冲突检测的调课

**输入**："把张三老师周五第3-4节的课调到周四下午5-6节，如果教室冲突则换个教室"

**AI 执行流程**：
1. 执行场景1的步骤1-5
2. 发现周四下午5-6节教室被占用
3. 自动搜索其他可用教室
4. 生成预览方案并说明冲突和调整原因
5. 用户确认后执行

### 场景3：批量调课

**输入**："把张三老师周五的所有课都调到周四"

**AI 执行流程**：
1. 查询张三老师周五的所有课
2. 批量检测周四是否有冲突
3. 生成批量预览方案
4. 用户确认后批量执行

---

## 八、Spring Boot 后端关键接口

| 功能 | 接口路径 | 说明 |
|-----|---------|------|
| 登录认证 | `POST /v1/auth/login` | 获取 Token |
| 查询教师 | `GET /v1/teacher/getByName` | 根据姓名查询 |
| 查询教师课表 | `GET /v1/schedule/timetable/teacher` | 需要教师 UUID 和学期 UUID |
| 查询教室课表 | `GET /v1/schedule/timetable/classroom` | 检查教室是否被占用 |
| 更新排课 | `PUT /v1/schedule/update` | 调整课程，status=0 预览，status=1 正式 |
| 分页查询排课 | `GET /v1/schedule/getPage` | 多条件筛选 |

### 关键文件位置

| 文件 | 路径 |
|-----|------|
| ScheduleController | `src/main/java/io/github/flashlack1314/smartschedulecorev2/controller/ScheduleController.java` |
| ScheduleServiceImpl | `src/main/java/io/github/flashlack1314/smartschedulecorev2/service/impl/ScheduleServiceImpl.java` |
| ScheduleDO | `src/main/java/io/github/flashlack1314/smartschedulecorev2/model/entity/ScheduleDO.java` |
| AuthInterceptor | `src/main/java/io/github/flashlack1314/smartschedulecorev2/interceptor/AuthInterceptor.java` |

---

## 九、实现步骤

### 第一步：基础设施（1天）

1. 创建 Python 项目结构
2. 实现配置管理（config.py）
3. 实现 HTTP 客户端（services/http_client.py）
4. 实现 Redis 客户端（services/redis_client.py）
5. 实现 Token 管理器（services/token_manager.py）

### 第二步：核心工具（1-2天）

1. 实现认证工具（tools/auth_tools.py）
2. 实现查询工具（tools/query_tools.py）
3. 实现调整工具（tools/adjustment_tools.py）
4. 实现预览管理器（services/preview_manager.py）

### 第三步：集成测试（半天）

1. 完成主服务器入口（main.py）
2. 编写演示脚本
3. 测试完整调课流程
4. 准备答辩演示材料

---

## 十、答辩要点

### 1. 创新点

- **两阶段预览机制**：Redis 暂存 + 数据库持久化，避免误操作
- **原子化工具设计**：每个 MCP 工具职责单一，AI 可灵活组合
- **智能冲突检测**：多维度检测（教师、教室、班级）

### 2. 技术亮点

- **MCP 协议应用**：展示 AI 与实际业务系统的集成
- **自然语言交互**：降低教务人员操作门槛
- **前后端分离**：MCP 服务器独立，易于扩展

### 3. 演示建议

1. **场景化演示**：准备 2-3 个典型调课场景
2. **实时交互**：现场输入自然语言，展示 AI 解析和执行过程
3. **对比展示**：调整前后课表对比、冲突检测结果可视化
4. **容错处理**：演示冲突检测和错误处理

---

## 十一、可能的问题和解答

**Q1: 为什么使用 MCP 协议？**
A: MCP（Model Context Protocol）是 Anthropic 推出的 AI 与外部系统交互的标准协议，它让 AI 能够通过定义好的工具安全、可控地调用后端 API，避免了直接操作数据库的风险。

**Q2: 预览机制是如何实现的？**
A: 利用 Redis 暂存预览数据（status=0），用户确认后才更新数据库（status=1），类似数据库的两阶段提交，确保操作可撤销、可回滚。

**Q3: 如何保证数据一致性？**
A: 1) 预览数据存储在独立的 Redis Key，不影响正式数据；2) 冲突检测在预览阶段完成；3) 确认后才执行数据库更新，使用事务保证原子性。

**Q4: 系统的扩展性如何？**
A: MCP 工具采用原子化设计，新增功能只需添加新的工具函数，不影响现有代码。例如后续可以添加"智能推荐"、"历史记录"等工具。

---

## 总结

本项目实现了一个基于 MCP 协议的智能排课修正系统，核心特点：

✅ **自然语言交互**：教务人员用中文输入调课需求
✅ **AI 自动规划**：AI 解析意图，自动生成工具调用序列
✅ **两阶段预览**：预览 → 确认，避免误操作
✅ **冲突检测**：自动检测教师、教室、班级时间冲突
✅ **可扩展设计**：原子化工具，易于添加新功能

**预计实现时间**：2-3 天
**核心代码量**：约 1000-1500 行 Python

适用于毕业设计演示，展示了 AI + 传统信息系统的创新结合。
