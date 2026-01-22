# 智能排课系统数据库设计文档

## 概述

本文档描述了智能排课系统（Smart Schedule Core V2）的数据库结构。系统采用PostgreSQL数据库，使用`public` schema，数据库所有者为`smart-schedule-core`。

## 核心设计理念

1. **UUID主键**：所有表使用32位varchar类型的UUID作为主键
2. **JSONB灵活存储**：对于复杂关系（如教师列表、时间偏好等）使用JSONB类型
3. **外键约束**：严格的关系完整性约束
4. **详细注释**：所有表和字段都有完整的中文注释

## 数据库表结构

### 1. 基础组织架构表

#### 1.1 sc_semester（学期表）
**文件**: `sc_semester.sql`

| 字段名 | 类型 | 约束 | 说明 |
|--------|------|------|------|
| semester_uuid | varchar(32) | PRIMARY KEY | 学期UUID |
| semester_name | varchar(32) | NOT NULL | 学期名称 |

**用途**: 管理学期信息，作为排课的时间维度基础。

---

#### 1.2 sc_department（学院表）
**文件**: `sc_department.sql`

| 字段名 | 类型 | 约束 | 说明 |
|--------|------|------|------|
| department_uuid | varchar(32) | PRIMARY KEY | 学院UUID |
| department_name | varchar(32) | NOT NULL | 学院名称 |

**用途**: 组织架构的最高层级，管理各个学院/系部信息。

---

#### 1.3 sc_major（专业表）
**文件**: `sc_major.sql`

| 字段名 | 类型 | 约束 | 说明 |
|--------|------|------|------|
| major_uuid | varchar(32) | PRIMARY KEY | 专业UUID |
| department_uuid | varchar(32) | FOREIGN KEY → sc_department | 学院UUID |
| major_num | varchar | NOT NULL | 专业编号 |
| major_name | varchar | NOT NULL | 专业名称 |

**关系**:
- 属于某个学院（`department_uuid`）
- 被多个行政班级引用（`sc_class.major_uuid`）

**用途**: 管理专业信息，连接学院和行政班级。

---

### 2. 人员管理表

#### 2.1 sc_student（学生表）
**文件**: `sc_student.sql`

| 字段名 | 类型 | 约束 | 说明 |
|--------|------|------|------|
| student_uuid | varchar(32) | PRIMARY KEY | 学生UUID |
| student_id | varchar(32) | UNIQUE, NOT NULL | 学号（唯一编码） |
| student_name | varchar(32) | NOT NULL | 学生姓名 |
| class_uuid | varchar(32) | FOREIGN KEY → sc_class | 行政班级UUID |
| student_password | varchar(128) | NOT NULL | 学生密码（建议加密存储） |

**关系**:
- 属于某个行政班级（`class_uuid`）

**用途**: 存储学生基本信息和认证凭据。

---

#### 2.2 sc_teacher（教师表）
**文件**: `sc_teacher.sql`

| 字段名 | 类型 | 约束 | 说明 |
|--------|------|------|------|
| teacher_uuid | varchar(32) | PRIMARY KEY | 教师UUID |
| teacher_num | varchar(32) | UNIQUE, NOT NULL | 教师编号（唯一工号） |
| teacher_name | varchar(32) | NOT NULL | 教师名称 |
| title | varchar(32) | NOT NULL | 职称 |
| teacher_password | varchar | NOT NULL | 密码 |
| max_hours_per_week | integer | NOT NULL | 每周最高授课时长 |
| like_time | jsonb | NOT NULL | 喜欢时间（JSONB格式） |
| is_active | boolean | NOT NULL, DEFAULT true | 是否启用 |

**特色字段**:
- `like_time`: JSONB格式存储教师的时间偏好
- `max_hours_per_week`: 用于排课时的工作量平衡
- `is_active`: 支持教师账号的启用/禁用

**用途**: 存储教师信息、授课能力和时间偏好，是排课算法的核心输入之一。

---

#### 2.3 sc_academic_admin（教务管理表）
**文件**: `sc_academic_admin.sql`

| 字段名 | 类型 | 约束 | 说明 |
|--------|------|------|------|
| academic_uuid | varchar(32) | PRIMARY KEY | 教务人员UUID |
| department_uuid | varchar(32) | FOREIGN KEY → sc_department | 所属学院UUID |
| academic_num | varchar(32) | UNIQUE, NOT NULL | 教务工号（唯一编码） |
| academic_name | varchar(32) | NOT NULL | 教务名称 |
| academic_password | varchar(128) | NOT NULL | 教务密码（建议加密存储） |

**关系**:
- 属于某个学院（`department_uuid`）

**用途**: 管理教务人员账号，用于系统管理和排课操作权限控制。

---

### 3. 教学资源表

#### 3.1 sc_building（教学楼表）
**文件**: `sc_building.sql`

| 字段名 | 类型 | 约束 | 说明 |
|--------|------|------|------|
| building_uuid | varchar(32) | PRIMARY KEY | 教学楼UUID |
| building_num | varchar(32) | NOT NULL | 教学楼编号 |
| building_name | varchar(32) | NOT NULL | 教学楼名称 |

**用途**: 管理教学楼基本信息，为教室提供空间组织。

---

#### 3.2 sc_classroom（教室表）
**文件**: `sc_classroom.sql`

| 字段名 | 类型 | 约束 | 说明 |
|--------|------|------|------|
| classroom_uuid | varchar(32) | PRIMARY KEY | 教室UUID |
| building_uuid | varchar(32) | FOREIGN KEY → sc_building | 教学楼UUID |
| classroom_name | varchar | NOT NULL | 教室名称 |
| classroom_capacity | integer | NOT NULL | 教室容量 |
| classroom_type | varchar(32) | NOT NULL | 教室种类（后端枚举） |

**关系**:
- 位于某个教学楼（`building_uuid`）

**特色字段**:
- `classroom_capacity`: 用于匹配课程班级规模
- `classroom_type`: 支持不同类型教室（普通教室、实验室、多媒体教室等）

**用途**: 管理教室资源，是排课的空间维度约束。

---

### 4. 课程与教学组织表

#### 4.1 sc_class（行政班级表）
**文件**: `sc_class.sql`

| 字段名 | 类型 | 约束 | 说明 |
|--------|------|------|------|
| class_uuid | varchar(32) | PRIMARY KEY | 行政班级UUID |
| major_uuid | varchar(32) | FOREIGN KEY → sc_major | 专业UUID |
| class_name | varchar | NOT NULL | 行政班级名称 |

**关系**:
- 属于某个专业（`major_uuid`）
- 被多个学生引用（`sc_student.class_uuid`）
- 被多个教学班引用（`sc_teaching_class.class_uuids`）

**用途**: 管理行政班级（如"计算机2021-1班"），是学生的组织单位。

---

#### 4.2 sc_course（课程主表）
**文件**: `sc_course.sql`

| 字段名 | 类型 | 约束 | 说明 |
|--------|------|------|------|
| course_uuid | varchar(32) | PRIMARY KEY | 课程UUID |
| course_num | varchar(32) | UNIQUE, NOT NULL | 课程编号（唯一编码） |
| course_name | varchar(64) | NOT NULL | 课程名称 |
| course_type | varchar(32) | NOT NULL | 课程种类（后端枚举标识） |
| course_credit | numeric | NOT NULL | 课程学分（支持半分） |
| qualified_teacher_uuids | jsonb | NOT NULL, DEFAULT '[]'::jsonb | 具有教授资格的老师UUID列表 |

**特色字段**:
- `qualified_teacher_uuids`: JSONB数组存储可以讲授该课程的教师列表
- `course_credit`: 支持半学分（如1.5学分）

**用途**: 课程主数据，定义课程基本信息和授课资格。

---

#### 4.3 sc_teaching_class（教学班表）
**文件**: `sc_teaching_class.sql`

| 字段名 | 类型 | 约束 | 说明 |
|--------|------|------|------|
| teaching_class_uuid | varchar(32) | PRIMARY KEY | 教学班UUID |
| course_uuid | varchar(32) | FOREIGN KEY → sc_course | 课程UUID |
| teacher_uuid | varchar(32) | FOREIGN KEY → sc_teacher | 教师UUID |
| semester_uuid | varchar(32) | FOREIGN KEY → sc_semester | 学期UUID |
| class_uuids | jsonb | NOT NULL, DEFAULT '[]'::jsonb | 关联的行政班级UUID列表 |
| teaching_class_name | varchar(64) | | 教学班名称 |

**关系**:
- 关联课程（`course_uuid`）
- 关联教师（`teacher_uuid`）
- 属于某个学期（`semester_uuid`）
- 包含多个行政班级（`class_uuids`）

**特色字段**:
- `class_uuids`: JSONB数组存储该教学班包含的所有行政班级
  - 示例: `["class_uuid1", "class_uuid2", "class_uuid3"]`
  - 支持合班上课场景

**用途**: 排课的业务主体。一个教学班表示：
- 某门课 + 某个老师 + 某个学期 + 一组行政班级
- 是排课算法的基本调度单元

**核心概念**:
- **行政班级**: 学生的固定组织单位（如"计科2101班"）
- **教学班**: 临时的上课组织单位（如"高等数学-张老师-计科2101+2102"）

---

### 5. 排课核心表

#### 5.1 sc_schedule（排课表）
**文件**: `sc_schedule.sql`

这是系统的**核心表**，存储最终生成的课表数据。

| 字段名 | 类型 | 约束 | 说明 |
|--------|------|------|------|
| schedule_uuid | varchar(32) | PRIMARY KEY | 排课记录UUID |
| semester_uuid | varchar(32) | FOREIGN KEY → sc_semester | 学期UUID |
| teaching_class_uuid | varchar(32) | FOREIGN KEY → sc_teaching_class | 教学班UUID |
| course_uuid | varchar(32) | FOREIGN KEY → sc_course | 课程UUID（冗余字段） |
| teacher_uuid | varchar(32) | FOREIGN KEY → sc_teacher | 教师UUID（冗余字段） |
| classroom_uuid | varchar(32) | FOREIGN KEY → sc_classroom | 教室UUID（冗余字段） |
| day_of_week | integer | NOT NULL | 星期几（1-7） |
| section_start | integer | NOT NULL | 起始节次 |
| section_end | integer | NOT NULL | 结束节次 |
| weeks_json | jsonb | NOT NULL, DEFAULT '[]'::jsonb | 上课周次 |
| is_locked | boolean | NOT NULL, DEFAULT false | 锁定标识 |
| status | integer | NOT NULL, DEFAULT 0 | 状态（0-预览方案, 1-正式执行） |
| updated_at | timestamp | DEFAULT current_timestamp | 更新时间 |

**关系**:
- 属于某个学期（`semester_uuid`）
- 关联教学班（`teaching_class_uuid`）
- 冗余存储课程、教师、教室信息，减少JOIN查询

**时间维度**:
- `day_of_week`: 星期（1=周一, 7=周日）
- `section_start` / `section_end`: 节次范围（如1-2表示第1-2节）
- `weeks_json`: 上课周次数组（如`[1,2,3,4,5,6,7,8,9,10]`）

**控制字段**:
- `is_locked`: 标记该时间段是否已被人工锁定，排课算法不应修改
- `status`: 区分预览方案和正式执行方案

**用途**: 存储排课结果，每一行表示：
- 某个教学班在某个时间段的某间教室上课

---

#### 5.2 sc_schedule_conflict（排课冲突记录表）
**文件**: `sc_schedule_conflict.sql`

| 字段名 | 类型 | 约束 | 说明 |
|--------|------|------|------|
| conflict_uuid | varchar(32) | PRIMARY KEY | 冲突记录UUID |
| semester_uuid | varchar(32) | NOT NULL | 学期UUID |
| schedule_uuid_a | varchar(32) | FOREIGN KEY → sc_schedule | 排课记录A的UUID |
| schedule_uuid_b | varchar(32) | FOREIGN KEY → sc_schedule | 排课记录B的UUID |
| conflict_type | varchar(32) | NOT NULL | 冲突类型 |
| severity | integer | NOT NULL | 严重程度（1-硬冲突, 0-软冲突） |
| description | text | NOT NULL | 冲突描述 |

**冲突类型**（可能包括但不限于）:
- 教师时间冲突：同一教师在同一时间有多门课
- 教室冲突：同一教室在同一时间被分配多次
- 班级冲突：同一行政班级的学生在同一时间有多门课

**严重程度**:
- 1（硬冲突）：必须解决的冲突（如教师、教室、班级时间重叠）
- 0（软冲突）：建议优化的冲突（如教师连续授课、教室利用率低）

**用途**: 记录排课过程中检测到的冲突，支持冲突分析和解决。

---

## 数据关系图

```
sc_semester（学期）
    ↓
    ├─ sc_teaching_class（教学班）
    │       ↓
    │       ├─ sc_course（课程）──→ qualified_teacher_uuids ──→ sc_teacher（教师）
    │       ├─ sc_teacher（教师）
    │       └─ class_uuids ──→ sc_class（行政班级）
    │                              ↓
    │                              └─ sc_student（学生）
    │
    └─ sc_schedule（排课记录）
            ↓
            ├─ teaching_class_uuid ──→ sc_teaching_class
            ├─ course_uuid ──→ sc_course
            ├─ teacher_uuid ──→ sc_teacher
            └─ classroom_uuid ──→ sc_classroom
                                    ↓
                                    └─ building_uuid ──→ sc_building

sc_department（学院）
    ↓
    ├─ sc_major（专业）
    │       ↓
    │       └─ sc_class（行政班级）
    │
    └─ sc_academic_admin（教务管理）

sc_schedule_conflict（冲突记录）
    ↓
    ├─ schedule_uuid_a ──→ sc_schedule
    └─ schedule_uuid_b ──→ sc_schedule
```

## 核心业务流程

### 1. 排课准备阶段
1. 创建学期（`sc_semester`）
2. 导入基础数据：学院、专业、行政班级、学生、教师、教学楼、教室
3. 创建课程信息（`sc_course`），指定授课教师资格
4. 创建教学班（`sc_teaching_class`），指定课程、教师、学期、参与班级

### 2. 排课执行阶段
1. 读取该学期的所有教学班
2. 根据约束条件进行排课：
   - 教师时间偏好（`sc_teacher.like_time`）
   - 教师最大课时（`sc_teacher.max_hours_per_week`）
   - 教室容量匹配（`sc_classroom.classroom_capacity`）
   - 教室类型匹配（`sc_classroom.classroom_type`）
3. 生成排课记录（`sc_schedule`）
4. 检测冲突并记录（`sc_schedule_conflict`）

### 3. 人工调整阶段
1. 查看预览方案（`sc_schedule.status = 0`）
2. 手动调整不合理的时间段
3. 锁定确认的时间段（`sc_schedule.is_locked = true`）
4. 重新排课（跳过已锁定记录）
5. 确认后设置为正式方案（`sc_schedule.status = 1`）

### 4. 课表查询阶段
1. 学生查询：通过`sc_student.class_uuid` → `sc_teaching_class.class_uuids` → `sc_schedule`
2. 教师查询：通过`sc_teacher.teacher_uuid` → `sc_teaching_class.teacher_uuid` → `sc_schedule`
3. 教室查询：通过`sc_classroom.classroom_uuid` → `sc_schedule.classroom_uuid`

## JSONB字段说明

### sc_course.qualified_teacher_uuids
```json
["teacher_uuid_1", "teacher_uuid_2", "teacher_uuid_3"]
```
表示该课程可以由这3位教师讲授。

### sc_teacher.like_time
```json
{
  "preferred": [
    {"day": 1, "sections": [1, 2, 3]},
    {"day": 3, "sections": [5, 6, 7]}
  ],
  "unwanted": [
    {"day": 5, "sections": [9, 10]}
  ]
}
```
表示教师喜欢在周一第1-3节、周三第5-7节上课，不喜欢周五第9-10节。

### sc_teaching_class.class_uuids
```json
["class_uuid_1", "class_uuid_2", "class_uuid_3"]
```
表示该教学班包含3个行政班级的学生（合班上课）。

### sc_schedule.weeks_json
```json
[1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16]
```
表示该课程在整个学期的1-16周都上课。也支持单双周：
```json
[1, 3, 5, 7, 9, 11, 13, 15]  // 单周
[2, 4, 6, 8, 10, 12, 14, 16]  // 双周
```

## 数据库权限

所有表的所有权都归属给：`"smart-schedule-core"`

## 设计优势

1. **灵活性**: JSONB字段支持复杂关系，无需额外的关联表
2. **性能**: 冗余字段减少JOIN操作，提升查询性能
3. **扩展性**: 枚举类型由后端定义，数据库只存储字符串
4. **完整性**: 严格的外键约束保证数据一致性
5. **可维护性**: 详细的中文注释降低维护成本

## 后续优化建议

1. **索引优化**: 为常用查询字段添加索引（如`semester_uuid`、`teacher_uuid`等）
2. **分区策略**: 按学期对`sc_schedule`表进行分区
3. **视图封装**: 创建常用查询视图（如学生课表视图、教师课表视图）
4. **触发器**: 添加触发器自动检测部分冲突
5. **软删除**: 考虑为部分表添加`is_deleted`字段支持软删除

---

**文档版本**: 1.0
**最后更新**: 2026-01-22
**维护者**: Smart Schedule Core V2 Team