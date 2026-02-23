# 智能排课系统 API 文档

## 目录

- [教学班接口更新](#教学班接口更新)
- [智能排课接口](#智能排课接口)
- [数据模型](#数据模型)

---

## 教学班接口更新

### 1. 添加教学班

**接口地址**: `POST /teaching-class/add`

**请求参数**:

| 参数名 | 类型 | 必填 | 说明 | 默认值 |
|--------|------|------|------|--------|
| courseUuid | String | 是 | 课程UUID | - |
| teacherUuid | String | 是 | 教师UUID | - |
| semesterUuid | String | 是 | 学期UUID | - |
| teachingClassName | String | 是 | 教学班名称 | - |
| weeklySessions | Integer | 否 | 每周上课次数 | 1 |
| sectionsPerSession | Integer | 否 | 每次上课节次数 | 2 |

**请求示例**:

```json
{
  "courseUuid": "course123",
  "teacherUuid": "teacher456",
  "semesterUuid": "semester789",
  "teachingClassName": "高等数学-计科2101班",
  "weeklySessions": 2,
  "sectionsPerSession": 2
}
```

**响应示例**:

```json
{
  "code": 200,
  "message": "success",
  "data": "teaching-class-uuid"
}
```

---

### 2. 更新教学班

**接口地址**: `POST /teaching-class/update`

**请求参数**:

| 参数名 | 类型 | 必填 | 说明 | 默认值 |
|--------|------|------|------|--------|
| teachingClassUuid | String | 是 | 教学班UUID | - |
| courseUuid | String | 是 | 课程UUID | - |
| teacherUuid | String | 是 | 教师UUID | - |
| semesterUuid | String | 是 | 学期UUID | - |
| teachingClassName | String | 是 | 教学班名称 | - |
| weeklySessions | Integer | 否 | 每周上课次数 | 1 |
| sectionsPerSession | Integer | 否 | 每次上课节次数 | 2 |

**请求示例**:

```json
{
  "teachingClassUuid": "teaching-class-uuid",
  "courseUuid": "course123",
  "teacherUuid": "teacher456",
  "semesterUuid": "semester789",
  "teachingClassName": "高等数学-计科2101班",
  "weeklySessions": 3,
  "sectionsPerSession": 2
}
```

---

### 3. 查询教学班信息（更新）

**接口地址**: `GET /teaching-class/{teachingClassUuid}`

**响应参数（新增字段）**:

| 参数名 | 类型 | 说明 |
|--------|------|------|
| teachingClassUuid | String | 教学班UUID |
| teachingClassName | String | 教学班名称 |
| courseName | String | 课程名称 |
| teacherName | String | 教师名称 |
| semesterName | String | 学期名称 |
| teachingClassHours | Integer | 教学班学时（累计） |
| **weeklySessions** | **Integer** | **每周上课次数（新增）** |
| **sectionsPerSession** | **Integer** | **每次上课节次数（新增）** |

**响应示例**:

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "teachingClassUuid": "tc-123",
    "teachingClassName": "高等数学-计科2101班",
    "courseName": "高等数学",
    "teacherName": "张老师",
    "semesterName": "2024-2025-1",
    "teachingClassHours": 32,
    "weeklySessions": 2,
    "sectionsPerSession": 2
  }
}
```

---

### 4. 教学班分页查询（更新）

**接口地址**: `GET /teaching-class/page`

**请求参数**:

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| page | Integer | 是 | 页码 |
| size | Integer | 是 | 每页数量 |
| courseUuid | String | 否 | 课程UUID筛选 |
| teacherUuid | String | 否 | 教师UUID筛选 |
| semesterUuid | String | 否 | 学期UUID筛选 |

**响应示例**:

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "page": 1,
    "size": 10,
    "total": 50,
    "records": [
      {
        "teachingClassUuid": "tc-123",
        "teachingClassName": "高等数学-计科2101班",
        "courseName": "高等数学",
        "teacherName": "张老师",
        "semesterName": "2024-2025-1",
        "teachingClassHours": 32,
        "weeklySessions": 2,
        "sectionsPerSession": 2
      }
    ]
  }
}
```

---

## 智能排课接口

### 1. 执行自动排课

**接口地址**: `POST /auto-schedule/execute`

**功能描述**: 根据遗传算法自动生成排课方案，结果保存为预览状态（status=0）

**请求参数**:

| 参数名 | 类型 | 必填 | 说明 | 默认值 |
|--------|------|------|------|--------|
| semesterUuid | String | 是 | 学期UUID | - |
| teachingClassUuids | Array | 是 | 待排课的教学班UUID列表 | - |
| weeklySessionsConfig | Object | 否 | 每周上课次数配置 | - |
| classroomUuids | Array | 否 | 可用教室UUID列表（不传则查询所有） | - |
| populationSize | Integer | 否 | 种群大小 | 100 |
| maxGenerations | Integer | 否 | 最大迭代次数 | 500 |
| crossoverRate | Double | 否 | 交叉概率 | 0.8 |
| mutationRate | Double | 否 | 变异概率 | 0.2 |
| eliteSize | Integer | 否 | 精英保留数量 | 10 |

**weeklySessionsConfig 格式说明**:

```json
{
  "teaching-class-uuid-1": 2,
  "teaching-class-uuid-2": 3
}
```

如果不传 `weeklySessionsConfig`，则使用教学班表中的 `weekly_sessions` 字段值。

**请求示例**:

```json
{
  "semesterUuid": "semester-2024-1",
  "teachingClassUuids": [
    "tc-001",
    "tc-002",
    "tc-003"
  ],
  "weeklySessionsConfig": {
    "tc-001": 2,
    "tc-002": 3,
    "tc-003": 1
  },
  "classroomUuids": ["room-001", "room-002", "room-003"],
  "populationSize": 100,
  "maxGenerations": 500,
  "crossoverRate": 0.8,
  "mutationRate": 0.2,
  "eliteSize": 10
}
```

**响应参数**:

| 参数名 | 类型 | 说明 |
|--------|------|------|
| scheduleId | String | 排课方案ID |
| status | String | 方案状态：preview（预览）/ confirmed（已确认） |
| statistics | Object | 排课统计信息 |
| statistics.totalTeachingClasses | Integer | 教学班总数 |
| statistics.scheduledTeachingClasses | Integer | 已排课教学班数 |
| statistics.totalSchedules | Integer | 排课记录总数 |
| statistics.hardConstraintViolations | Integer | 硬约束违反次数 |
| statistics.softConstraintViolations | Integer | 软约束违反次数 |
| schedules | Array | 排课记录列表 |
| conflicts | Array | 冲突记录列表 |

**排课记录（schedules）字段**:

| 参数名 | 类型 | 说明 |
|--------|------|------|
| scheduleUuid | String | 排课记录UUID |
| teachingClassUuid | String | 教学班UUID |
| teachingClassName | String | 教学班名称 |
| courseUuid | String | 课程UUID |
| courseName | String | 课程名称 |
| teacherUuid | String | 教师UUID |
| teacherName | String | 教师名称 |
| classroomUuid | String | 教室UUID |
| classroomName | String | 教室名称 |
| dayOfWeek | Integer | 星期几（1-7） |
| sectionStart | Integer | 起始节次 |
| sectionEnd | Integer | 结束节次 |
| weeks | Array | 上课周次列表 |
| creditHours | Integer | 本次排课学时 |

**响应示例**:

```json
{
  "code": 200,
  "message": "排课成功",
  "data": {
    "scheduleId": "schedule-solution-001",
    "status": "preview",
    "statistics": {
      "totalTeachingClasses": 50,
      "scheduledTeachingClasses": 48,
      "totalSchedules": 156,
      "hardConstraintViolations": 0,
      "softConstraintViolations": 12
    },
    "schedules": [
      {
        "scheduleUuid": "sch-001",
        "teachingClassUuid": "tc-001",
        "teachingClassName": "高等数学-计科2101班",
        "courseUuid": "course-001",
        "courseName": "高等数学",
        "teacherUuid": "teacher-001",
        "teacherName": "张老师",
        "classroomUuid": "room-001",
        "classroomName": "A101",
        "dayOfWeek": 1,
        "sectionStart": 1,
        "sectionEnd": 2,
        "weeks": [1, 2, 3, 4, 5, 6, 7, 8],
        "creditHours": 2
      }
    ],
    "conflicts": []
  }
}
```

---

### 2. 确认排课方案

**接口地址**: `POST /auto-schedule/confirm`

**功能描述**: 将预览状态的排课方案转为正式状态（status: 0 → 1）

**请求参数**:

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| semesterUuid | String | 是 | 学期UUID |

**请求示例**:

```json
{
  "semesterUuid": "semester-2024-1"
}
```

**响应示例**:

```json
{
  "code": 200,
  "message": "排课方案已确认",
  "data": {
    "updatedCount": 156
  }
}
```

---

### 3. 清除预览排课

**接口地址**: `POST /auto-schedule/clear`

**功能描述**: 清除学期的预览排课方案（仅删除 status=0 的记录）

**请求参数**:

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| semesterUuid | String | 是 | 学期UUID |

**请求示例**:

```json
{
  "semesterUuid": "semester-2024-1"
}
```

**响应示例**:

```json
{
  "code": 200,
  "message": "预览方案已清除",
  "data": {
    "deletedCount": 156
  }
}
```

---

### 4. 查询排课结果

**接口地址**: `GET /auto-schedule/result/{semesterUuid}`

**功能描述**: 查询指定学期的排课结果（包括预览和正式状态）

**响应参数**:

| 参数名 | 类型 | 说明 |
|--------|------|------|
| semesterUuid | String | 学期UUID |
| previewCount | Integer | 预览状态排课数量（status=0） |
| confirmedCount | Integer | 正式状态排课数量（status=1） |
| schedules | Array | 排课记录列表 |

**响应示例**:

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "semesterUuid": "semester-2024-1",
    "previewCount": 156,
    "confirmedCount": 0,
    "schedules": [
      {
        "scheduleUuid": "sch-001",
        "teachingClassName": "高等数学-计科2101班",
        "teacherName": "张老师",
        "classroomName": "A101",
        "dayOfWeek": 1,
        "sectionStart": 1,
        "sectionEnd": 2,
        "weeks": [1, 2, 3, 4, 5, 6, 7, 8],
        "status": 0
      }
    ]
  }
}
```

---

## 数据模型

### 教学班相关

#### TeachingClassInfoDTO

```typescript
interface TeachingClassInfoDTO {
  teachingClassUuid: string;      // 教学班UUID
  teachingClassName: string;       // 教学班名称
  courseName: string;              // 课程名称
  teacherName: string;             // 教师名称
  semesterName: string;            // 学期名称
  teachingClassHours: number;      // 教学班学时（累计）
  weeklySessions: number;          // 每周上课次数（新增）
  sectionsPerSession: number;      // 每次上课节次数（新增）
}
```

#### AddTeachingClassVO

```typescript
interface AddTeachingClassVO {
  teachingClassUuid?: string;      // 教学班UUID（更新时需要）
  courseUuid: string;              // 课程UUID
  teacherUuid: string;             // 教师UUID
  semesterUuid: string;            // 学期UUID
  teachingClassName: string;       // 教学班名称
  weeklySessions?: number;         // 每周上课次数（可选，默认1）
  sectionsPerSession?: number;     // 每次上课节次数（可选，默认2）
}
```

---

### 智能排课相关

#### AutoScheduleVO

```typescript
interface AutoScheduleVO {
  semesterUuid: string;                      // 学期UUID
  teachingClassUuids: string[];              // 待排课的教学班UUID列表
  weeklySessionsConfig?: Record<string, number>;  // 每周上课次数配置（可选）
  classroomUuids?: string[];                 // 可用教室UUID列表（可选）
  populationSize?: number;                   // 种群大小（默认100）
  maxGenerations?: number;                   // 最大迭代次数（默认500）
  crossoverRate?: number;                    // 交叉概率（默认0.8）
  mutationRate?: number;                     // 变异概率（默认0.2）
  eliteSize?: number;                        // 精英保留数量（默认10）
}
```

#### ScheduleResult

```typescript
interface ScheduleResult {
  scheduleId: string;                        // 排课方案ID
  status: 'preview' | 'confirmed';           // 方案状态
  statistics: {
    totalTeachingClasses: number;            // 教学班总数
    scheduledTeachingClasses: number;        // 已排课教学班数
    totalSchedules: number;                  // 排课记录总数
    hardConstraintViolations: number;        // 硬约束违反次数
    softConstraintViolations: number;        // 软约束违反次数
  };
  schedules: ScheduleItem[];                 // 排课记录列表
  conflicts: ConflictItem[];                 // 冲突记录列表
}
```

#### ScheduleItem

```typescript
interface ScheduleItem {
  scheduleUuid: string;          // 排课记录UUID
  teachingClassUuid: string;     // 教学班UUID
  teachingClassName: string;     // 教学班名称
  courseUuid: string;            // 课程UUID
  courseName: string;            // 课程名称
  teacherUuid: string;           // 教师UUID
  teacherName: string;           // 教师名称
  classroomUuid: string;         // 教室UUID
  classroomName: string;         // 教室名称
  dayOfWeek: number;             // 星期几（1-7）
  sectionStart: number;          // 起始节次
  sectionEnd: number;            // 结束节次
  weeks: number[];               // 上课周次列表
  creditHours: number;           // 本次排课学时
}
```

#### ConflictItem

```typescript
interface ConflictItem {
  conflictUuid: string;          // 冲突记录UUID
  conflictType: string;          // 冲突类型
  description: string;           // 冲突描述
  relatedSchedules: string[];    // 相关排课记录UUID
}
```

---

## 错误码说明

| 错误码 | 说明 |
|--------|------|
| 200 | 成功 |
| 400 | 请求参数错误 |
| 404 | 资源不存在 |
| 500 | 服务器内部错误 |
| 1001 | 课程不存在 |
| 1002 | 教师不存在 |
| 1003 | 学期不存在 |
| 1004 | 教师没有该课程授课资格 |
| 1005 | 教学班被关联无法删除 |
| 2001 | 排课失败：资源不足 |
| 2002 | 排课失败：约束冲突过多 |

---

## 前端对接注意事项

### 1. 教学班表单更新

添加/编辑教学班时，需要新增两个输入框：

```html
<!-- 每周上课次数 -->
<el-form-item label="每周上课次数">
  <el-input-number
    v-model="form.weeklySessions"
    :min="1"
    :max="7"
    placeholder="默认1次"
  />
</el-form-item>

<!-- 每次上课节次数 -->
<el-form-item label="每次上课节次数">
  <el-input-number
    v-model="form.sectionsPerSession"
    :min="1"
    :max="6"
    :step="2"
    placeholder="默认2节"
  />
</el-form-item>
```

### 2. 教学班列表更新

在表格中新增两列显示：

```html
<el-table-column label="每周上课次数" prop="weeklySessions" width="120" />
<el-table-column label="每次节次数" prop="sectionsPerSession" width="120" />
```

### 3. 智能排课时配置每周次数

提供更细粒度的每周上课次数配置：

```typescript
// 排课时可以为每个教学班单独配置每周上课次数
const weeklyConfig: Record<string, number> = {
  'tc-001': 2,  // 高等数学每周2次
  'tc-002': 3,  // 英语每周3次
  'tc-003': 1   // 体育每周1次
};
```

### 4. 排课结果展示

排课结果中需要展示：
- 时间槽信息（星期、节次、周次）
- 冲突记录（如果有）
- 统计信息（完成率、违反次数）

### 5. 预览和确认流程

1. 调用 `/auto-schedule/execute` 执行排课，返回预览结果
2. 用户在界面上查看预览结果
3. 调用 `/auto-schedule/confirm` 确认方案，将预览转为正式
4. 如果不满意，可以调用 `/auto-schedule/clear` 清除预览重新排课
