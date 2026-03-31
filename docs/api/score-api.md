# 成绩管理接口文档

> 基础路径: `/v1/score`
> 认证方式: Bearer Token（请求头 `Authorization: Bearer <token>`）

---

## 目录

1. [添加成绩](#1-添加成绩)
2. [批量添加成绩](#2-批量添加成绩)
3. [更新成绩](#3-更新成绩)
4. [删除成绩](#4-删除成绩)
5. [获取单个成绩](#5-获取单个成绩)
6. [分页查询成绩](#6-分页查询成绩)
7. [查询学生成绩列表](#7-查询学生成绩列表)
8. [查询教学班成绩列表](#8-查询教学班成绩列表)
9. [获取成绩统计信息](#9-获取成绩统计信息)
10. [计算学生绩点](#10-计算学生绩点)
11. [数据结构说明](#11-数据结构说明)

---

## 1. 添加成绩

### 请求

```
POST /v1/score/add
```

### 权限

- 系统管理员 (SYSTEM_ADMIN)
- 教务管理员 (ACADEMIC_ADMIN)
- 教师 (TEACHER)

### 请求体

```json
{
  "student_uuid": "学生UUID",
  "teaching_class_uuid": "教学班UUID",
  "semester_uuid": "学期UUID",
  "usual_score": 85.5,
  "midterm_score": 78.0,
  "final_score": 82.5,
  "remark": "备注信息（可选）"
}
```

### 响应

```json
{
  "output": "Success",
  "message": "添加成绩成功",
  "data": "成绩UUID"
}
```

---

## 2. 批量添加成绩

### 请求

```
POST /v1/score/batchAdd
```

### 权限

- 系统管理员 (SYSTEM_ADMIN)
- 教务管理员 (ACADEMIC_ADMIN)
- 教师 (TEACHER)

### 请求体

```json
{
  "teaching_class_uuid": "教学班UUID",
  "semester_uuid": "学期UUID",
  "score_items": [
    {
      "student_uuid": "学生1的UUID",
      "usual_score": 85.5,
      "midterm_score": 78.0,
      "final_score": 82.5,
      "remark": "备注"
    },
    {
      "student_uuid": "学生2的UUID",
      "usual_score": 90.0,
      "midterm_score": 88.0,
      "final_score": 92.0,
      "remark": ""
    }
  ]
}
```

### 响应

```json
{
  "output": "Success",
  "message": "批量添加成绩成功",
  "data": 2
}
```

---

## 3. 更新成绩

### 请求

```
PUT /v1/score/update
```

### 权限

- 系统管理员 (SYSTEM_ADMIN)
- 教务管理员 (ACADEMIC_ADMIN)
- 教师 (TEACHER)

### 请求体

```json
{
  "score_uuid": "成绩UUID",
  "usual_score": 88.0,
  "midterm_score": 80.0,
  "final_score": 85.0,
  "remark": "修改备注"
}
```

### 响应

```json
{
  "output": "Success",
  "message": "更新成绩成功",
  "data": null
}
```

---

## 4. 删除成绩

### 请求

```
DELETE /v1/score/delete
```

### 权限

- 系统管理员 (SYSTEM_ADMIN)
- 教务管理员 (ACADEMIC_ADMIN)

### 请求参数

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| score_uuid | String | 是 | 成绩UUID |

### 请求示例

```
DELETE /v1/score/delete?score_uuid=abc123
```

### 响应

```json
{
  "output": "Success",
  "message": "删除成绩成功",
  "data": null
}
```

---

## 5. 获取单个成绩

### 请求

```
GET /v1/score/get
```

### 权限

- 系统管理员 (SYSTEM_ADMIN)
- 教务管理员 (ACADEMIC_ADMIN)
- 教师 (TEACHER)
- 学生 (STUDENT) - 只能查看自己的成绩

### 请求参数

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| score_uuid | String | 是 | 成绩UUID |

### 响应

```json
{
  "output": "Success",
  "message": "获取成绩信息成功",
  "data": {
    "score_uuid": "成绩UUID",
    "student_info": {
      "student_uuid": "学生UUID",
      "student_id": "学号",
      "student_name": "学生姓名",
      "class_info": {
        "class_uuid": "班级UUID",
        "class_name": "班级名称",
        "major_info": {
          "major_uuid": "专业UUID",
          "major_num": "专业编号",
          "major_name": "专业名称",
          "department_uuid": "学院UUID",
          "department_name": "学院名称"
        }
      }
    },
    "teaching_class_info": {
      "teaching_class_uuid": "教学班UUID",
      "course_name": "课程名称",
      "teacher_name": "教师姓名",
      "semester_name": "学期名称",
      "teaching_class_name": "教学班名称"
    },
    "semester_info": {
      "semester_uuid": "学期UUID",
      "semester_name": "学期名称",
      "semester_weeks": 18,
      "start_date": "2025-09-01",
      "end_date": "2026-01-15"
    },
    "usual_score": 85.5,
    "midterm_score": 78.0,
    "final_score": 82.5,
    "total_score": 82.35,
    "grade_point": 3.3,
    "remark": "备注",
    "create_time": "2025-03-29T10:00:00",
    "update_time": "2025-03-29T10:00:00"
  }
}
```

---

## 6. 分页查询成绩

### 请求

```
GET /v1/score/getPage
```

### 权限

- 系统管理员 (SYSTEM_ADMIN)
- 教务管理员 (ACADEMIC_ADMIN)
- 教师 (TEACHER)

### 请求参数

| 参数名 | 类型 | 必填 | 默认值 | 说明 |
|--------|------|------|--------|------|
| page | int | 否 | 1 | 页码 |
| size | int | 否 | 10 | 每页数量 |
| student_uuid | String | 否 | - | 学生UUID（筛选） |
| teaching_class_uuid | String | 否 | - | 教学班UUID（筛选） |
| semester_uuid | String | 否 | - | 学期UUID（筛选） |

### 请求示例

```
GET /v1/score/getPage?page=1&size=10&semester_uuid=xxx
```

### 响应

```json
{
  "output": "Success",
  "message": "获取成绩分页信息成功",
  "data": {
    "total": 100,
    "page": 1,
    "size": 10,
    "records": [
      {
        "score_uuid": "成绩UUID",
        "student_info": { "..." },
        "teaching_class_info": { "..." },
        "semester_info": { "..." },
        "usual_score": 85.5,
        "midterm_score": 78.0,
        "final_score": 82.5,
        "total_score": 82.35,
        "grade_point": 3.3,
        "remark": "",
        "create_time": "2025-03-29T10:00:00",
        "update_time": "2025-03-29T10:00:00"
      }
    ]
  }
}
```

---

## 7. 查询学生成绩列表

### 请求

```
GET /v1/score/student/list
```

### 权限

- 系统管理员 (SYSTEM_ADMIN)
- 教务管理员 (ACADEMIC_ADMIN)
- 教师 (TEACHER)
- 学生 (STUDENT) - 只能查看自己的成绩

### 请求参数

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| student_uuid | String | 是 | 学生UUID |
| semester_uuid | String | 否 | 学期UUID（不传则查询所有学期） |

### 请求示例

```
GET /v1/score/student/list?student_uuid=xxx&semester_uuid=xxx
```

### 响应

```json
{
  "output": "Success",
  "message": "获取学生成绩列表成功",
  "data": [
    {
      "score_uuid": "成绩UUID",
      "student_info": { "..." },
      "teaching_class_info": {
        "teaching_class_uuid": "教学班UUID",
        "course_name": "数据结构",
        "teacher_name": "张教授",
        "semester_name": "2025-2026学年第一学期"
      },
      "semester_info": { "..." },
      "usual_score": 85.5,
      "midterm_score": 78.0,
      "final_score": 82.5,
      "total_score": 82.35,
      "grade_point": 3.3,
      "remark": ""
    }
  ]
}
```

---

## 8. 查询教学班成绩列表

### 请求

```
GET /v1/score/teachingClass/list
```

### 权限

- 系统管理员 (SYSTEM_ADMIN)
- 教务管理员 (ACADEMIC_ADMIN)
- 教师 (TEACHER)

### 请求参数

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| teaching_class_uuid | String | 是 | 教学班UUID |

### 请求示例

```
GET /v1/score/teachingClass/list?teaching_class_uuid=xxx
```

### 响应

```json
{
  "output": "Success",
  "message": "获取教学班成绩列表成功",
  "data": [
    {
      "score_uuid": "成绩UUID",
      "student_info": {
        "student_uuid": "学生UUID",
        "student_id": "20210001",
        "student_name": "张三"
      },
      "teaching_class_info": { "..." },
      "semester_info": { "..." },
      "usual_score": 85.5,
      "midterm_score": 78.0,
      "final_score": 82.5,
      "total_score": 82.35,
      "grade_point": 3.3
    }
  ]
}
```

---

## 9. 获取成绩统计信息

### 请求

```
GET /v1/score/statistics
```

### 权限

- 系统管理员 (SYSTEM_ADMIN)
- 教务管理员 (ACADEMIC_ADMIN)
- 教师 (TEACHER)

### 请求参数

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| teaching_class_uuid | String | 是 | 教学班UUID |

### 请求示例

```
GET /v1/score/statistics?teaching_class_uuid=xxx
```

### 响应

```json
{
  "output": "Success",
  "message": "获取成绩统计信息成功",
  "data": {
    "teaching_class_uuid": "教学班UUID",
    "course_name": "数据结构",
    "teacher_name": "张教授",
    "total_students": 45,
    "entered_count": 42,
    "average_score": 78.56,
    "max_score": 98.5,
    "min_score": 45.0,
    "pass_count": 38,
    "pass_rate": 90.48,
    "excellent_count": 12,
    "excellent_rate": 28.57,
    "average_grade_point": 2.85
  }
}
```

---

## 10. 计算学生绩点

### 请求

```
GET /v1/score/gpa
```

### 权限

- 系统管理员 (SYSTEM_ADMIN)
- 教务管理员 (ACADEMIC_ADMIN)
- 教师 (TEACHER)
- 学生 (STUDENT) - 只能查看自己的绩点

### 请求参数

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| student_uuid | String | 是 | 学生UUID |
| semester_uuid | String | 否 | 学期UUID（不传则计算所有学期） |

### 请求示例

```
GET /v1/score/gpa?student_uuid=xxx
```

### 响应

```json
{
  "output": "Success",
  "message": "计算绩点成功",
  "data": 3.25
}
```

---

## 11. 数据结构说明

### 成绩计算规则

| 成绩组成 | 权重 |
|----------|------|
| 平时成绩 (usual_score) | 30% |
| 期中成绩 (midterm_score) | 20% |
| 期末成绩 (final_score) | 50% |

**总评成绩公式**：`total_score = usual * 0.3 + midterm * 0.2 + final * 0.5`

### 绩点对照表（标准4.0制）

| 总评分数 | 绩点 |
|----------|------|
| 90-100 | 4.0 |
| 85-89 | 3.7 |
| 82-84 | 3.3 |
| 78-81 | 3.0 |
| 75-77 | 2.7 |
| 72-74 | 2.3 |
| 68-71 | 2.0 |
| 64-67 | 1.5 |
| 60-63 | 1.0 |
| 0-59 | 0 |

### 数据关联关系

```
┌─────────────────┐     ┌─────────────────────┐     ┌─────────────────┐
│   sc_student    │────>│      sc_score       │<────│ sc_teaching_class│
│  (学生表)        │     │     (成绩表)         │     │   (教学班表)      │
└─────────────────┘     └─────────────────────┘     └─────────────────┘
        │                       │                           │
        │                       │                           │
        ▼                       ▼                           ▼
┌─────────────────┐     ┌─────────────────┐       ┌─────────────────┐
│    sc_class     │     │   sc_semester   │       │    sc_course    │
│  (行政班表)      │     │   (学期表)       │       │   (课程表)       │
└─────────────────┘     └─────────────────┘       └─────────────────┘
```

**说明**：
- 成绩表 (`sc_score`) 通过 `student_uuid` 关联学生
- 成绩表通过 `teaching_class_uuid` 关联教学班
- 成绩表通过 `semester_uuid` 关联学期
- 唯一约束：`(student_uuid, teaching_class_uuid)` 确保一个学生在同一教学班只有一条成绩记录

### 用户角色枚举

| 角色值 | 说明 |
|--------|------|
| SYSTEM_ADMIN | 系统管理员 |
| ACADEMIC_ADMIN | 教务管理员 |
| TEACHER | 教师 |
| STUDENT | 学生 |

---

## 错误响应示例

```json
{
  "output": "Fail",
  "message": "成绩不存在",
  "data": null
}
```

```json
{
  "output": "Fail",
  "message": "该学生的成绩已存在",
  "data": null
}
```

```json
{
  "output": "Fail",
  "message": "平时成绩必须在0-100之间",
  "data": null
}
```
