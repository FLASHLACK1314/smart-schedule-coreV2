# 首页接口对接文档

## 概述

本文档描述了智能排课系统首页模块的后端接口规范，供前端开发人员对接使用。

**基础信息：**
- 基础路径：`/v1/home`
- 认证方式：Bearer Token（请求头 `Authorization`）
- 响应格式：JSON（字段命名采用 **snake_case** 下划线风格）

---

## 通用响应结构

### 成功响应

```json
{
  "output": "Success",
  "message": "操作成功描述",
  "data": { ... }
}
```

### 失败响应

```json
{
  "output": "Fail",
  "error_message": "错误描述",
  "error_code": "ERROR_CODE"
}
```

### 常见错误码

| 错误码 | HTTP 状态码 | 说明 |
|--------|------------|------|
| UNAUTHORIZED | 401 | 未登录或 Token 过期 |
| FORBIDDEN | 403 | 无权限访问该接口 |
| INTERNAL_ERROR | 500 | 服务器内部错误 |

---

## 1. 获取首页聚合数据

### 接口信息

| 项目 | 说明 |
|------|------|
| 接口路径 | `GET /v1/home/dashboard` |
| 请求方式 | GET |
| 认证要求 | 需要登录 Token |
| 权限要求 | 所有用户类型均可访问 |

### 请求头

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| Authorization | string | 是 | Bearer Token |

### 请求参数

无

### 响应数据

```json
{
  "output": "Success",
  "message": "获取首页数据成功",
  "data": {
    "user_info": {
      "name": "张老师",
      "user_type": "TEACHER"
    },
    "stats": {
      "weekly_schedule": {
        "value": 128,
        "change_rate": 12.0
      },
      "active_teachers": {
        "value": 86,
        "change_rate": 5.0
      },
      "total_students": {
        "value": 2340,
        "change_rate": 8.0
      },
      "classroom_usage": {
        "value": 76.0,
        "change_rate": -2.0
      }
    },
    "recent_activities": [
      {
        "id": "abc123def456",
        "user_name": "李教务",
        "action_type": "AUTO_SCHEDULE",
        "action_text": "完成了智能排课",
        "created_at": "2025-03-25T14:30:00"
      }
    ],
    "today_courses": [
      {
        "id": "schedule001",
        "course_name": "高等数学",
        "class_name": "计算机2301班",
        "classroom_name": "教学楼A101",
        "start_section": 1,
        "end_section": 2,
        "start_time": "08:00",
        "end_time": "09:40"
      }
    ],
    "announcements": [
      {
        "id": "ann001",
        "title": "系统升级通知",
        "content": "系统将于本周六进行维护升级...",
        "priority": "HIGH",
        "created_at": "2025-03-24T10:00:00",
        "relative_time": "1天前"
      }
    ]
  }
}
```

### 字段说明

#### user_info - 用户信息

| 字段 | 类型 | 说明 |
|------|------|------|
| name | string | 用户名称 |
| user_type | string | 用户类型，见[用户类型枚举](#用户类型枚举) |

#### stats - 统计数据

| 字段 | 类型 | 说明 |
|------|------|------|
| weekly_schedule | StatItem | 本周排课统计 |
| active_teachers | StatItem | 活跃教师统计 |
| total_students | StatItem | 学生总数统计 |
| classroom_usage | StatItem | 教室使用率统计 |

#### StatItem - 统计项

| 字段 | 类型 | 说明 |
|------|------|------|
| value | number | 当前值 |
| change_rate | number | 较上周变化百分比，正数表示增加，负数表示减少 |

**业务说明：**
- `weekly_schedule`：统计当前周所有已确认的排课记录总数
- `active_teachers`：本周有排课记录或有登录操作的教师数量
- `total_students`：系统中学生总人数
- `classroom_usage`：教室平均使用率（百分比），计算公式：已排课节次 / (可用教室数 × 每日节次 × 5个工作日) × 100

#### recent_activities - 最近活动

| 字段 | 类型 | 说明 |
|------|------|------|
| id | string | 活动记录 UUID |
| user_name | string | 操作用户姓名 |
| action_type | string | 操作类型代码，见[操作类型枚举](#操作类型枚举) |
| action_text | string | 操作描述文本（可直接展示） |
| created_at | string | 操作时间，ISO 8601 格式 |

> **注意**：学生用户的 `recent_activities` 固定为空数组

#### today_courses - 今日课程

| 字段 | 类型 | 说明 |
|------|------|------|
| id | string | 排课记录 UUID |
| course_name | string | 课程名称 |
| class_name | string | 班级名称（多个班级用逗号分隔） |
| classroom_name | string | 教室名称 |
| start_section | integer | 开始节次（1-12） |
| end_section | integer | 结束节次（1-12） |
| start_time | string | 开始时间（HH:mm） |
| end_time | string | 结束时间（HH:mm） |

**根据用户类型返回不同数据：**
- **学生**：返回该学生所在班级今日的所有课程
- **教师**：返回该教师今日需要授课的课程
- **教务管理员/系统管理员**：返回空数组

#### announcements - 系统公告

| 字段 | 类型 | 说明 |
|------|------|------|
| id | string | 公告 UUID |
| title | string | 公告标题 |
| content | string | 公告内容 |
| priority | string | 优先级：HIGH/MEDIUM/LOW |
| created_at | string | 发布时间，ISO 8601 格式 |
| relative_time | string | 相对时间描述（如"3天前"、"1周前"） |

> **注意**：公告会根据用户类型（user_type）自动过滤

---

## 2. 获取最近活动列表

### 接口信息

| 项目 | 说明 |
|------|------|
| 接口路径 | `GET /v1/home/activities` |
| 请求方式 | GET |
| 认证要求 | 需要登录 Token |
| 权限要求 | **学生不可访问**，仅教师、教务管理员、系统管理员可访问 |

### 请求头

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| Authorization | string | 是 | Bearer Token |

### 请求参数

| 参数 | 类型 | 必填 | 默认值 | 说明 |
|------|------|------|--------|------|
| limit | integer | 否 | 10 | 返回条数 |

### 响应数据

```json
{
  "output": "Success",
  "message": "获取最近活动成功",
  "data": [
    {
      "id": "abc123def456",
      "user_name": "张老师",
      "action_type": "AUTO_SCHEDULE",
      "action_text": "完成了智能排课",
      "created_at": "2025-03-25T14:30:00"
    },
    {
      "id": "def456ghi789",
      "user_name": "李老师",
      "action_type": "UPDATE_TEACHER",
      "action_text": "更新了教师信息",
      "created_at": "2025-03-25T13:15:00"
    }
  ]
}
```

---

## 3. 获取今日课程

### 接口信息

| 项目 | 说明 |
|------|------|
| 接口路径 | `GET /v1/home/today-courses` |
| 请求方式 | GET |
| 认证要求 | 需要登录 Token |
| 权限要求 | 所有用户类型均可访问 |

### 请求头

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| Authorization | string | 是 | Bearer Token |

### 请求参数

无

### 响应数据

```json
{
  "output": "Success",
  "message": "获取今日课程成功",
  "data": {
    "date": "2025-03-25",
    "week_day": 2,
    "courses": [
      {
        "id": "schedule001",
        "course_name": "高等数学",
        "class_name": "计算机2301班,软件2301班",
        "classroom_name": "教学楼A101",
        "start_section": 1,
        "end_section": 2,
        "start_time": "08:00",
        "end_time": "09:40"
      },
      {
        "id": "schedule002",
        "course_name": "线性代数",
        "class_name": "计算机2302班",
        "classroom_name": "教学楼B203",
        "start_section": 3,
        "end_section": 4,
        "start_time": "10:00",
        "end_time": "11:40"
      }
    ]
  }
}
```

### 字段说明

| 字段 | 类型 | 说明 |
|------|------|------|
| date | string | 当前日期（yyyy-MM-dd） |
| week_day | integer | 星期几（1-7，1为周一，7为周日） |
| courses | array | 今日课程列表 |

---

## 4. 获取系统公告

### 接口信息

| 项目 | 说明 |
|------|------|
| 接口路径 | `GET /v1/home/announcements` |
| 请求方式 | GET |
| 认证要求 | 需要登录 Token |
| 权限要求 | 所有用户类型均可访问 |

### 请求头

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| Authorization | string | 是 | Bearer Token |

### 请求参数

| 参数 | 类型 | 必填 | 默认值 | 说明 |
|------|------|------|--------|------|
| limit | integer | 否 | 5 | 返回条数 |

### 响应数据

```json
{
  "output": "Success",
  "message": "获取系统公告成功",
  "data": {
    "announcements": [
      {
        "id": "ann001",
        "title": "下周期末考试排课截止",
        "content": "请各位老师于3月30日前完成期末考试时间安排，逾期系统将自动关闭排课功能。",
        "priority": "HIGH",
        "created_at": "2025-03-22T10:00:00",
        "relative_time": "3天前"
      },
      {
        "id": "ann002",
        "title": "系统维护通知",
        "content": "系统将于3月28日凌晨2:00-4:00进行维护升级，届时系统将暂停服务。",
        "priority": "MEDIUM",
        "created_at": "2025-03-18T09:00:00",
        "relative_time": "1周前"
      }
    ]
  }
}
```

---

## 5. 发布公告

### 接口信息

| 项目 | 说明 |
|------|------|
| 接口路径 | `POST /v1/home/announcements` |
| 请求方式 | POST |
| 认证要求 | 需要登录 Token |
| 权限要求 | **仅限系统管理员和教务管理员** |

### 请求头

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| Authorization | string | 是 | Bearer Token |
| Content-Type | string | 是 | application/json |

### 请求体

```json
{
  "title": "公告标题",
  "content": "公告内容",
  "priority": "HIGH",
  "user_type": "STUDENT"
}
```

### 请求参数说明

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| title | string | 是 | 公告标题，建议不超过100字符 |
| content | string | 是 | 公告内容 |
| priority | string | 否 | 优先级：HIGH/MEDIUM/LOW，默认为 MEDIUM |
| user_type | string | 是 | 目标用户类型：STUDENT/TEACHER/ACADEMIC_ADMIN/SYSTEM_ADMIN |

### 响应数据

```json
{
  "output": "Success",
  "message": "发布公告成功",
  "data": {
    "id": "ann003",
    "title": "公告标题",
    "content": "公告内容",
    "priority": "HIGH",
    "created_at": "2025-03-25T16:30:00",
    "relative_time": "刚刚"
  }
}
```

### 字段说明

| 字段 | 类型 | 说明 |
|------|------|------|
| id | string | 新创建的公告 UUID |
| title | string | 公告标题 |
| content | string | 公告内容 |
| priority | string | 优先级 |
| created_at | string | 发布时间，ISO 8601 格式 |
| relative_time | string | 相对时间描述（新发布时为"刚刚"） |

### 错误响应

```json
{
  "output": "Fail",
  "error_message": "无效的用户类型: XXX",
  "error_code": "BAD_REQUEST"
}
```

### 使用示例

**cURL:**

```bash
curl -X POST 'https://api.example.com/v1/home/announcements' \
  -H 'Authorization: Bearer your_token_here' \
  -H 'Content-Type: application/json' \
  -d '{
    "title": "期末考试安排通知",
    "content": "本学期期末考试将于6月15日开始，请各位同学提前做好复习准备。",
    "priority": "HIGH",
    "user_type": "STUDENT"
  }'
```

**JavaScript (axios):**

```javascript
const response = await axios.post('/v1/home/announcements', {
  title: '期末考试安排通知',
  content: '本学期期末考试将于6月15日开始，请各位同学提前做好复习准备。',
  priority: 'HIGH',
  user_type: 'STUDENT'
}, {
  headers: { Authorization: `Bearer ${token}` }
})
```

---

## 枚举定义

### 用户类型枚举

| 值 | 说明 |
|------|------|
| STUDENT | 学生 |
| TEACHER | 教师 |
| ACADEMIC_ADMIN | 教务管理员 |
| SYSTEM_ADMIN | 系统管理员 |

### 操作类型枚举

| 值 | 描述文本 | 说明 |
|------|----------|------|
| AUTO_SCHEDULE | 完成了智能排课 | 使用遗传算法自动排课 |
| MANUAL_SCHEDULE | 手动添加了排课 | 手动添加排课记录 |
| UPDATE_SCHEDULE | 修改了排课信息 | 修改已有排课记录 |
| DELETE_SCHEDULE | 删除了排课记录 | 删除排课记录 |
| ADD_TEACHER | 添加了新教师 | 新增教师信息 |
| UPDATE_TEACHER | 更新了教师信息 | 修改教师信息 |
| ADD_COURSE | 添加了新课程 | 新增课程信息 |
| UPDATE_COURSE | 更新了课程信息 | 修改课程信息 |
| ADD_CLASSROOM | 添加了新教室 | 新增教室信息 |
| EXPORT_TIMETABLE | 导出了课表数据 | 导出课表 |
| IMPORT_DATA | 导入了基础数据 | 批量导入数据 |
| LOGIN | 登录了系统 | 用户登录 |

### 公告优先级枚举

| 值 | 说明 | 建议展示方式 |
|------|------|-------------|
| HIGH | 高优先级 | 红色标记或置顶显示 |
| MEDIUM | 中优先级 | 普通展示 |
| LOW | 低优先级 | 普通展示或折叠 |

---

## TypeScript 类型定义

```typescript
// ========== 基础类型 ==========

/** 用户类型 */
type UserType = 'STUDENT' | 'TEACHER' | 'ACADEMIC_ADMIN' | 'SYSTEM_ADMIN'

/** 公告优先级 */
type Priority = 'HIGH' | 'MEDIUM' | 'LOW'

/** 操作类型 */
type ActionType =
  | 'AUTO_SCHEDULE'
  | 'MANUAL_SCHEDULE'
  | 'UPDATE_SCHEDULE'
  | 'DELETE_SCHEDULE'
  | 'ADD_TEACHER'
  | 'UPDATE_TEACHER'
  | 'ADD_COURSE'
  | 'UPDATE_COURSE'
  | 'ADD_CLASSROOM'
  | 'EXPORT_TIMETABLE'
  | 'IMPORT_DATA'
  | 'LOGIN'

// ========== DTO 类型 ==========

/** 统计项 */
interface StatItem {
  /** 当前值 */
  value: number
  /** 较上周变化百分比 */
  change_rate: number
}

/** 统计数据 */
interface Stats {
  /** 本周排课 */
  weekly_schedule: StatItem
  /** 活跃教师 */
  active_teachers: StatItem
  /** 学生总数 */
  total_students: StatItem
  /** 教室使用率 */
  classroom_usage: StatItem
}

/** 用户信息 */
interface UserInfo {
  /** 用户名称 */
  name: string
  /** 用户类型 */
  user_type: UserType
}

/** 活动记录 */
interface Activity {
  /** 活动记录 UUID */
  id: string
  /** 操作用户姓名 */
  user_name: string
  /** 操作类型代码 */
  action_type: ActionType
  /** 操作描述文本 */
  action_text: string
  /** 操作时间 */
  created_at: string
}

/** 今日课程 */
interface TodayCourse {
  /** 排课记录 UUID */
  id: string
  /** 课程名称 */
  course_name: string
  /** 班级名称（多个班级用逗号分隔） */
  class_name: string
  /** 教室名称 */
  classroom_name: string
  /** 开始节次 */
  start_section: number
  /** 结束节次 */
  end_section: number
  /** 开始时间 */
  start_time: string
  /** 结束时间 */
  end_time: string
}

/** 今日课程响应 */
interface TodayCourseResponse {
  /** 当前日期 */
  date: string
  /** 星期几（1-7） */
  week_day: number
  /** 课程列表 */
  courses: TodayCourse[]
}

/** 系统公告 */
interface Announcement {
  /** 公告 UUID */
  id: string
  /** 公告标题 */
  title: string
  /** 公告内容 */
  content: string
  /** 优先级 */
  priority: Priority
  /** 发布时间 */
  created_at: string
  /** 相对时间描述 */
  relative_time: string
}

/** 系统公告响应 */
interface AnnouncementResponse {
  /** 公告列表 */
  announcements: Announcement[]
}

/** 创建公告请求 */
interface CreateAnnouncementRequest {
  /** 公告标题 */
  title: string
  /** 公告内容 */
  content: string
  /** 优先级（可选，默认 MEDIUM） */
  priority?: Priority
  /** 目标用户类型 */
  user_type: UserType
}

/** 首页聚合数据 */
interface Dashboard {
  /** 用户信息 */
  user_info: UserInfo
  /** 统计数据 */
  stats: Stats
  /** 最近活动（学生用户为空） */
  recent_activities: Activity[]
  /** 今日课程 */
  today_courses: TodayCourse[]
  /** 系统公告 */
  announcements: Announcement[]
}

// ========== API 响应类型 ==========

/** 通用响应 */
interface BaseResponse<T> {
  /** 结果状态 */
  output: 'Success' | 'Fail'
  /** 消息 */
  message?: string
  /** 数据 */
  data?: T
  /** 错误信息 */
  error_message?: string
  /** 错误码 */
  error_code?: string
}

/** 首页聚合数据响应 */
type DashboardResponse = BaseResponse<Dashboard>

/** 活动列表响应 */
type ActivitiesResponse = BaseResponse<Activity[]>

/** 今日课程响应 */
type TodayCoursesResponse = BaseResponse<TodayCourseResponse>

/** 系统公告响应 */
type AnnouncementsResponse = BaseResponse<AnnouncementResponse>

/** 创建公告响应 */
type CreateAnnouncementResponse = BaseResponse<Announcement>
```

---

## 前端展示建议

### 统计卡片变化率

```
change_rate > 0  → 显示 "+12% 较上周"（绿色/上升箭头）
change_rate < 0  → 显示 "-2% 较上周"（红色/下降箭头）
change_rate = 0  → 显示 "持平 较上周"（灰色/横线）
```

### 相对时间展示

`relative_time` 字段已由后端计算返回，前端可直接使用。如需自行处理：

| 时间差 | 建议展示 |
|--------|---------|
| < 1分钟 | 刚刚 |
| < 1小时 | X分钟前 |
| < 24小时 | X小时前 |
| < 7天 | X天前 |
| < 30天 | X周前 |
| >= 30天 | X个月前 |

### 课程节次时间对照

| 节次 | 开始时间 | 结束时间 |
|------|---------|---------|
| 1-2 | 08:00 | 09:40 |
| 3-4 | 10:00 | 11:40 |
| 5-6 | 14:00 | 15:40 |
| 7-8 | 16:00 | 17:40 |
| 9-10 | 19:00 | 20:40 |
| 11-12 | 21:00 | 22:40 |

> 实际节次时间以后端返回的 `start_time` 和 `end_time` 为准

---

## 接口权限汇总

| 接口 | 学生 | 教师 | 教务管理员 | 系统管理员 |
|------|------|------|-----------|-----------|
| GET /v1/home/dashboard | ✅ | ✅ | ✅ | ✅ |
| GET /v1/home/activities | ❌ | ✅ | ✅ | ✅ |
| GET /v1/home/today-courses | ✅ | ✅ | ✅ | ✅ |
| GET /v1/home/announcements | ✅ | ✅ | ✅ | ✅ |
| POST /v1/home/announcements | ❌ | ❌ | ✅ | ✅ |

---

## 版本历史

| 版本 | 日期 | 说明 |
|------|------|------|
| v1.1 | 2025-03-31 | 新增发布公告接口 |
| v1.0 | 2025-03-25 | 初始版本 |
