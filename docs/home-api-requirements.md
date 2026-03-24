# 首页数据接口需求文档

## 概述

本文档定义了智能排课系统首页所需的后端接口，包括统计数据、最近活动、今日课程和系统公告。

---

## 1. 首页聚合数据接口

### 接口信息

| 项目 | 说明 |
|------|------|
| 接口路径 | `GET /api/home/dashboard` |
| 请求方式 | GET |
| 认证要求 | 需要登录 Token |

### 请求参数

无

### 响应数据

```json
{
  "output": "Success",
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
    "recent_activities": [...],
    "today_courses": [...],
    "announcements": [...]
  }
}
```

---

## 2. 统计数据详情

### 2.1 本周排课 (weekly_schedule)

| 字段 | 类型 | 说明 |
|------|------|------|
| value | integer | 本周排课总节数 |
| change_rate | float | 较上周变化百分比，正数表示增加，负数表示减少 |

**业务说明：**
- 统计当前周（周一至周日）所有已确认的排课记录总数
- change_rate = (本周数量 - 上周数量) / 上周数量 * 100

### 2.2 活跃教师 (active_teachers)

| 字段 | 类型 | 说明 |
|------|------|------|
| value | integer | 活跃教师数量 |
| change_rate | float | 较上周变化百分比 |

**业务说明：**
- 活跃教师定义：本周有排课记录或有登录操作的老师
- change_rate 基于上周活跃教师数量计算

### 2.3 学生总数 (total_students)

| 字段 | 类型 | 说明 |
|------|------|------|
| value | integer | 系统中学生总人数 |
| change_rate | float | 较上周变化百分比 |

**业务说明：**
- 统计所有状态为"在读"的学生总数
- change_rate 基于上周学生总数计算

### 2.4 教室使用率 (classroom_usage)

| 字段 | 类型 | 说明 |
|------|------|------|
| value | float | 教室平均使用率（百分比） |
| change_rate | float | 较上周变化百分比 |

**业务说明：**
- 使用率 = 已排课节次 / (可用教室数 × 每日节次 × 5个工作日) × 100
- 仅统计当前周、工作日（周一至周五）的使用情况

---

## 3. 最近活动接口

### 接口信息

| 项目 | 说明 |
|------|------|
| 接口路径 | `GET /api/home/activities` |
| 请求方式 | GET |
| 认证要求 | 需要登录 Token |

### 请求参数

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| limit | integer | 否 | 返回条数，默认10 |

### 响应数据

```json
{
  "output": "Success",
  "data": {
    "activities": [
      {
        "id": 1,
        "user_name": "张老师",
        "action_type": "AUTO_SCHEDULE",
        "action_text": "完成了智能排课",
        "created_at": "2024-12-20T14:30:00"
      },
      {
        "id": 2,
        "user_name": "李老师",
        "action_type": "UPDATE_TEACHER",
        "action_text": "更新了教师信息",
        "created_at": "2024-12-20T13:15:00"
      },
      {
        "id": 3,
        "user_name": "王老师",
        "action_type": "ADD_COURSE",
        "action_text": "添加了新课程",
        "created_at": "2024-12-20T12:00:00"
      },
      {
        "id": 4,
        "user_name": "赵老师",
        "action_type": "EXPORT_TIMETABLE",
        "action_text": "导出了课表数据",
        "created_at": "2024-12-20T11:00:00"
      }
    ]
  }
}
```

### 字段说明

| 字段 | 类型 | 说明 |
|------|------|------|
| id | integer | 活动记录ID |
| user_name | string | 操作用户姓名 |
| action_type | string | 操作类型代码 |
| action_text | string | 操作描述文本（可直接展示） |
| created_at | string | 操作时间，ISO 8601 格式 |

### 操作类型枚举 (action_type)

| 代码 | 描述文本 |
|------|----------|
| AUTO_SCHEDULE | 完成了智能排课 |
| MANUAL_SCHEDULE | 手动添加了排课 |
| UPDATE_SCHEDULE | 修改了排课信息 |
| DELETE_SCHEDULE | 删除了排课记录 |
| ADD_TEACHER | 添加了新教师 |
| UPDATE_TEACHER | 更新了教师信息 |
| ADD_COURSE | 添加了新课程 |
| UPDATE_COURSE | 更新了课程信息 |
| ADD_CLASSROOM | 添加了新教室 |
| EXPORT_TIMETABLE | 导出了课表数据 |
| IMPORT_DATA | 导入了基础数据 |
| LOGIN | 登录了系统 |

---

## 4. 今日课程接口

### 接口信息

| 项目 | 说明 |
|------|------|
| 接口路径 | `GET /api/home/today-courses` |
| 请求方式 | GET |
| 认证要求 | 需要登录 Token |

### 请求参数

无

### 响应数据

```json
{
  "output": "Success",
  "data": {
    "date": "2024-12-20",
    "week_day": 5,
    "courses": [
      {
        "id": 1,
        "course_name": "高等数学",
        "class_name": "计算机2301班",
        "classroom_name": "教学楼A101",
        "start_section": 1,
        "end_section": 2,
        "start_time": "08:00",
        "end_time": "09:40"
      },
      {
        "id": 2,
        "course_name": "线性代数",
        "class_name": "软件2302班",
        "classroom_name": "教学楼B203",
        "start_section": 3,
        "end_section": 4,
        "start_time": "10:00",
        "end_time": "11:40"
      },
      {
        "id": 3,
        "course_name": "概率论",
        "class_name": "数学2301班",
        "classroom_name": "教学楼C305",
        "start_section": 5,
        "end_section": 6,
        "start_time": "14:00",
        "end_time": "15:40"
      }
    ]
  }
}
```

### 字段说明

| 字段 | 类型 | 说明 |
|------|------|------|
| date | string | 当前日期 |
| week_day | integer | 星期几（1-7，1为周一） |
| courses | array | 今日课程列表 |
| courses[].id | integer | 排课记录ID |
| courses[].course_name | string | 课程名称 |
| courses[].class_name | string | 班级名称（多个班级用逗号分隔） |
| courses[].classroom_name | string | 教室名称 |
| courses[].start_section | integer | 开始节次 |
| courses[].end_section | integer | 结束节次 |
| courses[].start_time | string | 开始时间（HH:mm） |
| courses[].end_time | string | 结束时间（HH:mm） |

### 业务说明

**根据用户类型返回不同数据：**
- **学生**：返回该学生所在班级今日的所有课程
- **教师**：返回该教师今日需要授课的课程
- **教务管理员/系统管理员**：返回空数组或可配置返回全部今日课程

---

## 5. 系统公告接口

### 接口信息

| 项目 | 说明 |
|------|------|
| 接口路径 | `GET /api/home/announcements` |
| 请求方式 | GET |
| 认证要求 | 需要登录 Token |

### 请求参数

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| limit | integer | 否 | 返回条数，默认5 |

### 响应数据

```json
{
  "output": "Success",
  "data": {
    "announcements": [
      {
        "id": 1,
        "title": "下周期末考试排课截止",
        "content": "请各位老师于12月23日前完成期末考试时间安排。",
        "priority": "HIGH",
        "created_at": "2024-12-17T10:00:00",
        "relative_time": "3天后"
      },
      {
        "id": 2,
        "title": "系统维护通知",
        "content": "系统将于12月28日凌晨2:00-4:00进行维护升级。",
        "priority": "NORMAL",
        "created_at": "2024-12-13T09:00:00",
        "relative_time": "1周前"
      }
    ]
  }
}
```

### 字段说明

| 字段 | 类型 | 说明 |
|------|------|------|
| id | integer | 公告ID |
| title | string | 公告标题 |
| content | string | 公告内容 |
| priority | string | 优先级：HIGH/MEDIUM/LOW |
| created_at | string | 发布时间，ISO 8601 格式 |
| relative_time | string | 相对时间描述（如"3天后"、"1周前"） |

### 优先级枚举

| 代码 | 说明 | 前端展示 |
|------|------|----------|
| HIGH | 高优先级 | 红色标记或置顶 |
| MEDIUM | 中优先级 | 普通展示 |
| LOW | 低优先级 | 普通展示 |

---

## 6. TypeScript 类型定义

```typescript
// 首页聚合数据
interface DashboardResponse {
  output: 'Success'
  data: {
    user_info: {
      name: string
      user_type: UserType
    }
    stats: {
      weekly_schedule: StatItem
      active_teachers: StatItem
      total_students: StatItem
      classroom_usage: StatItem
    }
    recent_activities: Activity[]
    today_courses: TodayCourse[]
    announcements: Announcement[]
  }
}

// 统计项
interface StatItem {
  value: number
  change_rate: number
}

// 最近活动
interface Activity {
  id: number
  user_name: string
  action_type: ActionType
  action_text: string
  created_at: string
}

// 今日课程
interface TodayCourse {
  id: number
  course_name: string
  class_name: string
  classroom_name: string
  start_section: number
  end_section: number
  start_time: string
  end_time: string
}

// 系统公告
interface Announcement {
  id: number
  title: string
  content: string
  priority: 'HIGH' | 'MEDIUM' | 'LOW'
  created_at: string
  relative_time: string
}

// 操作类型
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

// 用户类型
type UserType = 'STUDENT' | 'TEACHER' | 'ACADEMIC_ADMIN' | 'SYSTEM_ADMIN'
```

---

## 7. 前端展示逻辑说明

### 统计卡片变化率展示

```
change_rate > 0  → 显示 "+12% 较上周"（绿色）
change_rate < 0  → 显示 "-2% 较上周"（红色）
change_rate = 0  → 显示 "持平 较上周"（灰色）
```

### 相对时间计算（前端可自行处理或后端返回）

| 时间差 | 展示文本 |
|--------|----------|
| < 1分钟 | 刚刚 |
| < 1小时 | X分钟前 |
| < 24小时 | X小时前 |
| < 7天 | X天前 |
| < 30天 | X周前 |
| >= 30天 | X个月前 |

---

## 8. 错误响应

```json
{
  "output": "Fail",
  "error_message": "未授权访问",
  "error_code": "UNAUTHORIZED"
}
```

### 常见错误码

| 错误码 | HTTP状态码 | 说明 |
|--------|-----------|------|
| UNAUTHORIZED | 401 | 未登录或Token过期 |
| FORBIDDEN | 403 | 无权限访问 |
| INTERNAL_ERROR | 500 | 服务器内部错误 |

---

## 版本历史

| 版本 | 日期 | 说明 |
|------|------|------|
| v1.0 | 2024-12-20 | 初始版本 |
