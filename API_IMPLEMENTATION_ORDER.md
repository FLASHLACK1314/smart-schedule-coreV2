# 智能排课系统 API 接口实现顺序指南

## 📊 当前实现状态总览

### 已完成模块（8个）

- ✅ **BuildingController**（教学楼）- 完整 CRUD
- ✅ **ClassroomController**（教室）- 完整 CRUD
- ✅ **SemesterController**（学期）- 完整 CRUD
- ✅ **AuthController**（认证）- 完整认证功能
- ✅ **SystemAdminController**（系统管理员）- 部分功能
- ✅ **DepartmentController**（学院）- 完整 CRUD ✨ **新增**
- ✅ **CourseTypeController**（课程类型）- 完整 CRUD ✨ **新增**
- ✅ **ClassroomTypeController**（教室类型）- 完整 CRUD ✨ **新增**

### 部分完成模块（2个）

- ⚠️ **StudentController**（学生）- 仅实现 add，缺少 get/update/delete/getPage
- ⚠️ **UserController**（用户）- 仅实现 getUserInfo

### 未开始模块（9个）

- ❌ MajorController（专业）- 已创建文件，未实现
- ❌ ClassController（行政班级）- 已创建文件，未实现
- ❌ CourseController（课程）- 已创建文件，未实现
- ❌ CourseClassroomTypeController（课程类型-教室类型关联）- ❌ **缺少文件** ✨
- ❌ CourseQualificationController（课程教师资格）- ❌ **缺少文件**
- ❌ TeacherController（教师）- 已创建文件，未实现
- ❌ AcademicController（教务管理）- 已创建文件，未实现
- ❌ TeachingClassController（教学班）- 已创建文件，未实现
- ❌ TeachingClassClassController（教学班-行政班关联）- ❌ **缺少文件**
- ❌ ScheduleController（排课）- 已创建文件，未实现（**用户要求放到最后**）
- ❌ ScheduleConflictController（排课冲突）- 已创建文件，未实现（**用户要求放到最后**）

> **说明**：共 18 个数据库表，其中 3 个关联表（course_classroom_type, course_qualification, teaching_class_class）尚未创建控制器文件

---

## 🎯 推荐实现顺序（基于数据库依赖和业务逻辑）

### 第一阶段：基础数据管理（零依赖）✅ **已完成**

**优先级：🔥 最高 - 其他模块的基础**

#### 1. DepartmentController（学院管理）✅ **已完成**

**文件路径**: `controller/DepartmentController.java`

**依赖关系**: 无外键依赖，基础表

**已实现的接口**:

- ✅ `POST /v1/department/add` - 添加学院
- ✅ `GET /v1/department/getPage` - 分页查询学院（支持按学院名称模糊查询）
- ✅ `PUT /v1/department/update` - 更新学院信息
- ✅ `GET /v1/department/get` - 获取单个学院信息
- ✅ `DELETE /v1/department/delete` - 删除学院（检查专业引用）

**实现的关键点**:

- ✅ 学院名称唯一性检查
- ✅ 删除前检查专业引用
- ✅ DAO层增强：添加 existsByDepartmentName、existsByDepartmentNameExcludeUuid、getDepartmentPage
- ✅ Service层：完整业务逻辑和日志
- ✅ Controller层：5个REST端点，权限控制
- ✅ VO：AddDepartmentVO

---

#### 2. CourseTypeController（课程类型管理）✅ **已完成**

**文件路径**: `controller/CourseTypeController.java`

**依赖关系**: 无外键依赖，基础表

**已实现的接口**:

- ✅ `POST /v1/courseType/add` - 添加课程类型
- ✅ `GET /v1/courseType/getPage` - 分页查询课程类型
- ✅ `PUT /v1/courseType/update` - 更新课程类型
- ✅ `GET /v1/courseType/get` - 获取课程类型信息
- ✅ `DELETE /v1/courseType/delete` - 删除课程类型

**实现的关键点**:

- ✅ 类型名称唯一性检查（字段：typeName）
- ✅ DAO层增强：添加 existsByCourseTypeName、existsByCourseTypeNameExcludeUuid、getCourseTypePage
- ✅ Service层：完整业务逻辑
- ✅ Controller层：5个REST端点
- ✅ VO：AddCourseTypeVO

---

#### 3. ClassroomTypeController（教室类型管理）✅ **已完成**

**文件路径**: `controller/ClassroomTypeController.java`

**依赖关系**: 无外键依赖，基础表

**已实现的接口**:

- ✅ `POST /v1/classroomType/add` - 添加教室类型
- ✅ `GET /v1/classroomType/getPage` - 分页查询教室类型
- ✅ `PUT /v1/classroomType/update` - 更新教室类型
- ✅ `GET /v1/classroomType/get` - 获取教室类型信息
- ✅ `DELETE /v1/classroomType/delete` - 删除教室类型

**实现的关键点**:

- ✅ 类型名称唯一性检查（字段：typeName）
- ✅ DAO层增强：添加 existsByClassroomTypeName、existsByClassroomTypeNameExcludeUuid、getClassroomTypePage
- ✅ Service层：完整业务逻辑
- ✅ Controller层：5个REST端点
- ✅ VO：AddClassroomTypeVO

---

### 第二阶段：一级依赖数据（依赖基础表）

**优先级：🔥 高 - 构建核心业务结构** **已完成**

#### 4. MajorController（专业管理）

**文件路径**: `controller/MajorController.java`

**依赖关系**: `major.department_uuid → department.department_uuid`

**需要实现的接口**:

- `POST /v1/major/add` - 添加专业（需选择所属学院）
- `GET /v1/major/getPage` - 分页查询专业（支持按专业名称/编号/学院筛选）
- `PUT /v1/major/update` - 更新专业信息
- `GET /v1/major/get` - 获取专业信息（返回时包含学院名称）
- `DELETE /v1/major/delete` - 删除专业（需检查是否被 class 引用）

**关键点**:

- 专业编号唯一性检查
- 需关联查询学院名称
- 删除前需检查是否有行政班级引用

---

#### 5. ClassController（行政班级管理）**已完成**

**文件路径**: `controller/ClassController.java`

**依赖关系**: `class.major_uuid → major.major_uuid`

**需要实现的接口**:

- `POST /v1/class/add` - 添加行政班级（需选择所属专业）
- `GET /v1/class/getPage` - 分页查询行政班级（支持按班级名称/专业/学院筛选）
- `PUT /v1/class/update` - 更新行政班级信息
- `GET /v1/class/get` - 获取行政班级信息（返回时包含专业、学院名称）
- `DELETE /v1/class/delete` - 删除行政班级（需检查是否被 student 引用）

**关键点**:

- 班级名称在同一专业下唯一性检查
- 需关联查询专业、学院名称
- 删除前需检查是否有学生引用

---

#### 6. TeacherController（教师管理） **已完成**

**文件路径**: `controller/TeacherController.java`

**依赖关系**: `teacher.department_uuid → department.department_uuid`

**需要实现的接口**:

- `POST /v1/teacher/add` - 添加教师（需选择所属学院）
- `GET /v1/teacher/getPage` - 分页查询教师（支持按教师姓名/工号/学院筛选）
- `PUT /v1/teacher/update` - 更新教师信息
- `GET /v1/teacher/get` - 获取教师信息（返回时包含学院名称）
- `DELETE /v1/teacher/delete` - 删除教师（需检查是否被 teaching_class/course_qualification/schedule 引用）

**关键点**:

- 教师工号唯一性检查
- 密码加密：使用 `PasswordUtil.encrypt()`
- 需关联查询学院名称
- 删除前需检查是否被教学班、课程资格、排课引用
- 包含 JSONB 字段：`like_time`（教师时间偏好）存储样式：周一的上午3-4节课存储为 `{"1": [3,4]}`
- 已经有了返回值TeacherInfoDTO，具体实现可以参考其他的控制器层和实现

---

#### 7. CourseController（课程管理） **已完成**

**文件路径**: `controller/CourseController.java`

**依赖关系**: `course.course_type_uuid → course_type.course_type_uuid`

**需要实现的接口**:

- `POST /v1/course/add` - 添加课程（需选择课程类型）
- `GET /v1/course/getPage` - 分页查询课程（支持按课程名称/编号/类型筛选）
- `PUT /v1/course/update` - 更新课程信息
- `GET /v1/course/get` - 获取课程信息（返回时包含课程类型名称）
- `DELETE /v1/course/delete` - 删除课程（需检查是否被 teaching_class/course_qualification/schedule 引用）

**关键点**:

- 课程编号唯一性检查
- 学分使用 `BigDecimal` 类型
- 需关联查询课程类型名称
- 删除前需检查是否被教学班、课程资格、排课引用(排课引用用Todo留下来)
- 已经有了返回值CourseInfoDTO，具体实现可以参考其他的控制器层和实现

---

#### 8. CourseClassroomTypeController（课程类型-教室类型关联管理）⚠️ **需要创建** **已完成**

**文件路径**: `controller/CourseClassroomTypeController.java` ⚠️ **需要创建**

**依赖关系**:

- `course_classroom_type.course_type_uuid → course_type.course_type_uuid`
- `course_classroom_type.classroom_type_uuid → classroom_type.classroom_type_uuid`

**需要实现的接口**:

- `POST /v1/courseClassroomType/add` - 添加课程类型-教室类型关联（指定某类课程可以在某类教室上课）
- `GET /v1/courseClassroomType/getPage` - 分页查询关联关系
- `DELETE /v1/courseClassroomType/delete` - 删除关联关系

**关键点**:

- 防止重复添加同一课程类型-教室类型组合
- 需关联查询课程类型名称、教室类型名称
- **排课时使用此表验证课程是否可以在指定教室上课**（后续TODO）
- 表结构：`sc_course_classroom_type(relation_uuid, course_type_uuid, classroom_type_uuid)`

---

#### 9. CourseQualificationController（课程教师资格管理） **已完成**

**文件路径**: `controller/CourseQualificationController.java` ⚠️ **需要创建**

**依赖关系**:

- `course_qualification.course_uuid → course.course_uuid`
- `course_qualification.teacher_uuid → teacher.teacher_uuid`

**需要实现的接口**:

- `POST /v1/courseQualification/add` - 添加教师资格（需选择课程和教师）
- `GET /v1/courseQualification/getPage` - 分页查询教师资格
- `DELETE /v1/courseQualification/delete` - 删除教师资格

**关键点**:

- 防止重复添加同一课程-教师组合
- 需关联查询课程名称、教师姓名、学院名称，这里应该创建新的dto来返回这个数据

---

### 第三阶段：二级依赖数据（依赖一级依赖表）

**优先级：🟡 中 - 完善业务数据**

#### 10. StudentController（学生管理）- 补充完整

**文件路径**: `controller/StudentController.java`

**依赖关系**: `student.class_uuid → class.class_uuid`

**当前状态**: ⚠️ 仅实现 add 接口

**需要补充的接口**:

- `GET /v1/student/getPage` - 分页查询学生（支持按学号/姓名/班级/专业/学院筛选）
- `PUT /v1/student/update` - 更新学生信息
- `GET /v1/student/get` - 获取学生信息（返回时包含班级、专业、学院名称）
- `DELETE /v1/student/delete` - 删除学生

**关键点**:

- 学号唯一性检查
- 密码加密：使用 `PasswordUtil.encrypt()`
- 需关联查询班级、专业、学院名称
- 支持多级筛选（按学院→专业→班级）

---

#### 11. AcademicController（教务管理员管理）

**文件路径**: `controller/AcademicController.java`

**依赖关系**: `academic_admin.department_uuid → department.department_uuid`

**需要实现的接口**:

- `POST /v1/academic/add` - 添加教务管理员（需选择所属学院）
- `GET /v1/academic/getPage` - 分页查询教务管理员（支持按姓名/工号/学院筛选）
- `PUT /v1/academic/update` - 更新教务管理员信息
- `GET /v1/academic/get` - 获取教务管理员信息（返回时包含学院名称）
- `DELETE /v1/academic/delete` - 删除教务管理员

**关键点**:

- 工号唯一性检查
- 密码加密：使用 `PasswordUtil.encrypt()`
- 需关联查询学院名称

---

### 第四阶段：三级依赖数据（核心业务）

**优先级：🟠 中高 - 教学班管理**

#### 12. TeachingClassController（教学班管理）

**文件路径**: `controller/TeachingClassController.java`

**依赖关系**:

- `teaching_class.course_uuid → course.course_uuid`
- `teaching_class.teacher_uuid → teacher.teacher_uuid`
- `teaching_class.semester_uuid → semester.semester_uuid`

**需要实现的接口**:

- `POST /v1/teachingClass/add` - 添加教学班（需选择课程、教师、学期）
- `GET /v1/teachingClass/getPage` - 分页查询教学班（支持按课程/教师/学期筛选）
- `PUT /v1/teachingClass/update` - 更新教学班信息
- `GET /v1/teachingClass/get` - 获取教学班信息（返回时包含课程、教师、学期名称）
- `DELETE /v1/teachingClass/delete` - 删除教学班（需检查是否被 teaching_class_class/schedule 引用）

**关键点**:

- 需关联查询课程、教师、学期名称
- 删除前需检查是否被教学班-行政班关联、排课引用
- 验证所选教师具有该课程的授课资格

---

#### 13. TeachingClassClassController（教学班-行政班关联管理）

**文件路径**: `controller/TeachingClassClassController.java` ⚠️ **需要创建**

**依赖关系**:

- `teaching_class_class.teaching_class_uuid → teaching_class.teaching_class_uuid`
- `teaching_class_class.class_uuid → class.class_uuid`

**需要实现的接口**:

- `POST /v1/teachingClassClass/add` - 添加行政班到教学班
- `GET /v1/teachingClassClass/getPage` - 分页查询教学班的行政班列表
- `DELETE /v1/teachingClassClass/delete` - 移除教学班的行政班

**关键点**:

- 防止重复添加同一行政班
- 需关联查询教学班名称、行政班名称、专业、学院

---

### 第五阶段：排课系统（最高级依赖）

**优先级：🔴 低 - 核心功能，用户要求放到最后**

#### 14. ScheduleController（排课管理）⏳ **最后实现**

**文件路径**: `controller/ScheduleController.java`

**依赖关系**:

- `schedule.semester_uuid → semester.semester_uuid`
- `schedule.teaching_class_uuid → teaching_class.teaching_class_uuid`
- `schedule.classroom_uuid → classroom.classroom_uuid`
- 冗余字段：`course_uuid`, `teacher_uuid`

**需要实现的接口**:

- `POST /v1/schedule/add` - 添加排课记录
- `GET /v1/schedule/getPage` - 分页查询排课记录（多维度筛选）
- `PUT /v1/schedule/update` - 更新排课记录
- `GET /v1/schedule/get` - 获取排课记录详情
- `DELETE /v1/schedule/delete` - 删除排课记录
- `POST /v1/schedule/auto-schedule` - 自动排课（核心功能）
- `GET /v1/schedule/conflicts` - 查看排课冲突

**关键点**:

- 复杂的关联查询（教学班、课程、教师、教室、学期）
- JSONB 字段：`weeks_json`（上课周次）
- 时间冲突检测
- 教室容量检测
- 教师工作量检测
- 支持锁定标识 `is_locked`

---

#### 15. ScheduleConflictController（排课冲突管理）⏳ **最后实现**

**文件路径**: `controller/ScheduleConflictController.java`

**依赖关系**:

- `schedule_conflict.semester_uuid → semester.semester_uuid`
- `schedule_conflict.schedule_uuid_a → schedule.schedule_uuid`
- `schedule_conflict.schedule_uuid_b → schedule.schedule_uuid`

**需要实现的接口**:

- `GET /v1/scheduleConflict/getPage` - 分页查询冲突记录
- `GET /v1/scheduleConflict/get` - 获取冲突详情
- `DELETE /v1/scheduleConflict/delete` - 删除冲突记录（已解决）

**关键点**:

- 只读接口为主（冲突由系统自动检测生成）
- 需关联查询两门排课记录的详细信息
- 支持按严重程度筛选

---

## 📋 实现检查清单

### 阶段 1：基础数据（第 1-3 步）✅ **已完成**

- [x] DepartmentController 完整实现 ✅
- [x] CourseTypeController 完整实现 ✅
- [x] ClassroomTypeController 完整实现 ✅

### 阶段 2：一级依赖（第 4-9 步）

- [ ] MajorController 完整实现
- [ ] ClassController 完整实现
- [ ] TeacherController 完整实现
- [ ] CourseController 完整实现
- [ ] CourseClassroomTypeController 创建并实现 ✨ **新增**
- [ ] CourseQualificationController 创建并实现

### 阶段 3：二级依赖（第 10-11 步）

- [ ] StudentController 补充完整
- [ ] AcademicController 完整实现

### 阶段 4：三级依赖（第 12-13 步）

- [ ] TeachingClassController 完整实现
- [ ] TeachingClassClassController 创建并实现

### 阶段 5：排课系统（第 14-15 步）

- [ ] ScheduleController 完整实现（排课算法）
- [ ] ScheduleConflictController 完整实现

---

## 💡 实现建议

### 1. 参考已完成模块

- **BuildingController** 和 **ClassroomController** 是最佳参考
- 包含完整的 CRUD 操作
- 包含分页查询和多条件筛选
- 包含权限控制注解 `@RequireRole`

### 2. Service 层实现要点

- Service 接口定义方法签名
- ServiceImpl 实现业务逻辑
- DAO 层实现数据库操作
- 密码使用 `PasswordUtil.encrypt()` 加密
- UUID 使用 `UuidUtil.generateUuidNoDash()` 生成

### 3. Controller 层实现要点

- 使用 `@PostMapping`、`@GetMapping`、`@PutMapping`、`@DeleteMapping`
- 权限控制：管理员和教务可操作，所有角色可查询
- 使用 `@RequireRole` 注解控制权限
- 返回统一格式：`ResultUtil.success("消息", data)`

### 4. DTO 和 VO 设计

- **InfoDTO**：用于返回数据（包含关联表的名称）
- **AddVO**：用于添加操作
- **UpdateVO**：用于更新操作（可选，也可用 AddVO）
- 使用 `@Data` 和 `@Accessors(chain = true)`

### 5. 删除操作注意事项

删除前必须检查：

- 是否有子表引用
- 是否被排课使用
- 是否有关联数据
  使用 DAO 层的 `existsBy*` 方法检查

---

## 🚀 快速启动指南

**建议按顺序实现的模块（必做）**:

1. Department → 2. CourseType → 3. ClassroomType → 4. Major → 5. Class → 6. Student（补充）→ 7. Teacher → 8. Course → 9.
   CourseClassroomType ✨ **新增** → 10. CourseQualification → 11. Academic → 12. TeachingClass → 13.
   TeachingClassClass → 14. Schedule（最后）→ 15. ScheduleConflict（最后）

**可选模块**:

- 完善 SystemAdminController（修复路径拼写错误）
- 完善 UserController（根据业务需求）

---

## 📝 备注

- 所有接口的实现请参考 `BuildingController` 和 `ClassroomController` 的模式
- 数据库表依赖关系详见 `DatabaseInitProperties.initializeDefaultTables()`
- 初始化数据示例参考 `InitializeDatabase.java`
- 排课算法可以最后实现，先完成基础 CRUD

---

## 📅 更新日志

### 2026-02-04 - 阶段一完成 ✅

**已完成模块（3个）**:

1. ✅ **DepartmentController**（学院管理）
    - 实现完整的 CRUD 功能
    - 支持学院名称唯一性检查
    - 删除前检查专业引用（通过 MajorDAO.existsByDepartmentUuid）
    - 创建 AddDepartmentVO

2. ✅ **CourseTypeController**（课程类型管理）
    - 实现完整的 CRUD 功能
    - 支持类型名称唯一性检查（字段：typeName）
    - 创建 AddCourseTypeVO
    - 注意：DO 和 DTO 使用 typeName 字段，VO 使用 courseTypeName

3. ✅ **ClassroomTypeController**（教室类型管理）
    - 实现完整的 CRUD 功能
    - 支持类型名称唯一性检查（字段：typeName）
    - 创建 AddClassroomTypeVO
    - 注意：DO 和 DTO 使用 typeName 字段，VO 使用 classroomTypeName

**辅助增强**:

- ✅ **MajorDAO** - 添加 existsByDepartmentUuid() 和 countByDepartmentUuid() 方法支持学院删除检查

**字段命名规范**:

- DO 实体类：使用 `typeName`（映射数据库 `type_name`）
- DTO 类：使用 `typeName`（与 DO 保持一致）
- VO 类：使用 `classroomTypeName`/`courseTypeName`（与具体业务相关）
- Controller：从 VO 获取使用完整名称
- Service/DAO：与 DO/DTO 交互使用 `getTypeName()`/`setTypeName()`

**技术要点**:

- 统一使用 `LambdaQueryWrapper` 进行查询
- 权限控制：`@RequireRole` 注解
- 错误处理：`BusinessException` + `ErrorCode.OPERATION_FAILED`
- 日志记录：`@Slf4j` + `log.info()`
- UUID 生成：`UuidUtil.generateUuidNoDash()`

**下一步**: 阶段二 - 一级依赖数据（Major、Class、Teacher、Course、CourseQualification）